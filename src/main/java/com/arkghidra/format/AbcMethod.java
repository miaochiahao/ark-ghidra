package com.arkghidra.format;

/**
 * A method defined in the .abc file.
 */
public class AbcMethod {
    private final int classIdx;
    private final int protoIdx;
    private final String name;
    private final long accessFlags;
    private final long codeOff;
    private final long offset;

    public AbcMethod(int classIdx, int protoIdx, String name, long accessFlags,
            long codeOff, long offset) {
        this.classIdx = classIdx;
        this.protoIdx = protoIdx;
        this.name = name;
        this.accessFlags = accessFlags;
        this.codeOff = codeOff;
        this.offset = offset;
    }

    public int getClassIdx() {
        return classIdx;
    }
    public int getProtoIdx() {
        return protoIdx;
    }
    public String getName() {
        return name;
    }
    public long getAccessFlags() {
        return accessFlags;
    }
    public long getCodeOff() {
        return codeOff;
    }
    public long getOffset() {
        return offset;
    }
}
