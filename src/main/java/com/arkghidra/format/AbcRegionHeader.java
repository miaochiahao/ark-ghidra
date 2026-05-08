package com.arkghidra.format;

/**
 * Region header: splits the file into indexed regions.
 * 4-byte aligned, sorted by start_off.
 */
public class AbcRegionHeader {
    private final long startOff;
    private final long endOff;
    private final long classIdxSize;
    private final long classIdxOff;
    private final long methodIdxSize;
    private final long methodIdxOff;
    private final long fieldIdxSize;
    private final long fieldIdxOff;
    private final long protoIdxSize;
    private final long protoIdxOff;

    public AbcRegionHeader(long startOff, long endOff,
            long classIdxSize, long classIdxOff,
            long methodIdxSize, long methodIdxOff,
            long fieldIdxSize, long fieldIdxOff,
            long protoIdxSize, long protoIdxOff) {
        this.startOff = startOff;
        this.endOff = endOff;
        this.classIdxSize = classIdxSize;
        this.classIdxOff = classIdxOff;
        this.methodIdxSize = methodIdxSize;
        this.methodIdxOff = methodIdxOff;
        this.fieldIdxSize = fieldIdxSize;
        this.fieldIdxOff = fieldIdxOff;
        this.protoIdxSize = protoIdxSize;
        this.protoIdxOff = protoIdxOff;
    }

    public boolean containsOffset(long off) {
        return off >= startOff && off < endOff;
    }

    public long getStartOff() {
        return startOff;
    }
    public long getEndOff() {
        return endOff;
    }
    public long getClassIdxSize() {
        return classIdxSize;
    }
    public long getClassIdxOff() {
        return classIdxOff;
    }
    public long getMethodIdxSize() {
        return methodIdxSize;
    }
    public long getMethodIdxOff() {
        return methodIdxOff;
    }
    public long getFieldIdxSize() {
        return fieldIdxSize;
    }
    public long getFieldIdxOff() {
        return fieldIdxOff;
    }
    public long getProtoIdxSize() {
        return protoIdxSize;
    }
    public long getProtoIdxOff() {
        return protoIdxOff;
    }
}
