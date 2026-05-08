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
    private final long intValue;

    /**
     * Constructs a field without int value (backward compatible).
     */
    public AbcField(int classIdx, int typeIdx, String name, long accessFlags, long offset) {
        this(classIdx, typeIdx, name, accessFlags, offset, 0);
    }

    /**
     * Constructs a field with int value.
     *
     * <p>The int value is set from the INT_VALUE field tag (0x01).
     * For fields named "moduleRecordIdx", this contains the literal
     * array offset of the module record.
     *
     * @param classIdx the class index
     * @param typeIdx the type index
     * @param name the field name
     * @param accessFlags the access flags
     * @param offset the byte offset of this field in the file
     * @param intValue the INT_VALUE from field tags, or 0 if none
     */
    public AbcField(int classIdx, int typeIdx, String name, long accessFlags,
            long offset, long intValue) {
        this.classIdx = classIdx;
        this.typeIdx = typeIdx;
        this.name = name;
        this.accessFlags = accessFlags;
        this.offset = offset;
        this.intValue = intValue;
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
    public long getIntValue() {
        return intValue;
    }

    /**
     * Returns true if this field is a module record index field.
     *
     * @return true if the field name is "moduleRecordIdx"
     */
    public boolean isModuleRecordIdx() {
        return "moduleRecordIdx".equals(name);
    }
}
