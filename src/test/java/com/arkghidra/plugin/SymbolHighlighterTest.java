package com.arkghidra.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for SymbolHighlighter word extraction and occurrence finding.
 */
class SymbolHighlighterTest {

    private SymbolHighlighter highlighter;

    @BeforeEach
    void setUp() {
        highlighter = new SymbolHighlighter();
    }

    @Test
    @DisplayName("extractWordAt returns word when offset is inside identifier")
    void testExtractWordAt_insideWord() {
        String text = "let count = 0;";
        // "let count = 0;" — 'count' spans indices 4-8
        assertEquals("count", highlighter.extractWordAt(text, 4));
        assertEquals("count", highlighter.extractWordAt(text, 6));
        assertEquals("count", highlighter.extractWordAt(text, 8));
    }

    @Test
    @DisplayName("extractWordAt returns empty string for non-identifier char")
    void testExtractWordAt_nonIdentifier() {
        String text = "let count = 0;";
        assertEquals("", highlighter.extractWordAt(text, 3));
        assertEquals("", highlighter.extractWordAt(text, 10));
    }

    @Test
    @DisplayName("extractWordAt returns empty string for null text")
    void testExtractWordAt_nullText() {
        assertEquals("", highlighter.extractWordAt(null, 0));
    }

    @Test
    @DisplayName("extractWordAt returns empty string for out-of-bounds offset")
    void testExtractWordAt_outOfBounds() {
        assertEquals("", highlighter.extractWordAt("abc", -1));
        assertEquals("", highlighter.extractWordAt("abc", 10));
    }

    @Test
    @DisplayName("extractWordAt handles single-char word")
    void testExtractWordAt_singleChar() {
        assertEquals("x", highlighter.extractWordAt("x + y", 0));
        assertEquals("y", highlighter.extractWordAt("x + y", 4));
    }

    @Test
    @DisplayName("extractWordAt handles word with underscore and dollar")
    void testExtractWordAt_underscoreAndDollar() {
        String text = "_myVar$1 = 5;";
        assertEquals("_myVar$1", highlighter.extractWordAt(text, 0));
        assertEquals("_myVar$1", highlighter.extractWordAt(text, 4));
    }

    @Test
    @DisplayName("findAllOccurrences finds all whole-word matches")
    void testFindAllOccurrences_wholeWord() {
        String text = "let count = count + 1; // count";
        List<Integer> positions =
                highlighter.findAllOccurrences(text, "count");
        assertEquals(3, positions.size());
        assertEquals(4, positions.get(0));
        assertEquals(12, positions.get(1));
        assertEquals(26, positions.get(2));
    }

    @Test
    @DisplayName("findAllOccurrences does not match substrings")
    void testFindAllOccurrences_noSubstringMatch() {
        String text = "counter = count + 1;";
        List<Integer> positions =
                highlighter.findAllOccurrences(text, "count");
        assertEquals(1, positions.size());
        assertEquals(10, positions.get(0));
    }

    @Test
    @DisplayName("findAllOccurrences returns empty list for no match")
    void testFindAllOccurrences_noMatch() {
        List<Integer> positions =
                highlighter.findAllOccurrences("let x = 1;", "foo");
        assertTrue(positions.isEmpty());
    }

    @Test
    @DisplayName("findAllOccurrences returns empty list for null inputs")
    void testFindAllOccurrences_nullInputs() {
        assertTrue(highlighter.findAllOccurrences(null, "foo").isEmpty());
        assertTrue(highlighter.findAllOccurrences("text", null).isEmpty());
        assertTrue(highlighter.findAllOccurrences("text", "").isEmpty());
    }

    @Test
    @DisplayName("findAllOccurrences handles word at start and end of text")
    void testFindAllOccurrences_startAndEnd() {
        String text = "foo bar foo";
        List<Integer> positions =
                highlighter.findAllOccurrences(text, "foo");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0));
        assertEquals(8, positions.get(1));
    }

    @Test
    @DisplayName("ArkTSOutputProvider.findAllSubstrings is case-insensitive")
    void testFindAllSubstrings_caseInsensitive() {
        String text = "function Foo() { return foo; }";
        List<Integer> positions =
                ArkTSOutputProvider.findAllSubstrings(text, "foo");
        assertEquals(2, positions.size());
    }

    @Test
    @DisplayName("ArkTSOutputProvider.findAllSubstrings returns empty for no match")
    void testFindAllSubstrings_noMatch() {
        List<Integer> positions =
                ArkTSOutputProvider.findAllSubstrings("let x = 1;", "zzz");
        assertTrue(positions.isEmpty());
    }

    @Test
    @DisplayName("ArkTSOutputProvider.findAllSubstrings handles null inputs")
    void testFindAllSubstrings_nullInputs() {
        assertTrue(
                ArkTSOutputProvider.findAllSubstrings(null, "foo").isEmpty());
        assertTrue(
                ArkTSOutputProvider.findAllSubstrings("text", null).isEmpty());
        assertTrue(
                ArkTSOutputProvider.findAllSubstrings("text", "").isEmpty());
    }
}
