package com.arkghidra.decompile;

/**
 * Type of edge between basic blocks in a control flow graph.
 */
public enum EdgeType {
    /** Normal fall-through from one block to the next. */
    FALL_THROUGH,
    /** Unconditional jump edge. */
    JUMP,
    /** Conditional branch taken (condition is true). */
    CONDITIONAL_TRUE,
    /** Conditional branch not taken (condition is false). */
    CONDITIONAL_FALSE,
    /** Exception handler edge (try to catch). */
    EXCEPTION_HANDLER
}
