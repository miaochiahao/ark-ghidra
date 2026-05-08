package com.arkghidra.format;

import java.util.List;

/**
 * Debug information associated with a method's code section.
 *
 * <p>Contains the source line number mapping, parameter names from debug metadata,
 * and local variable information. Based on DWARF v3 line number program encoding.
 */
public class AbcDebugInfo {
    private final long lineStart;
    private final long numParameters;
    private final List<String> parameterNames;
    private final long constantPoolSize;
    private final List<Long> lineNumProgramIdx;
    private final long offset;

    /**
     * Constructs debug info.
     *
     * @param lineStart the starting source line number
     * @param numParameters the number of parameters with debug names
     * @param parameterNames the parameter names (may contain nulls for unnamed params)
     * @param constantPoolSize the size of the constant pool in entries
     * @param lineNumProgramIdx indices into the line number program table
     * @param offset the byte offset of this debug info in the ABC file
     */
    public AbcDebugInfo(long lineStart, long numParameters,
            List<String> parameterNames, long constantPoolSize,
            List<Long> lineNumProgramIdx, long offset) {
        this.lineStart = lineStart;
        this.numParameters = numParameters;
        this.parameterNames = parameterNames;
        this.constantPoolSize = constantPoolSize;
        this.lineNumProgramIdx = lineNumProgramIdx;
        this.offset = offset;
    }

    public long getLineStart() {
        return lineStart;
    }

    public long getNumParameters() {
        return numParameters;
    }

    /**
     * Returns the parameter names from debug info.
     * Entries may be null if the parameter has no debug name.
     *
     * @return the parameter names list
     */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    public long getConstantPoolSize() {
        return constantPoolSize;
    }

    public List<Long> getLineNumProgramIdx() {
        return lineNumProgramIdx;
    }

    public long getOffset() {
        return offset;
    }
}
