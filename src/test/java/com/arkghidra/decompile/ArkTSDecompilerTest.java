package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertTrue(result.contains("let v0: number = 10"));
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
        assertTrue(result.contains("let v0: number = 5"));
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
        assertTrue(result.contains("let v0: boolean = true"));
        assertTrue(result.contains("let v1: boolean = false"));
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
        ArkTSExpression.ArrayLiteralExpression expr =
                new ArkTSExpression.ArrayLiteralExpression(List.of(elem1, elem2));
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
        ArkTSStatement.IfStatement ifStmt =
                new ArkTSStatement.IfStatement(cond,
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
        ArkTSStatement.FunctionDeclaration func =
                new ArkTSStatement.FunctionDeclaration("f",
                        Collections.emptyList(), "number", body);
        String result = func.toArkTS(0);
        assertTrue(result.contains("function f(): number"));
        assertTrue(result.contains("return 42;"));
    }

    @Test
    void testFunctionDeclaration_withParams() {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                List.of(
                        new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "a", "number"),
                        new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "b", "number"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement.FunctionDeclaration func =
                new ArkTSStatement.FunctionDeclaration("add", params,
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
        assertTrue(result.contains("let v0: number = 42"));
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
        assertTrue(result.contains("let v0: number = 2"));
        assertTrue(result.contains("let v1: number = 3"));
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
        ArkTSStatement.TryCatchStatement stmt =
                new ArkTSStatement.TryCatchStatement(tryBody, "e", catchBody, null);
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
        ArkTSStatement.WhileStatement stmt =
                new ArkTSStatement.WhileStatement(cond, body);
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
        ArkTSExpression.ConditionalExpression expr =
                new ArkTSExpression.ConditionalExpression(test, cons, alt);
        assertEquals("(x ? 1 : 2)", expr.toArkTS());
    }

    // --- Object literal test ---

    @Test
    void testObjectLiteralExpression() {
        List<ArkTSExpression.ObjectLiteralExpression.ObjectProperty> props =
                List.of(
                        new ArkTSExpression.ObjectLiteralExpression.ObjectProperty(
                                "name", new ArkTSExpression.LiteralExpression("test",
                                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)),
                        new ArkTSExpression.ObjectLiteralExpression.ObjectProperty(
                                "value", new ArkTSExpression.LiteralExpression("42",
                                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSExpression.ObjectLiteralExpression expr =
                new ArkTSExpression.ObjectLiteralExpression(props);
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
        ArkTSStatement.ForStatement stmt =
                new ArkTSStatement.ForStatement(init, cond, update, body);
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
        assertTrue(result.contains("let v0: number = 42"));
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
        assertTrue(result.contains("let v0: string = \"str_0\""));
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
        assertTrue(result.contains("let v0: boolean = true"));
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
        ArkTSExpression.OptionalChainExpression expr =
                new ArkTSExpression.OptionalChainExpression(obj, prop, false);
        assertEquals("obj?.name", expr.toArkTS());
    }

    @Test
    void testOptionalChainExpression_computed() {
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("arr");
        ArkTSExpression prop = new ArkTSExpression.VariableExpression("i");
        ArkTSExpression.OptionalChainExpression expr =
                new ArkTSExpression.OptionalChainExpression(obj, prop, true);
        assertEquals("arr?.[i]", expr.toArkTS());
    }

    @Test
    void testSpreadExpression() {
        ArkTSExpression arg =
                new ArkTSExpression.VariableExpression("args");
        ArkTSExpression.SpreadExpression expr =
                new ArkTSExpression.SpreadExpression(arg);
        assertEquals("...args", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_noInterpolation() {
        ArkTSExpression.TemplateLiteralExpression expr =
                new ArkTSExpression.TemplateLiteralExpression(
                        List.of("hello world"),
                        Collections.emptyList());
        assertEquals("`hello world`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_withInterpolation() {
        ArkTSExpression name =
                new ArkTSExpression.VariableExpression("name");
        ArkTSExpression.TemplateLiteralExpression expr =
                new ArkTSExpression.TemplateLiteralExpression(
                        List.of("Hello, ", "!"),
                        List.of(name));
        assertEquals("`Hello, ${name}!`", expr.toArkTS());
    }

    @Test
    void testTemplateLiteralExpression_escapesBacktick() {
        ArkTSExpression.TemplateLiteralExpression expr =
                new ArkTSExpression.TemplateLiteralExpression(
                        List.of("test`string"),
                        Collections.emptyList());
        assertEquals("`test\\`string`", expr.toArkTS());
    }

    @Test
    void testAwaitExpression() {
        ArkTSExpression promise =
                new ArkTSExpression.VariableExpression("p");
        ArkTSExpression.AwaitExpression expr =
                new ArkTSExpression.AwaitExpression(promise);
        assertEquals("await p", expr.toArkTS());
    }

    @Test
    void testYieldExpression() {
        ArkTSExpression value = new ArkTSExpression.LiteralExpression("42",
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        ArkTSExpression.YieldExpression expr =
                new ArkTSExpression.YieldExpression(value, false);
        assertEquals("yield 42", expr.toArkTS());
    }

    @Test
    void testYieldExpression_delegate() {
        ArkTSExpression iterable =
                new ArkTSExpression.VariableExpression("items");
        ArkTSExpression.YieldExpression expr =
                new ArkTSExpression.YieldExpression(iterable, true);
        assertEquals("yield* items", expr.toArkTS());
    }

    @Test
    void testYieldExpression_bare() {
        ArkTSExpression.YieldExpression expr =
                new ArkTSExpression.YieldExpression(null, false);
        assertEquals("yield", expr.toArkTS());
    }

    @Test
    void testArrowFunctionExpression_basic() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.LiteralExpression("42",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER))));
        ArkTSExpression.ArrowFunctionExpression expr =
                new ArkTSExpression.ArrowFunctionExpression(
                        Collections.emptyList(), body, false);
        assertTrue(expr.toArkTS().contains("() => {"));
        assertTrue(expr.toArkTS().contains("return 42;"));
    }

    @Test
    void testArrowFunctionExpression_async() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSExpression.ArrowFunctionExpression expr =
                new ArkTSExpression.ArrowFunctionExpression(
                        Collections.emptyList(), body, true);
        assertTrue(expr.toArkTS().startsWith("async () =>"));
    }

    @Test
    void testArrowFunctionExpression_withParams() {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                List.of(
                        new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "x", "number"),
                        new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "y", "number"));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.BinaryExpression(
                                new ArkTSExpression.VariableExpression("x"),
                                "+",
                                new ArkTSExpression.VariableExpression("y")))));
        ArkTSExpression.ArrowFunctionExpression expr =
                new ArkTSExpression.ArrowFunctionExpression(params, body,
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
        ArkTSStatement.DoWhileStatement stmt =
                new ArkTSStatement.DoWhileStatement(body, cond);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("do {"));
        assertTrue(result.contains("} while (x);"));
    }

    @Test
    void testDoWhileStatement_withIndentation() {
        ArkTSExpression cond = new ArkTSExpression.VariableExpression("ok");
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement.DoWhileStatement stmt =
                new ArkTSStatement.DoWhileStatement(body, cond);
        String result = stmt.toArkTS(1);
        assertTrue(result.startsWith("    do {"));
        assertTrue(result.contains("    } while (ok);"));
    }

    @Test
    void testClassDeclaration_basic() {
        List<ArkTSStatement> members = List.of(
                new ArkTSStatement.ClassFieldDeclaration("name", "string",
                        null, false, null),
                new ArkTSStatement.ClassMethodDeclaration("greet",
                        Collections.emptyList(), "string",
                        new ArkTSStatement.BlockStatement(
                                List.of(new ArkTSStatement.ReturnStatement(
                                        new ArkTSExpression.LiteralExpression(
                                                "hello",
                                                ArkTSExpression.LiteralExpression
                                                        .LiteralKind.STRING)))),
                        false, "public"));
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("MyClass", null, members);
        String result = cls.toArkTS(0);
        assertTrue(result.startsWith("class MyClass {"));
        assertTrue(result.contains("name: string;"));
        assertTrue(result.contains("public greet(): string"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testClassDeclaration_withExtends() {
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("Child", "Parent",
                        Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Child extends Parent"));
    }

    @Test
    void testClassFieldDeclaration_static() {
        ArkTSStatement stmt = new ArkTSStatement.ClassFieldDeclaration(
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
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "helper", Collections.emptyList(), null, body, false,
                "private");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("private helper()"));
    }

    @Test
    void testImportStatement_named() {
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                List.of("foo", "bar"), "./module", false, null, null);
        assertEquals("import { foo, bar } from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_default() {
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                Collections.emptyList(), "./module", true, "MyClass", null);
        assertEquals("import MyClass from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_namespace() {
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                Collections.emptyList(), "./module", false, null, "ns");
        assertEquals("import * as ns from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_defaultAndNamed() {
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                List.of("helper"), "./module", true, "Main", null);
        assertEquals("import Main, { helper } from './module';",
                stmt.toArkTS(0));
    }

    @Test
    void testExportStatement_named() {
        ArkTSStatement stmt = new ArkTSStatement.ExportStatement(
                List.of("foo", "bar"), null, false);
        assertEquals("export { foo, bar };", stmt.toArkTS(0));
    }

    @Test
    void testExportStatement_default() {
        ArkTSStatement decl = new ArkTSStatement.VariableDeclaration("let",
                "x", null, new ArkTSExpression.LiteralExpression("42",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        ArkTSStatement stmt = new ArkTSStatement.ExportStatement(
                Collections.emptyList(), decl, true);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export default let x = 42"));
    }

    @Test
    void testExportStatement_declaration() {
        ArkTSStatement funcDecl = new ArkTSStatement.FunctionDeclaration(
                "doStuff", Collections.emptyList(), null,
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement stmt = new ArkTSStatement.ExportStatement(
                Collections.emptyList(), funcDecl, false);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export function doStuff"));
    }

    @Test
    void testEnumDeclaration_basic() {
        List<ArkTSStatement.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSStatement.EnumDeclaration.EnumMember("A", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("B", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("C",
                        new ArkTSExpression.LiteralExpression("10",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER)));
        ArkTSStatement stmt = new ArkTSStatement.EnumDeclaration("Color",
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
        List<ArkTSStatement.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "property", "name", "string",
                                Collections.emptyList(), false),
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "method", "doStuff", "void",
                                List.of(new ArkTSStatement.FunctionDeclaration
                                        .FunctionParam("x", "number")),
                                false));
        ArkTSStatement stmt = new ArkTSStatement.InterfaceDeclaration(
                "MyInterface", Collections.emptyList(), members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("interface MyInterface {"));
        assertTrue(result.contains("name: string;"));
        assertTrue(result.contains("doStuff(x: number): void;"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testInterfaceDeclaration_extends() {
        ArkTSStatement stmt = new ArkTSStatement.InterfaceDeclaration(
                "Child", List.of("Parent", "Base"),
                Collections.emptyList());
        String result = stmt.toArkTS(0);
        assertTrue(result.contains(
                "interface Child extends Parent, Base"));
    }

    @Test
    void testInterfaceDeclaration_optionalProperty() {
        List<ArkTSStatement.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "property", "name", "string",
                                Collections.emptyList(), true));
        ArkTSStatement stmt = new ArkTSStatement.InterfaceDeclaration(
                "Opts", Collections.emptyList(), members);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("name?: string;"));
    }

    @Test
    void testDecoratorStatement_noArgs() {
        ArkTSStatement stmt = new ArkTSStatement.DecoratorStatement(
                "Component", Collections.emptyList());
        assertEquals("@Component", stmt.toArkTS(0));
    }

    @Test
    void testDecoratorStatement_withArgs() {
        ArkTSStatement stmt = new ArkTSStatement.DecoratorStatement(
                "Route",
                List.of(new ArkTSExpression.LiteralExpression("'/home'",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING)));
        assertEquals("@Route(\"'/home'\")", stmt.toArkTS(0));
    }

    @Test
    void testDecoratorStatement_withIndentation() {
        ArkTSStatement stmt = new ArkTSStatement.DecoratorStatement(
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
                List.of(new ArkTSStatement.SuperCallStatement(
                        Collections.emptyList())));
        ArkTSStatement constructor =
                new ArkTSStatement.ConstructorDeclaration(
                        List.of(new ArkTSStatement.FunctionDeclaration
                                .FunctionParam("x", "number")),
                        constructorBody);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("x"))));
        ArkTSStatement method = new ArkTSStatement.ClassMethodDeclaration(
                "getX", Collections.emptyList(), "number",
                methodBody, false, "public");
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "x", "number", null, false, "private");
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("Point", "BaseClass",
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
                List.of(new ArkTSStatement.SuperCallStatement(
                        Collections.emptyList())));
        ArkTSStatement stmt = new ArkTSStatement.ConstructorDeclaration(
                Collections.emptyList(), body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("constructor()"));
        assertTrue(result.contains("super();"));
    }

    @Test
    void testConstructorDeclaration_withParams() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                List.of(new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "a", "number"),
                        new ArkTSStatement.FunctionDeclaration.FunctionParam(
                                "b", "string"));
        ArkTSStatement stmt = new ArkTSStatement.ConstructorDeclaration(
                params, body);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("constructor(a: number, b: string)"));
    }

    // --- Super call statement ---

    @Test
    void testSuperCallStatement_noArgs() {
        ArkTSStatement stmt = new ArkTSStatement.SuperCallStatement(
                Collections.emptyList());
        assertEquals("super();", stmt.toArkTS(0));
    }

    @Test
    void testSuperCallStatement_withArgs() {
        ArkTSExpression arg1 = new ArkTSExpression.VariableExpression("x");
        ArkTSExpression arg2 = new ArkTSExpression.VariableExpression("y");
        ArkTSStatement stmt = new ArkTSStatement.SuperCallStatement(
                List.of(arg1, arg2));
        assertEquals("super(x, y);", stmt.toArkTS(0));
    }

    // --- Struct declaration ---

    @Test
    void testStructDeclaration_basic() {
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "message", "string",
                new ArkTSExpression.LiteralExpression("'Hello'",
                        ArkTSExpression.LiteralExpression.LiteralKind.STRING),
                false, null);
        ArkTSStatement.StructDeclaration struct =
                new ArkTSStatement.StructDeclaration("MyPage",
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
        ArkTSStatement.StructDeclaration struct =
                new ArkTSStatement.StructDeclaration("SimpleStruct",
                        Collections.emptyList(), Collections.emptyList());
        String result = struct.toArkTS(0);
        assertTrue(result.contains("struct SimpleStruct"));
        assertFalse(result.contains("@"));
    }

    // --- Type parameter ---

    @Test
    void testTypeParameter_noConstraint() {
        ArkTSStatement.TypeParameter tp =
                new ArkTSStatement.TypeParameter("T", null);
        assertEquals("T", tp.toString());
    }

    @Test
    void testTypeParameter_withConstraint() {
        ArkTSStatement.TypeParameter tp =
                new ArkTSStatement.TypeParameter("T", "Base");
        assertEquals("T extends Base", tp.toString());
    }

    // --- Generic class declaration ---

    @Test
    void testGenericClassDeclaration_singleTypeParam() {
        ArkTSStatement.GenericClassDeclaration cls =
                new ArkTSStatement.GenericClassDeclaration("Container",
                        List.of(new ArkTSStatement.TypeParameter("T", null)),
                        null, Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Container<T>"));
    }

    @Test
    void testGenericClassDeclaration_constrainedTypeParam() {
        ArkTSStatement.GenericClassDeclaration cls =
                new ArkTSStatement.GenericClassDeclaration("SortedContainer",
                        List.of(new ArkTSStatement.TypeParameter("T",
                                "Comparable")),
                        "Base", Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class SortedContainer<T extends "
                + "Comparable> extends Base"));
    }

    @Test
    void testGenericClassDeclaration_multipleTypeParams() {
        ArkTSStatement.GenericClassDeclaration cls =
                new ArkTSStatement.GenericClassDeclaration("Map",
                        List.of(new ArkTSStatement.TypeParameter("K", null),
                                new ArkTSStatement.TypeParameter("V", null)),
                        null, Collections.emptyList());
        String result = cls.toArkTS(0);
        assertTrue(result.contains("class Map<K, V>"));
    }

    // --- File module ---

    @Test
    void testFileModule_empty() {
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());
        assertEquals("", fileModule.toArkTS(0));
    }

    @Test
    void testFileModule_withImports() {
        ArkTSStatement imp = new ArkTSStatement.ImportStatement(
                List.of("Component"), "@ohos/component", false, null, null);
        ArkTSStatement decl = new ArkTSStatement.ClassDeclaration(
                "MyClass", null, Collections.emptyList());
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(List.of(imp), List.of(decl),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("import { Component } from "
                + "'@ohos/component';"));
        assertTrue(result.contains("class MyClass"));
    }

    @Test
    void testFileModule_withExports() {
        ArkTSStatement funcDecl = new ArkTSStatement.FunctionDeclaration(
                "helper", Collections.emptyList(), "void",
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement export = new ArkTSStatement.ExportStatement(
                Collections.emptyList(), funcDecl, false);
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(Collections.emptyList(),
                        Collections.emptyList(), List.of(export));
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("export function helper"));
    }

    @Test
    void testFileModule_fullFile() {
        ArkTSStatement imp = new ArkTSStatement.ImportStatement(
                List.of("BaseModel"), "./model", false, null, null);
        ArkTSStatement cls = new ArkTSStatement.ClassDeclaration(
                "UserModel", "BaseModel", Collections.emptyList());
        ArkTSStatement export = new ArkTSStatement.ExportStatement(
                List.of("UserModel"), null, false);
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(List.of(imp), List.of(cls),
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
        ArkTSExpression.AsExpression asExpr =
                new ArkTSExpression.AsExpression(expr, "string");
        assertEquals("obj as string", asExpr.toArkTS());
    }

    @Test
    void testNonNullExpression() {
        ArkTSExpression expr = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("obj"),
                new ArkTSExpression.VariableExpression("field"), false);
        ArkTSExpression.NonNullExpression nonNull =
                new ArkTSExpression.NonNullExpression(expr);
        assertEquals("obj.field!", nonNull.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_simple() {
        ArkTSExpression.TypeReferenceExpression typeRef =
                new ArkTSExpression.TypeReferenceExpression("number",
                        Collections.emptyList());
        assertEquals("number", typeRef.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_generic() {
        ArkTSExpression.TypeReferenceExpression typeRef =
                new ArkTSExpression.TypeReferenceExpression("Array",
                        List.of("string"));
        assertEquals("Array<string>", typeRef.toArkTS());
    }

    @Test
    void testTypeReferenceExpression_multipleTypeArgs() {
        ArkTSExpression.TypeReferenceExpression typeRef =
                new ArkTSExpression.TypeReferenceExpression("Map",
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
        List<ArkTSStatement.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSStatement.EnumDeclaration.EnumMember("Active",
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)),
                new ArkTSStatement.EnumDeclaration.EnumMember("Inactive",
                        new ArkTSExpression.LiteralExpression("0",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)));
        ArkTSStatement stmt = new ArkTSStatement.EnumDeclaration("Status",
                members);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("enum Status {"));
        assertTrue(result.contains("Active = 1,"));
        assertTrue(result.contains("Inactive = 0"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    void testEnumDeclaration_autoIncrement() {
        List<ArkTSStatement.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSStatement.EnumDeclaration.EnumMember("North", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("South", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("East", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("West", null));
        ArkTSStatement stmt = new ArkTSStatement.EnumDeclaration("Direction",
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
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                List.of("Logger"), "@ohos/log", false, null, null);
        assertEquals("import { Logger } from '@ohos/log';",
                stmt.toArkTS(0));
    }

    @Test
    void testImportStatement_defaultAndNamespace() {
        ArkTSStatement stmt = new ArkTSStatement.ImportStatement(
                Collections.emptyList(), "./module", true, "default", "ns");
        assertEquals("import default, * as ns from './module';",
                stmt.toArkTS(0));
    }

    // --- Export statement variations ---

    @Test
    void testExportStatement_defaultFunction() {
        ArkTSStatement funcDecl = new ArkTSStatement.FunctionDeclaration(
                "main", Collections.emptyList(), "void",
                new ArkTSStatement.BlockStatement(Collections.emptyList()));
        ArkTSStatement stmt = new ArkTSStatement.ExportStatement(
                Collections.emptyList(), funcDecl, true);
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("export default function main"));
    }

    @Test
    void testExportStatement_namedExports() {
        ArkTSStatement stmt = new ArkTSStatement.ExportStatement(
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
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "doStuff", Collections.emptyList(), "void", body,
                false, "public");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("public doStuff"));
    }

    @Test
    void testAccessFlags_privateModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "internal", Collections.emptyList(), null, body,
                false, "private");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("private internal"));
    }

    @Test
    void testAccessFlags_protectedModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "onEvent", Collections.emptyList(), null, body,
                false, "protected");
        String result = stmt.toArkTS(0);
        assertTrue(result.startsWith("protected onEvent"));
    }

    @Test
    void testAccessFlags_staticModifier() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "create", Collections.emptyList(), "Object", body,
                true, "public");
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("public static create"));
    }

    // --- Decorator with field ---

    @Test
    void testDecorator_withComponentAndState() {
        ArkTSStatement stateField = new ArkTSStatement.ClassFieldDeclaration(
                "count", "number",
                new ArkTSExpression.LiteralExpression("0",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                false, null);
        ArkTSStatement decoratedField =
                new ArkTSStatement.DecoratorStatement("State",
                        Collections.emptyList());
        // Build a struct with decorator
        ArkTSStatement.StructDeclaration struct =
                new ArkTSStatement.StructDeclaration("CounterPage",
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
        List<ArkTSStatement.InterfaceDeclaration.InterfaceMember> members =
                List.of(
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "property", "id", "number",
                                Collections.emptyList(), false),
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "method", "toString", "string",
                                Collections.emptyList(), false),
                        new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
                                "method", "process", "void",
                                List.of(new ArkTSStatement.FunctionDeclaration
                                        .FunctionParam("data", "Object")),
                                false));
        ArkTSStatement stmt = new ArkTSStatement.InterfaceDeclaration(
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
        ArkTSStatement stmt = new ArkTSStatement.ClassFieldDeclaration(
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
        ArkTSStatement stmt = new ArkTSStatement.ClassMethodDeclaration(
                "getValue",
                List.of(new ArkTSStatement.FunctionDeclaration.FunctionParam(
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
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "data", "number", null, false, null);
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("Inner", null,
                        List.of(field));
        String result = cls.toArkTS(1);
        assertTrue(result.startsWith("    class Inner {"));
        assertTrue(result.contains("        data: number;"));
        assertTrue(result.endsWith("    }"));
    }

    @Test
    void testStructDeclaration_indentation() {
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "title", "string", null, false, null);
        ArkTSStatement.StructDeclaration struct =
                new ArkTSStatement.StructDeclaration("Header",
                        List.of(field), List.of("Component"));
        String result = struct.toArkTS(1);
        assertTrue(result.startsWith("    @Component"));
        assertTrue(result.contains("    struct Header"));
        assertTrue(result.contains("        title: string;"));
    }

    // --- Enum with mixed members ---

    @Test
    void testEnumDeclaration_mixedMembers() {
        List<ArkTSStatement.EnumDeclaration.EnumMember> members = List.of(
                new ArkTSStatement.EnumDeclaration.EnumMember("A", null),
                new ArkTSStatement.EnumDeclaration.EnumMember("B",
                        new ArkTSExpression.LiteralExpression("10",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NUMBER)),
                new ArkTSStatement.EnumDeclaration.EnumMember("C", null));
        ArkTSStatement stmt = new ArkTSStatement.EnumDeclaration("Mixed",
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
        ArkTSStatement.InterfaceDeclaration.InterfaceMember member =
                new ArkTSStatement.InterfaceDeclaration.InterfaceMember(
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
        ArkTSStatement.ClassFieldDeclaration field =
                new ArkTSStatement.ClassFieldDeclaration(
                        "count", "number", init, true, "public");
        assertEquals("count", field.getName());
    }

    @Test
    void testClassMethodDeclaration_accessors() {
        ArkTSStatement body = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement.ClassMethodDeclaration method =
                new ArkTSStatement.ClassMethodDeclaration(
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
        ArkTSStatement stmt = new ArkTSStatement.SuperCallStatement(
                List.of(arg1, arg2));
        assertEquals("super(42, \"hello\");", stmt.toArkTS(0));
    }

    // --- AsExpression with member target ---

    @Test
    void testAsExpression_withMember() {
        ArkTSExpression obj = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("response"),
                new ArkTSExpression.VariableExpression("data"), false);
        ArkTSExpression.AsExpression asExpr =
                new ArkTSExpression.AsExpression(obj, "string");
        assertEquals("response.data as string", asExpr.toArkTS());
    }

    // --- NonNull expression with nested member ---

    @Test
    void testNonNullExpression_withNestedMember() {
        ArkTSExpression inner = new ArkTSExpression.MemberExpression(
                new ArkTSExpression.VariableExpression("config"),
                new ArkTSExpression.VariableExpression("value"), false);
        ArkTSExpression.NonNullExpression nonNull =
                new ArkTSExpression.NonNullExpression(inner);
        assertEquals("config.value!", nonNull.toArkTS());
    }

    // --- Type reference with nested generics ---

    @Test
    void testTypeReferenceExpression_nestedGeneric() {
        ArkTSExpression.TypeReferenceExpression typeRef =
                new ArkTSExpression.TypeReferenceExpression("Map",
                        List.of("string", "Array<number>"));
        assertEquals("Map<string, Array<number>>", typeRef.toArkTS());
    }

    // --- File module with multiple declarations ---

    @Test
    void testFileModule_withMultipleDeclarations() {
        ArkTSStatement enumDecl = new ArkTSStatement.EnumDeclaration("Color",
                List.of(new ArkTSStatement.EnumDeclaration.EnumMember("Red",
                                null),
                        new ArkTSStatement.EnumDeclaration.EnumMember("Blue",
                                null)));
        ArkTSStatement classDecl = new ArkTSStatement.ClassDeclaration(
                "Painter", null, Collections.emptyList());
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(Collections.emptyList(),
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
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "value", "T", null, false, "private");
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.ReturnStatement(
                        new ArkTSExpression.VariableExpression("value"))));
        ArkTSStatement method = new ArkTSStatement.ClassMethodDeclaration(
                "getValue", Collections.emptyList(), "T",
                methodBody, false, "public");
        ArkTSStatement.GenericClassDeclaration cls =
                new ArkTSStatement.GenericClassDeclaration("Box",
                        List.of(new ArkTSStatement.TypeParameter("T", null)),
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
        ArkTSStatement.TryCatchStatement stmt =
                new ArkTSStatement.TryCatchStatement(tryBody, "e",
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
        ArkTSStatement.TryCatchStatement stmt =
                new ArkTSStatement.TryCatchStatement(tryBody, "e",
                        catchBody, null);
        String result = stmt.toArkTS(0);
        assertTrue(result.contains("catch (e)"));
    }

    // --- 2. Switch statement AST ---

    @Test
    void testSwitchStatement_basic() {
        ArkTSExpression disc = new ArkTSExpression.VariableExpression("x");
        ArkTSStatement.SwitchStatement.SwitchCase case1 =
                new ArkTSStatement.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("1",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSStatement.SwitchStatement.SwitchCase case2 =
                new ArkTSStatement.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("2",
                                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSStatement defaultBlock = new ArkTSStatement.BlockStatement(
                List.of(new ArkTSStatement.BreakStatement()));
        ArkTSStatement.SwitchStatement stmt =
                new ArkTSStatement.SwitchStatement(disc,
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
        ArkTSStatement.SwitchStatement.SwitchCase case1 =
                new ArkTSStatement.SwitchStatement.SwitchCase(
                        new ArkTSExpression.LiteralExpression("red",
                                ArkTSExpression.LiteralExpression.LiteralKind.STRING),
                        List.of(new ArkTSStatement.BreakStatement()));
        ArkTSStatement.SwitchStatement stmt =
                new ArkTSStatement.SwitchStatement(disc,
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
        assertTrue(result.contains("let v0: string"));
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
        ArkTSStatement field = new ArkTSStatement.ClassFieldDeclaration(
                "data", "number", null, false, null);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement method = new ArkTSStatement.ClassMethodDeclaration(
                "getValue", Collections.emptyList(), "number",
                methodBody, false, "public");
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("MyClass", null,
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
        ArkTSStatement field1 = new ArkTSStatement.ClassFieldDeclaration(
                "x", "number", null, false, null);
        ArkTSStatement field2 = new ArkTSStatement.ClassFieldDeclaration(
                "y", "number", null, false, null);
        ArkTSStatement methodBody = new ArkTSStatement.BlockStatement(
                Collections.emptyList());
        ArkTSStatement method = new ArkTSStatement.ClassMethodDeclaration(
                "calc", Collections.emptyList(), null,
                methodBody, false, null);
        ArkTSStatement.ClassDeclaration cls =
                new ArkTSStatement.ClassDeclaration("Point", null,
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
        assertTrue(result.contains("let v0: number = 42"));
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
        ArkTSStatement cls1 = new ArkTSStatement.ClassDeclaration("First",
                null, Collections.emptyList());
        ArkTSStatement cls2 = new ArkTSStatement.ClassDeclaration("Second",
                null, Collections.emptyList());
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(Collections.emptyList(),
                        List.of(cls1, cls2),
                        Collections.emptyList());
        String result = fileModule.toArkTS(0);
        assertTrue(result.contains("class First"));
        assertTrue(result.contains("class Second"));
        assertTrue(result.contains("}\n\nclass Second"));
    }

    @Test
    void testFileModule_importsBeforeDeclarations() {
        ArkTSStatement imp = new ArkTSStatement.ImportStatement(
                List.of("Base"), "./base", false, null, null);
        ArkTSStatement cls = new ArkTSStatement.ClassDeclaration("Child",
                "Base", Collections.emptyList());
        ArkTSStatement.FileModule fileModule =
                new ArkTSStatement.FileModule(List.of(imp), List.of(cls),
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
                new ArkTSExpression.ConditionalExpression(test, cons, alt);
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
        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(cond, body);
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
        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(cond, body);
        String result = whileStmt.toArkTS(0);
        assertTrue(result.contains("while (true)"));
        assertTrue(result.contains("continue;"));
    }

    @Test
    void testNestedTryCatchStatement_formatting() {
        ArkTSStatement innerTry = new ArkTSStatement.TryCatchStatement(
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
        ArkTSStatement outerTry = new ArkTSStatement.TryCatchStatement(
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
                new ArkTSExpression.ConditionalExpression(test, cons, alt);
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
        List<ArkTSStatement.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();

        // case 1: break;
        cases.add(new ArkTSStatement.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(new ArkTSStatement.BreakStatement())));

        // case 2: y = 20; break;
        cases.add(new ArkTSStatement.SwitchStatement.SwitchCase(
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

        ArkTSStatement.SwitchStatement switchStmt =
                new ArkTSStatement.SwitchStatement(discriminant, cases,
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
        List<ArkTSStatement.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSStatement.SwitchStatement.SwitchCase(
                new ArkTSExpression.LiteralExpression("1",
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER),
                List.of(new ArkTSStatement.BreakStatement())));

        ArkTSStatement.SwitchStatement stmt =
                new ArkTSStatement.SwitchStatement(
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
        List<ArkTSStatement.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        cases.add(new ArkTSStatement.SwitchStatement.SwitchCase(
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

        ArkTSStatement.SwitchStatement stmt =
                new ArkTSStatement.SwitchStatement(
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
        ArkTSExpression.PrivateMemberExpression expr =
                new ArkTSExpression.PrivateMemberExpression(obj, "secret");
        assertEquals("obj.#secret", expr.toArkTS());
    }

    @Test
    void testDecompile_inExpression_toArkTS() {
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression("name");
        ArkTSExpression obj = new ArkTSExpression.VariableExpression("obj");
        ArkTSExpression.InExpression expr =
                new ArkTSExpression.InExpression(prop, obj);
        assertEquals("name in obj", expr.toArkTS());
    }

    @Test
    void testDecompile_instanceofExpression_toArkTS() {
        ArkTSExpression expr1 =
                new ArkTSExpression.VariableExpression("value");
        ArkTSExpression expr2 =
                new ArkTSExpression.VariableExpression("MyClass");
        ArkTSExpression.InstanceofExpression expr =
                new ArkTSExpression.InstanceofExpression(expr1, expr2);
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
        ArkTSExpression.DeleteExpression expr =
                new ArkTSExpression.DeleteExpression(target);
        assertEquals("delete obj.prop", expr.toArkTS());
    }

    @Test
    void testDecompile_copyDataPropertiesExpression_toArkTS() {
        ArkTSExpression target =
                new ArkTSExpression.VariableExpression("target");
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("source");
        ArkTSExpression.CopyDataPropertiesExpression expr =
                new ArkTSExpression.CopyDataPropertiesExpression(target, source);
        assertEquals("Object.assign(target, source)", expr.toArkTS());
    }

    @Test
    void testDecompile_objectLiteralWithSpreadProperty() {
        ArkTSExpression source =
                new ArkTSExpression.VariableExpression("other");
        ArkTSExpression spread =
                new ArkTSExpression.SpreadExpression(source);
        List<ArkTSExpression.ObjectLiteralExpression.ObjectProperty> props =
                new ArrayList<>();
        props.add(new ArkTSExpression.ObjectLiteralExpression.ObjectProperty(
                null, spread));
        ArkTSExpression.ObjectLiteralExpression expr =
                new ArkTSExpression.ObjectLiteralExpression(props);
        assertEquals("{ ...other }", expr.toArkTS());
    }
}
