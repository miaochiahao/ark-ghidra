package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Tracks type information through register assignments during decompilation.
 *
 * <p>Maps instruction patterns to ArkTS types and provides type annotations
 * for variable declarations. The type tracker maintains a register-to-type map
 * that is updated as instructions are processed. Supports union types,
 * generic types, array types, and type guard narrowing.
 */
public class TypeInference {

    private final Map<String, String> registerTypes;
    private final Map<String, String> methodReturnTypes;
    private final Map<String, String> lexicalVarTypes;

    /**
     * Constructs a new type inference engine with empty state.
     */
    public TypeInference() {
        this.registerTypes = new HashMap<>(16);
        this.methodReturnTypes = new HashMap<>(8);
        this.lexicalVarTypes = new HashMap<>(8);
    }

    /**
     * Clears all type state for reuse across blocks.
     */
    public void reset() {
        registerTypes.clear();
        methodReturnTypes.clear();
        lexicalVarTypes.clear();
    }

    /**
     * Infers the ArkTS type for an instruction that loads a value into the accumulator.
     *
     * @param insn the instruction
     * @return the inferred ArkTS type name, or null if unknown
     */
    public String inferTypeForInstruction(ArkInstruction insn) {
        return inferTypeForInstruction(insn, null);
    }

    /**
     * Infers the ArkTS type for an instruction with optional context
     * for register name resolution.
     *
     * @param insn the instruction
     * @param ctx the decompilation context (may be null)
     * @return the inferred ArkTS type name, or null if unknown
     */
    public String inferTypeForInstruction(ArkInstruction insn,
            DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        if (opcode == ArkOpcodesCompat.LDAI || opcode == ArkOpcodesCompat.FLDAI) {
            return "number";
        }
        if (opcode == ArkOpcodesCompat.LDA_STR) {
            return "string";
        }
        if (opcode == ArkOpcodesCompat.LDTRUE || opcode == ArkOpcodesCompat.LDFALSE) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.LDNULL) {
            return "null";
        }
        if (opcode == ArkOpcodesCompat.LDUNDEFINED) {
            return "undefined";
        }
        if (opcode == ArkOpcodesCompat.CREATEEMPTYOBJECT
                || opcode == ArkOpcodesCompat.CREATEOBJECTWITHBUFFER) {
            return "Object";
        }
        if (opcode == ArkOpcodesCompat.CREATEEMPTYARRAY
                || opcode == ArkOpcodesCompat.CREATEARRAYWITHBUFFER) {
            return "Array<unknown>";
        }
        if (opcode == ArkOpcodesCompat.LDA) {
            int reg = (int) operands.get(0).getValue();
            String regName = ctx != null
                    ? ctx.resolveRegisterName(reg) : "v" + reg;
            return registerTypes.getOrDefault(regName, null);
        }
        if (opcode == ArkOpcodesCompat.LDTHIS) {
            return "Object";
        }
        if (opcode == ArkOpcodesCompat.LDNAN || opcode == ArkOpcodesCompat.LDINFINITY) {
            return "number";
        }
        if (isBinaryArithmeticOp(opcode)) {
            return "number";
        }
        if (isComparisonOp(opcode)) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.TYPEOF) {
            return "string";
        }
        if (opcode == ArkOpcodesCompat.NOT) {
            return "boolean";
        }
        if (opcode == ArkOpcodesCompat.NEG || opcode == ArkOpcodesCompat.INC
                || opcode == ArkOpcodesCompat.DEC) {
            return "number";
        }
        if (isCallOpcode(opcode)) {
            return inferCallReturnType(insn);
        }
        if (isPropertyLoadOpcode(opcode)) {
            return inferPropertyLoadType(insn, ctx);
        }
        if (opcode == ArkOpcodesCompat.LDSYMBOL) {
            return "symbol";
        }
        if (opcode == ArkOpcodesCompat.LDBIGINT) {
            return "bigint";
        }
        if (opcode == ArkOpcodesCompat.LDNEWTARGET) {
            return "Function";
        }
        if (opcode == ArkOpcodesCompat.LDLEXVAR) {
            if (operands.size() >= 2) {
                String key = "lex_" + operands.get(0).getValue()
                        + "_" + operands.get(1).getValue();
                return lexicalVarTypes.get(key);
            }
        }
        return null;
    }

    /**
     * Records the type of a lexical variable after a store.
     *
     * @param level the lexical scope level
     * @param slot the slot index within the scope
     * @param typeName the inferred type name
     */
    public void setLexicalVarType(int level, int slot,
            String typeName) {
        if (typeName != null) {
            lexicalVarTypes.put(
                    "lex_" + level + "_" + slot, typeName);
        }
    }

    /**
     * Records the type of a register after a store instruction.
     *
     * @param registerName the register name (e.g. "v0")
     * @param typeName the ArkTS type name
     */
    public void setRegisterType(String registerName, String typeName) {
        if (typeName != null) {
            registerTypes.put(registerName, typeName);
        }
    }

    /**
     * Returns the inferred type for a register.
     *
     * @param registerName the register name
     * @return the type name, or null if unknown
     */
    public String getRegisterType(String registerName) {
        return registerTypes.get(registerName);
    }

    /**
     * Registers a known method return type.
     *
     * @param methodName the method name
     * @param returnType the return type
     */
    public void setMethodReturnType(String methodName, String returnType) {
        methodReturnTypes.put(methodName, returnType);
    }

    /**
     * Infers the ArkTS type annotation string for a variable declaration.
     *
     * @param registerName the register name
     * @param inferredType the inferred type (may be null)
     * @return the type annotation string, or null if type is unknown or redundant
     */
    public static String formatTypeAnnotation(String registerName,
            String inferredType) {
        if (inferredType == null) {
            return null;
        }
        // Skip 'Object' as it is the default and adds noise
        if ("Object".equals(inferredType)) {
            return null;
        }
        return inferredType;
    }

    /**
     * Returns a type annotation suitable for a variable declaration,
     * omitting the annotation when the initializer makes the type obvious.
     *
     * <p>For example, {@code let x: number = 42} simplifies to
     * {@code let x = 42} because the numeric literal makes the type clear.
     * Similarly, boolean and string literals do not need annotations.
     *
     * @param inferredType the inferred type (may be null)
     * @param initializer the initializer expression (may be null)
     * @return the type annotation string, or null if redundant
     */
    public static String formatTypeAnnotationForDeclaration(
            String inferredType, ArkTSExpression initializer) {
        if (inferredType == null) {
            return null;
        }
        if ("Object".equals(inferredType)) {
            return null;
        }
        if (isTypeObviousFromLiteral(inferredType, initializer)) {
            return null;
        }
        return inferredType;
    }

    /**
     * Checks whether the type is obvious from the literal initializer.
     *
     * @param type the inferred type
     * @param expr the initializer expression
     * @return true if the type is obvious from the expression
     */
    static boolean isTypeObviousFromLiteral(String type,
            ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression.LiteralKind kind =
                    ((ArkTSExpression.LiteralExpression) expr).getKind();
            switch (type) {
                case "number":
                    return kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.NUMBER
                            || kind == ArkTSExpression.LiteralExpression
                                    .LiteralKind.NAN
                            || kind == ArkTSExpression.LiteralExpression
                                    .LiteralKind.INFINITY;
                case "boolean":
                    return kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.BOOLEAN;
                case "string":
                    return kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.STRING;
                case "null":
                    return kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.NULL;
                case "undefined":
                    return kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.UNDEFINED;
                default:
                    return false;
            }
        }
        // Empty array literal → type is obvious
        if ("Array".equals(type)
                && expr instanceof ArkTSAccessExpressions.ArrayLiteralExpression) {
            ArkTSAccessExpressions.ArrayLiteralExpression arr =
                    (ArkTSAccessExpressions.ArrayLiteralExpression) expr;
            return arr.getElements().isEmpty();
        }
        // Empty object literal → type is obvious
        if ("Object".equals(type)
                && expr instanceof ArkTSAccessExpressions.ObjectLiteralExpression) {
            ArkTSAccessExpressions.ObjectLiteralExpression obj =
                    (ArkTSAccessExpressions.ObjectLiteralExpression) expr;
            return obj.getProperties().isEmpty();
        }
        // New expression → type is obvious from constructor name
        if (expr instanceof ArkTSExpression.NewExpression) {
            ArkTSExpression.NewExpression newExpr =
                    (ArkTSExpression.NewExpression) expr;
            ArkTSExpression callee = newExpr.getCallee();
            if (callee instanceof ArkTSExpression.VariableExpression) {
                String name =
                        ((ArkTSExpression.VariableExpression) callee)
                                .getName();
                // Only obvious for well-known constructors
                return "Error".equals(name) || "Array".equals(name)
                        || "Map".equals(name) || "Set".equals(name)
                        || "Promise".equals(name)
                        || name.startsWith("Error");
            }
        }
        return false;
    }

    /**
     * Returns true if the opcode is a binary arithmetic operation producing a number.
     *
     * @param opcode the opcode
     * @return true if arithmetic
     */
    public static boolean isBinaryArithmeticOp(int opcode) {
        return opcode == ArkOpcodesCompat.ADD2
                || opcode == ArkOpcodesCompat.SUB2
                || opcode == ArkOpcodesCompat.MUL2
                || opcode == ArkOpcodesCompat.DIV2
                || opcode == ArkOpcodesCompat.MOD2
                || opcode == ArkOpcodesCompat.SHL2
                || opcode == ArkOpcodesCompat.SHR2
                || opcode == ArkOpcodesCompat.ASHR2
                || opcode == ArkOpcodesCompat.AND2
                || opcode == ArkOpcodesCompat.OR2
                || opcode == ArkOpcodesCompat.XOR2
                || opcode == ArkOpcodesCompat.EXP;
    }

    /**
     * Returns true if the opcode is a comparison operation producing a boolean.
     *
     * @param opcode the opcode
     * @return true if comparison
     */
    public static boolean isComparisonOp(int opcode) {
        return opcode == ArkOpcodesCompat.EQ
                || opcode == ArkOpcodesCompat.NOTEQ
                || opcode == ArkOpcodesCompat.LESS
                || opcode == ArkOpcodesCompat.LESSEQ
                || opcode == ArkOpcodesCompat.GREATER
                || opcode == ArkOpcodesCompat.GREATEREQ
                || opcode == ArkOpcodesCompat.STRICTEQ
                || opcode == ArkOpcodesCompat.STRICTNOTEQ
                || opcode == ArkOpcodesCompat.INSTANCEOF
                || opcode == ArkOpcodesCompat.ISIN
                || opcode == ArkOpcodesCompat.ISTRUE
                || opcode == ArkOpcodesCompat.ISFALSE;
    }

    private static boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3
                || opcode == ArkOpcodesCompat.CALLTHIS0WITHNAME
                || opcode == ArkOpcodesCompat.CALLTHIS1WITHNAME
                || opcode == ArkOpcodesCompat.CALLTHIS2WITHNAME
                || opcode == ArkOpcodesCompat.CALLTHIS3WITHNAME
                || opcode == ArkOpcodesCompat.CALLTHISRANGEWITHNAME;
    }

    private static final java.util.Map<String, String>
            PROPERTY_TYPE_MAP = java.util.Map.ofEntries(
                    java.util.Map.entry("length", "number"),
                    java.util.Map.entry("size", "number"),
                    java.util.Map.entry("byteLength", "number"),
                    java.util.Map.entry("byteOffset", "number"),
                    java.util.Map.entry("constructor", "Function"),
                    java.util.Map.entry("prototype", "object"),
                    java.util.Map.entry("name", "string"),
                    java.util.Map.entry("message", "string"),
                    java.util.Map.entry("stack", "string"),
                    java.util.Map.entry("toString", "string"),
                    java.util.Map.entry("valueOf", "object"));

    /**
     * Looks up the inferred type for a well-known property name.
     *
     * @param propName the property name
     * @return the inferred type, or null if unknown
     */
    public static String lookupPropertyType(String propName) {
        return PROPERTY_TYPE_MAP.get(propName);
    }

    private String inferPropertyLoadType(ArkInstruction insn,
            DecompilationContext ctx) {
        if (ctx == null) {
            return null;
        }
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        // Index-based access: try to extract element type from tracked
        // array type on the accumulator.
        if (opcode == ArkOpcodesCompat.LDOBJBYINDEX) {
            return inferIndexAccessType(ctx);
        }

        // Value-based loads have dynamic keys — cannot infer type
        if (opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            return null;
        }

        // Name-based loads: resolve property name from string table
        if (operands.size() >= 2) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            if (propName != null && !propName.startsWith("str_")) {
                return PROPERTY_TYPE_MAP.get(propName);
            }
        }
        return null;
    }

    /**
     * Infers the element type when accessing an array by index via the
     * accumulator. Returns the element type if the accumulator holds a
     * tracked array variable, otherwise null.
     */
    private String inferIndexAccessType(DecompilationContext ctx) {
        ArkTSExpression accExpr = ctx.currentAccValue;
        if (accExpr instanceof ArkTSExpression.VariableExpression) {
            String varName =
                    ((ArkTSExpression.VariableExpression) accExpr)
                            .getName();
            String varType = registerTypes.get(varName);
            if (isArrayType(varType)) {
                return extractArrayElementType(varType);
            }
        }
        return null;
    }

    private static boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME;
    }

    private String inferCallReturnType(ArkInstruction insn) {
        if (!insn.getOperands().isEmpty()) {
            int methodIdx = (int) insn.getOperands().get(0).getValue();
            return methodReturnTypes.getOrDefault("method_" + methodIdx, null);
        }
        return null;
    }

    /**
     * Returns true if the operator string is a comparison operator.
     *
     * @param op the operator string
     * @return true if comparison
     */
    public static boolean isComparisonOpFromSymbol(String op) {
        return "==".equals(op) || "!=".equals(op)
                || "===".equals(op) || "!==".equals(op)
                || "<".equals(op) || "<=".equals(op)
                || ">".equals(op) || ">=".equals(op)
                || "instanceof".equals(op) || "in".equals(op);
    }

    /**
     * Returns true if the operator string is a binary arithmetic operator.
     *
     * @param op the operator string
     * @return true if arithmetic
     */
    public static boolean isBinaryArithmeticOpFromSymbol(String op) {
        return "+".equals(op) || "-".equals(op)
                || "*".equals(op) || "/".equals(op)
                || "%".equals(op) || "**".equals(op)
                || "<<".equals(op) || ">>>".equals(op) || ">>".equals(op)
                || "&".equals(op) || "|".equals(op) || "^".equals(op);
    }

    // --- Union type support ---

    /**
     * Builds a union type string from a list of constituent types.
     *
     * <p>Deduplicates types and sorts them for deterministic output.
     * Returns null if the list is empty. Returns the single type if
     * only one unique type is present.
     *
     * @param types the list of type names to union
     * @return the union type string (e.g. "string | number"), or null
     */
    public static String buildUnionType(List<String> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        List<String> unique = new ArrayList<>();
        for (String t : types) {
            if (t != null && !unique.contains(t)) {
                unique.add(t);
            }
        }
        if (unique.isEmpty()) {
            return null;
        }
        if (unique.size() == 1) {
            return unique.get(0);
        }
        unique.sort(String::compareToIgnoreCase);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unique.size(); i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(unique.get(i));
        }
        return sb.toString();
    }

    /**
     * Checks whether a type string represents a union type.
     *
     * @param type the type string
     * @return true if the type contains a union separator
     */
    public static boolean isUnionType(String type) {
        return type != null && type.contains(" | ");
    }

    /**
     * Parses a union type string into its constituent types.
     *
     * @param type the union type string (e.g. "string | number")
     * @return the list of individual type names
     */
    public static List<String> parseUnionTypes(String type) {
        List<String> result = new ArrayList<>();
        if (type == null) {
            return result;
        }
        String[] parts = type.split("\\s*\\|\\s*");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    // --- Nullable type support ---

    /**
     * Builds a nullable type annotation from a base type and null/undefined flags.
     *
     * <p>Produces union types like {@code T | null}, {@code T | undefined},
     * or {@code T | null | undefined}. Skips union wrapping when the base
     * type is already "null" or "undefined".
     *
     * @param baseType the base type name (e.g. "string", "number")
     * @param canBeNull true if the value can be null
     * @param canBeUndefined true if the value can be undefined
     * @return the nullable type string
     */
    public static String inferNullableType(String baseType,
            boolean canBeNull, boolean canBeUndefined) {
        if (baseType == null) {
            if (canBeNull && canBeUndefined) {
                return "null | undefined";
            }
            if (canBeNull) {
                return "null";
            }
            if (canBeUndefined) {
                return "undefined";
            }
            return null;
        }
        if ("null".equals(baseType) || "undefined".equals(baseType)) {
            return baseType;
        }
        if (!canBeNull && !canBeUndefined) {
            return baseType;
        }
        StringBuilder sb = new StringBuilder(baseType);
        if (canBeNull) {
            sb.append(" | null");
        }
        if (canBeUndefined) {
            sb.append(" | undefined");
        }
        return sb.toString();
    }

    /**
     * Formats a property declaration with optional syntax when appropriate.
     *
     * <p>When {@code isOptional} is true, renders as {@code name?: type}.
     * Otherwise renders as {@code name: type}.
     *
     * @param name the property name
     * @param type the type annotation (may be null)
     * @param isOptional true if the property is optional
     * @return the formatted property declaration string
     */
    public static String formatOptionalProperty(String name, String type,
            boolean isOptional) {
        String typeStr = type != null ? type : "unknown";
        if (isOptional) {
            return name + "?: " + typeStr;
        }
        return name + ": " + typeStr;
    }

    /**
     * Widens the current type when a null or undefined value is assigned.
     *
     * <p>If the assigned value is a null literal, appends " | null" to the
     * current type. If it is an undefined literal, appends " | undefined".
     * Returns the current type unchanged for all other assignments.
     *
     * @param currentType the current inferred type (may be null)
     * @param assignedValue the value being assigned (may be null)
     * @return the widened type, or currentType if no widening needed
     */
    public static String inferTypeFromNullAssignment(String currentType,
            ArkTSExpression assignedValue) {
        if (!(assignedValue instanceof ArkTSExpression.LiteralExpression)) {
            return currentType;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) assignedValue;
        if (lit.getKind()
                == ArkTSExpression.LiteralExpression.LiteralKind.NULL) {
            if (currentType == null) {
                return "null";
            }
            if (currentType.contains("| null")) {
                return currentType;
            }
            return currentType + " | null";
        }
        if (lit.getKind()
                == ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED) {
            if (currentType == null) {
                return "undefined";
            }
            if (currentType.contains("| undefined")) {
                return currentType;
            }
            return currentType + " | undefined";
        }
        return currentType;
    }

    // --- Array type support ---

    /**
     * Formats a type as an array type: element[] or Array&lt;element&gt;.
     *
     * <p>Uses shorthand {@code element[]} syntax for simple types and
     * {@code Array<element>} for complex types (union, generic).
     *
     * @param elementType the element type name
     * @return the array type string
     */
    public static String formatArrayType(String elementType) {
        if (elementType == null) {
            return "Array<unknown>";
        }
        if (isUnionType(elementType) || elementType.contains("<")) {
            return "Array<" + elementType + ">";
        }
        return elementType + "[]";
    }

    /**
     * Infers the element type of an array from its initial element
     * expressions.
     *
     * <p>Returns null for empty arrays or mixed types.
     *
     * @param elements the array element expressions
     * @return the inferred element type name, or null
     */
    public static String inferArrayElementType(
            List<ArkTSExpression> elements) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        String commonType = null;
        for (ArkTSExpression elem : elements) {
            String elemType = inferExpressionType(elem);
            if (elemType == null) {
                return null;
            }
            if (commonType == null) {
                commonType = elemType;
            } else if (!commonType.equals(elemType)) {
                return null;
            }
        }
        return commonType;
    }

    private static String inferExpressionType(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) expr;
            switch (lit.getKind()) {
                case NUMBER:
                case NAN:
                case INFINITY:
                    return "number";
                case STRING:
                    return "string";
                case BOOLEAN:
                    return "boolean";
                case NULL:
                    return "null";
                case UNDEFINED:
                    return "undefined";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Checks whether a type string represents an array type.
     *
     * @param type the type string
     * @return true if the type is an array type (ends with "[]" or starts with "Array&lt;")
     */
    public static boolean isArrayType(String type) {
        return type != null
                && (type.endsWith("[]") || type.startsWith("Array<"));
    }

    /**
     * Extracts the element type from an array type string.
     *
     * @param type the array type string (e.g. "number[]" or "Array&lt;string&gt;")
     * @return the element type, or "unknown" if parsing fails
     */
    public static String extractArrayElementType(String type) {
        if (type == null) {
            return "unknown";
        }
        if (type.endsWith("[]")) {
            return type.substring(0, type.length() - 2);
        }
        if (type.startsWith("Array<") && type.endsWith(">")) {
            return type.substring(6, type.length() - 1);
        }
        return "unknown";
    }

    // --- Generic type support ---

    /**
     * Formats a generic type string: {@code Name<arg1, arg2>}.
     *
     * @param baseName the base type name (e.g. "Container")
     * @param typeArgs the type arguments (e.g. ["T"])
     * @return the generic type string
     */
    public static String formatGenericType(String baseName,
            List<String> typeArgs) {
        if (baseName == null) {
            return "Object";
        }
        if (typeArgs == null || typeArgs.isEmpty()) {
            return baseName;
        }
        StringBuilder sb = new StringBuilder(baseName);
        sb.append("<");
        for (int i = 0; i < typeArgs.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(typeArgs.get(i));
        }
        sb.append(">");
        return sb.toString();
    }

    /**
     * Checks whether a type string represents a generic type.
     *
     * @param type the type string
     * @return true if the type has angle brackets
     */
    public static boolean isGenericType(String type) {
        return type != null && type.contains("<") && type.contains(">");
    }

    // --- Type guard (typeof narrowing) support ---

    /**
     * Maps a typeof comparison string to the corresponding ArkTS type.
     *
     * <p>For example, {@code typeof x === "string"} narrows to {@code string}.
     *
     * @param typeofString the string literal compared in the typeof check
     * @return the corresponding type name, or null if not a recognized typeof
     */
    public static String typeofStringToType(String typeofString) {
        if (typeofString == null) {
            return null;
        }
        switch (typeofString) {
            case "\"string\"":
                return "string";
            case "\"number\"":
                return "number";
            case "\"boolean\"":
                return "boolean";
            case "\"undefined\"":
                return "undefined";
            case "\"object\"":
                return "Object";
            case "\"function\"":
                return "Function";
            case "\"symbol\"":
                return "symbol";
            case "\"bigint\"":
                return "bigint";
            default:
                return null;
        }
    }

    /**
     * Attempts to narrow a variable's type based on a typeof guard.
     *
     * <p>If the expression is a binary comparison of typeof var === "typename",
     * returns the narrowed type. Otherwise returns null.
     *
     * @param expr the expression to analyze
     * @return the narrowed type name, or null if not a typeof guard
     */
    public static String narrowTypeFromTypeofGuard(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        if (!"===".equals(bin.getOperator()) && !"==".equals(bin.getOperator())) {
            return null;
        }
        ArkTSExpression left = bin.getLeft();
        ArkTSExpression right = bin.getRight();
        String typeofOperand = extractTypeofOperand(left);
        String literalValue = extractStringLiteral(right);
        if (typeofOperand != null && literalValue != null) {
            return typeofStringToType(literalValue);
        }
        typeofOperand = extractTypeofOperand(right);
        literalValue = extractStringLiteral(left);
        if (typeofOperand != null && literalValue != null) {
            return typeofStringToType(literalValue);
        }
        return null;
    }

    private static String extractTypeofOperand(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.UnaryExpression)) {
            return null;
        }
        ArkTSExpression.UnaryExpression unary =
                (ArkTSExpression.UnaryExpression) expr;
        if (!"typeof".equals(unary.getOperator())) {
            return null;
        }
        return unary.getOperand() instanceof ArkTSExpression.VariableExpression
                ? ((ArkTSExpression.VariableExpression) unary.getOperand())
                        .getName()
                : "expr";
    }

    private static String extractStringLiteral(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return null;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind() == ArkTSExpression.LiteralExpression.LiteralKind.STRING) {
            return "\"" + lit.getValue() + "\"";
        }
        return null;
    }
}
