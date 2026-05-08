package com.arkghidra.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.LoadSpec;

import com.arkghidra.format.AbcHeader;

/**
 * Tests for {@link AbcLoader}.
 */
class AbcLoaderTest {

    private AbcLoader loader;

    @BeforeEach
    void setUp() {
        loader = new AbcLoader();
    }

    @Test
    void testGetName_returnsExpectedName() {
        assertEquals("Ark Bytecode Loader", loader.getName());
    }

    @Test
    void testGetTier_isSpecializedTargetLoader() {
        assertNotNull(loader.getTier());
    }

    @Test
    void testFindSupportedLoadSpecs_withAbcFile_returnsSingleSpec()
            throws IOException {
        byte[] abcData = buildMinimalAbc();
        ByteProvider provider = new InMemoryByteProvider(abcData);
        Collection<LoadSpec> specs =
                loader.findSupportedLoadSpecs(provider);
        try {
            assertEquals(1, specs.size());
            LoadSpec spec = specs.iterator().next();
            assertTrue(spec.isPreferred());
        } finally {
            provider.close();
        }
    }

    @Test
    void testFindSupportedLoadSpecs_withNonAbcFile_returnsEmpty()
            throws IOException {
        byte[] notAbc = new byte[]{0x00, 0x01, 0x02, 0x03,
            0x04, 0x05, 0x06, 0x07};
        ByteProvider provider = new InMemoryByteProvider(notAbc);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertTrue(specs.isEmpty());
        } finally {
            provider.close();
        }
    }

    @Test
    void testFindSupportedLoadSpecs_withShortFile_returnsEmpty()
            throws IOException {
        byte[] shortData = new byte[]{0x50, 0x41};
        ByteProvider provider = new InMemoryByteProvider(shortData);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            assertTrue(specs.isEmpty());
        } finally {
            provider.close();
        }
    }

    @Test
    void testFindSupportedLoadSpecs_languageId_isArkBytecode()
            throws IOException {
        byte[] abcData = buildMinimalAbc();
        ByteProvider provider = new InMemoryByteProvider(abcData);
        try {
            Collection<LoadSpec> specs =
                    loader.findSupportedLoadSpecs(provider);
            LoadSpec spec = specs.iterator().next();
            String langId = spec.getLanguageCompilerSpec()
                    .languageID.getIdAsString();
            assertTrue(langId.startsWith("ArkBytecode"),
                    "Language ID should start with ArkBytecode, got: "
                    + langId);
        } finally {
            provider.close();
        }
    }

    @Test
    void testToNamespaceName_stripsLeadingLandTrailingSemicolon() {
        assertEquals("com/example/TestClass",
                AbcLoaderUtils.toNamespaceName("Lcom/example/TestClass;"));
    }

    @Test
    void testToNamespaceName_handlesNoPrefixOrSuffix() {
        assertEquals("MyClass", AbcLoaderUtils.toNamespaceName("MyClass"));
    }

    @Test
    void testToNamespaceName_handlesNull() {
        assertEquals("unknown", AbcLoaderUtils.toNamespaceName(null));
    }

    @Test
    void testToNamespaceName_handlesEmptyString() {
        assertEquals("unknown", AbcLoaderUtils.toNamespaceName(""));
    }

    @Test
    void testToNamespaceName_handlesOnlyL() {
        assertEquals("", AbcLoaderUtils.toNamespaceName("L"));
    }

    @Test
    void testToNamespaceName_handlesLAndSemicolon() {
        assertEquals("", AbcLoaderUtils.toNamespaceName("L;"));
    }

    @Test
    void testMagicConstant_matchesAbcHeader() {
        assertEquals(AbcHeader.MAGIC[0], (byte) 'P');
        assertEquals(AbcHeader.MAGIC[1], (byte) 'A');
        assertEquals(AbcHeader.MAGIC[2], (byte) 'N');
        assertEquals(AbcHeader.MAGIC[3], (byte) 'D');
        assertEquals(AbcHeader.MAGIC[4], (byte) 'A');
    }

    private static byte[] buildMinimalAbc() {
        ByteBuffer bb = ByteBuffer.allocate(512)
                .order(ByteOrder.LITTLE_ENDIAN);

        int classOff = 256;
        int codeOff = 350;
        int regionHeaderOff = 360;
        int classIdxOff = 400;
        int literalArrayIdxOff = 408;
        int lnpIdxOff = 416;
        int classRegionIdxOff = 420;
        int fileSize = 512;

        // Header (60 bytes)
        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(fileSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(1);
        bb.putInt(classIdxOff);
        bb.putInt(0);
        bb.putInt(lnpIdxOff);
        bb.putInt(0);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        // Class index
        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Region header (40 bytes)
        bb.position(regionHeaderOff);
        bb.putInt(classOff);
        bb.putInt(codeOff + 32);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);

        // Class region index
        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // String area
        bb.position(200);
        byte[] className = mutf8String("Lcom/example/TestClass;");
        bb.put(className);
        int methodNameStringOff = bb.position();
        bb.put(mutf8String("testMethod"));

        // Class definition
        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/TestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        // Method definition
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameStringOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code section
        bb.position(codeOff);
        bb.put(uleb128(2));
        bb.put(uleb128(1));
        bb.put(uleb128(4));
        bb.put(uleb128(0));
        bb.put(new byte[]{0x00, 0x60, 0x00, 0x64});

        bb.position(0);
        byte[] result = new byte[fileSize];
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

    private static byte[] mutf8String(String s) {
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
