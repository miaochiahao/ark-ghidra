package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for object literal shorthand rendering.
 */
class ObjectShorthandTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    @Nested
    @DisplayName("Object shorthand detection")
    class ShorthandDetection {

        @Test
        @DisplayName("Matching key-value uses shorthand")
        void testMatchingKeyValueUsesShorthand() {
            ArkTSAccessExpressions.ObjectLiteralExpression obj =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            List.of(
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "name", var("name")),
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "age", var("age"))));

            String output = obj.toArkTS();
            assertEquals("{ name, age }", output);
        }

        @Test
        @DisplayName("Non-matching key-value uses full syntax")
        void testNonMatchingKeyValueUsesFullSyntax() {
            ArkTSAccessExpressions.ObjectLiteralExpression obj =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            List.of(
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "name",
                                            lit("Alice",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)),
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "count",
                                            lit("42",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER))));

            String output = obj.toArkTS();
            assertTrue(output.contains("name:"),
                    "Should use full syntax for non-matching: " + output);
            assertTrue(output.contains("count:"),
                    "Should use full syntax for non-matching: " + output);
        }

        @Test
        @DisplayName("Mixed shorthand and full syntax")
        void testMixedShorthandAndFull() {
            ArkTSAccessExpressions.ObjectLiteralExpression obj =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            List.of(
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "name", var("name")),
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            "value",
                                            lit("42",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER))));

            String output = obj.toArkTS();
            assertTrue(output.contains("name"),
                    "Should contain 'name': " + output);
            assertTrue(output.contains("value:"),
                    "Should use full syntax for 'value': " + output);
            assertTrue(output.contains("name,"),
                    "Should use shorthand for 'name': " + output);
        }

        @Test
        @DisplayName("Computed key not affected by shorthand")
        void testComputedKeyNotAffected() {
            ArkTSAccessExpressions.ObjectLiteralExpression obj =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            List.of(
                                    new ArkTSAccessExpressions
                                            .ObjectLiteralExpression.ObjectProperty(
                                            var("key"), var("key"), true)));

            String output = obj.toArkTS();
            assertTrue(output.contains("[key]: key"),
                    "Computed key should use full syntax: " + output);
        }
    }
}
