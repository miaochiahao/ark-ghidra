package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.function.BiConsumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

    private static final String FILTER_PLACEHOLDER = "Filter classes and methods...";

    private final JPanel mainPanel;
    private final JTree structureTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final JTextField filterField;
    private final JLabel breadcrumbLabel;
    private AbcFile currentAbcFile;
    private MethodNavigationListener navigationListener;
    private ClassNavigationListener classNavigationListener;
    private BiConsumer<AbcClass, File> exportClassCallback;
    private java.util.function.Consumer<AbcClass> copyAsArkTSCallback;
    private JToggleButton filterPublicButton;
    private JToggleButton filterPrivateButton;
    private JToggleButton filterStaticButton;

    public AbcStructureProvider(Tool tool, String owner) {
        super(tool, "ABC Structure", owner);
        rootNode = new DefaultMutableTreeNode("ABC File (not loaded)");
        treeModel = new DefaultTreeModel(rootNode);
        structureTree = new JTree(treeModel);
        structureTree.setRootVisible(true);
        structureTree.setShowsRootHandles(true);
        structureTree.setFont(new Font("Monospaced", Font.PLAIN, 12));
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

        breadcrumbLabel = new JLabel("");
        breadcrumbLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        breadcrumbLabel.setForeground(Color.GRAY);
        breadcrumbLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        filterPublicButton = new JToggleButton("Pub");
        filterPublicButton.setToolTipText("Show only public methods");
        filterPublicButton.addActionListener(e -> rebuildTree());

        filterPrivateButton = new JToggleButton("Priv");
        filterPrivateButton.setToolTipText("Show only private methods");
        filterPrivateButton.addActionListener(e -> rebuildTree());

        filterStaticButton = new JToggleButton("Static");
        filterStaticButton.setToolTipText("Show only static methods");
        filterStaticButton.addActionListener(e -> rebuildTree());

        JPanel modifierFilterPanel = new JPanel();
        modifierFilterPanel.add(filterPublicButton);
        modifierFilterPanel.add(filterPrivateButton);
        modifierFilterPanel.add(filterStaticButton);

        structureTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateBreadcrumb();
                fireClassSelectionIfApplicable();
            }
        });

        JScrollPane scrollPane = new JScrollPane(structureTree);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterField, BorderLayout.NORTH);
        topPanel.add(modifierFilterPanel, BorderLayout.CENTER);
        topPanel.add(breadcrumbLabel, BorderLayout.SOUTH);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setDefaultWindowPosition(docking.WindowPosition.LEFT);
        setTitle("ABC Structure");
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

            menu.show(structureTree, e.getX(), e.getY());
        } else if (userObj instanceof String) {
            String label = (String) userObj;
            if (isClassNameNode(label)) {
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
        if (navigationListener == null) {
            return;
        }
        TreePath path = structureTree.getSelectionPath();
        if (path == null) {
            return;
        }
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObj = node.getUserObject();
        if (userObj instanceof AbcMethod) {
            navigationListener.onMethodSelected((AbcMethod) userObj);
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
        Msg.warn(OWNER, "ABC Structure error: " + message);
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
        if (!pubSelected && !privSelected && !staticSelected) {
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
        return false;
    }

    private void rebuildTree() {
        rootNode.removeAllChildren();

        if (currentAbcFile == null) {
            rootNode.setUserObject("ABC File (not loaded)");
            treeModel.reload();
            return;
        }

        String filter = filterField.getText();
        if (FILTER_PLACEHOLDER.equals(filter)) {
            filter = "";
        }

        rootNode.setUserObject("ABC File ("
                + currentAbcFile.getClasses().size() + " classes)");

        DefaultMutableTreeNode classesNode =
                new DefaultMutableTreeNode("Classes");
        rootNode.add(classesNode);

        for (AbcClass cls : currentAbcFile.getClasses()) {
            String className = formatClassName(cls.getName());
            boolean classNameMatches = matchesFilter(className, filter);

            boolean anyMethodMatches = false;
            if (!classNameMatches && !filter.isEmpty()) {
                for (AbcMethod method : cls.getMethods()) {
                    if (matchesFilter(method.getName(), filter) && passesModifierFilter(method)) {
                        anyMethodMatches = true;
                        break;
                    }
                }
            }

            if (!filter.isEmpty() && !classNameMatches && !anyMethodMatches) {
                continue;
            }

            boolean anyMethodPassesModifier = false;
            for (AbcMethod method : cls.getMethods()) {
                if (passesModifierFilter(method)) {
                    anyMethodPassesModifier = true;
                    break;
                }
            }
            boolean modifierFilterActive = (filterPublicButton != null && filterPublicButton.isSelected())
                    || (filterPrivateButton != null && filterPrivateButton.isSelected())
                    || (filterStaticButton != null && filterStaticButton.isSelected());
            if (modifierFilterActive && !anyMethodPassesModifier) {
                continue;
            }

            DefaultMutableTreeNode classNode =
                    new DefaultMutableTreeNode(cls) {
                        @Override
                        public String toString() {
                            AbcClass c = (AbcClass) getUserObject();
                            return formatClassName(c.getName());
                        }
                    };
            classesNode.add(classNode);

            DefaultMutableTreeNode methodsNode =
                    new DefaultMutableTreeNode(
                            "Methods (" + cls.getMethods().size() + ")");
            classNode.add(methodsNode);

            for (AbcMethod method : cls.getMethods()) {
                if (!filter.isEmpty() && !classNameMatches
                        && !matchesFilter(method.getName(), filter)) {
                    continue;
                }
                if (!passesModifierFilter(method)) {
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

        treeModel.reload();

        expandTree();
    }

    private void expandTree() {
        for (int i = 0; i < structureTree.getRowCount(); i++) {
            structureTree.expandRow(i);
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

    private static String formatMethodPrefix(AbcMethod method) {
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
                    return staticTag + " (" + numArgs + " arg"
                            + (numArgs == 1 ? "" : "s")
                            + ") @0x" + Long.toHexString(method.getCodeOff());
                }
            } catch (Exception e) {
                // fall through to default
            }
        }
        return staticTag + " @0x" + Long.toHexString(method.getCodeOff());
    }
}
