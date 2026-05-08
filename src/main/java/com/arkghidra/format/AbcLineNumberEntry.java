package com.arkghidra.format;

/**
 * A single line number entry mapping a bytecode PC to a source line number.
 */
public class AbcLineNumberEntry {
    private final long pc;
    private final long line;

    /**
     * Constructs a line number entry.
     *
     * @param pc the bytecode program counter offset
     * @param line the source line number
     */
    public AbcLineNumberEntry(long pc, long line) {
        this.pc = pc;
        this.line = line;
    }

    public long getPc() {
        return pc;
    }

    public long getLine() {
        return line;
    }
}
