package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Property access and special operation expression AST nodes for ArkTS decompilation.
 *
 * <p>Contains private member access, in/instanceof checks, delete, copy data,
 * generator state, destructuring, and nullish coalescing expression types that
 * extend {@link ArkTSExpression}.
 */
public class ArkTSPropertyExpressions {

    private ArkTSPropertyExpressions() {
        // utility class — do not instantiate
    }

    // --- Private member access (obj.#prop) ---

    /**
     * A private property access expression: obj.#prop.
     *
     * <p>Used for private fields in ArkTS/TypeScript classes that are
     * accessed with the hash prefix syntax.
     */
    public static class PrivateMemberExpression extends ArkTSExpression {
        private final ArkTSExpression object;
        private final String propertyName;

        /**
         * Constructs a private member expression.
         *
         * @param object the object expression
         * @param propertyName the private property name (without the # prefix)
         */
        public PrivateMemberExpression(ArkTSExpression object,
                String propertyName) {
            this.object = object;
            this.propertyName = propertyName;
        }

        public ArkTSExpression getObject() {
            return object;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String toArkTS() {
            return object.toArkTS() + ".#" + propertyName;
        }
    }

    // --- Private field declaration (#field) ---

    /**
     * A private field declaration expression: #field.
     *
     * <p>Represents the declaration of a private field on a class,
     * emitted by the {@code createprivateproperty} runtime call.
     * In ArkTS, private fields are declared using the hash prefix
     * syntax within the class body: {@code #fieldName;}.
     */
    public static class PrivateFieldDeclarationExpression
            extends ArkTSExpression {
        private final String fieldName;

        /**
         * Constructs a private field declaration expression.
         *
         * @param fieldName the private field name (without the # prefix)
         */
        public PrivateFieldDeclarationExpression(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }

        @Override
        public String toArkTS() {
            return "#" + fieldName;
        }
    }

    // --- In expression (prop in obj) ---

    /**
     * An {@code in} expression: prop in obj.
     *
     * <p>Checks whether a property exists on an object.
     */
    public static class InExpression extends ArkTSExpression {
        private final ArkTSExpression property;
        private final ArkTSExpression object;

        /**
         * Constructs an in-expression.
         *
         * @param property the property name expression
         * @param object the object expression
         */
        public InExpression(ArkTSExpression property,
                ArkTSExpression object) {
            this.property = property;
            this.object = object;
        }

        public ArkTSExpression getProperty() {
            return property;
        }

        public ArkTSExpression getObject() {
            return object;
        }

        @Override
        public String toArkTS() {
            return property.toArkTS() + " in " + object.toArkTS();
        }
    }

    // --- Instanceof expression (expr instanceof Type) ---

    /**
     * An {@code instanceof} expression: expr instanceof Type.
     */
    public static class InstanceofExpression extends ArkTSExpression {
        private final ArkTSExpression expression;
        private final ArkTSExpression targetType;

        /**
         * Constructs an instanceof expression.
         *
         * @param expression the expression being tested
         * @param targetType the target type expression
         */
        public InstanceofExpression(ArkTSExpression expression,
                ArkTSExpression targetType) {
            this.expression = expression;
            this.targetType = targetType;
        }

        public ArkTSExpression getExpression() {
            return expression;
        }

        public ArkTSExpression getTargetType() {
            return targetType;
        }

        @Override
        public String toArkTS() {
            return expression.toArkTS() + " instanceof "
                    + targetType.toArkTS();
        }
    }

    // --- Delete expression (delete obj.prop) ---

    /**
     * A delete expression: delete obj.prop.
     */
    public static class DeleteExpression extends ArkTSExpression {
        private final ArkTSExpression target;

        /**
         * Constructs a delete expression.
         *
         * @param target the property to delete
         */
        public DeleteExpression(ArkTSExpression target) {
            this.target = target;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        @Override
        public String toArkTS() {
            return "delete " + target.toArkTS();
        }
    }

    // --- Object spread / copy (copyDataProperties) ---

    /**
     * A copy data properties expression: Object.assign(target, source).
     *
     * <p>Represents the internal copyDataProperties operation which copies
     * all enumerable own properties from source to target.
     */
    public static class CopyDataPropertiesExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final ArkTSExpression source;

        /**
         * Constructs a copy data properties expression.
         *
         * @param target the target object
         * @param source the source object
         */
        public CopyDataPropertiesExpression(ArkTSExpression target,
                ArkTSExpression source) {
            this.target = target;
            this.source = source;
        }

        public ArkTSExpression getTarget() {
            return target;
        }

        public ArkTSExpression getSource() {
            return source;
        }

        @Override
        public String toArkTS() {
            return "Object.assign(" + target.toArkTS() + ", "
                    + source.toArkTS() + ")";
        }
    }

    // --- Generator state expression ---

    /**
     * A generator state set expression: generator.state = value.
     *
     * <p>Used internally for generator state machine management.
     */
    public static class GeneratorStateExpression extends ArkTSExpression {
        private final ArkTSExpression value;

        /**
         * Constructs a generator state expression.
         *
         * @param value the state value expression
         */
        public GeneratorStateExpression(ArkTSExpression value) {
            this.value = value;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS() {
            return "/* setgeneratorstate " + value.toArkTS() + " */";
        }
    }

    // --- Array destructuring ---

    /**
     * An array destructuring expression: [a, b, ...rest] = source.
     */
    public static class ArrayDestructuringExpression extends ArkTSExpression {
        private final List<ArrayBinding> bindings;
        private final String restBinding;
        private final ArkTSExpression source;

        /**
         * A single binding in an array destructuring pattern.
         */
        public static class ArrayBinding {
            private final String name;
            private final ArkTSExpression defaultValue;

            /**
             * Constructs an array binding without a default value.
             *
             * @param name the variable name
             */
            public ArrayBinding(String name) {
                this.name = name;
                this.defaultValue = null;
            }

            /**
             * Constructs an array binding with a default value.
             *
             * @param name the variable name
             * @param defaultValue the default value (may be null)
             */
            public ArrayBinding(String name,
                    ArkTSExpression defaultValue) {
                this.name = name;
                this.defaultValue = defaultValue;
            }

            public String getName() {
                return name;
            }

            public ArkTSExpression getDefaultValue() {
                return defaultValue;
            }

            /**
             * Returns the ArkTS source text for this binding.
             *
             * @return the binding string
             */
            public String toArkTS() {
                if (defaultValue != null) {
                    return name + " = " + defaultValue.toArkTS();
                }
                return name;
            }
        }

        /**
         * Constructs an array destructuring expression with simple
         * string bindings (no defaults).
         *
         * @param bindings the variable names for positional bindings
         * @param restBinding the rest variable name (may be null)
         * @param source the source expression being destructured
         */
        public ArrayDestructuringExpression(List<String> bindings,
                String restBinding, ArkTSExpression source) {
            List<ArrayBinding> converted = new ArrayList<>();
            for (String b : bindings) {
                converted.add(new ArrayBinding(b));
            }
            this.bindings = Collections.unmodifiableList(converted);
            this.restBinding = restBinding;
            this.source = source;
        }

        /**
         * Constructs an array destructuring expression with structured
         * bindings (supports defaults).
         *
         * @param bindings the array bindings
         * @param restBinding the rest variable name (may be null)
         * @param source the source expression being destructured
         */
        public ArrayDestructuringExpression(
                List<ArrayBinding> bindings,
                String restBinding, ArkTSExpression source,
                boolean useStructuredBindings) {
            this.bindings = Collections.unmodifiableList(
                    new ArrayList<>(bindings));
            this.restBinding = restBinding;
            this.source = source;
        }

        public List<ArrayBinding> getBindings() {
            return bindings;
        }

        public String getRestBinding() {
            return restBinding;
        }

        public ArkTSExpression getSource() {
            return source;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < bindings.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(bindings.get(i).toArkTS());
            }
            if (restBinding != null) {
                if (!bindings.isEmpty()) {
                    sb.append(", ");
                }
                sb.append("...").append(restBinding);
            }
            sb.append("]");
            if (source != null) {
                sb.append(" = ").append(source.toArkTS());
            }
            return sb.toString();
        }
    }

    // --- Object destructuring ---

    /**
     * An object destructuring expression: { prop1, prop2: alias } = source.
     */
    public static class ObjectDestructuringExpression extends ArkTSExpression {
        private final List<DestructuringBinding> bindings;
        private final ArkTSExpression source;

        /**
         * Constructs an object destructuring expression.
         *
         * @param bindings the property bindings
         * @param source the source expression
         */
        public ObjectDestructuringExpression(
                List<DestructuringBinding> bindings,
                ArkTSExpression source) {
            this.bindings = Collections.unmodifiableList(
                    new ArrayList<>(bindings));
            this.source = source;
        }

        public List<DestructuringBinding> getBindings() {
            return bindings;
        }

        public ArkTSExpression getSource() {
            return source;
        }

        @Override
        public String toArkTS() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (int i = 0; i < bindings.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(bindings.get(i).toArkTS());
            }
            sb.append(" }");
            if (source != null) {
                sb.append(" = ").append(source.toArkTS());
            }
            return sb.toString();
        }

        /**
         * A single binding in an object destructuring pattern.
         */
        public static class DestructuringBinding {
            private final String property;
            private final String alias;
            private final ArkTSExpression defaultValue;

            /**
             * Constructs a destructuring binding without a default value.
             *
             * @param property the property name
             * @param alias the alias (may be null if same as property)
             */
            public DestructuringBinding(String property, String alias) {
                this.property = property;
                this.alias = alias;
                this.defaultValue = null;
            }

            /**
             * Constructs a destructuring binding with a default value.
             *
             * @param property the property name
             * @param alias the alias (may be null if same as property)
             * @param defaultValue the default value expression (may be null)
             */
            public DestructuringBinding(String property, String alias,
                    ArkTSExpression defaultValue) {
                this.property = property;
                this.alias = alias;
                this.defaultValue = defaultValue;
            }

            public String getProperty() {
                return property;
            }

            public String getAlias() {
                return alias;
            }

            public ArkTSExpression getDefaultValue() {
                return defaultValue;
            }

            /**
             * Returns the ArkTS source text for this binding.
             *
             * @return the binding string
             */
            public String toArkTS() {
                StringBuilder sb = new StringBuilder();
                if (alias != null && !alias.equals(property)) {
                    sb.append(property).append(": ").append(alias);
                } else {
                    sb.append(property);
                }
                if (defaultValue != null) {
                    sb.append(" = ").append(defaultValue.toArkTS());
                }
                return sb.toString();
            }
        }
    }

    // --- Nullish coalescing ---

    /**
     * A nullish coalescing expression: left ?? right.
     * Returns the left operand if it is not null/undefined,
     * otherwise returns the right operand.
     */
    public static class NullishCoalescingExpression extends ArkTSExpression {
        private final ArkTSExpression left;
        private final ArkTSExpression right;

        /**
         * Constructs a nullish coalescing expression.
         *
         * @param left the left operand (the checked value)
         * @param right the right operand (the fallback)
         */
        public NullishCoalescingExpression(ArkTSExpression left,
                ArkTSExpression right) {
            this.left = left;
            this.right = right;
        }

        public ArkTSExpression getLeft() {
            return left;
        }

        public ArkTSExpression getRight() {
            return right;
        }

        @Override
        public String toArkTS() {
            return left.toArkTS() + " ?? " + right.toArkTS();
        }
    }

    // --- Super expression ---

    /**
     * The {@code super} keyword expression.
     *
     * <p>Used for super property access (super.prop, super[expr]) and
     * super constructor calls (super()). Represents the parent class
     * context in ArkTS class inheritance.
     */
    public static class SuperExpression extends ArkTSExpression {
        @Override
        public String toArkTS() {
            return "super";
        }
    }

    // --- Define property expression ---

    /**
     * A define property expression: Object.defineProperty(obj, prop, value).
     *
     * <p>Represents the Ark bytecode {@code definepropertybyname} instruction
     * which defines a property on an object with specific attributes
     * (writable, enumerable, configurable). Distinct from simple field
     * definition ({@code definefieldbyname}) which is a plain assignment.
     */
    public static class DefinePropertyExpression extends ArkTSExpression {
        private final ArkTSExpression object;
        private final ArkTSExpression property;
        private final ArkTSExpression value;

        /**
         * Constructs a define property expression.
         *
         * @param object the target object
         * @param property the property name expression
         * @param value the value expression
         */
        public DefinePropertyExpression(ArkTSExpression object,
                ArkTSExpression property, ArkTSExpression value) {
            this.object = object;
            this.property = property;
            this.value = value;
        }

        public ArkTSExpression getObject() {
            return object;
        }

        public ArkTSExpression getProperty() {
            return property;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS() {
            return "Object.defineProperty(" + object.toArkTS() + ", "
                    + property.toArkTS() + ", { value: "
                    + value.toArkTS() + " })";
        }
    }

    // --- Template object expression ---

    /**
     * A template object reference expression.
     *
     * <p>Represents the result of {@code gettemplateobject} which retrieves
     * the frozen template strings array associated with a tagged template
     * literal. Used as the first argument to tag functions.
     */
    public static class TemplateObjectExpression extends ArkTSExpression {
        private final int templateIndex;

        /**
         * Constructs a template object expression.
         *
         * @param templateIndex the template literal index
         */
        public TemplateObjectExpression(int templateIndex) {
            this.templateIndex = templateIndex;
        }

        public int getTemplateIndex() {
            return templateIndex;
        }

        @Override
        public String toArkTS() {
            return "/* template_" + templateIndex + " */";
        }
    }

    // --- Type predicate expression ---

    /**
     * A type predicate expression: expr is Type.
     *
     * <p>Used in type guard functions to narrow types at compile time.
     * For example: {@code function isString(x: unknown): x is string}.
     */
    public static class TypePredicateExpression extends ArkTSExpression {
        private final ArkTSExpression expression;
        private final String typeName;

        /**
         * Constructs a type predicate expression.
         *
         * @param expression the expression being tested
         * @param typeName the target type name
         */
        public TypePredicateExpression(ArkTSExpression expression,
                String typeName) {
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
            return expression.toArkTS() + " is " + typeName;
        }
    }

    // --- Const assertion expression ---

    /**
     * A const assertion expression: expr as const.
     *
     * <p>Marks a literal or array as deeply readonly, inferring the
     * narrowest possible type. For example:
     * {@code const arr = [1, 2, 3] as const;}
     */
    public static class ConstAssertionExpression extends ArkTSExpression {
        private final ArkTSExpression expression;

        /**
         * Constructs a const assertion expression.
         *
         * @param expression the expression being asserted as const
         */
        public ConstAssertionExpression(ArkTSExpression expression) {
            this.expression = expression;
        }

        public ArkTSExpression getExpression() {
            return expression;
        }

        @Override
        public String toArkTS() {
            return expression.toArkTS() + " as const";
        }
    }

    // --- Satisfies expression ---

    /**
     * A satisfies expression: expr satisfies Type.
     *
     * <p>Validates that an expression matches a type without widening
     * or changing the inferred type. For example:
     * {@code const config = { color: "blue" } satisfies Config;}
     */
    public static class SatisfiesExpression extends ArkTSExpression {
        private final ArkTSExpression expression;
        private final String typeName;

        /**
         * Constructs a satisfies expression.
         *
         * @param expression the expression being validated
         * @param typeName the type to satisfy
         */
        public SatisfiesExpression(ArkTSExpression expression,
                String typeName) {
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
            return expression.toArkTS() + " satisfies " + typeName;
        }
    }

    // --- Static field expression ---

    /**
     * A static field definition expression: static obj.prop = value.
     *
     * <p>Emitted when {@code definefieldbyname} has the static flag set
     * in its flags byte, indicating a static class field assignment.
     */
    public static class StaticFieldExpression extends ArkTSExpression {
        private final ArkTSExpression target;
        private final ArkTSExpression value;

        /**
         * Constructs a static field expression.
         *
         * @param target the member expression being assigned to
         * @param value the value expression
         */
        public StaticFieldExpression(ArkTSExpression target,
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
            return "static " + target.toArkTS() + " = " + value.toArkTS();
        }
    }
}
