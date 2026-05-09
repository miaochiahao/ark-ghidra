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
     *
     * <p>Supports both static property names ({@code key: value}) and
     * computed property names ({@code [expr]: value}).
     */
    public static class ObjectLiteralExpression extends ArkTSExpression {
        private final List<ObjectProperty> properties;

        /**
         * A key-value pair in an object literal.
         *
         * <p>When {@code computedKey} is non-null, the property uses
         * computed name syntax: {@code [computedKey]: value}.
         */
        public static class ObjectProperty {
            private final String key;
            private final ArkTSExpression computedKey;
            private final ArkTSExpression value;

            /**
             * Constructs an object property with a static string key.
             *
             * @param key the property key
             * @param value the property value expression
             */
            public ObjectProperty(String key, ArkTSExpression value) {
                this.key = key;
                this.computedKey = null;
                this.value = value;
            }

            /**
             * Constructs an object property with a computed key expression.
             *
             * @param computedKey the computed key expression
             * @param value the property value expression
             * @param isComputed marker to distinguish from string-key
             *        constructor; always pass {@code true}
             */
            public ObjectProperty(ArkTSExpression computedKey,
                    ArkTSExpression value, boolean isComputed) {
                this.key = null;
                this.computedKey = computedKey;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public ArkTSExpression getComputedKey() {
                return computedKey;
            }

            /**
             * Returns true if this property uses computed key syntax.
             *
             * @return true if computed key ({@code [expr]: value})
             */
            public boolean isComputed() {
                return computedKey != null;
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
                if (prop.computedKey != null) {
                    joiner.add("[" + prop.computedKey.toArkTS() + "]: "
                            + prop.value.toArkTS());
                } else if (prop.key == null) {
                    joiner.add(prop.value.toArkTS());
                } else {
                    String valueStr = prop.value.toArkTS();
                    if (prop.key.equals(valueStr)) {
                        joiner.add(prop.key);
                    } else {
                        joiner.add(prop.key + ": " + valueStr);
                    }
                }
            }
            return "{ " + joiner + " }";
        }
    }

    // --- Rest parameter expression ---

    /**
     * A rest parameter expression used in function signatures: ...args.
     * Represents the rest parameter binding itself, not a spread usage.
     */
    public static class RestParameterExpression extends ArkTSExpression {
        private final String name;
        private final String typeName;

        /**
         * Constructs a rest parameter expression.
         *
         * @param name the parameter name
         * @param typeName the type annotation (may be null)
         */
        public RestParameterExpression(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        @Override
        public String toArkTS() {
            if (typeName != null) {
                return "..." + name + ": " + typeName;
            }
            return "..." + name;
        }
    }

    // --- Spread call expression ---

    /**
     * A function call with spread arguments: fn(a, ...args, b).
     * Used when some arguments are spread from an iterable.
     */
    public static class SpreadCallExpression extends ArkTSExpression {
        private final ArkTSExpression callee;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a spread call expression.
         *
         * @param callee the callee expression
         * @param arguments the argument expressions (may contain
         *                  SpreadExpression elements)
         */
        public SpreadCallExpression(ArkTSExpression callee,
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

    // --- Spread new expression ---

    /**
     * A new expression with spread arguments: new Ctor(a, ...args).
     */
    public static class SpreadNewExpression extends ArkTSExpression {
        private final ArkTSExpression callee;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a spread new expression.
         *
         * @param callee the constructor expression
         * @param arguments the argument expressions (may contain
         *                  SpreadExpression elements)
         */
        public SpreadNewExpression(ArkTSExpression callee,
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

    // --- Spread array expression ---

    /**
     * An array literal with spread elements: [1, ...arr, 2].
     * Used when a mix of regular and spread elements appear.
     */
    public static class SpreadArrayExpression extends ArkTSExpression {
        private final List<ArkTSExpression> elements;

        /**
         * Constructs a spread array expression.
         *
         * @param elements the element expressions (may contain
         *                 SpreadExpression elements)
         */
        public SpreadArrayExpression(List<ArkTSExpression> elements) {
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

    // --- Spread object expression ---

    /**
     * An object literal with spread properties: { ...obj, key: val }.
     * Supports mixing spread and regular properties.
     */
    public static class SpreadObjectExpression extends ArkTSExpression {
        private final List<ArkTSExpression> properties;

        /**
         * Constructs a spread object expression.
         *
         * @param properties the property expressions (may be
         *                   SpreadExpression for spreads, or
         *                   ObjectProperty for key-value pairs)
         */
        public SpreadObjectExpression(List<ArkTSExpression> properties) {
            this.properties = Collections.unmodifiableList(
                    new ArrayList<>(properties));
        }

        public List<ArkTSExpression> getProperties() {
            return properties;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression prop : properties) {
                joiner.add(prop.toArkTS());
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
            return String.format(java.util.Locale.ROOT, "%s ? %s : %s",
                    test.toArkTS(), consequent.toArkTS(),
                    alternate.toArkTS());
        }
    }

    // --- Switch expression ---

    /**
     * A switch expression: switch (x) { case 1: "one" case 2: "two" default: "other" }.
     *
     * <p>ArkTS supports switch expressions where each case body is an
     * expression rather than a statement list.
     */
    public static class SwitchExpression extends ArkTSExpression {
        private final ArkTSExpression discriminant;
        private final List<SwitchExprCase> cases;
        private final ArkTSExpression defaultValue;

        /**
         * A single case in a switch expression.
         */
        public static class SwitchExprCase {
            private final List<ArkTSExpression> tests;
            private final ArkTSExpression value;

            public SwitchExprCase(ArkTSExpression test,
                    ArkTSExpression value) {
                this.tests = Collections.singletonList(test);
                this.value = value;
            }

            public SwitchExprCase(List<ArkTSExpression> tests,
                    ArkTSExpression value) {
                this.tests = Collections.unmodifiableList(
                        new ArrayList<>(tests));
                this.value = value;
            }

            public List<ArkTSExpression> getTests() {
                return tests;
            }

            public ArkTSExpression getValue() {
                return value;
            }
        }

        /**
         * Constructs a switch expression.
         *
         * @param discriminant the expression being switched on
         * @param cases the case clauses
         * @param defaultValue the default value expression (may be null)
         */
        public SwitchExpression(ArkTSExpression discriminant,
                List<SwitchExprCase> cases,
                ArkTSExpression defaultValue) {
            this.discriminant = discriminant;
            this.cases = Collections.unmodifiableList(
                    new ArrayList<>(cases));
            this.defaultValue = defaultValue;
        }

        public ArkTSExpression getDiscriminant() {
            return discriminant;
        }

        public List<SwitchExprCase> getCases() {
            return cases;
        }

        public ArkTSExpression getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            sb.append("switch (").append(discriminant.toArkTS())
                    .append(") {");
            for (SwitchExprCase c : cases) {
                for (ArkTSExpression test : c.tests) {
                    sb.append(" case ").append(test.toArkTS())
                            .append(": ");
                }
                sb.append(c.value.toArkTS());
            }
            if (defaultValue != null) {
                sb.append(" default: ")
                        .append(defaultValue.toArkTS());
            }
            sb.append(" }");
            return sb.toString();
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
                        if (i + 1 < s.length() && s.charAt(i + 1) == '{') {
                            sb.append("\\${");
                            i++;
                        } else {
                            sb.append('$');
                        }
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            return sb.toString();
        }
    }

    // --- Tagged template literal ---

    /**
     * A tagged template literal expression: tag`template ${expr} rest`.
     *
     * <p>Tagged templates invoke a function with the template quasis and
     * interpolated expressions as arguments. Commonly used for DSLs
     * like styled-components, gql tags, html templates, etc.
     */
    public static class TaggedTemplateExpression extends ArkTSExpression {
        private final String tag;
        private final List<String> quasis;
        private final List<ArkTSExpression> expressions;

        /**
         * Constructs a tagged template literal expression.
         *
         * @param tag the tag function name
         * @param quasis the string parts (one more than expressions)
         * @param expressions the interpolated expressions
         */
        public TaggedTemplateExpression(String tag, List<String> quasis,
                List<ArkTSExpression> expressions) {
            this.tag = tag;
            this.quasis = Collections.unmodifiableList(new ArrayList<>(quasis));
            this.expressions = Collections.unmodifiableList(
                    new ArrayList<>(expressions));
        }

        public String getTag() {
            return tag;
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
            sb.append(tag);
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
                        if (i + 1 < s.length() && s.charAt(i + 1) == '{') {
                            sb.append("\\${");
                            i++;
                        } else {
                            sb.append('$');
                        }
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

    // --- Anonymous function expression ---

    /**
     * An anonymous function expression: function(params) { body }.
     *
     * <p>Used when a definefunc result is stored to a variable but cannot
     * be simplified to an arrow function (e.g. has multiple statements,
     * or is a generator/async function).
     */
    public static class AnonymousFunctionExpression
            extends ArkTSExpression {
        private final List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params;
        private final ArkTSStatement body;
        private final boolean isAsync;
        private final boolean isGenerator;

        /**
         * Constructs an anonymous function expression.
         *
         * @param params the parameters
         * @param body the function body
         * @param isAsync true if this is an async function
         * @param isGenerator true if this is a generator function
         */
        public AnonymousFunctionExpression(
                List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
                ArkTSStatement body, boolean isAsync,
                boolean isGenerator) {
            this.params = Collections.unmodifiableList(
                    new ArrayList<>(params));
            this.body = body;
            this.isAsync = isAsync;
            this.isGenerator = isGenerator;
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

        public boolean isGenerator() {
            return isGenerator;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            if (isAsync) {
                sb.append("async ");
            }
            sb.append("function");
            if (isGenerator) {
                sb.append("*");
            }
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (ArkTSDeclarations.FunctionDeclaration.FunctionParam p
                    : params) {
                paramJoiner.add(p.toString());
            }
            sb.append("(").append(paramJoiner).append(") ");
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body)
                        .toArkTS(0));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(1)).append("\n");
                sb.append("}");
            }
            return sb.toString();
        }
    }

    // --- Generator function expression ---

    /**
     * A generator function expression: function*(params) { body }.
     *
     * <p>Detected from creategeneratorobj opcode paired with definefunc.
     * The body contains yield expressions.
     */
    public static class GeneratorFunctionExpression
            extends ArkTSExpression {
        private final String name;
        private final List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params;
        private final ArkTSStatement body;
        private final boolean isAsync;

        /**
         * Constructs a generator function expression.
         *
         * @param name the function name (may be null for anonymous)
         * @param params the parameters
         * @param body the function body
         * @param isAsync true if this is an async generator
         */
        public GeneratorFunctionExpression(String name,
                List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
                ArkTSStatement body, boolean isAsync) {
            this.name = name;
            this.params = Collections.unmodifiableList(
                    new ArrayList<>(params));
            this.body = body;
            this.isAsync = isAsync;
        }

        public String getName() {
            return name;
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
            sb.append("function*");
            if (name != null && !name.isEmpty()) {
                sb.append(" ").append(name);
            }
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (ArkTSDeclarations.FunctionDeclaration.FunctionParam p
                    : params) {
                paramJoiner.add(p.toString());
            }
            sb.append("(").append(paramJoiner).append(") ");
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body)
                        .toArkTS(0));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(1)).append("\n");
                sb.append("}");
            }
            return sb.toString();
        }
    }

    // --- Closure expression ---

    /**
     * A closure (inner function that captures outer variables).
     *
     * <p>Represented as an arrow function or anonymous function that
     * references lexical variables from enclosing scopes.
     * The captured variables are tracked for informational purposes
     * but do not change the output syntax.
     */
    public static class ClosureExpression extends ArkTSExpression {
        private final ArkTSExpression innerFunction;
        private final List<String> capturedVariables;

        /**
         * Constructs a closure expression.
         *
         * @param innerFunction the inner function expression
         *        (arrow or anonymous)
         * @param capturedVariables the names of captured outer variables
         */
        public ClosureExpression(ArkTSExpression innerFunction,
                List<String> capturedVariables) {
            this.innerFunction = innerFunction;
            this.capturedVariables = Collections.unmodifiableList(
                    new ArrayList<>(capturedVariables));
        }

        public ArkTSExpression getInnerFunction() {
            return innerFunction;
        }

        public List<String> getCapturedVariables() {
            return capturedVariables;
        }

        @Override
        public String toArkTS() {
            return innerFunction.toArkTS();
        }
    }

    // --- IIFE (Immediately Invoked Function Expression) ---

    /**
     * An immediately invoked function expression (IIFE):
     * {@code (() => { ... })()} or {@code (function() { ... })()}.
     *
     * <p>Detected when a definefunc is stored to a register and then
     * immediately called via a call opcode on that same register.
     */
    public static class IifeExpression extends ArkTSExpression {
        private final ArkTSExpression functionExpression;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs an IIFE expression.
         *
         * @param functionExpression the function being immediately invoked
         *        (arrow or anonymous function expression)
         * @param arguments the call arguments (may be empty)
         */
        public IifeExpression(ArkTSExpression functionExpression,
                List<ArkTSExpression> arguments) {
            this.functionExpression = functionExpression;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public ArkTSExpression getFunctionExpression() {
            return functionExpression;
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
            return "(" + functionExpression.toArkTS() + ")(" + joiner + ")";
        }
    }

    // --- RegExp literal ---

    /**
     * A regular expression literal expression: /pattern/flags.
     *
     * <p>Represents a regex literal created by CREATEREGEXPWITHLITERAL.
     * The pattern and flags are extracted from the bytecode operand.
     */
    public static class RegExpLiteralExpression extends ArkTSExpression {
        private final String pattern;
        private final String flags;

        /**
         * Constructs a regex literal expression.
         *
         * @param pattern the regex pattern string
         * @param flags the regex flags string (may be empty)
         */
        public RegExpLiteralExpression(String pattern, String flags) {
            this.pattern = pattern;
            this.flags = flags != null ? flags : "";
        }

        public String getPattern() {
            return pattern;
        }

        public String getFlags() {
            return flags;
        }

        @Override
        public String toArkTS() {
            return "/" + escapeRegexPattern(pattern) + "/" + flags;
        }

        private static String escapeRegexPattern(String s) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '/') {
                    sb.append("\\/");
                } else if (c == '\\') {
                    sb.append("\\\\");
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    // --- Built-in object new expression ---

    /**
     * A built-in object construction expression.
     *
     * <p>Represents construction of well-known built-in objects like
     * Map, Set, Promise, Proxy, WeakMap, WeakSet, etc. detected from
     * the global name in the accumulator before newobjrange.
     * Emits standard {@code new BuiltInName(args)} syntax.
     */
    public static class BuiltInNewExpression extends ArkTSExpression {
        private final String className;
        private final List<ArkTSExpression> arguments;

        /**
         * Known built-in class names that the decompiler recognizes.
         */
        public static final java.util.Set<String> BUILT_IN_CLASSES =
                java.util.Set.of(
                        "Map", "Set", "WeakMap", "WeakSet",
                        "Promise", "Proxy",
                        "ArrayBuffer", "SharedArrayBuffer",
                        "DataView", "Float32Array", "Float64Array",
                        "Int8Array", "Int16Array", "Int32Array",
                        "Uint8Array", "Uint16Array", "Uint32Array",
                        "Uint8ClampedArray", "BigInt64Array",
                        "BigUint64Array",
                        "MapIterator", "SetIterator",
                        "RegExp", "Date", "Error",
                        "TypeError", "RangeError", "SyntaxError",
                        "ReferenceError", "URIError", "EvalError");

        /**
         * Constructs a built-in new expression.
         *
         * @param className the built-in class name
         * @param arguments the constructor arguments
         */
        public BuiltInNewExpression(String className,
                List<ArkTSExpression> arguments) {
            this.className = className;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public String getClassName() {
            return className;
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
            return "new " + className + "(" + joiner + ")";
        }
    }

    // --- Runtime call expression ---

    /**
     * A runtime call expression from CALLRUNTIME (0xFB) prefix.
     *
     * <p>Represents a call to an Ark runtime function such as
     * definefieldbyvalue, callinit, topropertykey, etc.
     * The decompiler maps well-known runtime calls to their
     * ArkTS equivalents where possible.
     */
    public static class RuntimeCallExpression extends ArkTSExpression {
        private final String runtimeName;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a runtime call expression.
         *
         * @param runtimeName the runtime function name (e.g.
         *                    "definefieldbyvalue")
         * @param arguments the argument expressions
         */
        public RuntimeCallExpression(String runtimeName,
                List<ArkTSExpression> arguments) {
            this.runtimeName = runtimeName;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public String getRuntimeName() {
            return runtimeName;
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
            return "/* runtime: " + runtimeName + "(" + joiner + ") */";
        }
    }

    // --- Dynamic import ---

    /**
     * A dynamic import expression: import('modulePath').
     *
     * <p>Represents a dynamic {@code import()} call, which returns a Promise
     * that resolves to the module's namespace object. Triggered by the
     * DYNAMICIMPORT (0xBD) opcode in Ark bytecode.
     */
    public static class DynamicImportExpression extends ArkTSExpression {
        private final ArkTSExpression specifier;

        /**
         * Constructs a dynamic import expression.
         *
         * @param specifier the module specifier expression (string or variable)
         */
        public DynamicImportExpression(ArkTSExpression specifier) {
            this.specifier = specifier;
        }

        public ArkTSExpression getSpecifier() {
            return specifier;
        }

        @Override
        public String toArkTS() {
            return "import(" + specifier.toArkTS() + ")";
        }
    }

    // --- Optional chain call ---

    /**
     * An optional chain call expression: obj?.method(args).
     *
     * <p>Detected when a property load is followed by a null check and
     * then a method call on the result. Emits the {@code ?.} syntax.
     */
    public static class OptionalChainCallExpression extends ArkTSExpression {
        private final ArkTSExpression object;
        private final ArkTSExpression property;
        private final boolean computed;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs an optional chain call expression.
         *
         * @param object the object expression
         * @param property the property (method) expression
         * @param computed true if bracket notation
         * @param arguments the call arguments
         */
        public OptionalChainCallExpression(ArkTSExpression object,
                ArkTSExpression property, boolean computed,
                List<ArkTSExpression> arguments) {
            this.object = object;
            this.property = property;
            this.computed = computed;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
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

        public List<ArkTSExpression> getArguments() {
            return arguments;
        }

        @Override
        public String toArkTS() {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression arg : arguments) {
                joiner.add(arg.toArkTS());
            }
            if (computed) {
                return object.toArkTS() + "?.["
                        + property.toArkTS() + "](" + joiner + ")";
            }
            return object.toArkTS() + "?."
                    + property.toArkTS() + "(" + joiner + ")";
        }
    }
}
