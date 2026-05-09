package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSExpression.CallExpression;
import com.arkghidra.decompile.ArkTSExpression.LiteralExpression;
import com.arkghidra.decompile.ArkTSExpression.MemberExpression;
import com.arkghidra.decompile.ArkTSExpression.VariableExpression;

/**
 * Tests for built-in method return type inference in
 * {@link OperatorHandler#getAccType}.
 */
@DisplayName("Built-in method return type inference")
class BuiltinMethodReturnTypeTest {

    private static String inferType(ArkTSExpression expr) {
        TypeInference typeInf = new TypeInference();
        return OperatorHandler.getAccType(expr, typeInf);
    }

    private static ArkTSExpression memberCall(String obj, String method) {
        return new CallExpression(
                new MemberExpression(
                        new VariableExpression(obj),
                        new LiteralExpression(method,
                                LiteralExpression.LiteralKind.STRING),
                        false),
                java.util.List.of());
    }

    private static ArkTSExpression ctorCall(String name) {
        return new CallExpression(
                new VariableExpression(name),
                java.util.List.of());
    }

    // --- String methods ---

    @Nested
    @DisplayName("String methods")
    class StringMethods {

        @Test
        @DisplayName("charAt returns string")
        void testCharAt() {
            assertEquals("string", inferType(memberCall("s", "charAt")));
        }

        @Test
        @DisplayName("indexOf returns number")
        void testIndexOf() {
            assertEquals("number", inferType(memberCall("s", "indexOf")));
        }

        @Test
        @DisplayName("split returns Array")
        void testSplit() {
            assertEquals("Array", inferType(memberCall("s", "split")));
        }

        @Test
        @DisplayName("includes returns boolean")
        void testIncludes() {
            assertEquals("boolean",
                    inferType(memberCall("s", "includes")));
        }

        @Test
        @DisplayName("startsWith returns boolean")
        void testStartsWith() {
            assertEquals("boolean",
                    inferType(memberCall("s", "startsWith")));
        }

        @Test
        @DisplayName("trim returns string")
        void testTrim() {
            assertEquals("string", inferType(memberCall("s", "trim")));
        }

        @Test
        @DisplayName("substring returns string")
        void testSubstring() {
            assertEquals("string",
                    inferType(memberCall("s", "substring")));
        }

        @Test
        @DisplayName("toUpperCase returns string")
        void testToUpperCase() {
            assertEquals("string",
                    inferType(memberCall("s", "toUpperCase")));
        }
    }

    // --- Array methods ---

    @Nested
    @DisplayName("Array methods")
    class ArrayMethods {

        @Test
        @DisplayName("push returns number")
        void testPush() {
            assertEquals("number", inferType(memberCall("arr", "push")));
        }

        @Test
        @DisplayName("map returns Array")
        void testMap() {
            assertEquals("Array", inferType(memberCall("arr", "map")));
        }

        @Test
        @DisplayName("filter returns Array")
        void testFilter() {
            assertEquals("Array",
                    inferType(memberCall("arr", "filter")));
        }

        @Test
        @DisplayName("join returns string")
        void testJoin() {
            assertEquals("string", inferType(memberCall("arr", "join")));
        }

        @Test
        @DisplayName("every returns boolean")
        void testEvery() {
            assertEquals("boolean",
                    inferType(memberCall("arr", "every")));
        }

        @Test
        @DisplayName("forEach returns void")
        void testForEach() {
            assertEquals("void",
                    inferType(memberCall("arr", "forEach")));
        }

        @Test
        @DisplayName("sort returns Array")
        void testSort() {
            assertEquals("Array", inferType(memberCall("arr", "sort")));
        }

        @Test
        @DisplayName("reduce returns Object")
        void testReduce() {
            assertEquals("Object",
                    inferType(memberCall("arr", "reduce")));
        }
    }

    // --- Math methods ---

    @Nested
    @DisplayName("Math methods")
    class MathMethods {

        @Test
        @DisplayName("Math.floor returns number")
        void testFloor() {
            assertEquals("number",
                    inferType(memberCall("Math", "floor")));
        }

        @Test
        @DisplayName("Math.abs returns number")
        void testAbs() {
            assertEquals("number",
                    inferType(memberCall("Math", "abs")));
        }

        @Test
        @DisplayName("Math.max returns number")
        void testMax() {
            assertEquals("number",
                    inferType(memberCall("Math", "max")));
        }

        @Test
        @DisplayName("Math.pow returns number")
        void testPow() {
            assertEquals("number",
                    inferType(memberCall("Math", "pow")));
        }
    }

    // --- Object/JSON static methods ---

    @Nested
    @DisplayName("Object and JSON static methods")
    class ObjectJsonMethods {

        @Test
        @DisplayName("Object.keys returns Array")
        void testKeys() {
            assertEquals("Array",
                    inferType(memberCall("Object", "keys")));
        }

        @Test
        @DisplayName("Object.values returns Array")
        void testValues() {
            assertEquals("Array",
                    inferType(memberCall("Object", "values")));
        }

        @Test
        @DisplayName("Object.entries returns Array")
        void testEntries() {
            assertEquals("Array",
                    inferType(memberCall("Object", "entries")));
        }

        @Test
        @DisplayName("JSON.parse returns Object")
        void testParse() {
            assertEquals("Object",
                    inferType(memberCall("JSON", "parse")));
        }

        @Test
        @DisplayName("JSON.stringify returns string")
        void testStringify() {
            assertEquals("string",
                    inferType(memberCall("JSON", "stringify")));
        }
    }

    // --- Map/Set methods ---

    @Nested
    @DisplayName("Map and Set methods")
    class MapSetMethods {

        @Test
        @DisplayName("Map.has returns boolean")
        void testMapHas() {
            assertEquals("boolean",
                    inferType(memberCall("m", "has")));
        }

        @Test
        @DisplayName("Set.add returns Set")
        void testSetAdd() {
            assertEquals("Set", inferType(memberCall("s", "add")));
        }

        @Test
        @DisplayName("Map.delete returns boolean")
        void testMapDelete() {
            assertEquals("boolean",
                    inferType(memberCall("m", "delete")));
        }
    }

    // --- Promise methods ---

    @Nested
    @DisplayName("Promise methods")
    class PromiseMethods {

        @Test
        @DisplayName("Promise.all returns Promise")
        void testPromiseAll() {
            assertEquals("Promise",
                    inferType(memberCall("Promise", "all")));
        }

        @Test
        @DisplayName("promise.then returns Promise")
        void testPromiseThen() {
            assertEquals("Promise",
                    inferType(memberCall("p", "then")));
        }

        @Test
        @DisplayName("promise.catch returns Promise")
        void testPromiseCatch() {
            assertEquals("Promise",
                    inferType(memberCall("p", "catch")));
        }
    }

    // --- Constructor calls (unchanged) ---

    @Nested
    @DisplayName("Constructor calls")
    class ConstructorCalls {

        @Test
        @DisplayName("Boolean() returns boolean")
        void testBoolean() {
            assertEquals("boolean", inferType(ctorCall("Boolean")));
        }

        @Test
        @DisplayName("Number() returns number")
        void testNumber() {
            assertEquals("number", inferType(ctorCall("Number")));
        }

        @Test
        @DisplayName("String() returns string")
        void testString() {
            assertEquals("string", inferType(ctorCall("String")));
        }

        @Test
        @DisplayName("Array() returns Array")
        void testArray() {
            assertEquals("Array", inferType(ctorCall("Array")));
        }
    }

    // --- Edge cases ---

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Computed member call returns null")
        void testComputedMemberCall() {
            ArkTSExpression expr = new CallExpression(
                    new MemberExpression(
                            new VariableExpression("obj"),
                            new VariableExpression("method"),
                            true),
                    java.util.List.of());
            assertNull(inferType(expr));
        }

        @Test
        @DisplayName("Unknown method returns null")
        void testUnknownMethod() {
            assertNull(inferType(memberCall("obj", "customMethod")));
        }

        @Test
        @DisplayName("Number.toFixed returns string")
        void testToFixed() {
            assertEquals("string",
                    inferType(memberCall("n", "toFixed")));
        }
    }
}
