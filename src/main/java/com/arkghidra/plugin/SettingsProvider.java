package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private final JComboBox<String> fontFamilyCombo;
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

        String[] fontFamilies = {"Monospaced", "Consolas", "Courier New", "DejaVu Sans Mono", "Source Code Pro"};
        fontFamilyCombo = new JComboBox<>(fontFamilies);
        fontFamilyCombo.setSelectedItem("Monospaced");
        fontFamilyCombo.setToolTipText("Font family for the decompiled code view");

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
        JLabel hintLabel = new JLabel(
                "<html><small>Increase timeout for complex methods that time out.</small></html>");
        formPanel.add(hintLabel, gbc);

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
     * Adds a listener that is notified when the font family selection changes.
     *
     * @param listener the action listener
     */
    public void addFontChangeListener(java.awt.event.ActionListener listener) {
        fontFamilyCombo.addActionListener(listener);
    }
}
