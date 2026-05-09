package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for output quality: expression simplification,
 * multi-catch, labeled break/continue, and AST rendering.
 */
class ArkTSOutputQualityTest {

    @Nested
    @DisplayName("Infinite loop rendering")
    class InfiniteLoopTests {
        @Test
        void testWhileTrue_literalBooleanTrue() {
            ArkTSExpression trueExpr =
                    new ArkTSExpression.LiteralExpression("true",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.BOOLEAN);
            ArkTSStatement body = new ArkTSStatement.BlockStatement(
                    List.of(new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.VariableExpression(
                                    "doWork()"))));
            ArkTSControlFlow.WhileStatement stmt =
                    new ArkTSControlFlow.WhileStatement(trueExpr, body);
            String result = stmt.toArkTS(0);
            assertTrue(result.startsWith("while (true)"),
                    "Should render while (true): " + result);
        }
    }

    @Nested
    @DisplayName("Rest destructuring")
    class RestDestructuringTests {
        @Test
        void testArrayDestructuringWithRest() {
            List<ArkTSPropertyExpressions.ArrayDestructuringExpression
                    .ArrayBinding> bindings = List.of(
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding("a"),
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding("b"));
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression("arr");
            ArkTSExpression destr =
                    new ArkTSPropertyExpressions
                            .ArrayDestructuringExpression(
                            bindings, "rest", source, true);
            assertTrue(destr.toArkTS().contains("...rest"),
                    "Should contain rest: " + destr.toArkTS());
        }

        @Test
        void testArrayDestructuringWithoutRest() {
            List<ArkTSPropertyExpressions.ArrayDestructuringExpression
                    .ArrayBinding> bindings = List.of(
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding("a"),
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding("b"));
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression("arr");
            ArkTSExpression destr =
                    new ArkTSPropertyExpressions
                            .ArrayDestructuringExpression(
                            bindings, null, source, true);
            assertFalse(destr.toArkTS().contains("..."),
                    "Should not contain rest: " + destr.toArkTS());
        }
    }

    @Nested
    @DisplayName("Expression simplification")
    class ExpressionSimplificationTests {
        @Test
        void testDoubleNegation_toNotEquals() {
            ArkTSExpression inner =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "==",
                            new ArkTSExpression.VariableExpression("y"));
            ArkTSExpression negated =
                    new ArkTSExpression.UnaryExpression(
                            "!", inner, true);
            ArkTSExpression simplified =
                    OperatorHandler.simplifyDoubleNegation(negated);
            assertTrue(simplified.toArkTS().contains("!="));
        }

        @Test
        void testBooleanComparison_xEqualsTrue() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "===",
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            assertEquals("x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testConstantFolding() {
            ArkTSExpression three =
                    new ArkTSExpression.LiteralExpression("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            ArkTSExpression four =
                    new ArkTSExpression.LiteralExpression("4",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            assertEquals("7",
                    OperatorHandler.tryFoldConstants(
                            three, "+", four).toArkTS());
        }

        @Test
        void testIdentitySimplification() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression zero =
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            assertEquals("x",
                    OperatorHandler.trySimplifyIdentity(
                            x, "+", zero).toArkTS());
        }

        @Test
        void testStringLiteralMerging() {
            ArkTSExpression hello =
                    new ArkTSExpression.LiteralExpression("\"hello\"",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            ArkTSExpression world =
                    new ArkTSExpression.LiteralExpression("\" world\"",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            assertEquals("\"hello world\"",
                    OperatorHandler.tryMergeStringLiterals(
                            hello, "+", world).toArkTS());
        }
    }

    @Nested
    @DisplayName("Multi-catch try/catch")
    class MultiCatchTests {
        @Test
        void testMultiCatchWithTwoTypes() {
            ArkTSStatement tryBody =
                    new ArkTSStatement.BlockStatement(List.of(
                            new ArkTSStatement.ExpressionStatement(
                                    new ArkTSExpression.VariableExpression(
                                            "risky()"))));
            ArkTSControlFlow.MultiCatchTryCatchStatement.CatchClause c1 =
                    new ArkTSControlFlow
                            .MultiCatchTryCatchStatement.CatchClause(
                            "e", "TypeError",
                            new ArkTSStatement.BlockStatement(List.of()));
            ArkTSControlFlow.MultiCatchTryCatchStatement.CatchClause c2 =
                    new ArkTSControlFlow
                            .MultiCatchTryCatchStatement.CatchClause(
                            "e", "RangeError",
                            new ArkTSStatement.BlockStatement(List.of()));
            ArkTSControlFlow.MultiCatchTryCatchStatement stmt =
                    new ArkTSControlFlow
                            .MultiCatchTryCatchStatement(
                            tryBody, List.of(c1, c2), null);
            String result = stmt.toArkTS(0);
            assertTrue(result.contains("catch (e: TypeError)"),
                    "Should have TypeError: " + result);
            assertTrue(result.contains("catch (e: RangeError)"),
                    "Should have RangeError: " + result);
        }
    }

    @Nested
    @DisplayName("Labeled break/continue")
    class LabeledBreakContinueTests {
        @Test
        void testBreakWithLabel() {
            assertEquals("break outer;",
                    new ArkTSStatement.BreakStatement("outer")
                            .toArkTS(0).trim());
        }

        @Test
        void testContinueWithLabel() {
            assertEquals("continue outer;",
                    new ArkTSStatement.ContinueStatement("outer")
                            .toArkTS(0).trim());
        }

        @Test
        void testLabeledStatement() {
            ArkTSStatement inner =
                    new ArkTSControlFlow.WhileStatement(
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN),
                            new ArkTSStatement.BlockStatement(List.of(
                                    new ArkTSStatement.BreakStatement(
                                            "outer"))));
            String result =
                    new ArkTSStatement.LabeledStatement("outer", inner)
                            .toArkTS(0);
            assertTrue(result.startsWith("outer:"));
            assertTrue(result.contains("while (true)"));
            assertTrue(result.contains("break outer"));
        }
    }
}
