package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcLiteralArray;
import com.arkghidra.format.AbcMethod;

/**
 * Tests for improved object key resolution from string table
 * in literal arrays (issue #148).
 *
 * <p>Verifies that {@code parseLiteralToString()} resolves actual string
 * names from the ABC string table (LNP index) instead of producing
 * generic {@code key_N} placeholders.</p>
 *
 * <p>ABC literal arrays store tagged values. Each entry is a tag byte
 * followed by a value whose size depends on the tag. The {@code numLiterals}
 * header field counts total tag+value slots, and the parser computes
 * {@code count = numLiterals / 2}. For object literals, entries are
 * interleaved key-value pairs: [key0, val0, key1, val1, ...].</p>
 */
class ObjectKeyResolutionTest {

    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
    }

    /**
     * Encodes a string as raw null-terminated bytes (no ULEB128 length
     * prefix) suitable for LNP string entries read by
     * {@code AbcFile.readMutf8At()}.
     */
    private static byte[] rawNullTerminated(String s) {
        byte[] strBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = new byte[strBytes.length + 1];
        System.arraycopy(strBytes, 0, result, 0, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    // =====================================================================
    // Helper: build ABC with LNP strings and a literal array for objects
    // =====================================================================

    /**
     * Builds an ABC binary that has:
     * <ul>
     *   <li>LNP strings: "name" (index 0), "age" (index 1)</li>
     *   <li>A literal array (index 0) encoding an object with two
     *       properties where keys are string indices encoded as int32:
     *       key0=stringIdx(0)="name" val0=42,
     *       key1=stringIdx(1)="age"   val1=30</li>
     *   <li>One class with one method whose bytecode uses
     *       CREATEOBJECTWITHBUFFER referencing literal array index 0</li>
     * </ul>
     *
     * <p>numLiterals = 8 gives count = 8/2 = 4 entries (2 key-value pairs).</p>
     *
     * @return parsed AbcFile
     */
    private static AbcFile buildAbcWithObjectLiteral() {
        int bufSize = 8192;
        ByteBuffer bb = ByteBuffer.allocate(bufSize)
                .order(ByteOrder.LITTLE_ENDIAN);

        int lnpAreaOff = 200;
        int stringAreaOff = 500;
        int classAreaOff = 800;
        int codeAreaOff = 2000;
        int literalArrayAreaOff = 3000;
        int regionHeaderOff = 4000;
        int classIdxOff = 4200;
        int methodRegionIdxOff = 4250;
        int fieldRegionIdxOff = 4270;
        int protoIdxOff = 4290;
        int classRegionIdxOff = 4310;
        int literalArrayIdxOff = 4330;
        int lnpIdxOff = 4350;

        // --- LNP strings (null-terminated, no ULEB128 length prefix) ---
        int str0Off = lnpAreaOff;
        byte[] str0Bytes = rawNullTerminated("name");
        bb.position(str0Off);
        bb.put(str0Bytes);

        int str1Off = str0Off + str0Bytes.length;
        byte[] str1Bytes = rawNullTerminated("age");
        bb.position(str1Off);
        bb.put(str1Bytes);

        // --- Class/method name strings ---
        int clsNameOff = stringAreaOff;
        byte[] clsNameBytes = AbcTestFixture.mutf8String("Ltest/MyClass;");
        bb.position(clsNameOff);
        bb.put(clsNameBytes);

        int methodNameOff = clsNameOff + clsNameBytes.length;
        byte[] methodNameBytes = AbcTestFixture.mutf8String("createObj");
        bb.position(methodNameOff);
        bb.put(methodNameBytes);

        // --- Literal array: object with 2 key-value pairs ---
        // numLiterals = 8 => count = 4 entries (2 key-value pairs)
        bb.position(literalArrayAreaOff);
        bb.putInt(8);
        // Entry 0: key - tag 0x03 (int32), value = string index 0 ("name")
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(0));
        // Entry 1: value - tag 0x03 (int32), value = 42
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(42));
        // Entry 2: key - tag 0x03 (int32), value = string index 1 ("age")
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(1));
        // Entry 3: value - tag 0x03 (int32), value = 30
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(30));

        // --- Class definition ---
        bb.position(classAreaOff);
        bb.put(AbcTestFixture.mutf8String("Ltest/MyClass;"));
        bb.putInt(0);
        bb.put(AbcTestFixture.uleb128(0x0001));
        bb.put(AbcTestFixture.uleb128(0));
        bb.put(AbcTestFixture.uleb128(1));
        bb.put(new byte[] {0x00});

        int methodCodeOff = codeAreaOff;
        byte[] methodCode = AbcTestFixture.concat(
                new byte[] {0x07, 0x00},
                AbcTestFixture.le16(0),
                new byte[] {0x61, 0x00},
                new byte[] {0x60, 0x00},
                new byte[] {0x64}
        );

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(AbcTestFixture.uleb128(0x0001));
        bb.put(new byte[] {0x01});
        bb.putInt(methodCodeOff);
        bb.put(new byte[] {0x00});

        // --- Code section ---
        bb.position(methodCodeOff);
        bb.put(AbcTestFixture.uleb128(4));
        bb.put(AbcTestFixture.uleb128(1));
        bb.put(AbcTestFixture.uleb128(methodCode.length));
        bb.put(AbcTestFixture.uleb128(0));
        bb.put(methodCode);

        // --- Region headers ---
        bb.position(regionHeaderOff);
        bb.putInt(classAreaOff);
        bb.putInt(codeAreaOff + 200);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        bb.putInt(1);
        bb.putInt(methodRegionIdxOff);
        bb.putInt(0);
        bb.putInt(fieldRegionIdxOff);
        bb.putInt(1);
        bb.putInt(protoIdxOff);

        // --- Index arrays ---
        bb.position(classIdxOff);
        bb.putInt(classAreaOff);

        bb.position(classRegionIdxOff);
        bb.putInt(classAreaOff);

        bb.position(methodRegionIdxOff);
        bb.putInt(0);

        bb.position(fieldRegionIdxOff);

        bb.position(protoIdxOff);
        bb.putShort((short) 0x0007);

        bb.position(literalArrayIdxOff);
        bb.putInt(literalArrayAreaOff);

        bb.position(lnpIdxOff);
        bb.putInt(str0Off);
        bb.putInt(str1Off);

        // --- Header ---
        bb.position(0);
        bb.put(new byte[] {'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[] {'0', '0', '0', '2'});
        bb.putInt(bufSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(1);
        bb.putInt(classIdxOff);
        bb.putInt(2);
        bb.putInt(lnpIdxOff);
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(0);
        byte[] data = new byte[bufSize];
        bb.get(data);
        return AbcFile.parse(data);
    }

    /**
     * Builds an ABC where the literal array key references a string index
     * (99) that does not exist in the LNP table (which is empty),
     * to verify fallback to {@code key_N} placeholder.
     *
     * <p>numLiterals = 4 => count = 2 entries (1 key-value pair).</p>
     */
    private static AbcFile buildAbcWithUnresolvableKey() {
        int bufSize = 4096;
        ByteBuffer bb = ByteBuffer.allocate(bufSize)
                .order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 500;
        int classAreaOff = 800;
        int codeAreaOff = 2000;
        int literalArrayAreaOff = 3000;
        int regionHeaderOff = 3500;
        int classIdxOff = 3600;
        int methodRegionIdxOff = 3650;
        int fieldRegionIdxOff = 3670;
        int protoIdxOff = 3690;
        int classRegionIdxOff = 3710;
        int literalArrayIdxOff = 3730;
        int lnpIdxOff = 3750;

        // --- Class/method name strings ---
        int clsNameOff = stringAreaOff;
        byte[] clsNameBytes = AbcTestFixture.mutf8String("Ltest/Fallback;");
        bb.position(clsNameOff);
        bb.put(clsNameBytes);

        int methodNameOff = clsNameOff + clsNameBytes.length;
        byte[] methodNameBytes = AbcTestFixture.mutf8String("test");
        bb.position(methodNameOff);
        bb.put(methodNameBytes);

        // --- Literal array: numLiterals=4 => 2 entries (1 key-value pair) ---
        bb.position(literalArrayAreaOff);
        bb.putInt(4);
        // Entry 0: key - tag 0x03, value = string index 99 (unresolvable)
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(99));
        // Entry 1: value - tag 0x03, value = 1
        bb.put((byte) 0x03);
        bb.put(AbcTestFixture.le32(1));

        // --- Class ---
        bb.position(classAreaOff);
        bb.put(AbcTestFixture.mutf8String("Ltest/Fallback;"));
        bb.putInt(0);
        bb.put(AbcTestFixture.uleb128(0x0001));
        bb.put(AbcTestFixture.uleb128(0));
        bb.put(AbcTestFixture.uleb128(1));
        bb.put(new byte[] {0x00});

        int methodCodeOff = codeAreaOff;
        byte[] methodCode = AbcTestFixture.concat(
                new byte[] {0x07, 0x00},
                AbcTestFixture.le16(0),
                new byte[] {0x61, 0x00},
                new byte[] {0x60, 0x00},
                new byte[] {0x64}
        );

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(AbcTestFixture.uleb128(0x0001));
        bb.put(new byte[] {0x01});
        bb.putInt(methodCodeOff);
        bb.put(new byte[] {0x00});

        // --- Code ---
        bb.position(methodCodeOff);
        bb.put(AbcTestFixture.uleb128(4));
        bb.put(AbcTestFixture.uleb128(1));
        bb.put(AbcTestFixture.uleb128(methodCode.length));
        bb.put(AbcTestFixture.uleb128(0));
        bb.put(methodCode);

        // --- Region ---
        bb.position(regionHeaderOff);
        bb.putInt(classAreaOff);
        bb.putInt(codeAreaOff + 200);
        bb.putInt(1);
        bb.putInt(classRegionIdxOff);
        bb.putInt(1);
        bb.putInt(methodRegionIdxOff);
        bb.putInt(0);
        bb.putInt(fieldRegionIdxOff);
        bb.putInt(1);
        bb.putInt(protoIdxOff);

        // --- Index arrays ---
        bb.position(classIdxOff);
        bb.putInt(classAreaOff);

        bb.position(classRegionIdxOff);
        bb.putInt(classAreaOff);

        bb.position(methodRegionIdxOff);
        bb.putInt(0);

        bb.position(fieldRegionIdxOff);

        bb.position(protoIdxOff);
        bb.putShort((short) 0x0007);

        bb.position(literalArrayIdxOff);
        bb.putInt(literalArrayAreaOff);

        // Empty LNP index
        bb.position(lnpIdxOff);

        // --- Header ---
        bb.position(0);
        bb.put(new byte[] {'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[] {'0', '0', '0', '2'});
        bb.putInt(bufSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(1);
        bb.putInt(classIdxOff);
        bb.putInt(0);
        bb.putInt(lnpIdxOff);
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(0);
        byte[] data = new byte[bufSize];
        bb.get(data);
        return AbcFile.parse(data);
    }

    // =====================================================================
    // Tests for resolved key names
    // =====================================================================

    @Nested
    class ResolvedKeyTests {

        @Test
        void testLiteralArrayParsedCorrectly() {
            AbcFile abc = buildAbcWithObjectLiteral();
            List<AbcLiteralArray> arrays = abc.getLiteralArrays();
            assertEquals(1, arrays.size(),
                    "Should have exactly one literal array");
            AbcLiteralArray la = arrays.get(0);
            assertEquals(4, la.size(),
                    "Literal array should have 4 entries (2 key-value pairs)");
        }

        @Test
        void testStringTableResolved() {
            AbcFile abc = buildAbcWithObjectLiteral();
            String str0 = abc.resolveStringByIndex(0);
            String str1 = abc.resolveStringByIndex(1);
            assertEquals("name", str0,
                    "String index 0 should resolve to 'name'");
            assertEquals("age", str1,
                    "String index 1 should resolve to 'age'");
        }

        @Test
        void testObjectKeysResolvedFromStringTable() {
            AbcFile abc = buildAbcWithObjectLiteral();
            AbcClass cls = abc.getClasses().get(0);
            AbcMethod method = cls.getMethods().get(0);
            AbcCode code = abc.getCodeForMethod(method);
            assertNotNull(code);

            String result = decompiler.decompileMethod(method, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("name"),
                    "Output should contain resolved key 'name': " + result);
            assertTrue(result.contains("age"),
                    "Output should contain resolved key 'age': " + result);
            assertFalse(result.contains("key_0"),
                    "Should NOT contain placeholder key_0: " + result);
            assertFalse(result.contains("key_1"),
                    "Should NOT contain placeholder key_1: " + result);
        }

        @Test
        void testObjectKeysResolvedInDecompiledFile() {
            AbcFile abc = buildAbcWithObjectLiteral();
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertTrue(result.contains("name"),
                    "Full file decompilation should contain 'name': "
                            + result);
            assertTrue(result.contains("age"),
                    "Full file decompilation should contain 'age': "
                            + result);
        }
    }

    // =====================================================================
    // Tests for fallback to key_N placeholders
    // =====================================================================

    @Nested
    class FallbackKeyTests {

        @Test
        void testUnresolvableKeyFallsBackToPlaceholder() {
            AbcFile abc = buildAbcWithUnresolvableKey();
            AbcClass cls = abc.getClasses().get(0);
            AbcMethod method = cls.getMethods().get(0);
            AbcCode code = abc.getCodeForMethod(method);
            assertNotNull(code);

            String result = decompiler.decompileMethod(method, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("key_99"),
                    "Should contain fallback key_99 for unresolvable index: "
                            + result);
        }

        @Test
        void testNullAbcFileFallsBackGracefully() {
            // When decompileInstructions is used, there's no AbcFile
            // so the literal array data won't be available. This tests
            // the null-ctx path indirectly.
            byte[] code = AbcTestFixture.concat(
                    new byte[] {0x07, 0x00},
                    AbcTestFixture.le16(0),
                    new byte[] {0x61, 0x00},
                    new byte[] {0x64}
            );
            com.arkghidra.disasm.ArkDisassembler disasm =
                    new com.arkghidra.disasm.ArkDisassembler();
            List<com.arkghidra.disasm.ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // Tests for uint8 string index variant
    // =====================================================================

    @Nested
    class SmallIndexKeyTests {

        @Test
        void testUint8StringIndexResolution() {
            int bufSize = 4096;
            ByteBuffer bb = ByteBuffer.allocate(bufSize)
                    .order(ByteOrder.LITTLE_ENDIAN);

            int lnpAreaOff = 200;
            int stringAreaOff = 400;
            int classAreaOff = 700;
            int codeAreaOff = 1800;
            int literalArrayAreaOff = 2500;
            int regionHeaderOff = 3000;
            int classIdxOff = 3100;
            int methodRegionIdxOff = 3150;
            int fieldRegionIdxOff = 3170;
            int protoIdxOff = 3190;
            int classRegionIdxOff = 3210;
            int literalArrayIdxOff = 3230;
            int lnpIdxOff = 3250;

            // LNP string 0: "color" (null-terminated, no length prefix)
            int str0Off = lnpAreaOff;
            byte[] str0 = rawNullTerminated("color");
            bb.position(str0Off);
            bb.put(str0);

            // Class/method names
            int clsNameOff = stringAreaOff;
            byte[] clsName = AbcTestFixture.mutf8String("Ltest/Small;");
            bb.position(clsNameOff);
            bb.put(clsName);

            int methNameOff = clsNameOff + clsName.length;
            byte[] methName = AbcTestFixture.mutf8String("test");
            bb.position(methNameOff);
            bb.put(methName);

            // Literal array: numLiterals=4 => 2 entries (1 key-value pair)
            // Key uses uint8 tag (0x08) to encode string index 0
            bb.position(literalArrayAreaOff);
            bb.putInt(4);
            bb.put((byte) 0x08);
            bb.put((byte) 0);
            bb.put((byte) 0x03);
            bb.put(AbcTestFixture.le32(255));

            // Class
            bb.position(classAreaOff);
            bb.put(AbcTestFixture.mutf8String("Ltest/Small;"));
            bb.putInt(0);
            bb.put(AbcTestFixture.uleb128(0x0001));
            bb.put(AbcTestFixture.uleb128(0));
            bb.put(AbcTestFixture.uleb128(1));
            bb.put(new byte[] {0x00});

            int methodCodeOff = codeAreaOff;
            byte[] methodCode = AbcTestFixture.concat(
                    new byte[] {0x07, 0x00},
                    AbcTestFixture.le16(0),
                    new byte[] {0x61, 0x00},
                    new byte[] {0x60, 0x00},
                    new byte[] {0x64}
            );

            bb.putShort((short) 0);
            bb.putShort((short) 0);
            bb.putInt(methNameOff);
            bb.put(AbcTestFixture.uleb128(0x0001));
            bb.put(new byte[] {0x01});
            bb.putInt(methodCodeOff);
            bb.put(new byte[] {0x00});

            // Code
            bb.position(methodCodeOff);
            bb.put(AbcTestFixture.uleb128(4));
            bb.put(AbcTestFixture.uleb128(1));
            bb.put(AbcTestFixture.uleb128(methodCode.length));
            bb.put(AbcTestFixture.uleb128(0));
            bb.put(methodCode);

            // Region
            bb.position(regionHeaderOff);
            bb.putInt(classAreaOff);
            bb.putInt(codeAreaOff + 200);
            bb.putInt(1);
            bb.putInt(classRegionIdxOff);
            bb.putInt(1);
            bb.putInt(methodRegionIdxOff);
            bb.putInt(0);
            bb.putInt(fieldRegionIdxOff);
            bb.putInt(1);
            bb.putInt(protoIdxOff);

            // Index arrays
            bb.position(classIdxOff);
            bb.putInt(classAreaOff);
            bb.position(classRegionIdxOff);
            bb.putInt(classAreaOff);
            bb.position(methodRegionIdxOff);
            bb.putInt(0);
            bb.position(fieldRegionIdxOff);
            bb.position(protoIdxOff);
            bb.putShort((short) 0x0007);
            bb.position(literalArrayIdxOff);
            bb.putInt(literalArrayAreaOff);
            bb.position(lnpIdxOff);
            bb.putInt(str0Off);

            // Header
            bb.position(0);
            bb.put(new byte[] {'P', 'A', 'N', 'D', 'A', 0, 0, 0});
            bb.putInt(0);
            bb.put(new byte[] {'0', '0', '0', '2'});
            bb.putInt(bufSize);
            bb.putInt(0);
            bb.putInt(0);
            bb.putInt(1);
            bb.putInt(classIdxOff);
            bb.putInt(1);
            bb.putInt(lnpIdxOff);
            bb.putInt(1);
            bb.putInt(literalArrayIdxOff);
            bb.putInt(1);
            bb.putInt(regionHeaderOff);

            bb.position(0);
            byte[] data = new byte[bufSize];
            bb.get(data);
            AbcFile abc = AbcFile.parse(data);

            AbcClass cls = abc.getClasses().get(0);
            AbcMethod method = cls.getMethods().get(0);
            AbcCode code = abc.getCodeForMethod(method);

            String result = decompiler.decompileMethod(method, code, abc);
            assertNotNull(result);
            assertTrue(result.contains("color"),
                    "Should resolve uint8 string index to 'color': "
                            + result);
        }
    }
}
