package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

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

    // --- If ---

    /**
     * An if/else statement.
     */
    public static class IfStatement extends ArkTSStatement {
        private final ArkTSExpression condition;
        private final ArkTSStatement thenBlock;
        private final ArkTSStatement elseBlock;

        /**
         * Constructs an if statement.
         *
         * @param condition the condition expression
         * @param thenBlock the then-branch
         * @param elseBlock the else-branch (may be null)
         */
        public IfStatement(ArkTSExpression condition,
                ArkTSStatement thenBlock, ArkTSStatement elseBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }

        public ArkTSExpression getCondition() {
            return condition;
        }

        public ArkTSStatement getThenBlock() {
            return thenBlock;
        }

        public ArkTSStatement getElseBlock() {
            return elseBlock;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("if (")
                    .append(condition.toArkTS()).append(") {\n");
            appendBlockBody(sb, thenBlock, indent + 1);
            sb.append(indent(indent)).append("}");
            if (elseBlock != null) {
                sb.append(" else ");
                if (elseBlock instanceof IfStatement) {
                    // else if
                    sb.append(elseBlock.toArkTS(indent));
                } else {
                    sb.append("{\n");
                    appendBlockBody(sb, elseBlock, indent + 1);
                    sb.append(indent(indent)).append("}");
                }
            }
            return sb.toString();
        }
    }

    // --- For ---

    /**
     * A for loop statement.
     */
    public static class ForStatement extends ArkTSStatement {
        private final ArkTSStatement init;
        private final ArkTSExpression condition;
        private final ArkTSExpression update;
        private final ArkTSStatement body;

        /**
         * Constructs a for statement.
         *
         * @param init the initialization (may be null)
         * @param condition the loop condition (may be null)
         * @param update the update expression (may be null)
         * @param body the loop body
         */
        public ForStatement(ArkTSStatement init,
                ArkTSExpression condition, ArkTSExpression update,
                ArkTSStatement body) {
            this.init = init;
            this.condition = condition;
            this.update = update;
            this.body = body;
        }

        @Override
        public String toArkTS(int indent) {
            String initStr = init != null
                    ? formatForInit(init) : "";
            String condStr = condition != null
                    ? condition.toArkTS() : "";
            String updateStr = update != null
                    ? update.toArkTS() : "";
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("for (")
                    .append(initStr).append("; ")
                    .append(condStr).append("; ")
                    .append(updateStr).append(") {\n");
            appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }

        private static String formatForInit(ArkTSStatement s) {
            if (s instanceof ExpressionStatement) {
                return ((ExpressionStatement) s).getExpression().toArkTS();
            }
            if (s instanceof VariableDeclaration) {
                VariableDeclaration decl = (VariableDeclaration) s;
                return decl.toForInitString();
            }
            return s.toArkTS(0).replace(";", "");
        }
    }

    // --- While ---

    /**
     * A while loop statement.
     */
    public static class WhileStatement extends ArkTSStatement {
        private final ArkTSExpression condition;
        private final ArkTSStatement body;

        /**
         * Constructs a while statement.
         *
         * @param condition the loop condition
         * @param body the loop body
         */
        public WhileStatement(ArkTSExpression condition,
                ArkTSStatement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("while (")
                    .append(condition.toArkTS()).append(") {\n");
            appendBlockBody(sb, body, indent + 1);
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

        @Override
        public String toArkTS(int indent) {
            return indent(indent) + "throw " + value.toArkTS() + ";";
        }
    }

    // --- Try/Catch ---

    /**
     * A try/catch/finally statement.
     */
    public static class TryCatchStatement extends ArkTSStatement {
        private final ArkTSStatement tryBlock;
        private final String catchParam;
        private final ArkTSStatement catchBlock;
        private final ArkTSStatement finallyBlock;

        /**
         * Constructs a try/catch statement.
         *
         * @param tryBlock the try body
         * @param catchParam the catch parameter name (may be null)
         * @param catchBlock the catch body (may be null)
         * @param finallyBlock the finally body (may be null)
         */
        public TryCatchStatement(ArkTSStatement tryBlock,
                String catchParam, ArkTSStatement catchBlock,
                ArkTSStatement finallyBlock) {
            this.tryBlock = tryBlock;
            this.catchParam = catchParam;
            this.catchBlock = catchBlock;
            this.finallyBlock = finallyBlock;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("try {\n");
            appendBlockBody(sb, tryBlock, indent + 1);
            sb.append(indent(indent)).append("}");
            if (catchBlock != null) {
                sb.append(" catch (");
                sb.append(catchParam != null ? catchParam : "e");
                sb.append(") {\n");
                appendBlockBody(sb, catchBlock, indent + 1);
                sb.append(indent(indent)).append("}");
            }
            if (finallyBlock != null) {
                sb.append(" finally {\n");
                appendBlockBody(sb, finallyBlock, indent + 1);
                sb.append(indent(indent)).append("}");
            }
            return sb.toString();
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

    // --- Function declaration ---

    /**
     * A function declaration.
     */
    public static class FunctionDeclaration extends ArkTSStatement {
        private final String name;
        private final List<FunctionParam> params;
        private final String returnType;
        private final ArkTSStatement body;

        /**
         * A function parameter.
         */
        public static class FunctionParam {
            private final String name;
            private final String typeName;

            /**
             * Constructs a function parameter.
             *
             * @param name the parameter name
             * @param typeName the type annotation (may be null)
             */
            public FunctionParam(String name, String typeName) {
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
            public String toString() {
                if (typeName != null) {
                    return name + ": " + typeName;
                }
                return name;
            }
        }

        /**
         * Constructs a function declaration.
         *
         * @param name the function name
         * @param params the parameters
         * @param returnType the return type (may be null)
         * @param body the function body (typically a BlockStatement)
         */
        public FunctionDeclaration(String name,
                List<FunctionParam> params, String returnType,
                ArkTSStatement body) {
            this.name = name;
            this.params = Collections.unmodifiableList(
                    new ArrayList<>(params));
            this.returnType = returnType;
            this.body = body;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toArkTS(int indent) {
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (FunctionParam p : params) {
                paramJoiner.add(p.toString());
            }
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("function ")
                    .append(name).append("(")
                    .append(paramJoiner).append(")");
            if (returnType != null) {
                sb.append(": ").append(returnType);
            }
            sb.append(" ");
            if (body instanceof BlockStatement) {
                sb.append(((BlockStatement) body).toArkTS(indent));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(indent + 1)).append("\n");
                sb.append(indent(indent)).append("}");
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
    private static void appendBlockBody(StringBuilder sb,
            ArkTSStatement body, int indent) {
        if (body instanceof BlockStatement) {
            for (ArkTSStatement stmt : ((BlockStatement) body).getBody()) {
                sb.append(stmt.toArkTS(indent)).append("\n");
            }
        } else {
            sb.append(body.toArkTS(indent)).append("\n");
        }
    }
}
