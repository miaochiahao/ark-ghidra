package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for SwitchExpression integration: replaceVariable, type inference,
 * name inference.
 */
class SwitchExpressionIntegrationTest {

    private static ArkTSExpression var(String name) {
        return new ArkTSExpression.VariableExpression(name);
    }

    private static ArkTSExpression lit(String value,
            ArkTSExpression.LiteralExpression.LiteralKind kind) {
        return new ArkTSExpression.LiteralExpression(value, kind);
    }

    @Nested
    @DisplayName("replaceVariable in SwitchExpression")
    class ReplaceVariableTest {

        @Test
        @DisplayName("Replaces variable in discriminant")
        void testReplaceDiscriminant() {
            ArkTSExpression expr =
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
                                                            .LiteralKind.STRING))),
                            null);

            ArkTSExpression result =
                    ExpressionVisitor.replaceVariable(expr, "x",
                            var("status"));
            assertNotNull(result);
            assertTrue(result
                    instanceof ArkTSAccessExpressions.SwitchExpression);
            String output = result.toArkTS();
            assertTrue(output.contains("status"),
                    "Should contain 'status': " + output);
        }

        @Test
        @DisplayName("Replaces variable in case values")
        void testReplaceCaseValues() {
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.SwitchExpression(
                            var("x"),
                            List.of(
                                    new ArkTSAccessExpressions.SwitchExpression
                                            .SwitchExprCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            var("fallback"))),
                            null);

            ArkTSExpression result =
                    ExpressionVisitor.replaceVariable(expr, "fallback",
                            lit("default",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING));
            assertNotNull(result);
            String output = result.toArkTS();
            assertTrue(output.contains("default"),
                    "Should contain 'default': " + output);
        }

        @Test
        @DisplayName("Replaces variable in default value")
        void testReplaceDefaultValue() {
            ArkTSExpression expr =
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
                                                            .LiteralKind.STRING))),
                            var("defaultValue"));

            ArkTSExpression result =
                    ExpressionVisitor.replaceVariable(expr, "defaultValue",
                            lit("unknown",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.STRING));
            assertNotNull(result);
            String output = result.toArkTS();
            assertTrue(output.contains("unknown"),
                    "Should contain 'unknown': " + output);
        }

        @Test
        @DisplayName("Count variable usage in SwitchExpression")
        void testCountVariableUsage() {
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.SwitchExpression(
                            var("x"),
                            List.of(
                                    new ArkTSAccessExpressions.SwitchExpression
                                            .SwitchExprCase(lit("1",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER),
                                            var("x"))),
                            var("x"));

            int count = ExpressionVisitor.countVariableUsage(expr, "x");
            assertEquals(3, count,
                    "Should count x in discriminant, case value, and default");
        }

        @Test
        @DisplayName("Count non-matching variable returns 0")
        void testCountNonMatchingVariable() {
            ArkTSExpression expr =
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
                                                            .LiteralKind.STRING))),
                            null);

            int count = ExpressionVisitor.countVariableUsage(expr, "y");
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("Type inference for SwitchExpression")
    class TypeInferenceTest {

        @Test
        @DisplayName("All string cases infer string type")
        void testStringTypeInference() {
            ArkTSExpression expr =
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

            String type = OperatorHandler.getAccType(expr,
                    new TypeInference());
            assertEquals("string", type);
        }

        @Test
        @DisplayName("Mixed types return null")
        void testMixedTypesReturnNull() {
            ArkTSExpression expr =
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
                                            lit("42",
                                                    ArkTSExpression
                                                            .LiteralExpression
                                                            .LiteralKind.NUMBER))),
                            null);

            String type = OperatorHandler.getAccType(expr,
                    new TypeInference());
            assertEquals(null, type,
                    "Mixed types should return null");
        }
    }

    @Nested
    @DisplayName("Name inference for SwitchExpression")
    class NameInferenceTest {

        @Test
        @DisplayName("Switch expression infers 'switchResult'")
        void testNameInference() {
            ArkTSExpression expr =
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
                                                            .LiteralKind.STRING))),
                            null);

            String name = InstructionHandlerTest
                    .inferNameFromExpression(expr);
            assertEquals("switchResult", name);
        }
    }

    /**
     * Helper to access package-private inferNameFromExpression.
     */
    static class InstructionHandlerTest {
        static String inferNameFromExpression(ArkTSExpression expr) {
            return inferName(expr);
        }

        private static String inferName(ArkTSExpression expr) {
            // Use reflection or direct call since method is private
            try {
                java.lang.reflect.Method m =
                        InstructionHandler.class.getDeclaredMethod(
                                "inferNameFromExpression",
                                ArkTSExpression.class, int.class);
                m.setAccessible(true);
                return (String) m.invoke(null, expr, 0);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
