package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/** Dockable panel that lists all ArkTS Decompiler keyboard shortcuts by category. */
public class ShortcutsProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final JTextArea shortcutsArea;

    private static final String SHORTCUTS_TEXT =
            "ArkTS Decompiler \u2014 Keyboard Shortcuts\n"
            + "======================================\n\n"
            + "NAVIGATION\n"
            + "  Alt+Left          Navigate back\n"
            + "  Alt+Right         Navigate forward\n"
            + "  Ctrl+P            Quick Open (jump to class/method)\n"
            + "  Ctrl+Down         Next method definition\n"
            + "  Ctrl+Up           Previous method definition\n"
            + "  \u25c4 Class / Class \u25ba  Previous/next class\n\n"
            + "SEARCH\n"
            + "  Ctrl+F            Find in current view\n"
            + "  Ctrl+H            Find & Replace\n"
            + "  Ctrl+Shift+F      Global search across all methods\n"
            + "  F3                Next occurrence\n"
            + "  Shift+F3          Previous occurrence\n\n"
            + "EDITING\n"
            + "  Ctrl+A            Select all\n"
            + "  Ctrl+Shift+C      Copy current line\n"
            + "  Ctrl+G            Go to line\n"
            + "  Ctrl+B            Add bookmark\n\n"
            + "PANELS\n"
            + "  Ctrl+Shift+E      Show HAP Explorer\n"
            + "  Ctrl+Shift+B      Show Bookmarks\n"
            + "  Ctrl+Shift+H      Show History\n"
            + "  Ctrl+Shift+X      Show Xref\n"
            + "  Ctrl+Shift+N      Show Notes\n"
            + "  Ctrl+Shift+,      Show Settings\n\n"
            + "FONT\n"
            + "  Ctrl+=            Increase font size\n"
            + "  Ctrl+-            Decrease font size\n"
            + "  Ctrl+0            Reset font size\n\n"
            + "DECOMPILATION\n"
            + "  Double-click      Decompile method/class\n"
            + "  Enter (in tree)   Decompile selected node\n"
            + "  Click             Highlight all occurrences\n"
            + "  Right-click       Context menu\n"
            + "  Hover             Show occurrence count\n\n"
            + "HAP EXPLORER FILTER SYNTAX\n"
            + "  args:N            Methods with exactly N args\n"
            + "  args:>N           Methods with more than N args\n"
            + "  args:<N           Methods with fewer than N args\n"
            + "  size:>N           Methods larger than N bytes\n"
            + "  size:<N           Methods smaller than N bytes\n"
            + "  name:pattern      Methods whose name contains pattern\n"
            + "  class:pattern     Classes whose name contains pattern\n\n"
            + "EXPORT\n"
            + "  HTML...           Export with dark or light theme\n"
            + "  Save...           Export as plain .ets file\n"
            + "  Export All...     Export all classes to directory\n"
            + "  Copy as Markdown  Wrap in ```typescript fences\n";

    public ShortcutsProvider(Tool tool, String owner) {
        super(tool, "Shortcuts", owner);

        shortcutsArea = new JTextArea(SHORTCUTS_TEXT);
        shortcutsArea.setEditable(false);
        shortcutsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        shortcutsArea.setCaretPosition(0);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(shortcutsArea), BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Shortcuts");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
