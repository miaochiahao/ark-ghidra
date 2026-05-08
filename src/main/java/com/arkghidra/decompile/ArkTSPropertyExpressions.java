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
        private final List<String> bindings;
        private final String restBinding;
        private final ArkTSExpression source;

        /**
         * Constructs an array destructuring expression.
         *
         * @param bindings the variable names for positional bindings
         * @param restBinding the rest variable name (may be null)
         * @param source the source expression being destructured
         */
        public ArrayDestructuringExpression(List<String> bindings,
                String restBinding, ArkTSExpression source) {
            this.bindings = Collections.unmodifiableList(
                    new ArrayList<>(bindings));
            this.restBinding = restBinding;
            this.source = source;
        }

        public List<String> getBindings() {
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
                sb.append(bindings.get(i));
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

            /**
             * Constructs a destructuring binding.
             *
             * @param property the property name
             * @param alias the alias (may be null if same as property)
             */
            public DestructuringBinding(String property, String alias) {
                this.property = property;
                this.alias = alias;
            }

            public String getProperty() {
                return property;
            }

            public String getAlias() {
                return alias;
            }

            /**
             * Returns the ArkTS source text for this binding.
             *
             * @return the binding string
             */
            public String toArkTS() {
                if (alias != null && !alias.equals(property)) {
                    return property + ": " + alias;
                }
                return property;
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
            return "(" + left.toArkTS() + " ?? " + right.toArkTS() + ")";
        }
    }
}
