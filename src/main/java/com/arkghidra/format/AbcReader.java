package com.arkghidra.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Reads primitive types from a Panda .abc binary buffer.
 * All multi-byte values are little-endian per the spec.
 *
 * <p>All read methods convert {@link BufferUnderflowException} into
 * {@link AbcFormatException} with descriptive messages, ensuring that
 * malformed input never propagates raw NIO exceptions.
 */
public class AbcReader {
    private static final int MAX_ULEB128_BYTES = 5;
    private final ByteBuffer buf;

    public AbcReader(byte[] data) {
        this.buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int position() {
        return buf.position();
    }

    public void position(int pos) {
        if (pos < 0 || pos > buf.capacity()) {
            throw new AbcFormatException(
                    "Position " + pos + " out of range [0, " + buf.capacity() + "]");
        }
        buf.position(pos);
    }

    public int remaining() {
        return buf.remaining();
    }

    public int capacity() {
        return buf.capacity();
    }

    public byte readU8() {
        checkRemaining(1, "readU8");
        return buf.get();
    }

    public int readU16() {
        checkRemaining(2, "readU16");
        return buf.getShort() & 0xFFFF;
    }

    public long readU32() {
        checkRemaining(4, "readU32");
        return buf.getInt() & 0xFFFFFFFFL;
    }

    public long readU64() {
        checkRemaining(8, "readU64");
        return buf.getLong();
    }

    public int readS32() {
        checkRemaining(4, "readS32");
        return buf.getInt();
    }

    public void readBytes(byte[] dst, int offset, int length) {
        checkRemaining(length, "readBytes");
        buf.get(dst, offset, length);
    }

    public byte[] readBytes(int length) {
        if (length < 0) {
            throw new AbcFormatException("Negative byte count: " + length);
        }
        checkRemaining(length, "readBytes");
        byte[] dst = new byte[length];
        buf.get(dst);
        return dst;
    }

    public long readUleb128() {
        if (buf.remaining() < 1) {
            throw new AbcFormatException(
                    "Not enough data for readUleb128: need 1 bytes, have "
                            + buf.remaining() + " at offset " + buf.position());
        }
        long result = 0;
        int shift = 0;
        int byteCount = 0;
        while (true) {
            byte b = buf.get();
            byteCount++;
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            if (buf.remaining() < 1) {
                throw new AbcFormatException(
                        "Not enough data for readUleb128: need 1 bytes, have 0 at offset "
                                + buf.position());
            }
            shift += 7;
            if (byteCount >= MAX_ULEB128_BYTES) {
                throw new AbcFormatException(
                        "ULEB128 value exceeds " + MAX_ULEB128_BYTES + " bytes");
            }
        }
        return result;
    }

    public long readSleb128() {
        if (buf.remaining() < 1) {
            throw new AbcFormatException(
                    "Not enough data for readSleb128: need 1 bytes, have "
                            + buf.remaining() + " at offset " + buf.position());
        }
        long result = 0;
        int shift = 0;
        int byteCount = 0;
        byte b;
        while (true) {
            b = buf.get();
            byteCount++;
            result |= (long) (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                break;
            }
            if (buf.remaining() < 1) {
                throw new AbcFormatException(
                        "Not enough data for readSleb128: need 1 bytes, have 0 at offset "
                                + buf.position());
            }
            if (byteCount >= MAX_ULEB128_BYTES) {
                throw new AbcFormatException(
                        "SLEB128 value exceeds " + MAX_ULEB128_BYTES + " bytes");
            }
        }
        if (shift < 64 && (b & 0x40) != 0) {
            result |= -(1L << shift);
        }
        return result;
    }

    private void checkRemaining(int needed, String operation) {
        if (buf.remaining() < needed) {
            throw new AbcFormatException(
                    "Not enough data for " + operation + ": need " + needed
                            + " bytes, have " + buf.remaining() + " at offset "
                            + buf.position());
        }
    }

    public String readMutf8() {
        long header = readUleb128();
        int utf16Len = (int) (header >>> 1);
        if (utf16Len < 0) {
            throw new AbcFormatException("MUTF-8 string length overflow: " + header);
        }
        boolean isAscii = (header & 1) != 0;
        if (isAscii) {
            checkRemaining(utf16Len + 1, "readMutf8 ASCII");
            byte[] ascii = new byte[utf16Len];
            buf.get(ascii);
            if (buf.get() != 0) {
                throw new AbcFormatException("MUTF-8 string not null-terminated");
            }
            return new String(ascii, java.nio.charset.StandardCharsets.US_ASCII);
        }
        checkRemaining(1, "readMutf8");
        StringBuilder sb = new StringBuilder(utf16Len);
        for (int i = 0; i < utf16Len; i++) {
            checkRemaining(1, "readMutf8");
            int b1 = buf.get() & 0xFF;
            if (b1 == 0) {
                break;
            }
            if (b1 < 0x80) {
                sb.append((char) b1);
            } else if ((b1 & 0xE0) == 0xC0) {
                checkRemaining(1, "readMutf8");
                int b2 = buf.get() & 0xFF;
                sb.append((char) (((b1 & 0x1F) << 6) | (b2 & 0x3F)));
            } else if ((b1 & 0xF0) == 0xE0) {
                checkRemaining(2, "readMutf8");
                int b2 = buf.get() & 0xFF;
                int b3 = buf.get() & 0xFF;
                sb.append((char) (((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)));
            } else {
                throw new AbcFormatException(
                        "Invalid MUTF-8 byte: 0x" + Integer.toHexString(b1));
            }
        }
        checkRemaining(1, "readMutf8");
        if (buf.get() != 0) {
            throw new AbcFormatException("MUTF-8 string not null-terminated");
        }
        return sb.toString();
    }

    public void align(int alignment) {
        int pos = buf.position();
        int pad = (alignment - (pos % alignment)) % alignment;
        if (pad > 0) {
            int newPos = pos + pad;
            if (newPos > buf.capacity()) {
                throw new AbcFormatException(
                        "Alignment past end of data: offset " + pos
                                + " + pad " + pad + " > capacity " + buf.capacity());
            }
            buf.position(newPos);
        }
    }
}
