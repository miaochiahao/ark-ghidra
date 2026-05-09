package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSAccessExpressions.ConditionalExpression;

/**
 * Tests for logical ternary simplification:
 * {@code cond ? value : undefined} -> {@code cond && value}.
 */
@DisplayName("Logical ternary simplification")
class LogicalTernaryTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    @Nested
    @DisplayName("Ternary to logical AND")
    class TernaryToAnd {

        @Test
        @DisplayName("cond ? value : undefined → cond && value")
        void testUndefinedBranchToAnd() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"),
                    var("value"),
                    lit("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .UNDEFINED));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression,
                    "Should be BinaryExpression: " + result.toArkTS());
            assertEquals("x && value", result.toArkTS());
        }

        @Test
        @DisplayName("cond ? value : null → cond && value")
        void testNullBranchToAnd() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"),
                    var("value"),
                    lit("null",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NULL));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression,
                    "Should be BinaryExpression: " + result.toArkTS());
            assertEquals("x && value", result.toArkTS());
        }

        @Test
        @DisplayName("cond ? undefined : value → !cond && value")
        void testUndefinedThenBranch() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"),
                    lit("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .UNDEFINED),
                    var("value"));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression,
                    "Should be BinaryExpression: " + result.toArkTS());
            String output = result.toArkTS();
            assertTrue(output.contains("&&"),
                    "Should contain &&: " + output);
            assertTrue(output.contains("!x"),
                    "Should contain !x: " + output);
            assertTrue(output.contains("value"),
                    "Should contain value: " + output);
        }

        @Test
        @DisplayName("cond ? null : value → !cond && value")
        void testNullThenBranch() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("flag"),
                    lit("null",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NULL),
                    var("result"));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression);
            String output = result.toArkTS();
            assertTrue(output.contains("&&"),
                    "Should contain &&: " + output);
            assertTrue(output.contains("!flag"),
                    "Should negate condition: " + output);
        }
    }

    @Nested
    @DisplayName("No simplification")
    class NoSimplification {

        @Test
        @DisplayName("cond ? a : b unchanged when both non-null")
        void testBothNonNull() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"),
                    lit("yes",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .STRING),
                    lit("no",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .STRING));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(result instanceof ConditionalExpression,
                    "Should remain as ternary: " + result.toArkTS());
        }

        @Test
        @DisplayName("cond ? undefined : null unchanged (both null-ish)")
        void testBothNullish() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"),
                    lit("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .UNDEFINED),
                    lit("null",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NULL));
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertTrue(result instanceof ConditionalExpression,
                    "Should remain as ternary when both null-ish");
        }

        @Test
        @DisplayName("Non-ConditionalExpression passes through")
        void testNonConditionalPassesThrough() {
            ArkTSExpression expr = var("x");
            ArkTSExpression result =
                    OperatorHandler.simplifyLogicalTernary(expr);
            assertEquals(expr, result);
        }
    }
}
