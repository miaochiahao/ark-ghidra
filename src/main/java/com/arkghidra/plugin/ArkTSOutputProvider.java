package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
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
 */
public class ArkTSOutputProvider extends ComponentProvider {

    private static final String OWNER =
            ArkTSOutputProvider.class.getSimpleName();

    // Syntax highlighting colors
    private static final Color COLOR_KEYWORD = new Color(0x0000CC);
    private static final Color COLOR_TYPE = new Color(0x008080);
    private static final Color COLOR_STRING = new Color(0x008000);
    private static final Color COLOR_COMMENT = new Color(0x808080);
    private static final Color COLOR_DECORATOR = new Color(0x800080);
    private static final Color COLOR_NUMBER = new Color(0xFF8C00);
    private static final Color COLOR_MODIFIER = new Color(0x0000CC);
    private static final Color COLOR_PLAIN = Color.BLACK;

    private final JPanel mainPanel;
    private final JTextPane codePane;
    private final JLabel headerLabel;
    private final ArkTSColorizer colorizer;

    public ArkTSOutputProvider(Tool tool, String owner) {
        super(tool, "ArkTS Output", owner);

        colorizer = new ArkTSColorizer();

        codePane = new JTextPane();
        codePane.setEditable(false);
        codePane.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(codePane);

        headerLabel = new JLabel("No decompiled code");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        addToolbarButtons(toolBar);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(toolBar, BorderLayout.SOUTH);

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
        headerLabel.setText("Decompiled: " + functionName);
        renderHighlightedCode(code);
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

    private void addToolbarButtons(JToolBar toolBar) {
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());
        toolBar.add(copyButton);

        JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(e -> saveToFile());
        toolBar.add(saveButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearOutput());
        toolBar.add(clearButton);
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

        SimpleAttributeSet plainStyle = createStyle(COLOR_PLAIN, false);
        SimpleAttributeSet keywordStyle = createStyle(
                COLOR_KEYWORD, true);
        SimpleAttributeSet typeStyle = createStyle(COLOR_TYPE, true);
        SimpleAttributeSet stringStyle = createStyle(COLOR_STRING, false);
        SimpleAttributeSet commentStyle = createStyle(
                COLOR_COMMENT, false);
        SimpleAttributeSet decoratorStyle = createStyle(
                COLOR_DECORATOR, true);
        SimpleAttributeSet numberStyle = createStyle(COLOR_NUMBER, false);
        SimpleAttributeSet modifierStyle = createStyle(
                COLOR_MODIFIER, true);

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
                    doc.insertString(doc.getLength(), "\n",
                            plainStyle);
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

    private static SimpleAttributeSet createStyle(Color color,
            boolean bold) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 13);
        return style;
    }

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
    }
}
