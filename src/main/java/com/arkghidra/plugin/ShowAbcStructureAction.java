package com.arkghidra.plugin;

import java.util.ArrayList;
import java.util.List;

import docking.ActionContext;
import docking.action.DockingAction;
import docking.action.MenuData;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;

import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcFile;
import com.arkghidra.loader.HapMetadata;
import com.arkghidra.loader.HapMetadataParser;

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

            loadHapMetadata(program, abcFile);
            plugin.showAbcStats(abcFile);
        } catch (Exception e) {
            Msg.error(OWNER, "Failed to parse ABC structure", e);
            plugin.getAbcStructureProvider().showError(
                    "Failed to parse ABC file: " + e.getMessage());
        }
    }

    private void loadHapMetadata(Program program, AbcFile abcFile) {
        try {
            HapMetadata metadata = tryReadModuleJsonBlock(program);
            if (metadata == null) {
                metadata = buildSyntheticMetadata(program, abcFile);
            }
            plugin.showHapMetadata(metadata);
            // Also update the HAP Explorer tree with abilities
            String hapName = program.getName();
            plugin.getAbcStructureProvider().setHapMetadata(metadata, hapName);
        } catch (Exception e) {
            Msg.warn(OWNER, "Failed to load HAP metadata: " + e.getMessage());
        }
    }

    private HapMetadata tryReadModuleJsonBlock(Program program) {
        Memory memory = program.getMemory();
        MemoryBlock block = memory.getBlock("module_json");
        if (block == null) {
            block = memory.getBlock("module.json");
        }
        if (block == null) {
            block = memory.getBlock("module.json5");
        }
        if (block != null) {
            try {
                byte[] bytes = new byte[(int) block.getSize()];
                block.getBytes(block.getStart(), bytes);
                HapMetadata parsed = HapMetadataParser.parse(bytes);
                if (parsed != null) {
                    return parsed;
                }
            } catch (Exception e) {
                Msg.warn(OWNER, "Failed to read module_json block: " + e.getMessage());
            }
        }
        return null;
    }

    private HapMetadata buildSyntheticMetadata(Program program, AbcFile abcFile) {
        List<HapMetadata.AbilityInfo> abilities = new ArrayList<>();
        for (AbcClass cls : abcFile.getClasses()) {
            String shortName = AbcStructureProvider.formatClassName(cls.getName());
            String simpleName = shortName.contains(".")
                    ? shortName.substring(shortName.lastIndexOf('.') + 1)
                    : shortName;
            if (simpleName.endsWith("Ability") || simpleName.endsWith("Page")
                    || simpleName.endsWith("Activity")) {
                abilities.add(new HapMetadata.AbilityInfo(simpleName, "", "page"));
            }
        }
        String moduleName = program.getName().replace(".hap", "").replace(".abc", "");
        String moduleNameFromComment = extractModuleNameFromComment(program);
        if (moduleNameFromComment != null && !moduleNameFromComment.isEmpty()) {
            moduleName = moduleNameFromComment;
        }
        return new HapMetadata(moduleName, "entry", "", 1, moduleName, "", abilities);
    }

    private String extractModuleNameFromComment(Program program) {
        try {
            String comment = program.getListing().getComment(
                    CodeUnit.PLATE_COMMENT,
                    program.getAddressFactory().getDefaultAddressSpace().getAddress(0));
            if (comment == null) {
                return null;
            }
            for (String line : comment.split("\n")) {
                if (line.startsWith("HAP Module: ")) {
                    return line.substring("HAP Module: ".length()).trim();
                }
            }
        } catch (Exception e) {
            Msg.warn(OWNER, "Failed to read plate comment: " + e.getMessage());
        }
        return null;
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
