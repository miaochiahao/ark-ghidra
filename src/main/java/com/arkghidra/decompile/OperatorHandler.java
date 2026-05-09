package com.arkghidra.decompile;

import java.util.List;

/**
 * Handles operator-related classification and translation for ArkTS
 * decompilation.
 *
 * <p>Provides static methods for classifying opcodes as binary/unary/property/
 * call operations and for mapping opcodes to their string representations.
 * Also handles type inference helpers for accumulator expressions.
 */
class OperatorHandler {

    private OperatorHandler() {
        // utility class
    }

    // --- Double negation simplification ---

    /**
     * Simplifies double negation and related patterns in expressions.
     *
     * <p>Patterns recognized:
     * <ul>
     *   <li>{@code !(a == b)} -> {@code a != b}</li>
     *   <li>{@code !(a != b)} -> {@code a == b}</li>
     *   <li>{@code !(a === b)} -> {@code a !== b}</li>
     *   <li>{@code !(a !== b)} -> {@code a === b}</li>
     *   <li>{@code !!x} -> {@code Boolean(x)}</li>
     * </ul>
     *
     * @param expr the expression to simplify
     * @return the simplified expression, or the original if no simplification
     *         applies
     */
    static ArkTSExpression simplifyDoubleNegation(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.UnaryExpression)) {
            return expr;
        }
        ArkTSExpression.UnaryExpression outer =
                (ArkTSExpression.UnaryExpression) expr;
        if (!"!".equals(outer.getOperator()) || !outer.isPrefix()) {
            return expr;
        }
        ArkTSExpression inner = outer.getOperand();

        // !!x -> Boolean(x)
        if (inner instanceof ArkTSExpression.UnaryExpression) {
            ArkTSExpression.UnaryExpression innerUnary =
                    (ArkTSExpression.UnaryExpression) inner;
            if ("!".equals(innerUnary.getOperator())
                    && innerUnary.isPrefix()) {
                return new ArkTSExpression.CallExpression(
                        new ArkTSExpression.VariableExpression("Boolean"),
                        List.of(innerUnary.getOperand()));
            }
        }

        // !(a == b) -> a != b, !(a === b) -> a !== b, etc.
        if (inner instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) inner;
            String negatedOp = negateComparisonOperator(bin.getOperator());
            if (negatedOp != null) {
                return new ArkTSExpression.BinaryExpression(
                        bin.getLeft(), negatedOp, bin.getRight());
            }
        }

        return expr;
    }

    /**
     * Returns the negated form of a comparison operator, or null if the
     * operator is not a comparison that has a direct negation.
     */
    private static String negateComparisonOperator(String op) {
        return switch (op) {
            case "==" -> "!=";
            case "!=" -> "==";
            case "===" -> "!==";
            case "!==" -> "===";
            default -> null;
        };
    }

    // --- Constant folding ---

    /**
     * Attempts to fold a binary expression with two constant operands.
     *
     * <p>If both operands are numeric literals and the operator is an
     * arithmetic operator, evaluates the expression at compile time
     * and returns a new literal. Returns the original expression if
     * folding is not possible or would lose precision.
     *
     * @param left the left operand
     * @param op the operator string
     * @param right the right operand
     * @return the folded literal expression, or a BinaryExpression
     */
    static ArkTSExpression tryFoldConstants(ArkTSExpression left,
            String op, ArkTSExpression right) {
        if (!isFoldableOperator(op)) {
            return new ArkTSExpression.BinaryExpression(left, op, right);
        }
        Double leftVal = extractNumericValue(left);
        Double rightVal = extractNumericValue(right);
        if (leftVal == null || rightVal == null) {
            return new ArkTSExpression.BinaryExpression(left, op, right);
        }
        double result;
        switch (op) {
            case "+":
                result = leftVal + rightVal;
                break;
            case "-":
                result = leftVal - rightVal;
                break;
            case "*":
                result = leftVal * rightVal;
                break;
            case "/":
                if (rightVal == 0.0) {
                    return new ArkTSExpression.BinaryExpression(
                            left, op, right);
                }
                result = leftVal / rightVal;
                break;
            case "%":
                if (rightVal == 0.0) {
                    return new ArkTSExpression.BinaryExpression(
                            left, op, right);
                }
                result = leftVal % rightVal;
                break;
            case "**":
                result = Math.pow(leftVal, rightVal);
                break;
            case "<<":
                result = (int) leftVal.doubleValue()
                        << (int) rightVal.doubleValue();
                break;
            case ">>":
                result = (int) leftVal.doubleValue()
                        >> (int) rightVal.doubleValue();
                break;
            case ">>>":
                result = (int) leftVal.doubleValue()
                        >>> (int) rightVal.doubleValue();
                break;
            case "&":
                result = (int) leftVal.doubleValue()
                        & (int) rightVal.doubleValue();
                break;
            case "|":
                result = (int) leftVal.doubleValue()
                        | (int) rightVal.doubleValue();
                break;
            case "^":
                result = (int) leftVal.doubleValue()
                        ^ (int) rightVal.doubleValue();
                break;
            default:
                return new ArkTSExpression.BinaryExpression(left, op, right);
        }
        // Only fold if the result is a clean integer or finite number
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return new ArkTSExpression.BinaryExpression(left, op, right);
        }
        if (result == Math.floor(result) && !Double.isInfinite(result)
                && Math.abs(result) <= Integer.MAX_VALUE) {
            int intResult = (int) result;
            return new ArkTSExpression.LiteralExpression(
                    String.valueOf(intResult),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        }
        return new ArkTSExpression.LiteralExpression(
                String.valueOf(result),
                ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
    }

    private static boolean isFoldableOperator(String op) {
        return "+".equals(op) || "-".equals(op)
                || "*".equals(op) || "/".equals(op)
                || "%".equals(op) || "**".equals(op)
                || "<<".equals(op) || ">>".equals(op)
                || ">>>".equals(op) || "&".equals(op)
                || "|".equals(op) || "^".equals(op);
    }

    private static Double extractNumericValue(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return null;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.NUMBER) {
            return null;
        }
        try {
            return Double.parseDouble(lit.getValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- String literal merging ---

    /**
     * Merges adjacent string literals in a binary "+" expression.
     *
     * <p>When both operands of a "+" expression are string literals,
     * they are concatenated into a single string literal:
     * {@code "hello" + " world"} -> {@code "hello world"}.
     *
     * @param left the left operand
     * @param op the operator string
     * @param right the right operand
     * @return a merged string literal, or a BinaryExpression if merging
     *         does not apply
     */
    static ArkTSExpression tryMergeStringLiterals(ArkTSExpression left,
            String op, ArkTSExpression right) {
        if (!"+".equals(op)) {
            return new ArkTSExpression.BinaryExpression(left, op, right);
        }
        String leftStr = extractStringValue(left);
        String rightStr = extractStringValue(right);
        if (leftStr != null && rightStr != null) {
            return new ArkTSExpression.LiteralExpression(
                    leftStr + rightStr,
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        }
        return new ArkTSExpression.BinaryExpression(left, op, right);
    }

    private static String extractStringValue(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return null;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.STRING) {
            return null;
        }
        String val = lit.getValue();
        // Strip surrounding quotes for merging
        if (val.length() >= 2
                && val.charAt(0) == '"'
                && val.charAt(val.length() - 1) == '"') {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    // --- Boolean comparison simplification ---

    /**
     * Simplifies comparisons against boolean literals.
     *
     * <p>Patterns recognized:
     * <ul>
     *   <li>{@code x === true} -> {@code x}</li>
     *   <li>{@code x === false} -> {@code !x}</li>
     *   <li>{@code x !== true} -> {@code !x}</li>
     *   <li>{@code x !== false} -> {@code x}</li>
     *   <li>{@code true === x} -> {@code x}</li>
     *   <li>{@code false === x} -> {@code !x}</li>
     * </ul>
     *
     * @param expr the expression to simplify
     * @return the simplified expression, or the original if no
     *         simplification applies
     */
    static ArkTSExpression simplifyBooleanComparison(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return expr;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        String op = bin.getOperator();
        if (!"===".equals(op) && !"!==".equals(op)) {
            return expr;
        }
        Boolean leftBool = extractBooleanValue(bin.getLeft());
        Boolean rightBool = extractBooleanValue(bin.getRight());
        if (leftBool == null && rightBool == null) {
            return expr;
        }
        boolean negate = "!==".equals(op);
        if (rightBool != null) {
            ArkTSExpression operand = bin.getLeft();
            if (rightBool == negate) {
                return new ArkTSExpression.UnaryExpression(
                        "!", operand, true);
            }
            return operand;
        }
        ArkTSExpression operand = bin.getRight();
        if (leftBool == negate) {
            return new ArkTSExpression.UnaryExpression(
                    "!", operand, true);
        }
        return operand;
    }

    private static Boolean extractBooleanValue(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return null;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN) {
            return null;
        }
        return Boolean.parseBoolean(lit.getValue());
    }

    // --- Type inference helper ---

    static String getAccType(ArkTSExpression expr, TypeInference typeInf) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) expr;
            switch (lit.getKind()) {
                case NUMBER:
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
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return typeInf.getRegisterType(
                    ((ArkTSExpression.VariableExpression) expr).getName());
        }
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            String op =
                    ((ArkTSExpression.BinaryExpression) expr).getOperator();
            if (TypeInference.isComparisonOpFromSymbol(op)) {
                return "boolean";
            }
            if (TypeInference.isBinaryArithmeticOpFromSymbol(op)) {
                return "number";
            }
        }
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            String op =
                    ((ArkTSExpression.UnaryExpression) expr).getOperator();
            if ("!".equals(op)) {
                return "boolean";
            }
            if ("-".equals(op)) {
                return "number";
            }
            if ("typeof".equals(op)) {
                return "string";
            }
        }
        if (expr instanceof ArkTSAccessExpressions.ArrayLiteralExpression) {
            ArkTSAccessExpressions.ArrayLiteralExpression arr =
                    (ArkTSAccessExpressions.ArrayLiteralExpression) expr;
            String elementType =
                    TypeInference.inferArrayElementType(arr.getElements());
            return TypeInference.formatArrayType(elementType);
        }
        if (expr instanceof ArkTSAccessExpressions.ObjectLiteralExpression) {
            return "Object";
        }
        if (expr instanceof ArkTSExpression.CallExpression) {
            return null;
        }
        if (expr instanceof ArkTSAccessExpressions
                .TemplateLiteralExpression) {
            return "string";
        }
        if (expr instanceof ArkTSExpression.NewExpression) {
            return null;
        }
        return null;
    }

    // --- Binary operator classification ---

    static boolean isBinaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.ADD2
                || opcode == ArkOpcodesCompat.SUB2
                || opcode == ArkOpcodesCompat.MUL2
                || opcode == ArkOpcodesCompat.DIV2
                || opcode == ArkOpcodesCompat.MOD2
                || opcode == ArkOpcodesCompat.EQ
                || opcode == ArkOpcodesCompat.NOTEQ
                || opcode == ArkOpcodesCompat.LESS
                || opcode == ArkOpcodesCompat.LESSEQ
                || opcode == ArkOpcodesCompat.GREATER
                || opcode == ArkOpcodesCompat.GREATEREQ
                || opcode == ArkOpcodesCompat.SHL2
                || opcode == ArkOpcodesCompat.SHR2
                || opcode == ArkOpcodesCompat.ASHR2
                || opcode == ArkOpcodesCompat.AND2
                || opcode == ArkOpcodesCompat.OR2
                || opcode == ArkOpcodesCompat.XOR2
                || opcode == ArkOpcodesCompat.EXP
                || opcode == ArkOpcodesCompat.STRICTEQ
                || opcode == ArkOpcodesCompat.STRICTNOTEQ
                || opcode == ArkOpcodesCompat.INSTANCEOF
                || opcode == ArkOpcodesCompat.ISIN;
    }

    static String getBinaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.ADD2: return "+";
            case ArkOpcodesCompat.SUB2: return "-";
            case ArkOpcodesCompat.MUL2: return "*";
            case ArkOpcodesCompat.DIV2: return "/";
            case ArkOpcodesCompat.MOD2: return "%";
            case ArkOpcodesCompat.EQ: return "==";
            case ArkOpcodesCompat.NOTEQ: return "!=";
            case ArkOpcodesCompat.LESS: return "<";
            case ArkOpcodesCompat.LESSEQ: return "<=";
            case ArkOpcodesCompat.GREATER: return ">";
            case ArkOpcodesCompat.GREATEREQ: return ">=";
            case ArkOpcodesCompat.SHL2: return "<<";
            case ArkOpcodesCompat.SHR2: return ">>>";
            case ArkOpcodesCompat.ASHR2: return ">>";
            case ArkOpcodesCompat.AND2: return "&";
            case ArkOpcodesCompat.OR2: return "|";
            case ArkOpcodesCompat.XOR2: return "^";
            case ArkOpcodesCompat.EXP: return "**";
            case ArkOpcodesCompat.STRICTEQ: return "===";
            case ArkOpcodesCompat.STRICTNOTEQ: return "!==";
            case ArkOpcodesCompat.INSTANCEOF: return "instanceof";
            case ArkOpcodesCompat.ISIN: return "in";
            default: return "/* op */";
        }
    }

    // --- Unary operator classification ---

    static boolean isUnaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.NEG
                || opcode == ArkOpcodesCompat.NOT
                || opcode == ArkOpcodesCompat.TYPEOF;
    }

    static String getUnaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.NEG: return "-";
            case ArkOpcodesCompat.NOT: return "!";
            case ArkOpcodesCompat.TYPEOF: return "typeof";
            default: return "/* unary */";
        }
    }

    // --- Call opcode classification ---

    static boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3
                || opcode == ArkOpcodesCompat.CALLTHISRANGE
                || opcode == ArkOpcodesCompat.CALLRANGE;
    }

    // --- Property opcode classification ---

    static boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME
                || opcode == ArkOpcodesCompat.LDSUPERBYVALUE;
    }

    static boolean isPropertyStoreOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.STOBJBYNAME
                || opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYNAME
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYINDEX
                || opcode == ArkOpcodesCompat.STSUPERBYNAME
                || opcode == ArkOpcodesCompat.STSUPERBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUEWITHNAMESET
                || opcode == ArkOpcodesCompat.STOWNBYNAMEWITHNAMESET;
    }
}
