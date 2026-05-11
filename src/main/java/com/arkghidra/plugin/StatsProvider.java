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

import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Dockable panel that displays statistics about the loaded ABC file.
 *
 * <p>Shows total class, method, and field counts, bytecode size metrics,
 * the largest method by code size, and a breakdown of access modifiers.</p>
 */
public class StatsProvider extends ComponentProvider {

    private final JPanel mainPanel;
    private final JTextArea statsArea;

    /**
     * Constructs the Stats provider.
     *
     * @param tool  the Ghidra tool that owns this provider
     * @param owner the plugin name used as the component owner
     */
    public StatsProvider(Tool tool, String owner) {
        super(tool, "ABC Stats", owner);

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setText("No ABC file loaded.");

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("ABC Stats");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Computes and displays statistics for the given ABC file.
     *
     * @param abcFile the parsed ABC file, or null to clear the panel
     */
    public void showStats(AbcFile abcFile) {
        if (abcFile == null) {
            statsArea.setText("No ABC file loaded.");
            return;
        }

        int totalClasses = abcFile.getClasses().size();
        int totalMethods = 0;
        int totalFields = 0;
        long totalBytes = 0;
        long maxSize = 0;
        String largestMethod = "";
        int publicCount = 0;
        int privateCount = 0;
        int protectedCount = 0;
        int staticCount = 0;
        int abstractCount = 0;

        for (AbcClass cls : abcFile.getClasses()) {
            totalFields += cls.getFields().size();
            for (AbcMethod method : cls.getMethods()) {
                totalMethods++;
                long flags = method.getAccessFlags();
                if ((flags & AbcAccessFlags.ACC_PUBLIC) != 0) {
                    publicCount++;
                }
                if ((flags & AbcAccessFlags.ACC_PRIVATE) != 0) {
                    privateCount++;
                }
                if ((flags & AbcAccessFlags.ACC_PROTECTED) != 0) {
                    protectedCount++;
                }
                if ((flags & AbcAccessFlags.ACC_STATIC) != 0) {
                    staticCount++;
                }
                if (method.getCodeOff() == 0) {
                    abstractCount++;
                    continue;
                }
                try {
                    AbcCode code = abcFile.getCodeForMethod(method);
                    if (code != null) {
                        long size = code.getCodeSize();
                        totalBytes += size;
                        if (size > maxSize) {
                            maxSize = size;
                            largestMethod = method.getName() + " (" + size + "b)";
                        }
                    }
                } catch (Exception e) {
                    // skip methods that fail to load
                }
            }
        }

        int nonAbstract = totalMethods - abstractCount;
        long avgSize = nonAbstract > 0 ? totalBytes / nonAbstract : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("ABC File Statistics\n");
        sb.append("===================\n\n");
        sb.append(String.format("Classes:          %d%n", totalClasses));
        sb.append(String.format("Methods:          %d%n", totalMethods));
        sb.append(String.format("  Abstract:       %d%n", abstractCount));
        sb.append(String.format("  With code:      %d%n", nonAbstract));
        sb.append(String.format("Fields:           %d%n", totalFields));
        sb.append("\n");
        sb.append(String.format("Total bytecode:   %d bytes%n", totalBytes));
        sb.append(String.format("Avg method size:  %d bytes%n", avgSize));
        sb.append(String.format("Largest method:   %s%n",
                largestMethod.isEmpty() ? "(none)" : largestMethod));
        sb.append("\n");
        sb.append("Access modifiers:\n");
        sb.append(String.format("  public:         %d%n", publicCount));
        sb.append(String.format("  private:        %d%n", privateCount));
        sb.append(String.format("  protected:      %d%n", protectedCount));
        sb.append(String.format("  static:         %d%n", staticCount));

        statsArea.setText(sb.toString());
        statsArea.setCaretPosition(0);
    }
}
