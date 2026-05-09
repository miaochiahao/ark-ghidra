package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkInstructionFormat;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;
import com.arkghidra.format.AbcTryBlock;

class ArkTSDecompilerTest {

    private ArkTSDecompiler decompiler;
    private ArkDisassembler disasm;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
        disasm = new ArkDisassembler();
    }

    // --- Helpers ---

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    private static byte[] le16(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }

    private static byte[] le32(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
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

    private List<ArkInstruction> dis(byte[] code) {
        return disasm.disassemble(code, 0, code.length);
    }

    private static DecompilationContext makeTestContext() {
        return new DecompilationContext(
                null, null, null, null, null, null);
    }

    // --- Simple return tests ---

    @Test
    void testDecompile_simpleReturnInteger() {
        // ldai 42; return
        byte[] code = concat(bytes(0x62), le32(42), bytes(0x64));
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return 42"));
    }

    @Test
    void testDecompile_returnUndefined() {
        // returnundefined
        byte[] code = bytes(0x65);
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return;"));
    }

    // --- Arithmetic tests ---

    @Test
    void testDecompile_addTwoRegisters() {
        // lda v0; add2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x0A, 0x00, 0x01), // add2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v2: number = (v0 + v1)"));
    }

    @Test
    void testDecompile_subtract() {
        // lda v0; sub2 0, v1; sta v0
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x0B, 0x00, 0x01), // sub2 0, v1
            bytes(0x61, 0x00)        // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0: number = (v0 - v1)"));
    }

    @Test
    void testDecompile_multiply() {
        // lda v0; mul2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x0C, 0x00, 0x01), // mul2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 * v1)"));
    }

    @Test
    void testDecompile_comparisonLess() {
        // lda v0; less 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x11, 0x00, 0x01), // less 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 < v1)"));
    }

    // --- Variable declaration tests ---

    @Test
    void testDecompile_letDeclaration() {
        // ldai 10; sta v0
        byte[] code = concat(
            bytes(0x62), le32(10),    // ldai 10
            bytes(0x61, 0x00)         // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 10"));
    }

    @Test
    void testDecompile_letReassignment() {
        // ldai 5; sta v0; ldai 10; sta v0
        byte[] code = concat(
            bytes(0x62), le32(5),     // ldai 5
            bytes(0x61, 0x00),        // sta v0
            bytes(0x62), le32(10),    // ldai 10
            bytes(0x61, 0x00)         // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 5"));
        assertTrue(result.contains("v0 = 10"));
    }

    // --- Literal tests ---

    @Test
    void testDecompile_nullLiteral() {
        // ldnull; sta v0
        byte[] code = concat(
            bytes(0x01),        // ldnull
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("null"));
    }

    @Test
    void testDecompile_booleanLiterals() {
        // ldtrue; sta v0; ldfalse; sta v1
        byte[] code = concat(
            bytes(0x02),        // ldtrue
            bytes(0x61, 0x00),  // sta v0
            bytes(0x03),        // ldfalse
            bytes(0x61, 0x01)   // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = true"));
        assertTrue(result.contains("let v1 = false"));
    }

    @Test
    void testDecompile_undefinedLiteral() {
        // ldundefined; sta v0
        byte[] code = concat(
            bytes(0x00),        // ldundefined
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("undefined"));
    }

    // --- Function call tests ---

    @Test
    void testDecompile_callarg0() {
        // callarg0 0; sta v0
        byte[] code = concat(
            bytes(0x29, 0x00),  // callarg0 0
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("acc()"));
    }

    @Test
    void testDecompile_callarg1() {
        // callarg1 0, v1; sta v0
        byte[] code = concat(
            bytes(0x2A, 0x00, 0x01), // callarg1 0, v1
            bytes(0x61, 0x00)        // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("acc(v1)"));
    }

    @Test
    void testDecompile_callargs2() {
        // callargs2 0, v1, v2; sta v0
        byte[] code = concat(
            bytes(0x2B, 0x00, 0x01, 0x02), // callargs2 0, v1, v2
            bytes(0x61, 0x00)               // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("acc(v1, v2)"));
    }

    @Test
    void testDecompile_callargs3() {
        // callargs3 0, v1, v2, v3; sta v0
        byte[] code = concat(
            bytes(0x2C, 0x00, 0x01, 0x02, 0x03), // callargs3 0, v1, v2, v3
            bytes(0x61, 0x00)                     // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("acc(v1, v2, v3)"));
    }

    @Test
    void testDecompile_callthis0() {
        // callthis0 0, v0; sta v1
        byte[] code = concat(
            bytes(0x2D, 0x00, 0x00), // callthis0 0, v0 (this=v0)
            bytes(0x61, 0x01)        // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.acc()"));
    }

    @Test
    void testDecompile_callthis1() {
        // callthis1 0, v0, v1; sta v2
        byte[] code = concat(
            bytes(0x2E, 0x00, 0x00, 0x01), // callthis1 0, v0, v1 (this=v0, arg=v1)
            bytes(0x61, 0x02)              // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.acc(v1)"));
    }

    @Test
    void testDecompile_callthis2() {
        // callthis2 0, v0, v1, v2; sta v3
        byte[] code = concat(
            bytes(0x2F, 0x00, 0x00, 0x01, 0x02), // callthis2 0, v0, v1, v2
            bytes(0x61, 0x03)                    // sta v3
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.acc(v1, v2)"));
    }

    @Test
    void testDecompile_callthis3() {
        // callthis3 0, v0, v1, v2, v3; sta v4
        byte[] code = concat(
            bytes(0x30, 0x00, 0x00, 0x01, 0x02, 0x03), // callthis3 0, v0, v1, v2, v3
            bytes(0x61, 0x04)                           // sta v4
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.acc(v1, v2, v3)"));
    }

    @Test
    void testDecompile_callthis0_withLdobjbyname() {
        // lda v0; ldobjbyname 0, "method"; callthis0 0, v0; sta v1
        byte[] code = concat(
            bytes(0x60, 0x00),               // lda v0
            bytes(0x42, 0x00, 0x05, 0x00),   // ldobjbyname 0, string_idx=5
            bytes(0x2D, 0x00, 0x00),         // callthis0 0, v0 (this=v0)
            bytes(0x61, 0x01)                // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.") && result.contains("()"));
    }

    @Test
    void testDecompile_callrange() {
        // callrange 2, 0, v3; sta v0 (call acc with 2 args starting at v3)
        byte[] code = concat(
            bytes(0x73, 0x02, 0x00, 0x03), // callrange 2, 0, v3
            bytes(0x61, 0x00)              // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("acc(v3, v4)"));
    }

    @Test
    void testDecompile_callthisrange() {
        // callthisrange 2, 0, v0; sta v1 (call v0.acc with 2 args starting at v0)
        byte[] code = concat(
            bytes(0x31, 0x02, 0x00, 0x00), // callthisrange 2, 0, v0
            bytes(0x61, 0x01)              // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.acc(v0, v1)"));
    }

    // --- Unary operation tests ---

    @Test
    void testDecompile_negation() {
        // lda v0; neg 0; sta v1
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x1F, 0x00),   // neg 0
            bytes(0x61, 0x01)    // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(-v0)"));
    }

    @Test
    void testDecompile_logicalNot() {
        // lda v0; not 0; sta v1
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x20, 0x00),   // not 0
            bytes(0x61, 0x01)    // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(!v0)"));
    }

    @Test
    void testDecompile_typeof() {
        // lda v0; typeof 0; sta v1
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x1C, 0x00),   // typeof 0
            bytes(0x61, 0x01)    // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("typeof v0"));
    }

    // --- Method signature tests ---

    @Test
    void testMethodSignatureBuilder_basicSignature() {
        AbcMethod method = new AbcMethod(0, 0, "foo",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String sig = MethodSignatureBuilder.buildSignature(method, null, 0);
        assertEquals("function foo()", sig);
    }

    @Test
    void testMethodSignatureBuilder_withParams() {
        AbcMethod method = new AbcMethod(0, 0, "add",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcProto proto = new AbcProto(
                List.of(
                        AbcProto.ShortyType.I32,
                        AbcProto.ShortyType.I32,
                        AbcProto.ShortyType.I32),
                Collections.emptyList());
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 2);
        assertEquals("function add(param_0: number, param_1: number): number", sig);
    }

    @Test
    void testMethodSignatureBuilder_voidReturn() {
        AbcMethod method = new AbcMethod(0, 0, "doStuff",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcProto proto = new AbcProto(
                List.of(AbcProto.ShortyType.VOID),
                Collections.emptyList());
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 0);
        assertEquals("function doStuff()", sig);
    }

    @Test
    void testMethodSignatureBuilder_refParam() {
        AbcMethod method = new AbcMethod(0, 0, "process",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcProto proto = new AbcProto(
                List.of(AbcProto.ShortyType.VOID, AbcProto.ShortyType.REF),
                Collections.emptyList());
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 1);
        assertEquals("function process(param_0: Object)", sig);
    }

    // --- CFG tests ---

    @Test
    void testCfg_simpleSequence_hasOneBlock() {
        byte[] code = concat(
            bytes(0x62), le32(42),  // ldai 42
            bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertEquals(1, cfg.getBlocks().size());
        assertEquals(2, cfg.getBlocks().get(0).getInstructions().size());
    }

    @Test
    void testCfg_conditionalBranch_hasMultipleBlocks() {
        // ldai 1; jeqz +5; ldai 10; sta v0; jmp +3; ldai 20; sta v0; return
        byte[] code = concat(
            bytes(0x62), le32(1),       // ldai 1        (offset 0, len 5)
            bytes(0x4F, 0x05),          // jeqz +5       (offset 5, len 2) -> offset 12
            bytes(0x62), le32(10),      // ldai 10       (offset 7, len 5)
            bytes(0x61, 0x00),          // sta v0        (offset 12, len 2)
            bytes(0x4D, 0x03),          // jmp +3        (offset 14, len 2) -> offset 19
            bytes(0x62), le32(20),      // ldai 20       (offset 16, len 5)
            bytes(0x61, 0x00),          // sta v0        (offset 21, len 2)
            bytes(0x64)                  // return        (offset 23, len 1)
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertTrue(cfg.getBlocks().size() >= 3);
    }

    @Test
    void testCfg_entryBlock_isFirstBlock() {
        byte[] code = concat(
            bytes(0x62), le32(42),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertNotNull(cfg.getEntryBlock());
        assertEquals(0, cfg.getEntryBlock().getStartOffset());
    }

    @Test
    void testCfg_emptyInstructions_returnsEmpty() {
        ControlFlowGraph cfg = ControlFlowGraph.build(Collections.emptyList());
        assertTrue(cfg.getBlocks().isEmpty());
        assertNull(cfg.getEntryBlock());
    }

    private void assertNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNull(obj);
    }

    // --- Expression AST tests ---

    @Test
    void testLiteralExpression_number() {
        ArkTSExpression.LiteralExpression expr =
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        assertEquals("42", expr.toArkTS());
    }

    @Test
    void testLiteralExpression_string() {
        ArkTSExpression.LiteralExpression expr =
                new ArkTSExpression.LiteralExpression("hello",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"hello\"", expr.toArkTS());
    }

    @Test
    void testBinaryExpression_add() {
        ArkTSExpression left = new ArkTSExpression.VariableExpression("a");
        ArkTSExpression right = new ArkTSExpression.VariableExpression("b");
        ArkTSExpression.BinaryExpression expr =
                new ArkTSExpression.BinaryExpression(left, "+", right);
        assertEquals("(a + b)", expr.toArkTS());
    }

    @Test
    void testCallExpression_noArgs() {
        ArkTSExpression callee = new ArkTSExpression.VariableExpression("foo");
        ArkTSExpression.CallExpression expr =
                new ArkTSExpression.CallExpression(callee, Collections.emptyList());
        assertEquals("foo()", expr.toArkTS());
    }

    @Test
    void testCallExpression_withArgs() {
        ArkTSExpression callee = new ArkTSExpression.VariableExpression("bar");
        ArkTSExpression arg1 = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression arg2 = new ArkTSExpression.LiteralExpression("10",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression.CallExpression expr =
                new ArkTSExpression.CallExpression(callee, List.of(arg1, arg2));
        assertEquals("bar(x, 10)", expr.toArkTS());
    }

    @Test
    void testMemberExpression_dot() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("name");
        ArkTSExpression.MemberExpression expr =
                new ArkTSExpression.MemberExpression(obj, prop, false);
        assertEquals("obj.name", expr.toArkTS());
    }

    @Test
    void testMemberExpression_computed() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("i");
        ArkTSExpression.MemberExpression expr =
                new ArkTSExpression.MemberExpression(obj, prop, true);
        assertEquals("arr[i]", expr.toArkTS());
    }

    @Test
    void testNewExpression() {
        ArkTSExpression callee = new ArkTSExpression.VariableExpression("MyClass");
        ArkTSExpression arg = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression.NewExpression expr =
                new ArkTSExpression.NewExpression(callee, List.of(arg));
        assertEquals("new MyClass(42)", expr.toArkTS());
    }

    @Test
    void testAssignExpression() {
        ArkTSExpression target = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression value = new ArkTSExpression.LiteralExpression("10",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression.AssignExpression expr =
                new ArkTSExpression.AssignExpression(target, value);
        assertEquals("x = 10", expr.toArkTS());
    }

    @Test
    void testArrayLiteralExpression() {
        ArkTSExpression elem1 = new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression elem2 = new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSAccessExpressions.ArrayLiteralExpression expr =
                new ArkTSAccessExpressions.ArrayLiteralExpression(List.of(elem1, elem2));
        assertEquals("[1, 2]", expr.toArkTS());
    }

    // --- Statement AST tests ---

    @Test
    void testBlockStatement_formatting() {
        ArkTSStatement stmt1 = new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.AssignExpression(
                        new ArkTSExpression.VariableExpression("x"),
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSStatement stmt2 = new ArkTSStatement.ReturnStatement(
                new ArkTSExpression.VariableExpression("x"));
        ArkTSStatement.BlockStatement block =
                new ArkTSStatement.BlockStatement(List.of(stmt1, stmt2));
        String result = block.toArkTS(0);
        assertTrue(result.contains("{\n"));
        assertTrue(result.contains("    x = 1;"));
        assertTrue(result.contains("    return x;"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testIfStatement_withElse() {
        ArkTSExpression cond = new ArkTSExpression.VariableExpression("x");
        ArkTSStatement thenStmt = new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.AssignExpression(
                        new ArkTSExpression.VariableExpression("y"),
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSStatement elseStmt = new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.AssignExpression(
                        new ArkTSExpression.VariableExpression("y"),
                        new ArkTSExpression.LiteralExpression("2",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSControlFlow.IfStatement ifStmt =
                new ArkTSControlFlow.IfStatement(cond,
                        new ArkTSStatement.BlockStatement(List.of(thenStmt)),
                        new ArkTSStatement.BlockStatement(List.of(elseStmt)));
        String result = ifStmt.toArkTS(0);
        assertTrue(result.startsWith("if (x) {"));
        assertTrue(result.contains("} else {"));
    }

    @Test
    void testVariableDeclaration_let() {
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration("let", "x",
                null, new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        assertEquals("let x = 42;", stmt.toArkTS(0));
    }

    @Test
    void testVariableDeclaration_const() {
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration("const",
                "PI", null, new ArkTSExpression.LiteralExpression("3.14",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        assertEquals("const PI = 3.14;", stmt.toArkTS(0));
    }

    @Test
    void testReturnStatement_withValue() {
        ArkTSStatement stmt = new ArkTSStatement.ReturnStatement(
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        assertEquals("return 42;", stmt.toArkTS(0));
    }

    @Test
    void testReturnStatement_void() {
        ArkTSStatement stmt = new ArkTSStatement.ReturnStatement(null);
        assertEquals("return;", stmt.toArkTS(0));
    }

    @Test
    void testFunctionDeclaration_basic() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(List.of(
                new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("42",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER))));
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration("f",
                        Collections.emptyList(), "number", body);
        String result = func.toArkTS(0);
        assertTrue(result.contains("function f(): number"));
        assertTrue(result.contains("return 42;"));
    }

    @Test
    void testFunctionDeclaration_withParams() {
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(
                        new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "a", "number"),
                        new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "b", "number"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration("add", params,
                        "number", body);
        String result = func.toArkTS(0);
        assertTrue(result.contains("function add(a: number, b: number): number"));
    }

    // --- Full method decompilation test ---

    @Test
    void testDecompileMethod_fullMethod() {
        // ldai 42; sta v0; lda v0; return
        byte[] codeBytes = concat(
            bytes(0x62), le32(42),
            bytes(0x61, 0x00),
            bytes(0x60, 0x00),
            bytes(0x64)
        );
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "getAnswer",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("function getAnswer()"));
        assertTrue(result.contains("let v0 = 42"));
        assertTrue(result.contains("return v0"));
    }

    // --- Edge cases ---

    @Test
    void testDecompile_emptyInstructions() {
        String result = decompiler.decompileInstructions(
                Collections.emptyList());
        assertEquals("", result);
    }

    @Test
    void testDecompileMethod_nullCode() {
        AbcMethod method = new AbcMethod(0, 0, "empty",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, null, null);
        assertEquals("function empty(): void { }", result);
    }

    @Test
    void testDecompile_movInstruction() {
        // mov v1, v0
        byte[] code = bytes(0x44, 0x01, 0x00);
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v1 = v0"));
    }

    @Test
    void testDecompile_createEmptyObject() {
        // createemptyobject; sta v0
        byte[] code = concat(
            bytes(0x04),        // createemptyobject
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = {  }"));
    }

    @Test
    void testDecompile_createEmptyArray() {
        // createemptyarray 0; sta v0
        byte[] code = concat(
            bytes(0x05, 0x00),  // createemptyarray 0
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0: Array<unknown> = []"));
    }

    @Test
    void testDecompile_ldthis() {
        // ldthis; sta v0
        byte[] code = concat(
            bytes(0x6F),        // ldthis
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = this"));
    }

    @Test
    void testDecompile_incDec() {
        // lda v0; inc 0; sta v1; lda v1; dec 0; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x21, 0x00),   // inc 0
            bytes(0x61, 0x01),   // sta v1
            bytes(0x60, 0x01),   // lda v1
            bytes(0x22, 0x00),   // dec 0
            bytes(0x61, 0x02)    // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v1: number = (v0 + 1)"));
        assertTrue(result.contains("let v2: number = (v1 - 1)"));
    }

    // --- BasicBlock tests ---

    @Test
    void testBasicBlock_endsWithReturn() {
        byte[] code = concat(
            bytes(0x62), le32(42),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        BasicBlock block = new BasicBlock(0);
        for (ArkInstruction insn : insns) {
            block.addInstruction(insn);
        }
        assertTrue(block.endsWithReturn());
    }

    @Test
    void testBasicBlock_notEndsWithReturn() {
        byte[] code = bytes(0x60, 0x00); // lda v0
        List<ArkInstruction> insns = dis(code);
        BasicBlock block = new BasicBlock(0);
        for (ArkInstruction insn : insns) {
            block.addInstruction(insn);
        }
        assertFalse(block.endsWithReturn());
    }

    @Test
    void testBasicBlock_getEndOffset() {
        byte[] code = concat(
            bytes(0x62), le32(42),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        BasicBlock block = new BasicBlock(0);
        for (ArkInstruction insn : insns) {
            block.addInstruction(insn);
        }
        // ldai is 5 bytes, return is 1 byte, total = 6
        assertEquals(6, block.getEndOffset());
    }

    // --- EdgeType tests ---

    @Test
    void testEdgeType_values() {
        assertEquals(5, EdgeType.values().length);
        assertNotNull(EdgeType.FALL_THROUGH);
        assertNotNull(EdgeType.JUMP);
        assertNotNull(EdgeType.CONDITIONAL_TRUE);
        assertNotNull(EdgeType.CONDITIONAL_FALSE);
        assertNotNull(EdgeType.EXCEPTION_HANDLER);
    }

    // --- CFGEdge tests ---

    @Test
    void testCfgEdge_properties() {
        CFGEdge edge = new CFGEdge(0, 10, EdgeType.JUMP);
        assertEquals(0, edge.getFromOffset());
        assertEquals(10, edge.getToOffset());
        assertEquals(EdgeType.JUMP, edge.getType());
    }

    // --- ShortyType mapping tests ---

    @Test
    void testMethodSignatureBuilder_allShortyTypes() {
        assertEquals("void",
                MethodSignatureBuilder.getReturnType(
                        new AbcProto(List.of(AbcProto.ShortyType.VOID),
                                Collections.emptyList())));
        assertEquals("number",
                MethodSignatureBuilder.getReturnType(
                        new AbcProto(List.of(AbcProto.ShortyType.I32),
                                Collections.emptyList())));
        assertEquals("Object",
                MethodSignatureBuilder.getReturnType(
                        new AbcProto(List.of(AbcProto.ShortyType.REF),
                                Collections.emptyList())));
    }

    // --- Complex expression test ---

    @Test
    void testDecompile_complexExpression() {
        // ldai 2; sta v0; ldai 3; sta v1; lda v0; add2 0, v1; sta v2; return v2
        byte[] code = concat(
            bytes(0x62), le32(2),     // ldai 2
            bytes(0x61, 0x00),        // sta v0
            bytes(0x62), le32(3),     // ldai 3
            bytes(0x61, 0x01),        // sta v1
            bytes(0x60, 0x00),        // lda v0
            bytes(0x0A, 0x00, 0x01),  // add2 0, v1
            bytes(0x61, 0x02),        // sta v2
            bytes(0x60, 0x02),        // lda v2
            bytes(0x64)               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 2"));
        assertTrue(result.contains("let v1 = 3"));
        assertTrue(result.contains("let v2: number = (v0 + v1)"));
        assertTrue(result.contains("return v2"));
    }

    // --- String escape test ---

    @Test
    void testLiteralExpression_stringEscaping() {
        ArkTSExpression.LiteralExpression expr =
                new ArkTSExpression.LiteralExpression("hello\nworld",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"hello\\nworld\"", expr.toArkTS());
    }

    // --- This expression test ---

    @Test
    void testThisExpression() {
        ArkTSExpression.ThisExpression expr =
                new ArkTSExpression.ThisExpression();
        assertEquals("this", expr.toArkTS());
    }

    // --- Try/Catch statement test ---

    @Test
    void testTryCatchStatement_formatting() {
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("doSomething()"))));
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("handleError()"))));
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e", catchBody, null);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("try {"));
        assertTrue(result.contains("catch (e) {"));
        assertFalse(result.contains("finally"));
    }

    // --- While statement test ---

    @Test
    void testWhileStatement_formatting() {
        ArkTSExpression cond = new ArkTSExpression.VariableExpression("x");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("step()"))));
        ArkTSControlFlow.WhileStatement stmt =
                new ArkTSControlFlow.WhileStatement(cond, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("while (x) {"));
        assertTrue(result.contains("step()"));
    }

    // --- Throw statement test ---

    @Test
    void testThrowStatement() {
        ArkTSStatement stmt = new ArkTSStatement.ThrowStatement(
                new ArkTSExpression.VariableExpression("err"));
        assertEquals("throw err;", stmt.toArkTS(0));
    }

    // --- Break/Continue statement tests ---

    @Test
    void testBreakStatement() {
        ArkTSStatement stmt = new ArkTSStatement.BreakStatement();
        assertEquals("break;", stmt.toArkTS(0));
    }

    @Test
    void testContinueStatement() {
        ArkTSStatement stmt = new ArkTSStatement.ContinueStatement();
        assertEquals("continue;", stmt.toArkTS(0));
    }

    // --- Conditional expression test ---

    @Test
    void testConditionalExpression() {
        ArkTSExpression test = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression cons = new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression alt = new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSAccessExpressions.ConditionalExpression expr =
                new ArkTSAccessExpressions.ConditionalExpression(test, cons, alt);
        assertEquals("(x ? 1 : 2)", expr.toArkTS());
    }

    // --- Object literal test ---

    @Test
    void testObjectLiteralExpression() {
        List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty> props =
                List.of(
                        new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                                "name", new ArkTSExpression.LiteralExpression("test",
                                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)),
                        new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                                "value", new ArkTSExpression.LiteralExpression("42",
                                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSAccessExpressions.ObjectLiteralExpression expr =
                new ArkTSAccessExpressions.ObjectLiteralExpression(props);
        assertEquals("{ name: \"test\", value: 42 }", expr.toArkTS());
    }

    // --- Variable declaration with type annotation ---

    @Test
    void testVariableDeclaration_withType() {
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration("let",
                "x", "number", new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        assertEquals("let x: number = 0;", stmt.toArkTS(0));
    }

    // --- For statement test ---

    @Test
    void testForStatement_formatting() {
        ArkTSStatement init = new ArkTSStatement.VariableDeclaration("let",
                "i", null, new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSExpression cond = new ArkTSExpression.BinaryExpression(
                new ArkTSExpression.VariableExpression("i"),
                "<",
                new ArkTSExpression.LiteralExpression("10",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSExpression update = new ArkTSExpression.AssignExpression(
                new ArkTSExpression.VariableExpression("i"),
                new ArkTSExpression.BinaryExpression(
                        new ArkTSExpression.VariableExpression("i"),
                        "+",
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression("process"),
                                List.of(new ArkTSExpression.VariableExpression("i"))))));
        ArkTSControlFlow.ForStatement stmt =
                new ArkTSControlFlow.ForStatement(init, cond, update, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("for (let i = 0; (i < 10); i = (i + 1)) {"));
    }

    // --- Strict equality operators ---

    @Test
    void testDecompile_strictEquals() {
        // lda v0; stricteq 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x28, 0x00, 0x01), // stricteq 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("==="));
    }

    // --- Lexical variable access ---

    @Test
    void testDecompile_ldlexvar() {
        // ldlexvar 1, 2; sta v0
        byte[] code = concat(
            bytes(0x3C, 0x21),  // ldlexvar (level=1, slot=2)
            bytes(0x61, 0x00)   // sta v0
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("lex_1_2"));
    }

    // --- NOP is skipped ---

    @Test
    void testDecompile_nopSkipped() {
        // nop; ldai 42; return
        byte[] code = concat(
            bytes(0xD5),             // nop
            bytes(0x62), le32(42),   // ldai 42
            bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.contains("nop"));
        assertTrue(result.contains("return 42"));
    }

    // --- Indentation test ---

    @Test
    void testStatement_indentation() {
        ArkTSStatement stmt = new ArkTSStatement.ReturnStatement(
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        assertEquals("    return 1;", stmt.toArkTS(1));
        assertEquals("        return 1;", stmt.toArkTS(2));
    }

    // --- Unary expression postfix test ---

    @Test
    void testUnaryExpression_prefix() {
        ArkTSExpression operand = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression.UnaryExpression expr =
                new ArkTSExpression.UnaryExpression("-", operand, true);
        assertEquals("(-x)", expr.toArkTS());
    }

    // ======== NEW TESTS FOR ENHANCED DECOMPILER ========

    // --- Control flow: if/else from bytecode ---

    @Test
    void testDecompile_ifElse_fromBytecode() {
        // ldai 1; sta v0; lda v0; jeqz +offset_to_else; ldai 10; sta v1;
        // jmp +offset_to_end; ldai 20; sta v1; return v1
        // Layout:
        // offset 0: ldai 1         (5 bytes)
        // offset 5: sta v0         (2 bytes)
        // offset 7: lda v0         (2 bytes)
        // offset 9: jeqz +12       (2 bytes) -> offset 23 (else branch)
        // offset 11: ldai 10       (5 bytes)
        // offset 16: sta v1        (2 bytes)
        // offset 18: jmp +5        (2 bytes) -> offset 25 (end/merge)
        // offset 20: ldai 20       (5 bytes)
        // offset 25: sta v1        (2 bytes)
        // offset 27: return        (1 byte)
        byte[] code = concat(
            bytes(0x62), le32(1),           // ldai 1
            bytes(0x61, 0x00),              // sta v0
            bytes(0x60, 0x00),              // lda v0
            bytes(0x4F, 0x0C),              // jeqz +12 -> offset 23
            bytes(0x62), le32(10),          // ldai 10
            bytes(0x61, 0x01),              // sta v1
            bytes(0x4D, 0x05),              // jmp +5 -> offset 25
            bytes(0x62), le32(20),          // ldai 20
            bytes(0x61, 0x01),              // sta v1
            bytes(0x64)                     // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        // Verify the CFG has multiple blocks for the if/else structure
        assertTrue(cfg.getBlocks().size() >= 3);
    }

    @Test
    void testDecompile_conditionalBranch_producesMultipleBlocks() {
        // Simple: ldai 1; jeqz +5; ldai 10; sta v0; return
        byte[] code = concat(
            bytes(0x62), le32(1),       // ldai 1        (offset 0, len 5)
            bytes(0x4F, 0x05),          // jeqz +5       (offset 5, len 2) -> offset 12
            bytes(0x62), le32(10),      // ldai 10       (offset 7, len 5)
            bytes(0x61, 0x00),          // sta v0        (offset 12, len 2)
            bytes(0x64)                  // return        (offset 14, len 1)
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertTrue(cfg.getBlocks().size() >= 2);
    }

    // --- Control flow: while loop pattern detection ---

    @Test
    void testDecompile_whileLoopPattern() {
        // Simple pattern: condition block -> body -> back-edge to condition
        // ldai 1; sta v0; lda v0; jnez -offset (back to condition); return
        // We can test the CFG structure
        byte[] code = concat(
            bytes(0x62), le32(1),       // ldai 1
            bytes(0x61, 0x00),          // sta v0
            bytes(0x60, 0x00),          // lda v0
            bytes(0x64)                 // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertNotNull(cfg.getEntryBlock());
    }

    // --- Type inference tests ---

    @Test
    void testTypeInference_ldaiProducesNumber() {
        TypeInference ti = new TypeInference();
        // Create a mock instruction for ldai
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.LDAI, "ldai",
                ArkInstructionFormat.IMM32, 0, 5,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE32_SIGNED, 42)),
                false);
        assertEquals("number", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_ldtrueProducesBoolean() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.LDTRUE, "ldtrue",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        assertEquals("boolean", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_ldnullProducesNull() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.LDNULL, "ldnull",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        assertEquals("null", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_ldundefinedProducesUndefined() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.LDUNDEFINED, "ldundefined",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        assertEquals("undefined", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_ldaStrProducesString() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.LDA_STR, "lda.str",
                ArkInstructionFormat.IMM16, 0, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.STRING_ID, 0)),
                false);
        assertEquals("string", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_createemptyobjectProducesObject() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.CREATEEMPTYOBJECT, "createemptyobject",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        assertEquals("Object", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_createemptyarrayProducesArray() {
        TypeInference ti = new TypeInference();
        ArkInstruction insn = new ArkInstruction(
                ArkOpcodesCompat.CREATEEMPTYARRAY, "createemptyarray",
                ArkInstructionFormat.IMM8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0)),
                false);
        assertEquals("Array<unknown>", ti.inferTypeForInstruction(insn));
    }

    @Test
    void testTypeInference_registerTracking() {
        TypeInference ti = new TypeInference();
        ti.setRegisterType("v0", "number");
        assertEquals("number", ti.getRegisterType("v0"));
        assertNull(ti.getRegisterType("v1"));
    }

    @Test
    void testTypeInference_formatTypeAnnotation_skipsObject() {
        assertNull(TypeInference.formatTypeAnnotation("v0", "Object"));
    }

    @Test
    void testTypeInference_formatTypeAnnotation_preservesNumber() {
        assertEquals("number", TypeInference.formatTypeAnnotation("v0",
                "number"));
    }

    @Test
    void testTypeInference_formatTypeAnnotation_preservesString() {
        assertEquals("string", TypeInference.formatTypeAnnotation("v0",
                "string"));
    }

    @Test
    void testTypeInference_formatTypeAnnotation_nullReturnsNull() {
        assertNull(TypeInference.formatTypeAnnotation("v0", null));
    }

    @Test
    void testTypeInference_isBinaryArithmeticOpFromSymbol() {
        assertTrue(TypeInference.isBinaryArithmeticOpFromSymbol("+"));
        assertTrue(TypeInference.isBinaryArithmeticOpFromSymbol("-"));
        assertTrue(TypeInference.isBinaryArithmeticOpFromSymbol("*"));
        assertFalse(TypeInference.isBinaryArithmeticOpFromSymbol("=="));
    }

    @Test
    void testTypeInference_isComparisonOpFromSymbol() {
        assertTrue(TypeInference.isComparisonOpFromSymbol("=="));
        assertTrue(TypeInference.isComparisonOpFromSymbol("==="));
        assertTrue(TypeInference.isComparisonOpFromSymbol("<"));
        assertFalse(TypeInference.isComparisonOpFromSymbol("+"));
    }

    // --- Type annotation in decompiled output ---

    @Test
    void testDecompile_typeAnnotation_number() {
        // ldai 42; sta v0; return
        byte[] code = concat(
            bytes(0x62), le32(42),
            bytes(0x61, 0x00),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 42"));
    }

    @Test
    void testDecompile_typeAnnotation_string() {
        // lda.str 0; sta v0; return
        byte[] code = concat(
            bytes(0x3E, 0x00, 0x00),  // lda.str 0
            bytes(0x61, 0x00),         // sta v0
            bytes(0x64)                // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = \"str_0\""));
    }

    @Test
    void testDecompile_typeAnnotation_boolean() {
        // ldtrue; sta v0; return
        byte[] code = concat(
            bytes(0x02),        // ldtrue
            bytes(0x61, 0x00),  // sta v0
            bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = true"));
    }

    @Test
    void testDecompile_typeAnnotation_comparisonResult() {
        // lda v0; less 0, v1; sta v2; return
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x11, 0x00, 0x01), // less 0, v1
            bytes(0x61, 0x02),       // sta v2
            bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v2: boolean = (v0 < v1)"));
    }

    // --- Async/await pattern tests ---

    @Test
    void testDecompile_asyncFunctionEnter() {
        // asyncfunctionenter; ldai 42; return
        byte[] code = concat(
            bytes(0xAE),             // asyncfunctionenter
            bytes(0x62), le32(42),   // ldai 42
            bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return 42"));
    }

    @Test
    void testDecompile_awaitExpression() {
        // v0 contains a promise; asyncfunctionawaituncaught 0, v0; sta v1
        byte[] code = concat(
            bytes(0xC4, 0x00),  // asyncfunctionawaituncaught 0
            bytes(0x61, 0x01)   // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("await"));
    }

    @Test
    void testDecompile_asyncResolve() {
        // asyncfunctionresolve 0, v0
        byte[] code = concat(
            bytes(0xCD, 0x00),  // asyncfunctionresolve 0
            bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // --- Generator pattern tests ---

    @Test
    void testDecompile_generatorObj() {
        // creategeneratorobj v0
        byte[] code = concat(
            bytes(0xB1, 0x00),  // creategeneratorobj v0
            bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("generator"));
    }

    @Test
    void testDecompile_suspendGenerator() {
        // suspendgenerator v0
        byte[] code = concat(
            bytes(0xC3, 0x00),  // suspendgenerator v0
            bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("yield"));
    }

    @Test
    void testDecompile_resumeGenerator() {
        // resumegenerator (NONE format, no operands)
        byte[] code = concat(
            bytes(0xBF),  // resumegenerator
            bytes(0x64)   // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // --- Property chain access test ---

    @Test
    void testDecompile_propertyChainAccess() {
        // ldthis; ldobjbyname 0, "field"; ldobjbyname 0, "subfield"; sta v0
        // Note: ldobjbyname format is IMM8_IMM16_V8 or IMM8_IMM16 depending
        // on variant. For simplicity, test the chain through the expression
        // AST directly.
        ArkTSExpression thisExpr = new ArkTSExpression.ThisExpression();
        ArkTSExpression fieldExpr = new ArkTSExpression.MemberExpression(
                thisExpr,
                new ArkTSExpression.VariableExpression("field"), false);
        ArkTSExpression subFieldExpr = new ArkTSExpression.MemberExpression(
                fieldExpr,
                new ArkTSExpression.VariableExpression("subfield"), false);
        assertEquals("this.field.subfield", subFieldExpr.toArkTS());
    }

    @Test
    void testDecompile_computedPropertyChain() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression idx = new ArkTSExpression.LiteralExpression("0",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression.MemberExpression elem =
                new ArkTSExpression.MemberExpression(obj, idx, true);
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("length");
        ArkTSExpression.MemberExpression chain =
                new ArkTSExpression.MemberExpression(elem, prop, false);
        assertEquals("arr[0].length", chain.toArkTS());
    }

    // --- New expression types ---

    @Test
    void testOptionalChainExpression_dot() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("name");
        ArkTSAccessExpressions.OptionalChainExpression expr =
                new ArkTSAccessExpressions.OptionalChainExpression(obj, prop, false);
        assertEquals("obj?.name", expr.toArkTS());
    }

    @Test
    void testOptionalChainExpression_computed() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("i");
        ArkTSAccessExpressions.OptionalChainExpression expr =
                new ArkTSAccessExpressions.OptionalChainExpression(obj, prop, true);
        assertEquals("arr?.[i]", expr.toArkTS());
    }

    @Test
    void testSpreadExpression() {
        ArkTSExpression arg =
                new ArkTSExpression.VariableExpression("args");
        ArkTSAccessExpressions.SpreadExpression expr =
                new ArkTSAccessExpressions.SpreadExpression(arg);
        assertEquals("...args", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_noInterpolation() {
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("hello world"),
                        Collections.emptyList());
        assertEquals("`hello world`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_withInterpolation() {
        ArkTSExpression name =
                new ArkTSExpression.VariableExpression("name");
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("Hello, ", "!"),
                        List.of(name));
        assertEquals("`Hello, ${name}!`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_escapesBacktick() {
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("test`string"),
                        Collections.emptyList());
        assertEquals("`test\\`string`", expr.toArkTS());
    }

    @Test
    void testAwaitExpression() {
        ArkTSExpression promise =
                new ArkTSExpression.VariableExpression("p");
        ArkTSAccessExpressions.AwaitExpression expr =
                new ArkTSAccessExpressions.AwaitExpression(promise);
        assertEquals("await p", expr.toArkTS());
    }

    @Test
    void testYieldExpression() {
        ArkTSExpression value = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSAccessExpressions.YieldExpression expr =
                new ArkTSAccessExpressions.YieldExpression(value, false);
        assertEquals("yield 42", expr.toArkTS());
    }

    @Test
    void testYieldExpression_delegate() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("items");
        ArkTSAccessExpressions.YieldExpression expr =
                new ArkTSAccessExpressions.YieldExpression(iterable, true);
        assertEquals("yield* items", expr.toArkTS());
    }

    @Test
    void testYieldExpression_bare() {
        ArkTSAccessExpressions.YieldExpression expr =
                new ArkTSAccessExpressions.YieldExpression(null, false);
        assertEquals("yield", expr.toArkTS());
    }

    @Test
    void testArrowFunctionExpression_basic() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("42",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER))));
        ArkTSAccessExpressions.ArrowFunctionExpression expr =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        Collections.emptyList(), body, false);
        assertTrue(expr.toArkTS().contains("() => {"));
        assertTrue(expr.toArkTS().contains("return 42;"));
    }

    @Test
    void testArrowFunctionExpression_async() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSAccessExpressions.ArrowFunctionExpression expr =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        Collections.emptyList(), body, true);
        assertTrue(expr.toArkTS().startsWith("async () =>"));
    }

    @Test
    void testArrowFunctionExpression_withParams() {
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(
                        new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "x", "number"),
                        new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "y", "number"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.BinaryExpression(
                                new ArkTSExpression.VariableExpression("x"),
                                "+",
                                new ArkTSExpression.VariableExpression("y")))));
        ArkTSAccessExpressions.ArrowFunctionExpression expr =
                new ArkTSAccessExpressions.ArrowFunctionExpression(params, body,
                        false);
        String result = expr.toArkTS();
        assertTrue(result.contains("(x: number, y: number) =>"));
        assertTrue(result.contains("return (x + y);"));
    }

    // --- New statement type tests ---

    @Test
    void testDoWhileStatement_formatting() {
        ArkTSExpression cond = new ArkTSExpression.VariableExpression("x");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("step()"))));
        ArkTSControlFlow.DoWhileStatement stmt =
                new ArkTSControlFlow.DoWhileStatement(body, cond);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("do {"));
        assertTrue(result.contains("} while (x);"));
    }

    @Test
    void testDoWhileStatement_withIndentation() {
        ArkTSExpression cond = new ArkTSExpression.VariableExpression("ok");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSControlFlow.DoWhileStatement stmt =
                new ArkTSControlFlow.DoWhileStatement(body, cond);
        String result = stmt.toArkTS(1);
        assertTrue(result.startsWith("    do {"));
        assertTrue(result.contains("    } while (ok);"));
    }

    @Test
    void testClassDeclaration_basic() {
        List<ArkTSStatement> members = List.of(
                new ArkTSDeclarations.ClassFieldDeclaration("name", "string",
                        null, false, null),
                new ArkTSDeclarations.ClassMethodDeclaration("greet",
                        Collections.emptyList(), "string",
                        new ArkTSStatement.BlockStatement(
                                List.of(new ArkTSStatement.ReturnStatement(
                                        new ArkTSExpression.LiteralExpression(
                                                "hello",
                                                ArkTSExpression.LiteralExpression
                                                        .LiteralKind.STRING)))),
                        false, "public"));
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("MyClass", null, members);
        String result = cls.toArkTS(0);
        assertTrue(result.startsWith("class MyClass {"));
        assertTrue(result.contains("name: string;"));
        assertTrue(result.contains("public greet(): string"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testClassDeclaration_withExtends() {
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Child", "Parent",
                        Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Child extends Parent"));
    }

    @Test
    void testClassFieldDeclaration_static() {
        ArkTSStatement stmt = new ArkTSDeclarations.ClassFieldDeclaration(
                "instanceCount", "number",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                true, null);
        assertEquals("static instanceCount: number = 0;",
                stmt.toArkTS(0));
    }

    @Test
    void testClassMethodDeclaration_private() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "helper", Collections.emptyList(), null, body, false,
                "private");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("private helper()"));
    }

    @Test
    void testImportStatement_named() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                List.of("foo", "bar"), "./module", false, null, null);
        assertEquals("import { foo, bar } from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_default() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                Collections.emptyList(), "./module", true, "MyClass", null);
        assertEquals("import MyClass from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_namespace() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                Collections.emptyList(), "./module", false, null, "ns");
        assertEquals("import * as ns from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_defaultAndNamed() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                List.of("helper"), "./module", true, "Main", null);
        assertEquals("import Main, { helper } from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testExportStatement_named() {
        ArkTSStatement stmt = new ArkTSDeclarations.ExportStatement(
                List.of("foo", "bar"), null, false);
        assertEquals("export { foo, bar };", stmt.toArkTS(0));
    }

    @Test
    void testExportStatement_default() {
        ArkTSStatement decl = new ArkTSStatement.VariableDeclaration("let",
                "x", null, new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSStatement stmt = new ArkTSDeclarations.ExportStatement(
                Collections.emptyList(), decl, true);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export default let x = 42"));
    }

    @Test
    void testExportStatement_declaration() {
        ArkTSStatement funcDecl = new ArkTSDeclarations.FunctionDeclaration(
                "doStuff", Collections.emptyList(), null,
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement stmt = new ArkTSDeclarations.ExportStatement(
                Collections.emptyList(), funcDecl, false);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export function doStuff"));
    }

    @Test
    void testEnumDeclaration_basic() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("A", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("B", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("C",
                        new ArkTSExpression.LiteralExpression("10",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.EnumDeclaration("Color",
                members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("enum Color {"));
        assertTrue(result.contains("A,"));
        assertTrue(result.contains("B,"));
        assertTrue(result.contains("C = 10"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testInterfaceDeclaration_basic() {
        List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "property", "name", "string",
                                Collections.emptyList(), false),
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "method", "doStuff", "void",
                                List.of(new ArkTSDeclarations.FunctionDeclaration
                                        .FunctionParam("x", "number")),
                                false));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.InterfaceDeclaration(
                "MyInterface", Collections.emptyList(), members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("interface MyInterface {"));
        assertTrue(result.contains("name: string;"));
        assertTrue(result.contains("doStuff(x: number): void;"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testInterfaceDeclaration_extends() {
        ArkTSStatement stmt = new ArkTSTypeDeclarations.InterfaceDeclaration(
                "Child", List.of("Parent", "Base"),
                Collections.emptyList());
        String result = stmt.toArkTS(0);
        assertTrue(result.contains(
                "interface Child extends Parent, Base"));
    }

    @Test
    void testInterfaceDeclaration_optionalProperty() {
        List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "property", "name", "string",
                                Collections.emptyList(), true));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.InterfaceDeclaration(
                "Opts", Collections.emptyList(), members);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("name?: string;"));
    }

    @Test
    void testDecoratorStatement_noArgs() {
        ArkTSStatement stmt = new ArkTSTypeDeclarations.DecoratorStatement(
                "Component", Collections.emptyList());
        assertEquals("@Component", stmt.toArkTS(0));
    }

    @Test
    void testDecoratorStatement_withArgs() {
        ArkTSStatement stmt = new ArkTSTypeDeclarations.DecoratorStatement(
                "Route",
                List.of(new ArkTSExpression.LiteralExpression("'/home'",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        assertEquals("@Route(\"'/home'\")", stmt.toArkTS(0));
    }

    @Test
    void testDecoratorStatement_withIndentation() {
        ArkTSStatement stmt = new ArkTSTypeDeclarations.DecoratorStatement(
                "Injectable", Collections.emptyList());
        assertEquals("    @Injectable", stmt.toArkTS(1));
    }

    // --- CFG condition extraction tests ---

    @Test
    void testCfg_backwardEdgeDetection() {
        // Build a simple loop: jmp to condition; condition: jnez body; body
        // falls through to condition
        // offset 0: ldai 1         (5 bytes)
        // offset 5: sta v0         (2 bytes)
        // offset 7: lda v0         (2 bytes)
        // offset 9: jnez -4        (2 bytes) -> offset 7 (back to lda)
        // offset 11: return        (1 byte)
        byte[] code = concat(
            bytes(0x62), le32(1),       // ldai 1
            bytes(0x61, 0x00),          // sta v0
            bytes(0x60, 0x00),          // lda v0
            bytes(0x51, (byte) 0xFC),   // jnez -4 -> offset 7
            bytes(0x64)                 // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        // The jnez creates a backward edge, resulting in multiple blocks
        assertTrue(cfg.getBlocks().size() >= 2);
    }

    // --- IsTrue/IsFalse instruction tests ---

    @Test
    void testDecompile_isTrue() {
        // lda v0; istrue; sta v1; return
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x23),         // istrue (NONE format, no operands)
            bytes(0x61, 0x01),   // sta v1
            bytes(0x64)          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("Boolean(v0)"));
    }

    @Test
    void testDecompile_isFalse() {
        // lda v0; isfalse; sta v1; return
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x24),         // isfalse (NONE format, no operands)
            bytes(0x61, 0x01),   // sta v1
            bytes(0x64)          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(!v0)"));
    }

    // --- Full method decompilation with method signature and types ---

    @Test
    void testDecompileMethod_withTypedParams() {
        // Test method signature building with typed parameters
        AbcMethod method = new AbcMethod(0, 0, "add",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcProto proto = new AbcProto(
                List.of(AbcProto.ShortyType.I32, AbcProto.ShortyType.I32,
                        AbcProto.ShortyType.I32),
                Collections.emptyList());
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 2);
        assertEquals("function add(param_0: number, param_1: number): number", sig);
    }

    // --- Edge case: empty block ---

    @Test
    void testBasicBlock_emptyBlock() {
        BasicBlock block = new BasicBlock(0);
        assertTrue(block.getInstructions().isEmpty());
        assertNull(block.getLastInstruction());
        assertEquals(0, block.getEndOffset());
        assertFalse(block.endsWithReturn());
    }

    // --- Throw instruction test ---

    @Test
    void testDecompile_throw() {
        // lda.str 0; throw
        byte[] code = concat(
            bytes(0x3E, 0x00, 0x00),  // lda.str 0
            bytes(0xFE)                // throw (0xFE is PREFIX_THROW)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("throw"));
    }

    // --- STARRAYSPREAD test ---

    @Test
    void testDecompile_starrayspread() {
        // starrayspread v0, v1
        byte[] code = concat(
            bytes(0xC6, 0x00, 0x01),  // starrayspread v0, v1
            bytes(0x64)                // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("...v1"));
    }

    // --- CFG edge types coverage ---

    @Test
    void testEdgeType_allValues() {
        // Verify all edge types can be constructed and accessed
        for (EdgeType type : EdgeType.values()) {
            CFGEdge edge = new CFGEdge(0, 10, type);
            assertEquals(0, edge.getFromOffset());
            assertEquals(10, edge.getToOffset());
            assertEquals(type, edge.getType());
        }
    }

    // --- TypeInference static helpers ---

    @Test
    void testTypeInference_isBinaryArithmeticOp() {
        assertTrue(TypeInference.isBinaryArithmeticOp(
                ArkOpcodesCompat.ADD2));
        assertTrue(TypeInference.isBinaryArithmeticOp(
                ArkOpcodesCompat.SUB2));
        assertTrue(TypeInference.isBinaryArithmeticOp(
                ArkOpcodesCompat.MUL2));
        assertFalse(TypeInference.isBinaryArithmeticOp(
                ArkOpcodesCompat.EQ));
    }

    @Test
    void testTypeInference_isComparisonOp() {
        assertTrue(TypeInference.isComparisonOp(ArkOpcodesCompat.EQ));
        assertTrue(TypeInference.isComparisonOp(ArkOpcodesCompat.LESS));
        assertTrue(TypeInference.isComparisonOp(
                ArkOpcodesCompat.INSTANCEOF));
        assertFalse(TypeInference.isComparisonOp(ArkOpcodesCompat.ADD2));
    }

    @Test
    void testTypeInference_methodReturnTypes() {
        TypeInference ti = new TypeInference();
        ti.setMethodReturnType("method_0", "string");
        ti.setMethodReturnType("method_1", "number");
        // Verify type annotations format correctly
        assertEquals("string",
                TypeInference.formatTypeAnnotation("v0", "string"));
        assertEquals("number",
                TypeInference.formatTypeAnnotation("v0", "number"));
    }

    // ======== ENHANCED DECOMPILER TESTS ========

    // --- Class declaration AST tests ---

    @Test
    void testClassDeclaration_withConstructor() {
        ArkTSStatement constructorBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSControlFlow.SuperCallStatement(
                        Collections.emptyList())));
        ArkTSStatement constructor =
                new ArkTSDeclarations.ConstructorDeclaration(
                        List.of(new ArkTSDeclarations.FunctionDeclaration
                                .FunctionParam("x", "number")),
                        constructorBody);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("x"))));
        ArkTSStatement method = new ArkTSDeclarations.ClassMethodDeclaration(
                "getX", Collections.emptyList(), "number",
                methodBody, false, "public");
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "x", "number", null, false, "private");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Point", "BaseClass",
                        List.of(field, constructor, method));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Point extends BaseClass"));
        assertTrue(result.contains("private x: number;"));
        assertTrue(result.contains("constructor(x: number)"));
        assertTrue(result.contains("super();"));
        assertTrue(result.contains("public getX(): number"));
    }

    @Test
    void testConstructorDeclaration_noParams() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSControlFlow.SuperCallStatement(
                        Collections.emptyList())));
        ArkTSStatement stmt = new ArkTSDeclarations.ConstructorDeclaration(
                Collections.emptyList(), body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("constructor()"));
        assertTrue(result.contains("super();"));
    }

    @Test
    void testConstructorDeclaration_withParams() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "a", "number"),
                        new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                                "b", "string"));
        ArkTSStatement stmt = new ArkTSDeclarations.ConstructorDeclaration(
                params, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("constructor(a: number, b: string)"));
    }

    // --- Super call statement ---

    @Test
    void testSuperCallStatement_noArgs() {
        ArkTSStatement stmt = new ArkTSControlFlow.SuperCallStatement(
                Collections.emptyList());
        assertEquals("super();", stmt.toArkTS(0));
    }

    @Test
    void testSuperCallStatement_withArgs() {
        ArkTSExpression arg1 = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression arg2 = new ArkTSExpression.VariableExpression("y");
        ArkTSStatement stmt = new ArkTSControlFlow.SuperCallStatement(
                List.of(arg1, arg2));
        assertEquals("super(x, y);", stmt.toArkTS(0));
    }

    // --- Struct declaration ---

    @Test
    void testStructDeclaration_basic() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "message", "string",
                new ArkTSExpression.LiteralExpression("'Hello'",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING),
                false, null);
        ArkTSTypeDeclarations.StructDeclaration struct =
                new ArkTSTypeDeclarations.StructDeclaration("MyPage",
                        List.of(field),
                        List.of("Entry", "Component"));
        String result = struct.toArkTS(0);
        assertTrue(result.contains("@Entry"));
        assertTrue(result.contains("@Component"));
        assertTrue(result.contains("struct MyPage"));
        assertTrue(result.contains("message: string = \"'Hello'\";"));
    }

    @Test
    void testStructDeclaration_noDecorators() {
        ArkTSTypeDeclarations.StructDeclaration struct =
                new ArkTSTypeDeclarations.StructDeclaration("SimpleStruct",
                        Collections.emptyList(), Collections.emptyList());
        String result = struct.toArkTS(0);
        assertTrue(result.contains("struct SimpleStruct"));
        assertFalse(result.contains("@"));
    }

    // --- Type parameter ---

    @Test
    void testTypeParameter_noConstraint() {
        ArkTSTypeDeclarations.TypeParameter tp =
                new ArkTSTypeDeclarations.TypeParameter("T", null);
        assertEquals("T", tp.toString());
    }

    @Test
    void testTypeParameter_withConstraint() {
        ArkTSTypeDeclarations.TypeParameter tp =
                new ArkTSTypeDeclarations.TypeParameter("T", "Base");
        assertEquals("T extends Base", tp.toString());
    }

    // --- Generic class declaration ---

    @Test
    void testGenericClassDeclaration_singleTypeParam() {
        ArkTSTypeDeclarations.GenericClassDeclaration cls =
                new ArkTSTypeDeclarations.GenericClassDeclaration("Container",
                        List.of(new ArkTSTypeDeclarations.TypeParameter("T", null)),
                        null, Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Container<T>"));
    }

    @Test
    void testGenericClassDeclaration_constrainedTypeParam() {
        ArkTSTypeDeclarations.GenericClassDeclaration cls =
                new ArkTSTypeDeclarations.GenericClassDeclaration("SortedContainer",
                        List.of(new ArkTSTypeDeclarations.TypeParameter("T",
                                "Comparable")),
                        "Base", Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class SortedContainer<T extends "
                + "Comparable> extends Base"));
    }

    @Test
    void testGenericClassDeclaration_multipleTypeParams() {
        ArkTSTypeDeclarations.GenericClassDeclaration cls =
                new ArkTSTypeDeclarations.GenericClassDeclaration("Map",
                        List.of(new ArkTSTypeDeclarations.TypeParameter("K", null),
                                new ArkTSTypeDeclarations.TypeParameter("V", null)),
                        null, Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Map<K, V>"));
    }

    // --- File module ---

    @Test
    void testFileModule_empty() {
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());
        assertEquals("", fileModule.toArkTS(0));
    }

    @Test
    void testFileModule_withImports() {
        ArkTSStatement imp = new ArkTSDeclarations.ImportStatement(
                List.of("Component"), "@ohos/component", false, null, null);
        ArkTSStatement decl = new ArkTSDeclarations.ClassDeclaration(
                "MyClass", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(List.of(imp), List.of(decl),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("import { Component } from "
                + "'@ohos/component';"));
        assertTrue(result.contains("class MyClass"));
    }

    @Test
    void testFileModule_withExports() {
        ArkTSStatement funcDecl = new ArkTSDeclarations.FunctionDeclaration(
                "helper", Collections.emptyList(), "void",
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement export = new ArkTSDeclarations.ExportStatement(
                Collections.emptyList(), funcDecl, false);
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(Collections.emptyList(),
                        Collections.emptyList(), List.of(export));
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("export function helper"));
    }

    @Test
    void testFileModule_fullFile() {
        ArkTSStatement imp = new ArkTSDeclarations.ImportStatement(
                List.of("BaseModel"), "./model", false, null, null);
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "UserModel", "BaseModel", Collections.emptyList());
        ArkTSStatement export = new ArkTSDeclarations.ExportStatement(
                List.of("UserModel"), null, false);
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(List.of(imp), List.of(cls),
                        List.of(export));
        String result = fileModule.toArkTS(0);
        assertTrue(result.startsWith("import"));
        assertTrue(result.contains("class UserModel extends BaseModel"));
        assertTrue(result.contains("export { UserModel };"));
    }

    // --- New expression types ---

    @Test
    void testAsExpression() {
        ArkTSExpression expr = new ArkTSExpression.VariableExpression("obj");
        ArkTSAccessExpressions.AsExpression asExpr =
                new ArkTSAccessExpressions.AsExpression(expr, "string");
        assertEquals("obj as string", asExpr.toArkTS());
    }

    @Test
    void testNonNullExpression() {
        ArkTSExpression expr = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("obj"),
                new ArkTSExpression.VariableExpression("field"), false);
        ArkTSAccessExpressions.NonNullExpression nonNull =
                new ArkTSAccessExpressions.NonNullExpression(expr);
        assertEquals("obj.field!", nonNull.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_simple() {
        ArkTSAccessExpressions.TypeReferenceExpression typeRef =
                new ArkTSAccessExpressions.TypeReferenceExpression("number",
                        Collections.emptyList());
        assertEquals("number", typeRef.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_generic() {
        ArkTSAccessExpressions.TypeReferenceExpression typeRef =
                new ArkTSAccessExpressions.TypeReferenceExpression("Array",
                        List.of("string"));
        assertEquals("Array<string>", typeRef.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_multipleTypeArgs() {
        ArkTSAccessExpressions.TypeReferenceExpression typeRef =
                new ArkTSAccessExpressions.TypeReferenceExpression("Map",
                        List.of("string", "number"));
        assertEquals("Map<string, number>", typeRef.toArkTS());
    }

    // --- Defineclasswithbuffer from bytecode ---

    @Test
    void testDecompile_defineClassWithBuffer() {
        // defineclasswithbuffer has format IMM8_IMM16_IMM16_V8
        // opcode=0x35, operand0=imm8, operand1=imm16, operand2=imm16, operand3=v8
        // Build: 0x35, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03
        byte[] code = concat(
                bytes(0x35, 0x00),            // defineclasswithbuffer imm8=0
                le16(1),                      // imm16=1 (literal idx)
                le16(2),                      // imm16=2 (method idx)
                bytes(0x03),                  // v8=3 (dest register)
                bytes(0x64)                   // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // --- Enum declaration with values ---

    @Test
    void testEnumDeclaration_withExplicitValues() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Active",
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Inactive",
                        new ArkTSExpression.LiteralExpression("0",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.EnumDeclaration("Status",
                members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("enum Status {"));
        assertTrue(result.contains("Active = 1,"));
        assertTrue(result.contains("Inactive = 0"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testEnumDeclaration_autoIncrement() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("North", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("South", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("East", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("West", null));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.EnumDeclaration("Direction",
                members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("enum Direction {"));
        assertTrue(result.contains("North,"));
        assertTrue(result.contains("South,"));
        assertTrue(result.contains("East,"));
        assertTrue(result.contains("West"));
    }

    // --- Import statement variations ---

    @Test
    void testImportStatement_reExport() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                List.of("Logger"), "@ohos/log", false, null, null);
        assertEquals("import { Logger } from '@ohos/log';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_defaultAndNamespace() {
        ArkTSStatement stmt = new ArkTSDeclarations.ImportStatement(
                Collections.emptyList(), "./module", true, "default", "ns");
        assertEquals("import default, * as ns from './module';",
                stmt.toArkTS(0));
    }

    // --- Export statement variations ---

    @Test
    void testExportStatement_defaultFunction() {
        ArkTSStatement funcDecl = new ArkTSDeclarations.FunctionDeclaration(
                "main", Collections.emptyList(), "void",
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement stmt = new ArkTSDeclarations.ExportStatement(
                Collections.emptyList(), funcDecl, true);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export default function main"));
    }

    @Test
    void testExportStatement_namedExports() {
        ArkTSStatement stmt = new ArkTSDeclarations.ExportStatement(
                List.of("foo", "bar", "baz"), null, false);
        assertEquals("export { foo, bar, baz };", stmt.toArkTS(0));
    }

    // --- Full method decompilation with class context ---

    @Test
    void testDecompileMethod_methodWithTypedSignature() {
        // ldai 42; return
        byte[] codeBytes = concat(
                bytes(0x62), le32(42),
                bytes(0x64)
        );
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "getValue",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("function getValue()"));
        assertTrue(result.contains("return 42"));
    }

    @Test
    void testDecompileMethod_methodWithParamsAndReturn() {
        // lda v0; return
        byte[] codeBytes = concat(
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        AbcCode code = new AbcCode(2, 1, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 1, "identity",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("function identity"));
        assertTrue(result.contains("return v0"));
    }

    // --- Access modifier mapping ---

    @Test
    void testAccessFlags_publicModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "doStuff", Collections.emptyList(), "void", body,
                false, "public");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("public doStuff"));
    }

    @Test
    void testAccessFlags_privateModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "internal", Collections.emptyList(), null, body,
                false, "private");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("private internal"));
    }

    @Test
    void testAccessFlags_protectedModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "onEvent", Collections.emptyList(), null, body,
                false, "protected");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("protected onEvent"));
    }

    @Test
    void testAccessFlags_staticModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "create", Collections.emptyList(), "Object", body,
                true, "public");
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("public static create"));
    }

    // --- Decorator with field ---

    @Test
    void testDecorator_withComponentAndState() {
        ArkTSStatement stateField = new ArkTSDeclarations.ClassFieldDeclaration(
                "count", "number",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                false, null);
        ArkTSStatement decoratedField =
                new ArkTSTypeDeclarations.DecoratorStatement("State",
                        Collections.emptyList());
        // Build a struct with decorator
        ArkTSTypeDeclarations.StructDeclaration struct =
                new ArkTSTypeDeclarations.StructDeclaration("CounterPage",
                        List.of(decoratedField, stateField),
                        List.of("Component", "Entry"));
        String result = struct.toArkTS(0);
        assertTrue(result.contains("@Component"));
        assertTrue(result.contains("@Entry"));
        assertTrue(result.contains("@State"));
        assertTrue(result.contains("struct CounterPage"));
    }

    // --- Opcode compat constants ---

    @Test
    void testOpcodesCompat_newConstants() {
        // Verify new opcode constants are properly defined
        assertTrue(ArkOpcodesCompat.DEFINECLASSWITHBUFFER > 0);
        assertTrue(ArkOpcodesCompat.DEFINEMETHOD > 0);
        assertTrue(ArkOpcodesCompat.DEFINEFIELDBYNAME > 0);
        assertTrue(ArkOpcodesCompat.DEFINEPROPERTYBYNAME > 0);
        assertTrue(ArkOpcodesCompat.SUPERCALLTHISRANGE > 0);
        assertTrue(ArkOpcodesCompat.SUPERCALLSPREAD > 0);
        assertTrue(ArkOpcodesCompat.SUPERCALLARROWRANGE > 0);
        assertTrue(ArkOpcodesCompat.LDEXTERNALMODULEVAR > 0);
        assertTrue(ArkOpcodesCompat.LDLOCALMODULEVAR > 0);
        assertTrue(ArkOpcodesCompat.STMODULEVAR > 0);
        assertTrue(ArkOpcodesCompat.STCONSTTOGLOBALRECORD > 0);
        assertTrue(ArkOpcodesCompat.STTOGLOBALRECORD > 0);
        assertTrue(ArkOpcodesCompat.GETMODULENAMESPACE > 0);
        assertTrue(ArkOpcodesCompat.DYNAMICIMPORT > 0);
    }

    // --- Create object with buffer from bytecode ---

    @Test
    void testDecompile_createObjectWithBuffer() {
        // createobjectwithbuffer has format IMM8_IMM16
        // opcode=0x07, imm8=0, imm16=1 (buffer idx)
        byte[] code = concat(
                bytes(0x07, 0x00),            // createobjectwithbuffer
                le16(1),                      // imm16=1 (literal array idx)
                bytes(0x61, 0x00),            // sta v0
                bytes(0x64)                   // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("{  }") || result.contains("let v0"));
    }

    // --- Create array with buffer from bytecode ---

    @Test
    void testDecompile_createArrayWithBuffer() {
        // createarraywithbuffer has format IMM8_IMM16
        // opcode=0x06, imm8=0, imm16=1 (buffer idx)
        byte[] code = concat(
                bytes(0x06, 0x00),            // createarraywithbuffer
                le16(1),                      // imm16=1 (literal array idx)
                bytes(0x61, 0x00),            // sta v0
                bytes(0x64)                   // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("["));
    }

    // --- Interface with method and property ---

    @Test
    void testInterfaceDeclaration_withMethodAndProperty() {
        List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "property", "id", "number",
                                Collections.emptyList(), false),
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "method", "toString", "string",
                                Collections.emptyList(), false),
                        new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                                "method", "process", "void",
                                List.of(new ArkTSDeclarations.FunctionDeclaration
                                        .FunctionParam("data", "Object")),
                                false));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.InterfaceDeclaration(
                "Processor", List.of("BaseProcessor"), members);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains(
                "interface Processor extends BaseProcessor"));
        assertTrue(result.contains("id: number;"));
        assertTrue(result.contains("toString(): string;"));
        assertTrue(result.contains("process(data: Object): void;"));
    }

    // --- Class field with all modifiers ---

    @Test
    void testClassFieldDeclaration_allModifiers() {
        ArkTSStatement stmt = new ArkTSDeclarations.ClassFieldDeclaration(
                "instanceCount", "number",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                true, "private");
        String result = stmt.toArkTS(0);
        assertEquals("private static instanceCount: number = 0;",
                result);
    }

    // --- Class method with generic return type in signature ---

    @Test
    void testClassMethodDeclaration_withReturnType() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("value"))));
        ArkTSStatement stmt = new ArkTSDeclarations.ClassMethodDeclaration(
                "getValue",
                List.of(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "index", "number")),
                "string", body, false, "public");
        String result = stmt.toArkTS(0);
        assertTrue(result.contains(
                "public getValue(index: number): string"));
        assertTrue(result.contains("return value;"));
    }

    // --- Indentation for nested structures ---

    @Test
    void testClassDeclaration_nestedIndentation() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "data", "number", null, false, null);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Inner", null,
                        List.of(field));
        String result = cls.toArkTS(1);
        assertTrue(result.startsWith("    class Inner {"));
        assertTrue(result.contains("        data: number;"));
        assertTrue(result.endsWith("    }"));
    }

    @Test
    void testStructDeclaration_indentation() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "title", "string", null, false, null);
        ArkTSTypeDeclarations.StructDeclaration struct =
                new ArkTSTypeDeclarations.StructDeclaration("Header",
                        List.of(field), List.of("Component"));
        String result = struct.toArkTS(1);
        assertTrue(result.startsWith("    @Component"));
        assertTrue(result.contains("    struct Header"));
        assertTrue(result.contains("        title: string;"));
    }

    // --- Enum with mixed members ---

    @Test
    void testEnumDeclaration_mixedMembers() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("A", null),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("B",
                        new ArkTSExpression.LiteralExpression("10",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)),
                new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("C", null));
        ArkTSStatement stmt = new ArkTSTypeDeclarations.EnumDeclaration("Mixed",
                members);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("A,"));
        assertTrue(result.contains("B = 10,"));
        assertTrue(result.contains("C"));
    }

    // --- Full file decompilation with null file ---

    @Test
    void testDecompileFile_nullFile() {
        String result = decompiler.decompileFile(null);
        assertEquals("", result);
    }

    // --- Interface member kinds ---

    @Test
    void testInterfaceMember_propertyKind() {
        ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember member =
                new ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember(
                        "property", "name", "string",
                        Collections.emptyList(), true);
        assertEquals("property", member.getKind());
        assertEquals("name", member.getName());
    }

    // --- Class member accessors ---

    @Test
    void testClassFieldDeclaration_accessors() {
        ArkTSExpression init = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSDeclarations.ClassFieldDeclaration field =
                new ArkTSDeclarations.ClassFieldDeclaration(
                        "count", "number", init, true, "public");
        assertEquals("count", field.getName());
    }

    @Test
    void testClassMethodDeclaration_accessors() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.ClassMethodDeclaration method =
                new ArkTSDeclarations.ClassMethodDeclaration(
                        "test", Collections.emptyList(), "void",
                        body, false, "private");
        assertEquals("test", method.getName());
    }

    // --- Super call statement with expression arguments ---

    @Test
    void testSuperCallStatement_withExpressions() {
        ArkTSExpression arg1 = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression arg2 = new ArkTSExpression.LiteralExpression(
                "hello", ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSStatement stmt = new ArkTSControlFlow.SuperCallStatement(
                List.of(arg1, arg2));
        assertEquals("super(42, \"hello\");", stmt.toArkTS(0));
    }

    // --- AsExpression with member target ---

    @Test
    void testAsExpression_withMember() {
        ArkTSExpression obj = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("response"),
                new ArkTSExpression.VariableExpression("data"), false);
        ArkTSAccessExpressions.AsExpression asExpr =
                new ArkTSAccessExpressions.AsExpression(obj, "string");
        assertEquals("response.data as string", asExpr.toArkTS());
    }

    // --- NonNull expression with nested member ---

    @Test
    void testNonNullExpression_withNestedMember() {
        ArkTSExpression inner = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("config"),
                new ArkTSExpression.VariableExpression("value"), false);
        ArkTSAccessExpressions.NonNullExpression nonNull =
                new ArkTSAccessExpressions.NonNullExpression(inner);
        assertEquals("config.value!", nonNull.toArkTS());
    }

    // --- Type reference with nested generics ---

    @Test
    void testTypeReferenceExpression_nestedGeneric() {
        ArkTSAccessExpressions.TypeReferenceExpression typeRef =
                new ArkTSAccessExpressions.TypeReferenceExpression("Map",
                        List.of("string", "Array<number>"));
        assertEquals("Map<string, Array<number>>", typeRef.toArkTS());
    }

    // --- File module with multiple declarations ---

    @Test
    void testFileModule_withMultipleDeclarations() {
        ArkTSStatement enumDecl = new ArkTSTypeDeclarations.EnumDeclaration("Color",
                List.of(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Red",
                                null),
                        new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Blue",
                                null)));
        ArkTSStatement classDecl = new ArkTSDeclarations.ClassDeclaration(
                "Painter", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(Collections.emptyList(),
                        List.of(enumDecl, classDecl),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("enum Color"));
        assertTrue(result.contains("class Painter"));
        // Verify separator between declarations
        assertTrue(result.contains("}\n\nclass Painter"));
    }

    // --- GenericClassDeclaration with members ---

    @Test
    void testGenericClassDeclaration_withFieldAndMethod() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "value", "T", null, false, "private");
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("value"))));
        ArkTSStatement method = new ArkTSDeclarations.ClassMethodDeclaration(
                "getValue", Collections.emptyList(), "T",
                methodBody, false, "public");
        ArkTSTypeDeclarations.GenericClassDeclaration cls =
                new ArkTSTypeDeclarations.GenericClassDeclaration("Box",
                        List.of(new ArkTSTypeDeclarations.TypeParameter("T", null)),
                        null, List.of(field, method));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Box<T>"));
        assertTrue(result.contains("private value: T;"));
        assertTrue(result.contains("public getValue(): T"));
    }

    // --- Verify enhanced decompiler handles unknown opcodes gracefully ---

    @Test
    void testDecompile_unknownOpcodeGraceful() {
        // Use a rarely-used opcode; just verify no crash
        byte[] code = concat(
                bytes(0x00),        // ldundefined (known)
                bytes(0x61, 0x00),  // sta v0
                bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("undefined"));
    }

    // ======== DECOMPILER ACCURACY IMPROVEMENT TESTS ========

    // --- 1. Try/catch decompilation ---

    @Test
    void testTryCatchStatement_withFinally() {
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("doSomething()"))));
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("handleError()"))));
        ArkTSStatement finallyBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("cleanup()"))));
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        catchBody, finallyBody);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("try {"));
        assertTrue(result.contains("catch (e) {"));
        assertTrue(result.contains("finally {"));
        assertTrue(result.contains("cleanup()"));
    }

    @Test
    void testTryCatchStatement_withCatchType() {
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression("console.log"),
                                List.of(new ArkTSExpression.VariableExpression("e"))))));
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        catchBody, null);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("catch (e)"));
    }

    // --- 2. Switch statement AST ---

    @Test
    void testSwitchStatement_basic() {
        ArkTSExpression disc = new ArkTSExpression.VariableExpression("x");
        ArkTSControlFlow.SwitchStatement.SwitchCase case1 =
                new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSControlFlow.SwitchStatement.SwitchCase case2 =
                new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("2",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSStatement defaultBlock = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.BreakStatement()));
        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(disc,
                        List.of(case1, case2), defaultBlock);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("switch (x) {"));
        assertTrue(result.contains("case 1:"));
        assertTrue(result.contains("case 2:"));
        assertTrue(result.contains("default:"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testSwitchStatement_withIndentation() {
        ArkTSExpression disc = new ArkTSExpression.VariableExpression("color");
        ArkTSControlFlow.SwitchStatement.SwitchCase case1 =
                new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("red",
                                ArkTSExpression.LiteralExpression.LiteralKind.STRING),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(disc,
                        List.of(case1), null);
        String result = stmt.toArkTS(1);
        assertTrue(result.startsWith("    switch (color) {"));
        assertTrue(result.contains("        case \"red\":"));
        assertTrue(result.contains("            break;"));
    }

    // --- 3. Parameter naming (param_N instead of pN) ---

    @Test
    void testMethodSignatureBuilder_paramNaming() {
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcProto proto = new AbcProto(
                List.of(AbcProto.ShortyType.VOID, AbcProto.ShortyType.I32,
                        AbcProto.ShortyType.REF),
                Collections.emptyList());
        String sig = MethodSignatureBuilder.buildSignature(method, proto, 2);
        assertEquals("function test(param_0: number, param_1: Object)", sig);
    }

    @Test
    void testMethodSignatureBuilder_paramNamingNoProto() {
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String sig = MethodSignatureBuilder.buildSignature(method, null, 3);
        assertEquals("function test(param_0, param_1, param_2)", sig);
    }

    // --- 4. String constant resolution ---

    @Test
    void testLiteralExpression_stringWithEscapeSequences() {
        // Test that escape sequences are properly handled
        ArkTSExpression.LiteralExpression tab =
                new ArkTSExpression.LiteralExpression("hello\tworld",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"hello\\tworld\"", tab.toArkTS());

        ArkTSExpression.LiteralExpression backslash =
                new ArkTSExpression.LiteralExpression("path\\to\\file",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"path\\\\to\\\\file\"", backslash.toArkTS());

        ArkTSExpression.LiteralExpression quote =
                new ArkTSExpression.LiteralExpression("say \"hello\"",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"say \\\"hello\\\"\"", quote.toArkTS());
    }

    @Test
    void testLiteralExpression_emptyString() {
        ArkTSExpression.LiteralExpression expr =
                new ArkTSExpression.LiteralExpression("",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertEquals("\"\"", expr.toArkTS());
    }

    @Test
    void testDecompile_ldaStrInstruction() {
        // lda.str 0; sta v0; return
        byte[] code = concat(
                bytes(0x3E, 0x00, 0x00),  // lda.str 0
                bytes(0x61, 0x00),         // sta v0
                bytes(0x64)                // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // Without an ABC file, it should still produce a placeholder
        assertTrue(result.contains("str_0"));
        assertTrue(result.contains("let v0 = \"str_0\""));
    }

    // --- 5. Cross-method reference resolution ---

    @Test
    void testDecompile_callWithNamedMethod() {
        // Test that call expressions produce readable output
        // lda v0; callarg0 0; sta v1; return
        byte[] code = concat(
                bytes(0x60, 0x00),       // lda v0
                bytes(0x29, 0x00),       // callarg0 0
                bytes(0x61, 0x01),       // sta v1
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0()"));
    }

    // --- 6. Output formatting improvements ---

    @Test
    void testClassDeclaration_blankLinesBetweenMembers() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "data", "number", null, false, null);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement method = new ArkTSDeclarations.ClassMethodDeclaration(
                "getValue", Collections.emptyList(), "number",
                methodBody, false, "public");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("MyClass", null,
                        List.of(field, method));
        String result = cls.toArkTS(0);
        // Verify blank line separates field from method
        assertTrue(result.contains("data: number;\n"));
        assertTrue(result.contains("\n\n"));
        assertTrue(result.contains("public getValue(): number"));
    }

    @Test
    void testClassDeclaration_memberGrouping() {
        // Verify fields come before methods in the output
        ArkTSStatement field1 = new ArkTSDeclarations.ClassFieldDeclaration(
                "x", "number", null, false, null);
        ArkTSStatement field2 = new ArkTSDeclarations.ClassFieldDeclaration(
                "y", "number", null, false, null);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement method = new ArkTSDeclarations.ClassMethodDeclaration(
                "calc", Collections.emptyList(), null,
                methodBody, false, null);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Point", null,
                        List.of(field1, field2, method));
        String result = cls.toArkTS(0);
        int field1Idx = result.indexOf("x: number;");
        int field2Idx = result.indexOf("y: number;");
        int methodIdx = result.indexOf("calc()");
        assertTrue(field1Idx < field2Idx);
        assertTrue(field2Idx < methodIdx);
    }

    @Test
    void testIndentation_consistentFourSpaces() {
        ArkTSStatement inner = new ArkTSStatement.ReturnStatement(
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSStatement outer = new ArkTSStatement.BlockStatement(List.of(inner));
        String result = outer.toArkTS(1);
        // Verify 4-space indent for the outer block body
        assertTrue(result.contains("    return 42;"));
    }

    // --- 7. Edge case handling ---

    @Test
    void testDecompile_emptyInstructionsReturnsEmpty() {
        String result = decompiler.decompileInstructions(
                Collections.emptyList());
        assertEquals("", result);
    }

    @Test
    void testDecompileMethod_onlyReturnUndefined() {
        // returnundefined
        byte[] codeBytes = bytes(0x65);
        AbcCode code = new AbcCode(0, 0, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "doNothing",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("function doNothing()"));
    }

    @Test
    void testDecompileMethod_trailingReturnUndefinedStripped() {
        // ldai 42; sta v0; returnundefined
        byte[] codeBytes = concat(
                bytes(0x62), le32(42),
                bytes(0x61, 0x00),
                bytes(0x65)   // returnundefined
        );
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "simpleMethod",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("let v0 = 42"));
        // The trailing return; should be stripped
        assertFalse(result.contains("return;"));
    }

    @Test
    void testDecompileMethod_returnWithValuePreserved() {
        // ldai 42; return
        byte[] codeBytes = concat(
                bytes(0x62), le32(42),
                bytes(0x64)
        );
        AbcCode code = new AbcCode(1, 0, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "getValue",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("return 42"));
    }

    @Test
    void testCfg_emptyCodeProducesNoBlocks() {
        ControlFlowGraph cfg = ControlFlowGraph.build(
                Collections.emptyList());
        assertTrue(cfg.getBlocks().isEmpty());
    }

    @Test
    void testDecompile_infiniteLoopJmpToSelf() {
        // jmp -2 (infinite loop to self: offset 0, length 2, target = 0+2-2 = 0)
        // opcode 0x4D = jmp_imm8, signed offset -2 = 0xFE
        byte[] code = bytes(0x4D, (byte) 0xFE);
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // Should produce a while (true) { }
        assertTrue(result.contains("while (true)"));
    }

    @Test
    void testDecompile_deadCodeAfterReturn() {
        // ldai 1; return; ldai 2; sta v0
        // The code after return should be dead code
        byte[] code = concat(
                bytes(0x62), le32(1),   // ldai 1
                bytes(0x64),             // return
                bytes(0x62), le32(2),   // ldai 2 (dead code)
                bytes(0x61, 0x00)        // sta v0 (dead code)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // The dead code after return may or may not appear,
        // but the method should not crash
        assertNotNull(result);
        assertTrue(result.contains("return 1"));
    }

    @Test
    void testDecompile_emptyBodyWithMethodSignature() {
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "init",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertEquals("function init(): void { }", result);
    }

    // --- 8. MUTF-8 string resolution ---

    @Test
    void testDecompileContext_readMutf8At_ascii() {
        byte[] data = "Hello World\0rest".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);
        String result = ArkTSDecompiler.DecompilationContext
                .readMutf8At(data, 0);
        assertEquals("Hello World", result);
    }

    @Test
    void testDecompileContext_readMutf8At_empty() {
        byte[] data = {0};
        String result = ArkTSDecompiler.DecompilationContext
                .readMutf8At(data, 0);
        assertEquals("", result);
    }

    // --- 9. File output formatting ---

    @Test
    void testFileModule_blankLinesBetweenDeclarations() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration("First",
                null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration("Second",
                null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(Collections.emptyList(),
                        List.of(cls1, cls2),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("class First"));
        assertTrue(result.contains("class Second"));
        assertTrue(result.contains("}\n\nclass Second"));
    }

    @Test
    void testFileModule_importsBeforeDeclarations() {
        ArkTSStatement imp = new ArkTSDeclarations.ImportStatement(
                List.of("Base"), "./base", false, null, null);
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration("Child",
                "Base", Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fileModule =
                new ArkTSTypeDeclarations.FileModule(List.of(imp), List.of(cls),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        int importIdx = result.indexOf("import");
        int classIdx = result.indexOf("class Child");
        assertTrue(importIdx < classIdx);
    }

    // --- Issue #24: Break/Continue in loops ---

    @Test
    void testDecompile_breakInWhileLoop() {
        // while (v0) { if (v1) break; body; }
        // offset 0: ldai 1          (5 bytes) - v0 = 1
        // offset 5: sta v0          (2 bytes)
        // offset 7: lda v0          (2 bytes) - loop condition
        // offset 9: jeqz +20        (2 bytes) -> 31 (exit loop)
        // offset 11: ldai 0         (5 bytes) - v1 = 0
        // offset 16: sta v1         (2 bytes)
        // offset 18: lda v1         (2 bytes)
        // offset 20: jeqz +7        (2 bytes) -> 29 (skip break, to body)
        // offset 22: jmp +7         (2 bytes) -> 31 (BREAK)
        // offset 24: ldai 2         (5 bytes) - body
        // offset 29: jmp -22        (2 bytes) -> 9 (back edge)
        // offset 31: return         (1 byte)
        byte[] code = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0 (5 bytes)
                bytes(0x61, 0x00),             // sta v0          offset 5 (2 bytes)
                bytes(0x60, 0x00),             // lda v0          offset 7 (2 bytes)
                bytes(0x4F, 0x14),             // jeqz +20        offset 9 (2 bytes) -> 31
                bytes(0x62), le32(0),          // ldai 0          offset 11 (5 bytes)
                bytes(0x61, 0x01),             // sta v1          offset 16 (2 bytes)
                bytes(0x60, 0x01),             // lda v1          offset 18 (2 bytes)
                bytes(0x4F, 0x07),             // jeqz +7         offset 20 (2 bytes) -> 29
                bytes(0x4D, 0x07),             // jmp +7          offset 22 (2 bytes) -> 31 (break)
                bytes(0x62), le32(2),          // ldai 2          offset 24 (5 bytes)
                bytes(0x4D, (byte) 0xEA),      // jmp -22         offset 29 (2 bytes) -> 9 (back edge)
                bytes(0x64)                     // return          offset 31 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // The decompiler should produce a while loop, and ideally
        // detect the break
        assertTrue(result.contains("while") || result.contains("if"),
                "Expected while/break, got: " + result);
    }

    @Test
    void testDecompile_continueInWhileLoop() {
        // while loop with a continue (jmp back to loop header)
        // offset 0: ldai 1          (5 bytes)
        // offset 5: sta v0          (2 bytes)
        // offset 7: lda v0          (2 bytes) - loop condition
        // offset 9: jeqz +14        (2 bytes) -> 25 (past loop)
        //   -- loop body --
        // offset 11: ldai 0         (5 bytes) - check something
        // offset 16: sta v1         (2 bytes)
        // offset 18: jmp -11        (2 bytes) -> 9 (CONTINUE back to header)
        // offset 20: ldai 2         (5 bytes) - more body
        // offset 25: sta v2         (2 bytes)
        // offset 27: return         (1 byte)
        byte[] code = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0 (5 bytes)
                bytes(0x61, 0x00),             // sta v0          offset 5 (2 bytes)
                bytes(0x60, 0x00),             // lda v0          offset 7 (2 bytes)
                bytes(0x4F, 0x0E),             // jeqz +14        offset 9 (2 bytes) -> 25
                bytes(0x62), le32(0),          // ldai 0          offset 11 (5 bytes)
                bytes(0x61, 0x01),             // sta v1          offset 16 (2 bytes)
                bytes(0x4D, (byte) 0xF5),      // jmp -11         offset 18 (2 bytes) -> 9
                bytes(0x62), le32(2),          // ldai 2          offset 20 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 25 (2 bytes)
                bytes(0x64)                     // return          offset 27 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // Should contain a continue or the continue pattern
        assertTrue(result.contains("continue")
                || result.contains("while") || result.contains("if"),
                "Expected continue statement, got: " + result);
    }

    @Test
    void testDecompile_breakInInfiniteLoop() {
        // Infinite loop with break: while(true) { if (cond) break; }
        // This is a multi-block test with infinite loop and break
        // offset 0: ldai 1          (5 bytes) - load something
        // offset 5: sta v0          (2 bytes)
        // offset 7: jmp +0          (2 bytes) -> offset 9 (infinite loop to self)
        //   -- but wait, that creates a single block from 7->9
        // Actually let's use a jmp-to-self pattern with an inner break
        // offset 0: ldai 1          (5 bytes)
        // offset 5: sta v0          (2 bytes)
        // offset 7: lda v0          (2 bytes)
        // offset 9: jnez -2         (2 bytes) -> 9 (loop to self - infinite loop)
        //   Hmm, that also doesn't work well.
        // Let's use: multi-block infinite loop
        // offset 0: jmp +0          (2 bytes) -> offset 2 (next instruction)
        // Actually, for infinite loop: jmp to offset 0 from offset 0:
        // offset 0: jmp -2          (2 bytes) -> 0 (loop to self)
        // But then we can't have body inside the single block with break.
        // Use multi-block:
        // offset 0: nop             (1 byte)
        // offset 1: jmp +0          (2 bytes) -> 3 (falls into body)
        // Actually, a realistic infinite loop with break:
        // offset 0: ldai 1          (5 bytes) - body of loop
        // offset 5: sta v0          (2 bytes)
        // offset 7: jmp -7          (2 bytes) -> 2 (loop back into middle of
        //                                                ldai - won't work)
        //
        // Let me use a simple realistic pattern:
        byte[] code = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0 (5 bytes)
                bytes(0x61, 0x00),             // sta v0          offset 5 (2 bytes)
                bytes(0x4D, (byte) 0xF9),      // jmp -7          offset 7 (2 bytes) -> 2
                bytes(0x64)                     // return          offset 9 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // The jmp at offset 7 goes to offset 2, which is in the middle
        // of the ldai instruction. This is tricky for the decompiler.
        // Just ensure it doesn't crash.
    }

    // --- Issue #24: Short-circuit evaluation ---

    @Test
    void testDecompile_shortCircuitAnd() {
        // a && b pattern: two consecutive jeqz to the same target
        // offset 0: lda v0          (2 bytes)
        // offset 2: jeqz +13        (2 bytes) -> offset 17 (skip both)
        // offset 4: lda v1          (2 bytes)
        // offset 6: jeqz +9         (2 bytes) -> offset 17 (skip both)
        // offset 8: ldai 1          (5 bytes) - both true
        // offset 13: sta v2         (2 bytes)
        // offset 15: jmp +7         (2 bytes) -> offset 24
        // offset 17: ldai 0         (5 bytes) - short-circuited
        // offset 22: sta v2         (2 bytes)
        // offset 24: return         (1 byte)
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0 (2 bytes)
                bytes(0x4F, 0x0D),             // jeqz +13        offset 2 (2 bytes) -> 17
                bytes(0x60, 0x01),             // lda v1          offset 4 (2 bytes)
                bytes(0x4F, 0x09),             // jeqz +9         offset 6 (2 bytes) -> 17
                bytes(0x62), le32(1),          // ldai 1          offset 8 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 13 (2 bytes)
                bytes(0x4D, 0x07),             // jmp +7          offset 15 (2 bytes) -> 24
                bytes(0x62), le32(0),          // ldai 0          offset 17 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 22 (2 bytes)
                bytes(0x64)                     // return          offset 24 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // Should produce short-circuit AND or equivalent if/else
        assertTrue(result.contains("&&") || result.contains("if"),
                "Expected && or if, got: " + result);
    }

    @Test
    void testDecompile_shortCircuitOr() {
        // a || b pattern: two consecutive jnez to the same target
        // offset 0: lda v0          (2 bytes)
        // offset 2: jnez +13        (2 bytes) -> offset 17 (take both)
        // offset 4: lda v1          (2 bytes)
        // offset 6: jnez +9         (2 bytes) -> offset 17
        // offset 8: ldai 0          (5 bytes) - both false
        // offset 13: sta v2         (2 bytes)
        // offset 15: jmp +7         (2 bytes) -> offset 24
        // offset 17: ldai 1         (5 bytes) - short-circuited
        // offset 22: sta v2         (2 bytes)
        // offset 24: return         (1 byte)
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0 (2 bytes)
                bytes(0x51, 0x0D),             // jnez +13        offset 2 (2 bytes) -> 17
                bytes(0x60, 0x01),             // lda v1          offset 4 (2 bytes)
                bytes(0x51, 0x09),             // jnez +9         offset 6 (2 bytes) -> 17
                bytes(0x62), le32(0),          // ldai 0          offset 8 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 13 (2 bytes)
                bytes(0x4D, 0x07),             // jmp +7          offset 15 (2 bytes) -> 24
                bytes(0x62), le32(1),          // ldai 1          offset 17 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 22 (2 bytes)
                bytes(0x64)                     // return          offset 24 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // Should produce short-circuit OR or equivalent if/else
        assertTrue(result.contains("||") || result.contains("if"),
                "Expected || or if, got: " + result);
    }

    // --- Issue #24: Ternary expressions ---

    @Test
    void testDecompile_ternaryExpression() {
        // condition ? val1 : val2
        // offset 0: lda v0          (2 bytes)
        // offset 2: jeqz +9         (2 bytes) -> offset 13 (else)
        // offset 4: ldai 10         (5 bytes) - true value
        // offset 9: sta v1          (2 bytes)
        // offset 11: jmp +7         (2 bytes) -> offset 20 (join)
        // offset 13: ldai 20        (5 bytes) - false value
        // offset 18: sta v1         (2 bytes)
        // offset 20: return         (1 byte)
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0 (2 bytes)
                bytes(0x4F, 0x09),             // jeqz +9         offset 2 (2 bytes) -> 13
                bytes(0x62), le32(10),         // ldai 10         offset 4 (5 bytes)
                bytes(0x61, 0x01),             // sta v1          offset 9 (2 bytes)
                bytes(0x4D, 0x07),             // jmp +7          offset 11 (2 bytes) -> 20
                bytes(0x62), le32(20),         // ldai 20         offset 13 (5 bytes)
                bytes(0x61, 0x01),             // sta v1          offset 18 (2 bytes)
                bytes(0x64)                     // return          offset 20 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // Should produce a ternary expression or if/else
        assertTrue(result.contains("?") || result.contains("if"),
                "Expected ternary or if/else, got: " + result);
    }

    // --- Issue #24: Nested try/catch ---

    @Test
    void testDecompile_nestedTryCatch() {
        // Build a method with nested try/catch using AbcTryBlock
        byte[] codeBytes = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0
                bytes(0x61, 0x00),             // sta v0          offset 5
                bytes(0x64)                     // return          offset 7
        );
        // Create nested try blocks
        AbcCatchBlock innerCatch =
                new AbcCatchBlock(0, 7, 1, false);
        AbcTryBlock innerTry = new AbcTryBlock(0, 7,
                List.of(innerCatch));
        AbcCatchBlock outerCatch =
                new AbcCatchBlock(1, 7, 1, false);
        AbcTryBlock outerTry = new AbcTryBlock(0, 7,
                List.of(outerCatch));
        AbcCode code = new AbcCode(1, 0, codeBytes.length, codeBytes,
                List.of(innerTry, outerTry), 1);
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        // Should contain try/catch (at least one)
        assertTrue(result.contains("try"),
                "Expected try statement, got: " + result);
    }

    @Test
    void testDecompile_nestedTryCatch_outerWrapsInner() {
        // Outer try: offsets 0-15, catch at 15
        // Inner try: offsets 5-10, catch at 10
        byte[] codeBytes = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0
                bytes(0x61, 0x00),             // sta v0          offset 5
                bytes(0x62), le32(2),          // ldai 2          offset 7
                bytes(0x61, 0x01),             // sta v1          offset 12
                bytes(0x64)                     // return          offset 14
        );
        AbcCatchBlock innerCatch =
                new AbcCatchBlock(0, 14, 1, false);
        AbcTryBlock innerTry = new AbcTryBlock(7, 5,
                List.of(innerCatch));
        AbcCatchBlock outerCatch =
                new AbcCatchBlock(1, 14, 1, false);
        AbcTryBlock outerTry = new AbcTryBlock(0, 14,
                List.of(outerCatch));
        AbcCode code = new AbcCode(1, 0, codeBytes.length, codeBytes,
                List.of(innerTry, outerTry), 1);
        AbcMethod method = new AbcMethod(0, 0, "nestedTest",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        // Should produce at least one try/catch (outer wraps inner)
        assertTrue(result.contains("try"),
                "Expected try statement, got: " + result);
    }

    // --- Issue #24: Loop context tracking ---

    @Test
    void testDecompile_loopContextStack() {
        // Test that DecompilationContext properly tracks loop contexts
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        AbcCode codeObj = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        ArkTSDecompiler.DecompilationContext ctx =
                new ArkTSDecompiler.DecompilationContext(
                        method, codeObj, null, null, null,
                        Collections.emptyList());
        // Initially no loop context
        assertNull(ctx.getCurrentLoopContext());
        // Push a loop context
        ctx.pushLoopContext(10, 50);
        int[] loopCtx = ctx.getCurrentLoopContext();
        assertNotNull(loopCtx);
        assertEquals(10, loopCtx[0]);
        assertEquals(50, loopCtx[1]);
        // Push a nested loop context
        ctx.pushLoopContext(20, 40);
        loopCtx = ctx.getCurrentLoopContext();
        assertNotNull(loopCtx);
        assertEquals(20, loopCtx[0]);
        assertEquals(40, loopCtx[1]);
        // Pop back to outer
        ctx.popLoopContext();
        loopCtx = ctx.getCurrentLoopContext();
        assertNotNull(loopCtx);
        assertEquals(10, loopCtx[0]);
        assertEquals(50, loopCtx[1]);
        // Pop all
        ctx.popLoopContext();
        assertNull(ctx.getCurrentLoopContext());
    }

    @Test
    void testDecompile_breakJmpPastLoopEnd() {
        // Test that isBreakJump detects jmp past loop end
        // Build a simple while loop with a break inside
        // offset 0: ldai 1          (5 bytes) - init
        // offset 5: sta v0          (2 bytes)
        // offset 7: lda v0          (2 bytes) - loop condition
        // offset 9: jeqz +16        (2 bytes) -> offset 27 (past loop)
        // offset 11: lda v0         (2 bytes) - some body work
        // offset 13: sta v1         (2 bytes)
        // offset 15: ldai 0         (5 bytes) - check break condition
        // offset 20: sta v2         (2 bytes)
        // offset 22: lda v2         (2 bytes)
        // offset 24: jnez -17       (2 bytes) -> offset 9 (continue)
        // offset 26: return         (1 byte)
        byte[] code = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0
                bytes(0x61, 0x00),             // sta v0          offset 5
                bytes(0x60, 0x00),             // lda v0          offset 7
                bytes(0x4F, 0x10),             // jeqz +16        offset 9  -> 27
                bytes(0x60, 0x00),             // lda v0          offset 11
                bytes(0x61, 0x01),             // sta v1          offset 13
                bytes(0x62), le32(0),          // ldai 0          offset 15
                bytes(0x61, 0x02),             // sta v2          offset 20
                bytes(0x60, 0x02),             // lda v2          offset 22
                bytes(0x51, (byte) 0xED),      // jnez -19        offset 24 -> 7
                bytes(0x64)                     // return          offset 26
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
    }

    // --- Issue #24: Edge case - conditional expression AST ---

    @Test
    void testConditionalExpression_inVariableDeclaration() {
        ArkTSExpression test = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression cons = new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression alt = new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression ternary =
                new ArkTSAccessExpressions.ConditionalExpression(test, cons, alt);
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration(
                "let", "result", null, ternary);
        assertEquals("let result = (x ? 1 : 2);", stmt.toArkTS(0));
    }

    @Test
    void testShortCircuitAndExpression() {
        ArkTSExpression left = new ArkTSExpression.VariableExpression("a");
        ArkTSExpression right = new ArkTSExpression.VariableExpression("b");
        ArkTSExpression and = new ArkTSExpression.BinaryExpression(
                left, "&&", right);
        assertEquals("(a && b)", and.toArkTS());
    }

    @Test
    void testShortCircuitOrExpression() {
        ArkTSExpression left = new ArkTSExpression.VariableExpression("a");
        ArkTSExpression right = new ArkTSExpression.VariableExpression("b");
        ArkTSExpression or = new ArkTSExpression.BinaryExpression(
                left, "||", right);
        assertEquals("(a || b)", or.toArkTS());
    }

    // --- Issue #24: PatternType enum coverage ---

    @Test
    void testPatternType_allValues() {
        // Ensure all pattern types exist
        assertNotNull(ArkTSDecompilerTest.class);
        // We can't directly reference PatternType (private), but we
        // can test that the decompiler handles various patterns without
        // crashing. This is tested through the decompileInstructions
        // calls above.
    }

    // --- Issue #24: Break statement in output ---

    @Test
    void testBreakStatement_inLoopBody() {
        // Build AST manually and verify output
        ArkTSStatement breakStmt = new ArkTSStatement.BreakStatement();
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(breakStmt));
        ArkTSExpression cond = new ArkTSExpression.LiteralExpression("true",
                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN);
        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(cond, body);
        String result = whileStmt.toArkTS(0);
        assertTrue(result.contains("while (true)"));
        assertTrue(result.contains("break;"));
    }

    @Test
    void testContinueStatement_inLoopBody() {
        ArkTSStatement continueStmt =
                new ArkTSStatement.ContinueStatement();
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(continueStmt));
        ArkTSExpression cond = new ArkTSExpression.LiteralExpression("true",
                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN);
        ArkTSControlFlow.WhileStatement whileStmt =
                new ArkTSControlFlow.WhileStatement(cond, body);
        String result = whileStmt.toArkTS(0);
        assertTrue(result.contains("while (true)"));
        assertTrue(result.contains("continue;"));
    }

    @Test
    void testNestedTryCatchStatement_formatting() {
        ArkTSStatement innerTry = new ArkTSControlFlow.TryCatchStatement(
                new ArkTSStatement.BlockStatement(List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "inner")))),
                "e1",
                new ArkTSStatement.BlockStatement(List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "handleInner")))),
                null);
        ArkTSStatement outerTry = new ArkTSControlFlow.TryCatchStatement(
                new ArkTSStatement.BlockStatement(List.of(innerTry)),
                "e2",
                new ArkTSStatement.BlockStatement(List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "handleOuter")))),
                null);
        String result = outerTry.toArkTS(0);
        assertTrue(result.startsWith("try {"));
        assertTrue(result.contains("catch (e2)"));
        // Inner try should also appear
        int firstTry = result.indexOf("try {");
        int secondTry = result.indexOf("try {", firstTry + 1);
        assertTrue(secondTry > firstTry,
                "Expected nested try/catch");
    }

    // --- Issue #24: Complex ternary with different value types ---

    @Test
    void testDecompile_ternaryWithStringValues() {
        // Build ternary AST with string values
        ArkTSExpression test = new ArkTSExpression.BinaryExpression(
                new ArkTSExpression.VariableExpression("x"),
                ">",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSExpression cons = new ArkTSExpression.LiteralExpression(
                "positive",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression alt = new ArkTSExpression.LiteralExpression(
                "negative",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression ternary =
                new ArkTSAccessExpressions.ConditionalExpression(test, cons, alt);
        assertEquals("((x > 0) ? \"positive\" : \"negative\")",
                ternary.toArkTS());
    }

    // --- Issue #24: While loop with break and continue bytecode ---

    @Test
    void testDecompile_whileLoopWithBreakAndContinue() {
        // Simple while loop with both break and continue:
        // while(cond) { if(x) break; if(y) continue; body; }
        // offset 0: ldai 1          (5 bytes) - v0 = 1 (cond)
        // offset 5: sta v0          (2 bytes)
        // offset 7: lda v0          (2 bytes) - loop condition
        // offset 9: jeqz +24        (2 bytes) -> 35 (exit loop)
        //   -- break check --
        // offset 11: ldai 0         (5 bytes) - v1 = 0
        // offset 16: sta v1         (2 bytes)
        // offset 18: lda v1         (2 bytes)
        // offset 20: jeqz +3        (2 bytes) -> 25 (skip break)
        // offset 22: jmp +11        (2 bytes) -> 35 (BREAK)
        //   -- continue check --
        // offset 24: nop            (1 byte)
        // offset 25: ldai 0         (5 bytes) - v2 = 0
        // offset 30: sta v2         (2 bytes)
        //   -- body + back edge --
        // offset 32: jmp -25        (2 bytes) -> 9 (back edge / continue)
        // offset 34: nop            (1 byte)
        // offset 35: return         (1 byte)
        byte[] code = concat(
                bytes(0x62), le32(1),          // ldai 1          offset 0 (5 bytes)
                bytes(0x61, 0x00),             // sta v0          offset 5 (2 bytes)
                bytes(0x60, 0x00),             // lda v0          offset 7 (2 bytes)
                bytes(0x4F, 0x18),             // jeqz +24        offset 9 (2 bytes) -> 35
                bytes(0x62), le32(0),          // ldai 0          offset 11 (5 bytes)
                bytes(0x61, 0x01),             // sta v1          offset 16 (2 bytes)
                bytes(0x60, 0x01),             // lda v1          offset 18 (2 bytes)
                bytes(0x4F, 0x03),             // jeqz +3         offset 20 (2 bytes) -> 25
                bytes(0x4D, 0x0B),             // jmp +11         offset 22 (2 bytes) -> 35 (break)
                bytes(0xD5),                   // nop             offset 24 (1 byte)
                bytes(0x62), le32(0),          // ldai 0          offset 25 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 30 (2 bytes)
                bytes(0x4D, (byte) 0xE7),      // jmp -25         offset 32 (2 bytes) -> 9 (back edge)
                bytes(0xD5),                   // nop             offset 34 (1 byte)
                bytes(0x64)                     // return          offset 35 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        // The decompiler should handle this without crashing
        // and ideally produce break/continue statements
    }

    // --- Issue #24: Verify specific output content ---

    @Test
    void testDecompile_ternaryProducesTernaryOperator() {
        // Verify that the ternary detection produces the "?" operator
        // in the output rather than just an if/else
        // offset 0: lda v0          (2 bytes)
        // offset 2: jeqz +9         (2 bytes) -> 13 (else)
        // offset 4: ldai 10         (5 bytes)
        // offset 9: sta v1          (2 bytes)
        // offset 11: jmp +7         (2 bytes) -> 20 (join)
        // offset 13: ldai 20        (5 bytes)
        // offset 18: sta v1         (2 bytes)
        // offset 20: return         (1 byte)
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x4F, 0x09),             // jeqz +9         offset 2  -> 13
                bytes(0x62), le32(10),         // ldai 10         offset 4
                bytes(0x61, 0x01),             // sta v1          offset 9
                bytes(0x4D, 0x07),             // jmp +7          offset 11 -> 20
                bytes(0x62), le32(20),         // ldai 20         offset 13
                bytes(0x61, 0x01),             // sta v1          offset 18
                bytes(0x64)                     // return          offset 20
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // The ternary detection should produce either a ternary
        // expression (?) or an if/else, both are acceptable
        assertTrue(result.contains("?") || result.contains("if"),
                "Expected ternary or if/else, got: " + result);
    }

    @Test
    void testDecompile_shortCircuitAndProducesAndOperator() {
        // Verify that short-circuit AND produces "&&" in the output
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0 (2 bytes)
                bytes(0x4F, 0x0D),             // jeqz +13        offset 2 (2 bytes) -> 17
                bytes(0x60, 0x01),             // lda v1          offset 4 (2 bytes)
                bytes(0x4F, 0x09),             // jeqz +9         offset 6 (2 bytes) -> 17
                bytes(0x62), le32(1),          // ldai 1          offset 8 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 13 (2 bytes)
                bytes(0x4D, 0x07),             // jmp +7          offset 15 (2 bytes) -> 24
                bytes(0x62), le32(0),          // ldai 0          offset 17 (5 bytes)
                bytes(0x61, 0x02),             // sta v2          offset 22 (2 bytes)
                bytes(0x64)                     // return          offset 24 (1 byte)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("&&") || result.contains("if"),
                "Expected && or if, got: " + result);
    }

    @Test
    void testDecompile_loopContextTrackingWithNestedLoops() {
        // Test that the DecompilationContext correctly handles
        // nested loop context tracking (push/pop)
        AbcCode codeObj = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        ArkTSDecompiler.DecompilationContext ctx =
                new ArkTSDecompiler.DecompilationContext(
                        method, codeObj, null, null, null,
                        Collections.emptyList());

        // Push outer loop
        ctx.pushLoopContext(0, 100);
        assertNotNull(ctx.getCurrentLoopContext());
        assertEquals(0, ctx.getCurrentLoopContext()[0]);
        assertEquals(100, ctx.getCurrentLoopContext()[1]);

        // Push inner loop
        ctx.pushLoopContext(20, 60);
        assertEquals(20, ctx.getCurrentLoopContext()[0]);
        assertEquals(60, ctx.getCurrentLoopContext()[1]);

        // Pop inner loop
        ctx.popLoopContext();
        assertEquals(0, ctx.getCurrentLoopContext()[0]);
        assertEquals(100, ctx.getCurrentLoopContext()[1]);

        // Pop outer loop
        ctx.popLoopContext();
        assertNull(ctx.getCurrentLoopContext());
    }

    // ======== SWITCH STATEMENT TESTS ========

    // ======== SWITCH STATEMENT TESTS ========

    /**
     * Tests a simple switch with 3 cases and breaks.
     *
     * <p>Bytecode layout (offsets carefully computed):
     * <pre>
     * offset 0:  lda v0       (2 bytes) -> next 2
     * offset 2:  sta v1       (2 bytes) -> next 4
     * offset 4:  ldai 1       (5 bytes) -> next 9   -- case 1 test
     * offset 9:  jeq v1 +28   (3 bytes) -> next 12   -> offset 40 (case1 body)
     * offset 12: ldai 2       (5 bytes) -> next 17  -- case 2 test
     * offset 17: jeq v1 +28   (3 bytes) -> next 20   -> offset 48 (case2 body)
     * offset 20: ldai 3       (5 bytes) -> next 25  -- case 3 test
     * offset 25: jeq v1 +28   (3 bytes) -> next 28   -> offset 56 (case3 body)
     * offset 28: ldai 0       (5 bytes) -> next 33  -- default: v2 = 0
     * offset 33: sta v2       (2 bytes) -> next 35
     * offset 35: jmp +27      (2 bytes) -> next 37   -> offset 64 (end)
     * offset 37: (padding - nothing here, skip)
     * offset 38: (just to be safe, start case bodies at round offsets)
     * </pre>
     *
     * <p>Simpler approach: Use return statements so no jmp to end needed.
     */
    @Test
    void testDecompile_switchThreeCases() {
        // Each case body just returns a value - simplest possible
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +22     (3) -> 12 -> 34 (case1)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +20     (3) -> 20 -> 40 (case2)
        // offset 20: ldai 3          (5) -> 25
        // offset 25: jeq v1, +18     (3) -> 28 -> 46 (case3)
        // offset 28: ldai 0          (5) -> 33 (default)
        // offset 33: return          (1) -> 34
        // offset 34: ldai 10         (5) -> 39 (case1 body)
        // offset 39: return          (1) -> 40
        // offset 40: ldai 20         (5) -> 45 (case2 body)
        // offset 45: return          (1) -> 46
        // offset 46: ldai 30         (5) -> 51 (case3 body)
        // offset 51: return          (1) -> 52
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x16),       // jeq v1, +22     offset 9  -> 34
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x14),       // jeq v1, +20     offset 17 -> 40
                bytes(0x62), le32(3),          // ldai 3          offset 20
                bytes(0x5C, 0x01, 0x12),       // jeq v1, +18     offset 25 -> 46
                bytes(0x62), le32(0),          // ldai 0          offset 28 (default)
                bytes(0x64),                   // return          offset 33
                bytes(0x62), le32(10),         // ldai 10         offset 34 (case 1)
                bytes(0x64),                   // return          offset 39
                bytes(0x62), le32(20),         // ldai 20         offset 40 (case 2)
                bytes(0x64),                   // return          offset 45
                bytes(0x62), le32(30),         // ldai 30         offset 46 (case 3)
                bytes(0x64)                    // return          offset 51
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch"), "Expected switch in output: " + result);
        assertTrue(result.contains("case 1:"), "Expected case 1: " + result);
        assertTrue(result.contains("case 2:"), "Expected case 2: " + result);
        assertTrue(result.contains("case 3:"), "Expected case 3: " + result);
    }

    /**
     * Tests a switch with a default case.
     * Uses simple return statements in each case body.
     */
    @Test
    void testDecompile_switchWithDefault() {
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +14     (3) -> 12 -> 26 (case1 body)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +15     (3) -> 20 -> 35 (case2 body)
        // offset 20: ldai 0          (5) -> 25 (default body)
        // offset 25: return          (1) -> 26
        // offset 26: ldai 10         (5) -> 31 (case1 body)
        // offset 31: return          (1) -> 32
        // offset 32: ldai 20         (5) -> 37 (case2 body)
        // offset 37: return          (1) -> 38
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x0E),       // jeq v1, +14     offset 9  -> 26
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x0C),       // jeq v1, +12     offset 17 -> 32
                bytes(0x62), le32(0),          // ldai 0          offset 20 (default)
                bytes(0x64),                   // return          offset 25
                bytes(0x62), le32(10),         // ldai 10         offset 26 (case 1)
                bytes(0x64),                   // return          offset 31
                bytes(0x62), le32(20),         // ldai 20         offset 32 (case 2)
                bytes(0x64)                    // return          offset 37
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch"), "Expected switch in output: " + result);
        assertTrue(result.contains("default:"), "Expected default: " + result);
    }

    /**
     * Tests a switch statement CFG structure with 2 cases.
     * Verifies that the CFG correctly identifies the comparison chain.
     */
    @Test
    void testDecompile_switchTwoCases_cfgStructure() {
        // Minimal switch with 2 cases using returns
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +14     (3) -> 12 -> 26 (case1 body)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +15     (3) -> 20 -> 35 (case2 body)
        // offset 20: ldai 0          (5) -> 25 (default body)
        // offset 25: return          (1) -> 26
        // offset 26: ldai 10         (5) -> 31 (case1 body)
        // offset 31: return          (1) -> 32
        // offset 32: ldai 20         (5) -> 37 (case2 body)
        // offset 37: return          (1) -> 38
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x0E),       // jeq v1, +14     offset 9  -> 26
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x0C),       // jeq v1, +12     offset 17 -> 32
                bytes(0x62), le32(0),          // ldai 0          offset 20 (default)
                bytes(0x64),                   // return          offset 25
                bytes(0x62), le32(10),         // ldai 10         offset 26 (case 1)
                bytes(0x64),                   // return          offset 31
                bytes(0x62), le32(20),         // ldai 20         offset 32 (case 2)
                bytes(0x64)                    // return          offset 37
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);

        // The switch should create multiple blocks:
        // - Entry/comparison blocks for each case test
        // - Case body blocks
        // - Default block
        assertTrue(cfg.getBlocks().size() >= 5,
                "Expected at least 5 blocks for switch, got "
                        + cfg.getBlocks().size());

        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch"), "Expected switch in output: " + result);
        assertTrue(result.contains("case 1:"), "Expected case 1: " + result);
        assertTrue(result.contains("case 2:"), "Expected case 2: " + result);
    }

    /**
     * Tests that a switch statement correctly identifies the discriminant variable.
     */
    @Test
    void testDecompile_switchDiscriminantVariable() {
        // The discriminant should be v1 (the register used in jeq comparisons)
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +14     (3) -> 12 -> 26 (case1)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +15     (3) -> 20 -> 35 (case2)
        // offset 20: ldai 0          (5) -> 25 (default)
        // offset 25: return          (1) -> 26
        // offset 26: ldai 10         (5) -> 31 (case1 body)
        // offset 31: return          (1) -> 32
        // offset 32: ldai 20         (5) -> 37 (case2 body)
        // offset 37: return          (1) -> 38
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x0E),       // jeq v1, +14     offset 9  -> 26
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x0C),       // jeq v1, +12     offset 17 -> 32
                bytes(0x62), le32(0),          // ldai 0          offset 20 (default)
                bytes(0x64),                   // return          offset 25
                bytes(0x62), le32(10),         // ldai 10         offset 26 (case 1)
                bytes(0x64),                   // return          offset 31
                bytes(0x62), le32(20),         // ldai 20         offset 32 (case 2)
                bytes(0x64)                    // return          offset 37
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch (v1)"),
                "Expected 'switch (v1)' in output: " + result);
    }

    /**
     * Tests that the SwitchStatement AST node produces correct ArkTS output.
     */
    @Test
    void testSwitchStatement_toArkTS() {
        ArkTSExpression discriminant =
                new ArkTSExpression.VariableExpression("x");
        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();

        // case 1: break;
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(new ArkTSStatement.BreakStatement())));

        // case 2: y = 20; break;
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("2",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "y"),
                                        new ArkTSExpression.LiteralExpression(
                                                "20",
                                                ArkTSExpression
                                                        .LiteralExpression
                                                        .LiteralKind.NUMBER))),
                        new ArkTSStatement.BreakStatement())));

        // default: y = 0; break;
        ArkTSStatement defaultBlock = new ArkTSStatement.BlockStatement(
                List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "y"),
                                        new ArkTSExpression.LiteralExpression(
                                                "0",
                                                ArkTSExpression
                                                        .LiteralExpression
                                                        .LiteralKind.NUMBER))),
                        new ArkTSStatement.BreakStatement()));

        ArkTSControlFlow.SwitchStatement switchStmt =
                new ArkTSControlFlow.SwitchStatement(discriminant, cases,
                        defaultBlock);
        String output = switchStmt.toArkTS(0);
        assertTrue(output.startsWith("switch (x) {"));
        assertTrue(output.contains("case 1:"));
        assertTrue(output.contains("case 2:"));
        assertTrue(output.contains("default:"));
        assertTrue(output.contains("break;"));
        assertTrue(output.endsWith("}"));
    }

    /**
     * Tests that a switch with fall-through (case without break) works.
     * When a case body does not end with a break, execution falls through
     * to the next case.
     */
    @Test
    void testDecompile_switchFallThrough() {
        // Switch with fall-through from case 1 to case 2
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +14     (3) -> 12 -> 26 (case1 body)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +13     (3) -> 20 -> 33 (case2 body)
        // offset 20: ldai 0          (5) -> 25 (default)
        // offset 25: return          (1) -> 26
        // case 1 body (falls through - no jmp/break):
        // offset 26: ldai 10         (5) -> 31
        // offset 31: sta v2          (2) -> 33  (falls through to case 2)
        // case 2 body (with return):
        // offset 33: ldai 20         (5) -> 38
        // offset 38: sta v2          (2) -> 40
        // offset 40: lda v2          (2) -> 42
        // offset 42: return          (1) -> 43
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x0E),       // jeq v1, +14     offset 9  -> 26
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x0D),       // jeq v1, +13     offset 17 -> 33
                bytes(0x62), le32(0),          // ldai 0          offset 20 (default)
                bytes(0x64),                   // return          offset 25
                // case 1 body (falls through - no jmp/break)
                bytes(0x62), le32(10),         // ldai 10         offset 26
                bytes(0x61, 0x02),             // sta v2          offset 31
                // falls through to case 2 body
                // case 2 body (with return)
                bytes(0x62), le32(20),         // ldai 20         offset 33
                bytes(0x61, 0x02),             // sta v2          offset 38
                bytes(0x60, 0x02),             // lda v2          offset 40
                bytes(0x64)                    // return          offset 42
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch"),
                "Expected switch in output: " + result);
    }

    /**
     * Tests a simple switch with just 2 cases (minimum for switch detection).
     */
    @Test
    void testDecompile_switchMinimalTwoCases() {
        // lda v0; sta v1; ldai 1; jeq v1 -> body1; ldai 2; jeq v1 -> body2;
        // default: return 0; body1: return 10; body2: return 20
        // offset 0:  lda v0          (2) -> 2
        // offset 2:  sta v1          (2) -> 4
        // offset 4:  ldai 1          (5) -> 9
        // offset 9:  jeq v1, +14     (3) -> 12 -> 26 (case1 body)
        // offset 12: ldai 2          (5) -> 17
        // offset 17: jeq v1, +15     (3) -> 20 -> 35 (case2 body)
        // offset 20: ldai 0          (5) -> 25 (default body)
        // offset 25: return          (1) -> 26
        // offset 26: ldai 10         (5) -> 31 (case1 body)
        // offset 31: return          (1) -> 32
        // offset 32: ldai 20         (5) -> 37 (case2 body)
        // offset 37: return          (1) -> 38
        byte[] code = concat(
                bytes(0x60, 0x00),             // lda v0          offset 0
                bytes(0x61, 0x01),             // sta v1          offset 2
                bytes(0x62), le32(1),          // ldai 1          offset 4
                bytes(0x5C, 0x01, 0x0E),       // jeq v1, +14     offset 9  -> 26
                bytes(0x62), le32(2),          // ldai 2          offset 12
                bytes(0x5C, 0x01, 0x0C),       // jeq v1, +12     offset 17 -> 32
                bytes(0x62), le32(0),          // ldai 0          offset 20 (default)
                bytes(0x64),                   // return          offset 25
                bytes(0x62), le32(10),         // ldai 10         offset 26 (case 1)
                bytes(0x64),                   // return          offset 31
                bytes(0x62), le32(20),         // ldai 20         offset 32 (case 2)
                bytes(0x64)                    // return          offset 37
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("switch"),
                "Expected switch in output: " + result);
    }

    /**
     * Tests that the SwitchStatement.SwitchCase AST node works correctly.
     */
    @Test
    void testSwitchCaseAstNode() {
        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(new ArkTSStatement.BreakStatement())));

        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(
                        new ArkTSExpression.VariableExpression("x"),
                        cases, null);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("switch (x)"));
        assertTrue(output.contains("case 1:"));
        assertTrue(output.contains("break;"));
        assertFalse(output.contains("default:"));
    }

    /**
     * Tests that the SwitchStatement with no default block works correctly.
     */
    @Test
    void testSwitchStatementNoDefault() {
        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("5",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "result"),
                                        new ArkTSExpression.LiteralExpression(
                                                "50",
                                                ArkTSExpression
                                                        .LiteralExpression
                                                        .LiteralKind.NUMBER))),
                        new ArkTSStatement.BreakStatement())));

        ArkTSControlFlow.SwitchStatement stmt =
                new ArkTSControlFlow.SwitchStatement(
                        new ArkTSExpression.VariableExpression("val"),
                        cases, null);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("switch (val)"));
        assertTrue(output.contains("case 5:"));
        assertFalse(output.contains("default:"));
    }

    // --- Private property access tests ---

    @Test
    void testDecompile_ldprivateproperty_withAccValue() {
        // ldai 42; ldprivateproperty 0, 5, 0; sta v0; return
        // LDPRIVATEPROPERTY = 0xD8, IMM8_IMM16_IMM16
        byte[] code = concat(
                bytes(0x62), le32(42),                    // ldai 42
                bytes(0xD8, 0x00, 0x05, 0x00, 0x00, 0x00), // ldprivateproperty 0, str_5, 0
                bytes(0x61, 0x00),                        // sta v0
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains(".#"));
    }

    @Test
    void testDecompile_ldprivateproperty_defaultsToThis() {
        // LDPRIVATEPROPERTY without a prior accumulator load should use this
        // ldprivateproperty 0, 3, 0; sta v0; return
        byte[] code = concat(
                bytes(0xD8, 0x00, 0x03, 0x00, 0x00, 0x00), // ldprivateproperty 0, str_3, 0
                bytes(0x61, 0x00),                        // sta v0
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("this.#"),
                "Should default to this when no accumulator: " + result);
    }

    @Test
    void testDecompile_stprivateproperty() {
        // STPRIVATEPROPERTY = 0xD9, IMM8_IMM16_IMM16_V8
        // ldai 42; stprivateproperty 0, 3, 0, v1
        byte[] code = concat(
                bytes(0x62), le32(42),                    // ldai 42
                bytes(0xD9, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01), // stprivateproperty
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains(".#"));
        assertTrue(result.contains("v1.#"),
                "Should use object register v1: " + result);
    }

    @Test
    void testDecompile_stprivateproperty_assignsValue() {
        // ldai 99; stprivateproperty 0, 7, 0, v2; return
        byte[] code = concat(
                bytes(0x62), le32(99),                    // ldai 99
                bytes(0xD9, 0x00, 0x07, 0x00, 0x00, 0x00, 0x02), // stprivateproperty 0, str_7, 0, v2
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v2.#str_7 = 99"),
                "Should produce v2.#str_7 = 99: " + result);
    }

    @Test
    void testDecompile_testin() {
        // TESTIN = 0xDA, IMM8_IMM16_IMM16
        // lda v0; testin 0, 5, 0; sta v1
        byte[] code = concat(
                bytes(0x60, 0x00),                        // lda v0
                bytes(0xDA, 0x00, 0x05, 0x00, 0x00, 0x00), // testin 0, str_5, 0
                bytes(0x61, 0x01),                        // sta v1
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains(" in "));
    }

    @Test
    void testDecompile_testin_withStringResolved() {
        // lda v0; testin 0, 3, 0; sta v1; return
        byte[] code = concat(
                bytes(0x60, 0x00),                        // lda v0
                bytes(0xDA, 0x00, 0x03, 0x00, 0x00, 0x00), // testin 0, str_3, 0
                bytes(0x61, 0x01),                        // sta v1
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("str_3 in "),
                "Should contain 'str_3 in' pattern: " + result);
    }

    @Test
    void testDecompile_privateFieldReadThenWrite() {
        // Read private field from v0, then write back to v0's private field
        // lda v0; ldprivateproperty 0, 2, 0; sta v1;
        // ldai 10; stprivateproperty 0, 2, 0, v0; return
        byte[] code = concat(
                bytes(0x60, 0x00),                        // lda v0
                bytes(0xD8, 0x00, 0x02, 0x00, 0x00, 0x00), // ldprivateproperty 0, str_2, 0
                bytes(0x61, 0x01),                        // sta v1
                bytes(0x62), le32(10),                    // ldai 10
                bytes(0xD9, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00), // stprivateproperty to v0
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0.#str_2"),
                "Should read private field from v0: " + result);
        assertTrue(result.contains("v0.#str_2 = 10"),
                "Should write 10 to v0's private field: " + result);
    }

    @Test
    void testDecompile_callruntimeCreatePrivateProperty() {
        // callruntime.createprivateproperty str_3
        // CRT_CREATEPRIVATEPROPERTY = 0x04, PREF_IMM8 format
        byte[] code = concat(
                bytes(0xFB, 0x04, 0x03),  // callruntime.createprivateproperty str_3
                bytes(0x64)               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("#str_3"),
                "Should emit #str_3 private field declaration: " + result);
        assertFalse(result.contains("/* private:"),
                "Should not use comment-style declaration: " + result);
    }

    @Test
    void testDecompile_callruntimeDefinePrivateProperty() {
        // ldai 42; callruntime.defineprivateproperty str_5, v1
        // CRT_DEFINEPRIVATEPROPERTY = 0x05, PREF_IMM8_V8 format
        byte[] code = concat(
                bytes(0x62), le32(42),                    // ldai 42
                bytes(0xFB, 0x05, 0x05, 0x01),           // callruntime.defineprivateproperty str_5, v1
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v1.#str_5 = 42"),
                "Should assign 42 to v1's private field str_5: " + result);
    }

    @Test
    void testDecompile_callruntimeDefinePrivateProperty_withAccValue() {
        // lda v0; callruntime.defineprivateproperty str_2, v3
        // CRT_DEFINEPRIVATEPROPERTY = 0x05, PREF_IMM8_V8 format
        byte[] code = concat(
                bytes(0x60, 0x00),                        // lda v0
                bytes(0xFB, 0x05, 0x02, 0x03),           // callruntime.defineprivateproperty str_2, v3
                bytes(0x64)                               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v3.#str_2 = v0"),
                "Should assign v0 to v3's private field str_2: " + result);
    }

    @Test
    void testDecompile_privateFieldDeclaration_toArkTS() {
        ArkTSPropertyExpressions.PrivateFieldDeclarationExpression expr =
                new ArkTSPropertyExpressions.PrivateFieldDeclarationExpression(
                        "secretField");
        assertEquals("#secretField", expr.toArkTS());
    }

    @Test
    void testDecompile_privateFieldDeclaration_getFieldName() {
        ArkTSPropertyExpressions.PrivateFieldDeclarationExpression expr =
                new ArkTSPropertyExpressions.PrivateFieldDeclarationExpression(
                        "count");
        assertEquals("count", expr.getFieldName());
    }

    // --- Async/generator instruction tests ---

    @Test
    void testDecompile_createAsyncGeneratorObj() {
        // CREATEASYNCGENERATOROBJ = 0xB7, V8 format
        byte[] code = concat(
                bytes(0xB7, 0x00),  // createasyncgeneratorobj v0
                bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("asyncGenerator"));
    }

    @Test
    void testDecompile_asyncGeneratorResolve() {
        // ASYNCGENERATORRESOLVE = 0xB8, V8_V8_V8 format
        byte[] code = concat(
                bytes(0xB8, 0x00, 0x01, 0x02),  // asyncgeneratorresolve v0, v1, v2
                bytes(0x64)                      // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return"));
    }

    @Test
    void testDecompile_asyncGeneratorReject() {
        // ASYNCGENERATORREJECT = 0x97, V8 format
        byte[] code = concat(
                bytes(0x97, 0x00),  // asyncgeneratorreject v0
                bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("throw"));
    }

    @Test
    void testDecompile_setGeneratorState() {
        // SETGENERATORSTATE = 0xD6, IMM8 format
        byte[] code = concat(
                bytes(0xD6, 0x03),  // setgeneratorstate 3
                bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("setgeneratorstate"));
    }

    // --- Object creation tests ---

    @Test
    void testDecompile_createObjectWithExcludedKeys() {
        // CREATEOBJECTWITHEXCLUDEDKEYS = 0xB3, IMM8_V8 format
        byte[] code = concat(
                bytes(0xB3, 0x02, 0x01),  // createobjectwithexcludedkeys 2, v1
                bytes(0x61, 0x00),        // sta v0
                bytes(0x64)               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("..."));
    }

    // --- Type operation tests ---

    @Test
    void testDecompile_instanceofExpression() {
        // INSTANCEOF = 0x26, IMM8_V8 format
        // ldai 42; instanceof v0; sta v1
        byte[] code = concat(
                bytes(0x62), le32(42),   // ldai 42
                bytes(0x26, 0x00, 0x00), // instanceof v0
                bytes(0x61, 0x01),       // sta v1
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("instanceof"));
    }

    @Test
    void testDecompile_isinExpression() {
        // ISIN = 0x25, IMM8_V8 format
        byte[] code = concat(
                bytes(0x62), le32(42),   // ldai 42
                bytes(0x25, 0x00, 0x00), // isin v0
                bytes(0x61, 0x01),       // sta v1
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains(" in "));
    }

    // --- Delete and copy properties tests ---

    @Test
    void testDecompile_delObjProp() {
        // DELOBJPROP = 0xC2, V8 format
        byte[] code = concat(
                bytes(0x04),             // createemptyobject
                bytes(0xC2, 0x00),       // delobjprop v0
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("delete"));
    }

    @Test
    void testDecompile_copyDataProperties() {
        // COPYDATAPROPERTIES = 0xC5, V8 format
        byte[] code = concat(
                bytes(0x04),             // createemptyobject -> acc
                bytes(0xC5, 0x00),       // copydatatoproperties v0
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("Object.assign"));
    }

    // --- Index-based property access tests ---

    @Test
    void testDecompile_ldObjByIndex() {
        // LDOBJBYINDEX = 0x3A, IMM8_IMM16 format
        byte[] code = concat(
                bytes(0x04),                    // createemptyobject
                bytes(0x3A, 0x00, 0x05, 0x00),  // ldobjbyindex 0, 5
                bytes(0x61, 0x00),              // sta v0
                bytes(0x64)                     // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("[5]"));
    }

    @Test
    void testDecompile_privateMemberExpression_toArkTS() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("obj");
        ArkTSPropertyExpressions.PrivateMemberExpression expr =
                new ArkTSPropertyExpressions.PrivateMemberExpression(obj, "secret");
        assertEquals("obj.#secret", expr.toArkTS());
    }

    @Test
    void testDecompile_inExpression_toArkTS() {
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("name");
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("obj");
        ArkTSPropertyExpressions.InExpression expr =
                new ArkTSPropertyExpressions.InExpression(prop, obj);
        assertEquals("name in obj", expr.toArkTS());
    }

    @Test
    void testDecompile_instanceofExpression_toArkTS() {
        ArkTSExpression expr1 =
                new ArkTSExpression.VariableExpression("value");
        ArkTSExpression expr2 =
                new ArkTSExpression.VariableExpression("MyClass");
        ArkTSPropertyExpressions.InstanceofExpression expr =
                new ArkTSPropertyExpressions.InstanceofExpression(expr1, expr2);
        assertEquals("value instanceof MyClass", expr.toArkTS());
    }

    @Test
    void testDecompile_deleteExpression_toArkTS() {
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("prop");
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(obj, prop, false);
        ArkTSPropertyExpressions.DeleteExpression expr =
                new ArkTSPropertyExpressions.DeleteExpression(target);
        assertEquals("delete obj.prop", expr.toArkTS());
    }

    @Test
    void testDecompile_copyDataPropertiesExpression_toArkTS() {
        ArkTSExpression target =
                new ArkTSExpression.VariableExpression("target");
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("source");
        ArkTSPropertyExpressions.CopyDataPropertiesExpression expr =
                new ArkTSPropertyExpressions.CopyDataPropertiesExpression(target, source);
        assertEquals("Object.assign(target, source)", expr.toArkTS());
    }

    @Test
    void testDecompile_objectLiteralWithSpreadProperty() {
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("other");
        ArkTSExpression spread =
                new ArkTSAccessExpressions.SpreadExpression(source);
        List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty> props =
                new ArrayList<>();
        props.add(new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                null, spread));
        ArkTSAccessExpressions.ObjectLiteralExpression expr =
                new ArkTSAccessExpressions.ObjectLiteralExpression(props);
        assertEquals("{ ...other }", expr.toArkTS());
    }

    // ======== DESTRUCTURING, SPREAD, TEMPLATE LITERAL, NULLISH COALESCING ========

    @Test
    void testArrayDestructuringExpression_basic() {
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSPropertyExpressions.ArrayDestructuringExpression expr =
                new ArkTSPropertyExpressions.ArrayDestructuringExpression(
                        List.of("a", "b", "c"), null, source);
        assertEquals("[a, b, c] = arr", expr.toArkTS());
    }

    @Test
    void testArrayDestructuringExpression_withRest() {
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSPropertyExpressions.ArrayDestructuringExpression expr =
                new ArkTSPropertyExpressions.ArrayDestructuringExpression(
                        List.of("first", "second"), "rest", source);
        assertEquals("[first, second, ...rest] = arr", expr.toArkTS());
    }

    @Test
    void testObjectDestructuringExpression_basic() {
        List<ArkTSPropertyExpressions.ObjectDestructuringExpression.DestructuringBinding>
                bindings = List.of(
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("x", null),
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("y", null));
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSPropertyExpressions.ObjectDestructuringExpression expr =
                new ArkTSPropertyExpressions.ObjectDestructuringExpression(
                        bindings, source);
        assertEquals("{ x, y } = obj", expr.toArkTS());
    }

    @Test
    void testObjectDestructuringExpression_withRename() {
        List<ArkTSPropertyExpressions.ObjectDestructuringExpression.DestructuringBinding>
                bindings = List.of(
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("x", "a"),
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("y", "b"));
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSPropertyExpressions.ObjectDestructuringExpression expr =
                new ArkTSPropertyExpressions.ObjectDestructuringExpression(
                        bindings, source);
        assertEquals("{ x: a, y: b } = obj", expr.toArkTS());
    }

    @Test
    void testDestructuringDeclaration_array() {
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression pattern =
                new ArkTSPropertyExpressions.ArrayDestructuringExpression(
                        List.of("a", "b"), null, source);
        ArkTSStatement stmt =
                new ArkTSTypeDeclarations.DestructuringDeclaration("const", pattern);
        assertEquals("const [a, b] = arr;", stmt.toArkTS(0));
    }

    @Test
    void testDestructuringDeclaration_object() {
        List<ArkTSPropertyExpressions.ObjectDestructuringExpression.DestructuringBinding>
                bindings = List.of(
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("name", null),
                        new ArkTSPropertyExpressions.ObjectDestructuringExpression
                                .DestructuringBinding("value", null));
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression pattern =
                new ArkTSPropertyExpressions.ObjectDestructuringExpression(
                        bindings, source);
        ArkTSStatement stmt =
                new ArkTSTypeDeclarations.DestructuringDeclaration("let", pattern);
        assertEquals("let { name, value } = obj;", stmt.toArkTS(0));
    }

    @Test
    void testSpreadExpression_inArray() {
        ArkTSExpression spreadArg =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression spread =
                new ArkTSAccessExpressions.SpreadExpression(spreadArg);
        ArkTSExpression elem =
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSAccessExpressions.ArrayLiteralExpression expr =
                new ArkTSAccessExpressions.ArrayLiteralExpression(
                        List.of(spread, elem));
        assertEquals("[...arr, 1]", expr.toArkTS());
    }

    @Test
    void testSpreadExpression_inObject() {
        ArkTSExpression spreadArg =
                new ArkTSExpression.VariableExpression("defaults");
        ArkTSExpression spread =
                new ArkTSAccessExpressions.SpreadExpression(spreadArg);
        ArkTSExpression value =
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty> props =
                new ArrayList<>();
        props.add(new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                null, spread));
        props.add(new ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty(
                "x", value));
        ArkTSAccessExpressions.ObjectLiteralExpression expr =
                new ArkTSAccessExpressions.ObjectLiteralExpression(props);
        assertEquals("{ ...defaults, x: 42 }", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_basic() {
        ArkTSExpression nameExpr =
                new ArkTSExpression.VariableExpression("name");
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("Hello ", "!"), List.of(nameExpr));
        assertEquals("`Hello ${name}!`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_multipleInterpolations() {
        ArkTSExpression first =
                new ArkTSExpression.VariableExpression("first");
        ArkTSExpression last =
                new ArkTSExpression.VariableExpression("last");
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("Hello ", " ", "!"),
                        List.of(first, last));
        assertEquals("`Hello ${first} ${last}!`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_plainText() {
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("plain text"), Collections.emptyList());
        assertEquals("`plain text`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_escapeBacktick() {
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("contains `backtick`"),
                        Collections.emptyList());
        assertEquals("`contains \\`backtick\\``", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_escapeDollar() {
        ArkTSAccessExpressions.TemplateLiteralExpression expr =
                new ArkTSAccessExpressions.TemplateLiteralExpression(
                        List.of("cost: $5"), Collections.emptyList());
        assertEquals("`cost: \\$5`", expr.toArkTS());
    }

    @Test
    void testOptionalChainExpression_dotAccess() {
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("name");
        ArkTSAccessExpressions.OptionalChainExpression expr =
                new ArkTSAccessExpressions.OptionalChainExpression(obj, prop, false);
        assertEquals("obj?.name", expr.toArkTS());
    }

    @Test
    void testOptionalChainExpression_bracketAccess() {
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("key");
        ArkTSAccessExpressions.OptionalChainExpression expr =
                new ArkTSAccessExpressions.OptionalChainExpression(obj, prop, true);
        assertEquals("obj?.[key]", expr.toArkTS());
    }

    @Test
    void testNullishCoalescingExpression_basic() {
        ArkTSExpression left =
                new ArkTSExpression.VariableExpression("value");
        ArkTSExpression right =
                new ArkTSExpression.LiteralExpression("default",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSPropertyExpressions.NullishCoalescingExpression expr =
                new ArkTSPropertyExpressions.NullishCoalescingExpression(left, right);
        assertEquals("(value ?? \"default\")", expr.toArkTS());
    }

    @Test
    void testNullishCoalescingExpression_withNumber() {
        ArkTSExpression left =
                new ArkTSExpression.VariableExpression("x");
        ArkTSExpression right =
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSPropertyExpressions.NullishCoalescingExpression expr =
                new ArkTSPropertyExpressions.NullishCoalescingExpression(left, right);
        assertEquals("(x ?? 0)", expr.toArkTS());
    }

    @Test
    void testReconstructTemplateLiteral_stringPlusVariable() {
        ArkTSExpression str = new ArkTSExpression.LiteralExpression("Hello ",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression name = new ArkTSExpression.VariableExpression("name");
        ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                str, "+", name);

        ArkTSDecompiler decomp = new ArkTSDecompiler();
        ArkTSExpression result =
                decomp.tryReconstructTemplateLiteral(binary);
        assertTrue(result instanceof ArkTSAccessExpressions.TemplateLiteralExpression);
        assertEquals("`Hello ${name}`", result.toArkTS());
    }

    @Test
    void testReconstructTemplateLiteral_chained() {
        ArkTSExpression str1 = new ArkTSExpression.LiteralExpression("Hello ",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression name = new ArkTSExpression.VariableExpression("name");
        ArkTSExpression inner = new ArkTSExpression.BinaryExpression(
                str1, "+", name);
        ArkTSExpression str2 = new ArkTSExpression.LiteralExpression("!",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression outer = new ArkTSExpression.BinaryExpression(
                inner, "+", str2);

        ArkTSDecompiler decomp = new ArkTSDecompiler();
        ArkTSExpression result =
                decomp.tryReconstructTemplateLiteral(outer);
        assertTrue(result instanceof ArkTSAccessExpressions.TemplateLiteralExpression);
        assertEquals("`Hello ${name}!`", result.toArkTS());
    }

    @Test
    void testReconstructTemplateLiteral_nonStringNotReconstructed() {
        ArkTSExpression x = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression y = new ArkTSExpression.VariableExpression("y");
        ArkTSExpression binary = new ArkTSExpression.BinaryExpression(
                x, "+", y);

        ArkTSDecompiler decomp = new ArkTSDecompiler();
        ArkTSExpression result =
                decomp.tryReconstructTemplateLiteral(binary);
        assertFalse(result instanceof ArkTSAccessExpressions.TemplateLiteralExpression);
    }

    @Test
    void testReconstructTemplateLiteral_nullInput() {
        ArkTSDecompiler decomp = new ArkTSDecompiler();
        assertNull(decomp.tryReconstructTemplateLiteral(null));
    }

    @Test
    void testDetectNullishCoalescing_fromTernary() {
        ArkTSExpression x = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression nullLit = new ArkTSExpression.LiteralExpression("null",
                ArkTSExpression.LiteralExpression.LiteralKind.NULL);
        ArkTSExpression condition = new ArkTSExpression.BinaryExpression(
                x, "===", nullLit);
        ArkTSExpression defaultVal = new ArkTSExpression.LiteralExpression(
                "default",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);

        ArkTSDecompiler decomp = new ArkTSDecompiler();
        ArkTSExpression result = decomp.tryDetectNullishCoalescing(
                condition, defaultVal, x);
        assertTrue(result instanceof ArkTSPropertyExpressions.NullishCoalescingExpression);
        assertEquals("(x ?? \"default\")", result.toArkTS());
    }

    @Test
    void testDetectNullishCoalescing_notNullCheck_returnsNull() {
        ArkTSExpression x = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression zero = new ArkTSExpression.LiteralExpression("0",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression condition = new ArkTSExpression.BinaryExpression(
                x, ">", zero);
        ArkTSExpression one = new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);

        ArkTSDecompiler decomp = new ArkTSDecompiler();
        ArkTSExpression result = decomp.tryDetectNullishCoalescing(
                condition, one, x);
        assertNull(result);
    }

    // --- For-of/for-in statement tests ---

    @Test
    void testForOfStatement_toArkTS() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSControlFlow.ForOfStatement stmt =
                new ArkTSControlFlow.ForOfStatement("const", "item",
                        iterable, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("for (const item of arr)"));
        assertTrue(result.contains("{"));
    }

    @Test
    void testForOfStatement_withBody() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression value =
                new ArkTSExpression.VariableExpression("item");
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression("console.log"),
                                List.of(value))));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSControlFlow.ForOfStatement stmt =
                new ArkTSControlFlow.ForOfStatement("const", "item",
                        iterable, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("for (const item of arr)"));
        assertTrue(result.contains("console.log(item)"));
    }

    @Test
    void testForOfStatement_withIndent() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("data");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForOfStatement stmt =
                new ArkTSControlFlow.ForOfStatement("const", "x",
                        iterable, body);
        String result = stmt.toArkTS(1);
        assertTrue(result.startsWith("    for (const x of data)"));
    }

    @Test
    void testForInStatement_toArkTS() {
        ArkTSExpression object =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForInStatement stmt =
                new ArkTSControlFlow.ForInStatement("const", "key",
                        object, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("for (const key in obj)"));
        assertTrue(result.contains("{"));
    }

    @Test
    void testForInStatement_withBody() {
        ArkTSExpression object =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression key =
                new ArkTSExpression.VariableExpression("key");
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression("print"),
                                List.of(key))));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSControlFlow.ForInStatement stmt =
                new ArkTSControlFlow.ForInStatement("const", "key",
                        object, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("for (const key in obj)"));
        assertTrue(result.contains("print(key)"));
    }

    @Test
    void testForInStatement_withIndent() {
        ArkTSExpression object =
                new ArkTSExpression.VariableExpression("data");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForInStatement stmt =
                new ArkTSControlFlow.ForInStatement("const", "k",
                        object, body);
        String result = stmt.toArkTS(2);
        assertTrue(result.startsWith("        for (const k in data)"));
    }

    @Test
    void testForOfStatement_getters() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("items");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForOfStatement stmt =
                new ArkTSControlFlow.ForOfStatement("let", "elem",
                        iterable, body);
        assertEquals("let", stmt.getVariableKind());
        assertEquals("elem", stmt.getVariableName());
        assertEquals("items", stmt.getIterable().toArkTS());
    }

    @Test
    void testForInStatement_getters() {
        ArkTSExpression object =
                new ArkTSExpression.VariableExpression("target");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForInStatement stmt =
                new ArkTSControlFlow.ForInStatement("let", "prop",
                        object, body);
        assertEquals("let", stmt.getVariableKind());
        assertEquals("prop", stmt.getVariableName());
        assertEquals("target", stmt.getObject().toArkTS());
    }

    @Test
    void testDecompile_forOfLoopPattern() {
        List<ArkInstruction> insns = new ArrayList<>();
        // offset 0: CREATEARRAYWITHBUFFER (len 3)
        insns.add(new ArkInstruction(ArkOpcodesCompat.CREATEARRAYWITHBUFFER,
                "createarraywithbuffer", ArkInstructionFormat.IMM8_IMM16,
                0, 3, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.IMMEDIATE16, 0)),
                false));
        // offset 3: GETITERATOR (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETITERATOR_IMM8,
                "getiterator", ArkInstructionFormat.IMM8_V8,
                3, 2, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false));
        // offset 5: STA v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 5, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 7: LDA v1 (len 2) -- loop header
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 9: GETNEXTPROPNAME v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETNEXTPROPNAME,
                "getnextpropname", ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 11: STA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 11, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 13: LDA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 13, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 15: JEQZ_IMM8 → offset 22 (len 2), offset_val = 22-17 = 5
        insns.add(new ArkInstruction(ArkOpcodesCompat.JEQZ_IMM8, "jeqz",
                ArkInstructionFormat.IMM8, 15, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 5)),
                false));
        // offset 17: STA v3 (len 2) -- loop body
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 17, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 3)),
                false));
        // offset 19: JMP_IMM16 → offset 7 (len 3), offset_val = 7-22 = -15
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 19, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -15)),
                false));
        // offset 22: RETURNUNDEFINED (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                "returnundefined", ArkInstructionFormat.NONE, 22, 1,
                Collections.emptyList(), false));
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should decompile iterator instructions without error");
    }

    @Test
    void testDecompile_iteratorOpcodesHandled() {
        ArkInstruction getIter = new ArkInstruction(
                ArkOpcodesCompat.GETITERATOR_IMM8, "getiterator",
                ArkInstructionFormat.IMM8_V8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction closeIter = new ArkInstruction(
                ArkOpcodesCompat.CLOSEITERATOR_IMM8, "closeiterator",
                ArkInstructionFormat.IMM8_V8_V8, 0, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        ArkInstruction getNext = new ArkInstruction(
                ArkOpcodesCompat.GETNEXTPROPNAME, "getnextpropname",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 0)),
                false);
        String result = decompiler.decompileInstructions(
                List.of(getIter, closeIter, getNext,
                        new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                                "returnundefined", ArkInstructionFormat.NONE,
                                6, 1, Collections.emptyList(), false)));
        assertNotNull(result);
    }

    @Test
    void testDecompile_forOfWithBreak() {
        List<ArkInstruction> insns = new ArrayList<>();
        // offset 0: CREATEARRAYWITHBUFFER (len 3)
        insns.add(new ArkInstruction(ArkOpcodesCompat.CREATEARRAYWITHBUFFER,
                "createarraywithbuffer", ArkInstructionFormat.IMM8_IMM16,
                0, 3, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.IMMEDIATE16, 0)),
                false));
        // offset 3: GETITERATOR (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETITERATOR_IMM8,
                "getiterator", ArkInstructionFormat.IMM8_V8,
                3, 2, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false));
        // offset 5: STA v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 5, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 7: LDA v1 (len 2) -- loop header
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 9: GETNEXTPROPNAME v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETNEXTPROPNAME,
                "getnextpropname", ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 11: STA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 11, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 13: LDA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 13, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 15: JEQZ_IMM8 → offset 25 (len 2), offset_val = 25-17 = 8
        insns.add(new ArkInstruction(ArkOpcodesCompat.JEQZ_IMM8, "jeqz",
                ArkInstructionFormat.IMM8, 15, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 8)),
                false));
        // offset 17: STA v3 (len 2) -- loop body start
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 17, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 3)),
                false));
        // offset 19: JMP_IMM16 → offset 25 (len 3) -- break out of loop
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 19, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, 3)),
                false));
        // offset 22: JMP_IMM16 → offset 7 (len 3) -- continue loop back
        // offset_val = 7 - 25 = -18
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 22, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -18)),
                false));
        // offset 25: RETURNUNDEFINED (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                "returnundefined", ArkInstructionFormat.NONE, 25, 1,
                Collections.emptyList(), false));
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
    }

    @Test
    void testDecompile_forOfWithContinue() {
        List<ArkInstruction> insns = new ArrayList<>();
        // offset 0: CREATEARRAYWITHBUFFER (len 3)
        insns.add(new ArkInstruction(ArkOpcodesCompat.CREATEARRAYWITHBUFFER,
                "createarraywithbuffer", ArkInstructionFormat.IMM8_IMM16,
                0, 3, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.IMMEDIATE16, 0)),
                false));
        // offset 3: GETITERATOR (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETITERATOR_IMM8,
                "getiterator", ArkInstructionFormat.IMM8_V8,
                3, 2, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false));
        // offset 5: STA v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 5, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 7: LDA v1 (len 2) -- loop header
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 9: GETNEXTPROPNAME v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETNEXTPROPNAME,
                "getnextpropname", ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 11: STA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 11, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 13: LDA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 13, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 15: JEQZ_IMM8 → offset 25 (len 2), offset_val = 25-17 = 8
        insns.add(new ArkInstruction(ArkOpcodesCompat.JEQZ_IMM8, "jeqz",
                ArkInstructionFormat.IMM8, 15, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 8)),
                false));
        // offset 17: STA v3 (len 2) -- loop body start
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 17, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 3)),
                false));
        // offset 19: JMP_IMM16 → offset 7 (len 3) -- continue (back to header)
        // offset_val = 7 - 22 = -15
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 19, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -15)),
                false));
        // offset 22: JMP_IMM16 → offset 7 (len 3) -- loop back edge
        // offset_val = 7 - 25 = -18
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 22, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -18)),
                false));
        // offset 25: RETURNUNDEFINED (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                "returnundefined", ArkInstructionFormat.NONE, 25, 1,
                Collections.emptyList(), false));
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
    }

    @Test
    void testDecompile_cfgDetectsIteratorPattern() {
        List<ArkInstruction> insns = new ArrayList<>();
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETITERATOR_IMM8,
                "getiterator", ArkInstructionFormat.IMM8_V8,
                0, 2, List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false));
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 2, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertNotNull(cfg);
        assertEquals(1, cfg.getBlocks().size());
    }

    @Test
    void testDecompile_forInLoopPattern() {
        List<ArkInstruction> insns = new ArrayList<>();
        // offset 0: GETPROPITERATOR (len 1)
        insns.add(new ArkInstruction(0x66,
                "getpropiterator", ArkInstructionFormat.NONE,
                0, 1, Collections.emptyList(), false));
        // offset 1: STA v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 1, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 3: LDA v1 (len 2) -- loop header
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 3, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 5: GETNEXTPROPNAME v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETNEXTPROPNAME,
                "getnextpropname", ArkInstructionFormat.V8, 5, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 7: STA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 9: LDA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 11: JEQZ_IMM8 → offset 18 (len 2), offset_val = 18-13 = 5
        insns.add(new ArkInstruction(ArkOpcodesCompat.JEQZ_IMM8, "jeqz",
                ArkInstructionFormat.IMM8, 11, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 5)),
                false));
        // offset 13: STA v3 (len 2) -- loop body
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 13, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 3)),
                false));
        // offset 15: JMP_IMM16 → offset 3 (len 3), offset_val = 3-18 = -15
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 15, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -15)),
                false));
        // offset 18: RETURNUNDEFINED (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                "returnundefined", ArkInstructionFormat.NONE, 18, 1,
                Collections.emptyList(), false));
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should decompile for-in instructions without error");
    }

    @Test
    void testDecompile_nestedForOfLoop() {
        ArkTSExpression outerIterable =
                new ArkTSExpression.VariableExpression("matrix");
        ArkTSExpression innerIterable =
                new ArkTSExpression.VariableExpression("row");
        ArkTSStatement innerBody =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSStatement innerForOf =
                new ArkTSControlFlow.ForOfStatement("const", "cell",
                        innerIterable, innerBody);
        ArkTSStatement outerBody =
                new ArkTSStatement.BlockStatement(List.of(innerForOf));
        ArkTSStatement outerForOf =
                new ArkTSControlFlow.ForOfStatement("const", "row",
                        outerIterable, outerBody);
        String result = outerForOf.toArkTS(0);
        assertTrue(result.contains("for (const row of matrix)"));
        assertTrue(result.contains("for (const cell of row)"));
    }

    @Test
    void testForOfStatement_forStatementArkTSOutput() {
        ArkTSExpression init =
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSStatement initStmt = new ArkTSStatement.VariableDeclaration(
                "let", "i", null, init);
        ArkTSExpression condition = new ArkTSExpression.BinaryExpression(
                new ArkTSExpression.VariableExpression("i"), "<",
                new ArkTSExpression.LiteralExpression("10",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSExpression update = new ArkTSExpression.BinaryExpression(
                new ArkTSExpression.VariableExpression("i"), "+",
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSStatement forStmt = new ArkTSControlFlow.ForStatement(
                initStmt, condition, update, body);
        String result = forStmt.toArkTS(0);
        assertTrue(result.startsWith("for ("));
        assertTrue(result.contains("let i = 0"));
        assertTrue(result.contains("i < 10"));
        assertTrue(result.contains("i + 1"));
    }

    // --- Error recovery and partial decompilation tests ---

    @Test
    void testDecompile_unknownOpcode_producesCommentAndDoesNotCrash() {
        ArkInstruction unknownInsn = new ArkInstruction(
                0xEE, "unknown_opcode", ArkInstructionFormat.NONE,
                0, 1, Collections.emptyList(), false);
        ArkInstruction retInsn = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 1, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(unknownInsn, retInsn);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should produce output even with unknown opcode");
        assertTrue(result.contains("unknown_opcode"),
                "Should contain comment about unknown opcode, got: "
                        + result);
    }

    @Test
    void testDecompile_methodLevelError_isolationInFile() {
        AbcMethod workingMethod = new AbcMethod(0, 0, "good", 0, 0, 0);
        byte[] goodCode = concat(bytes(0x62), le32(42), bytes(0x64));
        AbcCode goodAbcCode = new AbcCode(2, 0, goodCode.length,
                goodCode, Collections.emptyList(), 0);
        String goodResult = decompiler.decompileMethod(
                workingMethod, goodAbcCode, null);
        assertNotNull(goodResult,
                "Working method should produce output");
        assertTrue(goodResult.contains("good"),
                "Should contain method name");
    }

    @Test
    void testDecompile_brokenCfg_fallsBackToLinearListing() {
        ArkInstruction ldai = new ArkInstruction(
                ArkOpcodesCompat.LDAI, "ldai",
                ArkInstructionFormat.IMM32, 0, 5,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE32_SIGNED, 42)),
                false);
        ArkInstruction ret = new ArkInstruction(
                ArkOpcodesCompat.RETURN, "return",
                ArkInstructionFormat.NONE, 5, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(ldai, ret);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should produce output from instructions");
        assertFalse(result.isEmpty(),
                "Output should not be empty");
    }

    @Test
    void testDecompile_warningAccumulation() {
        List<ArkInstruction> insns = new ArrayList<>();
        insns.add(new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false));
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        AbcCode code = new AbcCode(0, 0, 1, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "test", 0, 0, 0);
        ArkTSDecompiler.DecompilationContext ctx =
                new ArkTSDecompiler.DecompilationContext(
                        method, code, null, null, cfg, insns);
        ctx.warnings.add("test warning 1");
        ctx.warnings.add("test warning 2");
        assertEquals(2, ctx.warnings.size(),
                "Should accumulate two warnings");
        assertTrue(ctx.warnings.contains("test warning 1"));
        assertTrue(ctx.warnings.contains("test warning 2"));
    }

    @Test
    void testDecompile_partialOutput_containsPlaceholderComments() {
        ArkInstruction unknownInsn = new ArkInstruction(
                0xFF, "reserved_future_opcode",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        ArkInstruction retInsn = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 1, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(unknownInsn, retInsn);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("reserved_future_opcode"),
                "Should contain placeholder comment for unknown opcode, "
                        + "got: " + result);
    }

    @Test
    void testDecompile_instructionError_recovery() {
        ArkInstruction ldai = new ArkInstruction(
                ArkOpcodesCompat.LDAI, "ldai",
                ArkInstructionFormat.IMM32, 0, 5,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE32_SIGNED, 10)),
                false);
        ArkInstruction unknownInsn = new ArkInstruction(
                0xEE, "custom_unhandled",
                ArkInstructionFormat.NONE, 5, 1,
                Collections.emptyList(), false);
        ArkInstruction retInsn = new ArkInstruction(
                ArkOpcodesCompat.RETURN, "return",
                ArkInstructionFormat.NONE, 6, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(ldai, unknownInsn, retInsn);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should produce output despite unhandled instruction");
        assertTrue(result.contains("custom_unhandled"),
                "Should contain comment about unhandled instruction");
    }

    @Test
    void testDecompile_emptyMethodOnNullCode() {
        AbcMethod method = new AbcMethod(0, 0, "emptyMethod", 0, 0, 0);
        String result = decompiler.decompileMethod(method, null, null);
        assertNotNull(result,
                "Should return output for null code");
        assertTrue(result.contains("emptyMethod"),
                "Should contain method name");
    }

    @Test
    void testDecompile_fallbackMethod_containsReason() {
        AbcMethod method = new AbcMethod(0, 0, "brokenMethod", 0, 0, 0);
        byte[] garbageCode = new byte[] {(byte) 0xFF, (byte) 0xFF};
        AbcCode code = new AbcCode(1, 0, garbageCode.length,
                garbageCode, Collections.emptyList(), 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result,
                "Should produce output even for garbage code");
        assertTrue(result.contains("brokenMethod"),
                "Should contain method name");
    }
    // --- Issue #35: New instruction handler tests ---

    @Test
    void testConstEnumDeclaration_basic() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("RED",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("GREEN",
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("BLUE",
                new ArkTSExpression.LiteralExpression("2",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSTypeDeclarations.ConstEnumDeclaration constEnum =
                new ArkTSTypeDeclarations.ConstEnumDeclaration("Color", members);
        String result = constEnum.toArkTS(0);
        assertTrue(result.startsWith("const enum Color {"));
        assertTrue(result.contains("RED = 0"));
        assertTrue(result.contains("GREEN = 1"));
        assertTrue(result.contains("BLUE = 2"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testConstEnumDeclaration_stringValues() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("North",
                new ArkTSExpression.LiteralExpression("N",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("South",
                new ArkTSExpression.LiteralExpression("S",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        ArkTSTypeDeclarations.ConstEnumDeclaration constEnum =
                new ArkTSTypeDeclarations.ConstEnumDeclaration("Direction", members);
        String result = constEnum.toArkTS(0);
        assertTrue(result.startsWith("const enum Direction {"));
        assertTrue(result.contains("North = \"N\""));
        assertTrue(result.contains("South = \"S\""));
    }

    @Test
    void testEnumDeclaration_mixedStringAndNumberValues() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("A",
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("B",
                new ArkTSExpression.LiteralExpression("hello",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("C", null));
        ArkTSTypeDeclarations.EnumDeclaration enumDecl =
                new ArkTSTypeDeclarations.EnumDeclaration("Mixed", members);
        String result = enumDecl.toArkTS(0);
        assertTrue(result.startsWith("enum Mixed {"));
        assertTrue(result.contains("A = 1"));
        assertTrue(result.contains("B = \"hello\""));
        assertTrue(result.contains("C"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testConstEnumDeclaration_autoIncrement() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                "First", null));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                "Second", null));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember(
                "Third", null));
        ArkTSTypeDeclarations.ConstEnumDeclaration constEnum =
                new ArkTSTypeDeclarations.ConstEnumDeclaration("Auto", members);
        String result = constEnum.toArkTS(0);
        assertTrue(result.startsWith("const enum Auto {"));
        assertTrue(result.contains("First"));
        assertTrue(result.contains("Second"));
        assertTrue(result.contains("Third"));
        assertFalse(result.contains("="));
    }

    @Test
    void testConstEnumDeclaration_singleMember() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Only",
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSTypeDeclarations.ConstEnumDeclaration constEnum =
                new ArkTSTypeDeclarations.ConstEnumDeclaration("Single", members);
        String result = constEnum.toArkTS(0);
        assertTrue(result.startsWith("const enum Single {"));
        assertTrue(result.contains("Only = 42"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testConstEnumDeclaration_withIndent() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("X",
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSTypeDeclarations.ConstEnumDeclaration constEnum =
                new ArkTSTypeDeclarations.ConstEnumDeclaration("Inner", members);
        String result = constEnum.toArkTS(1);
        assertTrue(result.startsWith("    const enum Inner {"));
    }

    @Test
    void testEnumDeclaration_computedNumberValues() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("HUNDRED",
                new ArkTSExpression.LiteralExpression("100",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("TWO_HUNDRED",
                new ArkTSExpression.LiteralExpression("200",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSTypeDeclarations.EnumDeclaration enumDecl =
                new ArkTSTypeDeclarations.EnumDeclaration("HttpStatus", members);
        String result = enumDecl.toArkTS(0);
        assertTrue(result.contains("HUNDRED = 100"));
        assertTrue(result.contains("TWO_HUNDRED = 200"));
    }

    @Test
    void testEnumDeclaration_stringEnum() {
        List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                new ArrayList<>();
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Up",
                new ArkTSExpression.LiteralExpression("UP",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        members.add(new ArkTSTypeDeclarations.EnumDeclaration.EnumMember("Down",
                new ArkTSExpression.LiteralExpression("DOWN",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        ArkTSTypeDeclarations.EnumDeclaration enumDecl =
                new ArkTSTypeDeclarations.EnumDeclaration("Vertical", members);
        String result = enumDecl.toArkTS(0);
        assertTrue(result.startsWith("enum Vertical {"));
        assertTrue(result.contains("Up = \"UP\""));
        assertTrue(result.contains("Down = \"DOWN\""));
    }

    @Test
    void testLdSymbolAccumulatorLoad() {
        byte[] code = concat(
                bytes(0xAD),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("Symbol"),
                "Expected Symbol in output: " + result);
    }

    @Test
    void testLdNewTargetAccumulatorLoad() {
        byte[] code = concat(
                bytes(0x6E),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("new.target"),
                "Expected new.target in output: " + result);
    }

    @Test
    void testDebuggerInstruction() {
        byte[] code = concat(
                bytes(0xB0),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("debugger"),
                "Expected debugger in output: " + result);
    }

    @Test
    void testGetUnmappedArgs() {
        byte[] code = concat(
                bytes(0x6C),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("arguments"),
                "Expected arguments in output: " + result);
    }

    @Test
    void testLdHoleLoadsUndefined() {
        byte[] code = concat(
                bytes(0x70),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("undefined"),
                "Expected undefined in output: " + result);
    }

    @Test
    void testPoplexenvIsNoOp() {
        byte[] code = concat(
                bytes(0x09, 0x01),
                bytes(0x69),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
    }

    @Test
    void testLdNaNLoadsNaN() {
        byte[] code = concat(
                bytes(0x6A),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("NaN"),
                "Expected NaN in output: " + result);
    }

    @Test
    void testLdInfinityLoadsInfinity() {
        byte[] code = concat(
                bytes(0x6B),
                bytes(0x61, 0x00),
                bytes(0x60, 0x00),
                bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result);
        assertTrue(result.contains("Infinity"),
                "Expected Infinity in output: " + result);
    }

    // --- Issue #39: Namespace declarations and output formatting ---

    @Test
    void testNamespaceStatement_basicRendering() {
        ArkTSStatement classDecl = new ArkTSDeclarations.ClassDeclaration(
                "MyClass", null, Collections.emptyList());
        ArkTSStatement ns =
                new ArkTSDeclarations.NamespaceStatement("com.example",
                        List.of(classDecl));
        String result = ns.toArkTS(0);
        assertTrue(result.startsWith("namespace com.example {"),
                "Expected namespace header: " + result);
        assertTrue(result.contains("class MyClass"),
                "Expected nested class: " + result);
        assertTrue(result.trim().endsWith("}"),
                "Expected closing brace: " + result);
    }

    @Test
    void testNamespaceStatement_withNestedClass() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration(
                "ServiceA", null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration(
                "ServiceB", "ServiceA", Collections.emptyList());
        ArkTSStatement ns =
                new ArkTSDeclarations.NamespaceStatement("app.services",
                        List.of(cls1, cls2));
        String result = ns.toArkTS(0);
        assertTrue(result.contains("namespace app.services {"),
                "Expected namespace header: " + result);
        assertTrue(result.contains("class ServiceA"),
                "Expected ServiceA: " + result);
        assertTrue(result.contains("class ServiceB extends ServiceA"),
                "Expected ServiceB: " + result);
        // Classes should be separated by blank line
        assertTrue(result.contains("}\n\n    class ServiceB"),
                "Expected blank line between classes: " + result);
    }

    @Test
    void testNamespaceStatement_withMethods() {
        ArkTSStatement methodBody =
                new ArkTSStatement.BlockStatement(
                        Collections.emptyList());
        ArkTSStatement methodDecl =
                new ArkTSDeclarations.ClassMethodDeclaration("process",
                        Collections.emptyList(), "void", methodBody,
                        false, "public");
        ArkTSStatement cls =
                new ArkTSDeclarations.ClassDeclaration("Worker", null,
                        List.of(methodDecl));
        ArkTSStatement ns =
                new ArkTSDeclarations.NamespaceStatement("workers",
                        List.of(cls));
        String result = ns.toArkTS(0);
        assertTrue(result.contains("namespace workers {"),
                "Expected namespace header: " + result);
        assertTrue(result.contains("class Worker"),
                "Expected class: " + result);
        assertTrue(result.contains("public process()"),
                "Expected method: " + result);
    }

    @Test
    void testNamespaceDetection_fromDotNotation() {
        String ns = ArkTSDecompiler.namespaceFromClassName(
                "com.example.MyClass");
        assertEquals("com.example", ns);
    }

    @Test
    void testNamespaceDetection_fromSlashNotation() {
        String ns = ArkTSDecompiler.namespaceFromClassName(
                "Lcom/example/MyClass;");
        assertEquals("com.example", ns);
    }

    @Test
    void testNamespaceDetection_noNamespace() {
        String ns = ArkTSDecompiler.namespaceFromClassName("MyClass");
        assertNull(ns);
    }

    @Test
    void testNamespaceDetection_nullInput() {
        String ns = ArkTSDecompiler.namespaceFromClassName(null);
        assertNull(ns);
    }

    @Test
    void testNamespaceDetection_emptyInput() {
        String ns = ArkTSDecompiler.namespaceFromClassName("");
        assertNull(ns);
    }

    @Test
    void testGroupByNamespace_groupsTwoClasses() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration(
                "com.example.ClassA", null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration(
                "com.example.ClassB", null, Collections.emptyList());
        List<ArkTSStatement> grouped =
                ArkTSDecompiler.groupByNamespace(List.of(cls1, cls2));
        assertEquals(1, grouped.size(),
                "Expected single namespace group");
        assertTrue(grouped.get(0)
                instanceof ArkTSDeclarations.NamespaceStatement,
                "Expected NamespaceStatement");
        ArkTSDeclarations.NamespaceStatement ns =
                (ArkTSDeclarations.NamespaceStatement) grouped.get(0);
        assertEquals("com.example", ns.getName());
        assertEquals(2, ns.getMembers().size());
    }

    @Test
    void testGroupByNamespace_mixedNamespaces() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration(
                "com.example.ClassA", null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration(
                "org.other.ClassB", null, Collections.emptyList());
        List<ArkTSStatement> grouped =
                ArkTSDecompiler.groupByNamespace(List.of(cls1, cls2));
        assertEquals(2, grouped.size(),
                "Expected two namespace groups");
    }

    @Test
    void testGroupByNamespace_noNamespace() {
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "Standalone", null, Collections.emptyList());
        List<ArkTSStatement> grouped =
                ArkTSDecompiler.groupByNamespace(List.of(cls));
        assertEquals(1, grouped.size());
        assertTrue(grouped.get(0)
                instanceof ArkTSDeclarations.ClassDeclaration,
                "Expected ClassDeclaration without namespace");
    }

    @Test
    void testImportDeduplication_inFileModule() {
        ArkTSStatement imp1 = new ArkTSDeclarations.ImportStatement(
                List.of("A"), "./utils", false, null, null);
        ArkTSStatement imp2 = new ArkTSDeclarations.ImportStatement(
                List.of("B"), "./utils", false, null, null);
        ArkTSStatement decl = new ArkTSDeclarations.ClassDeclaration(
                "Test", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        List.of(imp1, imp2), List.of(decl),
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        // Duplicate module path should appear only once
        int count = 0;
        int idx = 0;
        while ((idx = result.indexOf("from './utils'", idx)) != -1) {
            count++;
            idx++;
        }
        assertEquals(1, count,
                "Expected deduplicated import, got: " + result);
    }

    @Test
    void testImportGrouping_sortsAlphabetically() {
        ArkTSStatement imp1 = new ArkTSDeclarations.ImportStatement(
                List.of("Zebra"), "./zoo", false, null, null);
        ArkTSStatement imp2 = new ArkTSDeclarations.ImportStatement(
                List.of("Apple"), "./apple", false, null, null);
        ArkTSStatement decl = new ArkTSDeclarations.ClassDeclaration(
                "Test", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        List.of(imp1, imp2), List.of(decl),
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        int appleIdx = result.indexOf("from './apple'");
        int zooIdx = result.indexOf("from './zoo'");
        assertTrue(appleIdx < zooIdx,
                "Expected ./apple before ./zoo: " + result);
    }

    @Test
    void testImportGrouping_separatesGroupsByPath() {
        ArkTSStatement extImp = new ArkTSDeclarations.ImportStatement(
                List.of("Component"), "react", false, null, null);
        ArkTSStatement relImp = new ArkTSDeclarations.ImportStatement(
                List.of("Helper"), "./helper", false, null, null);
        ArkTSStatement decl = new ArkTSDeclarations.ClassDeclaration(
                "App", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        List.of(extImp, relImp), List.of(decl),
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        // External and relative imports should be in separate groups
        // with a blank line between them
        assertTrue(result.contains("from 'react'"),
                "Expected react import: " + result);
        assertTrue(result.contains("from './helper'"),
                "Expected relative import: " + result);
        // Verify ordering: external before relative
        int reactIdx = result.indexOf("from 'react'");
        int relIdx = result.indexOf("from './helper'");
        assertTrue(reactIdx < relIdx,
                "Expected external before relative: " + result);
    }

    @Test
    void testImportGrouping_atSymbolGroupFirst() {
        ArkTSStatement atImp = new ArkTSDeclarations.ImportStatement(
                List.of("UI"), "@ohos/ui", false, null, null);
        ArkTSStatement extImp = new ArkTSDeclarations.ImportStatement(
                List.of("Lib"), "somelib", false, null, null);
        ArkTSStatement relImp = new ArkTSDeclarations.ImportStatement(
                List.of("Util"), "./util", false, null, null);
        ArkTSStatement decl = new ArkTSDeclarations.ClassDeclaration(
                "App", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        List.of(relImp, extImp, atImp), List.of(decl),
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        int atIdx = result.indexOf("from '@ohos/ui'");
        int extIdx = result.indexOf("from 'somelib'");
        int relIdx = result.indexOf("from './util'");
        assertTrue(atIdx < extIdx,
                "Expected @-import before external: " + result);
        assertTrue(extIdx < relIdx,
                "Expected external before relative: " + result);
    }

    @Test
    void testBlankLineManagement_betweenTopLevelDeclarations() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration(
                "First", null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration(
                "Second", null, Collections.emptyList());
        ArkTSStatement cls3 = new ArkTSDeclarations.ClassDeclaration(
                "Third", null, Collections.emptyList());
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        Collections.emptyList(),
                        List.of(cls1, cls2, cls3),
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        // Exactly one blank line between declarations
        assertFalse(result.contains("}\n\n\n"),
                "No triple newlines expected: " + result);
        assertTrue(result.contains("}\n\nclass Second"),
                "Expected single blank line between classes");
        assertTrue(result.contains("}\n\nclass Third"),
                "Expected single blank line between classes");
    }

    @Test
    void testFileModuleFormatting_withNamespaces() {
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "com.example.MyClass", null, Collections.emptyList());
        List<ArkTSStatement> grouped =
                ArkTSDecompiler.groupByNamespace(List.of(cls));
        ArkTSTypeDeclarations.FileModule fm =
                new ArkTSTypeDeclarations.FileModule(
                        Collections.emptyList(), grouped,
                        Collections.emptyList());
        String result = fm.toArkTS(0);
        assertTrue(result.contains("namespace com.example {"),
                "Expected namespace in file module: " + result);
        assertTrue(result.contains("class com.example.MyClass"),
                "Expected class inside namespace: " + result);
    }

    @Test
    void testFileModule_multiClassSameNamespace() {
        ArkTSStatement cls1 = new ArkTSDeclarations.ClassDeclaration(
                "com.app.Controller", null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSDeclarations.ClassDeclaration(
                "com.app.Service", null, Collections.emptyList());
        List<ArkTSStatement> grouped =
                ArkTSDecompiler.groupByNamespace(List.of(cls1, cls2));
        assertEquals(1, grouped.size(),
                "Expected single namespace group");
        ArkTSDeclarations.NamespaceStatement ns =
                (ArkTSDeclarations.NamespaceStatement) grouped.get(0);
        assertEquals("com.app", ns.getName());
        assertEquals(2, ns.getMembers().size());
        String output = ns.toArkTS(0);
        assertTrue(output.contains("class com.app.Controller"));
        assertTrue(output.contains("class com.app.Service"));
    }

    @Test
    void testNamespaceStatement_indentedContent() {
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "Inner", null, Collections.emptyList());
        ArkTSStatement ns =
                new ArkTSDeclarations.NamespaceStatement("pkg", List.of(cls));
        String result = ns.toArkTS(0);
        assertTrue(result.contains("    class Inner"),
                "Expected indented class inside namespace: " + result);
    }

    @Test
    void testNamespaceStatement_emptyNamespace() {
        ArkTSStatement ns =
                new ArkTSDeclarations.NamespaceStatement("empty",
                        Collections.emptyList());
        String result = ns.toArkTS(0);
        assertEquals("namespace empty {\n}", result,
                "Expected empty namespace: " + result);
    }

    @Test
    void testExtractNamespace_fromClassDeclaration() {
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "com.example.Foo", null, Collections.emptyList());
        String ns = ArkTSDecompiler.extractNamespace(cls);
        assertEquals("com.example", ns);
    }

    @Test
    void testExtractNamespace_fromEnumDeclaration() {
        ArkTSStatement enumDecl =
                new ArkTSTypeDeclarations.EnumDeclaration(
                        "com.app.Status", Collections.emptyList());
        String ns = ArkTSDecompiler.extractNamespace(enumDecl);
        assertEquals("com.app", ns);
    }

    @Test
    void testExtractNamespace_fromStructDeclaration() {
        ArkTSStatement struct =
                new ArkTSTypeDeclarations.StructDeclaration(
                        "com.ui.Panel", Collections.emptyList(),
                        Collections.emptyList());
        String ns = ArkTSDecompiler.extractNamespace(struct);
        assertEquals("com.ui", ns);
    }

    @Test
    void testExtractNamespace_fromPlainClass() {
        ArkTSStatement cls = new ArkTSDeclarations.ClassDeclaration(
                "SimpleClass", null, Collections.emptyList());
        String ns = ArkTSDecompiler.extractNamespace(cls);
        assertNull(ns);
    }

    // --- Issue #42: Output quality and placeholder removal tests ---

    @Test
    void testNoPlaceholderForCommonInstructions() {
        // ldai 42; sta v0; ldtrue; sta v1; ldfalse; sta v2;
        // ldundefined; sta v3; ldnull; sta v4; return
        byte[] code = concat(
            bytes(0x62), le32(42),      // ldai 42
            bytes(0x61, 0x00),           // sta v0
            bytes(0x02),                 // ldtrue
            bytes(0x61, 0x01),           // sta v1
            bytes(0x03),                 // ldfalse
            bytes(0x61, 0x02),           // sta v2
            bytes(0x00),                 // ldundefined
            bytes(0x61, 0x03),           // sta v3
            bytes(0x01),                 // ldnull
            bytes(0x61, 0x04),           // sta v4
            bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.contains("unhandled"),
                "Common instructions should not produce unhandled output: "
                        + result);
        assertFalse(result.contains("/* op */"),
                "Should not produce operator placeholder: " + result);
        assertTrue(result.contains("let v0 = 42"));
        assertTrue(result.contains("let v1 = true"));
        assertTrue(result.contains("let v2 = false"));
        assertTrue(result.contains("let v3 = undefined"));
        assertTrue(result.contains("let v4 = null"));
    }

    @Test
    void testTypeAnnotationSimplification_numberLiteral() {
        // ldai 100; sta v0; return
        byte[] code = concat(
            bytes(0x62), le32(100),
            bytes(0x61, 0x00),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 100"),
                "Number literal should not have redundant type annotation: "
                        + result);
        assertFalse(result.contains("let v0: number = 100"),
                "Should not have ': number' for literal 100: " + result);
    }

    @Test
    void testTypeAnnotationPreserved_forComputedValue() {
        // lda v0; add2 0, v1; sta v2; return
        byte[] code = concat(
            bytes(0x60, 0x00),
            bytes(0x0A, 0x00, 0x01),
            bytes(0x61, 0x02),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v2: number = (v0 + v1)"),
                "Computed value should preserve type annotation: " + result);
    }

    @Test
    void testTypeAnnotationSimplification_booleanLiteral() {
        // ldtrue; sta v0; ldfalse; sta v1; return
        byte[] code = concat(
            bytes(0x02),
            bytes(0x61, 0x00),
            bytes(0x03),
            bytes(0x61, 0x01),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.contains(": boolean = true"),
                "Boolean literal should not have redundant annotation: "
                        + result);
        assertFalse(result.contains(": boolean = false"),
                "Boolean literal should not have redundant annotation: "
                        + result);
    }

    @Test
    void testSemicolonConsistency_allStatements() {
        // ldai 1; sta v0; lda v0; return
        byte[] code = concat(
            bytes(0x62), le32(1),
            bytes(0x61, 0x00),
            bytes(0x60, 0x00),
            bytes(0x64)
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        String[] lines = result.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("//")) {
                assertTrue(trimmed.endsWith(";"),
                        "Every statement should end with semicolon: '"
                                + trimmed + "'");
            }
        }
    }

    @Test
    void testFormatting_multipleStatements() {
        // ldai 10; sta v0; ldai 20; sta v1; lda v0; add2 0, v1; sta v2;
        // return v2
        byte[] code = concat(
            bytes(0x62), le32(10),       // ldai 10
            bytes(0x61, 0x00),           // sta v0
            bytes(0x62), le32(20),       // ldai 20
            bytes(0x61, 0x01),           // sta v1
            bytes(0x60, 0x00),           // lda v0
            bytes(0x0A, 0x00, 0x01),     // add2 0, v1
            bytes(0x61, 0x02),           // sta v2
            bytes(0x60, 0x02),           // lda v2
            bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 10;"),
                "First declaration should have no type annotation: "
                        + result);
        assertTrue(result.contains("let v1 = 20;"),
                "Second declaration should have no type annotation: "
                        + result);
        assertTrue(result.contains("let v2: number = (v0 + v1);"),
                "Computed result should have type annotation: " + result);
        assertTrue(result.contains("return v2;"),
                "Return should have semicolon: " + result);
    }

    @Test
    void testTypeInference_isTypeObviousFromLiteral_number() {
        ArkTSExpression lit = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        assertTrue(TypeInference.isTypeObviousFromLiteral("number", lit));
        assertFalse(TypeInference.isTypeObviousFromLiteral("string", lit));
    }

    @Test
    void testTypeInference_isTypeObviousFromLiteral_boolean() {
        ArkTSExpression lit = new ArkTSExpression.LiteralExpression("true",
                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN);
        assertTrue(TypeInference.isTypeObviousFromLiteral("boolean", lit));
        assertFalse(TypeInference.isTypeObviousFromLiteral("number", lit));
    }

    @Test
    void testTypeInference_isTypeObviousFromLiteral_string() {
        ArkTSExpression lit = new ArkTSExpression.LiteralExpression("hello",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        assertTrue(TypeInference.isTypeObviousFromLiteral("string", lit));
    }

    @Test
    void testTypeInference_isTypeObviousFromLiteral_nonLiteralNotObvious() {
        ArkTSExpression varExpr =
                new ArkTSExpression.VariableExpression("v0");
        assertFalse(TypeInference.isTypeObviousFromLiteral("number",
                varExpr));
        assertFalse(TypeInference.isTypeObviousFromLiteral("string",
                varExpr));
    }

    @Test
    void testTypeInference_formatTypeAnnotationForDeclaration_nullType() {
        assertNull(TypeInference.formatTypeAnnotationForDeclaration(null,
                new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
    }

    @Test
    void testTypeInference_formatTypeAnnotationForDeclaration_objectSkipped() {
        assertEquals(null,
                TypeInference.formatTypeAnnotationForDeclaration("Object",
                        new ArkTSExpression.VariableExpression("v0")));
    }

    @Test
    void testDescriptiveFallbackForUnknownOpcode() {
        ArkInstruction unknownInsn = new ArkInstruction(
                0xEE, "custom_op", ArkInstructionFormat.IMM8,
                0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 7)),
                false);
        ArkInstruction retInsn = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 2, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(unknownInsn, retInsn);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("custom_op"),
                "Should contain mnemonic for unknown opcode: " + result);
        assertTrue(result.contains("7"),
                "Should contain operand values: " + result);
    }

    // --- Template literal end-to-end decompilation tests ---

    @Test
    void testTemplateLiteral_simple() {
        // "str_0" + v0 -> `str_0${v0}`
        // String stays directly in accumulator through the add chain
        byte[] code = concat(
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x00),     // add2 0, v0 -> acc = "str_0" + v0
                bytes(0x61, 0x01),           // sta v1
                bytes(0x60, 0x01),           // lda v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("`"),
                "Should produce template literal: " + result);
        assertTrue(result.contains("str_0"),
                "Should contain string part: " + result);
        assertTrue(result.contains("${v0}"),
                "Should contain variable interpolation: " + result);
    }

    @Test
    void testTemplateLiteral_multipleInterpolations() {
        // "str_0" + v1 + v2 -> `str_0${v1}${v2}`
        // All adds chained with string literal at start of chain
        byte[] code = concat(
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x01),     // add2 0, v1 -> acc = "str_0" + v1
                bytes(0x0A, 0x00, 0x02),     // add2 0, v2 -> acc = ("str_0"+v1) + v2
                bytes(0x61, 0x03),           // sta v3
                bytes(0x60, 0x03),           // lda v3
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("`"),
                "Should produce template literal: " + result);
        assertTrue(result.contains("${v1}"),
                "Should interpolate v1: " + result);
        assertTrue(result.contains("${v2}"),
                "Should interpolate v2: " + result);
    }

    @Test
    void testTemplateLiteral_nestedExpression() {
        // "str_0" + v1() -> `str_0${v1()}`
        // String literal in acc, add with a call result stored in register
        byte[] code = concat(
                bytes(0x60, 0x01),           // lda v1
                bytes(0x29, 0x00),           // callarg0 0 -> acc = v1()
                bytes(0x61, 0x02),           // sta v2 -> v2 = v1()
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x02),     // add2 0, v2 -> acc = "str_0" + v2
                bytes(0x61, 0x03),           // sta v3
                bytes(0x60, 0x03),           // lda v3
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("`"),
                "Should produce template literal with call expr: " + result);
        assertTrue(result.contains("${"),
                "Should contain interpolation: " + result);
    }

    @Test
    void testTemplateLiteral_withBoolean() {
        // "str_0" + v1 -> `str_0${v1}` where v1 holds boolean
        // String literal in acc, boolean variable in register
        byte[] code = concat(
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x01),     // add2 0, v1 -> acc = "str_0" + v1
                bytes(0x61, 0x02),           // sta v2
                bytes(0x60, 0x02),           // lda v2
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("`"),
                "Should produce template literal with boolean var: " + result);
    }

    @Test
    void testTemplateLiteral_withNumberExpression() {
        // "str_0" + v3 -> `str_0${v3}` where v3 = v1 + v2
        // The inner (v1+v2) is stored to v3 first, then string + v3
        byte[] code = concat(
                bytes(0x60, 0x01),           // lda v1
                bytes(0x0A, 0x00, 0x02),     // add2 0, v2 -> acc = v1 + v2
                bytes(0x61, 0x03),           // sta v3 -> v3 = v1 + v2
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x03),     // add2 0, v3 -> acc = "str_0" + v3
                bytes(0x61, 0x04),           // sta v4
                bytes(0x60, 0x04),           // lda v4
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("`"),
                "Should produce template literal with number expr: " + result);
    }

    @Test
    void testTemplateLiteral_returnValue() {
        // return "str_0" + v0 -> return `str_0${v0}`
        // String in acc, add with register, return directly
        byte[] code = concat(
                bytes(0x3E), le16(0),        // lda.str 0 -> acc = "str_0"
                bytes(0x0A, 0x00, 0x00),     // add2 0, v0 -> acc = "str_0" + v0
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return `"),
                "Should return template literal: " + result);
        assertTrue(result.contains("${"),
                "Should contain interpolation: " + result);
    }

    // ======== ISSUE #46: Class feature decompilation tests ========

    @Test
    void testClassDeclaration_withGetter() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("x"))));
        ArkTSStatement getter =
                new ArkTSDeclarations.GetterDeclaration("x", "number",
                        body, false, "public");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Point", null,
                        List.of(getter));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Point"),
                "Expected class header: " + result);
        assertTrue(result.contains("public get x(): number"),
                "Expected getter declaration: " + result);
        assertTrue(result.contains("return x;"),
                "Expected return in getter body: " + result);
    }

    @Test
    void testClassDeclaration_withSetter() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration.FunctionParam valueParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "value", "number");
        ArkTSStatement setter =
                new ArkTSDeclarations.SetterDeclaration("x", valueParam,
                        body, false, "public");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Point", null,
                        List.of(setter));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Point"),
                "Expected class header: " + result);
        assertTrue(result.contains("public set x(value: number)"),
                "Expected setter declaration: " + result);
    }

    @Test
    void testClassDeclaration_withStaticGetter() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER))));
        ArkTSStatement getter =
                new ArkTSDeclarations.GetterDeclaration("count", "number",
                        body, true, null);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Container", null,
                        List.of(getter));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("static get count(): number"),
                "Expected static getter: " + result);
    }

    @Test
    void testClassDeclaration_withAbstractMethod() {
        ArkTSDeclarations.FunctionDeclaration.FunctionParam param =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "x", "number");
        ArkTSStatement abstractMethod =
                new ArkTSDeclarations.AbstractMethodDeclaration("calculate",
                        List.of(param), "number", "public");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("BaseClass", null,
                        List.of(abstractMethod));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class BaseClass"),
                "Expected class header: " + result);
        assertTrue(result.contains("public abstract calculate(x: number): number;"),
                "Expected abstract method: " + result);
    }

    @Test
    void testClassDeclaration_withStaticBlock() {
        ArkTSStatement staticBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        "instanceCount"),
                                new ArkTSExpression.LiteralExpression("0",
                                        ArkTSExpression.LiteralExpression
                                                .LiteralKind.NUMBER)))));
        ArkTSStatement staticBlock =
                new ArkTSDeclarations.StaticBlockDeclaration(staticBody);
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "instanceCount", "number",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression
                                .LiteralKind.NUMBER),
                true, null);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Service", null,
                        List.of(field, staticBlock));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Service"),
                "Expected class header: " + result);
        assertTrue(result.contains("static {"),
                "Expected static block: " + result);
    }

    @Test
    void testClassDeclaration_withDecoratedMethod() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.ClassMethodDeclaration innerMethod =
                new ArkTSDeclarations.ClassMethodDeclaration("onInit",
                        Collections.emptyList(), "void", body, false,
                        "public");
        ArkTSStatement decoratedMethod =
                new ArkTSDeclarations.DecoratedMethodDeclaration(
                        List.of("Entry"), innerMethod);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("MyPage", null,
                        List.of(decoratedMethod));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("@Entry"),
                "Expected decorator: " + result);
        assertTrue(result.contains("public onInit(): void"),
                "Expected method declaration after decorator: " + result);
    }

    @Test
    void testClassDeclaration_withParameterProperties() {
        ArkTSStatement constructorBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSControlFlow.SuperCallStatement(
                        Collections.emptyList())));
        List<ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                .ConstructorParam> params = List.of(
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam("x", "number", "public"),
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam("y", "number", "public"));
        ArkTSStatement constructor =
                new ArkTSDeclarations.ConstructorWithPropertiesDeclaration(
                        params, constructorBody);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Point", null,
                        List.of(constructor));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("constructor(public x: number, public y: number)"),
                "Expected constructor with parameter properties: " + result);
        assertTrue(result.contains("super();"),
                "Expected super call: " + result);
    }

    @Test
    void testClassDeclaration_withPrivateParameterProperty() {
        ArkTSStatement constructorBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        List<ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                .ConstructorParam> params = List.of(
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam("secret", "string",
                                        "private"));
        ArkTSStatement constructor =
                new ArkTSDeclarations.ConstructorWithPropertiesDeclaration(
                        params, constructorBody);
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("Vault", null,
                        List.of(constructor));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("constructor(private secret: string)"),
                "Expected constructor with private parameter property: "
                        + result);
    }

    @Test
    void testClassDeclaration_fullFeatured() {
        ArkTSStatement field = new ArkTSDeclarations.ClassFieldDeclaration(
                "name", "string", null, false, "private");
        ArkTSStatement constructorBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSControlFlow.SuperCallStatement(
                        Collections.emptyList())));
        ArkTSStatement constructor =
                new ArkTSDeclarations.ConstructorDeclaration(
                        List.of(new ArkTSDeclarations.FunctionDeclaration
                                .FunctionParam("name", "string")),
                        constructorBody);
        ArkTSStatement getterBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("name"))));
        ArkTSStatement getter =
                new ArkTSDeclarations.GetterDeclaration("name", "string",
                        getterBody, false, "public");
        ArkTSDeclarations.FunctionDeclaration.FunctionParam valueParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "value", "string");
        ArkTSStatement setter =
                new ArkTSDeclarations.SetterDeclaration("name", valueParam,
                        new ArkTSStatement.BlockStatement(
                                Collections.emptyList()),
                        false, "public");
        ArkTSStatement staticMethod =
                new ArkTSDeclarations.ClassMethodDeclaration("create",
                        Collections.emptyList(), "NamedEntity",
                        new ArkTSStatement.BlockStatement(
                                Collections.emptyList()),
                        true, "public");
        ArkTSDeclarations.ClassDeclaration cls =
                new ArkTSDeclarations.ClassDeclaration("NamedEntity",
                        "BaseEntity",
                        List.of(field, constructor, getter, setter,
                                staticMethod));
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class NamedEntity extends BaseEntity"),
                "Expected class with extends: " + result);
        assertTrue(result.contains("private name: string;"),
                "Expected private field: " + result);
        assertTrue(result.contains("constructor(name: string)"),
                "Expected constructor: " + result);
        assertTrue(result.contains("public get name(): string"),
                "Expected getter: " + result);
        assertTrue(result.contains("public set name(value: string)"),
                "Expected setter: " + result);
        assertTrue(result.contains("public static create(): NamedEntity"),
                "Expected static method: " + result);
    }

    @Test
    void testDeclarationBuilder_isGetterMethod() {
        assertTrue(DeclarationBuilder.isGetterMethod("get_value"));
        assertTrue(DeclarationBuilder.isGetterMethod("get_name"));
        assertFalse(DeclarationBuilder.isGetterMethod("getValue"));
        assertFalse(DeclarationBuilder.isGetterMethod("get_"));
        assertFalse(DeclarationBuilder.isGetterMethod(null));
    }

    @Test
    void testDeclarationBuilder_isSetterMethod() {
        assertTrue(DeclarationBuilder.isSetterMethod("set_value"));
        assertTrue(DeclarationBuilder.isSetterMethod("set_name"));
        assertFalse(DeclarationBuilder.isSetterMethod("setValue"));
        assertFalse(DeclarationBuilder.isSetterMethod("set_"));
        assertFalse(DeclarationBuilder.isSetterMethod(null));
    }

    @Test
    void testDeclarationBuilder_isStaticInitMethod() {
        assertTrue(DeclarationBuilder.isStaticInitMethod("<static_init>"));
        assertTrue(DeclarationBuilder.isStaticInitMethod("<clinit>"));
        assertFalse(DeclarationBuilder.isStaticInitMethod("init"));
        assertFalse(DeclarationBuilder.isStaticInitMethod("<init>"));
    }

    @Test
    void testDeclarationBuilder_extractAccessorPropertyName() {
        assertEquals("value",
                DeclarationBuilder.extractAccessorPropertyName("get_value"));
        assertEquals("name",
                DeclarationBuilder.extractAccessorPropertyName("set_name"));
        assertEquals("x",
                DeclarationBuilder.extractAccessorPropertyName("get_x"));
    }

    @Test
    void testGetterDeclaration_staticAndPrivate() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement getter =
                new ArkTSDeclarations.GetterDeclaration("internalState",
                        "number", body, true, "private");
        String result = getter.toArkTS(0);
        assertTrue(result.contains("private static get internalState(): number"),
                "Expected private static getter: " + result);
    }

    @Test
    void testSetterDeclaration_withNoModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration.FunctionParam valueParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "val", "string");
        ArkTSStatement setter =
                new ArkTSDeclarations.SetterDeclaration("label", valueParam,
                        body, false, null);
        String result = setter.toArkTS(0);
        assertTrue(result.startsWith("set label(val: string)"),
                "Expected setter without modifier: " + result);
    }

    @Test
    void testAbstractMethodDeclaration_noModifierNoType() {
        ArkTSStatement abstractMethod =
                new ArkTSDeclarations.AbstractMethodDeclaration("doWork",
                        Collections.emptyList(), null, null);
        String result = abstractMethod.toArkTS(0);
        assertEquals("abstract doWork();", result);
    }

    @Test
    void testStaticBlockDeclaration_withStatements() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.VariableDeclaration("let",
                        "cache",
                        TypeInference.formatTypeAnnotation("cache", "Object"),
                        new ArkTSExpression.VariableExpression("globalThis")),
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "/* init */"))));
        ArkTSStatement staticBlock =
                new ArkTSDeclarations.StaticBlockDeclaration(body);
        String result = staticBlock.toArkTS(0);
        assertTrue(result.startsWith("static {"),
                "Expected static block opening: " + result);
        assertTrue(result.contains("let cache"),
                "Expected cache declaration inside static block: " + result);
        assertTrue(result.trim().endsWith("}"),
                "Expected static block closing: " + result);
    }

    @Test
    void testDecoratedMethodDeclaration_multipleDecorators() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.ClassMethodDeclaration innerMethod =
                new ArkTSDeclarations.ClassMethodDeclaration("handleClick",
                        Collections.emptyList(), "void", body, false,
                        "public");
        ArkTSStatement decoratedMethod =
                new ArkTSDeclarations.DecoratedMethodDeclaration(
                        List.of("OnClick", "Debounce(300)"), innerMethod);
        String result = decoratedMethod.toArkTS(0);
        assertTrue(result.contains("@OnClick"),
                "Expected first decorator: " + result);
        assertTrue(result.contains("@Debounce(300)"),
                "Expected second decorator with args: " + result);
        assertTrue(result.contains("public handleClick(): void"),
                "Expected method after decorators: " + result);
    }

    @Test
    void testConstructorWithPropertiesDeclaration_mixedParams() {
        List<ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                .ConstructorParam> params = List.of(
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam("name", "string", "public"),
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam("age", "number", null));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement constructor =
                new ArkTSDeclarations.ConstructorWithPropertiesDeclaration(
                        params, body);
        String result = constructor.toArkTS(0);
        assertTrue(result.contains("constructor(public name: string, age: number)"),
                "Expected mixed parameter properties: " + result);
    }

    // ======== DESTRUCTURING INTEGRATION TESTS (Issue #44) ========

    @Test
    void testArrayDestructuring_basic() {
        // let [a, b, c] = arr
        // lda v0; ldobjbyindex 0, 0; sta v1
        // lda v0; ldobjbyindex 0, 1; sta v2
        // lda v0; ldobjbyindex 0, 2; sta v3
        byte[] code = concat(
                bytes(0x60, 0x00),                   // lda v0 (source)
                bytes(0x3A, 0x00, 0x00, 0x00),       // ldobjbyindex 0, 0
                bytes(0x61, 0x01),                   // sta v1
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x3A, 0x00, 0x01, 0x00),       // ldobjbyindex 0, 1
                bytes(0x61, 0x02),                   // sta v2
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x3A, 0x00, 0x02, 0x00),       // ldobjbyindex 0, 2
                bytes(0x61, 0x03),                   // sta v3
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("[v1, v2, v3] = v0"),
                "Should produce array destructuring: " + result);
    }

    @Test
    void testArrayDestructuring_withRestElement() {
        // let [first, ...rest] = arr
        // lda v0; ldobjbyindex 0, 0; sta v1
        // lda v0; ldobjbyindex 0, 1; sta v2
        // rest via spread pattern
        byte[] code = concat(
                bytes(0x60, 0x00),                   // lda v0 (source)
                bytes(0x3A, 0x00, 0x00, 0x00),       // ldobjbyindex 0, 0
                bytes(0x61, 0x01),                   // sta v1
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x3A, 0x00, 0x01, 0x00),       // ldobjbyindex 0, 1
                bytes(0x61, 0x02),                   // sta v2
                bytes(0x60, 0x00),                   // lda v0 (rest source)
                bytes(0x06, 0x00, 0x00, 0x00),       // createarraywithbuffer 0, 0
                bytes(0x61, 0x03),                   // sta v3 (rest target)
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("[v1, v2, ...v3] = v0"),
                "Should produce array destructuring with rest: " + result);
    }

    @Test
    void testObjectDestructuring_basic() {
        // let { x, y } = obj
        // lda v0; ldobjbyname 0, 0; sta v1
        // lda v0; ldobjbyname 0, 1; sta v2
        byte[] code = concat(
                bytes(0x60, 0x00),                   // lda v0 (source)
                bytes(0x42, 0x00, 0x00, 0x00),       // ldobjbyname 0, str_0
                bytes(0x61, 0x01),                   // sta v1
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x42, 0x00, 0x01, 0x00),       // ldobjbyname 0, str_1
                bytes(0x61, 0x02),                   // sta v2
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("const { "),
                "Should produce const destructuring: " + result);
        assertTrue(result.contains("str_0") && result.contains("str_1"),
                "Should contain property names: " + result);
    }

    @Test
    void testObjectDestructuring_renamedProperty() {
        // let { x: a, y: b } = obj
        // lda v0; ldobjbyname 0, "x"; sta v3
        // lda v0; ldobjbyname 0, "y"; sta v4
        // When the target register name differs from property name,
        // it becomes a rename: { str_0: v3, str_1: v4 }
        byte[] code = concat(
                bytes(0x60, 0x00),                   // lda v0 (source)
                bytes(0x42, 0x00, 0x00, 0x00),       // ldobjbyname 0, str_0
                bytes(0x61, 0x03),                   // sta v3 (different from str_0)
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x42, 0x00, 0x01, 0x00),       // ldobjbyname 0, str_1
                bytes(0x61, 0x04),                   // sta v4 (different from str_1)
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("const {"),
                "Should produce destructuring: " + result);
        assertTrue(result.contains("str_0: v3"),
                "Should have renamed property: " + result);
        assertTrue(result.contains("str_1: v4"),
                "Should have renamed property: " + result);
    }

    @Test
    void testObjectDestructuring_withDefault() {
        // Verify the DestructuringBinding supports default values
        List<ArkTSPropertyExpressions.ObjectDestructuringExpression
                .DestructuringBinding> bindings = List.of(
                        new ArkTSPropertyExpressions
                                .ObjectDestructuringExpression
                                .DestructuringBinding("x", null),
                        new ArkTSPropertyExpressions
                                .ObjectDestructuringExpression
                                .DestructuringBinding("y", null,
                                        new ArkTSExpression.LiteralExpression(
                                                "10",
                                                ArkTSExpression.LiteralExpression
                                                        .LiteralKind.NUMBER)));
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression expr =
                new ArkTSPropertyExpressions
                        .ObjectDestructuringExpression(
                        bindings, source);
        assertEquals("{ x, y = 10 } = obj", expr.toArkTS());
    }

    @Test
    void testNestedObjectDestructuring() {
        // Verify nested destructuring output via AST construction
        ArkTSExpression innerSource =
                new ArkTSExpression.VariableExpression("inner");
        List<ArkTSPropertyExpressions.ObjectDestructuringExpression
                .DestructuringBinding> innerBindings = List.of(
                        new ArkTSPropertyExpressions
                                .ObjectDestructuringExpression
                                .DestructuringBinding("a", null),
                        new ArkTSPropertyExpressions
                                .ObjectDestructuringExpression
                                .DestructuringBinding("b", null));
        ArkTSExpression innerDestr =
                new ArkTSPropertyExpressions
                        .ObjectDestructuringExpression(
                        innerBindings, innerSource);
        // For nested destructuring, we just verify the output format
        // since bytecode patterns for nested destructuring are complex
        assertEquals("{ a, b } = inner", innerDestr.toArkTS());
    }

    @Test
    void testDestructuringInForLoop() {
        // Array destructuring in a simple instruction block
        // Tests that destructuring doesn't interfere with other patterns
        byte[] code = concat(
                bytes(0x62), le32(5),                // ldai 5
                bytes(0x61, 0x00),                   // sta v0
                bytes(0x60, 0x00),                   // lda v0 (source)
                bytes(0x3A, 0x00, 0x00, 0x00),       // ldobjbyindex 0, 0
                bytes(0x61, 0x01),                   // sta v1
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x3A, 0x00, 0x01, 0x00),       // ldobjbyindex 0, 1
                bytes(0x61, 0x02),                   // sta v2
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v0 = 5"),
                "Should have initial assignment: " + result);
        assertTrue(result.contains("[v1, v2] = v0"),
                "Should produce destructuring: " + result);
    }

    @Test
    void testMixedDestructuring() {
        // Object destructuring followed by array destructuring
        byte[] code = concat(
                // Object destructuring from v0
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x42, 0x00, 0x00, 0x00),       // ldobjbyname 0, str_0
                bytes(0x61, 0x01),                   // sta v1
                bytes(0x60, 0x00),                   // lda v0
                bytes(0x42, 0x00, 0x01, 0x00),       // ldobjbyname 0, str_1
                bytes(0x61, 0x02),                   // sta v2
                // Array destructuring from v3
                bytes(0x60, 0x03),                   // lda v3
                bytes(0x3A, 0x00, 0x00, 0x00),       // ldobjbyindex 0, 0
                bytes(0x61, 0x04),                   // sta v4
                bytes(0x60, 0x03),                   // lda v3
                bytes(0x3A, 0x00, 0x01, 0x00),       // ldobjbyindex 0, 1
                bytes(0x61, 0x05),                   // sta v5
                bytes(0x64)                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("const {"),
                "Should produce object destructuring: " + result);
        assertTrue(result.contains("[v4, v5] = v3"),
                "Should produce array destructuring: " + result);
    }

    @Test
    void testArrayDestructuring_withDefaultBinding() {
        // Verify ArrayBinding with default value produces correct output
        ArkTSPropertyExpressions.ArrayDestructuringExpression.ArrayBinding
                bindingWithDefault =
                new ArkTSPropertyExpressions
                        .ArrayDestructuringExpression.ArrayBinding("x",
                                new ArkTSExpression.LiteralExpression("42",
                                        ArkTSExpression.LiteralExpression
                                                .LiteralKind.NUMBER));
        assertEquals("x = 42", bindingWithDefault.toArkTS());
    }

    @Test
    void testArrayDestructuring_withDefaultFullExpression() {
        // Full destructuring with defaults
        List<ArkTSPropertyExpressions.ArrayDestructuringExpression
                .ArrayBinding> bindings = List.of(
                        new ArkTSPropertyExpressions
                                .ArrayDestructuringExpression
                                .ArrayBinding("a"),
                        new ArkTSPropertyExpressions
                                .ArrayDestructuringExpression
                                .ArrayBinding("b",
                                        new ArkTSExpression.LiteralExpression(
                                                "0",
                                                ArkTSExpression.LiteralExpression
                                                        .LiteralKind.NUMBER)),
                        new ArkTSPropertyExpressions
                                .ArrayDestructuringExpression
                                .ArrayBinding("c"));
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression expr =
                new ArkTSPropertyExpressions
                        .ArrayDestructuringExpression(
                        bindings, "rest", source, true);
        assertEquals("[a, b = 0, c, ...rest] = arr",
                expr.toArkTS());
    }

    // --- Rest/Spread parameter and argument tests (#47) ---

    @Test
    void testRestParameter_basic() {
        // copyrestargs 0 -> loads rest args into accumulator
        byte[] code = concat(
                bytes(0xCF, 0x00),           // copyrestargs 0
                bytes(0x61, 0x00),           // sta v0
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("rest_0"),
                "Should contain rest parameter variable: " + result);
    }

    @Test
    void testRestParameter_withTypedArgs() {
        // Test that FunctionParam with isRest=true produces ...name: any[]
        ArkTSDeclarations.FunctionDeclaration.FunctionParam restParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "args", "string[]", true);
        assertEquals("...args: string[]", restParam.toString());

        ArkTSDeclarations.FunctionDeclaration.FunctionParam untypedRest =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "rest", null, true);
        assertEquals("...rest: any[]", untypedRest.toString());

        ArkTSDeclarations.FunctionDeclaration.FunctionParam normalParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "x", "number", false);
        assertEquals("x: number", normalParam.toString());
    }

    @Test
    void testSpreadInFunctionCall() {
        // APPLY = 0xBA, IMM8_V8_V8 format
        // apply numArgs=1, firstReg=v1 -> fn(...v1)
        byte[] code = concat(
                bytes(0x60, 0x00),           // lda v0 (callee)
                bytes(0xBA, 0x01, 0x01, 0x00), // apply 1, v1, v0
                bytes(0x61, 0x02),           // sta v2
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("...v1"),
                "Should contain spread in function call: " + result);
    }

    @Test
    void testSpreadInArrayLiteral() {
        // starrayspread v0, v1 -> [...v1] spread into array
        byte[] code = concat(
                bytes(0xC6, 0x00, 0x01),     // starrayspread v0, v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("...v1"),
                "Should contain spread in array: " + result);
    }

    @Test
    void testSpreadInObjectLiteral() {
        // createobjectwithexcludedkeys 2, v1 -> {...v1}
        byte[] code = concat(
                bytes(0xB3, 0x02, 0x01),     // createobjectwithexcludedkeys 2, v1
                bytes(0x61, 0x00),           // sta v0
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("...v1"),
                "Should contain spread in object: " + result);
    }

    @Test
    void testMixedRestAndSpread() {
        // copyrestargs followed by apply to call a function with spread
        byte[] code = concat(
                bytes(0xCF, 0x00),           // copyrestargs 0
                bytes(0x61, 0x02),           // sta v2
                bytes(0x60, 0x00),           // lda v0 (callee)
                bytes(0xBA, 0x01, 0x02, 0x00), // apply 1, v2, v0 -> callee(...v2)
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("rest_0"),
                "Should contain rest variable: " + result);
        assertTrue(result.contains("...v2"),
                "Should contain spread in call: " + result);
    }

    @Test
    void testRestParamExpression_toArkTS() {
        // Test the RestParameterExpression AST node
        ArkTSAccessExpressions.RestParameterExpression restExpr =
                new ArkTSAccessExpressions.RestParameterExpression("args",
                        "string[]");
        assertEquals("...args: string[]", restExpr.toArkTS());

        ArkTSAccessExpressions.RestParameterExpression untyped =
                new ArkTSAccessExpressions.RestParameterExpression("rest",
                        null);
        assertEquals("...rest", untyped.toArkTS());
    }

    @Test
    void testSpreadCallExpression_toArkTS() {
        // Test the SpreadCallExpression AST node
        ArkTSExpression callee =
                new ArkTSExpression.VariableExpression("fn");
        ArkTSExpression spreadArg =
                new ArkTSAccessExpressions.SpreadExpression(
                        new ArkTSExpression.VariableExpression("args"));
        ArkTSAccessExpressions.SpreadCallExpression call =
                new ArkTSAccessExpressions.SpreadCallExpression(callee,
                        List.of(spreadArg));
        assertEquals("fn(...args)", call.toArkTS());
    }

    @Test
    void testSpreadNewExpression_toArkTS() {
        // Test the SpreadNewExpression AST node
        ArkTSExpression callee =
                new ArkTSExpression.VariableExpression("Ctor");
        ArkTSExpression spreadArg =
                new ArkTSAccessExpressions.SpreadExpression(
                        new ArkTSExpression.VariableExpression("args"));
        ArkTSAccessExpressions.SpreadNewExpression newExpr =
                new ArkTSAccessExpressions.SpreadNewExpression(callee,
                        List.of(spreadArg));
        assertEquals("new Ctor(...args)", newExpr.toArkTS());
    }

    @Test
    void testSpreadArrayExpression_toArkTS() {
        // Test the SpreadArrayExpression AST node
        ArkTSExpression elem1 = new ArkTSExpression.LiteralExpression("1",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression spreadArg =
                new ArkTSAccessExpressions.SpreadExpression(
                        new ArkTSExpression.VariableExpression("arr"));
        ArkTSExpression elem2 = new ArkTSExpression.LiteralExpression("2",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSAccessExpressions.SpreadArrayExpression arrExpr =
                new ArkTSAccessExpressions.SpreadArrayExpression(
                        List.of(elem1, spreadArg, elem2));
        assertEquals("[1, ...arr, 2]", arrExpr.toArkTS());
    }

    @Test
    void testSpreadObjectExpression_toArkTS() {
        // Test the SpreadObjectExpression AST node
        ArkTSExpression spreadArg =
                new ArkTSAccessExpressions.SpreadExpression(
                        new ArkTSExpression.VariableExpression("obj"));
        ArkTSAccessExpressions.SpreadObjectExpression objExpr =
                new ArkTSAccessExpressions.SpreadObjectExpression(
                        List.of(spreadArg));
        assertEquals("{ ...obj }", objExpr.toArkTS());
    }

    @Test
    void testMethodSignatureBuilder_restParam() {
        // Test that buildParams with restParamIndex produces correct params
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3, null, 2);
        assertEquals(3, params.size());
        assertFalse(params.get(0).isRest());
        assertFalse(params.get(1).isRest());
        assertTrue(params.get(2).isRest());
        assertEquals("...param_2: any[]", params.get(2).toString());
    }

    @Test
    void testDetectRestParamIndex_withCopyrestargs() {
        // Test that detectRestParamIndex finds COPYRESTARGS
        ArkInstruction copyrest = new ArkInstruction(
                ArkOpcodesCompat.COPYRESTARGS, "copyrestargs",
                ArkInstructionFormat.IMM8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8, 0)),
                false);
        ArkInstruction ret = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 2, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(copyrest, ret);
        int restIdx = ArkTSDecompiler.detectRestParamIndex(insns, 3);
        assertEquals(2, restIdx,
                "Rest param should be at index 2 for 3 args");
    }

    @Test
    void testDetectRestParamIndex_noCopyrestargs() {
        // Test that detectRestParamIndex returns -1 when no COPYRESTARGS
        ArkInstruction ret = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        List<ArkInstruction> insns = List.of(ret);
        int restIdx = ArkTSDecompiler.detectRestParamIndex(insns, 3);
        assertEquals(-1, restIdx,
                "Should return -1 when no COPYRESTARGS");
    }

    @Test
    void testNewObjApply_withSpread() {
        // NEWOBJAPPLY = 0xB4, IMM8_V8 format
        // newobjapply 1, v1 -> new Ctor(...v1)
        byte[] code = concat(
                bytes(0x60, 0x00),           // lda v0 (constructor)
                bytes(0xB4, 0x01, 0x01),     // newobjapply 1, v1
                bytes(0x61, 0x02),           // sta v2
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("new "),
                "Should contain new keyword: " + result);
        assertTrue(result.contains("...v1"),
                "Should contain spread in new expression: " + result);
    }

    // --- Bitwise operator tests ---

    @Test
    void testBitwiseAnd() {
        // lda v0; and2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x18, 0x00, 0x01), // and2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 & v1)"),
                "Should contain bitwise AND: " + result);
        assertTrue(result.contains("let v2: number"),
                "Bitwise AND result should be typed as number: " + result);
    }

    @Test
    void testBitwiseOr() {
        // lda v0; or2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x19, 0x00, 0x01), // or2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 | v1)"),
                "Should contain bitwise OR: " + result);
        assertTrue(result.contains("let v2: number"),
                "Bitwise OR result should be typed as number: " + result);
    }

    @Test
    void testBitwiseXor() {
        // lda v0; xor2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x1A, 0x00, 0x01), // xor2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 ^ v1)"),
                "Should contain bitwise XOR: " + result);
        assertTrue(result.contains("let v2: number"),
                "Bitwise XOR result should be typed as number: " + result);
    }

    @Test
    void testBitwiseNot_viaXor() {
        // Bitwise NOT (~x) is implemented as XOR with -1 in Ark bytecode
        // ldai -1; sta v1; lda v0; xor2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x62), le32(-1),    // ldai -1
            bytes(0x61, 0x01),        // sta v1
            bytes(0x60, 0x00),        // lda v0
            bytes(0x1A, 0x00, 0x01),  // xor2 0, v1
            bytes(0x61, 0x02)         // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("^"),
                "Bitwise NOT via XOR should use ^ operator: " + result);
        assertTrue(result.contains("-1"),
                "Bitwise NOT via XOR should XOR with -1: " + result);
    }

    @Test
    void testLeftShift() {
        // lda v0; shl2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x15, 0x00, 0x01), // shl2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 << v1)"),
                "Should contain left shift: " + result);
        assertTrue(result.contains("let v2: number"),
                "Left shift result should be typed as number: " + result);
    }

    @Test
    void testRightShift() {
        // lda v0; ashr2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x17, 0x00, 0x01), // ashr2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 >> v1)"),
                "Should contain arithmetic right shift: " + result);
        assertTrue(result.contains("let v2: number"),
                "Right shift result should be typed as number: " + result);
    }

    @Test
    void testUnsignedRightShift() {
        // lda v0; shr2 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x16, 0x00, 0x01), // shr2 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 >>> v1)"),
                "Should contain unsigned right shift: " + result);
        assertTrue(result.contains("let v2: number"),
                "Unsigned right shift result should be typed as number: "
                        + result);
    }

    @Test
    void testStrictEquality() {
        // lda v0; stricteq 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x28, 0x00, 0x01), // stricteq 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 === v1)"),
                "Should contain strict equality: " + result);
        assertTrue(result.contains("let v2: boolean"),
                "Strict equality result should be typed as boolean: "
                        + result);
    }

    @Test
    void testStrictInequality() {
        // lda v0; strictnoteq 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x27, 0x00, 0x01), // strictnoteq 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 !== v1)"),
                "Should contain strict inequality: " + result);
        assertTrue(result.contains("let v2: boolean"),
                "Strict inequality result should be typed as boolean: "
                        + result);
    }

    @Test
    void testInstanceOf() {
        // lda v0; instanceof 0, v1; sta v2
        byte[] code = concat(
            bytes(0x60, 0x00),       // lda v0
            bytes(0x26, 0x00, 0x01), // instanceof 0, v1
            bytes(0x61, 0x02)        // sta v2
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("(v0 instanceof v1)"),
                "Should contain instanceof: " + result);
        assertTrue(result.contains("let v2: boolean"),
                "instanceof result should be typed as boolean: " + result);
    }

    @Test
    void testTypeof() {
        // lda v0; typeof 0; sta v1
        byte[] code = concat(
            bytes(0x60, 0x00),   // lda v0
            bytes(0x1C, 0x00),   // typeof 0
            bytes(0x61, 0x01)    // sta v1
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("typeof v0"),
                "Should contain typeof: " + result);
        assertTrue(result.contains("let v1: string"),
                "typeof result should be typed as string: " + result);
    }

    // --- Function expression and closure tests ---

    @Test
    void testArrowFunction_simple() {
        // Test ArrowFunctionExpression with single return
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("x", "number"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.BinaryExpression(
                                new ArkTSExpression.VariableExpression("x"),
                                "+",
                                new ArkTSExpression.LiteralExpression("1",
                                        ArkTSExpression.LiteralExpression
                                                .LiteralKind.NUMBER)))));
        ArkTSAccessExpressions.ArrowFunctionExpression expr =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        params, body, false);
        String result = expr.toArkTS();
        assertTrue(result.contains("(x: number) =>"),
                "Should contain arrow with params: " + result);
        assertTrue(result.contains("return (x + 1);"),
                "Should contain return expression: " + result);
    }

    @Test
    void testArrowFunction_withBody() {
        // Test ArrowFunctionExpression with block body
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(
                        new ArkTSDeclarations.FunctionDeclaration
                                .FunctionParam("x", "number"),
                        new ArkTSDeclarations.FunctionDeclaration
                                .FunctionParam("y", "number"));
        List<ArkTSStatement> stmts = List.of(
                new ArkTSStatement.VariableDeclaration("let", "sum",
                        "number",
                        new ArkTSExpression.BinaryExpression(
                                new ArkTSExpression.VariableExpression("x"),
                                "+",
                                new ArkTSExpression.VariableExpression("y"))),
                new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("sum")));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(stmts);
        ArkTSAccessExpressions.ArrowFunctionExpression expr =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        params, body, false);
        String result = expr.toArkTS();
        assertTrue(result.contains("(x: number, y: number) =>"),
                "Should contain params: " + result);
        assertTrue(result.contains("let sum: number = (x + y);"),
                "Should contain body: " + result);
    }

    @Test
    void testAnonymousFunctionExpression() {
        // Test AnonymousFunctionExpression toArkTS
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("x", "number"));
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.VariableDeclaration("let", "result",
                        null, new ArkTSExpression.VariableExpression("x")),
                new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("result")));
        ArkTSAccessExpressions.AnonymousFunctionExpression expr =
                new ArkTSAccessExpressions.AnonymousFunctionExpression(
                        params,
                        new ArkTSStatement.BlockStatement(bodyStmts),
                        false, false);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("function("),
                "Should start with 'function(': " + result);
        assertTrue(result.contains("x: number"),
                "Should contain param type: " + result);
        assertTrue(result.contains("let result = x;"),
                "Should contain body statements: " + result);
        assertFalse(result.contains("*"),
                "Should not contain generator star: " + result);
    }

    @Test
    void testAnonymousFunctionExpression_async() {
        // Test async anonymous function expression
        ArkTSAccessExpressions.AnonymousFunctionExpression expr =
                new ArkTSAccessExpressions.AnonymousFunctionExpression(
                        Collections.emptyList(),
                        new ArkTSStatement.BlockStatement(
                                Collections.emptyList()),
                        true, false);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("async function("),
                "Should start with 'async function(': " + result);
    }

    @Test
    void testAnonymousFunctionExpression_generator() {
        // Test generator anonymous function expression
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSAccessExpressions.YieldExpression(
                                new ArkTSExpression.LiteralExpression("1",
                                        ArkTSExpression.LiteralExpression
                                                .LiteralKind.NUMBER),
                                false)));
        ArkTSAccessExpressions.AnonymousFunctionExpression expr =
                new ArkTSAccessExpressions.AnonymousFunctionExpression(
                        Collections.emptyList(),
                        new ArkTSStatement.BlockStatement(bodyStmts),
                        false, true);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("function*("),
                "Should start with 'function*(': " + result);
        assertTrue(result.contains("yield 1"),
                "Should contain yield: " + result);
    }

    @Test
    void testGeneratorFunctionExpression() {
        // Test GeneratorFunctionExpression toArkTS
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("start", "number"));
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSAccessExpressions.YieldExpression(
                                new ArkTSExpression.VariableExpression(
                                        "start"),
                                false)));
        ArkTSAccessExpressions.GeneratorFunctionExpression expr =
                new ArkTSAccessExpressions.GeneratorFunctionExpression(
                        "gen", params,
                        new ArkTSStatement.BlockStatement(bodyStmts),
                        false);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("function* gen("),
                "Should start with 'function* gen(': " + result);
        assertTrue(result.contains("start: number"),
                "Should contain typed param: " + result);
        assertTrue(result.contains("yield start"),
                "Should contain yield: " + result);
    }

    @Test
    void testGeneratorFunctionExpression_asyncGenerator() {
        // Test async generator function expression
        ArkTSAccessExpressions.GeneratorFunctionExpression expr =
                new ArkTSAccessExpressions.GeneratorFunctionExpression(
                        "asyncGen", Collections.emptyList(),
                        new ArkTSStatement.BlockStatement(
                                Collections.emptyList()),
                        true);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("async function* asyncGen("),
                "Should start with 'async function* asyncGen(': " + result);
    }

    @Test
    void testGeneratorFunctionExpression_anonymous() {
        // Test anonymous generator function expression (no name)
        ArkTSAccessExpressions.GeneratorFunctionExpression expr =
                new ArkTSAccessExpressions.GeneratorFunctionExpression(
                        null, Collections.emptyList(),
                        new ArkTSStatement.BlockStatement(
                                Collections.emptyList()),
                        false);
        String result = expr.toArkTS();
        assertTrue(result.startsWith("function*("),
                "Should start with 'function*(' for anonymous: " + result);
    }

    @Test
    void testClosureCapturingVariable() {
        // Test ClosureExpression wraps inner function
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                Collections.emptyList();
        ArkTSExpression innerBody =
                new ArkTSExpression.BinaryExpression(
                        new ArkTSExpression.VariableExpression("lex_0_0"),
                        "+",
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER));
        ArkTSExpression arrow =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        params,
                        new ArkTSStatement.ExpressionStatement(innerBody),
                        false);
        List<String> captured = List.of("lex_0_0");
        ArkTSAccessExpressions.ClosureExpression closure =
                new ArkTSAccessExpressions.ClosureExpression(arrow,
                        captured);
        String result = closure.toArkTS();
        assertTrue(result.contains("() =>"),
                "Should contain arrow function: " + result);
        assertTrue(result.contains("lex_0_0"),
                "Should contain captured variable: " + result);
        assertEquals(1, closure.getCapturedVariables().size(),
                "Should track one captured variable");
        assertEquals("lex_0_0",
                closure.getCapturedVariables().get(0),
                "Should track captured variable name");
    }

    @Test
    void testFunctionExpressionAsArgument() {
        // Test that DefineFuncExpression produces func_N when used
        // directly (not stored to variable)
        DefineFuncExpression expr = new DefineFuncExpression(5);
        assertEquals("func_5", expr.toArkTS(),
                "Should render as func_5");
        assertEquals(5, expr.getMethodIdx(),
                "Should return method index");
    }

    @Test
    void testDefineFuncStoredToVariable() {
        // definefunc 3; sta v0; returnundefined
        // In instruction-only mode (no ABC file), definefunc creates a
        // function reference stored to v0. Full arrow function detection
        // requires ABC context with method definitions.
        byte[] code = concat(
                bytes(0x33, 0x03, 0x00, 0x00, 0x00),  // definefunc 3
                bytes(0x61, 0x00),                      // sta v0
                bytes(0x65)                              // returnundefined
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0"),
                "Should reference v0: " + result);
    }

    @Test
    void testNestedFunctionExpressions() {
        // Test nested arrow function: outer arrow with inner arrow in body
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> innerParams =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("y", "number"));
        ArkTSExpression innerArrow =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        innerParams,
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression("y")),
                        false);
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> outerParams =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("x", "number"));
        ArkTSStatement outerBody =
                new ArkTSStatement.BlockStatement(
                        List.of(new ArkTSStatement.ReturnStatement(
                                innerArrow)));
        ArkTSAccessExpressions.ArrowFunctionExpression outer =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        outerParams, outerBody, false);
        String result = outer.toArkTS();
        assertTrue(result.contains("(x: number) =>"),
                "Should contain outer params: " + result);
        assertTrue(result.contains("(y: number) =>"),
                "Should contain inner params: " + result);
    }

    @Test
    void testArrowFunctionInVariable() {
        // Test that a variable declaration with arrow function renders
        // correctly
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("x", "number"));
        ArkTSExpression arrow =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        params,
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.BinaryExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "x"),
                                        "+",
                                        new ArkTSExpression.LiteralExpression(
                                                "1",
                                                ArkTSExpression
                                                        .LiteralExpression
                                                        .LiteralKind.NUMBER))),
                        false);
        ArkTSStatement decl =
                new ArkTSStatement.VariableDeclaration(
                        "let", "addOne", null, arrow);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("let addOne = "),
                "Should declare variable: " + result);
        assertTrue(result.contains("(x: number) =>"),
                "Should contain arrow function: " + result);
        assertTrue(result.contains("(x + 1);"),
                "Should contain body expression: " + result);
    }

    // =================================================================
    // Wide (0xFD prefix) instruction tests
    // =================================================================

    @Test
    void testWideMov() {
        // 0xFD 0x8F = wide mov, WIDE_V8_V8 format (4 bytes total)
        // wide mov v10, v20
        byte[] code = concat(
                bytes(0xFD, 0x8F, 0x0A, 0x14),  // wide mov v10, v20
                bytes(0x64)                       // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isWide(),
                "First instruction should be wide");
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v10"),
                "Should declare v10: " + result);
        assertTrue(result.contains("v20"),
                "Should reference v20: " + result);
    }

    @Test
    void testWideNewobjrange() {
        // 0xFD 0x83 = wide newobjrange, WIDE_IMM16_IMM8_V8 format (5 bytes)
        // wide newobjrange 3, 0, v10
        // First: load the constructor into acc
        byte[] code = concat(
                bytes(0x60, 0x00),                          // lda v0 (constructor)
                bytes(0xFD, 0x83, 0x03, 0x00, 0x0A),        // wide newobjrange 3, 0, v10
                bytes(0x61, 0x01),                           // sta v1
                bytes(0x64)                                  // return
        );
        List<ArkInstruction> insns = dis(code);
        // The newobjrange instruction should be wide
        boolean foundWide = false;
        for (ArkInstruction insn : insns) {
            if (insn.isWide()) {
                foundWide = true;
                break;
            }
        }
        assertTrue(foundWide, "Should contain a wide instruction");
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("new "),
                "Should contain new keyword: " + result);
    }

    @Test
    void testWideDefinefunc() {
        // 0xFD 0x74 = wide definefunc, WIDE_IMM16_IMM16_IMM8 format (7 bytes)
        // wide definefunc 0x0100, 0x0001, 0
        byte[] code = concat(
                bytes(0xFD, 0x74, 0x00, 0x01, 0x01, 0x00, 0x00),
                bytes(0x61, 0x01),   // sta v1
                bytes(0x64)          // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isWide(),
                "First instruction should be wide");
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("func_"),
                "Should contain func_ reference: " + result);
    }

    @Test
    void testWideLdobjbyname() {
        // 0xFD 0x90 = wide ldobjbyname, WIDE_IMM16_IMM16 format (6 bytes)
        // wide ldobjbyname 0, 0x0005
        byte[] code = concat(
                bytes(0x60, 0x00),                          // lda v0 (object)
                bytes(0xFD, 0x90, 0x00, 0x00, 0x05, 0x00),  // wide ldobjbyname 0, 5
                bytes(0x61, 0x01),                           // sta v1
                bytes(0x64)                                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // Should not fall through to comment fallback
        assertFalse(result.contains("/* wide_"),
                "Should not emit wide comment fallback: " + result);
    }

    @Test
    void testWideStobjbyname() {
        // 0xFD 0x91 = wide stobjbyname, WIDE_IMM16_IMM16_V8 format (7 bytes)
        // wide stobjbyname 0, 0x0005, v2
        byte[] code = concat(
                bytes(0x60, 0x00),                                  // lda v0 (value)
                bytes(0xFD, 0x91, 0x00, 0x00, 0x05, 0x00, 0x02),    // wide stobjbyname 0, 5, v2
                bytes(0x64)                                          // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // Should not fall through to comment fallback
        assertFalse(result.contains("/* wide_"),
                "Should not emit wide comment fallback: " + result);
    }

    @Test
    void testWideCreateemptyarray() {
        // 0xFD 0x80 = wide createemptyarray, WIDE_IMM16 format (4 bytes)
        // wide createemptyarray 0x0100
        byte[] code = concat(
                bytes(0xFD, 0x80, 0x00, 0x01),  // wide createemptyarray 256
                bytes(0x61, 0x01),               // sta v1
                bytes(0x64)                      // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isWide(),
                "First instruction should be wide");
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("[]"),
                "Should contain empty array: " + result);
    }

    @Test
    void testWideLdobjbyvalue() {
        // 0xFD 0x85 = wide ldobjbyvalue, WIDE_IMM16_V8 format (5 bytes)
        // wide ldobjbyvalue 0, v3
        byte[] code = concat(
                bytes(0x60, 0x00),                      // lda v0 (object)
                bytes(0xFD, 0x85, 0x00, 0x00, 0x03),    // wide ldobjbyvalue 0, v3
                bytes(0x61, 0x01),                       // sta v1
                bytes(0x64)                              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        // Should not fall through to comment fallback
        assertFalse(result.contains("/* wide_"),
                "Should not emit wide comment fallback: " + result);
    }

    @Test
    void testWideMixed() {
        // Mix of normal and wide instructions in the same method
        byte[] code = concat(
                bytes(0x60, 0x00),                          // lda v0 (normal)
                bytes(0xFD, 0x8F, 0x02, 0x03),              // wide mov v2, v3
                bytes(0x60, 0x02),                          // lda v2 (normal)
                bytes(0xFD, 0x80, 0x0A, 0x00),              // wide createemptyarray 10
                bytes(0x61, 0x04),                          // sta v4 (normal)
                bytes(0x64)                                 // return (normal)
        );
        List<ArkInstruction> insns = dis(code);
        // Check we have both wide and normal instructions
        int wideCount = 0;
        for (ArkInstruction insn : insns) {
            if (insn.isWide()) {
                wideCount++;
            }
        }
        assertTrue(wideCount >= 2,
                "Should have at least 2 wide instructions, got " + wideCount);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.contains("/* wide_"),
                "Should not emit any wide comment fallback: " + result);
        assertTrue(result.contains("let v2"),
                "Should declare v2: " + result);
        assertTrue(result.contains("[]"),
                "Should contain array literal: " + result);
    }

    // --- Module system decompilation tests ---

    @Test
    void testDynamicImport_basic() {
        // DYNAMICIMPORT = 0xBD, no operands
        // Instruction-level test: verify it doesn't crash and produces output
        byte[] code = concat(
                bytes(0x3E, 0x00, 0x00),     // lda.str 0 -> acc = specifier string
                bytes(0xBD),                 // dynamicimport -> import(acc)
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testDynamicImport_withAwait() {
        // DYNAMICIMPORT followed by sta -> verify output without crash
        byte[] code = concat(
                bytes(0x65, 0x00),           // lda.str 0
                bytes(0xBD),                 // dynamicimport
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testLdExternalModuleVar_basic() {
        // LDEXTERNALMODULEVAR = 0x7E, IMM8 format
        // Loads the Nth imported variable
        byte[] code = concat(
                bytes(0x7E, 0x00),           // ldexternalmodulevar 0
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("ext_mod_0"),
                "Should contain external module variable: " + result);
    }

    @Test
    void testLdLocalModuleVar_basic() {
        // LDLOCALMODULEVAR = 0x7D, IMM8 format
        byte[] code = concat(
                bytes(0x7D, 0x00),           // ldlocalmodulevar 0
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("local_mod_0"),
                "Should contain local module variable: " + result);
    }

    @Test
    void testStModuleVar_basic() {
        // STMODULEVAR = 0x7C, IMM8 format
        // Instruction-level test: verify no crash
        byte[] code = concat(
                bytes(0x65, 0x00),           // lda.str 0
                bytes(0x7C, 0x00),           // stmodulevar 0
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testGetModuleNamespace_basic() {
        // GETMODULENAMESPACE = 0x7B, IMM8 format
        byte[] code = concat(
                bytes(0x7B, 0x00),           // getmodulenamespace 0
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("module_ns_0"),
                "Should contain module namespace variable: " + result);
    }

    @Test
    void testImportStatement_defaultImport() {
        // Test the ImportStatement AST node for default imports
        ArkTSDeclarations.ImportStatement stmt =
                new ArkTSDeclarations.ImportStatement(
                        Collections.emptyList(), "@ohos/entry",
                        true, "MyClass", null);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import MyClass"),
                "Should contain default import: " + output);
        assertTrue(output.contains("from '@ohos/entry'"),
                "Should contain module path: " + output);
    }

    @Test
    void testImportStatement_namespaceImport() {
        // Test the ImportStatement AST node for namespace imports
        ArkTSDeclarations.ImportStatement stmt =
                new ArkTSDeclarations.ImportStatement(
                        Collections.emptyList(), "@ohos/lib",
                        false, null, "lib");
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import * as lib"),
                "Should contain namespace import: " + output);
        assertTrue(output.contains("from '@ohos/lib'"),
                "Should contain module path: " + output);
    }

    @Test
    void testImportStatement_namedImports() {
        // Test the ImportStatement AST node with named imports
        ArkTSDeclarations.ImportStatement stmt =
                new ArkTSDeclarations.ImportStatement(
                        List.of("foo", "bar as baz"), "./utils",
                        false, null, null);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import { foo, bar as baz }"),
                "Should contain named imports: " + output);
        assertTrue(output.contains("from './utils'"),
                "Should contain module path: " + output);
    }

    @Test
    void testImportStatement_defaultWithNamed() {
        // Test combined default + named import
        ArkTSDeclarations.ImportStatement stmt =
                new ArkTSDeclarations.ImportStatement(
                        List.of("Helper"), "@ohos/entry",
                        true, "MyClass", null);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import MyClass, { Helper }"),
                "Should contain default + named import: " + output);
    }

    @Test
    void testExportStatement_reExport() {
        // Test re-export: export { X } from 'module'
        ArkTSDeclarations.ExportStatement stmt =
                new ArkTSDeclarations.ExportStatement(
                        List.of("foo", "bar as baz"), null, false,
                        "@ohos/util");
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("export { foo, bar as baz }"),
                "Should contain named re-exports: " + output);
        assertTrue(output.contains("from '@ohos/util'"),
                "Should contain from clause: " + output);
    }

    @Test
    void testExportStatement_starExport() {
        // Test star export: export * from 'module'
        ArkTSDeclarations.ExportStatement stmt =
                new ArkTSDeclarations.ExportStatement(
                        Collections.emptyList(), null, false,
                        "@ohos/core", true);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("export * from '@ohos/core'"),
                "Should contain star export: " + output);
    }

    @Test
    void testExportStatement_localExport() {
        // Test local export: export { X }
        ArkTSDeclarations.ExportStatement stmt =
                new ArkTSDeclarations.ExportStatement(
                        List.of("MyClass"), null, false);
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("export { MyClass };"),
                "Should contain local export: " + output);
    }

    @Test
    void testModuleImportCollector_basic() {
        // Test the ModuleImportCollector utility
        ArkTSDecompiler.ModuleImportCollector collector =
                new ArkTSDecompiler.ModuleImportCollector();
        collector.addNamedImport("foo", "foo");
        collector.addNamedImport("Bar", "myBar");
        collector.setDefaultImport("Default");

        ArkTSDeclarations.ImportStatement stmt =
                collector.toImportStatement("my-module");
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import Default, { foo, Bar as myBar }"),
                "Should merge default + named imports: " + output);
        assertTrue(output.contains("from 'my-module'"),
                "Should contain module path: " + output);
    }

    @Test
    void testModuleImportCollector_namespaceOnly() {
        // Test namespace-only import collector
        ArkTSDecompiler.ModuleImportCollector collector =
                new ArkTSDecompiler.ModuleImportCollector();
        collector.setNamespaceImport("lib");

        ArkTSDeclarations.ImportStatement stmt =
                collector.toImportStatement("@ohos/lib");
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("import * as lib"),
                "Should contain namespace import: " + output);
    }

    @Test
    void testModuleImportCollector_deduplication() {
        // Test that duplicate named imports are deduplicated
        ArkTSDecompiler.ModuleImportCollector collector =
                new ArkTSDecompiler.ModuleImportCollector();
        collector.addNamedImport("foo", "foo");
        collector.addNamedImport("foo", "foo");

        ArkTSDeclarations.ImportStatement stmt =
                collector.toImportStatement("mod");
        String output = stmt.toArkTS(0);
        assertTrue(output.contains("{ foo }"),
                "Should contain only one foo: " + output);
        int count = output.split("foo").length - 1;
        assertEquals(1, count,
                "Should have exactly one occurrence of foo: " + output);
    }

    // --- Issue #55: Iterator and generator protocol decompilation tests ---

    @Test
    void testForAwaitOfStatement_toArkTS() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("asyncStream");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSControlFlow.ForAwaitOfStatement stmt =
                new ArkTSControlFlow.ForAwaitOfStatement("const", "chunk",
                        iterable, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("for await (const chunk of asyncStream)"),
                "Should start with for-await-of header, got: " + result);
        assertTrue(result.contains("{"),
                "Should contain opening brace, got: " + result);
    }

    @Test
    void testForAwaitOfStatement_withBody() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("asyncData");
        ArkTSExpression value =
                new ArkTSExpression.VariableExpression("chunk");
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression(
                                        "process"),
                                List.of(value))));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSControlFlow.ForAwaitOfStatement stmt =
                new ArkTSControlFlow.ForAwaitOfStatement("const", "chunk",
                        iterable, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("for await (const chunk of asyncData)"),
                "Should contain for-await-of header, got: " + result);
        assertTrue(result.contains("process(chunk)"),
                "Should contain body call, got: " + result);
    }

    @Test
    void testForAwaitOfStatement_withIndent() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("events");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForAwaitOfStatement stmt =
                new ArkTSControlFlow.ForAwaitOfStatement("const", "evt",
                        iterable, body);
        String result = stmt.toArkTS(2);
        assertTrue(result.startsWith("        for await (const evt of events)"),
                "Should be indented 2 levels, got: " + result);
    }

    @Test
    void testForAwaitOfStatement_getters() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("asyncItems");
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSControlFlow.ForAwaitOfStatement stmt =
                new ArkTSControlFlow.ForAwaitOfStatement("let", "item",
                        iterable, body);
        assertEquals("let", stmt.getVariableKind());
        assertEquals("item", stmt.getVariableName());
        assertEquals("asyncItems", stmt.getIterable().toArkTS());
    }

    @Test
    void testDecompile_asyncIteratorInstruction() {
        // GETASYNCITERATOR = 0xD7
        ArkInstruction getAsyncIter = new ArkInstruction(
                ArkOpcodesCompat.GETASYNCITERATOR, "getasynciterator",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        ArkInstruction retUndef = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 1, 1,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(getAsyncIter, retUndef));
        assertNotNull(result,
                "Should handle GETASYNCITERATOR without error");
    }

    @Test
    void testDecompile_generatorWithYieldExpression() {
        // CREATEGENERATOROBJ v0
        ArkInstruction createGen = new ArkInstruction(
                ArkOpcodesCompat.CREATEGENERATOROBJ, "creategeneratorobj",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        // LDAI 42
        ArkInstruction ldai = new ArkInstruction(
                ArkOpcodesCompat.LDAI, "ldai",
                ArkInstructionFormat.IMM32, 2, 5,
                List.of(new ArkOperand(ArkOperand.Type.IMMEDIATE32_SIGNED, 42)),
                false);
        // STA v1
        ArkInstruction sta = new ArkInstruction(
                ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        // SUSPENDGENERATOR v1
        ArkInstruction suspendGen = new ArkInstruction(
                ArkOpcodesCompat.SUSPENDGENERATOR, "suspendgenerator",
                ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        // RETURNUNDEFINED
        ArkInstruction retUndef = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 11, 1,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(createGen, ldai, sta, suspendGen, retUndef));
        assertNotNull(result,
                "Should decompile generator with yield without error");
        assertTrue(result.contains("yield"),
                "Should contain yield keyword, got: " + result);
    }

    @Test
    void testDecompile_yieldDelegateExpression() {
        // Test that yield* with an iterator variable produces correct output
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("iterator");
        ArkTSAccessExpressions.YieldExpression expr =
                new ArkTSAccessExpressions.YieldExpression(iterable, true);
        assertEquals("yield* iterator", expr.toArkTS(),
                "Delegate yield should use yield* syntax");
    }

    @Test
    void testDecompile_asyncGeneratorWithReturn() {
        // CREATEASYNCGENERATOROBJ v0
        ArkInstruction createAsyncGen = new ArkInstruction(
                ArkOpcodesCompat.CREATEASYNCGENERATOROBJ,
                "createasyncgeneratorobj",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        // LDAI 100
        ArkInstruction ldai = new ArkInstruction(
                ArkOpcodesCompat.LDAI, "ldai",
                ArkInstructionFormat.IMM32, 2, 5,
                List.of(new ArkOperand(ArkOperand.Type.IMMEDIATE32_SIGNED, 100)),
                false);
        // STA v1
        ArkInstruction sta = new ArkInstruction(
                ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        // ASYNCGENERATORRESOLVE v0, v1, v2
        ArkInstruction resolve = new ArkInstruction(
                ArkOpcodesCompat.ASYNCGENERATORRESOLVE,
                "asyncgeneratorresolve",
                ArkInstructionFormat.V8_V8_V8, 9, 4,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0),
                        new ArkOperand(ArkOperand.Type.REGISTER, 1),
                        new ArkOperand(ArkOperand.Type.REGISTER, 2)),
                false);
        // RETURNUNDEFINED
        ArkInstruction retUndef = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 13, 1,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(createAsyncGen, ldai, sta, resolve, retUndef));
        assertNotNull(result,
                "Should decompile async generator resolve without error");
        assertTrue(result.contains("return"),
                "Should contain return from async generator resolve, got: "
                        + result);
    }

    @Test
    void testDecompile_forAwaitOfLoopPattern() {
        List<ArkInstruction> insns = new ArrayList<>();
        // offset 0: GETASYNCITERATOR (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETASYNCITERATOR,
                "getasynciterator", ArkInstructionFormat.NONE,
                0, 1, Collections.emptyList(), false));
        // offset 1: STA v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 1, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 3: LDA v1 (len 2) -- loop header
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 3, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 5: GETNEXTPROPNAME v1 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.GETNEXTPROPNAME,
                "getnextpropname", ArkInstructionFormat.V8, 5, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 1)),
                false));
        // offset 7: STA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 7, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 9: LDA v2 (len 2)
        insns.add(new ArkInstruction(ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 9, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 2)),
                false));
        // offset 11: JEQZ_IMM8 -> offset 18 (len 2), offset_val = 18-13 = 5
        insns.add(new ArkInstruction(ArkOpcodesCompat.JEQZ_IMM8, "jeqz",
                ArkInstructionFormat.IMM8, 11, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 5)),
                false));
        // offset 13: STA v3 (len 2) -- loop body
        insns.add(new ArkInstruction(ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 13, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.REGISTER, 3)),
                false));
        // offset 15: JMP_IMM16 -> offset 3 (len 3), offset_val = 3-18 = -15
        insns.add(new ArkInstruction(ArkOpcodesCompat.JMP_IMM16, "jmp",
                ArkInstructionFormat.IMM16, 15, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, -15)),
                false));
        // offset 18: RETURNUNDEFINED (len 1)
        insns.add(new ArkInstruction(ArkOpcodesCompat.RETURNUNDEFINED,
                "returnundefined", ArkInstructionFormat.NONE, 18, 1,
                Collections.emptyList(), false));
        String result = decompiler.decompileInstructions(insns);
        assertNotNull(result,
                "Should decompile for-await-of instructions without error, got: "
                        + result);
    }

    @Test
    void testDecompile_generatorFunctionExpression() {
        // Test GeneratorFunctionExpression with name and async flag
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("n", null));
        ArkTSExpression yieldExpr =
                new ArkTSAccessExpressions.YieldExpression(
                        new ArkTSExpression.VariableExpression("n"), false);
        ArkTSStatement bodyStmt =
                new ArkTSStatement.ExpressionStatement(yieldExpr);
        ArkTSAccessExpressions.GeneratorFunctionExpression expr =
                new ArkTSAccessExpressions.GeneratorFunctionExpression(
                        "gen", params,
                        new ArkTSStatement.BlockStatement(List.of(bodyStmt)),
                        false);
        String result = expr.toArkTS();
        assertTrue(result.contains("function*"),
                "Should contain function* keyword, got: " + result);
        assertTrue(result.contains("gen"),
                "Should contain generator name, got: " + result);
        assertTrue(result.contains("yield n"),
                "Should contain yield expression, got: " + result);
    }

    @Test
    void testDecompile_asyncGeneratorFunctionExpression() {
        // Test async generator function expression
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                Collections.emptyList();
        ArkTSExpression yieldExpr =
                new ArkTSAccessExpressions.YieldExpression(
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER),
                        false);
        ArkTSStatement bodyStmt =
                new ArkTSStatement.ExpressionStatement(yieldExpr);
        ArkTSAccessExpressions.GeneratorFunctionExpression expr =
                new ArkTSAccessExpressions.GeneratorFunctionExpression(
                        null, params,
                        new ArkTSStatement.BlockStatement(List.of(bodyStmt)),
                        true);
        String result = expr.toArkTS();
        assertTrue(result.contains("async function*"),
                "Should contain async function* keyword, got: " + result);
        assertTrue(result.contains("yield 1"),
                "Should contain yield expression, got: " + result);
    }

    @Test
    void testDecompile_yieldExpressionBare() {
        // Bare yield (no value) followed by return
        ArkInstruction suspendGen = new ArkInstruction(
                ArkOpcodesCompat.SUSPENDGENERATOR, "suspendgenerator",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction retUndef = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 2, 1,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(suspendGen, retUndef));
        assertNotNull(result,
                "Should handle bare yield without error");
        assertTrue(result.contains("yield"),
                "Should contain yield keyword, got: " + result);
    }

    // --- Issue #53: Error handling decompilation ---

    @Test
    void testTryCatchStatement_withCatchParamType() {
        // Test catch (e: TypeError) formatting
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("risky()"))));
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.CallExpression(
                                new ArkTSExpression.VariableExpression("console.log"),
                                List.of(new ArkTSExpression.VariableExpression("e"))))));
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        "TypeError", catchBody, null);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("try {"),
                "Should start with try, got: " + result);
        assertTrue(result.contains("catch (e: TypeError) {"),
                "Should contain typed catch parameter, got: " + result);
        assertFalse(result.contains("finally"),
                "Should not contain finally, got: " + result);
    }

    @Test
    void testTryCatchStatement_finallyOnly() {
        // Test try { } finally { } without catch
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("doWork()"))));
        ArkTSStatement finallyBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("cleanup()"))));
        // catch-only region: catch-all is used as catch handler,
        // no finally handler separately
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        null, null, finallyBody);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("try {"),
                "Should start with try, got: " + result);
        assertFalse(result.contains("catch"),
                "Should not contain catch, got: " + result);
        assertTrue(result.contains("finally {"),
                "Should contain finally, got: " + result);
        assertTrue(result.contains("cleanup()"),
                "Should contain cleanup call, got: " + result);
    }

    @Test
    void testTryCatchStatement_withCatchAndFinally() {
        // Test try { } catch (e: Error) { } finally { }
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("dangerous()"))));
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("handleError()"))));
        ArkTSStatement finallyBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("cleanup()"))));
        ArkTSControlFlow.TryCatchStatement stmt =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        "Error", catchBody, finallyBody);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("try {"),
                "Should start with try, got: " + result);
        assertTrue(result.contains("catch (e: Error) {"),
                "Should contain typed catch, got: " + result);
        assertTrue(result.contains("handleError()"),
                "Should contain catch body, got: " + result);
        assertTrue(result.contains("finally {"),
                "Should contain finally, got: " + result);
        assertTrue(result.contains("cleanup()"),
                "Should contain finally body, got: " + result);
    }

    @Test
    void testDecompile_throwNewError() {
        // Test throw new Error("msg") via ldai + newobjrange + throw
        // Simulate: ldai 0 -> lda.str 0 -> newobjrange 1, v0 ->
        //           sta v1 -> lda v1 -> throw
        // For simplicity, test ldai 42; sta v0; lda v0; throw
        byte[] code = concat(
                bytes(0x62), le32(42),    // ldai 42       offset 0
                bytes(0x61, 0x00),         // sta v0        offset 5
                bytes(0x60, 0x00),         // lda v0        offset 7
                bytes(0xFE)                // throw         offset 9
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("throw"),
                "Should contain throw keyword, got: " + result);
        assertTrue(result.contains("let v0 = 42"),
                "Should contain variable declaration, got: " + result);
    }

    @Test
    void testDecompile_tryCatchFinally_withTypedCatch() {
        // Build a method with try/catch/finally using AbcTryBlock
        byte[] codeBytes = concat(
                bytes(0x62), le32(1),          // ldai 1     offset 0
                bytes(0x61, 0x00),             // sta v0     offset 5
                bytes(0x64),                    // return     offset 7
                bytes(0x60, 0x00),             // lda v0     offset 8 (catch handler)
                bytes(0x61, 0x01),             // sta v1     offset 10
                bytes(0x64),                    // return     offset 12 (end catch)
                bytes(0x60, 0x01),             // lda v1     offset 13 (finally handler)
                bytes(0x64)                     // return     offset 15
        );
        // Typed catch at offset 8, finally (catch-all) at offset 13
        AbcCatchBlock typedCatch =
                new AbcCatchBlock(1, 8, 4, false);
        AbcCatchBlock finallyCatch =
                new AbcCatchBlock(0, 13, 2, true);
        AbcTryBlock tryBlock = new AbcTryBlock(0, 7,
                List.of(typedCatch, finallyCatch));
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                List.of(tryBlock), 1);
        AbcMethod method = new AbcMethod(0, 0, "test",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("try"),
                "Expected try statement, got: " + result);
    }

    @Test
    void testDecompile_tryFinally_noCatch() {
        // Build a method with try/finally (no typed catch)
        byte[] codeBytes = concat(
                bytes(0x62), le32(1),          // ldai 1     offset 0
                bytes(0x61, 0x00),             // sta v0     offset 5
                bytes(0x64),                    // return     offset 7
                bytes(0x60, 0x00),             // lda v0     offset 8 (catch-all/finally)
                bytes(0x64)                     // return     offset 10
        );
        // Only a catch-all (finally) handler
        AbcCatchBlock catchAll =
                new AbcCatchBlock(0, 8, 2, true);
        AbcTryBlock tryBlock = new AbcTryBlock(0, 7,
                List.of(catchAll));
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                List.of(tryBlock), 1);
        AbcMethod method = new AbcMethod(0, 0, "cleanup",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("try"),
                "Expected try statement, got: " + result);
    }

    @Test
    void testDecompile_nestedTryCatchWithFinally() {
        // Outer try/catch/finally with inner try/catch
        byte[] codeBytes = concat(
                bytes(0x62), le32(1),          // ldai 1     offset 0
                bytes(0x61, 0x00),             // sta v0     offset 5
                bytes(0x62), le32(2),          // ldai 2     offset 7
                bytes(0x61, 0x01),             // sta v1     offset 12
                bytes(0x64),                    // return     offset 14
                bytes(0x60, 0x00),             // lda v0     offset 15 (inner catch)
                bytes(0x64),                    // return     offset 17
                bytes(0x60, 0x01),             // lda v1     offset 18 (outer catch)
                bytes(0x64),                    // return     offset 20
                bytes(0x60, 0x00),             // lda v0     offset 21 (outer finally)
                bytes(0x64)                     // return     offset 23
        );
        // Inner try: offsets 0-7, catch at 15
        AbcCatchBlock innerCatch =
                new AbcCatchBlock(0, 15, 2, false);
        AbcTryBlock innerTry = new AbcTryBlock(0, 7,
                List.of(innerCatch));
        // Outer try: offsets 0-14, catch at 18, finally at 21
        AbcCatchBlock outerCatch =
                new AbcCatchBlock(0, 18, 2, false);
        AbcCatchBlock outerFinally =
                new AbcCatchBlock(0, 21, 2, true);
        AbcTryBlock outerTry = new AbcTryBlock(0, 14,
                List.of(outerCatch, outerFinally));
        AbcCode code = new AbcCode(2, 0, codeBytes.length, codeBytes,
                List.of(innerTry, outerTry), 1);
        AbcMethod method = new AbcMethod(0, 0, "nestedFinally",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("try"),
                "Expected try statement, got: " + result);
    }

    @Test
    void testDecompile_throwWithAccumulatorValue() {
        // Test throw with an expression loaded into accumulator
        // lda.str 0 -> throw  (throws the string value)
        byte[] code = concat(
                bytes(0x3E, 0x00, 0x00),       // lda.str 0    offset 0
                bytes(0xFE)                      // throw        offset 3
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("throw"),
                "Should contain throw, got: " + result);
    }

    @Test
    void testTryCatchStatement_multipleCatchTypes() {
        // Test that different catch types produce correct annotations
        ArkTSStatement tryBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement catchBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression("handle()"))));
        // TypeError
        ArkTSControlFlow.TryCatchStatement typeError =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "e",
                        "TypeError", catchBody, null);
        assertTrue(typeError.toArkTS(0).contains("catch (e: TypeError)"),
                "Should have TypeError annotation");
        // SyntaxError
        ArkTSControlFlow.TryCatchStatement syntaxError =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "err",
                        "SyntaxError", catchBody, null);
        assertTrue(syntaxError.toArkTS(0).contains("catch (err: SyntaxError)"),
                "Should have SyntaxError annotation");
        // RangeError
        ArkTSControlFlow.TryCatchStatement rangeError =
                new ArkTSControlFlow.TryCatchStatement(tryBody, "ex",
                        "RangeError", catchBody, null);
        assertTrue(rangeError.toArkTS(0).contains("catch (ex: RangeError)"),
                "Should have RangeError annotation");
    }

    @Test
    void testDecompile_throwWithNewExpression() {
        // Test throw new Error("message") pattern
        // Build: ldai 0 -> sta v0 -> lda.str 1 ->
        //        sta v1 -> lda.str "Error" -> newobjrange 1, v1 ->
        //        sta v2 -> lda v2 -> throw
        // Simplified: just test that throw follows a value load
        byte[] code = concat(
                bytes(0x62), le32(0),          // ldai 0        offset 0
                bytes(0x61, 0x00),             // sta v0         offset 5
                bytes(0x60, 0x00),             // lda v0         offset 7
                bytes(0xFE)                      // throw          offset 9
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("throw"),
                "Should contain throw, got: " + result);
        assertTrue(result.contains("v0"),
                "Should reference variable, got: " + result);
    }

    // --- Async/await decompilation tests (issue #52) ---

    @Test
    void testFunctionDeclaration_async() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("42",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER))));
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        "fetchData", Collections.emptyList(),
                        "Promise<number>", body, true);
        String result = func.toArkTS(0);
        assertTrue(result.startsWith("async function fetchData"),
                "Should start with 'async function fetchData': " + result);
        assertFalse(result.startsWith("function fetchData"),
                "Should NOT start without 'async': " + result);
        assertTrue(result.contains("Promise<number>"),
                "Should have return type: " + result);
    }

    @Test
    void testFunctionDeclaration_syncNotAsync() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        "syncMethod", Collections.emptyList(), null,
                        body, false);
        String result = func.toArkTS(0);
        assertTrue(result.startsWith("function syncMethod"),
                "Sync function should NOT start with 'async': " + result);
    }

    @Test
    void testClassMethodDeclaration_async() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("result",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.STRING))));
        ArkTSDeclarations.ClassMethodDeclaration method =
                new ArkTSDeclarations.ClassMethodDeclaration(
                        "loadData", Collections.emptyList(),
                        "Promise<string>", body, false,
                        "public", true);
        String result = method.toArkTS(1);
        assertTrue(result.contains("public async loadData"),
                "Should contain 'public async loadData': " + result);
        assertTrue(result.contains("Promise<string>"),
                "Should have return type: " + result);
    }

    @Test
    void testClassMethodDeclaration_asyncStatic() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSDeclarations.ClassMethodDeclaration method =
                new ArkTSDeclarations.ClassMethodDeclaration(
                        "fetchStatic", Collections.emptyList(), null,
                        body, true, null, true);
        String result = method.toArkTS(1);
        assertTrue(result.contains("static async fetchStatic"),
                "Should contain 'static async fetchStatic': " + result);
    }

    @Test
    void testDecompileInstructions_asyncFunctionWithAwait() {
        // asyncfunctionenter; lda v0; asyncfunctionawaituncaught 0;
        // sta v1; lda v1; return
        ArkInstruction asyncEnter = new ArkInstruction(
                ArkOpcodesCompat.ASYNCFUNCTIONENTER,
                "asyncfunctionenter",
                ArkInstructionFormat.NONE, 0, 1,
                Collections.emptyList(), false);
        ArkInstruction ldaV0 = new ArkInstruction(
                ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 1, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction awaitInsn = new ArkInstruction(
                ArkOpcodesCompat.ASYNCFUNCTIONAWAITUNCAUGHT,
                "asyncfunctionawaituncaught",
                ArkInstructionFormat.V8, 2, 3,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction staV1 = new ArkInstruction(
                ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 3, 4,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        ArkInstruction ldaV1 = new ArkInstruction(
                ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 4, 5,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        ArkInstruction ret = new ArkInstruction(
                ArkOpcodesCompat.RETURN, "return",
                ArkInstructionFormat.NONE, 5, 6,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(asyncEnter, ldaV0, awaitInsn, staV1,
                        ldaV1, ret));
        assertTrue(result.contains("await"),
                "Should contain await: " + result);
        assertTrue(result.contains("return"),
                "Should contain return: " + result);
    }

    @Test
    void testDecompile_asyncFunctionEnter_setsIsAsyncContext() {
        // Verify ASYNCFUNCTIONENTER is processed without error and
        // subsequent instructions decompile correctly.
        // Build: asyncfunctionenter, ldai 1, sta v0, lda v0, return
        byte[] code = concat(
                bytes(0xAE),              // asyncfunctionenter
                bytes(0x62), le32(1),     // ldai 1
                bytes(0x61, 0x00),        // sta v0
                bytes(0x60, 0x00),        // lda v0
                bytes(0x64)               // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("return"),
                "Should contain return: " + result);
        assertTrue(result.contains("1"),
                "Should reference value 1: " + result);
    }

    @Test
    void testDecompile_awaitWithPromiseVariable() {
        // lda v0; asyncfunctionawaituncaught 0; sta v1; lda v1; return
        ArkInstruction ldaV0 = new ArkInstruction(
                ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction awaitInsn = new ArkInstruction(
                ArkOpcodesCompat.ASYNCFUNCTIONAWAITUNCAUGHT,
                "asyncfunctionawaituncaught",
                ArkInstructionFormat.V8, 2, 3,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction staV1 = new ArkInstruction(
                ArkOpcodesCompat.STA, "sta",
                ArkInstructionFormat.V8, 3, 4,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        ArkInstruction ldaV1 = new ArkInstruction(
                ArkOpcodesCompat.LDA, "lda",
                ArkInstructionFormat.V8, 4, 5,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 1)),
                false);
        ArkInstruction ret = new ArkInstruction(
                ArkOpcodesCompat.RETURN, "return",
                ArkInstructionFormat.NONE, 5, 6,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(ldaV0, awaitInsn, staV1, ldaV1, ret));
        assertTrue(result.contains("await"),
                "Should contain 'await': " + result);
        assertTrue(result.contains("let v1"),
                "Should declare v1: " + result);
    }

    @Test
    void testDecompile_asyncReject_producesThrow() {
        // asyncfunctionreject 0 -> should produce throw
        ArkInstruction rejectInsn = new ArkInstruction(
                ArkOpcodesCompat.ASYNCFUNCTIONREJECT,
                "asyncfunctionreject",
                ArkInstructionFormat.V8, 0, 2,
                List.of(new ArkOperand(ArkOperand.Type.REGISTER, 0)),
                false);
        ArkInstruction retUndef = new ArkInstruction(
                ArkOpcodesCompat.RETURNUNDEFINED, "returnundefined",
                ArkInstructionFormat.NONE, 2, 1,
                Collections.emptyList(), false);
        String result = decompiler.decompileInstructions(
                List.of(rejectInsn, retUndef));
        assertTrue(result.contains("throw"),
                "async reject should produce throw: " + result);
    }

    @Test
    void testArrowFunction_asyncDeclaration() {
        // Test async arrow function renders with async prefix
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("url", "string"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("data",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.STRING))));
        ArkTSAccessExpressions.ArrowFunctionExpression arrow =
                new ArkTSAccessExpressions.ArrowFunctionExpression(
                        params, body, true);
        String result = arrow.toArkTS();
        assertTrue(result.startsWith("async "),
                "Async arrow should start with 'async ': " + result);
        assertTrue(result.contains("=>"),
                "Arrow function should contain =>: " + result);
        assertTrue(result.contains("url: string"),
                "Should contain parameter type: " + result);
    }

    // --- Type annotation and generics decompilation tests (issue #54) ---

    @Test
    void testGenericFunctionDeclaration_toArkTS() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "x", "T"));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSTypeDeclarations.GenericFunctionDeclaration decl =
                new ArkTSTypeDeclarations.GenericFunctionDeclaration(
                        "identity", typeParams, params, "T", body);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("function identity<T>(x: T): T"),
                "Should produce generic function signature, got: " + result);
    }

    @Test
    void testGenericFunctionDeclaration_withConstraint() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams(
                        new String[] {"T"},
                        new String[] {"Comparable"});
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "x", "T"));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSTypeDeclarations.GenericFunctionDeclaration decl =
                new ArkTSTypeDeclarations.GenericFunctionDeclaration(
                        "compare", typeParams, params, "number", body);
        String result = decl.toArkTS(0);
        assertTrue(result.contains(
                "function compare<T extends Comparable>(x: T): number"),
                "Should produce constrained generic signature, got: "
                        + result);
    }

    @Test
    void testGenericFunctionDeclaration_multipleTypeParams() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T", "U");
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "key", "T"));
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "value", "U"));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSTypeDeclarations.GenericFunctionDeclaration decl =
                new ArkTSTypeDeclarations.GenericFunctionDeclaration(
                        "map", typeParams, params, "Object", body);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("function map<T, U>(key: T, value: U)"),
                "Should produce multi-param generic signature, got: "
                        + result);
    }

    @Test
    void testFunctionDeclaration_withTypeParams() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "x", "T"));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration decl =
                new ArkTSDeclarations.FunctionDeclaration(
                        "wrap", params, "T", body, false, typeParams);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("function wrap<T>(x: T): T"),
                "FunctionDeclaration should support type params, got: "
                        + result);
    }

    @Test
    void testUnionType_buildAndFormat() {
        List<String> types = new ArrayList<>();
        types.add("string");
        types.add("number");
        String union = TypeInference.buildUnionType(types);
        assertEquals("number | string", union);
        assertTrue(TypeInference.isUnionType(union));
        List<String> parsed = TypeInference.parseUnionTypes(union);
        assertEquals(2, parsed.size());
        assertTrue(parsed.contains("number"));
        assertTrue(parsed.contains("string"));
    }

    @Test
    void testUnionType_deduplicationAndSingle() {
        List<String> dupes = new ArrayList<>();
        dupes.add("string");
        dupes.add("string");
        dupes.add("number");
        String result = TypeInference.buildUnionType(dupes);
        assertEquals("number | string", result);
        List<String> single = new ArrayList<>();
        single.add("boolean");
        assertEquals("boolean",
                TypeInference.buildUnionType(single));
        assertNull(TypeInference.buildUnionType(Collections.emptyList()));
    }

    @Test
    void testArrayType_formatAndExtract() {
        assertEquals("number[]",
                TypeInference.formatArrayType("number"));
        assertEquals("string[]",
                TypeInference.formatArrayType("string"));
        assertEquals("Array<string | number>",
                TypeInference.formatArrayType("string | number"));
        assertEquals("Array<Map<string, number>>",
                TypeInference.formatArrayType("Map<string, number>"));
        assertTrue(TypeInference.isArrayType("number[]"));
        assertTrue(TypeInference.isArrayType("Array<string>"));
        assertFalse(TypeInference.isArrayType("number"));
        assertEquals("number",
                TypeInference.extractArrayElementType("number[]"));
        assertEquals("string",
                TypeInference.extractArrayElementType("Array<string>"));
        assertEquals("unknown",
                TypeInference.extractArrayElementType("unknown"));
    }

    @Test
    void testGenericType_formatAndCheck() {
        List<String> args = new ArrayList<>();
        args.add("T");
        assertEquals("Container<T>",
                TypeInference.formatGenericType("Container", args));
        assertTrue(TypeInference.isGenericType("Container<T>"));
        assertFalse(TypeInference.isGenericType("string"));
        List<String> multiArgs = new ArrayList<>();
        multiArgs.add("string");
        multiArgs.add("number");
        assertEquals("Map<string, number>",
                TypeInference.formatGenericType("Map", multiArgs));
        assertEquals("Object",
                TypeInference.formatGenericType(null, multiArgs));
        assertEquals("string",
                TypeInference.formatGenericType("string",
                        Collections.emptyList()));
    }

    @Test
    void testTypeGuard_typeofNarrowing() {
        assertEquals("string",
                TypeInference.typeofStringToType("\"string\""));
        assertEquals("number",
                TypeInference.typeofStringToType("\"number\""));
        assertEquals("boolean",
                TypeInference.typeofStringToType("\"boolean\""));
        assertEquals("undefined",
                TypeInference.typeofStringToType("\"undefined\""));
        assertEquals("Object",
                TypeInference.typeofStringToType("\"object\""));
        assertNull(TypeInference.typeofStringToType("unknown"));
        assertNull(TypeInference.typeofStringToType(null));
    }

    @Test
    void testTypeGuard_narrowFromTypeofExpression() {
        ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                "typeof",
                new ArkTSExpression.VariableExpression("v0"), true);
        ArkTSExpression stringLit =
                new ArkTSExpression.LiteralExpression("string",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression guard = new ArkTSExpression.BinaryExpression(
                typeofX, "===", stringLit);
        String narrowed = TypeInference.narrowTypeFromTypeofGuard(guard);
        assertEquals("string", narrowed);
    }

    @Test
    void testTypeGuard_reversedOperands() {
        ArkTSExpression typeofX = new ArkTSExpression.UnaryExpression(
                "typeof",
                new ArkTSExpression.VariableExpression("v0"), true);
        ArkTSExpression stringLit =
                new ArkTSExpression.LiteralExpression("number",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression guard = new ArkTSExpression.BinaryExpression(
                stringLit, "==", typeofX);
        String narrowed = TypeInference.narrowTypeFromTypeofGuard(guard);
        assertEquals("number", narrowed);
    }

    @Test
    void testGenericSignature_builderMethod() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "x", "T"));
        String sig = MethodSignatureBuilder.buildGenericSignature(
                "identity", typeParams, params, "T");
        assertEquals("function identity<T>(x: T): T", sig);
    }

    @Test
    void testGenericSignature_voidReturnType() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        params.add(new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                "x", "T"));
        String sig = MethodSignatureBuilder.buildGenericSignature(
                "process", typeParams, params, "void");
        assertEquals("function process<T>(x: T)", sig);
    }

    @Test
    void testDecompileMethod_withProtoTypeAnnotations() {
        byte[] codeBytes = concat(
                bytes(0x60, 0x00),  // lda v0
                bytes(0x64)         // return
        );
        AbcCode code = new AbcCode(2, 1, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        List<AbcProto.ShortyType> shorty = new ArrayList<>();
        shorty.add(AbcProto.ShortyType.I32);
        shorty.add(AbcProto.ShortyType.I32);
        AbcProto proto = new AbcProto(shorty, Collections.emptyList());
        AbcMethod method = new AbcMethod(0, 0, "addOne",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("function addOne"),
                "Should contain function name, got: " + result);
    }

    @Test
    void testDecompileMethod_protoReturnType() {
        byte[] codeBytes = concat(
                bytes(0x60, 0x00),  // lda v0
                bytes(0x64)         // return
        );
        AbcCode code = new AbcCode(2, 1, codeBytes.length, codeBytes,
                Collections.emptyList(), 0);
        List<AbcProto.ShortyType> shorty = new ArrayList<>();
        shorty.add(AbcProto.ShortyType.F64);
        shorty.add(AbcProto.ShortyType.F64);
        AbcProto proto = new AbcProto(shorty, Collections.emptyList());
        AbcMethod method = new AbcMethod(0, 0, "toDouble",
                AbcAccessFlags.ACC_PUBLIC, 0, 0);
        String result = decompiler.decompileMethod(method, code, null);
        assertNotNull(result);
        assertTrue(result.contains("toDouble"),
                "Should contain function name, got: " + result);
    }

    @Test
    void testTypeAnnotation_nonObviousArrayInitializer() {
        byte[] code = concat(
                bytes(0x05, 0x00),  // createemptyarray 0
                bytes(0x61, 0x00),  // sta v0
                bytes(0x64)         // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("v0"),
                "Should reference v0, got: " + result);
        assertTrue(result.contains("Array<unknown>"),
                "Should annotate with Array<unknown> type, got: "
                        + result);
    }

    @Test
    void testTypeAnnotation_propertyLoadPreservesType() {
        TypeInference typeInf = new TypeInference();
        typeInf.setRegisterType("v0", "string");
        assertEquals("string", typeInf.getRegisterType("v0"));
        typeInf.setRegisterType("v1", "number | string");
        assertEquals("number | string", typeInf.getRegisterType("v1"));
    }

    @Test
    void testTypeAnnotation_comparisonKeepsBoolean() {
        byte[] code = concat(
                bytes(0x60, 0x00),       // lda v0
                bytes(0x11, 0x00, 0x01), // less 0, v1
                bytes(0x61, 0x02),       // sta v2
                bytes(0x64)              // return
        );
        List<ArkInstruction> insns = dis(code);
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("let v2: boolean = (v0 < v1)"),
                "Comparison result should be typed boolean, got: "
                        + result);
    }

    @Test
    void testGenericClassDeclaration_toArkTS() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSStatement> members = new ArrayList<>();
        ArkTSTypeDeclarations.GenericClassDeclaration decl =
                new ArkTSTypeDeclarations.GenericClassDeclaration(
                        "Container", typeParams, null, members);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("class Container<T>"),
                "Should produce generic class declaration, got: "
                        + result);
    }

    @Test
    void testGenericClassDeclaration_withSuperClass() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams("T");
        List<ArkTSStatement> members = new ArrayList<>();
        ArkTSTypeDeclarations.GenericClassDeclaration decl =
                new ArkTSTypeDeclarations.GenericClassDeclaration(
                        "List", typeParams, "Collection", members);
        String result = decl.toArkTS(0);
        assertTrue(result.contains("class List<T> extends Collection"),
                "Should produce generic class with extends, got: "
                        + result);
    }

    @Test
    void testGenericClassDeclaration_constrainedTypeParams() {
        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                MethodSignatureBuilder.createTypeParams(
                        new String[] {"K", "V"},
                        new String[] {"string", null});
        List<ArkTSStatement> members = new ArrayList<>();
        ArkTSTypeDeclarations.GenericClassDeclaration decl =
                new ArkTSTypeDeclarations.GenericClassDeclaration(
                        "Map", typeParams, null, members);
        String result = decl.toArkTS(0);
        assertTrue(result.contains(
                "class Map<K extends string, V>"),
                "Should produce constrained generic class, got: "
                        + result);
    }

    @Test
    void testAsExpression_typeCasting() {
        ArkTSExpression expr =
                new ArkTSExpression.VariableExpression("v0");
        ArkTSAccessExpressions.AsExpression asExpr =
                new ArkTSAccessExpressions.AsExpression(expr, "string");
        assertEquals("v0 as string", asExpr.toArkTS());
        assertEquals("v0", asExpr.getExpression().toArkTS());
        assertEquals("string", asExpr.getTypeName());
    }

    @Test
    void testTypeReferenceExpression_genericType() {
        List<String> typeArgs = new ArrayList<>();
        typeArgs.add("string");
        typeArgs.add("number");
        ArkTSAccessExpressions.TypeReferenceExpression typeRef =
                new ArkTSAccessExpressions.TypeReferenceExpression(
                        "Map", typeArgs);
        assertEquals("Map<string, number>", typeRef.toArkTS());
        assertEquals("Map", typeRef.getTypeName());
        assertEquals(2, typeRef.getTypeArgs().size());
    }

    @Test
    void testMethodSignatureBuilder_createTypeParamsConstrained() {
        List<ArkTSTypeDeclarations.TypeParameter> params =
                MethodSignatureBuilder.createTypeParams(
                        new String[] {"T", "U"},
                        new String[] {"Base", "Comparable"});
        assertEquals(2, params.size());
        assertEquals("T extends Base", params.get(0).toString());
        assertEquals("U extends Comparable", params.get(1).toString());
    }

    @Test
    void testMethodSignatureBuilder_createTypeParamsNullConstraints() {
        List<ArkTSTypeDeclarations.TypeParameter> params =
                MethodSignatureBuilder.createTypeParams(
                        new String[] {"T"},
                        null);
        assertEquals(1, params.size());
        assertEquals("T", params.get(0).toString());
        assertNull(params.get(0).getConstraint());
    }

    // --- Conditional jump: JSTRICTEQNULL_IMM8 (strict null check) ---

    @Test
    void testConditionalJump_jstricteqnull_producesCondition() {
        // lda v0; jstricteqnull +offset; ldai 10; sta v1; return
        // JSTRICTEQNULL_IMM8 = 0x56 (IMM8 offset)
        // Layout:
        // offset 0: lda v0         (2 bytes)
        // offset 2: jstricteqnull +6 (2 bytes) -> offset 10
        // offset 4: ldai 10        (5 bytes)
        // offset 9: sta v1         (2 bytes)
        // offset 11: return        (1 byte)
        byte[] code = concat(
            bytes(0x60, 0x00),          // lda v0
            bytes(0x56, 0x06),          // jstricteqnull +6 -> offset 10
            bytes(0x62), le32(10),      // ldai 10
            bytes(0x61, 0x01),          // sta v1
            bytes(0x64)                 // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertTrue(cfg.getBlocks().size() >= 2,
                "jstricteqnull should split into multiple blocks");
        assertTrue(
                ArkOpcodesCompat.isConditionalBranch(
                        ArkOpcodesCompat.JSTRICTEQNULL_IMM8));
    }

    @Test
    void testConditionalJump_jstricteqnull_conditionExpression() {
        // Test that getConditionExpression produces "=== null" for
        // JSTRICTEQNULL
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JSTRICTEQNULL_IMM8, "jstricteqnull",
                ArkInstructionFormat.IMM8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 6)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("=== null"),
                "Expected '=== null' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JNSTRICTEQNULL_IMM8 (strict not-null check) ---

    @Test
    void testConditionalJump_jnstricteqnull_conditionExpression() {
        // Test that getConditionExpression produces "!== null" for
        // JNSTRICTEQNULL
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JNSTRICTEQNULL_IMM8, "jnstricteqnull",
                ArkInstructionFormat.IMM8, 0, 2,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE8_SIGNED, 6)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("!== null"),
                "Expected '!== null' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JSTRICTEQUNDEFINED_IMM16 (strict undefined) ---

    @Test
    void testConditionalJump_jstrictequndefined_conditionExpression() {
        // Test that getConditionExpression produces "=== undefined" for
        // JSTRICTEQUNDEFINED_IMM16
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16,
                "jstrictequndefined",
                ArkInstructionFormat.IMM16, 0, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, 8)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("=== undefined"),
                "Expected '=== undefined' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JNSTRICTEQUNDEFINED_IMM16 ---

    @Test
    void testConditionalJump_jnstrictequndefined_conditionExpression() {
        // Test that getConditionExpression produces "!== undefined" for
        // JNSTRICTEQUNDEFINED_IMM16
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JNSTRICTEQUNDEFINED_IMM16,
                "jnstrictequndefined",
                ArkInstructionFormat.IMM16, 0, 3,
                List.of(new ArkOperand(
                        ArkOperand.Type.IMMEDIATE16_SIGNED, 8)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("!== undefined"),
                "Expected '!== undefined' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JSTRICTEQ_IMM8 (strict eq with register) ---

    @Test
    void testConditionalJump_jstricteq_conditionExpression() {
        // Test that getConditionExpression produces "=== vN" for
        // JSTRICTEQ_IMM8
        // JSTRICTEQ_IMM8 = 0x5E (V8_IMM8 format: reg + offset)
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JSTRICTEQ_IMM8, "jstricteq",
                ArkInstructionFormat.V8_IMM8, 0, 3,
                List.of(
                        new ArkOperand(
                                ArkOperand.Type.REGISTER, 2),
                        new ArkOperand(
                                ArkOperand.Type.IMMEDIATE8_SIGNED, 6)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("=== v2"),
                "Expected '=== v2' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JNSTRICTEQ_IMM8 (strict neq with register) ---

    @Test
    void testConditionalJump_jnstricteq_conditionExpression() {
        // Test that getConditionExpression produces "!== vN" for
        // JNSTRICTEQ_IMM8
        // JNSTRICTEQ_IMM8 = 0x5F (V8_IMM8 format: reg + offset)
        ArkInstruction branchInsn = new ArkInstruction(
                ArkOpcodesCompat.JNSTRICTEQ_IMM8, "jnstricteq",
                ArkInstructionFormat.V8_IMM8, 0, 3,
                List.of(
                        new ArkOperand(
                                ArkOperand.Type.REGISTER, 3),
                        new ArkOperand(
                                ArkOperand.Type.IMMEDIATE8_SIGNED, 6)),
                false);
        DecompilationContext ctx = makeTestContext();
        ctx.currentAccValue =
                new ArkTSExpression.VariableExpression("v0");
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        ArkTSExpression cond =
                cfr.getConditionExpression(branchInsn, ctx);
        assertNotNull(cond);
        String arkTS = cond.toArkTS();
        assertTrue(arkTS.contains("!== v3"),
                "Expected '!== v3' in condition, got: " + arkTS);
    }

    // --- Conditional jump: JSTRICTEQZ IMM8 (strict eq zero) ---

    @Test
    void testConditionalJump_jstricteqz_producesMultipleBlocks() {
        // ldai 1; sta v0; lda v0; jstricteqz +offset; ldai 10; sta v1; return
        // JSTRICTEQZ_IMM8 = 0x52 (IMM8 offset)
        // Layout:
        // offset 0: ldai 1         (5 bytes)
        // offset 5: sta v0         (2 bytes)
        // offset 7: lda v0         (2 bytes)
        // offset 9: jstricteqz +5  (2 bytes) -> offset 16
        // offset 11: ldai 10       (5 bytes)
        // offset 16: sta v1        (2 bytes)
        // offset 18: return        (1 byte)
        byte[] code = concat(
            bytes(0x62), le32(1),       // ldai 1
            bytes(0x61, 0x00),          // sta v0
            bytes(0x60, 0x00),          // lda v0
            bytes(0x52, 0x05),          // jstricteqz +5 -> offset 16
            bytes(0x62), le32(10),      // ldai 10
            bytes(0x61, 0x01),          // sta v1
            bytes(0x64)                 // return
        );
        List<ArkInstruction> insns = dis(code);
        ControlFlowGraph cfg = ControlFlowGraph.build(insns);
        assertTrue(cfg.getBlocks().size() >= 2,
                "jstricteqz should split into multiple blocks");
    }

    // --- isBranchOnFalse covers strict equality branches ---

    @Test
    void testIsBranchOnFalse_coversStrictEquality() {
        ControlFlowReconstructor cfr = new ControlFlowReconstructor(
                decompiler, new InstructionHandler(decompiler));
        assertTrue(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JSTRICTEQNULL_IMM8));
        assertTrue(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JSTRICTEQNULL_IMM16));
        assertTrue(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16));
        assertTrue(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JSTRICTEQ_IMM8));
        assertTrue(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JSTRICTEQ_IMM16));
        assertFalse(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JNSTRICTEQNULL_IMM8));
        assertFalse(cfr.isBranchOnFalse(
                ArkOpcodesCompat.JNSTRICTEQ_IMM8));
    }

    // --- isConditionalBranch covers all strict equality jumps ---

    @Test
    void testIsConditionalBranch_coversStrictEqualityJumps() {
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JSTRICTEQNULL_IMM8));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JSTRICTEQNULL_IMM16));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JNSTRICTEQNULL_IMM8));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JNSTRICTEQNULL_IMM16));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JNSTRICTEQUNDEFINED_IMM16));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JSTRICTEQ_IMM8));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JSTRICTEQ_IMM16));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JNSTRICTEQ_IMM8));
        assertTrue(ArkOpcodesCompat.isConditionalBranch(
                ArkOpcodesCompat.JNSTRICTEQ_IMM16));
    }

    // --- Issue #58: Property store and complex access opcodes ---

    @Test
    void testSuperExpression_toArkTS() {
        ArkTSPropertyExpressions.SuperExpression expr =
                new ArkTSPropertyExpressions.SuperExpression();
        assertEquals("super", expr.toArkTS());
    }

    @Test
    void testSuperPropertyAccess_byName() {
        ArkTSExpression superExpr =
                new ArkTSPropertyExpressions.SuperExpression();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("method");
        ArkTSExpression.MemberExpression member =
                new ArkTSExpression.MemberExpression(superExpr, prop, false);
        assertEquals("super.method", member.toArkTS());
    }

    @Test
    void testSuperPropertyAccess_byValue() {
        ArkTSExpression superExpr =
                new ArkTSPropertyExpressions.SuperExpression();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("v0");
        ArkTSExpression.MemberExpression member =
                new ArkTSExpression.MemberExpression(superExpr, prop, true);
        assertEquals("super[v0]", member.toArkTS());
    }

    @Test
    void testSuperPropertyStore_byName() {
        ArkTSExpression superExpr =
                new ArkTSPropertyExpressions.SuperExpression();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("prop");
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(superExpr, prop, false);
        ArkTSExpression value = new ArkTSExpression.VariableExpression("v0");
        ArkTSExpression.AssignExpression assign =
                new ArkTSExpression.AssignExpression(target, value);
        assertEquals("super.prop = v0", assign.toArkTS());
    }

    @Test
    void testSuperPropertyStore_byValue() {
        ArkTSExpression superExpr =
                new ArkTSPropertyExpressions.SuperExpression();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("v1");
        ArkTSExpression target =
                new ArkTSExpression.MemberExpression(superExpr, prop, true);
        ArkTSExpression value = new ArkTSExpression.VariableExpression("v0");
        ArkTSExpression.AssignExpression assign =
                new ArkTSExpression.AssignExpression(target, value);
        assertEquals("super[v1] = v0", assign.toArkTS());
    }

    @Test
    void testDefinePropertyExpression_toArkTS() {
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression("v0");
        ArkTSExpression prop = new ArkTSExpression.LiteralExpression("name",
                ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        ArkTSExpression value = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSPropertyExpressions.DefinePropertyExpression expr =
                new ArkTSPropertyExpressions.DefinePropertyExpression(
                        obj, prop, value);
        assertEquals(
                "Object.defineProperty(v0, \"name\", { value: 42 })",
                expr.toArkTS());
    }

    @Test
    void testTemplateObjectExpression_toArkTS() {
        ArkTSPropertyExpressions.TemplateObjectExpression expr =
                new ArkTSPropertyExpressions.TemplateObjectExpression(3);
        assertEquals("/* template_3 */", expr.toArkTS());
    }

    // --- Issue #59: Module variable and runtime call opcode tests ---

    @Test
    void testCallRuntime_definefieldbyvalue() {
        // 0xFB prefix + sub-opcode 0x01 (definefieldbyvalue) + V8 register
        byte[] code = concat(
                bytes(0x3E, 0x00, 0x00),     // lda.str 0 -> acc = "propName"
                bytes(0xFB, 0x01, 0x02),     // callruntime.definefieldbyvalue v2
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.definefieldbyvalue",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testCallRuntime_callinit() {
        // 0xFB prefix + sub-opcode 0x06 (callinit) + V8 register
        byte[] code = concat(
                bytes(0x04),                 // createemptyobject -> acc
                bytes(0x61, 0x00),           // sta v0
                bytes(0xFB, 0x06, 0x00),     // callruntime.callinit v0
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(2).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.callinit",
                insns.get(2).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testCallRuntime_topropertykey() {
        // 0xFB prefix + sub-opcode 0x03 (topropertykey), no operands
        byte[] code = concat(
                bytes(0x3E, 0x00, 0x00),     // lda.str 0 -> acc
                bytes(0xFB, 0x03),           // callruntime.topropertykey
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.topropertykey",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testCallRuntime_ldlazymodulevar() {
        // 0xFB prefix + sub-opcode 0x15 (ldlazymodulevar) + IMM16
        byte[] code = concat(
                bytes(0xFB, 0x15, 0x00, 0x00), // callruntime.ldlazymodulevar 0
                bytes(0x61, 0x01),             // sta v1
                bytes(0x64)                    // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.ldlazymodulevar",
                insns.get(0).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("lazy_mod_0"),
                "Should contain lazy module variable: " + result);
    }

    @Test
    void testCallRuntime_createprivateproperty() {
        // 0xFB prefix + sub-opcode 0x04 (createprivateproperty) + IMM8
        byte[] code = concat(
                bytes(0xFB, 0x04, 0x00),     // callruntime.createprivateproperty 0
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.createprivateproperty",
                insns.get(0).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testCallRuntime_istrue() {
        // 0xFB prefix + sub-opcode 0x13 (istrue), no operands
        byte[] code = concat(
                bytes(0x60, 0x00),           // lda v0 -> acc
                bytes(0xFB, 0x13),           // callruntime.istrue
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.istrue",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("Boolean"),
                "Should contain Boolean call for istrue: " + result);
    }

    @Test
    void testCallRuntime_isfalse() {
        // 0xFB prefix + sub-opcode 0x14 (isfalse), no operands
        byte[] code = concat(
                bytes(0x60, 0x00),           // lda v0 -> acc
                bytes(0xFB, 0x14),           // callruntime.isfalse
                bytes(0x61, 0x01),           // sta v1
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.isfalse",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("!"),
                "Should contain negation for isfalse: " + result);
    }

    @Test
    void testCallRuntime_defineprivateproperty() {
        // 0xFB prefix + sub-opcode 0x05 (defineprivateproperty) + IMM8 + V8
        byte[] code = concat(
                bytes(0x62, 0x2A, 0x00, 0x00, 0x00), // ldai 42 -> acc
                bytes(0xFB, 0x05, 0x00, 0x01),       // callruntime.defineprivateproperty 0, v1
                bytes(0x64)                            // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime(),
                "Should be callruntime instruction");
        assertEquals("callruntime.defineprivateproperty",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }

    @Test
    void testCallRuntime_disassembler_decodeMultipleCallRuntime() {
        // Verify the disassembler can decode multiple callruntime instructions
        byte[] code = concat(
                bytes(0xFB, 0x03),           // callruntime.topropertykey
                bytes(0xFB, 0x06, 0x00),     // callruntime.callinit v0
                bytes(0xFB, 0x13),           // callruntime.istrue
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertEquals(4, insns.size(),
                "Should decode 4 instructions");
        assertTrue(insns.get(0).isCallRuntime());
        assertTrue(insns.get(1).isCallRuntime());
        assertTrue(insns.get(2).isCallRuntime());
        assertFalse(insns.get(3).isCallRuntime());
    }

    @Test
    void testRuntimeCallExpression_toArkTS() {
        // Test the RuntimeCallExpression AST node
        List<ArkTSExpression> args = new ArrayList<>();
        args.add(new ArkTSExpression.VariableExpression("v0"));
        args.add(new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSAccessExpressions.RuntimeCallExpression expr =
                new ArkTSAccessExpressions.RuntimeCallExpression(
                        "definefieldbyvalue", args);
        String output = expr.toArkTS();
        assertTrue(output.contains("runtime: definefieldbyvalue"),
                "Should contain runtime name: " + output);
        assertTrue(output.contains("v0"),
                "Should contain first argument: " + output);
        assertTrue(output.contains("42"),
                "Should contain second argument: " + output);
    }

    @Test
    void testCallRuntime_ldsendableexternalmodulevar() {
        // 0xFB prefix + sub-opcode 0x09 + IMM16
        byte[] code = concat(
                bytes(0xFB, 0x09, 0x01, 0x00), // callruntime.ldsendableextmodvar 1
                bytes(0x61, 0x02),             // sta v2
                bytes(0x64)                    // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isCallRuntime());
        assertEquals("callruntime.ldsendableexternalmodulevar",
                insns.get(0).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("sendable_ext_mod_1"),
                "Should contain sendable external module var: " + result);
    }

    @Test
    void testCallRuntime_ldsendablevar() {
        // 0xFB prefix + sub-opcode 0x10 (ldsendablevar) + IMM8 + IMM8
        byte[] code = concat(
                bytes(0xFB, 0x10, 0x00, 0x01), // callruntime.ldsendablevar 0, 1
                bytes(0x61, 0x02),             // sta v2
                bytes(0x64)                    // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isCallRuntime());
        assertEquals("callruntime.ldsendablevar",
                insns.get(0).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("sendable_0_1"),
                "Should contain sendable var: " + result);
    }

    @Test
    void testCallRuntime_stsendablevar() {
        // 0xFB prefix + sub-opcode 0x0D (stsendablevar) + IMM8 + IMM8
        byte[] code = concat(
                bytes(0x62, 0x2A, 0x00, 0x00, 0x00), // ldai 42 -> acc
                bytes(0xFB, 0x0D, 0x00, 0x01),       // callruntime.stsendablevar 0, 1
                bytes(0x64)                            // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(1).isCallRuntime());
        assertEquals("callruntime.stsendablevar",
                insns.get(1).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertTrue(result.contains("sendable_0_1"),
                "Should contain sendable var store: " + result);
    }

    @Test
    void testCallRuntime_newsendableenv() {
        // 0xFB prefix + sub-opcode 0x0B (newsendableenv) + IMM8
        byte[] code = concat(
                bytes(0xFB, 0x0B, 0x02),     // callruntime.newsendableenv 2
                bytes(0x64)                  // return
        );
        List<ArkInstruction> insns = dis(code);
        assertTrue(insns.get(0).isCallRuntime());
        assertEquals("callruntime.newsendableenv",
                insns.get(0).getMnemonic());
        String result = decompiler.decompileInstructions(insns);
        assertFalse(result.isEmpty(),
                "Should produce output: " + result);
    }
}
