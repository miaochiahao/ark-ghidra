package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

/**
 * Holds shared state during decompilation of a single method.
 * Package-private for testing (referenced as
 * {@code ArkTSDecompiler.DecompilationContext}).
 */
public class DecompilationContext {
    final AbcMethod method;
    final AbcCode code;
    final AbcProto proto;
    final AbcFile abcFile;
    final ControlFlowGraph cfg;
    final List<ArkInstruction> instructions;
    final int numArgs;
    boolean isAsync;
    ArkTSExpression currentAccValue;

    /**
     * Index of the rest parameter in the parameter list, or -1 if none.
     * Detected by scanning instructions for COPYRESTARGS opcode.
     */
    int restParamIndex;

    /**
     * The register number that receives the rest parameter array,
     * or -1 if not detected.
     */
    int restParamRegister;

    /**
     * Accumulated warnings during decompilation.
     */
    final List<String> warnings;

    /**
     * Tracks the last expression stored to each register.
     * Used to inline function expressions as call arguments.
     */
    private final Map<Integer, ArkTSExpression> registerExpressions =
            new HashMap<>();

    /**
     * Set of register numbers that are captured by closures (inner functions).
     * When an inner function references variables from the outer scope via
     * lexical access, the corresponding registers are tracked here.
     */
    private final Set<Integer> capturedRegisters = new HashSet<>();

    /**
     * Stack of loop contexts for break/continue detection.
     * Each entry is [loopHeaderOffset, loopEndOffset].
     */
    final List<int[]> loopContextStack;

    /**
     * Stack of loop labels for labeled break/continue support.
     * Parallel to {@link #loopContextStack}; may contain null
     * entries for unlabeled loops.
     */
    private final List<String> loopLabelStack;

    /**
     * Counter for auto-generating unique loop labels.
     */
    private int loopLabelCounter;

    /**
     * Cache for string resolution keyed by string table index.
     * Delegates to the file-level cache in AbcFile when available;
     * falls back to a per-method cache when no AbcFile is set.
     */
    private Map<Integer, String> stringResolveCache;

    /**
     * Maps register numbers to debug variable names.
     * Populated from {@link com.arkghidra.format.AbcLocalVariable} entries.
     */
    private final Map<Integer, String> registerNames = new HashMap<>();

    /**
     * Maps register numbers to context-inferred variable names.
     * Used as fallback when debug names are not available.
     * Inferred from call results, property access, etc.
     */
    private final Map<Integer, String> inferredNames = new HashMap<>();

    /**
     * Maps lexical variable keys ("level_slot") to inferred names.
     * Populated when STLEXVAR stores a value with a known name.
     */
    private final Map<String, String> lexVarNames = new HashMap<>();

    /**
     * Maps bytecode offsets to source line numbers from debug info.
     * Populated from {@link com.arkghidra.format.AbcLineNumberEntry} data.
     */
    private final Map<Integer, Long> lineNumberMap = new HashMap<>();

    /**
     * Tracks the last source line number emitted as a comment.
     * Prevents duplicate line comments when multiple instructions
     * map to the same source line.
     */
    private long lastEmittedLine = -1;

    /**
     * Constructs a decompilation context.
     *
     * @param method the method being decompiled
     * @param code the method's code section
     * @param proto the method's prototype (may be null)
     * @param abcFile the parent ABC file (may be null)
     * @param cfg the control flow graph
     * @param instructions the decoded instructions
     */
    public DecompilationContext(AbcMethod method, AbcCode code,
            AbcProto proto, AbcFile abcFile,
            ControlFlowGraph cfg,
            List<ArkInstruction> instructions) {
        this.method = method;
        this.code = code;
        this.proto = proto;
        this.abcFile = abcFile;
        if (abcFile != null) {
            stringResolveCache = null;
        } else {
            stringResolveCache = new HashMap<>();
        }
        this.cfg = cfg;
        this.instructions = instructions;
        this.numArgs = code != null ? (int) code.getNumArgs() : 0;
        this.isAsync = false;
        this.currentAccValue = null;
        this.restParamIndex = -1;
        this.restParamRegister = -1;
        this.loopContextStack = new ArrayList<>();
        this.loopLabelStack = new ArrayList<>();
        this.loopLabelCounter = 0;
        this.warnings = new ArrayList<>();
    }

    /**
     * Pushes a loop context onto the stack (unlabeled).
     *
     * @param headerOffset the loop header (condition) offset
     * @param endOffset the offset just past the loop end
     */
    public void pushLoopContext(int headerOffset, int endOffset) {
        pushLoopContext(headerOffset, endOffset, null);
    }

    /**
     * Pushes a loop context onto the stack with an optional label.
     *
     * @param headerOffset the loop header (condition) offset
     * @param endOffset the offset just past the loop end
     * @param label the loop label, or null for unlabeled
     */
    public void pushLoopContext(int headerOffset, int endOffset,
            String label) {
        loopContextStack.add(new int[] {headerOffset, endOffset});
        loopLabelStack.add(label);
    }

    /**
     * Pops the most recent loop context from the stack.
     */
    public void popLoopContext() {
        if (!loopContextStack.isEmpty()) {
            loopContextStack.remove(loopContextStack.size() - 1);
            loopLabelStack.remove(loopLabelStack.size() - 1);
        }
    }

    /**
     * Returns the current innermost loop context, or null.
     *
     * @return [headerOffset, endOffset] or null
     */
    public int[] getCurrentLoopContext() {
        if (loopContextStack.isEmpty()) {
            return null;
        }
        return loopContextStack.get(loopContextStack.size() - 1);
    }

    /**
     * Returns the label of the current innermost loop, or null.
     *
     * @return the loop label or null
     */
    public String getCurrentLoopLabel() {
        if (loopLabelStack.isEmpty()) {
            return null;
        }
        return loopLabelStack.get(loopLabelStack.size() - 1);
    }

    /**
     * Generates a unique label for a loop and returns it.
     *
     * @return the generated label (e.g. "loop_0")
     */
    public String generateLoopLabel() {
        return "loop_" + (loopLabelCounter++);
    }

    /**
     * Finds the label for a break jump that targets the given offset.
     * If the innermost loop matches, returns null (unlabeled break).
     * If an outer loop matches, returns that loop's label.
     *
     * @param jumpTarget the target offset of the jump
     * @return the label for a labeled break, or null for unlabeled
     */
    public String findBreakLabel(int jumpTarget) {
        if (loopContextStack.isEmpty()) {
            return null;
        }
        // Check from innermost (last) to outermost (first)
        for (int i = loopContextStack.size() - 1; i >= 0; i--) {
            int[] ctx = loopContextStack.get(i);
            int loopEnd = ctx[1];
            if (jumpTarget >= loopEnd) {
                // Innermost match -> unlabeled break
                if (i == loopContextStack.size() - 1) {
                    return null;
                }
                // Outer match -> labeled break
                return loopLabelStack.get(i);
            }
        }
        return null;
    }

    /**
     * Finds the label for a continue jump that targets the given offset.
     * If the innermost loop header matches, returns null (unlabeled continue).
     * If an outer loop header matches, returns that loop's label.
     *
     * @param jumpTarget the target offset of the jump
     * @return the label for a labeled continue, or null for unlabeled
     */
    public String findContinueLabel(int jumpTarget) {
        if (loopContextStack.isEmpty()) {
            return null;
        }
        // Check from innermost (last) to outermost (first)
        for (int i = loopContextStack.size() - 1; i >= 0; i--) {
            int[] ctx = loopContextStack.get(i);
            int loopHeader = ctx[0];
            int loopEnd = ctx[1];
            if (jumpTarget == loopHeader && jumpTarget < loopEnd) {
                // Innermost match -> unlabeled continue
                if (i == loopContextStack.size() - 1) {
                    return null;
                }
                // Outer match -> labeled continue
                return loopLabelStack.get(i);
            }
        }
        return null;
    }

    /**
     * Resolves a string table index to a string value.
     * Returns a placeholder if the string cannot be resolved.
     * When an AbcFile is available, delegates to the file-level cache.
     * Otherwise uses a per-method cache.
     *
     * @param stringIdx the string table index
     * @return the resolved string or a placeholder
     */
    public String resolveString(int stringIdx) {
        if (abcFile != null) {
            return abcFile.resolveStringByIndex(stringIdx);
        }
        String cached = stringResolveCache.get(stringIdx);
        if (cached != null) {
            return cached;
        }
        String result = resolveStringUncached(stringIdx);
        stringResolveCache.put(stringIdx, result);
        return result;
    }

    private String resolveStringUncached(int stringIdx) {
        if (abcFile != null) {
            // resolveString() already delegates to abcFile.resolveStringByIndex()
            // which uses the combined index. This method is only reached when
            // the file-level cache is not available, so delegate directly.
            return abcFile.resolveStringByIndex(stringIdx);
        }
        return "str_" + stringIdx;
    }

    /**
     * Sets the debug name for a register.
     *
     * @param reg the register number
     * @param name the variable name from debug info
     */
    public void setRegisterName(int reg, String name) {
        registerNames.put(reg, name);
    }

    /**
     * Returns whether a register has a debug name.
     *
     * @param reg the register number
     * @return true if a debug name is set
     */
    public boolean hasRegisterName(int reg) {
        return registerNames.containsKey(reg);
    }

    /**
     * Returns the debug name for a register, or null if not available.
     *
     * @param reg the register number
     * @return the variable name, or null
     */
    public String getRegisterName(int reg) {
        return registerNames.get(reg);
    }

    /**
     * Returns the variable name for a register: debug name if available,
     * inferred name if available, otherwise "v" + reg.
     *
     * @param reg the register number
     * @return the variable name to use in output
     */
    public String resolveRegisterName(int reg) {
        String name = registerNames.get(reg);
        if (name != null) {
            return name;
        }
        String inferred = inferredNames.get(reg);
        return inferred != null ? inferred : "v" + reg;
    }

    /**
     * Sets an inferred name for a register. Only sets if no debug name
     * exists for the register.
     *
     * @param reg the register number
     * @param name the inferred variable name
     */
    public void setInferredName(int reg, String name) {
        if (name != null && !name.isEmpty()
                && !registerNames.containsKey(reg)) {
            String uniqueName = uniquifyName(name, reg);
            inferredNames.put(reg, uniqueName);
        }
    }

    private String uniquifyName(String name, int skipReg) {
        if (!isNameUsed(name, skipReg)) {
            return name;
        }
        for (int i = 2; i < 100; i++) {
            String candidate = name + i;
            if (!isNameUsed(candidate, skipReg)) {
                return candidate;
            }
        }
        return name;
    }

    private boolean isNameUsed(String name, int skipReg) {
        for (var entry : registerNames.entrySet()) {
            if (entry.getKey() != skipReg
                    && name.equals(entry.getValue())) {
                return true;
            }
        }
        for (var entry : inferredNames.entrySet()) {
            if (entry.getKey() != skipReg
                    && name.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the null terminator and basic MUTF-8 multi-byte sequences.
     *
     * @param data the raw bytecode data
     * @param offset the starting offset
     * @return the decoded string
     */
    public static String readMutf8At(byte[] data, int offset) {
        int pos = offset;
        StringBuilder sb = new StringBuilder();
        while (pos < data.length && data[pos] != 0) {
            int b = data[pos] & 0xFF;
            if (b < 0x80) {
                sb.append((char) b);
                pos++;
            } else if ((b & 0xE0) == 0xC0) {
                if (pos + 1 < data.length) {
                    int b2 = data[pos + 1] & 0xFF;
                    char ch = (char) (((b & 0x1F) << 6)
                            | (b2 & 0x3F));
                    sb.append(ch);
                    pos += 2;
                } else {
                    break;
                }
            } else if ((b & 0xF0) == 0xE0) {
                if (pos + 2 < data.length) {
                    int b2 = data[pos + 1] & 0xFF;
                    int b3 = data[pos + 2] & 0xFF;
                    char ch = (char) (((b & 0x0F) << 12)
                            | ((b2 & 0x3F) << 6) | (b3 & 0x3F));
                    sb.append(ch);
                    pos += 3;
                } else {
                    break;
                }
            } else {
                pos++;
            }
        }
        return sb.toString();
    }

    /**
     * Records the expression stored to a register.
     *
     * @param reg the register number
     * @param expr the expression stored
     */
    public void setRegisterExpression(int reg,
            ArkTSExpression expr) {
        registerExpressions.put(reg, expr);
    }

    /**
     * Gets the last expression stored to a register, or null.
     *
     * @param reg the register number
     * @return the expression or null
     */
    public ArkTSExpression getRegisterExpression(int reg) {
        return registerExpressions.get(reg);
    }

    /**
     * Marks a register as captured by a closure (inner function).
     *
     * @param reg the register number captured by an inner function
     */
    public void addCapturedRegister(int reg) {
        capturedRegisters.add(reg);
    }

    /**
     * Returns whether a register is captured by any inner function.
     *
     * @param reg the register number
     * @return true if the register is captured
     */
    public boolean isRegisterCaptured(int reg) {
        return capturedRegisters.contains(reg);
    }

    /**
     * Returns an unmodifiable view of all captured register numbers.
     *
     * @return the set of captured register numbers
     */
    public Set<Integer> getCapturedRegisters() {
        return Collections.unmodifiableSet(capturedRegisters);
    }

    /**
     * Stores a mapping from bytecode offset to source line number.
     *
     * @param offset the bytecode offset
     * @param line the source line number
     */
    public void setLineNumber(int offset, long line) {
        lineNumberMap.put(offset, line);
    }

    /**
     * Returns the source line number for a bytecode offset, or null.
     *
     * @param offset the bytecode offset
     * @return the source line number, or null
     */
    public Long getLineNumber(int offset) {
        return lineNumberMap.get(offset);
    }

    /**
     * Checks whether a line comment should be emitted for the given offset,
     * and updates the last-emitted line tracking. Returns the line number
     * if a comment should be emitted, or null otherwise.
     *
     * @param offset the bytecode offset
     * @return the source line number to emit, or null if already emitted
     */
    public Long checkAndMarkLineEmitted(int offset) {
        Long line = lineNumberMap.get(offset);
        if (line != null && line != lastEmittedLine) {
            lastEmittedLine = line;
            return line;
        }
        return null;
    }

    /**
     * Sets an inferred name for a lexical variable.
     *
     * @param level the scope level
     * @param slot the variable slot
     * @param name the inferred variable name
     */
    public void setLexVarName(int level, int slot, String name) {
        if (name != null && !name.isEmpty()) {
            lexVarNames.put(level + "_" + slot, name);
        }
    }

    /**
     * Returns the inferred name for a lexical variable, or null.
     *
     * @param level the scope level
     * @param slot the variable slot
     * @return the inferred name, or null
     */
    public String getLexVarName(int level, int slot) {
        return lexVarNames.get(level + "_" + slot);
    }

    /**
     * Resolves the display name for a lexical variable: inferred name
     * if available, otherwise "lex_level_slot".
     *
     * @param level the scope level
     * @param slot the variable slot
     * @return the variable name to use in output
     */
    public String resolveLexVarName(int level, int slot) {
        String name = lexVarNames.get(level + "_" + slot);
        return name != null ? name : "lex_" + level + "_" + slot;
    }
}
