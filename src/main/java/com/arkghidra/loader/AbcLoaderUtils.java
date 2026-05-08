package com.arkghidra.loader;

import ghidra.app.util.importer.MessageLog;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.Namespace;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.SymbolTable;
import ghidra.util.Msg;
import ghidra.util.task.TaskMonitor;

import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Shared utility methods for loading ABC files into Ghidra programs.
 * Used by both {@link AbcLoader} and {@link HapLoader}.
 */
public final class AbcLoaderUtils {

    private AbcLoaderUtils() {
    }

    /**
     * Creates an initialized memory block and fills it with ABC file bytes.
     *
     * @param memory the program memory
     * @param space the address space
     * @param blockName name for the memory block (e.g., "abc", "abc_0")
     * @param baseOffset base address offset for this block
     * @param fileBytes the raw ABC file bytes
     * @param monitor the task monitor
     * @param log the message log
     */
    public static void createMemoryBlock(Memory memory, AddressSpace space,
            String blockName, long baseOffset, byte[] fileBytes,
            TaskMonitor monitor, MessageLog log) {
        try {
            Address start = space.getAddress(baseOffset);
            MemoryBlock block = memory.createInitializedBlock(
                    blockName, start, fileBytes.length, (byte) 0, monitor, false);
            block.setRead(true);
            block.setWrite(false);
            block.setExecute(true);
            block.setSourceName("Ark Bytecode Loader");
            memory.setBytes(start, fileBytes);
            Msg.info(AbcLoaderUtils.class.getSimpleName(),
                    "Created memory block '" + blockName
                            + "' of size " + fileBytes.length
                            + " at offset 0x" + Long.toHexString(baseOffset));
        } catch (Exception e) {
            String msg = "Failed to create memory block '" + blockName
                    + "': " + e.getMessage();
            log.appendMsg(msg);
            Msg.error(AbcLoaderUtils.class.getSimpleName(), msg, e);
        }
    }

    /**
     * Creates class symbols and namespaces for all classes in an ABC file.
     *
     * @param program the Ghidra program
     * @param abc the parsed ABC file
     * @param space the address space
     * @param baseOffset offset to add to all addresses
     * @param monitor the task monitor
     * @param log the message log
     */
    public static void createClassSymbols(Program program, AbcFile abc,
            AddressSpace space, long baseOffset,
            TaskMonitor monitor, MessageLog log) {
        SymbolTable symbolTable = program.getSymbolTable();
        Namespace globalNs = program.getGlobalNamespace();
        String owner = AbcLoaderUtils.class.getSimpleName();

        monitor.setMessage("Creating class symbols...");
        monitor.initialize(abc.getClasses().size());

        for (int i = 0; i < abc.getClasses().size(); i++) {
            if (monitor.isCancelled()) {
                break;
            }
            AbcClass cls = abc.getClasses().get(i);
            try {
                String namespaceName = toNamespaceName(cls.getName());
                Namespace classNs = symbolTable.getOrCreateNameSpace(
                        globalNs, namespaceName, SourceType.IMPORTED);

                Address classAddr = space.getAddress(baseOffset + cls.getOffset());
                symbolTable.createLabel(classAddr, cls.getName(),
                        classNs, SourceType.IMPORTED);

                Msg.info(owner, "Created class symbol: " + cls.getName()
                        + " at " + classAddr);
            } catch (Exception e) {
                String msg = "Failed to create class symbol for "
                        + cls.getName() + ": " + e.getMessage();
                log.appendMsg(msg);
                Msg.warn(owner, msg);
            }
            monitor.setProgress(i + 1);
        }
    }

    /**
     * Creates Ghidra functions for all methods in an ABC file.
     *
     * @param program the Ghidra program
     * @param abc the parsed ABC file
     * @param space the address space
     * @param baseOffset offset to add to all addresses
     * @param monitor the task monitor
     * @param log the message log
     */
    public static void createMethodFunctions(Program program, AbcFile abc,
            AddressSpace space, long baseOffset,
            TaskMonitor monitor, MessageLog log) {
        FunctionManager funcMgr = program.getFunctionManager();
        SymbolTable symbolTable = program.getSymbolTable();
        Namespace globalNs = program.getGlobalNamespace();
        String owner = AbcLoaderUtils.class.getSimpleName();

        int totalMethods = 0;
        for (AbcClass cls : abc.getClasses()) {
            totalMethods += cls.getMethods().size();
        }

        monitor.setMessage("Creating method functions...");
        monitor.initialize(totalMethods);

        int processed = 0;
        for (AbcClass cls : abc.getClasses()) {
            if (monitor.isCancelled()) {
                break;
            }
            String nsName = toNamespaceName(cls.getName());
            Namespace classNs = symbolTable.getNamespace(nsName, globalNs);
            if (classNs == null) {
                classNs = globalNs;
            }

            for (AbcMethod method : cls.getMethods()) {
                if (monitor.isCancelled()) {
                    break;
                }
                processed++;

                if (method.getCodeOff() == 0) {
                    monitor.setProgress(processed);
                    continue;
                }

                AbcCode code = abc.getCodeForMethod(method);
                if (code == null) {
                    monitor.setProgress(processed);
                    continue;
                }

                try {
                    Address funcAddr = space.getAddress(
                            baseOffset + method.getCodeOff());

                    Function existing = funcMgr.getFunctionAt(funcAddr);
                    if (existing != null) {
                        monitor.setProgress(processed);
                        continue;
                    }

                    AddressSet body = new AddressSet(funcAddr,
                            funcAddr.add(code.getCodeSize() - 1));
                    Function func = funcMgr.createFunction(
                            method.getName(), classNs, funcAddr, body,
                            SourceType.IMPORTED);

                    func.setComment(buildMethodComment(
                            cls.getName(), method, code));

                    Address methodAddr = space.getAddress(
                            baseOffset + method.getOffset());
                    symbolTable.createLabel(methodAddr, method.getName(),
                            classNs, SourceType.IMPORTED);

                    Msg.info(owner, "Created function: " + cls.getName()
                            + "." + method.getName() + " at " + funcAddr);
                } catch (Exception e) {
                    String msg = "Failed to create function for "
                            + cls.getName() + "." + method.getName()
                            + ": " + e.getMessage();
                    log.appendMsg(msg);
                    Msg.warn(owner, msg);
                }
                monitor.setProgress(processed);
            }
        }
    }

    /**
     * Converts a class name like "Lcom/example/MyClass;" to
     * "com/example/MyClass" for use as a namespace.
     *
     * @param className the raw ABC class name
     * @return the sanitized namespace name
     */
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

    /**
     * Builds a comment string for a method function.
     *
     * @param className the class name
     * @param method the method
     * @param code the code section
     * @return the comment string
     */
    public static String buildMethodComment(String className,
            AbcMethod method, AbcCode code) {
        return className + "." + method.getName()
                + " (vregs=" + code.getNumVregs()
                + ", args=" + code.getNumArgs()
                + ", codeSize=" + code.getCodeSize() + ")";
    }
}
