package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSAccessExpressions.ConditionalExpression;

/**
 * Tests for void 0 simplification and ternary-to-OR conversion.
 */
@DisplayName("void 0 and ternary-to-OR simplification")
class VoidZeroAndTernaryOrTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    @Nested
    @DisplayName("void 0 → undefined")
    class VoidZeroSimplification {

        @Test
        @DisplayName("void 0 → undefined")
        void testVoidZeroToUndefined() {
            ArkTSExpression expr = new ArkTSExpression.UnaryExpression("void",
                    lit("0",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NUMBER),
                    true);
            ArkTSExpression result =
                    OperatorHandler.simplifyVoidZero(expr);
            assertTrue(
                    result instanceof ArkTSExpression.LiteralExpression,
                    "Should be LiteralExpression: " + result.toArkTS());
            assertEquals("undefined", result.toArkTS());
        }

        @Test
        @DisplayName("void 1 not simplified")
        void testVoidOneNotSimplified() {
            ArkTSExpression expr = new ArkTSExpression.UnaryExpression("void",
                    lit("1",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NUMBER),
                    true);
            ArkTSExpression result =
                    OperatorHandler.simplifyVoidZero(expr);
            assertEquals("void 1", result.toArkTS());
        }

        @Test
        @DisplayName("void x not simplified")
        void testVoidVariableNotSimplified() {
            ArkTSExpression expr = new ArkTSExpression.UnaryExpression("void",
                    var("x"), true);
            ArkTSExpression result =
                    OperatorHandler.simplifyVoidZero(expr);
            assertEquals("void x", result.toArkTS());
        }

        @Test
        @DisplayName("Non-void unary passes through")
        void testNonVoidPassesThrough() {
            ArkTSExpression expr = new ArkTSExpression.UnaryExpression("!",
                    var("x"), true);
            ArkTSExpression result =
                    OperatorHandler.simplifyVoidZero(expr);
            assertEquals(expr, result);
        }
    }

    @Nested
    @DisplayName("Ternary to OR (x ? x : value → x || value)")
    class TernaryToOr {

        @Test
        @DisplayName("x ? x : default → x || default")
        void testTernaryToOr() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"), var("x"),
                    lit("0",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NUMBER));
            ArkTSExpression result =
                    OperatorHandler.simplifyTernaryToOr(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression,
                    "Should be BinaryExpression: " + result.toArkTS());
            assertEquals("x || 0", result.toArkTS());
        }

        @Test
        @DisplayName("x ? y : value not simplified (different vars)")
        void testDifferentVarsNotSimplified() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("x"), var("y"),
                    lit("0",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NUMBER));
            ArkTSExpression result =
                    OperatorHandler.simplifyTernaryToOr(expr);
            assertTrue(result instanceof ConditionalExpression,
                    "Should stay as ternary when vars differ");
        }

        @Test
        @DisplayName("Non-conditional passes through")
        void testNonConditionalPassesThrough() {
            ArkTSExpression expr = var("x");
            ArkTSExpression result =
                    OperatorHandler.simplifyTernaryToOr(expr);
            assertEquals(expr, result);
        }

        @Test
        @DisplayName("x ? x : null → x || null")
        void testTernaryToNullOr() {
            ArkTSExpression expr = new ConditionalExpression(
                    var("result"), var("result"),
                    lit("null",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .NULL));
            ArkTSExpression result =
                    OperatorHandler.simplifyTernaryToOr(expr);
            assertTrue(
                    result instanceof ArkTSExpression.BinaryExpression,
                    "Should be BinaryExpression: " + result.toArkTS());
            assertEquals("result || null", result.toArkTS());
        }
    }
}
