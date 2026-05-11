package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOpcodes;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcMethod;

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
        static final StatementResult NO_OP = new StatementResult(null, null);

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
        int rawOpcode = insn.getOpcode();
        // Normalize wide (0xFD-prefixed) sub-opcodes and 16-bit variant
        // primary opcodes to their canonical equivalents so all downstream
        // dispatch logic works unchanged.
        int opcode = insn.isWide()
                ? ArkOpcodesCompat.normalizeWideOpcode(rawOpcode)
                : ArkOpcodesCompat.normalizeVariantOpcode(rawOpcode);
        List<ArkOperand> operands = insn.getOperands();

        // --- CallRuntime (0xFB-prefixed) instructions ---
        if (insn.isCallRuntime()) {
            return handleCallRuntime(rawOpcode, operands, accValue, ctx);
        }

        // --- Throw (0xFE-prefixed) instructions ---
        // Only sub-opcode 0x00 (throw) is a real throw statement.
        // Sub-opcodes 0x01-0x09 are runtime assertion checks
        // (throw.notexists, throw.undefinedifhole, etc.) inserted
        // by the compiler — they should not appear in decompiled output.
        if (insn.isThrow()) {
            if (rawOpcode == ArkOpcodes.THROW) {
                ArkTSExpression val = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                return new StatementResult(
                        new ArkTSStatement.ThrowStatement(val), null);
            }
            return StatementResult.NO_OP;
        }

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
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(reg));
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
            return StatementResult.NO_OP;
        }

        // --- Generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEGENERATOROBJ) {
            return handleCreateGeneratorObj(operands, accValue,
                    declaredVars, ctx);
        }
        if (opcode == ArkOpcodesCompat.SUSPENDGENERATOR) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(reg));
            boolean isDelegate = isYieldDelegate(value);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSAccessExpressions.YieldExpression(
                                    value, isDelegate)),
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
                                    ctx.resolveRegisterName(reg))));
        }
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONRESOLVE) {
            int reg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(reg))),
                    null);
        }
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONREJECT) {
            int reg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(reg))),
                    null);
        }

        // --- Async generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEASYNCGENERATOROBJ) {
            return handleCreateAsyncGeneratorObj(operands, declaredVars,
                    ctx);
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORRESOLVE) {
            int valueReg = (int) operands.get(1).getValue();
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(valueReg))),
                    null);
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORREJECT) {
            int valueReg = (int) operands.get(0).getValue();
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(valueReg))),
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
            return new StatementResult(null,
                    PropertyAccessHandler.translatePrivatePropertyLoad(
                            insn, accValue, ctx));
        }
        if (opcode == ArkOpcodesCompat.STPRIVATEPROPERTY) {
            return PropertyAccessHandler.translatePrivatePropertyStore(
                    insn, accValue, ctx);
        }

        // --- TestIn (prop in obj) ---
        if (opcode == ArkOpcodesCompat.TESTIN) {
            return new StatementResult(null,
                    PropertyAccessHandler.translateTestIn(
                            insn, accValue, ctx));
        }

        // --- Object creation with excluded keys ---
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHEXCLUDEDKEYS) {
            int srcReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression spreadExpr =
                    new ArkTSAccessExpressions.SpreadExpression(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(srcReg)));
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
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(keyReg));
            ArkTSExpression getter =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(getterReg));
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
                                            ctx.resolveRegisterName(setterReg)),
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
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(srcReg));
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
        // isa.yaml pseudo: acc = ecma_op(acc, operand_0)
        // So acc is the LEFT operand, register is the RIGHT operand.
        if (OperatorHandler.isBinaryOp(opcode)) {
            String op = OperatorHandler.getBinaryOperator(opcode);
            int reg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression left = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression right =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(reg));
            ArkTSExpression result =
                    OperatorHandler.tryFoldConstants(left, op, right);
            // Try merging string literals for "+" (only if folding didn't
            // already produce a literal)
            if ("+".equals(op)
                    && result instanceof ArkTSExpression.BinaryExpression) {
                ArkTSExpression merged =
                        OperatorHandler.tryMergeStringLiterals(
                                left, op, right);
                if (merged != result) {
                    result = merged;
                }
            }
            // Simplify boolean comparisons: x === true -> x
            result = OperatorHandler.simplifyBooleanComparison(result);
            // Normalize comparisons: 0 < x -> x > 0
            result = OperatorHandler.normalizeComparison(result);
            // Simplify redundant typeof/undefined/null patterns
            result =
                    OperatorHandler.simplifyRedundantTypeofNull(result);
            // Simplify identity operations: x + 0 -> x, x * 1 -> x
            if (result instanceof ArkTSExpression.BinaryExpression) {
                ArkTSExpression.BinaryExpression bin =
                        (ArkTSExpression.BinaryExpression) result;
                result = OperatorHandler.trySimplifyIdentity(
                        bin.getLeft(), bin.getOperator(), bin.getRight());
            }
            // Attempt string concatenation -> template literal for +
            if ("+".equals(op) && accValue != null) {
                ArkTSExpression tmpl =
                        tryReconstructTemplateLiteral(result);
                if (tmpl != result) {
                    result = tmpl;
                }
            }
            return new StatementResult(null, result);
        }

        // --- Unary operations: acc = OP acc ---
        if (OperatorHandler.isUnaryOp(opcode)) {
            String op;
            if (opcode == ArkOpcodesCompat.NOT) {
                op = OperatorHandler.getNotOperator(accValue, typeInf);
            } else {
                op = OperatorHandler.getUnaryOperator(opcode);
            }
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression result =
                    new ArkTSExpression.UnaryExpression(op, operand, true);
            result = OperatorHandler.simplifyDoubleNegation(result);
            result = OperatorHandler.simplifyUnaryNegation(result);
            result = OperatorHandler.simplifyVoidZero(result);
            return new StatementResult(null, result);
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
            ArkTSExpression callExpr =
                    PropertyAccessHandler.translateCall(insn, accValue, ctx);
            // Method calls on objects (obj.method()) typically have side
            // effects and should be emitted as statements even when their
            // return value is unused (e.g., arr.push(val), map.set(k, v))
            if (callExpr instanceof ArkTSExpression.CallExpression) {
                ArkTSExpression callee =
                        ((ArkTSExpression.CallExpression) callExpr)
                                .getCallee();
                if (callee instanceof ArkTSExpression.MemberExpression) {
                    return new StatementResult(
                            new ArkTSStatement.ExpressionStatement(
                                    callExpr),
                            callExpr);
                }
            }
            return new StatementResult(null, callExpr);
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
                            ctx.resolveLexVarName(level, slot)));
        }
        if (opcode == ArkOpcodesCompat.STLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            if (accValue != null) {
                // Infer name from the stored value
                String inferredName = inferLexVarName(accValue, ctx);
                if (inferredName != null) {
                    ctx.setLexVarName(level, slot, inferredName);
                }
                String accType = OperatorHandler.getAccType(
                        accValue, typeInf);
                typeInf.setLexicalVarType(level, slot, accType);
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                ctx.resolveLexVarName(
                                                        level, slot)),
                                        accValue)),
                        accValue);
            }
            return null;
        }

        // --- New object range ---
        if (opcode == ArkOpcodesCompat.NEWOBJRANGE) {
            return handleNewObjRange(operands, accValue, ctx);
        }

        // --- Define function ---
        if (opcode == ArkOpcodesCompat.DEFINEFUNC) {
            int methodIdx = (int) operands.get(0).getValue();
            String funcName = resolveMethodName(ctx, methodIdx);
            if (funcName == null || funcName.isEmpty()) {
                funcName = "func_" + methodIdx;
            }
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(funcName));
        }

        // --- Define method ---
        if (opcode == ArkOpcodesCompat.DEFINEMETHOD) {
            int methodIdx = (int) operands.get(0).getValue();
            String methodName = resolveMethodName(ctx, methodIdx);
            if (methodName == null || methodName.isEmpty()) {
                methodName = "method_" + methodIdx;
            }
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(methodName));
        }

        // --- Define class with buffer ---
        if (opcode == ArkOpcodesCompat.DEFINECLASSWITHBUFFER) {
            return objectCreationHandler
                    .processDefineClassWithBuffer(insn, ctx);
        }

        // --- Define field by name ---
        if (opcode == ArkOpcodesCompat.DEFINEFIELDBYNAME) {
            return handleDefineByName(opcode, operands, accValue, ctx);
        }

        // --- Define property by name (Object.defineProperty semantics) ---
        if (opcode == ArkOpcodesCompat.DEFINEPROPERTYBYNAME) {
            return handleDefinePropertyByName(
                    operands, accValue, ctx);
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

    /**
     * Well-known built-in class names that map to special variable names
     * rather than simple decapitalization.
     */
    private static final java.util.Map<String, String>
            BUILTIN_CLASS_VAR_NAMES = java.util.Map.ofEntries(
                    java.util.Map.entry("Error", "error"),
                    java.util.Map.entry("TypeError", "typeError"),
                    java.util.Map.entry("RangeError", "rangeError"),
                    java.util.Map.entry("SyntaxError", "syntaxError"),
                    java.util.Map.entry("ReferenceError",
                            "referenceError"),
                    java.util.Map.entry("URIError", "uriError"),
                    java.util.Map.entry("EvalError", "evalError"),
                    java.util.Map.entry("Map", "map"),
                    java.util.Map.entry("Set", "set"),
                    java.util.Map.entry("WeakMap", "weakMap"),
                    java.util.Map.entry("WeakSet", "weakSet"),
                    java.util.Map.entry("Array", "arr"),
                    java.util.Map.entry("Promise", "promise"),
                    java.util.Map.entry("RegExp", "regex"),
                    java.util.Map.entry("Date", "date"));

    /**
     * Static method call patterns that produce specific variable names.
     * Keys are "ObjectName.methodName", values are the inferred name.
     */
    private static final java.util.Map<String, String>
            STATIC_CALL_RESULT_NAMES = java.util.Map.ofEntries(
                    java.util.Map.entry("JSON.parse", "parsed"),
                    java.util.Map.entry("JSON.stringify", "json"),
                    java.util.Map.entry("Object.keys", "keys"),
                    java.util.Map.entry("Object.values", "values"),
                    java.util.Map.entry("Object.entries", "entries"),
                    java.util.Map.entry("Object.assign", "assigned"),
                    java.util.Map.entry("Array.from", "arr"),
                    java.util.Map.entry("Array.of", "arr"),
                    java.util.Map.entry("Promise.all", "results"),
                    java.util.Map.entry("Promise.race", "result"),
                    java.util.Map.entry("Promise.resolve", "resolved"),
                    java.util.Map.entry("Promise.reject", "rejected"));

    /**
     * Infers a variable name from the expression being stored.
     * Patterns: method call result (getName → name), property access
     * (obj.length → length), constructor (new Foo → foo),
     * static calls (JSON.parse → parsed), BuiltInNew expressions.
     */
    private static String inferNameFromExpression(ArkTSExpression expr,
            int reg) {
        // Method call: obj.method() → method, foo.getName() → name
        if (expr instanceof ArkTSExpression.CallExpression) {
            ArkTSExpression callee =
                    ((ArkTSExpression.CallExpression) expr).getCallee();
            // Check for static call patterns: JSON.parse → parsed
            String staticName = tryStaticCallName(callee);
            if (staticName != null) {
                return staticName;
            }
            String name = extractNameFromCallee(callee);
            if (name != null) {
                return sanitizeName(name);
            }
        }
        // Property access: obj.prop → prop, obj.getProp() → prop
        if (expr instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression member =
                    (ArkTSExpression.MemberExpression) expr;
            if (!member.isComputed()) {
                ArkTSExpression prop = member.getProperty();
                if (prop instanceof ArkTSExpression.VariableExpression) {
                    return sanitizeName(
                            ((ArkTSExpression.VariableExpression) prop)
                                    .getName());
                }
            }
        }
        // BuiltInNew expression: new Map() → map, new Error() → error
        if (expr instanceof ArkTSAccessExpressions.BuiltInNewExpression) {
            String className =
                    ((ArkTSAccessExpressions.BuiltInNewExpression) expr)
                            .getClassName();
            return inferNameFromClassName(className);
        }
        // New expression: new ClassName() → className
        if (expr instanceof ArkTSExpression.NewExpression) {
            ArkTSExpression callee =
                    ((ArkTSExpression.NewExpression) expr).getCallee();
            if (callee instanceof ArkTSExpression.VariableExpression) {
                String className =
                        ((ArkTSExpression.VariableExpression) callee)
                                .getName();
                return inferNameFromClassName(className);
            }
            // Member expression callee: new ns.ClassName() → className
            if (callee instanceof ArkTSExpression.MemberExpression) {
                ArkTSExpression.MemberExpression member =
                        (ArkTSExpression.MemberExpression) callee;
                if (!member.isComputed()) {
                    ArkTSExpression prop = member.getProperty();
                    if (prop
                            instanceof ArkTSExpression.VariableExpression) {
                        String className =
                                ((ArkTSExpression.VariableExpression) prop)
                                        .getName();
                        return inferNameFromClassName(className);
                    }
                }
            }
        }
        // Binary comparison: use is/has prefix for boolean results
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            String op = bin.getOperator();
            if ("===".equals(op) || "==" .equals(op)
                    || "!==" .equals(op) || "!=" .equals(op)
                    || ">" .equals(op) || "<" .equals(op)
                    || ">=" .equals(op) || "<=" .equals(op)) {
                return inferBooleanName(bin.getRight());
            }
        }
        // Unary not: !expr → isNot + operand name
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            ArkTSExpression.UnaryExpression unary =
                    (ArkTSExpression.UnaryExpression) expr;
            if ("!".equals(unary.getOperator())) {
                String inner = inferBooleanName(unary.getOperand());
                if (inner != null) {
                    return "isNot" + Character.toUpperCase(inner.charAt(0))
                            + inner.substring(1);
                }
            }
        }
        // Typeof comparison: typeof x === "string" → isString
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            ArkTSExpression right = bin.getRight();
            if (right instanceof ArkTSExpression.LiteralExpression) {
                String val = ((ArkTSExpression.LiteralExpression) right)
                        .getValue();
                if ("string".equals(val) || "number".equals(val)
                        || "boolean".equals(val)
                        || "function".equals(val)
                        || "object".equals(val)) {
                    return "is" + Character.toUpperCase(val.charAt(0))
                            + val.substring(1);
                }
            }
        }
        // Switch expression: infer from discriminant
        if (expr instanceof ArkTSAccessExpressions.SwitchExpression) {
            return "switchResult";
        }
        return null;
    }

    /**
     * Infers a variable name from a class name used in a constructor call.
     * Uses a lookup table for well-known built-in types, falling back to
     * decapitalization for custom classes.
     *
     * @param className the class name (e.g. "Error", "Map", "MyClass")
     * @return the inferred variable name (e.g. "error", "map", "myClass")
     */
    private static String inferNameFromClassName(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        String mapped = BUILTIN_CLASS_VAR_NAMES.get(className);
        if (mapped != null) {
            return mapped;
        }
        return decapitalize(className);
    }

    /**
     * Tries to match a callee expression against known static call
     * patterns. For example, JSON.parse → "parsed",
     * Object.keys → "keys".
     *
     * @param callee the callee expression from a call
     * @return the inferred variable name, or null if no match
     */
    private static String tryStaticCallName(ArkTSExpression callee) {
        if (!(callee instanceof ArkTSExpression.MemberExpression)) {
            return null;
        }
        ArkTSExpression.MemberExpression member =
                (ArkTSExpression.MemberExpression) callee;
        if (member.isComputed()) {
            return null;
        }
        // Extract object name
        ArkTSExpression obj = member.getObject();
        if (!(obj instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        String objName =
                ((ArkTSExpression.VariableExpression) obj).getName();
        // Extract method name
        ArkTSExpression prop = member.getProperty();
        if (!(prop instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        String methodName =
                ((ArkTSExpression.VariableExpression) prop).getName();
        String key = objName + "." + methodName;
        return STATIC_CALL_RESULT_NAMES.get(key);
    }

    private static String inferBooleanName(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            String val = ((ArkTSExpression.LiteralExpression) expr)
                    .getValue();
            if ("null".equals(val)) {
                return "isNull";
            }
            if ("undefined".equals(val)) {
                return "isUndefined";
            }
            if ("NaN".equals(val)) {
                return "isNaN";
            }
            // Boolean literals
            if ("true".equals(val)) {
                return "isTrue";
            }
            if ("false".equals(val)) {
                return "isFalse";
            }
        }
        if (expr instanceof ArkTSExpression.VariableExpression) {
            String name = ((ArkTSExpression.VariableExpression) expr)
                    .getName();
            if (name != null && !name.startsWith("v")
                    && !name.equals("acc")) {
                return "has" + Character.toUpperCase(name.charAt(0))
                        + name.substring(1);
            }
        }
        // Member expression: obj.active → hasActive
        if (expr instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression member =
                    (ArkTSExpression.MemberExpression) expr;
            if (!member.isComputed()) {
                ArkTSExpression prop = member.getProperty();
                if (prop instanceof ArkTSExpression.VariableExpression) {
                    String propName =
                            ((ArkTSExpression.VariableExpression) prop)
                                    .getName();
                    return "has" + Character.toUpperCase(propName.charAt(0))
                            + propName.substring(1);
                }
            }
        }
        return null;
    }

    private static String extractNameFromCallee(ArkTSExpression callee) {
        if (callee instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression member =
                    (ArkTSExpression.MemberExpression) callee;
            if (!member.isComputed()) {
                ArkTSExpression prop = member.getProperty();
                if (prop instanceof ArkTSExpression.VariableExpression) {
                    return ((ArkTSExpression.VariableExpression) prop)
                            .getName();
                }
            }
        }
        if (callee instanceof ArkTSExpression.VariableExpression) {
            return ((ArkTSExpression.VariableExpression) callee).getName();
        }
        return null;
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        // Remove common verb prefixes: get/set/is/has/can/will/should
        if (name.length() > 3 && name.startsWith("get")
                && Character.isUpperCase(name.charAt(3))) {
            return decapitalize(name.substring(3));
        }
        if (name.length() > 3 && name.startsWith("set")
                && Character.isUpperCase(name.charAt(3))) {
            return decapitalize(name.substring(3));
        }
        if (name.length() > 2 && name.startsWith("is")
                && Character.isUpperCase(name.charAt(2))) {
            return decapitalize(name.substring(2));
        }
        if (name.length() > 3 && name.startsWith("has")
                && Character.isUpperCase(name.charAt(3))) {
            return decapitalize(name.substring(3));
        }
        if (name.length() > 3 && name.startsWith("can")
                && Character.isUpperCase(name.charAt(3))) {
            return decapitalize(name.substring(3));
        }
        if (name.length() > 4 && name.startsWith("will")
                && Character.isUpperCase(name.charAt(4))) {
            return decapitalize(name.substring(4));
        }
        if (name.length() > 6 && name.startsWith("should")
                && Character.isUpperCase(name.charAt(6))) {
            return decapitalize(name.substring(6));
        }
        // Ensure result starts with a lowercase letter
        if (Character.isUpperCase(name.charAt(0))) {
            return decapitalize(name);
        }
        return name;
    }

    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name.toLowerCase();
        }
        return Character.toLowerCase(name.charAt(0))
                + name.substring(1);
    }

    private StatementResult handleCreateGeneratorObj(
            List<ArkOperand> operands, ArkTSExpression accValue,
            Set<String> declaredVars, DecompilationContext ctx) {
        int reg = (int) operands.get(0).getValue();
        String varName = ctx.resolveRegisterName(reg);
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
            List<ArkOperand> operands, Set<String> declaredVars,
            DecompilationContext ctx) {
        int reg = (int) operands.get(0).getValue();
        String varName = ctx.resolveRegisterName(reg);
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

    /**
     * Heuristic: checks if a yielded value expression indicates yield*
     * delegation. A yield* occurs when the yielded value is the result
     * of getting an iterator from another iterable.
     *
     * @param value the yielded value expression
     * @return true if this looks like a yield* delegate
     */
    private static boolean isYieldDelegate(ArkTSExpression value) {
        if (value == null) {
            return false;
        }
        String name = null;
        if (value instanceof ArkTSExpression.VariableExpression) {
            name = ((ArkTSExpression.VariableExpression) value).getName();
        }
        if (name == null) {
            return false;
        }
        return "iterator".equals(name)
                || "propIterator".equals(name)
                || name.startsWith("iter_")
                || name.startsWith("delegate_");
    }

    private StatementResult handleSta(ArkInstruction insn,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx, Set<String> declaredVars,
            TypeInference typeInf) {
        int reg = (int) operands.get(0).getValue();
        String varName = ctx.resolveRegisterName(reg);
        if (accValue != null) {
            // Track expression stored to register for later inlining
            ctx.setRegisterExpression(reg, accValue);

            // Infer variable name from expression context when no debug name
            if (!ctx.hasRegisterName(reg)) {
                String inferred = inferNameFromExpression(accValue, reg);
                if (inferred != null) {
                    ctx.setInferredName(reg, inferred);
                    varName = inferred;
                }
            }

            // Check if storing a definefunc expression to a variable
            if (accValue instanceof DefineFuncExpression) {
                return handleDefineFuncStore(
                        (DefineFuncExpression) accValue, varName,
                        declaredVars, ctx);
            }

            accValue = tryReconstructTemplateLiteral(accValue);
            String accType =
                    OperatorHandler.getAccType(accValue, typeInf);
            typeInf.setRegisterType(varName, accType);

            // Check for compound assignment or increment/decrement
            ArkTSExpression compoundExpr =
                    tryCompoundAssignOrUpdate(accValue, varName);
            if (compoundExpr != null) {
                if (!declaredVars.contains(varName)
                        && !(reg < ctx.numArgs)) {
                    declaredVars.add(varName);
                }
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                compoundExpr),
                        accValue);
            }

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

    /**
     * Checks if the accumulator value represents a compound assignment
     * or increment/decrement pattern when stored back to the same
     * register that was the left operand.
     *
     * <p>Patterns detected:
     * <ul>
     *   <li>{@code v0 + 1} stored to v0 -> {@code v0++}</li>
     *   <li>{@code v0 - 1} stored to v0 -> {@code v0--}</li>
     *   <li>{@code v0 + v1} stored to v0 -> {@code v0 += v1}</li>
     *   <li>{@code v0 * v1} stored to v0 -> {@code v0 *= v1}</li>
     * </ul>
     *
     * @param accValue the accumulator expression
     * @param targetVarName the target variable name for the store
     * @return a CompoundAssignExpression, IncrementExpression, or null
     *         if no compound pattern is detected
     */
    private static ArkTSExpression tryCompoundAssignOrUpdate(
            ArkTSExpression accValue, String targetVarName) {
        if (!(accValue
                instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression binExpr =
                (ArkTSExpression.BinaryExpression) accValue;
        ArkTSExpression left = binExpr.getLeft();
        String op = binExpr.getOperator();

        // Left operand must be a variable matching the target
        if (!(left instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        String leftName =
                ((ArkTSExpression.VariableExpression) left).getName();
        if (!leftName.equals(targetVarName)) {
            return null;
        }

        // Only arithmetic and bitwise operators have compound forms
        if (!isCompoundOperator(op)) {
            return null;
        }

        // Check for increment/decrement: v0 + 1 -> v0++, v0 - 1 -> v0--
        if (isLiteralOne(binExpr.getRight())) {
            boolean isIncrement = "+".equals(op);
            return new ArkTSExpression.IncrementExpression(
                    new ArkTSExpression.VariableExpression(targetVarName),
                    false, isIncrement);
        }

        // General compound assignment: v0 += v1
        return new ArkTSExpression.CompoundAssignExpression(
                new ArkTSExpression.VariableExpression(targetVarName),
                op + "=",
                binExpr.getRight());
    }

    /**
     * Returns true if the operator supports a compound assignment form.
     *
     * @param op the operator string
     * @return true if compound form exists (e.g. "+" -> "+=")
     */
    private static boolean isCompoundOperator(String op) {
        return "+".equals(op) || "-".equals(op)
                || "*".equals(op) || "/".equals(op)
                || "%".equals(op) || "**".equals(op)
                || "<<".equals(op) || ">>".equals(op)
                || ">>>".equals(op) || "&".equals(op)
                || "|".equals(op) || "^".equals(op);
    }

    /**
     * Returns true if the expression is a literal numeric 1.
     *
     * @param expr the expression to check
     * @return true if it is the literal value 1
     */
    private static boolean isLiteralOne(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.NUMBER) {
            return false;
        }
        return "1".equals(lit.getValue());
    }

    /**
     * Handles storing a definefunc result to a variable.
     * Converts the definefunc into an arrow function or anonymous
     * function expression depending on context.
     */
    private StatementResult handleDefineFuncStore(
            DefineFuncExpression defineFunc, String varName,
            Set<String> declaredVars, DecompilationContext ctx) {
        int methodIdx = defineFunc.getMethodIdx();

        // Build placeholder parameters
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        if (ctx != null && ctx.code != null) {
            int numArgs = ctx.numArgs;
            for (int i = 0; i < numArgs; i++) {
                params.add(
                        new ArkTSDeclarations.FunctionDeclaration
                                .FunctionParam("param_" + i, null));
            }
        }

        // Try to resolve method name from abcFile
        String methodName = resolveMethodName(ctx, methodIdx);
        boolean isAsync = ctx != null && ctx.isAsync;

        // Check for generator by looking at the method's code
        boolean isGenerator = detectGeneratorMethod(ctx, methodIdx);

        // Build the body from nested code if available
        List<ArkTSStatement> bodyStmts = resolveDefineFuncBody(
                ctx, methodIdx);

        ArkTSExpression funcExpr;
        if (isGenerator) {
            funcExpr = new ArkTSAccessExpressions
                    .GeneratorFunctionExpression(
                            methodName, params,
                            new ArkTSStatement.BlockStatement(bodyStmts),
                            isAsync);
        } else if (bodyStmts.size() == 1
                && bodyStmts.get(0)
                        instanceof ArkTSStatement.ReturnStatement) {
            // Single return statement -> arrow function with expression
            ArkTSExpression returnVal =
                    ((ArkTSStatement.ReturnStatement) bodyStmts.get(0))
                            .getValue();
            funcExpr = new ArkTSAccessExpressions
                    .ArrowFunctionExpression(
                            params,
                            returnVal != null
                                    ? new ArkTSStatement.ExpressionStatement(
                                            returnVal)
                                    : new ArkTSStatement.BlockStatement(
                                            bodyStmts),
                            isAsync);
        } else if (bodyStmts.isEmpty()) {
            funcExpr = new ArkTSAccessExpressions
                    .ArrowFunctionExpression(
                            params,
                            new ArkTSStatement.BlockStatement(
                                    Collections.emptyList()),
                            isAsync);
        } else {
            funcExpr = new ArkTSAccessExpressions
                    .AnonymousFunctionExpression(
                            params,
                            new ArkTSStatement.BlockStatement(bodyStmts),
                            isAsync, false);
        }

        // Detect closure: check if the function body references
        // lexical variables from outer scope
        funcExpr = tryWrapAsClosure(funcExpr, ctx);

        if (!declaredVars.contains(varName)) {
            declaredVars.add(varName);
            return new StatementResult(
                    new ArkTSStatement.VariableDeclaration(
                            "let", varName, null, funcExpr),
                    funcExpr);
        }
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        varName),
                                funcExpr)),
                funcExpr);
    }

    /**
     * Resolves the method name from the ABC file for a definefunc.
     */
    private String resolveMethodName(DecompilationContext ctx,
            int methodIdx) {
        if (ctx != null && ctx.abcFile != null) {
            AbcMethod method = findMethodByIdx(ctx, methodIdx);
            if (method != null) {
                return DeclarationBuilder.sanitizeMethodName(
                        method.getName());
            }
        }
        return null;
    }

    /**
     * Detects if a method is a generator by checking its code
     * for suspendgenerator/yield instructions.
     */
    private boolean detectGeneratorMethod(DecompilationContext ctx,
            int methodIdx) {
        if (ctx == null || ctx.abcFile == null) {
            return false;
        }
        AbcMethod method = findMethodByIdx(ctx, methodIdx);
        if (method == null) {
            return false;
        }
        AbcCode code = ctx.abcFile.getCodeForMethod(method);
        if (code == null || code.getInstructions() == null) {
            return false;
        }
        for (byte b : code.getInstructions()) {
            int unsigned = b & 0xFF;
            if (unsigned == ArkOpcodesCompat.SUSPENDGENERATOR
                    || unsigned == ArkOpcodesCompat
                            .CREATEGENERATOROBJ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to resolve the body of a definefunc method from
     * the ABC file.
     */
    private List<ArkTSStatement> resolveDefineFuncBody(
            DecompilationContext ctx, int methodIdx) {
        if (ctx == null || ctx.abcFile == null) {
            return Collections.emptyList();
        }
        AbcMethod method = findMethodByIdx(ctx, methodIdx);
        if (method == null) {
            return Collections.emptyList();
        }
        AbcCode code = ctx.abcFile.getCodeForMethod(method);
        if (code == null || code.getCodeSize() == 0) {
            return Collections.emptyList();
        }
        try {
            return decompiler.decompileMethodBody(method, code,
                    ctx.abcFile);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Finds a method by its index in the flat method list across
     * all classes.
     *
     * @param ctx the decompilation context
     * @param methodIdx the flat method index
     * @return the method, or null if not found
     */
    private AbcMethod findMethodByIdx(DecompilationContext ctx,
            int methodIdx) {
        if (ctx == null || ctx.abcFile == null || methodIdx < 0) {
            return null;
        }
        return ctx.abcFile.getMethodByFlatIndex(methodIdx);
    }

    /**
     * Infers a lexical variable name from the stored expression.
     * Uses function names, property names, and variable names
     * to give lexical variables meaningful identifiers.
     */
    private static String inferLexVarName(ArkTSExpression value,
            DecompilationContext ctx) {
        if (value instanceof ArkTSExpression.VariableExpression) {
            String name = ((ArkTSExpression.VariableExpression) value)
                    .getName();
            // Skip generic register names and acc
            if (name != null && !name.equals("acc")
                    && !name.startsWith("v") && name.length() > 1) {
                try {
                    Integer.parseInt(name.substring(1));
                    return null; // register name like v0, v1
                } catch (NumberFormatException e) {
                    return name; // actual named variable
                }
            }
        }
        // Property access: obj.prop → prop
        if (value instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression member =
                    (ArkTSExpression.MemberExpression) value;
            if (!member.isComputed()) {
                ArkTSExpression prop = member.getProperty();
                if (prop instanceof ArkTSExpression.VariableExpression) {
                    return ((ArkTSExpression.VariableExpression) prop)
                            .getName();
                }
            }
        }
        // Call expression: use callee name
        if (value instanceof ArkTSExpression.CallExpression) {
            ArkTSExpression callee =
                    ((ArkTSExpression.CallExpression) value).getCallee();
            if (callee instanceof ArkTSExpression.MemberExpression) {
                ArkTSExpression prop =
                        ((ArkTSExpression.MemberExpression) callee)
                                .getProperty();
                if (prop instanceof ArkTSExpression.VariableExpression) {
                    return ((ArkTSExpression.VariableExpression) prop)
                            .getName();
                }
            }
            if (callee instanceof ArkTSExpression.VariableExpression) {
                return ((ArkTSExpression.VariableExpression) callee)
                        .getName();
            }
        }
        return null;
    }

    /**
     * Checks if a function expression references lexical variables
     * and wraps it as a ClosureExpression if so. Also tracks captured
     * registers in the decompilation context.
     */
    private ArkTSExpression tryWrapAsClosure(
            ArkTSExpression funcExpr,
            DecompilationContext ctx) {
        if (ctx == null || ctx.abcFile == null) {
            return funcExpr;
        }
        List<String> captured = new ArrayList<>();
        collectLexicalReferences(funcExpr, captured);
        if (!captured.isEmpty()) {
            // Track captured registers in context for downstream use
            for (String varName : captured) {
                if (varName.startsWith("v")) {
                    try {
                        int reg = Integer.parseInt(varName.substring(1));
                        ctx.addCapturedRegister(reg);
                    } catch (NumberFormatException e) {
                        // Not a register reference, skip
                    }
                }
            }
            return new ArkTSAccessExpressions.ClosureExpression(
                    funcExpr, captured);
        }
        return funcExpr;
    }

    /**
     * Collects lexical variable references (lex_LEVEL_SLOT)
     * from an expression tree.
     */
    private void collectLexicalReferences(ArkTSExpression expr,
            List<String> captured) {
        if (expr instanceof ArkTSExpression.VariableExpression) {
            String name =
                    ((ArkTSExpression.VariableExpression) expr).getName();
            if (name.startsWith("lex_")
                    && !captured.contains(name)) {
                captured.add(name);
            }
        } else if (expr
                instanceof ArkTSAccessExpressions.ArrowFunctionExpression) {
            ArkTSAccessExpressions.ArrowFunctionExpression arrow =
                    (ArkTSAccessExpressions.ArrowFunctionExpression) expr;
            collectLexicalReferencesFromStatement(
                    arrow.getBody(), captured);
        } else if (expr instanceof ArkTSAccessExpressions
                .AnonymousFunctionExpression) {
            ArkTSAccessExpressions.AnonymousFunctionExpression anon =
                    (ArkTSAccessExpressions
                            .AnonymousFunctionExpression) expr;
            collectLexicalReferencesFromStatement(
                    anon.getBody(), captured);
        }
    }

    /**
     * Collects lexical variable references from a statement tree.
     */
    private void collectLexicalReferencesFromStatement(
            ArkTSStatement stmt, List<String> captured) {
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            for (ArkTSStatement child
                    : ((ArkTSStatement.BlockStatement) stmt).getBody()) {
                collectLexicalReferencesFromStatement(child, captured);
            }
        } else if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            collectLexicalReferences(
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression(),
                    captured);
        } else if (stmt
                instanceof ArkTSStatement.VariableDeclaration) {
            ArkTSExpression init =
                    ((ArkTSStatement.VariableDeclaration) stmt)
                            .getInitializer();
            if (init != null) {
                collectLexicalReferences(init, captured);
            }
        } else if (stmt
                instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) stmt).getValue();
            if (val != null) {
                collectLexicalReferences(val, captured);
            }
        }
    }

    private StatementResult handleMov(List<ArkOperand> operands,
            DecompilationContext ctx, Set<String> declaredVars,
            TypeInference typeInf) {
        int dstReg = (int) operands.get(0).getValue();
        int srcReg = (int) operands.get(1).getValue();
        String dstName = ctx.resolveRegisterName(dstReg);
        String srcName = ctx.resolveRegisterName(srcReg);
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
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int numArgs = (int) operands.get(0).getValue();
        List<ArkTSExpression> args = new ArrayList<>();
        int firstReg = (int) operands.get(
                operands.size() - 1).getValue();
        for (int a = 0; a < numArgs; a++) {
            args.add(new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(firstReg + a)));
        }
        ArkTSExpression callee = resolveCallee(accValue, ctx);

        // STA preserves accValue, so the accumulator may hold a literal
        // (e.g., the last stored argument) instead of the class reference.
        // The first register in the range often holds the class reference.
        // If accValue is a literal but firstReg has a class-like expression,
        // use the register expression as callee and shift args.
        if (isLikelyLiteral(callee)) {
            if (numArgs > 0) {
                ArkTSExpression firstArgExpr =
                        ctx.getRegisterExpression(firstReg);
                if (firstArgExpr != null
                        && !isLikelyLiteral(firstArgExpr)) {
                    callee = firstArgExpr;
                    args.remove(0);
                }
            } else {
                // Zero-arg case: the register may hold the class reference
                ArkTSExpression regExpr =
                        ctx.getRegisterExpression(firstReg);
                if (regExpr != null && !isLikelyLiteral(regExpr)) {
                    callee = regExpr;
                }
            }
        }

        // Detect built-in class construction (Map, Set, Promise, etc.)
        if (callee instanceof ArkTSExpression.VariableExpression) {
            String name =
                    ((ArkTSExpression.VariableExpression) callee).getName();
            if (ArkTSAccessExpressions.BuiltInNewExpression.BUILT_IN_CLASSES
                    .contains(name)) {
                return new StatementResult(null,
                        new ArkTSAccessExpressions.BuiltInNewExpression(
                                name, args));
            }
        }

        return new StatementResult(null,
                new ArkTSExpression.NewExpression(callee, args));
    }

    /**
     * Resolves the callee expression for newobjrange by checking if the
     * accumulator holds a register reference with a stored expression
     * (e.g., a class name resolved from defineclasswithbuffer).
     *
     * <p>When the accumulator value is a VariableExpression with a
     * register-style name (v0, v1, etc.), this method checks if a more
     * descriptive expression was stored to that register. This allows
     * {@code new MyClass(args)} instead of {@code new v0(args)}.
     */
    private static ArkTSExpression resolveCallee(
            ArkTSExpression accValue, DecompilationContext ctx) {
        if (accValue == null) {
            return new ArkTSExpression.VariableExpression(ACC);
        }
        if (!(accValue instanceof ArkTSExpression.VariableExpression)) {
            return accValue;
        }
        String name = ((ArkTSExpression.VariableExpression) accValue)
                .getName();
        // Check if this is a register-style name (v0, v1, etc.)
        // that might have a stored expression from defineclasswithbuffer
        if (name.startsWith("v") && name.length() > 1) {
            try {
                int reg = Integer.parseInt(name.substring(1));
                ArkTSExpression stored =
                        ctx.getRegisterExpression(reg);
                if (stored != null) {
                    return stored;
                }
            } catch (NumberFormatException e) {
                // Not a register-style name, use as-is
            }
        }
        return accValue;
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
                    || "false".equals(name)
                    || "NaN".equals(name)
                    || "Infinity".equals(name);
        }
        return false;
    }

    private static StatementResult handleDefineByName(int opcode,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int flags = (int) operands.get(0).getValue();
        int stringIdx = (int) operands.get(1).getValue();
        String fieldName = ctx.resolveString(stringIdx);
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        boolean isStaticField = (flags & 0x01) != 0;
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(
                        new ArkTSExpression.VariableExpression(
                                ctx.resolveRegisterName(objReg)),
                        new ArkTSExpression.VariableExpression(fieldName),
                        false);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        if (isStaticField) {
            ArkTSExpression staticTarget =
                    new ArkTSPropertyExpressions.StaticFieldExpression(
                            target, value);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(staticTarget),
                    value);
        }
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(target, value)),
                value);
    }

    /**
     * Handles definepropertybyname instructions which use
     * Object.defineProperty semantics (with attribute control)
     * rather than simple field assignment.
     */
    private static StatementResult handleDefinePropertyByName(
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int stringIdx = (int) operands.get(1).getValue();
        String propName = PropertyAccessHandler.sanitizePropertyName(
                ctx.resolveString(stringIdx));
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(objReg));
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(propName);
        ArkTSExpression value = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(obj, prop, false);
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(target, value)),
                value);
    }

    // --- CallRuntime (0xFB prefix) instruction handling ---

    /**
     * Handles callruntime (0xFB-prefixed) instructions.
     *
     * <p>Maps runtime sub-opcodes to their ArkTS equivalents where
     * possible. Some runtime calls have direct ArkTS semantics
     * (e.g. callinit → constructor initialization, definefieldbyvalue
     * → property definition). Others emit a descriptive comment.
     *
     * @param subOpcode the callruntime sub-opcode
     * @param operands the decoded operands
     * @param accValue the current accumulator value
     * @param ctx the decompilation context
     * @return the statement result, or null for no-ops
     */
    private static StatementResult handleCallRuntime(int subOpcode,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        // --- callinit: constructor initialization call ---
        if (subOpcode == ArkOpcodesCompat.CRT_CALLINIT) {
            // callinit takes a V8 register for the object being
            // initialized. This is typically a no-op in ArkTS output
            // since constructor initialization is implicit.
            return StatementResult.NO_OP;
        }

        // --- definefieldbyvalue: obj[key] = acc (field definition) ---
        if (subOpcode == ArkOpcodesCompat.CRT_DEFINEFIELDBYVALUE) {
            int objReg = (int) operands.get(0).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(objReg));
            // The key is in the accumulator, value from context
            ArkTSExpression key = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(obj, key, true);
            return new StatementResult(null, target);
        }

        // --- definefieldbyindex: obj[index] = acc (field by index) ---
        if (subOpcode == ArkOpcodesCompat.CRT_DEFINEFIELDBYINDEX) {
            int objReg = (int) operands.get(0).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(objReg));
            return new StatementResult(null,
                    new ArkTSExpression.MemberExpression(
                            obj, new ArkTSExpression.VariableExpression(ACC),
                            true));
        }

        // --- topropertykey: convert accumulator to property key ---
        if (subOpcode == ArkOpcodesCompat.CRT_TOPROPERTYKEY) {
            // This is a type conversion runtime call, result is
            // the accumulator coerced to a property key
            return new StatementResult(null, accValue);
        }

        // --- createprivateproperty: declare private field ---
        if (subOpcode == ArkOpcodesCompat.CRT_CREATEPRIVATEPROPERTY) {
            return PropertyAccessHandler
                    .translateCreatePrivateProperty(operands, ctx);
        }

        // --- defineprivateproperty: set private property value ---
        if (subOpcode == ArkOpcodesCompat.CRT_DEFINEPRIVATEPROPERTY) {
            return PropertyAccessHandler
                    .translateDefinePrivateProperty(
                            operands, accValue, ctx);
        }

        // --- istrue/isfalse runtime versions ---
        if (subOpcode == ArkOpcodesCompat.CRT_ISTRUE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Boolean"),
                            List.of(operand)));
        }
        if (subOpcode == ArkOpcodesCompat.CRT_ISFALSE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.UnaryExpression("!", operand, true));
        }

        // --- ldlazymodulevar: lazy-loaded module variable ---
        if (subOpcode == ArkOpcodesCompat.CRT_LDLAZYMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "lazy_mod_" + varIdx));
        }

        // --- ldsendableexternalmodulevar: sendable module var ---
        if (subOpcode == ArkOpcodesCompat.CRT_LDSENDABLEEXTERNALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "sendable_ext_mod_" + varIdx));
        }

        // --- ldsendablevar / ldsendablevarptr: load sendable var ---
        if (subOpcode == ArkOpcodesCompat.CRT_LDSENDABLEVAR
                || subOpcode == ArkOpcodesCompat.CRT_LDSENDABLEVARPTR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "sendable_" + level + "_" + slot));
        }

        // --- stsendablevar / stsendablevarptr: store sendable var ---
        if (subOpcode == ArkOpcodesCompat.CRT_STSENDABLEVAR
                || subOpcode == ArkOpcodesCompat.CRT_STSENDABLEVARPTR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "sendable_" + level + "_"
                                                        + slot),
                                        accValue)),
                        accValue);
            }
            return null;
        }

        // --- newsendableenv: create sendable lexical environment ---
        if (subOpcode == ArkOpcodesCompat.CRT_NEWSENDABLEENV) {
            return StatementResult.NO_OP;
        }

        // --- definesendableclass: define a sendable class ---
        if (subOpcode == ArkOpcodesCompat.CRT_DEFINESENDABLECLASS) {
            if (operands.size() >= 1) {
                int methodIdx = (int) operands.get(0).getValue();
                String className = resolveClassLikeName(methodIdx, ctx);
                return new StatementResult(null,
                        new ArkTSExpression.VariableExpression(
                                "/* sendable class: "
                                        + (className != null
                                                ? className : "method_" + methodIdx)
                                        + " */"));
            }
            return StatementResult.NO_OP;
        }

        // --- ldsendableclass: load sendable class ---
        if (subOpcode == ArkOpcodesCompat.CRT_LDSENDABLECLASS) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "sendable_class"));
        }

        // --- notifyconcurrentresult: concurrent result notification ---
        if (subOpcode == ArkOpcodesCompat.CRT_NOTIFYCONCURRENTRESULT) {
            return StatementResult.NO_OP;
        }

        // --- Generic fallback for unknown callruntime sub-opcodes ---
        String name = ArkOpcodes.getCallRuntimeMnemonic(subOpcode);
        return new StatementResult(null,
                new ArkTSAccessExpressions.RuntimeCallExpression(
                        name, Collections.emptyList()));
    }

    // --- Delegates for external callers ---

    private static String resolveClassLikeName(int methodIdx,
            DecompilationContext ctx) {
        if (ctx == null || ctx.abcFile == null) {
            return null;
        }
        try {
            if (methodIdx >= 0
                    && methodIdx < ctx.abcFile.getMethods().size()) {
                AbcMethod m = ctx.abcFile.getMethods().get(methodIdx);
                String name = m.getName();
                if (name != null && !name.isEmpty()) {
                    return DeclarationBuilder.sanitizeClassName(name);
                }
            }
        } catch (Exception e) {
            // Fall through
        }
        return null;
    }

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

    /**
     * Checks if the given expression is a DefineFuncExpression.
     *
     * @param expr the expression to check
     * @return true if it is a DefineFuncExpression
     */
    static boolean isDefineFuncExpression(ArkTSExpression expr) {
        return expr instanceof DefineFuncExpression;
    }

    /**
     * Extracts the method index from a DefineFuncExpression.
     *
     * @param expr the expression (must be a DefineFuncExpression)
     * @return the method index
     */
    static int getDefineFuncMethodIdx(ArkTSExpression expr) {
        return ((DefineFuncExpression) expr).getMethodIdx();
    }

    /**
     * Creates an arrow function expression from a definefunc result.
     * The body is a simple expression (single return value).
     *
     * @param params the function parameters
     * @param bodyExpr the body expression
     * @param isAsync true if async
     * @return the arrow function expression
     */
    static ArkTSExpression createArrowFunction(
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            ArkTSExpression bodyExpr, boolean isAsync) {
        return new ArkTSAccessExpressions.ArrowFunctionExpression(
                params,
                new ArkTSStatement.ExpressionStatement(bodyExpr),
                isAsync);
    }

    /**
     * Creates an arrow function expression with a block body.
     *
     * @param params the function parameters
     * @param bodyStmts the body statements
     * @param isAsync true if async
     * @return the arrow function expression
     */
    static ArkTSExpression createArrowFunctionWithBody(
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            List<ArkTSStatement> bodyStmts, boolean isAsync) {
        return new ArkTSAccessExpressions.ArrowFunctionExpression(
                params,
                new ArkTSStatement.BlockStatement(bodyStmts),
                isAsync);
    }

    /**
     * Creates an anonymous function expression from a definefunc result.
     *
     * @param params the function parameters
     * @param bodyStmts the body statements
     * @param isAsync true if async
     * @param isGenerator true if generator
     * @return the anonymous function expression
     */
    static ArkTSExpression createAnonymousFunction(
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            List<ArkTSStatement> bodyStmts, boolean isAsync,
            boolean isGenerator) {
        return new ArkTSAccessExpressions.AnonymousFunctionExpression(
                params,
                new ArkTSStatement.BlockStatement(bodyStmts),
                isAsync, isGenerator);
    }

    /**
     * Creates a generator function expression from a definefunc result.
     *
     * @param name the function name (may be null)
     * @param params the function parameters
     * @param bodyStmts the body statements
     * @param isAsync true if async
     * @return the generator function expression
     */
    static ArkTSExpression createGeneratorFunction(String name,
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            List<ArkTSStatement> bodyStmts, boolean isAsync) {
        return new ArkTSAccessExpressions.GeneratorFunctionExpression(
                name, params,
                new ArkTSStatement.BlockStatement(bodyStmts),
                isAsync);
    }
}
