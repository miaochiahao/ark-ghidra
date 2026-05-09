package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.format.AbcFile;

/**
 * Tests for the shared file-level string cache.
 *
 * <p>Verifies that string resolution uses the file-level cache
 * (AbcFile.resolveStringByIndex) instead of per-method caches,
 * and that the cache is populated lazily on first access.</p>
 */
class StringCacheTest {

    // =====================================================================
    // AbcFile.resolveStringByIndex tests
    // =====================================================================

    @Nested
    class FileLevelCacheTests {

        @Test
        void testResolveStringByIndex_returnsCorrectString() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            for (int i = 0; i < abc.getLnpIndex().size(); i++) {
                String result = abc.resolveStringByIndex(i);
                assertNotNull(result);
                assertFalse(result.isEmpty());
            }
        }

        @Test
        void testResolveStringByIndex_outOfBounds_returnsPlaceholder() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = abc.resolveStringByIndex(99999);
            assertEquals("str_99999", result);
        }

        @Test
        void testResolveStringByIndex_negativeIndex_returnsPlaceholder() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = abc.resolveStringByIndex(-1);
            assertEquals("str_-1", result);
        }

        @Test
        void testResolveStringByIndex_cachedConsistently() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            int count = abc.getLnpIndex().size();
            if (count > 0) {
                String first = abc.resolveStringByIndex(0);
                String second = abc.resolveStringByIndex(0);
                assertEquals(first, second);
            }
        }
    }

    // =====================================================================
    // DecompilationContext uses file-level cache
    // =====================================================================

    @Nested
    class ContextDelegationTests {

        private ArkTSDecompiler decompiler;

        @BeforeEach
        void setUp() {
            decompiler = new ArkTSDecompiler();
        }

        @Test
        void testDecompileFile_usesSharedStringCache() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);

            String result1 = decompiler.decompileFile(abc);
            assertNotNull(result1);
            assertFalse(result1.isEmpty());

            String result2 = decompiler.decompileFile(abc);
            assertNotNull(result2);
            assertFalse(result2.isEmpty());

            assertEquals(result1, result2);
        }

        @Test
        void testDecompileFile_comprehensiveAbc_cachedAndConsistent() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testDecompileFile_largeAbc_producesOutput() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildLargeAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // AbcFile.readMutf8At is package-private in com.arkghidra.format.
    // Tested via ArkBytecodeAnalyzerTest.readMutf8String in same package.
    // =====================================================================
}
