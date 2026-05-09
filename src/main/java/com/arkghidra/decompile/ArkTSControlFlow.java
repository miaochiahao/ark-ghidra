package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Control flow statement AST nodes for ArkTS decompilation.
 *
 * <p>Contains if/else, loops, switch, try/catch, and super call statements.
 * All classes extend {@link ArkTSStatement}.
 */
public class ArkTSControlFlow {

    private ArkTSControlFlow() {
        // utility class — do not instantiate
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
            ArkTSStatement.appendBlockBody(sb, thenBlock, indent + 1);
            sb.append(indent(indent)).append("}");
            if (elseBlock != null) {
                sb.append(" else ");
                if (elseBlock instanceof IfStatement) {
                    // else if
                    sb.append(elseBlock.toArkTS(indent));
                } else {
                    sb.append("{\n");
                    ArkTSStatement.appendBlockBody(sb, elseBlock, indent + 1);
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
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }

        private static String formatForInit(ArkTSStatement s) {
            if (s instanceof ArkTSStatement.ExpressionStatement) {
                return ((ArkTSStatement.ExpressionStatement) s)
                        .getExpression().toArkTS();
            }
            if (s instanceof ArkTSStatement.VariableDeclaration) {
                ArkTSStatement.VariableDeclaration decl =
                        (ArkTSStatement.VariableDeclaration) s;
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

        public ArkTSExpression getCondition() {
            return condition;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("while (")
                    .append(condition.toArkTS()).append(") {\n");
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- For-Of ---

    /**
     * A for-of loop statement.
     */
    public static class ForOfStatement extends ArkTSStatement {
        private final String variableKind;
        private final String variableName;
        private final ArkTSExpression iterable;
        private final ArkTSStatement body;

        /**
         * Constructs a for-of statement.
         *
         * @param variableKind "let" or "const"
         * @param variableName the loop variable name
         * @param iterable the iterable expression
         * @param body the loop body
         */
        public ForOfStatement(String variableKind, String variableName,
                ArkTSExpression iterable, ArkTSStatement body) {
            this.variableKind = variableKind;
            this.variableName = variableName;
            this.iterable = iterable;
            this.body = body;
        }

        public String getVariableKind() {
            return variableKind;
        }

        public String getVariableName() {
            return variableName;
        }

        public ArkTSExpression getIterable() {
            return iterable;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("for (")
                    .append(variableKind).append(" ")
                    .append(variableName).append(" of ")
                    .append(iterable.toArkTS()).append(") {\n");
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- For-In ---

    /**
     * A for-in loop statement.
     */
    public static class ForInStatement extends ArkTSStatement {
        private final String variableKind;
        private final String variableName;
        private final ArkTSExpression object;
        private final ArkTSStatement body;

        /**
         * Constructs a for-in statement.
         *
         * @param variableKind "let" or "const"
         * @param variableName the loop variable name
         * @param object the object expression
         * @param body the loop body
         */
        public ForInStatement(String variableKind, String variableName,
                ArkTSExpression object, ArkTSStatement body) {
            this.variableKind = variableKind;
            this.variableName = variableName;
            this.object = object;
            this.body = body;
        }

        public String getVariableKind() {
            return variableKind;
        }

        public String getVariableName() {
            return variableName;
        }

        public ArkTSExpression getObject() {
            return object;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("for (")
                    .append(variableKind).append(" ")
                    .append(variableName).append(" in ")
                    .append(object.toArkTS()).append(") {\n");
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- For-Await-Of ---

    /**
     * A for-await-of loop statement: for await (const x of asyncIterable).
     *
     * <p>Used for async iteration over async iterables, detected from
     * the GETASYNCITERATOR opcode pattern in the CFG.
     */
    public static class ForAwaitOfStatement extends ArkTSStatement {
        private final String variableKind;
        private final String variableName;
        private final ArkTSExpression iterable;
        private final ArkTSStatement body;

        /**
         * Constructs a for-await-of statement.
         *
         * @param variableKind "let" or "const"
         * @param variableName the loop variable name
         * @param iterable the async iterable expression
         * @param body the loop body
         */
        public ForAwaitOfStatement(String variableKind,
                String variableName, ArkTSExpression iterable,
                ArkTSStatement body) {
            this.variableKind = variableKind;
            this.variableName = variableName;
            this.iterable = iterable;
            this.body = body;
        }

        public String getVariableKind() {
            return variableKind;
        }

        public String getVariableName() {
            return variableName;
        }

        public ArkTSExpression getIterable() {
            return iterable;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("for await (")
                    .append(variableKind).append(" ")
                    .append(variableName).append(" of ")
                    .append(iterable.toArkTS()).append(") {\n");
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Do/While ---

    /**
     * A do/while loop statement.
     */
    public static class DoWhileStatement extends ArkTSStatement {
        private final ArkTSExpression condition;
        private final ArkTSStatement body;

        /**
         * Constructs a do/while statement.
         *
         * @param body the loop body
         * @param condition the loop condition
         */
        public DoWhileStatement(ArkTSStatement body,
                ArkTSExpression condition) {
            this.body = body;
            this.condition = condition;
        }

        public ArkTSExpression getCondition() {
            return condition;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("do {\n");
            ArkTSStatement.appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("} while (")
                    .append(condition.toArkTS()).append(");");
            return sb.toString();
        }
    }

    // --- Try/Catch ---

    /**
     * A try/catch/finally statement.
     */
    public static class TryCatchStatement extends ArkTSStatement {
        private final ArkTSStatement tryBlock;
        private final String catchParam;
        private final String catchParamType;
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
            this(tryBlock, catchParam, null, catchBlock, finallyBlock);
        }

        /**
         * Constructs a try/catch statement with catch parameter type.
         *
         * @param tryBlock the try body
         * @param catchParam the catch parameter name (may be null)
         * @param catchParamType the catch parameter type annotation (may be null)
         * @param catchBlock the catch body (may be null)
         * @param finallyBlock the finally body (may be null)
         */
        public TryCatchStatement(ArkTSStatement tryBlock,
                String catchParam, String catchParamType,
                ArkTSStatement catchBlock,
                ArkTSStatement finallyBlock) {
            this.tryBlock = tryBlock;
            this.catchParam = catchParam;
            this.catchParamType = catchParamType;
            this.catchBlock = catchBlock;
            this.finallyBlock = finallyBlock;
        }

        public ArkTSStatement getTryBlock() {
            return tryBlock;
        }

        public String getCatchParam() {
            return catchParam;
        }

        public String getCatchParamType() {
            return catchParamType;
        }

        public ArkTSStatement getCatchBlock() {
            return catchBlock;
        }

        public ArkTSStatement getFinallyBlock() {
            return finallyBlock;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("try {\n");
            ArkTSStatement.appendBlockBody(sb, tryBlock, indent + 1);
            sb.append(indent(indent)).append("}");
            if (catchBlock != null) {
                sb.append(" catch (");
                String param = catchParam != null ? catchParam : "e";
                sb.append(param);
                if (catchParamType != null) {
                    sb.append(": ").append(catchParamType);
                }
                sb.append(") {\n");
                ArkTSStatement.appendBlockBody(sb, catchBlock, indent + 1);
                sb.append(indent(indent)).append("}");
            }
            if (finallyBlock != null) {
                sb.append(" finally {\n");
                ArkTSStatement.appendBlockBody(sb, finallyBlock, indent + 1);
                sb.append(indent(indent)).append("}");
            }
            return sb.toString();
        }
    }

    // --- Super call statement ---

    /**
     * A super() call statement used in constructors.
     */
    public static class SuperCallStatement extends ArkTSStatement {
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a super call statement.
         *
         * @param arguments the arguments to super()
         */
        public SuperCallStatement(List<ArkTSExpression> arguments) {
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public List<ArkTSExpression> getArguments() {
            return arguments;
        }

        @Override
        public String toArkTS(int indent) {
            StringJoiner joiner = new StringJoiner(", ");
            for (ArkTSExpression arg : arguments) {
                joiner.add(arg.toArkTS());
            }
            return indent(indent) + "super(" + joiner + ");";
        }
    }

    // --- Switch statement ---

    /**
     * A switch/case statement.
     */
    public static class SwitchStatement extends ArkTSStatement {
        private final ArkTSExpression discriminant;
        private final List<SwitchCase> cases;
        private final ArkTSStatement defaultBlock;

        /**
         * A single case clause in a switch statement.
         */
        public static class SwitchCase {
            private final ArkTSExpression test;
            private final List<ArkTSStatement> body;

            /**
             * Constructs a switch case.
             *
             * @param test the test expression (null for default)
             * @param body the case body statements
             */
            public SwitchCase(ArkTSExpression test,
                    List<ArkTSStatement> body) {
                this.test = test;
                this.body = Collections.unmodifiableList(
                        new ArrayList<>(body));
            }

            public ArkTSExpression getTest() {
                return test;
            }

            public List<ArkTSStatement> getBody() {
                return body;
            }
        }

        /**
         * Constructs a switch statement.
         *
         * @param discriminant the expression being switched on
         * @param cases the case clauses
         * @param defaultBlock the default block (may be null)
         */
        public SwitchStatement(ArkTSExpression discriminant,
                List<SwitchCase> cases, ArkTSStatement defaultBlock) {
            this.discriminant = discriminant;
            this.cases = Collections.unmodifiableList(
                    new ArrayList<>(cases));
            this.defaultBlock = defaultBlock;
        }

        public ArkTSExpression getDiscriminant() {
            return discriminant;
        }

        public List<SwitchCase> getCases() {
            return cases;
        }

        public ArkTSStatement getDefaultBlock() {
            return defaultBlock;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("switch (")
                    .append(discriminant.toArkTS()).append(") {\n");
            for (SwitchCase c : cases) {
                sb.append(indent(indent + 1)).append("case ")
                        .append(c.test.toArkTS()).append(":\n");
                for (ArkTSStatement stmt : c.body) {
                    sb.append(stmt.toArkTS(indent + 2)).append("\n");
                }
            }
            if (defaultBlock != null) {
                sb.append(indent(indent + 1)).append("default:\n");
                ArkTSStatement.appendBlockBody(sb, defaultBlock, indent + 2);
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }
}
