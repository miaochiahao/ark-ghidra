package com.arkghidra.plugin;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import docking.ComponentProvider;
import docking.Tool;
import docking.WindowPosition;

/**
 * Dockable panel that shows the call graph for the currently decompiled method.
 *
 * <p>The panel has two sections:
 * <ul>
 *   <li><b>Calls</b> — methods that the current code calls (callees)</li>
 *   <li><b>Called by</b> — methods that call the current symbol (callers)</li>
 * </ul>
 * Callee detection parses the decompiled code text for {@code identifier(} patterns
 * and cross-references them against the known method list.  Caller detection requires
 * the full file code map and searches every other method for a call to the current name.
 * Double-clicking an entry triggers the navigation callback.</p>
 */
public class CallGraphProvider extends ComponentProvider {

    private static final String OWNER = CallGraphProvider.class.getSimpleName();
    private static final Pattern CALL_PATTERN = Pattern.compile("\\b([A-Za-z_$][\\w$]*)\\s*\\(");
    private static final Set<String> SKIP_WORDS = Set.of(
            "if", "for", "while", "switch", "catch",
            "function", "class", "new", "return", "typeof", "instanceof");

    private final JPanel mainPanel;
    private final JLabel headerLabel;
    private final DefaultListModel<String> calleesModel;
    private final DefaultListModel<String> callersModel;
    private final JList<String> calleesList;
    private final JList<String> callersList;

    private Consumer<String> navigationCallback;

    public CallGraphProvider(Tool tool, String owner) {
        super(tool, "Call Graph", owner);

        headerLabel = new JLabel("No method selected");

        calleesModel = new DefaultListModel<>();
        calleesList = new JList<>(calleesModel);
        calleesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        calleesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        calleesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleCalleesClick();
                }
            }
        });

        callersModel = new DefaultListModel<>();
        callersList = new JList<>(callersModel);
        callersList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        callersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        callersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleCallersClick();
                }
            }
        });

        JPanel calleesPanel = new JPanel(new BorderLayout());
        calleesPanel.add(new JLabel("Calls:"), BorderLayout.NORTH);
        calleesPanel.add(new JScrollPane(calleesList), BorderLayout.CENTER);

        JPanel callersPanel = new JPanel(new BorderLayout());
        callersPanel.add(new JLabel("Called by:"), BorderLayout.NORTH);
        callersPanel.add(new JScrollPane(callersList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, calleesPanel, callersPanel);
        splitPane.setResizeWeight(0.5);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        setDefaultWindowPosition(WindowPosition.BOTTOM);
        setTitle("Call Graph");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Sets the callback invoked when the user double-clicks a method entry.
     *
     * @param cb consumer that receives the bare method name (without trailing {@code ()})
     */
    public void setNavigationCallback(Consumer<String> cb) {
        this.navigationCallback = cb;
    }

    /**
     * Updates the call graph for the given method using only the current method's code.
     * Caller detection is not available in this mode; a placeholder is shown instead.
     *
     * @param methodName     the current method/function name
     * @param currentCode    the decompiled code of the current method
     * @param allMethodNames list of all known method names in the file
     */
    public void showCallGraph(String methodName, String currentCode, List<String> allMethodNames) {
        calleesModel.clear();
        callersModel.clear();

        if (methodName == null || methodName.isEmpty()
                || currentCode == null || currentCode.isEmpty()) {
            headerLabel.setText("No method selected");
            return;
        }

        headerLabel.setText(methodName);

        Set<String> callees = extractCallees(currentCode, allMethodNames);
        for (String callee : callees) {
            calleesModel.addElement(callee + "()");
        }
        if (callees.isEmpty()) {
            calleesModel.addElement("(no outgoing calls detected)");
        }

        callersModel.addElement("(load full file to see callers)");
    }

    /**
     * Updates the call graph with full file context so both callers and callees can be shown.
     *
     * @param methodName  the current method name
     * @param currentCode the current method's decompiled code
     * @param allCode     map of method name to decompiled code for all methods in the file
     */
    public void showCallGraphWithCallers(String methodName, String currentCode,
            Map<String, String> allCode) {
        calleesModel.clear();
        callersModel.clear();

        if (methodName == null || methodName.isEmpty()) {
            headerLabel.setText("No method selected");
            return;
        }

        headerLabel.setText(methodName);

        List<String> allMethodNames = new ArrayList<>(allCode.keySet());

        Set<String> callees = extractCallees(currentCode, allMethodNames);
        for (String callee : callees) {
            calleesModel.addElement(callee + "()");
        }
        if (callees.isEmpty()) {
            calleesModel.addElement("(no outgoing calls detected)");
        }

        Set<String> callers = new LinkedHashSet<>();
        Pattern callerPattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(");
        for (Map.Entry<String, String> entry : allCode.entrySet()) {
            if (!entry.getKey().equals(methodName)) {
                Matcher m = callerPattern.matcher(entry.getValue());
                if (m.find()) {
                    callers.add(entry.getKey());
                }
            }
        }
        for (String caller : callers) {
            callersModel.addElement(caller + "()");
        }
        if (callers.isEmpty()) {
            callersModel.addElement("(no callers detected)");
        }
    }

    private Set<String> extractCallees(String code, List<String> knownMethods) {
        Set<String> callees = new LinkedHashSet<>();
        if (code == null || code.isEmpty()) {
            return callees;
        }
        Set<String> knownSet = new LinkedHashSet<>(knownMethods);
        Matcher m = CALL_PATTERN.matcher(code);
        while (m.find()) {
            String name = m.group(1);
            if (!SKIP_WORDS.contains(name) && knownSet.contains(name)) {
                callees.add(name);
            }
        }
        return callees;
    }

    private void handleCalleesClick() {
        int idx = calleesList.getSelectedIndex();
        if (idx < 0 || navigationCallback == null) {
            return;
        }
        String entry = calleesModel.getElementAt(idx);
        if (entry.endsWith("()")) {
            navigationCallback.accept(entry.substring(0, entry.length() - 2));
        }
    }

    private void handleCallersClick() {
        int idx = callersList.getSelectedIndex();
        if (idx < 0 || navigationCallback == null) {
            return;
        }
        String entry = callersModel.getElementAt(idx);
        if (entry.endsWith("()")) {
            navigationCallback.accept(entry.substring(0, entry.length() - 2));
        }
    }
}
