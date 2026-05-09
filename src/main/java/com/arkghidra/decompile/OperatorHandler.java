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

    // --- Identity operation simplification ---

    /**
     * Simplifies identity operations where one operand is a no-op literal.
     *
     * <p>Patterns recognized:
     * <ul>
     *   <li>{@code x + 0} -> {@code x}, {@code 0 + x} -> {@code x}</li>
     *   <li>{@code x - 0} -> {@code x}</li>
     *   <li>{@code x * 1} -> {@code x}, {@code 1 * x} -> {@code x}</li>
     *   <li>{@code x / 1} -> {@code x}</li>
     *   <li>{@code x || false} -> {@code x}, {@code false || x} -> {@code x}</li>
     *   <li>{@code x && true} -> {@code x}, {@code true && x} -> {@code x}</li>
     * </ul>
     *
     * @param left the left operand
     * @param op the operator string
     * @param right the right operand
     * @return the simplified expression, or a BinaryExpression if no
     *         simplification applies
     */
    static ArkTSExpression trySimplifyIdentity(ArkTSExpression left,
            String op, ArkTSExpression right) {
        switch (op) {
            case "+":
                if (isNumericZero(right)) {
                    return left;
                }
                if (isNumericZero(left)) {
                    return right;
                }
                break;
            case "-":
                if (isNumericZero(right)) {
                    return left;
                }
                break;
            case "*":
                if (isNumericOne(right)) {
                    return left;
                }
                if (isNumericOne(left)) {
                    return right;
                }
                break;
            case "/":
                if (isNumericOne(right)) {
                    return left;
                }
                break;
            case "||":
                if (isBooleanFalse(right)) {
                    return left;
                }
                if (isBooleanFalse(left)) {
                    return right;
                }
                break;
            case "&&":
                if (isBooleanTrue(right)) {
                    return left;
                }
                if (isBooleanTrue(left)) {
                    return right;
                }
                break;
            default:
                break;
        }
        return new ArkTSExpression.BinaryExpression(left, op, right);
    }

    private static boolean isNumericZero(ArkTSExpression expr) {
        return isNumericLiteral(expr, 0.0);
    }

    private static boolean isNumericOne(ArkTSExpression expr) {
        return isNumericLiteral(expr, 1.0);
    }

    private static boolean isNumericLiteral(ArkTSExpression expr,
            double expected) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.NUMBER) {
            return false;
        }
        try {
            double val = Double.parseDouble(lit.getValue());
            return val == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBooleanFalse(ArkTSExpression expr) {
        return isBooleanLiteral(expr, false);
    }

    private static boolean isBooleanTrue(ArkTSExpression expr) {
        return isBooleanLiteral(expr, true);
    }

    private static boolean isBooleanLiteral(ArkTSExpression expr,
            boolean expected) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN) {
            return false;
        }
        return Boolean.parseBoolean(lit.getValue()) == expected;
    }

    // --- Boolean comparison simplification ---

    /**
     * Simplifies comparisons against boolean literals.
     *
     * <p>Patterns recognized (both strict and loose equality):
     * <ul>
     *   <li>{@code x === true} -> {@code x}, {@code x == true} ->
     *       {@code x}</li>
     *   <li>{@code x === false} -> {@code !x}, {@code x == false}
     *       -> {@code !x}</li>
     *   <li>{@code x !== true} -> {@code !x}, {@code x != true} ->
     *       {@code !x}</li>
     *   <li>{@code x !== false} -> {@code x}, {@code x != false}
     *       -> {@code x}</li>
     *   <li>{@code true === x} -> {@code x}, {@code true == x} ->
     *       {@code x}</li>
     *   <li>{@code false === x} -> {@code !x}, {@code false == x}
     *       -> {@code !x}</li>
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
        boolean isEquality = "===" .equals(op) || "==".equals(op);
        boolean isInequality = "!==".equals(op) || "!=".equals(op);
        if (!isEquality && !isInequality) {
            return expr;
        }
        Boolean leftBool = extractBooleanValue(bin.getLeft());
        Boolean rightBool = extractBooleanValue(bin.getRight());
        if (leftBool == null && rightBool == null) {
            return expr;
        }
        boolean negate = isInequality;
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

    // --- Redundant typeof null simplification ---

    /**
     * Simplifies redundant typeof/undefined/null guard patterns.
     *
     * <p>Patterns recognized:
     * <ul>
     *   <li>{@code typeof x !== "undefined" && x !== null} ->
     *       {@code x != null}</li>
     *   <li>{@code typeof x !== "undefined" && x != null} ->
     *       {@code x != null}</li>
     *   <li>{@code typeof x !== "undefined"} -> {@code x !== undefined}</li>
     *   <li>{@code typeof x === "undefined"} -> {@code x === undefined}</li>
     * </ul>
     *
     * @param expr the expression to simplify
     * @return the simplified expression, or the original if no simplification
     *         applies
     */
    static ArkTSExpression simplifyRedundantTypeofNull(
            ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return expr;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        if (!"&&".equals(bin.getOperator())) {
            return simplifyStandaloneTypeof(expr);
        }
        ArkTSExpression left = bin.getLeft();
        ArkTSExpression right = bin.getRight();
        String leftTypeofVar = extractTypeofVariable(left);
        String rightTypeofVar = extractTypeofVariable(right);
        boolean leftUndef = isUndefinedTypeofCheck(left, true);
        boolean rightUndef = isUndefinedTypeofCheck(right, true);
        if (leftUndef) {
            String nullCheckVar = extractNullCheckVariable(right);
            if (nullCheckVar != null
                    && nullCheckVar.equals(leftTypeofVar)
                    && isNullOrUndefinedCheck(right, nullCheckVar)) {
                return new ArkTSExpression.BinaryExpression(
                        new ArkTSExpression.VariableExpression(nullCheckVar),
                        "!=", new ArkTSExpression.LiteralExpression(
                                "null",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NULL));
            }
        }
        if (rightUndef) {
            String nullCheckVar = extractNullCheckVariable(left);
            if (nullCheckVar != null
                    && nullCheckVar.equals(rightTypeofVar)
                    && isNullOrUndefinedCheck(left, nullCheckVar)) {
                return new ArkTSExpression.BinaryExpression(
                        new ArkTSExpression.VariableExpression(nullCheckVar),
                        "!=", new ArkTSExpression.LiteralExpression(
                                "null",
                                ArkTSExpression.LiteralExpression
                                        .LiteralKind.NULL));
            }
        }
        return expr;
    }

    private static ArkTSExpression simplifyStandaloneTypeof(
            ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return expr;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        String op = bin.getOperator();
        boolean negated = "!==".equals(op);
        if (!negated && !"===".equals(op)) {
            return expr;
        }
        if (!isUndefinedTypeofCheck(expr, negated)) {
            return expr;
        }
        String varName = extractTypeofVariable(expr);
        if (varName == null) {
            return expr;
        }
        return new ArkTSExpression.BinaryExpression(
                new ArkTSExpression.VariableExpression(varName),
                negated ? "!==" : "===",
                new ArkTSExpression.LiteralExpression("undefined",
                        ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
    }

    private static boolean isNullOrUndefinedCheck(
            ArkTSExpression expr, String expectedVar) {
        if (expectedVar == null) {
            return false;
        }
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return false;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        if (!"!==".equals(bin.getOperator()) && !"!=".equals(
                bin.getOperator())) {
            return false;
        }
        String varName = extractVariableName(bin.getLeft());
        if (expectedVar.equals(varName) && isNullLiteral(bin.getRight())) {
            return true;
        }
        return expectedVar.equals(extractVariableName(bin.getRight()))
                && isNullLiteral(bin.getLeft());
    }

    private static boolean isNullLiteral(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        return ((ArkTSExpression.LiteralExpression) expr).getKind()
                == ArkTSExpression.LiteralExpression.LiteralKind.NULL;
    }

    private static String extractNullCheckVariable(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        if (!"!==".equals(bin.getOperator()) && !"!=".equals(
                bin.getOperator())) {
            return null;
        }
        String leftVar = extractVariableName(bin.getLeft());
        if (leftVar != null && isNullLiteral(bin.getRight())) {
            return leftVar;
        }
        String rightVar = extractVariableName(bin.getRight());
        if (rightVar != null && isNullLiteral(bin.getLeft())) {
            return rightVar;
        }
        return null;
    }

    private static String extractVariableName(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        return ((ArkTSExpression.VariableExpression) expr).getName();
    }

    private static boolean isUndefinedTypeofCheck(ArkTSExpression expr,
            boolean negated) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return false;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        String expectedOp = negated ? "!==" : "===";
        if (!expectedOp.equals(bin.getOperator())) {
            return false;
        }
        return isTypeofOfVariable(bin.getLeft())
                && isStringLiteral(bin.getRight(), "undefined");
    }

    private static boolean isTypeofOfVariable(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.UnaryExpression)) {
            return false;
        }
        ArkTSExpression.UnaryExpression unary =
                (ArkTSExpression.UnaryExpression) expr;
        return "typeof".equals(unary.getOperator()) && unary.isPrefix()
                && unary.getOperand()
                        instanceof ArkTSExpression.VariableExpression;
    }

    private static String extractTypeofVariable(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) expr;
        if (bin.getLeft() instanceof ArkTSExpression.UnaryExpression) {
            ArkTSExpression.UnaryExpression unary =
                    (ArkTSExpression.UnaryExpression) bin.getLeft();
            if ("typeof".equals(unary.getOperator())
                    && unary.getOperand()
                            instanceof ArkTSExpression.VariableExpression) {
                return ((ArkTSExpression.VariableExpression) unary
                        .getOperand()).getName();
            }
        }
        return null;
    }

    private static boolean isStringLiteral(ArkTSExpression expr,
            String expected) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        return lit.getKind()
                == ArkTSExpression.LiteralExpression.LiteralKind.STRING
                && expected.equals(lit.getValue());
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
            ArkTSExpression callee =
                    ((ArkTSExpression.CallExpression) expr).getCallee();
            if (callee instanceof ArkTSExpression.VariableExpression) {
                String name =
                        ((ArkTSExpression.VariableExpression) callee)
                                .getName();
                return switch (name) {
                    case "Boolean" -> "boolean";
                    case "Number" -> "number";
                    case "String" -> "string";
                    case "Array" -> "Array";
                    case "Map" -> "Map";
                    case "Set" -> "Set";
                    case "Promise" -> "Promise";
                    default -> null;
                };
            }
            return null;
        }
        if (expr instanceof ArkTSAccessExpressions
                .TemplateLiteralExpression) {
            return "string";
        }
        if (expr instanceof ArkTSExpression.NewExpression) {
            ArkTSExpression callee =
                    ((ArkTSExpression.NewExpression) expr).getCallee();
            if (callee instanceof ArkTSExpression.VariableExpression) {
                return ((ArkTSExpression.VariableExpression) callee)
                        .getName();
            }
            return null;
        }
        if (expr instanceof ArkTSAccessExpressions
                .DynamicImportExpression) {
            return "Promise";
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
