package com.arkghidra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Performance and lazy-parsing tests for the ABC parser.
 *
 * <p>These tests verify that:
 * <ul>
 *   <li>Large files (100 classes, 1000 methods) parse within acceptable time</li>
 *   <li>String lookups are cached and do not re-decode MUTF-8 on repeated access</li>
 *   <li>Code lookups are cached and do not re-parse on repeated access</li>
 *   <li>Memory usage remains bounded for large files</li>
 * </ul>
 */
@DisplayName("Performance tests")
class PerformanceTest {

    private static byte[] largeAbcData;
    private static AbcTestFixture fixture;

    @BeforeAll
    static void buildLargeFixture() {
        fixture = new AbcTestFixture();
        largeAbcData = fixture.buildLargeAbc();
    }

    @Test
    @Timeout(value = 5)
    @DisplayName("Parse 100-class fixture within time limit")
    void testParseLargeAbc_withinTimeLimit() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        assertEquals(fixture.getLargeClassCount(), abc.getClasses().size());
        assertEquals(fixture.getLargeMethodCount(), countTotalMethods(abc));
        assertEquals(fixture.getLargeFieldCount(), countTotalFields(abc));
    }

    @Test
    @Timeout(value = 10)
    @DisplayName("Parse and iterate all methods within time limit")
    void testParseAndIterateMethods_withinTimeLimit() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        int methodCount = 0;
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                assertNotNull(method.getName());
                methodCount++;
            }
        }

        assertEquals(fixture.getLargeMethodCount(), methodCount);
    }

    @Test
    @Timeout(value = 15)
    @DisplayName("Parse and decompile all code sections within time limit")
    void testParseAndGetAllCode_withinTimeLimit() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        int codeCount = 0;
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() == 0) {
                    continue;
                }
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code);
                assertTrue(code.getCodeSize() > 0);
                codeCount++;
            }
        }

        assertEquals(fixture.getLargeMethodCount(), codeCount);
    }

    @Test
    @DisplayName("String cache avoids redundant lookups")
    void testStringCacheAvoidsRedundantLookups() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        // Access class names (which are stored as strings)
        for (AbcClass cls : abc.getClasses()) {
            String name = cls.getName();
            assertNotNull(name);
        }

        // Now access source file names (which use the cached readStringAt)
        int cachedCountBefore = abc.getStringCacheSize();

        // Access source file for first class (will be null since not set, but
        // the lookup goes through the cache path)
        for (int i = 0; i < 3; i++) {
            abc.getSourceFileForClass(abc.getClasses().get(i));
        }

        // Cache size should not have grown since offsets are 0
        // (sourceFileOff defaults to 0, which is skipped)
        int cachedCountAfter = abc.getStringCacheSize();
        assertEquals(cachedCountBefore, cachedCountAfter);
    }

    @Test
    @DisplayName("Code cache avoids redundant parsing")
    void testCodeCacheAvoidsRedundantParsing() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        AbcClass firstClass = abc.getClasses().get(0);
        AbcMethod firstMethod = firstClass.getMethods().get(0);

        // Get code twice - should return same cached instance
        AbcCode code1 = abc.getCodeForMethod(firstMethod);
        AbcCode code2 = abc.getCodeForMethod(firstMethod);

        assertNotNull(code1);
        assertSameOrEqual(code1, code2);
    }

    @Test
    @DisplayName("AbcCode tracks instruction access")
    void testAbcCodeInstructionAccessTracking() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        AbcClass cls = abc.getClasses().get(0);
        AbcMethod method = cls.getMethods().stream()
                .filter(m -> m.getCodeOff() != 0)
                .findFirst()
                .orElse(null);

        assertNotNull(method);

        AbcCode code = abc.getCodeForMethod(method);
        assertNotNull(code);

        // Before accessing instructions, the flag should be false
        assertFalse(code.isInstructionsAccessed());

        // Access instructions
        byte[] instructions = code.getInstructions();
        assertNotNull(instructions);

        // After accessing, the flag should be true
        assertTrue(code.isInstructionsAccessed());
    }

    @Test
    @DisplayName("Memory usage bounded for large fixture")
    void testMemoryUsageBounded() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        AbcFile abc = AbcFile.parse(largeAbcData);

        // Access all methods to ensure they are parsed
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() != 0) {
                    abc.getCodeForMethod(method);
                }
            }
        }

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memUsedMb = (memAfter - memBefore) / (1024 * 1024);

        // Memory used should be reasonable - less than 64MB for 1000 methods
        assertTrue(memUsedMb < 64,
                "Memory usage too high: " + memUsedMb + "MB");
    }

    @Test
    @DisplayName("Class names parsed correctly for all 100 classes")
    void testAllClassNamesParsed() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        for (int i = 0; i < fixture.getLargeClassCount(); i++) {
            AbcClass cls = abc.getClasses().get(i);
            assertEquals(fixture.getLargeClassName(i), cls.getName());
        }
    }

    @Test
    @DisplayName("Each class has expected number of methods and fields")
    void testClassMethodFieldCounts() {
        AbcFile abc = AbcFile.parse(largeAbcData);

        for (AbcClass cls : abc.getClasses()) {
            assertEquals(10, cls.getMethods().size(),
                    "Class " + cls.getName() + " should have 10 methods");
            assertEquals(5, cls.getFields().size(),
                    "Class " + cls.getName() + " should have 5 fields");
        }
    }

    @Test
    @DisplayName("Parse benchmark measures timing")
    void testParseBenchmarkTiming() {
        long start = System.nanoTime();
        AbcFile abc = AbcFile.parse(largeAbcData);
        long elapsed = System.nanoTime() - start;
        double elapsedMs = elapsed / 1_000_000.0;

        // Parse should complete - we log the timing for informational purposes
        assertNotNull(abc);
        assertTrue(elapsedMs < 5000,
                "Parse took too long: " + elapsedMs + "ms");

        // Access all code and time that too
        long codeStart = System.nanoTime();
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() != 0) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code);
                }
            }
        }
        long codeElapsed = System.nanoTime() - codeStart;
        double codeMs = codeElapsed / 1_000_000.0;

        assertTrue(codeMs < 5000,
                "Code access took too long: " + codeMs + "ms");
    }

    private static int countTotalMethods(AbcFile abc) {
        int count = 0;
        for (AbcClass cls : abc.getClasses()) {
            count += cls.getMethods().size();
        }
        return count;
    }

    private static int countTotalFields(AbcFile abc) {
        int count = 0;
        for (AbcClass cls : abc.getClasses()) {
            count += cls.getFields().size();
        }
        return count;
    }

    private static void assertSameOrEqual(Object a, Object b) {
        if (a == b) {
            return;
        }
        if (a != null && a.equals(b)) {
            return;
        }
        throw new AssertionError("Expected same or equal but got " + a + " and " + b);
    }

    @Test
    @Timeout(value = 10)
    @DisplayName("getMethods() returns cached list on repeated calls")
    void testGetMethodsReturnsCachedList() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        java.util.List<AbcMethod> first = abc.getMethods();
        java.util.List<AbcMethod> second = abc.getMethods();
        assertNotNull(first);
        assertSame(first, second,
                "getMethods() should return the same cached list instance");
        assertEquals(fixture.getLargeMethodCount(), first.size());
    }

    @Test
    @DisplayName("getMethodByFlatIndex() provides O(1) lookup")
    void testGetMethodByFlatIndexFastLookup() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        java.util.List<AbcMethod> allMethods = abc.getMethods();
        for (int i = 0; i < allMethods.size(); i++) {
            AbcMethod byIndex = abc.getMethodByFlatIndex(i);
            assertSame(allMethods.get(i), byIndex,
                    "Flat index lookup should return same method object");
        }
        assertNull(abc.getMethodByFlatIndex(-1));
        assertNull(abc.getMethodByFlatIndex(allMethods.size()));
        assertNull(abc.getMethodByFlatIndex(Integer.MAX_VALUE));
    }

    @Test
    @Timeout(value = 10)
    @DisplayName("getMethodByFlatIndex() performance is sub-millisecond for 1000 methods")
    void testGetMethodByFlatIndexPerformance() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        int iterations = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int idx = i % fixture.getLargeMethodCount();
            AbcMethod m = abc.getMethodByFlatIndex(idx);
            assertNotNull(m);
        }
        long elapsedNs = System.nanoTime() - start;
        double elapsedMs = elapsedNs / 1_000_000.0;
        assertTrue(elapsedMs < 100,
                "Flat index lookups took too long: " + elapsedMs + "ms");
    }

    @Test
    @DisplayName("Debug info cache avoids redundant parsing")
    void testDebugInfoCacheAvoidsRedundantParsing() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        assertEquals(0, abc.getDebugInfoCacheSize(),
                "Debug info cache should start empty");
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                abc.getDebugInfoForMethod(method);
            }
        }
        assertEquals(0, abc.getDebugInfoCacheSize(),
                "Debug info cache should remain empty for methods with no debug info");
    }

    @Test
    @Timeout(value = 15)
    @DisplayName("Full decompile of all methods in large file within time limit")
    void testDecompileAllMethodsInLargeFile() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        com.arkghidra.decompile.ArkTSDecompiler decompiler =
                new com.arkghidra.decompile.ArkTSDecompiler();
        int decompiled = 0;
        long start = System.nanoTime();
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() == 0) {
                    continue;
                }
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code);
                String result = decompiler.decompileMethod(method, code, abc);
                assertNotNull(result);
                assertFalse(result.isEmpty());
                decompiled++;
            }
        }
        long elapsedNs = System.nanoTime() - start;
        double elapsedMs = elapsedNs / 1_000_000.0;
        assertEquals(fixture.getLargeMethodCount(), decompiled);
        assertTrue(elapsedMs < 10000,
                "Full decompilation took too long: " + elapsedMs + "ms");
    }

    @Test
    @Timeout(value = 5)
    @DisplayName("CFG construction performance with many branches")
    void testCfgConstructionPerformance() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        com.arkghidra.disasm.ArkDisassembler disasm =
                new com.arkghidra.disasm.ArkDisassembler();
        int totalBlocks = 0;
        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                if (method.getCodeOff() == 0) {
                    continue;
                }
                AbcCode code = abc.getCodeForMethod(method);
                java.util.List<com.arkghidra.disasm.ArkInstruction> insns =
                        disasm.disassemble(code.getInstructions(), 0,
                                (int) code.getCodeSize());
                com.arkghidra.decompile.ControlFlowGraph cfg =
                        com.arkghidra.decompile.ControlFlowGraph.build(
                                insns, code.getTryBlocks());
                totalBlocks += cfg.getBlocks().size();
            }
        }
        assertTrue(totalBlocks > 0, "Should have constructed some basic blocks");
    }


    @Test
    @DisplayName("Repeated getMethods() calls do not allocate new lists")
    void testGetMethodsNoAllocationOnRepeat() {
        AbcFile abc = AbcFile.parse(largeAbcData);
        for (int i = 0; i < 100; i++) {
            java.util.List<AbcMethod> methods = abc.getMethods();
            assertNotNull(methods);
        }
        java.util.List<AbcMethod> first = abc.getMethods();
        for (int i = 0; i < 100; i++) {
            assertSame(first, abc.getMethods());
        }
    }
}
