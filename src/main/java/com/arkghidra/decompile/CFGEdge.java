package com.arkghidra.decompile;

/**
 * An edge in the control flow graph connecting two basic blocks.
 */
public class CFGEdge {
    private final int fromOffset;
    private final int toOffset;
    private final EdgeType type;

    /**
     * Constructs a CFG edge.
     *
     * @param fromOffset the byte offset of the source block
     * @param toOffset the byte offset of the target block
     * @param type the edge type
     */
    public CFGEdge(int fromOffset, int toOffset, EdgeType type) {
        this.fromOffset = fromOffset;
        this.toOffset = toOffset;
        this.type = type;
    }

    public int getFromOffset() {
        return fromOffset;
    }

    public int getToOffset() {
        return toOffset;
    }

    public EdgeType getType() {
        return type;
    }
}
