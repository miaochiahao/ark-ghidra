package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Reconstructs structured control flow from the CFG into ArkTS statements.
 *
 * <p>Detects if/else, while, for, do/while, for-of, for-in, switch,
 * ternary, short-circuit, and try/catch patterns from the raw CFG and
 * produces structured ArkTS statement nodes.
 *
 * <p>Pattern-specific processing is delegated to:
 * <ul>
 *   <li>{@link BranchProcessor} -- if/else, ternary, short-circuit</li>
 *   <li>{@link LoopProcessor} -- while, for, do-while, for-of, for-in</li>
 *   <li>{@link SwitchProcessor} -- switch/case</li>
 *   <li>{@link TryCatchProcessor} -- try/catch/finally</li>
 * </ul>
 *
 * <p>Block instruction processing is delegated to
 * {@link BlockInstructionProcessor}.
 */
class ControlFlowReconstructor {

    static final String ACC = "acc";

    private final ArkTSDecompiler decompiler;

    private final BlockInstructionProcessor blockProc;
    private final BranchProcessor branchProcessor;
    private final LoopProcessor loopProcessor;
    private final SwitchProcessor switchProcessor;
    private final TryCatchProcessor tryCatchProcessor;

    ControlFlowReconstructor(ArkTSDecompiler decompiler,
            InstructionHandler instrHandler) {
        this.decompiler = decompiler;
        this.blockProc = new BlockInstructionProcessor(instrHandler);
        this.branchProcessor = new BranchProcessor(this);
        this.loopProcessor = new LoopProcessor(this);
        this.switchProcessor = new SwitchProcessor(this);
        this.tryCatchProcessor = new TryCatchProcessor(this, decompiler);
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
                tryCatchProcessor.buildTryCatchRegions(ctx, cfg);

        for (BasicBlock block : blocks) {
            if (visited.contains(block)) {
                continue;
            }

            if (isDeadCode(block, cfg, visited)) {
                visited.add(block);
                continue;
            }

            TryCatchRegion tcr = tryCatchProcessor.findTryCatchRegion(
                    block.getStartOffset(), tryCatchRegions);
            if (tcr != null && !tcr.isProcessed()) {
                tcr.markProcessed();
                visited.add(block);
                stmts.addAll(tryCatchProcessor.processTryCatch(
                        tcr, ctx, cfg, visited));
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
                    stmts.addAll(branchProcessor.processIfElse(block,
                            pattern, ctx, cfg, visited));
                    break;
                case IF_ONLY:
                    visited.add(block);
                    stmts.addAll(branchProcessor.processIfOnly(block,
                            pattern, ctx, cfg, visited));
                    break;
                case WHILE_LOOP:
                    visited.add(block);
                    stmts.addAll(loopProcessor.processWhileLoop(block,
                            pattern, ctx, cfg, visited));
                    break;
                case FOR_LOOP:
                    visited.add(block);
                    stmts.addAll(loopProcessor.processForLoop(block,
                            pattern, ctx, cfg, visited));
                    break;
                case DO_WHILE:
                    visited.add(block);
                    stmts.addAll(loopProcessor.processDoWhile(block,
                            pattern, ctx, cfg, visited));
                    break;
                case FOR_OF_LOOP:
                    visited.add(block);
                    stmts.addAll(loopProcessor.processForOfLoop(block,
                            pattern, ctx, cfg, visited));
                    break;
                case FOR_IN_LOOP:
                    visited.add(block);
                    stmts.addAll(loopProcessor.processForInLoop(block,
                            pattern, ctx, cfg, visited));
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
                    stmts.addAll(branchProcessor.processTernary(block,
                            pattern, ctx, cfg, visited));
                    break;
                case SHORT_CIRCUIT_AND:
                    visited.add(block);
                    stmts.addAll(
                            branchProcessor.processShortCircuitAnd(
                                    block, pattern, ctx, cfg, visited));
                    break;
                case SHORT_CIRCUIT_OR:
                    visited.add(block);
                    stmts.addAll(
                            branchProcessor.processShortCircuitOr(
                                    block, pattern, ctx, cfg, visited));
                    break;
                case SWITCH:
                    visited.add(block);
                    stmts.addAll(switchProcessor.processSwitch(block,
                            pattern, ctx, cfg, visited));
                    break;
                default:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    break;
            }
        }

        return stmts;
    }

    // --- Pattern detection (delegates to sub-processors) ---

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
                ControlFlowPattern forOfP =
                        loopProcessor.detectForOfPattern(
                                block, loopBody, cfg);
                if (forOfP != null) {
                    return forOfP;
                }
                ControlFlowPattern forInP =
                        loopProcessor.detectForInPattern(
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

            ControlFlowPattern switchP =
                    switchProcessor.detectSwitchPattern(
                            block, trueBranch, falseBranch, cfg);
            if (switchP != null) {
                return switchP;
            }

            ControlFlowPattern ternaryP =
                    branchProcessor.detectTernaryPattern(
                            block, trueBranch, falseBranch, cfg);
            if (ternaryP != null) {
                return ternaryP;
            }

            ControlFlowPattern shortCircuitP =
                    branchProcessor.detectShortCircuitPattern(
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

    // --- Condition extraction (shared by processors) ---

    ArkTSExpression getConditionExpression(
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

    boolean isBranchOnFalse(int opcode) {
        return opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16;
    }

    // --- Block instruction processing (delegates to BlockInstructionProcessor) ---

    List<ArkTSStatement> processBlockInstructions(BasicBlock block,
            DecompilationContext ctx) {
        return blockProc.processBlockInstructions(block, ctx);
    }

    List<ArkTSStatement> processBlockInstructionsExcluding(
            BasicBlock block, DecompilationContext ctx,
            ArkInstruction excludeInsn) {
        return blockProc.processBlockInstructionsExcluding(
                block, ctx, excludeInsn);
    }

    List<ArkTSStatement> processBlockInstructionsExcludingIterator(
            BasicBlock block, DecompilationContext ctx,
            ArkInstruction excludeInsn) {
        return blockProc.processBlockInstructionsExcludingIterator(
                block, ctx, excludeInsn);
    }

    ArkTSExpression extractBlockValue(BasicBlock block,
            DecompilationContext ctx) {
        return blockProc.extractBlockValue(block, ctx);
    }

    // --- Helpers ---

    private BasicBlock getSuccessorByType(List<CFGEdge> edges,
            EdgeType type) {
        for (CFGEdge edge : edges) {
            if (edge.getType() == type) {
                return null;
            }
        }
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
}
