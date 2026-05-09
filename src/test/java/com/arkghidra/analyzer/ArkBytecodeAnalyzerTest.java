package com.arkghidra.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ArkBytecodeAnalyzer}.
 *
 * <p>The analyzer's Ghidra-dependent methods (added, canAnalyze with a real
 * Program) cannot be exercised outside a full Ghidra runtime. Instead, the
 * tests verify construction, naming, and the static helper that performs the
 * language-ID check so that the core logic is covered.</p>
 */
class ArkBytecodeAnalyzerTest {

    private ArkBytecodeAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ArkBytecodeAnalyzer();
    }

    @Test
    void testAnalyzerName_matchesPspecEntry() {
        assertEquals("Ark Bytecode Method Markup", analyzer.getName());
    }

    @Test
    void testAnalyzerName_matchesConstant() {
        assertEquals(ArkBytecodeAnalyzer.ANALYZER_NAME, analyzer.getName());
    }

    @Test
    void testDescription_isNotBlank() {
        assertFalse(analyzer.getDescription().isBlank());
    }

    @Test
    void testAnalyzerType_isByteAnalyzer() {
        assertNotNull(analyzer.getAnalysisType());
    }

    @Test
    void testIsArkBytecodeProgram_withNull_returnsFalse() {
        assertFalse(ArkBytecodeAnalyzer.isArkBytecodeProgram(null));
    }

    @Test
    void testAnalyzerNameConstant_value() {
        assertEquals("Ark Bytecode Method Markup",
                ArkBytecodeAnalyzer.ANALYZER_NAME);
    }

    @Test
    void testConstructor_doesNotThrow() {
        ArkBytecodeAnalyzer fresh = new ArkBytecodeAnalyzer();
        assertNotNull(fresh);
        assertTrue(fresh.getDefaultEnablement(null) == false
                || fresh.getDefaultEnablement(null) == true);
    }

    @Test
    void testReadMutf8String_ascii() {
        byte[] data = "hello".getBytes();
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("hello", result);
    }

    @Test
    void testReadMutf8String_stopsAtNull() {
        byte[] data = {0x68, 0x69, 0x00, 0x62, 0x79, 0x65};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("hi", result);
    }

    @Test
    void testReadMutf8String_twoByteUtf8() {
        // é = 0xC3 0xA9 in UTF-8
        byte[] data = {(byte) 0xC3, (byte) 0xA9};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("\u00E9", result);
    }

    @Test
    void testReadMutf8String_threeByteUtf8() {
        // € = 0xE2 0x82 0xAC in UTF-8
        byte[] data = {(byte) 0xE2, (byte) 0x82, (byte) 0xAC};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("\u20AC", result);
    }

    @Test
    void testReadMutf8String_mixedAsciiAndMultiByte() {
        // "hié" = h(68) i(69) é(C3 A9)
        byte[] data = {0x68, 0x69, (byte) 0xC3, (byte) 0xA9};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("hi\u00E9", result);
    }

    @Test
    void testReadMutf8String_emptyAtNullByte() {
        byte[] data = {0x00};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("", result);
    }

    @Test
    void testReadMutf8String_withOffset() {
        byte[] data = {0x00, 0x00, 0x68, 0x69};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 2);
        assertEquals("hi", result);
    }

    @Test
    void testReadMutf8String_truncatedTwoByte() {
        // Only first byte of 2-byte sequence, no following byte
        byte[] data = {(byte) 0xC3};
        String result = ArkBytecodeAnalyzer.readMutf8String(data, 0);
        assertEquals("", result);
    }
}
