package com.arkghidra.disasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkOperand.Type;

/**
 * End-to-end disassembly verification tests.
 *
 * <p>Builds specific bytecode sequences for known patterns, disassembles them,
 * and verifies the instruction sequence is correct. Each test builds raw bytecode,
 * passes it through the disassembler, and checks both the instruction sequence
 * and operand values.</p>
 */
class E2EDisassemblyTest {

    private ArkDisassembler disasm;

    @BeforeEach
    void setUp() {
        disasm = new ArkDisassembler();
    }

    // =====================================================================
    // Helper methods
    // =====================================================================

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

    // =====================================================================
    // Simple function: ldai + return
    // =====================================================================

    @Test
    void testSimpleFunction_ldai_return() {
        // ldai 42, return
        byte[] code = concat(bytes(0x62), le32(42), bytes(0x64));

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(2, insns.size());

        ArkInstruction ldaiInsn = insns.get(0);
        assertEquals("ldai", ldaiInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM32, ldaiInsn.getFormat());
        assertEquals(5, ldaiInsn.getLength());
        assertEquals(1, ldaiInsn.getOperands().size());
        assertEquals(Type.IMMEDIATE32_SIGNED, ldaiInsn.getOperands().get(0).getType());
        assertEquals(42L, ldaiInsn.getOperands().get(0).getValue());

        ArkInstruction retInsn = insns.get(1);
        assertEquals("return", retInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.NONE, retInsn.getFormat());
        assertEquals(1, retInsn.getLength());
        assertEquals(0, retInsn.getOperands().size());
    }

    @Test
    void testSimpleFunction_ldai_negative_return() {
        // ldai -1, return
        byte[] code = concat(bytes(0x62), le32(-1), bytes(0x64));

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(2, insns.size());
        assertEquals(-1L, insns.get(0).getOperands().get(0).getValue());
    }

    @Test
    void testSimpleFunction_ldai_zero_return() {
        // ldai 0, return
        byte[] code = concat(bytes(0x62), le32(0), bytes(0x64));

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(2, insns.size());
        assertEquals(0L, insns.get(0).getOperands().get(0).getValue());
    }

    // =====================================================================
    // Arithmetic: lda + add2 + sta + return
    // =====================================================================

    @Test
    void testArithmetic_lda_add2_sta_return() {
        // lda v0, add2 0x00, v1, sta v2, return
        byte[] code = concat(
            bytes(0x60, 0x00),         // lda v0
            bytes(0x0A, 0x00, 0x01),   // add2 0x00, v1
            bytes(0x61, 0x02),         // sta v2
            bytes(0x64)                // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(4, insns.size());

        // Verify lda v0
        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals(1, insns.get(0).getOperands().size());
        assertEquals(Type.REGISTER, insns.get(0).getOperands().get(0).getType());
        assertEquals(0, insns.get(0).getOperands().get(0).getValue());

        // Verify add2 0x00, v1
        assertEquals("add2", insns.get(1).getMnemonic());
        assertEquals(2, insns.get(1).getOperands().size());
        assertEquals(Type.IMMEDIATE8, insns.get(1).getOperands().get(0).getType());
        assertEquals(0x00, insns.get(1).getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insns.get(1).getOperands().get(1).getType());
        assertEquals(1, insns.get(1).getOperands().get(1).getValue());

        // Verify sta v2
        assertEquals("sta", insns.get(2).getMnemonic());
        assertEquals(Type.REGISTER, insns.get(2).getOperands().get(0).getType());
        assertEquals(2, insns.get(2).getOperands().get(0).getValue());

        // Verify return
        assertEquals("return", insns.get(3).getMnemonic());
    }

    @Test
    void testArithmetic_sub2_multiply_divide() {
        // Test sub2
        byte[] subCode = concat(
            bytes(0x60, 0x02),         // lda v2
            bytes(0x0B, 0x00, 0x03),   // sub2 0x00, v3
            bytes(0x64)                // return
        );
        List<ArkInstruction> subInsns = disasm.disassemble(subCode, 0, subCode.length);
        assertEquals(3, subInsns.size());
        assertEquals("sub2", subInsns.get(1).getMnemonic());

        // Test mul2
        byte[] mulCode = concat(
            bytes(0x60, 0x02),         // lda v2
            bytes(0x0C, 0x00, 0x03),   // mul2 0x00, v3
            bytes(0x64)                // return
        );
        List<ArkInstruction> mulInsns = disasm.disassemble(mulCode, 0, mulCode.length);
        assertEquals(3, mulInsns.size());
        assertEquals("mul2", mulInsns.get(1).getMnemonic());

        // Test div2
        byte[] divCode = concat(
            bytes(0x60, 0x02),         // lda v2
            bytes(0x0D, 0x00, 0x03),   // div2 0x00, v3
            bytes(0x64)                // return
        );
        List<ArkInstruction> divInsns = disasm.disassemble(divCode, 0, divCode.length);
        assertEquals(3, divInsns.size());
        assertEquals("div2", divInsns.get(1).getMnemonic());
    }

    @Test
    void testArithmetic_mod2_and2_or2() {
        // mod2
        byte[] modCode = bytes(0x0E, 0x00, 0x01);
        ArkInstruction modInsn = disasm.decodeInstruction(modCode, 0);
        assertEquals("mod2", modInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, modInsn.getFormat());

        // and2
        byte[] andCode = bytes(0x18, 0x00, 0x01);
        ArkInstruction andInsn = disasm.decodeInstruction(andCode, 0);
        assertEquals("and2", andInsn.getMnemonic());

        // or2
        byte[] orCode = bytes(0x19, 0x00, 0x01);
        ArkInstruction orInsn = disasm.decodeInstruction(orCode, 0);
        assertEquals("or2", orInsn.getMnemonic());
    }

    // =====================================================================
    // Branch: ldai + jeqz + ldai + jmp + return
    // =====================================================================

    @Test
    void testBranch_jeqz_jmp_forward() {
        // ldai 0, jeqz +8, ldai 1, jmp +4, ldai 2, return
        // Offsets: 0(ldai), 5(jeqz), 7(ldai), 12(jmp), 14(ldai), 19(return)
        byte[] code = concat(
            bytes(0x62), le32(0),      // offset 0: ldai 0 (5 bytes)
            bytes(0x4F, 0x08),         // offset 5: jeqz +8 -> target 13 (wait: 5+8=13)
            bytes(0x62), le32(1),      // offset 7: ldai 1 (5 bytes)
            bytes(0x4D, 0x04),         // offset 12: jmp +4 -> target 16
            bytes(0x62), le32(2),      // offset 14: ldai 2 (5 bytes)
            bytes(0x64)                // offset 19: return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(6, insns.size());

        // Verify instruction mnemonics
        assertEquals("ldai", insns.get(0).getMnemonic());
        assertEquals("jeqz", insns.get(1).getMnemonic());
        assertEquals("ldai", insns.get(2).getMnemonic());
        assertEquals("jmp", insns.get(3).getMnemonic());
        assertEquals("ldai", insns.get(4).getMnemonic());
        assertEquals("return", insns.get(5).getMnemonic());

        // Verify branch operands
        assertEquals(Type.JUMP_OFFSET8, insns.get(1).getOperands().get(0).getType());
        assertEquals(8, insns.get(1).getOperands().get(0).getValue());

        assertEquals(Type.JUMP_OFFSET8, insns.get(3).getOperands().get(0).getType());
        assertEquals(4, insns.get(3).getOperands().get(0).getValue());

        // Verify offsets
        assertEquals(0, insns.get(0).getOffset());
        assertEquals(5, insns.get(1).getOffset());
        assertEquals(7, insns.get(2).getOffset());
        assertEquals(12, insns.get(3).getOffset());
        assertEquals(14, insns.get(4).getOffset());
        assertEquals(19, insns.get(5).getOffset());
    }

    @Test
    void testBranch_jeqz_with16bitOffset() {
        // ldai 0, jeqz_imm16 +256
        byte[] code = concat(
            bytes(0x62), le32(0),      // ldai 0
            bytes(0x50), le16(0x0100), // jeqz_imm16 +256
            bytes(0x64)                // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());
        assertEquals("jeqz", insns.get(1).getMnemonic());
        assertEquals(ArkInstructionFormat.IMM16, insns.get(1).getFormat());
        assertEquals(Type.JUMP_OFFSET16, insns.get(1).getOperands().get(0).getType());
        assertEquals(0x0100, insns.get(1).getOperands().get(0).getValue());
    }

    @Test
    void testBranch_backwardJmp() {
        // nop, nop, jmp -4 (back to offset 0)
        byte[] code = concat(
            bytes(0xD5),               // offset 0: nop
            bytes(0xD5),               // offset 1: nop
            bytes(0x4D, (byte) 0xFC),  // offset 2: jmp -4 -> target -2 (underflow, but valid encoding)
            bytes(0x64)                // offset 4: return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(4, insns.size());
        assertEquals("jmp", insns.get(2).getMnemonic());
        assertEquals(-4, insns.get(2).getOperands().get(0).getValue());
    }

    @Test
    void testBranch_jnez() {
        // lda v0, jnez +10, return
        byte[] code = concat(
            bytes(0x60, 0x00),        // lda v0
            bytes(0x51, 0x0A),        // jnez +10
            bytes(0x64)               // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());
        assertEquals("jnez", insns.get(1).getMnemonic());
        assertEquals(Type.JUMP_OFFSET8, insns.get(1).getOperands().get(0).getType());
        assertEquals(10, insns.get(1).getOperands().get(0).getValue());
    }

    @Test
    void testBranch_jeq_withRegister() {
        // jeq v3, +32
        byte[] code = bytes(0x5C, 0x03, 0x20);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("jeq", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.V8_IMM8, insn.getFormat());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.REGISTER, insn.getOperands().get(0).getType());
        assertEquals(3, insn.getOperands().get(0).getValue());
        assertEquals(Type.JUMP_OFFSET8, insn.getOperands().get(1).getType());
        assertEquals(0x20, insn.getOperands().get(1).getValue());
    }

    // =====================================================================
    // Call: lda + callarg0 + return
    // =====================================================================

    @Test
    void testCall_callarg0() {
        // lda v0, callarg0 0x05, sta v1, return
        byte[] code = concat(
            bytes(0x60, 0x00),        // lda v0
            bytes(0x29, 0x05),        // callarg0 0x05
            bytes(0x61, 0x01),        // sta v1
            bytes(0x64)               // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(4, insns.size());

        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals("callarg0", insns.get(1).getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8, insns.get(1).getFormat());
        assertEquals(Type.IMMEDIATE8, insns.get(1).getOperands().get(0).getType());
        assertEquals(0x05, insns.get(1).getOperands().get(0).getValue());
        assertEquals("sta", insns.get(2).getMnemonic());
        assertEquals("return", insns.get(3).getMnemonic());
    }

    @Test
    void testCall_callarg1() {
        // callarg1 0x03, v2
        byte[] code = bytes(0x2A, 0x03, 0x02);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callarg1", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, insn.getFormat());
        assertEquals(2, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE8, insn.getOperands().get(0).getType());
        assertEquals(3, insn.getOperands().get(0).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(1).getType());
        assertEquals(2, insn.getOperands().get(1).getValue());
    }

    @Test
    void testCall_callthis0() {
        // callthis0 0x07, v3
        byte[] code = bytes(0x2D, 0x07, 0x03);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callthis0", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, insn.getFormat());
    }

    @Test
    void testCall_callargs2() {
        // callargs2 0x02, v5, v6
        byte[] code = bytes(0x2B, 0x02, 0x05, 0x06);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callargs2", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_V8, insn.getFormat());
        assertEquals(3, insn.getOperands().size());
    }

    @Test
    void testCall_callargs3() {
        // callargs3 0x03, v1, v2, v3
        byte[] code = bytes(0x2C, 0x03, 0x01, 0x02, 0x03);
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("callargs3", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_V8_V8, insn.getFormat());
        assertEquals(4, insn.getOperands().size());
    }

    // =====================================================================
    // Property access: ldobjbyname + stobjbyname
    // =====================================================================

    @Test
    void testPropertyAccess_ldobjbyname_stobjbyname() {
        // lda v0, ldobjbyname 0x01, 0x0001, stobjbyname 0x01, 0x0002, v1, return
        byte[] code = concat(
            bytes(0x60, 0x00),                 // lda v0
            bytes(0x42, 0x01), le16(0x0001),   // ldobjbyname 0x01, 0x0001
            bytes(0x43, 0x01), le16(0x0002), bytes(0x01), // stobjbyname 0x01, 0x0002, v1
            bytes(0x64)                        // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(4, insns.size());

        // Verify ldobjbyname
        ArkInstruction ldObj = insns.get(1);
        assertEquals("ldobjbyname", ldObj.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, ldObj.getFormat());
        assertEquals(4, ldObj.getLength());
        assertEquals(2, ldObj.getOperands().size());
        assertEquals(Type.IMMEDIATE8, ldObj.getOperands().get(0).getType());
        assertEquals(0x01, ldObj.getOperands().get(0).getValue());
        assertEquals(Type.IMMEDIATE16, ldObj.getOperands().get(1).getType());
        assertEquals(0x0001, ldObj.getOperands().get(1).getValue());

        // Verify stobjbyname
        ArkInstruction stObj = insns.get(2);
        assertEquals("stobjbyname", stObj.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_V8, stObj.getFormat());
        assertEquals(5, stObj.getLength());
        assertEquals(3, stObj.getOperands().size());
        assertEquals(Type.IMMEDIATE8, stObj.getOperands().get(0).getType());
        assertEquals(0x01, stObj.getOperands().get(0).getValue());
        assertEquals(Type.IMMEDIATE16, stObj.getOperands().get(1).getType());
        assertEquals(0x0002, stObj.getOperands().get(1).getValue());
        assertEquals(Type.REGISTER, stObj.getOperands().get(2).getType());
        assertEquals(1, stObj.getOperands().get(2).getValue());
    }

    @Test
    void testPropertyAccess_ldobjbyvalue_stobjbyvalue() {
        // ldobjbyvalue 0x00, v1
        byte[] ldCode = bytes(0x37, 0x00, 0x01);
        ArkInstruction ldInsn = disasm.decodeInstruction(ldCode, 0);
        assertEquals("ldobjbyvalue", ldInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, ldInsn.getFormat());

        // stobjbyvalue 0x00, v1, v2
        byte[] stCode = bytes(0x38, 0x00, 0x01, 0x02);
        ArkInstruction stInsn = disasm.decodeInstruction(stCode, 0);
        assertEquals("stobjbyvalue", stInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8_V8, stInsn.getFormat());
    }

    @Test
    void testPropertyAccess_ldthisbyname() {
        // ldthisbyname 0x00, 0x0010
        byte[] code = concat(bytes(0x49, 0x00), le16(0x0010));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("ldthisbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
    }

    @Test
    void testPropertyAccess_stownbyname() {
        // stownbyname 0x00, 0x0042, v5
        byte[] code = concat(bytes(0x7A, 0x00), le16(0x0042), bytes(0x05));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("stownbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16_V8, insn.getFormat());
    }

    // =====================================================================
    // Wide instructions: 0xFD prefix variants
    // =====================================================================

    @Test
    void testWide_createemptyarray() {
        // 0xFD 0x80 0x0100
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
    void testWide_ldobjbyname() {
        // 0xFD 0x90 0x0001 0x0042
        byte[] code = concat(bytes(0xFD, 0x90), le16(0x0001), le16(0x0042));
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
    void testWide_stobjbyname() {
        // 0xFD 0x91 0x0001 0x0042 0x05
        byte[] code = concat(bytes(0xFD, 0x91), le16(0x0001), le16(0x0042), bytes(0x05));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("stobjbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM16_V8, insn.getFormat());
        assertEquals(7, insn.getLength());
        assertEquals(3, insn.getOperands().size());
        assertEquals(Type.IMMEDIATE16, insn.getOperands().get(0).getType());
        assertEquals(0x0001, insn.getOperands().get(0).getValue());
        assertEquals(Type.IMMEDIATE16, insn.getOperands().get(1).getType());
        assertEquals(0x0042, insn.getOperands().get(1).getValue());
        assertEquals(Type.REGISTER, insn.getOperands().get(2).getType());
        assertEquals(5, insn.getOperands().get(2).getValue());
    }

    @Test
    void testWide_mov() {
        // 0xFD 0x8F 0x0A 0x14
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
    void testWide_stobjbyvalue() {
        // 0xFD 0x86 0x0100 0x03 0x05
        byte[] code = concat(bytes(0xFD, 0x86), le16(0x0100), bytes(0x03, 0x05));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("stobjbyvalue", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_V8_V8, insn.getFormat());
        assertEquals(6, insn.getLength());
    }

    @Test
    void testWide_ldobjbyvalue() {
        // 0xFD 0x85 0x0001 0x05
        byte[] code = concat(bytes(0xFD, 0x85), le16(0x0001), bytes(0x05));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("ldobjbyvalue", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_V8, insn.getFormat());
        assertEquals(5, insn.getLength());
    }

    @Test
    void testWide_ldlexvar() {
        // 0xFD 0x8A 0x0001 0x0002
        byte[] code = concat(bytes(0xFD, 0x8A), le16(0x0001), le16(0x0002));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("ldlexvar", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM16, insn.getFormat());
        assertEquals(6, insn.getLength());
    }

    @Test
    void testWide_definefunc() {
        // 0xFD 0x74 0x0001 0x0005 0x02
        byte[] code = concat(bytes(0xFD, 0x74), le16(0x0001), le16(0x0005), bytes(0x02));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("definefunc", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM16_IMM8, insn.getFormat());
        assertEquals(7, insn.getLength());
    }

    @Test
    void testWide_newobjrange() {
        // 0xFD 0x83 0x0001 0x03 0x05
        byte[] code = concat(bytes(0xFD, 0x83), le16(0x0001), bytes(0x03, 0x05));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("newobjrange", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16_IMM8_V8, insn.getFormat());
        assertEquals(5, insn.getLength());
    }

    @Test
    void testWide_typeof() {
        // 0xFD 0x84 0x0100
        byte[] code = concat(bytes(0xFD, 0x84), le16(0x0100));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertTrue(insn.isWide());
        assertEquals("typeof", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.WIDE_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
    }

    // =====================================================================
    // Mixed wide + normal instruction sequences
    // =====================================================================

    @Test
    void testMixedSequence_wideAndNormalInstructions() {
        // lda v0, wide ldobjbyname, return
        byte[] code = concat(
            bytes(0x60, 0x00),                                        // lda v0 (2 bytes)
            bytes(0xFD, 0x90), le16(0x0001), le16(0x0042),           // wide ldobjbyname (6 bytes)
            bytes(0x64)                                               // return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());

        // Normal instruction
        assertEquals("lda", insns.get(0).getMnemonic());
        assertFalse(insns.get(0).isWide());
        assertEquals(0, insns.get(0).getOffset());
        assertEquals(2, insns.get(0).getLength());

        // Wide instruction
        assertTrue(insns.get(1).isWide());
        assertEquals("ldobjbyname", insns.get(1).getMnemonic());
        assertEquals(2, insns.get(1).getOffset());
        assertEquals(6, insns.get(1).getLength());

        // Normal instruction after wide
        assertEquals("return", insns.get(2).getMnemonic());
        assertFalse(insns.get(2).isWide());
        assertEquals(8, insns.get(2).getOffset());
    }

    @Test
    void testMixedSequence_multipleWideInstructions() {
        // wide createemptyarray, wide mov, return
        byte[] code = concat(
            bytes(0xFD, 0x80), le16(0x0100),        // wide createemptyarray (4 bytes)
            bytes(0xFD, 0x8F, 0x0A, 0x14),          // wide mov (4 bytes)
            bytes(0x64)                               // return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());

        assertTrue(insns.get(0).isWide());
        assertEquals(4, insns.get(0).getLength());
        assertEquals(0, insns.get(0).getOffset());

        assertTrue(insns.get(1).isWide());
        assertEquals(4, insns.get(1).getLength());
        assertEquals(4, insns.get(1).getOffset());

        assertFalse(insns.get(2).isWide());
        assertEquals(1, insns.get(2).getLength());
        assertEquals(8, insns.get(2).getOffset());
    }

    // =====================================================================
    // String and constant loading
    // =====================================================================

    @Test
    void testStringLoad_ldaStr() {
        // lda.str 0x0042, sta v0, return
        byte[] code = concat(
            bytes(0x3E), le16(0x0042),   // lda.str 0x0042
            bytes(0x61, 0x00),           // sta v0
            bytes(0x64)                  // return
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(3, insns.size());

        assertEquals("lda.str", insns.get(0).getMnemonic());
        assertEquals(ArkInstructionFormat.IMM16, insns.get(0).getFormat());
        assertEquals(3, insns.get(0).getLength());
        assertEquals(Type.IMMEDIATE16, insns.get(0).getOperands().get(0).getType());
        assertEquals(0x0042, insns.get(0).getOperands().get(0).getValue());
    }

    @Test
    void testFloatLoad_fldai() {
        // fldai 3.14
        double value = 3.14;
        byte[] code = concat(bytes(0x63), le64(Double.doubleToRawLongBits(value)));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);

        assertEquals("fldai", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM64, insn.getFormat());
        assertEquals(9, insn.getLength());
        assertEquals(Type.IMMEDIATE64_SIGNED, insn.getOperands().get(0).getType());
        assertEquals(Double.doubleToRawLongBits(value),
                insn.getOperands().get(0).getValue());
    }

    // =====================================================================
    // Lexical variable access
    // =====================================================================

    @Test
    void testLexVar_ldlexvar_stlexvar() {
        // ldlexvar 1, 3 (packed 0x31), stlexvar 2, 0 (packed 0x02)
        byte[] ldCode = bytes(0x3C, 0x31);
        ArkInstruction ldInsn = disasm.decodeInstruction(ldCode, 0);
        assertEquals("ldlexvar", ldInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM4_IMM4, ldInsn.getFormat());
        assertEquals(1, ldInsn.getOperands().get(0).getValue());
        assertEquals(3, ldInsn.getOperands().get(1).getValue());

        byte[] stCode = bytes(0x3D, 0x20);
        ArkInstruction stInsn = disasm.decodeInstruction(stCode, 0);
        assertEquals("stlexvar", stInsn.getMnemonic());
        assertEquals(0, stInsn.getOperands().get(0).getValue());
        assertEquals(2, stInsn.getOperands().get(1).getValue());
    }

    // =====================================================================
    // Global variable access
    // =====================================================================

    @Test
    void testGlobalAccess_tryldglobalbyname() {
        // tryldglobalbyname 0x00, 0x0042
        byte[] code = concat(bytes(0x3F, 0x00), le16(0x0042));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("tryldglobalbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
    }

    @Test
    void testGlobalAccess_trystglobalbyname() {
        // trystglobalbyname 0x00, 0x0042
        byte[] code = concat(bytes(0x40, 0x00), le16(0x0042));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("trystglobalbyname", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
    }

    // =====================================================================
    // Singleton and special instructions
    // =====================================================================

    @Test
    void testSingletons_ldundefinedToReturnundefined() {
        int[] opcodes = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x64, 0x65,
            0x6A, 0x6B, 0x6D, 0x6F, 0xD5
        };

        String[] expected = {
            "ldundefined", "ldnull", "ldtrue", "ldfalse",
            "createemptyobject", "return", "returnundefined",
            "ldnan", "ldinfinity", "ldglobal", "ldthis", "nop"
        };

        for (int i = 0; i < opcodes.length; i++) {
            byte[] code = bytes(opcodes[i]);
            ArkInstruction insn = disasm.decodeInstruction(code, 0);
            assertEquals(expected[i], insn.getMnemonic(),
                    "Mnemonic mismatch for opcode 0x"
                            + String.format("%02X", opcodes[i]));
            assertEquals(ArkInstructionFormat.NONE, insn.getFormat());
            assertEquals(1, insn.getLength());
            assertTrue(insn.getOperands().isEmpty());
        }
    }

    // =====================================================================
    // Full method pattern: while loop with conditional break
    // =====================================================================

    @Test
    void testFullMethod_whileLoopWithBreak() {
        // while loop: lda v0, jeqz +8, inc 0x0, sta v0, jmp -8, return
        byte[] code = concat(
            bytes(0x60, 0x00),         // offset 0: lda v0 (2 bytes)
            bytes(0x4F, 0x08),         // offset 2: jeqz +8 -> offset 10
            bytes(0x21, 0x00),         // offset 4: inc 0x0 (2 bytes)
            bytes(0x61, 0x00),         // offset 6: sta v0 (2 bytes)
            bytes(0x4D, (byte) 0xF8),  // offset 8: jmp -8 -> offset 0
            bytes(0x64)                // offset 10: return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(6, insns.size());

        // Verify all offsets chain correctly
        assertEquals(0, insns.get(0).getOffset());
        assertEquals(2, insns.get(1).getOffset());
        assertEquals(4, insns.get(2).getOffset());
        assertEquals(6, insns.get(3).getOffset());
        assertEquals(8, insns.get(4).getOffset());
        assertEquals(10, insns.get(5).getOffset());

        // Final offset = code length
        assertEquals(code.length, insns.get(5).getNextOffset());

        // Verify backward jump
        assertEquals(-8, insns.get(4).getOperands().get(0).getValue());
    }

    // =====================================================================
    // Full method pattern: if/else
    // =====================================================================

    @Test
    void testFullMethod_ifElse() {
        // ldai 0, jeqz +8, ldai 1, jmp +4, ldai 2, return
        byte[] code = concat(
            bytes(0x62), le32(0),      // offset 0: ldai 0 (5 bytes)
            bytes(0x4F, 0x08),         // offset 5: jeqz +8 -> offset 13
            bytes(0x62), le32(1),      // offset 7: ldai 1 (5 bytes)
            bytes(0x4D, 0x04),         // offset 12: jmp +4 -> offset 16
            bytes(0x62), le32(2),      // offset 14: ldai 2 (5 bytes)
            bytes(0x64)                // offset 19: return (1 byte)
        );

        List<ArkInstruction> insns = disasm.disassemble(code, 0, code.length);
        assertEquals(6, insns.size());
        assertEquals(20, code.length);

        // Verify offsets
        assertEquals(0, insns.get(0).getOffset());
        assertEquals(5, insns.get(1).getOffset());
        assertEquals(7, insns.get(2).getOffset());
        assertEquals(12, insns.get(3).getOffset());
        assertEquals(14, insns.get(4).getOffset());
        assertEquals(19, insns.get(5).getOffset());

        // Verify branch targets
        assertEquals(8, insns.get(1).getOperands().get(0).getValue());
        assertEquals(4, insns.get(3).getOperands().get(0).getValue());
    }

    // =====================================================================
    // Object creation and manipulation
    // =====================================================================

    @Test
    void testObjectCreation_createobjectwithbuffer() {
        // createobjectwithbuffer 0x01, 0x0003
        byte[] code = concat(bytes(0x07, 0x01), le16(0x0003));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("createobjectwithbuffer", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
    }

    @Test
    void testObjectCreation_createarraywithbuffer() {
        // createarraywithbuffer 0x01, 0x000A
        byte[] code = concat(bytes(0x06, 0x01), le16(0x000A));
        ArkInstruction insn = disasm.decodeInstruction(code, 0);
        assertEquals("createarraywithbuffer", insn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_IMM16, insn.getFormat());
        assertEquals(4, insn.getLength());
    }

    // =====================================================================
    // Comparison operations
    // =====================================================================

    @Test
    void testComparisons_eq_less_greater() {
        // eq
        byte[] eqCode = bytes(0x0F, 0x00, 0x01);
        ArkInstruction eqInsn = disasm.decodeInstruction(eqCode, 0);
        assertEquals("eq", eqInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8_V8, eqInsn.getFormat());

        // less
        byte[] lessCode = bytes(0x11, 0x00, 0x01);
        ArkInstruction lessInsn = disasm.decodeInstruction(lessCode, 0);
        assertEquals("less", lessInsn.getMnemonic());

        // greater
        byte[] greaterCode = bytes(0x13, 0x00, 0x01);
        ArkInstruction greaterInsn = disasm.decodeInstruction(greaterCode, 0);
        assertEquals("greater", greaterInsn.getMnemonic());
    }

    // =====================================================================
    // Unary operations
    // =====================================================================

    @Test
    void testUnaryOperations_inc_dec_neg_not() {
        // inc
        byte[] incCode = bytes(0x21, 0x00);
        ArkInstruction incInsn = disasm.decodeInstruction(incCode, 0);
        assertEquals("inc", incInsn.getMnemonic());
        assertEquals(ArkInstructionFormat.IMM8, incInsn.getFormat());

        // dec
        byte[] decCode = bytes(0x22, 0x00);
        ArkInstruction decInsn = disasm.decodeInstruction(decCode, 0);
        assertEquals("dec", decInsn.getMnemonic());

        // neg
        byte[] negCode = bytes(0x1F, 0x00);
        ArkInstruction negInsn = disasm.decodeInstruction(negCode, 0);
        assertEquals("neg", negInsn.getMnemonic());

        // not
        byte[] notCode = bytes(0x20, 0x00);
        ArkInstruction notInsn = disasm.decodeInstruction(notCode, 0);
        assertEquals("not", notInsn.getMnemonic());
    }
}
