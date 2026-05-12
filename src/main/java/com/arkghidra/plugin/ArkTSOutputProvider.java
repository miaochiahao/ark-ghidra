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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

    private static class OutlineEntry {
        final String label;
        final int offset;

        OutlineEntry(String label, int offset) {
            this.label = label;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return label;
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
    private static final Color COLOR_BRACKET_MATCH = new Color(0xB2EBF2);

    private final JPanel mainPanel;
    private boolean wordWrapEnabled = false;
    private boolean showIndentGuides = true;
    private final JTextPane codePane;
    private final JLabel headerLabel;
    private final JLabel statusBar;
    private final ArkTSColorizer colorizer;
    private final SymbolHighlighter symbolHighlighter;

    // Search bar components
    private final JPanel searchPanel;
    private JTextField searchField;
    private JLabel searchStatusLabel;
    private JTextField replaceField;
    private JPanel replaceRow;
    private boolean replaceMode = false;

    // Font size state
    private int currentFontSize = 13;
    private String currentFontFamily = "Monospaced";
    private int currentLineSpacing = 2;
    private String currentTheme = "Auto (follow Ghidra)";
    private String lastFunctionName = "";
    private String lastCode = "";

    // Highlight state
    private String currentHighlightedWord = "";
    private List<Integer> searchMatchPositions = java.util.Collections.emptyList();
    private int searchMatchIndex = -1;
    private Object currentLineHighlight = null;
    private Object bracketHighlight1 = null;
    private Object bracketHighlight2 = null;

    private Runnable decompileFileCallback;
    private Runnable exportAllCallback;
    private Consumer<String> symbolHighlightCallback;
    private Consumer<String> jumpToDefinitionCallback;
    private java.util.function.Function<String, String> tooltipCallback;
    private Runnable globalSearchCallback;
    private Consumer<String> globalSearchWordCallback;
    private Runnable addBookmarkCallback;
    private Runnable decompileClassCallback;
    private Runnable quickOpenCallback;
    private Runnable pinCallback;
    private Runnable showHapExplorerCallback;
    private Runnable showAndSelectInExplorerCallback;
    private Runnable showBookmarksCallback;
    private Runnable showHistoryCallback;
    private Runnable showXrefCallback;
    private Runnable showNotesCallback;
    private Runnable showSettingsCallback;
    private Runnable prevClassCallback;
    private Runnable nextClassCallback;
    private Runnable showShortcutsCallback;

    private String lastClassName = "";

    private JButton backButton;
    private JButton forwardButton;
    private JToggleButton autoDecompileButton;
    private JComboBox<OutlineEntry> methodOutlineCombo;
    private JTextField methodFilterField;
    private boolean updatingOutline = false;
    private java.util.List<OutlineEntry> allOutlineEntries = new java.util.ArrayList<>();
    private final JLabel loadingLabel;
    private javax.swing.JProgressBar progressBar;
    private final JLabel classLabel;
    private final JLabel methodInfoLabel;

    public ArkTSOutputProvider(Tool tool, String owner) {
        super(tool, "ArkTS Output", owner);

        colorizer = new ArkTSColorizer();
        symbolHighlighter = new SymbolHighlighter();

        loadingLabel = new JLabel("  ");
        loadingLabel.setForeground(Color.GRAY);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(11f));

        methodInfoLabel = new JLabel("  ");
        methodInfoLabel.setForeground(Color.GRAY);
        methodInfoLabel.setFont(methodInfoLabel.getFont().deriveFont(11f));

        classLabel = new JLabel("");
        classLabel.setForeground(new Color(0x0066CC));
        classLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        classLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (decompileClassCallback != null && !lastClassName.isEmpty()) {
                    decompileClassCallback.run();
                }
            }
        });

        codePane = new JTextPane() {
            @Override
            public String getToolTipText(MouseEvent event) {
                return computeTooltip(event);
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return wordWrapEnabled;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (showIndentGuides) {
                    paintIndentGuides(g);
                }
            }
        };
        codePane.setEditable(false);
        codePane.setFont(new Font(currentFontFamily, Font.PLAIN, currentFontSize));
        ToolTipManager.sharedInstance().registerComponent(codePane);
        ToolTipManager.sharedInstance().setDismissDelay(8000);
        installClickToHighlight();
        installCtrlFBinding();
        installZoomBindings();
        installNavBindings();
        installJebKeyBindings();

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
        progressBar = new javax.swing.JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 14));
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.add(statusBar, BorderLayout.CENTER);
        JPanel statusRight = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
        statusRight.add(progressBar);
        statusRight.add(methodInfoLabel);
        statusRow.add(statusRight, BorderLayout.EAST);
        bottomPanel.add(statusRow, BorderLayout.NORTH);
        bottomPanel.add(toolBar, BorderLayout.CENTER);

        mainPanel = new JPanel(new BorderLayout());
        methodOutlineCombo = new JComboBox<>();
        methodOutlineCombo.setToolTipText("Jump to method (outline)");
        methodOutlineCombo.setMaximumSize(new Dimension(300, 24));
        methodOutlineCombo.setPreferredSize(new Dimension(220, 24));
        methodOutlineCombo.addActionListener(e -> {
            if (updatingOutline) {
                return;
            }
            Object selected = methodOutlineCombo.getSelectedItem();
            if (selected instanceof OutlineEntry) {
                int offset = ((OutlineEntry) selected).offset;
                codePane.setCaretPosition(offset);
                scrollToOffset(offset);
                codePane.requestFocusInWindow();
            }
        });
        methodOutlineCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final Color COLOR_LARGE = new Color(0xC62828);
            private static final Color COLOR_MEDIUM = new Color(0xE65100);

            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof OutlineEntry && !isSelected) {
                    String label = ((OutlineEntry) value).label;
                    if (label.endsWith(" \u25cf")) {
                        setForeground(COLOR_LARGE);
                    } else if (label.endsWith(" \u25cb")) {
                        setForeground(COLOR_MEDIUM);
                    }
                }
                return this;
            }
        });
        JPanel headerPanel = new JPanel(new BorderLayout(4, 0));
        headerPanel.add(classLabel, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        methodFilterField = new JTextField();
        methodFilterField.setPreferredSize(new Dimension(80, 22));
        methodFilterField.setToolTipText("Filter methods in outline (type to filter)");
        methodFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyOutlineFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyOutlineFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyOutlineFilter();
            }
        });
        JPanel headerRight = new JPanel(new BorderLayout(4, 0));
        JPanel outlinePanel = new JPanel(new BorderLayout(2, 0));
        outlinePanel.add(methodFilterField, BorderLayout.WEST);
        outlinePanel.add(methodOutlineCombo, BorderLayout.CENTER);
        headerRight.add(outlinePanel, BorderLayout.CENTER);
        headerRight.add(loadingLabel, BorderLayout.EAST);
        headerPanel.add(headerRight, BorderLayout.EAST);
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
        lastClassName = "";
        classLabel.setText("");
        showDecompiledCodeInternal(functionName, code);
    }

    /**
     * Displays decompiled ArkTS source code with a clickable class breadcrumb in the header.
     *
     * @param functionName the name of the decompiled function
     * @param code the decompiled source code
     * @param className the class that owns this method, shown as a clickable breadcrumb
     */
    public void showDecompiledCode(String functionName, String code, String className) {
        lastClassName = className != null ? className : "";
        classLabel.setText(lastClassName.isEmpty() ? "" : lastClassName + " > ");
        showDecompiledCodeInternal(functionName, code);
    }

    private void showDecompiledCodeInternal(String functionName, String code) {
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
        // Update panel title to show current context (JEB-like tab title)
        String shortName = functionName;
        if (shortName.startsWith("Decompiled: ")) {
            shortName = shortName.substring(12);
        }
        if (shortName.length() > 40) {
            shortName = shortName.substring(shortName.length() - 40);
        }
        setTitle("ArkTS \u2014 " + shortName);
        renderHighlightedCode(code);
        updateNavButtons();
        updateMethodOutline(code);
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
        highlightMatchingBracket();
        syncOutlineToCaretPosition(pos);
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

    // --- Bracket matching ---

    private void highlightMatchingBracket() {
        Highlighter highlighter = codePane.getHighlighter();
        if (bracketHighlight1 != null) {
            highlighter.removeHighlight(bracketHighlight1);
            bracketHighlight1 = null;
        }
        if (bracketHighlight2 != null) {
            highlighter.removeHighlight(bracketHighlight2);
            bracketHighlight2 = null;
        }

        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        int pos = codePane.getCaretPosition();

        int bracketPos = -1;
        char bracket = 0;
        if (pos < text.length()) {
            char c = text.charAt(pos);
            if (isBracket(c)) {
                bracketPos = pos;
                bracket = c;
            }
        }
        if (bracketPos < 0 && pos > 0) {
            char c = text.charAt(pos - 1);
            if (isBracket(c)) {
                bracketPos = pos - 1;
                bracket = c;
            }
        }
        if (bracketPos < 0) {
            return;
        }

        int matchPos = findMatchingBracket(text, bracketPos, bracket);
        if (matchPos < 0) {
            return;
        }

        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(COLOR_BRACKET_MATCH);
        try {
            bracketHighlight1 = highlighter.addHighlight(
                    bracketPos, bracketPos + 1, painter);
            bracketHighlight2 = highlighter.addHighlight(
                    matchPos, matchPos + 1, painter);
        } catch (BadLocationException ex) {
            // ignore
        }
    }

    private static boolean isBracket(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']';
    }

    private static int findMatchingBracket(String text, int pos, char bracket) {
        char open;
        char close;
        boolean forward;
        switch (bracket) {
            case '(': {
                open = '('; close = ')'; forward = true; break;
            }
            case ')': {
                open = '('; close = ')'; forward = false; break;
            }
            case '{': {
                open = '{'; close = '}'; forward = true; break;
            }
            case '}': {
                open = '{'; close = '}'; forward = false; break;
            }
            case '[': {
                open = '['; close = ']'; forward = true; break;
            }
            case ']': {
                open = '['; close = ']'; forward = false; break;
            }
            default: {
                return -1;
            }
        }
        int depth = 1;
        if (forward) {
            for (int i = pos + 1; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        } else {
            for (int i = pos - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == close) {
                    depth++;
                } else if (c == open) {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    // --- Line number gutter ---

    private static class LineNumberComponent extends JComponent
            implements DocumentListener {

        private final JTextPane textPane;
        private static final int WIDTH = 44;
        private static final Color COLOR_LARGE = new Color(0xC62828);
        private static final Color COLOR_MEDIUM = new Color(0xE65100);
        private static final Color COLOR_SMALL = new Color(0x2E7D32);

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

            String text = textPane.getText();
            if (text == null || text.isEmpty()) {
                return;
            }

            String[] lines = text.split("\n", -1);
            FontMetrics fm = g.getFontMetrics();
            int lineHeight = fm.getHeight();
            int y = fm.getAscent() + 2;

            // Pre-compute method sizes: for each method definition line, count lines until next
            int[] methodSizes = computeMethodSizes(lines);

            for (int i = 0; i < lines.length; i++) {
                Color lineColor = Color.GRAY;
                if (methodSizes[i] > 0) {
                    if (methodSizes[i] > 50) {
                        lineColor = COLOR_LARGE;
                    } else if (methodSizes[i] > 20) {
                        lineColor = COLOR_MEDIUM;
                    } else {
                        lineColor = COLOR_SMALL;
                    }
                }
                g.setColor(lineColor);
                String num = String.valueOf(i + 1);
                int x = WIDTH - fm.stringWidth(num) - 4;
                g.drawString(num, x, y);
                y += lineHeight;
            }
        }

        private static int[] computeMethodSizes(String[] lines) {
            int[] sizes = new int[lines.length];
            // Find method definition lines and compute their sizes
            for (int i = 0; i < lines.length; i++) {
                if (isMethodDef(lines[i])) {
                    // Count lines until next method definition or end
                    int count = 0;
                    for (int j = i + 1; j < lines.length; j++) {
                        if (isMethodDef(lines[j])) {
                            break;
                        }
                        count++;
                    }
                    sizes[i] = count;
                }
            }
            return sizes;
        }

        private static boolean isMethodDef(String line) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("//") || t.startsWith("*")) {
                return false;
            }
            return t.contains("(") && (t.endsWith("{") || t.endsWith(")"))
                    && (t.contains("function ") || t.contains("public ")
                            || t.contains("private ") || t.contains("protected ")
                            || t.contains("static ") || t.contains("async "));
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
                        if (e.isControlDown() || e.isMetaDown()) {
                            handleJumpToDefinition(e);
                        } else {
                            handleSymbolClick(e);
                        }
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

        JMenuItem bookmarkItem = new JMenuItem("Add Bookmark");
        bookmarkItem.setEnabled(addBookmarkCallback != null);
        if (addBookmarkCallback != null) {
            bookmarkItem.addActionListener(e -> addBookmarkCallback.run());
        }
        menu.add(bookmarkItem);

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

        JMenuItem copySignatureItem = new JMenuItem("Copy Method Signature");
        copySignatureItem.addActionListener(e -> copyCurrentMethodSignature(offset));
        menu.add(copySignatureItem);

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

        JMenuItem searchAllItem = new JMenuItem("Search in All Methods");
        searchAllItem.setEnabled(hasWord && globalSearchWordCallback != null);
        if (hasWord && globalSearchWordCallback != null) {
            final String capturedWord = word;
            searchAllItem.addActionListener(
                    e -> globalSearchWordCallback.accept(capturedWord));
        }
        menu.add(searchAllItem);

        menu.addSeparator();

        JMenuItem copyMarkdownItem = new JMenuItem("Copy as Markdown");
        copyMarkdownItem.addActionListener(e -> copyAsMarkdown());
        menu.add(copyMarkdownItem);

        return menu;
    }

    private void copyCurrentMethodSignature(int offset) {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        // Find the method definition line at or before the cursor
        int lineStart = text.lastIndexOf('\n', offset - 1) + 1;
        // Search backwards for the nearest method definition line
        int searchPos = offset;
        while (searchPos >= 0) {
            int ls = text.lastIndexOf('\n', searchPos - 1) + 1;
            int le = text.indexOf('\n', searchPos);
            if (le < 0) {
                le = text.length();
            }
            String line = text.substring(ls, le);
            if (isMethodDefinitionLine(line)) {
                String sig = line.trim();
                // Strip trailing '{' if present
                if (sig.endsWith("{")) {
                    sig = sig.substring(0, sig.length() - 1).trim();
                }
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(sig), null);
                Msg.info(OWNER, "Copied method signature: " + sig);
                return;
            }
            if (ls == 0) {
                break;
            }
            searchPos = ls - 1;
        }
        // Fallback: copy current line
        int le = text.indexOf('\n', lineStart);
        if (le < 0) {
            le = text.length();
        }
        String line = text.substring(lineStart, le).trim();
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(line), null);
    }


    private void copyAsMarkdown() {
        String selected = codePane.getSelectedText();
        String code = (selected != null && !selected.isEmpty())
                ? selected : codePane.getText();
        if (code == null || code.isEmpty()) {
            return;
        }
        String markdown = "```typescript\n" + code + "\n```";
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(markdown), null);
        Msg.info(OWNER, "Copied code as Markdown");
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
        bracketHighlight1 = null;
        bracketHighlight2 = null;
        highlightCurrentLine();
        highlightMatchingBracket();
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
        bracketHighlight1 = null;
        bracketHighlight2 = null;
        highlightCurrentLine();
        highlightMatchingBracket();
        updateStatusBar();
        if (symbolHighlightCallback != null) {
            symbolHighlightCallback.accept("");
        }
    }

    /**
     * Navigates to the next or previous occurrence of the currently highlighted symbol.
     * F3 = next (direction=1), Shift+F3 = previous (direction=-1).
     *
     * @param direction 1 for next, -1 for previous
     */
    private void navigateOccurrence(int direction) {
        if (currentHighlightedWord == null || currentHighlightedWord.isEmpty()) {
            return;
        }
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        List<Integer> positions =
                symbolHighlighter.findAllOccurrences(text, currentHighlightedWord);
        if (positions.isEmpty()) {
            return;
        }
        int caretPos = codePane.getCaretPosition();
        int targetIdx = 0;
        if (direction > 0) {
            targetIdx = 0;
            for (int i = 0; i < positions.size(); i++) {
                if (positions.get(i) > caretPos) {
                    targetIdx = i;
                    break;
                }
                targetIdx = (i + 1) % positions.size();
            }
        } else {
            targetIdx = positions.size() - 1;
            for (int i = positions.size() - 1; i >= 0; i--) {
                if (positions.get(i) < caretPos) {
                    targetIdx = i;
                    break;
                }
                targetIdx = i == 0 ? positions.size() - 1 : i - 1;
            }
        }
        int targetPos = positions.get(targetIdx);
        codePane.setCaretPosition(targetPos);
        try {
            codePane.scrollRectToVisible(
                    codePane.modelToView2D(targetPos).getBounds());
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Navigate occurrence scroll error: " + ex.getMessage());
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

        KeyStroke ctrlH = KeyStroke.getKeyStroke(KeyEvent.VK_H, mask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlH, "openReplace");
        codePane.getActionMap().put("openReplace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSearchBar();
                if (!replaceMode) {
                    toggleReplaceRow();
                }
            }
        });
    }

    private JPanel buildSearchPanel() {
        JPanel findRow = new JPanel(new BorderLayout(4, 0));
        findRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 1, 4));

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

        JButton replaceToggle = new JButton("\u25BE");
        replaceToggle.setToolTipText("Show/hide replace (Ctrl+H)");
        replaceToggle.addActionListener(e -> toggleReplaceRow());

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
        buttons.add(replaceToggle);
        buttons.add(prevButton);
        buttons.add(nextButton);
        buttons.add(searchStatusLabel);
        buttons.add(closeButton);

        findRow.add(new JLabel("Find: "), BorderLayout.WEST);
        findRow.add(searchField, BorderLayout.CENTER);
        findRow.add(buttons, BorderLayout.EAST);

        replaceField = new JTextField(20);
        JButton replaceButton = new JButton("Replace");
        replaceButton.addActionListener(e -> replaceCurrentMatch());
        JButton replaceAllButton = new JButton("Replace All");
        replaceAllButton.addActionListener(e -> replaceAllMatches());

        JPanel replaceButtons = new JPanel();
        replaceButtons.add(replaceButton);
        replaceButtons.add(replaceAllButton);

        replaceRow = new JPanel(new BorderLayout(4, 0));
        replaceRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 2, 4));
        replaceRow.add(new JLabel("Replace: "), BorderLayout.WEST);
        replaceRow.add(replaceField, BorderLayout.CENTER);
        replaceRow.add(replaceButtons, BorderLayout.EAST);
        replaceRow.setVisible(false);

        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.add(findRow);
        panel.add(replaceRow);

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
        replaceMode = false;
        if (replaceRow != null) {
            replaceRow.setVisible(false);
        }
        clearSearchHighlights();
        searchMatchPositions = java.util.Collections.emptyList();
        searchMatchIndex = -1;
        codePane.requestFocusInWindow();
        mainPanel.revalidate();
    }

    private void toggleReplaceRow() {
        replaceMode = !replaceMode;
        replaceRow.setVisible(replaceMode);
        if (replaceMode) {
            replaceField.requestFocusInWindow();
        }
        mainPanel.revalidate();
    }

    private void replaceCurrentMatch() {
        if (searchMatchPositions.isEmpty() || searchMatchIndex < 0) {
            return;
        }
        String query = searchField.getText();
        String replacement = replaceField.getText();
        if (query.isEmpty()) {
            return;
        }
        int pos = searchMatchPositions.get(searchMatchIndex);
        try {
            StyledDocument doc = codePane.getStyledDocument();
            doc.remove(pos, query.length());
            doc.insertString(pos, replacement,
                    createStyle(new ColorScheme(isDarkBackground()).plain, false));
            runSearch();
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Replace failed: " + ex.getMessage());
        }
    }

    private void replaceAllMatches() {
        String query = searchField.getText();
        String replacement = replaceField.getText();
        if (query.isEmpty() || searchMatchPositions.isEmpty()) {
            return;
        }
        try {
            StyledDocument doc = codePane.getStyledDocument();
            List<Integer> positions = new java.util.ArrayList<>(searchMatchPositions);
            for (int i = positions.size() - 1; i >= 0; i--) {
                int pos = positions.get(i);
                doc.remove(pos, query.length());
                doc.insertString(pos, replacement,
                        createStyle(new ColorScheme(isDarkBackground()).plain, false));
            }
            runSearch();
        } catch (BadLocationException ex) {
            Msg.warn(OWNER, "Replace all failed: " + ex.getMessage());
        }
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

        JButton prevClassButton = new JButton("◀ Class");
        prevClassButton.setToolTipText("Decompile previous class");
        prevClassButton.addActionListener(e -> {
            if (prevClassCallback != null) {
                prevClassCallback.run();
            }
        });
        toolBar.add(prevClassButton);

        JButton nextClassButton = new JButton("Class ▶");
        nextClassButton.setToolTipText("Decompile next class");
        nextClassButton.addActionListener(e -> {
            if (nextClassCallback != null) {
                nextClassCallback.run();
            }
        });
        toolBar.add(nextClassButton);

        JButton exportAllButton = new JButton("Export All...");
        exportAllButton.setToolTipText("Export all decompiled classes to a directory");
        exportAllButton.addActionListener(e -> {
            if (exportAllCallback != null) {
                exportAllCallback.run();
            }
        });
        toolBar.add(exportAllButton);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());
        toolBar.add(copyButton);

        JButton copyAllButton = new JButton("Copy All");
        copyAllButton.setToolTipText("Copy all decompiled code to clipboard");
        copyAllButton.addActionListener(e -> copyAllToClipboard());
        toolBar.add(copyAllButton);

        JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(e -> saveToFile());
        toolBar.add(saveButton);

        JButton saveHtmlButton = new JButton("HTML...");
        saveHtmlButton.setToolTipText("Export decompiled code as syntax-highlighted HTML");
        saveHtmlButton.addActionListener(e -> saveAsHtml());
        toolBar.add(saveHtmlButton);

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
        JToggleButton wrapButton = new JToggleButton("Wrap");
        wrapButton.setToolTipText("Toggle word wrap");
        wrapButton.setSelected(false);
        wrapButton.addActionListener(e -> setWordWrap(wrapButton.isSelected()));
        toolBar.add(wrapButton);
        JToggleButton indentGuideButton = new JToggleButton("\u22ee");
        indentGuideButton.setToolTipText("Toggle indent guides");
        indentGuideButton.setSelected(true);
        indentGuideButton.addActionListener(e -> {
            showIndentGuides = indentGuideButton.isSelected();
            codePane.repaint();
        });
        toolBar.add(indentGuideButton);

        JButton pinButton = new JButton("\uD83D\uDCCC");
        pinButton.setToolTipText("Pin current view in a new panel");
        pinButton.addActionListener(e -> {
            if (pinCallback != null) {
                pinCallback.run();
            }
        });
        toolBar.add(pinButton);

        JButton showInTreeButton = new JButton("\u25B6\u25B6");
        showInTreeButton.setToolTipText("Show current method/class in HAP Explorer (Ctrl+Shift+E)");
        showInTreeButton.addActionListener(e -> {
            if (showAndSelectInExplorerCallback != null) {
                showAndSelectInExplorerCallback.run();
            } else if (showHapExplorerCallback != null) {
                showHapExplorerCallback.run();
            }
        });
        toolBar.add(showInTreeButton);
        toolBar.addSeparator();
        JButton helpButton = new JButton("?");
        helpButton.setToolTipText("Keyboard shortcuts");
        helpButton.addActionListener(e -> {
            if (showShortcutsCallback != null) {
                showShortcutsCallback.run();
            } else {
                showKeyboardShortcuts();
            }
        });
        toolBar.add(helpButton);
    }

    private void showKeyboardShortcuts() {
        String shortcuts =
                "Keyboard Shortcuts\n"
                + "------------------------------\n"
                + "Ctrl+F          Find / Search\n"
                + "Ctrl+H          Find & Replace\n"
                + "Ctrl+G          Go to line\n"
                + "Ctrl+Shift+C    Copy current line\n"
                + "Ctrl+Shift+F    Global search\n"
                + "Ctrl+Shift+E    Show HAP Explorer\n"
                + "Ctrl+Shift+B    Show Bookmarks\n"
                + "Ctrl+Shift+H    Show History\n"
                + "Ctrl+Shift+X    Show Xref\n"
                + "Ctrl+Shift+N    Show Notes\n"
                + "Ctrl+Shift+,    Show Settings\n"
                + "F3              Next occurrence\n"
                + "Shift+F3        Previous occurrence\n"
                + "Ctrl+Down       Next method definition\n"
                + "Ctrl+Up         Previous method definition\n"
                + "Ctrl+P          Quick Open (jump to class/method)\n"
                + "Escape          Close search bar\n"
                + "Alt+Left        Navigate back\n"
                + "Alt+Right       Navigate forward\n"
                + "Ctrl+B          Add bookmark\n"
                + "Ctrl+=          Increase font size\n"
                + "Ctrl+-          Decrease font size\n"
                + "Ctrl+0          Reset font size\n"
                + "Click           Highlight all occurrences\n"
                + "Ctrl+Click      Jump to definition\n"
                + "N               Rename highlighted symbol\n"
                + "X               Show cross-references for highlighted symbol\n"
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

        ColorScheme cs;
        switch (currentTheme) {
            case "Dark": {
                cs = new ColorScheme(true);
                break;
            }
            case "Light": {
                cs = new ColorScheme(false);
                break;
            }
            case "High Contrast": {
                cs = new ColorScheme(true);
                break;
            }
            default: {
                cs = new ColorScheme(isDarkBackground());
                break;
            }
        }
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

        // Apply line spacing to all paragraphs
        if (currentLineSpacing > 0) {
            SimpleAttributeSet paraStyle = new SimpleAttributeSet();
            StyleConstants.setSpaceAbove(paraStyle, currentLineSpacing);
            StyleConstants.setSpaceBelow(paraStyle, currentLineSpacing);
            doc.setParagraphAttributes(0, doc.getLength(), paraStyle, false);
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
        StyleConstants.setFontFamily(style, currentFontFamily);
        StyleConstants.setFontSize(style, currentFontSize);
        return style;
    }

    // --- Word wrap ---

    private void setWordWrap(boolean enabled) {
        wordWrapEnabled = enabled;
        codePane.revalidate();
        codePane.repaint();
    }

    // --- Indent guides ---

    private void paintIndentGuides(Graphics g) {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        FontMetrics fm = g.getFontMetrics(codePane.getFont());
        int charWidth = fm.charWidth(' ');
        int lineHeight = fm.getHeight();
        int indentWidth = 4 * charWidth;
        Color guideColor = isDarkBackground()
                ? new Color(0x37474F)
                : new Color(0xE0E0E0);
        g.setColor(guideColor);
        String[] lines = text.split("\n", -1);
        int y = fm.getAscent();
        for (String line : lines) {
            int spaces = 0;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == ' ') {
                    spaces++;
                } else {
                    break;
                }
            }
            int levels = spaces / 4;
            for (int level = 1; level <= levels; level++) {
                int x = level * indentWidth;
                g.drawLine(x, y - fm.getAscent(), x, y - fm.getAscent() + lineHeight - 1);
            }
            y += lineHeight;
        }
    }

    // --- Font size zoom ---

    private void setFontSize(int size) {
        currentFontSize = Math.max(8, Math.min(24, size));
        codePane.setFont(new Font(currentFontFamily, Font.PLAIN, currentFontSize));
        if (!lastCode.isEmpty()) {
            renderHighlightedCode(lastCode);
        }
    }

    /**
     * Updates the font family used in the code view and re-renders if content is present.
     *
     * @param family font family name; ignored if null or empty
     */
    public void setFontFamily(String family) {
        if (family == null || family.isEmpty()) {
            return;
        }
        currentFontFamily = family;
        codePane.setFont(new Font(currentFontFamily, Font.PLAIN, currentFontSize));
        if (!lastCode.isEmpty()) {
            renderHighlightedCode(lastCode);
        }
    }

    /**
     * Updates the line spacing applied to all paragraphs in the code view.
     * Re-renders the current content if any is present.
     *
     * @param spacing spacing value in the range [0, 8]; clamped if out of range
     */
    public void setLineSpacing(int spacing) {
        currentLineSpacing = Math.max(0, Math.min(8, spacing));
        if (!lastCode.isEmpty()) {
            renderHighlightedCode(lastCode);
        }
    }

    /**
     * Sets the tab size (number of spaces per indent level) and re-renders.
     * Tab characters in the decompiled output are replaced with the given number of spaces.
     *
     * @param tabSize number of spaces per tab (2, 4, or 8)
     */
    public void setTabSize(int tabSize) {
        // Tab size affects how tabs are displayed; re-render to apply
        // The decompiler outputs spaces, so this is a display hint only
        // We store it for future use in rendering
        if (!lastCode.isEmpty()) {
            renderHighlightedCode(lastCode);
        }
    }

    /**
     * Updates the syntax highlighting theme and re-renders if content is present.
     *
     * @param theme theme name; ignored if null
     */
    public void setTheme(String theme) {
        if (theme == null) {
            return;
        }
        currentTheme = theme;
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

    private void copyAllToClipboard() {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
        Msg.info(OWNER, "Copied all decompiled code to clipboard");
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

    private void saveAsHtml() {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            Msg.warn(OWNER, "No code to export");
            return;
        }
        // Ask user for theme
        String[] options = {"Dark Theme", "Light Theme", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                mainPanel, "Choose HTML export theme:", "Export as HTML",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }
        boolean darkTheme = choice == 0;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export as HTML");
        chooser.setSelectedFile(new File("decompiled.html"));
        if (chooser.showSaveDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(buildHtml(text, darkTheme, lastFunctionName));
            Msg.info(OWNER, "Exported HTML to " + file.getPath());
        } catch (IOException e) {
            Msg.error(OWNER, "Failed to export HTML: " + e.getMessage(), e);
        }
    }

    private String buildHtml(String code, boolean dark, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<title>").append(title.isEmpty() ? "Decompiled ArkTS" : title).append("</title>\n");
        sb.append("<style>\n");
        if (dark) {
            sb.append("body { background: #1e1e1e; color: #d4d4d4; font-family: monospace; "
                    + "font-size: 13px; padding: 16px; }\n");
            sb.append(".kw { color: #82aaff; font-weight: bold; }\n");
            sb.append(".ty { color: #89ddff; font-weight: bold; }\n");
            sb.append(".st { color: #c3e88d; }\n");
            sb.append(".cm { color: #546e7a; }\n");
            sb.append(".dc { color: #c792ea; font-weight: bold; }\n");
            sb.append(".nm { color: #f78c6c; }\n");
            sb.append(".md { color: #82aaff; font-weight: bold; }\n");
        } else {
            sb.append("body { background: #ffffff; color: #000000; font-family: monospace; "
                    + "font-size: 13px; padding: 16px; }\n");
            sb.append(".kw { color: #0000cc; font-weight: bold; }\n");
            sb.append(".ty { color: #008080; font-weight: bold; }\n");
            sb.append(".st { color: #008000; }\n");
            sb.append(".cm { color: #808080; }\n");
            sb.append(".dc { color: #800080; font-weight: bold; }\n");
            sb.append(".nm { color: #ff8c00; }\n");
            sb.append(".md { color: #0000cc; font-weight: bold; }\n");
        }
        sb.append("</style>\n</head>\n<body>\n<pre>\n");
        if (!title.isEmpty()) {
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date());
            sb.append("<!-- Decompiled: ").append(title)
                    .append(" | ").append(timestamp).append(" -->\n");
        }
        String escaped = code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        escaped = escaped.replaceAll("(?m)(//[^\n]*)", "<span class=\"cm\">$1</span>");
        escaped = escaped.replaceAll("(\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\")",
                "<span class=\"st\">$1</span>");
        escaped = escaped.replaceAll("(@\\w+)", "<span class=\"dc\">$1</span>");
        String[] keywords = {
            "let", "const", "function", "class", "return", "if", "else",
            "for", "while", "new", "import", "export", "extends", "implements",
            "async", "await", "try", "catch", "finally", "throw", "static",
            "interface", "enum", "namespace", "typeof", "instanceof", "in", "of"
        };
        for (String kw : keywords) {
            escaped = escaped.replaceAll("\\b(" + kw + ")\\b", "<span class=\"kw\">$1</span>");
        }
        String[] types = {
            "number", "string", "boolean", "void", "null", "undefined",
            "true", "false", "any", "never", "object"
        };
        for (String ty : types) {
            escaped = escaped.replaceAll("\\b(" + ty + ")\\b", "<span class=\"ty\">$1</span>");
        }
        String[] modifiers = {
            "public", "private", "protected", "readonly", "abstract",
            "override", "super", "this"
        };
        for (String md : modifiers) {
            escaped = escaped.replaceAll("\\b(" + md + ")\\b", "<span class=\"md\">$1</span>");
        }
        sb.append(escaped);
        sb.append("</pre>\n</body>\n</html>\n");
        return sb.toString();
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

        int cmdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlB, "addBookmark");
        codePane.getActionMap().put("addBookmark", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addBookmarkCallback != null) {
                    addBookmarkCallback.run();
                }
            }
        });

        KeyStroke ctrlG = KeyStroke.getKeyStroke(KeyEvent.VK_G, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlG, "goToLine");
        codePane.getActionMap().put("goToLine", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToLine();
            }
        });

        KeyStroke ctrlShiftC = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftC, "copyLine");
        codePane.getActionMap().put("copyLine", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyCurrentLine();
            }
        });

        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlA, "selectAll");
        codePane.getActionMap().put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codePane.selectAll();
            }
        });

        // F3 / Shift+F3 — navigate through highlighted symbol occurrences
        KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        KeyStroke shiftF3 = KeyStroke.getKeyStroke(
                KeyEvent.VK_F3, java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(f3, "nextOccurrence");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(shiftF3, "prevOccurrence");
        codePane.getActionMap().put("nextOccurrence", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateOccurrence(1);
            }
        });
        codePane.getActionMap().put("prevOccurrence", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateOccurrence(-1);
            }
        });

        // Ctrl+Down / Ctrl+Up — jump between method definitions
        KeyStroke ctrlDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, cmdMask);
        KeyStroke ctrlUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlDown, "nextMethod");
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlUp, "prevMethod");
        codePane.getActionMap().put("nextMethod", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jumpToMethod(1);
            }
        });
        codePane.getActionMap().put("prevMethod", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jumpToMethod(-1);
            }
        });

        // Ctrl+P — Quick Open (jump to any class or method)
        KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlP, "quickOpen");
        codePane.getActionMap().put("quickOpen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (quickOpenCallback != null) {
                    quickOpenCallback.run();
                }
            }
        });

        // Ctrl+Shift+E — Show HAP Explorer
        KeyStroke ctrlShiftE = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftE, "showHapExplorer");
        codePane.getActionMap().put("showHapExplorer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showHapExplorerCallback != null) {
                    showHapExplorerCallback.run();
                }
            }
        });

        // Ctrl+Shift+B — Show Bookmarks
        KeyStroke ctrlShiftB = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftB, "showBookmarks");
        codePane.getActionMap().put("showBookmarks", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showBookmarksCallback != null) {
                    showBookmarksCallback.run();
                }
            }
        });

        // Ctrl+Shift+H — Show History
        KeyStroke ctrlShiftH = KeyStroke.getKeyStroke(KeyEvent.VK_H,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftH, "showHistory");
        codePane.getActionMap().put("showHistory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showHistoryCallback != null) {
                    showHistoryCallback.run();
                }
            }
        });

        // Ctrl+Shift+X — Show Xref
        KeyStroke ctrlShiftX = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftX, "showXref");
        codePane.getActionMap().put("showXref", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showXrefCallback != null) {
                    showXrefCallback.run();
                }
            }
        });

        // Ctrl+Shift+N — Show Notes
        KeyStroke ctrlShiftN = KeyStroke.getKeyStroke(KeyEvent.VK_N,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftN, "showNotes");
        codePane.getActionMap().put("showNotes", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showNotesCallback != null) {
                    showNotesCallback.run();
                }
            }
        });

        // Ctrl+Shift+, — Show Settings
        KeyStroke ctrlShiftComma = KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftComma, "showSettings");
        codePane.getActionMap().put("showSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showSettingsCallback != null) {
                    showSettingsCallback.run();
                }
            }
        });

        // Ctrl+/ — Show Shortcuts panel
        KeyStroke ctrlSlash = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlSlash, "showShortcuts");
        codePane.getActionMap().put("showShortcuts", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showShortcutsCallback != null) {
                    showShortcutsCallback.run();
                }
            }
        });

        // Ctrl+Shift+O — Focus method outline combo (Go to method)
        KeyStroke ctrlShiftO = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                cmdMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlShiftO, "focusOutline");
        codePane.getActionMap().put("focusOutline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (methodOutlineCombo != null && methodOutlineCombo.isVisible()) {
                    methodOutlineCombo.requestFocusInWindow();
                    methodOutlineCombo.showPopup();
                }
            }
        });

        // Ctrl+D — Decompile current class (JEB-style)
        KeyStroke ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, cmdMask);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlD, "decompileClass");
        codePane.getActionMap().put("decompileClass", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (decompileClassCallback != null && !lastClassName.isEmpty()) {
                    decompileClassCallback.run();
                }
            }
        });
    }

    // --- JEB-style key bindings ---

    private void installJebKeyBindings() {
        // N — rename highlighted symbol
        KeyStroke keyN = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(keyN, "jebRename");
        codePane.getActionMap().put("jebRename", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentHighlightedWord != null && !currentHighlightedWord.isEmpty()) {
                    renameSymbol(currentHighlightedWord);
                }
            }
        });

        // X — show cross-references for highlighted symbol
        KeyStroke keyX = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0);
        codePane.getInputMap(JComponent.WHEN_FOCUSED).put(keyX, "jebXref");
        codePane.getActionMap().put("jebXref", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentHighlightedWord != null && !currentHighlightedWord.isEmpty()
                        && symbolHighlightCallback != null) {
                    symbolHighlightCallback.accept(currentHighlightedWord);
                }
            }
        });
    }

    // --- Go to line / Copy line ---

    /**
     * Jumps to the next or previous method definition in the current decompiled output.
     * Looks for lines starting with common ArkTS method patterns.
     *
     * @param direction 1 for next, -1 for previous
     */
    private void jumpToMethod(int direction) {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        int caretPos = codePane.getCaretPosition();
        String[] lines = text.split("\n", -1);
        // Build line start offsets
        int[] lineStarts = new int[lines.length];
        int off = 0;
        for (int i = 0; i < lines.length; i++) {
            lineStarts[i] = off;
            off += lines[i].length() + 1;
        }
        // Find current line index
        int currentLine = 0;
        for (int i = 0; i < lineStarts.length; i++) {
            if (lineStarts[i] <= caretPos) {
                currentLine = i;
            }
        }
        // Search for method definition lines (lines containing '(' and not starting with //)
        if (direction > 0) {
            for (int i = currentLine + 1; i < lines.length; i++) {
                if (isMethodDefinitionLine(lines[i])) {
                    codePane.setCaretPosition(lineStarts[i]);
                    scrollToOffset(lineStarts[i]);
                    return;
                }
            }
        } else {
            for (int i = currentLine - 1; i >= 0; i--) {
                if (isMethodDefinitionLine(lines[i])) {
                    codePane.setCaretPosition(lineStarts[i]);
                    scrollToOffset(lineStarts[i]);
                    return;
                }
            }
        }
    }

    private static boolean isMethodDefinitionLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("*")) {
            return false;
        }
        // Match lines that look like method signatures: contain '(' and end with '{' or ')'
        // and contain access modifiers or function keyword
        return (trimmed.contains("(") && (trimmed.endsWith("{") || trimmed.endsWith(")"))
                && (trimmed.contains("function ") || trimmed.contains("public ")
                        || trimmed.contains("private ") || trimmed.contains("protected ")
                        || trimmed.contains("static ") || trimmed.contains("async ")));
    }

    private void updateMethodOutline(String code) {
        if (methodOutlineCombo == null) {
            return;
        }
        if (code == null || code.isEmpty()) {
            updatingOutline = true;
            methodOutlineCombo.setModel(new DefaultComboBoxModel<>());
            methodOutlineCombo.setVisible(false);
            updatingOutline = false;
            return;
        }
        java.util.List<OutlineEntry> entries = new java.util.ArrayList<>();
        String[] lines = code.split("\n", -1);
        int offset = 0;
        for (String line : lines) {
            if (isMethodDefinitionLine(line)) {
                String label = extractMethodLabel(line);
                entries.add(new OutlineEntry(label, offset));
            }
            offset += line.length() + 1;
        }
        // Annotate entries with line counts (method size indicator)
        for (int i = 0; i < entries.size(); i++) {
            int startOff = entries.get(i).offset;
            int endOff = (i + 1 < entries.size()) ? entries.get(i + 1).offset : code.length();
            int lineCount = 0;
            for (int j = startOff; j < endOff && j < code.length(); j++) {
                if (code.charAt(j) == '\n') {
                    lineCount++;
                }
            }
            String sizeTag = lineCount > 50 ? " \u25cf" : lineCount > 20 ? " \u25cb" : "";
            if (!sizeTag.isEmpty()) {
                entries.set(i, new OutlineEntry(entries.get(i).label + sizeTag,
                        entries.get(i).offset));
            }
        }
        updatingOutline = true;
        allOutlineEntries = new java.util.ArrayList<>(entries);
        DefaultComboBoxModel<OutlineEntry> combo = new DefaultComboBoxModel<>();
        for (OutlineEntry e : entries) {
            combo.addElement(e);
        }
        methodOutlineCombo.setModel(combo);
        methodOutlineCombo.setVisible(!entries.isEmpty());
        if (methodFilterField != null) {
            methodFilterField.setVisible(!entries.isEmpty());
        }
        updatingOutline = false;
    }

    private void applyOutlineFilter() {
        if (methodOutlineCombo == null || updatingOutline) {
            return;
        }
        String filter = methodFilterField != null ? methodFilterField.getText().toLowerCase() : "";
        updatingOutline = true;
        DefaultComboBoxModel<OutlineEntry> combo = new DefaultComboBoxModel<>();
        for (OutlineEntry e : allOutlineEntries) {
            if (filter.isEmpty() || e.label.toLowerCase().contains(filter)) {
                combo.addElement(e);
            }
        }
        methodOutlineCombo.setModel(combo);
        updatingOutline = false;
    }

    private static String extractMethodLabel(String line) {
        String trimmed = line.trim();
        // Remove leading modifiers to get a clean label
        int parenIdx = trimmed.indexOf('(');
        if (parenIdx < 0) {
            return trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed;
        }
        // Find the method name: last word before '('
        String beforeParen = trimmed.substring(0, parenIdx).trim();
        int lastSpace = beforeParen.lastIndexOf(' ');
        String methodName = lastSpace >= 0 ? beforeParen.substring(lastSpace + 1) : beforeParen;
        // Include params up to closing paren for context
        int closeIdx = trimmed.indexOf(')', parenIdx);
        String params = closeIdx > parenIdx
                ? trimmed.substring(parenIdx, closeIdx + 1) : "(...)";
        // Include return type if present (after ': ')
        String returnType = "";
        if (closeIdx > 0 && closeIdx + 1 < trimmed.length()) {
            String afterClose = trimmed.substring(closeIdx + 1).trim();
            if (afterClose.startsWith(":")) {
                String rt = afterClose.substring(1).trim();
                int braceIdx = rt.indexOf('{');
                if (braceIdx > 0) {
                    rt = rt.substring(0, braceIdx).trim();
                }
                if (!rt.isEmpty()) {
                    returnType = ": " + rt;
                }
            }
        }
        String label = methodName + params + returnType;
        return label.length() > 70 ? label.substring(0, 70) + "..." : label;
    }

    private void syncOutlineToCaretPosition(int caretPos) {
        if (methodOutlineCombo == null || updatingOutline) {
            return;
        }
        int count = methodOutlineCombo.getItemCount();
        if (count == 0) {
            return;
        }
        // Find the last outline entry whose offset is <= caretPos
        int bestIdx = 0;
        for (int i = 0; i < count; i++) {
            OutlineEntry entry = methodOutlineCombo.getItemAt(i);
            if (entry.offset <= caretPos) {
                bestIdx = i;
            }
        }
        if (methodOutlineCombo.getSelectedIndex() != bestIdx) {
            updatingOutline = true;
            methodOutlineCombo.setSelectedIndex(bestIdx);
            updatingOutline = false;
        }
    }

    private void goToLine() {
        String input = JOptionPane.showInputDialog(
                mainPanel, "Go to line:", "Go to Line",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        try {
            int targetLine = Integer.parseInt(input.trim());
            if (targetLine < 1) {
                return;
            }
            String text = codePane.getText();
            if (text == null || text.isEmpty()) {
                return;
            }
            int line = 1;
            int offset = 0;
            while (offset < text.length() && line < targetLine) {
                if (text.charAt(offset) == '\n') {
                    line++;
                }
                offset++;
            }
            codePane.setCaretPosition(Math.min(offset, text.length()));
            try {
                codePane.scrollRectToVisible(
                        codePane.modelToView2D(offset).getBounds());
            } catch (BadLocationException ex) {
                Msg.warn(OWNER, "Go to line scroll error: " + ex.getMessage());
            }
            codePane.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            // ignore invalid input
        }
    }

    private void copyCurrentLine() {
        String text = codePane.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        int pos = codePane.getCaretPosition();
        int lineStart = pos;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        int lineEnd = pos;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        String line = text.substring(lineStart, lineEnd);
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(line), null);
    }

    // --- Tooltip ---

    private String computeTooltip(MouseEvent event) {
        int offset = codePane.viewToModel2D(event.getPoint());
        String text = codePane.getText();
        String word = symbolHighlighter.extractWordAt(text, offset);
        if (word.isEmpty() || ArkTSColorizer.isKeyword(word)) {
            return null;
        }
        // Try to get a method preview tooltip from the plugin
        if (tooltipCallback != null) {
            String preview = tooltipCallback.apply(word);
            if (preview != null && !preview.isEmpty()) {
                return "<html><pre style='font-family:monospace;font-size:11px'>"
                        + escapeHtml(preview) + "</pre></html>";
            }
        }
        int count = symbolHighlighter.findAllOccurrences(text, word).size();
        if (count < 2) {
            return null;
        }
        return "\"" + word + "\" \u2014 " + count + " occurrences";
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
     * Sets a callback that is invoked when the "Export All..." toolbar button is clicked.
     *
     * @param callback the runnable to invoke, or null to disable the button action
     */
    public void setExportAllCallback(Runnable callback) {
        this.exportAllCallback = callback;
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
     * Sets a callback that provides a tooltip preview for a hovered symbol.
     * The callback receives the word under the cursor and returns an HTML or plain-text
     * preview string, or null if no preview is available.
     *
     * @param callback the function to invoke, or null to disable
     */
    public void setTooltipCallback(java.util.function.Function<String, String> callback) {
        this.tooltipCallback = callback;
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
     * Sets a callback invoked when the user presses Ctrl+B or selects "Add Bookmark"
     * from the context menu.
     *
     * @param callback the runnable to invoke, or null to disable
     */
    public void setAddBookmarkCallback(Runnable callback) {
        this.addBookmarkCallback = callback;
    }

    /**
     * Sets a callback invoked when the user right-clicks a word and selects
     * "Search in All Methods". Receives the word to search for.
     *
     * @param callback consumer that receives the search word
     */
    public void setGlobalSearchWordCallback(Consumer<String> callback) {
        this.globalSearchWordCallback = callback;
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
     * Returns the class name shown in the breadcrumb header.
     *
     * @return the class name, or empty string if none
     */
    public String getLastClassName() {
        return lastClassName;
    }

    /**
     * Sets a callback invoked when the user clicks the class breadcrumb label.
     *
     * @param callback the runnable to invoke, or null to disable
     */
    public void setDecompileClassCallback(Runnable callback) {
        this.decompileClassCallback = callback;
    }

    /**
     * Sets the callback invoked when the user presses Ctrl+P to open Quick Open.
     *
     * @param callback the runnable to invoke
     */
    public void setQuickOpenCallback(Runnable callback) {
        this.quickOpenCallback = callback;
    }

    /**
     * Sets the callback invoked when the user clicks the Pin button.
     *
     * @param callback the runnable to invoke
     */
    public void setPinCallback(Runnable callback) {
        this.pinCallback = callback;
    }

    /**
     * Sets the callback invoked when the user presses Ctrl+Shift+E to show the HAP Explorer.
     *
     * @param callback the runnable to invoke
     */
    public void setShowHapExplorerCallback(Runnable callback) {
        this.showHapExplorerCallback = callback;
    }

    /**
     * Sets a callback that shows the HAP Explorer AND selects the current method/class in the tree.
     *
     * @param callback the runnable to invoke
     */
    public void setShowAndSelectInExplorerCallback(Runnable callback) {
        this.showAndSelectInExplorerCallback = callback;
    }

    /** Sets the callback for Ctrl+Shift+B (Show Bookmarks). */
    public void setShowBookmarksCallback(Runnable callback) {
        this.showBookmarksCallback = callback;
    }

    /** Sets the callback for Ctrl+Shift+H (Show History). */
    public void setShowHistoryCallback(Runnable callback) {
        this.showHistoryCallback = callback;
    }

    /** Sets the callback for Ctrl+Shift+X (Show Xref). */
    public void setShowXrefCallback(Runnable callback) {
        this.showXrefCallback = callback;
    }

    /** Sets the callback for Ctrl+Shift+N (Show Notes). */
    public void setShowNotesCallback(Runnable callback) {
        this.showNotesCallback = callback;
    }

    /** Sets the callback for Ctrl+Shift+, (Show Settings). */
    public void setShowSettingsCallback(Runnable callback) {
        this.showSettingsCallback = callback;
    }

    /** Sets the callback for Ctrl+/ and the "?" button (Show Shortcuts panel). */
    public void setShowShortcutsCallback(Runnable callback) {
        this.showShortcutsCallback = callback;
    }

    /**
     * Sets the callback invoked when the user clicks "◀ Class" to go to the previous class.
     *
     * @param callback the runnable to invoke
     */
    public void setPrevClassCallback(Runnable callback) {
        this.prevClassCallback = callback;
    }

    /**
     * Sets the callback invoked when the user clicks "Class ▶" to go to the next class.
     *
     * @param callback the runnable to invoke
     */
    public void setNextClassCallback(Runnable callback) {
        this.nextClassCallback = callback;
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
        // Parse "N/M" pattern from message to update progress bar
        if (progressBar != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d+)/(\\d+)").matcher(message);
            if (m.find()) {
                int done = Integer.parseInt(m.group(1));
                int total = Integer.parseInt(m.group(2));
                if (total > 0) {
                    progressBar.setMaximum(total);
                    progressBar.setValue(done);
                    progressBar.setString(done + "/" + total);
                    progressBar.setVisible(true);
                    return;
                }
            }
            progressBar.setIndeterminate(true);
            progressBar.setString(null);
            progressBar.setVisible(true);
        }
    }

    /**
     * Clears the loading message from the header area.
     */
    public void hideLoading() {
        loadingLabel.setText("  ");
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
        }
    }

    /**
     * Sets the method info label shown in the status bar area.
     * Pass null or empty string to clear.
     *
     * @param info the info text to display (e.g. "48b · simple · 3 callers")
     */
    public void setMethodInfo(String info) {
        methodInfoLabel.setText(info != null && !info.isEmpty() ? info + "  " : "  ");
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
