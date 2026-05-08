package com.arkghidra.disasm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.arkghidra.disasm.ArkOperand.Type;

/**
 * Decodes Ark bytecode bytes into structured {@link ArkInstruction} objects.
 *
 * <p>The disassembler reads raw method bytecode (as stored in the ABC code section)
 * and produces a list of decoded instructions. It handles the 0xFD wide prefix for
 * instructions with 16-bit operands and validates that enough bytes are available
 * for each instruction.
 *
 * <p>Usage:
 * <pre>
 *   ArkDisassembler disasm = new ArkDisassembler();
 *   List&lt;ArkInstruction&gt; insns = disasm.disassemble(codeBytes, 0, codeBytes.length);
 * </pre>
 */
public class ArkDisassembler {

    /**
     * Decodes a single instruction starting at the given offset.
     *
     * @param bytes the raw method bytecode
     * @param offset the byte offset to start decoding
     * @return the decoded instruction
     * @throws IndexOutOfBoundsException if offset is out of range
     * @throws DisassemblyException if the bytes cannot be decoded
     */
    public ArkInstruction decodeInstruction(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new DisassemblyException("Byte array is null");
        }
        if (offset < 0 || offset >= bytes.length) {
            throw new DisassemblyException(
                "Offset " + offset + " out of range [0, " + bytes.length + ")");
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buf.position(offset);

        int opcode = buf.get() & 0xFF;

        if (opcode == ArkOpcodes.PREFIX_WIDE) {
            return decodeWide(buf, offset);
        }

        return decodePrimary(opcode, buf, offset, false);
    }

    /**
     * Disassembles a range of bytecode into a list of instructions.
     *
     * @param bytes the raw method bytecode
     * @param offset the starting byte offset
     * @param length the number of bytes to disassemble
     * @return the list of decoded instructions
     * @throws DisassemblyException on invalid input or decode errors
     */
    public List<ArkInstruction> disassemble(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            throw new DisassemblyException("Byte array is null");
        }
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new DisassemblyException(
                "Invalid range: offset=" + offset + " length=" + length
                + " bytesLength=" + bytes.length);
        }

        List<ArkInstruction> result = new ArrayList<>();
        int end = offset + length;
        int pc = offset;

        while (pc < end) {
            ArkInstruction insn = decodeInstruction(bytes, pc);
            result.add(insn);
            pc = insn.getNextOffset();
        }

        return result;
    }

    private ArkInstruction decodePrimary(int opcode, ByteBuffer buf, int offset,
            boolean wide) {
        ArkInstructionFormat format = ArkOpcodes.getFormat(opcode);
        String mnemonic = ArkOpcodes.getMnemonic(opcode);
        int formatLen = format.getLength();

        if (buf.remaining() < formatLen - 1) {
            throw new DisassemblyException(
                "Not enough bytes for instruction " + mnemonic
                + " at offset " + offset
                + ": need " + formatLen + ", have " + (buf.remaining() + 1));
        }

        List<ArkOperand> operands = new ArrayList<>();
        decodeOperands(opcode, format, buf, offset, operands);

        return new ArkInstruction(opcode, mnemonic, format, offset, formatLen,
            operands, wide);
    }

    private ArkInstruction decodeWide(ByteBuffer buf, int offset) {
        if (buf.remaining() < 1) {
            throw new DisassemblyException(
                "Truncated wide instruction at offset " + offset);
        }

        int subOpcode = buf.get() & 0xFF;
        ArkInstructionFormat wideFormat = ArkOpcodes.getWideFormat(subOpcode);
        String mnemonic = ArkOpcodes.getWideMnemonic(subOpcode);
        int formatLen = wideFormat.getLength();

        // Length includes the 2-byte opcode prefix; buf has consumed 2 bytes already
        int operandsLen = formatLen - 2;
        if (buf.remaining() < operandsLen) {
            throw new DisassemblyException(
                "Not enough bytes for wide instruction " + mnemonic
                + " at offset " + offset
                + ": need " + formatLen + ", have " + (buf.remaining() + 2));
        }

        List<ArkOperand> operands = new ArrayList<>();
        decodeWideOperands(subOpcode, wideFormat, buf, offset, operands);

        return new ArkInstruction(subOpcode, mnemonic, wideFormat, offset, formatLen,
            operands, true);
    }

    private void decodeOperands(int opcode, ArkInstructionFormat format,
            ByteBuffer buf, int offset, List<ArkOperand> operands) {
        switch (format) {
            case NONE:
                break;

            case V8:
                operands.add(reg(buf));
                break;

            case V8_V8:
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case V8_V8_V8:
                operands.add(reg(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case V8_V8_V8_V8:
                operands.add(reg(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case IMM8:
                if (isJump8(opcode)) {
                    operands.add(jumpOffset8(buf, offset));
                } else {
                    operands.add(imm8(buf));
                }
                break;

            case IMM16:
                if (isJump16(opcode)) {
                    operands.add(jumpOffset16(buf, offset));
                } else {
                    operands.add(imm16(buf));
                }
                break;

            case IMM4_IMM4:
                byte packed = buf.get();
                operands.add(new ArkOperand(Type.IMMEDIATE4, packed & 0x0F));
                operands.add(new ArkOperand(Type.IMMEDIATE4, (packed >> 4) & 0x0F));
                break;

            case IMM8_V8:
                operands.add(imm8(buf));
                operands.add(reg(buf));
                break;

            case IMM8_IMM8:
                operands.add(imm8(buf));
                operands.add(imm8(buf));
                break;

            case IMM8_IMM16:
                operands.add(imm8(buf));
                operands.add(imm16(buf));
                break;

            case IMM16_V8:
                if (opcode == ArkOpcodes.CLOSEITERATOR_IMM16) {
                    operands.add(imm16(buf));
                    operands.add(reg(buf));
                } else {
                    operands.add(imm16(buf));
                    operands.add(reg(buf));
                }
                break;

            case IMM8_V8_V8:
                operands.add(imm8(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case IMM8_V8_V8_V8:
                operands.add(imm8(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case IMM32:
                operands.add(imm32Signed(buf));
                break;

            case IMM64:
                operands.add(imm64Signed(buf));
                break;

            case IMM8_IMM16_IMM8:
                operands.add(imm8(buf));
                operands.add(imm16(buf));
                operands.add(imm8(buf));
                break;

            case IMM8_IMM16_V8:
                operands.add(imm8(buf));
                operands.add(imm16(buf));
                operands.add(reg(buf));
                break;

            case IMM8_IMM16_IMM16:
                operands.add(imm8(buf));
                operands.add(imm16(buf));
                operands.add(imm16(buf));
                break;

            case IMM8_IMM16_IMM16_V8:
                operands.add(imm8(buf));
                operands.add(imm16(buf));
                operands.add(imm16(buf));
                operands.add(reg(buf));
                break;

            case IMM8_IMM8_V8:
                operands.add(imm8(buf));
                operands.add(imm8(buf));
                operands.add(reg(buf));
                break;

            case V8_IMM8:
                operands.add(reg(buf));
                operands.add(jumpOffset8(buf, offset));
                break;

            case V8_IMM16:
                operands.add(reg(buf));
                operands.add(jumpOffset16(buf, offset));
                break;

            case IMM8_V8_IMM16:
                operands.add(imm8(buf));
                operands.add(reg(buf));
                operands.add(imm16(buf));
                break;

            default:
                break;
        }
    }

    private void decodeWideOperands(int subOpcode, ArkInstructionFormat format,
            ByteBuffer buf, int offset, List<ArkOperand> operands) {
        switch (format) {
            case WIDE_IMM16:
                operands.add(imm16(buf));
                break;

            case WIDE_IMM16_IMM16:
                operands.add(imm16(buf));
                operands.add(imm16(buf));
                break;

            case WIDE_IMM16_V8:
                operands.add(imm16(buf));
                operands.add(reg(buf));
                break;

            case WIDE_IMM16_V8_V8:
                operands.add(imm16(buf));
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case WIDE_V8_V8:
                operands.add(reg(buf));
                operands.add(reg(buf));
                break;

            case WIDE_IMM16_IMM8_V8:
                operands.add(imm16(buf));
                operands.add(imm8(buf));
                operands.add(reg(buf));
                break;

            case WIDE_IMM16_IMM16_V8:
                operands.add(imm16(buf));
                operands.add(imm16(buf));
                operands.add(reg(buf));
                break;

            case WIDE_IMM16_IMM16_IMM8:
                operands.add(imm16(buf));
                operands.add(imm16(buf));
                operands.add(imm8(buf));
                break;

            case WIDE_IMM16_V8_IMM16:
                operands.add(imm16(buf));
                operands.add(reg(buf));
                operands.add(imm16(buf));
                break;

            default:
                break;
        }
    }

    // --- Operand readers ---

    private static ArkOperand reg(ByteBuffer buf) {
        return new ArkOperand(Type.REGISTER, buf.get() & 0xFF);
    }

    private static ArkOperand imm8(ByteBuffer buf) {
        return new ArkOperand(Type.IMMEDIATE8, buf.get() & 0xFF);
    }

    private static ArkOperand imm16(ByteBuffer buf) {
        return new ArkOperand(Type.IMMEDIATE16, buf.getShort() & 0xFFFF);
    }

    private static ArkOperand imm32Signed(ByteBuffer buf) {
        return new ArkOperand(Type.IMMEDIATE32_SIGNED, buf.getInt());
    }

    private static ArkOperand imm64Signed(ByteBuffer buf) {
        return new ArkOperand(Type.IMMEDIATE64_SIGNED, buf.getLong());
    }

    private static ArkOperand jumpOffset8(ByteBuffer buf, int instrOffset) {
        int raw = buf.get(); // sign-extended byte
        return new ArkOperand(Type.JUMP_OFFSET8, raw);
    }

    private static ArkOperand jumpOffset16(ByteBuffer buf, int instrOffset) {
        short raw = buf.getShort(); // sign-extended short
        return new ArkOperand(Type.JUMP_OFFSET16, raw);
    }

    // --- Jump opcode helpers ---

    private static boolean isJump8(int opcode) {
        switch (opcode) {
            case ArkOpcodes.JMP_IMM8:
            case ArkOpcodes.JEQZ_IMM8:
            case ArkOpcodes.JNEZ_IMM8:
            case ArkOpcodes.JSTRICTEQZ_IMM8:
            case ArkOpcodes.JNSTRICTEQZ_IMM8:
            case ArkOpcodes.JEQNULL_IMM8:
            case ArkOpcodes.JNENULL_IMM8:
            case ArkOpcodes.JSTRICTEQNULL_IMM8:
            case ArkOpcodes.JNSTRICTEQNULL_IMM8:
            case ArkOpcodes.JEQUNDEFINED_IMM8:
            case ArkOpcodes.JNEUNDEFINED_IMM8:
                return true;
            default:
                return false;
        }
    }

    private static boolean isJump16(int opcode) {
        switch (opcode) {
            case ArkOpcodes.JMP_IMM16:
            case ArkOpcodes.JEQZ_IMM16:
            case ArkOpcodes.JNEZ_IMM16:
            case ArkOpcodes.JSTRICTEQZ_IMM16:
            case ArkOpcodes.JNSTRICTEQZ_IMM16:
            case ArkOpcodes.JEQNULL_IMM16:
            case ArkOpcodes.JNENULL_IMM16:
            case ArkOpcodes.JSTRICTEQNULL_IMM16:
            case ArkOpcodes.JNSTRICTEQNULL_IMM16:
            case ArkOpcodes.JEQUNDEFINED_IMM16:
            case ArkOpcodes.JNEUNDEFINED_IMM16:
            case ArkOpcodes.JSTRICTEQUNDEFINED_IMM16:
            case ArkOpcodes.JNSTRICTEQUNDEFINED_IMM16:
                return true;
            default:
                return false;
        }
    }
}
