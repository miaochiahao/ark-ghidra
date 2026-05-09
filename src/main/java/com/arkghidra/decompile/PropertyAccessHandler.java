package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Handles property access, private field access, and call translation
 * during ArkTS decompilation.
 *
 * <p>Translates property load/store instructions (ldobjbyname, stobjbyvalue,
 * etc.), private field access (ldprivateproperty, stprivateproperty,
 * createprivateproperty, defineprivateproperty), and call instructions
 * (callarg0, callthisrange, etc.) into their corresponding ArkTS
 * expression trees.
 */
class PropertyAccessHandler {

    private static final String ACC = "acc";

    private PropertyAccessHandler() {
        // utility class — all methods are static
    }

    // --- Call translation ---

    static ArkTSExpression translateCall(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.isWide()
                ? ArkOpcodesCompat.normalizeWideOpcode(insn.getOpcode())
                : insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression callee = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        List<ArkTSExpression> args = new ArrayList<>();

        switch (opcode) {
            // CALLARG: acc holds the function, args from registers
            case ArkOpcodesCompat.CALLARG0:
                break;
            case ArkOpcodesCompat.CALLARG1:
                args.add(resolveArgExpression(
                        (int) operands.get(1).getValue(), ctx));
                break;
            case ArkOpcodesCompat.CALLARGS2:
                args.add(resolveArgExpression(
                        (int) operands.get(1).getValue(), ctx));
                args.add(resolveArgExpression(
                        (int) operands.get(2).getValue(), ctx));
                break;
            case ArkOpcodesCompat.CALLARGS3:
                args.add(resolveArgExpression(
                        (int) operands.get(1).getValue(), ctx));
                args.add(resolveArgExpression(
                        (int) operands.get(2).getValue(), ctx));
                args.add(resolveArgExpression(
                        (int) operands.get(3).getValue(), ctx));
                break;
            // CALLTHIS: acc holds the method, first register is this
            case ArkOpcodesCompat.CALLTHIS0: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj, callee, false);
                break;
            }
            case ArkOpcodesCompat.CALLTHIS1: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj, callee, false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(2).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHIS2: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj, callee, false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(2).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(3).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHIS3: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj, callee, false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(2).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(3).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(4).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHISRANGE: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj, callee, false);
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(firstReg + a)));
                    }
                }
                break;
            }
            case ArkOpcodesCompat.CALLRANGE:
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(firstReg + a)));
                    }
                }
                break;
            default:
                break;
        }

        return new ArkTSExpression.CallExpression(callee, args);
    }

    // --- Property load translation ---

    static ArkTSExpression translatePropertyLoad(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.isWide()
                ? ArkOpcodesCompat.normalizeWideOpcode(insn.getOpcode())
                : insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.LDSUPERBYNAME
                || opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            obj = new ArkTSPropertyExpressions.SuperExpression();
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
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(reg));
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
        int opcode = insn.isWide()
                ? ArkOpcodesCompat.normalizeWideOpcode(insn.getOpcode())
                : insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.STSUPERBYNAME
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            obj = new ArkTSPropertyExpressions.SuperExpression();
        } else {
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            obj = new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(objReg));
        }

        ArkTSExpression prop;
        if (opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            int keyReg = (int) operands.get(
                    operands.size() - 2).getValue();
            prop = new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(keyReg));
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

    // --- Private property access translation ---

    /**
     * Translates a ldprivateproperty instruction into a private member
     * read expression: obj.#field.
     *
     * <p>The object comes from the accumulator value (or defaults to
     * {@code this} when no accumulator value is available). The field
     * name is resolved from the string table.
     *
     * @param insn the instruction
     * @param accValue the current accumulator value
     * @param ctx the decompilation context
     * @return the resulting private member expression
     */
    static ArkTSExpression translatePrivatePropertyLoad(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int stringIdx = (int) operands.get(1).getValue();
        String propName = ctx.resolveString(stringIdx);
        ArkTSExpression obj = accValue != null
                ? accValue
                : new ArkTSExpression.ThisExpression();
        return new ArkTSPropertyExpressions.PrivateMemberExpression(
                obj, propName);
    }

    /**
     * Translates a stprivateproperty instruction into a private member
     * write: obj.#field = value.
     *
     * <p>The value comes from the accumulator. The object register is the
     * last operand. The field name is resolved from the string table.
     *
     * @param insn the instruction
     * @param accValue the current accumulator value
     * @param ctx the decompilation context
     * @return a statement result with the assignment expression
     */
    static InstructionHandler.StatementResult translatePrivatePropertyStore(
            ArkInstruction insn, ArkTSExpression accValue,
            DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int stringIdx = (int) operands.get(1).getValue();
        String propName = ctx.resolveString(stringIdx);
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression target =
                new ArkTSPropertyExpressions.PrivateMemberExpression(
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(objReg)),
                        propName);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new InstructionHandler.StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(target, value)),
                value);
    }

    /**
     * Translates a callruntime.createprivateproperty instruction into
     * a private field declaration expression.
     *
     * <p>In ArkTS, private fields are declared with the {@code #field}
     * syntax in the class body. This method produces a
     * {@link ArkTSPropertyExpressions.PrivateFieldDeclarationExpression}
     * that renders as the field name with the hash prefix.
     *
     * @param operands the instruction operands
     * @param ctx the decompilation context
     * @return a statement result containing the declaration expression
     */
    static InstructionHandler.StatementResult translateCreatePrivateProperty(
            List<ArkOperand> operands, DecompilationContext ctx) {
        int stringIdx = (int) operands.get(0).getValue();
        String propName = ctx.resolveString(stringIdx);
        return new InstructionHandler.StatementResult(null,
                new ArkTSPropertyExpressions
                        .PrivateFieldDeclarationExpression(propName));
    }

    /**
     * Translates a callruntime.defineprivateproperty instruction into
     * a private property assignment: obj.#field = value.
     *
     * <p>Used in constructors to initialize private field values after
     * the field has been declared with createprivateproperty.
     *
     * @param operands the instruction operands
     * @param accValue the current accumulator value
     * @param ctx the decompilation context
     * @return a statement result with the assignment expression
     */
    static InstructionHandler.StatementResult translateDefinePrivateProperty(
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int stringIdx = (int) operands.get(0).getValue();
        String propName = ctx.resolveString(stringIdx);
        int objReg = (int) operands.get(1).getValue();
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(objReg));
        ArkTSExpression target =
                new ArkTSPropertyExpressions.PrivateMemberExpression(
                        obj, propName);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new InstructionHandler.StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(target, value)),
                value);
    }

    /**
     * Translates a testin instruction into an in-expression: prop in obj.
     *
     * <p>The property name is resolved from the string table. The object
     * comes from the accumulator (or a placeholder variable).
     *
     * @param insn the instruction
     * @param accValue the current accumulator value
     * @param ctx the decompilation context
     * @return the resulting in-expression
     */
    static ArkTSExpression translateTestIn(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int stringIdx = (int) operands.get(1).getValue();
        String propName = ctx.resolveString(stringIdx);
        ArkTSExpression obj = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new ArkTSPropertyExpressions.InExpression(
                new ArkTSExpression.VariableExpression(propName),
                obj);
    }

    /**
     * Resolves a register to its expression. If the register holds a
     * function-like expression (arrow, anonymous, definefunc),
     * inline it as a callback argument.
     */
    private static ArkTSExpression resolveArgExpression(
            int reg, DecompilationContext ctx) {
        ArkTSExpression stored = ctx.getRegisterExpression(reg);
        if (stored != null && isInlineableFunction(stored)) {
            return stored;
        }
        return new ArkTSExpression.VariableExpression(
                ctx.resolveRegisterName(reg));
    }

    private static boolean isInlineableFunction(ArkTSExpression expr) {
        return expr instanceof ArkTSAccessExpressions.ArrowFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .AnonymousFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .GeneratorFunctionExpression
                || expr instanceof ArkTSAccessExpressions.IifeExpression
                || InstructionHandler.isDefineFuncExpression(expr);
    }
}
