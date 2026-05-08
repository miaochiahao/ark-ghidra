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
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.Namespace;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.SymbolTable;
import ghidra.util.Msg;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

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

        createMemoryBlock(memory, space, fileBytes, monitor, log);
        createClassSymbols(program, abc, space, monitor, log);
        createMethodFunctions(program, abc, space, monitor, log);
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

    private void createMemoryBlock(Memory memory, AddressSpace space,
            byte[] fileBytes, TaskMonitor monitor, MessageLog log) {
        try {
            Address start = space.getAddress(0);
            MemoryBlock block = memory.createInitializedBlock(
                    "abc", start, fileBytes.length, (byte) 0, monitor, false);
            block.setRead(true);
            block.setWrite(false);
            block.setExecute(true);
            block.setSourceName("Ark Bytecode Loader");
            memory.setBytes(start, fileBytes);
            Msg.info(OWNER, "Created memory block 'abc' of size " + fileBytes.length);
        } catch (Exception e) {
            String msg = "Failed to create memory block: " + e.getMessage();
            log.appendMsg(msg);
            Msg.error(OWNER, msg, e);
        }
    }

    private void createClassSymbols(Program program, AbcFile abc,
            AddressSpace space, TaskMonitor monitor, MessageLog log) {
        SymbolTable symbolTable = program.getSymbolTable();
        Namespace globalNs = program.getGlobalNamespace();

        for (AbcClass cls : abc.getClasses()) {
            if (monitor.isCancelled()) {
                break;
            }
            try {
                String namespaceName = toNamespaceName(cls.getName());
                Namespace classNs = symbolTable.getOrCreateNameSpace(
                        globalNs, namespaceName, SourceType.IMPORTED);

                Address classAddr = space.getAddress(cls.getOffset());
                symbolTable.createLabel(classAddr, cls.getName(),
                        classNs, SourceType.IMPORTED);

                Msg.info(OWNER, "Created class symbol: " + cls.getName()
                        + " at " + classAddr);
            } catch (Exception e) {
                String msg = "Failed to create class symbol for "
                        + cls.getName() + ": " + e.getMessage();
                log.appendMsg(msg);
                Msg.warn(OWNER, msg);
            }
        }
    }

    private void createMethodFunctions(Program program, AbcFile abc,
            AddressSpace space, TaskMonitor monitor, MessageLog log) {
        FunctionManager funcMgr = program.getFunctionManager();
        SymbolTable symbolTable = program.getSymbolTable();

        for (AbcClass cls : abc.getClasses()) {
            if (monitor.isCancelled()) {
                break;
            }
            Namespace classNs = findClassNamespace(program, cls.getName());

            for (AbcMethod method : cls.getMethods()) {
                if (monitor.isCancelled()) {
                    break;
                }
                if (method.getCodeOff() == 0) {
                    continue;
                }

                AbcCode code = abc.getCodeForMethod(method);
                if (code == null) {
                    continue;
                }

                try {
                    Address funcAddr = space.getAddress(method.getCodeOff());

                    Function existing = funcMgr.getFunctionAt(funcAddr);
                    if (existing != null) {
                        continue;
                    }

                    AddressSet body = new AddressSet(funcAddr,
                            funcAddr.add(code.getCodeSize() - 1));
                    Function func = funcMgr.createFunction(
                            method.getName(), classNs, funcAddr, body,
                            SourceType.IMPORTED);

                    String comment = buildMethodComment(cls.getName(), method, code);
                    func.setComment(comment);

                    Address methodAddr = space.getAddress(method.getOffset());
                    symbolTable.createLabel(methodAddr, method.getName(),
                            classNs, SourceType.IMPORTED);

                    Msg.info(OWNER, "Created function: " + cls.getName()
                            + "." + method.getName() + " at " + funcAddr);
                } catch (Exception e) {
                    String msg = "Failed to create function for "
                            + cls.getName() + "." + method.getName()
                            + ": " + e.getMessage();
                    log.appendMsg(msg);
                    Msg.warn(OWNER, msg);
                }
            }
        }
    }

    private Namespace findClassNamespace(Program program, String className) {
        SymbolTable symbolTable = program.getSymbolTable();
        Namespace globalNs = program.getGlobalNamespace();
        String nsName = toNamespaceName(className);
        Namespace ns = symbolTable.getNamespace(nsName, globalNs);
        return ns != null ? ns : globalNs;
    }

    public static String toNamespaceName(String className) {
        if (className == null || className.isEmpty()) {
            return "unknown";
        }
        String name = className;
        if (name.startsWith("L")) {
            name = name.substring(1);
        }
        if (name.endsWith(";")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private String buildMethodComment(String className, AbcMethod method,
            AbcCode code) {
        return className + "." + method.getName()
                + " (vregs=" + code.getNumVregs()
                + ", args=" + code.getNumArgs()
                + ", codeSize=" + code.getCodeSize() + ")";
    }
}
