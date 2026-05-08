package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;

/**
 * Processes basic block instructions into ArkTS statements.
 *
 * <p>Handles the instruction-by-instruction walk of a basic block,
 * tracking the accumulator value, producing statements, and
 * handling special cases like break/continue jumps, destructuring
 * patterns, and iterator-specific instruction filtering.
 *
 * <p>Shared by {@link ControlFlowReconstructor} and the
 * pattern-specific processor classes.
 */
class BlockInstructionProcessor {

    private final InstructionHandler instrHandler;

    BlockInstructionProcessor(InstructionHandler instrHandler) {
        this.instrHandler = instrHandler;
    }

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
        List<ArkInstruction> instructions = block.getInstructions();

        for (int idx = 0; idx < instructions.size(); idx++) {
            ArkInstruction insn = instructions.get(idx);
            if (insn == excludeInsn) {
                continue;
            }

            int opcode = insn.getOpcode();

            if (opcode == ArkOpcodesCompat.NOP) {
                continue;
            }

            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                if (isBreakJump(insn, ctx)) {
                    stmts.add(new ArkTSStatement.BreakStatement());
                    continue;
                }
                if (isContinueJump(insn, ctx)) {
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
                if (accValue != null) {
                    accValue = instrHandler
                            .tryReconstructTemplateLiteral(accValue);
                }
                stmts.add(new ArkTSStatement.ReturnStatement(accValue));
                accValue = null;
                continue;
            }
            if (opcode == ArkOpcodesCompat.RETURNUNDEFINED) {
                stmts.add(new ArkTSStatement.ReturnStatement(null));
                accValue = null;
                continue;
            }

            // Try to detect destructuring patterns starting at LDA
            if (opcode == ArkOpcodesCompat.LDA) {
                ObjectCreationHandler.DestructuringResult destrResult =
                        tryDetectDestructuring(instructions, idx,
                                ctx, declaredVars);
                if (destrResult != null) {
                    stmts.add(destrResult.statement);
                    accValue = null;
                    idx += destrResult.instructionsConsumed - 1;
                    continue;
                }
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

    List<ArkTSStatement> processBlockInstructionsExcludingIterator(
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
                if (isBreakJump(insn, ctx)) {
                    stmts.add(new ArkTSStatement.BreakStatement());
                    continue;
                }
                if (isContinueJump(insn, ctx)) {
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
                if (accValue != null) {
                    accValue = instrHandler
                            .tryReconstructTemplateLiteral(accValue);
                }
                stmts.add(new ArkTSStatement.ReturnStatement(accValue));
                accValue = null;
                continue;
            }
            if (opcode == ArkOpcodesCompat.RETURNUNDEFINED) {
                stmts.add(new ArkTSStatement.ReturnStatement(null));
                accValue = null;
                continue;
            }

            // Try to detect destructuring patterns starting at LDA
            if (opcode == ArkOpcodesCompat.LDA) {
                ObjectCreationHandler.DestructuringResult destrResult =
                        tryDetectDestructuring(instructions, idx,
                                ctx, declaredVars);
                if (destrResult != null) {
                    stmts.add(destrResult.statement);
                    accValue = null;
                    idx += destrResult.instructionsConsumed - 1;
                    continue;
                }
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

    ArkTSExpression extractBlockValue(BasicBlock block,
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
        if (accValue != null) {
            accValue = instrHandler
                    .tryReconstructTemplateLiteral(accValue);
        }
        return accValue;
    }

    /**
     * Attempts to detect either an array or object destructuring
     * pattern starting at the given instruction index.
     *
     * @param instructions the instruction list
     * @param idx the current instruction index
     * @param ctx the decompilation context
     * @param declaredVars the set of already-declared variables
     * @return a DestructuringResult if detected, null otherwise
     */
    private ObjectCreationHandler.DestructuringResult
            tryDetectDestructuring(List<ArkInstruction> instructions,
                    int idx, DecompilationContext ctx,
                    Set<String> declaredVars) {

        // Need at least 3 instructions for a pattern:
        // lda + ldobjby* + sta
        if (idx + 2 >= instructions.size()) {
            return null;
        }

        ArkInstruction nextInsn = instructions.get(idx + 1);
        int nextOpcode = nextInsn.getOpcode();

        // Try array destructuring first (ldobjbyindex pattern)
        if (nextOpcode == ArkOpcodesCompat.LDOBJBYINDEX) {
            ObjectCreationHandler.DestructuringResult result =
                    instrHandler.tryDetectArrayDestructuringInBlock(
                            instructions, idx, ctx, declaredVars);
            if (result != null) {
                return result;
            }
        }

        // Try object destructuring (ldobjbyname pattern)
        if (nextOpcode == ArkOpcodesCompat.LDOBJBYNAME) {
            ObjectCreationHandler.DestructuringResult result =
                    instrHandler.tryDetectObjectDestructuringInBlock(
                            instructions, idx, ctx, declaredVars);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private boolean isBreakJump(ArkInstruction insn,
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
            DecompilationContext ctx) {
        int[] loopCtx = ctx.getCurrentLoopContext();
        if (loopCtx == null) {
            return false;
        }
        int loopHeader = loopCtx[0];
        int loopEnd = loopCtx[1];
        int target = ControlFlowGraph.getJumpTargetPublic(insn);
        return target == loopHeader && target < loopEnd;
    }
}
