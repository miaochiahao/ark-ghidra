package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Handles property access and call translation during ArkTS decompilation.
 *
 * <p>Translates property load/store instructions (ldobjbyname, stobjbyvalue,
 * etc.) and call instructions (callarg0, callthisrange, etc.) into their
 * corresponding ArkTS expression trees.
 */
class PropertyAccessHandler {

    private static final String ACC = "acc";

    private PropertyAccessHandler() {
        // utility class — all methods are static
    }

    // --- Call translation ---

    static ArkTSExpression translateCall(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression callee;
        List<ArkTSExpression> args = new ArrayList<>();

        switch (opcode) {
            case ArkOpcodesCompat.CALLARG0:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
            case ArkOpcodesCompat.CALLARG1:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                break;
            case ArkOpcodesCompat.CALLARGS2:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                break;
            case ArkOpcodesCompat.CALLARGS3:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(3).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS0:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
            case ArkOpcodesCompat.CALLTHIS1:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS2:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS3:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(3).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHISRANGE:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                "v" + (firstReg + a)));
                    }
                }
                break;
            case ArkOpcodesCompat.CALLRANGE:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                "v" + (firstReg + a)));
                    }
                }
                break;
            default:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
        }

        return new ArkTSExpression.CallExpression(callee, args);
    }

    // --- Property load translation ---

    static ArkTSExpression translatePropertyLoad(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.LDSUPERBYNAME
                || opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            obj = new ArkTSExpression.VariableExpression("super");
        } else {
            obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }

        if (opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            int reg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new ArkTSExpression.MemberExpression(obj, prop, true);
        }

        if (opcode == ArkOpcodesCompat.LDOBJBYINDEX) {
            int index = (int) operands.get(1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.LiteralExpression(
                            String.valueOf(index),
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            return new ArkTSExpression.MemberExpression(obj, prop, true);
        }

        // Name-based access
        String propName = ctx.resolveString(
                (int) operands.get(1).getValue());
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.MemberExpression(obj, prop, false);
    }

    // --- Property store translation ---

    static ArkTSExpression translatePropertyStore(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.STSUPERBYNAME
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            obj = new ArkTSExpression.VariableExpression("super");
        } else {
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            obj = new ArkTSExpression.VariableExpression("v" + objReg);
        }

        ArkTSExpression prop;
        if (opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            int keyReg = (int) operands.get(
                    operands.size() - 2).getValue();
            prop = new ArkTSExpression.VariableExpression("v" + keyReg);
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, prop, true),
                    accValue != null ? accValue
                            : new ArkTSExpression.VariableExpression(ACC));
        }

        if (opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STOWNBYINDEX) {
            int index = (int) operands.get(2).getValue();
            prop = new ArkTSExpression.LiteralExpression(
                    String.valueOf(index),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, prop, true),
                    accValue != null ? accValue
                            : new ArkTSExpression.VariableExpression(ACC));
        }

        // Name-based
        String propName = ctx.resolveString(
                (int) operands.get(1).getValue());
        prop = new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.AssignExpression(
                new ArkTSExpression.MemberExpression(obj, prop, false),
                accValue != null ? accValue
                        : new ArkTSExpression.VariableExpression(ACC));
    }
}
