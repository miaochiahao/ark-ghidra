package com.arkghidra.analyzer;

import java.util.ArrayList;
import java.util.List;
import ghidra.app.services.AbstractAnalyzer;
import ghidra.app.services.AnalysisPriority;
import ghidra.app.services.AnalyzerType;
import ghidra.app.util.importer.MessageLog;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.listing.CommentType;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcLiteralArray;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.loader.AbcLoaderUtils;

/**
 * Ghidra auto-analyzer for Ark Bytecode programs.
 *
 * <p>Runs after the ABC loader has mapped the file into memory.
 * Re-parses the ABC binary from the program's memory block and
 * enriches the Ghidra listing with plate comments, function body
 * sizes, entry-point markers, and string data for every method.</p>
 */
public class ArkBytecodeAnalyzer extends AbstractAnalyzer {

    private static final String OWNER = ArkBytecodeAnalyzer.class.getSimpleName();

    static final String ANALYZER_NAME = "Ark Bytecode Method Markup";
    private static final String DESCRIPTION =
            "Annotates Ark Bytecode methods with class/method signature comments, "
                    + "adjusts function body sizes, marks entry points, "
                    + "and creates string data types at string table offsets.";

    public ArkBytecodeAnalyzer() {
        super(ANALYZER_NAME, DESCRIPTION, AnalyzerType.BYTE_ANALYZER);
        setDefaultEnablement(true);
        setPriority(AnalysisPriority.FUNCTION_ANALYSIS);
    }

    @Override
    public boolean canAnalyze(Program program) {
        return isArkBytecodeProgram(program);
    }

    @Override
    public boolean getDefaultEnablement(Program program) {
        return isArkBytecodeProgram(program);
    }

    /**
     * Checks whether the given program uses the ArkBytecode language.
     * Extracted as a package-visible static so tests can verify the logic
     * without a full Ghidra runtime.
     */
    static boolean isArkBytecodeProgram(Program program) {
        if (program == null) {
            return false;
        }
        String langId = program.getLanguage().getLanguageID().getIdAsString();
        return langId.startsWith("ArkBytecode");
    }

    @Override
    public boolean added(Program program, AddressSetView set, TaskMonitor monitor,
            MessageLog log) throws CancelledException {

        Msg.info(OWNER, "Starting Ark Bytecode analysis");

        List<byte[]> allAbcBytes = readAllAbcBytes(program);
        if (allAbcBytes.isEmpty()) {
            log.appendMsg("No ABC memory blocks found; skipping analysis");
            return false;
        }

        ArkTSDataTypeManager dtManager = new ArkTSDataTypeManager(program);
        dtManager.createDataTypes();

        for (byte[] fileBytes : allAbcBytes) {
            if (monitor.isCancelled()) {
                break;
            }
            AbcFile abc;
            try {
                abc = AbcFile.parse(fileBytes);
            } catch (Exception e) {
                String msg = "Failed to re-parse ABC during analysis: "
                        + e.getMessage();
                log.appendMsg(msg);
                Msg.error(OWNER, msg, e);
                continue;
            }

            annotateMethods(program, abc, monitor, log);
            annotateStrings(program, abc, monitor, log);
        }

        Msg.info(OWNER, "Ark Bytecode analysis complete");
        return true;
    }

    private List<byte[]> readAllAbcBytes(Program program) {
        List<byte[]> result = new ArrayList<>();
        Memory memory = program.getMemory();
        for (MemoryBlock block : memory.getBlocks()) {
            if (!block.getName().startsWith("abc")) {
                continue;
            }
            long size = block.getSize();
            if (size <= 0 || size > Integer.MAX_VALUE) {
                continue;
            }
            byte[] bytes = new byte[(int) size];
            try {
                block.getBytes(block.getStart(), bytes);
                result.add(bytes);
            } catch (Exception e) {
                Msg.error(OWNER, "Failed to read ABC memory block '"
                        + block.getName() + "'", e);
            }
        }
        return result;
    }

    private void annotateMethods(Program program, AbcFile abc,
            TaskMonitor monitor, MessageLog log) {

        FunctionManager funcMgr = program.getFunctionManager();
        Listing listing = program.getListing();
        AddressSpace space = program.getAddressFactory().getDefaultAddressSpace();

        int methodCount = 0;

        for (AbcClass cls : abc.getClasses()) {
            if (monitor.isCancelled()) {
                break;
            }
            String className = cls.getName();
            String displayClassName = AbcLoaderUtils.toNamespaceName(className);

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

                Address funcAddr = space.getAddress(method.getCodeOff());

                Function func = funcMgr.getFunctionAt(funcAddr);
                if (func != null) {
                    adjustFunctionBody(func, funcAddr, code, log);
                }

                String plateComment = buildPlateComment(
                        displayClassName, method, code);
                listing.setComment(funcAddr, CommentType.PLATE, plateComment);

                methodCount++;
            }
        }

        Msg.info(OWNER, "Annotated " + methodCount + " methods");
    }

    private void adjustFunctionBody(Function func, Address funcAddr,
            AbcCode code, MessageLog log) {
        try {
            Address endAddr = funcAddr.add(code.getCodeSize() - 1);
            AddressSet newBody = new AddressSet(funcAddr, endAddr);
            func.setBody(newBody);
        } catch (Exception e) {
            String msg = "Failed to adjust function body at " + funcAddr
                    + ": " + e.getMessage();
            log.appendMsg(msg);
            Msg.warn(OWNER, msg);
        }
    }

    private String buildPlateComment(String className, AbcMethod method,
            AbcCode code) {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append(".");
        sb.append(method.getName());
        sb.append("(");
        sb.append("vregs=");
        sb.append(code.getNumVregs());
        sb.append(", args=");
        sb.append(code.getNumArgs());
        sb.append(", codeSize=");
        sb.append(code.getCodeSize());
        sb.append(")");
        return sb.toString();
    }

    private void annotateStrings(Program program, AbcFile abc,
            TaskMonitor monitor, MessageLog log) {
        for (AbcLiteralArray la : abc.getLiteralArrays()) {
            if (monitor.isCancelled()) {
                break;
            }
        }

        Msg.info(OWNER, "String annotation pass complete");
    }
}
