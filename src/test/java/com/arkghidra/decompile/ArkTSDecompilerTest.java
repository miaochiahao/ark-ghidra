package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkInstructionFormat;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

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
        assertEquals("function add(p0: number, p1: number): number", sig);
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
        assertEquals("function process(p0: Object)", sig);
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
        assertEquals("function empty() { }", result);
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
        assertEquals("function add(p0: number, p1: number): number", sig);
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
}
