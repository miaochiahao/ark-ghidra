package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

/**
 * Decompiles Ark bytecode methods into ArkTS source code.
 *
 * <p>The decompiler works in several phases:
 * <ol>
 *   <li>Disassemble the method bytecode into instructions</li>
 *   <li>Build a control flow graph (CFG) from the instructions</li>
 *   <li>Walk instructions to build an expression stack (simulating the accumulator)</li>
 *   <li>Generate ArkTS statements from expressions and control flow</li>
 *   <li>Pretty-print the result</li>
 * </ol>
 */
public class ArkTSDecompiler {

    private static final String ACC = "acc";

    /**
     * Decompiles a method to ArkTS source code.
     *
     * @param method the method to decompile
     * @param code the method's code section
     * @param abcFile the parent ABC file (for resolving string/constant references)
     * @return the decompiled ArkTS source code
     */
    public String decompileMethod(AbcMethod method, AbcCode code,
            AbcFile abcFile) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return buildEmptyMethod(method, null);
        }

        ArkDisassembler disasm = new ArkDisassembler();
        List<ArkInstruction> instructions = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        if (instructions.isEmpty()) {
            return buildEmptyMethod(method, null);
        }

        ControlFlowGraph cfg = ControlFlowGraph.build(instructions,
                code.getTryBlocks());

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        List<ArkTSStatement> bodyStmts = generateStatements(ctx);

        return buildMethodSource(method, proto, code, bodyStmts);
    }

    /**
     * Decompiles just the instructions (without ABC metadata) for testing.
     *
     * @param instructions the decoded instructions
     * @return the decompiled ArkTS source
     */
    public String decompileInstructions(List<ArkInstruction> instructions) {
        if (instructions.isEmpty()) {
            return "";
        }
        ControlFlowGraph cfg = ControlFlowGraph.build(instructions);
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "f", 0, 0, 0);
        DecompilationContext ctx = new DecompilationContext(
                method, code, null, null, cfg, instructions);

        List<ArkTSStatement> stmts = generateStatements(ctx);
        StringBuilder sb = new StringBuilder();
        for (ArkTSStatement stmt : stmts) {
            sb.append(stmt.toArkTS(0)).append("\n");
        }
        return sb.toString().trim();
    }

    // --- Statement generation ---

    private List<ArkTSStatement> generateStatements(DecompilationContext ctx) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkInstruction> instructions = ctx.instructions;

        // Track which registers have been declared
        Set<String> declaredVars = new HashSet<>();
        // Declare parameter variables
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }

        ArkTSExpression accValue = null;
        int i = 0;
        while (i < instructions.size()) {
            ArkInstruction insn = instructions.get(i);
            int opcode = insn.getOpcode();

            // Skip NOP
            if (opcode == ArkOpcodesCompat.NOP) {
                i++;
                continue;
            }

            // Handle instruction categories
            StatementResult result = processInstruction(
                    insn, ctx, accValue, declaredVars, i);
            if (result != null) {
                if (result.statement != null) {
                    stmts.add(result.statement);
                }
                accValue = result.newAccValue;
            } else {
                accValue = null;
            }

            i++;
        }

        return stmts;
    }

    private static class StatementResult {
        ArkTSStatement statement;
        ArkTSExpression newAccValue;

        StatementResult(ArkTSStatement statement,
                ArkTSExpression newAccValue) {
            this.statement = statement;
            this.newAccValue = newAccValue;
        }
    }

    private StatementResult processInstruction(ArkInstruction insn,
            DecompilationContext ctx, ArkTSExpression accValue,
            Set<String> declaredVars, int index) {
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
                // Use string table index as placeholder if no ABC file
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
                        new ArkTSExpression.ObjectLiteralExpression(
                                Collections.emptyList()));
            case ArkOpcodesCompat.CREATEEMPTYARRAY:
                return new StatementResult(null,
                        new ArkTSExpression.ArrayLiteralExpression(
                                Collections.emptyList()));
            default:
                break;
        }

        // --- Store from accumulator ---
        if (opcode == ArkOpcodesCompat.STA) {
            int reg = (int) operands.get(0).getValue();
            String varName = "v" + reg;
            if (accValue != null) {
                if (!declaredVars.contains(varName)
                        && !(reg < ctx.numArgs)) {
                    declaredVars.add(varName);
                    ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration(
                            "let", varName, null, accValue);
                    return new StatementResult(stmt, accValue);
                }
                ArkTSStatement stmt = new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(varName),
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
            if (!declaredVars.contains(dstName)
                    && !(dstReg < ctx.numArgs)) {
                declaredVars.add(dstName);
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", dstName, null, srcExpr),
                        null);
            }
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.VariableExpression(dstName),
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

        // --- Return ---
        if (opcode == ArkOpcodesCompat.RETURN) {
            ArkTSExpression val = accValue;
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(val), null);
        }
        if (opcode == ArkOpcodesCompat.RETURNUNDEFINED) {
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(null), null);
        }

        // --- Conditional branch (if) ---
        if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
            // For a first pass, emit a comment about the branch
            // The CFG will be used for more sophisticated analysis later
            return null;
        }

        // --- Unconditional jump ---
        if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
            // Jumps are handled by CFG analysis
            return null;
        }

        // --- Function calls ---
        if (isCallOpcode(opcode)) {
            ArkTSExpression callExpr = translateCall(insn, accValue, ctx);
            return new StatementResult(null, callExpr);
        }

        // --- Property access (load) ---
        if (isPropertyLoadOpcode(opcode)) {
            ArkTSExpression expr = translatePropertyLoad(insn, accValue, ctx);
            return new StatementResult(null, expr);
        }

        // --- Property store ---
        if (isPropertyStoreOpcode(opcode)) {
            ArkTSExpression expr = translatePropertyStore(insn, accValue, ctx);
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
            // The constructor is in acc; args are in consecutive registers
            List<ArkTSExpression> args = new ArrayList<>();
            int firstReg = (int) operands.get(operands.size() - 1).getValue();
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
            // In a first pass, emit a placeholder
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "func_" + operands.get(1).getValue());
            return new StatementResult(null, expr);
        }

        // --- Fallback: emit a comment ---
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* " + insn.getMnemonic() + " */")),
                null);
    }

    // --- Call translation ---

    private ArkTSExpression translateCall(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression callee;
        List<ArkTSExpression> args = new ArrayList<>();

        switch (opcode) {
            case ArkOpcodesCompat.CALLARG0:
                // callarg0 methodIdx -- calls with 0 args, callee was in acc before
                // Actually: callarg0 puts acc as function, calls with no args
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
            case ArkOpcodesCompat.CALLARG1:
                // callarg1 methodIdx, v0 -- calls with 1 arg
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
                // callthis0 methodIdx, this_reg -- this is in v[operand]
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
            default:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
        }

        return new ArkTSExpression.CallExpression(callee, args);
    }

    // --- Property access translation ---

    private ArkTSExpression translatePropertyLoad(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else {
            obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }

        if (opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            int reg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new ArkTSExpression.MemberExpression(obj, prop, true);
        }

        // Name-based access
        String propName = ctx.resolveString((int) operands.get(1).getValue());
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.MemberExpression(obj, prop, false);
    }

    private ArkTSExpression translatePropertyStore(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else {
            // The object register is typically the last operand for stores
            int objReg = (int) operands.get(operands.size() - 1).getValue();
            obj = new ArkTSExpression.VariableExpression("v" + objReg);
        }

        ArkTSExpression prop;
        if (opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUE) {
            int keyReg = (int) operands.get(operands.size() - 2).getValue();
            prop = new ArkTSExpression.VariableExpression("v" + keyReg);
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, prop, true),
                    accValue != null ? accValue
                            : new ArkTSExpression.VariableExpression(ACC));
        }

        // Name-based
        String propName = ctx.resolveString((int) operands.get(1).getValue());
        prop = new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.AssignExpression(
                new ArkTSExpression.MemberExpression(obj, prop, false),
                accValue != null ? accValue
                        : new ArkTSExpression.VariableExpression(ACC));
    }

    // --- Helpers ---

    private boolean isBinaryOp(int opcode) {
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

    private String getBinaryOperator(int opcode) {
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

    private boolean isUnaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.NEG
                || opcode == ArkOpcodesCompat.NOT
                || opcode == ArkOpcodesCompat.TYPEOF;
    }

    private String getUnaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.NEG: return "-";
            case ArkOpcodesCompat.NOT: return "!";
            case ArkOpcodesCompat.TYPEOF: return "typeof";
            default: return "/* unary */";
        }
    }

    private boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3;
    }

    private boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME;
    }

    private boolean isPropertyStoreOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.STOBJBYNAME
                || opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYNAME
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYINDEX
                || opcode == ArkOpcodesCompat.STSUPERBYNAME;
    }

    private AbcProto resolveProto(AbcMethod method, AbcFile abcFile) {
        if (abcFile == null || method.getProtoIdx() < 0) {
            return null;
        }
        List<AbcProto> protos = abcFile.getProtos();
        int idx = method.getProtoIdx();
        if (idx < protos.size()) {
            return protos.get(idx);
        }
        return null;
    }

    private String buildEmptyMethod(AbcMethod method, AbcProto proto) {
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 0);
        return sig + " { }";
    }

    private String buildMethodSource(AbcMethod method, AbcProto proto,
            AbcCode code, List<ArkTSStatement> bodyStmts) {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto, code.getNumArgs());
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body = new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSStatement.FunctionDeclaration func =
                new ArkTSStatement.FunctionDeclaration(
                        method.getName(), params, returnType, body);
        return func.toArkTS(0);
    }

    // --- Context ---

    /**
     * Holds shared state during decompilation of a single method.
     */
    private static class DecompilationContext {
        final AbcMethod method;
        final AbcCode code;
        final AbcProto proto;
        final AbcFile abcFile;
        final ControlFlowGraph cfg;
        final List<ArkInstruction> instructions;
        final int numArgs;

        DecompilationContext(AbcMethod method, AbcCode code,
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
        }

        /**
         * Resolves a string table index to a string value.
         * Returns a placeholder if the string cannot be resolved.
         *
         * @param stringIdx the string table index
         * @return the resolved string or a placeholder
         */
        String resolveString(int stringIdx) {
            return "str_" + stringIdx;
        }
    }
}
