package com.arkghidra.format;

/**
 * A field defined in the .abc file.
 */
public class AbcField {
    private final int classIdx;
    private final int typeIdx;
    private final String name;
    private final long accessFlags;
    private final long offset;

    public AbcField(int classIdx, int typeIdx, String name, long accessFlags, long offset) {
        this.classIdx = classIdx;
        this.typeIdx = typeIdx;
        this.name = name;
        this.accessFlags = accessFlags;
        this.offset = offset;
    }

    public int getClassIdx() {
        return classIdx;
    }
    public int getTypeIdx() {
        return typeIdx;
    }
    public String getName() {
        return name;
    }
    public long getAccessFlags() {
        return accessFlags;
    }
    public long getOffset() {
        return offset;
    }
}
