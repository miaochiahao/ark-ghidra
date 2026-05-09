package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for switch expression detection and rendering.
 */
class SwitchExpressionTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    private static ArkTSStatement assign(String target,
            ArkTSExpression value) {
        return new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.AssignExpression(var(target), value));
    }

    @Nested
    @DisplayName("Switch expression detection")
    class SwitchExpressionDetection {

        @Test
        @DisplayName("Simple switch with all cases assigning to same var")
        void testSimpleSwitchExpression() {
            ArkTSStatement varDecl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", "result", null, null);
            ArkTSControlFlow.SwitchStatement switchStmt =
                    new ArkTSControlFlow.SwitchStatement(
                            var("x"),
                            List.of(
                                    new ArkTSControlFlow.SwitchStatement
                                            .SwitchCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            List.of(
                                                    assign("result",
                                                            lit("one",
                                                                    ArkTSExpression
                                                                            .LiteralExpression
                                                                            .LiteralKind.STRING)),
                                                    new ArkTSStatement.BreakStatement())),
                                    new ArkTSControlFlow.SwitchStatement
                                            .SwitchCase(lit("2",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            List.of(
                                                    assign("result",
                                                            lit("two",
                                                                    ArkTSExpression
                                                                            .LiteralExpression
                                                                            .LiteralKind.STRING)),
                                                    new ArkTSStatement.BreakStatement()))),
                            new ArkTSStatement.BlockStatement(List.of(
                                    assign("result",
                                            lit("other",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)),
                                    new ArkTSStatement.BreakStatement())));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.detectSwitchExpressions(
                            List.of(varDecl, switchStmt));

            assertEquals(1, result.size(),
                    "Should merge into one statement");
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.VariableDeclaration);
            ArkTSStatement.VariableDeclaration decl =
                    (ArkTSStatement.VariableDeclaration) result.get(0);
            assertTrue(decl.getInitializer()
                    instanceof ArkTSAccessExpressions.SwitchExpression,
                    "Initializer should be switch expression");
        }

        @Test
        @DisplayName("Non-matching switch (mixed assignments) preserved")
        void testNonMatchingSwitchPreserved() {
            ArkTSStatement varDecl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", "result", null, null);
            ArkTSControlFlow.SwitchStatement switchStmt =
                    new ArkTSControlFlow.SwitchStatement(
                            var("x"),
                            List.of(
                                    new ArkTSControlFlow.SwitchStatement
                                            .SwitchCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            List.of(
                                                    assign("otherVar",
                                                            lit("one",
                                                                    ArkTSExpression
                                                                            .LiteralExpression
                                                                            .LiteralKind.STRING)),
                                                    new ArkTSStatement.BreakStatement()))),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.detectSwitchExpressions(
                            List.of(varDecl, switchStmt));

            assertEquals(2, result.size(),
                    "Should not merge — different variable");
        }

        @Test
        @DisplayName("Switch with initialized var decl not converted")
        void testInitializedVarDeclNotConverted() {
            ArkTSStatement varDecl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", "result", null, lit("0",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            ArkTSControlFlow.SwitchStatement switchStmt =
                    new ArkTSControlFlow.SwitchStatement(
                            var("x"),
                            List.of(
                                    new ArkTSControlFlow.SwitchStatement
                                            .SwitchCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            List.of(assign("result",
                                                    lit("one",
                                                            ArkTSExpression
                                                                    .LiteralExpression
                                                                    .LiteralKind.STRING))))),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.detectSwitchExpressions(
                            List.of(varDecl, switchStmt));

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Switch without preceding var decl not converted")
        void testNoVarDeclNotConverted() {
            ArkTSControlFlow.SwitchStatement switchStmt =
                    new ArkTSControlFlow.SwitchStatement(
                            var("x"),
                            List.of(
                                    new ArkTSControlFlow.SwitchStatement
                                            .SwitchCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            List.of(assign("result",
                                                    lit("one",
                                                            ArkTSExpression
                                                                    .LiteralExpression
                                                                    .LiteralKind.STRING))))),
                            null);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.detectSwitchExpressions(
                            List.of(switchStmt));

            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.SwitchStatement);
        }
    }

    @Nested
    @DisplayName("Switch expression rendering")
    class SwitchExpressionRendering {

        @Test
        @DisplayName("Switch expression renders with case values")
        void testSwitchExpressionRendering() {
            ArkTSAccessExpressions.SwitchExpression expr =
                    new ArkTSAccessExpressions.SwitchExpression(
                            var("x"),
                            List.of(
                                    new ArkTSAccessExpressions.SwitchExpression
                                            .SwitchExprCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            lit("one",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING)),
                                    new ArkTSAccessExpressions.SwitchExpression
                                            .SwitchExprCase(lit("2",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            lit("two",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING))),
                            lit("other",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING));

            String output = expr.toArkTS();
            assertTrue(output.startsWith("switch ("),
                    "Should start with 'switch': " + output);
            assertTrue(output.contains("case 1:"),
                    output);
            assertTrue(output.contains("\"one\""),
                    output);
            assertTrue(output.contains("case 2:"),
                    output);
            assertTrue(output.contains("\"two\""),
                    output);
            assertTrue(output.contains("default:"),
                    output);
            assertTrue(output.contains("\"other\""),
                    output);
        }

        @Test
        @DisplayName("Switch expression without default")
        void testSwitchExpressionNoDefault() {
            ArkTSAccessExpressions.SwitchExpression expr =
                    new ArkTSAccessExpressions.SwitchExpression(
                            var("status"),
                            List.of(
                                    new ArkTSAccessExpressions.SwitchExpression
                                            .SwitchExprCase(lit("200",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            lit("OK",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.STRING))),
                            null);

            String output = expr.toArkTS();
            assertTrue(output.contains("case 200:"), output);
            assertTrue(output.contains("\"OK\""), output);
            assertFalse(output.contains("default:"),
                    "Should not have default: " + output);
        }
    }
}
