package com.arkghidra.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.decompile.ArkTSDeclarations;
import org.junit.jupiter.api.Test;

/**
 * Tests for ABC import/export module record parsing and decompiler integration.
 *
 * <p>Builds synthetic .abc binaries containing module record LiteralArrays
 * and verifies that the parser correctly extracts import/export metadata
 * and the decompiler generates proper ArkTS import/export statements.
 */
class AbcImportExportTest {

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
        byte[] strBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] header = uleb128(((long) s.length() << 1) | 1);
        byte[] result = new byte[header.length + strBytes.length + 1];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(strBytes, 0, result, header.length, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    /**
     * Builds a minimal ABC binary with a module record LiteralArray.
     *
     * <p>The module record contains:
     * <ul>
     *   <li>1 module request: "@ohos/entry"</li>
     *   <li>1 regular import: import { doSomething as myFunc } from '@ohos/entry'</li>
     *   <li>1 local export: export { MyClass }</li>
     * </ul>
     */
    private static byte[] buildAbcWithModuleRecord() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int stringArea2Off = 350;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

        // Header
        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0); // checksum
        bb.put(new byte[]{'0', '0', '0', '2'}); // version
        bb.putInt(fileSize);
        bb.putInt(0); // foreign_off
        bb.putInt(0); // foreign_size
        bb.putInt(1); // num_classes
        bb.putInt(classIdxOff);
        bb.putInt(0); // num_lnps
        bb.putInt(lnpIdxOff);
        bb.putInt(1); // num_literalarrays
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1); // num_index_regions
        bb.putInt(regionHeaderOff);

        // String area (primary - for class/method/import/export names)
        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/MyClass;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("testMethod"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("@ohos/entry"));
        int importNameOff = bb.position();
        bb.put(mutf8String("doSomething"));
        int localNameOff = bb.position();
        bb.put(mutf8String("myFunc"));
        int exportNameOff = bb.position();
        bb.put(mutf8String("MyClass"));

        // Second string area for field names (away from class data)
        bb.position(stringArea2Off);
        int dummyFieldNameOff = bb.position();
        bb.put(mutf8String("dummyField"));

        // Class index
        bb.position(classIdxOff);
        bb.putInt(classOff);

        // Literal array index
        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

        // Region header
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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record literal array
        bb.position(moduleRecordLaOff);
        int numLiterals = 14;
        bb.putInt(numLiterals);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(1); // regular_import_num
        bb.putInt(localNameOff);
        bb.putInt(importNameOff);
        bb.putShort((short) 0);
        bb.putInt(0); // namespace_import_num
        bb.putInt(1); // local_export_num
        bb.putInt(classNameOff);
        bb.putInt(exportNameOff);
        bb.putInt(0); // indirect_export_num
        bb.putInt(0); // star_export_num

        // Class definition
        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/MyClass;"));
        bb.putInt(0); // super_class_off
        bb.put(uleb128(0x0001)); // access_flags
        bb.put(uleb128(2)); // num_fields
        bb.put(uleb128(1)); // num_methods
        bb.put(new byte[]{0x00}); // class tag END

        // Field 1: moduleRecordIdx
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01); // INT_VALUE tag
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00); // END tag

        // Field 2: dummy field
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(dummyFieldNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x00); // END tag

        // Method
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code
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

    @Test
    void testParseModuleRecord_basicFields() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);

        assertEquals(1, abc.getClasses().size());
        assertEquals(1, abc.getModuleRecordCount());

        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);
        assertEquals(1, record.getModuleRequestOffsets().size());
        assertEquals(1, record.getRegularImports().size());
        assertEquals(0, record.getNamespaceImports().size());
        assertEquals(1, record.getLocalExports().size());
        assertEquals(0, record.getIndirectExports().size());
        assertEquals(0, record.getStarExports().size());
    }

    @Test
    void testParseModuleRecord_moduleRequestsResolved() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        List<String> moduleRequests = abc.resolveModuleRequests(record);
        assertEquals(1, moduleRequests.size());
        assertEquals("@ohos/entry", moduleRequests.get(0));
    }

    @Test
    void testParseModuleRecord_regularImport() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        AbcModuleRecord.RegularImport ri = record.getRegularImports().get(0);
        assertEquals(0, ri.getModuleRequestIdx());

        String importName = abc.getSourceFileName(ri.getImportNameOffset());
        String localName = abc.getSourceFileName(ri.getLocalNameOffset());
        assertEquals("doSomething", importName);
        assertEquals("myFunc", localName);
    }

    @Test
    void testParseModuleRecord_localExport() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        AbcModuleRecord.LocalExport le = record.getLocalExports().get(0);
        String exportName = abc.getSourceFileName(le.getExportNameOffset());
        assertEquals("MyClass", exportName);
    }

    @Test
    void testParseModuleRecord_fieldModuleRecordIdx() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);

        AbcClass cls = abc.getClasses().get(0);
        boolean foundModuleRecordField = false;
        for (AbcField field : cls.getFields()) {
            if (field.isModuleRecordIdx()) {
                foundModuleRecordField = true;
                assertTrue(field.getIntValue() > 0);
            }
        }
        assertTrue(foundModuleRecordField, "Should find moduleRecordIdx field");
    }

    @Test
    void testModuleRecord_noModuleRecord_returnsNull() {
        byte[] data = buildMinimalAbc();
        AbcFile abc = AbcFile.parse(data);
        assertNull(abc.getModuleRecord(0));
        assertEquals(0, abc.getModuleRecordCount());
    }

    @Test
    void testGetAllModuleRecords() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);
        Map<Integer, AbcModuleRecord> allRecords = abc.getAllModuleRecords();
        assertEquals(1, allRecords.size());
        assertTrue(allRecords.containsKey(0));
    }

    @Test
    void testDecompilerGeneratesImportStatements() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("import"),
                "Output should contain import statement, got: " + output);
        assertTrue(output.contains("@ohos/entry"),
                "Output should contain module path, got: " + output);
    }

    @Test
    void testDecompilerGeneratesExportStatements() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("export"),
                "Output should contain export statement, got: " + output);
    }

    @Test
    void testFieldIsModuleRecordIdx() {
        AbcField normalField = new AbcField(0, 0, "normalField", 1, 100);
        assertFalse(normalField.isModuleRecordIdx());
        assertEquals(0, normalField.getIntValue());

        AbcField mrField = new AbcField(0, 0, "moduleRecordIdx", 1, 100, 500);
        assertTrue(mrField.isModuleRecordIdx());
        assertEquals(500, mrField.getIntValue());
    }

    // --- ABC with namespace import ---

    /**
     * Builds an ABC with a namespace import (import * as lib from 'module').
     */
    private static byte[] buildAbcWithNamespaceImport() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/App;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("run"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("@ohos/lib"));
        int namespaceNameOff = bb.position();
        bb.put(mutf8String("lib"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 1 namespace import
        bb.position(moduleRecordLaOff);
        bb.putInt(8);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff); // module request string offset
        bb.putInt(0); // regular_import_num
        bb.putInt(1); // namespace_import_num
        bb.putInt(namespaceNameOff); // local_name_off
        bb.putShort((short) 0); // module_request_idx
        bb.putInt(0); // local_export_num
        bb.putInt(0); // indirect_export_num
        bb.putInt(0); // star_export_num

        // Class
        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/App;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1)); // 1 field
        bb.put(uleb128(1)); // 1 method
        bb.put(new byte[]{0x00}); // class tag END

        // Field: moduleRecordIdx
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01); // tag = INT_VALUE
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00); // tag = END

        // Method
        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        // Code
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

    @Test
    void testParseModuleRecord_namespaceImport() {
        byte[] data = buildAbcWithNamespaceImport();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        assertEquals(0, record.getRegularImports().size());
        assertEquals(1, record.getNamespaceImports().size());

        AbcModuleRecord.NamespaceImport ni = record.getNamespaceImports().get(0);
        assertEquals(0, ni.getModuleRequestIdx());
        String localName = abc.getSourceFileName(ni.getLocalNameOffset());
        assertEquals("lib", localName);
    }

    @Test
    void testDecompilerNamespaceImport() {
        byte[] data = buildAbcWithNamespaceImport();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("import * as lib"),
                "Output should contain namespace import, got: " + output);
        assertTrue(output.contains("@ohos/lib"),
                "Output should contain module path, got: " + output);
    }

    // --- ABC with star export ---

    /**
     * Builds an ABC with a star export (export * from 'module').
     */
    private static byte[] buildAbcWithStarExport() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/ReExport;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("init"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("@ohos/core"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 1 star export
        bb.position(moduleRecordLaOff);
        bb.putInt(6);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(0); // regular_import_num
        bb.putInt(0); // namespace_import_num
        bb.putInt(0); // local_export_num
        bb.putInt(0); // indirect_export_num
        bb.putInt(1); // star_export_num
        bb.putShort((short) 0); // module_request_idx

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/ReExport;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01);
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00);

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

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

    @Test
    void testParseModuleRecord_starExport() {
        byte[] data = buildAbcWithStarExport();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        assertEquals(1, record.getStarExports().size());
        assertEquals(0, record.getStarExports().get(0).getModuleRequestIdx());
        assertEquals(0, record.getRegularImports().size());
        assertEquals(0, record.getLocalExports().size());
    }

    @Test
    void testParseModuleRecord_indirectExport() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/Wrapper;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("wrap"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("@ohos/util"));
        int exportNameOff = bb.position();
        bb.put(mutf8String("helper"));
        int importNameOff = bb.position();
        bb.put(mutf8String("internalHelper"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 1 indirect export
        bb.position(moduleRecordLaOff);
        bb.putInt(10);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(0); // regular_import_num
        bb.putInt(0); // namespace_import_num
        bb.putInt(0); // local_export_num
        bb.putInt(1); // indirect_export_num
        bb.putInt(exportNameOff); // export_name_off
        bb.putInt(importNameOff); // import_name_off
        bb.putShort((short) 0); // module_request_idx
        bb.putInt(0); // star_export_num

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/Wrapper;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01);
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00);

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

        bb.position(codeOff);
        bb.put(uleb128(2));
        bb.put(uleb128(1));
        bb.put(uleb128(4));
        bb.put(uleb128(0));
        bb.put(new byte[]{0x00, 0x60, 0x00, 0x64});

        bb.position(0);
        byte[] data = new byte[fileSize];
        bb.get(data);

        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        assertEquals(1, record.getIndirectExports().size());
        AbcModuleRecord.IndirectExport ie = record.getIndirectExports().get(0);
        assertEquals("helper", abc.getSourceFileName(ie.getExportNameOffset()));
        assertEquals("internalHelper", abc.getSourceFileName(ie.getImportNameOffset()));
        assertEquals(0, ie.getModuleRequestIdx());
    }

    // --- Minimal ABC without module record ---

    private static byte[] buildMinimalAbc() {
        ByteBuffer bb = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 256;
        int codeOff = 350;
        int regionHeaderOff = 360;
        int classIdxOff = 400;
        int literalArrayIdxOff = 408;
        int lnpIdxOff = 416;
        int classRegionIdxOff = 420;
        int fileSize = 512;

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

        bb.position(classIdxOff);
        bb.putInt(classOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        bb.position(stringAreaOff);
        byte[] className = mutf8String("Lcom/example/TestClass;");
        bb.put(className);
        int methodNameStringOff = bb.position();
        bb.put(mutf8String("testMethod"));

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/TestClass;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(0));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameStringOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

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

    @Test
    void testModuleRecord_caching() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);

        AbcModuleRecord record1 = abc.getModuleRecord(0);
        AbcModuleRecord record2 = abc.getModuleRecord(0);
        assertNotNull(record1);
        assertSame(record1, record2);
    }

    @Test
    void testModuleRecord_invalidClassIdx() {
        byte[] data = buildAbcWithModuleRecord();
        AbcFile abc = AbcFile.parse(data);
        assertNull(abc.getModuleRecord(99));
    }

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same instance but got different objects");
        }
    }

    // --- Star export decompilation ---

    @Test
    void testDecompilerStarExport() {
        byte[] data = buildAbcWithStarExport();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("export * from '@ohos/core'"),
                "Output should contain star export, got: " + output);
    }

    // --- ABC with default import ---

    /**
     * Builds an ABC with a default import (import React from 'react').
     */
    private static byte[] buildAbcWithDefaultImport() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/App;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("render"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("react"));
        int importNameOff = bb.position();
        bb.put(mutf8String("default"));
        int localNameOff = bb.position();
        bb.put(mutf8String("React"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 1 regular import with importName = "default"
        bb.position(moduleRecordLaOff);
        bb.putInt(10);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(1); // regular_import_num
        bb.putInt(localNameOff); // local_name_off = "React"
        bb.putInt(importNameOff); // import_name_off = "default"
        bb.putShort((short) 0); // module_request_idx
        bb.putInt(0); // namespace_import_num
        bb.putInt(0); // local_export_num
        bb.putInt(0); // indirect_export_num
        bb.putInt(0); // star_export_num

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/App;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01);
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00);

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

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

    @Test
    void testParseModuleRecord_defaultImport() {
        byte[] data = buildAbcWithDefaultImport();
        AbcFile abc = AbcFile.parse(data);
        AbcModuleRecord record = abc.getModuleRecord(0);
        assertNotNull(record);

        assertEquals(1, record.getRegularImports().size());
        AbcModuleRecord.RegularImport ri = record.getRegularImports().get(0);
        String importName = abc.getSourceFileName(ri.getImportNameOffset());
        String localName = abc.getSourceFileName(ri.getLocalNameOffset());
        assertEquals("default", importName);
        assertEquals("React", localName);
    }

    @Test
    void testDecompilerDefaultImport() {
        byte[] data = buildAbcWithDefaultImport();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("import React"),
                "Output should contain default import, got: " + output);
        assertTrue(output.contains("from 'react'"),
                "Output should contain module path, got: " + output);
    }

    // --- ABC with re-export (indirect export with from clause) ---

    /**
     * Builds an ABC with indirect export that includes the from clause.
     */
    private static byte[] buildAbcWithReExport() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/Index;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("init"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("./utils"));
        int exportNameOff = bb.position();
        bb.put(mutf8String("helper"));
        int importNameOff = bb.position();
        bb.put(mutf8String("internalHelper"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 1 indirect export
        bb.position(moduleRecordLaOff);
        bb.putInt(10);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(0); // regular_import_num
        bb.putInt(0); // namespace_import_num
        bb.putInt(0); // local_export_num
        bb.putInt(1); // indirect_export_num
        bb.putInt(exportNameOff);
        bb.putInt(importNameOff);
        bb.putShort((short) 0);
        bb.putInt(0); // star_export_num

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/Index;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01);
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00);

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

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

    @Test
    void testDecompilerReExport_withFromClause() {
        byte[] data = buildAbcWithReExport();
        AbcFile abc = AbcFile.parse(data);

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        String output = decompiler.decompileFile(abc);

        assertTrue(output.contains("export"),
                "Output should contain export statement, got: " + output);
        assertTrue(output.contains("from './utils'"),
                "Output should contain from clause, got: " + output);
        assertTrue(output.contains("internalHelper"),
                "Output should contain imported name, got: " + output);
    }

    // --- ABC with mixed imports from same module ---

    /**
     * Builds an ABC with both named and namespace imports from
     * the same module to test import merging.
     */
    private static byte[] buildAbcWithMixedImports() {
        ByteBuffer bb = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);

        int stringAreaOff = 200;
        int classOff = 400;
        int codeOff = 550;
        int regionHeaderOff = 600;
        int classIdxOff = 660;
        int literalArrayIdxOff = 670;
        int lnpIdxOff = 680;
        int classRegionIdxOff = 690;
        int moduleRecordLaOff = 700;
        int fileSize = 4096;

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
        bb.putInt(1);
        bb.putInt(literalArrayIdxOff);
        bb.putInt(1);
        bb.putInt(regionHeaderOff);

        bb.position(stringAreaOff);
        int classNameOff = bb.position();
        bb.put(mutf8String("Lcom/example/Mixed;"));
        int methodNameOff = bb.position();
        bb.put(mutf8String("run"));
        int moduleRecordIdxNameOff = bb.position();
        bb.put(mutf8String("moduleRecordIdx"));
        int modulePathOff = bb.position();
        bb.put(mutf8String("@ohos/lib"));
        int importName1Off = bb.position();
        bb.put(mutf8String("foo"));
        int localName1Off = bb.position();
        bb.put(mutf8String("foo"));
        int importName2Off = bb.position();
        bb.put(mutf8String("Bar"));
        int localName2Off = bb.position();
        bb.put(mutf8String("myBar"));
        int nsNameOff = bb.position();
        bb.put(mutf8String("lib"));

        bb.position(classIdxOff);
        bb.putInt(classOff);

        bb.position(literalArrayIdxOff);
        bb.putInt(moduleRecordLaOff);

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

        bb.position(classRegionIdxOff);
        bb.putInt(classOff);

        // Module record: 2 regular imports + 1 namespace import, all from same module
        bb.position(moduleRecordLaOff);
        bb.putInt(8);
        bb.putInt(1); // num_module_requests
        bb.putInt(modulePathOff);
        bb.putInt(2); // regular_import_num
        // Regular import 1: import { foo }
        bb.putInt(localName1Off);
        bb.putInt(importName1Off);
        bb.putShort((short) 0);
        // Regular import 2: import { Bar as myBar }
        bb.putInt(localName2Off);
        bb.putInt(importName2Off);
        bb.putShort((short) 0);
        bb.putInt(1); // namespace_import_num
        bb.putInt(nsNameOff);
        bb.putShort((short) 0);
        bb.putInt(0); // local_export_num
        bb.putInt(0); // indirect_export_num
        bb.putInt(0); // star_export_num

        bb.position(classOff);
        bb.put(mutf8String("Lcom/example/Mixed;"));
        bb.putInt(0);
        bb.put(uleb128(0x0001));
        bb.put(uleb128(1));
        bb.put(uleb128(1));
        bb.put(new byte[]{0x00});

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(moduleRecordIdxNameOff);
        bb.put(uleb128(0x0001));
        bb.put((byte) 0x01);
        bb.put(uleb128(moduleRecordLaOff));
        bb.put((byte) 0x00);

        bb.putShort((short) 0);
        bb.putShort((short) 0);
        bb.putInt(methodNameOff);
        bb.put(uleb128(0x0001));
        bb.put(new byte[]{0x01});
        bb.putInt(codeOff);
        bb.put(new byte[]{0x00});

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

    @Test
    void testDecompilerMixedImports_sameModuleMerged() {
        // Test the ModuleImportCollector merging logic directly via AST
        ArkTSDecompiler.ModuleImportCollector collector =
                new ArkTSDecompiler.ModuleImportCollector();
        collector.addNamedImport("foo", "foo");
        collector.addNamedImport("Bar", "myBar");
        collector.setNamespaceImport("lib");

        List<ArkTSDeclarations.ImportStatement> stmts =
                collector.toImportStatements("@ohos/lib");
        StringBuilder sb = new StringBuilder();
        for (ArkTSDeclarations.ImportStatement stmt : stmts) {
            sb.append(stmt.toArkTS(0)).append("\n");
        }
        String output = sb.toString().trim();

        assertTrue(output.contains("@ohos/lib"),
                "Output should contain module path, got: " + output);
        assertTrue(output.contains("foo"),
                "Output should contain named import foo, got: " + output);
        assertTrue(output.contains("Bar as myBar"),
                "Output should contain aliased import, got: " + output);
        assertTrue(output.contains("lib"),
                "Output should contain namespace import, got: " + output);
    }
}
