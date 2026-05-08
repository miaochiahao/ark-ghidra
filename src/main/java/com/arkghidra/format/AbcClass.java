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

    public AbcClass(String name, long superClassOff, long accessFlags,
            List<AbcField> fields, List<AbcMethod> methods, long offset) {
        this.name = name;
        this.superClassOff = superClassOff;
        this.accessFlags = accessFlags;
        this.fields = fields;
        this.methods = methods;
        this.offset = offset;
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
}
