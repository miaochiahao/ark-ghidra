package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

/**
 * Builds ArkTS method signatures from ABC method and proto metadata.
 *
 * <p>Maps shorty type codes to ArkTS type names and constructs parameter lists
 * with type annotations. When debug info is available, uses real parameter
 * names instead of synthetic ones (param_0, param_1, etc.). Supports generic
 * function signatures with type parameters.
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
    public static List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> buildParams(
            AbcProto proto, long numArgs) {
        return buildParams(proto, numArgs, null, -1);
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
    public static List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> buildParams(
            AbcProto proto, long numArgs, List<String> debugNames) {
        return buildParams(proto, numArgs, debugNames, -1);
    }

    /**
     * Builds the list of parameter names with type annotations,
     * using debug parameter names when available, and marking a
     * parameter as a rest parameter.
     *
     * <p>When debugNames is provided and has an entry for a given parameter
     * index, that name is used instead of "param_N". When restParamIndex
     * is non-negative, that parameter is marked as a rest parameter
     * ({@code ...name: type[]}).
     *
     * @param proto the method prototype
     * @param numArgs the number of arguments from the code section
     * @param debugNames the debug parameter names (may be null or contain nulls)
     * @param restParamIndex the index of the rest parameter, or -1 if none
     * @return the list of parameter strings
     */
    public static List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> buildParams(
            AbcProto proto, long numArgs, List<String> debugNames,
            int restParamIndex) {
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        if (proto == null) {
            for (int i = 0; i < numArgs; i++) {
                String name = resolveParamName(i, debugNames);
                boolean isRest = (i == restParamIndex);
                String type = isRest ? "any[]" : null;
                params.add(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam(name, type, isRest));
            }
            return params;
        }

        List<AbcProto.ShortyType> shorty = proto.getShorty();
        // shorty[0] is the return type; shorty[1..] are parameter types
        int paramCount = Math.max(0, shorty.size() - 1);
        for (int i = 0; i < paramCount; i++) {
            String typeName = shortyToArkType(shorty.get(i + 1));
            String name = resolveParamName(i, debugNames);
            boolean isRest = (i == restParamIndex);
            String resolvedType = isRest
                    ? restifyType(typeName) : typeName;
            params.add(new ArkTSDeclarations.FunctionDeclaration
                    .FunctionParam(name, resolvedType, isRest));
        }

        // If there are more args than shorty entries, add untyped params
        for (int i = paramCount; i < numArgs; i++) {
            String name = resolveParamName(i, debugNames);
            boolean isRest = (i == restParamIndex);
            String type = isRest ? "any[]" : null;
            params.add(new ArkTSDeclarations.FunctionDeclaration
                    .FunctionParam(name, type, isRest));
        }

        return params;
    }

    /**
     * Builds the list of parameter names with type annotations and
     * detected default values from the method's bytecode prologue.
     *
     * <p>First builds parameters normally, then scans the method's
     * instructions for default value patterns. When a default is
     * detected, the parameter is recreated with the default value
     * and optional flag set appropriately.
     *
     * @param proto the method prototype
     * @param code the method's code section (for scanning defaults)
     * @param debugNames the debug parameter names (may be null or contain nulls)
     * @return the list of parameters with defaults applied
     */
    public static List<ArkTSDeclarations.FunctionDeclaration.FunctionParam>
            buildParamsWithDefaults(AbcProto proto, AbcCode code,
                    List<String> debugNames) {
        long numArgs = code != null ? code.getNumArgs() : 0;
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                buildParams(proto, numArgs, debugNames);

        if (code == null || numArgs == 0) {
            return params;
        }

        List<ParameterDefaultDetector.ParamDefault> defaults =
                ParameterDefaultDetector.detectDefaults(
                        code, (int) numArgs);

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> result =
                new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ArkTSDeclarations.FunctionDeclaration.FunctionParam p =
                    params.get(i);
            if (i < defaults.size() && defaults.get(i) != null) {
                ParameterDefaultDetector.ParamDefault d = defaults.get(i);
                result.add(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam(p.getName(), p.getTypeName(),
                                p.isRest(), d.defaultValue, d.isOptional));
            } else {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Converts a type to its array form for rest parameters.
     * For example, "number" becomes "number[]", "string" becomes "string[]".
     *
     * @param baseType the base type
     * @return the array type string
     */
    private static String restifyType(String baseType) {
        if (baseType == null) {
            return "any[]";
        }
        if (baseType.endsWith("[]")) {
            return baseType;
        }
        return baseType + "[]";
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
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
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
     * Resolves the ArkTS type for a parameter at the given index
     * using the proto's shorty type descriptor.
     *
     * <p>The shorty list is structured as: shorty[0] = return type,
     * shorty[1..] = parameter types. This method returns the ArkTS
     * type name for the parameter at {@code paramIndex}, or null
     * if the proto is null or the index is out of range.
     *
     * @param proto the method prototype (may be null)
     * @param paramIndex the 0-based parameter index
     * @return the ArkTS type name, or null if unavailable
     */
    public static String resolveTypeFromShorty(AbcProto proto,
            int paramIndex) {
        if (proto == null) {
            return null;
        }
        List<AbcProto.ShortyType> shorty = proto.getShorty();
        int typeIndex = paramIndex + 1;
        if (typeIndex >= shorty.size()) {
            return null;
        }
        return shortyToArkType(shorty.get(typeIndex));
    }

    /**
     * Maps a shorty type to an ArkTS type string.
     *
     * <p>U1 (boolean) maps to {@code "boolean"}. Integer and floating-point
     * types map to {@code "number"} since ArkTS uses a unified numeric type.
     * REF maps to {@code "Object"} (class name resolution pending). ANY
     * also maps to {@code "Object"}.
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
                return "boolean";
            case I8:
            case U8:
            case I16:
            case U16:
            case I32:
            case U32:
            case I64:
            case U64:
                return "number";
            case F32:
            case F64:
                return "number";
            case REF:
                return "Object";
            case ANY:
                return "Object";
            default:
                return "Object";
        }
    }

    // --- Generic function signature support ---

    /**
     * Builds a generic function signature string with type parameters.
     *
     * <p>Produces output like
     * {@code function foo<T>(x: T): T} or
     * {@code function foo<T extends Base>(x: T): T}.
     *
     * @param name the function name
     * @param typeParams the type parameters
     * @param params the function parameters with types
     * @param returnType the return type (may be null for void)
     * @return the complete signature string
     */
    public static String buildGenericSignature(String name,
            List<ArkTSTypeDeclarations.TypeParameter> typeParams,
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("function ").append(name);
        if (typeParams != null && !typeParams.isEmpty()) {
            sb.append("<");
            for (int i = 0; i < typeParams.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(typeParams.get(i).toString());
            }
            sb.append(">");
        }
        sb.append("(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).toString());
        }
        sb.append(")");
        if (returnType != null && !"void".equals(returnType)) {
            sb.append(": ").append(returnType);
        }
        return sb.toString();
    }

    /**
     * Builds a generic function declaration AST node.
     *
     * @param name the function name
     * @param typeParams the type parameters (e.g. [T, U extends Base])
     * @param params the function parameters with type annotations
     * @param returnType the return type (may be null)
     * @param body the function body
     * @return the generic function declaration
     */
    public static ArkTSTypeDeclarations.GenericFunctionDeclaration buildGenericFunction(
            String name,
            List<ArkTSTypeDeclarations.TypeParameter> typeParams,
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            String returnType,
            ArkTSStatement body) {
        return new ArkTSTypeDeclarations.GenericFunctionDeclaration(
                name, typeParams, params, returnType, body);
    }

    /**
     * Resolves a reference type index to a type name.
     *
     * <p>When a proto has REF shorty types, the referenceTypes list contains
     * indices that can be resolved to class/type names. Falls back to "Object"
     * when the index cannot be resolved.
     *
     * @param proto the method prototype
     * @param paramIndex the parameter index (0-based, excluding return type)
     * @return the resolved type name, or null if no reference info available
     */
    public static String resolveReferenceType(AbcProto proto, int paramIndex) {
        if (proto == null || proto.getReferenceTypes() == null) {
            return null;
        }
        List<Integer> refTypes = proto.getReferenceTypes();
        int idx = paramIndex + 1;
        if (idx < refTypes.size() && refTypes.get(idx) >= 0) {
            return "ref_" + refTypes.get(idx);
        }
        return null;
    }

    /**
     * Creates type parameters from a list of type parameter names.
     *
     * @param names the type parameter names (e.g. ["T", "U"])
     * @return the list of TypeParameter objects with no constraints
     */
    public static List<ArkTSTypeDeclarations.TypeParameter> createTypeParams(
            String... names) {
        List<ArkTSTypeDeclarations.TypeParameter> result =
                new ArrayList<>();
        for (String name : names) {
            result.add(new ArkTSTypeDeclarations.TypeParameter(name, null));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Creates type parameters with constraints.
     *
     * <p>Each entry in constraints can be null (no constraint) or a type name.
     *
     * @param names the type parameter names
     * @param constraints the constraint types (parallel array to names)
     * @return the list of TypeParameter objects
     */
    public static List<ArkTSTypeDeclarations.TypeParameter> createTypeParams(
            String[] names, String[] constraints) {
        List<ArkTSTypeDeclarations.TypeParameter> result =
                new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String constraint = (constraints != null && i < constraints.length)
                    ? constraints[i] : null;
            result.add(new ArkTSTypeDeclarations.TypeParameter(
                    names[i], constraint));
        }
        return Collections.unmodifiableList(result);
    }
}
