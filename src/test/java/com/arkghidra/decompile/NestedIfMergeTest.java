package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for nested if-condition merging post-processing.
 * Verifies that nested if-only statements are collapsed into
 * a single if with && conditions.
 */
class NestedIfMergeTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSStatement ret(ArkTSExpression expr) {
        return new ArkTSStatement.ReturnStatement(expr);
    }

    @Nested
    @DisplayName("Two-level nested if merging")
    class TwoLevelMerge {

        @Test
        @DisplayName("if (a) { if (b) { return x } } -> if (a && b) { return x }")
        void testSimpleTwoLevelMerge() {
            ArkTSControlFlow.IfStatement inner =
                    new ArkTSControlFlow.IfStatement(var("b"),
                            ret(var("x")), null);
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            new ArkTSStatement.BlockStatement(List.of(inner)),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement);
            ArkTSControlFlow.IfStatement merged =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            String condition = merged.getCondition().toArkTS();
            assertTrue(condition.contains("&&"),
                    "Should contain &&: " + condition);
            assertTrue(condition.contains("a"),
                    "Should contain a: " + condition);
            assertTrue(condition.contains("b"),
                    "Should contain b: " + condition);
            assertEquals(null, merged.getElseBlock());
        }

        @Test
        @DisplayName("If-else inner is not merged")
        void testInnerIfElseNotMerged() {
            ArkTSControlFlow.IfStatement inner =
                    new ArkTSControlFlow.IfStatement(var("b"),
                            ret(var("x")), ret(var("y")));
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            new ArkTSStatement.BlockStatement(List.of(inner)),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            ArkTSControlFlow.IfStatement out =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            // Should NOT merge because inner has an else block
            assertFalse(out.getCondition().toArkTS().contains("&&"));
        }

        @Test
        @DisplayName("If-else outer is not merged")
        void testOuterIfElseNotMerged() {
            ArkTSControlFlow.IfStatement inner =
                    new ArkTSControlFlow.IfStatement(var("b"),
                            ret(var("x")), null);
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            new ArkTSStatement.BlockStatement(List.of(inner)),
                            ret(var("y")));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            ArkTSControlFlow.IfStatement out =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            assertFalse(out.getCondition().toArkTS().contains("&&"));
        }
    }

    @Nested
    @DisplayName("Three-level nested if merging")
    class ThreeLevelMerge {

        @Test
        @DisplayName("Three levels collapse to a && b && c")
        void testThreeLevelMerge() {
            ArkTSControlFlow.IfStatement deepest =
                    new ArkTSControlFlow.IfStatement(var("c"),
                            ret(var("x")), null);
            ArkTSControlFlow.IfStatement middle =
                    new ArkTSControlFlow.IfStatement(var("b"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(deepest)),
                            null);
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(middle)),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            ArkTSControlFlow.IfStatement merged =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            String condition = merged.getCondition().toArkTS();
            assertTrue(condition.contains("a"), condition);
            assertTrue(condition.contains("b"), condition);
            assertTrue(condition.contains("c"), condition);
            // Should have two && operators for three conditions
            long andCount = condition.chars()
                    .filter(ch -> ch == '&')
                    .count();
            assertTrue(andCount >= 4,
                    "Should have at least 4 & chars (two &&): "
                            + condition);
        }
    }

    @Nested
    @DisplayName("Non-mergeable patterns")
    class NonMergeablePatterns {

        @Test
        @DisplayName("Multiple statements in then-block prevents merge")
        void testMultipleStatementsPreventsMerge() {
            ArkTSControlFlow.IfStatement inner =
                    new ArkTSControlFlow.IfStatement(var("b"),
                            ret(var("x")), null);
            ArkTSStatement outerThen =
                    new ArkTSStatement.BlockStatement(List.of(
                            inner,
                            new ArkTSStatement.ExpressionStatement(
                                    var("y"))));
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            outerThen, null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            ArkTSControlFlow.IfStatement out =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            assertFalse(out.getCondition().toArkTS().contains("&&"));
        }

        @Test
        @DisplayName("Non-if statement in then-block prevents merge")
        void testNonIfInThenBlockPreventsMerge() {
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(var("a"),
                            ret(var("x")), null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            assertEquals(1, result.size());
            ArkTSControlFlow.IfStatement out =
                    (ArkTSControlFlow.IfStatement) result.get(0);
            assertEquals("a", out.getCondition().toArkTS());
        }

        @Test
        @DisplayName("Empty list returns empty list")
        void testEmptyList() {
            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(List.of());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Non-if statement passes through")
        void testNonIfStatement() {
            ArkTSStatement stmt = ret(var("x"));
            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(stmt));
            assertEquals(1, result.size());
            assertTrue(result.get(0) instanceof ArkTSStatement.ReturnStatement);
        }
    }

    @Nested
    @DisplayName("Output rendering")
    class OutputRendering {

        @Test
        @DisplayName("Merged if renders with && in condition")
        void testMergedRendering() {
            ArkTSControlFlow.IfStatement inner =
                    new ArkTSControlFlow.IfStatement(
                            new ArkTSExpression.BinaryExpression(
                                    var("x"), ">", new ArkTSExpression
                                            .LiteralExpression("0",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER)),
                            ret(var("y")), null);
            ArkTSControlFlow.IfStatement outer =
                    new ArkTSControlFlow.IfStatement(
                            new ArkTSExpression.BinaryExpression(
                                    var("x"), "!==",
                                    new ArkTSExpression.LiteralExpression(
                                            "null",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NULL)),
                            new ArkTSStatement.BlockStatement(List.of(inner)),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.mergeNestedIfConditions(
                            List.of(outer));

            String output = result.get(0).toArkTS(0);
            assertTrue(output.startsWith("if ("),
                    "Should start with 'if (': " + output);
            assertTrue(output.contains("&&"), output);
            assertTrue(output.contains("x !== null"), output);
            assertTrue(output.contains("x > 0"), output);
        }
    }
}
