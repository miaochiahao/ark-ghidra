package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcCode;

/**
 * Detects parameter default values from a method's instruction prologue.
 *
 * <p>Ark bytecode encodes default parameter values as a sequence of
 * conditional checks at the beginning of a method body. The compiler
 * generates code that checks whether each parameter is undefined and,
 * if so, assigns the default value. The typical patterns are:
 *
 * <p><b>Pattern A (jneundefined):</b>
 * <pre>
 *   lda vN                     ; load parameter register
 *   jneundefined skip          ; if param !== undefined, skip default
 *   &lt;load default value&gt;      ; ldai, lda.str, ldtrue, etc.
 *   sta vN                     ; store default into param register
 *   skip:                      ; rest of method body
 * </pre>
 *
 * <p><b>Pattern B (lda + ldundefined + stricteq + jnez):</b>
 * <pre>
 *   lda vN                     ; load parameter register
 *   ldundefined                ; load undefined
 *   stricteq 0, vN             ; acc = (vN === undefined)
 *   jnez setDefault            ; if strictly equal (is undefined), set default
 *   jmp skip
 *   setDefault:
 *   &lt;load default value&gt;
 *   sta vN
 *   skip:
 * </pre>
 *
 * <p><b>Pattern C (lda + ldundefined + eq + jeqz):</b>
 * <pre>
 *   lda vN                     ; load parameter into acc
 *   ldundefined                ; push undefined for comparison
 *   eq 0, vN                   ; acc = (vN == undefined)
 *   jeqz skip                  ; if NOT equal (has value), skip
 *   &lt;load default value&gt;
 *   sta vN
 *   skip:
 * </pre>
 *
 * <p>For each detected default, the parameter is marked with either
 * a concrete default value or as optional ({@code param?: type}) when
 * the default is {@code undefined} or {@code null}.
 */
class ParameterDefaultDetector {

    /**
     * Result of parameter default detection.
     * Contains a default value string and an optional flag.
     */
    static class ParamDefault {
        final String defaultValue;
        final boolean isOptional;

        ParamDefault(String defaultValue, boolean isOptional) {
            this.defaultValue = defaultValue;
            this.isOptional = isOptional;
        }
    }

    /**
     * Detects parameter default values from a method's instructions.
     *
     * <p>Scans the prologue instructions looking for the default parameter
     * assignment pattern. Returns a list (parallel to params) where each
     * entry is either null (no default) or a ParamDefault with the detected
     * default value.
     *
     * @param instructions the method's disassembled instructions
     * @param numArgs the number of declared arguments
     * @return list of detected defaults, one per parameter index (may contain nulls)
     */
    static List<ParamDefault> detectDefaults(
            List<ArkInstruction> instructions, int numArgs) {
        List<ParamDefault> defaults =
                new ArrayList<>(numArgs);
        for (int i = 0; i < numArgs; i++) {
            defaults.add(null);
        }

        if (instructions == null || instructions.isEmpty() || numArgs == 0) {
            return defaults;
        }

        int idx = 0;
        int insnCount = instructions.size();
        int prologueLimit = Math.min(insnCount, numArgs * 8);

        while (idx < prologueLimit) {
            DetectionResult detected = tryDetectDefault(
                    instructions, idx, prologueLimit, numArgs);
            if (detected == null) {
                break;
            }
            idx = detected.nextIndex;

            int paramIdx = detected.paramIndex;
            if (paramIdx >= 0 && paramIdx < numArgs) {
                defaults.set(paramIdx, new ParamDefault(
                        detected.defaultValue, detected.isOptional));
            }
        }

        return defaults;
    }

    /**
     * Internal result for default detection at a specific position.
     */
    private static class DetectionResult {
        final int paramIndex;
        final String defaultValue;
        final boolean isOptional;
        final int nextIndex;

        DetectionResult(int paramIndex, String defaultValue,
                boolean isOptional, int nextIndex) {
            this.paramIndex = paramIndex;
            this.defaultValue = defaultValue;
            this.isOptional = isOptional;
            this.nextIndex = nextIndex;
        }
    }

    /**
     * Attempts to detect a single parameter default starting at the given
     * instruction index.
     */
    private static DetectionResult tryDetectDefault(
            List<ArkInstruction> instructions, int startIdx,
            int limit, int numArgs) {

        // Pattern A: lda vN; jneundefined skip; <load>; sta vN
        DetectionResult result = tryPatternJneUndefined(
                instructions, startIdx, limit, numArgs);
        if (result != null) {
            return result;
        }

        // Pattern B: lda vN; ldundefined; stricteq 0, vN; jnez setDefault;
        //            jmp skip; <load>; sta vN; skip:
        result = tryPatternStrictEq(
                instructions, startIdx, limit, numArgs);
        if (result != null) {
            return result;
        }

        // Pattern C: lda vN; ldundefined; eq 0, vN; jeqz skip;
        //            <load>; sta vN; skip:
        result = tryPatternEq(
                instructions, startIdx, limit, numArgs);
        if (result != null) {
            return result;
        }

        return null;
    }

    /**
     * Pattern A: lda vN; jneundefined skip; &lt;loadDefault&gt;; sta vN
     */
    private static DetectionResult tryPatternJneUndefined(
            List<ArkInstruction> instructions, int startIdx,
            int limit, int numArgs) {
        if (startIdx + 3 >= limit) {
            return null;
        }

        ArkInstruction ldaInsn = instructions.get(startIdx);
        if (getNormalizedOpcode(ldaInsn) != ArkOpcodesCompat.LDA) {
            return null;
        }
        int paramReg = (int) ldaInsn.getOperands().get(0).getValue();
        if (paramReg >= numArgs) {
            return null;
        }

        ArkInstruction jumpInsn = instructions.get(startIdx + 1);
        int jumpOpcode = getNormalizedOpcode(jumpInsn);
        if (jumpOpcode != ArkOpcodesCompat.JNEUNDEFINED_IMM8
                && jumpOpcode != ArkOpcodesCompat.JNEUNDEFINED_IMM16) {
            return null;
        }

        // Calculate the jump target (skip past default)
        int jumpTarget = ControlFlowGraph.getJumpTargetPublic(jumpInsn);

        // The default value is loaded between the jump and the target
        int defaultLoadIdx = startIdx + 2;
        if (defaultLoadIdx >= instructions.size()) {
            return null;
        }

        String defaultValue = extractDefaultValue(
                instructions, defaultLoadIdx);
        if (defaultValue == null) {
            return null;
        }

        // Verify sta vN at the expected position
        int staIdx = defaultLoadIdx + 1;
        // For multi-instruction default loads (e.g. fldai is 9 bytes),
        // find the sta instruction
        while (staIdx < limit && staIdx < instructions.size()) {
            ArkInstruction staCandidate = instructions.get(staIdx);
            if (getNormalizedOpcode(staCandidate) == ArkOpcodesCompat.STA) {
                int staReg = (int) staCandidate.getOperands()
                        .get(0).getValue();
                if (staReg == paramReg) {
                    boolean isOptional = "undefined".equals(defaultValue)
                            || "null".equals(defaultValue);
                    int nextIdx = staIdx + 1;
                    // Verify next instruction is at or past the jump target
                    if (nextIdx < instructions.size()
                            && instructions.get(nextIdx).getOffset()
                                    >= jumpTarget) {
                        return new DetectionResult(paramReg,
                                defaultValue, isOptional, nextIdx);
                    }
                    // Still accept even if we can't verify the target
                    return new DetectionResult(paramReg,
                            defaultValue, isOptional, nextIdx);
                }
            }
            staIdx++;
        }

        return null;
    }

    /**
     * Pattern B: lda vN; ldundefined; stricteq 0, vN; jnez setDefault;
     *            jmp skip; &lt;loadDefault&gt;; sta vN; skip:
     */
    private static DetectionResult tryPatternStrictEq(
            List<ArkInstruction> instructions, int startIdx,
            int limit, int numArgs) {
        if (startIdx + 6 >= limit) {
            return null;
        }

        ArkInstruction ldaInsn = instructions.get(startIdx);
        if (getNormalizedOpcode(ldaInsn) != ArkOpcodesCompat.LDA) {
            return null;
        }
        int paramReg = (int) ldaInsn.getOperands().get(0).getValue();
        if (paramReg >= numArgs) {
            return null;
        }

        ArkInstruction ldundefinedInsn = instructions.get(startIdx + 1);
        if (getNormalizedOpcode(ldundefinedInsn)
                != ArkOpcodesCompat.LDUNDEFINED) {
            return null;
        }

        ArkInstruction strictEqInsn = instructions.get(startIdx + 2);
        if (getNormalizedOpcode(strictEqInsn)
                != ArkOpcodesCompat.STRICTEQ) {
            return null;
        }

        ArkInstruction jnezInsn = instructions.get(startIdx + 3);
        int jnezOpcode = getNormalizedOpcode(jnezInsn);
        if (jnezOpcode != ArkOpcodesCompat.JNEZ_IMM8
                && jnezOpcode != ArkOpcodesCompat.JNEZ_IMM16) {
            return null;
        }

        ArkInstruction jmpInsn = instructions.get(startIdx + 4);
        int jmpOpcode = getNormalizedOpcode(jmpInsn);
        if (jmpOpcode != ArkOpcodesCompat.JMP_IMM8
                && jmpOpcode != ArkOpcodesCompat.JMP_IMM16) {
            return null;
        }

        int defaultLoadIdx = startIdx + 5;
        if (defaultLoadIdx >= instructions.size()) {
            return null;
        }
        String defaultValue = extractDefaultValue(
                instructions, defaultLoadIdx);
        if (defaultValue == null) {
            return null;
        }

        int staIdx = defaultLoadIdx + 1;
        while (staIdx < limit && staIdx < instructions.size()) {
            ArkInstruction staCandidate = instructions.get(staIdx);
            if (getNormalizedOpcode(staCandidate)
                    == ArkOpcodesCompat.STA) {
                int staReg = (int) staCandidate.getOperands()
                        .get(0).getValue();
                if (staReg == paramReg) {
                    boolean isOptional = "undefined".equals(defaultValue)
                            || "null".equals(defaultValue);
                    return new DetectionResult(paramReg,
                            defaultValue, isOptional, staIdx + 1);
                }
            }
            staIdx++;
        }

        return null;
    }

    /**
     * Pattern C: lda vN; ldundefined; eq 0, vN; jeqz skip;
     *            &lt;loadDefault&gt;; sta vN; skip:
     */
    private static DetectionResult tryPatternEq(
            List<ArkInstruction> instructions, int startIdx,
            int limit, int numArgs) {
        if (startIdx + 5 >= limit) {
            return null;
        }

        ArkInstruction ldaInsn = instructions.get(startIdx);
        if (getNormalizedOpcode(ldaInsn) != ArkOpcodesCompat.LDA) {
            return null;
        }
        int paramReg = (int) ldaInsn.getOperands().get(0).getValue();
        if (paramReg >= numArgs) {
            return null;
        }

        ArkInstruction ldundefinedInsn = instructions.get(startIdx + 1);
        if (getNormalizedOpcode(ldundefinedInsn)
                != ArkOpcodesCompat.LDUNDEFINED) {
            return null;
        }

        ArkInstruction eqInsn = instructions.get(startIdx + 2);
        if (getNormalizedOpcode(eqInsn) != ArkOpcodesCompat.EQ) {
            return null;
        }

        ArkInstruction jeqzInsn = instructions.get(startIdx + 3);
        int jeqzOpcode = getNormalizedOpcode(jeqzInsn);
        if (jeqzOpcode != ArkOpcodesCompat.JEQZ_IMM8
                && jeqzOpcode != ArkOpcodesCompat.JEQZ_IMM16) {
            return null;
        }

        int defaultLoadIdx = startIdx + 4;
        if (defaultLoadIdx >= instructions.size()) {
            return null;
        }
        String defaultValue = extractDefaultValue(
                instructions, defaultLoadIdx);
        if (defaultValue == null) {
            return null;
        }

        int staIdx = defaultLoadIdx + 1;
        while (staIdx < limit && staIdx < instructions.size()) {
            ArkInstruction staCandidate = instructions.get(staIdx);
            if (getNormalizedOpcode(staCandidate)
                    == ArkOpcodesCompat.STA) {
                int staReg = (int) staCandidate.getOperands()
                        .get(0).getValue();
                if (staReg == paramReg) {
                    boolean isOptional = "undefined".equals(defaultValue)
                            || "null".equals(defaultValue);
                    return new DetectionResult(paramReg,
                            defaultValue, isOptional, staIdx + 1);
                }
            }
            staIdx++;
        }

        return null;
    }

    /**
     * Extracts the default value string from a load instruction.
     *
     * @param instructions the instruction list
     * @param idx the index of the load instruction
     * @return the default value string, or null if not a recognized load
     */
    private static String extractDefaultValue(
            List<ArkInstruction> instructions, int idx) {
        if (idx >= instructions.size()) {
            return null;
        }
        ArkInstruction insn = instructions.get(idx);
        int opcode = getNormalizedOpcode(insn);
        List<ArkOperand> operands = insn.getOperands();

        switch (opcode) {
            case ArkOpcodesCompat.LDAI:
                return String.valueOf(operands.get(0).getValue());
            case ArkOpcodesCompat.FLDAI:
                double val = Double.longBitsToDouble(
                        operands.get(0).getValue());
                return String.valueOf(val);
            case ArkOpcodesCompat.LDA_STR:
                return "str_" + operands.get(0).getValue();
            case ArkOpcodesCompat.LDUNDEFINED:
                return "undefined";
            case ArkOpcodesCompat.LDNULL:
                return "null";
            case ArkOpcodesCompat.LDTRUE:
                return "true";
            case ArkOpcodesCompat.LDFALSE:
                return "false";
            case ArkOpcodesCompat.LDNAN:
                return "NaN";
            case ArkOpcodesCompat.LDINFINITY:
                return "Infinity";
            default:
                return null;
        }
    }

    /**
     * Gets the normalized opcode, handling wide prefix.
     */
    private static int getNormalizedOpcode(ArkInstruction insn) {
        return insn.isWide()
                ? ArkOpcodesCompat.normalizeWideOpcode(insn.getOpcode())
                : insn.getOpcode();
    }

    /**
     * Detects parameter defaults from an AbcCode section.
     *
     * <p>Convenience method that disassembles the code and runs detection.
     *
     * @param code the ABC code section (may be null)
     * @param numArgs the number of declared arguments
     * @return list of detected defaults
     */
    static List<ParamDefault> detectDefaults(AbcCode code, int numArgs) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0 || numArgs == 0) {
            List<ParamDefault> empty = new ArrayList<>(numArgs);
            for (int i = 0; i < numArgs; i++) {
                empty.add(null);
            }
            return empty;
        }
        ArkDisassembler disasm = new ArkDisassembler();
        List<ArkInstruction> instructions =
                disasm.disassemble(code.getInstructions(),
                        0, (int) code.getCodeSize());
        return detectDefaults(instructions, numArgs);
    }
}
