package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel for configuring decompiler settings.
 *
 * <p>Provides controls for the per-method timeout and other decompiler options.
 * Settings are applied immediately and used for all subsequent decompilations.</p>
 */
public class SettingsProvider extends ComponentProvider {

    private static final long DEFAULT_TIMEOUT_MS = 5000L;
    private static final int TIMEOUT_MIN_S = 1;
    private static final int TIMEOUT_MAX_S = 60;

    private final JPanel mainPanel;
    private final JSpinner timeoutSpinner;
    private final JCheckBox autoDecompileCheckBox;
    private final JCheckBox skipTrivialCheckBox;
    private final JCheckBox showInlineNotesCheckBox;
    private final JCheckBox showComplexityHeaderCheckBox;
    private final JCheckBox autoSaveNotesCheckBox;
    private final JComboBox<String> fontFamilyCombo;
    private final JSlider lineSpacingSlider;
    private final JComboBox<String> themeCombo;
    private final JSpinner tabSizeSpinner;
    private final JLabel timeoutLabel;

    private long timeoutMs = DEFAULT_TIMEOUT_MS;
    private boolean autoDecompileOnLoad = true;

    public SettingsProvider(Tool tool, String owner) {
        super(tool, "Decompile Settings", owner);

        timeoutSpinner = new JSpinner(new SpinnerNumberModel(
                (int) (DEFAULT_TIMEOUT_MS / 1000),
                TIMEOUT_MIN_S, TIMEOUT_MAX_S, 1));
        timeoutSpinner.setFont(new Font("Monospaced", Font.PLAIN, 12));
        timeoutLabel = new JLabel("seconds per method");

        timeoutSpinner.addChangeListener(e -> {
            int secs = (Integer) timeoutSpinner.getValue();
            timeoutMs = secs * 1000L;
        });

        autoDecompileCheckBox = new JCheckBox("Auto-decompile on cursor move", true);
        autoDecompileCheckBox.setToolTipText(
                "Automatically decompile when cursor moves to a function in the Listing");

        skipTrivialCheckBox = new JCheckBox("Skip trivial methods (< 20 bytes)", false);
        skipTrivialCheckBox.setToolTipText(
                "Skip methods with fewer than 20 bytes when decompiling a class");

        showInlineNotesCheckBox = new JCheckBox("Show notes inline in decompiled output", true);
        showInlineNotesCheckBox.setToolTipText(
                "Prepend method notes as a comment block in the decompiled output");

        showComplexityHeaderCheckBox = new JCheckBox("Show complexity header in method output", true);
        showComplexityHeaderCheckBox.setToolTipText(
                "Show bytecode size and complexity label at the top of each decompiled method");

        autoSaveNotesCheckBox = new JCheckBox("Auto-save notes on program close", false);
        autoSaveNotesCheckBox.setToolTipText(
                "Automatically export notes to a file when Ghidra closes");

        lineSpacingSlider = new JSlider(0, 8, 2);
        lineSpacingSlider.setMajorTickSpacing(2);
        lineSpacingSlider.setMinorTickSpacing(1);
        lineSpacingSlider.setPaintTicks(true);
        lineSpacingSlider.setToolTipText("Line spacing (0=compact, 8=spacious)");

        String[] fontFamilies = {"Monospaced", "Consolas", "Courier New", "DejaVu Sans Mono", "Source Code Pro"};
        fontFamilyCombo = new JComboBox<>(fontFamilies);
        fontFamilyCombo.setSelectedItem("Monospaced");
        fontFamilyCombo.setToolTipText("Font family for the decompiled code view");

        String[] themes = {"Auto (follow Ghidra)", "Light", "Dark", "High Contrast"};
        themeCombo = new JComboBox<>(themes);
        themeCombo.setSelectedItem("Auto (follow Ghidra)");
        themeCombo.setToolTipText("Syntax highlighting color theme");

        tabSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 8, 2));
        tabSizeSpinner.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabSizeSpinner.setToolTipText("Number of spaces per indentation level");

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Timeout:"), gbc);
        gbc.gridx = 1;
        formPanel.add(timeoutSpinner, gbc);
        gbc.gridx = 2;
        formPanel.add(timeoutLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        formPanel.add(autoDecompileCheckBox, gbc);

        gbc.gridy = 2;
        formPanel.add(skipTrivialCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Font:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(fontFamilyCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        formPanel.add(showInlineNotesCheckBox, gbc);

        gbc.gridy = 5;
        formPanel.add(showComplexityHeaderCheckBox, gbc);

        gbc.gridy = 6;
        formPanel.add(autoSaveNotesCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Spacing:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(lineSpacingSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(themeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tab size:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(tabSizeSpinner, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("spaces"), gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 3;
        JLabel hintLabel = new JLabel(
                "<html><small>Increase timeout for complex methods that time out.</small></html>");
        formPanel.add(hintLabel, gbc);

        gbc.gridy = 10;
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.setToolTipText("Restore all settings to their default values");
        resetButton.addActionListener(e -> resetToDefaults());
        formPanel.add(resetButton, gbc);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.NORTH);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Decompile Settings");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Returns the configured per-method timeout in milliseconds.
     *
     * @return timeout in milliseconds
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Returns whether auto-decompile on cursor move is enabled.
     *
     * @return true if auto-decompile is enabled
     */
    public boolean isAutoDecompileOnLoad() {
        return autoDecompileCheckBox.isSelected();
    }

    /**
     * Returns whether trivial methods (< 20 bytes) should be skipped when decompiling a class.
     *
     * @return true if trivial methods should be skipped
     */
    public boolean isSkipTrivialMethods() {
        return skipTrivialCheckBox.isSelected();
    }

    /**
     * Returns whether method notes should be prepended as a comment block in decompiled output.
     *
     * @return true if inline notes are enabled
     */
    public boolean isShowInlineNotes() {
        return showInlineNotesCheckBox.isSelected();
    }

    /**
     * Returns whether the complexity header should be shown at the top of each decompiled method.
     *
     * @return true if the complexity header is enabled
     */
    public boolean isShowComplexityHeader() {
        return showComplexityHeaderCheckBox.isSelected();
    }

    /**
     * Returns whether notes should be auto-saved when the program closes.
     *
     * @return true if auto-save is enabled
     */
    public boolean isAutoSaveNotes() {
        return autoSaveNotesCheckBox.isSelected();
    }

    /**
     * Returns the current line spacing value (0=compact, 8=spacious).
     *
     * @return line spacing value in the range [0, 8]
     */
    public int getLineSpacing() {
        return lineSpacingSlider.getValue();
    }

    /**
     * Returns the configured tab size (number of spaces per indent level).
     *
     * @return tab size (2, 4, or 8)
     */
    public int getTabSize() {
        return (Integer) tabSizeSpinner.getValue();
    }

    /**
     * Adds a listener that is notified when any setting changes.
     *
     * @param listener the change listener
     */
    public void addSettingsChangeListener(ChangeListener listener) {
        timeoutSpinner.addChangeListener(listener);
        autoDecompileCheckBox.addChangeListener(listener);
        skipTrivialCheckBox.addChangeListener(listener);
        showInlineNotesCheckBox.addChangeListener(listener);
        showComplexityHeaderCheckBox.addChangeListener(listener);
        autoSaveNotesCheckBox.addChangeListener(listener);
        lineSpacingSlider.addChangeListener(listener);
        tabSizeSpinner.addChangeListener(listener);
    }

    /**
     * Returns the selected font family name.
     *
     * @return font family name, never null
     */
    public String getFontFamily() {
        Object selected = fontFamilyCombo.getSelectedItem();
        return selected != null ? selected.toString() : "Monospaced";
    }

    /**
     * Returns the selected syntax highlighting theme name.
     *
     * @return theme name, never null
     */
    public String getTheme() {
        Object selected = themeCombo.getSelectedItem();
        return selected != null ? selected.toString() : "Auto (follow Ghidra)";
    }

    /**
     * Adds a listener that is notified when the font family selection changes.
     *
     * @param listener the action listener
     */
    public void addFontChangeListener(java.awt.event.ActionListener listener) {
        fontFamilyCombo.addActionListener(listener);
        themeCombo.addActionListener(listener);
    }

    private void resetToDefaults() {
        timeoutSpinner.setValue((int) (DEFAULT_TIMEOUT_MS / 1000));
        timeoutMs = DEFAULT_TIMEOUT_MS;
        autoDecompileCheckBox.setSelected(true);
        skipTrivialCheckBox.setSelected(false);
        showInlineNotesCheckBox.setSelected(true);
        showComplexityHeaderCheckBox.setSelected(true);
        autoSaveNotesCheckBox.setSelected(false);
        fontFamilyCombo.setSelectedItem("Monospaced");
        lineSpacingSlider.setValue(2);
        themeCombo.setSelectedItem("Auto (follow Ghidra)");
        tabSizeSpinner.setValue(4);
    }
}
