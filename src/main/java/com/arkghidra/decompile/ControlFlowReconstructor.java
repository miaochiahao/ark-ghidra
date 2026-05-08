package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcTryBlock;

/**
 * Reconstructs structured control flow from the CFG into ArkTS statements.
 *
 * <p>Detects if/else, while, for, do/while, for-of, for-in, switch,
 * ternary, short-circuit, and try/catch patterns from the raw CFG and
 * produces structured ArkTS statement nodes.
 */
class ControlFlowReconstructor {

    private static final String ACC = "acc";

    private final ArkTSDecompiler decompiler;
    private final InstructionHandler instrHandler;

    ControlFlowReconstructor(ArkTSDecompiler decompiler,
            InstructionHandler instrHandler) {
        this.decompiler = decompiler;
        this.instrHandler = instrHandler;
    }

    // --- Inner types ---

    enum PatternType {
        LINEAR, IF_ONLY, IF_ELSE, WHILE_LOOP, FOR_LOOP,
        FOR_OF_LOOP, FOR_IN_LOOP, DO_WHILE, BREAK,
        CONTINUE, TERNARY, SHORT_CIRCUIT_AND,
        SHORT_CIRCUIT_OR, SWITCH, UNKNOWN
    }

    static class ControlFlowPattern {
        PatternType type;
        BasicBlock conditionBlock;
        BasicBlock trueBlock;
        BasicBlock falseBlock;
        BasicBlock mergeBlock;
        BasicBlock initBlock;
        BasicBlock updateBlock;

        ArkTSExpression shortCircuitLeft;
        ArkTSExpression shortCircuitRight;

        String ternaryTargetVar;
        ArkTSExpression ternaryTrueValue;
        ArkTSExpression ternaryFalseValue;
        ArkTSExpression ternaryCondition;

        ArkTSExpression switchDiscriminant;

        String iteratorVarName;
        ArkTSExpression iterableExpr;
        BasicBlock iteratorSetupBlock;
        List<SwitchCaseInfo> switchCases;
        BasicBlock switchDefaultBlock;
        BasicBlock switchEndBlock;

        ControlFlowPattern(PatternType type) {
            this.type = type;
        }
    }

    static class SwitchCaseInfo {
        final ArkTSExpression testValue;
        final BasicBlock targetBlock;

        SwitchCaseInfo(ArkTSExpression testValue,
                BasicBlock targetBlock) {
            this.testValue = testValue;
            this.targetBlock = targetBlock;
        }
    }

    static class TryCatchRegion {
        final int startPc;
        final int endPc;
        final List<CatchHandler> handlers;
        boolean processed;

        TryCatchRegion(int startPc, int endPc,
                List<CatchHandler> handlers) {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlers = handlers;
            this.processed = false;
        }

        boolean isProcessed() {
            return processed;
        }

        void markProcessed() {
            processed = true;
        }
    }

    static class CatchHandler {
        final String typeName;
        final int handlerPc;

        CatchHandler(String typeName, int handlerPc) {
            this.typeName = typeName;
            this.handlerPc = handlerPc;
        }
    }

    // --- Main reconstruction ---

    List<ArkTSStatement> reconstructControlFlow(ControlFlowGraph cfg,
            List<BasicBlock> blocks, DecompilationContext ctx,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<TryCatchRegion> tryCatchRegions =
                buildTryCatchRegions(ctx, cfg);

        for (BasicBlock block : blocks) {
            if (visited.contains(block)) {
                continue;
            }

            if (isDeadCode(block, cfg, visited)) {
                visited.add(block);
                continue;
            }

            TryCatchRegion tcr = findTryCatchRegion(
                    block.getStartOffset(), tryCatchRegions);
            if (tcr != null && !tcr.isProcessed()) {
                tcr.markProcessed();
                visited.add(block);
                stmts.addAll(processTryCatch(tcr, ctx, cfg, visited));
                continue;
            }

            ArkInstruction lastInsn = block.getLastInstruction();
            if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int target = ControlFlowGraph
                        .getJumpTargetPublic(lastInsn);
                if (target == block.getStartOffset()) {
                    visited.add(block);
                    ctx.pushLoopContext(block.getStartOffset(),
                            block.getEndOffset());
                    List<ArkTSStatement> bodyStmts =
                            processBlockInstructionsExcluding(
                                    block, ctx, lastInsn);
                    ctx.popLoopContext();
                    ArkTSStatement body =
                            new ArkTSStatement.BlockStatement(bodyStmts);
                    ArkTSExpression trueCond =
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN);
                    stmts.add(new ArkTSControlFlow.WhileStatement(
                            trueCond, body));
                    continue;
                }
            }

            ControlFlowPattern pattern = detectPattern(
                    block, cfg, visited);

            switch (pattern.type) {
                case IF_ELSE:
                    visited.add(block);
                    stmts.addAll(processIfElse(block, pattern,
                            ctx, cfg, visited));
                    break;
                case IF_ONLY:
                    visited.add(block);
                    stmts.addAll(processIfOnly(block, pattern,
                            ctx, cfg, visited));
                    break;
                case WHILE_LOOP:
                    visited.add(block);
                    stmts.addAll(processWhileLoop(block, pattern,
                            ctx, cfg, visited));
                    break;
                case FOR_LOOP:
                    visited.add(block);
                    stmts.addAll(processForLoop(block, pattern,
                            ctx, cfg, visited));
                    break;
                case DO_WHILE:
                    visited.add(block);
                    stmts.addAll(processDoWhile(block, pattern,
                            ctx, cfg, visited));
                    break;
                case FOR_OF_LOOP:
                    visited.add(block);
                    stmts.addAll(processForOfLoop(block, pattern,
                            ctx, cfg, visited));
                    break;
                case FOR_IN_LOOP:
                    visited.add(block);
                    stmts.addAll(processForInLoop(block, pattern,
                            ctx, cfg, visited));
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
                case TERNARY:
                    visited.add(block);
                    stmts.addAll(processTernary(block, pattern,
                            ctx, cfg, visited));
                    break;
                case SHORT_CIRCUIT_AND:
                    visited.add(block);
                    stmts.addAll(processShortCircuitAnd(block, pattern,
                            ctx, cfg, visited));
                    break;
                case SHORT_CIRCUIT_OR:
                    visited.add(block);
                    stmts.addAll(processShortCircuitOr(block, pattern,
                            ctx, cfg, visited));
                    break;
                case SWITCH:
                    visited.add(block);
                    stmts.addAll(processSwitch(block, pattern,
                            ctx, cfg, visited));
                    break;
                default:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    break;
            }
        }

        return stmts;
    }

    // --- Pattern detection ---

    private ControlFlowPattern detectPattern(BasicBlock block,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<CFGEdge> successors = block.getSuccessors();

        if (successors.size() == 2) {
            BasicBlock trueBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_TRUE);
            BasicBlock falseBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_FALSE);

            if (trueBranch == null || falseBranch == null) {
                trueBranch = cfg.getBlockAt(
                        successors.get(0).getToOffset());
                falseBranch = cfg.getBlockAt(
                        successors.get(1).getToOffset());
            }

            if (trueBranch == null || falseBranch == null) {
                return new ControlFlowPattern(PatternType.LINEAR);
            }

            boolean trueLeadsBack = trueBranch.getStartOffset()
                    <= block.getStartOffset()
                    || hasBackEdgeTo(trueBranch, block, cfg);
            boolean falseLeadsBack = falseBranch.getStartOffset()
                    <= block.getStartOffset()
                    || hasBackEdgeTo(falseBranch, block, cfg);
            if (trueLeadsBack || falseLeadsBack) {
                BasicBlock loopBody = trueLeadsBack
                        ^ falseLeadsBack
                        ? (trueLeadsBack ? trueBranch : falseBranch)
                        : (trueBranch.getStartOffset() > block
                                .getStartOffset() ? trueBranch
                                : falseBranch);
                ControlFlowPattern forOfP = detectForOfPattern(
                        block, loopBody, cfg);
                if (forOfP != null) {
                    return forOfP;
                }
                ControlFlowPattern forInP = detectForInPattern(
                        block, loopBody, cfg);
                if (forInP != null) {
                    return forInP;
                }
                ControlFlowPattern p = new ControlFlowPattern(
                        PatternType.WHILE_LOOP);
                p.conditionBlock = block;
                p.trueBlock = loopBody;
                return p;
            }

            ControlFlowPattern switchP = detectSwitchPattern(
                    block, trueBranch, falseBranch, cfg);
            if (switchP != null) {
                return switchP;
            }

            ControlFlowPattern ternaryP = detectTernaryPattern(
                    block, trueBranch, falseBranch, cfg);
            if (ternaryP != null) {
                return ternaryP;
            }

            ControlFlowPattern shortCircuitP =
                    detectShortCircuitPattern(
                            block, trueBranch, falseBranch,
                            cfg, visited);
            if (shortCircuitP != null) {
                return shortCircuitP;
            }

            BasicBlock merge = findMergeBlock(
                    trueBranch, falseBranch, cfg);
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

            if (trueBranch != falseBranch) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.IF_ONLY);
                p.conditionBlock = block;
                p.trueBlock = trueBranch;
                p.falseBlock = falseBranch;
                return p;
            }
        }

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

    // --- Ternary detection ---

    private ControlFlowPattern detectTernaryPattern(
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

        ControlFlowPattern p =
                new ControlFlowPattern(PatternType.TERNARY);
        p.conditionBlock = condBlock;
        p.trueBlock = trueBranch;
        p.falseBlock = falseBranch;
        p.mergeBlock = mergeBlock;
        p.ternaryTargetVar = targetVar;
        return p;
    }

    // --- Short-circuit detection ---

    private ControlFlowPattern detectShortCircuitPattern(
            BasicBlock block, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        ArkInstruction lastInsn = block.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();

        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode()
                            == ArkOpcodesCompat.JEQZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JEQZ_IMM16)) {
                int target2 = ControlFlowGraph
                        .getJumpTargetPublic(nextLast);
                if (target1 == target2) {
                    ControlFlowPattern p = new ControlFlowPattern(
                            PatternType.SHORT_CIRCUIT_AND);
                    p.conditionBlock = block;
                    p.trueBlock = falseBranch;
                    p.falseBlock = trueBranch;
                    p.mergeBlock = cfg.getBlockAt(target1);
                    return p;
                }
            }
        }

        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16) {
            int target1 = ControlFlowGraph
                    .getJumpTargetPublic(lastInsn);

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode()
                            == ArkOpcodesCompat.JNEZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JNEZ_IMM16)) {
                int target2 = ControlFlowGraph
                        .getJumpTargetPublic(nextLast);
                if (target1 == target2) {
                    ControlFlowPattern p = new ControlFlowPattern(
                            PatternType.SHORT_CIRCUIT_OR);
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

    // --- For-of / For-in detection ---

    private ControlFlowPattern detectForOfPattern(
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
        ControlFlowPattern p =
                new ControlFlowPattern(PatternType.FOR_OF_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.iteratorVarName = itemVarName;
        p.iterableExpr = iterableExpr;
        p.iteratorSetupBlock = setupBlock;
        return p;
    }

    private ControlFlowPattern detectForInPattern(
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
        ControlFlowPattern p =
                new ControlFlowPattern(PatternType.FOR_IN_LOOP);
        p.conditionBlock = condBlock;
        p.trueBlock = loopBody;
        p.iteratorVarName = keyVarName;
        p.iterableExpr = objectExpr;
        p.iteratorSetupBlock = setupBlock;
        return p;
    }

    // --- Pattern processing ---

    private List<ArkTSStatement> processIfElse(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        List<ArkTSStatement> thenStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        List<ArkTSStatement> elseStmts =
                processBlockInstructions(pattern.falseBlock, ctx);
        ArkTSStatement elseBlock =
                new ArkTSStatement.BlockStatement(elseStmts);

        ArkTSControlFlow.IfStatement ifStmt =
                new ArkTSControlFlow.IfStatement(condition, thenBlock,
                        elseBlock);
        stmts.add(ifStmt);

        return stmts;
    }

    private List<ArkTSStatement> processIfOnly(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        BasicBlock branchBlock = pattern.trueBlock;
        visited.add(branchBlock);

        List<ArkTSStatement> thenStmts =
                processBlockInstructions(branchBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
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

    private List<ArkTSStatement> processWhileLoop(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
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
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ctx.popLoopContext();

        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(effectiveCondition,
                        bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processForLoop(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        if (pattern.initBlock != null) {
            stmts.addAll(processBlockInstructions(
                    pattern.initBlock, ctx));
            visited.add(pattern.initBlock);
        }

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        ArkTSExpression update = null;
        if (pattern.updateBlock != null) {
            List<ArkTSStatement> updateStmts =
                    processBlockInstructions(
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
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(condition, bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processDoWhile(BasicBlock block,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

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
                processBlockInstructions(block, ctx);

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

        ctx.popLoopContext();

        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSControlFlow.DoWhileStatement doWhile =
                new ArkTSControlFlow.DoWhileStatement(bodyBlock, condition);
        stmts.add(doWhile);

        return stmts;
    }

    private List<ArkTSStatement> processForOfLoop(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcludingIterator(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
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

    private List<ArkTSStatement> processForInLoop(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcludingIterator(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(
                condBlock, pattern, cfg);
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);
        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
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

    // --- Ternary processing ---

    private List<ArkTSStatement> processTernary(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        int lastOpcode =
                condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
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
                    processBlockInstructions(pattern.trueBlock, ctx);
            List<ArkTSStatement> elseStmts =
                    processBlockInstructions(pattern.falseBlock, ctx);
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
        ArkTSExpression accValue = null;
        TypeInference typeInf = new TypeInference();
        Set<String> declaredVars = new HashSet<>();
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }

        for (ArkInstruction insn : block.getInstructions()) {
            int opcode = insn.getOpcode();
            if (opcode == ArkOpcodesCompat.STA) {
                continue;
            }
            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                continue;
            }
            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                continue;
            }
            InstructionHandler.StatementResult result =
                    instrHandler.processInstruction(
                            insn, ctx, accValue, declaredVars, typeInf);
            if (result != null) {
                accValue = result.newAccValue != null
                        ? result.newAccValue : accValue;
            }
        }
        return accValue;
    }

    // --- Short-circuit processing ---

    private List<ArkTSStatement> processShortCircuitAnd(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression leftCond = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(ACC);
        }

        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts =
                processBlockInstructionsExcluding(
                        secondCondBlock, ctx,
                        secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond = getConditionExpression(
                secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(ACC);
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

    private List<ArkTSStatement> processShortCircuitOr(
            BasicBlock condBlock, ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression leftCond = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(ACC);
        }

        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts =
                processBlockInstructionsExcluding(
                        secondCondBlock, ctx,
                        secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond = getConditionExpression(
                secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(ACC);
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

    // --- Switch processing ---

    private ControlFlowPattern detectSwitchPattern(
            BasicBlock block, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg) {

        ArkInstruction lastInsn = block.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }

        int opcode = lastInsn.getOpcode();

        if (opcode != ArkOpcodesCompat.JEQ_IMM8
                && opcode != ArkOpcodesCompat.JEQ_IMM16) {
            return null;
        }

        List<ArkOperand> ops = lastInsn.getOperands();
        if (ops.isEmpty()) {
            return null;
        }
        int compareReg = (int) ops.get(0).getValue();

        ArkTSExpression caseValue = extractCaseValue(block);
        if (caseValue == null) {
            return null;
        }

        List<SwitchCaseInfo> cases = new ArrayList<>();
        cases.add(new SwitchCaseInfo(caseValue, trueBranch));

        BasicBlock current = falseBranch;
        Set<Integer> visitedOffsets = new HashSet<>();
        visitedOffsets.add(block.getStartOffset());

        while (current != null && !visitedOffsets.contains(
                current.getStartOffset())) {

            visitedOffsets.add(current.getStartOffset());

            ArkInstruction currentLast = current.getLastInstruction();
            if (currentLast == null) {
                break;
            }

            int currentOpcode = currentLast.getOpcode();

            if (currentOpcode == ArkOpcodesCompat.JEQ_IMM8
                    || currentOpcode == ArkOpcodesCompat.JEQ_IMM16) {
                List<ArkOperand> currentOps =
                        currentLast.getOperands();
                if (currentOps.isEmpty()) {
                    break;
                }
                int currentReg = (int) currentOps.get(0).getValue();
                if (currentReg != compareReg) {
                    break;
                }

                ArkTSExpression value =
                        extractCaseValue(current);
                if (value == null) {
                    break;
                }

                List<CFGEdge> succs = current.getSuccessors();
                if (succs.size() != 2) {
                    break;
                }

                BasicBlock caseTarget = getSuccessorByType(succs,
                        EdgeType.CONDITIONAL_TRUE);
                BasicBlock nextFallThrough = getSuccessorByType(succs,
                        EdgeType.CONDITIONAL_FALSE);
                if (caseTarget == null) {
                    caseTarget = cfg.getBlockAt(
                            succs.get(0).getToOffset());
                    nextFallThrough = cfg.getBlockAt(
                            succs.get(1).getToOffset());
                }
                if (caseTarget == null) {
                    break;
                }

                cases.add(new SwitchCaseInfo(value, caseTarget));
                current = nextFallThrough;
            } else {
                break;
            }
        }

        if (cases.size() < 2) {
            return null;
        }

        ArkTSExpression discriminant =
                new ArkTSExpression.VariableExpression(
                        "v" + compareReg);

        BasicBlock defaultBlock = current;
        BasicBlock endBlock = findSwitchEndBlock(
                cases, defaultBlock, cfg);

        ControlFlowPattern p =
                new ControlFlowPattern(PatternType.SWITCH);
        p.switchDiscriminant = discriminant;
        p.switchCases = cases;
        p.switchDefaultBlock = defaultBlock;
        p.switchEndBlock = endBlock;
        return p;
    }

    private ArkTSExpression extractCaseValue(BasicBlock block) {
        ArkInstruction lastInsn = block.getLastInstruction();
        for (ArkInstruction insn : block.getInstructions()) {
            if (insn == lastInsn) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.LDAI) {
                return new ArkTSExpression.LiteralExpression(
                        String.valueOf(
                                insn.getOperands().get(0).getValue()),
                        ArkTSExpression.LiteralExpression
                                .LiteralKind.NUMBER);
            }
        }
        return null;
    }

    private BasicBlock findSwitchEndBlock(
            List<SwitchCaseInfo> cases,
            BasicBlock defaultBlock, ControlFlowGraph cfg) {

        int maxOffset = 0;
        BasicBlock endBlock = null;

        for (SwitchCaseInfo sci : cases) {
            if (sci.targetBlock != null) {
                int end = sci.targetBlock.getEndOffset();
                if (end > maxOffset) {
                    maxOffset = end;
                    for (CFGEdge edge :
                            sci.targetBlock.getSuccessors()) {
                        BasicBlock succ =
                                cfg.getBlockAt(edge.getToOffset());
                        if (succ != null
                                && succ.getStartOffset()
                                        >= maxOffset) {
                            maxOffset = succ.getStartOffset();
                            endBlock = succ;
                        }
                    }
                }
            }
        }

        if (defaultBlock != null) {
            int end = defaultBlock.getEndOffset();
            if (end > maxOffset) {
                maxOffset = end;
                endBlock = null;
                for (CFGEdge edge :
                        defaultBlock.getSuccessors()) {
                    BasicBlock succ =
                            cfg.getBlockAt(edge.getToOffset());
                    if (succ != null
                            && succ.getStartOffset()
                                    >= maxOffset) {
                        maxOffset = succ.getStartOffset();
                        endBlock = succ;
                    }
                }
            }
        }

        return endBlock;
    }

    private List<ArkTSStatement> processSwitch(BasicBlock block,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                processBlockInstructionsExcluding(
                        block, ctx, block.getLastInstruction());
        stmts.addAll(preStmts);

        List<CFGEdge> succs = block.getSuccessors();
        BasicBlock fallThrough = null;
        if (succs.size() >= 2) {
            fallThrough = getSuccessorByType(succs,
                    EdgeType.CONDITIONAL_FALSE);
            if (fallThrough == null) {
                fallThrough = cfg.getBlockAt(
                        succs.get(1).getToOffset());
            }
        }

        BasicBlock walkCurrent = fallThrough;
        while (walkCurrent != null) {
            ArkInstruction curLast = walkCurrent.getLastInstruction();
            if (curLast == null) {
                break;
            }
            int curOpcode = curLast.getOpcode();
            if (curOpcode == ArkOpcodesCompat.JEQ_IMM8
                    || curOpcode == ArkOpcodesCompat.JEQ_IMM16) {
                visited.add(walkCurrent);
                List<CFGEdge> curSuccs = walkCurrent.getSuccessors();
                if (curSuccs.size() >= 2) {
                    BasicBlock ft = getSuccessorByType(curSuccs,
                            EdgeType.CONDITIONAL_FALSE);
                    if (ft != null) {
                        walkCurrent = ft;
                    } else {
                        walkCurrent = cfg.getBlockAt(
                                curSuccs.get(1).getToOffset());
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        int switchEndOffset = pattern.switchEndBlock != null
                ? pattern.switchEndBlock.getStartOffset()
                : Integer.MAX_VALUE;

        ctx.pushLoopContext(-1, switchEndOffset);

        List<ArkTSControlFlow.SwitchStatement.SwitchCase> switchCases =
                new ArrayList<>();

        Set<BasicBlock> processedCaseBodies = new HashSet<>();
        for (SwitchCaseInfo sci : pattern.switchCases) {
            if (sci.targetBlock != null
                    && !processedCaseBodies.contains(sci.targetBlock)
                    && !visited.contains(sci.targetBlock)) {
                visited.add(sci.targetBlock);
                processedCaseBodies.add(sci.targetBlock);

                List<ArkTSStatement> caseBodyStmts =
                        processSwitchCaseBody(sci.targetBlock,
                                ctx, switchEndOffset, cfg, visited);
                switchCases.add(
                        new ArkTSControlFlow.SwitchStatement.SwitchCase(
                                sci.testValue, caseBodyStmts));
            }
        }

        ArkTSStatement defaultBlock = null;
        if (pattern.switchDefaultBlock != null
                && !visited.contains(pattern.switchDefaultBlock)) {
            visited.add(pattern.switchDefaultBlock);
            List<ArkTSStatement> defaultStmts =
                    processSwitchCaseBody(pattern.switchDefaultBlock,
                            ctx, switchEndOffset, cfg, visited);
            if (!defaultStmts.isEmpty()) {
                defaultBlock = new ArkTSStatement.BlockStatement(
                        defaultStmts);
            }
        }

        ctx.popLoopContext();

        if (pattern.switchEndBlock != null) {
            visited.add(pattern.switchEndBlock);
        }

        if (!switchCases.isEmpty()) {
            stmts.add(new ArkTSControlFlow.SwitchStatement(
                    pattern.switchDiscriminant, switchCases,
                    defaultBlock));
        }

        return stmts;
    }

    private List<ArkTSStatement> processSwitchCaseBody(
            BasicBlock caseBody, DecompilationContext ctx,
            int switchEndOffset, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> bodyStmts = new ArrayList<>();
        BasicBlock current = caseBody;

        while (current != null) {
            ArkInstruction lastInsn = current.getLastInstruction();
            boolean hasBreak = false;
            boolean isFallThrough = false;

            if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int jmpTarget = ControlFlowGraph
                        .getJumpTargetPublic(lastInsn);
                if (jmpTarget >= switchEndOffset) {
                    hasBreak = true;
                } else if (jmpTarget > current.getStartOffset()
                        && jmpTarget < switchEndOffset) {
                    isFallThrough = true;
                }
            }

            if (hasBreak) {
                List<ArkTSStatement> blockStmts =
                        processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
                bodyStmts.add(new ArkTSStatement.BreakStatement());
            } else if (isFallThrough) {
                List<ArkTSStatement> blockStmts =
                        processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
            } else if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int jmpTarget = ControlFlowGraph
                        .getJumpTargetPublic(lastInsn);
                List<ArkTSStatement> blockStmts =
                        processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
                if (jmpTarget >= switchEndOffset) {
                    bodyStmts.add(new ArkTSStatement.BreakStatement());
                }
            } else if (current.endsWithReturn()) {
                bodyStmts.addAll(
                        processBlockInstructions(current, ctx));
                break;
            } else {
                bodyStmts.addAll(
                        processBlockInstructions(current, ctx));

                List<CFGEdge> succs = current.getSuccessors();
                if (succs.size() == 1) {
                    BasicBlock next = cfg.getBlockAt(
                            succs.get(0).getToOffset());
                    if (next != null && !visited.contains(next)
                            && next.getStartOffset()
                                    < switchEndOffset
                            && next.getStartOffset()
                                    > current.getStartOffset()) {
                        visited.add(next);
                        current = next;
                        continue;
                    }
                }
                break;
            }

            break;
        }

        return bodyStmts;
    }

    // --- Try/catch processing ---

    List<TryCatchRegion> buildTryCatchRegions(
            DecompilationContext ctx, ControlFlowGraph cfg) {
        List<TryCatchRegion> regions = new ArrayList<>();
        if (ctx.code == null || ctx.code.getTryBlocks() == null) {
            return regions;
        }
        for (AbcTryBlock tryBlock : ctx.code.getTryBlocks()) {
            int startPc = (int) tryBlock.getStartPc();
            int endPc = startPc + (int) tryBlock.getLength();
            List<CatchHandler> handlers = new ArrayList<>();
            CatchHandler finallyHandler = null;
            for (AbcCatchBlock catchBlock :
                    tryBlock.getCatchBlocks()) {
                String typeName = null;
                if (catchBlock.getTypeIdx() > 0
                        && ctx.abcFile != null) {
                    typeName = decompiler.resolveTypeName(
                            (int) catchBlock.getTypeIdx(), ctx.abcFile);
                }
                if (catchBlock.isCatchAll()) {
                    finallyHandler = new CatchHandler(
                            typeName, (int) catchBlock.getHandlerPc());
                } else {
                    handlers.add(new CatchHandler(
                            typeName, (int) catchBlock.getHandlerPc()));
                }
            }
            if (finallyHandler != null && handlers.isEmpty()) {
                handlers.add(finallyHandler);
            }
            if (!handlers.isEmpty()) {
                TryCatchRegion region = new TryCatchRegion(
                        startPc, endPc, handlers);
                regions.add(region);
            }
        }
        return regions;
    }

    private TryCatchRegion findTryCatchRegion(int offset,
            List<TryCatchRegion> regions) {
        for (TryCatchRegion region : regions) {
            if (region.startPc == offset && !region.isProcessed()) {
                return region;
            }
        }
        return null;
    }

    private List<ArkTSStatement> processTryCatch(TryCatchRegion tcr,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<TryCatchRegion> allRegions =
                buildTryCatchRegions(ctx, cfg);

        List<ArkTSStatement> tryBodyStmts = new ArrayList<>();
        for (BasicBlock block : cfg.getBlocks()) {
            if (block.getStartOffset() >= tcr.startPc
                    && block.getStartOffset() < tcr.endPc
                    && !visited.contains(block)) {
                TryCatchRegion nestedTcr = findTryCatchRegion(
                        block.getStartOffset(), allRegions);
                if (nestedTcr != null && nestedTcr != tcr
                        && !nestedTcr.isProcessed()
                        && nestedTcr.startPc >= tcr.startPc
                        && nestedTcr.endPc <= tcr.endPc) {
                    nestedTcr.markProcessed();
                    visited.add(block);
                    tryBodyStmts.addAll(
                            processTryCatch(nestedTcr, ctx, cfg,
                                    visited));
                } else {
                    visited.add(block);
                    tryBodyStmts.addAll(
                            processBlockInstructions(block, ctx));
                }
            }
        }
        ArkTSStatement tryBody =
                new ArkTSStatement.BlockStatement(tryBodyStmts);

        ArkTSStatement catchBody = null;
        String catchParam = "e";
        if (!tcr.handlers.isEmpty()) {
            CatchHandler firstHandler = tcr.handlers.get(0);
            if (firstHandler.typeName != null) {
                catchParam = "e";
            }
            List<ArkTSStatement> catchBodyStmts = new ArrayList<>();
            BasicBlock handlerBlock =
                    cfg.getBlockAt(firstHandler.handlerPc);
            if (handlerBlock != null
                    && !visited.contains(handlerBlock)) {
                visited.add(handlerBlock);
                catchBodyStmts.addAll(
                        processBlockInstructions(handlerBlock, ctx));
            }
            if (!catchBodyStmts.isEmpty()) {
                catchBody = new ArkTSStatement.BlockStatement(
                        catchBodyStmts);
            }
        }

        ArkTSControlFlow.TryCatchStatement tryCatch =
                new ArkTSControlFlow.TryCatchStatement(
                        tryBody, catchParam, catchBody, null);
        stmts.add(tryCatch);
        return stmts;
    }

    // --- Condition extraction ---

    private ArkTSExpression getConditionExpression(
            ArkInstruction branchInsn, DecompilationContext ctx) {
        if (branchInsn == null) {
            return null;
        }
        int opcode = branchInsn.getOpcode();
        List<ArkOperand> operands = branchInsn.getOperands();

        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16) {
            return ctx.currentAccValue != null
                    ? ctx.currentAccValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }
        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16) {
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
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JNENULL_IMM8
                || opcode == ArkOpcodesCompat.JNENULL_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.UNDEFINED));
        }
        if (opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.UNDEFINED));
        }
        if (opcode == ArkOpcodesCompat.JEQ_IMM8
                || opcode == ArkOpcodesCompat.JEQ_IMM16) {
            int reg = (int) operands.get(0).getValue();
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.VariableExpression("v" + reg));
        }
        if (opcode == ArkOpcodesCompat.JNE_IMM8
                || opcode == ArkOpcodesCompat.JNE_IMM16) {
            int reg = (int) operands.get(0).getValue();
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
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
        }
        if (opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!==",
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
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

    List<ArkTSStatement> processBlockInstructions(BasicBlock block,
            DecompilationContext ctx) {
        return processBlockInstructionsExcluding(block, ctx, null);
    }

    List<ArkTSStatement> processBlockInstructionsExcluding(
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

            if (opcode == ArkOpcodesCompat.NOP) {
                continue;
            }

            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
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

            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                ctx.currentAccValue = accValue;
                continue;
            }

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

            InstructionHandler.StatementResult result;
            try {
                result = instrHandler.processInstruction(
                        insn, ctx, accValue, declaredVars, typeInf);
            } catch (Exception e) {
                String msg = "error at offset 0x"
                        + Integer.toHexString(insn.getOffset())
                        + ": " + e.getMessage();
                ctx.warnings.add(msg);
                stmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* " + msg + " */")));
                accValue = null;
                continue;
            }
            if (result != null) {
                if (result.statement != null) {
                    stmts.add(result.statement);
                }
                accValue = result.newAccValue;
            } else {
                accValue = null;
            }
        }

        ctx.currentAccValue = accValue;
        return stmts;
    }

    private List<ArkTSStatement> processBlockInstructionsExcludingIterator(
            BasicBlock block, DecompilationContext ctx,
            ArkInstruction excludeInsn) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        Set<String> declaredVars = new HashSet<>();
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }
        ArkTSExpression accValue = null;
        TypeInference typeInf = new TypeInference();
        List<ArkInstruction> instructions = block.getInstructions();
        for (int idx = 0; idx < instructions.size(); idx++) {
            ArkInstruction insn = instructions.get(idx);
            if (insn == excludeInsn) {
                continue;
            }
            int opcode = insn.getOpcode();
            if (ArkOpcodesCompat.isGetIterator(opcode)
                    || ArkOpcodesCompat.isCloseIterator(opcode)
                    || opcode == ArkOpcodesCompat.GETNEXTPROPNAME
                    || opcode == ArkOpcodesCompat.GETPROPITERATOR) {
                continue;
            }
            if (opcode == ArkOpcodesCompat.STA && idx > 0) {
                int prevOpcode = instructions.get(idx - 1).getOpcode();
                if (ArkOpcodesCompat.isGetIterator(prevOpcode)
                        || prevOpcode
                                == ArkOpcodesCompat.GETNEXTPROPNAME
                        || prevOpcode
                                == ArkOpcodesCompat.GETPROPITERATOR) {
                    continue;
                }
            }
            if (opcode == ArkOpcodesCompat.NOP) {
                continue;
            }
            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
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
            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                ctx.currentAccValue = accValue;
                continue;
            }
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
            InstructionHandler.StatementResult result =
                    instrHandler.processInstruction(
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
        ctx.currentAccValue = accValue;
        return stmts;
    }

    // --- Helpers ---

    private boolean isBreakJump(ArkInstruction insn, BasicBlock block,
            DecompilationContext ctx) {
        int[] loopCtx = ctx.getCurrentLoopContext();
        if (loopCtx == null) {
            return false;
        }
        int loopEnd = loopCtx[1];
        int target = ControlFlowGraph.getJumpTargetPublic(insn);
        return target >= loopEnd;
    }

    private boolean isContinueJump(ArkInstruction insn,
            BasicBlock block, DecompilationContext ctx) {
        int[] loopCtx = ctx.getCurrentLoopContext();
        if (loopCtx == null) {
            return false;
        }
        int loopHeader = loopCtx[0];
        int loopEnd = loopCtx[1];
        int target = ControlFlowGraph.getJumpTargetPublic(insn);
        return target == loopHeader && target < loopEnd;
    }

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
        return null;
    }

    private BasicBlock findMergeBlock(BasicBlock a, BasicBlock b,
            ControlFlowGraph cfg) {
        Set<Integer> reachableFromA = new LinkedHashSet<>();
        collectReachable(a, cfg, reachableFromA, 10);
        Set<Integer> reachableFromB = new LinkedHashSet<>();
        collectReachable(b, cfg, reachableFromB, 10);

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

    private void collectReachable(BasicBlock start,
            ControlFlowGraph cfg, Set<Integer> reachable,
            int maxDepth) {
        if (maxDepth <= 0 || start == null) {
            return;
        }
        reachable.add(start.getStartOffset());
        for (CFGEdge edge : start.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && !reachable.contains(
                    succ.getStartOffset())) {
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
        for (CFGEdge edge : from.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && succ != from) {
                for (CFGEdge inner : succ.getSuccessors()) {
                    if (inner.getToOffset()
                            == target.getStartOffset()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDeadCode(BasicBlock block, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        if (block == cfg.getEntryBlock()) {
            return false;
        }
        for (CFGEdge pred : block.getPredecessors()) {
            if (pred.getType() == EdgeType.EXCEPTION_HANDLER) {
                return false;
            }
        }
        List<CFGEdge> preds = block.getPredecessors();
        if (preds.isEmpty()) {
            return true;
        }
        for (CFGEdge pred : preds) {
            if (pred.getType() == EdgeType.EXCEPTION_HANDLER) {
                return false;
            }
            BasicBlock predBlock =
                    cfg.getBlockAt(pred.getFromOffset());
            if (predBlock == null) {
                continue;
            }
            if (!visited.contains(predBlock)) {
                return false;
            }
        }
        return true;
    }

    private int estimateLoopEndOffset(BasicBlock condBlock,
            ControlFlowPattern pattern, ControlFlowGraph cfg) {
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
