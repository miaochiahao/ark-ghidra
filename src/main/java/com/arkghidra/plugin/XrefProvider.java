package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel that shows cross-references for the currently highlighted symbol.
 *
 * <p>Each entry displays the 1-based line number and the trimmed line text.
 * Clicking an entry navigates the ArkTS output panel to that line.</p>
 */
public class XrefProvider extends ComponentProvider {

    private static final String OWNER = XrefProvider.class.getSimpleName();

    private final JPanel mainPanel;
    private final JList<String> xrefList;
    private final DefaultListModel<String> listModel;
    private final JLabel headerLabel;

    private XrefNavigationListener navigationListener;
    private List<Integer> lineOffsets = Collections.emptyList();

    public XrefProvider(Tool tool, String owner) {
        super(tool, "Xref", owner);

        listModel = new DefaultListModel<>();
        xrefList = new JList<>(listModel);
        xrefList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        xrefList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        xrefList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleXrefClick();
                }
            }
        });

        headerLabel = new JLabel("No symbol selected");

        JScrollPane scrollPane = new JScrollPane(xrefList);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Xref");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the listener that is notified when the user clicks an xref entry.
     *
     * @param listener the navigation listener, or null to disable navigation
     */
    public void setNavigationListener(XrefNavigationListener listener) {
        this.navigationListener = listener;
    }

    /**
     * Updates the xref list for the given symbol and code text.
     *
     * @param symbol      the symbol name to search for
     * @param code        the full decompiled code text
     * @param highlighter the symbol highlighter used to find occurrences
     */
    public void showXrefs(String symbol, String code, SymbolHighlighter highlighter) {
        listModel.clear();
        lineOffsets = new ArrayList<>();

        if (symbol == null || symbol.isEmpty() || code == null || code.isEmpty()) {
            headerLabel.setText("No symbol selected");
            return;
        }

        List<Integer> positions = highlighter.findAllOccurrences(code, symbol);
        String[] lines = code.split("\n", -1);

        int[] lineStarts = new int[lines.length];
        int offset = 0;
        for (int i = 0; i < lines.length; i++) {
            lineStarts[i] = offset;
            offset += lines[i].length() + 1;
        }

        for (int pos : positions) {
            int lineNum = findLineNumber(pos, lineStarts);
            String lineText = lines[lineNum].trim();
            listModel.addElement("L" + (lineNum + 1) + ":  " + lineText);
            lineOffsets.add(lineStarts[lineNum]);
        }

        headerLabel.setText("\"" + symbol + "\" \u2014 " + positions.size() + " occurrences");
    }

    private int findLineNumber(int pos, int[] lineStarts) {
        for (int i = 0; i < lineStarts.length - 1; i++) {
            if (pos >= lineStarts[i] && pos < lineStarts[i + 1]) {
                return i;
            }
        }
        return lineStarts.length - 1;
    }

    private void handleXrefClick() {
        int idx = xrefList.getSelectedIndex();
        if (idx < 0 || idx >= lineOffsets.size() || navigationListener == null) {
            return;
        }
        navigationListener.onXrefSelected(lineOffsets.get(idx));
    }
}
