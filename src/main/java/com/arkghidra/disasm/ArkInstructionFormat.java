package com.arkghidra.disasm;

/**
 * Instruction format enum for Ark bytecode.
 *
 * <p>Each format describes the operand layout following the opcode byte(s) and provides
 * the total instruction length. Formats are named by the type and count of their operands.
 * The opcode byte itself always consumes 1 byte; wide-prefixed instructions consume 2
 * opcode bytes (0xFD + sub-opcode).
 *
 * <p>Instruction encoding (little-endian):
 * <ul>
 *   <li>NONE: opcode (1 byte)</li>
 *   <li>V8: opcode + reg8 (2 bytes)</li>
 *   <li>IMM8: opcode + imm8 (2 bytes)</li>
 *   <li>IMM4_IMM4: opcode + packed 4-bit pair (2 bytes)</li>
 *   <li>IMM8_V8: opcode + imm8 + reg8 (3 bytes)</li>
 *   <li>IMM8_IMM8: opcode + imm8 + imm8 (3 bytes)</li>
 *   <li>IMM8_IMM16: opcode + imm8 + imm16 (4 bytes)</li>
 *   <li>IMM16: opcode + imm16 (3 bytes)</li>
 *   <li>IMM16_V8: opcode + imm16 + reg8 (4 bytes)</li>
 *   <li>V8_V8: opcode + reg8 + reg8 (3 bytes)</li>
 *   <li>IMM8_V8_V8: opcode + imm8 + reg8 + reg8 (4 bytes)</li>
 *   <li>V8_V8_V8: opcode + reg8 + reg8 + reg8 (4 bytes)</li>
 *   <li>IMM8_V8_V8_V8: opcode + imm8 + reg8 + reg8 + reg8 (5 bytes)</li>
 *   <li>IMM8_V8_V8_V8_V8: opcode + imm8 + reg8 + reg8 + reg8 + reg8 (6 bytes)</li>
 *   <li>V8_V8_V8_V8: opcode + reg8 + reg8 + reg8 + reg8 (5 bytes)</li>
 *   <li>IMM32: opcode + imm32 (5 bytes)</li>
 *   <li>IMM64: opcode + imm64 (9 bytes)</li>
 *   <li>IMM8_IMM16_IMM8: opcode + imm8 + imm16 + imm8 (5 bytes)</li>
 *   <li>IMM8_IMM16_V8: opcode + imm8 + imm16 + reg8 (5 bytes)</li>
 *   <li>IMM8_IMM16_IMM16: opcode + imm8 + imm16 + imm16 (6 bytes)</li>
 *   <li>IMM8_IMM16_IMM16_V8: opcode + imm8 + imm16 + imm16 + reg8 (7 bytes)</li>
 *   <li>IMM8_IMM8_V8: opcode + imm8 + imm8 + reg8 (4 bytes)</li>
 *   <li>V8_IMM8: opcode + reg8 + imm8 (3 bytes)</li>
 *   <li>V8_IMM16: opcode + reg8 + imm16 (4 bytes)</li>
 *   <li>IMM8_V8_IMM16: opcode + imm8 + reg8 + imm16 (5 bytes)</li>
 * </ul>
 */
public enum ArkInstructionFormat {

    /** Opcode only, no operands. */
    NONE(1),
    /** Opcode + 8-bit register. */
    V8(2),
    /** Opcode + 8-bit unsigned immediate. */
    IMM8(2),
    /** Opcode + packed 4-bit pair (two sub-immediates in one byte). */
    IMM4_IMM4(2),
    /** Opcode + 8-bit immediate + 8-bit register. */
    IMM8_V8(3),
    /** Opcode + two 8-bit immediates. */
    IMM8_IMM8(3),
    /** Opcode + 8-bit immediate + 16-bit immediate. */
    IMM8_IMM16(4),
    /** Opcode + 16-bit unsigned/signed immediate. */
    IMM16(3),
    /** Opcode + 16-bit immediate + 8-bit register. */
    IMM16_V8(4),
    /** Opcode + two 8-bit registers. */
    V8_V8(3),
    /** Opcode + 8-bit immediate + two 8-bit registers. */
    IMM8_V8_V8(4),
    /** Opcode + three 8-bit registers. */
    V8_V8_V8(4),
    /** Opcode + 8-bit immediate + three 8-bit registers. */
    IMM8_V8_V8_V8(5),
    /** Opcode + 8-bit immediate + four 8-bit registers. */
    IMM8_V8_V8_V8_V8(6),
    /** Opcode + four 8-bit registers. */
    V8_V8_V8_V8(5),
    /** Opcode + 32-bit signed immediate. */
    IMM32(5),
    /** Opcode + 64-bit signed immediate. */
    IMM64(9),
    /** Opcode + 8-bit immediate + 16-bit immediate + 8-bit immediate. */
    IMM8_IMM16_IMM8(5),
    /** Opcode + 8-bit immediate + 16-bit immediate + 8-bit register. */
    IMM8_IMM16_V8(5),
    /** Opcode + 8-bit immediate + two 16-bit immediates. */
    IMM8_IMM16_IMM16(6),
    /** Opcode + 8-bit immediate + two 16-bit immediates + 8-bit register. */
    IMM8_IMM16_IMM16_V8(7),
    /** Opcode + 8-bit immediate + two 16-bit immediates + 8-bit immediate + register. */
    IMM8_IMM16_IMM16_IMM8_V8(8),
    /** Opcode + two 8-bit immediates + 8-bit register. */
    IMM8_IMM8_V8(4),
    /** Opcode + 8-bit register + 8-bit signed immediate (jump with reg). */
    V8_IMM8(3),
    /** Opcode + 8-bit register + 16-bit signed immediate (wide jump with reg). */
    V8_IMM16(4),
    /** Opcode + 8-bit immediate + 8-bit register + 16-bit immediate. */
    IMM8_V8_IMM16(5),

    // --- Wide-prefixed (0xFD) formats: 2 opcode bytes ---
    /** Wide: opcode pair (0xFD + sub) only, no operands. */
    WIDE_NONE(2),
    /** Wide: opcode pair + 16-bit immediate. */
    WIDE_IMM16(4),
    /** Wide: opcode pair + 16-bit immediate + 16-bit immediate. */
    WIDE_IMM16_IMM16(6),
    /** Wide: opcode pair + 16-bit immediate + 8-bit register. */
    WIDE_IMM16_V8(5),
    /** Wide: opcode pair + 16-bit immediate + two 8-bit registers. */
    WIDE_IMM16_V8_V8(6),
    /** Wide: opcode pair + two 8-bit registers. */
    WIDE_V8_V8(4),
    /** Wide: opcode pair + 16-bit immediate + 8-bit immediate + 8-bit register. */
    WIDE_IMM16_IMM8_V8(5),
    /** Wide: opcode pair + 16-bit immediate + 16-bit immediate + 8-bit register. */
    WIDE_IMM16_IMM16_V8(7),
    /** Wide: opcode pair + 16-bit immediate + 16-bit immediate + 8-bit immediate. */
    WIDE_IMM16_IMM16_IMM8(7),
    /** Wide: opcode pair + 16-bit immediate + 8-bit register + 16-bit immediate. */
    WIDE_IMM16_V8_IMM16(7),
    /** Wide: opcode pair + 16-bit immediate + 8-bit register. */
    WIDE_IMM16_IMM8_V8_ALT(5),

    // --- CallRuntime (0xFB) prefixed formats: 2 opcode bytes ---
    /** CallRuntime: prefix + sub-opcode only, no operands. */
    PREF_NONE(2),
    /** CallRuntime: prefix + sub-opcode + 8-bit register. */
    PREF_V8(3),
    /** CallRuntime: prefix + sub-opcode + 8-bit immediate. */
    PREF_IMM8(3),
    /** CallRuntime: prefix + sub-opcode + 16-bit immediate. */
    PREF_IMM16(4),
    /** CallRuntime: prefix + sub-opcode + 8-bit immediate + 8-bit register. */
    PREF_IMM8_V8(4),
    /** CallRuntime: prefix + sub-opcode + 16-bit immediate + 8-bit register. */
    PREF_IMM16_V8(5),
    /** CallRuntime: prefix + sub-opcode + 16-bit immediate + 8-bit immediate. */
    PREF_IMM16_IMM8(4),
    /** CallRuntime: prefix + sub-opcode + two 8-bit immediates. */
    PREF_IMM8_IMM8(4),
    /** CallRuntime: prefix + sub-opcode + 8-bit immediate + 8-bit immediate
     *  + 8-bit register. */
    PREF_IMM8_IMM8_V8(5),

    /** Unknown or invalid instruction format. */
    UNKNOWN(1);

    private final int length;

    ArkInstructionFormat(int length) {
        this.length = length;
    }

    /**
     * Returns the total instruction length in bytes for this format.
     *
     * @return instruction length in bytes
     */
    public int getLength() {
        return length;
    }
}
