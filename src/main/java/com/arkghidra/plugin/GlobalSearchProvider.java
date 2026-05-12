package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel that searches for a string across all decompiled methods in the current ABC file.
 *
 * <p>Provides a search field and button at the top, a results list showing
 * {@code ClassName.methodName [Ln]: matchingLine}, and double-click navigation to the matched
 * method.</p>
 */
public class GlobalSearchProvider extends ComponentProvider {

    private static final int MAX_HISTORY = 20;

    private final JPanel mainPanel;
    private final JComboBox<String> searchCombo;
    private final DefaultComboBoxModel<String> historyModel;
    private final JLabel statusLabel;
    private final DefaultListModel<String> resultsModel;
    private final JList<String> resultsList;
    private final LinkedList<String> searchHistory = new LinkedList<>();

    // Parallel lists storing the class/method for each displayed result entry
    private final List<String> resultMethodNames = new ArrayList<>();
    private final List<String> resultClassNames = new ArrayList<>();
    private final List<String> resultContexts = new ArrayList<>();

    // Callback: double-click on a result navigates to that method (classAndMethod[0/1])
    private Consumer<String[]> navigationCallback;

    // Callback: triggered when the user clicks Search — receives the query string
    private Consumer<String> searchCallback;

    public GlobalSearchProvider(Tool tool, String owner) {
        super(tool, "Global Search", owner);

        historyModel = new DefaultComboBoxModel<>();
        searchCombo = new JComboBox<>(historyModel);
        searchCombo.setEditable(true);
        searchCombo.setPrototypeDisplayValue("Search query...");
        statusLabel = new JLabel("Enter a search term");

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> triggerSearch());

        // Enter key in the combo box editor triggers search
        java.awt.Component editor = searchCombo.getEditor().getEditorComponent();
        if (editor instanceof JComponent) {
            ((JComponent) editor).getInputMap(JComponent.WHEN_FOCUSED)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
            ((JComponent) editor).getActionMap().put("search", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    triggerSearch();
                }
            });
        }

        JPanel topPanel = new JPanel(new BorderLayout(4, 0));
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchCombo, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        resultsModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsModel) {
            @Override
            public String getToolTipText(MouseEvent event) {
                int idx = locationToIndex(event.getPoint());
                if (idx >= 0 && idx < resultContexts.size()) {
                    String ctx = resultContexts.get(idx);
                    if (ctx != null && !ctx.isEmpty()) {
                        return "<html><pre style='font-family:monospace;font-size:11px'>"
                                + ctx.replace("&", "&amp;").replace("<", "&lt;")
                                        .replace(">", "&gt;")
                                + "</pre></html>";
                    }
                }
                return null;
            }
        };
        javax.swing.ToolTipManager.sharedInstance().registerComponent(resultsList);
        resultsList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleResultDoubleClick();
                }
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(topPanel, BorderLayout.NORTH);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultsList), BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Global Search");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the callback invoked when the user double-clicks a result entry.
     * The callback receives a two-element array: {@code [className, methodName]}.
     *
     * @param cb the navigation callback, or null to disable navigation
     */
    public void setNavigationCallback(Consumer<String[]> cb) {
        this.navigationCallback = cb;
    }

    /**
     * Sets the callback invoked when the user triggers a search.
     * The callback receives the trimmed query string.
     *
     * @param cb the search callback, or null to disable searching
     */
    public void setSearchCallback(Consumer<String> cb) {
        this.searchCallback = cb;
    }

    /**
     * Clears previous results and updates the status label to indicate a search is in progress.
     */
    public void clearResults() {
        resultsModel.clear();
        resultMethodNames.clear();
        resultClassNames.clear();
        resultContexts.clear();
        statusLabel.setText("Searching...");
    }

    /**
     * Adds a single search result entry to the results list.
     *
     * @param className  the class containing the match
     * @param methodName the method containing the match
     * @param lineText   the matching line text (will be trimmed for display)
     * @param lineNumber the 1-based line number within the decompiled method output
     */
    public void addResult(String className, String methodName,
            String lineText, int lineNumber) {
        String display = className + "." + methodName
                + " [L" + lineNumber + "]:  " + lineText.trim();
        resultsModel.addElement(display);
        resultMethodNames.add(methodName);
        resultClassNames.add(className);
        resultContexts.add(null); // context set separately via addResultWithContext
    }

    /**
     * Adds a result with surrounding context lines for tooltip display.
     *
     * @param className  the class containing the match
     * @param methodName the method containing the match
     * @param lineText   the matching line text
     * @param lineNumber the 1-based line number
     * @param context    surrounding lines for tooltip (may be null)
     */
    public void addResultWithContext(String className, String methodName,
            String lineText, int lineNumber, String context) {
        String display = className + "." + methodName
                + " [L" + lineNumber + "]:  " + lineText.trim();
        resultsModel.addElement(display);
        resultMethodNames.add(methodName);
        resultClassNames.add(className);
        resultContexts.add(context);
    }

    /**
     * Updates the status label after all results have been added.
     *
     * @param query the search query that was executed
     */
    public void finishSearch(String query) {
        int count = resultsModel.size();
        if (count == 0) {
            statusLabel.setText("No results for \"" + query + "\"");
        } else {
            statusLabel.setText(count + " result"
                    + (count == 1 ? "" : "s") + " for \"" + query + "\"");
        }
    }

    private void triggerSearch() {
        Object selected = searchCombo.getEditor().getItem();
        String query = selected != null ? selected.toString().trim() : "";
        if (query.isEmpty()) {
            return;
        }
        addToHistory(query);
        if (searchCallback != null) {
            clearResults();
            searchCallback.accept(query);
        }
    }

    private void addToHistory(String query) {
        searchHistory.remove(query);
        searchHistory.addFirst(query);
        while (searchHistory.size() > MAX_HISTORY) {
            searchHistory.removeLast();
        }
        historyModel.removeAllElements();
        for (String h : searchHistory) {
            historyModel.addElement(h);
        }
        searchCombo.getEditor().setItem(query);
    }

    /**
     * Pre-fills the search field with the given query and triggers a search immediately.
     * Used by external callers (e.g., "Show All Callers" context menu action).
     *
     * @param query the search string to pre-fill and execute
     */
    public void triggerSearch(String query) {
        searchCombo.getEditor().setItem(query);
        if (query.isEmpty()) {
            return;
        }
        addToHistory(query);
        if (searchCallback != null) {
            clearResults();
            searchCallback.accept(query);
        }
    }

    private void handleResultDoubleClick() {
        int idx = resultsList.getSelectedIndex();
        if (idx < 0 || navigationCallback == null) {
            return;
        }
        String className = resultClassNames.get(idx);
        String methodName = resultMethodNames.get(idx);
        navigationCallback.accept(new String[]{className, methodName});
    }
}
