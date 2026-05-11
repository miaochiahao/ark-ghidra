package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel for writing per-method notes.
 *
 * <p>Notes are stored in memory keyed by method/class name and persist
 * for the duration of the session. When the current view changes, the
 * notes for the new method are loaded automatically.</p>
 */
public class NotesProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final JTextArea notesArea;
    private final JLabel headerLabel;

    private final Map<String, String> notesMap = new HashMap<>();
    private String currentKey = "";
    private boolean updating = false;

    public NotesProvider(Tool tool, String owner) {
        super(tool, "Notes", owner);

        headerLabel = new JLabel("No method selected");
        headerLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        notesArea = new JTextArea();
        notesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEnabled(false);

        notesArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveCurrentNotes();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                saveCurrentNotes();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveCurrentNotes();
            }
        });

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton exportButton = new JButton("Export Notes...");
        exportButton.setToolTipText("Export all notes to a text file");
        exportButton.addActionListener(e -> exportNotes());
        toolBar.add(exportButton);
        mainPanel.add(toolBar, BorderLayout.SOUTH);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Notes");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Switches the notes panel to show notes for the given method/class.
     *
     * @param key the method or class name to show notes for
     */
    public void setCurrentKey(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        saveCurrentNotes();
        currentKey = key;
        headerLabel.setText(key);
        updating = true;
        String existing = notesMap.getOrDefault(key, "");
        notesArea.setText(existing);
        notesArea.setEnabled(true);
        notesArea.setCaretPosition(0);
        updating = false;
    }

    /**
     * Returns the notes for the given key, or empty string if none.
     *
     * @param key the method or class name
     * @return the notes text
     */
    public String getNotes(String key) {
        return notesMap.getOrDefault(key, "");
    }

    /**
     * Returns true if there are any notes for the given key.
     *
     * @param key the method or class name
     * @return true if notes exist
     */
    public boolean hasNotes(String key) {
        String notes = notesMap.get(key);
        return notes != null && !notes.isEmpty();
    }

    private void saveCurrentNotes() {
        if (updating || currentKey.isEmpty()) {
            return;
        }
        String text = notesArea.getText();
        if (text == null || text.isEmpty()) {
            notesMap.remove(currentKey);
        } else {
            notesMap.put(currentKey, text);
        }
    }

    private void exportNotes() {
        if (notesMap.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    mainPanel, "No notes to export.", "Export Notes",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Notes");
        chooser.setSelectedFile(new File("method_notes.txt"));
        if (chooser.showSaveDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("Method Notes\n");
            writer.write("============\n\n");
            // Sort by key for consistent output
            for (Map.Entry<String, String> entry : new TreeMap<>(notesMap).entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.write("## " + entry.getKey() + "\n");
                    writer.write(entry.getValue());
                    writer.write("\n\n");
                }
            }
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(
                    mainPanel, "Export failed: " + e.getMessage(),
                    "Export Notes", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
