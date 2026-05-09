package com.arkghidra.disasm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A decoded Ark bytecode instruction.
 *
 * <p>Contains the opcode, mnemonic, instruction format, byte offset within the method
 * bytecode, total length, and a list of decoded operands.
 */
public class ArkInstruction {

    private final int opcode;
    private final String mnemonic;
    private final ArkInstructionFormat format;
    private final int offset;
    private final int length;
    private final List<ArkOperand> operands;
    private final boolean wide;
    private final boolean callRuntime;

    /**
     * Constructs a decoded instruction.
     *
     * @param opcode the primary opcode byte, or the sub-opcode for wide instructions
     * @param mnemonic the human-readable mnemonic
     * @param format the instruction format
     * @param offset the byte offset within the method bytecode
     * @param length the total instruction length in bytes
     * @param operands the decoded operands (may be empty)
     * @param wide true if this is a wide (0xFD-prefixed) instruction
     */
    public ArkInstruction(int opcode, String mnemonic, ArkInstructionFormat format,
            int offset, int length, List<ArkOperand> operands, boolean wide) {
        this(opcode, mnemonic, format, offset, length, operands, wide, false);
    }

    /**
     * Constructs a decoded instruction with explicit callRuntime flag.
     *
     * @param opcode the primary opcode byte, or the sub-opcode for wide/callruntime
     * @param mnemonic the human-readable mnemonic
     * @param format the instruction format
     * @param offset the byte offset within the method bytecode
     * @param length the total instruction length in bytes
     * @param operands the decoded operands (may be empty)
     * @param wide true if this is a wide (0xFD-prefixed) instruction
     * @param callRuntime true if this is a callruntime (0xFB-prefixed) instruction
     */
    public ArkInstruction(int opcode, String mnemonic, ArkInstructionFormat format,
            int offset, int length, List<ArkOperand> operands, boolean wide,
            boolean callRuntime) {
        this.opcode = opcode & 0xFF;
        this.mnemonic = mnemonic;
        this.format = format;
        this.offset = offset;
        this.length = length;
        this.operands = Collections.unmodifiableList(new ArrayList<>(operands));
        this.wide = wide;
        this.callRuntime = callRuntime;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public ArkInstructionFormat getFormat() {
        return format;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public List<ArkOperand> getOperands() {
        return operands;
    }

    public boolean isWide() {
        return wide;
    }

    /**
     * Returns true if this is a callruntime (0xFB-prefixed) instruction.
     *
     * @return true if callruntime prefixed
     */
    public boolean isCallRuntime() {
        return callRuntime;
    }

    /**
     * Returns the byte offset immediately following this instruction.
     *
     * @return offset + length
     */
    public int getNextOffset() {
        return offset + length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "%04X: ", offset));
        if (wide) {
            sb.append(String.format(Locale.ROOT, "[wide] %s", mnemonic));
        } else if (callRuntime) {
            sb.append(String.format(Locale.ROOT, "[callruntime] %s",
                    mnemonic));
        } else {
            sb.append(mnemonic);
        }
        for (int i = 0; i < operands.size(); i++) {
            if (i == 0) {
                sb.append(' ');
            } else {
                sb.append(", ");
            }
            sb.append(operands.get(i).toString());
        }
        return sb.toString();
    }
}
