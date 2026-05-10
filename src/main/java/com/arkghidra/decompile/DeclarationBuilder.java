package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * struct detection, enum extraction, override detection, parameter property
 * detection, and getter/setter simplification.
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

        // Detect interface: ACC_INTERFACE flag or ACC_ABSTRACT with all
        // abstract methods and no fields
        if (isInterfaceClass(abcClass)) {
            return buildInterfaceDeclaration(
                    abcClass, abcFile, className, seenImports);
        }

        // Detect annotation class: ACC_ANNOTATION flag
        if (isAnnotationClass(abcClass)) {
            return buildAnnotationClassDeclaration(
                    abcClass, abcFile, className, seenImports);
        }

        List<String> decorators = detectDecorators(abcClass, abcFile,
                seenImports);

        String superClassName = resolveSuperClassName(
                abcClass.getSuperClassOff(), abcFile);

        if (superClassName != null && superClassName.contains(".")) {
            String module = superClassName.substring(0,
                    superClassName.lastIndexOf('.'));
            seenImports.add(module);
        }

        List<String> interfaceNames = resolveInterfaceNames(
                abcClass.getInterfaceOffsets(), abcFile);

        for (String ifaceName : interfaceNames) {
            if (ifaceName.contains(".")) {
                String module = ifaceName.substring(0,
                        ifaceName.lastIndexOf('.'));
                seenImports.add(module);
            }
        }

        List<ArkTSStatement> members = new ArrayList<>();

        boolean isAbstractClass =
                (abcClass.getAccessFlags()
                        & AbcAccessFlags.ACC_ABSTRACT) != 0;

        // Collect superclass method names for override detection
        Set<String> superMethodNames =
                collectSuperClassMethodNames(abcClass, abcFile);

        // Scan constructor for null/undefined field assignments
        Map<String, FieldNullability> fieldNullability =
                analyzeFieldNullability(abcClass, abcFile);

        for (AbcField field : abcClass.getFields()) {
            // Skip internal metadata fields that are not real class members
            if (isInternalMetadataField(field.getName())) {
                continue;
            }
            FieldNullability fn = fieldNullability.get(field.getName());
            ArkTSStatement fieldDecl = buildFieldDeclaration(
                    field, abcFile, fn);
            members.add(fieldDecl);
        }

        ArkTSStatement constructorDecl = null;
        List<ArkTSStatement> methodDecls = new ArrayList<>();
        List<AbcField> classFields = abcClass.getFields();
        for (AbcMethod method : abcClass.getMethods()) {
            try {
                if (isConstructorMethod(method, className)) {
                    constructorDecl = buildConstructorDeclaration(
                            method, abcFile, classFields);
                } else {
                    ArkTSStatement methodDecl = buildMethodOrAccessor(
                            method, abcFile, className, isAbstractClass,
                            superMethodNames);
                    if (methodDecl != null) {
                        methodDecls.add(methodDecl);
                    }
                }
            } catch (Exception e) {
                String mName = sanitizeMethodName(method.getName());
                methodDecls.add(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "/* error decompiling method "
                                                + mName + ": "
                                                + e.getMessage()
                                                + " */")));
            }
        }
        if (constructorDecl != null) {
            members.add(constructorDecl);
        }

        // Simplify getter/setter pairs into property declarations
        methodDecls = simplifyGetterSetterPairs(methodDecls, members);

        members.addAll(methodDecls);

        List<ArkTSTypeDeclarations.TypeParameter> typeParams =
                extractTypeParameters(abcClass);

        if (!decorators.isEmpty() && isStructClass(abcClass)) {
            return new ArkTSTypeDeclarations.StructDeclaration(
                    className, members, decorators);
        }
        boolean isSendable = decorators.contains("Sendable");
        if (!typeParams.isEmpty()) {
            return new ArkTSTypeDeclarations.GenericClassDeclaration(
                    className, typeParams, superClassName, interfaceNames,
                    members);
        }
        return new ArkTSDeclarations.ClassDeclaration(
                className, superClassName, interfaceNames, members,
                abcClass.getName(), decorators, isSendable,
                isAbstractClass);
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
        return buildFieldDeclaration(field, abcFile, null);
    }

    /**
     * Builds a field declaration with nullable type inference.
     *
     * <p>When the field has been assigned null or undefined in the
     * constructor body, the type annotation is widened to a union type
     * (e.g. {@code string | null}). When the field has no initializer
     * and could be undefined, optional syntax is used
     * ({@code prop?: type}).
     *
     * @param field the ABC field
     * @param abcFile the parent ABC file for annotation lookup
     * @param nullability nullable tracking info (may be null)
     * @return the field declaration statement
     */
    ArkTSDeclarations.ClassFieldDeclaration buildFieldDeclaration(
            AbcField field, AbcFile abcFile,
            FieldNullability nullability) {
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

        // Detect readonly (ACC_FINAL maps to readonly in ArkTS)
        boolean isReadonly = (field.getAccessFlags()
                & AbcAccessFlags.ACC_FINAL) != 0;

        // Build initializer from field int value when available
        ArkTSExpression initializer = null;
        if (field.getIntValue() != 0) {
            initializer = new ArkTSExpression.LiteralExpression(
                    String.valueOf(field.getIntValue()),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        }

        // Apply nullable type widening when null/undefined assignments
        // are detected in the constructor body
        if (nullability != null && typeName != null) {
            typeName = TypeInference.inferNullableType(typeName,
                    nullability.canBeNull, nullability.canBeUndefined);
        }

        return new ArkTSDeclarations.ClassFieldDeclaration(
                fieldName, typeName, initializer, isStatic, accessModifier,
                fieldDecorators, isReadonly);
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
        return buildMethodDeclaration(method, abcFile, className,
                Collections.emptySet());
    }

    /**
     * Builds a method declaration from an ABC method with override detection.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @param className the enclosing class name
     * @param superMethodNames set of method names in the superclass
     * @return the method declaration statement, or null
     */
    ArkTSStatement buildMethodDeclaration(AbcMethod method,
            AbcFile abcFile, String className,
            Set<String> superMethodNames) {
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
        boolean isOverride = !isStatic
                && superMethodNames.contains(sanitizeMethodName(
                        method.getName()));
        boolean isAbstract = (method.getAccessFlags()
                & AbcAccessFlags.ACC_ABSTRACT) != 0;

        return new ArkTSDeclarations.ClassMethodDeclaration(
                sanitizeMethodName(method.getName()), params, returnType, body,
                isStatic, accessModifier, isAsync, isOverride,
                isAbstract);
    }

    /**
     * Builds a constructor declaration from an ABC method, with
     * field-list-aware parameter property detection.
     *
     * @param method the constructor method
     * @param abcFile the parent ABC file
     * @param classFields the class fields (for access modifier lookup)
     * @return the constructor declaration statement
     */
    ArkTSStatement buildConstructorDeclaration(
            AbcMethod method, AbcFile abcFile,
            List<AbcField> classFields) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;
        AbcProto proto = decompiler.resolveProto(method, abcFile);
        return buildConstructorDeclaration(method, code, abcFile,
                proto, classFields);
    }

    /**
     * Builds a constructor declaration from an ABC method.
     *
     * <p>Detects parameter properties when the constructor body assigns
     * parameter values directly to {@code this.paramName}. When ALL
     * parameters follow this pattern, they are rendered with access
     * modifiers resolved from matching class fields:
     * {@code constructor(public name: string, private age: number)}.
     *
     * @param method the ABC method (must be a constructor)
     * @param code the method's code section
     * @param abcFile the parent ABC file
     * @param proto the method prototype
     * @return the constructor declaration statement
     */
    ArkTSStatement buildConstructorDeclaration(
            AbcMethod method, AbcCode code, AbcFile abcFile,
            AbcProto proto) {
        return buildConstructorDeclaration(method, code, abcFile,
                proto, Collections.emptyList());
    }

    /**
     * Builds a constructor declaration with field-aware parameter
     * property detection.
     *
     * <p>Only uses the parameter property shorthand when ALL constructor
     * parameters are assigned to {@code this.propName}. Access modifiers
     * are resolved from matching class field declarations.
     *
     * @param method the ABC method (must be a constructor)
     * @param code the method's code section
     * @param abcFile the parent ABC file
     * @param proto the method prototype
     * @param classFields the class fields for access modifier lookup
     * @return the constructor declaration statement
     */
    ArkTSStatement buildConstructorDeclaration(
            AbcMethod method, AbcCode code, AbcFile abcFile,
            AbcProto proto, List<AbcField> classFields) {
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

        // Detect parameter properties: params assigned to this.paramName
        List<ConstructorParamProperty> paramProps =
                detectConstructorParamProperties(params, bodyStmts);

        // Only use shorthand when ALL parameters are property assignments
        boolean allParamsAreProperties = !params.isEmpty()
                && paramProps.size() == params.size();

        // Filter out property assignment statements from the body
        List<ArkTSStatement> filteredBody =
                filterPropertyAssignments(bodyStmts, paramProps);

        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(filteredBody);

        if (allParamsAreProperties) {
            List<ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                    .ConstructorParam> constructorParams =
                    new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                ArkTSDeclarations.FunctionDeclaration.FunctionParam p =
                        params.get(i);
                ConstructorParamProperty cpp = findParamProperty(
                        paramProps, i);
                String modifier = "public";
                if (cpp != null) {
                    modifier = resolveFieldAccessModifier(
                            cpp.paramName, classFields);
                }
                constructorParams.add(
                        new ArkTSDeclarations.ConstructorWithPropertiesDeclaration
                                .ConstructorParam(p.getName(),
                                        p.getTypeName(), modifier));
            }
            return new ArkTSDeclarations.ConstructorWithPropertiesDeclaration(
                    constructorParams, body);
        }

        return new ArkTSDeclarations.ConstructorDeclaration(params, body);
    }

    /**
     * Finds a parameter property by index.
     */
    private ConstructorParamProperty findParamProperty(
            List<ConstructorParamProperty> props, int index) {
        for (ConstructorParamProperty cpp : props) {
            if (cpp.paramIndex == index) {
                return cpp;
            }
        }
        return null;
    }

    /**
     * Resolves the access modifier for a parameter property by looking
     * up the matching class field's access flags.
     *
     * @param paramName the parameter/property name
     * @param classFields the class fields
     * @return the access modifier, defaulting to "public"
     */
    private String resolveFieldAccessModifier(String paramName,
            List<AbcField> classFields) {
        for (AbcField field : classFields) {
            if (paramName.equals(field.getName())) {
                String modifier = accessFlagsToModifier(
                        field.getAccessFlags());
                return modifier != null ? modifier : "public";
            }
        }
        return "public";
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
                        long intVal = field.getIntValue();
                        ArkTSExpression valueExpr = intVal != 0
                                ? new ArkTSExpression.LiteralExpression(
                                        String.valueOf(intVal),
                                        ArkTSExpression.LiteralExpression
                                                .LiteralKind.NUMBER)
                                : null;
                        members.add(
                                new ArkTSTypeDeclarations.EnumDeclaration
                                        .EnumMember(field.getName(), valueExpr));
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
        return buildMethodOrAccessor(method, abcFile, className,
                isAbstractClass, Collections.emptySet());
    }

    /**
     * Builds a method declaration with override detection support.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @param className the enclosing class name
     * @param isAbstractClass true if the enclosing class is abstract
     * @param superMethodNames set of method names in the superclass
     * @return the declaration statement, or null
     */
    ArkTSStatement buildMethodOrAccessor(AbcMethod method,
            AbcFile abcFile, String className,
            boolean isAbstractClass, Set<String> superMethodNames) {
        String methodName = method.getName();

        // Check for getter: get_ prefix or ABC-encoded accessor with 0 params
        if (isGetterMethod(method, abcFile)) {
            return buildGetterDeclaration(method, abcFile);
        }

        // Check for setter: set_ prefix or ABC-encoded accessor with 1 param
        if (isSetterMethod(method, abcFile)) {
            return buildSetterDeclaration(method, abcFile);
        }

        // Check for static initialization block
        if (isStaticInitMethod(methodName)) {
            return buildStaticBlockDeclaration(method, abcFile);
        }

        // Check for abstract method: native in abstract class,
        // or method with empty body in abstract class
        boolean isAbstract = isAbstractClass
                && ((method.getAccessFlags()
                        & AbcAccessFlags.ACC_NATIVE) != 0
                    || hasEmptyBody(method, abcFile));
        if (isAbstract) {
            return buildAbstractMethodDeclaration(method, abcFile);
        }

        // Build the regular method
        ArkTSStatement methodDecl = buildMethodDeclaration(
                method, abcFile, className, superMethodNames);
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
                sanitizeMethodName(method.getName()), params, returnType, accessModifier);
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
        if ("<init>".equals(name) || "<ctor>".equals(name)
                || className.equals(name)) {
            return true;
        }
        // ABC encoded constructor: #~@N=#name — '=' indicates constructor
        if (name != null && name.startsWith("#~@")) {
            int gtIdx = name.indexOf('>');
            int eqIdx = name.indexOf('=');
            int ltIdx = name.indexOf('<');
            // '=' is the constructor indicator, and it must come before any '>' or '<'
            if (eqIdx > 0 && (gtIdx < 0 || eqIdx < gtIdx)
                    && (ltIdx < 0 || eqIdx < ltIdx)) {
                return true;
            }
        }
        return false;
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

        if (abcFile == null) {
            return decorators;
        }

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
        String name = rawName;
        // Strip L prefix and ; suffix from ABC class name format (Lcom/example/Foo;)
        if (name.startsWith("L") && name.endsWith(";")) {
            name = name.substring(1, name.length() - 1);
        }
        // Strip module version suffix (&1.0.3) — find last & followed by digits
        int lastAmp = name.lastIndexOf('&');
        if (lastAmp > 0) {
            String afterAmp = name.substring(lastAmp + 1);
            if (afterAmp.matches("\\d[\\d.]*")) {
                name = name.substring(0, lastAmp);
            }
        }
        // Take the last path component
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < name.length() - 1) {
            return name.substring(lastSlash + 1);
        }
        return name;
    }

    /**
     * Sanitizes an ABC-encoded method name into a readable ArkTS identifier.
     *
     * <p>Ark bytecode encodes method names with prefixes that indicate the
     * method's kind and inline cache slot:
     * <ul>
     *   <li>{@code #~@N>#name} — instance method</li>
     *   <li>{@code #~@N=#name} — constructor</li>
     *   <li>{@code #~@N<#name} — accessor (getter/setter)</li>
     *   <li>{@code #~@N>@M*#name} — prototype method</li>
     *   <li>{@code #*#name} — static method</li>
     *   <li>{@code #**#name} — static method variant</li>
     * </ul>
     *
     * <p>This method extracts the readable name after the last {@code #}.
     * Standard special names ({@code <init>}, {@code func_main_0}, etc.)
     * and accessor prefixes ({@code get_}, {@code set_}) are returned as-is.
     *
     * @param rawName the raw method name from the ABC file
     * @return a readable method name
     */
    static String sanitizeMethodName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "anonymous_method";
        }
        // Standard special names — return as-is
        if (rawName.startsWith("<") || rawName.startsWith("func_")) {
            return rawName;
        }
        // Accessor prefixes — return as-is (handled by isGetterMethod/isSetterMethod)
        if (rawName.startsWith("get_") || rawName.startsWith("set_")) {
            return rawName;
        }
        // Plain identifier (no ABC encoding)
        if (!rawName.startsWith("#")) {
            return rawName;
        }
        // ABC encoded names: extract name after last '#'
        int lastHash = rawName.lastIndexOf('#');
        if (lastHash >= 0 && lastHash < rawName.length() - 1) {
            String candidate = rawName.substring(lastHash + 1);
            if (!candidate.isEmpty() && isValidMethodIdentifier(candidate)) {
                return candidate;
            }
        }
        return "anonymous_method";
    }

    private static boolean isValidMethodIdentifier(String s) {
        if (s.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static final Set<String> INTERNAL_METADATA_FIELDS = Set.of(
            "pkgName", "isCommonjs", "hasTopLevelAwait",
            "isSharedModule", "scopeNames", "moduleRecordIdx");

    private static boolean isInternalMetadataField(String name) {
        return INTERNAL_METADATA_FIELDS.contains(name);
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

    boolean isGetterMethod(AbcMethod method, AbcFile abcFile) {
        String name = method.getName();
        if (isGetterMethod(name)) {
            return true;
        }
        if (name != null && name.startsWith("#~@")
                && isAccessorPrefix(name)) {
            AbcProto proto = decompiler.resolveProto(method, abcFile);
            int paramCount = proto != null
                    ? proto.getShorty().size() - 1 : 0;
            return paramCount == 0;
        }
        return false;
    }

    static boolean isSetterMethod(String methodName) {
        return methodName != null
                && methodName.startsWith("set_")
                && methodName.length() > 4;
    }

    boolean isSetterMethod(AbcMethod method, AbcFile abcFile) {
        String name = method.getName();
        if (isSetterMethod(name)) {
            return true;
        }
        if (name != null && name.startsWith("#~@")
                && isAccessorPrefix(name)) {
            AbcProto proto = decompiler.resolveProto(method, abcFile);
            int paramCount = proto != null
                    ? proto.getShorty().size() - 1 : 0;
            return paramCount == 1;
        }
        return false;
    }

    static boolean isAccessorPrefix(String name) {
        if (!name.startsWith("#~@")) {
            return false;
        }
        for (int i = 3; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '<') {
                return true;
            }
            if (c == '>' || c == '=') {
                return false;
            }
        }
        return false;
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
        if (accessorName == null) {
            return null;
        }
        // ABC encoded accessor: #~@N<#name — use sanitizeMethodName
        if (accessorName.startsWith("#")) {
            return sanitizeMethodName(accessorName);
        }
        // Standard get_/set_ prefix
        if (accessorName.length() > 4) {
            return accessorName.substring(4);
        }
        return accessorName;
    }

    // --- Override detection ---

    /**
     * Collects method names from the superclass chain for override detection.
     *
     * @param abcClass the current class
     * @param abcFile the parent ABC file
     * @return set of method names defined in superclasses
     */
    Set<String> collectSuperClassMethodNames(AbcClass abcClass,
            AbcFile abcFile) {
        Set<String> methodNames = new HashSet<>();
        if (abcFile == null || abcClass.getSuperClassOff() == 0) {
            return methodNames;
        }

        // Walk up the class hierarchy
        Set<Long> visited = new HashSet<>();
        long currentSuperOff = abcClass.getSuperClassOff();
        while (currentSuperOff != 0
                && !visited.contains(currentSuperOff)) {
            visited.add(currentSuperOff);
            AbcClass superClass = findClassByOffset(
                    currentSuperOff, abcFile);
            if (superClass == null) {
                break;
            }
            for (AbcMethod m : superClass.getMethods()) {
                String mName = m.getName();
                if (mName != null && !"<init>".equals(mName)
                        && !"<ctor>".equals(mName)
                        && !isGetterMethod(mName)
                        && !isSetterMethod(mName)
                        && !isStaticInitMethod(mName)
                        && !isConstructorMethod(m, "")) {
                    methodNames.add(sanitizeMethodName(mName));
                }
            }
            currentSuperOff = superClass.getSuperClassOff();
        }
        return methodNames;
    }

    /**
     * Finds a class in the ABC file by its byte offset.
     *
     * @param offset the class offset
     * @param abcFile the ABC file
     * @return the class, or null if not found
     */
    private AbcClass findClassByOffset(long offset, AbcFile abcFile) {
        for (AbcClass cls : abcFile.getClasses()) {
            if (cls.getOffset() == offset) {
                return cls;
            }
        }
        return null;
    }

    // --- Abstract method detection ---

    /**
     * Checks if a method has an empty body (no code or zero code size).
     * Used to detect abstract methods in abstract classes.
     *
     * @param method the method to check
     * @param abcFile the parent ABC file
     * @return true if the method body is empty
     */
    private boolean hasEmptyBody(AbcMethod method, AbcFile abcFile) {
        if (abcFile == null) {
            return method.getCodeOff() == 0;
        }
        AbcCode code = abcFile.getCodeForMethod(method);
        return code == null || code.getCodeSize() == 0;
    }

    // --- Constructor parameter property detection ---

    /**
     * Holds info about a constructor parameter that is a property.
     */
    private static class ConstructorParamProperty {
        final int paramIndex;
        final String paramName;

        ConstructorParamProperty(int paramIndex, String paramName) {
            this.paramIndex = paramIndex;
            this.paramName = paramName;
        }
    }

    /**
     * Detects constructor parameter properties by analyzing the body
     * for patterns like {@code this.paramName = param_N}.
     *
     * <p>A parameter is considered a property if the body contains
     * an assignment of the form {@code this.paramName = param_N}
     * where paramName matches or is similar to the parameter name.
     *
     * @param params the constructor parameters
     * @param bodyStmts the constructor body statements
     * @return the list of detected parameter properties
     */
    private List<ConstructorParamProperty> detectConstructorParamProperties(
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params,
            List<ArkTSStatement> bodyStmts) {
        List<ConstructorParamProperty> result = new ArrayList<>();
        if (params.isEmpty() || bodyStmts.isEmpty()) {
            return result;
        }

        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i).getName();
            String thisAccess = "this." + paramName;
            for (ArkTSStatement stmt : bodyStmts) {
                String stmtStr = stmt.toArkTS(0).trim();
                // Match patterns like: this.name = name;
                // or: this.param_0 = param_0;
                if (stmtStr.startsWith(thisAccess + " = ")
                        && stmtStr.endsWith(paramName + ";")) {
                    result.add(new ConstructorParamProperty(i, paramName));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Filters out property assignment statements from the constructor body,
     * since those are now represented as parameter property modifiers.
     *
     * @param bodyStmts the original body statements
     * @param paramProps the detected parameter properties
     * @return the filtered body statements
     */
    private List<ArkTSStatement> filterPropertyAssignments(
            List<ArkTSStatement> bodyStmts,
            List<ConstructorParamProperty> paramProps) {
        if (paramProps.isEmpty()) {
            return bodyStmts;
        }

        Set<String> assignmentsToRemove = new HashSet<>();
        for (ConstructorParamProperty cpp : paramProps) {
            assignmentsToRemove.add(
                    "this." + cpp.paramName + " = " + cpp.paramName + ";");
        }

        List<ArkTSStatement> filtered = new ArrayList<>();
        for (ArkTSStatement stmt : bodyStmts) {
            String stmtStr = stmt.toArkTS(0).trim();
            if (!assignmentsToRemove.contains(stmtStr)) {
                filtered.add(stmt);
            }
        }
        return filtered;
    }

    // --- Nullable field tracking ---

    /**
     * Tracks whether a field can be assigned null or undefined.
     */
    static class FieldNullability {
        final boolean canBeNull;
        final boolean canBeUndefined;

        FieldNullability(boolean canBeNull, boolean canBeUndefined) {
            this.canBeNull = canBeNull;
            this.canBeUndefined = canBeUndefined;
        }
    }

    /**
     * Scans constructor bodies for null/undefined assignments to fields.
     *
     * <p>Looks for patterns like {@code this.fieldName = null} and
     * {@code this.fieldName = undefined} in constructor decompiled
     * output. Returns a map from field name to nullability info.
     *
     * @param abcClass the class to analyze
     * @param abcFile the parent ABC file
     * @return map of field name to nullability info
     */
    private Map<String, FieldNullability> analyzeFieldNullability(
            AbcClass abcClass, AbcFile abcFile) {
        Map<String, FieldNullability> result = new HashMap<>();
        if (abcFile == null) {
            return result;
        }

        String className = sanitizeClassName(abcClass.getName());
        Map<String, Boolean> canBeNull = new HashMap<>();
        Map<String, Boolean> canBeUndefined = new HashMap<>();

        for (AbcMethod method : abcClass.getMethods()) {
            if (!isConstructorMethod(method, className)) {
                continue;
            }
            AbcCode code = abcFile.getCodeForMethod(method);
            if (code == null || code.getCodeSize() == 0) {
                continue;
            }
            try {
                List<ArkTSStatement> stmts =
                        decompiler.decompileMethodBody(method, code, abcFile);
                scanStatementsForNullFieldAssignments(stmts,
                        canBeNull, canBeUndefined);
            } catch (Exception e) {
                // Skip analysis on decompilation failure
            }
        }

        for (AbcField field : abcClass.getFields()) {
            String name = field.getName();
            boolean isNull = canBeNull.getOrDefault(name, false);
            boolean isUndef = canBeUndefined.getOrDefault(name, false);
            if (isNull || isUndef) {
                result.put(name, new FieldNullability(isNull, isUndef));
            }
        }
        return result;
    }

    /**
     * Recursively scans statements for {@code this.field = null/undefined}
     * assignment patterns.
     */
    private void scanStatementsForNullFieldAssignments(
            List<ArkTSStatement> stmts,
            Map<String, Boolean> canBeNull,
            Map<String, Boolean> canBeUndefined) {
        if (stmts == null) {
            return;
        }
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.ExpressionStatement) {
                ArkTSExpression expr =
                        ((ArkTSStatement.ExpressionStatement) stmt)
                                .getExpression();
                scanExpressionForNullFieldAssignment(expr,
                        canBeNull, canBeUndefined);
            } else if (stmt instanceof ArkTSStatement.BlockStatement) {
                scanStatementsForNullFieldAssignments(
                        ((ArkTSStatement.BlockStatement) stmt).getBody(),
                        canBeNull, canBeUndefined);
            } else if (stmt instanceof ArkTSControlFlow.IfStatement) {
                ArkTSControlFlow.IfStatement ifStmt =
                        (ArkTSControlFlow.IfStatement) stmt;
                if (ifStmt.getThenBlock() != null) {
                    scanStatementsForNullFieldAssignments(
                            extractBodyList(ifStmt.getThenBlock()),
                            canBeNull, canBeUndefined);
                }
                if (ifStmt.getElseBlock() != null) {
                    scanStatementsForNullFieldAssignments(
                            extractBodyList(ifStmt.getElseBlock()),
                            canBeNull, canBeUndefined);
                }
            }
        }
    }

    /**
     * Extracts statements from a statement that may be a block or single.
     */
    private List<ArkTSStatement> extractBodyList(ArkTSStatement stmt) {
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            return ((ArkTSStatement.BlockStatement) stmt).getBody();
        }
        return Collections.singletonList(stmt);
    }

    /**
     * Checks a single expression for {@code this.field = null/undefined}.
     * Handles both AssignExpression and BinaryExpression("=") patterns.
     */
    private void scanExpressionForNullFieldAssignment(
            ArkTSExpression expr,
            Map<String, Boolean> canBeNull,
            Map<String, Boolean> canBeUndefined) {
        ArkTSExpression target = null;
        ArkTSExpression value = null;

        if (expr instanceof ArkTSExpression.AssignExpression) {
            ArkTSExpression.AssignExpression assign =
                    (ArkTSExpression.AssignExpression) expr;
            target = assign.getTarget();
            value = assign.getValue();
        } else if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            if ("=".equals(bin.getOperator())) {
                target = bin.getLeft();
                value = bin.getRight();
            }
        }

        if (target == null || value == null) {
            return;
        }
        if (!(target instanceof ArkTSExpression.MemberExpression)) {
            return;
        }
        ArkTSExpression.MemberExpression member =
                (ArkTSExpression.MemberExpression) target;

        // Check that object is "this" (ThisExpression or VariableExpression)
        ArkTSExpression objExpr = member.getObject();
        boolean isThis = objExpr instanceof ArkTSExpression.ThisExpression
                || (objExpr instanceof ArkTSExpression.VariableExpression
                        && "this".equals(
                                ((ArkTSExpression.VariableExpression) objExpr)
                                        .getName()));
        if (!isThis) {
            return;
        }

        // Extract field name from the property expression
        String fieldName = null;
        if (member.getProperty()
                instanceof ArkTSExpression.VariableExpression) {
            fieldName = ((ArkTSExpression.VariableExpression)
                    member.getProperty()).getName();
        } else if (member.getProperty()
                instanceof ArkTSExpression.LiteralExpression) {
            fieldName = ((ArkTSExpression.LiteralExpression)
                    member.getProperty()).getValue();
        }
        if (fieldName == null) {
            return;
        }

        if (value instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) value;
            if (lit.getKind()
                    == ArkTSExpression.LiteralExpression.LiteralKind.NULL) {
                canBeNull.put(fieldName, true);
            } else if (lit.getKind()
                    == ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED) {
                canBeUndefined.put(fieldName, true);
            }
        }
    }

    // --- Getter/setter simplification ---

    /**
     * Simplifies matching getter/setter pairs into property declarations.
     *
     * <p>When a getter only returns a field and a setter only assigns it,
     * the pair is replaced by a single property declaration.
     *
     * @param methodDecls the method declarations to simplify
     * @param classMembers the existing class members (fields)
     * @return the simplified list of declarations
     */
    private List<ArkTSStatement> simplifyGetterSetterPairs(
            List<ArkTSStatement> methodDecls,
            List<ArkTSStatement> classMembers) {
        if (methodDecls.size() < 2) {
            return methodDecls;
        }

        // Index getter and setter declarations by property name
        List<ArkTSDeclarations.GetterDeclaration> getters =
                new ArrayList<>();
        List<ArkTSDeclarations.SetterDeclaration> setters =
                new ArrayList<>();
        List<ArkTSStatement> others = new ArrayList<>();

        for (ArkTSStatement decl : methodDecls) {
            if (decl instanceof ArkTSDeclarations.GetterDeclaration) {
                getters.add(
                        (ArkTSDeclarations.GetterDeclaration) decl);
            } else if (decl
                    instanceof ArkTSDeclarations.SetterDeclaration) {
                setters.add(
                        (ArkTSDeclarations.SetterDeclaration) decl);
            } else {
                others.add(decl);
            }
        }

        // If no getters or setters, nothing to simplify
        if (getters.isEmpty() || setters.isEmpty()) {
            return methodDecls;
        }

        // Check for simplifiable pairs
        List<ArkTSStatement> result = new ArrayList<>(others);
        Set<String> matchedGetters = new HashSet<>();
        Set<String> matchedSetters = new HashSet<>();

        for (ArkTSDeclarations.GetterDeclaration getter : getters) {
            for (ArkTSDeclarations.SetterDeclaration setter : setters) {
                if (getter.getName().equals(setter.getName())
                        && getter.isStatic() == setter.isStatic()
                        && isSimpleGetter(getter)
                        && isSimpleSetter(setter)) {
                    // Replace with a property declaration
                    String propName = getter.getName();
                    String propType = getter.getReturnType();
                    boolean isStatic = getter.isStatic();
                    String access = getter.getAccessModifier();
                    if (access == null
                            && setter.getAccessModifier() != null) {
                        access = setter.getAccessModifier();
                    }
                    result.add(new ArkTSDeclarations.ClassFieldDeclaration(
                            propName, propType, null, isStatic, access));
                    matchedGetters.add(getter.getName());
                    matchedSetters.add(setter.getName());
                    break;
                }
            }
        }

        // Add unmatched getters and setters
        for (ArkTSDeclarations.GetterDeclaration getter : getters) {
            if (!matchedGetters.contains(getter.getName())) {
                result.add(getter);
            }
        }
        for (ArkTSDeclarations.SetterDeclaration setter : setters) {
            if (!matchedSetters.contains(setter.getName())) {
                result.add(setter);
            }
        }

        return result;
    }

    /**
     * Checks if a getter body consists of only a single return statement.
     *
     * @param getter the getter to check
     * @return true if the getter is simple
     */
    private boolean isSimpleGetter(
            ArkTSDeclarations.GetterDeclaration getter) {
        if (getter.getBody() instanceof ArkTSStatement.BlockStatement) {
            List<ArkTSStatement> stmts =
                    ((ArkTSStatement.BlockStatement) getter.getBody())
                            .getBody();
            if (stmts.size() == 1
                    && stmts.get(0)
                            instanceof ArkTSStatement.ReturnStatement) {
                ArkTSExpression val =
                        ((ArkTSStatement.ReturnStatement) stmts.get(0))
                                .getValue();
                if (val instanceof ArkTSExpression.VariableExpression) {
                    String varName =
                            ((ArkTSExpression.VariableExpression) val)
                                    .getName();
                    return varName.equals(getter.getName())
                            || varName.equals("_" + getter.getName());
                }
            }
        }
        return false;
    }

    /**
     * Resolves interface offsets to class names.
     */
    List<String> resolveInterfaceNames(List<Long> interfaceOffsets,
            AbcFile abcFile) {
        if (interfaceOffsets == null || interfaceOffsets.isEmpty()
                || abcFile == null) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>(interfaceOffsets.size());
        for (Long off : interfaceOffsets) {
            if (off == 0) {
                continue;
            }
            String resolved = null;
            for (AbcClass cls : abcFile.getClasses()) {
                if (cls.getOffset() == off.longValue()) {
                    resolved = sanitizeClassName(cls.getName());
                    break;
                }
            }
            if (resolved == null) {
                resolved = "interface_" + off;
            }
            names.add(resolved);
        }
        return names;
    }

    /**
     * Checks if a setter body consists of only a single assignment.
     *
     * @param setter the setter to check
     * @return true if the setter is simple
     */
    private boolean isSimpleSetter(
            ArkTSDeclarations.SetterDeclaration setter) {
        if (setter.getBody() instanceof ArkTSStatement.BlockStatement) {
            List<ArkTSStatement> stmts =
                    ((ArkTSStatement.BlockStatement) setter.getBody())
                            .getBody();
            if (stmts.size() == 1) {
                String stmtStr = stmts.get(0).toArkTS(0).trim();
                // Match: this.prop = value;
                return stmtStr.equals(
                        "this." + setter.getName() + " = value;");
            }
        }
        return false;
    }

    private boolean isAnnotationClass(AbcClass abcClass) {
        return (abcClass.getAccessFlags()
                & AbcAccessFlags.ACC_ANNOTATION) != 0;
    }

    private ArkTSStatement buildAnnotationClassDeclaration(
            AbcClass abcClass, AbcFile abcFile, String className,
            Set<String> seenImports) {
        // Render as a class with a comment indicating it's an annotation type
        List<ArkTSStatement> members = new ArrayList<>();
        for (AbcField field : abcClass.getFields()) {
            ArkTSStatement fieldDecl = buildFieldDeclaration(
                    field, abcFile);
            members.add(fieldDecl);
        }
        for (AbcMethod method : abcClass.getMethods()) {
            try {
                if (isConstructorMethod(method, className)) {
                    ArkTSStatement constructor =
                            buildConstructorDeclaration(method, abcFile,
                                    abcClass.getFields());
                    if (constructor != null) {
                        members.add(constructor);
                    }
                } else {
                    ArkTSStatement methodDecl = buildMethodOrAccessor(
                            method, abcFile, className, false);
                    if (methodDecl != null) {
                        members.add(methodDecl);
                    }
                }
            } catch (Exception e) {
                String mName = sanitizeMethodName(method.getName());
                members.add(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "/* error decompiling method "
                                                + mName + ": "
                                                + e.getMessage()
                                                + " */")));
            }
        }
        return new ArkTSDeclarations.ClassDeclaration(
                className, null, Collections.emptyList(), members,
                abcClass.getName(), Collections.emptyList(), false, false);
    }

    // --- Interface detection ---

    private boolean isInterfaceClass(AbcClass abcClass) {
        long flags = abcClass.getAccessFlags();
        // Direct interface flag
        if ((flags & AbcAccessFlags.ACC_INTERFACE) != 0) {
            return true;
        }
        // Heuristic: abstract class with no fields and all abstract methods
        // Must have at least one non-constructor method
        if ((flags & AbcAccessFlags.ACC_ABSTRACT) == 0) {
            return false;
        }
        if (!abcClass.getFields().isEmpty()) {
            return false;
        }
        String className = sanitizeClassName(abcClass.getName());
        boolean hasAbstractMethods = false;
        for (AbcMethod method : abcClass.getMethods()) {
            if (isConstructorMethod(method, className)) {
                continue;
            }
            hasAbstractMethods = true;
            if ((method.getAccessFlags()
                    & AbcAccessFlags.ACC_ABSTRACT) == 0) {
                return false;
            }
        }
        return hasAbstractMethods;
    }

    private ArkTSStatement buildInterfaceDeclaration(AbcClass abcClass,
            AbcFile abcFile, String className, Set<String> seenImports) {
        List<String> extendsInterfaces = resolveInterfaceNames(
                abcClass.getInterfaceOffsets(), abcFile);

        for (String ifaceName : extendsInterfaces) {
            if (ifaceName.contains(".")) {
                String module = ifaceName.substring(0,
                        ifaceName.lastIndexOf('.'));
                seenImports.add(module);
            }
        }

        List<ArkTSTypeDeclarations.InterfaceDeclaration.InterfaceMember>
                members = new ArrayList<>();
        for (AbcMethod method : abcClass.getMethods()) {
            if (isConstructorMethod(method, className)) {
                continue;
            }
            String methodName = method.getName();
            AbcProto proto = decompiler.resolveProto(method, abcFile);
            String returnType = inferReturnTypeFromProto(proto);
            List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                    buildParamsFromProto(proto,
                            proto != null
                                    ? Math.max(0,
                                            proto.getShorty().size() - 1)
                                    : 0);
            members.add(new ArkTSTypeDeclarations.InterfaceDeclaration
                    .InterfaceMember("method", methodName, returnType,
                            params, false));
        }

        return new ArkTSTypeDeclarations.InterfaceDeclaration(
                className, extendsInterfaces, members);
    }

    private String inferReturnTypeFromProto(AbcProto proto) {
        if (proto == null) {
            return null;
        }
        AbcProto.ShortyType retType = proto.getReturnType();
        if (retType == null) {
            return null;
        }
        return switch (retType) {
            case VOID -> "void";
            case U1 -> "boolean";
            case I8, U8, I16, U16, I32, U32, I64, U64, F32, F64 -> "number";
            case REF, ANY -> null;
        };
    }

    private List<ArkTSDeclarations.FunctionDeclaration.FunctionParam>
            buildParamsFromProto(AbcProto proto, int numArgs) {
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                new ArrayList<>();
        if (proto == null || proto.getShorty().size() <= 1) {
            for (int i = 0; i < numArgs; i++) {
                params.add(new ArkTSDeclarations.FunctionDeclaration
                        .FunctionParam("param_" + i, null));
            }
            return params;
        }
        List<AbcProto.ShortyType> shorty = proto.getShorty();
        // shorty[0] = return type, shorty[1..] = param types
        int paramCount = Math.min(numArgs, shorty.size() - 1);
        for (int i = 0; i < paramCount; i++) {
            AbcProto.ShortyType typeChar = shorty.get(i + 1);
            String typeName = switch (typeChar) {
                case U1 -> "boolean";
                case I8, U8, I16, U16, I32, U32, I64, U64, F32, F64 -> "number";
                default -> null;
            };
            params.add(new ArkTSDeclarations.FunctionDeclaration
                    .FunctionParam("param_" + i, typeName));
        }
        return params;
    }
}
