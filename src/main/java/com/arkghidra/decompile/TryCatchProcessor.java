package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcTryBlock;

/**
 * Processes try/catch control flow patterns.
 *
 * <p>Builds try/catch regions from ABC exception table data,
 * finds blocks belonging to try bodies and catch handlers,
 * and generates structured try/catch statements including
 * support for nested regions.
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
            List<ControlFlowReconstructor.CatchHandler> handlers =
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
                    handlers.add(
                            new ControlFlowReconstructor.CatchHandler(
                                    typeName,
                                    (int) catchBlock.getHandlerPc()));
                }
            }
            if (finallyHandler != null && handlers.isEmpty()) {
                handlers.add(finallyHandler);
            }
            if (!handlers.isEmpty()) {
                ControlFlowReconstructor.TryCatchRegion region =
                        new ControlFlowReconstructor.TryCatchRegion(
                                startPc, endPc, handlers);
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

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ControlFlowReconstructor.TryCatchRegion> allRegions =
                buildTryCatchRegions(ctx, cfg);

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
        String catchParam = "e";
        if (!tcr.handlers.isEmpty()) {
            ControlFlowReconstructor.CatchHandler firstHandler =
                    tcr.handlers.get(0);
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
                        reconstructor.processBlockInstructions(
                                handlerBlock, ctx));
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
}
