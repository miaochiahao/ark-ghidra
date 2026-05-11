package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel that shows the navigation history as a visual list.
 *
 * <p>All previously viewed methods/classes are shown in chronological order
 * (most recent first). Clicking any entry restores that view via the
 * registered restore callback.</p>
 */
public class HistoryProvider extends ComponentProvider {

    private static final int MAX_HISTORY = 50;

    private final JPanel mainPanel;
    private final DefaultListModel<String> historyModel;
    private final JList<String> historyList;
    private final JLabel statusLabel;

    // Stores [functionName, code] for each history entry
    private final List<String[]> historyData = new ArrayList<>();

    // Callback: when user clicks an entry, restore that view
    private BiConsumer<String, String> restoreCallback;

    public HistoryProvider(Tool tool, String owner) {
        super(tool, "History", owner);

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleClick();
                }
            }
        });

        statusLabel = new JLabel("No history");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearHistory());
        toolBar.add(clearButton);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        mainPanel.add(toolBar, BorderLayout.SOUTH);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("History");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the callback invoked when the user clicks a history entry.
     * The callback receives the function name and the decompiled code text.
     *
     * @param cb the restore callback, or null to disable navigation
     */
    public void setRestoreCallback(BiConsumer<String, String> cb) {
        this.restoreCallback = cb;
    }

    /**
     * Records a navigation event. Adds to the top of the history list.
     * Removes any existing entry with the same function name before adding.
     *
     * @param functionName the display name (e.g. "login" or "Class: UserService")
     * @param code the decompiled code text
     */
    public void recordNavigation(String functionName, String code) {
        if (functionName == null || functionName.isEmpty()) {
            return;
        }
        // Remove existing entry with same name to avoid duplicates
        for (int i = historyData.size() - 1; i >= 0; i--) {
            if (historyData.get(i)[0].equals(functionName)) {
                historyData.remove(i);
                historyModel.remove(i);
            }
        }
        // Add at front (index 0 = most recent)
        historyData.add(0, new String[]{functionName, code});
        historyModel.add(0, functionName);
        // Trim to max
        while (historyData.size() > MAX_HISTORY) {
            historyData.remove(historyData.size() - 1);
            historyModel.remove(historyModel.size() - 1);
        }
        updateStatus();
    }

    private void handleClick() {
        int idx = historyList.getSelectedIndex();
        if (idx < 0 || restoreCallback == null) {
            return;
        }
        String[] entry = historyData.get(idx);
        restoreCallback.accept(entry[0], entry[1]);
    }

    private void clearHistory() {
        historyData.clear();
        historyModel.clear();
        updateStatus();
    }

    private void updateStatus() {
        int count = historyModel.size();
        statusLabel.setText(count == 0 ? "No history"
                : count + " entr" + (count == 1 ? "y" : "ies"));
    }
}
