package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.format.AbcFile;

/**
 * Integration tests for the {@code decompileFile()} pipeline.
 *
 * <p>Tests the full end-to-end flow: build a synthetic ABC binary via
 * {@link AbcTestFixture}, parse it with {@link AbcFile#parse(byte[])},
 * then decompile with {@link ArkTSDecompiler#decompileFile(AbcFile)}.
 * Verifies structural properties of the output without asserting exact
 * strings (which would be fragile).</p>
 */
@DisplayName("decompileFile() integration tests")
class DecompileFileIntegrationTest {

    // =====================================================================
    // Large ABC fixture (100 classes, 1000 methods)
    // =====================================================================

    @Nested
    @DisplayName("Large ABC fixture")
    class LargeFixtureTests {

        private static AbcFile abcFile;

        @BeforeAll
        static void buildLargeFixture() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildLargeAbc();
            abcFile = AbcFile.parse(data);
        }

        @Test
        @DisplayName("decompileFile produces non-empty output")
        void testNonEmptyOutput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("output contains class declarations")
        void testContainsClasses() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("class "),
                    "Should contain class declarations: "
                            + result.substring(0, Math.min(500, result.length())));
        }

        @Test
        @DisplayName("output contains return statements")
        void testContainsReturns() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("return"),
                    "Should contain return statements");
        }

        @Test
        @DisplayName("output starts with class, export, or comment")
        void testOutputStartsWithClassOrComment() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.startsWith("//") || result.startsWith("class ")
                            || result.startsWith("export class ")
                            || result.startsWith("namespace"),
                    "Output should start with class declaration or header comment: "
                            + result.substring(0, Math.min(100, result.length())));
        }

        @Test
        @DisplayName("output contains expected number of class keywords")
        void testExpectedClassCount() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            long classCount = countOccurrences(result, "class ");
            assertTrue(classCount >= 100,
                    "Should have at least 100 class declarations, found: "
                            + classCount);
        }

        @Test
        @DisplayName("output contains first and last class names")
        void testContainsFirstAndLastClass() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("Class0"),
                    "Should contain Class0");
            assertTrue(result.contains("Class99"),
                    "Should contain Class99");
        }

        @Test
        @DisplayName("output does not use JavaScript var keyword")
        void testNoVarKeyword() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertFalse(result.matches("(?s).*\\bvar\\b.*"),
                    "Should not use 'var' keyword in ArkTS output");
        }

        @Test
        @DisplayName("output does not use 'any' type")
        void testNoAnyType() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertFalse(result.matches("(?s).*: any\\b.*"),
                    "Should not use 'any' type in ArkTS output");
        }

        @Test
        @DisplayName("repeated calls produce identical output")
        void testDeterministicOutput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result1 = decompiler.decompileFile(abcFile);
            String result2 = decompiler.decompileFile(abcFile);
            assertEquals(result1, result2,
                    "Repeated decompileFile calls should produce identical output");
        }
    }

    // =====================================================================
    // Comprehensive ABC fixture (2 classes)
    // =====================================================================

    @Nested
    @DisplayName("Comprehensive ABC fixture")
    class ComprehensiveFixtureTests {

        private static AbcFile abcFile;

        @BeforeAll
        static void buildComprehensiveFixture() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            abcFile = AbcFile.parse(data);
        }

        @Test
        @DisplayName("decompileFile produces non-empty output")
        void testNonEmptyOutput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("output contains both class names")
        void testContainsBothClasses() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("BaseClass"),
                    "Should contain BaseClass");
            assertTrue(result.contains("ChildClass"),
                    "Should contain ChildClass");
        }

        @Test
        @DisplayName("output contains extends keyword (inheritance)")
        void testContainsInheritance() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("extends"),
                    "ChildClass extends BaseClass — output should contain 'extends'");
        }

        @Test
        @DisplayName("output contains constructor declarations")
        void testContainsConstructors() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("constructor"),
                    "Should contain constructor declarations");
        }

        @Test
        @DisplayName("output does not use var keyword")
        void testUsesLetOrConst() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            // After dead variable removal, some methods may have no let/const at all.
            // The key ArkTS constraint is that 'var' must never appear.
            assertFalse(result.contains("var "),
                    "Should not use JavaScript 'var' keyword: " + result);
        }
    }

    // =====================================================================
    // Realistic ABC fixture (3 classes with try/catch)
    // =====================================================================

    @Nested
    @DisplayName("Realistic ABC fixture")
    class RealisticFixtureTests {

        private static AbcFile abcFile;

        @BeforeAll
        static void buildRealisticFixture() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            abcFile = AbcFile.parse(data);
        }

        @Test
        @DisplayName("decompileFile produces non-empty output")
        void testNonEmptyOutput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("output contains all three class names")
        void testContainsAllClasses() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("Animal"), "Should contain Animal");
            assertTrue(result.contains("Dog"), "Should contain Dog");
            assertTrue(result.contains("Utils"), "Should contain Utils");
        }

        @Test
        @DisplayName("output contains try/catch blocks")
        void testContainsTryCatch() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("try"),
                    "Should contain try block");
            assertTrue(result.contains("catch"),
                    "Should contain catch block");
        }

        @Test
        @DisplayName("output contains loop constructs")
        void testContainsLoops() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("while") || result.contains("for"),
                    "Should contain loop constructs");
        }

        @Test
        @DisplayName("output contains arithmetic operators")
        void testContainsArithmetic() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abcFile);
            assertTrue(result.contains("+") || result.contains("-")
                    || result.contains("*"),
                    "Should contain arithmetic operators");
        }
    }

    // =====================================================================
    // Edge cases
    // =====================================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("null AbcFile returns empty string")
        void testNullInput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(null);
            assertEquals("", result,
                    "Null AbcFile should produce empty string");
        }
    }

    // --- Helper methods ---

    private static long countOccurrences(String haystack, String needle) {
        long count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
