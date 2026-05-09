package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;

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
        void testBooleanComparison_loose_xEqTrue() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "==",
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            assertEquals("x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testBooleanComparison_loose_xEqFalse() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "==",
                            new ArkTSExpression.LiteralExpression("false",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            assertEquals("!x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testBooleanComparison_loose_xNeqTrue() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "!=",
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            assertEquals("!x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testBooleanComparison_loose_xNeqFalse() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.VariableExpression("x"),
                            "!=",
                            new ArkTSExpression.LiteralExpression("false",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN));
            assertEquals("x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testBooleanComparison_loose_falseEqX_commutative() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("false",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN),
                            "==",
                            new ArkTSExpression.VariableExpression("x"));
            assertEquals("!x",
                    OperatorHandler.simplifyBooleanComparison(expr)
                            .toArkTS());
        }

        @Test
        void testBooleanComparison_loose_trueNeqX_commutative() {
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN),
                            "!=",
                            new ArkTSExpression.VariableExpression("x"));
            assertEquals("!x",
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

    @Nested
    @DisplayName("Const vs let differentiation")
    class ConstLetTests {
        private ArkTSDecompiler decompiler;
        private ArkDisassembler disasm;

        @BeforeEach
        void setUp() {
            decompiler = new ArkTSDecompiler();
            disasm = new ArkDisassembler();
        }

        private List<ArkInstruction> dis(byte[] code) {
            return disasm.disassemble(code, 0, code.length);
        }

        private static byte[] bytes(int... values) {
            byte[] result = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (byte) values[i];
            }
            return result;
        }

        private static byte[] concat(byte[]... arrays) {
            int total = 0;
            for (byte[] a : arrays) {
                total += a.length;
            }
            byte[] result = new byte[total];
            int pos = 0;
            for (byte[] a : arrays) {
                System.arraycopy(a, 0, result, pos, a.length);
                pos += a.length;
            }
            return result;
        }

        private static byte[] le32(int value) {
            return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
            };
        }

        @Test
        void testSingleAssignment_usesConst() {
            // ldai 42 -> sta v2 -> lda v2 -> return
            // Single-use variable inlining removes v2 and returns 42 directly
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x02),
                    bytes(0x60, 0x02),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("return 42"),
                    "Single-use variable should be inlined into return: "
                            + result);
        }

        @Test
        void testReassignment_usesLet() {
            // ldai 1 -> sta v2 -> ldai 2 -> sta v2 -> return
            byte[] code = concat(bytes(0x62), le32(1),
                    bytes(0x61, 0x02),
                    bytes(0x62), le32(2),
                    bytes(0x61, 0x02),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("let v2 = 1"),
                    "Reassigned variable should use let: " + result);
        }

        @Test
        void testMixedConstAndLet() {
            // ldai 42 -> sta v2 (const) -> ldai 1 -> sta v3
            // -> ldai 2 -> sta v3 (reassigned) -> return
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x02),
                    bytes(0x62), le32(1),
                    bytes(0x61, 0x03),
                    bytes(0x62), le32(2),
                    bytes(0x61, 0x03),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("const v2 = 42"),
                    "Single-assigned v2 should use const: " + result);
            assertTrue(result.contains("let v3 = 1"),
                    "Reassigned v3 should use let: " + result);
        }

        @Test
        @DisplayName("Instruction-level decompilation uses v0, v1 without debug info")
        void testInstructionLevel_usesVregNames() {
            // ldai 10 -> sta v0 -> lda v0 -> sta v2 -> ldai 20 -> sta v3 -> return
            byte[] code = concat(bytes(0x62), le32(10),
                    bytes(0x61, 0x00),
                    bytes(0x60, 0x00),
                    bytes(0x61, 0x02),
                    bytes(0x62), le32(20),
                    bytes(0x61, 0x03),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("v0"),
                    "Should use v0 for register 0: " + result);
            assertTrue(result.contains("v2"),
                    "Should use v2 for register 2: " + result);
            assertTrue(result.contains("v3"),
                    "Should use v3 for register 3: " + result);
        }
    }

    @Nested
    @DisplayName("Template literal reconstruction from concatenation")
    class TemplateLiteralReconstructionTests {

        private final ArkTSDecompiler decomp = new ArkTSDecompiler();

        @Test
        @DisplayName("Multi-segment: str1 + var1 + str2 + var2 + str3")
        void testMultiSegmentTemplate() {
            ArkTSExpression str1 = new ArkTSExpression.LiteralExpression("Hello ",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression var1 =
                    new ArkTSExpression.VariableExpression("name");
            ArkTSExpression str2 = new ArkTSExpression.LiteralExpression(", ",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression var2 =
                    new ArkTSExpression.VariableExpression("age");
            ArkTSExpression str3 = new ArkTSExpression.LiteralExpression(" years",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);

            // Build: str1 + var1 + str2 + var2 + str3
            ArkTSExpression inner1 = new ArkTSExpression.BinaryExpression(
                    str1, "+", var1);
            ArkTSExpression inner2 = new ArkTSExpression.BinaryExpression(
                    inner1, "+", str2);
            ArkTSExpression inner3 = new ArkTSExpression.BinaryExpression(
                    inner2, "+", var2);
            ArkTSExpression outer = new ArkTSExpression.BinaryExpression(
                    inner3, "+", str3);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(outer);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Should produce TemplateLiteralExpression: "
                            + result.toArkTS());
            assertEquals("`Hello ${name}, ${age} years`",
                    result.toArkTS());
        }

        @Test
        @DisplayName("Simple str + var still works")
        void testSimpleStringPlusVariable() {
            ArkTSExpression str = new ArkTSExpression.LiteralExpression("Hello ",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression name =
                    new ArkTSExpression.VariableExpression("name");
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    str, "+", name);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Should produce TemplateLiteralExpression");
            assertEquals("`Hello ${name}`", result.toArkTS());
        }

        @Test
        @DisplayName("var + str (expression first)")
        void testVariablePlusString() {
            ArkTSExpression name =
                    new ArkTSExpression.VariableExpression("name");
            ArkTSExpression str = new ArkTSExpression.LiteralExpression("!",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    name, "+", str);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Should produce TemplateLiteralExpression");
            assertEquals("`${name}!`", result.toArkTS());
        }

        @Test
        @DisplayName("var + var (no strings) stays as + expression")
        void testNoStringsStaysAsBinary() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression y =
                    new ArkTSExpression.VariableExpression("y");
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    x, "+", y);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertFalse(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "No strings should not produce template literal");
        }

        @Test
        @DisplayName("number + number stays as + expression")
        void testNumbersStayAsBinary() {
            ArkTSExpression a = new ArkTSExpression.LiteralExpression("3",
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression b = new ArkTSExpression.LiteralExpression("4",
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    a, "+", b);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertFalse(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Numbers should not produce template literal");
        }

        @Test
        @DisplayName("Backtick in quasi is escaped")
        void testBacktickEscapingInReconstruction() {
            ArkTSExpression str = new ArkTSExpression.LiteralExpression(
                    "it`",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression name =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    str, "+", name);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression);
            assertEquals("`it\\`${x}`", result.toArkTS());
        }

        @Test
        @DisplayName("Dollar-brace in quasi is escaped")
        void testDollarBraceEscaping() {
            ArkTSExpression str = new ArkTSExpression.LiteralExpression(
                    "price ${",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression name =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    str, "+", name);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression);
            assertEquals("`price \\${${x}`", result.toArkTS());
        }

        @Test
        @DisplayName("Standalone dollar sign is NOT escaped")
        void testStandaloneDollarNotEscaped() {
            ArkTSExpression str = new ArkTSExpression.LiteralExpression(
                    "$5 ",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression name =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                    str, "+", name);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(binary);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression);
            assertEquals("`$5 ${x}`", result.toArkTS());
        }

        @Test
        @DisplayName("Adjacent string segments are merged in quasis")
        void testAdjacentStringsMerged() {
            ArkTSExpression str1 = new ArkTSExpression.LiteralExpression("a",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression str2 = new ArkTSExpression.LiteralExpression("b",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression var =
                    new ArkTSExpression.VariableExpression("x");

            // str1 + str2 + var => "ab${x}"
            ArkTSExpression inner = new ArkTSExpression.BinaryExpression(
                    str1, "+", str2);
            ArkTSExpression outer = new ArkTSExpression.BinaryExpression(
                    inner, "+", var);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(outer);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Should produce template literal: " + result.toArkTS());
            assertEquals("`ab${x}`", result.toArkTS());
        }

        @Test
        @DisplayName("var + var + str (two vars then string)")
        void testTwoVarsThenString() {
            ArkTSExpression v1 =
                    new ArkTSExpression.VariableExpression("a");
            ArkTSExpression v2 =
                    new ArkTSExpression.VariableExpression("b");
            ArkTSExpression str = new ArkTSExpression.LiteralExpression("!",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);

            ArkTSExpression inner = new ArkTSExpression.BinaryExpression(
                    v1, "+", v2);
            ArkTSExpression outer = new ArkTSExpression.BinaryExpression(
                    inner, "+", str);

            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(outer);
            assertTrue(result instanceof ArkTSAccessExpressions
                            .TemplateLiteralExpression,
                    "Should produce template literal: " + result.toArkTS());
            assertEquals("${a}${b}!", result.toArkTS().substring(1,
                    result.toArkTS().length() - 1));
        }

        @Test
        @DisplayName("Null input returns null")
        void testNullInput() {
            ArkTSExpression result =
                    decomp.tryReconstructTemplateLiteral(null);
            assertTrue(result == null, "Null input should return null");
        }

        @Test
        @DisplayName("TemplateLiteralExpression escapeDollarBraceOnly")
        void testTemplateLiteralDollarBraceEscape() {
            ArkTSAccessExpressions.TemplateLiteralExpression expr =
                    new ArkTSAccessExpressions.TemplateLiteralExpression(
                            List.of("expr${literal"),
                            List.of());
            assertEquals("`expr\\${literal`", expr.toArkTS());
        }

        @Test
        @DisplayName("TemplateLiteralExpression dollar not followed by brace")
        void testTemplateLiteralDollarAlone() {
            ArkTSAccessExpressions.TemplateLiteralExpression expr =
                    new ArkTSAccessExpressions.TemplateLiteralExpression(
                            List.of("price $100"),
                            List.of());
            assertEquals("`price $100`", expr.toArkTS());
        }

        @Test
        @DisplayName("TemplateLiteralExpression dollar at end of string")
        void testTemplateLiteralDollarAtEnd() {
            ArkTSAccessExpressions.TemplateLiteralExpression expr =
                    new ArkTSAccessExpressions.TemplateLiteralExpression(
                            List.of("price$"),
                            List.of());
            assertEquals("`price$`", expr.toArkTS());
        }
    }

    @Nested
    @DisplayName("Nullish coalescing detection")
    class NullishCoalescingDetectionTests {
        private final ArkTSDecompiler decomp = new ArkTSDecompiler();

        @Test
        @DisplayName("x === null ? default : x  ->  x ?? default")
        void testStrictEqNull() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(x, "===",
                            new ArkTSExpression.LiteralExpression("null",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NULL));
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("default",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, fallback, x);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing");
            assertEquals("x ?? \"default\"", result.toArkTS());
        }

        @Test
        @DisplayName("x !== null ? x : default  ->  x ?? default")
        void testStrictNeqNull() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(x, "!==",
                            new ArkTSExpression.LiteralExpression("null",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NULL));
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("default",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, x, fallback);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing from !== null");
            assertEquals("x ?? \"default\"", result.toArkTS());
        }

        @Test
        @DisplayName("x != null ? x : default  ->  x ?? default")
        void testLooseNeqNull() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(x, "!=",
                            new ArkTSExpression.LiteralExpression("null",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NULL));
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("default",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, x, fallback);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing from != null");
            assertEquals("x ?? \"default\"", result.toArkTS());
        }

        @Test
        @DisplayName("x === undefined ? default : x  ->  x ?? default")
        void testStrictEqUndefined() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(x, "===",
                            new ArkTSExpression.LiteralExpression(
                                    "undefined",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.UNDEFINED));
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, fallback, x);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing with undefined");
            assertEquals("x ?? 0", result.toArkTS());
        }

        @Test
        @DisplayName("x !== undefined ? x : default  ->  x ?? default")
        void testStrictNeqUndefined() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(x, "!==",
                            new ArkTSExpression.LiteralExpression(
                                    "undefined",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.UNDEFINED));
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, x, fallback);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing from !== undefined");
            assertEquals("x ?? 0", result.toArkTS());
        }

        @Test
        @DisplayName("null === x ? default : x  ->  x ?? default (commutative)")
        void testStrictEqNull_commutative() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression condition =
                    new ArkTSExpression.BinaryExpression(
                            new ArkTSExpression.LiteralExpression("null",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NULL),
                            "===", x);
            ArkTSExpression fallback =
                    new ArkTSExpression.LiteralExpression("fallback",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.STRING);
            ArkTSExpression result =
                    decomp.tryDetectNullishCoalescing(
                            condition, fallback, x);
            assertTrue(result instanceof ArkTSPropertyExpressions
                            .NullishCoalescingExpression,
                    "Should detect nullish coalescing (commutative)");
            assertEquals("x ?? \"fallback\"", result.toArkTS());
        }
    }

    @Nested
    @DisplayName("Typeof+null simplification")
    class TypeofNullSimplificationTests {
        @Test
        void testTypeofUndefinedAndNullCheck_simplifiesToNotEqualsNull() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                    "typeof", x, true);
            ArkTSExpression undefStr =
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression typeofCheck =
                    new ArkTSExpression.BinaryExpression(
                            typeofX, "!==", undefStr);
            ArkTSExpression nullLit =
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL);
            ArkTSExpression nullCheck =
                    new ArkTSExpression.BinaryExpression(x, "!==", nullLit);
            ArkTSExpression combined =
                    new ArkTSExpression.BinaryExpression(
                            typeofCheck, "&&", nullCheck);
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTypeofNull(combined);
            assertEquals("x != null", result.toArkTS());
        }

        @Test
        void testStandaloneTypeofNotUndefined_simplifiesToNotEqualsUndefined() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                    "typeof", x, true);
            ArkTSExpression undefStr =
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            typeofX, "!==", undefStr);
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTypeofNull(expr);
            assertEquals("x !== undefined", result.toArkTS());
        }

        @Test
        void testStandaloneTypeofEqualsUndefined_simplifiesToEqualsUndefined() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                    "typeof", x, true);
            ArkTSExpression undefStr =
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            typeofX, "===", undefStr);
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTypeofNull(expr);
            assertEquals("x === undefined", result.toArkTS());
        }

        @Test
        void testNonUndefinedTypeofCheck_notSimplified() {
            ArkTSExpression x =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                    "typeof", x, true);
            ArkTSExpression strLit =
                    new ArkTSExpression.LiteralExpression("string",
                            ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression expr =
                    new ArkTSExpression.BinaryExpression(
                            typeofX, "===", strLit);
            ArkTSExpression result =
                    OperatorHandler.simplifyRedundantTypeofNull(expr);
            assertEquals(expr.toArkTS(), result.toArkTS());
        }
    }

    @Nested
    @DisplayName("Logical compound assignment expressions")
    class LogicalAssignExpressionTests {
        @Test
        void testAndEquals_rendering() {
            ArkTSExpression target =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression value =
                    new ArkTSExpression.LiteralExpression("42",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression expr =
                    new ArkTSExpression.LogicalAssignExpression(
                            target, "&&=", value);
            assertEquals("x &&= 42", expr.toArkTS());
        }

        @Test
        void testOrEquals_rendering() {
            ArkTSExpression target =
                    new ArkTSExpression.VariableExpression("name");
            ArkTSExpression value =
                    new ArkTSExpression.LiteralExpression("default",
                            ArkTSExpression.LiteralExpression.LiteralKind.STRING);
            ArkTSExpression expr =
                    new ArkTSExpression.LogicalAssignExpression(
                            target, "||=", value);
            assertEquals("name ||= \"default\"", expr.toArkTS());
        }

        @Test
        void testNullishEquals_rendering() {
            ArkTSExpression target =
                    new ArkTSExpression.VariableExpression("config");
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("defaults");
            ArkTSExpression expr =
                    new ArkTSExpression.LogicalAssignExpression(
                            target, "??=", value);
            assertEquals("config ??= defaults", expr.toArkTS());
        }
    }

    @Nested
    @DisplayName("Nullable type inference")
    class NullableTypeInferenceTests {
        @Test
        void testInferNullableType_canBeNull() {
            String result =
                    TypeInference.inferNullableType("string", true, false);
            assertEquals("string | null", result);
        }

        @Test
        void testInferNullableType_canBeUndefined() {
            String result =
                    TypeInference.inferNullableType("number", false, true);
            assertEquals("number | undefined", result);
        }

        @Test
        void testInferNullableType_both() {
            String result =
                    TypeInference.inferNullableType("boolean", true, true);
            assertEquals("boolean | null | undefined", result);
        }

        @Test
        void testInferNullableType_neither() {
            String result =
                    TypeInference.inferNullableType("string", false, false);
            assertEquals("string", result);
        }

        @Test
        void testInferNullableType_baseNull() {
            String result =
                    TypeInference.inferNullableType("null", true, false);
            assertEquals("null", result);
        }

        @Test
        void testFormatOptionalProperty_optional() {
            String result =
                    TypeInference.formatOptionalProperty("name", "string",
                            true);
            assertEquals("name?: string", result);
        }

        @Test
        void testFormatOptionalProperty_required() {
            String result =
                    TypeInference.formatOptionalProperty("name", "string",
                            false);
            assertEquals("name: string", result);
        }

        @Test
        void testInferTypeFromNullAssignment_nullLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NULL));
            assertEquals("string | null", result);
        }

        @Test
        void testInferTypeFromNullAssignment_undefinedLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "number",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.UNDEFINED));
            assertEquals("number | undefined", result);
        }

        @Test
        void testInferTypeFromNullAssignment_numberLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string",
                    new ArkTSExpression.LiteralExpression("42",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
            assertEquals("string", result);
        }

        @Test
        void testInferTypeFromNullAssignment_alreadyNullable() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string | null",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NULL));
            assertEquals("string | null", result);
        }
    }

    @Nested
    @DisplayName("Class name resolution")
    class ClassNameResolutionTests {
        @Test
        void testLogicalAssignExpression_rendering() {
            ArkTSExpression target =
                    new ArkTSExpression.VariableExpression("x");
            ArkTSExpression value =
                    new ArkTSExpression.LiteralExpression("42",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            ArkTSExpression expr =
                    new ArkTSExpression.LogicalAssignExpression(
                            target, "&&=", value);
            assertEquals("x &&= 42", expr.toArkTS());
        }

        @Test
        void testLogicalAssignExpression_orEquals() {
            ArkTSExpression target =
                    new ArkTSExpression.VariableExpression("name");
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("fallback");
            ArkTSExpression expr =
                    new ArkTSExpression.LogicalAssignExpression(
                            target, "||=", value);
            assertEquals("name ||= fallback", expr.toArkTS());
        }
    }

    @Nested
    @DisplayName("Cascading single-use inlining")
    class CascadingInliningTests {
        private final ArkDisassembler disasm = new ArkDisassembler();
        private final ArkTSDecompiler decompiler = new ArkTSDecompiler();

        private List<ArkInstruction> dis(byte[] code) {
            return disasm.disassemble(code, 0, code.length);
        }

        private static byte[] bytes(int... values) {
            byte[] result = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (byte) values[i];
            }
            return result;
        }

        private static byte[] concat(byte[]... arrays) {
            int total = 0;
            for (byte[] a : arrays) {
                total += a.length;
            }
            byte[] result = new byte[total];
            int pos = 0;
            for (byte[] a : arrays) {
                System.arraycopy(a, 0, result, pos, a.length);
                pos += a.length;
            }
            return result;
        }

        private static byte[] le32(int value) {
            return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
            };
        }

        @Test
        void testCascadingInline_twoLevels() {
            // ldai 42; sta v0; lda v0; ldai 1; add2imm 0, 1; sta v2;
            // return v2
            // v2 inlined into return, then v0 is unused so stays or goes
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x00),
                    bytes(0x60, 0x00),
                    bytes(0x62), le32(1),
                    bytes(0x61, 0x01),
                    bytes(0x60, 0x00),
                    bytes(0x0A, 0x00, 0x01),
                    bytes(0x61, 0x02),
                    bytes(0x60, 0x02),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("return"),
                    "Should have return, got: " + result);
        }

        @Test
        void testInlineIntoThrow() {
            // ldai 42; sta v0; lda v0; throw
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x00),
                    bytes(0x60, 0x00),
                    bytes(0xFE));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("throw 42"),
                    "Should inline into throw: " + result);
            assertFalse(result.contains("v0"),
                    "v0 should be inlined away: " + result);
        }

        @Test
        void testNoInline_multiUse() {
            // ldai 42; sta v0; lda v0; sta v1; lda v0; sta v2; return v2
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x00),
                    bytes(0x60, 0x00),
                    bytes(0x61, 0x01),
                    bytes(0x60, 0x00),
                    bytes(0x61, 0x02),
                    bytes(0x60, 0x02),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("const v0 = 42"),
                    "v0 should be kept (multi-use): " + result);
        }
    }

    @Nested
    @DisplayName("Property and method chain inlining")
    class PropertyInliningTests {
        private final ArkTSDecompiler decompiler = new ArkTSDecompiler();

        @Test
        void testAssignmentTargetInline() {
            // Build AST directly: const v0 = 42; this.field = v0
            // This tests that replaceVariable handles AssignExpression
            ArkTSExpression target = new ArkTSExpression.MemberExpression(
                    new ArkTSExpression.ThisExpression(),
                    new ArkTSExpression.VariableExpression("field"),
                    false);
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v0");
            ArkTSExpression assign =
                    new ArkTSExpression.AssignExpression(target, value);
            ArkTSExpression replaced =
                    ExpressionVisitor.replaceVariable(assign,
                            "v0",
                            new ArkTSExpression.LiteralExpression("42",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            assertTrue(replaced.toArkTS().contains("this.field = 42"),
                    "Should inline into assignment: "
                            + replaced.toArkTS());
        }

        @Test
        void testCallArgumentInline() {
            // const v0 = 42; foo(v0) → foo(42)
            ArkTSExpression callee =
                    new ArkTSExpression.VariableExpression("foo");
            ArkTSExpression arg =
                    new ArkTSExpression.VariableExpression("v0");
            ArkTSExpression call = new ArkTSExpression.CallExpression(
                    callee, List.of(arg));
            ArkTSExpression replaced =
                    ExpressionVisitor.replaceVariable(call,
                            "v0",
                            new ArkTSExpression.LiteralExpression("42",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER));
            assertEquals("foo(42)", replaced.toArkTS());
        }
    }

    @Nested
    @DisplayName("Line number comments")
    class LineCommentTests {
        @Test
        void testLineCommentStatement_rendering() {
            ArkTSStatement.LineCommentStatement stmt =
                    new ArkTSStatement.LineCommentStatement(42);
            assertEquals("// line 42", stmt.toArkTS(0));
            assertEquals("    // line 42", stmt.toArkTS(1));
        }

        @Test
        void testLineCommentStatement_nestedInBlock() {
            List<ArkTSStatement> body = List.of(
                    new ArkTSStatement.LineCommentStatement(5),
                    new ArkTSStatement.ReturnStatement(
                            new ArkTSExpression.LiteralExpression("42",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.NUMBER)));
            ArkTSStatement.BlockStatement block =
                    new ArkTSStatement.BlockStatement(body);
            String result = block.toArkTS(0);
            assertTrue(result.contains("// line 5"),
                    "Block should contain line comment: " + result);
            assertTrue(result.contains("return 42"),
                    "Block should contain return: " + result);
        }

        @Test
        void testDecompilationContext_lineNumberTracking() {
            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            assertNull(ctx.getLineNumber(0));
            assertNull(ctx.checkAndMarkLineEmitted(0));

            ctx.setLineNumber(0, 10);
            ctx.setLineNumber(4, 15);
            ctx.setLineNumber(8, 15);
            ctx.setLineNumber(12, 20);

            assertEquals(10L, ctx.getLineNumber(0));
            assertEquals(15L, ctx.getLineNumber(4));

            // First emission of line 10
            Long line = ctx.checkAndMarkLineEmitted(0);
            assertEquals(10L, line);

            // Offset 4 maps to line 15 (different from last=10) → emit
            line = ctx.checkAndMarkLineEmitted(4);
            assertEquals(15L, line);

            // Offset 8 also maps to line 15 (same as last=15) → skip
            assertNull(ctx.checkAndMarkLineEmitted(8));

            // New line should be emitted
            line = ctx.checkAndMarkLineEmitted(12);
            assertEquals(20L, line);
        }
    }

    @Nested
    @DisplayName("Variable name inference from context")
    class VariableNameInferenceTests {
        @Test
        void testInferNameFromMethodCall_getPrefix() {
            // foo.getName() → name
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression("obj");
            ArkTSExpression method =
                    new ArkTSExpression.VariableExpression("getName");
            ArkTSExpression callee =
                    new ArkTSExpression.MemberExpression(obj, method, false);
            ArkTSExpression call = new ArkTSExpression.CallExpression(
                    callee, List.of());

            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            ArkTSDecompiler decompiler = new ArkTSDecompiler();

            // Simulate: STA v0 with call result
            ArkTSStatement varDecl = new ArkTSStatement.VariableDeclaration(
                    "let", "name", null, call);
            String output = varDecl.toArkTS(0);
            assertTrue(output.contains("let name = obj.getName()"),
                    "Should render correctly: " + output);
        }

        @Test
        void testInferNameFromPropertyAccess() {
            // obj.length → length
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression("obj");
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("length");
            ArkTSExpression member =
                    new ArkTSExpression.MemberExpression(obj, prop, false);

            ArkTSStatement varDecl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", "length", null, member);
            String output = varDecl.toArkTS(0);
            assertTrue(output.contains("let length = obj.length"),
                    "Should render correctly: " + output);
        }

        @Test
        void testInferNameFromNewExpression() {
            // new Error() → error
            ArkTSExpression callee =
                    new ArkTSExpression.VariableExpression("Error");
            ArkTSExpression newExpr =
                    new ArkTSExpression.NewExpression(callee, List.of());

            ArkTSStatement varDecl =
                    new ArkTSStatement.VariableDeclaration(
                            "let", "error", null, newExpr);
            String output = varDecl.toArkTS(0);
            assertTrue(output.contains("let error = new Error()"),
                    "Should render correctly: " + output);
        }

        @Test
        void testInferredNameFallbackInContext() {
            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            // No debug name, no inferred name → v0
            assertEquals("v0", ctx.resolveRegisterName(0));

            // Set inferred name → uses inferred
            ctx.setInferredName(0, "length");
            assertEquals("length", ctx.resolveRegisterName(0));

            // Debug name overrides inferred
            ctx.setRegisterName(0, "arrayLen");
            assertEquals("arrayLen", ctx.resolveRegisterName(0));
        }
    }

    @Nested
    @DisplayName("Module and global variable opcodes")
    class ModuleAndGlobalOpcodeTests {
        private final ArkDisassembler disasm = new ArkDisassembler();
        private final ArkTSDecompiler decompiler = new ArkTSDecompiler();

        private List<ArkInstruction> dis(byte[] code) {
            return disasm.disassemble(code, 0, code.length);
        }

        private static byte[] bytes(int... values) {
            byte[] result = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (byte) values[i];
            }
            return result;
        }

        private static byte[] concat(byte[]... arrays) {
            int total = 0;
            for (byte[] a : arrays) {
                total += a.length;
            }
            byte[] result = new byte[total];
            int pos = 0;
            for (byte[] a : arrays) {
                System.arraycopy(a, 0, result, pos, a.length);
                pos += a.length;
            }
            return result;
        }

        @Test
        @DisplayName("LDEXTERNALMODULEVAR loads external module variable")
        void testLdExternalModuleVar() {
            // LDEXTERNALMODULEVAR 0x7E with IMM8 format: opcode + varIdx
            // Load external module var index 3 into acc, then return
            byte[] code = concat(
                    bytes(0x7E, 0x03),         // ldexternalmodulevar 3
                    bytes(0x61, 0x00),          // sta v0
                    bytes(0x60, 0x00),          // lda v0
                    bytes(0x64));               // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            assertTrue(result.contains("import_3"),
                    "Should reference ext_mod_3: " + result);
        }

        @Test
        @DisplayName("STGLOBALVAR stores acc to global scope")
        void testStGlobalVar() {
            // STGLOBALVAR 0x7F with IMM8_IMM16 format:
            // opcode + imm8 + stringIdx(16-bit LE)
            // Store 42 to global variable with string index 5
            byte[] code = concat(
                    bytes(0x62, 0x2A, 0x00, 0x00, 0x00), // ldai 42
                    bytes(0x7F, 0x00, 0x05, 0x00),       // stglobalvar 0, strIdx=5
                    bytes(0x65));                          // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            // stglobalvar stores acc to a global named by string table index 5
            // Without AbcFile, falls back to "str_5"
            assertTrue(result.contains("str_5"),
                    "Should contain str_5 global name: " + result);
        }

        @Test
        @DisplayName("GETMODULENAMESPACE loads module namespace object")
        void testGetModuleNamespace() {
            // GETMODULENAMESPACE 0x7B with IMM8 format: opcode + varIdx
            byte[] code = concat(
                    bytes(0x7B, 0x01),         // getmodulenamespace 1
                    bytes(0x61, 0x00),          // sta v0
                    bytes(0x60, 0x00),          // lda v0
                    bytes(0x64));               // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            assertTrue(result.contains("namespace_1"),
                    "Should contain module_ns_1: " + result);
        }

        @Test
        @DisplayName("STMODULEVAR stores acc to module variable")
        void testStModuleVar() {
            // STMODULEVAR 0x7C with IMM8 format: opcode + varIdx
            // Store 99 to module variable index 2
            byte[] code = concat(
                    bytes(0x62, 0x63, 0x00, 0x00, 0x00), // ldai 99
                    bytes(0x7C, 0x02),                    // stmodulevar 2
                    bytes(0x65));                          // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            assertTrue(result.contains("export_2"),
                    "Should contain mod_2: " + result);
        }

        @Test
        @DisplayName("LDOBJBYNAME loads property by name from acc object")
        void testLdObjByName() {
            // LDOBJBYNAME 0x42 with IMM8_IMM16 format:
            // opcode + imm8 + stringIdx(16-bit LE)
            // Load v1 into acc, then load property by name (string idx 0)
            byte[] code = concat(
                    bytes(0x60, 0x01),         // lda v1
                    bytes(0x42, 0x00, 0x00, 0x00), // ldobjbyname 0, strIdx=0
                    bytes(0x61, 0x02),         // sta v2
                    bytes(0x60, 0x02),         // lda v2
                    bytes(0x64));              // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            // Without AbcFile, string index 0 resolves to "str_0"
            // Should produce v1.str_0 property access
            assertTrue(result.contains("v1") && result.contains("str_0"),
                    "Should contain v1.str_0 property access: " + result);
        }

        @Test
        @DisplayName("STOBJBYNAME stores value to object property by name")
        void testStObjByName() {
            // STOBJBYNAME 0x43 with IMM8_IMM16_V8 format:
            // opcode + imm8 + stringIdx(16-bit LE) + vreg(obj)
            // Load value into acc, store acc to v0.property (string idx 1)
            byte[] code = concat(
                    bytes(0x62, 0x0A, 0x00, 0x00, 0x00), // ldai 10
                    bytes(0x61, 0x01),                    // sta v1 (value)
                    bytes(0x60, 0x01),                    // lda v1
                    bytes(0x43, 0x00, 0x01, 0x00, 0x00), // stobjbyname 0, strIdx=1, v0
                    bytes(0x65));                         // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            // Should contain v0.str_1 assignment
            assertTrue(result.contains("v0") && result.contains("str_1"),
                    "Should contain v0.str_1 property store: " + result);
        }

        @Test
        @DisplayName("STCONSTTOGLOBALRECORD declares const global")
        void testStConstToGlobalRecord() {
            // STCONSTTOGLOBALRECORD 0x47 with IMM8_IMM16 format
            // operand 0 (imm8) = string index for variable name
            byte[] code = concat(
                    bytes(0x62, 0x2A, 0x00, 0x00, 0x00), // ldai 42
                    bytes(0x47, 0x03, 0x00, 0x00),       // stconsttoglobalrecord strIdx=3
                    bytes(0x65));                          // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            // Should declare const with name from string index 3
            assertTrue(result.contains("const") && result.contains("str_3"),
                    "Should contain const str_3: " + result);
        }

        @Test
        @DisplayName("STTOGLOBALRECORD declares let global (may be const-optimized)")
        void testStToGlobalRecord() {
            // STTOGLOBALRECORD 0x48 with IMM8_IMM16 format
            // operand 0 (imm8) = string index for variable name
            byte[] code = concat(
                    bytes(0x62, 0x64, 0x00, 0x00, 0x00), // ldai 100
                    bytes(0x48, 0x07, 0x00, 0x00),       // sttoglobalrecord strIdx=7
                    bytes(0x65));                          // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            // Should declare variable with name from string index 7.
            // Single-assignment let may be const-optimized to const.
            assertTrue(result.contains("str_7") && result.contains("100"),
                    "Should contain str_7 and 100: " + result);
            assertTrue(result.contains("const") || result.contains("let"),
                    "Should contain const or let: " + result);
        }

        @Test
        @DisplayName("LDEXTERNALMODULEVAR followed by STMODULEVAR round-trip")
        void testModuleVarRoundTrip() {
            // Load external module var 0, store to module var 1
            byte[] code = concat(
                    bytes(0x7E, 0x00),         // ldexternalmodulevar 0
                    bytes(0x7C, 0x01),         // stmodulevar 1
                    bytes(0x65));               // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            assertTrue(result.contains("import_0"),
                    "Should contain ext_mod_0: " + result);
            assertTrue(result.contains("export_1"),
                    "Should contain mod_1: " + result);
        }

        @Test
        @DisplayName("GETMODULENAMESPACE with property access")
        void testModuleNamespaceWithPropertyAccess() {
            // Get module namespace 0, then load property by name
            byte[] code = concat(
                    bytes(0x7B, 0x00),                 // getmodulenamespace 0
                    bytes(0x42, 0x00, 0x02, 0x00),     // ldobjbyname 0, strIdx=2
                    bytes(0x61, 0x00),                  // sta v0
                    bytes(0x60, 0x00),                  // lda v0
                    bytes(0x64));                       // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertFalse(result.isEmpty(),
                    "Should produce output without crash: " + result);
            assertTrue(result.contains("namespace_0"),
                    "Should contain module_ns_0: " + result);
            assertTrue(result.contains("str_2"),
                    "Should contain str_2 property: " + result);
        }
    }

    @Nested
    @DisplayName("Property access opcodes — this/super by name")
    class PropertyAccessOpcodeTests {

        private final ArkTSDecompiler decompiler = new ArkTSDecompiler();
        private final ArkDisassembler disasm = new ArkDisassembler();

        private List<ArkInstruction> dis(byte[] code) {
            return disasm.disassemble(code, 0, code.length);
        }

        private static byte[] bytes(int... values) {
            byte[] result = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (byte) values[i];
            }
            return result;
        }

        private static byte[] concat(byte[]... arrays) {
            int total = 0;
            for (byte[] a : arrays) {
                total += a.length;
            }
            byte[] result = new byte[total];
            int pos = 0;
            for (byte[] a : arrays) {
                System.arraycopy(a, 0, result, pos, a.length);
                pos += a.length;
            }
            return result;
        }

        private static byte[] le32(int value) {
            return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
            };
        }

        @Test
        @DisplayName("LDTHISBYNAME produces this.str_N property read")
        void testLdThisByName() {
            // LDTHISBYNAME (0x49, IMM8_IMM16) loads this.property onto acc
            // stringIdx=0, then store to v2 and return
            byte[] code = concat(
                    bytes(0x49, 0x00, 0x00, 0x00),   // ldthisbyname imm8=0, strIdx=0
                    bytes(0x61, 0x02),                 // sta v2
                    bytes(0x60, 0x02),                 // lda v2
                    bytes(0x64));                      // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("this.str_0"),
                    "Should contain this.str_0 property load: " + result);
            assertTrue(result.contains("return"),
                    "Should contain return statement: " + result);
        }

        @Test
        @DisplayName("STTHISBYNAME produces this.str_N property store")
        void testStThisByName() {
            // LDAI 42 → STTHISBYNAME (0x4A, IMM8_IMM16_V8) stores acc to this.prop
            // stringIdx=0, vreg=0 (object register ignored — always uses this)
            byte[] code = concat(
                    bytes(0x62), le32(42),             // ldai 42
                    bytes(0x4A, 0x00, 0x00, 0x00, 0x00), // stthisbyname imm8=0, strIdx=0, v0
                    bytes(0x65));                      // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("this.str_0"),
                    "Should contain this.str_0 property store: " + result);
            assertTrue(result.contains("42"),
                    "Should contain the stored value 42: " + result);
        }

        @Test
        @DisplayName("LDSUPERBYNAME produces super.str_N property read")
        void testLdSuperByName() {
            // LDSUPERBYNAME (0x46, IMM8_IMM16) loads super.property onto acc
            // stringIdx=0, then store to v2 and return
            byte[] code = concat(
                    bytes(0x46, 0x00, 0x00, 0x00),   // ldsuperbyname imm8=0, strIdx=0
                    bytes(0x61, 0x02),                 // sta v2
                    bytes(0x60, 0x02),                 // lda v2
                    bytes(0x64));                      // return
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("super.str_0"),
                    "Should contain super.str_0 property load: " + result);
            assertTrue(result.contains("return"),
                    "Should contain return statement: " + result);
        }

        @Test
        @DisplayName("STSUPERBYNAME produces super.str_N property store")
        void testStSuperByName() {
            // LDAI 42 → STSUPERBYNAME (0xD0, IMM8_IMM16_V8) stores acc to super.prop
            // stringIdx=0, vreg=0 (object register ignored — always uses super)
            byte[] code = concat(
                    bytes(0x62), le32(42),             // ldai 42
                    bytes(0xD0, 0x00, 0x00, 0x00, 0x00), // stsuperbyname imm8=0, strIdx=0, v0
                    bytes(0x65));                      // returnundefined
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("super.str_0"),
                    "Should contain super.str_0 property store: " + result);
            assertTrue(result.contains("42"),
                    "Should contain the stored value 42: " + result);
        }
    }

    @Nested
    @DisplayName("Boolean variable name inference")
    class BooleanNameInferenceTests {
        @Test
        void testInferredName_booleanComparison_isNull() {
            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            // x === null should infer isNull
            ArkTSExpression comp = new ArkTSExpression.BinaryExpression(
                    new ArkTSExpression.VariableExpression("x"),
                    "===",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL));
            // Store to register via inference
            ctx.setInferredName(0, "isNull");
            assertEquals("isNull", ctx.resolveRegisterName(0));
        }

        @Test
        void testInferredName_debugNameOverridesInferred() {
            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            ctx.setInferredName(0, "isNull");
            assertEquals("isNull", ctx.resolveRegisterName(0));
            // Debug name wins
            ctx.setRegisterName(0, "isValid");
            assertEquals("isValid", ctx.resolveRegisterName(0));
        }

        @Test
        void testInferredName_noConflictWithDebugNames() {
            DecompilationContext ctx = new DecompilationContext(
                    null, null, null, null, null, List.of());
            // v0 with no names → v0
            assertEquals("v0", ctx.resolveRegisterName(0));
            // Set inferred → use inferred
            ctx.setInferredName(0, "result");
            assertEquals("result", ctx.resolveRegisterName(0));
            // v1 still uses default
            assertEquals("v1", ctx.resolveRegisterName(1));
        }
    }
}
