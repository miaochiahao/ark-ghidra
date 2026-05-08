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
 * type inference.
 */
class InstructionHandler {

    private static final String ACC = "acc";

    private final ArkTSDecompiler decompiler;

    InstructionHandler(ArkTSDecompiler decompiler) {
        this.decompiler = decompiler;
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
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        String.valueOf(operands.get(0).getValue()),
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.FLDAI: {
                double val = Double.longBitsToDouble(
                        operands.get(0).getValue());
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        String.valueOf(val),
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA_STR: {
                String str = ctx.resolveString(
                        (int) operands.get(0).getValue());
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        str, ArkTSExpression.LiteralExpression.LiteralKind.STRING);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA: {
                int reg = (int) operands.get(0).getValue();
                ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                        "v" + reg);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDUNDEFINED:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("undefined",
                                ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
            case ArkOpcodesCompat.LDNULL:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("null",
                                ArkTSExpression.LiteralExpression.LiteralKind.NULL));
            case ArkOpcodesCompat.LDTRUE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("true",
                                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDFALSE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("false",
                                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDNAN:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("NaN",
                                ArkTSExpression.LiteralExpression.LiteralKind.NAN));
            case ArkOpcodesCompat.LDINFINITY:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("Infinity",
                                ArkTSExpression.LiteralExpression.LiteralKind.INFINITY));
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

        if (opcode == ArkOpcodesCompat.SUSPENDGENERATOR) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSAccessExpressions.YieldExpression(value, false)),
                    null);
        }

        if (opcode == ArkOpcodesCompat.RESUMEGENERATOR) {
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression("generator");
            return new StatementResult(null, expr);
        }

        if (opcode == ArkOpcodesCompat.GETRESUMEMODE) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("resumeMode"));
        }

        // --- Async operations ---
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONAWAITUNCAUGHT) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression promise =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(null,
                    new ArkTSAccessExpressions.AwaitExpression(promise));
        }

        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONRESOLVE) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(value), null);
        }

        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONREJECT) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(value), null);
        }

        // --- Async generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEASYNCGENERATOROBJ) {
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

        if (opcode == ArkOpcodesCompat.ASYNCGENERATORRESOLVE) {
            int valueReg = (int) operands.get(1).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + valueReg);
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(value), null);
        }

        if (opcode == ArkOpcodesCompat.ASYNCGENERATORREJECT) {
            int valueReg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + valueReg);
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(value), null);
        }

        if (opcode == ArkOpcodesCompat.SETGENERATORSTATE) {
            int state = (int) operands.get(0).getValue();
            ArkTSExpression stateExpr = new ArkTSExpression.LiteralExpression(
                    String.valueOf(state),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSPropertyExpressions.GeneratorStateExpression(
                                    stateExpr)),
                    null);
        }

        // --- Private property access ---
        if (opcode == ArkOpcodesCompat.LDPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.ThisExpression();
            ArkTSExpression expr =
                    new ArkTSPropertyExpressions.PrivateMemberExpression(obj, propName);
            return new StatementResult(null, expr);
        }

        if (opcode == ArkOpcodesCompat.STPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression("v" + objReg);
            ArkTSExpression target =
                    new ArkTSPropertyExpressions.PrivateMemberExpression(obj, propName);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(target, value)),
                    value);
        }

        // --- TestIn (prop in obj) ---
        if (opcode == ArkOpcodesCompat.TESTIN) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression expr =
                    new ArkTSPropertyExpressions.InExpression(
                            new ArkTSExpression.VariableExpression(propName),
                            obj);
            return new StatementResult(null, expr);
        }

        // --- Object creation with excluded keys ---
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHEXCLUDEDKEYS) {
            int numKeys = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression("v" + srcReg);
            ArkTSExpression spreadExpr =
                    new ArkTSAccessExpressions.SpreadExpression(source);
            List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty> props =
                    new ArrayList<>();
            props.add(new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                    null, spreadExpr));
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(props);
            return new StatementResult(null, expr);
        }

        // --- Define getter/setter by value ---
        if (opcode == ArkOpcodesCompat.DEFINEGETTERSETTERBYVALUE) {
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.VariableExpression(
                                    "/* definegettersetterbyvalue */")),
                    null);
        }

        // --- Delete object property ---
        if (opcode == ArkOpcodesCompat.DELOBJPROP) {
            ArkTSExpression obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression expr =
                    new ArkTSPropertyExpressions.DeleteExpression(obj);
            return new StatementResult(null, expr);
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
                    new ArkTSPropertyExpressions.CopyDataPropertiesExpression(
                            target, source);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(target, expr)),
                    expr);
        }

        // --- IsTrue / IsFalse ---
        if (opcode == ArkOpcodesCompat.ISTRUE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression boolExpr =
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Boolean"),
                            List.of(operand));
            return new StatementResult(null, boolExpr);
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
            int reg = (int) operands.get(0).getValue();
            String varName = "v" + reg;
            if (accValue != null) {
                String inferredType = typeInf.inferTypeForInstruction(insn);
                String typeAnnotation = TypeInference.formatTypeAnnotation(
                        varName, getAccType(accValue, typeInf));
                typeInf.setRegisterType(varName,
                        getAccType(accValue, typeInf));

                if (!declaredVars.contains(varName)
                        && !(reg < ctx.numArgs)) {
                    declaredVars.add(varName);
                    ArkTSStatement stmt =
                            new ArkTSStatement.VariableDeclaration(
                                    "let", varName, typeAnnotation, accValue);
                    return new StatementResult(stmt, accValue);
                }
                ArkTSStatement stmt = new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        varName),
                                accValue));
                return new StatementResult(stmt, accValue);
            }
            return null;
        }

        // --- MOV ---
        if (opcode == ArkOpcodesCompat.MOV) {
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

        // --- Binary operations: acc = acc OP v[operand] ---
        if (isBinaryOp(opcode)) {
            String op = getBinaryOperator(opcode);
            int reg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression left = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression right =
                    new ArkTSExpression.VariableExpression("v" + reg);
            ArkTSExpression result =
                    new ArkTSExpression.BinaryExpression(left, op, right);
            return new StatementResult(null, result);
        }

        // --- Unary operations: acc = OP acc ---
        if (isUnaryOp(opcode)) {
            String op = getUnaryOperator(opcode);
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            boolean prefix = true;
            ArkTSExpression result =
                    new ArkTSExpression.UnaryExpression(op, operand, prefix);
            return new StatementResult(null, result);
        }

        // --- Inc/Dec: acc = acc +/- 1 ---
        if (opcode == ArkOpcodesCompat.INC || opcode == ArkOpcodesCompat.DEC) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression one = new ArkTSExpression.LiteralExpression("1",
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            String op = opcode == ArkOpcodesCompat.INC ? "+" : "-";
            ArkTSExpression result =
                    new ArkTSExpression.BinaryExpression(operand, op, one);
            return new StatementResult(null, result);
        }

        // --- Function calls ---
        if (isCallOpcode(opcode)) {
            ArkTSExpression callExpr = translateCall(insn, accValue, ctx);
            return new StatementResult(null, callExpr);
        }

        // --- Property access (load) ---
        if (isPropertyLoadOpcode(opcode)) {
            ArkTSExpression expr =
                    translatePropertyLoad(insn, accValue, ctx);
            return new StatementResult(null, expr);
        }

        // --- Property store ---
        if (isPropertyStoreOpcode(opcode)) {
            ArkTSExpression expr =
                    translatePropertyStore(insn, accValue, ctx);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(expr),
                    accValue);
        }

        // --- Lexical variable access ---
        if (opcode == ArkOpcodesCompat.LDLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "lex_" + level + "_" + slot);
            return new StatementResult(null, expr);
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

        // --- Define function ---
        if (opcode == ArkOpcodesCompat.DEFINEFUNC) {
            int methodIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "func_" + methodIdx);
            return new StatementResult(null, expr);
        }

        // --- Define method ---
        if (opcode == ArkOpcodesCompat.DEFINEMETHOD) {
            int methodIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "method_" + methodIdx);
            return new StatementResult(null, expr);
        }

        // --- Define class with buffer ---
        if (opcode == ArkOpcodesCompat.DEFINECLASSWITHBUFFER) {
            return processDefineClassWithBuffer(insn, ctx);
        }

        // --- Define field by name ---
        if (opcode == ArkOpcodesCompat.DEFINEFIELDBYNAME) {
            int stringIdx = (int) operands.get(1).getValue();
            String fieldName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + objReg),
                            new ArkTSExpression.VariableExpression(fieldName),
                            false);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    target, value)),
                    value);
        }

        // --- Define property by name ---
        if (opcode == ArkOpcodesCompat.DEFINEPROPERTYBYNAME) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + objReg),
                            new ArkTSExpression.VariableExpression(propName),
                            false);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    target, value)),
                    value);
        }

        // --- Super call this range ---
        if (opcode == ArkOpcodesCompat.SUPERCALLTHISRANGE
                || opcode == ArkOpcodesCompat.SUPERCALLARROWRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(
                    operands.size() - 1).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            ArkTSStatement superCall =
                    new ArkTSControlFlow.SuperCallStatement(args);
            return new StatementResult(superCall,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }

        // --- Super call spread ---
        if (opcode == ArkOpcodesCompat.SUPERCALLSPREAD) {
            int spreadReg = (int) operands.get(0).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.SpreadExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + spreadReg));
            List<ArkTSExpression> args = new ArrayList<>();
            args.add(spreadArg);
            ArkTSStatement superCall =
                    new ArkTSControlFlow.SuperCallStatement(args);
            return new StatementResult(superCall,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }

        // --- Module variable access ---
        if (opcode == ArkOpcodesCompat.LDEXTERNALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "ext_mod_" + varIdx);
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.LDLOCALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "local_mod_" + varIdx);
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.STMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "mod_" + varIdx),
                                        accValue)),
                        accValue);
            }
            return null;
        }
        if (opcode == ArkOpcodesCompat.GETMODULENAMESPACE) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "module_ns_" + varIdx);
            return new StatementResult(null, expr);
        }

        // --- Dynamic import ---
        if (opcode == ArkOpcodesCompat.DYNAMICIMPORT) {
            ArkTSExpression specifier = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression callExpr =
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("import"),
                            List.of(specifier));
            return new StatementResult(null, callExpr);
        }

        // --- Global record stores (const/var declarations) ---
        if (opcode == ArkOpcodesCompat.STCONSTTOGLOBALRECORD) {
            int stringIdx = (int) operands.get(0).getValue();
            String varName = ctx.resolveString(stringIdx);
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "const", varName, null, accValue),
                        accValue);
            }
            return null;
        }
        if (opcode == ArkOpcodesCompat.STTOGLOBALRECORD) {
            int stringIdx = (int) operands.get(0).getValue();
            String varName = ctx.resolveString(stringIdx);
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, null, accValue),
                        accValue);
            }
            return null;
        }

        // --- Create array/object with buffer ---
        if (opcode == ArkOpcodesCompat.CREATEARRAYWITHBUFFER) {
            int numElements = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.ArrayLiteralExpression(
                            createPlaceholderElements(numElements));
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHBUFFER) {
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            Collections.emptyList());
            return new StatementResult(null, expr);
        }

        // --- STARRAYSPREAD (spread into array) ---
        if (opcode == ArkOpcodesCompat.STARRAYSPREAD) {
            int dstReg = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(1).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSExpression.VariableExpression("v" + srcReg);
            ArkTSExpression spread =
                    new ArkTSAccessExpressions.SpreadExpression(spreadArg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(spread),
                    spread);
        }

        // --- Iterator opcodes ---
        if (ArkOpcodesCompat.isGetIterator(opcode)) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("iterator"));
        }
        if (ArkOpcodesCompat.isCloseIterator(opcode)) {
            return null;
        }
        if (opcode == ArkOpcodesCompat.GETNEXTPROPNAME) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("nextProp"));
        }
        if (opcode == ArkOpcodesCompat.GETPROPITERATOR) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("propIterator"));
        }

        // --- Accumulator-only loads (set acc, no statement) ---

        if (opcode == ArkOpcodesCompat.LDSYMBOL) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("Symbol"));
        }
        if (opcode == ArkOpcodesCompat.LDFUNCTION) {
            int funcIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "func_" + funcIdx));
        }
        if (opcode == ArkOpcodesCompat.LDNEWTARGET) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("new.target"));
        }
        if (opcode == ArkOpcodesCompat.LDHOLE) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("undefined"));
        }
        if (opcode == ArkOpcodesCompat.LDNAN) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("NaN"));
        }
        if (opcode == ArkOpcodesCompat.LDINFINITY) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("Infinity"));
        }
        if (opcode == ArkOpcodesCompat.LDBIGINT) {
            int bigintIdx = (int) operands.get(0).getValue();
            String bigintStr = ctx.resolveString(bigintIdx);
            return new StatementResult(null,
                    new ArkTSExpression.LiteralExpression(bigintStr + "n",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
        }

        // --- Type conversion ---

        if (opcode == ArkOpcodesCompat.TONUMBER) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Number"),
                            List.of(val)));
        }
        if (opcode == ArkOpcodesCompat.TONUMERIC) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Number"),
                            List.of(val)));
        }

        // --- Debugger ---

        if (opcode == ArkOpcodesCompat.DEBUGGER) {
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.VariableExpression("debugger")),
                    accValue);
        }

        // --- Lexical environment ---

        if (opcode == ArkOpcodesCompat.POPLEXENV) {
            return null;
        }
        if (opcode == ArkOpcodesCompat.NEWLEXENVWITHNAME) {
            return null;
        }

        // --- Argument handling ---

        if (opcode == ArkOpcodesCompat.GETUNMAPPEDARGS) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("arguments"));
        }
        if (opcode == ArkOpcodesCompat.COPYRESTARGS) {
            int restIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "rest_" + restIdx));
        }

        // --- Iterator result ---

        if (opcode == ArkOpcodesCompat.CREATEITERRESULTOBJ) {
            return new StatementResult(null,
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            Collections.emptyList()));
        }

        // --- Apply/call ---

        if (opcode == ArkOpcodesCompat.APPLY) {
            ArkTSExpression func = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(1).getValue();
            List<ArkTSExpression> applyArgs = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                applyArgs.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.ArrayLiteralExpression(applyArgs);
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(func,
                            List.of(new ArkTSAccessExpressions.SpreadExpression(
                                    spreadArg))));
        }
        if (opcode == ArkOpcodesCompat.NEWOBJAPPLY) {
            ArkTSExpression ctor = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(1).getValue();
            List<ArkTSExpression> applyArgs = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                applyArgs.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.ArrayLiteralExpression(applyArgs);
            return new StatementResult(null,
                    new ArkTSExpression.NewExpression(ctor,
                            List.of(new ArkTSAccessExpressions.SpreadExpression(
                                    spreadArg))));
        }

        // --- RegExp ---

        if (opcode == ArkOpcodesCompat.CREATEREGEXPWITHLITERAL) {
            int patternIdx = (int) operands.get(0).getValue();
            String pattern = ctx.resolveString(patternIdx);
            return new StatementResult(null,
                    new ArkTSExpression.NewExpression(
                            new ArkTSExpression.VariableExpression("RegExp"),
                            List.of(new ArkTSExpression.LiteralExpression(
                                    pattern,
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING))));
        }

        // --- Template ---

        if (opcode == ArkOpcodesCompat.GETTEMPLATEOBJECT) {
            int templateIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "template_" + templateIdx));
        }

        // --- Object prototype ---

        if (opcode == ArkOpcodesCompat.SETOBJECTWITHPROTO) {
            return null;
        }

        // --- Async iterator ---

        if (opcode == ArkOpcodesCompat.GETASYNCITERATOR) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "[Symbol.asyncIterator]"));
        }

        // --- Super by value ---

        if (opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            int keyReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + keyReg);
            return new StatementResult(null,
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            prop, true));
        }
        if (opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            int keyReg = (int) operands.get(
                    operands.size() - 2).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + keyReg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.MemberExpression(
                                            new ArkTSExpression
                                                    .VariableExpression(
                                                            "super"),
                                            prop, true),
                                    accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(ACC))),
                    accValue);
        }

        // --- Own property stores with name set ---

        if (opcode == ArkOpcodesCompat.STOWNBYVALUEWITHNAMESET) {
            int keyReg = (int) operands.get(
                    operands.size() - 2).getValue();
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression("v" + objReg);
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + keyReg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.MemberExpression(
                                            obj, prop, true),
                                    accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(ACC))),
                    accValue);
        }
        if (opcode == ArkOpcodesCompat.STOWNBYNAMEWITHNAMESET) {
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression("v" + objReg);
            String pn = ctx.resolveString(
                    (int) operands.get(1).getValue());
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression(pn);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.MemberExpression(
                                            obj, prop, false),
                                    accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(ACC))),
                    accValue);
        }

        // --- Class definition helpers ---

        if (opcode == ArkOpcodesCompat.DEFINEMETHOD) {
            int methodIdx = (int) operands.get(0).getValue();
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "method_" + methodIdx));
        }
        if (opcode == ArkOpcodesCompat.DEFINEFIELDBYNAME) {
            int stringIdx = (int) operands.get(0).getValue();
            String fieldName = ctx.resolveString(stringIdx);
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(fieldName));
        }
        if (opcode == ArkOpcodesCompat.DEFINEPROPERTYBYNAME) {
            int stringIdx = (int) operands.get(0).getValue();
            String pn = ctx.resolveString(stringIdx);
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(pn));
        }

        // --- Super calls ---

        if (opcode == ArkOpcodesCompat.SUPERCALLTHISRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(1).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }
        if (opcode == ArkOpcodesCompat.SUPERCALLSPREAD) {
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            List.of(new ArkTSAccessExpressions.SpreadExpression(
                                    accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(ACC)))));
        }
        if (opcode == ArkOpcodesCompat.SUPERCALLARROWRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(1).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }

        // --- Private property access ---

        if (opcode == ArkOpcodesCompat.LDPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(0).getValue();
            String fieldName = ctx.resolveString(stringIdx);
            return new StatementResult(null,
                    new ArkTSExpression.MemberExpression(
                            accValue != null ? accValue
                                    : new ArkTSExpression.VariableExpression(
                                            ACC),
                            new ArkTSExpression.VariableExpression(
                                    "#" + fieldName),
                            false));
        }
        if (opcode == ArkOpcodesCompat.STPRIVATEPROPERTY) {
            int stringIdx = (int) operands.get(0).getValue();
            String fieldName = ctx.resolveString(stringIdx);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.MemberExpression(
                                            accValue != null ? accValue
                                                    : new ArkTSExpression
                                                            .VariableExpression(
                                                                    ACC),
                                            new ArkTSExpression
                                                    .VariableExpression(
                                                            "#" + fieldName),
                                            false),
                                    new ArkTSExpression.VariableExpression(
                                            ACC))),
                    accValue);
        }

        // --- Testin ---

        if (opcode == ArkOpcodesCompat.TESTIN) {
            return new StatementResult(null,
                    new ArkTSExpression.BinaryExpression(
                            accValue != null ? accValue
                                    : new ArkTSExpression.VariableExpression(
                                            ACC),
                            "in",
                            new ArkTSExpression.VariableExpression(ACC)));
        }

        // --- Object property deletion and copy ---

        if (opcode == ArkOpcodesCompat.DELOBJPROP) {
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.CallExpression(
                                    new ArkTSExpression.MemberExpression(
                                            new ArkTSExpression
                                                    .VariableExpression(
                                                            "Object"),
                                            new ArkTSExpression
                                                    .VariableExpression(
                                                            "deleteProperty"),
                                            false),
                                    List.of(accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(
                                                            ACC)))),
                    null);
        }
        if (opcode == ArkOpcodesCompat.COPYDATAPROPERTIES) {
            return new StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.MemberExpression(
                                    new ArkTSExpression.VariableExpression(
                                            "Object"),
                                    new ArkTSExpression.VariableExpression(
                                            "assign"),
                                    false),
                            Collections.emptyList()));
        }

        // --- Object with excluded keys ---

        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHEXCLUDEDKEYS) {
            return new StatementResult(null,
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            Collections.emptyList()));
        }

        // --- Getter/setter by value ---

        if (opcode == ArkOpcodesCompat.DEFINEGETTERSETTERBYVALUE) {
            return null;
        }

        // --- Generator state management ---

        if (opcode == ArkOpcodesCompat.SETGENERATORSTATE) {
            return null;
        }
        if (opcode == ArkOpcodesCompat.CREATEASYNCGENERATOROBJ) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "asyncGenerator"));
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORRESOLVE) {
            return null;
        }
        if (opcode == ArkOpcodesCompat.ASYNCGENERATORREJECT) {
            return null;
        }

        // --- Fallback: emit a comment for unhandled opcode ---
        String fallbackMsg = "unhandled: " + insn.getMnemonic();
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* " + fallbackMsg + " */")),
                null);
    }

    // --- Type inference helper ---

    String getAccType(ArkTSExpression expr, TypeInference typeInf) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) expr;
            switch (lit.getKind()) {
                case NUMBER:
                    return "number";
                case STRING:
                    return "string";
                case BOOLEAN:
                    return "boolean";
                case NULL:
                    return "null";
                case UNDEFINED:
                    return "undefined";
                default:
                    return null;
            }
        }
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return typeInf.getRegisterType(
                    ((ArkTSExpression.VariableExpression) expr).getName());
        }
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            String op = ((ArkTSExpression.BinaryExpression) expr).getOperator();
            if (TypeInference.isComparisonOpFromSymbol(op)) {
                return "boolean";
            }
            if (TypeInference.isBinaryArithmeticOpFromSymbol(op)) {
                return "number";
            }
        }
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            String op = ((ArkTSExpression.UnaryExpression) expr).getOperator();
            if ("!".equals(op)) {
                return "boolean";
            }
            if ("-".equals(op)) {
                return "number";
            }
        }
        if (expr instanceof ArkTSAccessExpressions.ArrayLiteralExpression) {
            return "Array<unknown>";
        }
        if (expr instanceof ArkTSAccessExpressions.ObjectLiteralExpression) {
            return "Object";
        }
        if (expr instanceof ArkTSExpression.CallExpression) {
            return null;
        }
        if (expr instanceof ArkTSExpression.NewExpression) {
            return null;
        }
        return null;
    }

    // --- Call translation ---

    ArkTSExpression translateCall(ArkInstruction insn,
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

    // --- Property access translation ---

    ArkTSExpression translatePropertyLoad(ArkInstruction insn,
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

    ArkTSExpression translatePropertyStore(ArkInstruction insn,
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

    // --- Define class with buffer processing ---

    StatementResult processDefineClassWithBuffer(ArkInstruction insn,
            DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int methodIdx = (int) operands.get(0).getValue();
        int literalIdx = (int) operands.get(1).getValue();
        int lastReg = (int) operands.get(
                operands.size() - 1).getValue();

        String className = "class_" + methodIdx;
        if (ctx.abcFile != null) {
            className = decompiler.resolveClassNameFromMethod(
                    methodIdx, ctx.abcFile);
        }

        ArkTSExpression classExpr =
                new ArkTSExpression.VariableExpression(className);

        String varName = "v" + lastReg;
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration(
                "let", varName, className, classExpr);

        return new StatementResult(stmt, classExpr);
    }

    // --- Template literal reconstruction ---

    ArkTSExpression tryReconstructTemplateLiteral(
            ArkTSExpression accValue) {
        if (accValue == null) {
            return null;
        }
        List<ArkTSExpression> parts = new ArrayList<>();
        flattenAddChain(accValue, parts);
        List<String> quasis = new ArrayList<>();
        List<ArkTSExpression> expressions = new ArrayList<>();
        buildTemplateFromParts(parts, quasis, expressions);
        boolean hasString = false;
        for (ArkTSExpression part : parts) {
            if (isStringLiteralExpr(part)) {
                hasString = true;
                break;
            }
        }
        if (hasString && quasis.size() == expressions.size() + 1
                && !quasis.isEmpty()) {
            return new ArkTSAccessExpressions.TemplateLiteralExpression(
                    quasis, expressions);
        }
        return accValue;
    }

    private void flattenAddChain(ArkTSExpression expr,
            List<ArkTSExpression> parts) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            if ("+".equals(bin.getOperator())) {
                flattenAddChain(bin.getLeft(), parts);
                flattenAddChain(bin.getRight(), parts);
                return;
            }
        }
        parts.add(expr);
    }

    private void buildTemplateFromParts(List<ArkTSExpression> parts,
            List<String> quasis, List<ArkTSExpression> expressions) {
        StringBuilder currentQuasi = new StringBuilder();
        for (ArkTSExpression part : parts) {
            if (isStringLiteralExpr(part)) {
                currentQuasi.append(extractStringValue(part));
            } else {
                quasis.add(currentQuasi.toString());
                currentQuasi = new StringBuilder();
                expressions.add(part);
            }
        }
        quasis.add(currentQuasi.toString());
    }

    private boolean isStringLiteralExpr(ArkTSExpression expr) {
        return expr instanceof ArkTSExpression.LiteralExpression
                && ((ArkTSExpression.LiteralExpression) expr).getKind()
                        == ArkTSExpression.LiteralExpression.LiteralKind.STRING;
    }

    private String extractStringValue(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return ((ArkTSExpression.LiteralExpression) expr).getValue();
        }
        return "";
    }

    // --- Array destructuring detection ---

    ArkTSStatement tryDetectArrayDestructuring(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars,
            List<ArkTSStatement> stmts) {

        List<String> bindings = new ArrayList<>();
        String restBinding = null;
        String sourceVar = null;
        int consecutiveIdx = 0;
        int scanIdx = startIndex;

        while (scanIdx < instructions.size()) {
            ArkInstruction insn = instructions.get(scanIdx);
            int opcode = insn.getOpcode();

            if (opcode == ArkOpcodesCompat.NOP) {
                scanIdx++;
                continue;
            }

            if (opcode == ArkOpcodesCompat.LDA
                    && scanIdx + 2 < instructions.size()) {
                ArkInstruction nextInsn = instructions.get(scanIdx + 1);
                ArkInstruction afterNext = instructions.get(scanIdx + 2);

                if (nextInsn.getOpcode() == ArkOpcodesCompat.LDOBJBYINDEX
                        && afterNext.getOpcode() == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0).getValue();
                    String currentSource = "v" + srcReg;
                    List<ArkOperand> ldOps = nextInsn.getOperands();
                    int index = (int) ldOps.get(
                            ldOps.size() - 1).getValue();
                    int targetReg = (int) afterNext.getOperands().get(0)
                            .getValue();
                    String targetVar = "v" + targetReg;

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)
                            && index == consecutiveIdx) {
                        bindings.add(targetVar);
                        declaredVars.add(targetVar);
                        consecutiveIdx++;
                        scanIdx += 3;
                        continue;
                    }
                }
            }
            break;
        }

        if (bindings.size() >= 2 && sourceVar != null) {
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression(sourceVar);
            ArkTSExpression destrExpr =
                    new ArkTSPropertyExpressions.ArrayDestructuringExpression(
                            bindings, restBinding, source);
            return new ArkTSTypeDeclarations.DestructuringDeclaration(
                    "const", destrExpr);
        }
        return null;
    }

    // --- Object destructuring detection ---

    ArkTSStatement tryDetectObjectDestructuring(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars,
            List<ArkTSStatement> stmts) {

        List<ArkTSPropertyExpressions.ObjectDestructuringExpression.DestructuringBinding>
                bindings = new ArrayList<>();
        String sourceVar = null;
        int scanIdx = startIndex;
        int propertyCount = 0;

        while (scanIdx < instructions.size()) {
            ArkInstruction insn = instructions.get(scanIdx);
            int opcode = insn.getOpcode();

            if (opcode == ArkOpcodesCompat.NOP) {
                scanIdx++;
                continue;
            }

            if (opcode == ArkOpcodesCompat.LDA
                    && scanIdx + 2 < instructions.size()) {
                ArkInstruction nextInsn = instructions.get(scanIdx + 1);
                ArkInstruction afterNext = instructions.get(scanIdx + 2);

                if (nextInsn.getOpcode() == ArkOpcodesCompat.LDOBJBYNAME
                        && afterNext.getOpcode() == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0).getValue();
                    String currentSource = "v" + srcReg;
                    String propName = ctx.resolveString(
                            (int) nextInsn.getOperands().get(1).getValue());
                    int targetReg = (int) afterNext.getOperands().get(0)
                            .getValue();
                    String targetVar = "v" + targetReg;

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)) {
                        bindings.add(
                                new ArkTSPropertyExpressions
                                        .ObjectDestructuringExpression
                                        .DestructuringBinding(
                                                propName, null));
                        declaredVars.add(targetVar);
                        propertyCount++;
                        scanIdx += 3;
                        continue;
                    }
                }
            }
            break;
        }

        if (propertyCount >= 2 && sourceVar != null) {
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression(sourceVar);
            ArkTSExpression destrExpr =
                    new ArkTSPropertyExpressions.ObjectDestructuringExpression(
                            bindings, source);
            return new ArkTSTypeDeclarations.DestructuringDeclaration(
                    "const", destrExpr);
        }
        return null;
    }

    // --- Nullish coalescing detection ---

    ArkTSExpression tryDetectNullishCoalescing(
            ArkTSExpression condition,
            ArkTSExpression trueValue,
            ArkTSExpression falseValue) {
        if (condition == null || trueValue == null
                || falseValue == null) {
            return null;
        }
        if (isNullEqualityCheck(condition)) {
            ArkTSExpression checkedValue =
                    getNullCheckTarget(condition);
            if (checkedValue != null && expressionsMatch(checkedValue,
                    falseValue)) {
                return new ArkTSPropertyExpressions.NullishCoalescingExpression(
                        falseValue, trueValue);
            }
        }
        return null;
    }

    private boolean isNullEqualityCheck(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            return "===".equals(bin.getOperator())
                    || "==".equals(bin.getOperator());
        }
        return false;
    }

    private ArkTSExpression getNullCheckTarget(
            ArkTSExpression condition) {
        if (!(condition instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) condition;
        ArkTSExpression left = bin.getLeft();
        ArkTSExpression right = bin.getRight();
        if (isNullLiteralExpr(right)) {
            return left;
        }
        if (isNullLiteralExpr(left)) {
            return right;
        }
        return null;
    }

    private boolean isNullLiteralExpr(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return ((ArkTSExpression.LiteralExpression) expr).getKind()
                    == ArkTSExpression.LiteralExpression.LiteralKind.NULL;
        }
        return false;
    }

    private boolean expressionsMatch(ArkTSExpression a,
            ArkTSExpression b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.toArkTS().equals(b.toArkTS());
    }

    // --- Helpers ---

    static boolean isBinaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.ADD2
                || opcode == ArkOpcodesCompat.SUB2
                || opcode == ArkOpcodesCompat.MUL2
                || opcode == ArkOpcodesCompat.DIV2
                || opcode == ArkOpcodesCompat.MOD2
                || opcode == ArkOpcodesCompat.EQ
                || opcode == ArkOpcodesCompat.NOTEQ
                || opcode == ArkOpcodesCompat.LESS
                || opcode == ArkOpcodesCompat.LESSEQ
                || opcode == ArkOpcodesCompat.GREATER
                || opcode == ArkOpcodesCompat.GREATEREQ
                || opcode == ArkOpcodesCompat.SHL2
                || opcode == ArkOpcodesCompat.SHR2
                || opcode == ArkOpcodesCompat.ASHR2
                || opcode == ArkOpcodesCompat.AND2
                || opcode == ArkOpcodesCompat.OR2
                || opcode == ArkOpcodesCompat.XOR2
                || opcode == ArkOpcodesCompat.EXP
                || opcode == ArkOpcodesCompat.STRICTEQ
                || opcode == ArkOpcodesCompat.STRICTNOTEQ
                || opcode == ArkOpcodesCompat.INSTANCEOF
                || opcode == ArkOpcodesCompat.ISIN;
    }

    static String getBinaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.ADD2: return "+";
            case ArkOpcodesCompat.SUB2: return "-";
            case ArkOpcodesCompat.MUL2: return "*";
            case ArkOpcodesCompat.DIV2: return "/";
            case ArkOpcodesCompat.MOD2: return "%";
            case ArkOpcodesCompat.EQ: return "==";
            case ArkOpcodesCompat.NOTEQ: return "!=";
            case ArkOpcodesCompat.LESS: return "<";
            case ArkOpcodesCompat.LESSEQ: return "<=";
            case ArkOpcodesCompat.GREATER: return ">";
            case ArkOpcodesCompat.GREATEREQ: return ">=";
            case ArkOpcodesCompat.SHL2: return "<<";
            case ArkOpcodesCompat.SHR2: return ">>>";
            case ArkOpcodesCompat.ASHR2: return ">>";
            case ArkOpcodesCompat.AND2: return "&";
            case ArkOpcodesCompat.OR2: return "|";
            case ArkOpcodesCompat.XOR2: return "^";
            case ArkOpcodesCompat.EXP: return "**";
            case ArkOpcodesCompat.STRICTEQ: return "===";
            case ArkOpcodesCompat.STRICTNOTEQ: return "!==";
            case ArkOpcodesCompat.INSTANCEOF: return "instanceof";
            case ArkOpcodesCompat.ISIN: return "in";
            default: return "/* op */";
        }
    }

    static boolean isUnaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.NEG
                || opcode == ArkOpcodesCompat.NOT
                || opcode == ArkOpcodesCompat.TYPEOF;
    }

    static String getUnaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.NEG: return "-";
            case ArkOpcodesCompat.NOT: return "!";
            case ArkOpcodesCompat.TYPEOF: return "typeof";
            default: return "/* unary */";
        }
    }

    static boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3
                || opcode == ArkOpcodesCompat.CALLTHISRANGE
                || opcode == ArkOpcodesCompat.CALLRANGE;
    }

    static boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME
                || opcode == ArkOpcodesCompat.LDSUPERBYVALUE;
    }

    static boolean isPropertyStoreOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.STOBJBYNAME
                || opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYNAME
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYINDEX
                || opcode == ArkOpcodesCompat.STSUPERBYNAME
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUEWITHNAMESET
                || opcode == ArkOpcodesCompat.STOWNBYNAMEWITHNAMESET;
    }

    private static List<ArkTSExpression> createPlaceholderElements(
            int count) {
        List<ArkTSExpression> elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elements.add(new ArkTSExpression.LiteralExpression(
                    "/* element_" + i + " */",
                    ArkTSExpression.LiteralExpression
                            .LiteralKind.STRING));
        }
        return elements;
    }
}
