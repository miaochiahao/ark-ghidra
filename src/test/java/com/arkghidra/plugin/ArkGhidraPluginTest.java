package com.arkghidra.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcField;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Tests for the ArkTS Ghidra plugin UI components.
 *
 * <p>Since Ghidra Plugin tests require full Application initialization,
 * these tests focus on static logic, constants, and pure data operations
 * that do not require a running Ghidra environment.</p>
 */
class ArkGhidraPluginTest {

    @Test
    @DisplayName("Plugin name constant is correct")
    void testPluginName_isCorrect() {
        assertEquals("ArkTS Decompiler", ArkGhidraPlugin.PLUGIN_NAME);
    }

    @Test
    @DisplayName("DecompileToArkTSAction action name is non-null")
    void testDecompileActionName_isNonNull() {
        assertNotNull(DecompileToArkTSAction.ACTION_NAME);
        assertFalse(DecompileToArkTSAction.ACTION_NAME.isEmpty());
        assertEquals("Decompile to ArkTS",
                DecompileToArkTSAction.ACTION_NAME);
    }

    @Test
    @DisplayName("ShowAbcStructureAction action name is non-null")
    void testShowStructureActionName_isNonNull() {
        assertNotNull(ShowAbcStructureAction.ACTION_NAME);
        assertFalse(ShowAbcStructureAction.ACTION_NAME.isEmpty());
        assertEquals("Show ABC Structure",
                ShowAbcStructureAction.ACTION_NAME);
    }

    @Test
    @DisplayName("findMethodAtOffset returns correct method when found")
    void testFindMethodAtOffset_found() {
        AbcTestFixture fixture = new AbcTestFixture();
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abcFile = AbcFile.parse(data);

        AbcMethod found = DecompileToArkTSAction.findMethodAtOffset(
                abcFile, 1600);
        assertNotNull(found);
        assertEquals("<init>", found.getName());
    }

    @Test
    @DisplayName("findMethodAtOffset returns null when no method at offset")
    void testFindMethodAtOffset_notFound() {
        AbcTestFixture fixture = new AbcTestFixture();
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abcFile = AbcFile.parse(data);

        AbcMethod found = DecompileToArkTSAction.findMethodAtOffset(
                abcFile, 0xFFFF);
        assertNull(found);
    }

    @Test
    @DisplayName("AbcStructureProvider formatClassName handles L-prefix")
    void testFormatClassName_lprefix() {
        assertEquals("com.example.MyClass",
                AbcStructureProvider.formatClassName(
                        "Lcom/example/MyClass;"));
    }

    @Test
    @DisplayName("AbcStructureProvider formatClassName handles null")
    void testFormatClassName_null() {
        assertEquals("<unnamed>",
                AbcStructureProvider.formatClassName(null));
    }

    @Test
    @DisplayName("AbcStructureProvider formatClassName handles empty")
    void testFormatClassName_empty() {
        assertEquals("<unnamed>",
                AbcStructureProvider.formatClassName(""));
    }

    @Test
    @DisplayName("AbcStructureProvider formatClassName handles plain name")
    void testFormatClassName_plain() {
        assertEquals("MyClass",
                AbcStructureProvider.formatClassName("LMyClass;"));
    }

    @Test
    @DisplayName("DecompileToArkTSAction readAbcData returns null for null program")
    void testReadAbcData_nullProgram() {
        assertNull(DecompileToArkTSAction.readAbcData(null));
    }

    @Test
    @DisplayName("AbcStructureProvider class structure holds methods and fields")
    void testAbcClassStructure() {
        AbcMethod method1 =
                new AbcMethod(0, 0, "method1", 0, 0x50, 0);
        AbcMethod method2 =
                new AbcMethod(0, 1, "method2", 0, 0, 1);
        AbcField field1 =
                new AbcField(0, 0, "field1", 0, 0);
        AbcClass cls = new AbcClass("LTestClass;", 0, 1,
                Collections.singletonList(field1),
                java.util.List.of(method1, method2), 0);

        assertEquals("LTestClass;", cls.getName());
        assertEquals(2, cls.getMethods().size());
        assertEquals(1, cls.getFields().size());
        assertEquals("method1", cls.getMethods().get(0).getName());
        assertEquals("method2", cls.getMethods().get(1).getName());
        assertEquals("field1", cls.getFields().get(0).getName());
    }

    @Test
    @DisplayName("Comprehensive ABC fixture has correct class count")
    void testFixtureClassCount() {
        AbcTestFixture fixture = new AbcTestFixture();
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abcFile = AbcFile.parse(data);

        assertEquals(2, abcFile.getClasses().size());
        assertTrue(abcFile.getHeader().hasValidMagic());
    }

    @Test
    @DisplayName("Comprehensive ABC fixture methods have code offsets")
    void testFixtureMethodCodeOffsets() {
        AbcTestFixture fixture = new AbcTestFixture();
        byte[] data = fixture.buildComprehensiveAbc();
        AbcFile abcFile = AbcFile.parse(data);

        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod method : cls.getMethods()) {
                assertTrue(method.getCodeOff() > 0,
                        "Method " + method.getName()
                                + " should have a non-zero code offset");
            }
        }
    }
}
