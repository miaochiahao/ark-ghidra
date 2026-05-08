package com.arkghidra.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ghidra.app.util.Option;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractProgramWrapperLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.app.util.opinion.Loader;
import ghidra.app.util.opinion.LoaderTier;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.util.Msg;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

import com.arkghidra.format.AbcFile;

/**
 * Ghidra loader for Ark Bytecode (.abc) files produced by the
 * HarmonyOS / OpenHarmony Ark compiler.
 *
 * <p>The loader maps the entire ABC file into a single memory block,
 * creates a namespace and symbol for each class, and creates a Ghidra
 * function at every method's code offset so that the decompiler can
 * process it.</p>
 */
public class AbcLoader extends AbstractProgramWrapperLoader {

    private static final String OWNER = AbcLoader.class.getSimpleName();

    static final byte[] ABC_MAGIC = new byte[] {
        'P', 'A', 'N', 'D', 'A', 0, 0, 0
    };
    static final String LANG_ID = "ArkBytecode:LE:32:default";
    static final String COMPILER_ID = "default";

    @Override
    public String getName() {
        return "Ark Bytecode Loader";
    }

    @Override
    public LoaderTier getTier() {
        return LoaderTier.SPECIALIZED_TARGET_LOADER;
    }

    @Override
    public int getTierPriority() {
        return 0;
    }

    @Override
    public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider)
            throws IOException {
        List<LoadSpec> specs = new ArrayList<>();
        if (isAbcFile(provider)) {
            specs.add(new LoadSpec(this, 0,
                    new LanguageCompilerSpecPair(LANG_ID, COMPILER_ID), true));
        }
        return specs;
    }

    @Override
    public List<Option> getDefaultOptions(ByteProvider provider, LoadSpec loadSpec,
            DomainObject domainObject, boolean isLoadIntoProgram, boolean isFresh) {
        return super.getDefaultOptions(provider, loadSpec, domainObject,
                isLoadIntoProgram, isFresh);
    }

    @Override
    public String validateOptions(ByteProvider provider, LoadSpec loadSpec,
            List<Option> options, Program program) {
        return super.validateOptions(provider, loadSpec, options, program);
    }

    @Override
    protected void load(Program program, Loader.ImporterSettings settings)
            throws IOException, CancelledException {

        TaskMonitor monitor = settings.monitor();
        MessageLog log = settings.log();
        ByteProvider provider = settings.provider();

        byte[] fileBytes = provider.readBytes(0, provider.length());
        AbcFile abc;
        try {
            abc = AbcFile.parse(fileBytes);
        } catch (Exception e) {
            String msg = "Failed to parse ABC file: " + e.getMessage();
            log.appendMsg(msg);
            Msg.error(OWNER, msg, e);
            throw new IOException(msg, e);
        }

        AddressSpace space = program.getAddressFactory().getDefaultAddressSpace();
        Memory memory = program.getMemory();

        AbcLoaderUtils.createMemoryBlock(memory, space, "abc", 0,
                fileBytes, monitor, log);
        AbcLoaderUtils.createClassSymbols(program, abc, space, 0, monitor, log);
        AbcLoaderUtils.createMethodFunctions(program, abc, space, 0,
                monitor, log);
    }

    private boolean isAbcFile(ByteProvider provider) throws IOException {
        if (provider.length() < ABC_MAGIC.length) {
            return false;
        }
        byte[] magic = provider.readBytes(0, ABC_MAGIC.length);
        for (int i = 0; i < ABC_MAGIC.length; i++) {
            if (magic[i] != ABC_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }
}
