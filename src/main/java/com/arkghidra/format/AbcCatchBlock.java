package com.arkghidra.format;

public class AbcCatchBlock {
    private final long typeIdx;
    private final long handlerPc;
    private final long codeSize;
    private final boolean catchAll;

    public AbcCatchBlock(long typeIdx, long handlerPc, long codeSize, boolean catchAll) {
        this.typeIdx = typeIdx;
        this.handlerPc = handlerPc;
        this.codeSize = codeSize;
        this.catchAll = catchAll;
    }

    public long getTypeIdx() {
        return typeIdx;
    }
    public long getHandlerPc() {
        return handlerPc;
    }
    public long getCodeSize() {
        return codeSize;
    }
    public boolean isCatchAll() {
        return catchAll;
    }
}
