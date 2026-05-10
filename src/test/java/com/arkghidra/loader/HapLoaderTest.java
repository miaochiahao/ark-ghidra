package com.arkghidra.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.LoadSpec;

/**
 * Tests for {@link HapLoader}, {@link HapMetadataParser},
 * and {@link HapMetadata}.
 */
class HapLoaderTest {

    private HapLoader loader;

    @BeforeEach
    void setUp() {
        loader = new HapLoader();
    }

    // ---------------------------------------------------------------
    // HapLoader.getName()
    // ---------------------------------------------------------------

    @Test
    void testGetName_returnsHarmonyOSHapLoader() {
        assertEquals("HarmonyOS HAP Loader", loader.getName());
    }

    // ---------------------------------------------------------------
    // HapLoader.isHapFile()
    // ---------------------------------------------------------------

    @Test
    void testIsHapFile_withValidHap_returnsTrue() throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);
        ByteProvider provider = new InMemoryByteProvider(hap);
        try {
            assertTrue(loader.isHapFile(provider));
        } finally {
            provider.close();
        }
    }

    @Test
    void testIsHapFile_withPlainZipNoAbc_returnsFalse()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("module.json",
                "{ \"packageName\": \"test\" }".getBytes());
        entries.put("resources/index.txt", "hello".getBytes());
        byte[] zip = buildHapZip(entries);
        ByteProvider provider = new InMemoryByteProvider(zip);
        try {
            assertFalse(loader.isHapFile(provider));
        } finally {
            provider.close();
        }
    }

    @Test
    void testIsHapFile_withRawAbc_returnsFalse() throws IOException {
        byte[] abc = buildMinimalAbc();
        ByteProvider provider = new InMemoryByteProvider(abc);
        try {
            assertFalse(loader.isHapFile(provider));
        } finally {
            provider.close();
        }
    }

    @Test
    void testIsHapFile_withShortFile_returnsFalse() throws IOException {
        byte[] shortData = new byte[]{0x50, 0x4B};
        ByteProvider provider = new InMemoryByteProvider(shortData);
        try {
            assertFalse(loader.isHapFile(provider));
        } finally {
            provider.close();
        }
    }

    // ---------------------------------------------------------------
    // HapLoader.findSupportedLoadSpecs()
    // ---------------------------------------------------------------

    @Test
    void testFindSupportedLoadSpecs_withHap_returnsSpec()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);
        ByteProvider provider = new InMemoryByteProvider(hap);
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
    void testFindSupportedLoadSpecs_withNonHap_returnsEmpty()
            throws IOException {
        byte[] notZip = new byte[]{0x00, 0x01, 0x02, 0x03,
            0x04, 0x05, 0x06, 0x07};
        ByteProvider provider = new InMemoryByteProvider(notZip);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertTrue(specs.isEmpty());
        } finally {
            provider.close();
        }
    }

    // ---------------------------------------------------------------
    // HapLoader.extractAbcEntries()
    // ---------------------------------------------------------------

    @Test
    void testExtractAbcEntries_withTwoAbcs_returnsTwo()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", buildMinimalAbc());
        entries.put("ets/second.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(2, abcEntries.size());
        assertEquals("ets/modules.abc", abcEntries.get(0).path);
        assertEquals("ets/second.abc", abcEntries.get(1).path);
    }

    @Test
    void testExtractAbcEntries_ignoresNonAbcEntries()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", buildMinimalAbc());
        entries.put("module.json", "{}".getBytes());
        entries.put("resources/icon.png", new byte[]{0x01, 0x02});
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size());
        assertEquals("ets/modules.abc", abcEntries.get(0).path);
    }

    @Test
    void testExtractAbcEntries_caseInsensitiveExtension()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.ABC", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);

        List<HapLoader.AbcEntry> abcEntries =
                HapLoader.extractAbcEntries(hap);
        assertEquals(1, abcEntries.size());
    }

    // ---------------------------------------------------------------
    // HapLoader.extractModuleJson()
    // ---------------------------------------------------------------

    @Test
    void testExtractModuleJson_findsModuleJson() throws IOException {
        String json = "{\"module\":{}}";
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("module.json", json.getBytes());
        entries.put("ets/modules.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);

        byte[] result = HapLoader.extractModuleJson(hap);
        assertNotNull(result);
        assertEquals(json, new String(result));
    }

    @Test
    void testExtractModuleJson_findsConfigModuleJson()
            throws IOException {
        String json = "{\"module\":{}}";
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("config/module.json", json.getBytes());
        entries.put("ets/modules.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);

        byte[] result = HapLoader.extractModuleJson(hap);
        assertNotNull(result);
        assertEquals(json, new String(result));
    }

    @Test
    void testExtractModuleJson_returnsNullWhenMissing()
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("ets/modules.abc", buildMinimalAbc());
        byte[] hap = buildHapZip(entries);

        byte[] result = HapLoader.extractModuleJson(hap);
        assertNull(result);
    }

    // ---------------------------------------------------------------
    // HapMetadataParser.parse() - flat format
    // ---------------------------------------------------------------

    @Test
    void testHapMetadataParser_parsesFlatModuleJson() {
        String json = "{"
                + "\"name\": \"entry\","
                + "\"type\": \"entry\","
                + "\"versionName\": \"1.0.0\","
                + "\"versionCode\": 100,"
                + "\"packageName\": \"com.example.myapp\","
                + "\"vendorName\": \"ExampleCorp\""
                + "}";
        HapMetadata meta =
                HapMetadataParser.parse(json.getBytes());
        assertNotNull(meta);
        assertEquals("entry", meta.getModuleName());
        assertEquals("entry", meta.getModuleType());
        assertEquals("1.0.0", meta.getVersionName());
        assertEquals(100, meta.getVersionCode());
        assertEquals("com.example.myapp", meta.getPackageName());
        assertEquals("ExampleCorp", meta.getVendorName());
    }

    // ---------------------------------------------------------------
    // HapMetadataParser.parse() - nested module format
    // ---------------------------------------------------------------

    @Test
    void testHapMetadataParser_parsesNestedModuleJson() {
        String json = "{\"module\": {"
                + "\"name\": \"feature\","
                + "\"type\": \"feature\","
                + "\"versionName\": \"2.0.0\","
                + "\"versionCode\": 200,"
                + "\"packageName\": \"com.example.app\","
                + "\"vendorName\": \"TestVendor\""
                + "}}";
        HapMetadata meta =
                HapMetadataParser.parse(json.getBytes());
        assertNotNull(meta);
        assertEquals("feature", meta.getModuleName());
        assertEquals("feature", meta.getModuleType());
        assertEquals("2.0.0", meta.getVersionName());
        assertEquals(200, meta.getVersionCode());
        assertEquals("com.example.app", meta.getPackageName());
        assertEquals("TestVendor", meta.getVendorName());
    }

    // ---------------------------------------------------------------
    // HapMetadataParser.parse() - JSON5 features
    // ---------------------------------------------------------------

    @Test
    void testHapMetadataParser_handlesJson5Comments() {
        String json = "{\n"
                + "  // This is a comment\n"
                + "  \"name\": \"entry\",\n"
                + "  /* Block comment */\n"
                + "  \"type\": \"entry\",\n"
                + "  \"versionName\": \"2.0.0\",\n"
                + "  \"versionCode\": 200,\n"
                + "  \"packageName\": \"com.example.app\",\n"
                + "  \"vendorName\": \"Test\"\n"
                + "}";
        HapMetadata meta =
                HapMetadataParser.parse(json.getBytes());
        assertNotNull(meta);
        assertEquals("entry", meta.getModuleName());
        assertEquals("2.0.0", meta.getVersionName());
        assertEquals(200, meta.getVersionCode());
    }

    @Test
    void testHapMetadataParser_withGarbageInput_returnsEmptyFields() {
        byte[] garbage = "this is not json at all".getBytes();
        HapMetadata meta = HapMetadataParser.parse(garbage);
        assertNotNull(meta);
        assertEquals("", meta.getModuleName());
        assertEquals("", meta.getModuleType());
        assertEquals("", meta.getPackageName());
        assertEquals(0, meta.getVersionCode());
    }

    @Test
    void testHapMetadataParser_returnsNullForNullInput() {
        assertNull(HapMetadataParser.parse(null));
    }

    @Test
    void testHapMetadataParser_returnsNullForEmptyInput() {
        assertNull(HapMetadataParser.parse(new byte[0]));
    }

    @Test
    void testHapMetadataParser_stripsTrailingCommas() {
        String json = "{"
                + "\"name\": \"entry\","
                + "\"type\": \"entry\","
                + "\"versionName\": \"1.0\","
                + "\"versionCode\": 1,"
                + "\"packageName\": \"com.example.app\","
                + "\"vendorName\": \"Test\","
                + "}";
        HapMetadata meta =
                HapMetadataParser.parse(json.getBytes());
        assertNotNull(meta);
        assertEquals("entry", meta.getModuleName());
        assertEquals("com.example.app", meta.getPackageName());
    }

    // ---------------------------------------------------------------
    // HapMetadataParser.parse() - abilities
    // ---------------------------------------------------------------

    @Test
    void testHapMetadataParser_parsesAbilities() {
        String json = "{"
                + "\"name\": \"entry\","
                + "\"type\": \"entry\","
                + "\"versionName\": \"1.0\","
                + "\"versionCode\": 1,"
                + "\"packageName\": \"com.example.app\","
                + "\"vendorName\": \"Test\","
                + "\"abilities\": ["
                + "  {\"name\": \"MainAbility\","
                + "   \"label\": \"Main\","
                + "   \"type\": \"page\"}"
                + "]"
                + "}";
        HapMetadata meta =
                HapMetadataParser.parse(json.getBytes());
        assertNotNull(meta);
        List<HapMetadata.AbilityInfo> abilities =
                meta.getAbilities();
        assertEquals(1, abilities.size());
        assertEquals("MainAbility", abilities.get(0).getName());
        assertEquals("Main", abilities.get(0).getLabel());
        assertEquals("page", abilities.get(0).getType());
    }

    // ---------------------------------------------------------------
    // HapMetadataParser.preprocessJson5()
    // ---------------------------------------------------------------

    @Test
    void testPreprocessJson5_stripsSingleLineComments() {
        String input = "{\n"
                + "  // remove this\n"
                + "  \"key\": \"value\"\n"
                + "}";
        String result = HapMetadataParser.preprocessJson5(input);
        assertFalse(result.contains("remove this"));
        assertTrue(result.contains("\"key\": \"value\""));
    }

    @Test
    void testPreprocessJson5_stripsBlockComments() {
        String input = "{\n"
                + "  /* block\n"
                + "     comment */\n"
                + "  \"key\": \"value\"\n"
                + "}";
        String result = HapMetadataParser.preprocessJson5(input);
        assertFalse(result.contains("block"));
        assertFalse(result.contains("comment"));
        assertTrue(result.contains("\"key\": \"value\""));
    }

    @Test
    void testPreprocessJson5_stripsTrailingCommas() {
        String input = "{\"a\": 1, \"b\": 2, }";
        String result = HapMetadataParser.preprocessJson5(input);
        assertFalse(result.contains(", }"));
        assertTrue(result.contains("\"b\": 2"));
    }

    @Test
    void testPreprocessJson5_preservesSlashesInStrings() {
        String input = "{\"path\": \"http://example.com/foo\"}";
        String result = HapMetadataParser.preprocessJson5(input);
        assertTrue(result.contains("http://example.com/foo"));
    }

    @Test
    void testPreprocessJson5_returnsNullForNull() {
        assertNull(HapMetadataParser.preprocessJson5(null));
    }

    @Test
    void testPreprocessJson5_returnsEmptyForEmpty() {
        assertEquals("", HapMetadataParser.preprocessJson5(""));
    }

    // ---------------------------------------------------------------
    // HapMetadata data class
    // ---------------------------------------------------------------

    @Test
    void testHapMetadata_toString_containsFields() {
        HapMetadata meta = new HapMetadata(
                "entry", "entry", "1.0", 1,
                "com.example.app", "TestVendor",
                Collections.emptyList());
        String str = meta.toString();
        assertTrue(str.contains("entry"));
        assertTrue(str.contains("1.0"));
        assertTrue(str.contains("com.example.app"));
    }

    @Test
    void testHapMetadata_nullsBecomeDefaults() {
        HapMetadata meta = new HapMetadata(
                null, null, null, 0, null, null, null);
        assertEquals("", meta.getModuleName());
        assertEquals("", meta.getModuleType());
        assertEquals("", meta.getVersionName());
        assertEquals("", meta.getPackageName());
        assertEquals("", meta.getVendorName());
        assertNotNull(meta.getAbilities());
        assertTrue(meta.getAbilities().isEmpty());
    }

    @Test
    void testHapMetadata_abilitiesIsUnmodifiable() {
        HapMetadata.AbilityInfo ability =
                new HapMetadata.AbilityInfo("Main", "Main", "page");
        HapMetadata meta = new HapMetadata(
                "entry", "entry", "1.0", 1,
                "com.example", "Vendor",
                List.of(ability));
        List<HapMetadata.AbilityInfo> abilities =
                meta.getAbilities();
        assertEquals(1, abilities.size());
        assertEquals("Main", abilities.get(0).getName());
    }

    @Test
    void testAbilityInfo_nullsBecomeDefaults() {
        HapMetadata.AbilityInfo info =
                new HapMetadata.AbilityInfo(null, null, null);
        assertEquals("", info.getName());
        assertEquals("", info.getLabel());
        assertEquals("", info.getType());
    }

    // ---------------------------------------------------------------
    // HapLoader constants
    // ---------------------------------------------------------------

    @Test
    void testZipMagicConstant_values() {
        assertEquals(0x50, HapLoader.ZIP_MAGIC[0]);
        assertEquals(0x4B, HapLoader.ZIP_MAGIC[1]);
        assertEquals(0x03, HapLoader.ZIP_MAGIC[2]);
        assertEquals(0x04, HapLoader.ZIP_MAGIC[3]);
    }

    @Test
    void testAbcBlockPadding_value() {
        assertEquals(0x100000L, HapLoader.ABC_BLOCK_PADDING);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

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

    private static byte[] buildMinimalAbc() {
        return new byte[]{
            'P', 'A', 'N', 'D', 'A', 0, 0, 0,
            0, 0, 0, 0,
            '0', '0', '0', '2',
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
        };
    }

    /**
     * Simple in-memory ByteProvider that avoids triggering
     * Ghidra's Application initialization (unlike
     * RandomAccessByteProvider).
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
                throw new IOException("Index out of bounds: "
                        + index);
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
