package com.arkghidra.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.app.services.GoToService;
import ghidra.app.services.ProgramManager;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Program;
import ghidra.program.util.FunctionSignatureFieldLocation;
import ghidra.program.util.ProgramLocation;
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
    private CallGraphProvider callGraphProvider;

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
        abcStructureProvider.setExportClassCallback(this::exportClassToFile);
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
        callGraphProvider = new CallGraphProvider(tool, PLUGIN_NAME);
        callGraphProvider.setNavigationCallback(this::jumpToDefinition);
        tool.addComponentProvider(callGraphProvider, false);
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
            List<String> allMethodNames = getAllMethodNames(abcFile);
            callGraphProvider.showCallGraph(method.getName(), result, allMethodNames);
            tool.showComponentProvider(callGraphProvider, true);
            // Navigate Listing to the method's address
            if (method.getCodeOff() > 0) {
                Address addr = program.getAddressFactory()
                        .getDefaultAddressSpace().getAddress(method.getCodeOff());
                goToAddress(addr);
            }
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

    private void exportClassToFile(AbcClass abcClass, File file) {
        Program program = getCurrentProgram();
        if (program == null) {
            Msg.warn(OWNER, "No program open for export");
            return;
        }
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
                Msg.warn(OWNER, "Could not read ABC data for export");
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
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(sb.toString());
            }
            Msg.info(OWNER, "Exported class to " + file.getPath());
        } catch (IOException e) {
            Msg.error(OWNER, "Export failed: " + e.getMessage(), e);
        } catch (Exception e) {
            Msg.error(OWNER, "Export decompilation failed: " + e.getMessage(), e);
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

    /**
     * Called by Ghidra when the cursor moves to a new location in the Listing.
     * Automatically decompiles the function at the new location.
     */
    @Override
    protected void locationChanged(ProgramLocation loc) {
        if (loc == null || currentProgram == null) {
            return;
        }
        // Only auto-decompile when the output panel is visible
        if (!outputProvider.isVisible()) {
            return;
        }
        try {
            FunctionManager funcMgr = currentProgram.getFunctionManager();
            Function function = funcMgr.getFunctionContaining(loc.getAddress());
            if (function == null) {
                return;
            }
            // Avoid re-decompiling the same function
            String funcName = function.getName();
            if (funcName.equals(outputProvider.getLastFunctionName())) {
                return;
            }
            byte[] abcData = DecompileToArkTSAction.readAbcData(currentProgram);
            if (abcData == null) {
                return;
            }
            AbcFile abcFile = AbcFile.parse(abcData);
            long codeOff = function.getEntryPoint().getOffset();
            AbcMethod abcMethod = DecompileToArkTSAction.findMethodAtOffset(abcFile, codeOff);
            if (abcMethod == null) {
                return;
            }
            AbcCode code = abcFile.getCodeForMethod(abcMethod);
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileMethod(abcMethod, code, abcFile);
            outputProvider.showDecompiledCode(abcMethod.getName(), result);
            List<String> allMethodNames = getAllMethodNames(abcFile);
            callGraphProvider.showCallGraph(abcMethod.getName(), result, allMethodNames);
            Msg.info(OWNER, "Auto-decompiled on location change: " + abcMethod.getName());
        } catch (Exception e) {
            Msg.warn(OWNER, "Auto-decompile on location change failed: " + e.getMessage());
        }
    }

    /**
     * Navigates the Listing view to the given address using GoToService.
     */
    void goToAddress(Address address) {
        if (address == null || currentProgram == null) {
            return;
        }
        GoToService goToService = tool.getService(GoToService.class);
        if (goToService != null) {
            goToService.goTo(new FunctionSignatureFieldLocation(
                    currentProgram, address));
        }
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

    private List<String> getAllMethodNames(AbcFile abcFile) {
        List<String> names = new ArrayList<>();
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod m : cls.getMethods()) {
                names.add(m.getName());
            }
        }
        return names;
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
            if (callGraphProvider != null) {
                pluginTool.removeComponentProvider(callGraphProvider);
            }
        }
    }
}
