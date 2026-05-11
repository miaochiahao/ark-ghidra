package com.arkghidra.plugin;

import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.app.services.ProgramManager;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.listing.Program;
import ghidra.util.Msg;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.loader.HapMetadata;

/**
 * Main Ghidra plugin for ArkTS decompilation support.
 *
 * <p>Registers menu actions for decompiling Ark Bytecode methods to ArkTS
 * and for viewing the ABC file structure (classes, methods, fields).
 * Double-clicking a method node in the ABC Structure tree automatically
 * decompiles and displays the method without requiring a menu action.</p>
 */
@PluginInfo(
    status = PluginStatus.STABLE,
    packageName = "ArkTS",
    category = PluginCategoryNames.ANALYSIS,
    shortDescription = "ArkTS Decompiler",
    description = "Decompiles HarmonyOS ArkTS .abc bytecode to readable ArkTS source code."
)
public class ArkGhidraPlugin extends ProgramPlugin {

    public static final String PLUGIN_NAME = "ArkTS Decompiler";
    private static final String OWNER = ArkGhidraPlugin.class.getSimpleName();

    private AbcStructureProvider abcStructureProvider;
    private ArkTSOutputProvider outputProvider;
    private XrefProvider xrefProvider;
    private HapInfoProvider hapInfoProvider;

    public ArkGhidraPlugin(PluginTool tool) {
        super(tool);
        createProviders(tool);
        registerActions(tool);
        Msg.info(OWNER, "ArkTS Decompiler plugin initialized");
    }

    private void createProviders(PluginTool tool) {
        abcStructureProvider = new AbcStructureProvider(tool, PLUGIN_NAME);
        outputProvider = new ArkTSOutputProvider(tool, PLUGIN_NAME);
        xrefProvider = new XrefProvider(tool, PLUGIN_NAME);
        abcStructureProvider.setNavigationListener(this::onMethodDoubleClicked);
        abcStructureProvider.setClassNavigationListener(this::onClassClicked);
        outputProvider.setDecompileFileCallback(this::decompileWholeFile);
        outputProvider.setJumpToDefinitionCallback(this::jumpToDefinition);
        xrefProvider.setNavigationListener(offset -> outputProvider.scrollToOffset(offset));
        outputProvider.setSymbolHighlightCallback(word -> {
            if (word.isEmpty()) {
                xrefProvider.showXrefs("", "", outputProvider.getSymbolHighlighter());
            } else {
                xrefProvider.showXrefs(
                        word, outputProvider.getLastCode(), outputProvider.getSymbolHighlighter());
            }
            tool.showComponentProvider(xrefProvider, true);
        });
        tool.addComponentProvider(abcStructureProvider, false);
        tool.addComponentProvider(outputProvider, false);
        tool.addComponentProvider(xrefProvider, false);
        hapInfoProvider = new HapInfoProvider(tool, PLUGIN_NAME);
        hapInfoProvider.setAbilityClickCallback(this::jumpToAbility);
        tool.addComponentProvider(hapInfoProvider, false);
    }

    private void onMethodDoubleClicked(AbcMethod method) {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
                outputProvider.showMessage(
                        "// Could not read ABC data from program memory");
                return;
            }
            AbcFile abcFile = AbcFile.parse(abcData);
            AbcCode code = abcFile.getCodeForMethod(method);
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileMethod(method, code, abcFile);
            outputProvider.showDecompiledCode(method.getName(), result);
            tool.showComponentProvider(outputProvider, true);
            Msg.info(OWNER, "Decompiled method via tree: " + method.getName());
        } catch (Exception e) {
            Msg.error(OWNER, "Decompilation failed for " + method.getName(), e);
            outputProvider.showMessage(
                    "// Decompilation failed: " + e.getMessage());
        }
    }

    private void onClassClicked(AbcClass abcClass) {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        String className = AbcStructureProvider.formatClassName(abcClass.getName());
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
                outputProvider.showMessage(
                        "// Could not read ABC data from program memory");
                return;
            }
            AbcFile abcFile = AbcFile.parse(abcData);
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            StringBuilder sb = new StringBuilder();
            for (AbcMethod method : abcClass.getMethods()) {
                AbcCode code = abcFile.getCodeForMethod(method);
                sb.append(decompiler.decompileMethod(method, code, abcFile));
                sb.append("\n\n");
            }
            outputProvider.showDecompiledCode("Class: " + className, sb.toString());
            tool.showComponentProvider(outputProvider, true);
            Msg.info(OWNER, "Decompiled class via tree: " + className);
        } catch (Exception e) {
            Msg.error(OWNER, "Decompilation failed for class " + className, e);
            outputProvider.showMessage(
                    "// Decompilation failed: " + e.getMessage());
        }
    }

    private void decompileWholeFile() {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
                outputProvider.showMessage(
                        "// Could not read ABC data from program memory");
                return;
            }
            AbcFile abcFile = AbcFile.parse(abcData);
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            outputProvider.showDecompiledCode("File: " + program.getName(), result);
            tool.showComponentProvider(outputProvider, true);
            Msg.info(OWNER, "Decompiled whole file: " + program.getName());
        } catch (Exception e) {
            Msg.error(OWNER, "Whole-file decompilation failed", e);
            outputProvider.showMessage(
                    "// Decompilation failed: " + e.getMessage());
        }
    }

    private void registerActions(PluginTool tool) {
        tool.addAction(new DecompileToArkTSAction(this));
        tool.addAction(new ShowAbcStructureAction(this));
    }

    private void jumpToDefinition(String word) {
        AbcFile abcFile = getCurrentAbcFile();
        if (abcFile == null) {
            return;
        }
        for (AbcClass cls : abcFile.getClasses()) {
            String shortName = AbcStructureProvider.formatClassName(cls.getName());
            String simpleName = shortName.contains(".")
                    ? shortName.substring(shortName.lastIndexOf('.') + 1)
                    : shortName;
            if (simpleName.equals(word) || shortName.equals(word)) {
                onClassClicked(cls);
                abcStructureProvider.selectClass(cls);
                return;
            }
        }
    }

    private void jumpToAbility(String abilityName) {
        AbcFile abcFile = getCurrentAbcFile();
        if (abcFile == null) {
            return;
        }
        String lowerAbility = abilityName.toLowerCase();
        for (AbcClass cls : abcFile.getClasses()) {
            String shortName = AbcStructureProvider.formatClassName(cls.getName());
            String simpleName = shortName.contains(".")
                    ? shortName.substring(shortName.lastIndexOf('.') + 1)
                    : shortName;
            if (simpleName.toLowerCase().contains(lowerAbility)
                    || lowerAbility.contains(simpleName.toLowerCase())) {
                onClassClicked(cls);
                abcStructureProvider.selectClass(cls);
                return;
            }
        }
        outputProvider.showMessage("// Class not found for ability: " + abilityName);
    }

    private AbcFile getCurrentAbcFile() {
        Program program = getCurrentProgram();
        if (program == null) {
            return null;
        }
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
                return null;
            }
            return AbcFile.parse(abcData);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the current program from the ProgramManager service.
     *
     * @return the current program, or null if no program is open
     */
    @Override
    public Program getCurrentProgram() {
        ProgramManager pm = getTool().getService(ProgramManager.class);
        if (pm != null) {
            return pm.getCurrentProgram();
        }
        return null;
    }

    /**
     * Gets the ABC structure provider.
     *
     * @return the ABC structure provider
     */
    AbcStructureProvider getAbcStructureProvider() {
        return abcStructureProvider;
    }

    /**
     * Gets the ArkTS output provider.
     *
     * @return the output provider
     */
    ArkTSOutputProvider getOutputProvider() {
        return outputProvider;
    }

    /**
     * Gets the HAP info provider.
     *
     * @return the HAP info provider
     */
    public HapInfoProvider getHapInfoProvider() {
        return hapInfoProvider;
    }

    /**
     * Displays HAP metadata in the HAP Info panel and makes it visible.
     *
     * @param metadata the parsed HAP metadata, or null to clear the panel
     */
    public void showHapMetadata(HapMetadata metadata) {
        hapInfoProvider.showMetadata(metadata);
        tool.showComponentProvider(hapInfoProvider, true);
    }

    @Override
    public void cleanup() {
        PluginTool pluginTool = getTool();
        if (pluginTool != null) {
            if (abcStructureProvider != null) {
                pluginTool.removeComponentProvider(abcStructureProvider);
            }
            if (outputProvider != null) {
                pluginTool.removeComponentProvider(outputProvider);
            }
            if (xrefProvider != null) {
                pluginTool.removeComponentProvider(xrefProvider);
            }
            if (hapInfoProvider != null) {
                pluginTool.removeComponentProvider(hapInfoProvider);
            }
        }
    }
}
