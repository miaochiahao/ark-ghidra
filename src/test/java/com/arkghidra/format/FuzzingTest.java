package com.arkghidra.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.DisassemblyException;
import com.arkghidra.format.AbcFile;

/**
 * Fuzzing and robustness tests for the ABC parser and decompiler.
 *
 * <p>These tests verify that malformed, truncated, or otherwise invalid input
 * is handled gracefully -- the parser should throw descriptive
 * {@link AbcFormatException} rather than raw NullPointerException,
 * ArrayIndexOutOfBoundsException, or BufferUnderflowException.
 */
class FuzzingTest {

    // --- Helper methods ---

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

    /**
     * Builds a valid ABC header in the given buffer at offset 0.
     * Returns the position after the header (offset 60).
     */
    private static int writeHeader(ByteBuffer bb, int fileSize,
            int numClasses, int classIdxOff,
            int numLnps, int lnpIdxOff,
            int numLiteralArrays, int literalArrayIdxOff,
            int numIndexRegions, int indexSectionOff) {
        bb.position(0);
        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(fileSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(numClasses);
        bb.putInt(classIdxOff);
        bb.putInt(numLnps);
        bb.putInt(lnpIdxOff);
        bb.putInt(numLiteralArrays);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(numIndexRegions);
        bb.putInt(indexSectionOff);
        return bb.position();
    }

    // ========================================================================
    // 1. Truncated files
    // ========================================================================

    @Test
    void testParse_emptyArray_throwsAbcFormatException() {
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(new byte[0]));
    }

    @Test
    void testParse_oneByte_throwsAbcFormatException() {
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(new byte[]{0x00}));
    }

    @Test
    void testParse_magicOnly_throwsAbcFormatException() {
        byte[] data = new byte[8];
        System.arraycopy(AbcHeader.MAGIC, 0, data, 0, 8);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_headerOnlyNoClassData_throwsAbcFormatException() {
        // Exactly HEADER_SIZE (60) bytes: magic + 13 uint32 fields
        byte[] data = new byte[AbcHeader.HEADER_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(AbcHeader.MAGIC);
        bb.putInt(0); // checksum
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(AbcHeader.HEADER_SIZE); // fileSize
        bb.putInt(0); // foreignOff
        bb.putInt(0); // foreignSize
        bb.putInt(1);  // numClasses
        bb.putInt(100); // classIdxOff -> past end
        bb.putInt(0);  // numLnps
        bb.putInt(0);  // lnpIdxOff
        bb.putInt(0);  // numLiteralArrays
        bb.putInt(0);  // literalArrayIdxOff
        bb.putInt(0);  // numIndexRegions
        bb.putInt(0);  // indexSectionOff
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_truncatedClassIndexArray_throwsAbcFormatException() {
        // Header says 2 classes at classIdxOff, but only 1 entry fits
        int size = 256;
        int classIdxOff = 100;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 2, classIdxOff, 0, 0, 0, 0, 0, 0);
        // Write only 1 class index entry at classIdxOff
        bb.position(classIdxOff);
        bb.putInt(200);
        // No second entry - reading should fail
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    // ========================================================================
    // 2. Corrupted magic
    // ========================================================================

    @Test
    void testParse_wrongMagic_throwsAbcFormatException() {
        byte[] data = new byte[256];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new byte[]{'X', 'Y', 'Z', 'W', 0, 0, 0, 0});
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_correctMagicButTruncatedAfterMagic_throwsAbcFormatException() {
        byte[] data = new byte[12];
        System.arraycopy(AbcHeader.MAGIC, 0, data, 0, 8);
        // version + checksum area too short for full header
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    // ========================================================================
    // 3. Invalid ULEB128 / SLEB128
    // ========================================================================

    @Test
    void testUleb128_noTerminator_throwsAbcFormatException() {
        // All continuation bytes, never terminating
        byte[] data = {
            (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80
        };
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readUleb128);
    }

    @Test
    void testUleb128_overflowExceeds5Bytes_throwsAbcFormatException() {
        // 6 continuation bytes
        byte[] data = {
            (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, 0x01
        };
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readUleb128);
    }

    @Test
    void testUleb128_truncatedAtContinuation_throwsAbcFormatException() {
        // Starts with continuation bit but no more data
        byte[] data = {(byte) 0x80};
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readUleb128);
    }

    @Test
    void testSleb128_noTerminator_throwsAbcFormatException() {
        byte[] data = {
            (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80
        };
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readSleb128);
    }

    @Test
    void testSleb128_truncatedAtContinuation_throwsAbcFormatException() {
        byte[] data = {(byte) 0x80};
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readSleb128);
    }

    @Test
    void testUleb128_exactly5Bytes_succeeds() {
        // Max valid ULEB128: 5 bytes, last byte has no continuation
        byte[] data = {
            (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, 0x0F
        };
        AbcReader r = new AbcReader(data);
        long val = r.readUleb128();
        // Should be (0x0F << 28) = 0xF0000000 = 4026531840
        assertEquals(4026531840L, val);
    }

    // ========================================================================
    // 4. Out-of-bounds offsets
    // ========================================================================

    @Test
    void testParse_classIndexOffsetPastEnd_throwsAbcFormatException() {
        int size = 256;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // classIdxOff = 500, past file end
        writeHeader(bb, size, 1, 500, 0, 0, 0, 0, 0, 0);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_classOffsetPastEnd_throwsAbcFormatException() {
        int size = 256;
        int classIdxOff = 100;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 0, 0);
        // Class index points to offset 9999, past end
        bb.position(classIdxOff);
        bb.putInt(9999);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_literalArrayOffsetPastEnd_throwsAbcFormatException() {
        int size = 256;
        int laIdxOff = 100;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 0, 0, 0, 0, 1, laIdxOff, 0, 0);
        // Literal array index points to offset 9999
        bb.position(laIdxOff);
        bb.putInt(9999);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_methodCodeOffsetPastEnd_throwsAbcFormatException() {
        int size = 512;
        int stringAreaOff = 200;
        int classIdxOff = 300;
        int classOff = 350;
        int codeOff = 9999; // Past end
        int regionHeaderOff = 450;
        int classRegionIdxOff = 490;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        // Region header
        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(400);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // String area
        bb.position(stringAreaOff);
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));

        // Class index
        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Class definition
        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00}); // end tags

        // Method: code offset points past end
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff); // Past file end
        bb.put(new byte[]{0x00});

        // Parsing succeeds, but getCodeForMethod should throw
        AbcFile abc = AbcFile.parse(data);
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);
        assertEquals(codeOff, method.getCodeOff());
        assertThrows(AbcFormatException.class, () -> abc.getCodeForMethod(method));
    }

    @Test
    void testParse_stringOffsetPastEnd_throwsAbcFormatException() {
        int size = 512;
        int classIdxOff = 300;
        int classOff = 350;
        int regionHeaderOff = 450;
        int classRegionIdxOff = 490;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        // Region header
        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(400);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Class index
        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Class definition with method whose name offset is past end
        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00}); // end tags

        // Method with string offset past end
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(9999); // nameOff past end
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x00}); // end tags

        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    // ========================================================================
    // 5. Zero / negative sizes
    // ========================================================================

    @Test
    void testParse_zeroClassCount_succeedsWithEmptyClasses() {
        int size = 128;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 0, 0, 0, 0, 0, 0, 0, 0);
        AbcFile abc = AbcFile.parse(data);
        assertTrue(abc.getClasses().isEmpty());
    }

    @Test
    void testParse_zeroMethodCount_succeeds() {
        int size = 512;
        int stringAreaOff = 200;
        int classIdxOff = 300;
        int classOff = 350;
        int regionHeaderOff = 450;
        int classRegionIdxOff = 490;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(400);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Class with 0 methods
        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(0)); // numMethods = 0
        bb.put(new byte[]{0x00}); // end tags

        AbcFile abc = AbcFile.parse(data);
        assertEquals(1, abc.getClasses().size());
        assertTrue(abc.getClasses().get(0).getMethods().isEmpty());
    }

    @Test
    void testParseCode_codeSizeZero_succeeds() {
        // Build minimal ABC with codeSize=0
        int size = 512;
        int stringAreaOff = 200;
        int classIdxOff = 300;
        int classOff = 350;
        int codeOff = 400;
        int regionHeaderOff = 430;
        int classRegionIdxOff = 470;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 20);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code section with codeSize=0
        bb.position(codeOff);
        bb.put(uleb128(2));  // numVregs
        bb.put(uleb128(1));  // numArgs
        bb.put(uleb128(0));  // codeSize = 0
        bb.put(uleb128(0));  // triesSize = 0

        AbcFile abc = AbcFile.parse(data);
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);
        AbcCode code = abc.getCodeForMethod(method);
        assertNotNull(code);
        assertEquals(0, code.getCodeSize());
    }

    @Test
    void testParseCode_numVregsZero_succeeds() {
        // Code section with numVregs=0
        int size = 512;
        int classIdxOff = 300;
        int classOff = 350;
        int codeOff = 400;
        int regionHeaderOff = 430;
        int classRegionIdxOff = 470;
        int stringAreaOff = 200;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 20);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code with numVregs=0
        bb.position(codeOff);
        bb.put(uleb128(0));  // numVregs = 0
        bb.put(uleb128(1));  // numArgs
        bb.put(uleb128(2));  // codeSize
        bb.put(uleb128(0));  // triesSize
        bb.put(new byte[]{0x00, 0x64}); // nop, return

        AbcFile abc = AbcFile.parse(data);
        AbcCode code = abc.getCodeForMethod(
                abc.getClasses().get(0).getMethods().get(0));
        assertNotNull(code);
        assertEquals(0, code.getNumVregs());
    }

    // ========================================================================
    // 6. AbcReader robustness
    // ========================================================================

    @Test
    void testReader_readU8_noData_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[0]);
        assertThrows(AbcFormatException.class, r::readU8);
    }

    @Test
    void testReader_readU16_noData_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[0]);
        assertThrows(AbcFormatException.class, r::readU16);
    }

    @Test
    void testReader_readU32_noData_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[0]);
        assertThrows(AbcFormatException.class, r::readU32);
    }

    @Test
    void testReader_readU64_noData_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[0]);
        assertThrows(AbcFormatException.class, r::readU64);
    }

    @Test
    void testReader_readBytes_negativeLength_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[10]);
        assertThrows(AbcFormatException.class, () -> r.readBytes(-1));
    }

    @Test
    void testReader_readBytes_moreThanAvailable_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[5]);
        assertThrows(AbcFormatException.class, () -> r.readBytes(10));
    }

    @Test
    void testReader_position_negative_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[10]);
        assertThrows(AbcFormatException.class, () -> r.position(-1));
    }

    @Test
    void testReader_position_pastEnd_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[10]);
        assertThrows(AbcFormatException.class, () -> r.position(11));
    }

    @Test
    void testReader_position_atCapacity_succeeds() {
        AbcReader r = new AbcReader(new byte[10]);
        r.position(10);
        assertEquals(10, r.position());
    }

    @Test
    void testReader_readMutf8_truncatedString_throwsAbcFormatException() {
        // Header says ASCII string of length 5, but only 3 bytes follow
        byte[] data = new byte[6];
        data[0] = (byte) 0x0B; // (5 << 1) | 1 = 11 = 0x0B
        data[1] = 'h';
        data[2] = 'e';
        data[3] = 'l';
        // Missing bytes and null terminator
        AbcReader r = new AbcReader(data);
        assertThrows(AbcFormatException.class, r::readMutf8);
    }

    @Test
    void testReader_alignPastEnd_throwsAbcFormatException() {
        AbcReader r = new AbcReader(new byte[5]);
        r.position(5);
        // Alignment to 4 from position 5 would go past end
        assertThrows(AbcFormatException.class, () -> r.align(4));
    }

    @Test
    void testReader_exceptionMessageIsDescriptive() {
        AbcReader r = new AbcReader(new byte[2]);
        AbcFormatException ex = assertThrows(
                AbcFormatException.class, () -> r.readU32());
        String msg = ex.getMessage();
        assertTrue(msg.contains("readU32"), "Message should mention operation: " + msg);
        assertTrue(msg.contains("need 4"), "Message should mention bytes needed: " + msg);
        assertTrue(msg.contains("have 2"), "Message should mention bytes available: " + msg);
    }

    // ========================================================================
    // 7. Decompiler robustness
    // ========================================================================

    @Test
    void testDisassemble_emptyArray_returnsEmptyList() {
        ArkDisassembler disasm = new ArkDisassembler();
        List<?> result = disasm.disassemble(new byte[0], 0, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDisassemble_nullArray_throwsDisassemblyException() {
        ArkDisassembler disasm = new ArkDisassembler();
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(null, 0, 0));
    }

    @Test
    void testDisassemble_invalidOpcode_0xFF_producesUnknownInstruction() {
        // 0xFF is not a valid opcode in the Ark ISA - maps to UNKNOWN format
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {(byte) 0xFF};
        List<?> insns = disasm.disassemble(code, 0, 1);
        assertEquals(1, insns.size());
    }

    @Test
    void testDisassemble_truncatedInstruction_throwsDisassemblyException() {
        // ldai (0x28) expects IMM32 format (5 bytes total), but only 2 bytes provided
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {0x28, 0x01}; // opcode + partial operand
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(code, 0, 2));
    }

    @Test
    void testDisassemble_truncatedWideInstruction_throwsDisassemblyException() {
        // Wide prefix (0xFD) with no sub-opcode
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {(byte) 0xFD};
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(code, 0, 1));
    }

    @Test
    void testDisassemble_wideInstructionTruncatedOperands_throwsDisassemblyException() {
        // Wide prefix + sub-opcode but not enough operand bytes
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {(byte) 0xFD, 0x28, 0x01}; // wide ldai, only 1 byte of imm16
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(code, 0, 3));
    }

    @Test
    void testDisassemble_negativeOffset_throwsDisassemblyException() {
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {0x00};
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(code, -1, 1));
    }

    @Test
    void testDisassemble_lengthExceedsData_throwsDisassemblyException() {
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {0x00};
        assertThrows(DisassemblyException.class,
                () -> disasm.disassemble(code, 0, 10));
    }

    @Test
    void testDisassemble_singleValidOpcode_succeeds() {
        ArkDisassembler disasm = new ArkDisassembler();
        byte[] code = {0x00}; // ldundefined (NONE format)
        List<?> insns = disasm.disassemble(code, 0, 1);
        assertEquals(1, insns.size());
    }

    @Test
    void testDecompileMethod_nullCode_returnsEmptyMethod() {
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        AbcMethod method = new AbcMethod(0, 0, "test", 0x0001, 0, 0);
        String result = decompiler.decompileMethod(method, null, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testDecompileMethod_emptyInstructions_returnsEmptyMethod() {
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        AbcMethod method = new AbcMethod(0, 0, "test", 0x0001, 1, 0);
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
    }

    @Test
    void testDecompileMethod_returnUndefinedOnly_returnsEmptyMethod() {
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        AbcMethod method = new AbcMethod(0, 0, "test", 0x0001, 1, 0);
        // returnundefined opcode = 0x65 (NONE format, 1 byte)
        AbcCode code = new AbcCode(0, 0, 1, new byte[]{0x65},
                Collections.emptyList(), 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
    }

    @Test
    void testDecompileInstructions_emptyList_returnsEmpty() {
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        String result = decompiler.decompileInstructions(Collections.emptyList());
        assertEquals("", result);
    }

    @Test
    void testDecompileFile_nullAbcFile_returnsEmpty() {
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        String result = decompiler.decompileFile(null);
        assertEquals("", result);
    }

    // ========================================================================
    // 8. Edge cases in reader
    // ========================================================================

    @Test
    void testReader_capacity() {
        AbcReader r = new AbcReader(new byte[100]);
        assertEquals(100, r.capacity());
    }

    @Test
    void testReader_remaining_afterPartialRead() {
        AbcReader r = new AbcReader(new byte[10]);
        r.readU8();
        assertEquals(9, r.remaining());
    }

    @Test
    void testReader_position_validWithinBounds() {
        AbcReader r = new AbcReader(new byte[100]);
        r.position(50);
        assertEquals(50, r.position());
        r.position(0);
        assertEquals(0, r.position());
    }

    @Test
    void testReader_sleb128_singleBytePositive() {
        AbcReader r = new AbcReader(new byte[]{0x01});
        assertEquals(1, r.readSleb128());
    }

    @Test
    void testReader_sleb128_singleByteNegative() {
        AbcReader r = new AbcReader(new byte[]{0x7F});
        assertEquals(-1, r.readSleb128());
    }

    // ========================================================================
    // 9. Multiple zero-count indices
    // ========================================================================

    @Test
    void testParse_allZeroCounts_succeedsWithEmptyCollections() {
        int size = AbcHeader.HEADER_SIZE;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(AbcHeader.MAGIC);
        bb.putInt(0);
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(size);
        // All remaining fields are 0
        for (int i = 0; i < 10; i++) {
            bb.putInt(0);
        }
        AbcFile abc = AbcFile.parse(data);
        assertTrue(abc.getClasses().isEmpty());
        assertTrue(abc.getProtos().isEmpty());
        assertTrue(abc.getLiteralArrays().isEmpty());
        assertTrue(abc.getRegionHeaders().isEmpty());
        assertEquals(0, abc.getHeader().getNumClasses());
    }

    @Test
    void testParse_lnpIndexOffsetPastEnd_throwsAbcFormatException() {
        int size = 256;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 0, 0, 1, 500, 0, 0, 0, 0);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    @Test
    void testParse_indexSectionOffsetPastEnd_throwsAbcFormatException() {
        int size = 256;
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 0, 0, 0, 0, 0, 0, 1, 500);
        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    // ========================================================================
    // 10. AbcCode with large codeSize past data bounds
    // ========================================================================

    @Test
    void testParseCode_codeSizePastDataBounds_throwsAbcFormatException() {
        // codeSize claims 100 bytes but only 4 are available
        int size = 512;
        int classIdxOff = 300;
        int classOff = 350;
        int codeOff = 400;
        int regionHeaderOff = 430;
        int classRegionIdxOff = 470;
        int stringAreaOff = 200;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 20);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code section with codeSize=500 (past end)
        bb.position(codeOff);
        bb.put(uleb128(2));
        bb.put(uleb128(1));
        bb.put(uleb128(500)); // codeSize way past file bounds
        bb.put(uleb128(0));

        AbcFile abc = AbcFile.parse(data);
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);
        assertThrows(AbcFormatException.class,
                () -> abc.getCodeForMethod(method));
    }

    // ========================================================================
    // 11. Field offset past end
    // ========================================================================

    @Test
    void testParse_fieldWithBadStringOffset_throwsAbcFormatException() {
        int size = 512;
        int classIdxOff = 300;
        int classOff = 350;
        int regionHeaderOff = 450;
        int classRegionIdxOff = 490;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(400);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Class with 1 field and 0 methods
        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));  // numFields = 1
        bb.put(uleb128(0));  // numMethods = 0
        bb.put(new byte[]{0x00}); // end tags

        // Field with string offset past end
        bb.putShort((short) 0); // classIdx
        bb.putShort((short) 0); // typeIdx
        bb.putInt(9999);         // nameOff -> past end
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x00}); // end tags

        assertThrows(AbcFormatException.class, () -> AbcFile.parse(data));
    }

    // ========================================================================
    // 12. Valid minimal ABC round-trip
    // ========================================================================

    @Test
    void testParse_validMinimalAbc_roundTripSucceeds() {
        int size = 512;
        int stringAreaOff = 200;
        int classIdxOff = 300;
        int classOff = 350;
        int codeOff = 400;
        int regionHeaderOff = 430;
        int classRegionIdxOff = 470;

        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(bb, size, 1, classIdxOff, 0, 0, 0, 0, 1, regionHeaderOff);

        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 20);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(classOff);
        bb.put(mutf8String("LTestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
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

        // Parse should succeed
        AbcFile abc = AbcFile.parse(data);
        assertEquals(1, abc.getClasses().size());
        assertEquals("LTestClass;", abc.getClasses().get(0).getName());

        // Code round-trip
        AbcMethod method = abc.getClasses().get(0).getMethods().get(0);
        AbcCode code = abc.getCodeForMethod(method);
        assertNotNull(code);
        assertEquals(2, code.getNumVregs());
        assertEquals(1, code.getNumArgs());
        assertEquals(4, code.getCodeSize());
    }

    // ========================================================================
    // 13. ULEB128 valid edge cases
    // ========================================================================

    @Test
    void testUleb128_zeroValue() {
        AbcReader r = new AbcReader(new byte[]{0x00});
        assertEquals(0, r.readUleb128());
    }

    @Test
    void testUleb128_maxSingleByte() {
        AbcReader r = new AbcReader(new byte[]{0x7F});
        assertEquals(127, r.readUleb128());
    }

    @Test
    void testUleb128_minTwoByte() {
        AbcReader r = new AbcReader(new byte[]{(byte) 0x80, 0x01});
        assertEquals(128, r.readUleb128());
    }

    @Test
    void testSleb128_zeroValue() {
        AbcReader r = new AbcReader(new byte[]{0x00});
        assertEquals(0, r.readSleb128());
    }
}
