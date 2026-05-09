package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            byte[] code = concat(bytes(0x62), le32(42),
                    bytes(0x61, 0x02),
                    bytes(0x60, 0x02),
                    bytes(0x64));
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertTrue(result.contains("const v2 = 42"),
                    "Single-assignment should use const: " + result);
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
}
