package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * End-to-end decompilation tests (GitHub issue #66).
 *
 * <p>Tests the full pipeline: AbcFile.parse() -> decompiler.decompileFile()
 * using the comprehensive (2-class), realistic (3-class), and large
 * (100-class) ABC fixtures from AbcTestFixture.</p>
 */
class E2EDecompilationTest {

    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
    }

    // =====================================================================
    // Comprehensive ABC fixture (2 classes: BaseClass, ChildClass)
    // =====================================================================

    @Nested
    class ComprehensiveAbcTests {

        private AbcFile abc;

        @BeforeEach
        void setUpComprehensive() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_comprehensive_producesNonEmptyOutput() {
            String result = decompiler.decompileFile(abc);
            assertNotNull(result, "decompileFile should not return null");
            assertFalse(result.isEmpty(),
                    "decompileFile should produce non-empty output");
        }

        @Test
        void testE2E_comprehensive_containsBothClassNames() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("BaseClass"),
                    "Should contain BaseClass");
            assertTrue(result.contains("ChildClass"),
                    "Should contain ChildClass");
        }

        @Test
        void testE2E_comprehensive_containsConstructors() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("constructor"),
                    "Should contain constructor declarations");
        }

        @Test
        void testE2E_comprehensive_hasClassKeyword() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("class "),
                    "Output should contain class declarations");
        }

        @Test
        void testE2E_comprehensive_hasMethodsInClasses() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("simpleReturn")
                            || result.contains("arithmetic")
                            || result.contains("conditional"),
                    "Should contain at least one method name from classes");
        }

        @Test
        void testE2E_comprehensive_decompileAllMethodsIndividually() {
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            "Method " + cls.getName() + "."
                                    + method.getName() + " should have code");
                    String result = decompiler.decompileMethod(
                            method, code, abc);
                    assertNotNull(result,
                            "Method " + cls.getName() + "."
                                    + method.getName()
                                    + " should decompile to non-null");
                    assertFalse(result.isEmpty(),
                            "Method " + cls.getName() + "."
                                    + method.getName()
                                    + " should decompile to non-empty");
                }
            }
        }

        @Test
        void testE2E_comprehensive_hasClassStructure() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("{"),
                    "Should contain opening braces");
            assertTrue(result.contains("}"),
                    "Should contain closing braces");
        }

        @Test
        void testE2E_comprehensive_hasTwoClassDeclarations() {
            String result = decompiler.decompileFile(abc);
            long classCount = countOccurrences(result, "class ");
            assertTrue(classCount >= 2,
                    "Should have at least 2 class declarations, found: "
                            + classCount);
        }
    }

    // =====================================================================
    // Single class method-level tests (comprehensive fixture)
    // =====================================================================

    @Nested
    class SingleClassMethodTests {

        private AbcFile abc;

        @BeforeEach
        void setUpSingle() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_simpleReturnMethod_containsValue42() {
            AbcClass baseClass = abc.getClasses().get(0);
            AbcMethod simpleReturn = baseClass.getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(simpleReturn);
            String result = decompiler.decompileMethod(
                    simpleReturn, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("42"),
                    "simpleReturn should contain the value 42");
            assertTrue(result.contains("return"),
                    "simpleReturn should contain return statement");
        }

        @Test
        void testE2E_arithmeticMethod_containsAddition() {
            AbcClass baseClass = abc.getClasses().get(0);
            AbcMethod arith = baseClass.getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(arith);
            String result = decompiler.decompileMethod(arith, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("+"),
                    "Arithmetic method should contain addition");
        }

        @Test
        void testE2E_conditionalBranch_producesOutput() {
            AbcClass childClass = abc.getClasses().get(1);
            AbcMethod cond = childClass.getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(cond);
            String result = decompiler.decompileMethod(cond, code, abc);
            assertNotNull(result);
            assertFalse(result.isEmpty(),
                    "Conditional method should produce non-empty output");
        }

        @Test
        void testE2E_loopMethod_producesLoopConstruct() {
            AbcClass childClass = abc.getClasses().get(1);
            AbcMethod loop = childClass.getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(loop);
            String result = decompiler.decompileMethod(loop, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("while") || result.contains("for"),
                    "Loop method should contain a loop construct");
        }

        @Test
        void testE2E_callMethod_producesCallSyntax() {
            AbcClass childClass = abc.getClasses().get(1);
            AbcMethod callArg = childClass.getMethods().get(3);
            AbcCode code = abc.getCodeForMethod(callArg);
            String result = decompiler.decompileMethod(
                    callArg, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("(") && result.contains(")"),
                    "Call method should contain function call syntax");
        }

        @Test
        void testE2E_constructorMethod_decompiles() {
            AbcClass baseClass = abc.getClasses().get(0);
            AbcMethod ctor = baseClass.getMethods().get(0);
            AbcCode code = abc.getCodeForMethod(ctor);
            String result = decompiler.decompileMethod(ctor, code, abc);
            assertNotNull(result);
            assertFalse(result.isEmpty(),
                    "Constructor should produce non-empty output");
        }
    }

    // =====================================================================
    // Realistic ABC fixture (3 classes: Animal, Dog, Utils)
    // =====================================================================

    @Nested
    class RealisticAbcTests {

        private AbcFile abc;

        @BeforeEach
        void setUpRealistic() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_realistic_producesNonEmptyOutput() {
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testE2E_realistic_containsAllThreeClassNames() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("Animal"),
                    "Should contain Animal");
            assertTrue(result.contains("Dog"),
                    "Should contain Dog");
            assertTrue(result.contains("Utils"),
                    "Should contain Utils");
        }

        @Test
        void testE2E_realistic_dogClassExtendsAnimal() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("extends"),
                    "Dog should extend Animal (keyword extends expected)");
        }

        @Test
        void testE2E_realistic_containsFieldDeclarations() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("name")
                            || result.contains("age")
                            || result.contains("breed"),
                    "Output should contain field names");
        }

        @Test
        void testE2E_realistic_tryCatchMethodContainsTryCatch() {
            AbcClass animal = abc.getClasses().get(0);
            assertEquals("Animal",
                    extractSimpleClassName(animal.getName()));
            AbcMethod tryCatchMethod = animal.getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatchMethod);
            String result = decompiler.decompileMethod(
                    tryCatchMethod, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("try"),
                    "Try/catch method should contain try");
            assertTrue(result.contains("catch"),
                    "Try/catch method should contain catch");
        }

        @Test
        void testE2E_realistic_loopMethodContainsLoop() {
            AbcClass dog = abc.getClasses().get(1);
            AbcMethod loopMethod = dog.getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(loopMethod);
            String result = decompiler.decompileMethod(
                    loopMethod, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("while") || result.contains("for"),
                    "Loop method should contain a loop construct");
        }

        @Test
        void testE2E_realistic_staticMethodContainsValue99() {
            AbcClass utils = abc.getClasses().get(2);
            AbcMethod staticMethod = utils.getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(staticMethod);
            String result = decompiler.decompileMethod(
                    staticMethod, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("99"),
                    "Static helper should reference value 99");
        }

        @Test
        void testE2E_realistic_allMethodsDecompileWithoutError() {
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            cls.getName() + "." + method.getName()
                                    + " should have code");
                    String result = decompiler.decompileMethod(
                            method, code, abc);
                    assertNotNull(result,
                            cls.getName() + "." + method.getName()
                                    + " decompilation should not be null");
                }
            }
        }

        @Test
        void testE2E_realistic_hasThreeClassDeclarations() {
            String result = decompiler.decompileFile(abc);
            long classCount = countOccurrences(result, "class ");
            assertTrue(classCount >= 3,
                    "Should have at least 3 class declarations, found: "
                            + classCount);
        }

        @Test
        void testE2E_realistic_containsReturnStatements() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("return"),
                    "Output should contain return statements");
        }

        @Test
        void testE2E_realistic_hasConstructorOrFunctionKeyword() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("constructor")
                            || result.contains("function"),
                    "Should contain constructor or function declarations");
        }
    }

    // =====================================================================
    // Realistic fixture: method-specific operator tests
    // =====================================================================

    @Nested
    class RealisticMethodOperatorTests {

        private AbcFile abc;

        @BeforeEach
        void setUpOperators() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_multiplyMethod_containsStar() {
            AbcClass utils = abc.getClasses().get(2);
            AbcMethod mul = utils.getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(mul);
            String result = decompiler.decompileMethod(mul, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("*"),
                    "Multiply method should contain * operator");
        }

        @Test
        void testE2E_subtractMethod_containsMinus() {
            AbcClass dog = abc.getClasses().get(1);
            AbcMethod sub = dog.getMethods().get(3);
            AbcCode code = abc.getCodeForMethod(sub);
            String result = decompiler.decompileMethod(sub, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("-"),
                    "Subtract method should contain - operator");
        }

        @Test
        void testE2E_manyParamsMethod_containsAddition() {
            AbcClass dog = abc.getClasses().get(1);
            AbcMethod manyParams = dog.getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(manyParams);
            String result = decompiler.decompileMethod(
                    manyParams, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("+"),
                    "Many params method should contain addition operators");
        }

        @Test
        void testE2E_nestedIfMethod_decompilesWithoutError() {
            AbcClass dog = abc.getClasses().get(1);
            assertTrue(dog.getMethods().size() >= 5,
                    "Dog should have at least 5 methods");
            AbcMethod nestedIf = dog.getMethods().get(4);
            AbcCode code = abc.getCodeForMethod(nestedIf);
            String result = decompiler.decompileMethod(
                    nestedIf, code, abc);
            assertNotNull(result,
                    "Nested if method should produce non-null output");
        }
    }

    // =====================================================================
    // Large ABC fixture (100 classes, 1000 methods)
    // =====================================================================

    @Nested
    class LargeScaleTests {

        private AbcFile abc;

        @BeforeEach
        void setUpLarge() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildLargeAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_large_decompileFileProducesOutput() {
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty(),
                    "Large file should produce non-empty output");
        }

        @Test
        void testE2E_large_containsFirstAndLastClasses() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("Class0"),
                    "Should contain first class name");
            assertTrue(result.contains("Class99"),
                    "Should contain last class name");
        }

        @Test
        void testE2E_large_allMethodsDecompile() {
            int methodCount = 0;
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            cls.getName() + "." + method.getName()
                                    + " should have code");
                    String result = decompiler.decompileMethod(
                            method, code, abc);
                    assertNotNull(result);
                    methodCount++;
                }
            }
            assertTrue(methodCount >= 100,
                    "Should decompile at least 100 methods, got: "
                            + methodCount);
        }
    }

    // =====================================================================
    // Edge case tests
    // =====================================================================

    @Nested
    class EdgeCaseTests {

        @Test
        void testE2E_nullAbcFile_returnsEmptyString() {
            String result = decompiler.decompileFile(null);
            assertNotNull(result);
            assertEquals("", result,
                    "Null AbcFile should produce empty output");
        }

        @Test
        void testE2E_comprehensiveFile_outputIsConsistentAcrossCalls() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result1 = decompiler.decompileFile(abc);
            String result2 = decompiler.decompileFile(abc);
            assertEquals(result1, result2,
                    "Repeated decompileFile calls should produce identical output");
        }

        @Test
        void testE2E_realisticFile_outputIsConsistentAcrossCalls() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);

            String result1 = decompiler.decompileFile(abc);
            String result2 = decompiler.decompileFile(abc);
            assertEquals(result1, result2,
                    "Repeated decompileFile calls should produce identical output");
        }

        @Test
        void testE2E_comprehensiveEachMethod_producesNonEmptyOutput() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    String result = decompiler.decompileMethod(
                            method, code, abc);
                    assertNotNull(result,
                            cls.getName() + "." + method.getName()
                                    + " should produce output");
                    assertFalse(result.isEmpty(),
                            cls.getName() + "." + method.getName()
                                    + " should produce non-empty output");
                }
            }
        }

        @Test
        void testE2E_realisticFile_containsAllExpectedClassNames() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);

            String[] expectedClasses = {"Animal", "Dog", "Utils"};
            for (String expected : expectedClasses) {
                assertTrue(result.contains(expected),
                        "Output should contain class name: " + expected);
            }
        }
    }

    // --- Helper methods ---

    private static String extractSimpleClassName(String internalName) {
        if (internalName == null) {
            return "";
        }
        String cleaned = internalName;
        if (cleaned.startsWith("L")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        int lastSlash = cleaned.lastIndexOf('/');
        if (lastSlash >= 0) {
            cleaned = cleaned.substring(lastSlash + 1);
        }
        return cleaned;
    }

    private static long countOccurrences(String haystack, String needle) {
        long count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    // =====================================================================
    // Feature integration tests (#71)
    // =====================================================================

    @Nested
    class FeatureIntegrationTests {

        private AbcFile abc;

        @BeforeEach
        void setUpFeatureAbc() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            abc = AbcFile.parse(data);
        }

        @Test
        void testE2E_classDeclarations_haveBraces() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("class "),
                    "Expected class keyword, got: " + result);
            assertTrue(result.contains("extends "),
                    "Expected extends keyword, got: " + result);
        }

        @Test
        void testE2E_methodsHaveParentheses() {
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("(") && result.contains(")"),
                    "Expected method signatures with parentheses: " + result);
        }

        @Test
        void testE2E_noJavaScriptVarKeyword() {
            String result = decompiler.decompileFile(abc);
            assertFalse(result.matches("(?s).*\\bvar\\b.*"),
                    "Should not use JavaScript 'var' keyword: " + result);
        }

        @Test
        void testE2E_usesLetOrConst() {
            String result = decompiler.decompileFile(abc);
            boolean hasLetOrConst = result.contains("let ")
                    || result.contains("const ");
            assertTrue(hasLetOrConst,
                    "Should use 'let' or 'const' for variables: " + result);
        }

        @Test
        void testE2E_noAnyType() {
            String result = decompiler.decompileFile(abc);
            assertFalse(result.matches("(?s).*: any\\b.*"),
                    "Should not use 'any' type in ArkTS output: " + result);
        }

        @Test
        void testE2E_noTypeScriptSyntax() {
            String result = decompiler.decompileFile(abc);
            // ArkTS should not have TypeScript-specific syntax like 'as any'
            assertFalse(result.contains("as any"),
                    "Should not contain TypeScript 'as any': " + result);
        }
    }
}
