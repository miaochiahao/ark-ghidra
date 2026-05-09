package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for ArkTS statement AST nodes.
 *
 * <p>Each statement can produce ArkTS source text via {@link #toArkTS(int)},
 * which accepts an indentation level.
 */
public abstract class ArkTSStatement {

    /**
     * Returns the ArkTS source text for this statement at the given indentation level.
     *
     * @param indent the number of 4-space indent levels
     * @return the ArkTS source string
     */
    public abstract String toArkTS(int indent);

    /**
     * Returns indentation whitespace.
     *
     * @param level the indent level
     * @return a string of (level * 4) spaces
     */
    protected static String indent(int level) {
        return "    ".repeat(Math.max(0, level));
    }

    // --- Expression statement ---

    /**
     * An expression used as a statement: expression;
     */
    public static class ExpressionStatement extends ArkTSStatement {
        private final ArkTSExpression expression;

        /**
         * Constructs an expression statement.
         *
         * @param expression the expression
         */
        public ExpressionStatement(ArkTSExpression expression) {
            this.expression = expression;
        }

        public ArkTSExpression getExpression() {
            return expression;
        }

        @Override
        public String toArkTS(int indent) {
            return indent(indent) + expression.toArkTS() + ";";
        }
    }

    // --- Block ---

    /**
     * A block of statements: { ... }.
     */
    public static class BlockStatement extends ArkTSStatement {
        private final List<ArkTSStatement> body;

        /**
         * Constructs a block statement.
         *
         * @param body the statements in the block
         */
        public BlockStatement(List<ArkTSStatement> body) {
            this.body = Collections.unmodifiableList(new ArrayList<>(body));
        }

        public List<ArkTSStatement> getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            if (body.isEmpty()) {
                return indent(indent) + "{ }";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("{\n");
            for (ArkTSStatement stmt : body) {
                sb.append(stmt.toArkTS(indent + 1)).append("\n");
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Return ---

    /**
     * A return statement.
     */
    public static class ReturnStatement extends ArkTSStatement {
        private final ArkTSExpression value;

        /**
         * Constructs a return statement.
         *
         * @param value the return value (may be null for void return)
         */
        public ReturnStatement(ArkTSExpression value) {
            this.value = value;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS(int indent) {
            if (value != null) {
                return indent(indent) + "return " + value.toArkTS() + ";";
            }
            return indent(indent) + "return;";
        }
    }

    // --- Throw ---

    /**
     * A throw statement.
     */
    public static class ThrowStatement extends ArkTSStatement {
        private final ArkTSExpression value;

        /**
         * Constructs a throw statement.
         *
         * @param value the exception value
         */
        public ThrowStatement(ArkTSExpression value) {
            this.value = value;
        }

        public ArkTSExpression getValue() {
            return value;
        }

        @Override
        public String toArkTS(int indent) {
            return indent(indent) + "throw " + value.toArkTS() + ";";
        }
    }

    // --- Variable declaration ---

    /**
     * A variable declaration: let/const name = value.
     */
    public static class VariableDeclaration extends ArkTSStatement {
        private final String kind;
        private final String name;
        private final String typeName;
        private final ArkTSExpression initializer;

        /**
         * Constructs a variable declaration.
         *
         * @param kind "let" or "const"
         * @param name the variable name
         * @param typeName the type annotation (may be null)
         * @param initializer the initializer expression (may be null)
         */
        public VariableDeclaration(String kind, String name,
                String typeName, ArkTSExpression initializer) {
            this.kind = kind;
            this.name = name;
            this.typeName = typeName;
            this.initializer = initializer;
        }

        public String getName() {
            return name;
        }

        public String getKind() {
            return kind;
        }

        public String getTypeName() {
            return typeName;
        }

        public ArkTSExpression getInitializer() {
            return initializer;
        }

        @Override
        public String toArkTS(int indent) {
            return indent(indent) + toForInitString() + ";";
        }

        /**
         * Returns the declaration without trailing semicolon (for for-loop init).
         *
         * @return the declaration string without semicolon
         */
        public String toForInitString() {
            StringBuilder sb = new StringBuilder();
            sb.append(kind).append(" ").append(name);
            if (typeName != null) {
                sb.append(": ").append(typeName);
            }
            if (initializer != null) {
                sb.append(" = ").append(initializer.toArkTS());
            }
            return sb.toString();
        }
    }

    // --- Break ---

    /**
     * A break statement.
     */
    public static class BreakStatement extends ArkTSStatement {
        @Override
        public String toArkTS(int indent) {
            return indent(indent) + "break;";
        }
    }

    // --- Continue ---

    /**
     * A continue statement.
     */
    public static class ContinueStatement extends ArkTSStatement {
        @Override
        public String toArkTS(int indent) {
            return indent(indent) + "continue;";
        }
    }

    // --- Helper ---

    /**
     * Appends the body of a block-like statement to the string builder.
     * If the body is a BlockStatement, it unwraps one level.
     *
     * @param sb the builder
     * @param body the body statement
     * @param indent the indent level for the body
     */
    static void appendBlockBody(StringBuilder sb,
            ArkTSStatement body, int indent) {
        if (body instanceof BlockStatement) {
            for (ArkTSStatement stmt : ((BlockStatement) body).getBody()) {
                sb.append(stmt.toArkTS(indent)).append("\n");
            }
        } else {
            sb.append(body.toArkTS(indent)).append("\n");
        }
    }

    /**
     * Normalizes blank lines in decompiled output: replaces three or more
     * consecutive newlines with exactly two (one blank line), and strips
     * trailing whitespace from each line.
     *
     * @param text the raw decompiled output
     * @return normalized text with no double-blank lines
     */
    public static String normalizeBlankLines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text.replaceAll("\n{3,}", "\n\n");
        String[] lines = result.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i].stripTrailing());
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
