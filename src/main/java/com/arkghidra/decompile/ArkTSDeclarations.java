package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Core declaration statement AST nodes for ArkTS decompilation.
 *
 * <p>Contains function, class, field, method, constructor, import, and export
 * declarations. All classes extend {@link ArkTSStatement}.
 *
 * <p>Additional type-related declarations (enum, interface, struct, etc.) are
 * in {@link ArkTSTypeDeclarations}.
 */
public class ArkTSDeclarations {

    private ArkTSDeclarations() {
        // utility class — do not instantiate
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

        public List<FunctionParam> getParams() {
            return params;
        }

        public String getReturnType() {
            return returnType;
        }

        public ArkTSStatement getBody() {
            return body;
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
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body)
                        .toArkTS(indent));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(indent + 1)).append("\n");
                sb.append(indent(indent)).append("}");
            }
            return sb.toString();
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
            for (int i = 0; i < members.size(); i++) {
                sb.append(members.get(i).toArkTS(indent + 1)).append("\n");
                // Add blank line between methods/constructors
                if (i < members.size() - 1) {
                    sb.append("\n");
                }
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
        private final List<String> decorators;

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
            this(name, typeName, initializer, isStatic, accessModifier,
                    Collections.emptyList());
        }

        /**
         * Constructs a class field declaration with decorators.
         *
         * @param name the field name
         * @param typeName the type annotation (may be null)
         * @param initializer the initializer expression (may be null)
         * @param isStatic true if static
         * @param accessModifier the access modifier (may be null)
         * @param decorators the decorator names (may be empty)
         */
        public ClassFieldDeclaration(String name, String typeName,
                ArkTSExpression initializer, boolean isStatic,
                String accessModifier, List<String> decorators) {
            this.name = name;
            this.typeName = typeName;
            this.initializer = initializer;
            this.isStatic = isStatic;
            this.accessModifier = accessModifier;
            this.decorators = decorators != null
                    ? Collections.unmodifiableList(
                            new ArrayList<>(decorators))
                    : Collections.emptyList();
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        public ArkTSExpression getInitializer() {
            return initializer;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public String getAccessModifier() {
            return accessModifier;
        }

        public List<String> getDecorators() {
            return decorators;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            for (String dec : decorators) {
                sb.append(indent(indent)).append("@").append(dec)
                        .append("\n");
            }
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

        public List<FunctionDeclaration.FunctionParam> getParams() {
            return params;
        }

        public String getReturnType() {
            return returnType;
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
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body)
                        .toArkTS(indent));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(indent + 1)).append("\n");
                sb.append(indent(indent)).append("}");
            }
            return sb.toString();
        }
    }

    // --- Constructor declaration ---

    /**
     * A constructor declaration statement within a class body.
     */
    public static class ConstructorDeclaration extends ArkTSStatement {
        private final List<FunctionDeclaration.FunctionParam> params;
        private final ArkTSStatement body;

        /**
         * Constructs a constructor declaration.
         *
         * @param params the constructor parameters
         * @param body the constructor body
         */
        public ConstructorDeclaration(
                List<FunctionDeclaration.FunctionParam> params,
                ArkTSStatement body) {
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
            this.body = body;
        }

        public List<FunctionDeclaration.FunctionParam> getParams() {
            return params;
        }

        public ArkTSStatement getBody() {
            return body;
        }

        @Override
        public String toArkTS(int indent) {
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (FunctionDeclaration.FunctionParam p : params) {
                paramJoiner.add(p.toString());
            }
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("constructor(")
                    .append(paramJoiner).append(") ");
            if (body instanceof ArkTSStatement.BlockStatement) {
                sb.append(((ArkTSStatement.BlockStatement) body)
                        .toArkTS(indent));
            } else {
                sb.append("{\n");
                sb.append(body.toArkTS(indent + 1)).append("\n");
                sb.append(indent(indent)).append("}");
            }
            return sb.toString();
        }
    }

    // --- Namespace statement ---

    /**
     * A namespace declaration: namespace com.example { ... }.
     * Groups related classes, functions, and other declarations.
     */
    public static class NamespaceStatement extends ArkTSStatement {
        private final String name;
        private final List<ArkTSStatement> members;

        /**
         * Constructs a namespace statement.
         *
         * @param name the fully-qualified namespace name (e.g. "com.example")
         * @param members the declarations inside the namespace
         */
        public NamespaceStatement(String name,
                List<ArkTSStatement> members) {
            this.name = name;
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public List<ArkTSStatement> getMembers() {
            return members;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("namespace ")
                    .append(name).append(" {\n");
            for (int i = 0; i < members.size(); i++) {
                sb.append(members.get(i).toArkTS(indent + 1));
                if (i < members.size() - 1) {
                    sb.append("\n\n");
                }
            }
            if (!members.isEmpty()) {
                sb.append("\n");
            }
            sb.append(indent(indent)).append("}");
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
                if (!imports.isEmpty() || namespaceImport != null) {
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
}
