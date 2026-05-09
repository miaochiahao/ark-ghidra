package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Abstract base class for ArkTS expression AST nodes.
 *
 * <p>Each expression can be converted to ArkTS source text via {@link #toArkTS()}.
 */
public abstract class ArkTSExpression {

    /**
     * Returns the ArkTS source text for this expression.
     *
     * @return the ArkTS source string
     */
    public abstract String toArkTS();

    // --- Literal ---

    /**
     * A literal value: number, string, boolean, null, undefined.
     */
    public static class LiteralExpression extends ArkTSExpression {
        private final String value;
        private final LiteralKind kind;

        /**
         * The kind of literal.
         */
        public enum LiteralKind {
            NUMBER, STRING, BOOLEAN, NULL, UNDEFINED, NAN, INFINITY
        }

        /**
         * Constructs a literal expression.
         *
         * @param value the literal value as a string
         * @param kind the kind of literal
         */
        public LiteralExpression(String value, LiteralKind kind) {
            this.value = value;
            this.kind = kind;
        }

        public String getValue() {
            return value;
        }

        public LiteralKind getKind() {
            return kind;
        }

        @Override
        public String toArkTS() {
            switch (kind) {
                case STRING:
                    return "\"" + escapeString(value) + "\"";
                case NULL:
                    return "null";
                case UNDEFINED:
                    return "undefined";
                case BOOLEAN:
                    return value;
                case NAN:
                    return "NaN";
                case INFINITY:
                    return "Infinity";
                default:
                    return formatNumberLiteral(value);
            }
        }

        /**
         * Escapes a string value for embedding in double-quoted ArkTS source.
         *
         * <p>Handles: backslash, double quotes, newlines, carriage returns,
         * tabs, backspace, form feeds, null, and other control characters.
         *
         * @param s the raw string value
         * @return the escaped string (without surrounding quotes)
         */
        /**
         * Formats a number literal, using hex for common bitmask patterns.
         *
         * <p>Bitmask patterns: powers of 2 (256 → 0x100) and masks
         * (255 → 0xFF, 65535 → 0xFFFF). Only formats numbers >= 256.
         */
        private static String formatNumberLiteral(String value) {
            try {
                long n = Long.parseLong(value);
                if (n < 255) {
                    return value;
                }
                if (isPowerOfTwo(n) || isAllOnes(n)) {
                    return "0x" + Long.toHexString(n).toUpperCase();
                }
            } catch (NumberFormatException e) {
                // non-integer or overflow — keep as-is
            }
            return value;
        }

        private static boolean isPowerOfTwo(long n) {
            return n > 0 && (n & (n - 1)) == 0;
        }

        private static boolean isAllOnes(long n) {
            return n > 0 && (n & (n + 1)) == 0;
        }

        public static String escapeString(String s) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\'':
                        sb.append("'");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\0':
                        sb.append("\\0");
                        break;
                    default:
                        if (c < ' ') {
                            sb.append(String.format("\\x%02x", (int) c));
                        } else {
                            sb.append(c);
                        }
                        break;
                }
            }
            return sb.toString();
        }
    }

    // --- Variable ---

    /**
     * A named variable reference (register or accumulator).
     */
    public static class VariableExpression extends ArkTSExpression {
        private final String name;

        /**
         * Constructs a variable expression.
         *
         * @param name the variable name (e.g. "v0", "acc")
         */
        public VariableExpression(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toArkTS() {
            return name;
        }
    }

    // --- Binary ---

    /**
     * A binary operation: left op right.
     */
    public static class BinaryExpression extends ArkTSExpression {
        private final ArkTSExpression left;
        private final String operator;
        private final ArkTSExpression right;

        /**
         * Constructs a binary expression.
         *
         * @param left the left operand
         * @param operator the operator string (e.g. "+", "==")
         * @param right the right operand
         */
        public BinaryExpression(ArkTSExpression left, String operator,
                ArkTSExpression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public ArkTSExpression getLeft() {
            return left;
        }

        public String getOperator() {
            return operator;
        }

        public ArkTSExpression getRight() {
            return right;
        }

        @Override
        public String toArkTS() {
            int prec = operatorPrecedence(operator);
            String leftStr = left.toArkTS();
            String rightStr = right.toArkTS();
            boolean leftParens = needsParensLeft(left, prec);
            boolean rightParens = needsParensRight(right, prec);
            // Avoid redundant parentheses: if the rendered string already
            // starts with '(' and ends with ')', don't wrap again.
            if (leftParens && isAlreadyParenthesized(leftStr)) {
                leftParens = false;
            }
            if (rightParens && isAlreadyParenthesized(rightStr)) {
                rightParens = false;
            }
            StringBuilder sb = new StringBuilder();
            if (leftParens) {
                sb.append("(").append(leftStr).append(")");
            } else {
                sb.append(leftStr);
            }
            sb.append(" ").append(operator).append(" ");
            if (rightParens) {
                sb.append("(").append(rightStr).append(")");
            } else {
                sb.append(rightStr);
            }
            return sb.toString();
        }

        private static boolean isAlreadyParenthesized(String s) {
            if (s.length() < 2 || s.charAt(0) != '('
                    || s.charAt(s.length() - 1) != ')') {
                return false;
            }
            // Verify the closing ')' matches the opening '(' by checking
            // that no unmatched ')' appears before the final character.
            int depth = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0 && i < s.length() - 1) {
                        return false;
                    }
                }
            }
            return depth == 0;
        }

        private static boolean needsParensLeft(ArkTSExpression expr,
                int parentPrec) {
            if (!(expr instanceof BinaryExpression)) {
                return false;
            }
            return operatorPrecedence(
                    ((BinaryExpression) expr).operator) < parentPrec;
        }

        private static boolean needsParensRight(ArkTSExpression expr,
                int parentPrec) {
            if (!(expr instanceof BinaryExpression)) {
                return false;
            }
            int childPrec = operatorPrecedence(
                    ((BinaryExpression) expr).operator);
            return childPrec < parentPrec;
        }

        private static int operatorPrecedence(String op) {
            return switch (op) {
                case "||", "??", "&&" -> 1;
                case "==", "!=", "===",
                        "!==", "<", ">", "<=", ">=",
                        "in", "instanceof" -> 2;
                case "+", "-" -> 3;
                case "*", "/", "%" -> 4;
                case "**" -> 5;
                case "<<", ">>", ">>>" -> 6;
                case "&" -> 7;
                case "^" -> 8;
                case "|" -> 9;
                default -> 0;
            };
        }
    }

    // --- Unary ---

    /**
     * A unary operation: op operand.
     */
    public static class UnaryExpression extends ArkTSExpression {
        private final String operator;
        private final ArkTSExpression operand;
        private final boolean prefix;

        /**
         * Constructs a unary expression.
         *
         * @param operator the operator string (e.g. "-", "!", "typeof")
         * @param operand the operand
         * @param prefix true if the operator comes before the operand
         */
        public UnaryExpression(String operator, ArkTSExpression operand,
                boolean prefix) {
            this.operator = operator;
            this.operand = operand;
            this.prefix = prefix;
        }

        public String getOperator() {
            return operator;
        }

        public ArkTSExpression getOperand() {
            return operand;
        }

        public boolean isPrefix() {
            return prefix;
        }

        @Override
        public String toArkTS() {
            if (prefix) {
                if (operator.equals("typeof") || operator.equals("void")
                        || operator.equals("delete")
                        || operator.equals("await")) {
                    return operator + " " + operand.toArkTS();
                }
                boolean needParens = operand instanceof BinaryExpression;
                String inner = operand.toArkTS();
                if (needParens && !isAlreadyParenthesized(inner)) {
                    return operator + "(" + inner + ")";
                }
                return operator + inner;
            }
            return operand.toArkTS() + operator;
        }

        private static boolean isAlreadyParenthesized(String s) {
            if (s.length() < 2 || s.charAt(0) != '('
                    || s.charAt(s.length() - 1) != ')') {
                return false;
            }
            int depth = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0 && i < s.length() - 1) {
                        return false;
                    }
                }
            }
            return depth == 0;
        }
    }

    // --- Call ---

    /**
     * A function or method call.
     */
    public static class CallExpression extends ArkTSExpression {
        private final ArkTSExpression callee;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a call expression.
         *
         * @param callee the callee expression
         * @param arguments the argument expressions
         */
        public CallExpression(ArkTSExpression callee,
                List<ArkTSExpression> arguments) {
            this.callee = callee;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public ArkTSExpression getCallee() {
            return callee;
        }

        public List<ArkTSExpression> getArguments() {
            return arguments;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression arg : arguments) {
                joiner.add(arg.toArkTS());
            }
            return callee.toArkTS() + "(" + joiner + ")";
        }
    }

    // --- Member ---

    /**
     * A property access expression: object.property or object[index].
     */
    public static class MemberExpression extends ArkTSExpression {
        private final ArkTSExpression object;
        private final ArkTSExpression property;
        private final boolean computed;

        /**
         * Constructs a member expression.
         *
         * @param object the object expression
         * @param property the property expression
         * @param computed true if bracket notation (obj[expr]), false if dot (obj.prop)
         */
        public MemberExpression(ArkTSExpression object,
                ArkTSExpression property, boolean computed) {
            this.object = object;
            this.property = property;
            this.computed = computed;
        }

        public ArkTSExpression getObject() {
            return object;
        }

        public ArkTSExpression getProperty() {
            return property;
        }

        public boolean isComputed() {
            return computed;
        }

        @Override
        public String toArkTS() {
            if (computed) {
                String ident = tryExtractIdentifier(property);
                if (ident != null) {
                    return object.toArkTS() + "." + ident;
                }
                return object.toArkTS() + "[" + property.toArkTS() + "]";
            }
            return object.toArkTS() + "." + property.toArkTS();
        }

        private static String tryExtractIdentifier(ArkTSExpression expr) {
            if (!(expr instanceof LiteralExpression)) {
                return null;
            }
            LiteralExpression lit = (LiteralExpression) expr;
            if (lit.getKind()
                    != ArkTSExpression.LiteralExpression.LiteralKind.STRING) {
                return null;
            }
            String value = lit.getValue();
            if (value.isEmpty() || !isValidIdentifier(value)) {
                return null;
            }
            return value;
        }

        private static boolean isValidIdentifier(String s) {
            if (!Character.isJavaIdentifierStart(s.charAt(0))) {
                return false;
            }
            for (int i = 1; i < s.length(); i++) {
                if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    // --- Assign ---

    /**
     * An assignment expression: target = value.
     */
    public static class AssignExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final ArkTSExpression value;

        /**
         * Constructs an assignment expression.
         *
         * @param target the assignment target
         * @param value the value to assign
         */
        public AssignExpression(ArkTSExpression target,
                ArkTSExpression value) {
            this.target = target;
            this.value = value;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS() {
            return target.toArkTS() + " = " + value.toArkTS();
        }
    }

    // --- Compound Assignment ---

    /**
     * A compound assignment expression: target op= value.
     * For example: {@code x += 1}, {@code y *= 2}.
     */
    public static class CompoundAssignExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final String operator;
        private final ArkTSExpression value;

        public CompoundAssignExpression(ArkTSExpression target,
                String operator, ArkTSExpression value) {
            this.target = target;
            this.operator = operator;
            this.value = value;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        public String getOperator() {
            return operator;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS() {
            return target.toArkTS() + " " + operator + " "
                    + value.toArkTS();
        }
    }

    // --- Logical Compound Assignment ---

    /**
     * A logical compound assignment expression: {@code x &&= y},
     * {@code x ||= y}, {@code x ??= y}.
     */
    public static class LogicalAssignExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final String operator;
        private final ArkTSExpression value;

        /**
         * Constructs a logical compound assignment expression.
         *
         * @param target the assignment target (variable)
         * @param operator one of "&&=", "||=", "??="
         * @param value the value expression
         */
        public LogicalAssignExpression(ArkTSExpression target,
                String operator, ArkTSExpression value) {
            this.target = target;
            this.operator = operator;
            this.value = value;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        public String getOperator() {
            return operator;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS() {
            return target.toArkTS() + " " + operator + " "
                    + value.toArkTS();
        }
    }

    // --- Increment/Decrement ---

    /**
     * An increment or decrement expression: {@code x++}, {@code ++x},
     * {@code x--}, {@code --x}.
     */
    public static class IncrementExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final boolean isPrefix;
        private final boolean isIncrement;

        public IncrementExpression(ArkTSExpression target,
                boolean isPrefix, boolean isIncrement) {
            this.target = target;
            this.isPrefix = isPrefix;
            this.isIncrement = isIncrement;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        public boolean isPrefix() {
            return isPrefix;
        }

        public boolean isIncrement() {
            return isIncrement;
        }

        @Override
        public String toArkTS() {
            String op = isIncrement ? "++" : "--";
            if (isPrefix) {
                return op + target.toArkTS();
            }
            return target.toArkTS() + op;
        }
    }

    // --- New ---

    /**
     * A new expression: new Constructor(args).
     */
    public static class NewExpression extends ArkTSExpression {
        private final ArkTSExpression callee;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a new expression.
         *
         * @param callee the constructor expression
         * @param arguments the argument expressions
         */
        public NewExpression(ArkTSExpression callee,
                List<ArkTSExpression> arguments) {
            this.callee = callee;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public ArkTSExpression getCallee() {
            return callee;
        }

        public List<ArkTSExpression> getArguments() {
            return arguments;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression arg : arguments) {
                joiner.add(arg.toArkTS());
            }
            return "new " + callee.toArkTS() + "(" + joiner + ")";
        }
    }

    // --- This ---

    /**
     * The {@code this} keyword expression.
     */
    public static class ThisExpression extends ArkTSExpression {
        @Override
        public String toArkTS() {
            return "this";
        }
    }
}
