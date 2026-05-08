package com.arkghidra.format;

/**
 * A local variable entry from debug info, mapping a register to a variable name
 * over a range of bytecode offsets.
 */
public class AbcLocalVariable {
    private final long startPc;
    private final long endPc;
    private final int registerNum;
    private final String name;
    private final String typeName;
    private final String signature;

    /**
     * Constructs a local variable debug entry.
     *
     * @param startPc the start PC offset (inclusive)
     * @param endPc the end PC offset (exclusive)
     * @param registerNum the virtual register number
     * @param name the variable name
     * @param typeName the type name (may be null)
     * @param signature the type signature (may be null)
     */
    public AbcLocalVariable(long startPc, long endPc, int registerNum,
            String name, String typeName, String signature) {
        this.startPc = startPc;
        this.endPc = endPc;
        this.registerNum = registerNum;
        this.name = name;
        this.typeName = typeName;
        this.signature = signature;
    }

    public long getStartPc() {
        return startPc;
    }

    public long getEndPc() {
        return endPc;
    }

    public int getRegisterNum() {
        return registerNum;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSignature() {
        return signature;
    }
}
