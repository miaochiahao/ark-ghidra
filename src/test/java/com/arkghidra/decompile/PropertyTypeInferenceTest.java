package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkInstructionFormat;
import com.arkghidra.disasm.ArkOperand;

/**
 * Tests for property load type inference in {@link TypeInference}.
 *
 * <p>Verifies that property load opcodes (ldobjbyname, ldthisbyname,
 * ldsuperbyname, ldobjbyindex) return meaningful types based on the
 * property name rather than always returning null.
 */
@DisplayName("Property Type Inference")
class PropertyTypeInferenceTest {

    private TypeInference typeInf;
    private DecompilationContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        typeInf = new TypeInference();
        ctx = new DecompilationContext(null, null, null, null, null, null);
        Map<Integer, String> strings = new HashMap<>();
        strings.put(0, "length");
        strings.put(1, "size");
        strings.put(2, "constructor");
        strings.put(3, "prototype");
        strings.put(4, "toString");
        strings.put(5, "valueOf");
        strings.put(6, "message");
        strings.put(7, "name");
        strings.put(8, "stack");
        strings.put(9, "unknownProp");
        strings.put(10, "byteLength");
        strings.put(11, "byteOffset");
        injectStringCache(ctx, strings);
    }

    @SuppressWarnings("unchecked")
    private static void injectStringCache(DecompilationContext context,
            Map<Integer, String> entries) throws Exception {
        Field cacheField = DecompilationContext.class.getDeclaredField(
                "stringResolveCache");
        cacheField.setAccessible(true);
        Map<Integer, String> cache =
                (Map<Integer, String>) cacheField.get(context);
        if (cache == null) {
            cache = new HashMap<>();
            cacheField.set(context, cache);
        }
        cache.putAll(entries);
    }

    private static ArkInstruction makeImm8Imm16Insn(
            int opcode, String mnemonic, int stringIdx) {
        List<ArkOperand> operands = new ArrayList<>();
        operands.add(new ArkOperand(
                ArkOperand.Type.IMMEDIATE8, 0));
        operands.add(new ArkOperand(
                ArkOperand.Type.IMMEDIATE16, stringIdx));
        return new ArkInstruction(opcode, mnemonic,
                ArkInstructionFormat.IMM8_IMM16,
                0, 4, operands, false);
    }

    private static ArkInstruction makeLdobjbyname(int stringIdx) {
        return makeImm8Imm16Insn(0x42, "ldobjbyname", stringIdx);
    }

    private static ArkInstruction makeLdthisbyname(int stringIdx) {
        return makeImm8Imm16Insn(0x49, "ldthisbyname", stringIdx);
    }

    private static ArkInstruction makeLdsuperbyname(int stringIdx) {
        return makeImm8Imm16Insn(0x46, "ldsuperbyname", stringIdx);
    }

    private static ArkInstruction makeLdobjbyindex(int index) {
        return makeImm8Imm16Insn(0x3A, "ldobjbyindex", index);
    }

    private static ArkInstruction makeLdobjbyvalue() {
        List<ArkOperand> operands = new ArrayList<>();
        operands.add(new ArkOperand(
                ArkOperand.Type.IMMEDIATE8, 0));
        operands.add(new ArkOperand(
                ArkOperand.Type.REGISTER, 1));
        return new ArkInstruction(0x37, "ldobjbyvalue",
                ArkInstructionFormat.IMM8_V8,
                0, 3, operands, false);
    }

    @Nested
    @DisplayName("Known property names via ldobjbyname")
    class LdobjbynameKnownProperties {

        @Test
        @DisplayName(".length returns number")
        void testLength() {
            ArkInstruction insn = makeLdobjbyname(0);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".size returns number")
        void testSize() {
            ArkInstruction insn = makeLdobjbyname(1);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".constructor returns Function")
        void testConstructor() {
            ArkInstruction insn = makeLdobjbyname(2);
            assertEquals("Function",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".prototype returns object")
        void testPrototype() {
            ArkInstruction insn = makeLdobjbyname(3);
            assertEquals("object",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".toString returns string")
        void testToString() {
            ArkInstruction insn = makeLdobjbyname(4);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".valueOf returns object")
        void testValueOf() {
            ArkInstruction insn = makeLdobjbyname(5);
            assertEquals("object",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".message returns string")
        void testMessage() {
            ArkInstruction insn = makeLdobjbyname(6);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".name returns string")
        void testName() {
            ArkInstruction insn = makeLdobjbyname(7);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".stack returns string")
        void testStack() {
            ArkInstruction insn = makeLdobjbyname(8);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".byteLength returns number")
        void testByteLength() {
            ArkInstruction insn = makeLdobjbyname(10);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName(".byteOffset returns number")
        void testByteOffset() {
            ArkInstruction insn = makeLdobjbyname(11);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("Unknown property names")
    class UnknownProperties {

        @Test
        @DisplayName("unknown property returns null")
        void testUnknownProperty() {
            ArkInstruction insn = makeLdobjbyname(9);
            assertNull(typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("ldthisbyname property types")
    class LdthisbynameTypes {

        @Test
        @DisplayName("this.length returns number")
        void testThisLength() {
            ArkInstruction insn = makeLdthisbyname(0);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("this.name returns string")
        void testThisName() {
            ArkInstruction insn = makeLdthisbyname(7);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("ldsuperbyname property types")
    class LdsuperbynameTypes {

        @Test
        @DisplayName("super.size returns number")
        void testSuperSize() {
            ArkInstruction insn = makeLdsuperbyname(1);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("ldobjbyvalue returns null")
    class LdobjbyvalueTypes {

        @Test
        @DisplayName("value-based load returns null")
        void testValueBasedLoadReturnsNull() {
            ArkInstruction insn = makeLdobjbyvalue();
            assertNull(typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("ldobjbyindex with tracked array types")
    class LdobjbyindexTypes {

        @Test
        @DisplayName("returns element type for tracked number array")
        void testTrackedNumberArray() {
            ctx.currentAccValue =
                    new ArkTSExpression.VariableExpression("arr");
            typeInf.setRegisterType("arr", "number[]");
            ArkInstruction insn = makeLdobjbyindex(0);
            assertEquals("number",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("returns element type for tracked string array")
        void testTrackedStringArray() {
            ctx.currentAccValue =
                    new ArkTSExpression.VariableExpression("names");
            typeInf.setRegisterType("names", "string[]");
            ArkInstruction insn = makeLdobjbyindex(0);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("returns element type for Array<string> syntax")
        void testGenericArraySyntax() {
            ctx.currentAccValue =
                    new ArkTSExpression.VariableExpression("items");
            typeInf.setRegisterType("items", "Array<string>");
            ArkInstruction insn = makeLdobjbyindex(0);
            assertEquals("string",
                    typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("returns null when accumulator is not a variable")
        void testNonVariableAccumulator() {
            ctx.currentAccValue =
                    new ArkTSExpression.LiteralExpression("hello",
                            ArkTSExpression.LiteralExpression.LiteralKind
                                    .STRING);
            ArkInstruction insn = makeLdobjbyindex(0);
            assertNull(typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("returns null when register type is not array")
        void testNonArrayRegister() {
            ctx.currentAccValue =
                    new ArkTSExpression.VariableExpression("obj");
            typeInf.setRegisterType("obj", "Object");
            ArkInstruction insn = makeLdobjbyindex(0);
            assertNull(typeInf.inferTypeForInstruction(insn, ctx));
        }

        @Test
        @DisplayName("returns null when no accumulator value")
        void testNoAccumulatorValue() {
            ctx.currentAccValue = null;
            ArkInstruction insn = makeLdobjbyindex(0);
            assertNull(typeInf.inferTypeForInstruction(insn, ctx));
        }
    }

    @Nested
    @DisplayName("Null context handling")
    class NullContext {

        @Test
        @DisplayName("returns null when context is null")
        void testNullContext() {
            ArkInstruction insn = makeLdobjbyname(0);
            assertNull(typeInf.inferTypeForInstruction(insn, null));
        }
    }

    @Nested
    @DisplayName("Multiple property accesses")
    class MultipleAccesses {

        @Test
        @DisplayName("consecutive property loads return correct types")
        void testConsecutiveLoads() {
            assertEquals("number",
                    typeInf.inferTypeForInstruction(
                            makeLdobjbyname(0), ctx));
            assertEquals("number",
                    typeInf.inferTypeForInstruction(
                            makeLdobjbyname(1), ctx));
            assertEquals("string",
                    typeInf.inferTypeForInstruction(
                            makeLdobjbyname(7), ctx));
        }

        @Test
        @DisplayName("type inference is consistent across calls")
        void testConsistent() {
            ArkInstruction insn = makeLdobjbyname(0);
            String type1 = typeInf.inferTypeForInstruction(insn, ctx);
            String type2 = typeInf.inferTypeForInstruction(insn, ctx);
            assertEquals(type1, type2);
            assertEquals("number", type1);
        }
    }
}
