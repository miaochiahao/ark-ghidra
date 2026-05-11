package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Processes branch-related control flow patterns: if/else, if-only,
 * ternary expressions, short-circuit (&&/||) evaluation, optional
 * chaining, non-null assertions, and guard clauses.
 *
 * <p>Called from {@link ControlFlowReconstructor} for
 * {@code IF_ELSE}, {@code IF_ONLY}, {@code TERNARY},
 * {@code SHORT_CIRCUIT_AND}, and {@code SHORT_CIRCUIT_OR} patterns.
 *
 * <p>Also provides detection for optional chaining patterns
 * ({@code obj?.prop}) and non-null assertions ({@code expr!}).
 */
class BranchProcessor {

    private final ControlFlowReconstructor reconstructor;

    BranchProcessor(ControlFlowReconstructor reconstructor) {
        this.reconstructor = reconstructor;
    }

    // --- Ternary detection ---

    ControlFlowReconstructor.ControlFlowPattern detectTernaryPattern(
            BasicBlock condBlock, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg,
            DecompilationContext ctx) {

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
                targetVar = ctx.resolveRegisterName(
                        (int) insn.getOperands().get(0).getValue());
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
                String falseVar = ctx.resolveRegisterName(
                        (int) insn.getOperands().get(0).getValue());
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

        if (isAndConditionOpcode(opcode)) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && isAndConditionOpcode(
                            nextLast.getOpcode())) {
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

        if (isOrConditionOpcode(opcode)) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && isOrConditionOpcode(
                            nextLast.getOpcode())) {
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

        // For "branch on false" opcodes (jeqz, etc.), the trueBranch
        // edge is taken when the condition is falsy. In that case the
        // then-block is the falseBranch (fall-through) and the
        // else-block is the trueBranch (jump target).
        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        BasicBlock thenBlock;
        BasicBlock elseBlock;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            thenBlock = pattern.falseBlock;
            elseBlock = pattern.trueBlock;
        } else {
            thenBlock = pattern.trueBlock;
            elseBlock = pattern.falseBlock;
        }

        // Mark merge block as visited early to prevent it from being
        // collected into branch sub-graphs. Track if it was already
        // visited by a parent if-else chain to avoid duplicate
        // processing of the merge block in else-if chains.
        boolean mergeAlreadyVisited = pattern.mergeBlock != null
                && visited.contains(pattern.mergeBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Save accumulator state after condition block so both
        // branches start from the same state
        ArkTSExpression savedAccValue = ctx.currentAccValue;

        // Collect all then-branch blocks and process recursively
        // for multi-block then bodies (e.g., then-block with
        // nested conditionals, assignments, returns)
        BasicBlock thenStopBlock = pattern.mergeBlock != null
                ? pattern.mergeBlock : elseBlock;
        List<BasicBlock> thenBlocks = collectIfBranchBlocks(
                thenBlock, thenStopBlock, cfg, visited);
        thenBlocks.forEach(visited::remove);
        List<ArkTSStatement> thenStmts =
                reconstructor.reconstructSubGraph(
                        cfg, thenBlocks, ctx, visited);
        ArkTSStatement thenStmt =
                new ArkTSStatement.BlockStatement(thenStmts);

        // Restore accumulator for else branch so it also starts
        // from the condition block's state
        ctx.currentAccValue = savedAccValue;

        // Process else-block recursively using collectIfBranchBlocks
        // to handle multi-block else bodies and else-if chains.
        BasicBlock elseStopBlock = pattern.mergeBlock != null
                ? pattern.mergeBlock : thenBlock;
        List<BasicBlock> elseBlocks = collectIfBranchBlocks(
                elseBlock, elseStopBlock, cfg, visited);
        elseBlocks.forEach(visited::remove);
        List<ArkTSStatement> elseStmts =
                reconstructor.reconstructSubGraph(
                        cfg, elseBlocks, ctx, visited);
        ArkTSStatement elseStmt =
                new ArkTSStatement.BlockStatement(elseStmts);

        ArkTSControlFlow.IfStatement ifStmt =
                new ArkTSControlFlow.IfStatement(condition, thenStmt,
                        elseStmt);
        stmts.add(ifStmt);

        // Process the merge block (code after both branches converge).
        // Only process if this if-else is the owner (first to visit it).
        // Nested if-else in else-if chains share the same merge block
        // and should not duplicate it.
        if (pattern.mergeBlock != null && !mergeAlreadyVisited) {
            List<ArkTSStatement> mergeStmts =
                    reconstructor.processBlockInstructions(
                            pattern.mergeBlock, ctx);
            stmts.addAll(mergeStmts);
        }

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

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();

        // --- Optional chaining detection ---
        // After processing pre-branch instructions, check if acc holds
        // a MemberExpression and the branch is a null check
        List<ArkTSStatement> optChainResult =
                tryProcessOptionalChain(condBlock, pattern, ctx,
                        lastOpcode, condition, visited);
        if (optChainResult != null) {
            stmts.addAll(optChainResult);
            return stmts;
        }

        // --- Guard clause detection ---
        // If the branch block is a simple return, emit as guard clause
        BasicBlock branchBlock = pattern.trueBlock;
        BasicBlock otherBlock = pattern.falseBlock;
        List<ArkTSStatement> guardResult =
                tryProcessGuardClause(condBlock, pattern, ctx,
                        lastOpcode, condition, branchBlock,
                        otherBlock, visited);
        if (guardResult != null) {
            stmts.addAll(guardResult);
            return stmts;
        }

        // --- Standard if-only processing ---
        // Mark the fall-through as live so isDeadCode doesn't skip it
        reconstructor.addLiveContinuation(otherBlock);

        List<BasicBlock> thenBlocks = collectIfBranchBlocks(
                branchBlock, otherBlock, cfg, visited);
        List<ArkTSStatement> thenStmts =
                reconstructor.reconstructSubGraph(
                        cfg, thenBlocks, ctx, visited);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

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

    /**
     * Tries to process an optional chaining pattern in an IF_ONLY block.
     *
     * <p>Checks if the branch is a null-check and the accumulator holds
     * a MemberExpression (from a property load). If so, converts the
     * if-only into an optional chain expression.
     *
     * @return statements with optional chaining, or null if not detected
     */
    private List<ArkTSStatement> tryProcessOptionalChain(
            BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, int lastOpcode,
            ArkTSExpression condition, Set<BasicBlock> visited) {

        if (!isNullCheckBranch(lastOpcode)) {
            return null;
        }

        ArkTSExpression accValue = ctx.currentAccValue;
        if (accValue == null) {
            return null;
        }

        if (!(accValue instanceof ArkTSExpression.MemberExpression)) {
            return null;
        }

        // Determine which block is the null path vs non-null path
        BasicBlock nullPathBlock;
        BasicBlock nonNullPathBlock;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            nullPathBlock = pattern.trueBlock;
            nonNullPathBlock = pattern.falseBlock;
        } else {
            nullPathBlock = pattern.falseBlock;
            nonNullPathBlock = pattern.trueBlock;
        }

        // Verify null path assigns null/undefined
        if (!isSimpleNullAssignment(nullPathBlock, ctx)) {
            return null;
        }

        visited.add(nullPathBlock);
        visited.add(nonNullPathBlock);

        ArkTSExpression optionalExpr =
                tryConvertToOptionalChain(accValue);

        // Find target variable from non-null path
        ArkTSExpression savedAcc = ctx.currentAccValue;
        ctx.currentAccValue = optionalExpr;

        List<ArkTSStatement> nonNullStmts =
                reconstructor.processBlockInstructions(
                        nonNullPathBlock, ctx);
        String targetVar = findStaTarget(nonNullStmts);

        List<ArkTSStatement> result = new ArrayList<>();
        if (targetVar != null) {
            result.add(new ArkTSStatement.VariableDeclaration(
                    "let", targetVar, null, optionalExpr));
            for (ArkTSStatement s : nonNullStmts) {
                if (!(s instanceof ArkTSStatement.VariableDeclaration)) {
                    result.add(s);
                }
            }
        } else {
            result.add(new ArkTSStatement.ExpressionStatement(optionalExpr));
            result.addAll(nonNullStmts);
        }

        ctx.currentAccValue = savedAcc;
        return result;
    }

    /**
     * Tries to process a guard clause pattern in an IF_ONLY block.
     *
     * <p>A guard clause is when the branch block contains only a simple
     * return statement (early exit). Emits as: if (cond) return value;
     *
     * @return guard clause statements, or null if not a guard pattern
     */
    private List<ArkTSStatement> tryProcessGuardClause(
            BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, int lastOpcode,
            ArkTSExpression condition, BasicBlock branchBlock,
            BasicBlock otherBlock,
            Set<BasicBlock> visited) {

        if (!isGuardReturnBlock(branchBlock)) {
            return null;
        }

        // If the other branch also has significant code (not just a return),
        // this is not a guard clause — it's a normal if-then (e.g., default
        // parameter assignment). Guard clauses require the fall-through to
        // be the main continuation, not an alternative return path.
        if (otherBlock != null && !isGuardReturnBlock(otherBlock)) {
            return null;
        }

        visited.add(branchBlock);

        List<ArkTSStatement> guardStmts =
                reconstructor.processBlockInstructions(
                        branchBlock, ctx);
        ArkTSStatement guardBody =
                new ArkTSStatement.BlockStatement(guardStmts);

        // Determine effective condition for the guard
        ArkTSExpression guardCondition;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            guardCondition = condition;
        } else {
            guardCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        List<ArkTSStatement> result = new ArrayList<>();
        result.add(new ArkTSControlFlow.IfStatement(
                guardCondition, guardBody, null));
        return result;
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
            ternaryExpr =
                    OperatorHandler.simplifyRedundantTernary(ternaryExpr);
            ternaryExpr =
                    OperatorHandler.simplifyLogicalTernary(ternaryExpr);
            ternaryExpr =
                    OperatorHandler.simplifyTernaryToOr(ternaryExpr);
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
        if (isAndNeedsNegation(
                condBlock.getLastInstruction().getOpcode())) {
            leftCond = negateConditionExpression(leftCond);
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
        if (isAndNeedsNegation(
                secondCondBlock.getLastInstruction().getOpcode())) {
            rightCond = negateConditionExpression(rightCond);
        }

        ArkTSExpression combined =
                new ArkTSExpression.BinaryExpression(
                        leftCond, "&&", rightCond);

        BasicBlock thenBody = findShortCircuitThenBody(
                secondCondBlock, pattern.mergeBlock, cfg);
        if (thenBody != null && !visited.contains(thenBody)) {
            visited.add(pattern.falseBlock);
            if (pattern.mergeBlock != null) {
                visited.add(pattern.mergeBlock);
            }
            List<BasicBlock> thenBlocks = collectIfBranchBlocks(
                    thenBody, pattern.mergeBlock, cfg, visited);
            thenBlocks.forEach(visited::remove);
            List<ArkTSStatement> thenStmts =
                    reconstructor.reconstructSubGraph(
                            cfg, thenBlocks, ctx, visited);
            ArkTSStatement body =
                    new ArkTSStatement.BlockStatement(thenStmts);
            stmts.add(new ArkTSControlFlow.IfStatement(combined, body, null));
        } else {
            visited.add(pattern.falseBlock);
            if (pattern.mergeBlock != null) {
                visited.add(pattern.mergeBlock);
            }
            stmts.add(new ArkTSStatement.ExpressionStatement(combined));
        }

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

        ArkTSExpression combined =
                new ArkTSExpression.BinaryExpression(
                        leftCond, "||", rightCond);

        BasicBlock thenBody = findShortCircuitThenBody(
                secondCondBlock, pattern.mergeBlock, cfg);
        if (thenBody != null && !visited.contains(thenBody)) {
            visited.add(pattern.falseBlock);
            if (pattern.mergeBlock != null) {
                visited.add(pattern.mergeBlock);
            }
            List<BasicBlock> thenBlocks = collectIfBranchBlocks(
                    thenBody, pattern.mergeBlock, cfg, visited);
            thenBlocks.forEach(visited::remove);
            List<ArkTSStatement> thenStmts =
                    reconstructor.reconstructSubGraph(
                            cfg, thenBlocks, ctx, visited);
            ArkTSStatement body =
                    new ArkTSStatement.BlockStatement(thenStmts);
            stmts.add(new ArkTSControlFlow.IfStatement(combined, body, null));
        } else {
            visited.add(pattern.falseBlock);
            if (pattern.mergeBlock != null) {
                visited.add(pattern.mergeBlock);
            }
            stmts.add(new ArkTSStatement.ExpressionStatement(combined));
        }

        return stmts;
    }

    // --- Short-circuit then-body detection ---

    /**
     * Finds the "then" body block after a short-circuit condition pair.
     *
     * <p>For {@code if (a && b) { body } }, after the second jeqz,
     * the fall-through block (CONDITIONAL_FALSE successor of
     * secondCondBlock) is the then-body, and the jump target is the
     * merge point. If the fall-through differs from the merge, it
     * contains the guarded code.
     *
     * @param secondCondBlock the block containing the second condition
     * @param mergeBlock the merge block (jump target of both conditions)
     * @param cfg the control flow graph
     * @return the then-body block, or null if none found
     */
    private BasicBlock findShortCircuitThenBody(BasicBlock secondCondBlock,
            BasicBlock mergeBlock, ControlFlowGraph cfg) {
        List<CFGEdge> succs = secondCondBlock.getSuccessors();
        for (CFGEdge edge : succs) {
            if (edge.getType() == EdgeType.CONDITIONAL_FALSE) {
                BasicBlock fallThrough =
                        cfg.getBlockAt(edge.getToOffset());
                if (fallThrough != null
                        && fallThrough != mergeBlock
                        && fallThrough != secondCondBlock) {
                    return fallThrough;
                }
            }
        }
        return null;
    }

    // --- Logical compound assignment detection ---

    /**
     * Detects logical compound assignment patterns (&&=, ||=, ??=).
     *
     * <p>For {@code x &&= expr}: condBlock loads x, checks falsy
     * (jeqz), jumps to merge. Fall-through computes expr and stores to x.
     *
     * <p>For {@code x ||= expr}: condBlock loads x, checks truthy
     * (jnez), jumps to merge. Fall-through computes expr and stores to x.
     *
     * <p>For {@code x ??= expr}: condBlock loads x, checks
     * null/undefined (jeqnull, jstricteqnull, jequndefined,
     * jstrictequndefined), jumps to merge. Fall-through computes expr
     * and stores to x.
     *
     * @param condBlock the condition block
     * @param trueBranch the branch taken on true (jump target)
     * @param falseBranch the fall-through branch
     * @param cfg the control flow graph
     * @param ctx the decompilation context
     * @return a LOGICAL_ASSIGN pattern, or null if not detected
     */
    ControlFlowReconstructor.ControlFlowPattern detectLogicalAssignPattern(
            BasicBlock condBlock, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg,
            DecompilationContext ctx) {

        ArkInstruction lastInsn = condBlock.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();

        String op = null;
        // &&= : jeqz (jump if falsy) - skip branch is trueBranch
        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16) {
            op = "&&=";
        }
        // ||= : jnez (jump if truthy) - skip branch is trueBranch
        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16) {
            op = "||=";
        }
        // ??= : jeqnull/jstricteqnull/jequndefined - skip is trueBranch
        if (opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16) {
            op = "??=";
        }
        if (op == null) {
            return null;
        }

        if (!isTrivialSkipBlock(trueBranch)) {
            return null;
        }

        String targetVar =
                findSingleAssignmentTarget(falseBranch, ctx);
        if (targetVar == null) {
            return null;
        }

        String condVar =
                extractConditionVariable(condBlock, ctx);
        if (condVar == null || !condVar.equals(targetVar)) {
            return null;
        }

        ArkTSExpression valueExpr =
                reconstructor.extractBlockValue(falseBranch, ctx);

        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.LOGICAL_ASSIGN);
        p.conditionBlock = condBlock;
        p.trueBlock = trueBranch;
        p.falseBlock = falseBranch;
        p.logicalAssignOp = op;
        p.logicalAssignTargetVar = targetVar;
        p.logicalAssignValueExpr = valueExpr;
        return p;
    }

    private boolean isTrivialSkipBlock(BasicBlock block) {
        List<ArkInstruction> insns = block.getInstructions();
        if (insns.isEmpty()) {
            return true;
        }
        // Single unconditional jump - trivial
        if (insns.size() == 1) {
            ArkInstruction last = block.getLastInstruction();
            return ArkOpcodesCompat.isUnconditionalJump(last.getOpcode());
        }
        // lda vN; sta vN (identity store) + optional jump - trivial
        if (insns.size() <= 3) {
            int jmpCount = 0;
            int otherCount = 0;
            for (ArkInstruction insn : insns) {
                int o = insn.getOpcode();
                if (o == ArkOpcodesCompat.STA || o == ArkOpcodesCompat.LDA) {
                    continue;
                } else if (ArkOpcodesCompat.isUnconditionalJump(o)) {
                    jmpCount++;
                } else {
                    otherCount++;
                }
            }
            return otherCount == 0 && jmpCount <= 1;
        }
        return false;
    }

    private String findSingleAssignmentTarget(BasicBlock block,
            DecompilationContext ctx) {
        String target = null;
        for (ArkInstruction insn : block.getInstructions()) {
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                String varName = ctx.resolveRegisterName(
                        (int) insn.getOperands().get(0).getValue());
                if (target == null) {
                    target = varName;
                } else if (!target.equals(varName)) {
                    return null;
                }
            }
        }
        return target;
    }

    private String extractConditionVariable(BasicBlock condBlock,
            DecompilationContext ctx) {
        for (ArkInstruction insn : condBlock.getInstructions()) {
            if (insn == condBlock.getLastInstruction()) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.LDA) {
                return ctx.resolveRegisterName(
                        (int) insn.getOperands().get(0).getValue());
            }
        }
        return null;
    }

    List<ArkTSStatement> processLogicalAssign(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        String op = pattern.logicalAssignOp;
        String targetVar = pattern.logicalAssignTargetVar;
        ArkTSExpression valueExpr = pattern.logicalAssignValueExpr;

        if (op != null && targetVar != null) {
            ArkTSExpression logicalAssign =
                    new ArkTSExpression.LogicalAssignExpression(
                            new ArkTSExpression.VariableExpression(targetVar),
                            op,
                            valueExpr != null ? valueExpr
                                    : new ArkTSExpression.VariableExpression(
                                            targetVar));
            stmts.add(new ArkTSStatement.ExpressionStatement(
                    logicalAssign));
        }

        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        return stmts;
    }

    // --- Optional chaining detection ---

    /**
     * Detects an optional chaining pattern in an IF_ONLY branch.
     *
     * <p>Pattern: a property load (ldobjbyname, etc.) is followed by
     * jeqnull/jstricteqnull. The null-check branch jumps to a block
     * that assigns null/undefined, while the fall-through continues
     * using the result. This indicates {@code obj?.prop}.
     *
     * <p>If detected, returns an IF_ONLY pattern where the branch
     * block's instructions are rewritten to use an
     * OptionalChainExpression instead of the plain property load.
     *
     * @param condBlock the condition block
     * @param pattern the detected IF_ONLY pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @return statements with optional chaining, or null if not detected
     */
    List<ArkTSStatement> processOptionalChain(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        ArkInstruction lastInsn = condBlock.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();

        // Only trigger on null-check branches (jeqnull, jstricteqnull)
        if (!isNullCheckBranch(opcode)) {
            return null;
        }

        // Check the accumulator value — must be a member expression
        // (from a property load like ldobjbyname)
        ArkTSExpression accValue = ctx.currentAccValue;
        if (accValue == null) {
            return null;
        }

        ArkTSExpression optionalExpr =
                tryConvertToOptionalChain(accValue);
        if (optionalExpr == null) {
            return null;
        }

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process pre-branch instructions in condition block
        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, lastInsn);
        stmts.addAll(preStmts);

        // The branch block (null path) — typically assigns null/undefined
        // The fall-through block (non-null path) — continues with result
        int lastBranchOpcode = lastInsn.getOpcode();
        BasicBlock nullPathBlock;
        BasicBlock nonNullPathBlock;

        if (reconstructor.isBranchOnFalse(lastBranchOpcode)) {
            // jeqnull jumps when null -> nullPath is trueBlock
            nullPathBlock = pattern.trueBlock;
            nonNullPathBlock = pattern.falseBlock;
        } else {
            nullPathBlock = pattern.falseBlock;
            nonNullPathBlock = pattern.trueBlock;
        }

        visited.add(nullPathBlock);
        visited.add(nonNullPathBlock);

        // The non-null path should use the optional chain expression
        // Replace the accumulator value temporarily
        ArkTSExpression savedAcc = ctx.currentAccValue;
        ctx.currentAccValue = optionalExpr;

        // Process the non-null path (the one that uses the result)
        List<ArkTSStatement> nonNullStmts =
                reconstructor.processBlockInstructions(
                        nonNullPathBlock, ctx);

        // Check if non-null path stores to a variable — then we need
        // to also handle the null path storing null to same variable
        String targetVar = findStaTarget(nonNullStmts);

        if (targetVar != null && isSimpleNullAssignment(nullPathBlock, ctx)) {
            // Emit: let result = obj?.prop;
            // The optional expression is already captured
            stmts.add(new ArkTSStatement.VariableDeclaration(
                    "let", targetVar, null, optionalExpr));

            // Process remaining statements after the sta in nonNull block
            for (ArkTSStatement s : nonNullStmts) {
                if (!(s instanceof ArkTSStatement.VariableDeclaration)) {
                    stmts.add(s);
                }
            }
        } else {
            // Emit the optional chain as an expression statement
            stmts.add(new ArkTSStatement.ExpressionStatement(optionalExpr));
            stmts.addAll(nonNullStmts);
        }

        ctx.currentAccValue = savedAcc;
        return stmts;
    }

    /**
     * Returns true if the opcode is a null-check branch.
     */
    private static boolean isNullCheckBranch(int opcode) {
        return opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JNENULL_IMM8
                || opcode == ArkOpcodesCompat.JNENULL_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM16;
    }

    /**
     * Tries to convert a MemberExpression to an OptionalChainExpression.
     *
     * @param expr the expression to convert
     * @return an OptionalChainExpression, or null if not applicable
     */
    private static ArkTSExpression tryConvertToOptionalChain(
            ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression member =
                    (ArkTSExpression.MemberExpression) expr;
            return new ArkTSAccessExpressions.OptionalChainExpression(
                    member.getObject(), member.getProperty(),
                    member.isComputed());
        }
        if (expr instanceof ArkTSExpression.CallExpression) {
            ArkTSExpression.CallExpression call =
                    (ArkTSExpression.CallExpression) expr;
            if (call.getCallee()
                    instanceof ArkTSExpression.MemberExpression) {
                ArkTSExpression.MemberExpression member =
                        (ArkTSExpression.MemberExpression) call.getCallee();
                return new ArkTSAccessExpressions
                        .OptionalChainCallExpression(
                        member.getObject(), member.getProperty(),
                        member.isComputed(), call.getArguments());
            }
        }
        return null;
    }

    /**
     * Finds the target variable of the first VariableDeclaration or
     * assignment in a statement list.
     */
    private static String findStaTarget(List<ArkTSStatement> stmts) {
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.VariableDeclaration) {
                return ((ArkTSStatement.VariableDeclaration) stmt)
                        .getName();
            }
            if (stmt instanceof ArkTSStatement.ExpressionStatement) {
                ArkTSExpression expr =
                        ((ArkTSStatement.ExpressionStatement) stmt)
                                .getExpression();
                if (expr instanceof ArkTSExpression.AssignExpression) {
                    ArkTSExpression target =
                            ((ArkTSExpression.AssignExpression) expr)
                                    .getTarget();
                    if (target
                            instanceof ArkTSExpression.VariableExpression) {
                        return ((ArkTSExpression.VariableExpression) target)
                                .getName();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if a block simply assigns null/undefined to a variable.
     */
    private boolean isSimpleNullAssignment(BasicBlock block,
            DecompilationContext ctx) {
        List<ArkInstruction> insns = block.getInstructions();
        if (insns.isEmpty()) {
            return false;
        }
        // Check for: ldnull; sta vN or ldundefined; sta vN
        for (ArkInstruction insn : insns) {
            int op = insn.getOpcode();
            if (op == ArkOpcodesCompat.LDNULL
                    || op == ArkOpcodesCompat.LDUNDEFINED) {
                return true;
            }
        }
        return false;
    }

    // --- Non-null assertion detection ---

    /**
     * Processes a non-null assertion pattern.
     *
     * <p>Pattern: {@code jnenull} (jump if not null) where the non-null
     * path continues with the value and the null path throws/returns.
     * This emits the {@code expr!} syntax.
     *
     * @param expr the expression to assert non-null
     * @return a NonNullExpression wrapping the input, or the input
     *         unchanged if the assertion is not applicable
     */
    static ArkTSExpression applyNonNullAssertion(
            ArkTSExpression expr) {
        if (expr == null) {
            return null;
        }
        // If the expression is already an optional chain, don't add !
        if (expr instanceof ArkTSAccessExpressions.OptionalChainExpression
                || expr instanceof ArkTSAccessExpressions
                        .OptionalChainCallExpression) {
            return expr;
        }
        return new ArkTSAccessExpressions.NonNullExpression(expr);
    }

    // --- Guard clause detection ---

    /**
     * Detects a guard clause pattern in an IF_ONLY branch.
     *
     * <p>Pattern: {@code if (!condition) { return defaultValue }}
     * This is detected when the branch block (taken when the condition
     * is falsy) contains only a return statement, and the fall-through
     * continues with the main logic.
     *
     * <p>When detected, emits the guard clause as an early return rather
     * than wrapping the rest of the code in an else block.
     *
     * @param condBlock the condition block
     * @param pattern the detected IF_ONLY pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @return statements with guard clause, or null if not a guard
     */
    List<ArkTSStatement> detectGuardClause(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        // The branch block (taken on the branch condition) must contain
        // only a return statement (guard clause body)
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        BasicBlock branchBlock;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            branchBlock = pattern.trueBlock;
        } else {
            branchBlock = pattern.falseBlock;
        }

        // Check if the branch block is a simple return (guard body)
        if (!isGuardReturnBlock(branchBlock)) {
            return null;
        }

        // This IS a guard clause: emit early return
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

        // Negate the condition for guard clause: "if (!cond) return"
        ArkTSExpression guardCondition;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            // Branch taken when false -> condition is already negated
            guardCondition = condition;
        } else {
            guardCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        visited.add(branchBlock);

        List<ArkTSStatement> guardStmts =
                reconstructor.processBlockInstructions(
                        branchBlock, ctx);
        ArkTSStatement guardBody =
                new ArkTSStatement.BlockStatement(guardStmts);

        stmts.add(new ArkTSControlFlow.IfStatement(
                guardCondition, guardBody, null));

        return stmts;
    }

    /**
     * Returns true if a block contains only a return statement
     * (with or without a value), making it suitable as a guard body.
     */
    private boolean isGuardReturnBlock(BasicBlock block) {
        List<ArkInstruction> insns = block.getInstructions();
        if (insns.isEmpty()) {
            return false;
        }
        // The block should end with return or returnundefined
        // and have at most one load + one return
        boolean hasReturn = false;
        int nonNopCount = 0;
        for (ArkInstruction insn : insns) {
            int op = insn.getOpcode();
            if (op == ArkOpcodesCompat.RETURN
                    || op == ArkOpcodesCompat.RETURNUNDEFINED) {
                hasReturn = true;
            }
            if (op != ArkOpcodesCompat.NOP) {
                nonNopCount++;
            }
        }
        // Guard body: 1-3 instructions (load value + return, or just
        // return)
        return hasReturn && nonNopCount <= 3;
    }

    // --- Optional chaining with property load ---

    /**
     * Attempts to detect optional chaining from a sequence of
     * instructions within a condition block.
     *
     * <p>Looks for: property load (ldobjbyname etc.) followed by
     * jnenull (branch if not null). When the null path assigns
     * null/undefined and the non-null path continues, this indicates
     * optional chaining.
     *
     * @param condBlock the condition block ending with a branch
     * @param ctx the decompilation context
     * @return an optional chain expression, or null
     */
    static ArkTSExpression tryDetectOptionalChainFromBlock(
            BasicBlock condBlock, DecompilationContext ctx) {
        List<ArkInstruction> insns = condBlock.getInstructions();
        if (insns.size() < 2) {
            return null;
        }

        ArkInstruction lastInsn = condBlock.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int branchOp = lastInsn.getOpcode();

        // Check for jnenull (0x55) or jnstrecteqnull
        boolean isJumpIfNotNull =
                branchOp == ArkOpcodesCompat.JNENULL_IMM8
                || branchOp == ArkOpcodesCompat.JNENULL_IMM16
                || branchOp == ArkOpcodesCompat.JNSTRICTEQNULL_IMM8
                || branchOp == ArkOpcodesCompat.JNSTRICTEQNULL_IMM16;

        // Check for jeqnull (0x54) or jstricteqnull
        boolean isJumpIfNull =
                branchOp == ArkOpcodesCompat.JEQNULL_IMM8
                || branchOp == ArkOpcodesCompat.JEQNULL_IMM16
                || branchOp == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || branchOp == ArkOpcodesCompat.JSTRICTEQNULL_IMM16;

        if (!isJumpIfNotNull && !isJumpIfNull) {
            return null;
        }

        // The accumulator should hold the result of a property load
        ArkTSExpression accValue = ctx.currentAccValue;
        if (accValue == null) {
            return null;
        }

        return tryConvertToOptionalChain(accValue);
    }

    // --- Helper: extract property name from ldobjbyname instruction ---

    /**
     * Extracts the property name from a property load instruction if
     * it is a name-based access (ldobjbyname, ldthisbyname, etc.).
     *
     * @param insn the instruction
     * @param ctx the decompilation context
     * @return the property name, or null if not a name-based load
     */
    static String extractPropertyName(ArkInstruction insn,
            DecompilationContext ctx) {
        if (insn == null) {
            return null;
        }
        int opcode = ArkOpcodesCompat.getNormalizedOpcode(insn);
        if (opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME) {
            List<ArkOperand> operands = insn.getOperands();
            if (operands.size() >= 2) {
                return ctx.resolveString(
                        (int) operands.get(1).getValue());
            }
        }
        return null;
    }

    /**
     * Checks if a block has a conditional branch as successor.
     */
    private static boolean hasConditionalSuccessor(BasicBlock block,
            ControlFlowGraph cfg) {
        ArkInstruction last = block.getLastInstruction();
        if (last == null) {
            return false;
        }
        return ArkOpcodesCompat.isConditionalBranch(last.getOpcode());
    }

    // --- Opcode classification helpers for short-circuit detection ---

    private static boolean isAndConditionOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16
                || opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16;
    }

    private static boolean isOrConditionOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16
                || opcode == ArkOpcodesCompat.JNENULL_IMM8
                || opcode == ArkOpcodesCompat.JNENULL_IMM16
                || opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQUNDEFINED_IMM16;
    }

    static boolean isAndNeedsNegation(int opcode) {
        return opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16;
    }

    static ArkTSExpression negateConditionExpression(
            ArkTSExpression cond) {
        if (cond instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression be =
                    (ArkTSExpression.BinaryExpression) cond;
            String negOp = switch (be.getOperator()) {
                case "==" -> "!=";
                case "!=" -> "==";
                case "===" -> "!==";
                case "!==" -> "===";
                default -> be.getOperator();
            };
            return new ArkTSExpression.BinaryExpression(
                    be.getLeft(), negOp, be.getRight());
        }
        return new ArkTSExpression.UnaryExpression("!", cond, true);
    }

    /**
     * Collects all blocks reachable from startBlock up to (excluding)
     * the stopBlock, sorted by offset. Used to build a sub-CFG for
     * recursive processing of if/else branch bodies.
     */
    private static List<BasicBlock> collectIfBranchBlocks(
            BasicBlock startBlock, BasicBlock stopBlock,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {
        List<BasicBlock> result = new ArrayList<>();
        Set<BasicBlock> seen = new HashSet<>();
        List<BasicBlock> worklist = new ArrayList<>();
        worklist.add(startBlock);
        while (!worklist.isEmpty()) {
            BasicBlock current = worklist.remove(worklist.size() - 1);
            if (seen.contains(current) || visited.contains(current)) {
                continue;
            }
            seen.add(current);
            if (current == stopBlock) {
                continue;
            }
            result.add(current);
            for (CFGEdge succ : current.getSuccessors()) {
                BasicBlock succBlock = cfg.getBlockAt(succ.getToOffset());
                if (succBlock != null && !seen.contains(succBlock)) {
                    worklist.add(succBlock);
                }
            }
        }
        result.sort((a, b) ->
                Integer.compare(a.getStartOffset(), b.getStartOffset()));
        return result;
    }

    /**
     * Collects all blocks reachable from startBlock up to (excluding)
     * the stopBlock. Used to build a sub-CFG for recursive processing
     * of else-if chains.
     */
    private static List<BasicBlock> collectBlocksBetween(
            BasicBlock startBlock, BasicBlock stopBlock,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {
        List<BasicBlock> result = new ArrayList<>();
        Set<BasicBlock> seen = new HashSet<>();
        List<BasicBlock> worklist = new ArrayList<>();
        worklist.add(startBlock);
        while (!worklist.isEmpty()) {
            BasicBlock current = worklist.remove(worklist.size() - 1);
            if (seen.contains(current)) {
                continue;
            }
            seen.add(current);
            if (current == stopBlock) {
                continue;
            }
            result.add(current);
            for (CFGEdge succ : current.getSuccessors()) {
                BasicBlock succBlock = cfg.getBlockAt(succ.getToOffset());
                if (succBlock != null && !seen.contains(succBlock)) {
                    worklist.add(succBlock);
                }
            }
        }
        return result;
    }
}
