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
            appendBlockBody(sb, body, indent + 1);
            sb.append(indent(indent)).append("} while (")
                    .append(condition.toArkTS()).append(");");
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

    // --- Class declaration ---

    /**
     * A class declaration statement.
     */
    public static class ClassDeclaration extends ArkTSStatement {
        private final String name;
        private final String superClass;
        private final List<ArkTSStatement> members;

        /**
         * A class member (field, constructor, or method).
         */
        public static class ClassMember {
            private final String kind;
            private final String name;
            private final String typeName;
            private final List<FunctionDeclaration.FunctionParam> params;
            private final ArkTSStatement body;
            private final boolean isStatic;
            private final String accessModifier;

            /**
             * Constructs a class member.
             *
             * @param kind "field", "constructor", or "method"
             * @param name the member name
             * @param typeName the type annotation (may be null)
             * @param params the method parameters (may be empty)
             * @param body the method body (may be null for abstract)
             * @param isStatic true if the member is static
             * @param accessModifier "public", "private", "protected", or null
             */
            public ClassMember(String kind, String name, String typeName,
                    List<FunctionDeclaration.FunctionParam> params,
                    ArkTSStatement body, boolean isStatic,
                    String accessModifier) {
                this.kind = kind;
                this.name = name;
                this.typeName = typeName;
                this.params = Collections.unmodifiableList(
                        new ArrayList<>(params));
                this.body = body;
                this.isStatic = isStatic;
                this.accessModifier = accessModifier;
            }

            public String getKind() {
                return kind;
            }

            public String getName() {
                return name;
            }

            public String getTypeName() {
                return typeName;
            }

            public List<FunctionDeclaration.FunctionParam> getParams() {
                return params;
            }

            public ArkTSStatement getBody() {
                return body;
            }

            public boolean isStatic() {
                return isStatic;
            }

            public String getAccessModifier() {
                return accessModifier;
            }
        }

        /**
         * Constructs a class declaration.
         *
         * @param name the class name
         * @param superClass the super class name (may be null)
         * @param members the class members
         */
        public ClassDeclaration(String name, String superClass,
                List<ArkTSStatement> members) {
            this.name = name;
            this.superClass = superClass;
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public String getSuperClass() {
            return superClass;
        }

        public List<ArkTSStatement> getMembers() {
            return members;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("class ").append(name);
            if (superClass != null) {
                sb.append(" extends ").append(superClass);
            }
            sb.append(" {\n");
            for (ArkTSStatement member : members) {
                sb.append(member.toArkTS(indent + 1)).append("\n");
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Class field declaration ---

    /**
     * A class field declaration statement within a class body.
     */
    public static class ClassFieldDeclaration extends ArkTSStatement {
        private final String name;
        private final String typeName;
        private final ArkTSExpression initializer;
        private final boolean isStatic;
        private final String accessModifier;

        /**
         * Constructs a class field declaration.
         *
         * @param name the field name
         * @param typeName the type annotation (may be null)
         * @param initializer the initializer expression (may be null)
         * @param isStatic true if static
         * @param accessModifier the access modifier (may be null)
         */
        public ClassFieldDeclaration(String name, String typeName,
                ArkTSExpression initializer, boolean isStatic,
                String accessModifier) {
            this.name = name;
            this.typeName = typeName;
            this.initializer = initializer;
            this.isStatic = isStatic;
            this.accessModifier = accessModifier;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent));
            if (accessModifier != null) {
                sb.append(accessModifier).append(" ");
            }
            if (isStatic) {
                sb.append("static ");
            }
            sb.append(name);
            if (typeName != null) {
                sb.append(": ").append(typeName);
            }
            if (initializer != null) {
                sb.append(" = ").append(initializer.toArkTS());
            }
            sb.append(";");
            return sb.toString();
        }
    }

    // --- Class method declaration ---

    /**
     * A class method declaration statement within a class body.
     */
    public static class ClassMethodDeclaration extends ArkTSStatement {
        private final String name;
        private final List<FunctionDeclaration.FunctionParam> params;
        private final String returnType;
        private final ArkTSStatement body;
        private final boolean isStatic;
        private final String accessModifier;

        /**
         * Constructs a class method declaration.
         *
         * @param name the method name
         * @param params the parameters
         * @param returnType the return type (may be null)
         * @param body the method body
         * @param isStatic true if static
         * @param accessModifier the access modifier (may be null)
         */
        public ClassMethodDeclaration(String name,
                List<FunctionDeclaration.FunctionParam> params,
                String returnType, ArkTSStatement body, boolean isStatic,
                String accessModifier) {
            this.name = name;
            this.params = Collections.unmodifiableList(
                    new ArrayList<>(params));
            this.returnType = returnType;
            this.body = body;
            this.isStatic = isStatic;
            this.accessModifier = accessModifier;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent));
            if (accessModifier != null) {
                sb.append(accessModifier).append(" ");
            }
            if (isStatic) {
                sb.append("static ");
            }
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (FunctionDeclaration.FunctionParam p : params) {
                paramJoiner.add(p.toString());
            }
            sb.append(name).append("(").append(paramJoiner).append(")");
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

    // --- Import statement ---

    /**
     * An import statement: import { X, Y } from 'module'.
     */
    public static class ImportStatement extends ArkTSStatement {
        private final List<String> imports;
        private final String modulePath;
        private final boolean isDefault;
        private final String defaultImport;
        private final String namespaceImport;

        /**
         * Constructs an import statement.
         *
         * @param imports the named imports
         * @param modulePath the module path
         * @param isDefault true if this is a default import
         * @param defaultImport the default import name (may be null)
         * @param namespaceImport the namespace import name (may be null)
         */
        public ImportStatement(List<String> imports, String modulePath,
                boolean isDefault, String defaultImport,
                String namespaceImport) {
            this.imports = Collections.unmodifiableList(
                    new ArrayList<>(imports));
            this.modulePath = modulePath;
            this.isDefault = isDefault;
            this.defaultImport = defaultImport;
            this.namespaceImport = namespaceImport;
        }

        public List<String> getImports() {
            return imports;
        }

        public String getModulePath() {
            return modulePath;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("import ");
            if (defaultImport != null) {
                sb.append(defaultImport);
                if (!imports.isEmpty()) {
                    sb.append(", ");
                }
            }
            if (namespaceImport != null) {
                sb.append("* as ").append(namespaceImport);
            } else if (!imports.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ");
                for (String imp : imports) {
                    joiner.add(imp);
                }
                sb.append("{ ").append(joiner).append(" }");
            }
            sb.append(" from '").append(modulePath).append("';");
            return sb.toString();
        }
    }

    // --- Export statement ---

    /**
     * An export statement: export { X, Y } or export default expr.
     */
    public static class ExportStatement extends ArkTSStatement {
        private final List<String> exports;
        private final ArkTSStatement declaration;
        private final boolean isDefault;

        /**
         * Constructs an export statement.
         *
         * @param exports the named exports
         * @param declaration the declaration being exported (may be null)
         * @param isDefault true if this is a default export
         */
        public ExportStatement(List<String> exports,
                ArkTSStatement declaration, boolean isDefault) {
            this.exports = Collections.unmodifiableList(
                    new ArrayList<>(exports));
            this.declaration = declaration;
            this.isDefault = isDefault;
        }

        public List<String> getExports() {
            return exports;
        }

        public ArkTSStatement getDeclaration() {
            return declaration;
        }

        public boolean isDefault() {
            return isDefault;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("export ");
            if (isDefault) {
                sb.append("default ");
                if (declaration != null) {
                    sb.append(declaration.toArkTS(0));
                }
                return sb.toString();
            }
            if (declaration != null) {
                sb.append(declaration.toArkTS(0));
                return sb.toString();
            }
            StringJoiner joiner = new StringJoiner(", ");
            for (String exp : exports) {
                joiner.add(exp);
            }
            sb.append("{ ").append(joiner).append(" };");
            return sb.toString();
        }
    }

    // --- Enum declaration ---

    /**
     * An enum declaration statement.
     */
    public static class EnumDeclaration extends ArkTSStatement {
        private final String name;
        private final List<EnumMember> members;

        /**
         * A member of an enum.
         */
        public static class EnumMember {
            private final String name;
            private final ArkTSExpression value;

            /**
             * Constructs an enum member.
             *
             * @param name the member name
             * @param value the member value (may be null for auto-increment)
             */
            public EnumMember(String name, ArkTSExpression value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public ArkTSExpression getValue() {
                return value;
            }
        }

        /**
         * Constructs an enum declaration.
         *
         * @param name the enum name
         * @param members the enum members
         */
        public EnumDeclaration(String name, List<EnumMember> members) {
            this.name = name;
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public List<EnumMember> getMembers() {
            return members;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("enum ").append(name)
                    .append(" {\n");
            for (int i = 0; i < members.size(); i++) {
                EnumMember m = members.get(i);
                sb.append(indent(indent + 1)).append(m.name);
                if (m.value != null) {
                    sb.append(" = ").append(m.value.toArkTS());
                }
                if (i < members.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Interface declaration ---

    /**
     * An interface declaration statement.
     */
    public static class InterfaceDeclaration extends ArkTSStatement {
        private final String name;
        private final List<String> extendsInterfaces;
        private final List<InterfaceMember> members;

        /**
         * A member of an interface (property or method signature).
         */
        public static class InterfaceMember {
            private final String kind;
            private final String name;
            private final String typeName;
            private final List<FunctionDeclaration.FunctionParam> params;
            private final boolean isOptional;

            /**
             * Constructs an interface member.
             *
             * @param kind "property" or "method"
             * @param name the member name
             * @param typeName the type annotation (may be null)
             * @param params the method parameters (may be empty)
             * @param isOptional true if the member is optional
             */
            public InterfaceMember(String kind, String name, String typeName,
                    List<FunctionDeclaration.FunctionParam> params,
                    boolean isOptional) {
                this.kind = kind;
                this.name = name;
                this.typeName = typeName;
                this.params = Collections.unmodifiableList(
                        new ArrayList<>(params));
                this.isOptional = isOptional;
            }

            public String getKind() {
                return kind;
            }

            public String getName() {
                return name;
            }
        }

        /**
         * Constructs an interface declaration.
         *
         * @param name the interface name
         * @param extendsInterfaces the extended interfaces (may be empty)
         * @param members the interface members
         */
        public InterfaceDeclaration(String name,
                List<String> extendsInterfaces,
                List<InterfaceMember> members) {
            this.name = name;
            this.extendsInterfaces = Collections.unmodifiableList(
                    new ArrayList<>(extendsInterfaces));
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public List<String> getExtendsInterfaces() {
            return extendsInterfaces;
        }

        public List<InterfaceMember> getMembers() {
            return members;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("interface ").append(name);
            if (!extendsInterfaces.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ");
                for (String ext : extendsInterfaces) {
                    joiner.add(ext);
                }
                sb.append(" extends ").append(joiner);
            }
            sb.append(" {\n");
            for (InterfaceMember m : members) {
                sb.append(indent(indent + 1));
                sb.append(m.name);
                if (m.isOptional) {
                    sb.append("?");
                }
                if ("method".equals(m.kind)) {
                    StringJoiner paramJoiner = new StringJoiner(", ");
                    for (FunctionDeclaration.FunctionParam p : m.params) {
                        paramJoiner.add(p.toString());
                    }
                    sb.append("(").append(paramJoiner).append(")");
                }
                if (m.typeName != null) {
                    sb.append(": ").append(m.typeName);
                }
                sb.append(";\n");
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Decorator ---

    /**
     * A decorator expression: @DecoratorName or @DecoratorName(args).
     */
    public static class DecoratorStatement extends ArkTSStatement {
        private final String name;
        private final List<ArkTSExpression> arguments;

        /**
         * Constructs a decorator statement.
         *
         * @param name the decorator name
         * @param arguments the decorator arguments (may be empty)
         */
        public DecoratorStatement(String name,
                List<ArkTSExpression> arguments) {
            this.name = name;
            this.arguments = Collections.unmodifiableList(
                    new ArrayList<>(arguments));
        }

        public String getName() {
            return name;
        }

        public List<ArkTSExpression> getArguments() {
            return arguments;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("@").append(name);
            if (!arguments.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ");
                for (ArkTSExpression arg : arguments) {
                    joiner.add(arg.toArkTS());
                }
                sb.append("(").append(joiner).append(")");
            }
            return sb.toString();
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
