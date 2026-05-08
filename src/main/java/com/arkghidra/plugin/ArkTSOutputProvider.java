package com.arkghidra.plugin;

import java.awt.BorderLayout;
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
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import docking.ComponentProvider;
import docking.Tool;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import ghidra.util.HelpLocation;
import ghidra.util.Msg;

/**
 * Component provider that displays decompiled ArkTS source code.
 *
 * <p>Provides a text area with monospaced font for viewing results, along
 * with toolbar actions for copying to clipboard and saving to file.</p>
 */
public class ArkTSOutputProvider extends ComponentProvider {

    private static final String OWNER =
            ArkTSOutputProvider.class.getSimpleName();

    private final JPanel mainPanel;
    private final JTextArea codeArea;
    private final JLabel headerLabel;

    public ArkTSOutputProvider(Tool tool, String owner) {
        super(tool, "ArkTS Output", owner);

        codeArea = new JTextArea(20, 60);
        codeArea.setEditable(false);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        codeArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(codeArea);

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
     * Displays decompiled ArkTS source code.
     *
     * @param functionName the name of the decompiled function
     * @param code the decompiled source code
     */
    public void showDecompiledCode(String functionName, String code) {
        headerLabel.setText("Decompiled: " + functionName);
        codeArea.setText(code);
        codeArea.setCaretPosition(0);
    }

    /**
     * Shows an informational message in the output area.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        headerLabel.setText("Info");
        codeArea.setText(message);
    }

    /**
     * Gets the currently displayed code text.
     *
     * @return the code text
     */
    public String getCodeText() {
        return codeArea.getText();
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

    private void copyToClipboard() {
        String text = codeArea.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
        Msg.info(OWNER, "Copied decompiled code to clipboard");
    }

    private void saveToFile() {
        String text = codeArea.getText();
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
        codeArea.setText("");
        headerLabel.setText("No decompiled code");
    }
}
