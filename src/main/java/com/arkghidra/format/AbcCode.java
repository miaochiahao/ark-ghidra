package com.arkghidra.format;

import java.util.List;

/**
 * Code section of a method: registers, instructions, and try/catch blocks.
 */
public class AbcCode {
    private final long numVregs;
    private final long numArgs;
    private final long codeSize;
    private final byte[] instructions;
    private final List<AbcTryBlock> tryBlocks;
    private final long offset;

    public AbcCode(long numVregs, long numArgs, long codeSize, byte[] instructions,
            List<AbcTryBlock> tryBlocks, long offset) {
        this.numVregs = numVregs;
        this.numArgs = numArgs;
        this.codeSize = codeSize;
        this.instructions = instructions;
        this.tryBlocks = tryBlocks;
        this.offset = offset;
    }

    public long getNumVregs() {
        return numVregs;
    }
    public long getNumArgs() {
        return numArgs;
    }
    public long getCodeSize() {
        return codeSize;
    }
    public byte[] getInstructions() {
        return instructions;
    }
    public List<AbcTryBlock> getTryBlocks() {
        return tryBlocks;
    }
    public long getOffset() {
        return offset;
    }
}
