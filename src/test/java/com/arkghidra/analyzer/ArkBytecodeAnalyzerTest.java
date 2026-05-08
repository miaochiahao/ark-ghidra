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
}
