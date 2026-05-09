package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOpcodes;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcTryBlock;

/**
 * Builds and holds the control flow graph (CFG) for a method.
 *
 * <p>The CFG is constructed from a list of decoded instructions and optional
 * try/catch block information. It identifies basic block boundaries at branch
 * targets and branch sources, then connects blocks with typed edges.
 */
public class ControlFlowGraph {
    private final List<BasicBlock> blocks;
    private final Map<Integer, BasicBlock> blockByStartOffset;

    private ControlFlowGraph(List<BasicBlock> blocks,
            Map<Integer, BasicBlock> blockByStartOffset) {
        this.blocks = Collections.unmodifiableList(blocks);
        this.blockByStartOffset = Collections.unmodifiableMap(blockByStartOffset);
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    /**
     * Returns the basic block that starts at the given offset, or null.
     *
     * @param offset the byte offset
     * @return the block at that offset, or null
     */
    public BasicBlock getBlockAt(int offset) {
        return blockByStartOffset.get(offset);
    }

    /**
     * Returns the entry block (first block).
     *
     * @return the entry block
     */
    public BasicBlock getEntryBlock() {
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    /**
     * Builds a control flow graph from a list of decoded instructions.
     *
     * @param instructions the decoded instructions
     * @return the control flow graph
     */
    public static ControlFlowGraph build(List<ArkInstruction> instructions) {
        return build(instructions, Collections.emptyList());
    }

    /**
     * Builds a control flow graph from instructions and try/catch blocks.
     *
     * @param instructions the decoded instructions
     * @param tryBlocks the try/catch blocks from the ABC code section
     * @return the control flow graph
     */
    public static ControlFlowGraph build(List<ArkInstruction> instructions,
            List<AbcTryBlock> tryBlocks) {
        if (instructions.isEmpty()) {
            return new ControlFlowGraph(Collections.emptyList(),
                    Collections.emptyMap());
        }

        // Identify all basic block boundaries
        Set<Integer> leaders = new TreeSet<>();
        // First instruction is always a leader
        leaders.add(instructions.get(0).getOffset());

        // Targets of jumps are leaders
        for (ArkInstruction insn : instructions) {
            int target = getJumpTarget(insn);
            if (target >= 0) {
                leaders.add(target);
            }
        }

        // Exception handler entry points are leaders
        for (AbcTryBlock tryBlock : tryBlocks) {
            leaders.add((int) tryBlock.getStartPc());
            for (AbcCatchBlock catchBlock : tryBlock.getCatchBlocks()) {
                leaders.add((int) catchBlock.getHandlerPc());
            }
        }

        // Instruction after a terminator is a leader (fall-through start)
        for (int i = 0; i < instructions.size(); i++) {
            ArkInstruction insn = instructions.get(i);
            if (ArkOpcodesCompat.isTerminator(insn.getOpcode())) {
                int nextOffset = insn.getNextOffset();
                // Check if there is a next instruction
                if (i + 1 < instructions.size()
                        && instructions.get(i + 1).getOffset() == nextOffset) {
                    leaders.add(nextOffset);
                }
            }
        }

        // Build basic blocks
        List<BasicBlock> blocks = new ArrayList<>();
        Map<Integer, BasicBlock> blockMap = new LinkedHashMap<>();

        BasicBlock current = null;
        for (ArkInstruction insn : instructions) {
            if (leaders.contains(insn.getOffset())) {
                current = new BasicBlock(insn.getOffset());
                blocks.add(current);
                blockMap.put(insn.getOffset(), current);
            }
            if (current != null) {
                current.addInstruction(insn);
            }
        }

        // Add edges
        for (BasicBlock block : blocks) {
            ArkInstruction last = block.getLastInstruction();
            if (last == null) {
                continue;
            }
            int opcode = last.getOpcode();

            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                int target = getJumpTarget(last);
                addEdge(block, target, EdgeType.JUMP, blockMap);
            } else if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                int target = getJumpTarget(last);
                addEdge(block, target, EdgeType.CONDITIONAL_TRUE, blockMap);
                // Fall-through to next block
                int fallThroughOffset = last.getNextOffset();
                addEdge(block, fallThroughOffset, EdgeType.CONDITIONAL_FALSE,
                        blockMap);
            } else if (opcode != ArkOpcodes.RETURN
                    && opcode != ArkOpcodes.RETURNUNDEFINED) {
                // Fall-through to next block
                int fallThroughOffset = last.getNextOffset();
                addEdge(block, fallThroughOffset, EdgeType.FALL_THROUGH,
                        blockMap);
            }
        }

        // Add exception handler edges
        for (AbcTryBlock tryBlock : tryBlocks) {
            for (AbcCatchBlock catchBlock : tryBlock.getCatchBlocks()) {
                int handlerOffset = (int) catchBlock.getHandlerPc();
                BasicBlock handlerBlock = blockMap.get(handlerOffset);
                if (handlerBlock != null) {
                    CFGEdge edge = new CFGEdge(-1, handlerOffset,
                            EdgeType.EXCEPTION_HANDLER);
                    handlerBlock.addPredecessor(edge);
                }
            }
        }

        return new ControlFlowGraph(blocks, blockMap);
    }

    private static void addEdge(BasicBlock from, int toOffset, EdgeType type,
            Map<Integer, BasicBlock> blockMap) {
        BasicBlock toBlock = blockMap.get(toOffset);
        if (toBlock == null) {
            return;
        }
        CFGEdge edge = new CFGEdge(from.getStartOffset(), toOffset, type);
        from.addSuccessor(edge);
        toBlock.addPredecessor(edge);
    }

    /**
     * Returns the jump target offset for a branch instruction, or -1 if not a branch.
     * Public accessor for use by the decompiler.
     *
     * @param insn the instruction
     * @return the absolute target offset, or -1
     */
    public static int getJumpTargetPublic(ArkInstruction insn) {
        return getJumpTarget(insn);
    }

    /**
     * Returns the jump target offset for a branch instruction, or -1 if not a branch.
     *
     * @param insn the instruction
     * @return the absolute target offset, or -1
     */
    private static int getJumpTarget(ArkInstruction insn) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        if (operands.isEmpty()) {
            return -1;
        }

        // For V8_IMM8 / V8_IMM16 formats (jeq, jne, etc.), offset is the last operand
        // For IMM8 / IMM16 jump formats (jmp, jeqz, jnez, etc.), offset is the first operand
        ArkOperand offsetOperand;

        if (opcode == ArkOpcodesCompat.JEQ_IMM8
                || opcode == ArkOpcodesCompat.JEQ_IMM16
                || opcode == ArkOpcodesCompat.JNE_IMM8
                || opcode == ArkOpcodesCompat.JNE_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQ_IMM16
                || opcode == ArkOpcodesCompat.JNSTRICTEQ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQ_IMM16) {
            offsetOperand = operands.get(operands.size() - 1);
        } else if (ArkOpcodesCompat.isUnconditionalJump(opcode)
                || ArkOpcodesCompat.isConditionalBranch(opcode)) {
            offsetOperand = operands.get(0);
        } else {
            return -1;
        }

        long offset = offsetOperand.getValue();
        // Jump offsets are relative to the instruction's own offset
        return insn.getOffset() + insn.getLength() + (int) offset;
    }
}
