package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for expression simplification improvements:
 * unary negation folding, redundant ternary, comparison normalization.
 */
class ExpressionSimplificationTest {

    // ========================================================================
    // Unary negation folding
    // ========================================================================

    @Nested
    @DisplayName("Unary negation folding")
    class UnaryNegationFolding {

        @Test
        @DisplayName("NEG on positive literal produces negative literal")
        void testNegPositiveLiteral() {
            ArkTSExpression literal =
                    new ArkTSExpression.LiteralExpression("42",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression neg =
                    new ArkTSExpression.UnaryExpression("-", literal, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(neg);
            assertTrue(result instanceof ArkTSExpression.LiteralExpression,
                    "Should be a literal expression");
            assertEquals("-42", ((ArkTSExpression.LiteralExpression) result)
                    .getValue());
        }

        @Test
        @DisplayName("NEG on negative literal produces positive literal")
        void testNegNegativeLiteral() {
            ArkTSExpression literal =
                    new ArkTSExpression.LiteralExpression("-5",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression neg =
                    new ArkTSExpression.UnaryExpression("-", literal, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(neg);
            assertTrue(result instanceof ArkTSExpression.LiteralExpression,
                    "Should be a literal expression");
            assertEquals("5", ((ArkTSExpression.LiteralExpression) result)
                    .getValue());
        }

        @Test
        @DisplayName("NEG on 0 stays 0")
        void testNegZero() {
            ArkTSExpression zero =
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression neg =
                    new ArkTSExpression.UnaryExpression("-", zero, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(neg);
            assertTrue(result instanceof ArkTSExpression.LiteralExpression);
            assertEquals("0", ((ArkTSExpression.LiteralExpression) result)
                    .getValue());
        }

        @Test
        @DisplayName("NEG on variable stays as unary expression")
        void testNegVariable() {
            ArkTSExpression var =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression neg =
                    new ArkTSExpression.UnaryExpression("-", var, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(neg);
            assertTrue(result instanceof ArkTSExpression.UnaryExpression,
                    "Should remain unary");
            assertEquals("-", ((ArkTSExpression.UnaryExpression) result)
                    .getOperator());
        }

        @Test
        @DisplayName("NEG on NEG of variable cancels out: -(-x) -> x")
        void testDoubleNegVariable() {
            ArkTSExpression var =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression innerNeg =
                    new ArkTSExpression.UnaryExpression("-", var, true);
            ArkTSExpression outerNeg =
                    new ArkTSExpression.UnaryExpression("-", innerNeg, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(outerNeg);
            assertTrue(result instanceof ArkTSExpression.VariableExpression,
                    "Should be plain variable");
            assertEquals("x", ((ArkTSExpression.VariableExpression) result)
                    .getName());
        }

        @Test
        @DisplayName("Non-neg unary operator not affected")
        void testNonNegUnary() {
            ArkTSExpression var =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression notExpr =
                    new ArkTSExpression.UnaryExpression("!", var, true);
            ArkTSExpression result =
                    OperatorHandler.simplifyUnaryNegation(notExpr);
            assertTrue(result == notExpr,
                    "Should return same object for non-neg operator");
        }
    }

    // ========================================================================
    // Redundant ternary simplification
    // ========================================================================

    @Nested
    @DisplayName("Redundant ternary simplification")
    class RedundantTernarySimplification {

        @Test
        @DisplayName("cond ? true : false -> cond")
        void testTernaryTrueFalse() {
            ArkTSExpression cond =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression ternary =
                    new ArkTSAccessExpressions.ConditionalExpression(
                            cond,
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN),
                            new ArkTSExpression.LiteralExpression("false",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTernary(ternary);
            assertTrue(result instanceof ArkTSExpression.VariableExpression);
            assertEquals("x", ((ArkTSExpression.VariableExpression) result)
                    .getName());
        }

        @Test
        @DisplayName("cond ? false : true -> !cond")
        void testTernaryFalseTrue() {
            ArkTSExpression cond =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression ternary =
                    new ArkTSAccessExpressions.ConditionalExpression(
                            cond,
                            new ArkTSExpression.LiteralExpression("false",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN),
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTernary(ternary);
            assertTrue(result instanceof ArkTSExpression.UnaryExpression);
            assertEquals("!", ((ArkTSExpression.UnaryExpression) result)
                    .getOperator());
        }

        @Test
        @DisplayName("cond ? x : x -> x (identical branches)")
        void testTernaryIdenticalBranches() {
            ArkTSExpression cond =
                    new ArkTSExpression.VariableExpression("flag");
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression ternary =
                    new ArkTSAccessExpressions.ConditionalExpression(
                            cond, value, value);
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTernary(ternary);
            assertTrue(result instanceof ArkTSExpression.VariableExpression);
            assertEquals("x", ((ArkTSExpression.VariableExpression) result)
                    .getName());
        }

        @Test
        @DisplayName("Non-redundant ternary is preserved")
        void testNonRedundantTernary() {
            ArkTSExpression cond =
                    new ArkTSExpression.VariableExpression("flag");
            ArkTSExpression ternary =
                    new ArkTSAccessExpressions.ConditionalExpression(
                            cond,
                            new ArkTSExpression.LiteralExpression("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            new ArkTSExpression.LiteralExpression("2",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTernary(ternary);
            assertTrue(result
                    instanceof ArkTSAccessExpressions.ConditionalExpression);
        }

        @Test
        @DisplayName("Non-conditional expression passed through unchanged")
        void testNonConditionalExpr() {
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTernary(expr);
            assertTrue(result == expr);
        }
    }

    // ========================================================================
    // Comparison normalization
    // ========================================================================

    @Nested
    @DisplayName("Comparison normalization")
    class ComparisonNormalization {

        @Test
        @DisplayName("0 < x -> x > 0")
        void testLessThanSwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("0",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            "<",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result instanceof ArkTSExpression.BinaryExpression);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertEquals(">", bin.getOperator());
            assertTrue(bin.getLeft()
                    instanceof ArkTSExpression.VariableExpression);
            assertTrue(bin.getRight()
                    instanceof ArkTSExpression.LiteralExpression);
        }

        @Test
        @DisplayName("0 <= x -> x >= 0")
        void testLessEqualSwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("0",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            "<=",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertEquals(">=", bin.getOperator());
        }

        @Test
        @DisplayName("10 > x -> x < 10")
        void testGreaterThanSwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("10",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            ">",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertEquals("<", bin.getOperator());
        }

        @Test
        @DisplayName("5 >= x -> x <= 5")
        void testGreaterEqualSwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("5",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            ">=",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertEquals("<=", bin.getOperator());
        }

        @Test
        @DisplayName("null === x -> x === null")
        void testEqualitySwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("null",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NULL),
                            "===",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertEquals("===", bin.getOperator());
            assertTrue(bin.getLeft()
                    instanceof ArkTSExpression.VariableExpression);
            assertTrue(bin.getRight()
                    instanceof ArkTSExpression.LiteralExpression);
        }

        @Test
        @DisplayName("x > 0 stays x > 0 (already normalized)")
        void testAlreadyNormalized() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            ">",
                            new ArkTSExpression.LiteralExpression("0",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "Should return same object when already normalized");
        }

        @Test
        @DisplayName("instanceof not swapped (non-commutative)")
        void testInstanceofNotSwapped() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("Error",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING),
                            "instanceof",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "instanceof should not be swapped");
        }

        @Test
        @DisplayName("in operator not swapped (non-commutative)")
        void testInNotSwapped() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("key",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING),
                            "in",
                            new ArkTSExpression.VariableExpression("obj"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "in operator should not be swapped");
        }

        @Test
        @DisplayName("Two variables not swapped")
        void testTwoVariablesNotSwapped() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("a"),
                            "<",
                            new ArkTSExpression.VariableExpression("b"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "Two variables should not be swapped");
        }

        @Test
        @DisplayName("Two constants not swapped")
        void testTwoConstantsNotSwapped() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            "<",
                            new ArkTSExpression.LiteralExpression("2",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "Two constants should not be swapped");
        }

        @Test
        @DisplayName("Non-comparison binary expr passed through")
        void testNonComparisonBinary() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("0",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER),
                            "+",
                            new ArkTSExpression.VariableExpression("x"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            assertTrue(result == expr,
                    "Non-comparison operator should not be normalized");
        }

        @Test
        @DisplayName("this === x -> x === this")
        void testThisSwap() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.ThisExpression(),
                            "===",
                            new ArkTSExpression.VariableExpression("other"));
            ArkTSExpression result =
                    OperatorHandler.normalizeComparison(expr);
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) result;
            assertTrue(bin.getLeft()
                    instanceof ArkTSExpression.VariableExpression);
            assertTrue(bin.getRight()
                    instanceof ArkTSExpression.ThisExpression);
        }
    }
}
