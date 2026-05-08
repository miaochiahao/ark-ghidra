package com.arkghidra.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbcParserTest {

    private static byte[] uleb128(long value) {
        byte[] buf = new byte[10];
        int i = 0;
        do {
            byte b = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buf[i++] = b;
        } while (value != 0);
        byte[] result = new byte[i];
        System.arraycopy(buf, 0, result, 0, i);
        return result;
    }

    private static byte[] sleb128(long value) {
        byte[] buf = new byte[10];
        int i = 0;
        boolean more = true;
        while (more) {
            byte b = (byte) (value & 0x7F);
            value >>= 7;
            if ((value == 0 && (b & 0x40) == 0) || (value == -1 && (b & 0x40) != 0)) {
                more = false;
            } else {
                b |= 0x80;
            }
            buf[i++] = b;
        }
        byte[] result = new byte[i];
        System.arraycopy(buf, 0, result, 0, i);
        return result;
    }

    private static byte[] mutf8String(String s) {
        byte[] strBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] header = uleb128(((long) s.length() << 1) | 1);
        byte[] result = new byte[header.length + strBytes.length + 1];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(strBytes, 0, result, header.length, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    private static byte[] buildMinimalAbc() {
        ByteBuffer bb = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 256;
        int codeOff = 350;
        int regionHeaderOff = 360;
        int classIdxOff = 400;
        int literalArrayIdxOff = 408;
        int lnpIdxOff = 416;
        int classRegionIdxOff = 420;
        int fileSize = 512;

        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(fileSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(1);
        bb.putInt(classIdxOff);
        bb.putInt(0);
        bb.putInt(lnpIdxOff);
        bb.putInt(0);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 32);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        byte[] className = mutf8String("Lcom/example/TestClass;");
        bb.put(className);
        int methodNameStringOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/TestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameStringOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        bb.position(codeOff);
        bb.put(uleb128(2));
        bb.put(uleb128(1));
        bb.put(uleb128(4));
        bb.put(uleb128(0));
        bb.put(new byte[]{0x00, 0x60, 0x00, 0x64});

        bb.position(0);
        byte[] result = new byte[fileSize];
        bb.get(result);
        return result;
    }

    @Test
    void testUleb128SingleByte() {
        AbcReader r = new AbcReader(new byte[]{0x01});
        assertEquals(1, r.readUleb128());
    }

    @Test
    void testUleb128MultiByte() {
        AbcReader r = new AbcReader(new byte[]{(byte) 0x80, 0x01});
        assertEquals(128, r.readUleb128());
    }

    @Test
    void testUleb128LargeValue() {
        AbcReader r = new AbcReader(uleb128(16384));
        assertEquals(16384, r.readUleb128());
    }

    @Test
    void testSleb128Positive() {
        AbcReader r = new AbcReader(sleb128(42));
        assertEquals(42, r.readSleb128());
    }

    @Test
    void testSleb128Negative() {
        AbcReader r = new AbcReader(sleb128(-1));
        assertEquals(-1, r.readSleb128());
    }

    @Test
    void testSleb128LargeNegative() {
        AbcReader r = new AbcReader(sleb128(-12345));
        assertEquals(-12345, r.readSleb128());
    }

    @Test
    void testMutf8AsciiString() {
        byte[] data = mutf8String("hello");
        AbcReader r = new AbcReader(data);
        assertEquals("hello", r.readMutf8());
    }

    @Test
    void testMutf8EmptyString() {
        byte[] data = mutf8String("");
        AbcReader r = new AbcReader(data);
        assertEquals("", r.readMutf8());
    }

    @Test
    void testMutf8UnicodeString() {
        String unicode = "\u4F60\u597D";
        byte[] strBytes = unicode.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(uleb128(((long) unicode.length() << 1) | 0));
        bb.put(strBytes);
        bb.put((byte) 0);
        byte[] data = new byte[bb.position()];
        bb.flip();
        bb.get(data);
        AbcReader r = new AbcReader(data);
        assertEquals(unicode, r.readMutf8());
    }

    @Test
    void testAlignNoPadding() {
        AbcReader r = new AbcReader(new byte[16]);
        r.position(8);
        r.align(4);
        assertEquals(8, r.position());
    }

    @Test
    void testAlignWithPadding() {
        AbcReader r = new AbcReader(new byte[16]);
        r.position(5);
        r.align(4);
        assertEquals(8, r.position());
    }

    @Test
    void testAbcHeaderParsing() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        AbcHeader header = abc.getHeader();

        assertTrue(header.hasValidMagic());
        assertEquals(2, header.getVersionNumber());
        assertEquals(1, header.getNumClasses());
        assertEquals(512, header.getFileSize());
    }

    @Test
    void testInvalidMagic() {
        byte[] data = new byte[64];
        System.arraycopy("INVALID\0".getBytes(), 0, data, 0, 8);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testClassParsing() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        List<AbcClass> classes = abc.getClasses();

        assertEquals(1, classes.size());
        AbcClass cls = classes.get(0);
        assertEquals("Lcom/example/TestClass;", cls.getName());
        assertEquals(0, cls.getSuperClassOff());
        assertEquals(1, cls.getAccessFlags());
        assertEquals(0, cls.getFields().size());
        assertEquals(1, cls.getMethods().size());
    }

    @Test
    void testMethodParsing() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);

        assertEquals("testMethod", method.getName());
        assertEquals(1, method.getAccessFlags());
        assertTrue(method.getCodeOff() > 0);
    }

    @Test
    void testCodeParsing() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);
        assertTrue(method.getCodeOff() > 0 && method.getCodeOff() < data.length,
                "codeOff should be within file, got: " + method.getCodeOff());
        AbcCode code = abc.getCodeForMethod(method);

        assertNotNull(code);
        assertEquals(2, code.getNumVregs());
        assertEquals(1, code.getNumArgs());
        assertEquals(4, code.getCodeSize());
        assertTrue(code.getTryBlocks().isEmpty());
    }

    @Test
    void testForeignOffsetCheck() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        assertFalse(abc.isForeignOffset(0));
    }

    @Test
    void testShortyTypeFromCode() {
        assertEquals(AbcProto.ShortyType.VOID, AbcProto.ShortyType.fromCode(0x01));
        assertEquals(AbcProto.ShortyType.I32, AbcProto.ShortyType.fromCode(0x07));
        assertEquals(AbcProto.ShortyType.F64, AbcProto.ShortyType.fromCode(0x0A));
        assertEquals(AbcProto.ShortyType.REF, AbcProto.ShortyType.fromCode(0x0D));
    }

    @Test
    void testShortyTypeInvalidCode() {
        assertThrows(IllegalArgumentException.class,
                () -> AbcProto.ShortyType.fromCode(0xFF));
    }

    @Test
    void testRegionHeaderContainsOffset() {
        AbcRegionHeader rh = new AbcRegionHeader(100, 200, 0, 0, 0, 0, 0, 0, 0, 0);
        assertTrue(rh.containsOffset(100));
        assertTrue(rh.containsOffset(150));
        assertFalse(rh.containsOffset(99));
        assertFalse(rh.containsOffset(200));
    }

    @Test
    void testAccessFlagsConstants() {
        assertEquals(0x0001, AbcAccessFlags.ACC_PUBLIC);
        assertEquals(0x0020, AbcAccessFlags.ACC_SUPER);
        assertEquals(0x0200, AbcAccessFlags.ACC_INTERFACE);
        assertEquals(0x4000, AbcAccessFlags.ACC_ENUM);
    }

    @Test
    void testCatchBlock() {
        AbcCatchBlock cb = new AbcCatchBlock(0, 10, 8, true);
        assertTrue(cb.isCatchAll());
        assertEquals(0, cb.getTypeIdx());
        assertEquals(10, cb.getHandlerPc());
        assertEquals(8, cb.getCodeSize());
    }

    @Test
    void testTryBlock() {
        AbcCatchBlock cb = new AbcCatchBlock(5, 20, 16, false);
        AbcTryBlock tb = new AbcTryBlock(0, 10, List.of(cb));
        assertEquals(0, tb.getStartPc());
        assertEquals(10, tb.getLength());
        assertEquals(1, tb.getCatchBlocks().size());
    }

    @Test
    void testAbcHeaderVersionBytes() {
        AbcHeader h = new AbcHeader(
                AbcHeader.MAGIC.clone(), 0,
                new byte[]{'1', '2', '0', '4'},
                100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(1204, h.getVersionNumber());
    }

    @Test
    void testNoCodeReturnsNull() {
        AbcMethod method = new AbcMethod(0, 0, "noCode", 0, 0, 0);
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        AbcCode code = abc.getCodeForMethod(method);
        assertNull(code);
    }
}
