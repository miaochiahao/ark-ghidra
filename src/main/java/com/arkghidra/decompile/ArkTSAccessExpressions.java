package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Access and special expression AST nodes for ArkTS decompilation.
 *
 * <p>Contains optional chaining, spread, array/object literals, conditional,
 * template literals, await/yield, type operations, and arrow function
 * expression types that extend {@link ArkTSExpression}.
 *
 * <p>Property-related expressions (private member, in, instanceof, delete,
 * destructuring, nullish coalescing) are in {@link ArkTSPropertyExpressions}.
 */
public class ArkTSAccessExpressions {

    private ArkTSAccessExpressions() {
        // utility class — do not instantiate
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
                if (prop.key == null) {
                    joiner.add(prop.value.toArkTS());
                } else {
                    joiner.add(prop.key + ": " + prop.value.toArkTS());
                }
            }
            return "{ " + joiner + " }";
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
            return String.format(java.util.Locale.ROOT, "(%s ? %s : %s)",
                    test.toArkTS(), consequent.toArkTS(),
                    alternate.toArkTS());
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
        private final List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params;
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
                List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
                ArkTSStatement body, boolean isAsync) {
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
            this.body = body;
            this.isAsync = isAsync;
        }

        public List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> getParams() {
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
            for (ArkTSDeclarations.FunctionDeclaration.FunctionParam p : params) {
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
