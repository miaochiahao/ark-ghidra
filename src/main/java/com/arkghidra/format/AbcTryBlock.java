package com.arkghidra.format;

import java.util.List;

public class AbcTryBlock {
    private final long startPc;
    private final long length;
    private final List<AbcCatchBlock> catchBlocks;

    public AbcTryBlock(long startPc, long length, List<AbcCatchBlock> catchBlocks) {
        this.startPc = startPc;
        this.length = length;
        this.catchBlocks = catchBlocks;
    }

    public long getStartPc() {
        return startPc;
    }
    public long getLength() {
        return length;
    }
    public List<AbcCatchBlock> getCatchBlocks() {
        return catchBlocks;
    }
}
