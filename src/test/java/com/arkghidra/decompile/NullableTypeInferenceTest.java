package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSExpression.LiteralExpression;
import com.arkghidra.decompile.ArkTSExpression.LiteralExpression.LiteralKind;

/**
 * Tests for nullable type inference support in {@link TypeInference}.
 */
@DisplayName("Nullable Type Inference")
class NullableTypeInferenceTest {

    // --- inferNullableType ---

    @Nested
    @DisplayName("inferNullableType")
    class InferNullableTypeTest {

        @Test
        @DisplayName("returns base type when neither null nor undefined")
        void testNeitherNullNorUndefined() {
            String result = TypeInference.inferNullableType(
                    "string", false, false);
            assertEquals("string", result);
        }

        @Test
        @DisplayName("returns T | null when canBeNull is true")
        void testCanBeNull() {
            String result = TypeInference.inferNullableType(
                    "number", true, false);
            assertEquals("number | null", result);
        }

        @Test
        @DisplayName("returns T | undefined when canBeUndefined is true")
        void testCanBeUndefined() {
            String result = TypeInference.inferNullableType(
                    "string", false, true);
            assertEquals("string | undefined", result);
        }

        @Test
        @DisplayName("returns T | null | undefined when both flags true")
        void testBothFlags() {
            String result = TypeInference.inferNullableType(
                    "number", true, true);
            assertEquals("number | null | undefined", result);
        }

        @Test
        @DisplayName("skips union wrapping for null base type")
        void testNullBaseType() {
            String result = TypeInference.inferNullableType(
                    "null", true, true);
            assertEquals("null", result);
        }

        @Test
        @DisplayName("skips union wrapping for undefined base type")
        void testUndefinedBaseType() {
            String result = TypeInference.inferNullableType(
                    "undefined", true, true);
            assertEquals("undefined", result);
        }

        @Test
        @DisplayName("returns null when base type is null and no flags")
        void testNullBaseNoFlags() {
            String result = TypeInference.inferNullableType(
                    null, false, false);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null when base type null with canBeNull")
        void testNullBaseCanBeNull() {
            String result = TypeInference.inferNullableType(
                    null, true, false);
            assertEquals("null", result);
        }

        @Test
        @DisplayName("returns undefined when base null with canBeUndefined")
        void testNullBaseCanBeUndefined() {
            String result = TypeInference.inferNullableType(
                    null, false, true);
            assertEquals("undefined", result);
        }

        @Test
        @DisplayName("returns null | undefined when base null with both")
        void testNullBaseBoth() {
            String result = TypeInference.inferNullableType(
                    null, true, true);
            assertEquals("null | undefined", result);
        }

        @Test
        @DisplayName("handles complex generic base type")
        void testGenericBaseType() {
            String result = TypeInference.inferNullableType(
                    "Array<string>", true, false);
            assertEquals("Array<string> | null", result);
        }

        @Test
        @DisplayName("handles union base type")
        void testUnionBaseType() {
            String result = TypeInference.inferNullableType(
                    "string | number", true, false);
            assertEquals("string | number | null", result);
        }
    }

    // --- formatOptionalProperty ---

    @Nested
    @DisplayName("formatOptionalProperty")
    class FormatOptionalPropertyTest {

        @Test
        @DisplayName("formats optional property with ? syntax")
        void testOptional() {
            String result = TypeInference.formatOptionalProperty(
                    "name", "string", true);
            assertEquals("name?: string", result);
        }

        @Test
        @DisplayName("formats required property without ?")
        void testRequired() {
            String result = TypeInference.formatOptionalProperty(
                    "name", "string", false);
            assertEquals("name: string", result);
        }

        @Test
        @DisplayName("uses unknown for null type")
        void testNullType() {
            String result = TypeInference.formatOptionalProperty(
                    "prop", null, false);
            assertEquals("prop: unknown", result);
        }

        @Test
        @DisplayName("optional with nullable type")
        void testOptionalNullable() {
            String result = TypeInference.formatOptionalProperty(
                    "value", "string | null", true);
            assertEquals("value?: string | null", result);
        }
    }

    // --- inferTypeFromNullAssignment ---

    @Nested
    @DisplayName("inferTypeFromNullAssignment")
    class InferTypeFromNullAssignmentTest {

        @Test
        @DisplayName("widens type when null assigned")
        void testNullAssignment() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string",
                    new LiteralExpression("null", LiteralKind.NULL));
            assertEquals("string | null", result);
        }

        @Test
        @DisplayName("widens type when undefined assigned")
        void testUndefinedAssignment() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "number",
                    new LiteralExpression("undefined",
                            LiteralKind.UNDEFINED));
            assertEquals("number | undefined", result);
        }

        @Test
        @DisplayName("returns unchanged type for non-null assignment")
        void testNonLiteralAssignment() {
            ArkTSExpression other =
                    new ArkTSExpression.VariableExpression("x");
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string", other);
            assertEquals("string", result);
        }

        @Test
        @DisplayName("returns unchanged type for number literal")
        void testNumberLiteralAssignment() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string",
                    new LiteralExpression("42", LiteralKind.NUMBER));
            assertEquals("string", result);
        }

        @Test
        @DisplayName("does not duplicate null in union")
        void testNoDuplicateNull() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string | null",
                    new LiteralExpression("null", LiteralKind.NULL));
            assertEquals("string | null", result);
        }

        @Test
        @DisplayName("does not duplicate undefined in union")
        void testNoDuplicateUndefined() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string | undefined",
                    new LiteralExpression("undefined",
                            LiteralKind.UNDEFINED));
            assertEquals("string | undefined", result);
        }

        @Test
        @DisplayName("returns null when both inputs are null")
        void testBothNull() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    null, null);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null for null type and non-literal")
        void testNullTypeNonLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    null,
                    new ArkTSExpression.VariableExpression("x"));
            assertNull(result);
        }

        @Test
        @DisplayName("returns null literal type for null assignment to null type")
        void testNullAssignedToNullType() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    null,
                    new LiteralExpression("null", LiteralKind.NULL));
            assertEquals("null", result);
        }

        @Test
        @DisplayName("returns undefined literal type for undefined to null type")
        void testUndefinedAssignedToNullType() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    null,
                    new LiteralExpression("undefined",
                            LiteralKind.UNDEFINED));
            assertEquals("undefined", result);
        }

        @Test
        @DisplayName("widens with both null and undefined sequentially")
        void testSequentialWidening() {
            String type = "string";
            type = TypeInference.inferTypeFromNullAssignment(
                    type, new LiteralExpression("null", LiteralKind.NULL));
            assertEquals("string | null", type);
            type = TypeInference.inferTypeFromNullAssignment(
                    type, new LiteralExpression("undefined",
                            LiteralKind.UNDEFINED));
            assertEquals("string | null | undefined", type);
        }

        @Test
        @DisplayName("handles boolean literal without widening")
        void testBooleanLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "number",
                    new LiteralExpression("true", LiteralKind.BOOLEAN));
            assertEquals("number", result);
        }

        @Test
        @DisplayName("handles string literal without widening")
        void testStringLiteral() {
            String result = TypeInference.inferTypeFromNullAssignment(
                    "string",
                    new LiteralExpression("hello", LiteralKind.STRING));
            assertEquals("string", result);
        }
    }
}
