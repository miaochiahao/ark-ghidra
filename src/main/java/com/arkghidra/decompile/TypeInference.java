package com.arkghidra.decompile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Tracks type information through register assignments during decompilation.
 *
 * <p>Maps instruction patterns to ArkTS types and provides type annotations
 * for variable declarations. The type tracker maintains a register-to-type map
 * that is updated as instructions are processed.
 */
public class TypeInference {

    private final Map<String, String> registerTypes;
    private final Map<String, String> methodReturnTypes;

    /**
     * Constructs a new type inference engine with empty state.
     */
    public TypeInference() {
        this.registerTypes = new HashMap<>();
        this.methodReturnTypes = new HashMap<>();
    }

    /**
     * Infers the ArkTS type for an instruction that loads a value into the accumulator.
     *
     * @param insn the instruction
     * @return the inferred ArkTS type name, or null if unknown
     */
    public String inferTypeForInstruction(ArkInstruction insn) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        if (opcode == ArkOpcodesCompat.LDAI || opcode == ArkOpcodesCompat.FLDAI) {
            return "number";
        }
        if (opcode == ArkOpcodesCompat.LDA_STR) {
            return "string";
        }
        if (opcode == ArkOpcodesCompat.LDTRUE || opcode == ArkOpcodesCompat.LDFALSE) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.LDNULL) {
            return "null";
        }
        if (opcode == ArkOpcodesCompat.LDUNDEFINED) {
            return "undefined";
        }
        if (opcode == ArkOpcodesCompat.CREATEEMPTYOBJECT
                || opcode == ArkOpcodesCompat.CREATEOBJECTWITHBUFFER) {
            return "Object";
        }
        if (opcode == ArkOpcodesCompat.CREATEEMPTYARRAY
                || opcode == ArkOpcodesCompat.CREATEARRAYWITHBUFFER) {
            return "Array<unknown>";
        }
        if (opcode == ArkOpcodesCompat.LDA) {
            int reg = (int) operands.get(0).getValue();
            return registerTypes.getOrDefault("v" + reg, null);
        }
        if (opcode == ArkOpcodesCompat.LDTHIS) {
            return "Object";
        }
        if (opcode == ArkOpcodesCompat.LDNAN || opcode == ArkOpcodesCompat.LDINFINITY) {
            return "number";
        }
        if (isBinaryArithmeticOp(opcode)) {
            return "number";
        }
        if (isComparisonOp(opcode)) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.TYPEOF) {
            return "string";
        }
        if (opcode == ArkOpcodesCompat.NOT) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.NEG || opcode == ArkOpcodesCompat.INC
                || opcode == ArkOpcodesCompat.DEC) {
            return "number";
        }
        if (isCallOpcode(opcode)) {
            return inferCallReturnType(insn);
        }
        if (isPropertyLoadOpcode(opcode)) {
            return null;
        }
        return null;
    }

    /**
     * Records the type of a register after a store instruction.
     *
     * @param registerName the register name (e.g. "v0")
     * @param typeName the ArkTS type name
     */
    public void setRegisterType(String registerName, String typeName) {
        if (typeName != null) {
            registerTypes.put(registerName, typeName);
        }
    }

    /**
     * Returns the inferred type for a register.
     *
     * @param registerName the register name
     * @return the type name, or null if unknown
     */
    public String getRegisterType(String registerName) {
        return registerTypes.get(registerName);
    }

    /**
     * Registers a known method return type.
     *
     * @param methodName the method name
     * @param returnType the return type
     */
    public void setMethodReturnType(String methodName, String returnType) {
        methodReturnTypes.put(methodName, returnType);
    }

    /**
     * Infers the ArkTS type annotation string for a variable declaration.
     *
     * @param registerName the register name
     * @param inferredType the inferred type (may be null)
     * @return the type annotation string, or null if type is unknown or redundant
     */
    public static String formatTypeAnnotation(String registerName, String inferredType) {
        if (inferredType == null) {
            return null;
        }
        // Skip 'Object' as it is the default and adds noise
        if ("Object".equals(inferredType)) {
            return null;
        }
        return inferredType;
    }

    /**
     * Returns true if the opcode is a binary arithmetic operation producing a number.
     *
     * @param opcode the opcode
     * @return true if arithmetic
     */
    public static boolean isBinaryArithmeticOp(int opcode) {
        return opcode == ArkOpcodesCompat.ADD2
                || opcode == ArkOpcodesCompat.SUB2
                || opcode == ArkOpcodesCompat.MUL2
                || opcode == ArkOpcodesCompat.DIV2
                || opcode == ArkOpcodesCompat.MOD2
                || opcode == ArkOpcodesCompat.SHL2
                || opcode == ArkOpcodesCompat.SHR2
                || opcode == ArkOpcodesCompat.ASHR2
                || opcode == ArkOpcodesCompat.AND2
                || opcode == ArkOpcodesCompat.OR2
                || opcode == ArkOpcodesCompat.XOR2
                || opcode == ArkOpcodesCompat.EXP;
    }

    /**
     * Returns true if the opcode is a comparison operation producing a boolean.
     *
     * @param opcode the opcode
     * @return true if comparison
     */
    public static boolean isComparisonOp(int opcode) {
        return opcode == ArkOpcodesCompat.EQ
                || opcode == ArkOpcodesCompat.NOTEQ
                || opcode == ArkOpcodesCompat.LESS
                || opcode == ArkOpcodesCompat.LESSEQ
                || opcode == ArkOpcodesCompat.GREATER
                || opcode == ArkOpcodesCompat.GREATEREQ
                || opcode == ArkOpcodesCompat.STRICTEQ
                || opcode == ArkOpcodesCompat.STRICTNOTEQ
                || opcode == ArkOpcodesCompat.INSTANCEOF
                || opcode == ArkOpcodesCompat.ISIN
                || opcode == ArkOpcodesCompat.ISTRUE
                || opcode == ArkOpcodesCompat.ISFALSE;
    }

    private static boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3;
    }

    private static boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME;
    }

    private String inferCallReturnType(ArkInstruction insn) {
        if (!insn.getOperands().isEmpty()) {
            int methodIdx = (int) insn.getOperands().get(0).getValue();
            return methodReturnTypes.getOrDefault("method_" + methodIdx, null);
        }
        return null;
    }

    /**
     * Returns true if the operator string is a comparison operator.
     *
     * @param op the operator string
     * @return true if comparison
     */
    public static boolean isComparisonOpFromSymbol(String op) {
        return "==".equals(op) || "!=".equals(op)
                || "===".equals(op) || "!==".equals(op)
                || "<".equals(op) || "<=".equals(op)
                || ">".equals(op) || ">=".equals(op)
                || "instanceof".equals(op) || "in".equals(op);
    }

    /**
     * Returns true if the operator string is a binary arithmetic operator.
     *
     * @param op the operator string
     * @return true if arithmetic
     */
    public static boolean isBinaryArithmeticOpFromSymbol(String op) {
        return "+".equals(op) || "-".equals(op)
                || "*".equals(op) || "/".equals(op)
                || "%".equals(op) || "**".equals(op)
                || "<<".equals(op) || ">>>".equals(op) || ">>".equals(op)
                || "&".equals(op) || "|".equals(op) || "^".equals(op);
    }
}
