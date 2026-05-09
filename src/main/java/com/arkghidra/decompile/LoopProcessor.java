package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;

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

    // --- For-of detection ---

    ControlFlowReconstructor.ControlFlowPattern detectForOfPattern(
            BasicBlock condBlock, BasicBlock loopBody,
            ControlFlowGraph cfg) {
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
                        iterVarName = "v" + nextInsn.getOperands()
                                .get(0).getValue();
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
                        itemVarName = "v" + nextInsn.getOperands()
                                .get(0).getValue();
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
                            itemVarName = "v" + nextInsn.getOperands()
                                    .get(0).getValue();
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
            ControlFlowGraph cfg) {
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
                            iterVarName = "v" + nextInsn.getOperands()
                                    .get(0).getValue();
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
                        keyVarName = "v" + nextInsn.getOperands()
                                .get(0).getValue();
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
                            keyVarName = "v" + nextInsn.getOperands()
                                    .get(0).getValue();
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

        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);

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
        stmts.add(whileStmt);

        return stmts;
    }

    List<ArkTSStatement> processForLoop(BasicBlock condBlock,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        if (pattern.initBlock != null) {
            stmts.addAll(reconstructor.processBlockInstructions(
                    pattern.initBlock, ctx));
            visited.add(pattern.initBlock);
        }

        ArkTSExpression condition =
                reconstructor.getConditionExpression(
                        condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(
                    ControlFlowReconstructor.ACC);
        }

        ArkTSExpression update = null;
        if (pattern.updateBlock != null) {
            List<ArkTSStatement> updateStmts =
                    reconstructor.processBlockInstructions(
                            pattern.updateBlock, ctx);
            visited.add(pattern.updateBlock);
            if (!updateStmts.isEmpty() && updateStmts.get(
                    0) instanceof ArkTSStatement.ExpressionStatement) {
                update = ((ArkTSStatement.ExpressionStatement)
                        updateStmts.get(0)).getExpression();
            }
        }

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                reconstructor.processBlockInstructions(
                        pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(condition, bodyBlock);
        stmts.add(whileStmt);

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

        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);

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
        stmts.add(doWhile);

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
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);
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
        stmts.add(forOfStmt);
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
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);
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
        stmts.add(forInStmt);
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
            ControlFlowGraph cfg) {
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
                        iterVarName = "v" + nextInsn.getOperands()
                                .get(0).getValue();
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
                        itemVarName = "v" + nextInsn.getOperands()
                                .get(0).getValue();
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
                            itemVarName = "v" + nextInsn.getOperands()
                                    .get(0).getValue();
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
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);
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
        stmts.add(forAwaitOfStmt);
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
}
