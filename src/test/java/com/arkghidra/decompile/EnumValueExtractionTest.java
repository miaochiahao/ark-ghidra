package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for enum member value extraction and rendering.
 *
 * <p>Verifies that enum declarations with INT_VALUE tags render
 * numeric values (e.g., {@code RED = 1}) and enums without values
 * render as auto-increment (e.g., {@code RED}).
 */
class EnumValueExtractionTest {

    @Nested
    @DisplayName("Enum member rendering with values")
    class EnumMemberRendering {

        @Test
        @DisplayName("Enum with numeric values renders A = N format")
        void testEnumWithNumericValues() {
            List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                    List.of(
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "RED",
                                    new ArkTSExpression.LiteralExpression("1",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "GREEN",
                                    new ArkTSExpression.LiteralExpression("2",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "BLUE",
                                    new ArkTSExpression.LiteralExpression("3",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)));
            ArkTSTypeDeclarations.EnumDeclaration decl =
                    new ArkTSTypeDeclarations.EnumDeclaration("Color", members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("RED = 1"),
                    "Should have RED = 1: " + output);
            assertTrue(output.contains("GREEN = 2"),
                    "Should have GREEN = 2: " + output);
            assertTrue(output.contains("BLUE = 3"),
                    "Should have BLUE = 3: " + output);
        }

        @Test
        @DisplayName("Enum with mixed valued and unvalued members")
        void testEnumMixedValues() {
            List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                    List.of(
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "NONE", null),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "LOW",
                                    new ArkTSExpression.LiteralExpression("1",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "MEDIUM",
                                    new ArkTSExpression.LiteralExpression("2",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "HIGH",
                                    new ArkTSExpression.LiteralExpression("3",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)));
            ArkTSTypeDeclarations.EnumDeclaration decl =
                    new ArkTSTypeDeclarations.EnumDeclaration("Priority",
                            members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("NONE"),
                    "Should have NONE: " + output);
            assertFalse(output.contains("NONE ="),
                    "NONE should not have explicit value: " + output);
            assertTrue(output.contains("LOW = 1"),
                    "Should have LOW = 1: " + output);
        }

        @Test
        @DisplayName("Enum with HTTP status codes")
        void testHttpStatusEnum() {
            List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                    List.of(
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "OK",
                                    new ArkTSExpression.LiteralExpression("200",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "NOT_FOUND",
                                    new ArkTSExpression.LiteralExpression("404",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "INTERNAL_ERROR",
                                    new ArkTSExpression.LiteralExpression("500",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)));
            ArkTSTypeDeclarations.EnumDeclaration decl =
                    new ArkTSTypeDeclarations.EnumDeclaration("HttpStatus",
                            members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("OK = 200"), output);
            assertTrue(output.contains("NOT_FOUND = 404"), output);
            assertTrue(output.contains("INTERNAL_ERROR = 500"), output);
        }

        @Test
        @DisplayName("EnumMember value accessor returns correct expression")
        void testEnumMemberValueAccessor() {
            ArkTSExpression value =
                    new ArkTSExpression.LiteralExpression("42",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSTypeDeclarations.EnumDeclaration.EnumMember member =
                    new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                            "ANSWER", value);
            assertEquals("ANSWER", member.getName());
            assertEquals(value, member.getValue());
            assertEquals("42", member.getValue().toArkTS());
        }

        @Test
        @DisplayName("EnumMember with null value accessor")
        void testEnumMemberNullValue() {
            ArkTSTypeDeclarations.EnumDeclaration.EnumMember member =
                    new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                            "AUTO", null);
            assertEquals("AUTO", member.getName());
            assertNull(member.getValue());
        }

        @Test
        @DisplayName("Enum with negative values")
        void testEnumWithNegativeValues() {
            List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                    List.of(
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "ERROR",
                                    new ArkTSExpression.LiteralExpression("-1",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "SUCCESS",
                                    new ArkTSExpression.LiteralExpression("0",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)));
            ArkTSTypeDeclarations.EnumDeclaration decl =
                    new ArkTSTypeDeclarations.EnumDeclaration("Result", members);
            String output = decl.toArkTS(0);
            assertTrue(output.contains("ERROR = -1"), output);
            assertTrue(output.contains("SUCCESS = 0"), output);
        }

        @Test
        @DisplayName("Enum starts with 'enum' keyword")
        void testEnumKeywordRendering() {
            List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                    List.of(
                            new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                                    "A",
                                    new ArkTSExpression.LiteralExpression("1",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)));
            ArkTSTypeDeclarations.EnumDeclaration decl =
                    new ArkTSTypeDeclarations.EnumDeclaration("Test", members);
            String output = decl.toArkTS(0);
            assertTrue(output.startsWith("enum Test {"),
                    "Should start with 'enum Test {': " + output);
        }
    }
}
