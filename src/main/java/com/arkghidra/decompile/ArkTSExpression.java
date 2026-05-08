package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
                    return value;
            }
        }

        private static String escapeString(String s) {
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
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        sb.append(c);
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
            return "(" + left.toArkTS() + " " + operator + " "
                    + right.toArkTS() + ")";
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

        @Override
        public String toArkTS() {
            if (prefix) {
                // Word operators like typeof need a space
                if (operator.equals("typeof") || operator.equals("void")
                        || operator.equals("delete")
                        || operator.equals("await")) {
                    return operator + " " + operand.toArkTS();
                }
                return "(" + operator + operand.toArkTS() + ")";
            }
            return "(" + operand.toArkTS() + operator + ")";
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
                return object.toArkTS() + "[" + property.toArkTS() + "]";
            }
            return object.toArkTS() + "." + property.toArkTS();
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

    // --- Array literal ---

    /**
     * An array literal expression: [elem1, elem2, ...].
     */
    public static class ArrayLiteralExpression extends ArkTSExpression {
        private final List<ArkTSExpression> elements;

        /**
         * Constructs an array literal expression.
         *
         * @param elements the element expressions
         */
        public ArrayLiteralExpression(List<ArkTSExpression> elements) {
            this.elements = Collections.unmodifiableList(
                    new ArrayList<>(elements));
        }

        public List<ArkTSExpression> getElements() {
            return elements;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression elem : elements) {
                joiner.add(elem.toArkTS());
            }
            return "[" + joiner + "]";
        }
    }

    // --- Object literal ---

    /**
     * An object literal expression: { key: value, ... }.
     */
    public static class ObjectLiteralExpression extends ArkTSExpression {
        private final List<ObjectProperty> properties;

        /**
         * A key-value pair in an object literal.
         */
        public static class ObjectProperty {
            private final String key;
            private final ArkTSExpression value;

            /**
             * Constructs an object property.
             *
             * @param key the property key
             * @param value the property value expression
             */
            public ObjectProperty(String key, ArkTSExpression value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public ArkTSExpression getValue() {
                return value;
            }
        }

        /**
         * Constructs an object literal expression.
         *
         * @param properties the properties
         */
        public ObjectLiteralExpression(List<ObjectProperty> properties) {
            this.properties = Collections.unmodifiableList(
                    new ArrayList<>(properties));
        }

        public List<ObjectProperty> getProperties() {
            return properties;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ObjectProperty prop : properties) {
                joiner.add(prop.key + ": " + prop.value.toArkTS());
            }
            return "{ " + joiner + " }";
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

    // --- Condition (ternary) ---

    /**
     * A conditional (ternary) expression: test ? consequent : alternate.
     */
    public static class ConditionalExpression extends ArkTSExpression {
        private final ArkTSExpression test;
        private final ArkTSExpression consequent;
        private final ArkTSExpression alternate;

        /**
         * Constructs a conditional expression.
         *
         * @param test the test condition
         * @param consequent the value when true
         * @param alternate the value when false
         */
        public ConditionalExpression(ArkTSExpression test,
                ArkTSExpression consequent, ArkTSExpression alternate) {
            this.test = test;
            this.consequent = consequent;
            this.alternate = alternate;
        }

        public ArkTSExpression getTest() {
            return test;
        }

        public ArkTSExpression getConsequent() {
            return consequent;
        }

        public ArkTSExpression getAlternate() {
            return alternate;
        }

        @Override
        public String toArkTS() {
            return String.format(Locale.ROOT, "(%s ? %s : %s)",
                    test.toArkTS(), consequent.toArkTS(),
                    alternate.toArkTS());
        }
    }

    // --- Optional chaining ---

    /**
     * An optional chaining expression: obj?.property or obj?.[expr].
     */
    public static class OptionalChainExpression extends ArkTSExpression {
        private final ArkTSExpression object;
        private final ArkTSExpression property;
        private final boolean computed;

        /**
         * Constructs an optional chain expression.
         *
         * @param object the object expression
         * @param property the property expression
         * @param computed true if bracket notation, false if dot notation
         */
        public OptionalChainExpression(ArkTSExpression object,
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
                return object.toArkTS() + "?.[" + property.toArkTS() + "]";
            }
            return object.toArkTS() + "?." + property.toArkTS();
        }
    }

    // --- Spread ---

    /**
     * A spread expression: ...argument.
     */
    public static class SpreadExpression extends ArkTSExpression {
        private final ArkTSExpression argument;

        /**
         * Constructs a spread expression.
         *
         * @param argument the expression to spread
         */
        public SpreadExpression(ArkTSExpression argument) {
            this.argument = argument;
        }

        public ArkTSExpression getArgument() {
            return argument;
        }

        @Override
        public String toArkTS() {
            return "..." + argument.toArkTS();
        }
    }

    // --- Template literal ---

    /**
     * A template literal expression: `part1${expr}part2`.
     */
    public static class TemplateLiteralExpression extends ArkTSExpression {
        private final List<String> quasis;
        private final List<ArkTSExpression> expressions;

        /**
         * Constructs a template literal expression.
         *
         * @param quasis the string parts (one more than expressions)
         * @param expressions the interpolated expressions
         */
        public TemplateLiteralExpression(List<String> quasis,
                List<ArkTSExpression> expressions) {
            this.quasis = Collections.unmodifiableList(new ArrayList<>(quasis));
            this.expressions = Collections.unmodifiableList(
                    new ArrayList<>(expressions));
        }

        public List<String> getQuasis() {
            return quasis;
        }

        public List<ArkTSExpression> getExpressions() {
            return expressions;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            sb.append("`");
            for (int i = 0; i < quasis.size(); i++) {
                sb.append(escapeTemplateQuasi(quasis.get(i)));
                if (i < expressions.size()) {
                    sb.append("${").append(expressions.get(i).toArkTS())
                            .append("}");
                }
            }
            sb.append("`");
            return sb.toString();
        }

        private static String escapeTemplateQuasi(String s) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '`':
                        sb.append("\\`");
                        break;
                    case '$':
                        sb.append("\\$");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            return sb.toString();
        }
    }

    // --- Await ---

    /**
     * An await expression: await promise.
     */
    public static class AwaitExpression extends ArkTSExpression {
        private final ArkTSExpression argument;

        /**
         * Constructs an await expression.
         *
         * @param argument the promise expression
         */
        public AwaitExpression(ArkTSExpression argument) {
            this.argument = argument;
        }

        public ArkTSExpression getArgument() {
            return argument;
        }

        @Override
        public String toArkTS() {
            return "await " + argument.toArkTS();
        }
    }

    // --- Yield ---

    /**
     * A yield expression: yield value or yield* iterable.
     */
    public static class YieldExpression extends ArkTSExpression {
        private final ArkTSExpression argument;
        private final boolean delegate;

        /**
         * Constructs a yield expression.
         *
         * @param argument the yielded value (may be null for bare yield)
         * @param delegate true if this is yield* (delegate)
         */
        public YieldExpression(ArkTSExpression argument, boolean delegate) {
            this.argument = argument;
            this.delegate = delegate;
        }

        public ArkTSExpression getArgument() {
            return argument;
        }

        public boolean isDelegate() {
            return delegate;
        }

        @Override
        public String toArkTS() {
            if (delegate) {
                if (argument != null) {
                    return "yield* " + argument.toArkTS();
                }
                return "yield*";
            }
            if (argument != null) {
                return "yield " + argument.toArkTS();
            }
            return "yield";
        }
    }

    // --- Type cast (as) ---

    /**
     * A type cast expression using the {@code as} keyword: expr as Type.
     */
    public static class AsExpression extends ArkTSExpression {
        private final ArkTSExpression expression;
        private final String typeName;

        /**
         * Constructs an as-expression (type cast).
         *
         * @param expression the expression being cast
         * @param typeName the target type name
         */
        public AsExpression(ArkTSExpression expression, String typeName) {
            this.expression = expression;
            this.typeName = typeName;
        }

        public ArkTSExpression getExpression() {
            return expression;
        }

        public String getTypeName() {
            return typeName;
        }

        @Override
        public String toArkTS() {
            return expression.toArkTS() + " as " + typeName;
        }
    }

    // --- Non-null assertion ---

    /**
     * A non-null assertion expression: expr!.
     */
    public static class NonNullExpression extends ArkTSExpression {
        private final ArkTSExpression expression;

        /**
         * Constructs a non-null assertion expression.
         *
         * @param expression the expression being asserted non-null
         */
        public NonNullExpression(ArkTSExpression expression) {
            this.expression = expression;
        }

        public ArkTSExpression getExpression() {
            return expression;
        }

        @Override
        public String toArkTS() {
            return expression.toArkTS() + "!";
        }
    }

    // --- Type reference ---

    /**
     * A type reference expression used in type positions: TypeName or
     * TypeName&lt;Args&gt;.
     */
    public static class TypeReferenceExpression extends ArkTSExpression {
        private final String typeName;
        private final List<String> typeArgs;

        /**
         * Constructs a type reference expression.
         *
         * @param typeName the type name
         * @param typeArgs the type arguments (may be empty)
         */
        public TypeReferenceExpression(String typeName, List<String> typeArgs) {
            this.typeName = typeName;
            this.typeArgs = Collections.unmodifiableList(new ArrayList<>(typeArgs));
        }

        public String getTypeName() {
            return typeName;
        }

        public List<String> getTypeArgs() {
            return typeArgs;
        }

        @Override
        public String toArkTS() {
            if (typeArgs.isEmpty()) {
                return typeName;
            }
            StringJoiner joiner = new StringJoiner(", ");
            for (String arg : typeArgs) {
                joiner.add(arg);
            }
            return typeName + "<" + joiner + ">";
        }
    }

    // --- Arrow function ---

    /**
     * An arrow function expression: (params) => body.
     */
    public static class ArrowFunctionExpression extends ArkTSExpression {
        private final List<ArkTSStatement.FunctionDeclaration.FunctionParam> params;
        private final ArkTSStatement body;
        private final boolean isAsync;

        /**
         * Constructs an arrow function expression.
         *
         * @param params the parameters
         * @param body the function body
         * @param isAsync true if this is an async arrow function
         */
        public ArrowFunctionExpression(
                List<ArkTSStatement.FunctionDeclaration.FunctionParam> params,
                ArkTSStatement body, boolean isAsync) {
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
            this.body = body;
            this.isAsync = isAsync;
        }

        public List<ArkTSStatement.FunctionDeclaration.FunctionParam> getParams() {
            return params;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        public boolean isAsync() {
            return isAsync;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            if (isAsync) {
                sb.append("async ");
            }
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (ArkTSStatement.FunctionDeclaration.FunctionParam p : params) {
                paramJoiner.add(p.toString());
            }
            sb.append("(").append(paramJoiner).append(") => ");
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body).toArkTS(0));
            } else {
                sb.append(body.toArkTS(0));
            }
            return sb.toString();
        }
    }
}
