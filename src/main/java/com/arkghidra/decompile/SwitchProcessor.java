package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Processes switch-case control flow patterns.
 *
 * <p>Handles detection of switch patterns from chains of
 * {@code jeq} instructions, extracting case values, finding the
 * switch end block, and generating structured switch statements.
 *
 * <p>Called from {@link ControlFlowReconstructor} for
 * {@code SWITCH} patterns.
 */
class SwitchProcessor {

    private final ControlFlowReconstructor reconstructor;

    SwitchProcessor(ControlFlowReconstructor reconstructor) {
        this.reconstructor = reconstructor;
    }

    // --- Switch detection ---

    ControlFlowReconstructor.ControlFlowPattern detectSwitchPattern(
            BasicBlock block, BasicBlock trueBranch,
            BasicBlock falseBranch, ControlFlowGraph cfg,
            DecompilationContext ctx) {

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

        List<ControlFlowReconstructor.SwitchCaseInfo> cases =
                new ArrayList<>();
        cases.add(new ControlFlowReconstructor.SwitchCaseInfo(
                caseValue, trueBranch));

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
                        EdgeType.CONDITIONAL_TRUE, cfg);
                BasicBlock nextFallThrough = getSuccessorByType(succs,
                        EdgeType.CONDITIONAL_FALSE, cfg);
                if (caseTarget == null) {
                    caseTarget = cfg.getBlockAt(
                            succs.get(0).getToOffset());
                    nextFallThrough = cfg.getBlockAt(
                            succs.get(1).getToOffset());
                }
                if (caseTarget == null) {
                    break;
                }

                cases.add(new ControlFlowReconstructor.SwitchCaseInfo(
                        value, caseTarget));
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
                        ctx.resolveRegisterName(compareReg));

        BasicBlock defaultBlock = current;
        BasicBlock endBlock = findSwitchEndBlock(
                cases, defaultBlock, cfg);

        ControlFlowReconstructor.ControlFlowPattern p =
                new ControlFlowReconstructor.ControlFlowPattern(
                        ControlFlowReconstructor.PatternType.SWITCH);
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
            List<ControlFlowReconstructor.SwitchCaseInfo> cases,
            BasicBlock defaultBlock, ControlFlowGraph cfg) {

        int maxOffset = 0;
        BasicBlock endBlock = null;

        for (ControlFlowReconstructor.SwitchCaseInfo sci : cases) {
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

    // --- Switch processing ---

    List<ArkTSStatement> processSwitch(BasicBlock block,
            ControlFlowReconstructor.ControlFlowPattern pattern,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts =
                reconstructor.processBlockInstructionsExcluding(
                        block, ctx, block.getLastInstruction());
        stmts.addAll(preStmts);

        List<CFGEdge> succs = block.getSuccessors();
        BasicBlock fallThrough = null;
        if (succs.size() >= 2) {
            fallThrough = getSuccessorByType(succs,
                    EdgeType.CONDITIONAL_FALSE, cfg);
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
                            EdgeType.CONDITIONAL_FALSE, cfg);
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
        int caseIdx = 0;
        while (caseIdx < pattern.switchCases.size()) {
            ControlFlowReconstructor.SwitchCaseInfo sci =
                    pattern.switchCases.get(caseIdx);
            if (sci.targetBlock == null
                    || processedCaseBodies.contains(sci.targetBlock)
                    || visited.contains(sci.targetBlock)) {
                caseIdx++;
                continue;
            }
            List<ArkTSExpression> groupedTests = new ArrayList<>();
            groupedTests.add(sci.testValue);
            int scanIdx = caseIdx + 1;
            while (scanIdx < pattern.switchCases.size()) {
                ControlFlowReconstructor.SwitchCaseInfo nextSci =
                        pattern.switchCases.get(scanIdx);
                if (nextSci.targetBlock == sci.targetBlock) {
                    groupedTests.add(nextSci.testValue);
                    scanIdx++;
                } else {
                    break;
                }
            }
            visited.add(sci.targetBlock);
            processedCaseBodies.add(sci.targetBlock);
            List<ArkTSStatement> caseBodyStmts =
                    processSwitchCaseBody(sci.targetBlock,
                            ctx, switchEndOffset, cfg, visited);
            if (groupedTests.size() == 1) {
                switchCases.add(
                        new ArkTSControlFlow.SwitchStatement.SwitchCase(
                                sci.testValue, caseBodyStmts));
            } else {
                switchCases.add(
                        new ArkTSControlFlow.SwitchStatement.SwitchCase(
                                groupedTests, caseBodyStmts));
            }
            caseIdx = scanIdx;
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
                        reconstructor.processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
                bodyStmts.add(new ArkTSStatement.BreakStatement());
            } else if (isFallThrough) {
                List<ArkTSStatement> blockStmts =
                        reconstructor.processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
            } else if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int jmpTarget = ControlFlowGraph
                        .getJumpTargetPublic(lastInsn);
                List<ArkTSStatement> blockStmts =
                        reconstructor.processBlockInstructionsExcluding(
                                current, ctx, lastInsn);
                bodyStmts.addAll(blockStmts);
                if (jmpTarget >= switchEndOffset) {
                    bodyStmts.add(
                            new ArkTSStatement.BreakStatement());
                }
            } else if (current.endsWithReturn()) {
                bodyStmts.addAll(
                        reconstructor.processBlockInstructions(
                                current, ctx));
                break;
            } else {
                bodyStmts.addAll(
                        reconstructor.processBlockInstructions(
                                current, ctx));

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

    // --- Helpers ---

    private BasicBlock getSuccessorByType(List<CFGEdge> edges,
            EdgeType type, ControlFlowGraph cfg) {
        for (CFGEdge edge : edges) {
            if (edge.getType() == type) {
                return cfg.getBlockAt(edge.getToOffset());
            }
        }
        return null;
    }
}
