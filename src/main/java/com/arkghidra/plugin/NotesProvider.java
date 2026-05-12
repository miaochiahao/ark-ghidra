package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel for writing per-method notes.
 *
 * <p>Notes are stored in memory keyed by method/class name and persist
 * for the duration of the session. When the current view changes, the
 * notes for the new method are loaded automatically.</p>
 *
 * <p>The panel uses a split layout: the left side shows all annotated
 * keys (sorted), and the right side shows the notes for the selected key.</p>
 */
public class NotesProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final JTextArea notesArea;
    private final JLabel headerLabel;
    private final JList<String> annotatedList;
    private final DefaultListModel<String> annotatedModel;

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

        annotatedModel = new DefaultListModel<>();
        annotatedList = new JList<>(annotatedModel);
        annotatedList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        annotatedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annotatedList.addListSelectionListener((ListSelectionListener) e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = annotatedList.getSelectedValue();
                if (selected != null) {
                    setCurrentKey(selected);
                }
            }
        });
        annotatedList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String key = (String) value;
                String notes = notesMap.get(key);
                if (notes != null && !notes.isEmpty()) {
                    String firstLine = notes.split("\n")[0].trim();
                    if (firstLine.length() > 80) {
                        firstLine = firstLine.substring(0, 77) + "...";
                    }
                    setToolTipText(firstLine);
                } else {
                    setToolTipText(null);
                }
                return this;
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(annotatedList),
                new JScrollPane(notesArea));
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.3);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton exportButton = new JButton("Export Notes...");
        exportButton.setToolTipText("Export all notes to a text file");
        exportButton.addActionListener(e -> exportNotes());
        toolBar.add(exportButton);
        JButton showAllButton = new JButton("Show All");
        showAllButton.setToolTipText("Show all notes across all methods");
        showAllButton.addActionListener(e -> showAllNotes());
        toolBar.add(showAllButton);
        JButton loadButton = new JButton("Load Notes...");
        loadButton.setToolTipText("Load notes from a previously exported file");
        loadButton.addActionListener(e -> loadNotesFromFile());
        toolBar.add(loadButton);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
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
        updateAnnotatedList();
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

    /**
     * Returns all notes as an unmodifiable map.
     *
     * @return unmodifiable view of all notes keyed by method/class name
     */
    public Map<String, String> getAllNotes() {
        return Collections.unmodifiableMap(notesMap);
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
        updateAnnotatedList();
    }

    private void updateAnnotatedList() {
        annotatedModel.clear();
        for (String key : new TreeSet<>(notesMap.keySet())) {
            String notes = notesMap.get(key);
            if (notes != null && !notes.trim().isEmpty()) {
                annotatedModel.addElement(key);
            }
        }
    }

    private void loadNotesFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Notes");
        if (chooser.showOpenDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            String content = new String(
                    Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8);
            int loaded = parseAndLoadNotes(content);
            updateAnnotatedList();
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Loaded " + loaded + " note" + (loaded == 1 ? "" : "s") + " from " + file.getName(),
                    "Load Notes",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    mainPanel, "Load failed: " + e.getMessage(),
                    "Load Notes", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseAndLoadNotes(String content) {
        int count = 0;
        String[] sections = content.split("\n## ");
        for (String section : sections) {
            if (section.startsWith("## ")) {
                section = section.substring(3);
            }
            int newline = section.indexOf('\n');
            if (newline < 0) {
                continue;
            }
            String key = section.substring(0, newline).trim();
            String notes = section.substring(newline + 1).trim();
            if (!key.isEmpty() && !notes.isEmpty()) {
                notesMap.put(key, notes);
                count++;
            }
        }
        return count;
    }

    private void exportNotes() {
        if (notesMap.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel, "No notes to export.", "Export Notes",
                    JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(
                    mainPanel, "Export failed: " + e.getMessage(),
                    "Export Notes", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllNotes() {
        Map<String, String> allNotes = new TreeMap<>(notesMap);
        // Filter out empty notes
        allNotes.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().trim().isEmpty());

        if (allNotes.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel, "No notes yet.", "All Notes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build a list of "methodName: first line of notes"
        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<String> keys = new ArrayList<>(allNotes.keySet());
        for (String key : keys) {
            String notes = allNotes.get(key);
            String firstLine = notes.split("\n")[0].trim();
            if (firstLine.length() > 60) {
                firstLine = firstLine.substring(0, 57) + "...";
            }
            listModel.addElement(key + ": " + firstLine);
        }

        JList<String> notesList = new JList<>(listModel);
        notesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JDialog dialog = new JDialog();
        dialog.setTitle("All Notes (" + keys.size() + " methods)");
        dialog.setModal(false);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(mainPanel);

        notesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int idx = notesList.getSelectedIndex();
                    if (idx >= 0 && idx < keys.size()) {
                        setCurrentKey(keys.get(idx));
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.add(new JScrollPane(notesList));
        dialog.setVisible(true);
    }
}
