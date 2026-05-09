package com.arkghidra.format;

import java.util.Collections;
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
    private final List<Long> interfaceOffsets;

    /**
     * Constructs a class without source file info (backward compatible).
     */
    public AbcClass(String name, long superClassOff, long accessFlags,
            List<AbcField> fields, List<AbcMethod> methods, long offset) {
        this(name, superClassOff, accessFlags, fields, methods, offset, 0,
                Collections.emptyList());
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
        this(name, superClassOff, accessFlags, fields, methods, offset,
                sourceFileOff, Collections.emptyList());
    }

    /**
     * Constructs a class with source file info and interface offsets.
     *
     * @param name the class name
     * @param superClassOff the byte offset of the super class
     * @param accessFlags the access flags
     * @param fields the class fields
     * @param methods the class methods
     * @param offset the byte offset of this class in the file
     * @param sourceFileOff the string table offset of the source file name
     * @param interfaceOffsets the byte offsets of implemented interfaces
     */
    public AbcClass(String name, long superClassOff, long accessFlags,
            List<AbcField> fields, List<AbcMethod> methods, long offset,
            long sourceFileOff, List<Long> interfaceOffsets) {
        this.name = name;
        this.superClassOff = superClassOff;
        this.accessFlags = accessFlags;
        this.fields = fields;
        this.methods = methods;
        this.offset = offset;
        this.sourceFileOff = sourceFileOff;
        this.interfaceOffsets = interfaceOffsets != null
                ? Collections.unmodifiableList(interfaceOffsets)
                : Collections.emptyList();
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

    /**
     * Returns the byte offsets of implemented interfaces.
     *
     * @return list of interface class offsets (empty if none)
     */
    public List<Long> getInterfaceOffsets() {
        return interfaceOffsets;
    }
}
