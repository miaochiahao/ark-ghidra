package com.arkghidra.disasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkOperand.Type;

class ArkDisassemblerTest {

    private ArkDisassembler disasm;

    @BeforeEach
    void setUp() {
        disasm = new ArkDisassembler();
    }

    // --- Helper: build little-endian byte array ---

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    private static byte[] le16(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }

    private static byte[] le32(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }

    private static byte[] le64(long value) {
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
        return result;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) {
            total += a.length;
        }
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    // --- NONE format instructions ---

    @Test
    void testDecodeInstruction_ldundefined_returnsNoneFormat() {
        byte[] code = bytes(0x00);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(0x00, insn.getOpcode());
        assertEquals("ldundefined", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.NONE, insn.getFormat());
        assertEquals(1, insn.getLength());
        assertTrue(insn.getOperands().isEmpty());
        assertFalse(insn.isWide());
    }

    @Test
    void testDecodeInstruction_ldnull_returnsNoneFormat() {
        ArkInstruction insn = disasm.decodeInstruction(bytes(0x01), 0);
        assertEquals("ldnull", insn.getMnemonic());
        assertEquals(1, insn.getLength());
    }

    @Test
    void testDecodeInstruction_return_returnsNoneFormat() {
        ArkInstruction insn = disasm.decodeInstruction(bytes(0x64), 0);
        assertEquals("return", insn.getMnemonic());
        assertEquals(1, insn.getLength());
    }

    @Test
    void testDecodeInstruction_nop_returnsNoneFormat() {
        ArkInstruction insn = disasm.decodeInstruction(bytes(0xD5), 0);
        assertEquals("nop", insn.getMnemonic());
        assertEquals(1, insn.getLength());
    }

    // --- V8 format instructions ---

    @Test
    void testDecodeInstruction_lda_v8_returnsRegister() {
        byte[] code = bytes(0x60, 0x05);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("lda", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8, insn.getFormat());
        assertEquals(2, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(5, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_sta_v8_returnsRegister() {
        byte[] code = bytes(0x61, 0x0A);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("sta", insn.getMnemonic());
        assertEquals(1, insn.getOperands().size());
        assertEquals(0x0A, insn.getOperands().get(0).getValue());
    }

    // --- IMM8 format instructions ---

    @Test
    void testDecodeInstruction_createemptyarray_imm8_returnsImmediate() {
        byte[] code = bytes(0x05, 0x04);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("createemptyarray", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8, insn.getFormat());
        assertEquals(2, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
        assertEquals(4, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_inc_imm8_returnsImmediate() {
        byte[] code = bytes(0x21, 0x01);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("inc", insn.getMnemonic());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
    }

    // --- IMM16 format ---

    @Test
    void testDecodeInstruction_ldaStr_imm16_returnsImmediate() {
        byte[] code = concat(bytes(0x3E), le16(0x0042));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("lda.str", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM16, insn.getFormat());
        assertEquals(3, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE16, insn.getOperands().get(0).getType());
        assertEquals(0x0042, insn.getOperands().get(0).getValue());
    }

    // --- IMM4_IMM4 format (packed byte) ---

    @Test
    void testDecodeInstruction_ldlexvar_packed4_returnsTwoImms() {
        byte[] code = bytes(0x3C, 0x31);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("ldlexvar", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM4_IMM4, insn.getFormat());
        assertEquals(2, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        // low nibble = 1, high nibble = 3
        assertEquals(1, insn.getOperands().get(0).getValue());
        assertEquals(3, insn.getOperands().get(1).getValue());
    }

    // --- IMM8_V8 format ---

    @Test
    void testDecodeInstruction_add2_imm8_v8_returnsTwoOperands() {
        byte[] code = bytes(0x0A, 0x10, 0x03);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("add2", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, insn.getFormat());
        assertEquals(3, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
        assertEquals(0x10, insn.getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(3, insn.getOperands().get(1).getValue());
    }

    // --- IMM8_V8_V8 format ---

    @Test
    void testDecodeInstruction_callargs2_imm8_v8_v8() {
        byte[] code = bytes(0x2B, 0x02, 0x05, 0x06);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callargs2", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_V8, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(3, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(Type.REGISTER, insn.getOperands().get(2).getType());
    }

    // --- IMM8_V8_V8_V8 format ---

    @Test
    void testDecodeInstruction_callargs3_returnsFourOperands() {
        byte[] code = bytes(0x2C, 0x03, 0x01, 0x02, 0x03);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callargs3", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_V8_V8, insn.getFormat());
        assertEquals(5, insn.getLength());
        assertEquals(4, insn.getOperands().size());
    }

    // --- V8_V8 format ---

    @Test
    void testDecodeInstruction_mov_imm4_imm4() {
        // MOV (0x44) uses IMM4_IMM4 format: packed 4-bit register pair
        // Packed byte 0x53 → low nibble=3 (dst), high nibble=5 (src)
        byte[] code = bytes(0x44, 0x53);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("mov", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM4_IMM4, insn.getFormat());
        assertEquals(2, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(3, insn.getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(5, insn.getOperands().get(1).getValue());
    }

    @Test
    void testDecodeInstruction_mov_8_v8_v8() {
        // MOV_8 (0x45) uses V8_V8 format: two 8-bit registers
        byte[] code = bytes(0x45, 0x10, 0x20);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("mov", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_V8, insn.getFormat());
        assertEquals(3, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(0x10, insn.getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(0x20, insn.getOperands().get(1).getValue());
    }

    // --- IMM32 format ---

    @Test
    void testDecodeInstruction_ldai_imm32() {
        byte[] code = concat(bytes(0x62), le32(0x12345678));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("ldai", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM32, insn.getFormat());
        assertEquals(5, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE32_SIGNED, insn.getOperands().get(0).getType());
        assertEquals(0x12345678L, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_ldai_negativeValue() {
        byte[] code = concat(bytes(0x62), le32(-1));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(-1L, insn.getOperands().get(0).getValue());
    }

    // --- IMM64 format ---

    @Test
    void testDecodeInstruction_fldai_imm64() {
        byte[] code = concat(bytes(0x63), le64(Double.doubleToRawLongBits(3.14)));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("fldai", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM64, insn.getFormat());
        assertEquals(9, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE64_SIGNED, insn.getOperands().get(0).getType());
    }

    // --- IMM8_IMM16 format ---

    @Test
    void testDecodeInstruction_createarraywithbuffer() {
        byte[] code = concat(bytes(0x06, 0x01), le16(0x000A));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("createarraywithbuffer", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(0x01, insn.getOperands().get(0).getValue());
        assertEquals(0x000A, insn.getOperands().get(1).getValue());
    }

    // --- IMM8_IMM16_V8 format ---

    @Test
    void testDecodeInstruction_stobjbyname() {
        byte[] code = concat(bytes(0x43, 0x01), le16(0x0020), bytes(0x04));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("stobjbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_V8, insn.getFormat());
        assertEquals(5, insn.getLength());
        assertEquals(3, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
        assertEquals(Type.IMMEDIATE16, insn.getOperands().get(1).getType());
        assertEquals(Type.REGISTER, insn.getOperands().get(2).getType());
    }

    // --- IMM8_IMM16_IMM8 format ---

    @Test
    void testDecodeInstruction_definefunc() {
        byte[] code = concat(bytes(0x33, 0x01), le16(0x0005), bytes(0x02));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("definefunc", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_IMM8, insn.getFormat());
        assertEquals(5, insn.getLength());
        assertEquals(3, insn.getOperands().size());
    }

    // --- IMM8_IMM8_V8 format ---

    @Test
    void testDecodeInstruction_newobjrange() {
        byte[] code = bytes(0x08, 0x01, 0x03, 0x05);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("newobjrange", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM8_V8, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(3, insn.getOperands().size());
    }

    // --- V8_V8_V8_V8 format ---

    @Test
    void testDecodeInstruction_definegettersetterbyvalue() {
        byte[] code = bytes(0xBC, 0x01, 0x02, 0x03, 0x04);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("definegettersetterbyvalue", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_V8_V8_V8, insn.getFormat());
        assertEquals(5, insn.getLength());
        assertEquals(4, insn.getOperands().size());
        for (int i = 0; i < 4; i++) {
            assertEquals(Type.REGISTER, insn.getOperands().get(i).getType());
            assertEquals(i + 1, insn.getOperands().get(i).getValue());
        }
    }

    // --- Jump instructions (8-bit signed offset) ---

    @Test
    void testDecodeInstruction_jmp_imm8_positive() {
        byte[] code = bytes(0x4D, 0x0A);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jmp", insn.getMnemonic());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.JUMP_OFFSET8, insn.getOperands().get(0).getType());
        assertEquals(10, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_jmp_imm8_negative() {
        byte[] code = bytes(0x4D, 0xF0); // -16 signed
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(Type.JUMP_OFFSET8, insn.getOperands().get(0).getType());
        assertEquals(-16, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_jeqz_imm8() {
        byte[] code = bytes(0x4F, 0x14);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jeqz", insn.getMnemonic());
        assertEquals(Type.JUMP_OFFSET8, insn.getOperands().get(0).getType());
        assertEquals(0x14, insn.getOperands().get(0).getValue());
    }

    // --- Jump instructions (16-bit signed offset) ---

    @Test
    void testDecodeInstruction_jmp_imm16() {
        byte[] code = concat(bytes(0x4E), le16(0x0100));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jmp", insn.getMnemonic());
        assertEquals(3, insn.getLength());
        assertEquals(Type.JUMP_OFFSET16, insn.getOperands().get(0).getType());
        assertEquals(0x0100, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_jmp_imm16_negative() {
        byte[] code = concat(bytes(0x4E), le16((short) -50));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(-50, insn.getOperands().get(0).getValue());
    }

    // --- Jump with register (V8_IMM8) ---

    @Test
    void testDecodeInstruction_jeq_v8_imm8() {
        byte[] code = bytes(0x5C, 0x03, 0x20);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jeq", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_IMM8, insn.getFormat());
        assertEquals(3, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(3, insn.getOperands().get(0).getValue());
        assertEquals(Type.JUMP_OFFSET8, insn.getOperands().get(1).getType());
        assertEquals(0x20, insn.getOperands().get(1).getValue());
    }

    // --- Jump with register (V8_IMM16) ---

    @Test
    void testDecodeInstruction_jeq_v8_imm16() {
        byte[] code = concat(bytes(0xA7, 0x03), le16(0x0100));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jeq", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(Type.JUMP_OFFSET16, insn.getOperands().get(1).getType());
    }

    // --- Wide prefix (0xFD) instructions ---

    @Test
    void testDecodeInstruction_wide_createemptyarray() {
        byte[] code = concat(bytes(0xFD, 0x80), le16(0x0100));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertTrue(insn.isWide());
        assertEquals("createemptyarray", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(1, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE16, insn.getOperands().get(0).getType());
        assertEquals(0x0100, insn.getOperands().get(0).getValue());
    }

    @Test
    void testDecodeInstruction_wide_mov() {
        byte[] code = bytes(0xFD, 0x8F, 0x0A, 0x14);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertTrue(insn.isWide());
        assertEquals("mov", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_V8_V8, insn.getFormat());
        assertEquals(4, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(10, insn.getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(20, insn.getOperands().get(1).getValue());
    }

    @Test
    void testDecodeInstruction_wide_ldobjbyname() {
        byte[] code = concat(bytes(0xFD, 0x90), le16(0x01), le16(0x0042));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertTrue(insn.isWide());
        assertEquals("ldobjbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM16, insn.getFormat());
        assertEquals(6, insn.getLength());
        assertEquals(2, insn.getOperands().size());
        assertEquals(0x0001, insn.getOperands().get(0).getValue());
        assertEquals(0x0042, insn.getOperands().get(1).getValue());
    }

    @Test
    void testDecodeInstruction_wide_stobjbyvalue() {
        byte[] code = bytes(0xFD, 0x86, 0x01, 0x00, 0x03, 0x05);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertTrue(insn.isWide());
        assertEquals("stobjbyvalue", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_V8_V8, insn.getFormat());
        assertEquals(6, insn.getLength());
    }

    @Test
    void testDecodeInstruction_wide_definefunc() {
        byte[] code = concat(bytes(0xFD, 0x74), le16(0x01), le16(0x0005), bytes(0x02));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertTrue(insn.isWide());
        assertEquals("definefunc", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM16_IMM8, insn.getFormat());
        assertEquals(7, insn.getLength());
    }

    // --- Invalid / error handling ---

    @Test
    void testDecodeInstruction_nullBytes_throwsException() {
        assertThrows(DisassemblyException.class, () -> {
            disasm.decodeInstruction(null, 0);
        });
    }

    @Test
    void testDecodeInstruction_outOfBounds_throwsException() {
        assertThrows(DisassemblyException.class, () -> {
            disasm.decodeInstruction(bytes(0x00), 5);
        });
    }

    @Test
    void testDecodeInstruction_truncatedInstruction_throwsException() {
        // ldai needs 4 more bytes after opcode, but only 1 is available
        assertThrows(DisassemblyException.class, () -> {
            disasm.decodeInstruction(bytes(0x62, 0x01), 0);
        });
    }

    @Test
    void testDecodeInstruction_truncatedWide_throwsException() {
        // 0xFD but no sub-opcode
        assertThrows(DisassemblyException.class, () -> {
            disasm.decodeInstruction(bytes(0xFD), 0);
        });
    }

    @Test
    void testDisassemble_nullBytes_throwsException() {
        assertThrows(DisassemblyException.class, () -> {
            disasm.disassemble(null, 0, 0);
        });
    }

    @Test
    void testDisassemble_invalidRange_throwsException() {
        assertThrows(DisassemblyException.class, () -> {
            disasm.disassemble(bytes(0x00), 0, 10);
        });
    }

    // --- Disassemble a sequence ---

    @Test
    void testDisassemble_sequence_returnsInstructions() {
        byte[] code = concat(
            bytes(0x00),                     // ldundefined
            bytes(0x60, 0x01),               // lda v1
            bytes(0x0A, 0x10, 0x02),         // add2 0x10, v2
            bytes(0x61, 0x03),               // sta v3
            bytes(0x64)                       // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(5, insns.size());

        assertEquals("ldundefined", insns.get(0).getMnemonic());
        assertEquals(0, insns.get(0).getOffset());
        assertEquals(1, insns.get(0).getLength());

        assertEquals("lda", insns.get(1).getMnemonic());
        assertEquals(1, insns.get(1).getOffset());
        assertEquals(2, insns.get(1).getLength());

        assertEquals("add2", insns.get(2).getMnemonic());
        assertEquals(3, insns.get(2).getOffset());
        assertEquals(3, insns.get(2).getLength());

        assertEquals("sta", insns.get(3).getMnemonic());
        assertEquals(6, insns.get(3).getOffset());
        assertEquals(2, insns.get(3).getLength());

        assertEquals("return", insns.get(4).getMnemonic());
        assertEquals(8, insns.get(4).getOffset());
        assertEquals(1, insns.get(4).getLength());
    }

    @Test
    void testDisassemble_withWideInstruction_correctOffsets() {
        byte[] code = concat(
            bytes(0x00),                              // ldundefined (1 byte)
            bytes(0xFD, 0x80), le16(0x0100),          // wide createemptyarray (4 bytes)
            bytes(0x64)                                // return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());

        assertEquals(0, insns.get(0).getOffset());
        assertEquals(1, insns.get(0).getLength());

        assertEquals(1, insns.get(1).getOffset());
        assertEquals(4, insns.get(1).getLength());
        assertTrue(insns.get(1).isWide());

        assertEquals(5, insns.get(2).getOffset());
        assertEquals(1, insns.get(2).getLength());
    }

    @Test
    void testDisassemble_withOffset_startNotAtZero() {
        byte[] code = bytes(0x00, 0x00, 0x60, 0x05, 0x64);
        List<ArkInstruction> insns = disasm.disassemble(code, 2, 3);
        assertEquals(2, insns.size());
        assertEquals(2, insns.get(0).getOffset());
        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals(4, insns.get(1).getOffset());
        assertEquals("return", insns.get(1).getMnemonic());
    }

    // --- toString ---

    @Test
    void testToString_basicInstruction() {
        byte[] code = bytes(0x60, 0x05);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        String str = insn.toString();
        assertNotNull(str);
        assertTrue(str.contains("lda"));
        assertTrue(str.contains("v5"));
    }

    @Test
    void testToString_wideInstruction() {
        byte[] code = concat(bytes(0xFD, 0x80), le16(0x0100));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        String str = insn.toString();
        assertTrue(str.contains("[wide]"));
        assertTrue(str.contains("createemptyarray"));
    }

    // --- Next offset ---

    @Test
    void testGetNextOffset_returnsCorrectValue() {
        byte[] code = bytes(0x60, 0x05);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(2, insn.getNextOffset());
    }

    // --- IMM8_IMM16_IMM16 format ---

    @Test
    void testDecodeInstruction_ldprivateproperty() {
        byte[] code = concat(bytes(0xD8, 0x01), le16(0x0002), le16(0x0003));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("ldprivateproperty", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_IMM16, insn.getFormat());
        assertEquals(6, insn.getLength());
        assertEquals(3, insn.getOperands().size());
    }

    // --- IMM8_IMM16_IMM16_V8 format ---

    @Test
    void testDecodeInstruction_stprivateproperty() {
        byte[] code = concat(bytes(0xD9, 0x01), le16(0x0002), le16(0x0003), bytes(0x04));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("stprivateproperty", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_IMM16_V8, insn.getFormat());
        assertEquals(7, insn.getLength());
        assertEquals(4, insn.getOperands().size());
    }

    // --- V8_V8_V8 format ---

    @Test
    void testDecodeInstruction_asyncgeneratorresolve() {
        byte[] code = bytes(0xB8, 0x01, 0x02, 0x03);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("asyncgeneratorresolve", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_V8_V8, insn.getFormat());
        assertEquals(4, insn.getLength());
    }

    // --- IMM8_V8_IMM16 format ---

    @Test
    void testDecodeInstruction_stobjbyindex() {
        byte[] code = concat(bytes(0x3B, 0x01, 0x02), le16(0x0005));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("stobjbyindex", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_IMM16, insn.getFormat());
        assertEquals(5, insn.getLength());
    }

    // --- Unknown opcode ---

    @Test
    void testDecodeInstruction_unknownOpcode_returnsUnknown() {
        // Use an opcode byte that is not defined (0xEE is unlikely to be assigned)
        byte[] code = bytes(0xEE);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals(ArkInstructionFormat.UNKNOWN, insn.getFormat());
        assertTrue(insn.getMnemonic().startsWith("unknown_"));
        assertEquals(1, insn.getLength());
    }

    // --- Operand equals/hashCode ---

    @Test
    void testOperand_equals_sameTypeAndValue() {
        ArkOperand a = new ArkOperand(Type.REGISTER, 5);
        ArkOperand b = new ArkOperand(Type.REGISTER, 5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testOperand_toString_register() {
        ArkOperand op = new ArkOperand(Type.REGISTER, 42);
        assertEquals("v42", op.toString());
    }

    @Test
    void testOperand_toString_positiveJumpOffset() {
        ArkOperand op = new ArkOperand(Type.JUMP_OFFSET8, 10);
        assertEquals("0xA", op.toString());
    }

    @Test
    void testOperand_toString_negativeJumpOffset() {
        ArkOperand op = new ArkOperand(Type.JUMP_OFFSET8, -16);
        assertEquals("-0x10", op.toString());
    }

    @Test
    void testOperand_toString_immediate16() {
        ArkOperand op = new ArkOperand(Type.IMMEDIATE16, 0x1234);
        assertEquals("0x1234", op.toString());
    }

    // --- Instruction format lengths ---

    @Test
    void testInstructionFormatLengths() {
        assertEquals(1, ArkInstructionFormat.NONE.getLength());
        assertEquals(2, ArkInstructionFormat.V8.getLength());
        assertEquals(2, ArkInstructionFormat.IMM8.getLength());
        assertEquals(2, ArkInstructionFormat.IMM4_IMM4.getLength());
        assertEquals(3, ArkInstructionFormat.IMM8_V8.getLength());
        assertEquals(3, ArkInstructionFormat.IMM8_IMM8.getLength());
        assertEquals(4, ArkInstructionFormat.IMM8_IMM16.getLength());
        assertEquals(3, ArkInstructionFormat.IMM16.getLength());
        assertEquals(4, ArkInstructionFormat.IMM16_V8.getLength());
        assertEquals(3, ArkInstructionFormat.V8_V8.getLength());
        assertEquals(4, ArkInstructionFormat.IMM8_V8_V8.getLength());
        assertEquals(4, ArkInstructionFormat.V8_V8_V8.getLength());
        assertEquals(5, ArkInstructionFormat.IMM8_V8_V8_V8.getLength());
        assertEquals(5, ArkInstructionFormat.V8_V8_V8_V8.getLength());
        assertEquals(5, ArkInstructionFormat.IMM32.getLength());
        assertEquals(9, ArkInstructionFormat.IMM64.getLength());
        assertEquals(5, ArkInstructionFormat.IMM8_IMM16_IMM8.getLength());
        assertEquals(5, ArkInstructionFormat.IMM8_IMM16_V8.getLength());
        assertEquals(6, ArkInstructionFormat.IMM8_IMM16_IMM16.getLength());
        assertEquals(7, ArkInstructionFormat.IMM8_IMM16_IMM16_V8.getLength());
        assertEquals(8, ArkInstructionFormat.IMM8_IMM16_IMM16_IMM8_V8.getLength());
        assertEquals(4, ArkInstructionFormat.IMM8_IMM8_V8.getLength());
        assertEquals(3, ArkInstructionFormat.V8_IMM8.getLength());
        assertEquals(4, ArkInstructionFormat.V8_IMM16.getLength());
        assertEquals(5, ArkInstructionFormat.IMM8_V8_IMM16.getLength());
        // Wide formats
        assertEquals(2, ArkInstructionFormat.WIDE_NONE.getLength());
        assertEquals(4, ArkInstructionFormat.WIDE_IMM16.getLength());
        assertEquals(6, ArkInstructionFormat.WIDE_IMM16_IMM16.getLength());
        assertEquals(5, ArkInstructionFormat.WIDE_IMM16_V8.getLength());
        assertEquals(6, ArkInstructionFormat.WIDE_IMM16_V8_V8.getLength());
        assertEquals(4, ArkInstructionFormat.WIDE_V8_V8.getLength());
        assertEquals(1, ArkInstructionFormat.UNKNOWN.getLength());
    }

    // --- Full round-trip: known bytecode pattern ---

    @Test
    void testDisassemble_realisticSequence() {
        // Simulate: lda.str 0x42; sta v0; lda v0; add2 0x0, v1; sta v2; return
        byte[] code = concat(
            bytes(0x3E), le16(0x0042),        // lda.str 0x42
            bytes(0x61, 0x00),                 // sta v0
            bytes(0x60, 0x00),                 // lda v0
            bytes(0x0A, 0x00, 0x01),           // add2 0x0, v1
            bytes(0x61, 0x02),                 // sta v2
            bytes(0x64)                         // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(6, insns.size());

        // Verify offsets chain correctly
        int expectedOffset = 0;
        for (ArkInstruction insn : insns) {
            assertEquals(expectedOffset, insn.getOffset());
            expectedOffset = insn.getNextOffset();
        }
        assertEquals(code.length, expectedOffset);
    }

    @Test
    void testDisassemble_withJump_backward() {
        // jmp -2 (jump back to self)
        byte[] code = concat(
            bytes(0x4D, 0x00),   // jmp +0 (to offset 0)
            bytes(0x64)           // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(2, insns.size());
        assertEquals("jmp", insns.get(0).getMnemonic());
        assertEquals(0, insns.get(0).getOperands().get(0).getValue());
    }
}
