package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcTryBlock;

/**
 * Processes try/catch control flow patterns.
 *
 * <p>Builds try/catch regions from ABC exception table data,
 * finds blocks belonging to try bodies and catch handlers,
 * and generates structured try/catch statements including
 * support for nested regions, finally blocks, typed catch
 * parameters, multi-catch blocks, and throw expressions.
 *
 * <p>Called from {@link ControlFlowReconstructor} when
 * try/catch regions are detected.
 */
class TryCatchProcessor {

    private final ControlFlowReconstructor reconstructor;
    private final ArkTSDecompiler decompiler;

    TryCatchProcessor(ControlFlowReconstructor reconstructor,
            ArkTSDecompiler decompiler) {
        this.reconstructor = reconstructor;
        this.decompiler = decompiler;
    }

    // --- Region building ---

    List<ControlFlowReconstructor.TryCatchRegion> buildTryCatchRegions(
            DecompilationContext ctx, ControlFlowGraph cfg) {
        List<ControlFlowReconstructor.TryCatchRegion> regions =
                new ArrayList<>();
        if (ctx.code == null || ctx.code.getTryBlocks() == null) {
            return regions;
        }
        for (AbcTryBlock tryBlock : ctx.code.getTryBlocks()) {
            int startPc = (int) tryBlock.getStartPc();
            int endPc = startPc + (int) tryBlock.getLength();
            List<ControlFlowReconstructor.CatchHandler> typedHandlers =
                    new ArrayList<>();
            ControlFlowReconstructor.CatchHandler finallyHandler = null;
            for (AbcCatchBlock catchBlock :
                    tryBlock.getCatchBlocks()) {
                String typeName = null;
                if (catchBlock.getTypeIdx() > 0
                        && ctx.abcFile != null) {
                    typeName = decompiler.resolveTypeName(
                            (int) catchBlock.getTypeIdx(), ctx.abcFile);
                }
                if (catchBlock.isCatchAll()) {
                    finallyHandler =
                            new ControlFlowReconstructor.CatchHandler(
                                    typeName,
                                    (int) catchBlock.getHandlerPc());
                } else {
                    typedHandlers.add(
                            new ControlFlowReconstructor.CatchHandler(
                                    typeName,
                                    (int) catchBlock.getHandlerPc()));
                }
            }

            // Build region with both typed and finally handlers
            List<ControlFlowReconstructor.CatchHandler> allHandlers =
                    new ArrayList<>(typedHandlers);
            // Track finally handler separately for try/finally support
            ControlFlowReconstructor.TryCatchRegion region =
                    new ControlFlowReconstructor.TryCatchRegion(
                            startPc, endPc, allHandlers);
            region.setFinallyHandler(finallyHandler);
            // If only catch-all exists (no typed catches), treat it as a
            // catch handler for try/finally pattern
            if (finallyHandler != null && typedHandlers.isEmpty()) {
                allHandlers.add(finallyHandler);
                region.setFinallyOnly(true);
            }
            if (!allHandlers.isEmpty()) {
                regions.add(region);
            }
        }
        return regions;
    }

    // --- Region lookup ---

    ControlFlowReconstructor.TryCatchRegion findTryCatchRegion(
            int offset,
            List<ControlFlowReconstructor.TryCatchRegion> regions) {
        for (ControlFlowReconstructor.TryCatchRegion region : regions) {
            if (region.startPc == offset && !region.isProcessed()) {
                return region;
            }
        }
        return null;
    }

    // --- Try/catch processing ---

    List<ArkTSStatement> processTryCatch(
            ControlFlowReconstructor.TryCatchRegion tcr,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        // Check if this try-catch is a compiler-generated iterator
        // cleanup wrapper around a for-of/for-in loop. If the try body
        // contains iterator opcodes and the catch is just cleanup+rethrow,
        // process the inner blocks through pattern detection instead.
        if (isIteratorCleanupWrapper(tcr, ctx, cfg)) {
            return processIteratorCleanupWrapper(
                    tcr, ctx, cfg, visited);
        }

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ControlFlowReconstructor.TryCatchRegion> allRegions =
                buildTryCatchRegions(ctx, cfg);

        // Build try body from blocks within the try range
        List<ArkTSStatement> tryBodyStmts = new ArrayList<>();
        for (BasicBlock block : cfg.getBlocks()) {
            if (block.getStartOffset() >= tcr.startPc
                    && block.getStartOffset() < tcr.endPc
                    && !visited.contains(block)) {
                ControlFlowReconstructor.TryCatchRegion nestedTcr =
                        findTryCatchRegion(
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
                            reconstructor.processBlockInstructions(
                                    block, ctx));
                }
            }
        }
        ArkTSStatement tryBody =
                new ArkTSStatement.BlockStatement(tryBodyStmts);

        ArkTSStatement catchBody = null;
        ArkTSStatement finallyBody = null;
        String catchParam = "e";
        String catchParamType = null;

        // Build finally body from catch-all handler if present
        // and distinct from typed handlers (try-catch-finally)
        ControlFlowReconstructor.CatchHandler finallyHandler =
                tcr.getFinallyHandler();

        // Separate truly typed handlers from catch-all that was
        // merged in the finally-only case
        List<ControlFlowReconstructor.CatchHandler> trulyTyped =
                new ArrayList<>();
        for (ControlFlowReconstructor.CatchHandler h :
                tcr.handlers) {
            if (tcr.isFinallyOnly() && finallyHandler != null
                    && h.handlerPc == finallyHandler.handlerPc) {
                continue;
            }
            trulyTyped.add(h);
        }

        if (finallyHandler != null && !tcr.isFinallyOnly()) {
            BasicBlock finallyBlock =
                    cfg.getBlockAt(finallyHandler.handlerPc);
            if (finallyBlock != null
                    && !visited.contains(finallyBlock)) {
                visited.add(finallyBlock);
                List<ArkTSStatement> finallyStmts =
                        reconstructor.processBlockInstructions(
                                finallyBlock, ctx);
                if (!finallyStmts.isEmpty()) {
                    finallyBody =
                            new ArkTSStatement.BlockStatement(
                                    finallyStmts);
                }
            }
        }

        // Multiple typed handlers: use MultiCatchTryCatchStatement
        if (trulyTyped.size() > 1) {
            List<ArkTSControlFlow
                    .MultiCatchTryCatchStatement.CatchClause>
                    clauses = new ArrayList<>();
            for (ControlFlowReconstructor.CatchHandler handler :
                    trulyTyped) {
                String paramType = filterTypeName(
                        handler.typeName);
                BasicBlock handlerBlock =
                        cfg.getBlockAt(handler.handlerPc);
                ArkTSStatement handlerBody = null;
                if (handlerBlock != null
                        && !visited.contains(handlerBlock)) {
                    visited.add(handlerBlock);
                    List<ArkTSStatement> handlerStmts =
                            reconstructor.processBlockInstructions(
                                    handlerBlock, ctx);
                    if (!handlerStmts.isEmpty()) {
                        handlerBody =
                                new ArkTSStatement.BlockStatement(
                                        handlerStmts);
                    }
                }
                clauses.add(
                        new ArkTSControlFlow
                                .MultiCatchTryCatchStatement
                                .CatchClause("e", paramType,
                                handlerBody));
            }
            ArkTSControlFlow.MultiCatchTryCatchStatement multiCatch =
                    new ArkTSControlFlow
                            .MultiCatchTryCatchStatement(tryBody,
                            clauses, finallyBody);
            stmts.add(multiCatch);
            return stmts;
        }

        // Single handler (original path)
        if (!tcr.handlers.isEmpty()) {
            ControlFlowReconstructor.CatchHandler firstHandler =
                    tcr.handlers.get(0);
            catchParamType = filterTypeName(
                    firstHandler.typeName);
            catchBody = buildHandlerBody(tcr.handlers, ctx, cfg,
                    visited);
        }

        ArkTSControlFlow.TryCatchStatement tryCatch =
                new ArkTSControlFlow.TryCatchStatement(
                        tryBody, catchParam, catchParamType,
                        catchBody, finallyBody);
        stmts.add(tryCatch);
        return stmts;
    }

    // --- Helpers ---

    /**
     * Builds a block statement from handler blocks. Processes the first
     * handler block and any additional handler blocks for multi-catch.
     */
    private ArkTSStatement buildHandlerBody(
            List<ControlFlowReconstructor.CatchHandler> handlers,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> bodyStmts = new ArrayList<>();
        for (ControlFlowReconstructor.CatchHandler handler : handlers) {
            BasicBlock handlerBlock =
                    cfg.getBlockAt(handler.handlerPc);
            if (handlerBlock != null
                    && !visited.contains(handlerBlock)) {
                visited.add(handlerBlock);
                bodyStmts.addAll(
                        reconstructor.processBlockInstructions(
                                handlerBlock, ctx));
            }
        }
        if (!bodyStmts.isEmpty()) {
            return new ArkTSStatement.BlockStatement(bodyStmts);
        }
        return null;
    }

    /**
     * Filters type names for catch parameter annotations.
     * Returns null for generic types like "Object" that provide
     * no useful information, or for null/empty type names.
     *
     * @param typeName the resolved type name (may be null)
     * @return the type name if informative, null otherwise
     */
    private static String filterTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }
        // "Object" is the default catch-all type and provides
        // no useful information in a catch clause
        if ("Object".equals(typeName)) {
            return null;
        }
        return typeName;
    }

    /**
     * Checks if a try-catch region is a compiler-generated iterator
     * cleanup wrapper. The pattern is: try body contains iterator
     * opcodes (getiterator, closeiterator, getnextpropertyname)
     * and the catch body is just cleanup + rethrow.
     */
    private boolean isIteratorCleanupWrapper(
            ControlFlowReconstructor.TryCatchRegion tcr,
            DecompilationContext ctx, ControlFlowGraph cfg) {
        boolean hasIteratorOp = false;
        for (BasicBlock block : cfg.getBlocks()) {
            if (block.getStartOffset() >= tcr.startPc
                    && block.getStartOffset() < tcr.endPc) {
                for (ArkInstruction insn : block.getInstructions()) {
                    int opcode = insn.getOpcode();
                    if (ArkOpcodesCompat.isGetIterator(opcode)
                            || ArkOpcodesCompat.isCloseIterator(opcode)
                            || opcode
                                    == ArkOpcodesCompat.GETNEXTPROPNAME) {
                        hasIteratorOp = true;
                        break;
                    }
                }
                if (hasIteratorOp) {
                    break;
                }
            }
        }
        if (!hasIteratorOp) {
            return false;
        }
        // Check catch body: should be small (cleanup + rethrow)
        for (ControlFlowReconstructor.CatchHandler handler :
                tcr.handlers) {
            BasicBlock catchBlock =
                    cfg.getBlockAt(handler.handlerPc);
            if (catchBlock != null) {
                int insnCount = catchBlock.getInstructions().size();
                // Iterator cleanup catch blocks are typically
                // 3-6 instructions: closeiterator, lda, throw
                if (insnCount > 10) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Processes an iterator cleanup wrapper by un-wrapping the
     * try-catch and processing the inner blocks through normal
     * pattern detection (for-of/for-in loops).
     */
    private List<ArkTSStatement> processIteratorCleanupWrapper(
            ControlFlowReconstructor.TryCatchRegion tcr,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        // Collect all blocks in the try range and process them
        // through the normal control flow reconstruction so
        // for-of/for-in patterns can be detected
        List<BasicBlock> innerBlocks = new ArrayList<>();
        for (BasicBlock block : cfg.getBlocks()) {
            if (block.getStartOffset() >= tcr.startPc
                    && block.getStartOffset() < tcr.endPc
                    && !visited.contains(block)) {
                innerBlocks.add(block);
            }
        }
        // Also mark catch blocks as visited (skip cleanup code)
        for (ControlFlowReconstructor.CatchHandler handler :
                tcr.handlers) {
            BasicBlock catchBlock =
                    cfg.getBlockAt(handler.handlerPc);
            if (catchBlock != null) {
                visited.add(catchBlock);
            }
        }
        if (!innerBlocks.isEmpty()) {
            stmts.addAll(reconstructor.reconstructControlFlow(
                    cfg, innerBlocks, ctx, visited));
        }
        return stmts;
    }
}
