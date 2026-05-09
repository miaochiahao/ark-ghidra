package com.arkghidra.format;

import java.util.List;

/**
 * LiteralArray: array of tagged literal values.
 * Each literal has a type tag and a raw byte value.
 */
public class AbcLiteralArray {
    private final long numLiterals;
    private final List<byte[]> literals;
    private final List<Integer> tags;

    public AbcLiteralArray(long numLiterals, List<byte[]> literals,
            List<Integer> tags) {
        this.numLiterals = numLiterals;
        this.literals = literals;
        this.tags = tags;
    }

    public long getNumLiterals() {
        return numLiterals;
    }

    public List<byte[]> getLiterals() {
        return literals;
    }

    public List<Integer> getTags() {
        return tags;
    }

    /**
     * Returns the type tag for the literal at the given index.
     *
     * @param index the literal index
     * @return the tag value, or 0 if out of range
     */
    public int getTag(int index) {
        if (tags != null && index >= 0 && index < tags.size()) {
            return tags.get(index);
        }
        return 0;
    }

    /**
     * Returns the raw byte value for the literal at the given index.
     *
     * @param index the literal index
     * @return the value bytes, or null if out of range
     */
    public byte[] getValue(int index) {
        if (index >= 0 && index < literals.size()) {
            return literals.get(index);
        }
        return null;
    }

    /**
     * Returns the number of literal entries (pairs of tag+value).
     */
    public int size() {
        return literals.size();
    }
}
