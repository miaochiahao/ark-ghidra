package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcAnnotation;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcField;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;

/**
 * Builds class, method, field, and constructor declarations from ABC metadata.
 *
 * <p>Handles decorator detection, access modifiers, super class resolution,
 * struct detection, and enum extraction.
 */
class DeclarationBuilder {

    private final ArkTSDecompiler decompiler;

    DeclarationBuilder(ArkTSDecompiler decompiler) {
        this.decompiler = decompiler;
    }

    /**
     * Builds a class declaration from an ABC class definition.
     *
     * @param abcClass the ABC class
     * @param abcFile the parent ABC file for resolving references
     * @param seenImports collector for import module references
     * @return the class declaration statement, or null
     */
    ArkTSStatement buildClassDeclaration(AbcClass abcClass,
            AbcFile abcFile, Set<String> seenImports) {
        String className = sanitizeClassName(abcClass.getName());

        List<String> decorators = detectDecorators(abcClass, abcFile,
                seenImports);

        String superClassName = resolveSuperClassName(
                abcClass.getSuperClassOff(), abcFile);

        if (superClassName != null && superClassName.contains(".")) {
            String module = superClassName.substring(0,
                    superClassName.lastIndexOf('.'));
            seenImports.add(module);
        }

        List<ArkTSStatement> members = new ArrayList<>();

        boolean isAbstractClass =
                (abcClass.getAccessFlags()
                        & AbcAccessFlags.ACC_ABSTRACT) != 0;

        for (AbcField field : abcClass.getFields()) {
            ArkTSStatement fieldDecl = buildFieldDeclaration(
                    field, abcFile);
            members.add(fieldDecl);
        }

        ArkTSStatement constructorDecl = null;
        List<ArkTSStatement> methodDecls = new ArrayList<>();
        for (AbcMethod method : abcClass.getMethods()) {
            if (isConstructorMethod(method, className)) {
                constructorDecl = buildMethodDeclaration(
                        method, abcFile, className);
            } else {
                ArkTSStatement methodDecl = buildMethodOrAccessor(
                        method, abcFile, className, isAbstractClass);
                if (methodDecl != null) {
                    methodDecls.add(methodDecl);
                }
            }
        }
        if (constructorDecl != null) {
            members.add(constructorDecl);
        }
        members.addAll(methodDecls);

        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                extractTypeParameters(abcClass);

        if (!decorators.isEmpty() && isStructClass(abcClass)) {
            return new ArkTSTypeDeclarations.StructDeclaration(
                    className, members, decorators);
        }
        if (!typeParams.isEmpty()) {
            return new ArkTSTypeDeclarations.GenericClassDeclaration(
                    className, typeParams, superClassName, members);
        }
        return new ArkTSDeclarations.ClassDeclaration(
                className, superClassName, members, abcClass.getName());
    }

    /**
     * Builds a field declaration from an ABC field definition.
     *
     * @param field the ABC field
     * @param abcFile the parent ABC file for annotation lookup
     * @return the field declaration statement
     */
    ArkTSDeclarations.ClassFieldDeclaration buildFieldDeclaration(
            AbcField field, AbcFile abcFile) {
        String fieldName = field.getName();
        String typeName = inferFieldType(field);
        boolean isStatic = (field.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(
                field.getAccessFlags());

        List<String> fieldDecorators = new ArrayList<>();
        if (abcFile != null) {
            List<AbcAnnotation> fieldAnns =
                    abcFile.getAnnotationsForField(field.getOffset());
            for (AbcAnnotation ann : fieldAnns) {
                String simpleName = ann.getSimpleTypeName();
                if (!simpleName.isEmpty()
                        && !fieldDecorators.contains(simpleName)) {
                    fieldDecorators.add(simpleName);
                }
            }
        }
        if (fieldDecorators.isEmpty()) {
            String heuristic = detectFieldDecorator(fieldName);
            if (heuristic != null) {
                fieldDecorators.add(heuristic);
            }
        }

        return new ArkTSDeclarations.ClassFieldDeclaration(
                fieldName, typeName, null, isStatic, accessModifier,
                fieldDecorators);
    }

    /**
     * Builds a method declaration from an ABC method.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @param className the enclosing class name
     * @return the method declaration statement, or null
     */
    ArkTSStatement buildMethodDeclaration(AbcMethod method,
            AbcFile abcFile, String className) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;
        AbcProto proto = decompiler.resolveProto(method, abcFile);

        boolean isConstructor = isConstructorMethod(method, className);

        if (isConstructor) {
            return buildConstructorDeclaration(method, code, abcFile,
                    proto);
        }

        List<ArkTSStatement> bodyStmts = Collections.emptyList();
        if (code != null && code.getCodeSize() > 0) {
            try {
                bodyStmts = decompiler.decompileMethodBody(
                        method, code, abcFile);
            } catch (Exception e) {
                bodyStmts = new ArrayList<>();
                bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation failed: "
                                        + e.getMessage() + " */")));
            }
        }

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParamsWithDefaults(proto, code,
                        decompiler.getDebugParamNames(method, abcFile));
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        boolean isStatic = (method.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(
                method.getAccessFlags());
        boolean isAsync = code != null
                && decompiler.detectAsyncMethod(code);

        return new ArkTSDeclarations.ClassMethodDeclaration(
                method.getName(), params, returnType, body,
                isStatic, accessModifier, isAsync);
    }

    /**
     * Builds a constructor declaration from an ABC method.
     *
     * @param method the ABC method (must be a constructor)
     * @param code the method's code section
     * @param abcFile the parent ABC file
     * @param proto the method prototype
     * @return the constructor declaration statement
     */
    ArkTSDeclarations.ConstructorDeclaration buildConstructorDeclaration(
            AbcMethod method, AbcCode code, AbcFile abcFile,
            AbcProto proto) {
        List<ArkTSStatement> bodyStmts = new ArrayList<>();
        if (code != null && code.getCodeSize() > 0) {
            try {
                bodyStmts = decompiler.decompileMethodBody(
                        method, code, abcFile);
            } catch (Exception e) {
                bodyStmts = new ArrayList<>();
                bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation failed: "
                                        + e.getMessage() + " */")));
            }
        }

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParamsWithDefaults(proto, code,
                        decompiler.getDebugParamNames(method, abcFile));
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        return new ArkTSDeclarations.ConstructorDeclaration(params, body);
    }

    /**
     * Detects enum declarations from literal arrays in the ABC file.
     *
     * @param abcFile the ABC file
     * @return the list of detected enum declarations
     */
    List<ArkTSStatement> detectEnumsFromLiteralArrays(AbcFile abcFile) {
        List<ArkTSStatement> enums = new ArrayList<>();
        if (abcFile == null || abcFile.getLiteralArrays().isEmpty()) {
            return enums;
        }

        for (AbcClass cls : abcFile.getClasses()) {
            if ((cls.getAccessFlags() & AbcAccessFlags.ACC_ENUM) != 0) {
                List<ArkTSTypeDeclarations.EnumDeclaration.EnumMember> members =
                        new ArrayList<>();
                for (AbcField field : cls.getFields()) {
                    if ((field.getAccessFlags()
                            & AbcAccessFlags.ACC_STATIC) != 0) {
                        members.add(
                                new ArkTSTypeDeclarations.EnumDeclaration
                                        .EnumMember(field.getName(), null));
                    }
                }
                if (!members.isEmpty()) {
                    enums.add(new ArkTSTypeDeclarations.EnumDeclaration(
                            sanitizeClassName(cls.getName()), members));
                }
            }
        }

        return enums;
    }

    /**
     * Builds a method declaration, getter, setter, abstract method,
     * decorated method, or static block as appropriate.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @param className the enclosing class name
     * @param isAbstractClass true if the enclosing class is abstract
     * @return the declaration statement, or null
     */
    ArkTSStatement buildMethodOrAccessor(AbcMethod method,
            AbcFile abcFile, String className,
            boolean isAbstractClass) {
        String methodName = method.getName();

        // Check for getter pattern: method name starts with "get_"
        // or the method is annotated as a getter
        if (isGetterMethod(methodName)) {
            return buildGetterDeclaration(method, abcFile);
        }

        // Check for setter pattern: method name starts with "set_"
        if (isSetterMethod(methodName)) {
            return buildSetterDeclaration(method, abcFile);
        }

        // Check for static initialization block
        if (isStaticInitMethod(methodName)) {
            return buildStaticBlockDeclaration(method, abcFile);
        }

        // Check for abstract method
        if (isAbstractClass
                && (method.getAccessFlags()
                        & AbcAccessFlags.ACC_NATIVE) != 0) {
            return buildAbstractMethodDeclaration(method, abcFile);
        }

        // Build the regular method
        ArkTSStatement methodDecl = buildMethodDeclaration(
                method, abcFile, className);
        if (methodDecl == null) {
            return null;
        }

        // Check for method decorators
        List<String> methodDecorators =
                detectMethodDecorators(method, abcFile);
        if (!methodDecorators.isEmpty()
                && methodDecl
                        instanceof ArkTSDeclarations.ClassMethodDeclaration) {
            return new ArkTSDeclarations.DecoratedMethodDeclaration(
                    methodDecorators,
                    (ArkTSDeclarations.ClassMethodDeclaration) methodDecl);
        }

        return methodDecl;
    }

    /**
     * Builds a getter declaration from an ABC method.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @return the getter declaration statement
     */
    private ArkTSStatement buildGetterDeclaration(AbcMethod method,
            AbcFile abcFile) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;
        AbcProto proto = decompiler.resolveProto(method, abcFile);
        String propertyName = extractAccessorPropertyName(method.getName());
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        boolean isStatic = (method.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(
                method.getAccessFlags());

        List<ArkTSStatement> bodyStmts = Collections.emptyList();
        if (code != null && code.getCodeSize() > 0) {
            try {
                bodyStmts = decompiler.decompileMethodBody(
                        method, code, abcFile);
            } catch (Exception e) {
                bodyStmts = new ArrayList<>();
                bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation failed: "
                                        + e.getMessage() + " */")));
            }
        }
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);

        return new ArkTSDeclarations.GetterDeclaration(
                propertyName, returnType, body, isStatic, accessModifier);
    }

    /**
     * Builds a setter declaration from an ABC method.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @return the setter declaration statement
     */
    private ArkTSStatement buildSetterDeclaration(AbcMethod method,
            AbcFile abcFile) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;
        AbcProto proto = decompiler.resolveProto(method, abcFile);
        String propertyName = extractAccessorPropertyName(method.getName());
        boolean isStatic = (method.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(
                method.getAccessFlags());

        String valueTypeName = null;
        if (proto != null && proto.getShorty().size() > 1) {
            valueTypeName = MethodSignatureBuilder.getReturnType(proto);
        }

        ArkTSDeclarations.FunctionDeclaration.FunctionParam valueParam =
                new ArkTSDeclarations.FunctionDeclaration.FunctionParam(
                        "value", valueTypeName);

        List<ArkTSStatement> bodyStmts = Collections.emptyList();
        if (code != null && code.getCodeSize() > 0) {
            try {
                bodyStmts = decompiler.decompileMethodBody(
                        method, code, abcFile);
            } catch (Exception e) {
                bodyStmts = new ArrayList<>();
                bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation failed: "
                                        + e.getMessage() + " */")));
            }
        }
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);

        return new ArkTSDeclarations.SetterDeclaration(
                propertyName, valueParam, body, isStatic, accessModifier);
    }

    /**
     * Builds a static block declaration from an ABC method.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @return the static block declaration statement
     */
    private ArkTSStatement buildStaticBlockDeclaration(AbcMethod method,
            AbcFile abcFile) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;

        List<ArkTSStatement> bodyStmts = Collections.emptyList();
        if (code != null && code.getCodeSize() > 0) {
            try {
                bodyStmts = decompiler.decompileMethodBody(
                        method, code, abcFile);
            } catch (Exception e) {
                bodyStmts = new ArrayList<>();
                bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation failed: "
                                        + e.getMessage() + " */")));
            }
        }
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(bodyStmts);
        return new ArkTSDeclarations.StaticBlockDeclaration(body);
    }

    /**
     * Builds an abstract method declaration from an ABC method.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @return the abstract method declaration statement
     */
    private ArkTSStatement buildAbstractMethodDeclaration(
            AbcMethod method, AbcFile abcFile) {
        AbcProto proto = decompiler.resolveProto(method, abcFile);
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code != null ? code.getNumArgs() : 0,
                        decompiler.getDebugParamNames(method, abcFile));
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        String accessModifier = accessFlagsToModifier(
                method.getAccessFlags());

        return new ArkTSDeclarations.AbstractMethodDeclaration(
                method.getName(), params, returnType, accessModifier);
    }

    /**
     * Detects decorators for a method from annotations.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @return the list of decorator strings
     */
    private List<String> detectMethodDecorators(AbcMethod method,
            AbcFile abcFile) {
        List<String> decorators = new ArrayList<>();
        if (abcFile == null) {
            return decorators;
        }
        List<AbcAnnotation> annotations =
                abcFile.getAnnotationsForMethod(method.getOffset());
        for (AbcAnnotation ann : annotations) {
            String simpleName = ann.getSimpleTypeName();
            if (!simpleName.isEmpty()
                    && !decorators.contains(simpleName)) {
                if (ann.getElements().isEmpty()) {
                    decorators.add(simpleName);
                } else {
                    decorators.add(ann.toDecoratorString());
                }
            }
        }
        return decorators;
    }

    // --- Class helpers ---

    boolean isConstructorMethod(AbcMethod method, String className) {
        String name = method.getName();
        return "<init>".equals(name) || "<ctor>".equals(name)
                || className.equals(name);
    }

    String resolveSuperClassName(long superClassOff, AbcFile abcFile) {
        if (superClassOff == 0 || abcFile == null) {
            return null;
        }
        for (AbcClass cls : abcFile.getClasses()) {
            if (cls.getOffset() == superClassOff) {
                return sanitizeClassName(cls.getName());
            }
        }
        return "super_class_" + superClassOff;
    }

    private boolean isStructClass(AbcClass abcClass) {
        String name = abcClass.getName();
        if (name == null) {
            return false;
        }
        if (name.contains("Page") || name.contains("Component")
                || name.contains("View") || name.contains("Widget")) {
            return true;
        }
        return false;
    }

    private List<String> detectDecorators(AbcClass abcClass,
            AbcFile abcFile, Set<String> seenImports) {
        List<String> decorators = new ArrayList<>();

        int classIdx = abcFile.getClasses().indexOf(abcClass);
        if (classIdx >= 0) {
            List<AbcAnnotation> classAnns =
                    abcFile.getAnnotationsForClass(classIdx);
            for (AbcAnnotation ann : classAnns) {
                String simpleName = ann.getSimpleTypeName();
                if (!simpleName.isEmpty()
                        && !decorators.contains(simpleName)) {
                    if (ann.getElements().isEmpty()) {
                        decorators.add(simpleName);
                    } else {
                        decorators.add(ann.toDecoratorString());
                    }
                }
            }
        }

        for (AbcField field : abcClass.getFields()) {
            List<AbcAnnotation> fieldAnns =
                    abcFile.getAnnotationsForField(field.getOffset());
            for (AbcAnnotation ann : fieldAnns) {
                String simpleName = ann.getSimpleTypeName();
                if (!simpleName.isEmpty()
                        && !decorators.contains(simpleName)) {
                    if (ann.getElements().isEmpty()) {
                        decorators.add(simpleName);
                    } else {
                        decorators.add(ann.toDecoratorString());
                    }
                }
            }
        }

        if (!decorators.isEmpty()) {
            return decorators;
        }

        String name = abcClass.getName();
        if (name == null) {
            return decorators;
        }
        if (name.endsWith("Page") || name.contains("Page")) {
            decorators.add("Entry");
            decorators.add("Component");
        }
        for (AbcField field : abcClass.getFields()) {
            String fieldName = field.getName();
            if (fieldName != null) {
                String decorator = detectFieldDecorator(fieldName);
                if (decorator != null && !decorators.contains(decorator)) {
                    decorators.add(decorator);
                }
            }
        }
        return decorators;
    }

    private String detectFieldDecorator(String fieldName) {
        if (fieldName.startsWith("__") && fieldName.endsWith("__")) {
            String inner = fieldName.substring(2,
                    fieldName.length() - 2);
            switch (inner) {
                case "state":
                    return "State";
                case "prop":
                    return "Prop";
                case "link":
                    return "Link";
                case "provide":
                    return "Provide";
                case "consume":
                    return "Consume";
                case "watch":
                    return "Watch";
                default:
                    break;
            }
        }
        return null;
    }

    private List<ArkTSTypeDeclarations.TypeParameter> extractTypeParameters(
            AbcClass abcClass) {
        return Collections.emptyList();
    }

    private String inferFieldType(AbcField field) {
        return null;
    }

    static String accessFlagsToModifier(long flags) {
        if ((flags & AbcAccessFlags.ACC_PUBLIC) != 0) {
            return "public";
        }
        if ((flags & AbcAccessFlags.ACC_PRIVATE) != 0) {
            return "private";
        }
        if ((flags & AbcAccessFlags.ACC_PROTECTED) != 0) {
            return "protected";
        }
        return null;
    }

    boolean isExported(AbcClass abcClass) {
        return (abcClass.getAccessFlags()
                & AbcAccessFlags.ACC_PUBLIC) != 0;
    }

    static String sanitizeClassName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "AnonymousClass";
        }
        int lastSlash = rawName.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < rawName.length() - 1) {
            return rawName.substring(lastSlash + 1);
        }
        return rawName;
    }

    /**
     * Returns true if the method name indicates a getter accessor.
     * Getter methods in Ark bytecode are named "get_PropertyName".
     *
     * @param methodName the method name
     * @return true if this is a getter
     */
    static boolean isGetterMethod(String methodName) {
        return methodName != null
                && methodName.startsWith("get_")
                && methodName.length() > 4;
    }

    /**
     * Returns true if the method name indicates a setter accessor.
     * Setter methods in Ark bytecode are named "set_PropertyName".
     *
     * @param methodName the method name
     * @return true if this is a setter
     */
    static boolean isSetterMethod(String methodName) {
        return methodName != null
                && methodName.startsWith("set_")
                && methodName.length() > 4;
    }

    /**
     * Returns true if the method name indicates a static initialization
     * block. Static init methods in Ark bytecode are named
     * "&lt;static_init&gt;" or "&lt;clinit&gt;".
     *
     * @param methodName the method name
     * @return true if this is a static init block
     */
    static boolean isStaticInitMethod(String methodName) {
        return "<static_init>".equals(methodName)
                || "<clinit>".equals(methodName);
    }

    /**
     * Extracts the property name from an accessor method name.
     * "get_value" becomes "value", "set_name" becomes "name".
     *
     * @param accessorName the accessor method name
     * @return the property name
     */
    static String extractAccessorPropertyName(String accessorName) {
        if (accessorName != null && accessorName.length() > 4) {
            return accessorName.substring(4);
        }
        return accessorName;
    }
}
