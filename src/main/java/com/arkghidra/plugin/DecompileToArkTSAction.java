package com.arkghidra.plugin;

import docking.ActionContext;
import docking.action.DockingAction;
import docking.action.MenuData;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Action that decompiles the currently selected function to ArkTS source code.
 *
 * <p>Available from the menu bar under Tools &rarr; ArkTS and shows the result
 * in the ArkTS Output component provider.</p>
 */
public class DecompileToArkTSAction extends DockingAction {

    static final String ACTION_NAME = "Decompile to ArkTS";
    private static final String MENU_GROUP = "ArkTS";
    private static final String OWNER =
            DecompileToArkTSAction.class.getSimpleName();

    private final ArkGhidraPlugin plugin;

    public DecompileToArkTSAction(ArkGhidraPlugin plugin) {
        super(ACTION_NAME, plugin.getName());
        this.plugin = plugin;
        setMenuBarData(new MenuData(
                new String[] { "Tools", "ArkTS", ACTION_NAME }, MENU_GROUP));
        setEnabled(true);
        setDescription("Decompile the current function to ArkTS source code");
    }

    @Override
    public void actionPerformed(ActionContext context) {
        Program program = plugin.getCurrentProgram();
        if (program == null) {
            Msg.warn(OWNER, "No program is open");
            plugin.getOutputProvider().showMessage("No program is open.");
            return;
        }

        Function function = findCurrentFunction(program);
        if (function == null) {
            Msg.warn(OWNER, "No function at the current location");
            plugin.getOutputProvider().showMessage(
                    "No function at the current location. "
                            + "Position the cursor inside a function "
                            + "and try again.");
            return;
        }

        String result = decompileFunction(program, function);
        plugin.getOutputProvider().showDecompiledCode(
                function.getName(), result);
        Msg.info(OWNER, "Decompiled function: " + function.getName());
    }

    private Function findCurrentFunction(Program program) {
        FunctionManager funcMgr = program.getFunctionManager();

        if (funcMgr.getFunctions(true).hasNext()) {
            return funcMgr.getFunctions(true).next();
        }
        return null;
    }

    private String decompileFunction(Program program, Function function) {
        try {
            byte[] abcData = readAbcData(program);
            if (abcData == null) {
                return "// Could not read ABC data from program memory";
            }

            AbcFile abcFile = AbcFile.parse(abcData);
            ArkTSDecompiler decompiler = new ArkTSDecompiler();

            Address entry = function.getEntryPoint();
            long codeOff = entry.getOffset();

            AbcMethod abcMethod = findMethodAtOffset(abcFile, codeOff);
            if (abcMethod == null) {
                return "// No ABC method found at offset 0x"
                        + Long.toHexString(codeOff);
            }

            AbcCode code = abcFile.getCodeForMethod(abcMethod);
            return decompiler.decompileMethod(abcMethod, code, abcFile);
        } catch (Exception e) {
            Msg.error(OWNER, "Decompilation failed", e);
            return "// Decompilation failed: " + e.getMessage();
        }
    }

    static byte[] readAbcData(Program program) {
        if (program == null) {
            return null;
        }
        Memory memory = program.getMemory();
        MemoryBlock block = memory.getBlock("abc");
        if (block == null) {
            block = memory.getBlock(program.getAddressFactory()
                    .getDefaultAddressSpace().getAddress(0));
        }
        if (block == null) {
            return null;
        }
        byte[] data = new byte[(int) block.getSize()];
        try {
            block.getBytes(block.getStart(), data);
            return data;
        } catch (Exception e) {
            Msg.error(OWNER, "Failed to read memory block", e);
            return null;
        }
    }

    static AbcMethod findMethodAtOffset(AbcFile abcFile, long codeOff) {
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() == codeOff) {
                    return method;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEnabledForContext(ActionContext context) {
        return plugin.getCurrentProgram() != null;
    }
}
