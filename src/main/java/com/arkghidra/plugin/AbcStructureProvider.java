package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import docking.ComponentProvider;
import docking.Tool;
import ghidra.util.Msg;

import com.arkghidra.format.AbcClass;
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

    private final JPanel mainPanel;
    private final JTree structureTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private AbcFile currentAbcFile;
    private MethodNavigationListener navigationListener;

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
        });

        JScrollPane scrollPane = new JScrollPane(structureTree);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setDefaultWindowPosition(docking.WindowPosition.LEFT);
        setTitle("ABC Structure");
    }

    /**
     * Sets the listener to be notified when the user double-clicks a method node.
     *
     * @param listener the navigation listener, or null to remove
     */
    public void setNavigationListener(MethodNavigationListener listener) {
        this.navigationListener = listener;
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

    private void rebuildTree() {
        rootNode.removeAllChildren();

        if (currentAbcFile == null) {
            rootNode.setUserObject("ABC File (not loaded)");
            treeModel.reload();
            return;
        }

        rootNode.setUserObject("ABC File ("
                + currentAbcFile.getClasses().size() + " classes)");

        DefaultMutableTreeNode classesNode =
                new DefaultMutableTreeNode("Classes");
        rootNode.add(classesNode);

        for (AbcClass cls : currentAbcFile.getClasses()) {
            DefaultMutableTreeNode classNode =
                    new DefaultMutableTreeNode(
                            formatClassName(cls.getName()));
            classesNode.add(classNode);

            DefaultMutableTreeNode methodsNode =
                    new DefaultMutableTreeNode(
                            "Methods (" + cls.getMethods().size() + ")");
            classNode.add(methodsNode);

            for (AbcMethod method : cls.getMethods()) {
                DefaultMutableTreeNode methodNode =
                        new DefaultMutableTreeNode(method) {
                            @Override
                            public String toString() {
                                AbcMethod m = (AbcMethod) getUserObject();
                                return m.getName() + formatMethodSuffix(m);
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
}
