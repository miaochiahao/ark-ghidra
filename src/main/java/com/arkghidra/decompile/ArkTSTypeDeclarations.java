package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Type-related declaration AST nodes for ArkTS decompilation.
 *
 * <p>Contains enum, interface, struct, decorator, generic class, type parameter,
 * destructuring, and file module declarations. All statement classes extend
 * {@link ArkTSStatement}.
 */
public class ArkTSTypeDeclarations {

    private ArkTSTypeDeclarations() {
        // utility class — do not instantiate
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

    // --- Const enum declaration ---

    /**
     * A const enum declaration statement.
     */
    public static class ConstEnumDeclaration extends ArkTSStatement {
        private final String name;
        private final List<EnumDeclaration.EnumMember> members;

        /**
         * Constructs a const enum declaration.
         *
         * @param name the enum name
         * @param members the enum members
         */
        public ConstEnumDeclaration(String name,
                List<EnumDeclaration.EnumMember> members) {
            this.name = name;
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public List<EnumDeclaration.EnumMember> getMembers() {
            return members;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(indent)).append("const enum ").append(name)
                    .append(" {\n");
            for (int i = 0; i < members.size(); i++) {
                EnumDeclaration.EnumMember m = members.get(i);
                sb.append(indent(indent + 1)).append(m.getName());
                if (m.getValue() != null) {
                    sb.append(" = ").append(m.getValue().toArkTS());
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
            private final List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params;
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
                    List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
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
                    for (ArkTSDeclarations.FunctionDeclaration.FunctionParam p : m.params) {
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

    // --- Struct declaration ---

    /**
     * A struct declaration statement (ArkTS-specific).
     * In ArkTS, structs are declared with the {@code struct} keyword and are used
     * for UI components decorated with @Component.
     */
    public static class StructDeclaration extends ArkTSStatement {
        private final String name;
        private final List<ArkTSStatement> members;
        private final List<String> decorators;

        /**
         * Constructs a struct declaration.
         *
         * @param name the struct name
         * @param members the struct members (fields and methods)
         * @param decorators the decorator names applied to this struct (may be empty)
         */
        public StructDeclaration(String name, List<ArkTSStatement> members,
                List<String> decorators) {
            this.name = name;
            this.members = Collections.unmodifiableList(new ArrayList<>(members));
            this.decorators = Collections.unmodifiableList(new ArrayList<>(decorators));
        }

        public String getName() {
            return name;
        }

        public List<ArkTSStatement> getMembers() {
            return members;
        }

        public List<String> getDecorators() {
            return decorators;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            for (String dec : decorators) {
                sb.append(indent(indent)).append("@").append(dec).append("\n");
            }
            sb.append(indent(indent)).append("struct ").append(name)
                    .append(" {\n");
            for (int i = 0; i < members.size(); i++) {
                sb.append(members.get(i).toArkTS(indent + 1)).append("\n");
                if (i < members.size() - 1) {
                    sb.append("\n");
                }
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Type parameter declaration ---

    /**
     * A type parameter for generic classes or methods: &lt;T&gt; or
     * &lt;T extends Base&gt;.
     */
    public static class TypeParameter {
        private final String name;
        private final String constraint;

        /**
         * Constructs a type parameter.
         *
         * @param name the type parameter name (e.g. "T")
         * @param constraint the constraint type (e.g. "Base"), or null
         */
        public TypeParameter(String name, String constraint) {
            this.name = name;
            this.constraint = constraint;
        }

        public String getName() {
            return name;
        }

        public String getConstraint() {
            return constraint;
        }

        @Override
        public String toString() {
            if (constraint != null) {
                return name + " extends " + constraint;
            }
            return name;
        }
    }

    // --- Generic class declaration ---

    /**
     * A generic class declaration with type parameters.
     */
    public static class GenericClassDeclaration extends ArkTSStatement {
        private final String name;
        private final List<TypeParameter> typeParams;
        private final String superClass;
        private final List<ArkTSStatement> members;

        /**
         * Constructs a generic class declaration.
         *
         * @param name the class name
         * @param typeParams the type parameters (may be empty)
         * @param superClass the super class name (may be null)
         * @param members the class members
         */
        public GenericClassDeclaration(String name,
                List<TypeParameter> typeParams, String superClass,
                List<ArkTSStatement> members) {
            this.name = name;
            this.typeParams = Collections.unmodifiableList(
                    new ArrayList<>(typeParams));
            this.superClass = superClass;
            this.members = Collections.unmodifiableList(
                    new ArrayList<>(members));
        }

        public String getName() {
            return name;
        }

        public List<TypeParameter> getTypeParams() {
            return typeParams;
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
            if (!typeParams.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ");
                for (TypeParameter tp : typeParams) {
                    joiner.add(tp.toString());
                }
                sb.append("<").append(joiner).append(">");
            }
            if (superClass != null) {
                sb.append(" extends ").append(superClass);
            }
            sb.append(" {\n");
            for (int i = 0; i < members.size(); i++) {
                sb.append(members.get(i).toArkTS(indent + 1)).append("\n");
                if (i < members.size() - 1) {
                    sb.append("\n");
                }
            }
            sb.append(indent(indent)).append("}");
            return sb.toString();
        }
    }

    // --- Destructuring declaration ---

    /**
     * A destructuring variable declaration: const [a, b] = arr or
     * const { x, y } = obj.
     */
    public static class DestructuringDeclaration extends ArkTSStatement {
        private final String kind;
        private final ArkTSExpression pattern;

        /**
         * Constructs a destructuring declaration.
         *
         * @param kind "let" or "const"
         * @param pattern the destructuring pattern expression
         *     (ArrayDestructuringExpression or ObjectDestructuringExpression)
         */
        public DestructuringDeclaration(String kind,
                ArkTSExpression pattern) {
            this.kind = kind;
            this.pattern = pattern;
        }

        public String getKind() {
            return kind;
        }

        public ArkTSExpression getPattern() {
            return pattern;
        }

        @Override
        public String toArkTS(int indent) {
            return indent(indent) + kind + " "
                    + pattern.toArkTS() + ";";
        }
    }

    // --- File module (top-level compilation unit) ---

    /**
     * A file module representing a complete .ts source file.
     * Contains imports, top-level declarations, and exports.
     */
    public static class FileModule extends ArkTSStatement {
        private final List<ArkTSStatement> imports;
        private final List<ArkTSStatement> declarations;
        private final List<ArkTSStatement> exports;

        /**
         * Constructs a file module.
         *
         * @param imports the import statements
         * @param declarations the top-level declarations
         * @param exports the export statements
         */
        public FileModule(List<ArkTSStatement> imports,
                List<ArkTSStatement> declarations,
                List<ArkTSStatement> exports) {
            this.imports = Collections.unmodifiableList(new ArrayList<>(imports));
            this.declarations = Collections.unmodifiableList(
                    new ArrayList<>(declarations));
            this.exports = Collections.unmodifiableList(new ArrayList<>(exports));
        }

        public List<ArkTSStatement> getImports() {
            return imports;
        }

        public List<ArkTSStatement> getDeclarations() {
            return declarations;
        }

        public List<ArkTSStatement> getExports() {
            return exports;
        }

        @Override
        public String toArkTS(int indent) {
            StringBuilder sb = new StringBuilder();
            for (ArkTSStatement imp : imports) {
                sb.append(imp.toArkTS(indent)).append("\n");
            }
            if (!imports.isEmpty() && !declarations.isEmpty()) {
                sb.append("\n");
            }
            for (int i = 0; i < declarations.size(); i++) {
                sb.append(declarations.get(i).toArkTS(indent));
                if (i < declarations.size() - 1) {
                    sb.append("\n\n");
                }
            }
            if (!exports.isEmpty()) {
                sb.append("\n\n");
                for (int i = 0; i < exports.size(); i++) {
                    sb.append(exports.get(i).toArkTS(indent));
                    if (i < exports.size() - 1) {
                        sb.append("\n");
                    }
                }
            }
            return sb.toString();
        }
    }
}
