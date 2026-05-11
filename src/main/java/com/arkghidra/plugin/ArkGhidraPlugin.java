package com.arkghidra.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

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
    private GlobalSearchProvider globalSearchProvider;
    private BookmarkProvider bookmarkProvider;
    private HistoryProvider historyProvider;

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
        abcStructureProvider.setCopyAsArkTSCallback(this::copyClassAsArkTS);
        outputProvider.setDecompileFileCallback(this::decompileWholeFile);
        outputProvider.setExportAllCallback(this::exportAllClasses);
        outputProvider.setJumpToDefinitionCallback(this::jumpToDefinition);
        outputProvider.setDecompileClassCallback(() -> {
            String clsName = outputProvider.getLastClassName();
            if (clsName.isEmpty()) {
                return;
            }
            AbcFile abcFile = getCurrentAbcFile();
            if (abcFile == null) {
                return;
            }
            for (AbcClass cls : abcFile.getClasses()) {
                String formatted = AbcStructureProvider.formatClassName(cls.getName());
                if (formatted.equals(clsName) || formatted.endsWith("." + clsName)) {
                    onClassClicked(cls);
                    return;
                }
            }
        });
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
        globalSearchProvider = new GlobalSearchProvider(tool, PLUGIN_NAME);
        globalSearchProvider.setNavigationCallback(this::navigateToSearchResult);
        globalSearchProvider.setSearchCallback(this::performGlobalSearch);
        tool.addComponentProvider(globalSearchProvider, false);
        outputProvider.setGlobalSearchCallback(() ->
                tool.showComponentProvider(globalSearchProvider, true));
        outputProvider.setGlobalSearchWordCallback(word -> {
            globalSearchProvider.clearResults();
            tool.showComponentProvider(globalSearchProvider, true);
            performGlobalSearch(word);
        });
        bookmarkProvider = new BookmarkProvider(tool, PLUGIN_NAME);
        bookmarkProvider.setNavigationCallback(this::navigateToSearchResult);
        tool.addComponentProvider(bookmarkProvider, false);
        outputProvider.setAddBookmarkCallback(this::addCurrentBookmark);
        historyProvider = new HistoryProvider(tool, PLUGIN_NAME);
        historyProvider.setRestoreCallback((name, code) -> outputProvider.showDecompiledCode(name, code));
        tool.addComponentProvider(historyProvider, false);
    }

    private void onMethodDoubleClicked(AbcMethod method) {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        outputProvider.showLoading("Decompiling...");
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
            String clsName = findClassForMethod(abcFile, method);
            outputProvider.showDecompiledCode(method.getName(), result, clsName);
            historyProvider.recordNavigation(method.getName(), result);
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
        } finally {
            outputProvider.hideLoading();
        }
    }

    private void onClassClicked(AbcClass abcClass) {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        String className = AbcStructureProvider.formatClassName(abcClass.getName());
        outputProvider.showLoading("Decompiling...");
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
            historyProvider.recordNavigation("Class: " + className, sb.toString());
            tool.showComponentProvider(outputProvider, true);
            Msg.info(OWNER, "Decompiled class via tree: " + className);
        } catch (Exception e) {
            Msg.error(OWNER, "Decompilation failed for class " + className, e);
            outputProvider.showMessage(
                    "// Decompilation failed: " + e.getMessage());
        } finally {
            outputProvider.hideLoading();
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

    private void copyClassAsArkTS(AbcClass abcClass) {
        Program program = getCurrentProgram();
        if (program == null) {
            return;
        }
        try {
            byte[] abcData = DecompileToArkTSAction.readAbcData(program);
            if (abcData == null) {
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
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(sb.toString()), null);
            String className = AbcStructureProvider.formatClassName(abcClass.getName());
            Msg.info(OWNER, "Copied class to clipboard: " + className);
        } catch (Exception e) {
            Msg.warn(OWNER, "Copy as ArkTS failed: " + e.getMessage());
        }
    }

    private void decompileWholeFile() {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        outputProvider.showLoading("Decompiling file...");
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
            historyProvider.recordNavigation("File: " + program.getName(), result);
            tool.showComponentProvider(outputProvider, true);
            Msg.info(OWNER, "Decompiled whole file: " + program.getName());
        } catch (Exception e) {
            Msg.error(OWNER, "Whole-file decompilation failed", e);
            outputProvider.showMessage(
                    "// Decompilation failed: " + e.getMessage());
        } finally {
            outputProvider.hideLoading();
        }
    }

    private void exportAllClasses() {
        Program program = getCurrentProgram();
        if (program == null) {
            outputProvider.showMessage("No program is open.");
            return;
        }
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Export All Classes \u2014 Choose Directory");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(null) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        File dir = chooser.getSelectedFile();
        if (!dir.exists() && !dir.mkdirs()) {
            Msg.error(OWNER, "Could not create directory: " + dir.getPath());
            return;
        }
        outputProvider.showLoading("Exporting...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    byte[] abcData = DecompileToArkTSAction.readAbcData(program);
                    if (abcData == null) {
                        return null;
                    }
                    AbcFile abcFile = AbcFile.parse(abcData);
                    ArkTSDecompiler decompiler = new ArkTSDecompiler();
                    int count = 0;
                    for (AbcClass cls : abcFile.getClasses()) {
                        try {
                            String className =
                                    AbcStructureProvider.formatClassName(cls.getName());
                            String simpleName = className.contains(".")
                                    ? className.substring(className.lastIndexOf('.') + 1)
                                    : className;
                            if (simpleName.isEmpty() || simpleName.equals("<unnamed>")) {
                                continue;
                            }
                            StringBuilder sb = new StringBuilder();
                            for (AbcMethod method : cls.getMethods()) {
                                try {
                                    AbcCode code = abcFile.getCodeForMethod(method);
                                    sb.append(decompiler.decompileMethod(
                                            method, code, abcFile));
                                    sb.append("\n\n");
                                } catch (Exception e) {
                                    // skip failed methods
                                }
                            }
                            File outFile = new File(dir, simpleName + ".ets");
                            try (BufferedWriter writer = new BufferedWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(outFile),
                                            StandardCharsets.UTF_8))) {
                                writer.write(sb.toString());
                            }
                            count++;
                        } catch (Exception e) {
                            // skip failed classes
                        }
                    }
                    final int finalCount = count;
                    javax.swing.SwingUtilities.invokeLater(() ->
                            Msg.info(OWNER, "Exported " + finalCount
                                    + " classes to " + dir.getPath()));
                } catch (Exception e) {
                    Msg.error(OWNER, "Export all failed: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                outputProvider.hideLoading();
            }
        }.execute();
    }

    private void performGlobalSearch(String query) {
        Program program = getCurrentProgram();
        if (program == null) {
            globalSearchProvider.finishSearch(query);
            return;
        }
        byte[] abcData = DecompileToArkTSAction.readAbcData(program);
        if (abcData == null) {
            globalSearchProvider.finishSearch(query);
            return;
        }
        tool.showComponentProvider(globalSearchProvider, true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    AbcFile freshFile = AbcFile.parse(abcData);
                    ArkTSDecompiler decompiler = new ArkTSDecompiler();
                    String lowerQuery = query.toLowerCase();
                    for (AbcClass cls : freshFile.getClasses()) {
                        String className =
                                AbcStructureProvider.formatClassName(cls.getName());
                        String simpleName = className.contains(".")
                                ? className.substring(className.lastIndexOf('.') + 1)
                                : className;
                        for (AbcMethod method : cls.getMethods()) {
                            try {
                                AbcCode code = freshFile.getCodeForMethod(method);
                                String decompiled = decompiler.decompileMethod(
                                        method, code, freshFile);
                                String[] lines = decompiled.split("\n", -1);
                                for (int i = 0; i < lines.length; i++) {
                                    if (lines[i].toLowerCase().contains(lowerQuery)) {
                                        final String cls2 = simpleName;
                                        final String mth = method.getName();
                                        final String line = lines[i];
                                        final int lineNum = i + 1;
                                        javax.swing.SwingUtilities.invokeLater(() ->
                                                globalSearchProvider.addResult(
                                                        cls2, mth, line, lineNum));
                                    }
                                }
                            } catch (Exception e) {
                                // skip methods that fail to decompile
                            }
                        }
                    }
                } catch (Exception e) {
                    Msg.warn(OWNER, "Global search failed: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                globalSearchProvider.finishSearch(query);
            }
        }.execute();
    }

    private void navigateToSearchResult(String[] classAndMethod) {
        if (classAndMethod == null || classAndMethod.length < 2) {
            return;
        }
        String targetClass = classAndMethod[0];
        String targetMethod = classAndMethod[1];
        AbcFile abcFile = getCurrentAbcFile();
        if (abcFile == null) {
            return;
        }
        for (AbcClass cls : abcFile.getClasses()) {
            String simpleName = AbcStructureProvider.formatClassName(cls.getName());
            if (simpleName.contains(".")) {
                simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
            }
            if (simpleName.equals(targetClass)) {
                for (AbcMethod method : cls.getMethods()) {
                    if (method.getName().equals(targetMethod)) {
                        onMethodDoubleClicked(method);
                        return;
                    }
                }
            }
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
        if (!outputProvider.isAutoDecompileEnabled()) {
            return;
        }
        outputProvider.showLoading("Decompiling...");
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
            String clsName = findClassForMethod(abcFile, abcMethod);
            outputProvider.showDecompiledCode(abcMethod.getName(), result, clsName);
            historyProvider.recordNavigation(abcMethod.getName(), result);
            List<String> allMethodNames = getAllMethodNames(abcFile);
            callGraphProvider.showCallGraph(abcMethod.getName(), result, allMethodNames);
            Msg.info(OWNER, "Auto-decompiled on location change: " + abcMethod.getName());
        } catch (Exception e) {
            Msg.warn(OWNER, "Auto-decompile on location change failed: " + e.getMessage());
        } finally {
            outputProvider.hideLoading();
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

    private String findClassForMethod(AbcFile abcFile, AbcMethod method) {
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod m : cls.getMethods()) {
                if (m.getOffset() == method.getOffset()) {
                    return AbcStructureProvider.formatClassName(cls.getName());
                }
            }
        }
        return "";
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

    /**
     * Bookmarks the currently displayed method and makes the Bookmarks panel visible.
     */
    public void addCurrentBookmark() {
        String funcName = outputProvider.getLastFunctionName();
        if (funcName == null || funcName.isEmpty()) {
            return;
        }
        String className = "";
        String methodName = funcName;
        if (funcName.startsWith("Class: ")) {
            className = funcName.substring(7);
            methodName = className;
        }
        bookmarkProvider.addBookmark(className, methodName);
        tool.showComponentProvider(bookmarkProvider, true);
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
            if (globalSearchProvider != null) {
                pluginTool.removeComponentProvider(globalSearchProvider);
            }
            if (bookmarkProvider != null) {
                pluginTool.removeComponentProvider(bookmarkProvider);
            }
            if (historyProvider != null) {
                pluginTool.removeComponentProvider(historyProvider);
            }
        }
    }
}
