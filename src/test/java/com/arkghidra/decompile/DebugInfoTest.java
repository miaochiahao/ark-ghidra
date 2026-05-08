package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.ArrayList;

import com.arkghidra.format.AbcMethod;
import org.junit.jupiter.api.Test;

class MethodSignatureBuilderTest {

    @Test
    void testBuildParamsWithoutDebugNames() {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3);

        assertEquals(3, params.size());
        assertEquals("param_0", params.get(0).getName());
        assertEquals("param_1", params.get(1).getName());
        assertEquals("param_2", params.get(2).getName());
    }

    @Test
    void testBuildParamsWithDebugNames() {
        List<String> debugNames = List.of("width", "height", "depth");

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3, debugNames);

        assertEquals(3, params.size());
        assertEquals("width", params.get(0).getName());
        assertEquals("height", params.get(1).getName());
        assertEquals("depth", params.get(2).getName());
    }

    @Test
    void testBuildParamsWithPartialDebugNames() {
        // Debug names with null entry for second param
        List<String> debugNames = new ArrayList<>();
        debugNames.add("x");
        debugNames.add(null);
        debugNames.add("z");

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3, debugNames);

        assertEquals(3, params.size());
        assertEquals("x", params.get(0).getName());
        assertEquals("param_1", params.get(1).getName()); // null -> fallback
        assertEquals("z", params.get(2).getName());
    }

    @Test
    void testBuildParamsWithEmptyDebugName() {
        List<String> debugNames = List.of("first", "", "third");

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3, debugNames);

        assertEquals(3, params.size());
        assertEquals("first", params.get(0).getName());
        assertEquals("param_1", params.get(1).getName()); // empty -> fallback
        assertEquals("third", params.get(2).getName());
    }

    @Test
    void testBuildParamsWithFewerDebugNamesThanParams() {
        List<String> debugNames = List.of("only_one");

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(null, 3, debugNames);

        assertEquals(3, params.size());
        assertEquals("only_one", params.get(0).getName());
        assertEquals("param_1", params.get(1).getName()); // no debug name
        assertEquals("param_2", params.get(2).getName()); // no debug name
    }

    @Test
    void testBuildSignatureWithDebugNames() {
        List<String> debugNames = List.of("x", "y");

        AbcMethod method = new AbcMethod(0, 0, "add", 0, 0, 0);
        String sig = MethodSignatureBuilder.buildSignature(method, null, 2,
                debugNames);

        assertTrue(sig.startsWith("function add("));
        assertTrue(sig.contains("x"));
        assertTrue(sig.contains("y"));
    }

    @Test
    void testBuildSignatureWithoutDebugNames() {
        AbcMethod method = new AbcMethod(0, 0, "add", 0, 0, 0);
        String sig = MethodSignatureBuilder.buildSignature(method, null, 2);

        assertTrue(sig.contains("param_0"));
        assertTrue(sig.contains("param_1"));
    }
}
