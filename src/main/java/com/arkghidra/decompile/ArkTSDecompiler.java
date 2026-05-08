package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
 *   <li>Detect structured control flow patterns (if/else, loops) from the CFG</li>
 *   <li>Walk blocks to build expressions (simulating the accumulator)</li>
 *   <li>Generate ArkTS statements with type inference annotations</li>
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
        ControlFlowGraph cfg = ctx.cfg;
        List<BasicBlock> blocks = cfg.getBlocks();

        if (blocks.size() == 1) {
            // Single block: linear code, no control flow
            return processBlockInstructions(blocks.get(0), ctx);
        }

        // Use CFG-based decompilation with structured control flow
        Set<BasicBlock> visited = new HashSet<>();
        return reconstructControlFlow(cfg, blocks, ctx, visited);
    }

    // --- Control flow reconstruction ---

    /**
     * Reconstructs structured control flow from the CFG.
     *
     * <p>Walks the CFG in order, detecting if/else, while, for, and do/while
     * patterns and producing structured ArkTS statements.
     *
     * @param cfg the control flow graph
     * @param blocks the list of basic blocks
     * @param ctx the decompilation context
     * @param visited set of already-processed blocks
     * @return the list of reconstructed statements
     */
    private List<ArkTSStatement> reconstructControlFlow(ControlFlowGraph cfg,
            List<BasicBlock> blocks, DecompilationContext ctx,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        for (BasicBlock block : blocks) {
            if (visited.contains(block)) {
                continue;
            }

            // Detect control flow pattern from this block
            ControlFlowPattern pattern = detectPattern(block, cfg, visited);

            switch (pattern.type) {
                case IF_ELSE:
                    visited.add(block);
                    stmts.addAll(processIfElse(block, pattern, ctx, cfg,
                            visited));
                    break;
                case IF_ONLY:
                    visited.add(block);
                    stmts.addAll(processIfOnly(block, pattern, ctx, cfg,
                            visited));
                    break;
                case WHILE_LOOP:
                    visited.add(block);
                    stmts.addAll(processWhileLoop(block, pattern, ctx, cfg,
                            visited));
                    break;
                case FOR_LOOP:
                    visited.add(block);
                    stmts.addAll(processForLoop(block, pattern, ctx, cfg,
                            visited));
                    break;
                case DO_WHILE:
                    visited.add(block);
                    stmts.addAll(processDoWhile(block, pattern, ctx, cfg,
                            visited));
                    break;
                case BREAK:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    stmts.add(new ArkTSStatement.BreakStatement());
                    break;
                case CONTINUE:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    stmts.add(new ArkTSStatement.ContinueStatement());
                    break;
                default:
                    // LINEAR or UNKNOWN: process instructions normally
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    break;
            }
        }

        return stmts;
    }

    /**
     * The type of control flow pattern detected.
     */
    private enum PatternType {
        LINEAR, IF_ONLY, IF_ELSE, WHILE_LOOP, FOR_LOOP, DO_WHILE, BREAK,
        CONTINUE, UNKNOWN
    }

    /**
     * Describes a detected control flow pattern.
     */
    private static class ControlFlowPattern {
        PatternType type;
        BasicBlock conditionBlock;
        BasicBlock trueBlock;
        BasicBlock falseBlock;
        BasicBlock mergeBlock;
        BasicBlock initBlock;
        BasicBlock updateBlock;

        ControlFlowPattern(PatternType type) {
            this.type = type;
        }
    }

    private ControlFlowPattern detectPattern(BasicBlock block,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<CFGEdge> successors = block.getSuccessors();

        // Check if this block ends with a conditional branch
        if (successors.size() == 2) {
            BasicBlock trueBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_TRUE);
            BasicBlock falseBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_FALSE);

            if (trueBranch == null || falseBranch == null) {
                // Fallback: use edge order
                trueBranch = cfg.getBlockAt(successors.get(0).getToOffset());
                falseBranch = cfg.getBlockAt(successors.get(1).getToOffset());
            }

            if (trueBranch == null || falseBranch == null) {
                return new ControlFlowPattern(PatternType.LINEAR);
            }

            // Check for while loop: one successor jumps back to this block
            // or an earlier offset
            if (trueBranch.getStartOffset() <= block.getStartOffset()
                    || falseBranch.getStartOffset() <= block
                            .getStartOffset()) {
                BasicBlock loopBody = trueBranch.getStartOffset() > block
                        .getStartOffset() ? trueBranch : falseBranch;
                ControlFlowPattern p = new ControlFlowPattern(
                        PatternType.WHILE_LOOP);
                p.conditionBlock = block;
                p.trueBlock = loopBody;
                return p;
            }

            // Check for if/else: both branches eventually merge
            BasicBlock merge = findMergeBlock(trueBranch, falseBranch, cfg);
            if (merge != null && merge != trueBranch
                    && merge != falseBranch) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.IF_ELSE);
                p.conditionBlock = block;
                p.trueBlock = trueBranch;
                p.falseBlock = falseBranch;
                p.mergeBlock = merge;
                return p;
            }

            // if-only pattern (one branch merges back, the other continues)
            if (trueBranch != falseBranch) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.IF_ONLY);
                p.conditionBlock = block;
                p.trueBlock = trueBranch;
                p.falseBlock = falseBranch;
                return p;
            }
        }

        // Check for do/while: the block has a back-edge from a successor
        if (successors.size() == 1) {
            BasicBlock succ = cfg.getBlockAt(
                    successors.get(0).getToOffset());
            if (succ != null && hasBackEdgeTo(succ, block, cfg)) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.DO_WHILE);
                p.conditionBlock = succ;
                p.trueBlock = block;
                return p;
            }
        }

        return new ControlFlowPattern(PatternType.LINEAR);
    }

    private List<ArkTSStatement> processIfElse(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process non-branch instructions in the condition block
        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        // Get the condition expression from the last instruction
        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Mark the true and false blocks as visited
        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Process the true branch
        List<ArkTSStatement> thenStmts = processBlockInstructions(
                pattern.trueBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        // Process the false branch
        List<ArkTSStatement> elseStmts = processBlockInstructions(
                pattern.falseBlock, ctx);
        ArkTSStatement elseBlock =
                new ArkTSStatement.BlockStatement(elseStmts);

        ArkTSStatement.IfStatement ifStmt =
                new ArkTSStatement.IfStatement(condition, thenBlock,
                        elseBlock);
        stmts.add(ifStmt);

        return stmts;
    }

    private List<ArkTSStatement> processIfOnly(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // For jeqz-style branches, the "true" path (branch taken) is the
        // falseBranch, and fall-through is the normal path. We need to negate
        // the condition for if-style output.
        // The true branch is where we go when condition is true
        BasicBlock branchBlock = pattern.trueBlock;
        visited.add(branchBlock);

        List<ArkTSStatement> thenStmts =
                processBlockInstructions(branchBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        // Negate the condition for jeqz-style (branch-on-zero/false)
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        ArkTSStatement.IfStatement ifStmt =
                new ArkTSStatement.IfStatement(effectiveCondition, thenBlock,
                        null);
        stmts.add(ifStmt);

        return stmts;
    }

    private List<ArkTSStatement> processWhileLoop(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Negate condition for jeqz-style (branch when false means loop body
        // is on the fall-through path)
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(effectiveCondition,
                        bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processForLoop(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process init block if present
        if (pattern.initBlock != null) {
            stmts.addAll(processBlockInstructions(pattern.initBlock, ctx));
            visited.add(pattern.initBlock);
        }

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Process update expression
        ArkTSExpression update = null;
        if (pattern.updateBlock != null) {
            List<ArkTSStatement> updateStmts =
                    processBlockInstructions(pattern.updateBlock, ctx);
            visited.add(pattern.updateBlock);
            if (!updateStmts.isEmpty() && updateStmts.get(
                    0) instanceof ArkTSStatement.ExpressionStatement) {
                update = ((ArkTSStatement.ExpressionStatement) updateStmts
                        .get(0)).getExpression();
            }
        }

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(condition, bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processDoWhile(BasicBlock block,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> bodyStmts = processBlockInstructions(block, ctx);

        ArkTSExpression condition =
                new ArkTSExpression.VariableExpression(ACC);
        if (pattern.conditionBlock != null) {
            visited.add(pattern.conditionBlock);
            List<ArkTSStatement> condStmts =
                    processBlockInstructionsExcluding(
                            pattern.conditionBlock, ctx,
                            pattern.conditionBlock.getLastInstruction());
            bodyStmts.addAll(condStmts);
            condition = getConditionExpression(
                    pattern.conditionBlock.getLastInstruction(), ctx);
            if (condition == null) {
                condition = new ArkTSExpression.VariableExpression(ACC);
            }
        }

        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSStatement.DoWhileStatement doWhile =
                new ArkTSStatement.DoWhileStatement(bodyBlock, condition);
        stmts.add(doWhile);

        return stmts;
    }

    // --- Condition extraction ---

    /**
     * Extracts the condition expression from a conditional branch instruction.
     *
     * @param branchInsn the branch instruction
     * @param ctx the decompilation context
     * @return the condition expression, or null
     */
    private ArkTSExpression getConditionExpression(ArkInstruction branchInsn,
            DecompilationContext ctx) {
        if (branchInsn == null) {
            return null;
        }
        int opcode = branchInsn.getOpcode();

        // For jeqz/jnez, the accumulator is the condition
        // We track the accumulator value from processing prior instructions
        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16) {
            // jeqz: branch if acc == 0 (falsy)
            return ctx.currentAccValue != null
                    ? ctx.currentAccValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }
        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16) {
            // jnez: branch if acc != 0 (truthy)
            return ctx.currentAccValue != null
                    ? ctx.currentAccValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }
        if (opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JNENULL_IMM8
                || opcode == ArkOpcodesCompat.JNENULL_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
        }
        if (opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
        }
        // For jeq/jne with register comparison
        if (opcode == ArkOpcodesCompat.JEQ_IMM8
                || opcode == ArkOpcodesCompat.JEQ_IMM16) {
            List<ArkOperand> ops = branchInsn.getOperands();
            int reg = (int) ops.get(0).getValue();
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.VariableExpression("v" + reg));
        }
        if (opcode == ArkOpcodesCompat.JNE_IMM8
                || opcode == ArkOpcodesCompat.JNE_IMM16) {
            List<ArkOperand> ops = branchInsn.getOperands();
            int reg = (int) ops.get(0).getValue();
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.VariableExpression("v" + reg));
        }
        if (opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "===",
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        }
        if (opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!==",
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        }

        return ctx.currentAccValue;
    }

    private boolean isBranchOnFalse(int opcode) {
        return opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16;
    }

    // --- Block instruction processing ---

    /**
     * Processes all instructions in a basic block into statements.
     *
     * @param block the basic block
     * @param ctx the decompilation context
     * @return the list of statements
     */
    private List<ArkTSStatement> processBlockInstructions(BasicBlock block,
            DecompilationContext ctx) {
        return processBlockInstructionsExcluding(block, ctx, null);
    }

    /**
     * Processes instructions in a block, optionally excluding the last one.
     *
     * @param block the basic block
     * @param ctx the decompilation context
     * @param excludeInsn the instruction to exclude (typically the branch), or
     *            null
     * @return the list of statements
     */
    private List<ArkTSStatement> processBlockInstructionsExcluding(
            BasicBlock block, DecompilationContext ctx,
            ArkInstruction excludeInsn) {

        List<ArkTSStatement> stmts = new ArrayList<>();
        Set<String> declaredVars = new HashSet<>();
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }

        ArkTSExpression accValue = null;
        TypeInference typeInf = new TypeInference();

        for (ArkInstruction insn : block.getInstructions()) {
            if (insn == excludeInsn) {
                continue;
            }

            int opcode = insn.getOpcode();

            // Skip NOP
            if (opcode == ArkOpcodesCompat.NOP) {
                continue;
            }

            // Skip unconditional jumps (handled by CFG)
            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                // Check if this is a break or continue
                if (isBreakJump(insn, block, ctx)) {
                    stmts.add(new ArkTSStatement.BreakStatement());
                    continue;
                }
                if (isContinueJump(insn, block, ctx)) {
                    stmts.add(new ArkTSStatement.ContinueStatement());
                    continue;
                }
                continue;
            }

            // Skip conditional branches (handled by CFG reconstruction)
            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                // Save accValue for condition extraction later
                ctx.currentAccValue = accValue;
                continue;
            }

            // Skip return (handled separately)
            if (opcode == ArkOpcodesCompat.RETURN) {
                stmts.add(new ArkTSStatement.ReturnStatement(accValue));
                accValue = null;
                continue;
            }
            if (opcode == ArkOpcodesCompat.RETURNUNDEFINED) {
                stmts.add(new ArkTSStatement.ReturnStatement(null));
                accValue = null;
                continue;
            }

            StatementResult result = processInstruction(
                    insn, ctx, accValue, declaredVars, typeInf);
            if (result != null) {
                if (result.statement != null) {
                    stmts.add(result.statement);
                }
                accValue = result.newAccValue;
            } else {
                accValue = null;
            }
        }

        // Update context accumulator for condition extraction
        ctx.currentAccValue = accValue;
        return stmts;
    }

    private boolean isBreakJump(ArkInstruction insn, BasicBlock block,
            DecompilationContext ctx) {
        // A jump that goes beyond the current loop body is a break
        // For now, we detect by checking if the jump target is beyond the
        // block structure
        return false;
    }

    private boolean isContinueJump(ArkInstruction insn, BasicBlock block,
            DecompilationContext ctx) {
        // A jump back to the loop header is a continue
        return false;
    }

    // --- CFG helpers ---

    private BasicBlock getSuccessorByType(List<CFGEdge> edges,
            EdgeType type) {
        for (CFGEdge edge : edges) {
            if (edge.getType() == type) {
                return ctx_getBlock(edge.getToOffset());
            }
        }
        return null;
    }

    private BasicBlock ctx_getBlock(int offset) {
        // This is a convenience that needs access to the CFG; used during
        // pattern detection
        return null;
    }

    private BasicBlock findMergeBlock(BasicBlock a, BasicBlock b,
            ControlFlowGraph cfg) {
        // Simple heuristic: find a block that both paths can reach
        // Use a forward walk from both blocks to find the first common block
        Set<Integer> reachableFromA = new LinkedHashSet<>();
        collectReachable(a, cfg, reachableFromA, 10);
        Set<Integer> reachableFromB = new LinkedHashSet<>();
        collectReachable(b, cfg, reachableFromB, 10);

        // Find first common block in reverse post-order
        for (Integer offset : reachableFromA) {
            if (reachableFromB.contains(offset)) {
                BasicBlock merge = cfg.getBlockAt(offset);
                if (merge != a && merge != b) {
                    return merge;
                }
            }
        }
        return null;
    }

    private void collectReachable(BasicBlock start, ControlFlowGraph cfg,
            Set<Integer> reachable, int maxDepth) {
        if (maxDepth <= 0 || start == null) {
            return;
        }
        reachable.add(start.getStartOffset());
        for (CFGEdge edge : start.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && !reachable.contains(succ.getStartOffset())) {
                collectReachable(succ, cfg, reachable, maxDepth - 1);
            }
        }
    }

    private boolean hasBackEdgeTo(BasicBlock from, BasicBlock target,
            ControlFlowGraph cfg) {
        for (CFGEdge edge : from.getSuccessors()) {
            if (edge.getToOffset() == target.getStartOffset()) {
                return true;
            }
        }
        // Check one level deeper
        for (CFGEdge edge : from.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && succ != from) {
                for (CFGEdge inner : succ.getSuccessors()) {
                    if (inner.getToOffset() == target.getStartOffset()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- Instruction processing ---

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
                        new ArkTSExpression.ObjectLiteralExpression(
                                Collections.emptyList()));
            case ArkOpcodesCompat.CREATEEMPTYARRAY:
                return new StatementResult(null,
                        new ArkTSExpression.ArrayLiteralExpression(
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
                            new ArkTSExpression.YieldExpression(value, false)),
                    null);
        }

        if (opcode == ArkOpcodesCompat.RESUMEGENERATOR) {
            // RESUMEGENERATOR has NONE format (no operands)
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
                    new ArkTSExpression.AwaitExpression(promise));
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
                // Infer type for type annotation
                String inferredType = typeInf.inferTypeForInstruction(insn);
                // Use the accValue-producing instruction's type instead
                // STA itself doesn't produce a type; the accValue does
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
            int methodIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "func_" + methodIdx);
            return new StatementResult(null, expr);
        }

        // --- STARRAYSPREAD (spread into array) ---
        if (opcode == ArkOpcodesCompat.STARRAYSPREAD) {
            // starrayspread dst, src
            int dstReg = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(1).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSExpression.VariableExpression("v" + srcReg);
            ArkTSExpression spread =
                    new ArkTSExpression.SpreadExpression(spreadArg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(spread),
                    spread);
        }

        // --- Fallback: emit a comment ---
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* " + insn.getMnemonic() + " */")),
                null);
    }

    /**
     * Attempts to determine the type of an accumulator expression.
     *
     * @param expr the expression
     * @param typeInf the type inference engine
     * @return the inferred type name, or null
     */
    private String getAccType(ArkTSExpression expr, TypeInference typeInf) {
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
        if (expr instanceof ArkTSExpression.ArrayLiteralExpression) {
            return "Array<unknown>";
        }
        if (expr instanceof ArkTSExpression.ObjectLiteralExpression) {
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

    private ArkTSExpression translateCall(ArkInstruction insn,
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
        boolean isAsync;
        ArkTSExpression currentAccValue;

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
            this.isAsync = false;
            this.currentAccValue = null;
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
