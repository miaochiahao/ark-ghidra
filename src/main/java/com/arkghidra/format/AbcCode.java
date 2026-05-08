package com.arkghidra.format;

import java.util.List;

/**
 * Code section of a method: registers, instructions, and try/catch blocks.
 *
 * <p>Instruction bytes are stored as raw data. Decoded instructions are
 * produced lazily via {@link #getDecodedInstructions(AbcReader)} and cached
 * on first access to avoid unnecessary parsing overhead when code is loaded
 * but never decompiled.
 */
public class AbcCode {
    private final long numVregs;
    private final long numArgs;
    private final long codeSize;
    private final byte[] instructions;
    private final List<AbcTryBlock> tryBlocks;
    private final long offset;

    /**
     * Tracks whether the instruction bytes have been accessed via
     * {@link #getInstructions()}. Used by performance tests to verify
     * lazy parsing behaviour.
     */
    private volatile boolean instructionsAccessed;

    /**
     * Cached reference to the raw data array for lazy code parsing.
     * When set, a new AbcReader is created only when the instructions
     * are actually requested.
     */
    private volatile byte[] rawDataRef;
    private volatile int codeSectionOff;

    public AbcCode(long numVregs, long numArgs, long codeSize, byte[] instructions,
            List<AbcTryBlock> tryBlocks, long offset) {
        this.numVregs = numVregs;
        this.numArgs = numArgs;
        this.codeSize = codeSize;
        this.instructions = instructions;
        this.tryBlocks = tryBlocks;
        this.offset = offset;
        this.instructionsAccessed = false;
    }

    /**
     * Constructs an AbcCode that stores raw instruction bytes and supports
     * lazy re-parsing from the original file data.
     *
     * @param numVregs number of virtual registers
     * @param numArgs number of arguments
     * @param codeSize size of the bytecode in bytes
     * @param instructions the raw instruction bytes
     * @param tryBlocks try/catch blocks
     * @param offset the code section offset in the ABC file
     * @param rawDataRef reference to the full ABC file data (for lazy re-parsing)
     * @param codeSectionOff the offset within rawData where this code section starts
     */
    public AbcCode(long numVregs, long numArgs, long codeSize, byte[] instructions,
            List<AbcTryBlock> tryBlocks, long offset,
            byte[] rawDataRef, int codeSectionOff) {
        this(numVregs, numArgs, codeSize, instructions, tryBlocks, offset);
        this.rawDataRef = rawDataRef;
        this.codeSectionOff = codeSectionOff;
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

    /**
     * Returns the raw instruction bytes.
     *
     * <p>Callers that need decoded {@link com.arkghidra.disasm.ArkInstruction}
     * objects should use {@link #getDecodedInstructions(AbcReader)} instead,
     * which avoids double-parsing when the code was lazily loaded.
     *
     * @return the raw bytecode bytes
     */
    public byte[] getInstructions() {
        instructionsAccessed = true;
        return instructions;
    }

    /**
     * Returns whether {@link #getInstructions()} has been called.
     * Used for testing lazy parsing behaviour.
     *
     * @return true if instructions have been accessed
     */
    public boolean isInstructionsAccessed() {
        return instructionsAccessed;
    }

    public List<AbcTryBlock> getTryBlocks() {
        return tryBlocks;
    }
    public long getOffset() {
        return offset;
    }

    /**
     * Returns a freshly parsed AbcCode from the original file data, or this
     * instance if the raw data reference is not set.
     *
     * <p>This enables lazy code parsing: the loader can create lightweight
     * AbcCode objects that only store the code section offset, and the full
     * instruction bytes are re-parsed on demand.
     *
     * @param reader an AbcReader positioned at the ABC data (position is saved/restored)
     * @return a fully parsed AbcCode, or this instance if no raw data reference
     */
    public AbcCode getDecodedInstructions(AbcReader reader) {
        if (rawDataRef == null) {
            instructionsAccessed = true;
            return this;
        }
        return AbcFile.parseCode(reader, codeSectionOff);
    }
}
