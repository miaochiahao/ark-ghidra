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
    private final long debugInfoOff;

    /**
     * Constructs a method without debug info (backward compatible).
     */
    public AbcMethod(int classIdx, int protoIdx, String name, long accessFlags,
            long codeOff, long offset) {
        this(classIdx, protoIdx, name, accessFlags, codeOff, offset, 0);
    }

    /**
     * Constructs a method with debug info.
     *
     * @param debugInfoOff the offset of the debug info section, or 0 if none
     */
    public AbcMethod(int classIdx, int protoIdx, String name, long accessFlags,
            long codeOff, long offset, long debugInfoOff) {
        this.classIdx = classIdx;
        this.protoIdx = protoIdx;
        this.name = name;
        this.accessFlags = accessFlags;
        this.codeOff = codeOff;
        this.offset = offset;
        this.debugInfoOff = debugInfoOff;
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
    public long getDebugInfoOff() {
        return debugInfoOff;
    }
}
