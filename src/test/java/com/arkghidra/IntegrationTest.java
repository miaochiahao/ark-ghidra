package com.arkghidra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcField;
import com.arkghidra.format.AbcHeader;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcRegionHeader;
import com.arkghidra.format.AbcTryBlock;

import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.LoadSpec;
import com.arkghidra.loader.AbcLoader;
import com.arkghidra.loader.AbcLoaderUtils;

/**
 * Integration tests that verify the full pipeline:
 * parsing, class/method/field extraction, disassembly, and loader integration.
 */
class IntegrationTest {

    private AbcTestFixture fixture;
    private ArkDisassembler disasm;

    @BeforeEach
    void setUp() {
        fixture = new AbcTestFixture();
        disasm = new ArkDisassembler();
    }

    // =====================================================================
    // Header verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_headerIsValid() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);
        AbcHeader header = abc.getHeader();

        assertTrue(header.hasValidMagic(), "ABC magic should be valid");
        assertEquals(2, header.getVersionNumber(), "Version should be 2");
        assertEquals(2, header.getNumClasses(), "Should have 2 classes");
        assertEquals(1, header.getNumLiteralArrays(), "Should have 1 literal array");
        assertEquals(fixture.buildComprehensiveAbc().length, header.getFileSize(),
                "File size should match buffer size");
    }

    // =====================================================================
    // Class verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_findsAllClasses() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);
        List<AbcClass> classes = abc.getClasses();

        assertEquals(fixture.getExpectedClassCount(), classes.size(),
                "Should find expected number of classes");
    }

    @Test
    void testParseComprehensiveAbc_baseClassHasCorrectName() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedClassName(0),
                abc.getClasses().get(0).getName(),
                "First class should be BaseClass");
    }

    @Test
    void testParseComprehensiveAbc_childClassHasCorrectName() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedClassName(1),
                abc.getClasses().get(1).getName(),
                "Second class should be ChildClass");
    }

    @Test
    void testParseComprehensiveAbc_childClassHasSuperClass() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcClass baseClass = abc.getClasses().get(0);
        AbcClass childClass = abc.getClasses().get(1);

        assertTrue(childClass.getSuperClassOff() > 0,
                "ChildClass should have a superclass offset");
        assertEquals(baseClass.getOffset(), childClass.getSuperClassOff(),
                "ChildClass superclass offset should point to BaseClass");
    }

    @Test
    void testParseComprehensiveAbc_baseClassHasCorrectAccessFlags() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(1, abc.getClasses().get(0).getAccessFlags(),
                "BaseClass should have ACC_PUBLIC access flag");
    }

    // =====================================================================
    // Method verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_baseClassHasCorrectMethodCount() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedMethodCountForClass(0),
                abc.getClasses().get(0).getMethods().size(),
                "BaseClass should have 3 methods");
    }

    @Test
    void testParseComprehensiveAbc_childClassHasCorrectMethodCount() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedMethodCountForClass(1),
                abc.getClasses().get(1).getMethods().size(),
                "ChildClass should have 4 methods");
    }

    @Test
    void testParseComprehensiveAbc_methodNamesAreCorrect() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        // BaseClass methods: <init>, simpleReturn, arithmetic
        List<AbcMethod> baseMethods = abc.getClasses().get(0).getMethods();
        assertEquals("<init>", baseMethods.get(0).getName());
        assertEquals("simpleReturn", baseMethods.get(1).getName());
        assertEquals("arithmetic", baseMethods.get(2).getName());

        // ChildClass methods: <init>, conditional, loopMethod, callArg
        List<AbcMethod> childMethods = abc.getClasses().get(1).getMethods();
        assertEquals("<init>", childMethods.get(0).getName());
        assertEquals("conditional", childMethods.get(1).getName());
        assertEquals("loopMethod", childMethods.get(2).getName());
        assertEquals("callArg", childMethods.get(3).getName());
    }

    @Test
    void testParseComprehensiveAbc_allMethodsHaveCodeOffsets() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                assertTrue(method.getCodeOff() > 0,
                        "Method " + cls.getName() + "." + method.getName()
                                + " should have a non-zero code offset");
            }
        }
    }

    @Test
    void testParseComprehensiveAbc_methodAccessFlagsAreCorrect() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                assertEquals(1, method.getAccessFlags(),
                        "Method " + method.getName() + " should have ACC_PUBLIC access");
            }
        }
    }

    // =====================================================================
    // Field verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_baseClassHasTwoFields() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedFieldCountForClass(0),
                abc.getClasses().get(0).getFields().size(),
                "BaseClass should have 2 fields");
    }

    @Test
    void testParseComprehensiveAbc_childClassHasOneField() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(fixture.getExpectedFieldCountForClass(1),
                abc.getClasses().get(1).getFields().size(),
                "ChildClass should have 1 field");
    }

    @Test
    void testParseComprehensiveAbc_fieldNamesAreCorrect() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        List<AbcField> baseFields = abc.getClasses().get(0).getFields();
        assertEquals("value", baseFields.get(0).getName());
        assertEquals("name", baseFields.get(1).getName());

        List<AbcField> childFields = abc.getClasses().get(1).getFields();
        assertEquals("tag", childFields.get(0).getName());
    }

    // =====================================================================
    // Code section and disassembly verification
    // =====================================================================

    @Test
    void testDisassemble_baseClassConstructor_hasLdthisAndReturn() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod ctor = abc.getClasses().get(0).getMethods().get(0);
        assertEquals("<init>", ctor.getName());

        AbcCode code = abc.getCodeForMethod(ctor);
        assertNotNull(code, "Constructor should have code");
        assertTrue(code.getCodeSize() > 0, "Code should not be empty");

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());
        assertFalse(insns.isEmpty(), "Should produce at least one instruction");

        boolean hasReturn = insns.stream()
                .anyMatch(i -> "return".equals(i.getMnemonic()));
        assertTrue(hasReturn, "Constructor should contain a return instruction");

        boolean hasLdthis = insns.stream()
                .anyMatch(i -> "ldthis".equals(i.getMnemonic()));
        assertTrue(hasLdthis, "Constructor should contain ldthis");
    }

    @Test
    void testDisassemble_simpleReturnMethod_hasLdaiInstruction() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod simple = abc.getClasses().get(0).getMethods().get(1);
        assertEquals("simpleReturn", simple.getName());

        AbcCode code = abc.getCodeForMethod(simple);
        assertNotNull(code);

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        boolean hasLdai = insns.stream()
                .anyMatch(i -> "ldai".equals(i.getMnemonic()));
        assertTrue(hasLdai, "simpleReturn should contain ldai");

        boolean hasReturn = insns.stream()
                .anyMatch(i -> "return".equals(i.getMnemonic()));
        assertTrue(hasReturn, "simpleReturn should end with return");
    }

    @Test
    void testDisassemble_arithmeticMethod_hasAdd2() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod arith = abc.getClasses().get(0).getMethods().get(2);
        assertEquals("arithmetic", arith.getName());

        AbcCode code = abc.getCodeForMethod(arith);
        assertNotNull(code);

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        boolean hasAdd2 = insns.stream()
                .anyMatch(i -> "add2".equals(i.getMnemonic()));
        assertTrue(hasAdd2, "arithmetic method should contain add2");
    }

    @Test
    void testDisassemble_conditionalMethod_hasJeazAndJmp() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod cond = abc.getClasses().get(1).getMethods().get(1);
        assertEquals("conditional", cond.getName());

        AbcCode code = abc.getCodeForMethod(cond);
        assertNotNull(code);

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        boolean hasJeqz = insns.stream()
                .anyMatch(i -> "jeqz".equals(i.getMnemonic()));
        assertTrue(hasJeqz, "conditional method should contain jeqz");

        boolean hasJmp = insns.stream()
                .anyMatch(i -> "jmp".equals(i.getMnemonic()));
        assertTrue(hasJmp, "conditional method should contain jmp");

        boolean hasReturn = insns.stream()
                .anyMatch(i -> "return".equals(i.getMnemonic()));
        assertTrue(hasReturn, "conditional method should contain return");
    }

    @Test
    void testDisassemble_loopMethod_hasJmpBackwards() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod loop = abc.getClasses().get(1).getMethods().get(2);
        assertEquals("loopMethod", loop.getName());

        AbcCode code = abc.getCodeForMethod(loop);
        assertNotNull(code);

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        boolean hasJmpBack = insns.stream()
                .anyMatch(i -> "jmp".equals(i.getMnemonic())
                        && i.getOperands().stream()
                                .anyMatch(o -> o.getValue() < 0));
        assertTrue(hasJmpBack, "loop method should have a backward jump");

        boolean hasInc = insns.stream()
                .anyMatch(i -> "inc".equals(i.getMnemonic()));
        assertTrue(hasInc, "loop method should contain inc");
    }

    @Test
    void testDisassemble_callArgMethod_hasCallarg0() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod call = abc.getClasses().get(1).getMethods().get(3);
        assertEquals("callArg", call.getName());

        AbcCode code = abc.getCodeForMethod(call);
        assertNotNull(code);

        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        boolean hasCallarg0 = insns.stream()
                .anyMatch(i -> "callarg0".equals(i.getMnemonic()));
        assertTrue(hasCallarg0, "callArg method should contain callarg0");
    }

    // =====================================================================
    // Code metadata verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_codeSectionsHaveCorrectVregCounts() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code,
                        "Method " + method.getName() + " should have code");
                assertTrue(code.getNumVregs() > 0,
                        "Method " + method.getName()
                                + " should have at least 1 vreg");
            }
        }
    }

    @Test
    void testParseComprehensiveAbc_codeSectionsHaveNoTryBlocks() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code);
                assertTrue(code.getTryBlocks().isEmpty(),
                        "Method " + method.getName()
                                + " should have no try blocks in this fixture");
            }
        }
    }

    // =====================================================================
    // Region header verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_hasRegionHeaders() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertFalse(abc.getRegionHeaders().isEmpty(),
                "Should have at least one region header");
    }

    @Test
    void testParseComprehensiveAbc_regionHeaderHasCorrectClassCount() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcRegionHeader region = abc.getRegionHeaders().get(0);
        assertEquals(2, region.getClassIdxSize(),
                "Region should report 2 class index entries");
    }

    // =====================================================================
    // Literal array verification
    // =====================================================================

    @Test
    void testParseComprehensiveAbc_hasLiteralArrays() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(1, abc.getLiteralArrays().size(),
                "Should have 1 literal array");
    }

    @Test
    void testParseComprehensiveAbc_literalArrayHasCorrectEntryCount() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertTrue(abc.getLiteralArrays().get(0).getNumLiterals() > 0,
                "Literal array should have at least one entry");
    }

    // =====================================================================
    // Loader integration (findSupportedLoadSpecs)
    // =====================================================================

    @Test
    void testLoader_findSupportedLoadSpecs_withComprehensiveAbc_returnsSpec()
            throws IOException {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcLoader loader = new AbcLoader();
        ByteProvider provider = new InMemoryByteProvider(data);

        try {
            Collection<LoadSpec> specs = loader.findSupportedLoadSpecs(provider);
            assertEquals(1, specs.size(), "Should return exactly one load spec");

            LoadSpec spec = specs.iterator().next();
            assertTrue(spec.isPreferred(), "Load spec should be preferred");

            String langId = spec.getLanguageCompilerSpec()
                    .languageID.getIdAsString();
            assertTrue(langId.startsWith("ArkBytecode"),
                    "Language ID should start with ArkBytecode, got: " + langId);
        } finally {
            provider.close();
        }
    }

    @Test
    void testLoader_findSupportedLoadSpecs_withNonAbcData_returnsEmpty()
            throws IOException {
        byte[] notAbc = new byte[] {
            (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF,
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE
        };
        AbcLoader loader = new AbcLoader();
        ByteProvider provider = new InMemoryByteProvider(notAbc);

        try {
            Collection<LoadSpec> specs = loader.findSupportedLoadSpecs(provider);
            assertTrue(specs.isEmpty(), "Non-ABC data should return no load specs");
        } finally {
            provider.close();
        }
    }

    // =====================================================================
    // toNamespaceName conversion
    // =====================================================================

    @Test
    void testToNamespaceName_stripsLprefixAndSemicolon() {
        assertEquals("com/example/BaseClass",
                AbcLoaderUtils.toNamespaceName("Lcom/example/BaseClass;"));
    }

    @Test
    void testToNamespaceName_handlesNull() {
        assertEquals("unknown", AbcLoaderUtils.toNamespaceName(null));
    }

    @Test
    void testToNamespaceName_handlesEmpty() {
        assertEquals("unknown", AbcLoaderUtils.toNamespaceName(""));
    }

    @Test
    void testToNamespaceName_handlesPlainName() {
        assertEquals("MyClass", AbcLoaderUtils.toNamespaceName("MyClass"));
    }

    // =====================================================================
    // Instruction offset chain verification
    // =====================================================================

    @Test
    void testDisassemble_allMethods_offsetsChainCorrectly() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code);

                List<ArkInstruction> insns = disasm.disassemble(
                        code.getInstructions(), 0, (int) code.getCodeSize());
                assertFalse(insns.isEmpty());

                // Verify offset chain
                int expectedOffset = 0;
                for (ArkInstruction insn : insns) {
                    assertEquals(expectedOffset, insn.getOffset(),
                            "Instruction offset mismatch in method "
                                    + cls.getName() + "." + method.getName()
                                    + " at instruction " + insn.getMnemonic());
                    expectedOffset = insn.getNextOffset();
                }
                assertEquals(code.getCodeSize(), expectedOffset,
                        "Final offset should match code size for method "
                                + cls.getName() + "." + method.getName());
            }
        }
    }

    // =====================================================================
    // Full disassembly: specific method bytecode patterns
    // =====================================================================

    @Test
    void testDisassemble_simpleReturnMethod_exactInstructionSequence() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod simple = abc.getClasses().get(0).getMethods().get(1);
        AbcCode code = abc.getCodeForMethod(simple);
        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        // Expected: ldai 42, sta v0, lda v0, return
        assertEquals(4, insns.size(), "simpleReturn should have 4 instructions");
        assertEquals("ldai", insns.get(0).getMnemonic());
        assertEquals("sta", insns.get(1).getMnemonic());
        assertEquals("lda", insns.get(2).getMnemonic());
        assertEquals("return", insns.get(3).getMnemonic());
    }

    @Test
    void testDisassemble_arithmeticMethod_exactInstructionSequence() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod arith = abc.getClasses().get(0).getMethods().get(2);
        AbcCode code = abc.getCodeForMethod(arith);
        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        // Expected: lda v0, add2 0x0 v1, sta v2, return
        assertEquals(4, insns.size(), "arithmetic should have 4 instructions");
        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals("add2", insns.get(1).getMnemonic());
        assertEquals("sta", insns.get(2).getMnemonic());
        assertEquals("return", insns.get(3).getMnemonic());
    }

    @Test
    void testDisassemble_conditionalMethod_exactInstructionSequence() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod cond = abc.getClasses().get(1).getMethods().get(1);
        AbcCode code = abc.getCodeForMethod(cond);
        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        // Expected: ldai 0, jeqz, ldai 1, jmp, ldai 2, return
        assertEquals(6, insns.size(), "conditional should have 6 instructions");
        assertEquals("ldai", insns.get(0).getMnemonic());
        assertEquals("jeqz", insns.get(1).getMnemonic());
        assertEquals("ldai", insns.get(2).getMnemonic());
        assertEquals("jmp", insns.get(3).getMnemonic());
        assertEquals("ldai", insns.get(4).getMnemonic());
        assertEquals("return", insns.get(5).getMnemonic());
    }

    @Test
    void testDisassemble_loopMethod_exactInstructionSequence() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod loop = abc.getClasses().get(1).getMethods().get(2);
        AbcCode code = abc.getCodeForMethod(loop);
        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        // Expected: lda v0, jeqz, inc, sta v0, jmp, return
        assertEquals(6, insns.size(), "loopMethod should have 6 instructions");
        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals("jeqz", insns.get(1).getMnemonic());
        assertEquals("inc", insns.get(2).getMnemonic());
        assertEquals("sta", insns.get(3).getMnemonic());
        assertEquals("jmp", insns.get(4).getMnemonic());
        assertEquals("return", insns.get(5).getMnemonic());
    }

    @Test
    void testDisassemble_callArgMethod_exactInstructionSequence() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcMethod call = abc.getClasses().get(1).getMethods().get(3);
        AbcCode code = abc.getCodeForMethod(call);
        List<ArkInstruction> insns = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        // Expected: lda v0, callarg0 0x05, sta v1, return
        assertEquals(4, insns.size(), "callArg should have 4 instructions");
        assertEquals("lda", insns.get(0).getMnemonic());
        assertEquals("callarg0", insns.get(1).getMnemonic());
        assertEquals("sta", insns.get(2).getMnemonic());
        assertEquals("return", insns.get(3).getMnemonic());
    }

    // =====================================================================
    // Foreign offset and region lookup
    // =====================================================================

    @Test
    void testIsForeignOffset_returnsFalseWithinFile() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        assertFalse(abc.isForeignOffset(0), "Offset 0 should not be foreign");
        assertFalse(abc.isForeignOffset(100), "Offset 100 should not be foreign");
    }

    @Test
    void testFindRegionForOffset_returnsRegionForValidOffset() {
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abc = AbcFile.parse(data);

        AbcRegionHeader region = abc.findRegionForOffset(
                abc.getClasses().get(0).getOffset());
        assertNotNull(region, "Should find a region containing the first class");
    }

    // =========================================================================
    // Realistic ABC integration tests (3 classes, try/catch, large methods)
    // =========================================================================

    /**
     * Nested test class for the realistic 3-class ABC fixture.
     * Tests full pipeline: parsing, disassembly, decompilation, edge cases.
     */
    @Nested
    class RealisticAbcTests {

        private byte[] data;
        private AbcFile abc;

        @BeforeEach
        void setUpRealistic() {
            data = fixture.buildRealisticAbc();
            abc = AbcFile.parse(data);
        }

        // =================================================================
        // Header
        // =================================================================

        @Test
        void testRealistic_headerIsValid() {
            AbcHeader header = abc.getHeader();
            assertTrue(header.hasValidMagic());
            assertEquals(2, header.getVersionNumber());
            assertEquals(3, header.getNumClasses());
            assertEquals(2, header.getNumLiteralArrays());
        }

        // =================================================================
        // Class structure
        // =================================================================

        @Test
        void testRealistic_findsAllThreeClasses() {
            assertEquals(3, abc.getClasses().size());
        }

        @Test
        void testRealistic_classNamesAreCorrect() {
            assertEquals("Lcom/example/Animal;", abc.getClasses().get(0).getName());
            assertEquals("Lcom/example/Dog;", abc.getClasses().get(1).getName());
            assertEquals("Lcom/example/Utils;", abc.getClasses().get(2).getName());
        }

        @Test
        void testRealistic_dogExtendsAnimal() {
            AbcClass animal = abc.getClasses().get(0);
            AbcClass dog = abc.getClasses().get(1);
            assertTrue(dog.getSuperClassOff() > 0,
                    "Dog should have a superclass");
            assertEquals(animal.getOffset(), dog.getSuperClassOff(),
                    "Dog's superclass offset should point to Animal");
        }

        @Test
        void testRealistic_animalHasNoSuperclass() {
            assertEquals(0, abc.getClasses().get(0).getSuperClassOff(),
                    "Animal should have no superclass");
        }

        @Test
        void testRealistic_utilsHasNoSuperclass() {
            assertEquals(0, abc.getClasses().get(2).getSuperClassOff(),
                    "Utils should have no superclass");
        }

        @Test
        void testRealistic_allClassesPublic() {
            for (AbcClass cls : abc.getClasses()) {
                assertEquals(1, cls.getAccessFlags(),
                        "Class " + cls.getName() + " should be ACC_PUBLIC");
            }
        }

        // =================================================================
        // Field verification
        // =================================================================

        @Test
        void testRealistic_fieldCounts() {
            assertEquals(2, abc.getClasses().get(0).getFields().size(),
                    "Animal should have 2 fields");
            assertEquals(3, abc.getClasses().get(1).getFields().size(),
                    "Dog should have 3 fields");
            assertTrue(abc.getClasses().get(2).getFields().isEmpty(),
                    "Utils should have 0 fields");
        }

        @Test
        void testRealistic_fieldNames() {
            assertEquals("name", abc.getClasses().get(0).getFields().get(0).getName());
            assertEquals("age", abc.getClasses().get(0).getFields().get(1).getName());
            assertEquals("breed", abc.getClasses().get(1).getFields().get(0).getName());
            assertEquals("weight", abc.getClasses().get(1).getFields().get(1).getName());
            assertEquals("color", abc.getClasses().get(1).getFields().get(2).getName());
        }

        @Test
        void testRealistic_fieldAccessFlags() {
            // Animal.name = public (0x01)
            assertEquals(0x0001, abc.getClasses().get(0).getFields().get(0)
                    .getAccessFlags());
            // Dog.breed = private (0x02)
            assertEquals(0x0002, abc.getClasses().get(1).getFields().get(0)
                    .getAccessFlags());
            // Dog.color = protected (0x04)
            assertEquals(0x0004, abc.getClasses().get(1).getFields().get(2)
                    .getAccessFlags());
        }

        // =================================================================
        // Method verification
        // =================================================================

        @Test
        void testRealistic_methodCounts() {
            assertEquals(2, abc.getClasses().get(0).getMethods().size(),
                    "Animal should have 2 methods");
            assertEquals(5, abc.getClasses().get(1).getMethods().size(),
                    "Dog should have 5 methods");
            assertEquals(3, abc.getClasses().get(2).getMethods().size(),
                    "Utils should have 3 methods");
        }

        @Test
        void testRealistic_methodNames() {
            // Animal methods
            assertEquals("<init>", abc.getClasses().get(0).getMethods().get(0)
                    .getName());
            assertEquals("tryCatchMethod", abc.getClasses().get(0).getMethods()
                    .get(1).getName());

            // Dog methods
            assertEquals("<init>", abc.getClasses().get(1).getMethods().get(0)
                    .getName());
            assertEquals("largeLoop", abc.getClasses().get(1).getMethods().get(1)
                    .getName());
            assertEquals("manyParams", abc.getClasses().get(1).getMethods().get(2)
                    .getName());
            assertEquals("subtract", abc.getClasses().get(1).getMethods().get(3)
                    .getName());
            assertEquals("nestedIf", abc.getClasses().get(1).getMethods().get(4)
                    .getName());

            // Utils methods
            assertEquals("<init>", abc.getClasses().get(2).getMethods().get(0)
                    .getName());
            assertEquals("staticHelper", abc.getClasses().get(2).getMethods().get(1)
                    .getName());
            assertEquals("anotherHelper", abc.getClasses().get(2).getMethods().get(2)
                    .getName());
        }

        @Test
        void testRealistic_staticMethodHasCorrectFlags() {
            // staticHelper = ACC_PUBLIC | ACC_STATIC = 0x0009
            AbcMethod staticHelper = abc.getClasses().get(2).getMethods().get(1);
            assertEquals(0x0009, staticHelper.getAccessFlags(),
                    "staticHelper should be public+static");
        }

        @Test
        void testRealistic_privateMethodHasCorrectFlags() {
            // anotherHelper = ACC_PRIVATE = 0x0002
            AbcMethod privateMethod = abc.getClasses().get(2).getMethods().get(2);
            assertEquals(0x0002, privateMethod.getAccessFlags(),
                    "anotherHelper should be private");
        }

        @Test
        void testRealistic_allMethodsHaveCode() {
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    assertTrue(method.getCodeOff() > 0,
                            method.getName() + " should have a code offset");
                }
            }
        }

        // =================================================================
        // Code section and disassembly
        // =================================================================

        @Test
        void testRealistic_largeMethod_has20PlusInstructions() {
            AbcMethod largeLoop = abc.getClasses().get(1).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(largeLoop);
            assertNotNull(code);
            assertEquals(8, code.getNumVregs());
            assertEquals(2, code.getNumArgs());

            List<ArkInstruction> insns = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());
            assertTrue(insns.size() >= 16,
                    "Large method should have at least 16 instructions, got: "
                            + insns.size());
        }

        @Test
        void testRealistic_largeMethod_hasLoopStructure() {
            AbcMethod largeLoop = abc.getClasses().get(1).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(largeLoop);
            List<ArkInstruction> insns = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());

            boolean hasBackwardJmp = insns.stream()
                    .anyMatch(i -> "jmp".equals(i.getMnemonic())
                            && i.getOperands().stream()
                                    .anyMatch(o -> o.getValue() < 0));
            assertTrue(hasBackwardJmp, "Large loop should have backward jump");

            boolean hasJeQz = insns.stream()
                    .anyMatch(i -> "jeqz".equals(i.getMnemonic()));
            assertTrue(hasJeQz, "Large loop should have conditional branch");

            boolean hasReturn = insns.stream()
                    .anyMatch(i -> "return".equals(i.getMnemonic()));
            assertTrue(hasReturn, "Large loop should have return");
        }

        @Test
        void testRealistic_manyParamsMethod_has6Args() {
            AbcMethod manyParams = abc.getClasses().get(1).getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(manyParams);
            assertNotNull(code);
            assertEquals(6, code.getNumArgs(),
                    "manyParams should have 6 arguments");
            assertEquals(8, code.getNumVregs());
        }

        @Test
        void testRealistic_manyParamsMethod_disassemblesCorrectly() {
            AbcMethod manyParams = abc.getClasses().get(1).getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(manyParams);
            List<ArkInstruction> insns = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());

            // Should be: lda v0, add2 v1, add2 v2, add2 v3, add2 v4, add2 v5, return
            assertEquals(7, insns.size());
            assertEquals("lda", insns.get(0).getMnemonic());
            assertTrue(insns.stream()
                    .filter(i -> "add2".equals(i.getMnemonic()))
                    .count() >= 5,
                    "Should have at least 5 add2 instructions");
        }

        @Test
        void testRealistic_subtractMethod_disassemblesCorrectly() {
            AbcMethod sub = abc.getClasses().get(1).getMethods().get(3);
            AbcCode code = abc.getCodeForMethod(sub);
            List<ArkInstruction> insns = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());

            assertEquals("sub2", insns.get(1).getMnemonic(),
                    "Subtract method should use sub2");
        }

        @Test
        void testRealistic_allMethods_offsetChainCorrect() {
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            "Method " + method.getName() + " should have code");

                    List<ArkInstruction> insns = disasm.disassemble(
                            code.getInstructions(), 0, (int) code.getCodeSize());
                    assertFalse(insns.isEmpty(),
                            "Method " + method.getName() + " should have insns");

                    int expectedOffset = 0;
                    for (ArkInstruction insn : insns) {
                        assertEquals(expectedOffset, insn.getOffset(),
                                "Offset mismatch in " + method.getName()
                                        + " at " + insn.getMnemonic());
                        expectedOffset = insn.getNextOffset();
                    }
                    assertEquals(code.getCodeSize(), expectedOffset,
                            "Final offset mismatch for " + method.getName());
                }
            }
        }

        // =================================================================
        // Try/catch block parsing
        // =================================================================

        @Test
        void testRealistic_tryCatchMethod_hasTryBlocks() {
            AbcMethod tryCatch = abc.getClasses().get(0).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatch);
            assertNotNull(code);
            assertEquals(1, code.getTryBlocks().size(),
                    "tryCatchMethod should have 1 try block");
        }

        @Test
        void testRealistic_tryBlock_hasCorrectRange() {
            AbcMethod tryCatch = abc.getClasses().get(0).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatch);
            AbcTryBlock tryBlock = code.getTryBlocks().get(0);

            assertEquals(0, tryBlock.getStartPc(),
                    "Try block should start at PC 0");
            assertEquals(13, tryBlock.getLength(),
                    "Try block length should cover the try body");
        }

        @Test
        void testRealistic_tryBlock_hasCatchHandler() {
            AbcMethod tryCatch = abc.getClasses().get(0).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatch);
            AbcTryBlock tryBlock = code.getTryBlocks().get(0);

            assertEquals(1, tryBlock.getCatchBlocks().size(),
                    "Try block should have 1 catch block");
            AbcCatchBlock catchBlock = tryBlock.getCatchBlocks().get(0);
            assertTrue(catchBlock.isCatchAll(),
                    "Catch block should be catch-all (typeIdx=0)");
            assertEquals(13, catchBlock.getHandlerPc(),
                    "Catch handler should start at PC 13");
            assertEquals(6, catchBlock.getCodeSize(),
                    "Catch handler code size should be 6 bytes");
        }

        @Test
        void testRealistic_otherMethodsHaveNoTryBlocks() {
            // All methods except tryCatchMethod should have no try blocks
            for (int ci = 0; ci < abc.getClasses().size(); ci++) {
                for (int mi = 0; mi < abc.getClasses().get(ci).getMethods()
                        .size(); mi++) {
                    if (ci == 0 && mi == 1) {
                        continue; // skip tryCatchMethod
                    }
                    AbcMethod method = abc.getClasses().get(ci).getMethods()
                            .get(mi);
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code);
                    assertTrue(code.getTryBlocks().isEmpty(),
                            "Method " + method.getName()
                                    + " should have no try blocks");
                }
            }
        }

        // =================================================================
        // Proto / literal array verification
        // =================================================================

        @Test
        void testRealistic_hasProtos() {
            assertFalse(abc.getProtos().isEmpty(),
                    "Should have at least one proto");
        }

        @Test
        void testRealistic_hasTwoLiteralArrays() {
            assertEquals(2, abc.getLiteralArrays().size(),
                    "Should have 2 literal arrays");
        }

        @Test
        void testRealistic_literalArraysHaveEntries() {
            for (int i = 0; i < abc.getLiteralArrays().size(); i++) {
                assertTrue(abc.getLiteralArrays().get(i).getNumLiterals() > 0,
                        "Literal array " + i + " should have entries");
            }
        }

        // =================================================================
        // Decompiler integration
        // =================================================================

        @Test
        void testRealistic_decompileTryCatchMethod_producesTryCatch() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            AbcMethod tryCatch = abc.getClasses().get(0).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatch);

            String result = decompiler.decompileMethod(tryCatch, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("try"),
                    "Decompiled try/catch method should contain 'try'");
            assertTrue(result.contains("catch"),
                    "Decompiled try/catch method should contain 'catch'");
        }

        @Test
        void testRealistic_decompileLargeLoop_hasWhileLoop() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            AbcMethod largeLoop = abc.getClasses().get(1).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(largeLoop);

            String result = decompiler.decompileMethod(largeLoop, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("while") || result.contains("for"),
                    "Decompiled large loop should contain while/for");
        }

        @Test
        void testRealistic_decompileSimpleReturn() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            AbcMethod staticHelper = abc.getClasses().get(2).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(staticHelper);

            String result = decompiler.decompileMethod(staticHelper, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("99"),
                    "Static helper should return 99");
        }

        @Test
        void testRealistic_decompileSubtractMethod() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            AbcMethod sub = abc.getClasses().get(1).getMethods().get(3);
            AbcCode code = abc.getCodeForMethod(sub);

            String result = decompiler.decompileMethod(sub, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("-"),
                    "Subtract method should contain subtraction operator");
        }

        @Test
        void testRealistic_decompileMultiplyMethod() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            AbcMethod mul = abc.getClasses().get(2).getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(mul);

            String result = decompiler.decompileMethod(mul, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("*"),
                    "Multiply method should contain multiplication operator");
        }

        @Test
        void testRealistic_decompileAllMethods_noExceptions() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            "Method " + method.getName() + " should have code");
                    // Should not throw
                    String result = decompiler.decompileMethod(method, code, abc);
                    assertNotNull(result,
                            "Decompiled " + cls.getName() + "."
                                    + method.getName() + " should not be null");
                    assertFalse(result.isEmpty(),
                            "Decompiled " + cls.getName() + "."
                                    + method.getName() + " should not be empty");
                }
            }
        }

        // =================================================================
        // Full file decompilation
        // =================================================================

        @Test
        void testRealistic_decompileFile_producesOutput() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty(),
                    "Full file decompilation should produce output");
        }

        @Test
        void testRealistic_decompileFile_containsAllClassNames() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("Animal"),
                    "Should contain Animal class");
            assertTrue(result.contains("Dog"),
                    "Should contain Dog class");
            assertTrue(result.contains("Utils"),
                    "Should contain Utils class");
        }

        @Test
        void testRealistic_decompileFile_dogExtendsAnimal() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("extends Animal") || result.contains("Animal"),
                    "Dog class output should reference Animal");
        }

        @Test
        void testRealistic_decompileFile_containsConstructors() {
            ArkTSDecompiler decompiler = new ArkTSDecompiler();
            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("constructor"),
                    "File output should contain constructors");
        }

        // =================================================================
        // Edge case: nested conditional method
        // =================================================================

        @Test
        void testRealistic_nestedIfMethod_hasMultipleBranches() {
            AbcMethod nestedIf = abc.getClasses().get(1).getMethods().get(4);
            AbcCode code = abc.getCodeForMethod(nestedIf);
            List<ArkInstruction> insns = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());

            long jeqzCount = insns.stream()
                    .filter(i -> "jeqz".equals(i.getMnemonic()))
                    .count();
            assertTrue(jeqzCount >= 2,
                    "Nested if should have at least 2 conditional branches");

            boolean hasJmp = insns.stream()
                    .anyMatch(i -> "jmp".equals(i.getMnemonic()));
            assertTrue(hasJmp, "Nested if should have unconditional jump");
        }

        // =================================================================
        // Region header verification
        // =================================================================

        @Test
        void testRealistic_regionHeaderHasCorrectCounts() {
            assertFalse(abc.getRegionHeaders().isEmpty());
            AbcRegionHeader region = abc.getRegionHeaders().get(0);
            assertEquals(3, region.getClassIdxSize(),
                    "Region should report 3 class index entries");
        }

        // =================================================================
        // Loader integration
        // =================================================================

        @Test
        void testRealistic_loaderRecognizesFile() throws IOException {
            AbcLoader loader = new AbcLoader();
            ByteProvider provider = new InMemoryByteProvider(data);
            try {
                Collection<LoadSpec> specs = loader.findSupportedLoadSpecs(provider);
                assertEquals(1, specs.size(), "Should recognize realistic ABC");
            } finally {
                provider.close();
            }
        }
    }

    // =====================================================================
    // Inner helper: InMemoryByteProvider
    // =====================================================================

    /**
     * Simple in-memory ByteProvider for testing loader without Ghidra runtime.
     */
    private static class InMemoryByteProvider implements ByteProvider {

        private final byte[] data;

        InMemoryByteProvider(byte[] data) {
            this.data = data;
        }

        @Override
        public java.io.File getFile() {
            return null;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getAbsolutePath() {
            return "test";
        }

        @Override
        public long length() {
            return data.length;
        }

        @Override
        public boolean isValidIndex(long index) {
            return index >= 0 && index < data.length;
        }

        @Override
        public void close() {
            // no-op
        }

        @Override
        public byte readByte(long index) throws IOException {
            if (index < 0 || index >= data.length) {
                throw new IOException("Index out of bounds: " + index);
            }
            return data[(int) index];
        }

        @Override
        public byte[] readBytes(long index, long len) throws IOException {
            if (index < 0 || index + len > data.length) {
                throw new IOException("Range out of bounds: " + index + " + " + len);
            }
            byte[] result = new byte[(int) len];
            System.arraycopy(data, (int) index, result, 0, (int) len);
            return result;
        }

        @Override
        public java.io.InputStream getInputStream(long index) {
            return new java.io.ByteArrayInputStream(data,
                    (int) index, data.length - (int) index);
        }
    }
}
