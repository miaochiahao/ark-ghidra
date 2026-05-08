package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.arkghidra.disasm.ArkInstruction;

/**
 * A basic block in the control flow graph.
 *
 * <p>A basic block is a maximal sequence of instructions with a single entry point
 * (the first instruction) and a single exit point (the last instruction). Control
 * flow enters only at the start and leaves only at the end.
 */
public class BasicBlock {
    private final int startOffset;
    private final List<ArkInstruction> instructions;
    private final List<CFGEdge> successors;
    private final List<CFGEdge> predecessors;

    /**
     * Constructs a basic block starting at the given offset.
     *
     * @param startOffset the byte offset of the first instruction
     */
    public BasicBlock(int startOffset) {
        this.startOffset = startOffset;
        this.instructions = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Returns the byte offset of the end of this block (offset after the last instruction).
     *
     * @return end offset, or startOffset if the block is empty
     */
    public int getEndOffset() {
        if (instructions.isEmpty()) {
            return startOffset;
        }
        ArkInstruction last = instructions.get(instructions.size() - 1);
        return last.getNextOffset();
    }

    public List<ArkInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    public List<CFGEdge> getSuccessors() {
        return Collections.unmodifiableList(successors);
    }

    public List<CFGEdge> getPredecessors() {
        return Collections.unmodifiableList(predecessors);
    }

    /**
     * Adds an instruction to this block.
     *
     * @param insn the instruction to add
     */
    public void addInstruction(ArkInstruction insn) {
        instructions.add(insn);
    }

    /**
     * Adds a successor edge.
     *
     * @param edge the successor edge
     */
    public void addSuccessor(CFGEdge edge) {
        successors.add(edge);
    }

    /**
     * Adds a predecessor edge.
     *
     * @param edge the predecessor edge
     */
    public void addPredecessor(CFGEdge edge) {
        predecessors.add(edge);
    }

    /**
     * Returns the last instruction in this block, or null if empty.
     *
     * @return the last instruction, or null
     */
    public ArkInstruction getLastInstruction() {
        if (instructions.isEmpty()) {
            return null;
        }
        return instructions.get(instructions.size() - 1);
    }

    /**
     * Returns true if this block ends with a return instruction.
     *
     * @return true if the block ends with return
     */
    public boolean endsWithReturn() {
        ArkInstruction last = getLastInstruction();
        if (last == null) {
            return false;
        }
        int op = last.getOpcode();
        return op == ArkOpcodesCompat.RETURN || op == ArkOpcodesCompat.RETURNUNDEFINED;
    }
}
