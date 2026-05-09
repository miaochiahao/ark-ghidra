package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import com.arkghidra.format.AbcProto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for interface detection and rendering.
 */
class InterfaceDetectionTest {

    @Nested
    @DisplayName("Interface AST rendering")
    class InterfaceRendering {

        @Test
        @DisplayName("Simple interface renders correctly")
        void testSimpleInterfaceRendering() {
            List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember>
                    members = List.of(
                            new ArkTSTypeDeclarations.InterfaceDeclaration
                                    .InterfaceMember("method", "getName",
                                    "string", Collections.emptyList(),
                                    false),
                            new ArkTSTypeDeclarations.InterfaceDeclaration
                                    .InterfaceMember("method", "setValue",
                                    "void",
                                    List.of(new ArkTSDeclarations
                                            .FunctionDeclaration.FunctionParam(
                                            "param_0", "number")),
                                    false));

            ArkTSTypeDeclarations.InterfaceDeclaration decl =
                    new ArkTSTypeDeclarations.InterfaceDeclaration(
                            "Serializable", Collections.emptyList(),
                            members);
            String output = decl.toArkTS(0);
            assertTrue(output.startsWith("interface Serializable"),
                    "Should start with 'interface Serializable': " + output);
            assertTrue(output.contains("getName"),
                    "Should contain getName: " + output);
            assertTrue(output.contains("setValue"),
                    "Should contain setValue: " + output);
            assertTrue(output.contains("string"),
                    "Should contain string return type: " + output);
        }

        @Test
        @DisplayName("Interface with extends renders correctly")
        void testInterfaceExtends() {
            ArkTSTypeDeclarations.InterfaceDeclaration decl =
                    new ArkTSTypeDeclarations.InterfaceDeclaration(
                            "Derived",
                            List.of("Base", "Another"),
                            Collections.emptyList());
            String output = decl.toArkTS(0);
            assertTrue(output.contains("extends Base, Another"),
                    "Should contain extends: " + output);
        }

        @Test
        @DisplayName("Interface with optional members")
        void testInterfaceOptional() {
            List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember>
                    members = List.of(
                            new ArkTSTypeDeclarations.InterfaceDeclaration
                                    .InterfaceMember("property", "name",
                                    "string", Collections.emptyList(),
                                    true));

            ArkTSTypeDeclarations.InterfaceDeclaration decl =
                    new ArkTSTypeDeclarations.InterfaceDeclaration(
                            "Options", Collections.emptyList(), members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("name?:"),
                    "Should contain optional marker: " + output);
        }

        @Test
        @DisplayName("Interface with typed parameters")
        void testInterfaceTypedParams() {
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                    List.of(
                            new ArkTSDeclarations.FunctionDeclaration
                                    .FunctionParam("param_0", "number"),
                            new ArkTSDeclarations.FunctionDeclaration
                                    .FunctionParam("param_1", "string"));

            List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember>
                    members = List.of(
                            new ArkTSTypeDeclarations.InterfaceDeclaration
                                    .InterfaceMember("method", "process",
                                    "boolean", params, false));

            ArkTSTypeDeclarations.InterfaceDeclaration decl =
                    new ArkTSTypeDeclarations.InterfaceDeclaration(
                            "Processor", Collections.emptyList(), members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("process("),
                    "Should contain method: " + output);
            assertTrue(output.contains("param_0: number"),
                    "Should contain typed param: " + output);
            assertTrue(output.contains("param_1: string"),
                    "Should contain typed param: " + output);
            assertTrue(output.contains("): boolean"),
                    "Should contain return type: " + output);
        }
    }

    @Nested
    @DisplayName("Interface type inference")
    class InterfaceTypeInference {

        @Test
        @DisplayName("VOID shorty maps to void")
        void testVoidShorty() {
            AbcProto proto = new AbcProto(
                    List.of(AbcProto.ShortyType.VOID), Collections.emptyList());
            String result = inferReturnType(proto);
            assertEquals("void", result);
        }

        @Test
        @DisplayName("I32 shorty maps to number")
        void testI32Shorty() {
            AbcProto proto = new AbcProto(
                    List.of(AbcProto.ShortyType.I32, AbcProto.ShortyType.I32),
                    Collections.emptyList());
            String result = inferReturnType(proto);
            assertEquals("number", result);
        }

        @Test
        @DisplayName("U1 shorty maps to boolean")
        void testU1Shorty() {
            AbcProto proto = new AbcProto(
                    List.of(AbcProto.ShortyType.U1), Collections.emptyList());
            String result = inferReturnType(proto);
            assertEquals("boolean", result);
        }

        @Test
        @DisplayName("REF shorty maps to null (untyped)")
        void testRefShorty() {
            AbcProto proto = new AbcProto(
                    List.of(AbcProto.ShortyType.REF), Collections.emptyList());
            String result = inferReturnType(proto);
            assertEquals(null, result);
        }

        @Test
        @DisplayName("Null proto returns null")
        void testNullProto() {
            String result = inferReturnType(null);
            assertEquals(null, result);
        }
    }

    /**
     * Access package-private inferReturnTypeFromProto.
     */
    private static String inferReturnType(AbcProto proto) {
        try {
            java.lang.reflect.Method m =
                    DeclarationBuilder.class.getDeclaredMethod(
                            "inferReturnTypeFromProto", AbcProto.class);
            m.setAccessible(true);
            DeclarationBuilder db = new DeclarationBuilder(new ArkTSDecompiler());
            return (String) m.invoke(db, proto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
