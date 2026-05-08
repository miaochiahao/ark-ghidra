package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;

import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

/**
 * Builds ArkTS method signatures from ABC method and proto metadata.
 *
 * <p>Maps shorty type codes to ArkTS type names and constructs parameter lists
 * with type annotations. When debug info is available, uses real parameter
 * names instead of synthetic ones (param_0, param_1, etc.).
 */
public class MethodSignatureBuilder {

    /**
     * Builds the ArkTS return type string from the method proto.
     *
     * @param proto the method prototype
     * @return the ArkTS type string
     */
    public static String getReturnType(AbcProto proto) {
        if (proto == null) {
            return "void";
        }
        return shortyToArkType(proto.getReturnType());
    }

    /**
     * Builds the list of parameter names with type annotations.
     *
     * <p>Parameters are named using the pattern "param_0", "param_1", etc.
     * If no proto is available, parameters are still named "param_N".
     *
     * @param proto the method prototype
     * @param numArgs the number of arguments from the code section
     * @return the list of parameter strings like "param_0: number"
     */
    public static List<ArkTSStatement.FunctionDeclaration.FunctionParam> buildParams(
            AbcProto proto, long numArgs) {
        return buildParams(proto, numArgs, null);
    }

    /**
     * Builds the list of parameter names with type annotations,
     * using debug parameter names when available.
     *
     * <p>When debugNames is provided and has an entry for a given parameter
     * index, that name is used instead of "param_N".
     *
     * @param proto the method prototype
     * @param numArgs the number of arguments from the code section
     * @param debugNames the debug parameter names (may be null or contain nulls)
     * @return the list of parameter strings
     */
    public static List<ArkTSStatement.FunctionDeclaration.FunctionParam> buildParams(
            AbcProto proto, long numArgs, List<String> debugNames) {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        if (proto == null) {
            for (int i = 0; i < numArgs; i++) {
                String name = resolveParamName(i, debugNames);
                params.add(new ArkTSStatement.FunctionDeclaration.FunctionParam(
                        name, null));
            }
            return params;
        }

        List<AbcProto.ShortyType> shorty = proto.getShorty();
        // shorty[0] is the return type; shorty[1..] are parameter types
        int paramCount = Math.max(0, shorty.size() - 1);
        for (int i = 0; i < paramCount; i++) {
            String typeName = shortyToArkType(shorty.get(i + 1));
            String name = resolveParamName(i, debugNames);
            params.add(new ArkTSStatement.FunctionDeclaration.FunctionParam(
                    name, typeName));
        }

        // If there are more args than shorty entries, add untyped params
        for (int i = paramCount; i < numArgs; i++) {
            String name = resolveParamName(i, debugNames);
            params.add(new ArkTSStatement.FunctionDeclaration.FunctionParam(
                    name, null));
        }

        return params;
    }

    /**
     * Resolves a parameter name from debug info or falls back to param_N.
     */
    private static String resolveParamName(int index, List<String> debugNames) {
        if (debugNames != null && index < debugNames.size()) {
            String debugName = debugNames.get(index);
            if (debugName != null && !debugName.isEmpty()) {
                return debugName;
            }
        }
        return "param_" + index;
    }

    /**
     * Builds the complete method signature string.
     *
     * @param method the method
     * @param proto the method prototype
     * @param numArgs the number of arguments
     * @return the signature string, e.g. "function foo(p0: number): void"
     */
    public static String buildSignature(AbcMethod method, AbcProto proto,
            long numArgs) {
        return buildSignature(method, proto, numArgs, null);
    }

    /**
     * Builds the complete method signature string with debug parameter names.
     *
     * @param method the method
     * @param proto the method prototype
     * @param numArgs the number of arguments
     * @param debugNames the debug parameter names (may be null)
     * @return the signature string
     */
    public static String buildSignature(AbcMethod method, AbcProto proto,
            long numArgs, List<String> debugNames) {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                buildParams(proto, numArgs, debugNames);
        StringBuilder sb = new StringBuilder();
        sb.append("function ").append(method.getName()).append("(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).toString());
        }
        sb.append(")");
        String retType = getReturnType(proto);
        if (!retType.equals("void")) {
            sb.append(": ").append(retType);
        }
        return sb.toString();
    }

    /**
     * Maps a shorty type to an ArkTS type string.
     *
     * @param type the shorty type
     * @return the ArkTS type name
     */
    private static String shortyToArkType(AbcProto.ShortyType type) {
        if (type == null) {
            return "Object";
        }
        switch (type) {
            case VOID:
                return "void";
            case U1:
            case I8:
            case U8:
            case I16:
            case U16:
            case I32:
            case U32:
            case F32:
            case F64:
            case I64:
            case U64:
                return "number";
            case REF:
                return "Object";
            case ANY:
                return "Object";
            default:
                return "Object";
        }
    }
}
