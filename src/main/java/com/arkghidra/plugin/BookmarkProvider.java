package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel that lets users bookmark methods for quick access.
 *
 * <p>Each entry displays {@code ClassName.methodName}. Double-clicking an entry navigates
 * to that method. Right-clicking shows a context menu with a "Remove Bookmark" option.
 * Bookmarks persist in memory for the session.</p>
 */
public class BookmarkProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final DefaultListModel<String> bookmarksModel;
    private final JList<String> bookmarksList;
    private final JLabel statusLabel;

    // Parallel list storing [className, methodName] for each bookmark
    private final List<String[]> bookmarkData = new ArrayList<>();

    private Consumer<String[]> navigationCallback;

    public BookmarkProvider(Tool tool, String owner) {
        super(tool, "Bookmarks", owner);

        bookmarksModel = new DefaultListModel<>();
        bookmarksList = new JList<>(bookmarksModel);
        bookmarksList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bookmarksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookmarksList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && !e.isPopupTrigger()) {
                    handleDoubleClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        statusLabel = new JLabel("No bookmarks");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> clearAll());
        toolBar.add(clearButton);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(bookmarksList), BorderLayout.CENTER);
        mainPanel.add(toolBar, BorderLayout.SOUTH);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Bookmarks");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the callback invoked when the user double-clicks a bookmark entry.
     * The callback receives a two-element array: {@code [className, methodName]}.
     *
     * @param cb the navigation callback, or null to disable navigation
     */
    public void setNavigationCallback(Consumer<String[]> cb) {
        this.navigationCallback = cb;
    }

    /**
     * Adds a bookmark for the given class and method.
     * Does nothing if the bookmark already exists.
     *
     * @param className  the class name (simple name)
     * @param methodName the method name
     */
    public void addBookmark(String className, String methodName) {
        String display = className + "." + methodName;
        for (int i = 0; i < bookmarksModel.size(); i++) {
            if (bookmarksModel.getElementAt(i).equals(display)) {
                return;
            }
        }
        bookmarksModel.addElement(display);
        bookmarkData.add(new String[]{className, methodName});
        updateStatus();
    }

    private void handleDoubleClick() {
        int idx = bookmarksList.getSelectedIndex();
        if (idx < 0 || navigationCallback == null) {
            return;
        }
        navigationCallback.accept(bookmarkData.get(idx));
    }

    private void showContextMenu(MouseEvent e) {
        int idx = bookmarksList.locationToIndex(e.getPoint());
        if (idx < 0) {
            return;
        }
        bookmarksList.setSelectedIndex(idx);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Remove Bookmark");
        removeItem.addActionListener(ev -> removeBookmark(idx));
        menu.add(removeItem);
        menu.show(bookmarksList, e.getX(), e.getY());
    }

    private void removeBookmark(int idx) {
        if (idx < 0 || idx >= bookmarksModel.size()) {
            return;
        }
        bookmarksModel.remove(idx);
        bookmarkData.remove(idx);
        updateStatus();
    }

    private void clearAll() {
        bookmarksModel.clear();
        bookmarkData.clear();
        updateStatus();
    }

    private void updateStatus() {
        int count = bookmarksModel.size();
        statusLabel.setText(count == 0 ? "No bookmarks"
                : count + " bookmark" + (count == 1 ? "" : "s"));
    }
}
