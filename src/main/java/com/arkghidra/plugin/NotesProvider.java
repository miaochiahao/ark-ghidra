package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
}
