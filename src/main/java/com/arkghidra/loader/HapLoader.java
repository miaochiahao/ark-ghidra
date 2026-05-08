package com.arkghidra.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ghidra.app.util.Option;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractProgramWrapperLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.app.util.opinion.Loader;
import ghidra.app.util.opinion.LoaderTier;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.CommentType;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.util.Msg;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

import com.arkghidra.format.AbcFile;

/**
 * Ghidra loader for HarmonyOS HAP (HarmonyOS Ability Package) files.
 *
 * <p>A HAP file is a ZIP archive containing one or more .abc (Ark
 * Bytecode) files and a module.json manifest. This loader extracts
 * each .abc entry, parses it, and maps it into a Ghidra program at
 * spaced address offsets so that all modules are visible in the
 * same program.</p>
 */
public class HapLoader extends AbstractProgramWrapperLoader {

    private static final String OWNER = HapLoader.class.getSimpleName();

    /** ZIP local file header magic (PK\x03\x04). */
    static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    /** Address spacing between consecutive ABC blocks. */
    static final long ABC_BLOCK_SPACING = 0x100000L;

    static final String LANG_ID = "ArkBytecode:LE:32:default";
    static final String COMPILER_ID = "default";

    @Override
    public String getName() {
        return "HarmonyOS HAP Loader";
    }

    @Override
    public LoaderTier getTier() {
        return LoaderTier.SPECIALIZED_TARGET_LOADER;
    }

    @Override
    public int getTierPriority() {
        return 1;
    }

    @Override
    public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider)
            throws IOException {
        List<LoadSpec> specs = new ArrayList<>();
        if (isHapFile(provider)) {
            specs.add(new LoadSpec(this, 0,
                    new LanguageCompilerSpecPair(LANG_ID, COMPILER_ID),
                    true));
        }
        return specs;
    }

    @Override
    public List<Option> getDefaultOptions(ByteProvider provider,
            LoadSpec loadSpec, DomainObject domainObject,
            boolean isLoadIntoProgram, boolean isFresh) {
        return super.getDefaultOptions(provider, loadSpec, domainObject,
                isLoadIntoProgram, isFresh);
    }

    @Override
    public String validateOptions(ByteProvider provider, LoadSpec loadSpec,
            List<Option> options, Program program) {
        return super.validateOptions(provider, loadSpec, options, program);
    }

    @Override
    protected void load(Program program,
            Loader.ImporterSettings settings)
            throws IOException, CancelledException {

        TaskMonitor monitor = settings.monitor();
        MessageLog log = settings.log();
        ByteProvider provider = settings.provider();

        monitor.setMessage("Scanning HAP file for entries...");

        byte[] hapBytes = provider.readBytes(0, provider.length());

        List<AbcEntry> abcEntries = extractAbcEntries(hapBytes);
        if (abcEntries.isEmpty()) {
            String msg = "HAP file contains no .abc entries";
            log.appendMsg(msg);
            throw new IOException(msg);
        }

        abcEntries.sort((a, b) -> a.path.compareTo(b.path));

        AddressSpace space =
                program.getAddressFactory().getDefaultAddressSpace();
        Memory memory = program.getMemory();

        monitor.setMessage("Loading " + abcEntries.size()
                + " ABC entries...");
        monitor.initialize(abcEntries.size());

        for (int i = 0; i < abcEntries.size(); i++) {
            if (monitor.isCancelled()) {
                return;
            }
            AbcEntry entry = abcEntries.get(i);
            long baseOffset = (long) i * ABC_BLOCK_SPACING;
            String blockName = sanitizeBlockName(entry.path, i);

            AbcLoaderUtils.createMemoryBlock(memory, space, blockName,
                    baseOffset, entry.bytes, monitor, log);

            AbcFile abc;
            try {
                abc = AbcFile.parse(entry.bytes);
            } catch (Exception e) {
                String msg = "Failed to parse ABC entry '"
                        + entry.path + "': " + e.getMessage();
                log.appendMsg(msg);
                Msg.warn(OWNER, msg);
                monitor.setProgress(i + 1);
                continue;
            }

            AbcLoaderUtils.createClassSymbols(program, abc, space,
                    baseOffset, monitor, log);
            AbcLoaderUtils.createMethodFunctions(program, abc, space,
                    baseOffset, monitor, log);

            Msg.info(OWNER, "Loaded ABC entry: " + entry.path
                    + " at offset 0x"
                    + Long.toHexString(baseOffset));

            monitor.setProgress(i + 1);
        }

        byte[] moduleJson = extractModuleJson(hapBytes);
        if (moduleJson != null) {
            addMetadataComment(program, space, moduleJson, log);
        }
    }

    /**
     * Checks whether the given byte provider represents a HAP file
     * (ZIP archive containing at least one .abc entry).
     */
    boolean isHapFile(ByteProvider provider) throws IOException {
        if (provider.length() < ZIP_MAGIC.length) {
            return false;
        }
        byte[] magic = provider.readBytes(0, ZIP_MAGIC.length);
        for (int i = 0; i < ZIP_MAGIC.length; i++) {
            if (magic[i] != ZIP_MAGIC[i]) {
                return false;
            }
        }
        return containsAbcEntries(provider);
    }

    private boolean containsAbcEntries(ByteProvider provider)
            throws IOException {
        try (java.io.InputStream is = provider.getInputStream(0)) {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name != null
                        && name.toLowerCase().endsWith(".abc")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts all .abc entries from a ZIP byte array, sorted by path
     * for deterministic ordering.
     */
    static List<AbcEntry> extractAbcEntries(byte[] zipBytes)
            throws IOException {
        List<AbcEntry> entries = new ArrayList<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
        ZipInputStream zis = new ZipInputStream(bais);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name != null && name.toLowerCase().endsWith(".abc")) {
                byte[] content = readZipEntry(zis);
                entries.add(new AbcEntry(name, content));
            }
            zis.closeEntry();
        }
        zis.close();
        return entries;
    }

    /**
     * Extracts the module.json entry from a HAP ZIP.
     * Checks common paths: module.json, module.json5,
     * and nested variants like ets/module.json.
     */
    static byte[] extractModuleJson(byte[] zipBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
        ZipInputStream zis = new ZipInputStream(bais);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name != null && isModuleJson(name)) {
                byte[] content = readZipEntry(zis);
                zis.close();
                return content;
            }
            zis.closeEntry();
        }
        zis.close();
        return null;
    }

    private static boolean isModuleJson(String name) {
        String lower = name.toLowerCase();
        return lower.equals("module.json")
                || lower.equals("module.json5")
                || lower.endsWith("/module.json")
                || lower.endsWith("/module.json5");
    }

    private static byte[] readZipEntry(ZipInputStream zis)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = zis.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    private void addMetadataComment(Program program, AddressSpace space,
            byte[] moduleJsonBytes, MessageLog log) {
        HapMetadata metadata = HapMetadataParser.parse(moduleJsonBytes);
        if (metadata == null) {
            log.appendMsg("Failed to parse module.json metadata");
            return;
        }
        try {
            Address addr = space.getAddress(0);
            program.getListing().setComment(addr, CommentType.PLATE,
                    metadata.toString());
            Msg.info(OWNER, "Added HAP metadata plate comment at "
                    + addr);
        } catch (Exception e) {
            String msg = "Failed to add metadata comment: "
                    + e.getMessage();
            log.appendMsg(msg);
            Msg.warn(OWNER, msg);
        }
    }

    /**
     * Sanitizes a ZIP entry path into a valid Ghidra memory block name.
     * Replaces path separators with underscores and prefixes with
     * an index.
     *
     * @param zipPath the original ZIP entry path
     * @param index the entry index for unique naming
     * @return a sanitized block name
     */
    static String sanitizeBlockName(String zipPath, int index) {
        String name = zipPath.replace('/', '_').replace('\\', '_');
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        return "abc_" + index + "_" + name;
    }

    /**
     * Internal record for a ZIP entry with its path and content.
     */
    static class AbcEntry {
        final String path;
        final byte[] bytes;

        AbcEntry(String path, byte[] bytes) {
            this.path = path;
            this.bytes = bytes;
        }
    }
}
