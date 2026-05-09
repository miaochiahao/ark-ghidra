package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for if-else chain to switch conversion post-processing.
 */
class IfElseChainToSwitchTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    private static ArkTSExpression strictEq(ArkTSExpression left,
            ArkTSExpression right) {
        return new ArkTSExpression.BinaryExpression(left, "===", right);
    }

    @Nested
    @DisplayName("If-else chain to switch")
    class ChainToSwitch {

        @Test
        @DisplayName("3-branch if-else chain converts to switch")
        void testThreeBranchConvertsToSwitch() {
            // if (x === 1) { a } else if (x === 2) { b } else if (x === 3) { c }
            ArkTSStatement innerIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("c")))),
                    null);
            ArkTSStatement midIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("2",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("b")))),
                    innerIf);
            ArkTSControlFlow.IfStatement outerIf =
                    new ArkTSControlFlow.IfStatement(
                            strictEq(var("x"), lit("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("a")))),
                            midIf);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(outerIf));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.SwitchStatement,
                    "Should be switch: " + result.get(0).getClass()
                            .getSimpleName());
            String output = result.get(0).toArkTS(0);
            assertTrue(output.startsWith("switch (x)"),
                    "Should start with switch(x): " + output);
            assertTrue(output.contains("case 1:"),
                    "Should contain case 1: " + output);
            assertTrue(output.contains("case 2:"),
                    "Should contain case 2: " + output);
            assertTrue(output.contains("case 3:"),
                    "Should contain case 3: " + output);
        }

        @Test
        @DisplayName("3-branch chain with default")
        void testThreeBranchWithDefault() {
            ArkTSStatement innerIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("status"), lit("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("c")))),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("default")))));
            ArkTSStatement outerIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("status"), lit("1",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("a")))),
                    new ArkTSControlFlow.IfStatement(
                            strictEq(var("status"), lit("2",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("b")))),
                            innerIf));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(outerIf));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.SwitchStatement);
            String output = result.get(0).toArkTS(0);
            assertTrue(output.contains("default:"),
                    "Should contain default: " + output);
        }

        @Test
        @DisplayName("2-branch chain not converted (too short)")
        void testTwoBranchNotConverted() {
            ArkTSControlFlow.IfStatement ifStmt =
                    new ArkTSControlFlow.IfStatement(
                            strictEq(var("x"), lit("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("a")))),
                            new ArkTSControlFlow.IfStatement(
                                    strictEq(var("x"), lit("2",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                                    new ArkTSStatement.BlockStatement(
                                            List.of(new ArkTSStatement
                                                    .ExpressionStatement(
                                                            var("b")))),
                                    null));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(ifStmt));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement,
                    "2-branch should stay as if");
        }

        @Test
        @DisplayName("Mixed variables not converted")
        void testMixedVariablesNotConverted() {
            ArkTSStatement innerIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("y"), lit("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("c")))),
                    null);
            ArkTSControlFlow.IfStatement outerIf =
                    new ArkTSControlFlow.IfStatement(
                            strictEq(var("x"), lit("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("a")))),
                            new ArkTSControlFlow.IfStatement(
                                    strictEq(var("x"), lit("2",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                                    new ArkTSStatement.BlockStatement(
                                            List.of(new ArkTSStatement
                                                    .ExpressionStatement(
                                                            var("b")))),
                                    innerIf));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(outerIf));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement,
                    "Mixed variables should stay as if");
        }

        @Test
        @DisplayName("Non-=== operator not converted")
        void testNonStrictEqNotConverted() {
            ArkTSExpression looseEq = new ArkTSExpression.BinaryExpression(
                    var("x"), "==", lit("1",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
            ArkTSStatement innerIf = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("c")))),
                    null);
            ArkTSControlFlow.IfStatement outerIf =
                    new ArkTSControlFlow.IfStatement(
                            looseEq,
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("a")))),
                            new ArkTSControlFlow.IfStatement(
                                    strictEq(var("x"), lit("2",
                                            ArkTSExpression.LiteralExpression
                                                    .LiteralKind.NUMBER)),
                                    new ArkTSStatement.BlockStatement(
                                            List.of(new ArkTSStatement
                                                    .ExpressionStatement(
                                                            var("b")))),
                                    innerIf));

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(outerIf));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.IfStatement,
                    "Non-=== should stay as if");
        }

        @Test
        @DisplayName("Non-if statement passes through unchanged")
        void testNonIfPassesThrough() {
            ArkTSStatement retStmt = new ArkTSStatement.ReturnStatement(
                    lit("42", ArkTSExpression.LiteralExpression
                            .LiteralKind.NUMBER));
            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(retStmt));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSStatement.ReturnStatement);
        }

        @Test
        @DisplayName("4-branch chain converts to switch")
        void testFourBranchConvertsToSwitch() {
            ArkTSStatement if4 = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("4",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("d")))),
                    null);
            ArkTSStatement if3 = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("3",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("c")))),
                    if4);
            ArkTSStatement if2 = new ArkTSControlFlow.IfStatement(
                    strictEq(var("x"), lit("2",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER)),
                    new ArkTSStatement.BlockStatement(
                            List.of(new ArkTSStatement.ExpressionStatement(
                                    var("b")))),
                    if3);
            ArkTSControlFlow.IfStatement if1 =
                    new ArkTSControlFlow.IfStatement(
                            strictEq(var("x"), lit("1",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)),
                            new ArkTSStatement.BlockStatement(
                                    List.of(new ArkTSStatement
                                            .ExpressionStatement(var("a")))),
                            if2);

            List<ArkTSStatement> result =
                    ArkTSDecompiler.convertIfElseChainToSwitch(
                            List.of(if1));
            assertEquals(1, result.size());
            assertTrue(result.get(0)
                    instanceof ArkTSControlFlow.SwitchStatement);
            ArkTSControlFlow.SwitchStatement sw =
                    (ArkTSControlFlow.SwitchStatement) result.get(0);
            assertEquals(4, sw.getCases().size());
        }
    }
}
