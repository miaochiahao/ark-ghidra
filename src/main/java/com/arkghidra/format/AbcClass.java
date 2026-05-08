package com.arkghidra.format;

import java.util.List;

/**
 * A class defined in the .abc file.
 */
public class AbcClass {
    private final String name;
    private final long superClassOff;
    private final long accessFlags;
    private final List<AbcField> fields;
    private final List<AbcMethod> methods;
    private final long offset;
    private final long sourceFileOff;

    /**
     * Constructs a class without source file info (backward compatible).
     */
    public AbcClass(String name, long superClassOff, long accessFlags,
            List<AbcField> fields, List<AbcMethod> methods, long offset) {
        this(name, superClassOff, accessFlags, fields, methods, offset, 0);
    }

    /**
     * Constructs a class with source file info.
     *
     * @param sourceFileOff the string table offset of the source file name,
     *                      or 0 if not available
     */
    public AbcClass(String name, long superClassOff, long accessFlags,
            List<AbcField> fields, List<AbcMethod> methods, long offset,
            long sourceFileOff) {
        this.name = name;
        this.superClassOff = superClassOff;
        this.accessFlags = accessFlags;
        this.fields = fields;
        this.methods = methods;
        this.offset = offset;
        this.sourceFileOff = sourceFileOff;
    }

    public String getName() {
        return name;
    }
    public long getSuperClassOff() {
        return superClassOff;
    }
    public long getAccessFlags() {
        return accessFlags;
    }
    public List<AbcField> getFields() {
        return fields;
    }
    public List<AbcMethod> getMethods() {
        return methods;
    }
    public long getOffset() {
        return offset;
    }
    public long getSourceFileOff() {
        return sourceFileOff;
    }
}
