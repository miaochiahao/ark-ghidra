package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for control flow improvements: labeled break/continue,
 * switch fall-through case grouping, labeled loops, and
 * DecompilationContext label management.
 */
class ArkTSControlFlowTest {

    private DecompilationContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new DecompilationContext(
                null, null, null, null, null, null);
    }

    // --- Break/Continue statement rendering ---

    @Test
    void testBreakStatement_withoutLabel_rendersSimple() {
        ArkTSStatement.BreakStatement stmt =
                new ArkTSStatement.BreakStatement();
        assertEquals("break;", stmt.toArkTS(0));
    }

    @Test
    void testBreakStatement_withLabel_rendersLabel() {
        ArkTSStatement.BreakStatement stmt =
                new ArkTSStatement.BreakStatement("loop_0");
        assertEquals("break loop_0;", stmt.toArkTS(0));
    }

    @Test
    void testBreakStatement_labelGetter() {
        ArkTSStatement.BreakStatement unlabeled =
                new ArkTSStatement.BreakStatement();
        assertNull(unlabeled.getLabel());

        ArkTSStatement.BreakStatement labeled =
                new ArkTSStatement.BreakStatement("loop_1");
        assertEquals("loop_1", labeled.getLabel());
    }

    @Test
    void testContinueStatement_withoutLabel_rendersSimple() {
        ArkTSStatement.ContinueStatement stmt =
                new ArkTSStatement.ContinueStatement();
        assertEquals("continue;", stmt.toArkTS(0));
    }

    @Test
    void testContinueStatement_withLabel_rendersLabel() {
        ArkTSStatement.ContinueStatement stmt =
                new ArkTSStatement.ContinueStatement("loop_0");
        assertEquals("continue loop_0;", stmt.toArkTS(0));
    }

    @Test
    void testContinueStatement_labelGetter() {
        ArkTSStatement.ContinueStatement unlabeled =
                new ArkTSStatement.ContinueStatement();
        assertNull(unlabeled.getLabel());

        ArkTSStatement.ContinueStatement labeled =
                new ArkTSStatement.ContinueStatement("loop_2");
        assertEquals("loop_2", labeled.getLabel());
    }

    // --- Labeled statement ---

    @Test
    void testLabeledStatement_wrapsWhileLoop() {
        ArkTSExpression trueCond =
                new ArkTSExpression.LiteralExpression("true",
                        ArkTSExpression.LiteralExpression
                                .LiteralKind.BOOLEAN);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(new ArrayList<>());
        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(trueCond, body);

        ArkTSStatement.LabeledStatement labeled =
                new ArkTSStatement.LabeledStatement("loop_0", whileStmt);

        String output = labeled.toArkTS(0);
        assertTrue(output.startsWith("loop_0:"),
                "Expected label prefix in output: " + output);
        assertTrue(output.contains("while (true)"),
                "Expected while(true) in output: " + output);
    }

    @Test
    void testLabeledStatement_getLabel() {
        ArkTSStatement stmt =
                new ArkTSStatement.ReturnStatement(null);
        ArkTSStatement.LabeledStatement labeled =
                new ArkTSStatement.LabeledStatement("outer", stmt);
        assertEquals("outer", labeled.getLabel());
    }

    @Test
    void testLabeledStatement_getStatement() {
        ArkTSStatement inner =
                new ArkTSStatement.ReturnStatement(null);
        ArkTSStatement.LabeledStatement labeled =
                new ArkTSStatement.LabeledStatement("outer", inner);
        assertEquals(inner, labeled.getStatement());
    }

    // --- SwitchCase AST node tests ---

    @Test
    void testSwitchCase_singleTest_returnsSingletonList() {
        ArkTSExpression testExpr =
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression
                                .LiteralKind.NUMBER);
        List<ArkTSStatement> body = new ArrayList<>();
        body.add(new ArkTSStatement.BreakStatement());

        ArkTSControlFlow.SwitchStatement.SwitchCase sc =
                new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        testExpr, body);

        assertNotNull(sc.getTest());
        assertEquals("1", sc.getTest().toArkTS());
        assertEquals(1, sc.getTests().size(),
                "Single-test case should have singleton tests list");
        assertEquals("1", sc.getTests().get(0).toArkTS());
    }

    @Test
    void testSwitchCase_groupedTests_storesAllTests() {
        List<ArkTSExpression> tests = new ArrayList<>();
        tests.add(new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        tests.add(new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));

        List<ArkTSStatement> body = new ArrayList<>();
        body.add(new ArkTSStatement.BreakStatement());

        ArkTSControlFlow.SwitchStatement.SwitchCase sc =
                new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        tests, body);

        assertNotNull(sc.getTests());
        assertEquals(2, sc.getTests().size());
        assertEquals("1", sc.getTests().get(0).toArkTS());
        assertEquals("2", sc.getTests().get(1).toArkTS());
        assertEquals("1", sc.getTest().toArkTS(),
                "getTest() should return first test value");
    }

    @Test
    void testSwitchStatement_groupedCasesRendersMultipleLabels() {
        List<ArkTSExpression> tests = new ArrayList<>();
        tests.add(new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        tests.add(new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));

        List<ArkTSStatement> body = new ArrayList<>();
        body.add(new ArkTSStatement.BreakStatement());

        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                tests, body));

        ArkTSExpression discriminant =
                new ArkTSExpression.VariableExpression("v0");

        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(
                        discriminant, cases, null);

        String output = stmt.toArkTS(0);
        assertTrue(output.contains("case 1:"),
                "Expected 'case 1:' in output: " + output);
        assertTrue(output.contains("case 2:"),
                "Expected 'case 2:' in output: " + output);
        assertTrue(output.contains("break;"),
                "Expected 'break;' in output: " + output);
    }

    @Test
    void testSwitchStatement_singleCaseRendersOneLabel() {
        ArkTSExpression testExpr =
                new ArkTSExpression.LiteralExpression("5",
                        ArkTSExpression.LiteralExpression
                                .LiteralKind.NUMBER);
        List<ArkTSStatement> body = new ArrayList<>();
        body.add(new ArkTSStatement.BreakStatement());

        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                testExpr, body));

        ArkTSExpression discriminant =
                new ArkTSExpression.VariableExpression("v0");

        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(
                        discriminant, cases, null);

        String output = stmt.toArkTS(0);
        assertTrue(output.contains("case 5:"),
                "Expected 'case 5:' in output: " + output);
    }

    // --- DecompilationContext loop label management ---

    @Test
    void testPushLoopContext_noLabelForFirstLoop() {
        ctx.pushLoopContext(10, 50);
        assertNull(ctx.getCurrentLoopLabel(),
                "First (outermost) loop should not have a label");
    }

    @Test
    void testPushLoopContext_labelForSecondLoop() {
        ctx.pushLoopContext(10, 50);
        String label = ctx.generateLoopLabel();
        assertNotNull(label);
        assertTrue(label.startsWith("loop_"));

        ctx.pushLoopContext(60, 100, label);
        assertEquals(label, ctx.getCurrentLoopLabel());
    }

    @Test
    void testGenerateLoopLabel_generatesUniqueLabels() {
        String label0 = ctx.generateLoopLabel();
        String label1 = ctx.generateLoopLabel();
        String label2 = ctx.generateLoopLabel();
        assertTrue(label0.startsWith("loop_"));
        assertTrue(label1.startsWith("loop_"));
        assertTrue(label2.startsWith("loop_"));
        // All should be unique
        assertTrue(!label0.equals(label1));
        assertTrue(!label1.equals(label2));
    }

    @Test
    void testFindBreakLabel_innerLoop_returnsNull() {
        ctx.pushLoopContext(10, 50);
        String label = ctx.findBreakLabel(50);
        assertNull(label,
                "Break from innermost loop should be unlabeled");
    }

    @Test
    void testFindBreakLabel_outerLoop_returnsOuterLabel() {
        ctx.pushLoopContext(10, 50, "loop_0");
        ctx.pushLoopContext(60, 100);

        String label = ctx.findBreakLabel(50);
        assertEquals("loop_0", label,
                "Break to outer loop should return outer label");
    }

    @Test
    void testFindBreakLabel_noLoops_returnsNull() {
        String label = ctx.findBreakLabel(100);
        assertNull(label, "No loops on stack should return null");
    }

    @Test
    void testFindContinueLabel_innerLoop_returnsNull() {
        ctx.pushLoopContext(10, 50);
        String label = ctx.findContinueLabel(10);
        assertNull(label,
                "Continue in innermost loop should be unlabeled");
    }

    @Test
    void testFindContinueLabel_outerLoop_returnsOuterLabel() {
        ctx.pushLoopContext(10, 50, "loop_0");
        ctx.pushLoopContext(60, 100);

        String label = ctx.findContinueLabel(10);
        assertEquals("loop_0", label,
                "Continue to outer loop should return outer label");
    }

    @Test
    void testFindContinueLabel_noLoops_returnsNull() {
        String label = ctx.findContinueLabel(10);
        assertNull(label, "No loops on stack should return null");
    }

    @Test
    void testPopLoopContext_restoresPreviousLabel() {
        ctx.pushLoopContext(10, 50, "loop_0");
        ctx.pushLoopContext(60, 100, "loop_1");

        assertEquals("loop_1", ctx.getCurrentLoopLabel());

        ctx.popLoopContext();
        assertEquals("loop_0", ctx.getCurrentLoopLabel(),
                "After popping inner loop, should restore outer label");
    }

    @Test
    void testPopLoopContext_emptyStack_isNoOp() {
        ctx.popLoopContext(); // should not throw
        assertNull(ctx.getCurrentLoopContext());
    }

    @Test
    void testGetCurrentLoopContext_returnsHeaderAndEnd() {
        ctx.pushLoopContext(10, 50);
        int[] loopCtx = ctx.getCurrentLoopContext();
        assertNotNull(loopCtx);
        assertEquals(10, loopCtx[0]);
        assertEquals(50, loopCtx[1]);
    }

    @Test
    void testThreeLevelNesting_labelsCorrect() {
        ctx.pushLoopContext(10, 50, "loop_0");
        ctx.pushLoopContext(60, 100, "loop_1");
        ctx.pushLoopContext(110, 150);

        // Break to outermost
        assertEquals("loop_0", ctx.findBreakLabel(50));
        // Break to middle
        assertEquals("loop_1", ctx.findBreakLabel(100));
        // Break from innermost
        assertNull(ctx.findBreakLabel(150));

        // Continue to outermost
        assertEquals("loop_0", ctx.findContinueLabel(10));
        // Continue to middle
        assertEquals("loop_1", ctx.findContinueLabel(60));
        // Continue from innermost
        assertNull(ctx.findContinueLabel(110));
    }
}
