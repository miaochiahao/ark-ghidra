package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;

/**
 * Processes branch-related control flow patterns: if/else, if-only,
 * ternary expressions, and short-circuit (&&/||) evaluation.
 *
 * <p>Called from {@link ControlFlowReconstructor} for
 * {@code IF_ELSE}, {@code IF_ONLY}, {@code TERNARY},
 * {@code SHORT_CIRCUIT_AND}, and {@code SHORT_CIRCUIT_OR} patterns.
 */
class BranchProcessor {

    private final ControlFlowReconstructor reconstructor;

    BranchProcessor(ControlFlowReconstructor reconstructor) {
        this.reconstructor = reconstructor;
    }

    // --- Ternary detection ---

    ControlFlowReconstructor.ControlFlowPattern detectTernaryPattern(
            BasicBlock condBlock, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg) {

        ArkInstruction trueLast = trueBranch.getLastInstruction();
        if (trueLast == null
                || !ArkOpcodesCompat.isUnconditionalJump(
                        trueLast.getOpcode())) {
            return null;
        }

        int mergeOffset = ControlFlowGraph.getJumpTargetPublic(trueLast);
        BasicBlock mergeBlock = cfg.getBlockAt(mergeOffset);

        if (mergeBlock == null) {
            return null;
        }

        ArkInstruction falseLast = falseBranch.getLastInstruction();
        if (falseLast == null) {
            return null;
        }
        int falseEnd = falseLast.getNextOffset();
        boolean falseFlowsToMerge = falseEnd == mergeOffset
                || (mergeOffset > falseBranch.getStartOffset()
                        && mergeOffset <= falseEnd + 5);

        if (!falseFlowsToMerge) {
            if (!ArkOpcodesCompat.isUnconditionalJump(
                    falseLast.getOpcode())) {
                return null;
            }
            int falseJmpTarget =
                    ControlFlowGraph.getJumpTargetPublic(falseLast);
            if (falseJmpTarget != mergeOffset) {
                return null;
            }
        }

        String targetVar = null;

        for (ArkInstruction insn : trueBranch.getInstructions()) {
            if (insn == trueLast) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                targetVar = "v" + insn.getOperands().get(0).getValue();
            }
        }

        if (targetVar == null) {
            return null;
        }

        boolean hasMatchingSta = false;
        for (ArkInstruction insn : falseBranch.getInstructions()) {
            if (insn == falseLast
                    && !ArkOpcodesCompat.isUnconditionalJump(
                            falseLast.getOpcode())) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                String falseVar =
                        "v" + insn.getOperands().get(0).getValue();
                if (falseVar.equals(targetVar)) {
                    hasMatchingSta = true;
                }
            }
        }

        if (!hasMatchingSta) {
            return null;
        }

        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.TERNARY);
        p.conditionBlock = condBlock;
        p.trueBlock = trueBranch;
        p.falseBlock = falseBranch;
        p.mergeBlock = mergeBlock;
        p.ternaryTargetVar = targetVar;
        return p;
    }

    // --- Short-circuit detection ---

    ControlFlowReconstructor.ControlFlowPattern detectShortCircuitPattern(
            BasicBlock block, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        ArkInstruction lastInsn = block.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();

        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode()
                            == ArkOpcodesCompat.JEQZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JEQZ_IMM16
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JSTRICTEQZ_IMM16)) {
                int target2 = ControlFlowGraph
                        .getJumpTargetPublic(nextLast);
                if (target1 == target2) {
                    ControlFlowReconstructor.ControlFlowPattern p =
                            new ControlFlowReconstructor.ControlFlowPattern(
                                    ControlFlowReconstructor.PatternType
                                            .SHORT_CIRCUIT_AND);
                    p.conditionBlock = block;
                    p.trueBlock = falseBranch;
                    p.falseBlock = trueBranch;
                    p.mergeBlock = cfg.getBlockAt(target1);
                    return p;
                }
            }
        }

        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode()
                            == ArkOpcodesCompat.JNEZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JNEZ_IMM16
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JNSTRICTEQZ_IMM16)) {
                int target2 = ControlFlowGraph
                        .getJumpTargetPublic(nextLast);
                if (target1 == target2) {
                    ControlFlowReconstructor.ControlFlowPattern p =
                            new ControlFlowReconstructor.ControlFlowPattern(
                                    ControlFlowReconstructor.PatternType
                                            .SHORT_CIRCUIT_OR);
                    p.conditionBlock = block;
                    p.trueBlock = falseBranch;
                    p.falseBlock = trueBranch;
                    p.mergeBlock = cfg.getBlockAt(target1);
                    return p;
                }
            }
        }

        return null;
    }

    // --- If/else processing ---

    List<ArkTSStatement> processIfElse(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        List<ArkTSStatement> thenStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        List<ArkTSStatement> elseStmts =
                reconstructor.processBlockInstructions(
                        pattern.falseBlock, ctx);
        ArkTSStatement elseBlock =
                new ArkTSStatement.BlockStatement(elseStmts);

        ArkTSControlFlow.IfStatement ifStmt =
                new ArkTSControlFlow.IfStatement(condition, thenBlock,
                        elseBlock);
        stmts.add(ifStmt);

        return stmts;
    }

    List<ArkTSStatement> processIfOnly(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        BasicBlock branchBlock = pattern.trueBlock;
        visited.add(branchBlock);

        List<ArkTSStatement> thenStmts =
                reconstructor.processBlockInstructions(branchBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression(
                            "!", condition, true);
        }

        ArkTSControlFlow.IfStatement ifStmt =
                new ArkTSControlFlow.IfStatement(effectiveCondition,
                        thenBlock, null);
        stmts.add(ifStmt);

        return stmts;
    }

    // --- Ternary processing ---

    List<ArkTSStatement> processTernary(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            effectiveCondition = condition;
        }

        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        ArkTSExpression trueValue =
                extractBlockValue(pattern.trueBlock, ctx);
        ArkTSExpression falseValue =
                extractBlockValue(pattern.falseBlock, ctx);

        if (trueValue != null && falseValue != null) {
            ArkTSExpression ternaryExpr =
                    new ArkTSAccessExpressions.ConditionalExpression(
                            effectiveCondition, trueValue, falseValue);
            String targetVar = pattern.ternaryTargetVar;
            ArkTSStatement decl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", targetVar, null, ternaryExpr);
            stmts.add(decl);
        } else {
            List<ArkTSStatement> thenStmts =
                    reconstructor.processBlockInstructions(
                            pattern.trueBlock, ctx);
            List<ArkTSStatement> elseStmts =
                    reconstructor.processBlockInstructions(
                            pattern.falseBlock, ctx);
            ArkTSStatement thenBlock =
                    new ArkTSStatement.BlockStatement(thenStmts);
            ArkTSStatement elseBlock =
                    new ArkTSStatement.BlockStatement(elseStmts);
            stmts.add(new ArkTSControlFlow.IfStatement(
                    effectiveCondition, thenBlock, elseBlock));
        }

        return stmts;
    }

    private ArkTSExpression extractBlockValue(BasicBlock block,
            DecompilationContext ctx) {
        return reconstructor.extractBlockValue(block, ctx);
    }

    // --- Short-circuit processing ---

    List<ArkTSStatement> processShortCircuitAnd(
            BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression leftCond =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts =
                reconstructor.processBlockInstructionsExcluding(
                        secondCondBlock, ctx,
                        secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond =
                reconstructor.getConditionExpression(
                        secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        ArkTSExpression combined =
                new ArkTSExpression.BinaryExpression(
                        leftCond, "&&", rightCond);

        stmts.add(new ArkTSStatement.ExpressionStatement(combined));

        return stmts;
    }

    List<ArkTSStatement> processShortCircuitOr(
            BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression leftCond =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts =
                reconstructor.processBlockInstructionsExcluding(
                        secondCondBlock, ctx,
                        secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond =
                reconstructor.getConditionExpression(
                        secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        ArkTSExpression combined =
                new ArkTSExpression.BinaryExpression(
                        leftCond, "||", rightCond);

        stmts.add(new ArkTSStatement.ExpressionStatement(combined));

        return stmts;
    }
}
