package com.arkghidra.format;

import java.util.List;

/**
 * Method prototype with shorty encoding and reference types.
 */
public class AbcProto {
    private final List<ShortyType> shorty;
    private final List<Integer> referenceTypes;

    public AbcProto(List<ShortyType> shorty, List<Integer> referenceTypes) {
        this.shorty = shorty;
        this.referenceTypes = referenceTypes;
    }

    public List<ShortyType> getShorty() {
        return shorty;
    }
    public List<Integer> getReferenceTypes() {
        return referenceTypes;
    }

    public ShortyType getReturnType() {
        return shorty.isEmpty() ? ShortyType.VOID : shorty.get(0);
    }

    public enum ShortyType {
        VOID(0x01), U1(0x02), I8(0x03), U8(0x04), I16(0x05), U16(0x06),
        I32(0x07), U32(0x08), F32(0x09), F64(0x0A), I64(0x0B), U64(0x0C),
        REF(0x0D), ANY(0x0E);

        private final int code;

        ShortyType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ShortyType fromCode(int code) {
            for (ShortyType t : values()) {
                if (t.code == code) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unknown shorty type code: 0x" + Integer.toHexString(code));
        }
    }
}
