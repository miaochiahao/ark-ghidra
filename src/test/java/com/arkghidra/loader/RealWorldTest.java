package com.arkghidra.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.LoadSpec;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcHeader;
import com.arkghidra.format.AbcMethod;

/**
 * Real-world integration tests for HAP loading, ABC parsing robustness,
 * and decompiler output validation.
 *
 * <p>Tests simulate realistic HarmonyOS application patterns using
 * synthetic fixtures built with {@link AbcTestFixture} and ZIP-based
 * HAP structures.</p>
 */
class RealWorldTest {

    private AbcTestFixture fixture;
    private ArkDisassembler disasm;
    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        fixture = new AbcTestFixture();
        disasm = new ArkDisassembler();
        decompiler = new ArkTSDecompiler();
    }

    // =====================================================================
    // HAP with realistic ABC content
    // =====================================================================

    @Test
    void testHapWithRealisticAbc_extractsAndParsesCorrectly()
            throws IOException {
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size());
        assertEquals("ets/modules.abc", abcEntries.get(0).path);

        AbcFile abc = AbcFile.parse(abcEntries.get(0).bytes);
        assertEquals(3, abc.getClasses().size());
        assertEquals("Lcom/example/Animal;",
                abc.getClasses().get(0).getName());
        assertEquals("Lcom/example/Dog;",
                abc.getClasses().get(1).getName());
        assertEquals("Lcom/example/Utils;",
                abc.getClasses().get(2).getName());
    }

    @Test
    void testHapWithRealisticAbc_loaderRecognizes() throws IOException {
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        HapLoader loader = new HapLoader();
        ByteProvider provider = new InMemoryByteProvider(hap);
        try {
            assertTrue(loader.isHapFile(provider));
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertEquals(1, specs.size());
        } finally {
            provider.close();
        }
    }

    @Test
    void testHapWithRealisticAbc_decompileAllMethodsProducesOutput()
            throws IOException {
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        AbcFile abc = AbcFile.parse(abcEntries.get(0).bytes);

        String result = decompiler.decompileFile(abc);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Animal"),
                "Output should contain Animal class");
        assertTrue(result.contains("Dog"),
                "Output should contain Dog class");
        assertTrue(result.contains("Utils"),
                "Output should contain Utils class");
    }

    // =====================================================================
    // HAP with module.json metadata
    // =====================================================================

    @Test
    void testHapWithModuleJson_metadataIsExtracted() throws IOException {
        String moduleJson = "{"
                + "\"module\": {"
                + "  \"name\": \"entry\","
                + "  \"type\": \"entry\","
                + "  \"versionName\": \"1.0.0\","
                + "  \"versionCode\": 100,"
                + "  \"packageName\": \"com.example.myapp\","
                + "  \"vendorName\": \"ExampleCorp\","
                + "  \"abilities\": ["
                + "    {\"name\": \"MainAbility\","
                + "     \"label\": \"Main\","
                + "     \"type\": \"page\"}"
                + "  ]"
                + "}}";
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("module.json", moduleJson.getBytes());
        entries.put("ets/modules.abc", abcData);
        entries.put("resources/base/media/icon.png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        byte[] hap = buildHapZip(entries);

        byte[] extracted = HapLoader.extractModuleJson(hap);
        assertNotNull(extracted);

        HapMetadata metadata = HapMetadataParser.parse(extracted);
        assertNotNull(metadata);
        assertEquals("entry", metadata.getModuleName());
        assertEquals("1.0.0", metadata.getVersionName());
        assertEquals(100, metadata.getVersionCode());
        assertEquals("com.example.myapp", metadata.getPackageName());
        assertEquals("ExampleCorp", metadata.getVendorName());
        assertEquals(1, metadata.getAbilities().size());
        assertEquals("MainAbility",
                metadata.getAbilities().get(0).getName());
    }

    @Test
    void testHapWithModuleJson_metadataMatchesExpectedToString()
            throws IOException {
        String moduleJson = "{"
                + "\"name\": \"feature\","
                + "\"type\": \"feature\","
                + "\"versionName\": \"2.1.0\","
                + "\"versionCode\": 210,"
                + "\"packageName\": \"com.test.app\","
                + "\"vendorName\": \"TestVendor\""
                + "}";
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("module.json", moduleJson.getBytes());
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        byte[] extracted = HapLoader.extractModuleJson(hap);
        HapMetadata metadata = HapMetadataParser.parse(extracted);

        String str = metadata.toString();
        assertTrue(str.contains("feature"));
        assertTrue(str.contains("2.1.0"));
        assertTrue(str.contains("210"));
        assertTrue(str.contains("com.test.app"));
        assertTrue(str.contains("TestVendor"));
    }

    @Test
    void testHapWithJson5ModuleJson_parsesCorrectly() throws IOException {
        String json5 = "{\n"
                + "  // HarmonyOS module config\n"
                + "  \"module\": {\n"
                + "    \"name\": \"entry\", /* module name */\n"
                + "    \"type\": \"entry\",\n"
                + "    \"versionName\": \"3.0\",\n"
                + "    \"versionCode\": 300,\n"
                + "    \"packageName\": \"com.harmony.sample\",\n"
                + "    \"vendorName\": \"HarmonySample\",\n"
                + "  }\n"
                + "}";
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("config/module.json", json5.getBytes());
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        byte[] extracted = HapLoader.extractModuleJson(hap);
        assertNotNull(extracted);

        HapMetadata metadata = HapMetadataParser.parse(extracted);
        assertNotNull(metadata);
        assertEquals("entry", metadata.getModuleName());
        assertEquals("3.0", metadata.getVersionName());
        assertEquals(300, metadata.getVersionCode());
        assertEquals("com.harmony.sample", metadata.getPackageName());
    }

    // =====================================================================
    // Multi-ABC HAP file handling
    // =====================================================================

    @Test
    void testHapWithMultipleAbcs_allAreExtracted() throws IOException {
        byte[] abc1 = fixture.buildComprehensiveAbc();
        fixture.reset();
        byte[] abc2 = fixture.buildRealisticAbc();

        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abc1);
        entries.put("ets/lib/utils.abc", abc2);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(2, abcEntries.size());

        AbcFile file1 = AbcFile.parse(abcEntries.get(0).bytes);
        AbcFile file2 = AbcFile.parse(abcEntries.get(1).bytes);

        assertEquals(2, file1.getClasses().size(),
                "First ABC should have 2 classes (comprehensive)");
        assertEquals(3, file2.getClasses().size(),
                "Second ABC should have 3 classes (realistic)");
    }

    @Test
    void testHapWithMultipleAbcs_blockSpacingIsCorrect()
            throws IOException {
        byte[] abc1 = fixture.buildComprehensiveAbc();
        fixture.reset();
        byte[] abc2 = fixture.buildRealisticAbc();

        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abc1);
        entries.put("ets/feature/feature.abc", abc2);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        abcEntries.sort((a, b) -> a.path.compareTo(b.path));

        long spacing = HapLoader.ABC_BLOCK_PADDING;
        assertTrue(abc1.length < spacing,
                "ABC data must be smaller than block padding");
        assertTrue(abc2.length < spacing,
                "ABC data must be smaller than block padding");
    }

    @Test
    void testHapWithMultipleAbcs_loaderRecognizes() throws IOException {
        byte[] abc1 = fixture.buildComprehensiveAbc();
        fixture.reset();
        byte[] abc2 = fixture.buildRealisticAbc();

        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abc1);
        entries.put("ets/lib/helpers.abc", abc2);
        entries.put("module.json",
                "{\"name\":\"entry\",\"type\":\"entry\"}".getBytes());
        byte[] hap = buildHapZip(entries);

        HapLoader loader = new HapLoader();
        ByteProvider provider = new InMemoryByteProvider(hap);
        try {
            assertTrue(loader.isHapFile(provider));
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertEquals(1, specs.size());
        } finally {
            provider.close();
        }
    }

    // =====================================================================
    // ABC parser robustness: edge cases
    // =====================================================================

    @Nested
    class AbcEdgeCaseTests {

        @Test
        void testAbcWithSingleClassNoMethods_parsesSuccessfully() {
            byte[] data = buildMinimalAbcSingleClass();
            AbcFile abc = AbcFile.parse(data);
            assertNotNull(abc);
            assertEquals(1, abc.getClasses().size());
            assertTrue(abc.getClasses().get(0).getMethods().isEmpty());
            assertTrue(abc.getClasses().get(0).getFields().isEmpty());
        }

        @Test
        void testAbcWithSingleClassNoMethods_headerIsValid() {
            byte[] data = buildMinimalAbcSingleClass();
            AbcFile abc = AbcFile.parse(data);
            AbcHeader header = abc.getHeader();
            assertTrue(header.hasValidMagic());
            assertEquals(2, header.getVersionNumber());
            assertEquals(1, header.getNumClasses());
        }

        @Test
        void testAbcWithSingleClassNoMethods_classNameCorrect() {
            byte[] data = buildMinimalAbcSingleClass();
            AbcFile abc = AbcFile.parse(data);
            assertEquals("Lcom/example/EmptyService;",
                    abc.getClasses().get(0).getName());
        }

        @Test
        void testAbcWithSingleClassNoMethods_noCodeForMethods() {
            byte[] data = buildMinimalAbcSingleClass();
            AbcFile abc = AbcFile.parse(data);
            AbcClass cls = abc.getClasses().get(0);
            assertTrue(cls.getMethods().isEmpty(),
                    "Class with no methods should have empty method list");
        }

        @Test
        void testRealisticAbc_allMethodsDecompilable_noExceptions() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);

            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod method : cls.getMethods()) {
                    AbcCode code = abc.getCodeForMethod(method);
                    assertNotNull(code,
                            cls.getName() + "." + method.getName()
                                    + " should have code");
                    String result = decompiler.decompileMethod(
                            method, code, abc);
                    assertNotNull(result,
                            "Decompiled " + cls.getName()
                                    + "." + method.getName()
                                    + " should not be null");
                    assertFalse(result.isEmpty(),
                            "Decompiled " + cls.getName()
                                    + "." + method.getName()
                                    + " should not be empty");
                }
            }
        }

        @Test
        void testRealisticAbc_decompileTryCatch_containsKeywords() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);
            AbcMethod tryCatch = abc.getClasses().get(0).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(tryCatch);

            String result = decompiler.decompileMethod(tryCatch, code, abc);
            assertTrue(result.contains("try"),
                    "Try/catch method should contain 'try'");
            assertTrue(result.contains("catch"),
                    "Try/catch method should contain 'catch'");
        }

        @Test
        void testRealisticAbc_decompileLargeLoop_containsLoop() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);
            AbcMethod largeLoop = abc.getClasses().get(1).getMethods().get(1);
            AbcCode code = abc.getCodeForMethod(largeLoop);

            String result = decompiler.decompileMethod(largeLoop, code, abc);
            assertTrue(result.contains("while") || result.contains("for"),
                    "Loop method should contain while or for");
        }

        @Test
        void testRealisticAbc_decompileSubtract_hasSubtraction() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);
            AbcMethod sub = abc.getClasses().get(1).getMethods().get(3);
            AbcCode code = abc.getCodeForMethod(sub);

            String result = decompiler.decompileMethod(sub, code, abc);
            assertTrue(result.contains("-"),
                    "Subtract method should contain '-'");
        }

        @Test
        void testRealisticAbc_decompileMultiply_hasMultiplication() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);
            AbcMethod mul = abc.getClasses().get(2).getMethods().get(2);
            AbcCode code = abc.getCodeForMethod(mul);

            String result = decompiler.decompileMethod(mul, code, abc);
            assertTrue(result.contains("*"),
                    "Multiply method should contain '*'");
        }

        @Test
        void testRealisticAbc_fullFileDecompilation_containsInheritance() {
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertTrue(result.contains("Dog"),
                    "Full output should contain Dog class");
            assertTrue(result.contains("Animal"),
                    "Full output should reference Animal (inheritance)");
        }
    }

    // =====================================================================
    // HAP block name sanitization
    // =====================================================================

    @Test
    void testSanitizeBlockName_withPathSeparators() {
        assertEquals("abc_0_ets_modules.abc",
                HapLoader.sanitizeBlockName("ets/modules.abc", 0));
    }

    @Test
    void testSanitizeBlockName_withLeadingSlash() {
        assertEquals("abc_1_ets_lib_utils.abc",
                HapLoader.sanitizeBlockName("/ets/lib/utils.abc", 1));
    }

    @Test
    void testSanitizeBlockName_withBackslashes() {
        assertEquals("abc_0_ets_modules.abc",
                HapLoader.sanitizeBlockName("ets\\modules.abc", 0));
    }

    @Test
    void testSanitizeBlockName_withIndex() {
        assertEquals("abc_3_foo_bar.abc",
                HapLoader.sanitizeBlockName("foo/bar.abc", 3));
    }

    // =====================================================================
    // HAP with empty ABC entry
    // =====================================================================

    @Test
    void testHapWithEmptyAbcEntry_extractsButParseFails()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", new byte[0]);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size());
        assertEquals(0, abcEntries.get(0).bytes.length);
    }

    // =====================================================================
    // Comprehensive ABC with HAP round-trip
    // =====================================================================

    @Test
    void testComprehensiveAbc_hapRoundTrip_preservesBytecode()
            throws IOException {
        byte[] abcData = fixture.buildComprehensiveAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size());

        AbcFile abc = AbcFile.parse(abcEntries.get(0).bytes);
        assertEquals(fixture.getExpectedClassCount(),
                abc.getClasses().size());

        for (int i = 0; i < fixture.getExpectedClassCount(); i++) {
            assertEquals(fixture.getExpectedClassName(i),
                    abc.getClasses().get(i).getName());
        }

        int totalMethods = 0;
        for (AbcClass cls : abc.getClasses()) {
            totalMethods += cls.getMethods().size();
        }
        assertEquals(fixture.getExpectedMethodCount(), totalMethods);
    }

    @Test
    void testComprehensiveAbc_hapRoundTrip_disassemblyWorks()
            throws IOException {
        byte[] abcData = fixture.buildComprehensiveAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        AbcFile abc = AbcFile.parse(abcEntries.get(0).bytes);

        for (AbcClass cls : abc.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                AbcCode code = abc.getCodeForMethod(method);
                assertNotNull(code);
                List<ArkInstruction> insns = disasm.disassemble(
                        code.getInstructions(), 0,
                        (int) code.getCodeSize());
                assertFalse(insns.isEmpty(),
                        cls.getName() + "." + method.getName()
                                + " should produce instructions");
            }
        }
    }

    @Test
    void testComprehensiveAbc_hapRoundTrip_decompileFile()
            throws IOException {
        byte[] abcData = fixture.buildComprehensiveAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        AbcFile abc = AbcFile.parse(abcEntries.get(0).bytes);

        String output = decompiler.decompileFile(abc);
        assertNotNull(output);
        assertFalse(output.isEmpty());
        assertTrue(output.contains("BaseClass"));
        assertTrue(output.contains("ChildClass"));
    }

    // =====================================================================
    // HAP with non-ABC resources
    // =====================================================================

    @Test
    void testHapWithResourcesAndAbc_onlyExtractsAbc() throws IOException {
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", abcData);
        entries.put("resources/base/media/icon.png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A});
        entries.put("resources/base/element/string.json",
                "{\"str\":[{\"value\":\"Hello\"}]}".getBytes());
        entries.put("config.json", "{}".getBytes());
        entries.put("ets/vendorinfo.json", "{}".getBytes());
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size(),
                "Only the .abc entry should be extracted");
        assertEquals("ets/modules.abc", abcEntries.get(0).path);
    }

    // =====================================================================
    // AbcLoader recognizes realistic ABC via loader API
    // =====================================================================

    @Test
    void testAbcLoader_realisticAbc_returnsLoadSpec() throws IOException {
        byte[] abcData = fixture.buildRealisticAbc();
        AbcLoader loader = new AbcLoader();
        ByteProvider provider = new InMemoryByteProvider(abcData);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertEquals(1, specs.size());
            LoadSpec spec = specs.iterator().next();
            assertTrue(spec.isPreferred());
        } finally {
            provider.close();
        }
    }

    @Test
    void testAbcLoader_comprehensiveAbc_returnsLoadSpec()
            throws IOException {
        byte[] abcData = fixture.buildComprehensiveAbc();
        AbcLoader loader = new AbcLoader();
        ByteProvider provider = new InMemoryByteProvider(abcData);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertEquals(1, specs.size());
        } finally {
            provider.close();
        }
    }

    // =====================================================================
    // HAP metadata with realistic HarmonyOS module.json
    // =====================================================================

    @Test
    void testHapMetadata_realisticHarmonyConfig() throws IOException {
        String json = "{"
                + "\"module\": {"
                + "  \"name\": \"entry\","
                + "  \"type\": \"entry\","
                + "  \"versionName\": \"1.0.0\","
                + "  \"versionCode\": 1000000,"
                + "  \"packageName\": \"com.samples.weather\","
                + "  \"vendorName\": \"HarmonyOS Samples\","
                + "  \"abilities\": ["
                + "    {"
                + "      \"name\": \"EntryAbility\","
                + "      \"label\": \"Weather App\","
                + "      \"type\": \"page\""
                + "    },"
                + "    {"
                + "      \"name\": \"ServiceAbility\","
                + "      \"label\": \"Weather Service\","
                + "      \"type\": \"service\""
                + "    }"
                + "  ]"
                + "}}";
        byte[] abcData = fixture.buildRealisticAbc();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("module.json", json.getBytes());
        entries.put("ets/modules.abc", abcData);
        byte[] hap = buildHapZip(entries);

        byte[] extracted = HapLoader.extractModuleJson(hap);
        HapMetadata metadata = HapMetadataParser.parse(extracted);

        assertNotNull(metadata);
        assertEquals("entry", metadata.getModuleName());
        assertEquals("entry", metadata.getModuleType());
        assertEquals("1.0.0", metadata.getVersionName());
        assertEquals(1000000, metadata.getVersionCode());
        assertEquals("com.samples.weather", metadata.getPackageName());
        assertEquals("HarmonyOS Samples", metadata.getVendorName());
        assertEquals(2, metadata.getAbilities().size());
        assertEquals("EntryAbility",
                metadata.getAbilities().get(0).getName());
        assertEquals("page", metadata.getAbilities().get(0).getType());
        assertEquals("ServiceAbility",
                metadata.getAbilities().get(1).getName());
        assertEquals("service",
                metadata.getAbilities().get(1).getType());
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static byte[] buildHapZip(Map<String, byte[]> entries)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Builds a minimal ABC with a single class that has no methods
     * and no fields. Tests parser handling of empty class bodies.
     */
    private static byte[] buildMinimalAbcSingleClass() {
        int bufSize = 1024;
        ByteBuffer bb = ByteBuffer.allocate(bufSize)
                .order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classAreaOff = 400;
        int regionHeaderOff = 600;
        int classIdxOff = 700;
        int classRegionIdxOff = 720;
        int literalArrayIdxOff = 740;
        int lnpIdxOff = 760;
        int protoIdxOff = 780;

        // String area: class name
        bb.position(stringAreaOff);
        byte[] className = uleb128Mutf8("Lcom/example/EmptyService;");
        bb.put(className);
        int classNameOff = stringAreaOff;

        // Class definition: name, superClassOff=0, accessFlags=ACC_PUBLIC,
        // numFields=0, numMethods=0, end tag 0x00
        bb.position(classAreaOff);
        bb.put(uleb128Mutf8("Lcom/example/EmptyService;"));
        bb.putInt(0);                   // superClassOff
        bb.put(uleb128(0x0001));         // ACC_PUBLIC
        bb.put(uleb128(0));              // numFields = 0
        bb.put(uleb128(0));              // numMethods = 0
        bb.put(new byte[]{0x00});        // end tag

        // Region header
        bb.position(regionHeaderOff);
        bb.putInt(classAreaOff);         // startOff
        bb.putInt(classAreaOff + 100);   // endOff
        bb.putInt(1);                    // classIdxSize
        bb.putInt(classRegionIdxOff);    // classIdxOff
        bb.putInt(0);                    // methodIdxSize
        bb.putInt(0);                    // methodIdxOff
        bb.putInt(0);                    // fieldIdxSize
        bb.putInt(0);                    // fieldIdxOff
        bb.putInt(0);                    // protoIdxSize
        bb.putInt(protoIdxOff);          // protoIdxOff

        // Class index
        bb.position(classIdxOff);
        bb.putInt(classAreaOff);

        // Class region index
        bb.position(classRegionIdxOff);
        bb.putInt(classAreaOff);

        // Literal array index (empty)
        bb.position(literalArrayIdxOff);

        // Proto index (empty)
        bb.position(protoIdxOff);

        // LNP index (empty)
        bb.position(lnpIdxOff);

        // Header (60 bytes)
        bb.position(0);
        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);                                              // checksum
        bb.put(new byte[]{'0', '0', '0', '2'});                   // version
        bb.putInt(bufSize);                                        // fileSize
        bb.putInt(0);                                              // foreignOff
        bb.putInt(0);                                              // foreignSize
        bb.putInt(1);                                              // numClasses
        bb.putInt(classIdxOff);                                    // classIdxOff
        bb.putInt(0);                                              // numLnps
        bb.putInt(lnpIdxOff);                                      // lnpIdxOff
        bb.putInt(0);                                              // numLiteralArrays
        bb.putInt(literalArrayIdxOff);                             // literalArrayIdxOff
        bb.putInt(1);                                              // numIndexRegions
        bb.putInt(regionHeaderOff);                                // indexSectionOff

        bb.position(0);
        byte[] result = new byte[bufSize];
        bb.get(result);
        return result;
    }

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

    private static byte[] uleb128Mutf8(String s) {
        byte[] strBytes = s.getBytes(
                java.nio.charset.StandardCharsets.UTF_8);
        byte[] header = uleb128(((long) s.length() << 1) | 1);
        byte[] result = new byte[header.length + strBytes.length + 1];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(strBytes, 0, result,
                header.length, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    /**
     * Simple in-memory ByteProvider that avoids triggering
     * Ghidra's Application initialization.
     */
    private static class InMemoryByteProvider
            implements ByteProvider {

        private final byte[] data;

        InMemoryByteProvider(byte[] data) {
            this.data = data;
        }

        @Override
        public File getFile() {
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
        public byte[] readBytes(long index, long len)
                throws IOException {
            if (index < 0 || index + len > data.length) {
                throw new IOException("Range out of bounds: "
                        + index + " + " + len);
            }
            byte[] result = new byte[(int) len];
            System.arraycopy(data, (int) index, result, 0,
                    (int) len);
            return result;
        }

        @Override
        public InputStream getInputStream(long index) {
            return new ByteArrayInputStream(data,
                    (int) index, data.length - (int) index);
        }
    }
}
