package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for return-if ternary conversion post-processing.
 */
class ReturnIfTernaryTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    @Nested
    @DisplayName("if/else with both branches returning")
    class BothBranchesReturn {

        @Test
        @DisplayName("if/else return → return ternary")
        void testIfElseReturnToTernary() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("yes",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)))),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("no",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING))))));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ReturnStatement);
            String output = result.get(0).toArkTS(0);
            assertTrue(output.contains("?"),
                    "Should contain ternary: " + output);
            assertTrue(output.contains("yes"),
                    "Should contain 'yes': " + output);
            assertTrue(output.contains("no"),
                    "Should contain 'no': " + output);
        }

        @Test
        @DisplayName("if/else throw → throw ternary")
        void testIfElseThrowToTernary() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("err"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ThrowStatement(
                                            lit("ErrorA",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)))),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ThrowStatement(
                                            lit("ErrorB",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING))))));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ThrowStatement);
            String output = result.get(0).toArkTS(0);
            assertTrue(output.contains("?"),
                    "Should contain ternary: " + output);
        }

        @Test
        @DisplayName("Mixed return/throw not converted")
        void testMixedReturnThrowNotConverted() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("val",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)))),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ThrowStatement(
                                            lit("err",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING))))));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement,
                    "Mixed return/throw should stay as if/else");
        }
    }

    @Nested
    @DisplayName("Trailing return-if ternary")
    class TrailingReturnIf {

        @Test
        @DisplayName("if-return followed by return → return ternary")
        void testTrailingReturnToTernary() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("yes",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)))),
                            null),
                    new ArkTSStatement.ReturnStatement(
                            lit("no",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING)));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ReturnStatement);
            String output = result.get(0).toArkTS(0);
            assertTrue(output.contains("?"),
                    "Should contain ternary: " + output);
            assertTrue(output.contains("yes"),
                    "Should contain 'yes': " + output);
            assertTrue(output.contains("no"),
                    "Should contain 'no': " + output);
        }

        @Test
        @DisplayName("if-throw followed by throw → throw ternary")
        void testTrailingThrowToTernary() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("err"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ThrowStatement(
                                            lit("ErrorA",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)))),
                            null),
                    new ArkTSStatement.ThrowStatement(
                            lit("ErrorB",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING)));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ThrowStatement);
            String output = result.get(0).toArkTS(0);
            assertTrue(output.contains("ErrorA"),
                    "Should contain ErrorA: " + output);
        }

        @Test
        @DisplayName("if-return followed by non-return → not converted")
        void testTrailingNonReturnNotConverted() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER)))),
                            null),
                    new ArkTSStatement.ExpressionStatement(var("y")));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(2, result.size(),
                    "Should not merge with non-return trailing stmt");
        }

        @Test
        @DisplayName("if with non-return body + trailing return → not converted")
        void testNonReturnBodyTrailingNotConverted() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("y")))),
                            null),
                    new ArkTSStatement.ReturnStatement(
                            lit("42",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(2, result.size(),
                    "Should not merge non-return body with trailing return");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Non-if statement passes through")
        void testNonIfStatementPassesThrough() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSStatement.ReturnStatement(
                            lit("42",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ReturnStatement);
        }

        @Test
        @DisplayName("If with no else and no trailing return → passes through")
        void testIfNoElseNoTrailingPassesThrough() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement.ReturnStatement(
                                            lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER)))),
                            null));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement);
        }

        @Test
        @DisplayName("If/else with non-return body passes through")
        void testNonReturnBodyPassesThrough() {
            List<ArkTSStatement> stmts = List.of(
                    new ArkTSControlFlow.IfStatement(
                            var("x"),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("y")))),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("z"))))));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(stmts);
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement);
        }

        @Test
        @DisplayName("Empty input returns empty")
        void testEmptyInput() {
            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(
                            Collections.emptyList());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Null input returns null-safe")
        void testNullInput() {
            List<ArkTSStatement> result =
                    ArkTSDecompiler.simplifyReturnIfTernary(null);
            assertTrue(result == null || result.isEmpty());
        }
    }
}
