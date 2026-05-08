package com.arkghidra.disasm;

import java.util.Locale;

/**
 * A single operand of a decoded Ark bytecode instruction.
 *
 * <p>Each operand has a type (e.g. register, immediate, string ID) and a numeric value.
 * For signed operand types (jump offsets, signed immediates), the value is stored as a
 * sign-extended long.
 */
public class ArkOperand {

    /**
     * The kind of value an operand represents.
     */
    public enum Type {
        /** Virtual register index (0-255). */
        REGISTER,
        /** Unsigned 8-bit immediate. */
        IMMEDIATE8,
        /** Unsigned 16-bit immediate. */
        IMMEDIATE16,
        /** Signed 8-bit immediate (jump offsets). */
        IMMEDIATE8_SIGNED,
        /** Signed 16-bit immediate (jump offsets). */
        IMMEDIATE16_SIGNED,
        /** Signed 32-bit immediate (ldai). */
        IMMEDIATE32_SIGNED,
        /** Signed 64-bit immediate (fldai). */
        IMMEDIATE64_SIGNED,
        /** 4-bit packed sub-immediate. */
        IMMEDIATE4,
        /** String table index (16-bit). */
        STRING_ID,
        /** Literal array index (16-bit). */
        LITERAL_ID,
        /** Method index. */
        METHOD_ID,
        /** Jump offset (relative, signed). */
        JUMP_OFFSET8,
        /** Jump offset (relative, signed 16-bit). */
        JUMP_OFFSET16,
        /** Field/class index. */
        FIELD_ID
    }

    private final Type type;
    private final long value;

    /**
     * Constructs an operand.
     *
     * @param type the operand type
     * @param value the operand value
     */
    public ArkOperand(Type type, long value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Constructs an operand with an int value.
     *
     * @param type the operand type
     * @param value the operand value
     */
    public ArkOperand(Type type, int value) {
        this(type, (long) value);
    }

    public Type getType() {
        return type;
    }

    public long getValue() {
        return value;
    }

    /**
     * Returns the value as a signed int, truncating if necessary.
     *
     * @return the value as int
     */
    public int intValue() {
        return (int) value;
    }

    @Override
    public String toString() {
        switch (type) {
            case REGISTER:
                return "v" + value;
            case JUMP_OFFSET8:
            case JUMP_OFFSET16:
                return formatSigned(value);
            case IMMEDIATE8_SIGNED:
            case IMMEDIATE16_SIGNED:
            case IMMEDIATE32_SIGNED:
            case IMMEDIATE64_SIGNED:
                return formatSigned(value);
            default:
                return String.format(Locale.ROOT, "0x%X", value);
        }
    }

    private static String formatSigned(long v) {
        if (v >= 0) {
            return String.format(Locale.ROOT, "0x%X", v);
        }
        return String.format(Locale.ROOT, "-0x%X", -v);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArkOperand)) {
            return false;
        }
        ArkOperand other = (ArkOperand) obj;
        return type == other.type && value == other.value;
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + Long.hashCode(value);
    }
}
