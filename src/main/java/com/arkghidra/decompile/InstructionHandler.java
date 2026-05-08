package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Handles instruction-level processing during ArkTS decompilation.
 *
 * <p>Translates individual Ark bytecode instructions into ArkTS expressions
 * and statements, managing accumulator state, variable declarations, and
 * type inference. Delegates to specialized handler classes for property
 * access, operator classification, object creation, and load/store
 * concerns.
 */
class InstructionHandler {

    private static final String ACC = "acc";

    private final ArkTSDecompiler decompiler;
    private final ObjectCreationHandler objectCreationHandler;

    InstructionHandler(ArkTSDecompiler decompiler) {
        this.decompiler = decompiler;
        this.objectCreationHandler =
                new ObjectCreationHandler(decompiler);
    }

    /**
     * Result of processing a single instruction.
     */
    static class StatementResult {
        ArkTSStatement statement;
        ArkTSExpression newAccValue;

        StatementResult(ArkTSStatement statement,
                ArkTSExpression newAccValue) {
            this.statement = statement;
            this.newAccValue = newAccValue;
        }
    }

    StatementResult processInstruction(ArkInstruction insn,
            DecompilationContext ctx, ArkTSExpression accValue,
            Set<String> declaredVars, TypeInference typeInf) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        // --- Loads that set the accumulator ---
        switch (opcode) {
            case ArkOpcodesCompat.LDAI: {
                ArkTSExpression expr =
                        new ArkTSExpression.LiteralExpression(
                                String.valueOf(operands.get(0).getValue()),
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.FLDAI: {
                double val = Double.longBitsToDouble(
                        operands.get(0).getValue());
                ArkTSExpression expr =
                        new ArkTSExpression.LiteralExpression(
                                String.valueOf(val),
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA_STR: {
                String str = ctx.resolveString(
                        (int) operands.get(0).getValue());
                ArkTSExpression expr =
                        new ArkTSExpression.LiteralExpression(str,
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.STRING);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA: {
                int reg = (int) operands.get(0).getValue();
                ArkTSExpression expr =
                        new ArkTSExpression.VariableExpression("v" + reg);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDUNDEFINED:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("undefined",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.UNDEFINED));
            case ArkOpcodesCompat.LDNULL:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("null",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NULL));
            case ArkOpcodesCompat.LDTRUE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("true",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDFALSE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("false",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDNAN:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("NaN",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NAN));
            case ArkOpcodesCompat.LDINFINITY:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("Infinity",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.INFINITY));
            case ArkOpcodesCompat.LDTHIS:
                return new StatementResult(null,
                        new ArkTSExpression.ThisExpression());
            case ArkOpcodesCompat.LDGLOBAL:
                return new StatementResult(null,
                        new ArkTSExpression.VariableExpression("globalThis"));
            case ArkOpcodesCompat.CREATEEMPTYOBJECT:
                return new StatementResult(null,
                        new ArkTSAccessExpressions.ObjectLiteralExpression(
                                Collections.emptyList()));
            case ArkOpcodesCompat.CREATEEMPTYARRAY:
                return new StatementResult(null,
                        new ArkTSAccessExpressions.ArrayLiteralExpression(
                                Collections.emptyList()));
            default:
                break;
        }

        // --- Async function enter ---
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONENTER) {
            ctx.isAsync = true;
            return null;
        }

        // --- Generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEGENERATOROBJ) {
            return handleCreateGeneratorObj(operands, accValue,
                    declaredVars);
        }
        if (opcode == ArkOpcodesCompat.SUSPENDGENERATOR) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSAccessExpressions.YieldExpression(
                                    value, false)),
                    null);
        }
        if (opcode == ArkOpcodesCompat.RESUMEGENERATOR) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("generator"));
        }
        if (opcode == ArkOpcodesCompat.GETRESUMEMODE) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("resumeMode"));
        }

        // --- Async operations ---
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONAWAITUNCAUGHT) {
            int reg = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSAccessExpressions.AwaitExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + reg)));
        }
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONRESOLVE) {
            int reg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(
                            new ArkTSExpression.VariableExpression(
                                    "v" + reg)),
                    null);
        }
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONREJECT) {
            int reg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(
                            new ArkTSExpression.VariableExpression(
                                    "v" + reg)),
                    null);
        }

        // --- Async generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEASYNCGENERATOROBJ) {
            return handleCreateAsyncGeneratorObj(operands, declaredVars);
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORRESOLVE) {
            int valueReg = (int) operands.get(1).getValue();
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(
                            new ArkTSExpression.VariableExpression(
                                    "v" + valueReg)),
                    null);
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORREJECT) {
            int valueReg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(
                            new ArkTSExpression.VariableExpression(
                                    "v" + valueReg)),
                    null);
        }
        if (opcode == ArkOpcodesCompat.SETGENERATORSTATE) {
            int state = (int) operands.get(0).getValue();
            ArkTSExpression stateExpr =
                    new ArkTSExpression.LiteralExpression(
                            String.valueOf(state),
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSPropertyExpressions
                                    .GeneratorStateExpression(stateExpr)),
                    null);
        }

        // --- Private property access ---
        if (opcode == ArkOpcodesCompat.LDPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.ThisExpression();
            return new StatementResult(null,
                    new ArkTSPropertyExpressions.PrivateMemberExpression(
                            obj, propName));
        }
        if (opcode == ArkOpcodesCompat.STPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression target =
                    new ArkTSPropertyExpressions.PrivateMemberExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + objReg),
                            propName);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    target, value)),
                    value);
        }

        // --- TestIn (prop in obj) ---
        if (opcode == ArkOpcodesCompat.TESTIN) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSPropertyExpressions.InExpression(
                            new ArkTSExpression.VariableExpression(propName),
                            obj));
        }

        // --- Object creation with excluded keys ---
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHEXCLUDEDKEYS) {
            int srcReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression spreadExpr =
                    new ArkTSAccessExpressions.SpreadExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + srcReg));
            List<ArkTSExpression> props = new ArrayList<>();
            props.add(spreadExpr);
            return new StatementResult(null,
                    new ArkTSAccessExpressions.SpreadObjectExpression(
                            props));
        }

        // --- Define getter/setter by value ---
        if (opcode == ArkOpcodesCompat.DEFINEGETTERSETTERBYVALUE) {
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int keyReg = (int) operands.get(
                    operands.size() - 2).getValue();
            int getterReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + keyReg);
            ArkTSExpression getter =
                    new ArkTSExpression.VariableExpression("v" + getterReg);
            ArkTSExpression getterPair =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.LiteralExpression("get",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING),
                            getter, false);
            ArkTSExpression setterPair = null;
            if (operands.size() >= 4) {
                int setterReg = (int) operands.get(
                        operands.size() - 3).getValue();
                if (setterReg != getterReg) {
                    setterPair =
                            new ArkTSExpression.MemberExpression(
                                    new ArkTSExpression.LiteralExpression(
                                            "set",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.STRING),
                                    new ArkTSExpression.VariableExpression(
                                            "v" + setterReg),
                                    false);
                }
            }
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(obj, prop, true);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    target, getterPair)),
                    accValue);
        }

        // --- Delete object property ---
        if (opcode == ArkOpcodesCompat.DELOBJPROP) {
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSPropertyExpressions.DeleteExpression(obj));
        }

        // --- Copy data properties ---
        if (opcode == ArkOpcodesCompat.COPYDATAPROPERTIES) {
            int srcReg = (int) operands.get(0).getValue();
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression("v" + srcReg);
            ArkTSExpression target = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression expr =
                    new ArkTSPropertyExpressions
                            .CopyDataPropertiesExpression(target, source);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    target, expr)),
                    expr);
        }

        // --- IsTrue / IsFalse ---
        if (opcode == ArkOpcodesCompat.ISTRUE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Boolean"),
                            List.of(operand)));
        }
        if (opcode == ArkOpcodesCompat.ISFALSE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.UnaryExpression("!", operand, true));
        }

        // --- Throw ---
        if (opcode == ArkOpcodesCompat.THROW) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(val), null);
        }

        // --- Store from accumulator ---
        if (opcode == ArkOpcodesCompat.STA) {
            return handleSta(insn, operands, accValue, ctx,
                    declaredVars, typeInf);
        }

        // --- MOV ---
        if (opcode == ArkOpcodesCompat.MOV) {
            return handleMov(operands, ctx, declaredVars, typeInf);
        }

        // --- Binary operations: acc = acc OP v[operand] ---
        if (OperatorHandler.isBinaryOp(opcode)) {
            String op = OperatorHandler.getBinaryOperator(opcode);
            int reg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression left = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression right =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(null,
                    new ArkTSExpression.BinaryExpression(left, op, right));
        }

        // --- Unary operations: acc = OP acc ---
        if (OperatorHandler.isUnaryOp(opcode)) {
            String op = OperatorHandler.getUnaryOperator(opcode);
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.UnaryExpression(op, operand, true));
        }

        // --- Inc/Dec: acc = acc +/- 1 ---
        if (opcode == ArkOpcodesCompat.INC
                || opcode == ArkOpcodesCompat.DEC) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression one = new ArkTSExpression.LiteralExpression("1",
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            String op = opcode == ArkOpcodesCompat.INC ? "+" : "-";
            return new StatementResult(null,
                    new ArkTSExpression.BinaryExpression(operand, op, one));
        }

        // --- Function calls ---
        if (OperatorHandler.isCallOpcode(opcode)) {
            return new StatementResult(null,
                    PropertyAccessHandler.translateCall(insn, accValue, ctx));
        }

        // --- Property access (load) ---
        if (OperatorHandler.isPropertyLoadOpcode(opcode)) {
            return new StatementResult(null,
                    PropertyAccessHandler.translatePropertyLoad(
                            insn, accValue, ctx));
        }

        // --- Property store ---
        if (OperatorHandler.isPropertyStoreOpcode(opcode)) {
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            PropertyAccessHandler.translatePropertyStore(
                                    insn, accValue, ctx)),
                    accValue);
        }

        // --- Lexical variable access ---
        if (opcode == ArkOpcodesCompat.LDLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "lex_" + level + "_" + slot));
        }
        if (opcode == ArkOpcodesCompat.STLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "lex_" + level + "_" + slot),
                                        accValue)),
                        accValue);
            }
            return null;
        }

        // --- New object range ---
        if (opcode == ArkOpcodesCompat.NEWOBJRANGE) {
            return handleNewObjRange(operands, accValue);
        }

        // --- Define function ---
        if (opcode == ArkOpcodesCompat.DEFINEFUNC) {
            int methodIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "func_" + methodIdx));
        }

        // --- Define method ---
        if (opcode == ArkOpcodesCompat.DEFINEMETHOD) {
            int methodIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "method_" + methodIdx));
        }

        // --- Define class with buffer ---
        if (opcode == ArkOpcodesCompat.DEFINECLASSWITHBUFFER) {
            return objectCreationHandler
                    .processDefineClassWithBuffer(insn, ctx);
        }

        // --- Define field/property by name ---
        if (opcode == ArkOpcodesCompat.DEFINEFIELDBYNAME
                || opcode == ArkOpcodesCompat.DEFINEPROPERTYBYNAME) {
            return handleDefineByName(opcode, operands, accValue, ctx);
        }

        // --- Delegate remaining instruction categories ---
        StatementResult result = LoadStoreHandler.handleRemainingOpcodes(
                opcode, insn, operands, accValue, ctx,
                declaredVars, typeInf);
        if (result != null) {
            return result;
        }

        // --- Fallback: emit a descriptive comment for unhandled opcode ---
        String mnemonic = insn.getMnemonic();
        StringBuilder fb = new StringBuilder();
        fb.append("/* ").append(mnemonic).append("(");
        List<ArkOperand> fbOperands = insn.getOperands();
        for (int i = 0; i < fbOperands.size(); i++) {
            if (i > 0) {
                fb.append(", ");
            }
            fb.append(fbOperands.get(i).getValue());
        }
        fb.append(") */");
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                fb.toString())),
                null);
    }

    // --- Private instruction handlers ---

    private StatementResult handleCreateGeneratorObj(
            List<ArkOperand> operands, ArkTSExpression accValue,
            Set<String> declaredVars) {
        int reg = (int) operands.get(0).getValue();
        String varName = "v" + reg;
        ArkTSExpression expr =
                new ArkTSExpression.VariableExpression("generator");
        if (accValue != null) {
            expr = accValue;
        }
        if (!declaredVars.contains(varName)) {
            declaredVars.add(varName);
            return new StatementResult(
                    new ArkTSStatement.VariableDeclaration(
                            "let", varName, null, expr),
                    expr);
        }
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        varName),
                                expr)),
                expr);
    }

    private StatementResult handleCreateAsyncGeneratorObj(
            List<ArkOperand> operands, Set<String> declaredVars) {
        int reg = (int) operands.get(0).getValue();
        String varName = "v" + reg;
        ArkTSExpression expr =
                new ArkTSExpression.VariableExpression("asyncGenerator");
        if (!declaredVars.contains(varName)) {
            declaredVars.add(varName);
            return new StatementResult(
                    new ArkTSStatement.VariableDeclaration(
                            "let", varName, null, expr),
                    expr);
        }
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        varName),
                                expr)),
                expr);
    }

    private StatementResult handleSta(ArkInstruction insn,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx, Set<String> declaredVars,
            TypeInference typeInf) {
        int reg = (int) operands.get(0).getValue();
        String varName = "v" + reg;
        if (accValue != null) {
            accValue = tryReconstructTemplateLiteral(accValue);
            String accType =
                    OperatorHandler.getAccType(accValue, typeInf);
            typeInf.setRegisterType(varName, accType);
            String typeAnnotation =
                    TypeInference.formatTypeAnnotationForDeclaration(
                            accType, accValue);

            if (!declaredVars.contains(varName)
                    && !(reg < ctx.numArgs)) {
                declaredVars.add(varName);
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, typeAnnotation, accValue),
                        accValue);
            }
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.VariableExpression(
                                            varName),
                                    accValue)),
                    accValue);
        }
        return null;
    }

    private StatementResult handleMov(List<ArkOperand> operands,
            DecompilationContext ctx, Set<String> declaredVars,
            TypeInference typeInf) {
        int dstReg = (int) operands.get(0).getValue();
        int srcReg = (int) operands.get(1).getValue();
        String dstName = "v" + dstReg;
        String srcName = "v" + srcReg;
        ArkTSExpression srcExpr =
                new ArkTSExpression.VariableExpression(srcName);
        String srcType = typeInf.getRegisterType(srcName);
        String typeAnnotation = TypeInference.formatTypeAnnotation(
                dstName, srcType);
        typeInf.setRegisterType(dstName, srcType);

        if (!declaredVars.contains(dstName)
                && !(dstReg < ctx.numArgs)) {
            declaredVars.add(dstName);
            return new StatementResult(
                    new ArkTSStatement.VariableDeclaration(
                            "let", dstName, typeAnnotation, srcExpr),
                    null);
        }
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        dstName),
                                srcExpr)),
                null);
    }

    private static StatementResult handleNewObjRange(
            List<ArkOperand> operands, ArkTSExpression accValue) {
        int numArgs = (int) operands.get(0).getValue();
        List<ArkTSExpression> args = new ArrayList<>();
        int firstReg = (int) operands.get(
                operands.size() - 1).getValue();
        for (int a = 0; a < numArgs; a++) {
            args.add(new ArkTSExpression.VariableExpression(
                    "v" + (firstReg + a)));
        }
        ArkTSExpression callee = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new StatementResult(null,
                new ArkTSExpression.NewExpression(callee, args));
    }

    private static StatementResult handleDefineByName(int opcode,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int stringIdx = (int) operands.get(1).getValue();
        String fieldName = ctx.resolveString(stringIdx);
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(
                        new ArkTSExpression.VariableExpression("v" + objReg),
                        new ArkTSExpression.VariableExpression(fieldName),
                        false);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(target, value)),
                value);
    }

    // --- Delegates for external callers ---

    ArkTSExpression tryReconstructTemplateLiteral(
            ArkTSExpression accValue) {
        return objectCreationHandler
                .tryReconstructTemplateLiteral(accValue);
    }

    ArkTSExpression tryDetectNullishCoalescing(
            ArkTSExpression condition,
            ArkTSExpression trueValue,
            ArkTSExpression falseValue) {
        return objectCreationHandler.tryDetectNullishCoalescing(
                condition, trueValue, falseValue);
    }

    /**
     * Attempts to detect an array destructuring pattern starting at
     * the given instruction index within a basic block.
     */
    ObjectCreationHandler.DestructuringResult
            tryDetectArrayDestructuringInBlock(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars) {
        return objectCreationHandler.tryDetectArrayDestructuringInBlock(
                instructions, startIndex, ctx, declaredVars);
    }

    /**
     * Attempts to detect an object destructuring pattern starting at
     * the given instruction index within a basic block.
     */
    ObjectCreationHandler.DestructuringResult
            tryDetectObjectDestructuringInBlock(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars) {
        return objectCreationHandler.tryDetectObjectDestructuringInBlock(
                instructions, startIndex, ctx, declaredVars);
    }
}
