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

        // CALLTHIS: when acc holds a literal (last stored argument)
        // or is unresolved (acc variable), the method reference was
        // stored in a register. Try to recover.
        if (isCallThisOpcode(opcode)
                && (isLikelyLiteral(callee) || isUnresolvedAcc(callee))) {
            ArkTSExpression recovered =
                    recoverCalleeFromRegisters(ctx);
            if (recovered != null) {
                callee = recovered;
            }
        }

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
                callee = wrapCallThisCallee(callee, thisObj);
                break;
            }
            case ArkOpcodesCompat.CALLTHIS1: {
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(1).getValue()));
                callee = wrapCallThisCallee(callee, thisObj);
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
                callee = wrapCallThisCallee(callee, thisObj);
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
                callee = wrapCallThisCallee(callee, thisObj);
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
                callee = wrapCallThisCallee(callee, thisObj);
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

            // --- Vendor-specific named call opcodes (HarmonyOS API 12+) ---
            // These embed the method name (ID16) in operands[1].
            // Operands layout: [0]=IC slot, [1]=string_id (method name),
            // [2]=this register, [3..]=arg registers
            case ArkOpcodesCompat.CALLTHIS0WITHNAME: {
                int stringIdx = (int) operands.get(1).getValue();
                String methodName = sanitizePropertyName(
                        ctx.resolveString(stringIdx));
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(2).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj,
                        new ArkTSExpression.VariableExpression(methodName),
                        false);
                break;
            }
            case ArkOpcodesCompat.CALLTHIS1WITHNAME: {
                int stringIdx = (int) operands.get(1).getValue();
                String methodName = sanitizePropertyName(
                        ctx.resolveString(stringIdx));
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(2).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj,
                        new ArkTSExpression.VariableExpression(methodName),
                        false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(3).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHIS2WITHNAME: {
                int stringIdx = (int) operands.get(1).getValue();
                String methodName = sanitizePropertyName(
                        ctx.resolveString(stringIdx));
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(2).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj,
                        new ArkTSExpression.VariableExpression(methodName),
                        false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(3).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(4).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHIS3WITHNAME: {
                int stringIdx = (int) operands.get(1).getValue();
                String methodName = sanitizePropertyName(
                        ctx.resolveString(stringIdx));
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(2).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj,
                        new ArkTSExpression.VariableExpression(methodName),
                        false);
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(3).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(4).getValue())));
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(
                                (int) operands.get(5).getValue())));
                break;
            }
            case ArkOpcodesCompat.CALLTHISRANGEWITHNAME: {
                // IMM8_IMM8_IMM16_V8: [0]=IC, [1]=numArgs, [2]=string_id, [3]=firstReg
                int stringIdx = (int) operands.get(2).getValue();
                String methodName = sanitizePropertyName(
                        ctx.resolveString(stringIdx));
                ArkTSExpression thisObj =
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(
                                        (int) operands.get(3).getValue()));
                callee = new ArkTSExpression.MemberExpression(
                        thisObj,
                        new ArkTSExpression.VariableExpression(methodName),
                        false);
                int numRangeArgs = (int) operands.get(1).getValue();
                int firstReg = (int) operands.get(3).getValue();
                // numRangeArgs includes 'this' as the first element;
                // actual method args start at firstReg+1
                int numMethodArgs = numRangeArgs - 1;
                for (int a = 0; a < numMethodArgs; a++) {
                    args.add(new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(firstReg + 1 + a)));
                }
                break;
            }
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

        // For LDOBJBYVALUE: acc=key, register=object
        // For LDOBJBYNAME/LDOBJBYINDEX: acc=object
        if (opcode == ArkOpcodesCompat.LDOBJBYVALUE) {
            // acc = key, register = object
            ArkTSExpression key = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(objReg));
            return new ArkTSExpression.MemberExpression(obj, key, true);
        }

        if (opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            int reg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression key =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(reg));
            return new ArkTSExpression.MemberExpression(
                    new ArkTSExpression.ThisExpression(), key, true);
        }

        if (opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            int reg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression key =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(reg));
            return new ArkTSExpression.MemberExpression(
                    new ArkTSPropertyExpressions.SuperExpression(),
                    key, true);
        }

        // For name/index-based loads: acc = object
        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.LDTHISBYNAME) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.LDSUPERBYNAME) {
            obj = new ArkTSPropertyExpressions.SuperExpression();
        } else {
            obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
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
        String propName = sanitizePropertyName(ctx.resolveString(
                (int) operands.get(1).getValue()));
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

        // STOBJBYVALUE (IMM8_V8_V8): acc=value, operands[1]=object, operands[2]=key
        if (opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUE) {
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int objReg = (int) operands.get(1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(objReg));
            int keyReg = (int) operands.get(2).getValue();
            ArkTSExpression key =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(keyReg));
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, key, true),
                    value);
        }

        // STTHISBYVALUE (IMM8_V8_V8): this is object, operands[1]=key, operands[2]=value
        if (opcode == ArkOpcodesCompat.STTHISBYVALUE) {
            int keyReg = (int) operands.get(1).getValue();
            ArkTSExpression key =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(keyReg));
            int valReg = (int) operands.get(2).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(valReg));
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.ThisExpression(), key, true),
                    value);
        }

        // STSUPERBYVALUE (IMM8_V8_V8): super is object, operands[1]=key, operands[2]=value
        if (opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            int keyReg = (int) operands.get(1).getValue();
            ArkTSExpression key =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(keyReg));
            int valReg = (int) operands.get(2).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(valReg));
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(
                            new ArkTSPropertyExpressions.SuperExpression(),
                            key, true),
                    value);
        }

        // STOBJBYINDEX (IMM8_IMM16_V8): acc=object, operands[1]=index, operands[2]=value
        // STOWNBYINDEX (IMM8_IMM16_V8): same
        if (opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STOWNBYINDEX) {
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int index = (int) operands.get(1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.LiteralExpression(
                            String.valueOf(index),
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            int valReg = (int) operands.get(2).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(valReg));
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, prop, true),
                    value);
        }

        // Name-based stores: acc=object, operands[1]=string index, operands[2]=value register
        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.STTHISBYNAME) {
            obj = new ArkTSExpression.ThisExpression();
        } else if (opcode == ArkOpcodesCompat.STSUPERBYNAME) {
            obj = new ArkTSPropertyExpressions.SuperExpression();
        } else {
            obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }

        // Name-based

        // Name-based stores (IMM8_IMM16_V8): acc=value, operands[1]=name, operands[2]=object reg
        // For STOBJBYNAME: object = register at operands[2], value = accValue
        // For STTHISBYNAME/STSUPERBYNAME: object = this/super, value = accValue
        if (opcode != ArkOpcodesCompat.STTHISBYNAME
                && opcode != ArkOpcodesCompat.STSUPERBYNAME) {
            // STOBJBYNAME: object comes from register at operands[2]
            int objReg = (int) operands.get(2).getValue();
            obj = new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(objReg));
        }

        String propName = sanitizePropertyName(ctx.resolveString(
                (int) operands.get(1).getValue()));
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(propName);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new ArkTSExpression.AssignExpression(
                new ArkTSExpression.MemberExpression(obj, prop, false),
                value);
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
        String propName = sanitizePropertyName(ctx.resolveString(stringIdx));
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
        String propName = sanitizePropertyName(ctx.resolveString(stringIdx));
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
        String propName = sanitizePropertyName(ctx.resolveString(stringIdx));
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
        String propName = sanitizePropertyName(ctx.resolveString(stringIdx));
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
        String propName = sanitizePropertyName(ctx.resolveString(stringIdx));
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

    /**
     * Sanitizes a property name for use in decompiled output.
     * Replaces control characters with readable placeholders
     * and provides fallback for empty names.
     */
    static String sanitizePropertyName(String name) {
        if (name == null || name.isEmpty()) {
            return "_anonymous";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                sb.append(String.format("_%02x", (int) c));
            } else {
                sb.append(c);
            }
        }
        String result = sb.toString();
        // If the name is not a valid JS identifier, wrap it
        if (!result.isEmpty()
                && !Character.isJavaIdentifierStart(result.charAt(0))) {
            return "prop_" + result;
        }
        return result;
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

    private static boolean isCallThisOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3
                || opcode == ArkOpcodesCompat.CALLTHISRANGE;
    }

    private static boolean isLikelyLiteral(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return true;
        }
        if (expr instanceof ArkTSExpression.VariableExpression) {
            String name =
                    ((ArkTSExpression.VariableExpression) expr).getName();
            return "undefined".equals(name)
                    || "null".equals(name)
                    || "true".equals(name)
                    || "false".equals(name);
        }
        return false;
    }

    private static boolean isUnresolvedAcc(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return ACC.equals(
                    ((ArkTSExpression.VariableExpression) expr).getName());
        }
        return false;
    }

    private static ArkTSExpression wrapCallThisCallee(
            ArkTSExpression callee, ArkTSExpression thisObj) {
        if (callee instanceof ArkTSExpression.MemberExpression) {
            return callee;
        }
        return new ArkTSExpression.MemberExpression(thisObj, callee, false);
    }

    /**
     * Scans tracked register expressions for a callee-like expression
     * (MemberExpression or function ref) when the accumulator holds a
     * literal (last stored argument).
     */
    private static ArkTSExpression recoverCalleeFromRegisters(
            DecompilationContext ctx) {
        int maxReg = ctx.getMaxTrackedRegister();
        for (int r = maxReg; r >= 0; r--) {
            ArkTSExpression stored = ctx.getRegisterExpression(r);
            if (stored instanceof ArkTSExpression.MemberExpression) {
                return stored;
            }
        }
        return null;
    }
}
