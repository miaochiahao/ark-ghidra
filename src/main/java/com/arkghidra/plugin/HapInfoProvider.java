package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

import com.arkghidra.loader.HapMetadata;

/**
 * Dockable panel that displays HAP module metadata parsed from module.json5.
 *
 * <p>Shows the module name, type, version, package, and the list of declared
 * abilities. Clicking an ability entry triggers a navigation callback
 * so the caller can jump to the corresponding class in the ABC structure.</p>
 */
public class HapInfoProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final JLabel headerLabel;
    private final JTextPane infoArea;
    private final JList<String> abilitiesList;
    private final DefaultListModel<String> abilitiesModel;

    private List<String> abilityNames = Collections.emptyList();
    private Consumer<String> abilityClickCallback;

    /**
     * Constructs the HAP Info provider.
     *
     * @param tool  the Ghidra tool that owns this provider
     * @param owner the plugin name used as the component owner
     */
    public HapInfoProvider(Tool tool, String owner) {
        super(tool, "HAP Info", owner);

        headerLabel = new JLabel("No HAP loaded");

        infoArea = new JTextPane();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        abilitiesModel = new DefaultListModel<>();
        abilitiesList = new JList<>(abilitiesModel);
        abilitiesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        abilitiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        abilitiesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleAbilityClick();
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(infoArea),
                new JScrollPane(abilitiesList));
        splitPane.setResizeWeight(0.3);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("HAP Info");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the callback invoked when the user clicks an ability entry.
     *
     * @param cb consumer that receives the ability name (e.g. "EntryAbility")
     */
    public void setAbilityClickCallback(Consumer<String> cb) {
        this.abilityClickCallback = cb;
    }

    /**
     * Populates the panel with the given HAP metadata.
     * Passing {@code null} resets the panel to its empty state.
     *
     * @param metadata the parsed HAP metadata, or null to clear
     */
    public void showMetadata(HapMetadata metadata) {
        if (metadata == null) {
            headerLabel.setText("No HAP loaded");
            try {
                infoArea.getStyledDocument().remove(0, infoArea.getStyledDocument().getLength());
            } catch (BadLocationException e) {
                infoArea.setText("");
            }
            abilitiesModel.clear();
            abilityNames = Collections.emptyList();
            return;
        }

        headerLabel.setText("Module: " + metadata.getModuleName());
        renderMetadataHighlighted(metadata);

        abilitiesModel.clear();
        List<String> names = new ArrayList<>();
        for (HapMetadata.AbilityInfo ability : metadata.getAbilities()) {
            String entry = buildAbilityEntry(ability);
            abilitiesModel.addElement(entry);
            names.add(ability.getName());
        }
        abilityNames = Collections.unmodifiableList(names);
    }

    private void renderMetadataHighlighted(HapMetadata metadata) {
        StyledDocument doc = infoArea.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            return;
        }

        SimpleAttributeSet keyStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keyStyle, new Color(0x0000CC));
        StyleConstants.setBold(keyStyle, true);
        StyleConstants.setFontFamily(keyStyle, "Monospaced");
        StyleConstants.setFontSize(keyStyle, 12);

        SimpleAttributeSet valueStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(valueStyle, new Color(0x008000));
        StyleConstants.setBold(valueStyle, false);
        StyleConstants.setFontFamily(valueStyle, "Monospaced");
        StyleConstants.setFontSize(valueStyle, 12);

        SimpleAttributeSet numStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(numStyle, new Color(0xFF8C00));
        StyleConstants.setBold(numStyle, false);
        StyleConstants.setFontFamily(numStyle, "Monospaced");
        StyleConstants.setFontSize(numStyle, 12);

        appendKeyValue(doc, "Module:   ", metadata.getModuleName(), keyStyle, valueStyle);
        appendKeyValue(doc, "Type:     ", metadata.getModuleType(), keyStyle, valueStyle);
        appendKeyValue(doc, "Package:  ", metadata.getPackageName(), keyStyle, valueStyle);
        appendKeyValue(doc, "Version:  ", metadata.getVersionName(), keyStyle, valueStyle);
        appendKeyValue(doc, "Build:    ", String.valueOf(metadata.getVersionCode()), keyStyle, numStyle);
        if (!metadata.getVendorName().isEmpty()) {
            appendKeyValue(doc, "Vendor:   ", metadata.getVendorName(), keyStyle, valueStyle);
        }

        infoArea.setCaretPosition(0);
    }

    private static void appendKeyValue(StyledDocument doc, String key, String value,
            SimpleAttributeSet keyStyle, SimpleAttributeSet valueStyle) {
        try {
            doc.insertString(doc.getLength(), key, keyStyle);
            doc.insertString(doc.getLength(), value + "\n", valueStyle);
        } catch (BadLocationException e) {
            // ignore
        }
    }

    private static String buildAbilityEntry(HapMetadata.AbilityInfo ability) {
        StringBuilder sb = new StringBuilder(ability.getName());
        if (!ability.getType().isEmpty()) {
            sb.append("  [").append(ability.getType()).append(']');
        }
        if (!ability.getLabel().isEmpty()) {
            sb.append("  \"").append(ability.getLabel()).append('"');
        }
        return sb.toString();
    }

    private void handleAbilityClick() {
        int idx = abilitiesList.getSelectedIndex();
        if (idx < 0 || idx >= abilityNames.size() || abilityClickCallback == null) {
            return;
        }
        abilityClickCallback.accept(abilityNames.get(idx));
    }
}
