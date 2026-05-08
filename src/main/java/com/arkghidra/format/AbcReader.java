package com.arkghidra.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Reads primitive types from a Panda .abc binary buffer.
 * All multi-byte values are little-endian per the spec.
 */
public class AbcReader {
    private final ByteBuffer buf;

    public AbcReader(byte[] data) {
        this.buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int position() {
        return buf.position();
    }

    public void position(int pos) {
        buf.position(pos);
    }

    public int remaining() {
        return buf.remaining();
    }

    public byte readU8() {
        return buf.get();
    }

    public int readU16() {
        return buf.getShort() & 0xFFFF;
    }

    public long readU32() {
        return buf.getInt() & 0xFFFFFFFFL;
    }

    public long readU64() {
        return buf.getLong();
    }

    public int readS32() {
        return buf.getInt();
    }

    public void readBytes(byte[] dst, int offset, int length) {
        buf.get(dst, offset, length);
    }

    public byte[] readBytes(int length) {
        byte[] dst = new byte[length];
        buf.get(dst);
        return dst;
    }

    public long readUleb128() {
        long result = 0;
        int shift = 0;
        byte b;
        do {
            b = buf.get();
            result |= (long) (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    public long readSleb128() {
        long result = 0;
        int shift = 0;
        byte b;
        do {
            b = buf.get();
            result |= (long) (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        if (shift < 64 && (b & 0x40) != 0) {
            result |= -(1L << shift);
        }
        return result;
    }

    public String readMutf8() {
        long header = readUleb128();
        int utf16Len = (int) (header >>> 1);
        boolean isAscii = (header & 1) != 0;
        if (isAscii) {
            byte[] ascii = new byte[utf16Len];
            buf.get(ascii);
            if (buf.get() != 0) {
                throw new AbcFormatException("MUTF-8 string not null-terminated");
            }
            return new String(ascii, java.nio.charset.StandardCharsets.US_ASCII);
        }
        StringBuilder sb = new StringBuilder(utf16Len);
        for (int i = 0; i < utf16Len; i++) {
            int b1 = buf.get() & 0xFF;
            if (b1 == 0) {
                break;
            }
            if (b1 < 0x80) {
                sb.append((char) b1);
            } else if ((b1 & 0xE0) == 0xC0) {
                int b2 = buf.get() & 0xFF;
                sb.append((char) (((b1 & 0x1F) << 6) | (b2 & 0x3F)));
            } else if ((b1 & 0xF0) == 0xE0) {
                int b2 = buf.get() & 0xFF;
                int b3 = buf.get() & 0xFF;
                sb.append((char) (((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)));
            } else {
                throw new AbcFormatException("Invalid MUTF-8 byte: 0x" + Integer.toHexString(b1));
            }
        }
        if (buf.get() != 0) {
            throw new AbcFormatException("MUTF-8 string not null-terminated");
        }
        return sb.toString();
    }

    public void align(int alignment) {
        int pos = buf.position();
        int pad = (alignment - (pos % alignment)) % alignment;
        if (pad > 0) {
            buf.position(pos + pad);
        }
    }
}
