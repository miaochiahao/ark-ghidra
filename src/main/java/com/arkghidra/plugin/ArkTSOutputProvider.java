package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import docking.ComponentProvider;
import docking.Tool;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import ghidra.util.HelpLocation;
import ghidra.util.Msg;

/**
 * Component provider that displays decompiled ArkTS source code
 * with syntax highlighting.
 *
 * <p>Provides a styled text pane with monospaced font for viewing
 * results, along with toolbar actions for copying to clipboard and
 * saving to file. ArkTS keywords, types, strings, comments,
 * decorators, and numbers are each rendered in a distinct color.</p>
 *
 * <p>Interactive features:</p>
 * <ul>
 *   <li>Click a symbol to highlight all occurrences in the current view.</li>
 *   <li>Right-click for context menu: Copy, Find All References, Copy Symbol Name.</li>
 *   <li>Ctrl+F opens an inline search bar; Escape closes it.</li>
 * </ul>
 */
public class ArkTSOutputProvider extends ComponentProvider {

    private static final String OWNER =
            ArkTSOutputProvider.class.getSimpleName();

    private static final int MAX_HISTORY = 20;

    private static class HistoryEntry {
        final String functionName;
        final String code;

        HistoryEntry(String functionName, String code) {
            this.functionName = functionName;
            this.code = code;
        }
    }

    private final Deque<HistoryEntry> backStack = new ArrayDeque<>();
    private final Deque<HistoryEntry> forwardStack = new ArrayDeque<>();

    private static boolean isDarkBackground() {
        Color bg = javax.swing.UIManager.getColor("TextArea.background");
        if (bg == null) {
            return false;
        }
        double luminance = 0.299 * bg.getRed()
                + 0.587 * bg.getGreen()
                + 0.114 * bg.getBlue();
        return luminance < 128;
    }

    private static class ColorScheme {
        final Color keyword;
        final Color type;
        final Color string;
        final Color comment;
        final Color decorator;
        final Color number;
        final Color modifier;
        final Color plain;
        final Color background;

        ColorScheme(boolean dark) {
            if (dark) {
                keyword    = new Color(0x82AAFF);
                type       = new Color(0x89DDFF);
                string     = new Color(0xC3E88D);
                comment    = new Color(0x546E7A);
                decorator  = new Color(0xC792EA);
                number     = new Color(0xF78C6C);
                modifier   = new Color(0x82AAFF);
                plain      = new Color(0xEEEEEE);
                background = new Color(0x263238);
            } else {
                keyword    = new Color(0x0000CC);
                type       = new Color(0x008080);
                string     = new Color(0x008000);
                comment    = new Color(0x808080);
                decorator  = new Color(0x800080);
                number     = new Color(0xFF8C00);
                modifier   = new Color(0x0000CC);
                plain      = Color.BLACK;
                background = Color.WHITE;
            }
        }
    }

    // Highlight colors
    private static final Color COLOR_SYMBOL_HIGHLIGHT = new Color(0xFFE082);
    private static final Color COLOR_SEARCH_HIGHLIGHT = new Color(0xA5D6A7);
    private static final Color COLOR_SEARCH_CURRENT = new Color(0xFF8A65);

    private final JPanel mainPanel;
    private final JTextPane codePane;
    private final JLabel headerLabel;
    private final JLabel statusBar;
    private final ArkTSColorizer colorizer;
    private final SymbolHighlighter symbolHighlighter;

    // Search bar components
    private final JPanel searchPanel;
    private JTextField searchField;
    private JLabel searchStatusLabel;

    // Font size state
    private int currentFontSize = 13;
    private String lastFunctionName = "";
    private String lastCode = "";

    // Highlight state
    private String currentHighlightedWord = "";
    private List<Integer> searchMatchPositions = java.util.Collections.emptyList();
    private int searchMatchIndex = -1;
    private Object currentLineHighlight = null;

    private Runnable decompileFileCallback;
    private Consumer<String> symbolHighlightCallback;
    private Consumer<String> jumpToDefinitionCallback;
    private Runnable globalSearchCallback;

    private JButton backButton;
    private JButton forwardButton;
    private JToggleButton autoDecompileButton;
    private final JLabel loadingLabel;

    public ArkTSOutputProvider(Tool tool, String owner) {
        super(tool, "ArkTS Output", owner);

        colorizer = new ArkTSColorizer();
        symbolHighlighter = new SymbolHighlighter();

        loadingLabel = new JLabel("  ");
        loadingLabel.setForeground(Color.GRAY);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(11f));

        codePane = new JTextPane() {
            @Override
            public String getToolTipText(MouseEvent event) {
                return computeTooltip(event);
            }
        };
        codePane.setEditable(false);
        codePane.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ToolTipManager.sharedInstance().registerComponent(codePane);
        ToolTipManager.sharedInstance().setDismissDelay(3000);
        installClickToHighlight();
        installCtrlFBinding();
        installZoomBindings();
        installNavBindings();

        statusBar = new JLabel("Ready");
        statusBar.setFont(statusBar.getFont().deriveFont(11f));
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(0xF0F0F0));

        codePane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                updateStatusBar();
            }
        });

        JScrollPane scrollPane = new JScrollPane(codePane);
        scrollPane.setRowHeaderView(new LineNumberComponent(codePane));

        headerLabel = new JLabel("No decompiled code");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        addToolbarButtons(toolBar);

        searchPanel = buildSearchPanel();
        searchPanel.setVisible(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusBar, BorderLayout.NORTH);
        bottomPanel.add(toolBar, BorderLayout.CENTER);

        mainPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(loadingLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setDefaultWindowPosition(docking.WindowPosition.BOTTOM);
        setTitle("ArkTS Output");

        createCopyAction();
        createSaveAction();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Displays decompiled ArkTS source code with syntax highlighting.
     *
     * @param functionName the name of the decompiled function
     * @param code the decompiled source code
     */
    public void showDecompiledCode(String functionName, String code) {
        if (!lastCode.isEmpty() && !lastCode.equals(code)) {
            if (backStack.size() >= MAX_HISTORY) {
                backStack.pollLast();
            }
            backStack.push(new HistoryEntry(lastFunctionName, lastCode));
            forwardStack.clear();
        }
        headerLabel.setText("Decompiled: " + functionName);
        lastFunctionName = functionName;
        lastCode = code;
        currentHighlightedWord = "";
        searchMatchPositions = java.util.Collections.emptyList();
        searchMatchIndex = -1;
        renderHighlightedCode(code);
        updateNavButtons();
    }

    /**
     * Shows an informational message in the output area.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        headerLabel.setText("Info");
        codePane.setText(message);
    }

    /**
     * Gets the currently displayed code text.
     *
     * @return the code text
     */
    public String getCodeText() {
        return codePane.getText();
    }

    /**
     * Gets the word currently highlighted by click-to-highlight.
     *
     * @return the highlighted word, or empty string if none
     */
    public String getCurrentHighlightedWord() {
        return currentHighlightedWord;
    }

    // --- Status bar ---

    private void updateStatusBar() {
        int pos = codePane.getCaretPosition();
        String text = codePane.getText();
        if (text == null) {
            text = "";
        }
        int line = 1;
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        int col;
        if (pos == 0) {
            col = 1;
        } else {
            int lastNewline = text.lastIndexOf('\n', pos - 1);
            col = pos - lastNewline;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Ln ").append(line).append(", Col ").append(col);
        if (currentHighlightedWord != null && !currentHighlightedWord.isEmpty()) {
            int count = symbolHighlighter.findAllOccurrences(text, currentHighlightedWord).size();
            sb.append("  |  \"").append(currentHighlightedWord).append("\" \u2014 ")
                    .append(count).append(" occurrences");
        }
        statusBar.setText(sb.toString());
        highlightCurrentLine();
    }

    private void highlightCurrentLine() {
        Highlighter highlighter = codePane.getHighlighter();
        if (currentLineHighlight != null) {
            highlighter.removeHighlight(currentLineHighlight);
            currentLineHighlight = null;
        }
        try {
            int pos = codePane.getCaretPosition();
            String text = codePane.getText();
            if (text == null || text.isEmpty()) {
                return;
            }
            int lineStart = pos;
            while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }
            int lineEnd = pos;
            while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
                lineEnd++;
            }
            Color lineHighlightColor = isDarkBackground()
                    ? new Color(0x37474F)
                    : new Color(0xE8F5E9);
            currentLineHighlight = highlighter.addHighlight(
                    lineStart, lineEnd,
                    new DefaultHighlighter.DefaultHighlightPainter(lineHighlightColor));
        } catch (BadLocationException e) {
            // ignore
        }
    }

    // --- Line number gutter ---

    private static class LineNumberComponent extends JComponent
            implements DocumentListener {

        private final JTextPane textPane;
        private static final int WIDTH = 40;

        LineNumberComponent(JTextPane textPane) {
            this.textPane = textPane;
            setPreferredSize(new Dimension(WIDTH, 0));
            setBackground(new Color(0xF5F5F5));
            setOpaque(true);
            textPane.getDocument().addDocumentListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(textPane.getFont());
            g.setColor(Color.GRAY);

            String text = textPane.getText();
            if (text == null || text.isEmpty()) {
                return;
            }

            String[] lines = text.split("\n", -1);
            FontMetrics fm = g.getFontMetrics();
            int lineHeight = fm.getHeight();
            int y = fm.getAscent() + 2;

            for (int i = 1; i <= lines.length; i++) {
                String num = String.valueOf(i);
                int x = WIDTH - fm.stringWidth(num) - 4;
                g.drawString(num, x, y);
                y += lineHeight;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            repaint();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            repaint();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            repaint();
        }
    }

    // --- Click-to-highlight ---

    private void installClickToHighlight() {
        codePane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        handleJumpToDefinition(e);
                    } else if (e.getClickCount() == 1) {
                        handleSymbolClick(e);
                    }
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
    }

    private void showContextMenu(MouseEvent e) {
        int offset = codePane.viewToModel2D(e.getPoint());
        JPopupMenu menu = buildContextMenu(offset);
        menu.show(codePane, e.getX(), e.getY());
    }

    private JPopupMenu buildContextMenu(int offset) {
        String text = codePane.getText();
        String word = symbolHighlighter.extractWordAt(text, offset);
        boolean hasWord = word != null && !word.isEmpty();

        JPopupMenu menu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            String selected = codePane.getSelectedText();
            String toCopy = (selected != null && !selected.isEmpty())
                    ? selected
                    : codePane.getText();
            if (toCopy != null && !toCopy.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(toCopy), null);
            }
        });
        menu.add(copyItem);

        JMenuItem findRefsItem = new JMenuItem("Find All References");
        findRefsItem.setEnabled(hasWord);
        if (hasWord) {
            final String capturedWord = word;
            findRefsItem.addActionListener(e -> {
                currentHighlightedWord = capturedWord;
                applySymbolHighlights(text, capturedWord);
            });
        }
        menu.add(findRefsItem);

        JMenuItem gotoDefItem = new JMenuItem("Go to Definition");
        gotoDefItem.setEnabled(hasWord && jumpToDefinitionCallback != null);
        if (hasWord && jumpToDefinitionCallback != null) {
            final String capturedWord = word;
            gotoDefItem.addActionListener(e -> jumpToDefinitionCallback.accept(capturedWord));
        }
        menu.add(gotoDefItem);

        JMenuItem copySymbolItem = new JMenuItem("Copy Symbol Name");
        copySymbolItem.setEnabled(hasWord);
        if (hasWord) {
            final String capturedWord = word;
            copySymbolItem.addActionListener(e ->
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(capturedWord), null)
            );
        }
        menu.add(copySymbolItem);

        menu.addSeparator();

        JMenuItem addCommentItem = new JMenuItem("Add Comment...");
        addCommentItem.addActionListener(e -> addCommentAtOffset(offset));
        menu.add(addCommentItem);

        JMenuItem renameItem = new JMenuItem("Rename Symbol...");
        renameItem.setEnabled(hasWord);
        if (hasWord) {
            final String capturedWord = word;
            renameItem.addActionListener(e -> renameSymbol(capturedWord));
        }
        menu.add(renameItem);

        return menu;
    }

    private void addCommentAtOffset(int offset) {
        String comment = JOptionPane.showInputDialog(
                mainPanel, "Enter comment:", "Add Comment",
                JOptionPane.PLAIN_MESSAGE);
        if (comment == null || comment.isEmpty()) {
            return;
        }
        try {
            StyledDocument doc = codePane.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            int lineEnd = text.indexOf('\n', offset);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            String insertion = "  // user: " + comment;
            SimpleAttributeSet commentStyle = createStyle(
                    new ColorScheme(isDarkBackground()).comment, false);
            doc.insertString(lineEnd, insertion, commentStyle);
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Failed to insert comment: " + ex.getMessage());
        }
    }

    private void renameSymbol(String oldName) {
        String newName = (String) JOptionPane.showInputDialog(
                mainPanel, "Rename \"" + oldName + "\" to:",
                "Rename Symbol", JOptionPane.PLAIN_MESSAGE,
                null, null, oldName);
        if (newName == null || newName.isEmpty()
                || newName.equals(oldName)) {
            return;
        }
        try {
            StyledDocument doc = codePane.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            List<Integer> positions =
                    symbolHighlighter.findAllOccurrences(text, oldName);
            for (int i = positions.size() - 1; i >= 0; i--) {
                int pos = positions.get(i);
                doc.remove(pos, oldName.length());
                doc.insertString(pos, newName,
                        createStyle(new ColorScheme(isDarkBackground()).plain, false));
            }
            if (currentHighlightedWord.equals(oldName)) {
                currentHighlightedWord = newName;
            }
            Msg.info(OWNER, "Renamed \"" + oldName + "\" to \""
                    + newName + "\" (" + positions.size() + " occurrences)");
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Failed to rename symbol: " + ex.getMessage());
        }
    }

    private void handleJumpToDefinition(MouseEvent e) {
        if (jumpToDefinitionCallback == null) {
            return;
        }
        int offset = codePane.viewToModel2D(e.getPoint());
        String text = codePane.getText();
        String word = symbolHighlighter.extractWordAt(text, offset);
        if (!word.isEmpty()) {
            jumpToDefinitionCallback.accept(word);
        }
    }

    private void handleSymbolClick(MouseEvent e) {
        int offset = codePane.viewToModel2D(e.getPoint());
        String text = codePane.getText();
        String word = symbolHighlighter.extractWordAt(text, offset);

        if (word.isEmpty()) {
            clearSymbolHighlights();
            currentHighlightedWord = "";
            updateStatusBar();
            return;
        }
        if (word.equals(currentHighlightedWord)) {
            clearSymbolHighlights();
            currentHighlightedWord = "";
            updateStatusBar();
            return;
        }

        currentHighlightedWord = word;
        applySymbolHighlights(text, word);
    }

    private void applySymbolHighlights(String text, String word) {
        Highlighter highlighter = codePane.getHighlighter();
        highlighter.removeAllHighlights();
        highlightCurrentLine();
        List<Integer> positions =
                symbolHighlighter.findAllOccurrences(text, word);
        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(
                        COLOR_SYMBOL_HIGHLIGHT);
        for (int pos : positions) {
            try {
                highlighter.addHighlight(pos, pos + word.length(), painter);
            } catch (BadLocationException ex) {
                Msg.warn(OWNER, "Highlight error: " + ex.getMessage());
            }
        }
        updateStatusBar();
        if (symbolHighlightCallback != null) {
            symbolHighlightCallback.accept(word);
        }
    }

    private void clearSymbolHighlights() {
        codePane.getHighlighter().removeAllHighlights();
        highlightCurrentLine();
        updateStatusBar();
        if (symbolHighlightCallback != null) {
            symbolHighlightCallback.accept("");
        }
    }

    // --- Ctrl+F search bar ---

    private void installCtrlFBinding() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, mask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlF, "openSearch");
        codePane.getActionMap().put("openSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSearchBar();
            }
        });

        KeyStroke ctrlShiftF = KeyStroke.getKeyStroke(
                KeyEvent.VK_F, mask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftF, "openGlobalSearch");
        codePane.getActionMap().put("openGlobalSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (globalSearchCallback != null) {
                    globalSearchCallback.run();
                }
            }
        });
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(2, 4, 2, 4));

        searchField = new JTextField(20);
        searchStatusLabel = new JLabel("  ");

        JButton prevButton = new JButton("\u25B2");
        prevButton.setToolTipText("Previous match");
        prevButton.addActionListener(e -> navigateSearch(-1));

        JButton nextButton = new JButton("\u25BC");
        nextButton.setToolTipText("Next match");
        nextButton.addActionListener(e -> navigateSearch(1));

        JButton closeButton = new JButton("\u00D7");
        closeButton.setToolTipText("Close search (Esc)");
        closeButton.addActionListener(e -> closeSearchBar());

        searchField.addActionListener(e -> navigateSearch(1));
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        runSearch();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        runSearch();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        runSearch();
                    }
                });

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        searchField.getInputMap().put(escape, "closeSearch");
        searchField.getActionMap().put("closeSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeSearchBar();
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(prevButton);
        buttons.add(nextButton);
        buttons.add(searchStatusLabel);
        buttons.add(closeButton);

        panel.add(new JLabel("Find: "), BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    private void openSearchBar() {
        searchPanel.setVisible(true);
        searchField.requestFocusInWindow();
        searchField.selectAll();
        mainPanel.revalidate();
    }

    private void closeSearchBar() {
        searchPanel.setVisible(false);
        clearSearchHighlights();
        searchMatchPositions = java.util.Collections.emptyList();
        searchMatchIndex = -1;
        codePane.requestFocusInWindow();
        mainPanel.revalidate();
    }

    private void runSearch() {
        clearSearchHighlights();
        String query = searchField.getText();
        if (query.isEmpty()) {
            searchStatusLabel.setText("  ");
            searchMatchPositions = java.util.Collections.emptyList();
            searchMatchIndex = -1;
            return;
        }
        String text = codePane.getText();
        searchMatchPositions = findAllSubstrings(text, query);
        searchMatchIndex = searchMatchPositions.isEmpty() ? -1 : 0;
        applySearchHighlights(query);
        updateSearchStatus();
        scrollToCurrentMatch(query);
    }

    private void navigateSearch(int direction) {
        if (searchMatchPositions.isEmpty()) {
            return;
        }
        searchMatchIndex =
                (searchMatchIndex + direction + searchMatchPositions.size())
                        % searchMatchPositions.size();
        String query = searchField.getText();
        applySearchHighlights(query);
        updateSearchStatus();
        scrollToCurrentMatch(query);
    }

    private void applySearchHighlights(String query) {
        Highlighter highlighter = codePane.getHighlighter();
        highlighter.removeAllHighlights();
        Highlighter.HighlightPainter normalPainter =
                new DefaultHighlighter.DefaultHighlightPainter(
                        COLOR_SEARCH_HIGHLIGHT);
        Highlighter.HighlightPainter currentPainter =
                new DefaultHighlighter.DefaultHighlightPainter(
                        COLOR_SEARCH_CURRENT);
        for (int i = 0; i < searchMatchPositions.size(); i++) {
            int pos = searchMatchPositions.get(i);
            Highlighter.HighlightPainter painter =
                    (i == searchMatchIndex) ? currentPainter : normalPainter;
            try {
                highlighter.addHighlight(
                        pos, pos + query.length(), painter);
            } catch (BadLocationException ex) {
                Msg.warn(OWNER, "Search highlight error: " + ex.getMessage());
            }
        }
    }

    private void scrollToCurrentMatch(String query) {
        if (searchMatchIndex < 0
                || searchMatchIndex >= searchMatchPositions.size()) {
            return;
        }
        int pos = searchMatchPositions.get(searchMatchIndex);
        codePane.setCaretPosition(pos);
        try {
            codePane.scrollRectToVisible(
                    codePane.modelToView2D(pos).getBounds());
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Scroll error: " + ex.getMessage());
        }
    }

    private void updateSearchStatus() {
        if (searchMatchPositions.isEmpty()) {
            searchStatusLabel.setText(" No matches");
        } else {
            searchStatusLabel.setText(
                    " " + (searchMatchIndex + 1)
                            + "/" + searchMatchPositions.size());
        }
    }

    private void clearSearchHighlights() {
        codePane.getHighlighter().removeAllHighlights();
    }

    static List<Integer> findAllSubstrings(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Integer> positions = new java.util.ArrayList<>();
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int fromIndex = 0;
        while (fromIndex <= text.length() - query.length()) {
            int idx = lowerText.indexOf(lowerQuery, fromIndex);
            if (idx < 0) {
                break;
            }
            positions.add(idx);
            fromIndex = idx + 1;
        }
        return positions;
    }

    // --- Toolbar ---

    private void addToolbarButtons(JToolBar toolBar) {
        autoDecompileButton = new JToggleButton("Auto");
        autoDecompileButton.setSelected(true);
        autoDecompileButton.setToolTipText(
                "Auto-decompile when cursor moves to a function in the Listing");
        toolBar.add(autoDecompileButton);
        toolBar.addSeparator();

        backButton = new JButton("\u25C4");
        backButton.setToolTipText("Navigate back (Alt+Left)");
        backButton.setEnabled(false);
        backButton.addActionListener(e -> navigateBack());
        toolBar.add(backButton);

        forwardButton = new JButton("\u25BA");
        forwardButton.setToolTipText("Navigate forward (Alt+Right)");
        forwardButton.setEnabled(false);
        forwardButton.addActionListener(e -> navigateForward());
        toolBar.add(forwardButton);
        toolBar.addSeparator();

        JButton decompileFileButton = new JButton("Decompile File");
        decompileFileButton.setToolTipText("Decompile the entire ABC file");
        decompileFileButton.addActionListener(e -> {
            if (decompileFileCallback != null) {
                decompileFileCallback.run();
            }
        });
        toolBar.add(decompileFileButton);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());
        toolBar.add(copyButton);

        JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(e -> saveToFile());
        toolBar.add(saveButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearOutput());
        toolBar.add(clearButton);

        toolBar.addSeparator();
        JButton zoomInButton = new JButton("A+");
        zoomInButton.setToolTipText("Increase font size (Ctrl+=)");
        zoomInButton.addActionListener(e -> setFontSize(currentFontSize + 1));
        toolBar.add(zoomInButton);
        JButton zoomOutButton = new JButton("A-");
        zoomOutButton.setToolTipText("Decrease font size (Ctrl+-)");
        zoomOutButton.addActionListener(e -> setFontSize(currentFontSize - 1));
        toolBar.add(zoomOutButton);
        toolBar.addSeparator();
        JButton helpButton = new JButton("?");
        helpButton.setToolTipText("Keyboard shortcuts");
        helpButton.addActionListener(e -> showKeyboardShortcuts());
        toolBar.add(helpButton);
    }

    private void showKeyboardShortcuts() {
        String shortcuts =
                "Keyboard Shortcuts\n"
                + "------------------------------\n"
                + "Ctrl+F          Find / Search\n"
                + "Escape          Close search bar\n"
                + "Alt+Left        Navigate back\n"
                + "Alt+Right       Navigate forward\n"
                + "Ctrl+=          Increase font size\n"
                + "Ctrl+-          Decrease font size\n"
                + "Ctrl+0          Reset font size\n"
                + "Click           Highlight all occurrences\n"
                + "Double-click    Jump to definition\n"
                + "Right-click     Context menu\n"
                + "Hover           Show occurrence count\n";
        javax.swing.JOptionPane.showMessageDialog(
                mainPanel, shortcuts, "ArkTS Output \u2014 Shortcuts",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void createCopyAction() {
        DockingAction copyAction = new DockingAction("Copy", getOwner()) {
            @Override
            public void actionPerformed(docking.ActionContext context) {
                copyToClipboard();
            }
        };
        copyAction.setToolBarData(new ToolBarData(null, "output"));
        copyAction.setEnabled(true);
        copyAction.setDescription("Copy decompiled code to clipboard");
        copyAction.setHelpLocation(
                new HelpLocation(getOwner(), "Copy_Action"));
        addLocalAction(copyAction);
    }

    private void createSaveAction() {
        DockingAction saveAction = new DockingAction("Save", getOwner()) {
            @Override
            public void actionPerformed(docking.ActionContext context) {
                saveToFile();
            }
        };
        saveAction.setToolBarData(new ToolBarData(null, "output"));
        saveAction.setEnabled(true);
        saveAction.setDescription("Save decompiled code to file");
        saveAction.setHelpLocation(
                new HelpLocation(getOwner(), "Save_Action"));
        addLocalAction(saveAction);
    }

    // --- Rendering ---

    private void renderHighlightedCode(String code) {
        StyledDocument doc = codePane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            Msg.warn(OWNER, "Failed to clear document: " + e.getMessage());
            codePane.setText(code);
            return;
        }

        java.util.List<java.util.List<ArkTSColorizer.StyledSegment>>
                allLines = colorizer.colorizeSource(code);

        ColorScheme cs = new ColorScheme(isDarkBackground());
        codePane.setBackground(cs.background);
        SimpleAttributeSet plainStyle = createStyle(cs.plain, false);
        SimpleAttributeSet keywordStyle = createStyle(cs.keyword, true);
        SimpleAttributeSet typeStyle = createStyle(cs.type, true);
        SimpleAttributeSet stringStyle = createStyle(cs.string, false);
        SimpleAttributeSet commentStyle = createStyle(cs.comment, false);
        SimpleAttributeSet decoratorStyle = createStyle(cs.decorator, true);
        SimpleAttributeSet numberStyle = createStyle(cs.number, false);
        SimpleAttributeSet modifierStyle = createStyle(cs.modifier, true);

        for (int i = 0; i < allLines.size(); i++) {
            java.util.List<ArkTSColorizer.StyledSegment> lineSegments =
                    allLines.get(i);
            for (ArkTSColorizer.StyledSegment seg : lineSegments) {
                SimpleAttributeSet style =
                        styleForType(seg.getTokenType(),
                                plainStyle, keywordStyle, typeStyle,
                                stringStyle, commentStyle,
                                decoratorStyle, numberStyle,
                                modifierStyle);
                try {
                    doc.insertString(doc.getLength(),
                            seg.getText(), style);
                } catch (BadLocationException e) {
                    Msg.warn(OWNER,
                            "Failed to insert styled text: "
                                    + e.getMessage());
                }
            }
            if (i < allLines.size() - 1) {
                try {
                    doc.insertString(doc.getLength(), "\n", plainStyle);
                } catch (BadLocationException e) {
                    Msg.warn(OWNER,
                            "Failed to insert newline: "
                                    + e.getMessage());
                }
            }
        }

        codePane.setCaretPosition(0);
    }

    private static SimpleAttributeSet styleForType(
            ArkTSColorizer.TokenType type,
            SimpleAttributeSet plainStyle,
            SimpleAttributeSet keywordStyle,
            SimpleAttributeSet typeStyle,
            SimpleAttributeSet stringStyle,
            SimpleAttributeSet commentStyle,
            SimpleAttributeSet decoratorStyle,
            SimpleAttributeSet numberStyle,
            SimpleAttributeSet modifierStyle) {
        switch (type) {
            case KEYWORD:
                return keywordStyle;
            case TYPE:
                return typeStyle;
            case STRING:
                return stringStyle;
            case COMMENT:
                return commentStyle;
            case DECORATOR:
                return decoratorStyle;
            case NUMBER:
                return numberStyle;
            case MODIFIER:
                return modifierStyle;
            default:
                return plainStyle;
        }
    }

    private SimpleAttributeSet createStyle(Color color, boolean bold) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, currentFontSize);
        return style;
    }

    // --- Font size zoom ---

    private void setFontSize(int size) {
        currentFontSize = Math.max(8, Math.min(24, size));
        codePane.setFont(new Font("Monospaced", Font.PLAIN, currentFontSize));
        if (!lastCode.isEmpty()) {
            renderHighlightedCode(lastCode);
        }
    }

    private void installZoomBindings() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        KeyStroke zoomIn = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, mask);
        KeyStroke zoomInPlus = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, mask);
        KeyStroke zoomOut = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mask);
        KeyStroke zoomReset = KeyStroke.getKeyStroke(KeyEvent.VK_0, mask);

        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(zoomIn, "zoomIn");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(zoomInPlus, "zoomIn");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(zoomOut, "zoomOut");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(zoomReset, "zoomReset");

        codePane.getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFontSize(currentFontSize + 1);
            }
        });
        codePane.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFontSize(currentFontSize - 1);
            }
        });
        codePane.getActionMap().put("zoomReset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFontSize(13);
            }
        });
    }

    // --- Clipboard / File ---

    private void copyToClipboard() {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
        Msg.info(OWNER, "Copied decompiled code to clipboard");
    }

    private void saveToFile() {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            Msg.warn(OWNER, "No code to save");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Decompiled ArkTS");
        chooser.setSelectedFile(new File("decompiled.ets"));
        int result = chooser.showSaveDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedWriter writer =
                    new BufferedWriter(new FileWriter(file))) {
                writer.write(text);
                Msg.info(OWNER,
                        "Saved decompiled code to " + file.getPath());
            } catch (IOException e) {
                Msg.error(OWNER,
                        "Failed to save file: " + e.getMessage(), e);
            }
        }
    }

    private void clearOutput() {
        codePane.setText("");
        headerLabel.setText("No decompiled code");
        currentHighlightedWord = "";
        closeSearchBar();
        updateNavButtons();
    }

    // --- Navigation history ---

    /**
     * Navigates back to the previously viewed decompiled code.
     */
    public void navigateBack() {
        if (backStack.isEmpty()) {
            return;
        }
        HistoryEntry prev = backStack.pop();
        if (!lastCode.isEmpty()) {
            forwardStack.push(new HistoryEntry(lastFunctionName, lastCode));
        }
        headerLabel.setText("Decompiled: " + prev.functionName);
        lastFunctionName = prev.functionName;
        lastCode = prev.code;
        currentHighlightedWord = "";
        searchMatchPositions = java.util.Collections.emptyList();
        searchMatchIndex = -1;
        renderHighlightedCode(prev.code);
        updateNavButtons();
    }

    /**
     * Navigates forward to the next viewed decompiled code.
     */
    public void navigateForward() {
        if (forwardStack.isEmpty()) {
            return;
        }
        HistoryEntry next = forwardStack.pop();
        if (!lastCode.isEmpty()) {
            backStack.push(new HistoryEntry(lastFunctionName, lastCode));
        }
        headerLabel.setText("Decompiled: " + next.functionName);
        lastFunctionName = next.functionName;
        lastCode = next.code;
        currentHighlightedWord = "";
        searchMatchPositions = java.util.Collections.emptyList();
        searchMatchIndex = -1;
        renderHighlightedCode(next.code);
        updateNavButtons();
    }

    private void updateNavButtons() {
        if (backButton != null) {
            backButton.setEnabled(!backStack.isEmpty());
        }
        if (forwardButton != null) {
            forwardButton.setEnabled(!forwardStack.isEmpty());
        }
    }

    private void installNavBindings() {
        int altMask = java.awt.event.InputEvent.ALT_DOWN_MASK;
        KeyStroke altLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, altMask);
        KeyStroke altRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, altMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(altLeft, "navBack");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(altRight, "navForward");
        codePane.getActionMap().put("navBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateBack();
            }
        });
        codePane.getActionMap().put("navForward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateForward();
            }
        });
    }

    // --- Tooltip ---

    private String computeTooltip(MouseEvent event) {
        int offset = codePane.viewToModel2D(event.getPoint());
        String text = codePane.getText();
        String word = symbolHighlighter.extractWordAt(text, offset);
        if (word.isEmpty() || ArkTSColorizer.isKeyword(word)) {
            return null;
        }
        int count = symbolHighlighter.findAllOccurrences(text, word).size();
        if (count < 2) {
            return null;
        }
        return "\"" + word + "\" \u2014 " + count + " occurrences";
    }

    /**
     * Sets the callback invoked when the user clicks the "Decompile File" toolbar button.
     *
     * @param callback the runnable to invoke, or null to disable the button action
     */
    public void setDecompileFileCallback(Runnable callback) {
        this.decompileFileCallback = callback;
    }

    /**
     * Sets a callback that is invoked whenever the highlighted symbol changes.
     * The callback receives the new symbol name, or an empty string when highlights are cleared.
     *
     * @param callback the consumer to invoke, or null to disable
     */
    public void setSymbolHighlightCallback(Consumer<String> callback) {
        this.symbolHighlightCallback = callback;
    }

    /**
     * Sets a callback invoked when the user double-clicks a word or selects "Go to Definition".
     * The callback receives the word under the cursor so the caller can navigate to its definition.
     *
     * @param callback the consumer to invoke, or null to disable
     */
    public void setJumpToDefinitionCallback(Consumer<String> callback) {
        this.jumpToDefinitionCallback = callback;
    }

    /**
     * Sets a callback invoked when the user presses Ctrl+Shift+F to open the global search panel.
     *
     * @param callback the runnable to invoke, or null to disable
     */
    public void setGlobalSearchCallback(Runnable callback) {
        this.globalSearchCallback = callback;
    }

    /**
     * Returns the last decompiled code text shown in this panel.
     *
     * @return the last code, or empty string if none
     */
    public String getLastCode() {
        return lastCode;
    }

    /**
     * Returns the name of the last decompiled function.
     *
     * @return the last function name, or empty string if none
     */
    public String getLastFunctionName() {
        return lastFunctionName;
    }

    /**
     * Returns whether the auto-decompile toggle is currently enabled.
     *
     * @return true if auto-decompile is on
     */
    public boolean isAutoDecompileEnabled() {
        return autoDecompileButton != null && autoDecompileButton.isSelected();
    }

    /**
     * Shows a loading message in the header area.
     *
     * @param message the message to display
     */
    public void showLoading(String message) {
        loadingLabel.setText(message + "  ");
    }

    /**
     * Clears the loading message from the header area.
     */
    public void hideLoading() {
        loadingLabel.setText("  ");
    }

    /**
     * Returns the symbol highlighter used by this panel.
     *
     * @return the symbol highlighter
     */
    public SymbolHighlighter getSymbolHighlighter() {
        return symbolHighlighter;
    }

    /**
     * Scrolls the code pane to the given character offset and requests focus.
     *
     * @param offset the character offset to navigate to
     */
    public void scrollToOffset(int offset) {
        if (offset < 0) {
            return;
        }
        try {
            codePane.setCaretPosition(offset);
            codePane.scrollRectToVisible(
                    codePane.modelToView2D(offset).getBounds());
            codePane.requestFocusInWindow();
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "scrollToOffset error: " + ex.getMessage());
        }
    }
}
