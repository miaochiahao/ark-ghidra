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
     * Cache for string resolution keyed by string table index.
     * Avoids redundant MUTF-8 decoding when the same string index
     * is referenced multiple times during decompilation.
     */
    private final Map<Integer, String> stringResolveCache = new HashMap<>();

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
        this.cfg = cfg;
        this.instructions = instructions;
        this.numArgs = code != null ? (int) code.getNumArgs() : 0;
        this.isAsync = false;
        this.currentAccValue = null;
        this.restParamIndex = -1;
        this.restParamRegister = -1;
        this.loopContextStack = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Pushes a loop context onto the stack.
     *
     * @param headerOffset the loop header (condition) offset
     * @param endOffset the offset just past the loop end
     */
    public void pushLoopContext(int headerOffset, int endOffset) {
        loopContextStack.add(new int[] {headerOffset, endOffset});
    }

    /**
     * Pops the most recent loop context from the stack.
     */
    public void popLoopContext() {
        if (!loopContextStack.isEmpty()) {
            loopContextStack.remove(loopContextStack.size() - 1);
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
     * Resolves a string table index to a string value.
     * Returns a placeholder if the string cannot be resolved.
     * Results are cached per string index.
     *
     * @param stringIdx the string table index
     * @return the resolved string or a placeholder
     */
    public String resolveString(int stringIdx) {
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
            try {
                List<Long> lnpIndex = abcFile.getLnpIndex();
                if (stringIdx >= 0 && stringIdx < lnpIndex.size()) {
                    long strOff = lnpIndex.get(stringIdx);
                    byte[] data = abcFile.getRawData();
                    if (data != null && strOff >= 0
                            && strOff < data.length) {
                        return readMutf8At(data, (int) strOff);
                    }
                }
            } catch (Exception e) {
                // Fall through to placeholder
            }
        }
        return "str_" + stringIdx;
    }

    /**
     * Reads a Modified UTF-8 encoded string from raw bytecode data.
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
}
