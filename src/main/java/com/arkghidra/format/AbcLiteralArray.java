package com.arkghidra.format;

import java.util.List;

/**
 * LiteralArray: array of typed literal values.
 */
public class AbcLiteralArray {
    private final long numLiterals;
    private final List<byte[]> literals;

    public AbcLiteralArray(long numLiterals, List<byte[]> literals) {
        this.numLiterals = numLiterals;
        this.literals = literals;
    }

    public long getNumLiterals() {
        return numLiterals;
    }
    public List<byte[]> getLiterals() {
        return literals;
    }
}
