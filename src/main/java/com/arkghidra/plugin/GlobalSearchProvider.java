package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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

    private final JPanel mainPanel;
    private final JTextField searchField;
    private final JLabel statusLabel;
    private final DefaultListModel<String> resultsModel;
    private final JList<String> resultsList;

    // Parallel lists storing the class/method for each displayed result entry
    private final List<String> resultMethodNames = new ArrayList<>();
    private final List<String> resultClassNames = new ArrayList<>();

    // Callback: double-click on a result navigates to that method (classAndMethod[0/1])
    private Consumer<String[]> navigationCallback;

    // Callback: triggered when the user clicks Search — receives the query string
    private Consumer<String> searchCallback;

    public GlobalSearchProvider(Tool tool, String owner) {
        super(tool, "Global Search", owner);

        searchField = new JTextField(20);
        statusLabel = new JLabel("Enter a search term");

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> triggerSearch());

        searchField.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        searchField.getActionMap().put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerSearch();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout(4, 0));
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        resultsModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsModel);
        resultsList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
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
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }
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
