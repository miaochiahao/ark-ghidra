package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import docking.ComponentProvider;
import docking.Tool;
import ghidra.util.Msg;

import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcField;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.loader.HapMetadata;

/**
 * Component provider that displays the ABC file structure as a tree.
 *
 * <p>The tree shows:</p>
 * <ul>
 *   <li>Root: ABC File</li>
 *   <li>Classes (grouped by namespace)</li>
 *   <li>Methods under each class</li>
 *   <li>Fields under each class</li>
 * </ul>
 */
public class AbcStructureProvider extends ComponentProvider {

    private static final String OWNER =
            AbcStructureProvider.class.getSimpleName();

    private static final String FILTER_PLACEHOLDER = "Filter... (args:N, size:>N, name:X, class:X)";

    private final JPanel mainPanel;
    private final JTree structureTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final JTextField filterField;
    private final JLabel filterCountLabel;
    private final JLabel breadcrumbLabel;
    private final JLabel statsLabel;
    private AbcFile currentAbcFile;
    private HapMetadata currentHapMetadata;
    private String currentHapName = "";
    private String moduleJsonContent = "";
    private MethodNavigationListener navigationListener;
    private ClassNavigationListener classNavigationListener;
    private BiConsumer<AbcClass, File> exportClassCallback;
    private java.util.function.Consumer<AbcClass> copyAsArkTSCallback;
    private Consumer<String> showCallersCallback;
    private Consumer<AbcClass> showImplementationsCallback;
    private Runnable exportReportCallback;
    private Runnable decompileAllAbilitiesCallback;
    private Runnable refreshCallback;
    private NotesProvider notesProvider;
    private SettingsProvider settingsProvider;
    private JToggleButton filterPublicButton;
    private JToggleButton filterPrivateButton;
    private JToggleButton filterStaticButton;
    private JToggleButton filterAbstractButton;
    private JToggleButton filterHasNotesButton;
    private JToggleButton sortBySizeButton;
    private JTextField minSizeField;
    private String classTypeFilter = "All";

    public AbcStructureProvider(Tool tool, String owner) {
        super(tool, "HAP Explorer", owner);
        rootNode = new DefaultMutableTreeNode("HAP (not loaded)");
        treeModel = new DefaultTreeModel(rootNode);
        structureTree = new JTree(treeModel);
        structureTree.setRootVisible(true);
        structureTree.setShowsRootHandles(true);
        structureTree.setFont(new Font("Monospaced", Font.PLAIN, 12));
        structureTree.setCellRenderer(new MethodComplexityCellRenderer());
        javax.swing.ToolTipManager.sharedInstance().registerComponent(structureTree);
        structureTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTreeContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTreeContextMenu(e);
                }
            }
        });

        // Enter key triggers decompile on the selected node
        structureTree.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activateNode");
        structureTree.getActionMap().put("activateNode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDoubleClick();
            }
        });

        filterField = new JTextField();
        filterField.setForeground(Color.GRAY);
        filterField.setText(FILTER_PLACEHOLDER);
        filterField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (FILTER_PLACEHOLDER.equals(filterField.getText())) {
                    filterField.setText("");
                    filterField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (filterField.getText().isEmpty()) {
                    filterField.setForeground(Color.GRAY);
                    filterField.setText(FILTER_PLACEHOLDER);
                }
            }
        });
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuildTree();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuildTree();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuildTree();
            }
        });

        filterCountLabel = new JLabel("");
        filterCountLabel.setForeground(Color.GRAY);
        filterCountLabel.setFont(filterCountLabel.getFont().deriveFont(11f));

        breadcrumbLabel = new JLabel("");
        breadcrumbLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        breadcrumbLabel.setForeground(Color.GRAY);
        breadcrumbLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        statsLabel = new JLabel("  ");
        statsLabel.setForeground(Color.GRAY);
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        filterPublicButton = new JToggleButton("Pub");
        filterPublicButton.setToolTipText("Show only public methods");
        filterPublicButton.addActionListener(e -> rebuildTree());

        filterPrivateButton = new JToggleButton("Priv");
        filterPrivateButton.setToolTipText("Show only private methods");
        filterPrivateButton.addActionListener(e -> rebuildTree());

        filterStaticButton = new JToggleButton("Static");
        filterStaticButton.setToolTipText("Show only static methods");
        filterStaticButton.addActionListener(e -> rebuildTree());

        filterAbstractButton = new JToggleButton("Abs");
        filterAbstractButton.setToolTipText("Show only abstract/native methods");
        filterAbstractButton.addActionListener(e -> rebuildTree());

        filterHasNotesButton = new JToggleButton("★");
        filterHasNotesButton.setToolTipText("Show only methods with notes");
        filterHasNotesButton.addActionListener(e -> rebuildTree());

        JPanel modifierFilterPanel = new JPanel();
        modifierFilterPanel.add(filterPublicButton);
        modifierFilterPanel.add(filterPrivateButton);
        modifierFilterPanel.add(filterStaticButton);
        modifierFilterPanel.add(filterAbstractButton);
        modifierFilterPanel.add(filterHasNotesButton);

        sortBySizeButton = new JToggleButton("Sort↓");
        sortBySizeButton.setToolTipText("Sort methods by bytecode size (largest first)");
        sortBySizeButton.addActionListener(e -> rebuildTree());
        modifierFilterPanel.add(sortBySizeButton);

        minSizeField = new JTextField("0", 4);
        minSizeField.setToolTipText("Hide methods with fewer bytes than this (0 = show all)");
        minSizeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuildTree();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuildTree();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuildTree();
            }
        });
        modifierFilterPanel.add(new JLabel("\u2265"));
        modifierFilterPanel.add(minSizeField);

        JButton expandAllButton = new JButton("⊞");
        expandAllButton.setToolTipText("Expand all");
        expandAllButton.addActionListener(e -> expandTree());
        JButton collapseAllButton = new JButton("⊟");
        collapseAllButton.setToolTipText("Collapse all");
        collapseAllButton.addActionListener(e -> collapseTree());
        JButton decompileAbilitiesButton = new JButton("⚡");
        decompileAbilitiesButton.setToolTipText("Decompile all abilities");
        decompileAbilitiesButton.addActionListener(e -> {
            if (decompileAllAbilitiesCallback != null) {
                decompileAllAbilitiesCallback.run();
            }
        });
        modifierFilterPanel.add(expandAllButton);
        modifierFilterPanel.add(collapseAllButton);
        modifierFilterPanel.add(decompileAbilitiesButton);

        JButton refreshButton = new JButton("↻");
        refreshButton.setToolTipText("Refresh — re-parse the ABC file");
        refreshButton.addActionListener(e -> {
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
        modifierFilterPanel.add(refreshButton);

        JPanel classTypePanel = new JPanel();
        String[] classTypes = {"All", "Abilities", "Pages", "Native", "Interface", "Enum", "Classes"};
        ButtonGroup classTypeGroup = new ButtonGroup();
        for (String type : classTypes) {
            JRadioButton btn = new JRadioButton(type);
            btn.setSelected("All".equals(type));
            btn.addActionListener(e -> {
                classTypeFilter = type;
                rebuildTree();
            });
            classTypeGroup.add(btn);
            classTypePanel.add(btn);
        }

        structureTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateBreadcrumb();
                fireClassSelectionIfApplicable();
            }
        });

        JScrollPane scrollPane = new JScrollPane(structureTree);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new javax.swing.BoxLayout(filterPanel, javax.swing.BoxLayout.Y_AXIS));
        JPanel filterRow = new JPanel(new BorderLayout(4, 0));
        filterRow.add(filterField, BorderLayout.CENTER);
        filterRow.add(filterCountLabel, BorderLayout.EAST);
        filterPanel.add(filterRow);
        filterPanel.add(modifierFilterPanel);
        filterPanel.add(classTypePanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(breadcrumbLabel, BorderLayout.SOUTH);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statsLabel, BorderLayout.SOUTH);

        setDefaultWindowPosition(docking.WindowPosition.LEFT);
        setTitle("HAP Explorer");
    }

    /**
     * Sets the callback invoked when the user chooses "Export to .ets..." for a class node.
     *
     * @param cb a BiConsumer receiving the selected AbcClass and the target File
     */
    public void setExportClassCallback(BiConsumer<AbcClass, File> cb) {
        this.exportClassCallback = cb;
    }

    /**
     * Sets the callback invoked when the user chooses "Copy as ArkTS" for a class node.
     *
     * @param cb consumer that receives the selected AbcClass
     */
    public void setCopyAsArkTSCallback(java.util.function.Consumer<AbcClass> cb) {
        this.copyAsArkTSCallback = cb;
    }

    /**
     * Sets the callback invoked when the user chooses "Show All Callers" for a method node.
     *
     * @param cb consumer that receives the selected method name
     */
    public void setShowCallersCallback(Consumer<String> cb) {
        this.showCallersCallback = cb;
    }

    /**
     * Sets the callback invoked when the user chooses "Export Report" from the root context menu.
     *
     * @param cb the runnable to invoke
     */
    public void setExportReportCallback(Runnable cb) {
        this.exportReportCallback = cb;
    }

    /**
     * Sets the callback invoked when the user clicks the "Decompile All Abilities" button.
     *
     * @param cb the runnable to invoke
     */
    public void setDecompileAllAbilitiesCallback(Runnable cb) {
        this.decompileAllAbilitiesCallback = cb;
    }

    /**
     * Sets the callback invoked when the user clicks the Refresh button.
     *
     * @param cb the runnable to invoke
     */
    public void setRefreshCallback(Runnable cb) {
        this.refreshCallback = cb;
    }

    /**
     * Sets the callback invoked when the user chooses "Show Implementations" from the class context menu.
     *
     * @param cb the consumer to invoke with the selected class
     */
    public void setShowImplementationsCallback(Consumer<AbcClass> cb) {
        this.showImplementationsCallback = cb;
    }

    /**
     * Sets the notes provider so the tree can show indicators for methods with notes.
     *
     * @param provider the notes provider
     */
    public void setNotesProvider(NotesProvider provider) {
        this.notesProvider = provider;
    }

    /**
     * Sets the settings provider so the tree can respect display settings.
     *
     * @param provider the settings provider
     */
    public void setSettingsProvider(SettingsProvider provider) {
        this.settingsProvider = provider;
    }

    private void showTreeContextMenu(MouseEvent e) {
        TreePath path = structureTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        structureTree.setSelectionPath(path);
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObj = node.getUserObject();

        if (userObj instanceof AbcMethod) {
            AbcMethod method = (AbcMethod) userObj;
            JPopupMenu menu = new JPopupMenu();

            JMenuItem decompileItem = new JMenuItem("Decompile");
            decompileItem.addActionListener(ev -> {
                if (navigationListener != null) {
                    navigationListener.onMethodSelected(method);
                }
            });
            menu.add(decompileItem);

            JMenuItem copyNameItem = new JMenuItem("Copy Name");
            copyNameItem.addActionListener(ev -> copyToClipboard(method.getName()));
            menu.add(copyNameItem);

            JMenuItem copyOffsetItem = new JMenuItem("Copy Offset");
            copyOffsetItem.addActionListener(
                    ev -> copyToClipboard("0x" + Long.toHexString(method.getCodeOff())));
            menu.add(copyOffsetItem);

            JMenuItem callersItem = new JMenuItem("Show All Callers");
            callersItem.addActionListener(ev -> {
                if (showCallersCallback != null) {
                    showCallersCallback.accept(method.getName());
                }
            });
            menu.add(callersItem);

            JMenuItem copySignatureItem = new JMenuItem("Copy Signature");
            copySignatureItem.addActionListener(ev -> {
                String sig = formatMethodPrefix(method) + method.getName()
                        + formatMethodSuffixWithArgs(method, currentAbcFile);
                copyToClipboard(sig);
            });
            menu.add(copySignatureItem);

            menu.show(structureTree, e.getX(), e.getY());
        } else if (userObj instanceof AbcClass) {
            AbcClass cls = (AbcClass) userObj;
            String label = formatClassName(cls.getName());
            JPopupMenu menu = new JPopupMenu();

            JMenuItem copyNameItem = new JMenuItem("Copy Name");
            copyNameItem.addActionListener(ev -> copyToClipboard(label));
            menu.add(copyNameItem);

            JMenuItem exportItem = new JMenuItem("Export to .ets...");
            exportItem.addActionListener(ev -> {
                if (exportClassCallback == null) {
                    return;
                }
                String shortName = formatClassName(cls.getName());
                String simpleName = shortName.contains(".")
                        ? shortName.substring(shortName.lastIndexOf('.') + 1)
                        : shortName;
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(simpleName + ".ets"));
                chooser.setDialogTitle("Export Decompiled Class");
                if (chooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    exportClassCallback.accept(cls, chooser.getSelectedFile());
                }
            });
            menu.add(exportItem);

            JMenuItem copyArkTSItem = new JMenuItem("Copy as ArkTS");
            copyArkTSItem.setEnabled(copyAsArkTSCallback != null);
            if (copyAsArkTSCallback != null) {
                copyArkTSItem.addActionListener(ev -> copyAsArkTSCallback.accept(cls));
            }
            menu.add(copyArkTSItem);

            JMenuItem hierarchyItem = new JMenuItem("Show Hierarchy");
            hierarchyItem.addActionListener(ev -> showClassHierarchy(cls));
            menu.add(hierarchyItem);

            JMenuItem subclassesItem = new JMenuItem("Show Subclasses");
            subclassesItem.addActionListener(ev -> showSubclasses(cls));
            menu.add(subclassesItem);

            JMenuItem implItem = new JMenuItem("Show Implementations");
            implItem.addActionListener(ev -> {
                if (showImplementationsCallback != null) {
                    showImplementationsCallback.accept(cls);
                }
            });
            menu.add(implItem);

            menu.show(structureTree, e.getX(), e.getY());
        } else if (userObj instanceof String) {
            String label = (String) userObj;
            // Root node or HAP label — show Export Report
            if (label.startsWith("HAP") || label.endsWith("classes)")) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem exportReportItem = new JMenuItem("Export Report...");
                exportReportItem.setEnabled(exportReportCallback != null);
                if (exportReportCallback != null) {
                    exportReportItem.addActionListener(ev -> exportReportCallback.run());
                }
                menu.add(exportReportItem);
                menu.show(structureTree, e.getX(), e.getY());
            } else if (isClassNameNode(label)) {
                JPopupMenu menu = new JPopupMenu();

                JMenuItem copyNameItem = new JMenuItem("Copy Name");
                copyNameItem.addActionListener(ev -> copyToClipboard(label));
                menu.add(copyNameItem);

                menu.show(structureTree, e.getX(), e.getY());
            }
        }
    }

    private static boolean isClassNameNode(String label) {
        if (label == null) {
            return false;
        }
        if (label.equals("Classes")) {
            return false;
        }
        if (label.startsWith("Methods (") && label.endsWith(")")) {
            return false;
        }
        if (label.startsWith("Fields (") && label.endsWith(")")) {
            return false;
        }
        if (label.startsWith("ABC File")) {
            return false;
        }
        return true;
    }

    private void copyToClipboard(String text) {
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(text), null);
    }

    private void showClassHierarchy(AbcClass cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class Hierarchy\n");
        sb.append("---------------\n");
        String className = formatClassName(cls.getName());
        sb.append(className).append(" (current)\n");

        if (currentAbcFile != null) {
            long superOff = cls.getSuperClassOff();
            int depth = 0;
            while (superOff != 0 && depth < 10) {
                AbcClass parent = findClassByOffset(superOff);
                if (parent == null) {
                    sb.append("  extends <unknown @0x")
                            .append(Long.toHexString(superOff)).append(">\n");
                    break;
                }
                String parentName = formatClassName(parent.getName());
                sb.append("  extends ").append(parentName).append("\n");
                superOff = parent.getSuperClassOff();
                depth++;
            }
        }

        javax.swing.JOptionPane.showMessageDialog(
                mainPanel, sb.toString(), "Class Hierarchy",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSubclasses(AbcClass cls) {
        if (currentAbcFile == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    mainPanel, "No ABC file loaded.", "Show Subclasses",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String className = formatClassName(cls.getName());
        long clsOffset = cls.getOffset();
        List<String> subclasses = new ArrayList<>();
        for (AbcClass c : currentAbcFile.getClasses()) {
            if (c.getSuperClassOff() == clsOffset && c.getOffset() != clsOffset) {
                subclasses.add(formatClassName(c.getName()));
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Subclasses of ").append(className).append(":\n");
        sb.append("----------------------------\n");
        if (subclasses.isEmpty()) {
            sb.append("(none found in this ABC file)");
        } else {
            for (String sub : subclasses) {
                sb.append("  ").append(sub).append("\n");
            }
        }
        javax.swing.JOptionPane.showMessageDialog(
                mainPanel, sb.toString(), "Subclasses of " + className,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private AbcClass findClassByOffset(long offset) {
        if (currentAbcFile == null || offset == 0) {
            return null;
        }
        for (AbcClass c : currentAbcFile.getClasses()) {
            if (c.getOffset() == offset) {
                return c;
            }
        }
        return null;
    }

    private void updateBreadcrumb() {
        TreePath path = structureTree.getSelectionPath();
        if (path == null) {
            breadcrumbLabel.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Object[] components = path.getPath();
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                sb.append(" > ");
            }
            sb.append(components[i].toString());
        }
        breadcrumbLabel.setText(sb.toString());
    }

    /**
     * Sets the listener to be notified when the user double-clicks a method node.
     *
     * @param listener the navigation listener, or null to remove
     */
    public void setNavigationListener(MethodNavigationListener listener) {
        this.navigationListener = listener;
    }

    /**
     * Sets the listener to be notified when the user single-clicks a class node.
     *
     * @param listener the class navigation listener, or null to remove
     */
    public void setClassNavigationListener(ClassNavigationListener listener) {
        this.classNavigationListener = listener;
    }

    private void fireClassSelectionIfApplicable() {
        if (classNavigationListener == null) {
            return;
        }
        TreePath path = structureTree.getSelectionPath();
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObj = node.getUserObject();
        if (userObj instanceof AbcClass) {
            classNavigationListener.onClassSelected((AbcClass) userObj);
        }
    }

    private void handleDoubleClick() {
        TreePath path = structureTree.getSelectionPath();
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObj = node.getUserObject();

        // module.json5 node: show raw content
        if ("module.json5".equals(userObj) && !moduleJsonContent.isEmpty()) {
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(moduleJsonContent);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setCaretPosition(0);
            javax.swing.JOptionPane.showMessageDialog(
                    mainPanel,
                    new javax.swing.JScrollPane(textArea),
                    "module.json5",
                    javax.swing.JOptionPane.PLAIN_MESSAGE);
            return;
        }

        if (userObj instanceof AbcMethod && navigationListener != null) {
            navigationListener.onMethodSelected((AbcMethod) userObj);
        } else if (userObj instanceof AbcClass && classNavigationListener != null) {
            classNavigationListener.onClassSelected((AbcClass) userObj);
        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the ABC file to display and rebuilds the tree.
     *
     * @param abcFile the parsed ABC file
     */
    public void setAbcFile(AbcFile abcFile) {
        this.currentAbcFile = abcFile;
        rebuildTree();
        updateStats();
    }

    /**
     * Sets the HAP metadata (from module.json5) to show Abilities section.
     *
     * @param metadata the parsed HAP metadata, or null to clear
     * @param hapName the HAP file name for the root node label
     */
    public void setHapMetadata(HapMetadata metadata, String hapName) {
        this.currentHapMetadata = metadata;
        this.currentHapName = hapName != null ? hapName : "";
        rebuildTree();
        updateStats();
    }

    private void updateStats() {
        if (currentAbcFile == null) {
            statsLabel.setText("  ");
            return;
        }
        int totalMethods = 0;
        long totalBytes = 0;
        int abilityCount = 0;
        int pageCount = 0;
        int nativeCount = 0;
        int interfaceCount = 0;
        int enumCount = 0;
        int classCount = 0;
        for (AbcClass cls : currentAbcFile.getClasses()) {
            totalMethods += cls.getMethods().size();
            String badge = getClassTypeBadge(cls);
            if ("[A] ".equals(badge)) {
                abilityCount++;
            } else if ("[P] ".equals(badge)) {
                pageCount++;
            } else if ("[N] ".equals(badge)) {
                nativeCount++;
            } else if ("[I] ".equals(badge)) {
                interfaceCount++;
            } else if ("[E] ".equals(badge)) {
                enumCount++;
            } else {
                classCount++;
            }
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() != 0) {
                    try {
                        AbcCode code = currentAbcFile.getCodeForMethod(method);
                        if (code != null) {
                            totalBytes += code.getCodeSize();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        statsLabel.setText("[A]:" + abilityCount + " [P]:" + pageCount
                + " [N]:" + nativeCount + " [I]:" + interfaceCount
                + " [E]:" + enumCount + " [C]:" + classCount
                + " · " + totalMethods + " methods · " + (totalBytes / 1024) + " KB");
    }

    /**
     * Sets the raw module.json5 content for display in the tree.
     *
     * @param content the raw JSON content, or empty string to clear
     */
    public void setModuleJsonContent(String content) {
        this.moduleJsonContent = content != null ? content : "";
        rebuildTree();
    }

    /**
     * Shows an error message in place of the tree content.
     *
     * @param message the error message
     */
    public void showError(String message) {
        rootNode.removeAllChildren();
        rootNode.setUserObject("Error: " + message);
        treeModel.reload();
        Msg.warn(OWNER, "HAP Explorer error: " + message);
    }

    /**
     * Gets the currently loaded ABC file.
     *
     * @return the current ABC file, or null
     */
    public AbcFile getAbcFile() {
        return currentAbcFile;
    }

    /**
     * Selects the tree node corresponding to the given class, scrolls it into view,
     * and brings this provider to the front.
     *
     * @param targetClass the class to select
     */
    public void selectClass(AbcClass targetClass) {
        if (targetClass == null) {
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        java.util.Enumeration<?> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) nodes.nextElement();
            Object userObj = node.getUserObject();
            if (userObj instanceof AbcClass) {
                AbcClass cls = (AbcClass) userObj;
                if (cls.getName().equals(targetClass.getName())) {
                    TreePath path = new TreePath(node.getPath());
                    structureTree.setSelectionPath(path);
                    structureTree.scrollPathToVisible(path);
                    getTool().showComponentProvider(this, true);
                    return;
                }
            }
        }
    }

    /**
     * Selects the tree node for the given method, expanding its parent class if needed.
     *
     * @param targetMethod the method to select
     */
    public void selectMethod(AbcMethod targetMethod) {
        if (targetMethod == null) {
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        java.util.Enumeration<?> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) nodes.nextElement();
            Object userObj = node.getUserObject();
            if (userObj instanceof AbcMethod) {
                AbcMethod method = (AbcMethod) userObj;
                if (method.getOffset() == targetMethod.getOffset()) {
                    TreePath path = new TreePath(node.getPath());
                    structureTree.expandPath(path.getParentPath());
                    structureTree.setSelectionPath(path);
                    structureTree.scrollPathToVisible(path);
                    getTool().showComponentProvider(this, true);
                    return;
                }
            }
        }
    }

    /**
     * Returns true if the given name contains the filter text (case-insensitive),
     * or if the filter is empty.
     *
     * @param name   the string to test
     * @param filter the filter text
     * @return true if name matches the filter
     */
    static boolean matchesFilter(String name, String filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        if (name == null) {
            return false;
        }
        return name.toLowerCase().contains(filter.toLowerCase());
    }

    private boolean passesModifierFilter(AbcMethod method) {
        boolean pubSelected = filterPublicButton != null && filterPublicButton.isSelected();
        boolean privSelected = filterPrivateButton != null && filterPrivateButton.isSelected();
        boolean staticSelected = filterStaticButton != null && filterStaticButton.isSelected();
        boolean abstractSelected = filterAbstractButton != null && filterAbstractButton.isSelected();
        boolean hasNotesSelected = filterHasNotesButton != null && filterHasNotesButton.isSelected();
        if (!pubSelected && !privSelected && !staticSelected && !abstractSelected && !hasNotesSelected) {
            return true;
        }
        long flags = method.getAccessFlags();
        if (pubSelected && (flags & AbcAccessFlags.ACC_PUBLIC) != 0) {
            return true;
        }
        if (privSelected && (flags & AbcAccessFlags.ACC_PRIVATE) != 0) {
            return true;
        }
        if (staticSelected && (flags & AbcAccessFlags.ACC_STATIC) != 0) {
            return true;
        }
        if (abstractSelected && method.getCodeOff() == 0) {
            return true;
        }
        if (hasNotesSelected && notesProvider != null && notesProvider.hasNotes(method.getName())) {
            return true;
        }
        return false;
    }

    private long getMinSizeFilter() {
        if (minSizeField == null) {
            return 0;
        }
        try {
            long val = Long.parseLong(minSizeField.getText().trim());
            return Math.max(0, val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean passesMinSizeFilter(AbcMethod method) {
        long minSize = getMinSizeFilter();
        if (minSize <= 0 || currentAbcFile == null) {
            return true;
        }
        try {
            AbcCode code = currentAbcFile.getCodeForMethod(method);
            return code != null && code.getCodeSize() >= minSize;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean passesArgFilter(AbcMethod method, String filter) {
        if (!filter.startsWith("args:") && !filter.startsWith("size:")) {
            return true;
        }
        if (currentAbcFile == null) {
            return true;
        }
        if (filter.startsWith("args:")) {
            String spec = filter.substring(5).trim();
            try {
                AbcCode code = currentAbcFile.getCodeForMethod(method);
                long numArgs = code != null ? code.getNumArgs() : 0;
                if (spec.startsWith(">")) {
                    return numArgs > Long.parseLong(spec.substring(1).trim());
                } else if (spec.startsWith("<")) {
                    return numArgs < Long.parseLong(spec.substring(1).trim());
                } else {
                    return numArgs == Long.parseLong(spec);
                }
            } catch (Exception e) {
                return true;
            }
        }
        if (filter.startsWith("size:")) {
            String spec = filter.substring(5).trim();
            try {
                AbcCode code = currentAbcFile.getCodeForMethod(method);
                long size = code != null ? code.getCodeSize() : 0;
                if (spec.startsWith(">")) {
                    return size > Long.parseLong(spec.substring(1).trim());
                } else if (spec.startsWith("<")) {
                    return size < Long.parseLong(spec.substring(1).trim());
                } else {
                    return size == Long.parseLong(spec);
                }
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    private AbcClass findClassForAbility(String abilityName) {
        if (currentAbcFile == null || abilityName == null) {
            return null;
        }
        String lowerAbility = abilityName.toLowerCase();
        for (AbcClass cls : currentAbcFile.getClasses()) {
            String simpleName = formatClassName(cls.getName());
            if (simpleName.contains(".")) {
                simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
            }
            if (simpleName.toLowerCase().contains(lowerAbility)
                    || lowerAbility.contains(simpleName.toLowerCase())) {
                return cls;
            }
        }
        return null;
    }

    private void rebuildTree() {
        rootNode.removeAllChildren();

        if (currentAbcFile == null) {
            rootNode.setUserObject("HAP (not loaded)");
            treeModel.reload();
            return;
        }

        String filter = filterField.getText();
        if (FILTER_PLACEHOLDER.equals(filter)) {
            filter = "";
        }
        // Check for special filter syntax
        boolean isSpecialFilter = filter.startsWith("args:") || filter.startsWith("size:")
                || filter.startsWith("name:") || filter.startsWith("class:");
        String textFilter = isSpecialFilter ? "" : filter;
        String namePattern = "";
        String classPattern = "";
        if (filter.startsWith("name:")) {
            namePattern = filter.substring(5).trim().toLowerCase();
        } else if (filter.startsWith("class:")) {
            classPattern = filter.substring(6).trim().toLowerCase();
        }

        // Root node: show HAP name if available
        String rootLabel = currentHapName.isEmpty()
                ? "HAP (" + currentAbcFile.getClasses().size() + " classes)"
                : currentHapName + " (" + currentAbcFile.getClasses().size() + " classes)";
        rootNode.setUserObject(rootLabel);

        boolean showAbilities = "All".equals(classTypeFilter) || "Abilities".equals(classTypeFilter);
        boolean showPages = "All".equals(classTypeFilter) || "Pages".equals(classTypeFilter);
        boolean showClasses = "All".equals(classTypeFilter) || "Classes".equals(classTypeFilter)
                || "Interface".equals(classTypeFilter) || "Enum".equals(classTypeFilter);
        boolean showNative = "All".equals(classTypeFilter) || "Classes".equals(classTypeFilter)
                || "Native".equals(classTypeFilter);
        // When "Native" is selected, don't show the regular Classes section
        if ("Native".equals(classTypeFilter)) {
            showClasses = false;
        }

        // Abilities section from HapMetadata
        if (showAbilities && currentHapMetadata != null && !currentHapMetadata.getAbilities().isEmpty()) {
            int abilityCount = currentHapMetadata.getAbilities().size();
            DefaultMutableTreeNode abilitiesNode =
                    new DefaultMutableTreeNode("Abilities (" + abilityCount + ")");
            rootNode.add(abilitiesNode);
            for (HapMetadata.AbilityInfo ability : currentHapMetadata.getAbilities()) {
                String abilityLabel = ability.getName();
                if (!ability.getType().isEmpty()) {
                    abilityLabel += " [" + ability.getType() + "]";
                }
                // Find the matching AbcClass for this ability
                AbcClass abilityClass = findClassForAbility(ability.getName());
                if (abilityClass != null) {
                    final AbcClass finalCls = abilityClass;
                    final String finalLabel = abilityLabel;
                    DefaultMutableTreeNode abilityNode =
                            new DefaultMutableTreeNode(finalCls) {
                                @Override
                                public String toString() {
                                    return finalLabel;
                                }
                            };
                    abilitiesNode.add(abilityNode);
                    // Add methods under ability
                    for (AbcMethod method : abilityClass.getMethods()) {
                        DefaultMutableTreeNode methodNode =
                                new DefaultMutableTreeNode(method) {
                                    @Override
                                    public String toString() {
                                        AbcMethod m = (AbcMethod) getUserObject();
                                        return m.getName();
                                    }
                                };
                        abilityNode.add(methodNode);
                    }
                } else {
                    abilitiesNode.add(new DefaultMutableTreeNode(abilityLabel));
                }
            }
        }

        // module.json5 node
        if (!moduleJsonContent.isEmpty()) {
            final String jsonContent = moduleJsonContent;
            DefaultMutableTreeNode moduleJsonNode =
                    new DefaultMutableTreeNode("module.json5") {
                        @Override
                        public boolean isLeaf() {
                            return true;
                        }
                    };
            rootNode.add(moduleJsonNode);
        }

        // Pages section: ArkUI classes with build() method
        List<AbcClass> pageClasses = new ArrayList<>();
        for (AbcClass cls : currentAbcFile.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if ("build".equals(method.getName())) {
                    pageClasses.add(cls);
                    break;
                }
            }
        }
        if (showPages && !pageClasses.isEmpty()) {
            DefaultMutableTreeNode pagesNode =
                    new DefaultMutableTreeNode("Pages (" + pageClasses.size() + ")");
            rootNode.add(pagesNode);
            for (AbcClass pageCls : pageClasses) {
                String pageLabel = formatClassName(pageCls.getName());
                if (pageLabel.contains(".")) {
                    pageLabel = pageLabel.substring(pageLabel.lastIndexOf('.') + 1);
                }
                final AbcClass finalPageCls = pageCls;
                final String finalPageLabel = pageLabel;
                DefaultMutableTreeNode pageNode =
                        new DefaultMutableTreeNode(finalPageCls) {
                            @Override
                            public String toString() {
                                return finalPageLabel;
                            }
                        };
                pagesNode.add(pageNode);
            }
        }

        // Count non-native classes for the header
        long regularClassCount = currentAbcFile.getClasses().stream()
                .filter(c -> {
                    if (c.getMethods().isEmpty()) {
                        return true;
                    }
                    for (AbcMethod m : c.getMethods()) {
                        if (m.getCodeOff() != 0) {
                            return true;
                        }
                    }
                    return false;
                }).count();

        DefaultMutableTreeNode classesNode =
                new DefaultMutableTreeNode("Classes (" + regularClassCount + ")");
        if (showClasses) {
            rootNode.add(classesNode);
        }

        // Native Modules section: classes where all methods are abstract/native
        List<AbcClass> nativeClasses = new ArrayList<>();
        for (AbcClass cls : currentAbcFile.getClasses()) {
            if (cls.getMethods().isEmpty()) {
                continue;
            }
            boolean allNative = true;
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() != 0) {
                    allNative = false;
                    break;
                }
            }
            if (allNative) {
                nativeClasses.add(cls);
            }
        }
        if (!nativeClasses.isEmpty()) {
            DefaultMutableTreeNode nativeNode =
                    new DefaultMutableTreeNode(
                            "Native Modules (" + nativeClasses.size() + ")");
            if (showNative) {
                rootNode.add(nativeNode);
            }
            for (AbcClass nativeCls : nativeClasses) {
                String nativeLabel = formatClassName(nativeCls.getName());
                if (nativeLabel.contains(".")) {
                    nativeLabel = nativeLabel.substring(nativeLabel.lastIndexOf('.') + 1);
                }
                final AbcClass finalNativeCls = nativeCls;
                final String finalNativeLabel = nativeLabel;
                DefaultMutableTreeNode nativeClsNode =
                        new DefaultMutableTreeNode(finalNativeCls) {
                            @Override
                            public String toString() {
                                return finalNativeLabel;
                            }
                        };
                nativeNode.add(nativeClsNode);
            }
        }

        if (showClasses) {
            for (AbcClass cls : currentAbcFile.getClasses()) {
                String className = formatClassName(cls.getName());
                if (filter.startsWith("class:") && !classPattern.isEmpty()) {
                    if (!className.toLowerCase().contains(classPattern)) {
                        continue;
                    }
                }
                boolean classNameMatches = matchesFilter(className, textFilter);

                boolean anyMethodMatches = false;
                if (!classNameMatches && (!textFilter.isEmpty() || isSpecialFilter)) {
                    for (AbcMethod method : cls.getMethods()) {
                        if (matchesFilter(method.getName(), textFilter) && passesModifierFilter(method)
                                && passesMinSizeFilter(method)
                                && (!isSpecialFilter || passesArgFilter(method, filter))) {
                            anyMethodMatches = true;
                            break;
                        }
                    }
                }

                if ((!textFilter.isEmpty() || isSpecialFilter) && !classNameMatches && !anyMethodMatches) {
                    continue;
                }

                if ("Interface".equals(classTypeFilter)) {
                    if (!"[I] ".equals(getClassTypeBadge(cls))) {
                        continue;
                    }
                }
                if ("Enum".equals(classTypeFilter)) {
                    if (!"[E] ".equals(getClassTypeBadge(cls))) {
                        continue;
                    }
                }

                boolean anyMethodPassesModifier = false;
                for (AbcMethod method : cls.getMethods()) {
                    if (passesModifierFilter(method) && passesMinSizeFilter(method)
                            && (!isSpecialFilter || passesArgFilter(method, filter))) {
                        anyMethodPassesModifier = true;
                        break;
                    }
                }
                boolean modifierFilterActive = (filterPublicButton != null && filterPublicButton.isSelected())
                        || (filterPrivateButton != null && filterPrivateButton.isSelected())
                        || (filterStaticButton != null && filterStaticButton.isSelected())
                        || (filterAbstractButton != null && filterAbstractButton.isSelected())
                        || (filterHasNotesButton != null && filterHasNotesButton.isSelected());
                if (modifierFilterActive && !anyMethodPassesModifier) {
                    continue;
                }
    
                DefaultMutableTreeNode classNode =
                        new DefaultMutableTreeNode(cls) {
                            @Override
                            public String toString() {
                                AbcClass c = (AbcClass) getUserObject();
                                boolean showBadges = settingsProvider == null
                                        || settingsProvider.isShowClassTypeBadges();
                                boolean showCount = settingsProvider == null
                                        || settingsProvider.isShowMethodCountInClassNodes();
                                String name = showBadges
                                        ? getClassTypeBadge(c) + formatClassName(c.getName())
                                        : formatClassName(c.getName());
                                if (showCount) {
                                    int mc = c.getMethods().size();
                                    int fc = c.getFields().size();
                                    if (mc > 0 || fc > 0) {
                                        StringBuilder sb = new StringBuilder(name).append(" (");
                                        if (mc > 0) {
                                            sb.append(mc).append("m");
                                        }
                                        if (fc > 0) {
                                            if (mc > 0) {
                                                sb.append(" ");
                                            }
                                            sb.append(fc).append("f");
                                        }
                                        sb.append(")");
                                        return sb.toString();
                                    }
                                }
                                return name;
                            }
                        };
                classesNode.add(classNode);
    
                DefaultMutableTreeNode methodsNode =
                        new DefaultMutableTreeNode(
                                "Methods (" + cls.getMethods().size() + ")");
                classNode.add(methodsNode);
    
                // Build sorted/filtered method list
                List<AbcMethod> methodList = new ArrayList<>(cls.getMethods());
                if (sortBySizeButton != null && sortBySizeButton.isSelected()
                        && currentAbcFile != null) {
                    methodList.sort(Comparator.comparingLong((AbcMethod m) -> {
                        try {
                            AbcCode c = currentAbcFile.getCodeForMethod(m);
                            return c != null ? c.getCodeSize() : 0L;
                        } catch (Exception ex) {
                            return 0L;
                        }
                    }).reversed());
                }
    
                for (AbcMethod method : methodList) {
                    if (!textFilter.isEmpty() && !classNameMatches
                            && !matchesFilter(method.getName(), textFilter)) {
                        continue;
                    }
                    if (!passesModifierFilter(method)) {
                        continue;
                    }
                    if (!passesMinSizeFilter(method)) {
                        continue;
                    }
                    if (filter.startsWith("name:") && !namePattern.isEmpty()) {
                        if (!method.getName().toLowerCase().contains(namePattern)) {
                            continue;
                        }
                    }
                    if (isSpecialFilter && !passesArgFilter(method, filter)) {
                        continue;
                    }
                    DefaultMutableTreeNode methodNode =
                            new DefaultMutableTreeNode(method) {
                                @Override
                                public String toString() {
                                    AbcMethod m = (AbcMethod) getUserObject();
                                    return formatMethodPrefix(m) + m.getName()
                                            + formatMethodSuffixWithArgs(m, currentAbcFile);
                                }
                            };
                    methodsNode.add(methodNode);
                }
    
                if (!cls.getFields().isEmpty()) {
                    DefaultMutableTreeNode fieldsNode =
                            new DefaultMutableTreeNode(
                                    "Fields (" + cls.getFields().size() + ")");
                    classNode.add(fieldsNode);
    
                    for (AbcField field : cls.getFields()) {
                        DefaultMutableTreeNode fieldNode =
                                new DefaultMutableTreeNode(field.getName());
                        fieldsNode.add(fieldNode);
                    }
                }
            }
        }

        updateFilterCount();

        // Save which top-level sections are currently expanded
        java.util.Set<String> expandedSections = new java.util.HashSet<>();
        int rowsBefore = structureTree.getRowCount();
        for (int i = 0; i < rowsBefore; i++) {
            javax.swing.tree.TreePath path = structureTree.getPathForRow(i);
            if (path != null && path.getPathCount() == 2 && structureTree.isExpanded(path)) {
                expandedSections.add(path.getLastPathComponent().toString());
            }
        }

        treeModel.reload();

        // Restore expanded state, or use smart expansion for first load
        if (expandedSections.isEmpty()) {
            smartExpandTree();
        } else {
            structureTree.expandRow(0);
            int rowsAfter = structureTree.getRowCount();
            for (int i = 1; i < rowsAfter; i++) {
                javax.swing.tree.TreePath path = structureTree.getPathForRow(i);
                if (path != null && path.getPathCount() == 2) {
                    String label = path.getLastPathComponent().toString();
                    // Match by prefix (section name may have changed count)
                    boolean shouldExpand = false;
                    for (String saved : expandedSections) {
                        String savedBase = saved.contains("(") ? saved.substring(0, saved.indexOf('(')).trim() : saved;
                        String labelBase = label.contains("(") ? label.substring(0, label.indexOf('(')).trim() : label;
                        if (savedBase.equals(labelBase)) {
                            shouldExpand = true;
                            break;
                        }
                    }
                    if (shouldExpand) {
                        structureTree.expandRow(i);
                    }
                }
            }
        }
    }

    private void updateFilterCount() {
        String filter = filterField.getText();
        if (FILTER_PLACEHOLDER.equals(filter) || filter.isEmpty()) {
            if (currentAbcFile != null) {
                // Show class type breakdown when no filter is active
                int a = 0, p = 0, n = 0, iface = 0, e = 0, c = 0;
                for (AbcClass cls : currentAbcFile.getClasses()) {
                    String badge = getClassTypeBadge(cls);
                    if ("[A] ".equals(badge)) {
                        a++;
                    } else if ("[P] ".equals(badge)) {
                        p++;
                    } else if ("[N] ".equals(badge)) {
                        n++;
                    } else if ("[I] ".equals(badge)) {
                        iface++;
                    } else if ("[E] ".equals(badge)) {
                        e++;
                    } else {
                        c++;
                    }
                }
                filterCountLabel.setText("[A]:" + a + " [P]:" + p + " [N]:" + n
                        + " [I]:" + iface + " [E]:" + e + " [C]:" + c);
            } else {
                filterCountLabel.setText("");
            }
            return;
        }
        boolean isSpecialFilter = filter.startsWith("args:") || filter.startsWith("size:")
                || filter.startsWith("name:") || filter.startsWith("class:");
        String textFilter = isSpecialFilter ? "" : filter;
        String namePattern = "";
        String classPattern = "";
        if (filter.startsWith("name:")) {
            namePattern = filter.substring(5).trim().toLowerCase();
        } else if (filter.startsWith("class:")) {
            classPattern = filter.substring(6).trim().toLowerCase();
        }
        int classCount = 0;
        int methodCount = 0;
        if (currentAbcFile != null) {
            for (AbcClass cls : currentAbcFile.getClasses()) {
                String className = formatClassName(cls.getName());
                if (filter.startsWith("class:") && !classPattern.isEmpty()
                        && !className.toLowerCase().contains(classPattern)) {
                    continue;
                }
                boolean classMatches = matchesFilter(className, textFilter);
                if (classMatches && !isSpecialFilter) {
                    classCount++;
                }
                for (AbcMethod method : cls.getMethods()) {
                    if (filter.startsWith("name:") && !namePattern.isEmpty()) {
                        if (!method.getName().toLowerCase().contains(namePattern)) {
                            continue;
                        }
                        methodCount++;
                        continue;
                    }
                    if (matchesFilter(method.getName(), textFilter)
                            && (!isSpecialFilter || passesArgFilter(method, filter))) {
                        methodCount++;
                    }
                }
            }
        }
        if (classCount > 0 || methodCount > 0) {
            filterCountLabel.setText(classCount + "c " + methodCount + "m");
        } else {
            filterCountLabel.setText("no match");
        }
    }

    private void smartExpandTree() {
        // Expand root node
        structureTree.expandRow(0);
        // Expand all direct children of root (Abilities, Pages, Classes, etc.)
        // but not their children
        int rowCount = structureTree.getRowCount();
        for (int i = 1; i < rowCount; i++) {
            javax.swing.tree.TreePath path = structureTree.getPathForRow(i);
            if (path != null && path.getPathCount() == 2) {
                structureTree.expandRow(i);
            }
        }
    }

    private void expandTree() {
        for (int i = 0; i < structureTree.getRowCount(); i++) {
            structureTree.expandRow(i);
        }
    }

    private void collapseTree() {
        for (int i = structureTree.getRowCount() - 1; i >= 0; i--) {
            structureTree.collapseRow(i);
        }
    }

    static String formatClassName(String name) {
        if (name == null || name.isEmpty()) {
            return "<unnamed>";
        }
        String result = name;
        if (result.startsWith("L")) {
            result = result.substring(1);
        }
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.replace("/", ".");
    }

    private static String formatMethodSuffix(AbcMethod method) {
        if (method.getCodeOff() == 0) {
            return " (abstract/native)";
        }
        return " @0x" + Long.toHexString(method.getCodeOff());
    }

    static String formatMethodPrefix(AbcMethod method) {
        long flags = method.getAccessFlags();
        String prefix;
        if ((flags & AbcAccessFlags.ACC_PUBLIC) != 0) {
            prefix = "+ ";
        } else if ((flags & AbcAccessFlags.ACC_PRIVATE) != 0) {
            prefix = "- ";
        } else if ((flags & AbcAccessFlags.ACC_PROTECTED) != 0) {
            prefix = "# ";
        } else {
            prefix = "~ ";
        }
        return prefix;
    }

    private static String formatMethodSuffixWithArgs(AbcMethod method, AbcFile abcFile) {
        String staticTag = ((method.getAccessFlags() & AbcAccessFlags.ACC_STATIC) != 0)
                ? " [S]" : "";
        if (method.getCodeOff() == 0) {
            return staticTag + " (abstract/native)";
        }
        if (abcFile != null) {
            try {
                AbcCode code = abcFile.getCodeForMethod(method);
                if (code != null) {
                    long numArgs = code.getNumArgs();
                    long codeSize = code.getCodeSize();
                    return staticTag + " (" + numArgs + " arg"
                            + (numArgs == 1 ? "" : "s")
                            + ", " + codeSize + "b"
                            + ") @0x" + Long.toHexString(method.getCodeOff());
                }
            } catch (Exception e) {
                // fall through to default
            }
        }
        return staticTag + " @0x" + Long.toHexString(method.getCodeOff());
    }

    /**
     * Tree cell renderer that color-codes method nodes by bytecode size.
     * Abstract/native methods: gray.
     * Large methods (> 200b): red.
     * Medium methods (50-200b): orange.
     * Small methods (< 50b): default color.
     */
    private class MethodComplexityCellRenderer extends DefaultTreeCellRenderer {

        private static final int SIZE_LARGE = 200;
        private static final int SIZE_MEDIUM = 50;
        private static final Color COLOR_LARGE = new Color(0xC62828);
        private static final Color COLOR_MEDIUM = new Color(0xE65100);
        private static final Color COLOR_ABSTRACT = new Color(0x757575);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
            if (!selected && value instanceof DefaultMutableTreeNode) {
                Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObj instanceof AbcMethod) {
                    AbcMethod method = (AbcMethod) userObj;
                    if (method.getCodeOff() == 0) {
                        setForeground(COLOR_ABSTRACT);
                    } else if (currentAbcFile != null) {
                        try {
                            AbcCode code = currentAbcFile.getCodeForMethod(method);
                            if (code != null) {
                                long size = code.getCodeSize();
                                if (size > SIZE_LARGE) {
                                    setForeground(COLOR_LARGE);
                                } else if (size > SIZE_MEDIUM) {
                                    setForeground(COLOR_MEDIUM);
                                }
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    // Notes indicator: bold text if method has notes
                    if (notesProvider != null && notesProvider.hasNotes(method.getName())) {
                        setFont(getFont().deriveFont(Font.BOLD));
                        setToolTipText("Has notes");
                    } else {
                        setToolTipText(null);
                    }
                    // Strikethrough for likely-generated/internal private methods
                    long flags = method.getAccessFlags();
                    boolean isPrivate = (flags & AbcAccessFlags.ACC_PRIVATE) != 0;
                    String name = method.getName();
                    boolean isGenerated = name.startsWith("_") || name.contains("$")
                            || name.contains("#") || name.startsWith("lambda");
                    if (isPrivate && isGenerated) {
                        Font f = getFont();
                        Map<TextAttribute, Object> attrs = new HashMap<>(f.getAttributes());
                        attrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                        setFont(f.deriveFont(attrs));
                    }
                } else if (userObj instanceof AbcClass) {
                    // Tooltip showing full class name + parent + counts
                    AbcClass cls = (AbcClass) userObj;
                    String fullName = cls.getName();
                    if (fullName != null && !fullName.isEmpty()) {
                        String display = formatClassName(fullName);
                        StringBuilder tip = new StringBuilder("<html>");
                        tip.append("<b>").append(display).append("</b>");
                        // Show parent class if available
                        if (currentAbcFile != null && cls.getSuperClassOff() != 0) {
                            AbcClass parent = null;
                            for (AbcClass c : currentAbcFile.getClasses()) {
                                if (c.getOffset() == cls.getSuperClassOff()) {
                                    parent = c;
                                    break;
                                }
                            }
                            if (parent != null) {
                                tip.append("<br>extends ").append(formatClassName(parent.getName()));
                            }
                        }
                        tip.append("<br>").append(cls.getMethods().size()).append(" methods, ")
                                .append(cls.getFields().size()).append(" fields");
                        tip.append("</html>");
                        setToolTipText(tip.toString());
                    }
                }
            }
            return this;
        }
    }

    /**
     * Returns a type badge for a class: [A] Ability, [P] Page, [N] Native, [C] regular.
     *
     * @param cls the class to classify
     * @return the badge string including trailing space
     */
    static String getClassTypeBadge(AbcClass cls) {
        long flags = cls.getAccessFlags();
        // Check for annotation flag
        if ((flags & AbcAccessFlags.ACC_ANNOTATION) != 0) {
            return "[Ann] ";
        }
        // Check for enum flag
        if ((flags & AbcAccessFlags.ACC_ENUM) != 0) {
            return "[E] ";
        }
        // Check for interface flag
        if ((flags & AbcAccessFlags.ACC_INTERFACE) != 0) {
            return "[I] ";
        }
        if (cls.getMethods().isEmpty()) {
            return "[C] ";
        }
        boolean allNative = true;
        for (AbcMethod m : cls.getMethods()) {
            if (m.getCodeOff() != 0) {
                allNative = false;
                break;
            }
        }
        if (allNative) {
            return "[N] ";
        }
        for (AbcMethod m : cls.getMethods()) {
            if ("build".equals(m.getName())) {
                return "[P] ";
            }
        }
        java.util.Set<String> lifecycle = new java.util.HashSet<>(
                java.util.Arrays.asList("onCreate", "onDestroy", "onWindowStageCreate",
                        "onForeground", "onBackground", "onBackup", "onRestore"));
        for (AbcMethod m : cls.getMethods()) {
            if (lifecycle.contains(m.getName())) {
                return "[A] ";
            }
        }
        return "[C] ";
    }
}
