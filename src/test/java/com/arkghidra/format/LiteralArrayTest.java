package com.arkghidra.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;

/**
 * Tests for AbcLiteralArray parsing and tag preservation.
 *
 * <p>Verifies that literal arrays parsed from ABC files preserve
 * type tags alongside raw byte values, enabling proper type-aware
 * decompilation of array and object literals.</p>
 */
class LiteralArrayTest {

    // =====================================================================
    // AbcLiteralArray structure tests
    // =====================================================================

    @Nested
    class LiteralArrayStructureTests {

        @Test
        void testLiteralArray_storesTags() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            List<AbcLiteralArray> arrays = abc.getLiteralArrays();
            assertFalse(arrays.isEmpty());

            for (AbcLiteralArray la : arrays) {
                assertNotNull(la.getTags());
                assertEquals(la.getLiterals().size(),
                        la.getTags().size());
            }
        }

        @Test
        void testLiteralArray_getTag_returnsCorrectTag() {
            List<Integer> tags = List.of(0x01, 0x03, 0x19);
            AbcLiteralArray la = new AbcLiteralArray(6,
                    List.of(new byte[] {1}, new byte[4], new byte[] {1}),
                    tags);

            assertEquals(0x01, la.getTag(0));
            assertEquals(0x03, la.getTag(1));
            assertEquals(0x19, la.getTag(2));
        }

        @Test
        void testLiteralArray_getTag_outOfBounds_returnsZero() {
            AbcLiteralArray la = new AbcLiteralArray(2,
                    List.of(new byte[] {1}),
                    List.of(0x01));

            assertEquals(0, la.getTag(-1));
            assertEquals(0, la.getTag(99));
        }

        @Test
        void testLiteralArray_getValue_returnsCorrectBytes() {
            byte[] val = new byte[] {0x42, 0x00, 0x00, 0x00};
            AbcLiteralArray la = new AbcLiteralArray(4,
                    List.of(val), List.of(0x03));

            byte[] result = la.getValue(0);
            assertNotNull(result);
            assertEquals(4, result.length);
            assertEquals(0x42, result[0]);
        }

        @Test
        void testLiteralArray_getValue_outOfBounds_returnsNull() {
            AbcLiteralArray la = new AbcLiteralArray(2,
                    List.of(new byte[] {1}),
                    List.of(0x01));

            assertEquals(null, la.getValue(-1));
            assertEquals(null, la.getValue(99));
        }

        @Test
        void testLiteralArray_size_returnsCount() {
            AbcLiteralArray la = new AbcLiteralArray(6,
                    List.of(new byte[1], new byte[2], new byte[3]),
                    List.of(0x01, 0x09, 0x03));

            assertEquals(3, la.size());
        }

        @Test
        void testLiteralArray_getNumLiterals_returnsRawCount() {
            AbcLiteralArray la = new AbcLiteralArray(6,
                    List.of(new byte[1], new byte[2], new byte[3]),
                    List.of(0x01, 0x09, 0x03));

            assertEquals(6, la.getNumLiterals());
        }
    }

    // =====================================================================
    // AbcFile literal array integration tests
    // =====================================================================

    @Nested
    class AbcFileLiteralArrayTests {

        @Test
        void testAbcFile_literalArraysNotEmpty() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            assertFalse(abc.getLiteralArrays().isEmpty());
        }

        @Test
        void testAbcFile_literalArrayIndexNotEmpty() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            assertFalse(abc.getLiteralArrayIndex().isEmpty());
        }

        @Test
        void testAbcFile_literalArraysAreUnmodifiable() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            List<AbcLiteralArray> arrays = abc.getLiteralArrays();
            assertNotNull(arrays);
            boolean threw = false;
            try {
                arrays.add(new AbcLiteralArray(0,
                        List.of(), List.of()));
            } catch (UnsupportedOperationException e) {
                threw = true;
            }
            assertTrue(threw);
        }
    }

    // =====================================================================
    // AbcFile.readMutf8At tests (package-visible)
    // =====================================================================

    @Nested
    class ReadMutf8AtTests {

        @Test
        void testReadMutf8At_ascii() {
            byte[] data = "hello".getBytes();
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("hello", result);
        }

        @Test
        void testReadMutf8At_stopsAtNull() {
            byte[] data = {0x68, 0x69, 0x00, 0x62, 0x79};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("hi", result);
        }

        @Test
        void testReadMutf8At_twoByteUtf8() {
            byte[] data = {(byte) 0xC3, (byte) 0xA9};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("\u00E9", result);
        }

        @Test
        void testReadMutf8At_threeByteUtf8() {
            byte[] data = {(byte) 0xE2, (byte) 0x82, (byte) 0xAC};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("\u20AC", result);
        }

        @Test
        void testReadMutf8At_withOffset() {
            byte[] data = {0x00, 0x00, 0x68, 0x69};
            String result = AbcFile.readMutf8At(data, 2);
            assertEquals("hi", result);
        }

        @Test
        void testReadMutf8At_emptyAtNullByte() {
            byte[] data = {0x00};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("", result);
        }

        @Test
        void testReadMutf8At_truncatedTwoByte() {
            byte[] data = {(byte) 0xC3};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("", result);
        }

        @Test
        void testReadMutf8At_mixedAsciiAndMultiByte() {
            byte[] data = {0x68, 0x69, (byte) 0xC3, (byte) 0xA9};
            String result = AbcFile.readMutf8At(data, 0);
            assertEquals("hi\u00E9", result);
        }
    }
}
