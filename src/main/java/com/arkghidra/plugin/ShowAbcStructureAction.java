package com.arkghidra.plugin;

import docking.ActionContext;
import docking.action.DockingAction;
import docking.action.MenuData;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;

import com.arkghidra.format.AbcFile;

/**
 * Action that parses the ABC file loaded in the current program and
 * displays its class/method/field hierarchy in the ABC Structure viewer.
 *
 * <p>Available from the menu bar under Tools &rarr; ArkTS.</p>
 */
public class ShowAbcStructureAction extends DockingAction {

    static final String ACTION_NAME = "Show ABC Structure";
    private static final String MENU_GROUP = "ArkTS";
    private static final String OWNER = ShowAbcStructureAction.class.getSimpleName();

    private final ArkGhidraPlugin plugin;

    public ShowAbcStructureAction(ArkGhidraPlugin plugin) {
        super(ACTION_NAME, plugin.getName());
        this.plugin = plugin;
        setMenuBarData(new MenuData(
                new String[] { "Tools", "ArkTS", ACTION_NAME }, MENU_GROUP));
        setEnabled(true);
        setDescription("Show the ABC file class/method/field structure");
    }

    @Override
    public void actionPerformed(ActionContext context) {
        Program program = plugin.getCurrentProgram();
        if (program == null) {
            Msg.warn(OWNER, "No program is open");
            return;
        }

        try {
            byte[] abcData = readAbcData(program);
            if (abcData == null) {
                Msg.warn(OWNER, "No ABC data found in program memory");
                plugin.getAbcStructureProvider().showError(
                        "No ABC data found in program memory.");
                return;
            }

            AbcFile abcFile = AbcFile.parse(abcData);
            plugin.getAbcStructureProvider().setAbcFile(abcFile);

            plugin.getTool().showComponentProvider(
                    plugin.getAbcStructureProvider(), true);
            Msg.info(OWNER, "ABC structure displayed: "
                    + abcFile.getClasses().size() + " classes");
        } catch (Exception e) {
            Msg.error(OWNER, "Failed to parse ABC structure", e);
            plugin.getAbcStructureProvider().showError(
                    "Failed to parse ABC file: " + e.getMessage());
        }
    }

    private byte[] readAbcData(Program program) {
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

    @Override
    public boolean isEnabledForContext(ActionContext context) {
        return plugin.getCurrentProgram() != null;
    }
}
