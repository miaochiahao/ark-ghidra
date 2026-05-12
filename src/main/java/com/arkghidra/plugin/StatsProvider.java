package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        int publicCount = 0;
        int privateCount = 0;
        int protectedCount = 0;
        int staticCount = 0;
        int abstractCount = 0;
        int abilityCount = 0;
        int pageCount = 0;
        int interfaceCount = 0;
        int enumCount = 0;
        int annCount = 0;
        int largeMethodCount = 0;
        int mediumMethodCount = 0;

        // Track top 5 largest methods
        List<long[]> methodSizes = new ArrayList<>(); // [size, classIdx, methodIdx]

        for (AbcClass cls : abcFile.getClasses()) {
            totalFields += cls.getFields().size();
            String badge = AbcStructureProvider.getClassTypeBadge(cls);
            if ("[A] ".equals(badge)) {
                abilityCount++;
            } else if ("[P] ".equals(badge)) {
                pageCount++;
            } else if ("[I] ".equals(badge)) {
                interfaceCount++;
            } else if ("[E] ".equals(badge)) {
                enumCount++;
            } else if ("[Ann] ".equals(badge)) {
                annCount++;
            }
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
                        if (size > 200) {
                            largeMethodCount++;
                        } else if (size > 50) {
                            mediumMethodCount++;
                        }
                        String clsName = AbcStructureProvider.formatClassName(cls.getName());
                        String label = clsName + "." + method.getName() + " (" + size + "b)";
                        methodSizes.add(new long[]{size, label.hashCode()});
                        // Store label separately
                        methodSizes.get(methodSizes.size() - 1);
                    }
                } catch (Exception e) {
                    // skip methods that fail to load
                }
            }
        }

        // Collect top 5 largest methods properly
        List<String[]> topMethods = new ArrayList<>();
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() == 0) {
                    continue;
                }
                try {
                    AbcCode code = abcFile.getCodeForMethod(method);
                    if (code != null) {
                        long size = code.getCodeSize();
                        String clsName = AbcStructureProvider.formatClassName(cls.getName());
                        String simpleName = clsName.contains(".")
                                ? clsName.substring(clsName.lastIndexOf('.') + 1) : clsName;
                        topMethods.add(new String[]{
                            String.valueOf(size),
                            simpleName + "." + method.getName() + " (" + size + "b)"
                        });
                    }
                } catch (Exception e) {
                    // skip
                }
            }
        }
        topMethods.sort(Comparator.comparingLong((String[] a) -> Long.parseLong(a[0])).reversed());

        int nonAbstract = totalMethods - abstractCount;
        long avgSize = nonAbstract > 0 ? totalBytes / nonAbstract : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("ABC File Statistics\n");
        sb.append("===================\n\n");
        sb.append(String.format("Classes:          %d%n", totalClasses));
        sb.append(String.format("  [A] Abilities:  %d%n", abilityCount));
        sb.append(String.format("  [P] Pages:      %d%n", pageCount));
        sb.append(String.format("  [I] Interfaces: %d%n", interfaceCount));
        sb.append(String.format("  [E] Enums:      %d%n", enumCount));
        sb.append(String.format("  [Ann] Annot.:   %d%n", annCount));
        sb.append(String.format("  [C] Classes:    %d%n",
                totalClasses - abilityCount - pageCount - interfaceCount - enumCount - annCount));
        sb.append(String.format("Methods:          %d%n", totalMethods));
        sb.append(String.format("  Abstract:       %d%n", abstractCount));
        sb.append(String.format("  With code:      %d%n", nonAbstract));
        sb.append(String.format("  Large (>200b):  %d%n", largeMethodCount));
        sb.append(String.format("  Medium (>50b):  %d%n", mediumMethodCount));
        sb.append(String.format("Fields:           %d%n", totalFields));
        sb.append("\n");
        sb.append(String.format("Total bytecode:   %d bytes%n", totalBytes));
        sb.append(String.format("Avg method size:  %d bytes%n", avgSize));
        sb.append("\n");
        sb.append("Top 5 largest methods:\n");
        int limit = Math.min(5, topMethods.size());
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("  %d. %s%n", i + 1, topMethods.get(i)[1]));
        }
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
