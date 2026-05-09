package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Processes loop-related control flow patterns: while, for, do-while,
 * for-of, and for-in loops.
 *
 * <p>Called from {@link ControlFlowReconstructor} for
 * {@code WHILE_LOOP}, {@code FOR_LOOP}, {@code DO_WHILE},
 * {@code FOR_OF_LOOP}, and {@code FOR_IN_LOOP} patterns.
 */
class LoopProcessor {

    private final ControlFlowReconstructor reconstructor;

    LoopProcessor(ControlFlowReconstructor reconstructor) {
        this.reconstructor = reconstructor;
    }

    // --- Do-while detection ---

    /**
     * Detects a do-while loop pattern where a body block flows into a
     * condition block that conditionally jumps back to the body start.
     *
     * <p>CFG pattern:
     * <pre>
     * [body] -&gt; [condition] -&gt; (true)  -&gt; [body]  (backward edge)
     *                     -&gt; (false) -&gt; [exit]
     * </pre>
     *
     * <p>This is called from {@link ControlFlowReconstructor#detectPattern}
     * when a 2-successor block has a back edge to an earlier block that is
     * NOT the condition block itself (which would be a while loop).
     *
     * @param condBlock the block containing the conditional branch
     * @param trueBranch the true-successor of the conditional branch
     * @param falseBranch the false-successor of the conditional branch
     * @param cfg the control flow graph
     * @return the pattern if a do-while is detected, or null
     */
    ControlFlowReconstructor.ControlFlowPattern detectDoWhilePattern(
            BasicBlock condBlock, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg) {

        ArkInstruction lastInsn = condBlock.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }

        int opcode = lastInsn.getOpcode();
        if (!ArkOpcodesCompat.isConditionalBranch(opcode)) {
            return null;
        }

        // A do-while: the condition jumps backward to the body start.
        // The body block is the one the condition jumps back to.
        // In a while loop, the condition jumps backward to itself.
        // In a do-while, the condition jumps backward to a DIFFERENT block
        // (the body) that comes before the condition.

        int jumpTarget = ControlFlowGraph.getJumpTargetPublic(lastInsn);

        // The jump must go backward (to a block before the condition block)
        if (jumpTarget >= condBlock.getStartOffset()) {
            return null;
        }

        // The target block (body) must exist
        BasicBlock bodyBlock = cfg.getBlockAt(jumpTarget);
        if (bodyBlock == null) {
            return null;
        }

        // The body block must flow into the condition block (directly or
        // through a chain of blocks that eventually reaches the condition)
        boolean bodyReachesCondition = false;

        // Check direct edge from body to condition
        for (CFGEdge edge : bodyBlock.getSuccessors()) {
            if (edge.getToOffset() == condBlock.getStartOffset()) {
                bodyReachesCondition = true;
                break;
            }
        }

        // Check indirect: body -> ... -> condition (1 hop)
        if (!bodyReachesCondition) {
            for (CFGEdge edge : bodyBlock.getSuccessors()) {
                BasicBlock intermediate = cfg.getBlockAt(
                        edge.getToOffset());
                if (intermediate != null
                        && intermediate != bodyBlock) {
                    for (CFGEdge edge2 :
                            intermediate.getSuccessors()) {
                        if (edge2.getToOffset()
                                == condBlock.getStartOffset()) {
                            bodyReachesCondition = true;
                            break;
                        }
                    }
                }
                if (bodyReachesCondition) {
                    break;
                }
            }
        }

        if (!bodyReachesCondition) {
            return null;
        }

        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.DO_WHILE);
        p.conditionBlock = condBlock;
        p.trueBlock = bodyBlock;
        return p;
    }

    // --- Classic for-loop detection ---

    /**
     * Detects a classic for(init; cond; update) loop pattern.
     *
     * <p>CFG pattern:
     * <pre>
     * [init_block] -&gt; [cond_block] -&gt; (back) -&gt; [body...update] -&gt; [cond_block]
     *                           -&gt; (exit) -&gt; [exit_block]
     * </pre>
     *
     * <p>Detection criteria:
     * <ul>
     *   <li>The loop header (cond_block) has a conditional branch</li>
     *   <li>One successor leads back (loop body), the other exits</li>
     *   <li>The predecessor of the cond_block is a simple init block
     *       (variable declaration/assignment of a counter)</li>
     *   <li>The last block before the back edge contains an update
     *       (increment or compound assignment on the counter variable)</li>
     * </ul>
     *
     * @param condBlock the loop condition/header block
     * @param loopBody the first block of the loop body
     * @param cfg the control flow graph
     * @return the pattern if a for-loop is detected, or null
     */
    ControlFlowReconstructor.ControlFlowPattern detectClassicForLoopPattern(
            BasicBlock condBlock, BasicBlock loopBody,
            ControlFlowGraph cfg, DecompilationContext ctx) {

        ArkInstruction lastInsn = condBlock.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();
        if (!ArkOpcodesCompat.isConditionalBranch(opcode)) {
            return null;
        }

        // Find the init block: predecessor of condBlock that comes before it
        BasicBlock initBlock = null;
        for (CFGEdge pred : condBlock.getPredecessors()) {
            BasicBlock predBlock = cfg.getBlockAt(pred.getFromOffset());
            if (predBlock != null
                    && predBlock.getEndOffset() <= condBlock.getStartOffset()
                    + condBlock.getLastInstruction().getLength()) {
                // Only consider predecessors that are before or adjacent to
                // the condition block (not the loop body back-edge)
                boolean isBackEdge = false;
                if (predBlock.getStartOffset() >= condBlock.getStartOffset()) {
                    isBackEdge = true;
                }
                if (!isBackEdge && (initBlock == null
                        || predBlock.getStartOffset()
                                > initBlock.getStartOffset())) {
                    initBlock = predBlock;
                }
            }
        }

        // Verify init block has a simple variable init (ldai + sta or lda + sta)
        if (initBlock == null || !isSimpleInitBlock(initBlock)) {
            return null;
        }

        // Find the update block: the last block in the loop body chain
        // before the back edge to condBlock
        BasicBlock updateBlock =
                findUpdateBlock(loopBody, condBlock, cfg, new HashSet<>());

        if (updateBlock == null || !isUpdateBlock(updateBlock)) {
            return null;
        }

        // The update block must not be the same as the init block
        if (updateBlock == initBlock) {
            return null;
        }

        // Extract the loop counter variable from the init block
        String counterVar = extractCounterVariable(initBlock);
        if (counterVar == null) {
            return null;
        }

        // Verify the update modifies the counter variable
        if (!updateModifiesVariable(updateBlock, counterVar)) {
            return null;
        }

        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.FOR_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.initBlock = initBlock;
        p.updateBlock = updateBlock;
        p.forLoopCounterVar = counterVar;
        return p;
    }

    /**
     * Checks if a block is a simple initialization block (e.g., ldai N; sta vX).
     */
    private boolean isSimpleInitBlock(BasicBlock block) {
        List<ArkInstruction> insns = block.getInstructions();
        if (insns.isEmpty()) {
            return false;
        }
        // Look for pattern: ldai/lda + sta (possibly with other simple insns)
        boolean hasStore = false;
        boolean hasLoad = false;
        for (ArkInstruction insn : insns) {
            int op = insn.getOpcode();
            if (op == ArkOpcodesCompat.LDAI || op == ArkOpcodesCompat.LDA
                    || op == ArkOpcodesCompat.FLDAI) {
                hasLoad = true;
            }
            if (op == ArkOpcodesCompat.STA) {
                hasStore = true;
            }
            // Init block should not have branches, calls, or complex ops
            if (ArkOpcodesCompat.isConditionalBranch(op)
                    || ArkOpcodesCompat.isUnconditionalJump(op)
                    || op == ArkOpcodesCompat.RETURN
                    || op == ArkOpcodesCompat.RETURNUNDEFINED) {
                return false;
            }
        }
        return hasLoad && hasStore;
    }

    /**
     * Finds the update block by tracing the loop body to the block
     * that has a back edge to condBlock.
     */
    private BasicBlock findUpdateBlock(BasicBlock current,
            BasicBlock condBlock, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        if (current == null || visited.contains(current)) {
            return null;
        }
        visited.add(current);

        for (CFGEdge edge : current.getSuccessors()) {
            if (edge.getToOffset() == condBlock.getStartOffset()) {
                // This block jumps back to the condition, so it's
                // the update block (or the end of the body)
                return current;
            }
        }

        // Continue tracing through successors that are within the loop
        for (CFGEdge edge : current.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && succ.getStartOffset()
                    >= condBlock.getStartOffset()) {
                BasicBlock result =
                        findUpdateBlock(succ, condBlock, cfg, visited);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a block contains an update operation (inc, dec, or
     * compound assignment).
     */
    private boolean isUpdateBlock(BasicBlock block) {
        List<ArkInstruction> insns = block.getInstructions();
        for (ArkInstruction insn : insns) {
            int op = insn.getOpcode();
            // INC, DEC are increment/decrement on acc
            if (op == ArkOpcodesCompat.INC || op == ArkOpcodesCompat.DEC) {
                return true;
            }
            // add2/sub2 followed by sta could be i += 1 / i -= 1
            if (op == ArkOpcodesCompat.ADD2 || op == ArkOpcodesCompat.SUB2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the counter variable name from an init block.
     */
    private String extractCounterVariable(BasicBlock initBlock) {
        List<ArkInstruction> insns = initBlock.getInstructions();
        for (int i = 0; i < insns.size(); i++) {
            ArkInstruction insn = insns.get(i);
            int op = insn.getOpcode();
            if (op == ArkOpcodesCompat.STA) {
                List<ArkOperand> operands = insn.getOperands();
                if (!operands.isEmpty()) {
                    return "v" + operands.get(0).getValue();
                }
            }
        }
        return null;
    }

    /**
     * Checks if an update block modifies the specified counter variable.
     */
    private boolean updateModifiesVariable(BasicBlock updateBlock,
            String counterVar) {
        List<ArkInstruction> insns = updateBlock.getInstructions();
        // Look for: lda counterVar; inc/dec/add2/sub2; sta counterVar
        // Or the last sta writes to counterVar
        for (int i = 0; i < insns.size(); i++) {
            ArkInstruction insn = insns.get(i);
            int op = insn.getOpcode();
            // Pattern 1: lda counterVar before inc/dec/add2/sub2
            if (op == ArkOpcodesCompat.LDA && i + 1 < insns.size()) {
                List<ArkOperand> operands = insn.getOperands();
                if (!operands.isEmpty()) {
                    String varName = "v" + operands.get(0).getValue();
                    if (varName.equals(counterVar)) {
                        int nextOp = insns.get(i + 1).getOpcode();
                        if (nextOp == ArkOpcodesCompat.INC
                                || nextOp == ArkOpcodesCompat.DEC
                                || nextOp == ArkOpcodesCompat.ADD2
                                || nextOp == ArkOpcodesCompat.SUB2) {
                            return true;
                        }
                    }
                }
            }
            // Pattern 2: sta counterVar as the final write
            if (op == ArkOpcodesCompat.STA) {
                List<ArkOperand> operands = insn.getOperands();
                if (!operands.isEmpty()) {
                    String varName = "v" + operands.get(0).getValue();
                    if (varName.equals(counterVar)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- For-of detection ---

    ControlFlowReconstructor.ControlFlowPattern detectForOfPattern(
            BasicBlock condBlock, BasicBlock loopBody,
            ControlFlowGraph cfg, DecompilationContext ctx) {
        boolean hasGetIterator = false;
        BasicBlock setupBlock = null;
        for (ArkInstruction insn : condBlock.getInstructions()) {
            if (ArkOpcodesCompat.isGetIterator(insn.getOpcode())) {
                hasGetIterator = true;
                break;
            }
        }
        if (!hasGetIterator) {
            for (CFGEdge pred : condBlock.getPredecessors()) {
                BasicBlock predBlock =
                        cfg.getBlockAt(pred.getFromOffset());
                if (predBlock != null) {
                    for (ArkInstruction insn :
                            predBlock.getInstructions()) {
                        if (ArkOpcodesCompat.isGetIterator(
                                insn.getOpcode())) {
                            hasGetIterator = true;
                            setupBlock = predBlock;
                            break;
                        }
                    }
                    if (!hasGetIterator) {
                        for (CFGEdge pred2 :
                                predBlock.getPredecessors()) {
                            BasicBlock pred2Block =
                                    cfg.getBlockAt(
                                            pred2.getFromOffset());
                            if (pred2Block != null
                                    && pred2Block != condBlock) {
                                for (ArkInstruction insn2 :
                                        pred2Block.getInstructions()) {
                                    if (ArkOpcodesCompat.isGetIterator(
                                            insn2.getOpcode())) {
                                        hasGetIterator = true;
                                        setupBlock = pred2Block;
                                        break;
                                    }
                                }
                            }
                            if (hasGetIterator) {
                                break;
                            }
                        }
                    }
                }
                if (hasGetIterator) {
                    break;
                }
            }
        }
        if (!hasGetIterator) {
            return null;
        }
        String iterVarName = null;
        String itemVarName = null;
        List<ArkInstruction> searchInsns = setupBlock != null
                ? setupBlock.getInstructions()
                : condBlock.getInstructions();
        for (int i = 0; i < searchInsns.size() - 1; i++) {
            ArkInstruction insn = searchInsns.get(i);
            if (ArkOpcodesCompat.isGetIterator(insn.getOpcode())) {
                if (i + 1 < searchInsns.size()) {
                    ArkInstruction nextInsn = searchInsns.get(i + 1);
                    if (nextInsn.getOpcode()
                            == ArkOpcodesCompat.STA) {
                        iterVarName = ctx.resolveRegisterName(
                                (int) nextInsn.getOperands()
                                        .get(0).getValue());
                    }
                }
                break;
            }
        }
        for (int i = 0; i < condBlock.getInstructions().size();
                i++) {
            ArkInstruction insn =
                    condBlock.getInstructions().get(i);
            if (insn.getOpcode()
                    == ArkOpcodesCompat.GETNEXTPROPNAME) {
                if (i + 1 < condBlock.getInstructions().size()) {
                    ArkInstruction nextInsn =
                            condBlock.getInstructions().get(i + 1);
                    if (nextInsn.getOpcode()
                            == ArkOpcodesCompat.STA) {
                        itemVarName = ctx.resolveRegisterName(
                                (int) nextInsn.getOperands()
                                        .get(0).getValue());
                    }
                }
            }
        }
        if (itemVarName == null) {
            for (int i = 0; i < loopBody.getInstructions().size();
                    i++) {
                ArkInstruction insn =
                        loopBody.getInstructions().get(i);
                if (insn.getOpcode()
                        == ArkOpcodesCompat.GETNEXTPROPNAME) {
                    if (i + 1 < loopBody.getInstructions().size()) {
                        ArkInstruction nextInsn =
                                loopBody.getInstructions().get(i + 1);
                        if (nextInsn.getOpcode()
                                == ArkOpcodesCompat.STA) {
                            itemVarName = ctx.resolveRegisterName(
                                    (int) nextInsn.getOperands()
                                            .get(0).getValue());
                        }
                    }
                    break;
                }
            }
        }
        if (itemVarName == null) {
            itemVarName = "item";
        }
        ArkTSExpression iterableExpr =
                new ArkTSExpression.VariableExpression(
                        iterVarName != null ? iterVarName : "iterable");
        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.FOR_OF_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.iteratorVarName = itemVarName;
        p.iterableExpr = iterableExpr;
        p.iteratorSetupBlock = setupBlock;
        return p;
    }

    // --- For-in detection ---

    ControlFlowReconstructor.ControlFlowPattern detectForInPattern(
            BasicBlock condBlock, BasicBlock loopBody,
            ControlFlowGraph cfg, DecompilationContext ctx) {
        boolean hasPropIterator = false;
        BasicBlock setupBlock = null;
        for (CFGEdge pred : condBlock.getPredecessors()) {
            BasicBlock predBlock =
                    cfg.getBlockAt(pred.getFromOffset());
            if (predBlock != null) {
                for (ArkInstruction insn :
                        predBlock.getInstructions()) {
                    if (insn.getOpcode()
                            == ArkOpcodesCompat.GETPROPITERATOR) {
                        hasPropIterator = true;
                        setupBlock = predBlock;
                        break;
                    }
                }
                if (!hasPropIterator) {
                    for (CFGEdge pred2 :
                            predBlock.getPredecessors()) {
                        BasicBlock pred2Block =
                                cfg.getBlockAt(pred2.getFromOffset());
                        if (pred2Block != null
                                && pred2Block != condBlock) {
                            for (ArkInstruction insn2 :
                                    pred2Block.getInstructions()) {
                                if (insn2.getOpcode()
                                        == ArkOpcodesCompat
                                                .GETPROPITERATOR) {
                                    hasPropIterator = true;
                                    setupBlock = pred2Block;
                                    break;
                                }
                            }
                        }
                        if (hasPropIterator) {
                            break;
                        }
                    }
                }
            }
            if (hasPropIterator) {
                break;
            }
        }
        if (!hasPropIterator) {
            return null;
        }
        String keyVarName = null;
        String iterVarName = null;
        if (setupBlock != null) {
            List<ArkInstruction> setupInsns =
                    setupBlock.getInstructions();
            for (int i = 0; i < setupInsns.size() - 1; i++) {
                if (setupInsns.get(i).getOpcode()
                        == ArkOpcodesCompat.GETPROPITERATOR) {
                    if (i + 1 < setupInsns.size()) {
                        ArkInstruction nextInsn = setupInsns.get(i + 1);
                        if (nextInsn.getOpcode()
                                == ArkOpcodesCompat.STA) {
                            iterVarName = ctx.resolveRegisterName(
                                    (int) nextInsn.getOperands()
                                            .get(0).getValue());
                        }
                    }
                    break;
                }
            }
        }
        for (int i = 0; i < condBlock.getInstructions().size();
                i++) {
            ArkInstruction insn =
                    condBlock.getInstructions().get(i);
            if (insn.getOpcode()
                    == ArkOpcodesCompat.GETNEXTPROPNAME) {
                if (i + 1 < condBlock.getInstructions().size()) {
                    ArkInstruction nextInsn =
                            condBlock.getInstructions().get(i + 1);
                    if (nextInsn.getOpcode()
                            == ArkOpcodesCompat.STA) {
                        keyVarName = ctx.resolveRegisterName(
                                (int) nextInsn.getOperands()
                                        .get(0).getValue());
                    }
                }
                break;
            }
        }
        if (keyVarName == null) {
            for (int i = 0; i < loopBody.getInstructions().size();
                    i++) {
                ArkInstruction insn =
                        loopBody.getInstructions().get(i);
                if (insn.getOpcode()
                        == ArkOpcodesCompat.GETNEXTPROPNAME) {
                    if (i + 1 < loopBody.getInstructions().size()) {
                        ArkInstruction nextInsn =
                                loopBody.getInstructions().get(i + 1);
                        if (nextInsn.getOpcode()
                                == ArkOpcodesCompat.STA) {
                            keyVarName = ctx.resolveRegisterName(
                                    (int) nextInsn.getOperands()
                                            .get(0).getValue());
                        }
                    }
                    break;
                }
            }
        }
        if (keyVarName == null) {
            keyVarName = "key";
        }
        ArkTSExpression objectExpr =
                new ArkTSExpression.VariableExpression(
                        iterVarName != null ? iterVarName : "obj");
        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.FOR_IN_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.iteratorVarName = keyVarName;
        p.iterableExpr = objectExpr;
        p.iteratorSetupBlock = setupBlock;
        return p;
    }

    // --- Loop processing ---

    List<ArkTSStatement> processWhileLoop(BasicBlock condBlock,
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
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression(
                            "!", condition, true);
        }

        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);

        String whileLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ctx.popLoopContext();

        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(effectiveCondition,
                        bodyBlock);
        stmts.add(wrapLabel(whileStmt, whileLabel));

        return stmts;
    }

    List<ArkTSStatement> processForLoop(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Extract init statement from init block
        ArkTSStatement initStmt = null;
        if (pattern.initBlock != null) {
            List<ArkTSStatement> initStmts =
                    reconstructor.processBlockInstructions(
                            pattern.initBlock, ctx);
            visited.add(pattern.initBlock);
            if (!initStmts.isEmpty()) {
                initStmt = initStmts.get(0);
                // Add remaining statements if any
                for (int i = 1; i < initStmts.size(); i++) {
                    stmts.add(initStmts.get(i));
                }
            }
        }

        // Process condition
        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        // Pre-condition statements in the condition block are added to body
        // (they execute each iteration before the condition check)

        ArkTSExpression condition =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        // Negate condition if branch-on-false
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (reconstructor.isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        // Extract update expression from update block
        ArkTSExpression updateExpr = null;
        if (pattern.updateBlock != null) {
            List<ArkTSStatement> updateStmts =
                    reconstructor.processBlockInstructions(
                            pattern.updateBlock, ctx);
            visited.add(pattern.updateBlock);
            if (!updateStmts.isEmpty()) {
                ArkTSStatement firstStmt = updateStmts.get(0);
                if (firstStmt instanceof ArkTSStatement.ExpressionStatement) {
                    updateExpr =
                            ((ArkTSStatement.ExpressionStatement) firstStmt)
                                    .getExpression();
                } else if (firstStmt instanceof
                        ArkTSStatement.VariableDeclaration) {
                    // A variable declaration as update is unusual but handle it
                    updateExpr =
                            new ArkTSExpression.VariableExpression(
                                    ((ArkTSStatement.VariableDeclaration)
                                            firstStmt).getName());
                }
                // Add remaining update statements to body
                for (int i = 1; i < updateStmts.size(); i++) {
                    stmts.add(updateStmts.get(i));
                }
            }
        }

        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);

        String forLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);

        // Collect body statements: loop body minus update block
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts = new ArrayList<>();

        // Add pre-condition statements from condition block to body
        bodyStmts.addAll(preStmts);

        // Process the loop body blocks, excluding the update block
        if (pattern.trueBlock != pattern.updateBlock) {
            List<ArkTSStatement> trueBlockStmts =
                    collectBodyStatements(pattern.trueBlock,
                            pattern.updateBlock, ctx, cfg,
                            visited, new HashSet<>());
            bodyStmts.addAll(trueBlockStmts);
        }

        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ctx.popLoopContext();

        ArkTSControlFlow.ForStatement forStmt =
                new ArkTSControlFlow.ForStatement(
                        initStmt, effectiveCondition, updateExpr, bodyBlock);
        stmts.add(wrapLabel(forStmt, forLabel));

        return stmts;
    }

    /**
     * Collects body statements by walking the loop body blocks from
     * the first body block to (but not including) the update block.
     */
    private List<ArkTSStatement> collectBodyStatements(
            BasicBlock current, BasicBlock updateBlock,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited, Set<BasicBlock> bodyVisited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        if (current == null || bodyVisited.contains(current)) {
            return stmts;
        }
        bodyVisited.add(current);

        if (current == updateBlock) {
            return stmts;
        }

        visited.add(current);
        stmts.addAll(reconstructor.processBlockInstructions(current, ctx));

        // Follow successors within the loop body
        for (CFGEdge edge : current.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && !bodyVisited.contains(succ)
                    && succ.getStartOffset()
                            >= current.getStartOffset()) {
                stmts.addAll(collectBodyStatements(succ, updateBlock,
                        ctx, cfg, visited, bodyVisited));
            }
        }
        return stmts;
    }

    List<ArkTSStatement> processDoWhile(BasicBlock block,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        int loopHeaderOffset = block.getStartOffset();
        int loopEndOffset = block.getEndOffset();
        if (pattern.conditionBlock != null) {
            for (CFGEdge edge :
                    pattern.conditionBlock.getSuccessors()) {
                if (edge.getToOffset() > loopEndOffset) {
                    loopEndOffset = edge.getToOffset();
                }
            }
        }

        String doWhileLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);

        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(block, ctx);

        ArkTSExpression condition =
                new ArkTSExpression.VariableExpression(
                        ControlFlowReconstructor.ACC);
        if (pattern.conditionBlock != null) {
            visited.add(pattern.conditionBlock);
            List<ArkTSStatement> condStmts =
                    reconstructor.processBlockInstructionsExcluding(
                            pattern.conditionBlock, ctx,
                            pattern.conditionBlock.getLastInstruction());
            bodyStmts.addAll(condStmts);
            condition =
                    reconstructor.getConditionExpression(
                            pattern.conditionBlock.getLastInstruction(),
                            ctx);
            if (condition == null) {
                condition = new ArkTSExpression.VariableExpression(
                        ControlFlowReconstructor.ACC);
            }
        }

        ctx.popLoopContext();

        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSControlFlow.DoWhileStatement doWhile =
                new ArkTSControlFlow.DoWhileStatement(bodyBlock, condition);
        stmts.add(wrapLabel(doWhile, doWhileLabel));

        return stmts;
    }

    List<ArkTSStatement> processForOfLoop(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcludingIterator(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);
        String forOfLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ctx.popLoopContext();
        ArkTSStatement forOfStmt =
                new ArkTSControlFlow.ForOfStatement(
                        "const", pattern.iteratorVarName,
                        pattern.iterableExpr, bodyBlock);
        stmts.add(wrapLabel(forOfStmt, forOfLabel));
        return stmts;
    }

    List<ArkTSStatement> processForInLoop(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcludingIterator(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);
        String forInLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ctx.popLoopContext();
        ArkTSStatement forInStmt =
                new ArkTSControlFlow.ForInStatement(
                        "const", pattern.iteratorVarName,
                        pattern.iterableExpr, bodyBlock);
        stmts.add(wrapLabel(forInStmt, forInLabel));
        return stmts;
    }

    // --- For-await-of detection ---

    /**
     * Detects a for-await-of loop pattern from the control flow graph.
     * Similar to for-of but uses GETASYNCITERATOR instead of GETITERATOR.
     *
     * @param condBlock the loop condition/header block
     * @param loopBody the loop body block
     * @param cfg the control flow graph
     * @return the pattern if detected, or null
     */
    ControlFlowReconstructor.ControlFlowPattern detectForAwaitOfPattern(
            BasicBlock condBlock, BasicBlock loopBody,
            ControlFlowGraph cfg, DecompilationContext ctx) {
        boolean hasAsyncIterator = false;
        BasicBlock setupBlock = null;
        for (ArkInstruction insn : condBlock.getInstructions()) {
            if (ArkOpcodesCompat.isGetAsyncIterator(insn.getOpcode())) {
                hasAsyncIterator = true;
                break;
            }
        }
        if (!hasAsyncIterator) {
            for (CFGEdge pred : condBlock.getPredecessors()) {
                BasicBlock predBlock =
                        cfg.getBlockAt(pred.getFromOffset());
                if (predBlock != null) {
                    for (ArkInstruction insn :
                            predBlock.getInstructions()) {
                        if (ArkOpcodesCompat.isGetAsyncIterator(
                                insn.getOpcode())) {
                            hasAsyncIterator = true;
                            setupBlock = predBlock;
                            break;
                        }
                    }
                    if (!hasAsyncIterator) {
                        for (CFGEdge pred2 :
                                predBlock.getPredecessors()) {
                            BasicBlock pred2Block =
                                    cfg.getBlockAt(
                                            pred2.getFromOffset());
                            if (pred2Block != null
                                    && pred2Block != condBlock) {
                                for (ArkInstruction insn2 :
                                        pred2Block.getInstructions()) {
                                    if (ArkOpcodesCompat.isGetAsyncIterator(
                                            insn2.getOpcode())) {
                                        hasAsyncIterator = true;
                                        setupBlock = pred2Block;
                                        break;
                                    }
                                }
                            }
                            if (hasAsyncIterator) {
                                break;
                            }
                        }
                    }
                }
                if (hasAsyncIterator) {
                    break;
                }
            }
        }
        if (!hasAsyncIterator) {
            return null;
        }
        String iterVarName = null;
        String itemVarName = null;
        List<ArkInstruction> searchInsns = setupBlock != null
                ? setupBlock.getInstructions()
                : condBlock.getInstructions();
        for (int i = 0; i < searchInsns.size() - 1; i++) {
            ArkInstruction insn = searchInsns.get(i);
            if (ArkOpcodesCompat.isGetAsyncIterator(insn.getOpcode())) {
                if (i + 1 < searchInsns.size()) {
                    ArkInstruction nextInsn = searchInsns.get(i + 1);
                    if (nextInsn.getOpcode()
                            == ArkOpcodesCompat.STA) {
                        iterVarName = ctx.resolveRegisterName(
                                (int) nextInsn.getOperands()
                                        .get(0).getValue());
                    }
                }
                break;
            }
        }
        for (int i = 0; i < condBlock.getInstructions().size();
                i++) {
            ArkInstruction insn =
                    condBlock.getInstructions().get(i);
            if (insn.getOpcode()
                    == ArkOpcodesCompat.GETNEXTPROPNAME) {
                if (i + 1 < condBlock.getInstructions().size()) {
                    ArkInstruction nextInsn =
                            condBlock.getInstructions().get(i + 1);
                    if (nextInsn.getOpcode()
                            == ArkOpcodesCompat.STA) {
                        itemVarName = ctx.resolveRegisterName(
                                (int) nextInsn.getOperands()
                                        .get(0).getValue());
                    }
                }
            }
        }
        if (itemVarName == null) {
            for (int i = 0; i < loopBody.getInstructions().size();
                    i++) {
                ArkInstruction insn =
                        loopBody.getInstructions().get(i);
                if (insn.getOpcode()
                        == ArkOpcodesCompat.GETNEXTPROPNAME) {
                    if (i + 1 < loopBody.getInstructions().size()) {
                        ArkInstruction nextInsn =
                                loopBody.getInstructions().get(i + 1);
                        if (nextInsn.getOpcode()
                                == ArkOpcodesCompat.STA) {
                            itemVarName = ctx.resolveRegisterName(
                                    (int) nextInsn.getOperands()
                                            .get(0).getValue());
                        }
                    }
                    break;
                }
            }
        }
        if (itemVarName == null) {
            itemVarName = "item";
        }
        ArkTSExpression iterableExpr =
                new ArkTSExpression.VariableExpression(
                        iterVarName != null ? iterVarName
                                : "asyncIterable");
        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType
                                .FOR_AWAIT_OF_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.iteratorVarName = itemVarName;
        p.iterableExpr = iterableExpr;
        p.iteratorSetupBlock = setupBlock;
        return p;
    }

    // --- For-await-of processing ---

    /**
     * Processes a for-await-of loop: emits
     * for await (const x of asyncIterable) { body }.
     *
     * @param condBlock the loop condition/header block
     * @param pattern the detected for-await-of pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @param visited set of already-visited blocks
     * @return the generated statements
     */
    List<ArkTSStatement> processForAwaitOfLoop(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcludingIterator(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);
        String forAwaitOfLabel = pushLabeledLoopContext(ctx,
                loopHeaderOffset, loopEndOffset);
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ctx.popLoopContext();
        ArkTSStatement forAwaitOfStmt =
                new ArkTSControlFlow.ForAwaitOfStatement(
                        "const", pattern.iteratorVarName,
                        pattern.iterableExpr, bodyBlock);
        stmts.add(wrapLabel(forAwaitOfStmt, forAwaitOfLabel));
        return stmts;
    }

    // --- Loop offset helpers ---

    private int estimateLoopEndOffset(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            ControlFlowGraph cfg) {
        List<CFGEdge> successors = condBlock.getSuccessors();
        if (successors.size() == 2) {
            int target1 = successors.get(0).getToOffset();
            int target2 = successors.get(1).getToOffset();
            if (target1 <= condBlock.getStartOffset()) {
                return findLoopExit(pattern.trueBlock, condBlock, cfg);
            }
            if (target2 <= condBlock.getStartOffset()) {
                return findLoopExit(pattern.trueBlock, condBlock, cfg);
            }
            return Math.max(target1, target2);
        }
        if (pattern.trueBlock != null) {
            return pattern.trueBlock.getEndOffset();
        }
        return condBlock.getEndOffset();
    }

    private int findLoopExit(BasicBlock bodyBlock,
            BasicBlock headerBlock, ControlFlowGraph cfg) {
        if (bodyBlock == null) {
            return headerBlock.getEndOffset();
        }
        int maxOffset = bodyBlock.getEndOffset();
        for (CFGEdge edge : bodyBlock.getSuccessors()) {
            if (edge.getToOffset() > maxOffset) {
                maxOffset = edge.getToOffset();
            }
        }
        return maxOffset;
    }

    // --- Labeled loop helpers ---

    private String pushLabeledLoopContext(DecompilationContext ctx,
            int headerOffset, int endOffset) {
        String label = null;
        if (!ctx.loopContextStack.isEmpty()) {
            label = ctx.generateLoopLabel();
        }
        ctx.pushLoopContext(headerOffset, endOffset, label);
        return label;
    }

    private static ArkTSStatement wrapLabel(
            ArkTSStatement stmt, String label) {
        if (label != null) {
            return new ArkTSStatement.LabeledStatement(label, stmt);
        }
        return stmt;
    }
}
