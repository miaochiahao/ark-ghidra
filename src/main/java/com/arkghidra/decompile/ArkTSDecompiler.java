package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcDebugInfo;
import com.arkghidra.format.AbcField;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcProto;
import com.arkghidra.format.AbcTryBlock;

/**
 * Decompiles Ark bytecode methods into ArkTS source code.
 *
 * <p>The decompiler works in several phases:
 * <ol>
 *   <li>Disassemble the method bytecode into instructions</li>
 *   <li>Build a control flow graph (CFG) from the instructions</li>
 *   <li>Detect structured control flow patterns (if/else, loops) from the CFG</li>
 *   <li>Walk blocks to build expressions (simulating the accumulator)</li>
 *   <li>Generate ArkTS statements with type inference annotations</li>
 *   <li>Pretty-print the result</li>
 * </ol>
 */
public class ArkTSDecompiler {

    private static final String ACC = "acc";

    /**
     * Decompiles a method to ArkTS source code.
     *
     * @param method the method to decompile
     * @param code the method's code section
     * @param abcFile the parent ABC file (for resolving string/constant references)
     * @return the decompiled ArkTS source code
     */
    public String decompileMethod(AbcMethod method, AbcCode code,
            AbcFile abcFile) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return buildEmptyMethod(method, null);
        }

        ArkDisassembler disasm = new ArkDisassembler();
        List<ArkInstruction> instructions = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        if (instructions.isEmpty()) {
            return buildEmptyMethod(method, null);
        }

        // Handle trivial single-instruction bodies
        if (instructions.size() == 1) {
            ArkInstruction only = instructions.get(0);
            if (only.getOpcode() == ArkOpcodesCompat.RETURNUNDEFINED) {
                return buildEmptyMethod(method, resolveProto(method, abcFile));
            }
            if (only.getOpcode() == ArkOpcodesCompat.RETURN) {
                AbcProto proto = resolveProto(method, abcFile);
                ArkTSExpression val =
                        new ArkTSExpression.VariableExpression(ACC);
                List<ArkTSStatement> stmts = List.of(
                        new ArkTSStatement.ReturnStatement(val));
                return buildMethodSource(method, proto, code, stmts, abcFile);
            }
        }

        ControlFlowGraph cfg = ControlFlowGraph.build(instructions,
                code.getTryBlocks());

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        List<ArkTSStatement> bodyStmts = generateStatements(ctx);

        return buildMethodSource(method, proto, code, bodyStmts, abcFile);
    }

    /**
     * Decompiles just the instructions (without ABC metadata) for testing.
     *
     * @param instructions the decoded instructions
     * @return the decompiled ArkTS source
     */
    public String decompileInstructions(List<ArkInstruction> instructions) {
        if (instructions.isEmpty()) {
            return "";
        }
        ControlFlowGraph cfg = ControlFlowGraph.build(instructions);
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "f", 0, 0, 0);
        DecompilationContext ctx = new DecompilationContext(
                method, code, null, null, cfg, instructions);

        List<ArkTSStatement> stmts = generateStatements(ctx);
        StringBuilder sb = new StringBuilder();
        for (ArkTSStatement stmt : stmts) {
            sb.append(stmt.toArkTS(0)).append("\n");
        }
        return sb.toString().trim();
    }

    // --- Whole-file decompilation ---

    /**
     * Decompiles an entire ABC file into a complete ArkTS source file.
     *
     * <p>This method iterates all classes in the ABC file, builds class
     * declarations with constructors, methods, and fields, resolves imports
     * from module references, and generates a complete .ts file.
     *
     * @param abcFile the parsed ABC file
     * @return the decompiled ArkTS source code for the entire file
     */
    public String decompileFile(AbcFile abcFile) {
        if (abcFile == null) {
            return "";
        }

        List<ArkTSStatement> imports = new ArrayList<>();
        List<ArkTSStatement> declarations = new ArrayList<>();
        List<ArkTSStatement> exports = new ArrayList<>();
        Set<String> seenImports = new HashSet<>();

        for (AbcClass abcClass : abcFile.getClasses()) {
            ArkTSStatement classDecl =
                    buildClassDeclaration(abcClass, abcFile, seenImports);
            if (classDecl != null) {
                // Check if the class should be exported
                if (isExported(abcClass)) {
                    exports.add(new ArkTSStatement.ExportStatement(
                            Collections.emptyList(), classDecl, false));
                } else {
                    declarations.add(classDecl);
                }
            }
        }

        // Detect top-level enums from literal arrays
        List<ArkTSStatement> enumDecls =
                detectEnumsFromLiteralArrays(abcFile);
        declarations.addAll(enumDecls);

        // Build import statements from collected module references
        for (String moduleRef : seenImports) {
            imports.add(new ArkTSStatement.ImportStatement(
                    Collections.emptyList(), moduleRef, false, null, null));
        }

        // Build output with blank lines between classes
        return buildFileOutput(imports, declarations, exports);
    }

    /**
     * Builds the final output string for a decompiled file.
     * Adds blank lines between top-level declarations for readability.
     *
     * @param imports the import statements
     * @param declarations the top-level declarations
     * @param exports the export statements
     * @return the formatted source file string
     */
    private String buildFileOutput(List<ArkTSStatement> imports,
            List<ArkTSStatement> declarations,
            List<ArkTSStatement> exports) {
        StringBuilder sb = new StringBuilder();
        for (ArkTSStatement imp : imports) {
            sb.append(imp.toArkTS(0)).append("\n");
        }
        if (!imports.isEmpty() && (!declarations.isEmpty()
                || !exports.isEmpty())) {
            sb.append("\n");
        }
        for (int i = 0; i < declarations.size(); i++) {
            sb.append(declarations.get(i).toArkTS(0));
            if (i < declarations.size() - 1) {
                sb.append("\n\n");
            }
        }
        if (!exports.isEmpty()) {
            sb.append("\n\n");
            for (int i = 0; i < exports.size(); i++) {
                sb.append(exports.get(i).toArkTS(0));
                if (i < exports.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Builds a class declaration from an ABC class definition.
     *
     * <p>Members are grouped in order: fields first, then constructor,
     * then regular methods, with blank-line separation between them.
     *
     * @param abcClass the ABC class
     * @param abcFile the parent ABC file for resolving references
     * @param seenImports collector for import module references
     * @return the class declaration statement, or null
     */
    private ArkTSStatement buildClassDeclaration(AbcClass abcClass,
            AbcFile abcFile, Set<String> seenImports) {
        String className = sanitizeClassName(abcClass.getName());

        // Determine if this is a struct (ArkTS UI component)
        List<String> decorators = detectDecorators(abcClass);

        // Resolve super class
        String superClassName = resolveSuperClassName(
                abcClass.getSuperClassOff(), abcFile);

        // Track any module imports referenced by the super class
        if (superClassName != null && superClassName.contains(".")) {
            String module = superClassName.substring(0,
                    superClassName.lastIndexOf('.'));
            seenImports.add(module);
        }

        List<ArkTSStatement> members = new ArrayList<>();

        // Group 1: fields first
        for (AbcField field : abcClass.getFields()) {
            ArkTSStatement fieldDecl = buildFieldDeclaration(field);
            members.add(fieldDecl);
        }

        // Group 2: constructor, then regular methods
        ArkTSStatement constructorDecl = null;
        List<ArkTSStatement> methodDecls = new ArrayList<>();
        for (AbcMethod method : abcClass.getMethods()) {
            if (isConstructorMethod(method, className)) {
                constructorDecl = buildMethodDeclaration(
                        method, abcFile, className);
            } else {
                ArkTSStatement methodDecl = buildMethodDeclaration(
                        method, abcFile, className);
                if (methodDecl != null) {
                    methodDecls.add(methodDecl);
                }
            }
        }
        if (constructorDecl != null) {
            members.add(constructorDecl);
        }
        members.addAll(methodDecls);

        // Determine type parameters from class metadata
        List<ArkTSStatement.TypeParameter> typeParams =
                extractTypeParameters(abcClass);

        // Choose the right declaration type
        if (!decorators.isEmpty() && isStructClass(abcClass)) {
            return new ArkTSStatement.StructDeclaration(
                    className, members, decorators);
        }
        if (!typeParams.isEmpty()) {
            return new ArkTSStatement.GenericClassDeclaration(
                    className, typeParams, superClassName, members);
        }
        return new ArkTSStatement.ClassDeclaration(
                className, superClassName, members);
    }

    /**
     * Builds a field declaration from an ABC field definition.
     *
     * @param field the ABC field
     * @return the field declaration statement
     */
    private ArkTSStatement.ClassFieldDeclaration buildFieldDeclaration(
            AbcField field) {
        String fieldName = field.getName();
        String typeName = inferFieldType(field);
        boolean isStatic = (field.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(field.getAccessFlags());
        return new ArkTSStatement.ClassFieldDeclaration(
                fieldName, typeName, null, isStatic, accessModifier);
    }

    /**
     * Builds a method declaration from an ABC method.
     * Detects constructors and generates appropriate declaration types.
     *
     * @param method the ABC method
     * @param abcFile the parent ABC file
     * @param className the enclosing class name
     * @return the method declaration statement, or null
     */
    private ArkTSStatement buildMethodDeclaration(AbcMethod method,
            AbcFile abcFile, String className) {
        AbcCode code = abcFile != null
                ? abcFile.getCodeForMethod(method) : null;
        AbcProto proto = resolveProto(method, abcFile);

        boolean isConstructor = isConstructorMethod(method, className);

        if (isConstructor) {
            return buildConstructorDeclaration(method, code, abcFile, proto);
        }

        // Regular method
        List<ArkTSStatement> bodyStmts = Collections.emptyList();
        if (code != null && code.getCodeSize() > 0) {
            bodyStmts = decompileMethodBody(method, code, abcFile);
        }

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code != null ? code.getNumArgs() : 0,
                        getDebugParamNames(method, abcFile));
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body = new ArkTSStatement.BlockStatement(bodyStmts);
        boolean isStatic = (method.getAccessFlags()
                & AbcAccessFlags.ACC_STATIC) != 0;
        String accessModifier = accessFlagsToModifier(method.getAccessFlags());

        return new ArkTSStatement.ClassMethodDeclaration(
                method.getName(), params, returnType, body,
                isStatic, accessModifier);
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
    private ArkTSStatement.ConstructorDeclaration buildConstructorDeclaration(
            AbcMethod method, AbcCode code, AbcFile abcFile, AbcProto proto) {
        List<ArkTSStatement> bodyStmts = new ArrayList<>();
        if (code != null && code.getCodeSize() > 0) {
            bodyStmts = decompileMethodBody(method, code, abcFile);
        }

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code != null ? code.getNumArgs() : 0,
                        getDebugParamNames(method, abcFile));
        ArkTSStatement body = new ArkTSStatement.BlockStatement(bodyStmts);
        return new ArkTSStatement.ConstructorDeclaration(params, body);
    }

    /**
     * Decompiles the body of a method into statements.
     *
     * @param method the method
     * @param code the method's code section
     * @param abcFile the parent ABC file
     * @return the list of decompiled statements
     */
    private List<ArkTSStatement> decompileMethodBody(AbcMethod method,
            AbcCode code, AbcFile abcFile) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return Collections.emptyList();
        }

        ArkDisassembler disasm = new ArkDisassembler();
        List<ArkInstruction> instructions = disasm.disassemble(
                code.getInstructions(), 0, (int) code.getCodeSize());

        if (instructions.isEmpty()) {
            return Collections.emptyList();
        }

        ControlFlowGraph cfg = ControlFlowGraph.build(instructions,
                code.getTryBlocks());

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        return generateStatements(ctx);
    }

    // --- Class reconstruction helpers ---

    // --- Try/catch handling ---

    /**
     * Represents a try/catch region in the bytecode.
     */
    private static class TryCatchRegion {
        final int startPc;
        final int endPc;
        final List<CatchHandler> handlers;
        boolean processed;

        TryCatchRegion(int startPc, int endPc,
                List<CatchHandler> handlers) {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlers = handlers;
            this.processed = false;
        }

        boolean isProcessed() {
            return processed;
        }

        void markProcessed() {
            processed = true;
        }
    }

    /**
     * Represents a single catch handler within a try/catch region.
     */
    private static class CatchHandler {
        final String typeName;
        final int handlerPc;

        CatchHandler(String typeName, int handlerPc) {
            this.typeName = typeName;
            this.handlerPc = handlerPc;
        }
    }

    /**
     * Builds try/catch regions from the AbcCode try blocks.
     *
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @return the list of try/catch regions
     */
    private List<TryCatchRegion> buildTryCatchRegions(
            DecompilationContext ctx, ControlFlowGraph cfg) {
        List<TryCatchRegion> regions = new ArrayList<>();
        if (ctx.code == null || ctx.code.getTryBlocks() == null) {
            return regions;
        }
        for (AbcTryBlock tryBlock : ctx.code.getTryBlocks()) {
            int startPc = (int) tryBlock.getStartPc();
            int endPc = startPc + (int) tryBlock.getLength();
            List<CatchHandler> handlers = new ArrayList<>();
            CatchHandler finallyHandler = null;
            for (AbcCatchBlock catchBlock : tryBlock.getCatchBlocks()) {
                String typeName = null;
                if (catchBlock.getTypeIdx() > 0 && ctx.abcFile != null) {
                    typeName = resolveTypeName(
                            (int) catchBlock.getTypeIdx(), ctx.abcFile);
                }
                if (catchBlock.isCatchAll()) {
                    finallyHandler = new CatchHandler(
                            typeName, (int) catchBlock.getHandlerPc());
                } else {
                    handlers.add(new CatchHandler(
                            typeName, (int) catchBlock.getHandlerPc()));
                }
            }
            // If there's a catchAll with no typed handlers, treat as finally
            if (finallyHandler != null && handlers.isEmpty()) {
                handlers.add(finallyHandler);
            }
            if (!handlers.isEmpty()) {
                TryCatchRegion region = new TryCatchRegion(
                        startPc, endPc, handlers);
                regions.add(region);
            }
        }
        return regions;
    }

    /**
     * Finds a try/catch region that starts at the given offset.
     *
     * @param offset the byte offset
     * @param regions the list of try/catch regions
     * @return the matching region, or null
     */
    private TryCatchRegion findTryCatchRegion(int offset,
            List<TryCatchRegion> regions) {
        for (TryCatchRegion region : regions) {
            if (region.startPc == offset && !region.isProcessed()) {
                return region;
            }
        }
        return null;
    }

    /**
     * Processes a try/catch region into structured statements.
     *
     * @param tcr the try/catch region
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return the list of statements representing the try/catch
     */
    private List<ArkTSStatement> processTryCatch(TryCatchRegion tcr,
            DecompilationContext ctx, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Build try/catch regions for nested lookup
        List<TryCatchRegion> allRegions =
                buildTryCatchRegions(ctx, cfg);

        // Collect all blocks within the try range, processing
        // nested try/catch regions first
        List<ArkTSStatement> tryBodyStmts = new ArrayList<>();
        for (BasicBlock block : cfg.getBlocks()) {
            if (block.getStartOffset() >= tcr.startPc
                    && block.getStartOffset() < tcr.endPc
                    && !visited.contains(block)) {
                // Check if this block starts a nested try/catch region
                TryCatchRegion nestedTcr = findTryCatchRegion(
                        block.getStartOffset(), allRegions);
                if (nestedTcr != null && nestedTcr != tcr
                        && !nestedTcr.isProcessed()
                        && nestedTcr.startPc >= tcr.startPc
                        && nestedTcr.endPc <= tcr.endPc) {
                    // Nested try/catch: process it recursively
                    nestedTcr.markProcessed();
                    visited.add(block);
                    tryBodyStmts.addAll(
                            processTryCatch(nestedTcr, ctx, cfg, visited));
                } else {
                    visited.add(block);
                    tryBodyStmts.addAll(
                            processBlockInstructions(block, ctx));
                }
            }
        }
        ArkTSStatement tryBody =
                new ArkTSStatement.BlockStatement(tryBodyStmts);

        // Process catch handlers
        ArkTSStatement catchBody = null;
        String catchParam = "e";
        if (!tcr.handlers.isEmpty()) {
            CatchHandler firstHandler = tcr.handlers.get(0);
            if (firstHandler.typeName != null) {
                catchParam = "e";
            }
            List<ArkTSStatement> catchBodyStmts = new ArrayList<>();
            BasicBlock handlerBlock =
                    cfg.getBlockAt(firstHandler.handlerPc);
            if (handlerBlock != null
                    && !visited.contains(handlerBlock)) {
                visited.add(handlerBlock);
                catchBodyStmts.addAll(
                        processBlockInstructions(handlerBlock, ctx));
            }
            if (!catchBodyStmts.isEmpty()) {
                catchBody = new ArkTSStatement.BlockStatement(
                        catchBodyStmts);
            }
        }

        ArkTSStatement.TryCatchStatement tryCatch =
                new ArkTSStatement.TryCatchStatement(
                        tryBody, catchParam, catchBody, null);
        stmts.add(tryCatch);
        return stmts;
    }

    /**
     * Resolves a type index to a type name from the ABC file.
     *
     * @param typeIdx the type index
     * @param abcFile the ABC file
     * @return the type name, or null
     */
    private String resolveTypeName(int typeIdx, AbcFile abcFile) {
        if (abcFile == null) {
            return null;
        }
        // Try to resolve from the lnp index (string table)
        try {
            List<Long> lnpIndex = abcFile.getLnpIndex();
            if (typeIdx >= 0 && typeIdx < lnpIndex.size()) {
                long strOff = lnpIndex.get(typeIdx);
                byte[] data = abcFile.getRawData();
                if (data != null && strOff >= 0 && strOff < data.length) {
                    return DecompilationContext.readMutf8At(
                            data, (int) strOff);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Determines if a block is dead code (unreachable).
     * A block is dead if all its predecessors are already visited and
     * end with an unconditional terminator (return, unconditional jump).
     *
     * @param block the block to check
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return true if the block appears to be dead code
     */
    private boolean isDeadCode(BasicBlock block, ControlFlowGraph cfg,
            Set<BasicBlock> visited) {
        // The entry block is never dead code
        if (block == cfg.getEntryBlock()) {
            return false;
        }
        // Exception handler entry points are not dead code
        for (CFGEdge pred : block.getPredecessors()) {
            if (pred.getType() == EdgeType.EXCEPTION_HANDLER) {
                return false;
            }
        }
        // If there are no predecessors at all, it's dead
        List<CFGEdge> preds = block.getPredecessors();
        if (preds.isEmpty()) {
            return true;
        }
        // Check if all predecessors are visited and end with terminators
        // that don't reach this block
        for (CFGEdge pred : preds) {
            if (pred.getType() == EdgeType.EXCEPTION_HANDLER) {
                return false;
            }
            BasicBlock predBlock = cfg.getBlockAt(pred.getFromOffset());
            if (predBlock == null) {
                continue;
            }
            if (!visited.contains(predBlock)) {
                return false;
            }
        }
        // All predecessors visited; if none actually flow here, it's dead
        return true;
    }

    /**
     * Sanitizes a raw ABC class name into a valid ArkTS identifier.
     * ABC class names may include module path prefixes separated by '/'.
     *
     * @param rawName the raw class name from the ABC file
     * @return the sanitized class name
     */
    private String sanitizeClassName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "AnonymousClass";
        }
        // Take the last segment after '/'
        int lastSlash = rawName.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < rawName.length() - 1) {
            return rawName.substring(lastSlash + 1);
        }
        return rawName;
    }

    /**
     * Determines whether a method is a constructor.
     *
     * @param method the method to check
     * @param className the enclosing class name
     * @return true if the method is a constructor
     */
    private boolean isConstructorMethod(AbcMethod method, String className) {
        String name = method.getName();
        return "<init>".equals(name) || "<ctor>".equals(name)
                || className.equals(name);
    }

    /**
     * Resolves the super class name from the super class offset.
     *
     * @param superClassOff the offset of the super class in the ABC file
     * @param abcFile the parent ABC file
     * @return the super class name, or null if no super class
     */
    private String resolveSuperClassName(long superClassOff, AbcFile abcFile) {
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

    /**
     * Determines whether a class should be represented as an ArkTS struct.
     * Structs in ArkTS are typically UI components.
     *
     * @param abcClass the ABC class
     * @return true if the class is a struct
     */
    private boolean isStructClass(AbcClass abcClass) {
        String name = abcClass.getName();
        if (name == null) {
            return false;
        }
        // Heuristic: ArkTS structs often have names ending in "Page" or "Component"
        // or have @Component decorator markers
        return name.endsWith("Page") || name.endsWith("Component")
                || name.endsWith("View") || name.endsWith("Widget");
    }

    /**
     * Detects decorators applied to a class.
     * In ABC bytecode, decorators are often stored as annotations or
     * recognizable patterns in the class metadata.
     *
     * @param abcClass the ABC class
     * @return the list of decorator names
     */
    private List<String> detectDecorators(AbcClass abcClass) {
        List<String> decorators = new ArrayList<>();
        String name = abcClass.getName();
        if (name == null) {
            return decorators;
        }
        // Detect common ArkTS decorator patterns from class name
        if (name.endsWith("Page") || name.contains("Page")) {
            decorators.add("Entry");
            decorators.add("Component");
        }
        // Detect from field decorator patterns
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

    /**
     * Detects decorator type from a field name pattern.
     *
     * @param fieldName the field name
     * @return the decorator name, or null
     */
    private String detectFieldDecorator(String fieldName) {
        // Common ArkTS state management decorators
        if (fieldName.startsWith("__") && fieldName.endsWith("__")) {
            String inner = fieldName.substring(2, fieldName.length() - 2);
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

    /**
     * Extracts type parameters from class metadata.
     *
     * @param abcClass the ABC class
     * @return the list of type parameters (may be empty)
     */
    private List<ArkTSStatement.TypeParameter> extractTypeParameters(
            AbcClass abcClass) {
        // Type parameters are typically encoded in the ABC class metadata
        // For now, return empty as ABC format does not explicitly store
        // generic type parameters in the class definition
        return Collections.emptyList();
    }

    /**
     * Infers the type of a field from its metadata.
     *
     * @param field the ABC field
     * @return the inferred type name, or null
     */
    private String inferFieldType(AbcField field) {
        // Type inference from field metadata is limited in ABC
        // The typeIdx could be resolved via the type table if available
        return null;
    }

    /**
     * Converts access flags to a modifier string.
     *
     * @param flags the access flags
     * @return the modifier string, or null
     */
    private static String accessFlagsToModifier(long flags) {
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

    /**
     * Determines if a class is exported.
     *
     * @param abcClass the ABC class
     * @return true if the class is exported
     */
    private boolean isExported(AbcClass abcClass) {
        return (abcClass.getAccessFlags() & AbcAccessFlags.ACC_PUBLIC) != 0;
    }

    // --- Enum detection ---

    /**
     * Detects enum declarations from literal arrays in the ABC file.
     * Enums in Ark bytecode are often compiled as literal arrays with
     * consecutive integer values or string constants.
     *
     * @param abcFile the ABC file
     * @return the list of detected enum declarations
     */
    private List<ArkTSStatement> detectEnumsFromLiteralArrays(AbcFile abcFile) {
        List<ArkTSStatement> enums = new ArrayList<>();
        if (abcFile == null || abcFile.getLiteralArrays().isEmpty()) {
            return enums;
        }

        // Check classes marked as enums
        for (AbcClass cls : abcFile.getClasses()) {
            if ((cls.getAccessFlags() & AbcAccessFlags.ACC_ENUM) != 0) {
                List<ArkTSStatement.EnumDeclaration.EnumMember> members =
                        new ArrayList<>();
                for (AbcField field : cls.getFields()) {
                    if ((field.getAccessFlags()
                            & AbcAccessFlags.ACC_STATIC) != 0) {
                        members.add(
                                new ArkTSStatement.EnumDeclaration.EnumMember(
                                        field.getName(), null));
                    }
                }
                if (!members.isEmpty()) {
                    enums.add(new ArkTSStatement.EnumDeclaration(
                            sanitizeClassName(cls.getName()), members));
                }
            }
        }

        return enums;
    }

    // --- Statement generation ---

    private List<ArkTSStatement> generateStatements(DecompilationContext ctx) {
        ControlFlowGraph cfg = ctx.cfg;
        List<BasicBlock> blocks = cfg.getBlocks();

        if (blocks.isEmpty()) {
            return Collections.emptyList();
        }

        if (blocks.size() == 1) {
            // Single block: linear code, no control flow
            // Check for infinite loop (jmp to self)
            BasicBlock single = blocks.get(0);
            ArkInstruction lastInsn = single.getLastInstruction();
            if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int target = ControlFlowGraphAccessor
                        .getJumpTarget(lastInsn);
                if (target == single.getStartOffset()) {
                    // Infinite loop: jmp to self
                    List<ArkTSStatement> bodyStmts =
                            processBlockInstructionsExcluding(
                                    single, ctx, lastInsn);
                    ArkTSStatement body =
                            new ArkTSStatement.BlockStatement(bodyStmts);
                    ArkTSExpression trueCond =
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN);
                    return List.of(new ArkTSStatement.WhileStatement(
                            trueCond, body));
                }
            }
            return processBlockInstructions(single, ctx);
        }

        // Use CFG-based decompilation with structured control flow
        Set<BasicBlock> visited = new HashSet<>();
        return reconstructControlFlow(cfg, blocks, ctx, visited);
    }

    // --- Control flow reconstruction ---

    /**
     * Provides package-private access to jump target computation.
     */
    private static class ControlFlowGraphAccessor {
        static int getJumpTarget(ArkInstruction insn) {
            return ControlFlowGraph.getJumpTargetPublic(insn);
        }
    }

    /**
     * Reconstructs structured control flow from the CFG.
     *
     * <p>Walks the CFG in order, detecting if/else, while, for, do/while,
     * try/catch, switch patterns and producing structured ArkTS statements.
     * Also handles dead code after unconditional jumps and infinite loops.
     *
     * @param cfg the control flow graph
     * @param blocks the list of basic blocks
     * @param ctx the decompilation context
     * @param visited set of already-processed blocks
     * @return the list of reconstructed statements
     */
    private List<ArkTSStatement> reconstructControlFlow(ControlFlowGraph cfg,
            List<BasicBlock> blocks, DecompilationContext ctx,
            Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // First, detect try/catch blocks and build a lookup from try-block
        // ranges to their handler info
        List<TryCatchRegion> tryCatchRegions =
                buildTryCatchRegions(ctx, cfg);

        for (BasicBlock block : blocks) {
            if (visited.contains(block)) {
                continue;
            }

            // Check if this block is dead code (unreachable after a
            // terminator in a prior block)
            if (isDeadCode(block, cfg, visited)) {
                visited.add(block);
                continue;
            }

            // Check if this block starts a try/catch region
            TryCatchRegion tcr = findTryCatchRegion(
                    block.getStartOffset(), tryCatchRegions);
            if (tcr != null && !tcr.isProcessed()) {
                tcr.markProcessed();
                visited.add(block);
                stmts.addAll(processTryCatch(tcr, ctx, cfg, visited));
                continue;
            }

            // Check for infinite loop (jmp to self)
            ArkInstruction lastInsn = block.getLastInstruction();
            if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int target = ControlFlowGraphAccessor
                        .getJumpTarget(lastInsn);
                if (target == block.getStartOffset()) {
                    visited.add(block);
                    // Push loop context for break/continue in infinite loop
                    ctx.pushLoopContext(block.getStartOffset(),
                            block.getEndOffset());
                    List<ArkTSStatement> bodyStmts =
                            processBlockInstructionsExcluding(
                                    block, ctx, lastInsn);
                    ctx.popLoopContext();
                    ArkTSStatement body =
                            new ArkTSStatement.BlockStatement(bodyStmts);
                    ArkTSExpression trueCond =
                            new ArkTSExpression.LiteralExpression("true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN);
                    stmts.add(new ArkTSStatement.WhileStatement(
                            trueCond, body));
                    continue;
                }
            }

            // Detect control flow pattern from this block
            ControlFlowPattern pattern = detectPattern(block, cfg, visited);

            switch (pattern.type) {
                case IF_ELSE:
                    visited.add(block);
                    stmts.addAll(processIfElse(block, pattern, ctx, cfg,
                            visited));
                    break;
                case IF_ONLY:
                    visited.add(block);
                    stmts.addAll(processIfOnly(block, pattern, ctx, cfg,
                            visited));
                    break;
                case WHILE_LOOP:
                    visited.add(block);
                    stmts.addAll(processWhileLoop(block, pattern, ctx, cfg,
                            visited));
                    break;
                case FOR_LOOP:
                    visited.add(block);
                    stmts.addAll(processForLoop(block, pattern, ctx, cfg,
                            visited));
                    break;
                case DO_WHILE:
                    visited.add(block);
                    stmts.addAll(processDoWhile(block, pattern, ctx, cfg,
                            visited));
                    break;
                case BREAK:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    stmts.add(new ArkTSStatement.BreakStatement());
                    break;
                case CONTINUE:
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    stmts.add(new ArkTSStatement.ContinueStatement());
                    break;
                case TERNARY:
                    visited.add(block);
                    stmts.addAll(processTernary(block, pattern, ctx, cfg,
                            visited));
                    break;
                case SHORT_CIRCUIT_AND:
                    visited.add(block);
                    stmts.addAll(processShortCircuitAnd(block, pattern, ctx,
                            cfg, visited));
                    break;
                case SHORT_CIRCUIT_OR:
                    visited.add(block);
                    stmts.addAll(processShortCircuitOr(block, pattern, ctx,
                            cfg, visited));
                    break;
                default:
                    // LINEAR or UNKNOWN: process instructions normally
                    visited.add(block);
                    stmts.addAll(processBlockInstructions(block, ctx));
                    break;
            }
        }

        return stmts;
    }

    /**
     * The type of control flow pattern detected.
     */
    private enum PatternType {
        LINEAR, IF_ONLY, IF_ELSE, WHILE_LOOP, FOR_LOOP, DO_WHILE, BREAK,
        CONTINUE, TERNARY, SHORT_CIRCUIT_AND, SHORT_CIRCUIT_OR, UNKNOWN
    }

    /**
     * Describes a detected control flow pattern.
     */
    private static class ControlFlowPattern {
        PatternType type;
        BasicBlock conditionBlock;
        BasicBlock trueBlock;
        BasicBlock falseBlock;
        BasicBlock mergeBlock;
        BasicBlock initBlock;
        BasicBlock updateBlock;

        // Short-circuit fields
        ArkTSExpression shortCircuitLeft;
        ArkTSExpression shortCircuitRight;

        // Ternary fields
        String ternaryTargetVar;
        ArkTSExpression ternaryTrueValue;
        ArkTSExpression ternaryFalseValue;
        ArkTSExpression ternaryCondition;

        ControlFlowPattern(PatternType type) {
            this.type = type;
        }
    }

    private ControlFlowPattern detectPattern(BasicBlock block,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<CFGEdge> successors = block.getSuccessors();

        // Check if this block ends with a conditional branch
        if (successors.size() == 2) {
            BasicBlock trueBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_TRUE);
            BasicBlock falseBranch = getSuccessorByType(successors,
                    EdgeType.CONDITIONAL_FALSE);

            if (trueBranch == null || falseBranch == null) {
                // Fallback: use edge order
                trueBranch = cfg.getBlockAt(successors.get(0).getToOffset());
                falseBranch = cfg.getBlockAt(successors.get(1).getToOffset());
            }

            if (trueBranch == null || falseBranch == null) {
                return new ControlFlowPattern(PatternType.LINEAR);
            }

            // Check for while loop: one successor jumps back to this block
            // or an earlier offset
            if (trueBranch.getStartOffset() <= block.getStartOffset()
                    || falseBranch.getStartOffset() <= block
                            .getStartOffset()) {
                BasicBlock loopBody = trueBranch.getStartOffset() > block
                        .getStartOffset() ? trueBranch : falseBranch;
                ControlFlowPattern p = new ControlFlowPattern(
                        PatternType.WHILE_LOOP);
                p.conditionBlock = block;
                p.trueBlock = loopBody;
                return p;
            }

            // Check for ternary pattern: condition -> jeqz else_branch;
            // true_branch has single sta + jmp to merge; else_branch has
            // single sta at merge
            ControlFlowPattern ternaryP = detectTernaryPattern(
                    block, trueBranch, falseBranch, cfg);
            if (ternaryP != null) {
                return ternaryP;
            }

            // Check for short-circuit AND/OR: two consecutive conditional
            // branches to the same target
            ControlFlowPattern shortCircuitP = detectShortCircuitPattern(
                    block, trueBranch, falseBranch, cfg, visited);
            if (shortCircuitP != null) {
                return shortCircuitP;
            }

            // Check for if/else: both branches eventually merge
            BasicBlock merge = findMergeBlock(trueBranch, falseBranch, cfg);
            if (merge != null && merge != trueBranch
                    && merge != falseBranch) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.IF_ELSE);
                p.conditionBlock = block;
                p.trueBlock = trueBranch;
                p.falseBlock = falseBranch;
                p.mergeBlock = merge;
                return p;
            }

            // if-only pattern (one branch merges back, the other continues)
            if (trueBranch != falseBranch) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.IF_ONLY);
                p.conditionBlock = block;
                p.trueBlock = trueBranch;
                p.falseBlock = falseBranch;
                return p;
            }
        }

        // Check for do/while: the block has a back-edge from a successor
        if (successors.size() == 1) {
            BasicBlock succ = cfg.getBlockAt(
                    successors.get(0).getToOffset());
            if (succ != null && hasBackEdgeTo(succ, block, cfg)) {
                ControlFlowPattern p =
                        new ControlFlowPattern(PatternType.DO_WHILE);
                p.conditionBlock = succ;
                p.trueBlock = block;
                return p;
            }
        }

        return new ControlFlowPattern(PatternType.LINEAR);
    }

    /**
     * Detects a ternary expression pattern.
     * Pattern: condition -> jeqz else_branch
     * true_branch: [compute val1]; sta target; jmp merge
     * else_branch: [compute val2]; sta target
     * merge: ...
     *
     * @param condBlock the condition block
     * @param trueBranch the branch taken when condition is true
     * @param falseBranch the branch taken when condition is false
     * @param cfg the control flow graph
     * @return the ternary pattern, or null if not detected
     */
    private ControlFlowPattern detectTernaryPattern(BasicBlock condBlock,
            BasicBlock trueBranch, BasicBlock falseBranch,
            ControlFlowGraph cfg) {

        // Check if trueBranch ends with an unconditional jmp
        ArkInstruction trueLast = trueBranch.getLastInstruction();
        if (trueLast == null
                || !ArkOpcodesCompat.isUnconditionalJump(trueLast.getOpcode())) {
            return null;
        }

        // The jmp target should be the merge point
        int mergeOffset = ControlFlowGraphAccessor.getJumpTarget(trueLast);
        BasicBlock mergeBlock = cfg.getBlockAt(mergeOffset);

        // Check if falseBranch's fall-through goes to the merge point
        if (mergeBlock == null) {
            return null;
        }

        // The falseBranch should flow into the merge block
        ArkInstruction falseLast = falseBranch.getLastInstruction();
        if (falseLast == null) {
            return null;
        }
        int falseEnd = falseLast.getNextOffset();
        // The merge should be right after the falseBranch
        boolean falseFlowsToMerge = falseEnd == mergeOffset
                || (mergeOffset > falseBranch.getStartOffset()
                        && mergeOffset <= falseEnd + 5);

        if (!falseFlowsToMerge) {
            // Also check if falseBranch ends with jmp to merge
            if (!ArkOpcodesCompat.isUnconditionalJump(falseLast.getOpcode())) {
                return null;
            }
            int falseJmpTarget =
                    ControlFlowGraphAccessor.getJumpTarget(falseLast);
            if (falseJmpTarget != mergeOffset) {
                return null;
            }
        }

        // Check that trueBranch has exactly: some value-producing insn(s)
        // + sta + jmp. The sta tells us the target variable.
        String targetVar = null;
        ArkTSExpression trueValue = null;
        ArkTSExpression falseValue = null;

        // Scan trueBranch for sta instruction before the jmp
        for (ArkInstruction insn : trueBranch.getInstructions()) {
            if (insn == trueLast) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                targetVar = "v" + insn.getOperands().get(0).getValue();
            }
        }

        if (targetVar == null) {
            return null;
        }

        // Check that falseBranch also has a sta to the same variable
        boolean hasMatchingSta = false;
        for (ArkInstruction insn : falseBranch.getInstructions()) {
            if (insn == falseLast && !ArkOpcodesCompat.isUnconditionalJump(
                    falseLast.getOpcode())) {
                break;
            }
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                String falseVar =
                        "v" + insn.getOperands().get(0).getValue();
                if (falseVar.equals(targetVar)) {
                    hasMatchingSta = true;
                }
            }
        }

        if (!hasMatchingSta) {
            return null;
        }

        // This looks like a ternary pattern
        ControlFlowPattern p = new ControlFlowPattern(PatternType.TERNARY);
        p.conditionBlock = condBlock;
        p.trueBlock = trueBranch;
        p.falseBlock = falseBranch;
        p.mergeBlock = mergeBlock;
        p.ternaryTargetVar = targetVar;
        return p;
    }

    /**
     * Detects short-circuit evaluation patterns (&& and ||).
     *
     * <p>For AND: Block A has jeqz L; Block B (fall-through) also has
     * jeqz L. Both false-paths go to the same target.
     *
     * <p>For OR: Block A has jnez L; Block B (fall-through) also has
     * jnez L. Both true-paths go to the same target.
     *
     * @param block the first condition block
     * @param trueBranch the true branch from block
     * @param falseBranch the false branch from block
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return the short-circuit pattern, or null if not detected
     */
    private ControlFlowPattern detectShortCircuitPattern(BasicBlock block,
            BasicBlock trueBranch, BasicBlock falseBranch,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        ArkInstruction lastInsn = block.getLastInstruction();
        if (lastInsn == null) {
            return null;
        }
        int opcode = lastInsn.getOpcode();

        // For short-circuit AND (&&):
        // Block ends with jeqz to some target. The fall-through (trueBranch)
        // also ends with jeqz to the same target.
        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16) {
            int target1 = ControlFlowGraphAccessor.getJumpTarget(lastInsn);

            // The fall-through is the "trueBranch" for jeqz (condition
            // non-zero). Check if the fall-through block also ends with jeqz
            // to the same target.
            BasicBlock fallThrough = trueBranch;
            // For jeqz: CONDITIONAL_TRUE means branch taken (acc==0),
            // CONDITIONAL_FALSE means fall-through (acc!=0)
            // Actually in the CFG, CONDITIONAL_TRUE is the branch target
            // for jeqz. So the fall-through is falseBranch.
            // Wait - let me re-check. jeqz branches when acc==0.
            // In the CFG: CONDITIONAL_TRUE = branch target,
            // CONDITIONAL_FALSE = fall-through.
            // So for jeqz: trueBranch = where we jump when acc==0,
            // falseBranch = fall-through.
            // For && : a && b: if a is false, skip to end. If a is true,
            // check b.
            // jeqz skip (a is false -> skip); [check b]; jeqz skip
            // (b is false -> skip)
            // So the falseBranch (fall-through) should also end with jeqz
            // to the same target.

            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode() == ArkOpcodesCompat.JEQZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JEQZ_IMM16)) {
                int target2 =
                        ControlFlowGraphAccessor.getJumpTarget(nextLast);
                if (target1 == target2) {
                    // Short-circuit AND pattern detected
                    ControlFlowPattern p = new ControlFlowPattern(
                            PatternType.SHORT_CIRCUIT_AND);
                    p.conditionBlock = block;
                    p.trueBlock = falseBranch;
                    p.falseBlock = trueBranch;
                    p.mergeBlock = cfg.getBlockAt(target1);
                    return p;
                }
            }
        }

        // For short-circuit OR (||):
        // Block ends with jnez to some target. The fall-through
        // also ends with jnez to the same target.
        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16) {
            int target1 = ControlFlowGraphAccessor.getJumpTarget(lastInsn);

            // For jnez: trueBranch = where we jump when acc!=0,
            // falseBranch = fall-through.
            BasicBlock nextCondBlock = falseBranch;
            ArkInstruction nextLast = nextCondBlock.getLastInstruction();
            if (nextLast != null
                    && (nextLast.getOpcode() == ArkOpcodesCompat.JNEZ_IMM8
                    || nextLast.getOpcode()
                            == ArkOpcodesCompat.JNEZ_IMM16)) {
                int target2 =
                        ControlFlowGraphAccessor.getJumpTarget(nextLast);
                if (target1 == target2) {
                    // Short-circuit OR pattern detected
                    ControlFlowPattern p = new ControlFlowPattern(
                            PatternType.SHORT_CIRCUIT_OR);
                    p.conditionBlock = block;
                    p.trueBlock = falseBranch;
                    p.falseBlock = trueBranch;
                    p.mergeBlock = cfg.getBlockAt(target1);
                    return p;
                }
            }
        }

        return null;
    }

    private List<ArkTSStatement> processIfElse(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process non-branch instructions in the condition block
        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        // Get the condition expression from the last instruction
        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Mark the true and false blocks as visited
        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Process the true branch
        List<ArkTSStatement> thenStmts = processBlockInstructions(
                pattern.trueBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        // Process the false branch
        List<ArkTSStatement> elseStmts = processBlockInstructions(
                pattern.falseBlock, ctx);
        ArkTSStatement elseBlock =
                new ArkTSStatement.BlockStatement(elseStmts);

        ArkTSStatement.IfStatement ifStmt =
                new ArkTSStatement.IfStatement(condition, thenBlock,
                        elseBlock);
        stmts.add(ifStmt);

        return stmts;
    }

    private List<ArkTSStatement> processIfOnly(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // For jeqz-style branches, the "true" path (branch taken) is the
        // falseBranch, and fall-through is the normal path. We need to negate
        // the condition for if-style output.
        // The true branch is where we go when condition is true
        BasicBlock branchBlock = pattern.trueBlock;
        visited.add(branchBlock);

        List<ArkTSStatement> thenStmts =
                processBlockInstructions(branchBlock, ctx);
        ArkTSStatement thenBlock =
                new ArkTSStatement.BlockStatement(thenStmts);

        // Negate the condition for jeqz-style (branch-on-zero/false)
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        ArkTSStatement.IfStatement ifStmt =
                new ArkTSStatement.IfStatement(effectiveCondition, thenBlock,
                        null);
        stmts.add(ifStmt);

        return stmts;
    }

    private List<ArkTSStatement> processWhileLoop(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Negate condition for jeqz-style (branch when false means loop body
        // is on the fall-through path)
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
            effectiveCondition =
                    new ArkTSExpression.UnaryExpression("!", condition, true);
        }

        // Determine loop end offset for break/continue detection.
        // The loop ends where the false branch goes (past the loop body).
        int loopHeaderOffset = condBlock.getStartOffset();
        int loopEndOffset = estimateLoopEndOffset(condBlock, pattern, cfg);

        // Push loop context for break/continue detection
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        // Pop loop context
        ctx.popLoopContext();

        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(effectiveCondition,
                        bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processForLoop(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process init block if present
        if (pattern.initBlock != null) {
            stmts.addAll(processBlockInstructions(pattern.initBlock, ctx));
            visited.add(pattern.initBlock);
        }

        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // Process update expression
        ArkTSExpression update = null;
        if (pattern.updateBlock != null) {
            List<ArkTSStatement> updateStmts =
                    processBlockInstructions(pattern.updateBlock, ctx);
            visited.add(pattern.updateBlock);
            if (!updateStmts.isEmpty() && updateStmts.get(
                    0) instanceof ArkTSStatement.ExpressionStatement) {
                update = ((ArkTSStatement.ExpressionStatement) updateStmts
                        .get(0)).getExpression();
            }
        }

        visited.add(pattern.trueBlock);
        List<ArkTSStatement> bodyStmts =
                processBlockInstructions(pattern.trueBlock, ctx);
        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);

        ArkTSStatement.WhileStatement whileStmt =
                new ArkTSStatement.WhileStatement(condition, bodyBlock);
        stmts.add(whileStmt);

        return stmts;
    }

    private List<ArkTSStatement> processDoWhile(BasicBlock block,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Determine loop end offset for break/continue detection.
        int loopHeaderOffset = block.getStartOffset();
        int loopEndOffset = block.getEndOffset();
        if (pattern.conditionBlock != null) {
            // The condition block's successors after the loop tell us where
            // the loop ends
            for (CFGEdge edge : pattern.conditionBlock.getSuccessors()) {
                if (edge.getToOffset() > loopEndOffset) {
                    loopEndOffset = edge.getToOffset();
                }
            }
        }

        // Push loop context for break/continue detection
        ctx.pushLoopContext(loopHeaderOffset, loopEndOffset);

        List<ArkTSStatement> bodyStmts = processBlockInstructions(block, ctx);

        ArkTSExpression condition =
                new ArkTSExpression.VariableExpression(ACC);
        if (pattern.conditionBlock != null) {
            visited.add(pattern.conditionBlock);
            List<ArkTSStatement> condStmts =
                    processBlockInstructionsExcluding(
                            pattern.conditionBlock, ctx,
                            pattern.conditionBlock.getLastInstruction());
            bodyStmts.addAll(condStmts);
            condition = getConditionExpression(
                    pattern.conditionBlock.getLastInstruction(), ctx);
            if (condition == null) {
                condition = new ArkTSExpression.VariableExpression(ACC);
            }
        }

        // Pop loop context
        ctx.popLoopContext();

        ArkTSStatement bodyBlock =
                new ArkTSStatement.BlockStatement(bodyStmts);
        ArkTSStatement.DoWhileStatement doWhile =
                new ArkTSStatement.DoWhileStatement(bodyBlock, condition);
        stmts.add(doWhile);

        return stmts;
    }

    /**
     * Estimates the end offset of a while loop from its pattern and CFG.
     * The loop ends where the false branch of the condition goes.
     *
     * @param condBlock the condition block
     * @param pattern the detected pattern
     * @param cfg the control flow graph
     * @return the estimated loop end offset
     */
    private int estimateLoopEndOffset(BasicBlock condBlock,
            ControlFlowPattern pattern, ControlFlowGraph cfg) {
        // For while loops, one successor goes to the body (forward),
        // the other goes past the loop (the exit).
        // The exit is the loop end.
        List<CFGEdge> successors = condBlock.getSuccessors();
        if (successors.size() == 2) {
            int target1 = successors.get(0).getToOffset();
            int target2 = successors.get(1).getToOffset();
            // The loop body target is the one with higher offset
            // (forward branch). The exit is the one with lower offset
            // (back branch or branch past loop).
            if (target1 <= condBlock.getStartOffset()) {
                // target1 is the back-edge, target2 is body, exit is past body
                // Find where the body exits to
                return findLoopExit(pattern.trueBlock, condBlock, cfg);
            }
            if (target2 <= condBlock.getStartOffset()) {
                // target2 is the back-edge, target1 is body
                return findLoopExit(pattern.trueBlock, condBlock, cfg);
            }
            // Both targets are forward. The exit is the one that doesn't
            // eventually loop back.
            return Math.max(target1, target2);
        }
        // Fallback: use the body block's end offset
        if (pattern.trueBlock != null) {
            return pattern.trueBlock.getEndOffset();
        }
        return condBlock.getEndOffset();
    }

    /**
     * Finds the exit offset of a loop by walking successors until finding
     * one that goes past the loop header.
     *
     * @param bodyBlock the loop body block
     * @param headerBlock the loop header block
     * @param cfg the control flow graph
     * @return the estimated loop exit offset
     */
    private int findLoopExit(BasicBlock bodyBlock, BasicBlock headerBlock,
            ControlFlowGraph cfg) {
        if (bodyBlock == null) {
            return headerBlock.getEndOffset();
        }
        // The exit is past the body. Use the body's end offset as a
        // conservative estimate.
        int maxOffset = bodyBlock.getEndOffset();
        // Walk body's successors to find the furthest forward point
        for (CFGEdge edge : bodyBlock.getSuccessors()) {
            if (edge.getToOffset() > maxOffset) {
                maxOffset = edge.getToOffset();
            }
        }
        return maxOffset;
    }

    // --- Ternary expression processing ---

    /**
     * Processes a ternary expression pattern into a variable declaration.
     *
     * <p>Pattern: condition -> jeqz else; true_val; sta vN; jmp join;
     * else: false_val; sta vN; join: ...
     *
     * @param condBlock the condition block
     * @param pattern the ternary pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return the list of statements
     */
    private List<ArkTSStatement> processTernary(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process non-branch instructions in condition block
        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        // Get condition
        ArkTSExpression condition = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (condition == null) {
            condition = new ArkTSExpression.VariableExpression(ACC);
        }

        // For jeqz-style branches, negate the condition
        int lastOpcode = condBlock.getLastInstruction().getOpcode();
        ArkTSExpression effectiveCondition = condition;
        if (isBranchOnFalse(lastOpcode)) {
            effectiveCondition = condition;
            // The ternary already has the right polarity:
            // jeqz else -> condition ? trueVal : falseVal
        }

        // Mark blocks as visited
        visited.add(pattern.trueBlock);
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Get true value from trueBranch (excluding sta and jmp)
        ArkTSExpression trueValue =
                extractBlockValue(pattern.trueBlock, ctx);

        // Get false value from falseBranch (excluding sta)
        ArkTSExpression falseValue =
                extractBlockValue(pattern.falseBlock, ctx);

        if (trueValue != null && falseValue != null) {
            ArkTSExpression ternaryExpr =
                    new ArkTSExpression.ConditionalExpression(
                            effectiveCondition, trueValue, falseValue);
            String targetVar = pattern.ternaryTargetVar;
            ArkTSStatement decl = new ArkTSStatement.VariableDeclaration(
                    "let", targetVar, null, ternaryExpr);
            stmts.add(decl);
        } else {
            // Fallback to if/else if we can't extract values
            List<ArkTSStatement> thenStmts =
                    processBlockInstructions(pattern.trueBlock, ctx);
            List<ArkTSStatement> elseStmts =
                    processBlockInstructions(pattern.falseBlock, ctx);
            ArkTSStatement thenBlock =
                    new ArkTSStatement.BlockStatement(thenStmts);
            ArkTSStatement elseBlock =
                    new ArkTSStatement.BlockStatement(elseStmts);
            stmts.add(new ArkTSStatement.IfStatement(
                    effectiveCondition, thenBlock, elseBlock));
        }

        return stmts;
    }

    /**
     * Extracts the accumulator value produced by a block, excluding
     * sta/jmp instructions.
     *
     * @param block the block
     * @param ctx the decompilation context
     * @return the last accumulator value, or null
     */
    private ArkTSExpression extractBlockValue(BasicBlock block,
            DecompilationContext ctx) {
        ArkTSExpression accValue = null;
        TypeInference typeInf = new TypeInference();
        Set<String> declaredVars = new HashSet<>();
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }

        for (ArkInstruction insn : block.getInstructions()) {
            int opcode = insn.getOpcode();
            // Skip sta, jmp, conditional branches
            if (opcode == ArkOpcodesCompat.STA) {
                continue;
            }
            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                continue;
            }
            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                continue;
            }
            StatementResult result = processInstruction(
                    insn, ctx, accValue, declaredVars, typeInf);
            if (result != null) {
                accValue = result.newAccValue != null
                        ? result.newAccValue : accValue;
            }
        }
        return accValue;
    }

    // --- Short-circuit processing ---

    /**
     * Processes a short-circuit AND pattern (a && b).
     *
     * @param condBlock the first condition block
     * @param pattern the short-circuit AND pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return the list of statements
     */
    private List<ArkTSStatement> processShortCircuitAnd(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process non-branch instructions in first condition block
        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        // Get left condition
        ArkTSExpression leftCond = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(ACC);
        }

        // Get right condition from the second block
        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts = processBlockInstructionsExcluding(
                secondCondBlock, ctx, secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond = getConditionExpression(
                secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(ACC);
        }

        // Mark other blocks as visited
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Build combined condition: left && right
        ArkTSExpression combined = new ArkTSExpression.BinaryExpression(
                leftCond, "&&", rightCond);

        // The "body" of the combined condition is whatever comes after
        // the merge. For now, emit as an if statement with the combined
        // condition if there's content in the merge block.
        // If the short-circuit is used as a guard (no body after merge),
        // just emit the expression.
        stmts.add(new ArkTSStatement.ExpressionStatement(combined));

        return stmts;
    }

    /**
     * Processes a short-circuit OR pattern (a || b).
     *
     * @param condBlock the first condition block
     * @param pattern the short-circuit OR pattern
     * @param ctx the decompilation context
     * @param cfg the control flow graph
     * @param visited set of visited blocks
     * @return the list of statements
     */
    private List<ArkTSStatement> processShortCircuitOr(BasicBlock condBlock,
            ControlFlowPattern pattern, DecompilationContext ctx,
            ControlFlowGraph cfg, Set<BasicBlock> visited) {

        List<ArkTSStatement> stmts = new ArrayList<>();

        // Process non-branch instructions in first condition block
        List<ArkTSStatement> preStmts = processBlockInstructionsExcluding(
                condBlock, ctx, condBlock.getLastInstruction());
        stmts.addAll(preStmts);

        // Get left condition
        ArkTSExpression leftCond = getConditionExpression(
                condBlock.getLastInstruction(), ctx);
        if (leftCond == null) {
            leftCond = new ArkTSExpression.VariableExpression(ACC);
        }

        // Get right condition from the second block
        BasicBlock secondCondBlock = pattern.trueBlock;
        visited.add(secondCondBlock);
        List<ArkTSStatement> midStmts = processBlockInstructionsExcluding(
                secondCondBlock, ctx, secondCondBlock.getLastInstruction());
        stmts.addAll(midStmts);

        ArkTSExpression rightCond = getConditionExpression(
                secondCondBlock.getLastInstruction(), ctx);
        if (rightCond == null) {
            rightCond = new ArkTSExpression.VariableExpression(ACC);
        }

        // Mark other blocks as visited
        visited.add(pattern.falseBlock);
        if (pattern.mergeBlock != null) {
            visited.add(pattern.mergeBlock);
        }

        // Build combined condition: left || right
        ArkTSExpression combined = new ArkTSExpression.BinaryExpression(
                leftCond, "||", rightCond);

        stmts.add(new ArkTSStatement.ExpressionStatement(combined));

        return stmts;
    }

    // --- Condition extraction ---

    /**
     * Extracts the condition expression from a conditional branch instruction.
     *
     * @param branchInsn the branch instruction
     * @param ctx the decompilation context
     * @return the condition expression, or null
     */
    private ArkTSExpression getConditionExpression(ArkInstruction branchInsn,
            DecompilationContext ctx) {
        if (branchInsn == null) {
            return null;
        }
        int opcode = branchInsn.getOpcode();

        // For jeqz/jnez, the accumulator is the condition
        // We track the accumulator value from processing prior instructions
        if (opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16) {
            // jeqz: branch if acc == 0 (falsy)
            return ctx.currentAccValue != null
                    ? ctx.currentAccValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }
        if (opcode == ArkOpcodesCompat.JNEZ_IMM8
                || opcode == ArkOpcodesCompat.JNEZ_IMM16) {
            // jnez: branch if acc != 0 (truthy)
            return ctx.currentAccValue != null
                    ? ctx.currentAccValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }
        if (opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JNENULL_IMM8
                || opcode == ArkOpcodesCompat.JNENULL_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("null",
                            ArkTSExpression.LiteralExpression.LiteralKind.NULL));
        }
        if (opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
        }
        if (opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JNEUNDEFINED_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.LiteralExpression("undefined",
                            ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
        }
        // For jeq/jne with register comparison
        if (opcode == ArkOpcodesCompat.JEQ_IMM8
                || opcode == ArkOpcodesCompat.JEQ_IMM16) {
            List<ArkOperand> ops = branchInsn.getOperands();
            int reg = (int) ops.get(0).getValue();
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "==",
                    new ArkTSExpression.VariableExpression("v" + reg));
        }
        if (opcode == ArkOpcodesCompat.JNE_IMM8
                || opcode == ArkOpcodesCompat.JNE_IMM16) {
            List<ArkOperand> ops = branchInsn.getOperands();
            int reg = (int) ops.get(0).getValue();
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!=",
                    new ArkTSExpression.VariableExpression("v" + reg));
        }
        if (opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "===",
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        }
        if (opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JNSTRICTEQZ_IMM16) {
            return new ArkTSExpression.BinaryExpression(
                    ctx.currentAccValue != null
                            ? ctx.currentAccValue
                            : new ArkTSExpression.VariableExpression(ACC),
                    "!==",
                    new ArkTSExpression.LiteralExpression("0",
                            ArkTSExpression.LiteralExpression.LiteralKind.NUMBER));
        }

        return ctx.currentAccValue;
    }

    private boolean isBranchOnFalse(int opcode) {
        return opcode == ArkOpcodesCompat.JEQZ_IMM8
                || opcode == ArkOpcodesCompat.JEQZ_IMM16
                || opcode == ArkOpcodesCompat.JEQNULL_IMM8
                || opcode == ArkOpcodesCompat.JEQNULL_IMM16
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM8
                || opcode == ArkOpcodesCompat.JEQUNDEFINED_IMM16
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM8
                || opcode == ArkOpcodesCompat.JSTRICTEQZ_IMM16;
    }

    // --- Block instruction processing ---

    /**
     * Processes all instructions in a basic block into statements.
     *
     * @param block the basic block
     * @param ctx the decompilation context
     * @return the list of statements
     */
    private List<ArkTSStatement> processBlockInstructions(BasicBlock block,
            DecompilationContext ctx) {
        return processBlockInstructionsExcluding(block, ctx, null);
    }

    /**
     * Processes instructions in a block, optionally excluding the last one.
     *
     * @param block the basic block
     * @param ctx the decompilation context
     * @param excludeInsn the instruction to exclude (typically the branch), or
     *            null
     * @return the list of statements
     */
    private List<ArkTSStatement> processBlockInstructionsExcluding(
            BasicBlock block, DecompilationContext ctx,
            ArkInstruction excludeInsn) {

        List<ArkTSStatement> stmts = new ArrayList<>();
        Set<String> declaredVars = new HashSet<>();
        for (int i = 0; i < ctx.numArgs; i++) {
            declaredVars.add("v" + i);
        }

        ArkTSExpression accValue = null;
        TypeInference typeInf = new TypeInference();

        for (ArkInstruction insn : block.getInstructions()) {
            if (insn == excludeInsn) {
                continue;
            }

            int opcode = insn.getOpcode();

            // Skip NOP
            if (opcode == ArkOpcodesCompat.NOP) {
                continue;
            }

            // Skip unconditional jumps (handled by CFG)
            if (ArkOpcodesCompat.isUnconditionalJump(opcode)) {
                // Check if this is a break or continue
                if (isBreakJump(insn, block, ctx)) {
                    stmts.add(new ArkTSStatement.BreakStatement());
                    continue;
                }
                if (isContinueJump(insn, block, ctx)) {
                    stmts.add(new ArkTSStatement.ContinueStatement());
                    continue;
                }
                continue;
            }

            // Skip conditional branches (handled by CFG reconstruction)
            if (ArkOpcodesCompat.isConditionalBranch(opcode)) {
                // Save accValue for condition extraction later
                ctx.currentAccValue = accValue;
                continue;
            }

            // Skip return (handled separately)
            if (opcode == ArkOpcodesCompat.RETURN) {
                stmts.add(new ArkTSStatement.ReturnStatement(accValue));
                accValue = null;
                continue;
            }
            if (opcode == ArkOpcodesCompat.RETURNUNDEFINED) {
                stmts.add(new ArkTSStatement.ReturnStatement(null));
                accValue = null;
                continue;
            }

            StatementResult result = processInstruction(
                    insn, ctx, accValue, declaredVars, typeInf);
            if (result != null) {
                if (result.statement != null) {
                    stmts.add(result.statement);
                }
                accValue = result.newAccValue;
            } else {
                accValue = null;
            }
        }

        // Update context accumulator for condition extraction
        ctx.currentAccValue = accValue;
        return stmts;
    }

    private boolean isBreakJump(ArkInstruction insn, BasicBlock block,
            DecompilationContext ctx) {
        int[] loopCtx = ctx.getCurrentLoopContext();
        if (loopCtx == null) {
            return false;
        }
        int loopEnd = loopCtx[1];
        int target = ControlFlowGraphAccessor.getJumpTarget(insn);
        // A break jumps to or past the loop end
        return target >= loopEnd;
    }

    private boolean isContinueJump(ArkInstruction insn, BasicBlock block,
            DecompilationContext ctx) {
        int[] loopCtx = ctx.getCurrentLoopContext();
        if (loopCtx == null) {
            return false;
        }
        int loopHeader = loopCtx[0];
        int loopEnd = loopCtx[1];
        int target = ControlFlowGraphAccessor.getJumpTarget(insn);
        // A continue jumps back to the loop header, but is not the
        // natural back-edge from the last block (not a break)
        return target == loopHeader && target < loopEnd;
    }

    // --- CFG helpers ---

    private BasicBlock getSuccessorByType(List<CFGEdge> edges,
            EdgeType type) {
        for (CFGEdge edge : edges) {
            if (edge.getType() == type) {
                return ctx_getBlock(edge.getToOffset());
            }
        }
        return null;
    }

    private BasicBlock ctx_getBlock(int offset) {
        // This is a convenience that needs access to the CFG; used during
        // pattern detection
        return null;
    }

    private BasicBlock findMergeBlock(BasicBlock a, BasicBlock b,
            ControlFlowGraph cfg) {
        // Simple heuristic: find a block that both paths can reach
        // Use a forward walk from both blocks to find the first common block
        Set<Integer> reachableFromA = new LinkedHashSet<>();
        collectReachable(a, cfg, reachableFromA, 10);
        Set<Integer> reachableFromB = new LinkedHashSet<>();
        collectReachable(b, cfg, reachableFromB, 10);

        // Find first common block in reverse post-order
        for (Integer offset : reachableFromA) {
            if (reachableFromB.contains(offset)) {
                BasicBlock merge = cfg.getBlockAt(offset);
                if (merge != a && merge != b) {
                    return merge;
                }
            }
        }
        return null;
    }

    private void collectReachable(BasicBlock start, ControlFlowGraph cfg,
            Set<Integer> reachable, int maxDepth) {
        if (maxDepth <= 0 || start == null) {
            return;
        }
        reachable.add(start.getStartOffset());
        for (CFGEdge edge : start.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && !reachable.contains(succ.getStartOffset())) {
                collectReachable(succ, cfg, reachable, maxDepth - 1);
            }
        }
    }

    private boolean hasBackEdgeTo(BasicBlock from, BasicBlock target,
            ControlFlowGraph cfg) {
        for (CFGEdge edge : from.getSuccessors()) {
            if (edge.getToOffset() == target.getStartOffset()) {
                return true;
            }
        }
        // Check one level deeper
        for (CFGEdge edge : from.getSuccessors()) {
            BasicBlock succ = cfg.getBlockAt(edge.getToOffset());
            if (succ != null && succ != from) {
                for (CFGEdge inner : succ.getSuccessors()) {
                    if (inner.getToOffset() == target.getStartOffset()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- Instruction processing ---

    private static class StatementResult {
        ArkTSStatement statement;
        ArkTSExpression newAccValue;

        StatementResult(ArkTSStatement statement,
                ArkTSExpression newAccValue) {
            this.statement = statement;
            this.newAccValue = newAccValue;
        }
    }

    private StatementResult processInstruction(ArkInstruction insn,
            DecompilationContext ctx, ArkTSExpression accValue,
            Set<String> declaredVars, TypeInference typeInf) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        // --- Loads that set the accumulator ---
        switch (opcode) {
            case ArkOpcodesCompat.LDAI: {
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        String.valueOf(operands.get(0).getValue()),
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.FLDAI: {
                double val = Double.longBitsToDouble(
                        operands.get(0).getValue());
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        String.valueOf(val),
                        ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA_STR: {
                String str = ctx.resolveString(
                        (int) operands.get(0).getValue());
                ArkTSExpression expr = new ArkTSExpression.LiteralExpression(
                        str, ArkTSExpression.LiteralExpression.LiteralKind.STRING);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDA: {
                int reg = (int) operands.get(0).getValue();
                ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                        "v" + reg);
                return new StatementResult(null, expr);
            }
            case ArkOpcodesCompat.LDUNDEFINED:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("undefined",
                                ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED));
            case ArkOpcodesCompat.LDNULL:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("null",
                                ArkTSExpression.LiteralExpression.LiteralKind.NULL));
            case ArkOpcodesCompat.LDTRUE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("true",
                                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDFALSE:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("false",
                                ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN));
            case ArkOpcodesCompat.LDNAN:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("NaN",
                                ArkTSExpression.LiteralExpression.LiteralKind.NAN));
            case ArkOpcodesCompat.LDINFINITY:
                return new StatementResult(null,
                        new ArkTSExpression.LiteralExpression("Infinity",
                                ArkTSExpression.LiteralExpression.LiteralKind.INFINITY));
            case ArkOpcodesCompat.LDTHIS:
                return new StatementResult(null,
                        new ArkTSExpression.ThisExpression());
            case ArkOpcodesCompat.LDGLOBAL:
                return new StatementResult(null,
                        new ArkTSExpression.VariableExpression("globalThis"));
            case ArkOpcodesCompat.CREATEEMPTYOBJECT:
                return new StatementResult(null,
                        new ArkTSExpression.ObjectLiteralExpression(
                                Collections.emptyList()));
            case ArkOpcodesCompat.CREATEEMPTYARRAY:
                return new StatementResult(null,
                        new ArkTSExpression.ArrayLiteralExpression(
                                Collections.emptyList()));
            default:
                break;
        }

        // --- Async function enter ---
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONENTER) {
            ctx.isAsync = true;
            return null;
        }

        // --- Generator operations ---
        if (opcode == ArkOpcodesCompat.CREATEGENERATOROBJ) {
            int reg = (int) operands.get(0).getValue();
            String varName = "v" + reg;
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression("generator");
            if (accValue != null) {
                expr = accValue;
            }
            if (!declaredVars.contains(varName)) {
                declaredVars.add(varName);
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, null, expr),
                        expr);
            }
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.VariableExpression(
                                            varName),
                                    expr)),
                    expr);
        }

        if (opcode == ArkOpcodesCompat.SUSPENDGENERATOR) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.YieldExpression(value, false)),
                    null);
        }

        if (opcode == ArkOpcodesCompat.RESUMEGENERATOR) {
            // RESUMEGENERATOR has NONE format (no operands)
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression("generator");
            return new StatementResult(null, expr);
        }

        if (opcode == ArkOpcodesCompat.GETRESUMEMODE) {
            return new StatementResult(null,
                    new ArkTSExpression.VariableExpression("resumeMode"));
        }

        // --- Async operations ---
        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONAWAITUNCAUGHT) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression promise =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(null,
                    new ArkTSExpression.AwaitExpression(promise));
        }

        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONRESOLVE) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ReturnStatement(value), null);
        }

        if (opcode == ArkOpcodesCompat.ASYNCFUNCTIONREJECT) {
            int reg = (int) operands.get(0).getValue();
            ArkTSExpression value =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(value), null);
        }

        // --- IsTrue / IsFalse ---
        if (opcode == ArkOpcodesCompat.ISTRUE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression boolExpr =
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Boolean"),
                            List.of(operand));
            return new StatementResult(null, boolExpr);
        }
        if (opcode == ArkOpcodesCompat.ISFALSE) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.UnaryExpression("!", operand, true));
        }

        // --- Throw ---
        if (opcode == ArkOpcodesCompat.THROW) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ThrowStatement(val), null);
        }

        // --- Store from accumulator ---
        if (opcode == ArkOpcodesCompat.STA) {
            int reg = (int) operands.get(0).getValue();
            String varName = "v" + reg;
            if (accValue != null) {
                // Infer type for type annotation
                String inferredType = typeInf.inferTypeForInstruction(insn);
                // Use the accValue-producing instruction's type instead
                // STA itself doesn't produce a type; the accValue does
                String typeAnnotation = TypeInference.formatTypeAnnotation(
                        varName, getAccType(accValue, typeInf));
                typeInf.setRegisterType(varName,
                        getAccType(accValue, typeInf));

                if (!declaredVars.contains(varName)
                        && !(reg < ctx.numArgs)) {
                    declaredVars.add(varName);
                    ArkTSStatement stmt =
                            new ArkTSStatement.VariableDeclaration(
                                    "let", varName, typeAnnotation, accValue);
                    return new StatementResult(stmt, accValue);
                }
                ArkTSStatement stmt = new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.VariableExpression(
                                        varName),
                                accValue));
                return new StatementResult(stmt, accValue);
            }
            return null;
        }

        // --- MOV ---
        if (opcode == ArkOpcodesCompat.MOV) {
            int dstReg = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(1).getValue();
            String dstName = "v" + dstReg;
            String srcName = "v" + srcReg;
            ArkTSExpression srcExpr =
                    new ArkTSExpression.VariableExpression(srcName);
            String srcType = typeInf.getRegisterType(srcName);
            String typeAnnotation = TypeInference.formatTypeAnnotation(
                    dstName, srcType);
            typeInf.setRegisterType(dstName, srcType);

            if (!declaredVars.contains(dstName)
                    && !(dstReg < ctx.numArgs)) {
                declaredVars.add(dstName);
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", dstName, typeAnnotation, srcExpr),
                        null);
            }
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.VariableExpression(
                                            dstName),
                                    srcExpr)),
                    null);
        }

        // --- Binary operations: acc = acc OP v[operand] ---
        if (isBinaryOp(opcode)) {
            String op = getBinaryOperator(opcode);
            int reg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression left = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression right =
                    new ArkTSExpression.VariableExpression("v" + reg);
            ArkTSExpression result =
                    new ArkTSExpression.BinaryExpression(left, op, right);
            return new StatementResult(null, result);
        }

        // --- Unary operations: acc = OP acc ---
        if (isUnaryOp(opcode)) {
            String op = getUnaryOperator(opcode);
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            boolean prefix = true;
            ArkTSExpression result =
                    new ArkTSExpression.UnaryExpression(op, operand, prefix);
            return new StatementResult(null, result);
        }

        // --- Inc/Dec: acc = acc +/- 1 ---
        if (opcode == ArkOpcodesCompat.INC || opcode == ArkOpcodesCompat.DEC) {
            ArkTSExpression operand = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression one = new ArkTSExpression.LiteralExpression("1",
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
            String op = opcode == ArkOpcodesCompat.INC ? "+" : "-";
            ArkTSExpression result =
                    new ArkTSExpression.BinaryExpression(operand, op, one);
            return new StatementResult(null, result);
        }

        // --- Function calls ---
        if (isCallOpcode(opcode)) {
            ArkTSExpression callExpr = translateCall(insn, accValue, ctx);
            return new StatementResult(null, callExpr);
        }

        // --- Property access (load) ---
        if (isPropertyLoadOpcode(opcode)) {
            ArkTSExpression expr = translatePropertyLoad(insn, accValue, ctx);
            return new StatementResult(null, expr);
        }

        // --- Property store ---
        if (isPropertyStoreOpcode(opcode)) {
            ArkTSExpression expr = translatePropertyStore(insn, accValue, ctx);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(expr),
                    accValue);
        }

        // --- Lexical variable access ---
        if (opcode == ArkOpcodesCompat.LDLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "lex_" + level + "_" + slot);
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.STLEXVAR) {
            int level = (int) operands.get(0).getValue();
            int slot = (int) operands.get(1).getValue();
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "lex_" + level + "_" + slot),
                                        accValue)),
                        accValue);
            }
            return null;
        }

        // --- New object range ---
        if (opcode == ArkOpcodesCompat.NEWOBJRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            int firstReg = (int) operands.get(operands.size() - 1).getValue();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            ArkTSExpression callee = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(null,
                    new ArkTSExpression.NewExpression(callee, args));
        }

        // --- Define function ---
        if (opcode == ArkOpcodesCompat.DEFINEFUNC) {
            int methodIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "func_" + methodIdx);
            return new StatementResult(null, expr);
        }

        // --- Define method ---
        if (opcode == ArkOpcodesCompat.DEFINEMETHOD) {
            int methodIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "method_" + methodIdx);
            return new StatementResult(null, expr);
        }

        // --- Define class with buffer ---
        if (opcode == ArkOpcodesCompat.DEFINECLASSWITHBUFFER) {
            return processDefineClassWithBuffer(insn, ctx);
        }

        // --- Define field by name ---
        if (opcode == ArkOpcodesCompat.DEFINEFIELDBYNAME) {
            int stringIdx = (int) operands.get(1).getValue();
            String fieldName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression("v" + objReg),
                            new ArkTSExpression.VariableExpression(fieldName),
                            false);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(target, value)),
                    value);
        }

        // --- Define property by name ---
        if (opcode == ArkOpcodesCompat.DEFINEPROPERTYBYNAME) {
            int stringIdx = (int) operands.get(1).getValue();
            String propName = ctx.resolveString(stringIdx);
            int objReg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression target =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression("v" + objReg),
                            new ArkTSExpression.VariableExpression(propName),
                            false);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(target, value)),
                    value);
        }

        // --- Super call this range ---
        if (opcode == ArkOpcodesCompat.SUPERCALLTHISRANGE
                || opcode == ArkOpcodesCompat.SUPERCALLARROWRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(operands.size() - 1).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + (firstReg + a)));
            }
            ArkTSStatement superCall =
                    new ArkTSStatement.SuperCallStatement(args);
            return new StatementResult(superCall,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }

        // --- Super call spread ---
        if (opcode == ArkOpcodesCompat.SUPERCALLSPREAD) {
            int spreadReg = (int) operands.get(0).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSExpression.SpreadExpression(
                            new ArkTSExpression.VariableExpression(
                                    "v" + spreadReg));
            List<ArkTSExpression> args = new ArrayList<>();
            args.add(spreadArg);
            ArkTSStatement superCall =
                    new ArkTSStatement.SuperCallStatement(args);
            return new StatementResult(superCall,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("super"),
                            args));
        }

        // --- Module variable access ---
        if (opcode == ArkOpcodesCompat.LDEXTERNALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "ext_mod_" + varIdx);
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.LDLOCALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "local_mod_" + varIdx);
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.STMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.AssignExpression(
                                        new ArkTSExpression.VariableExpression(
                                                "mod_" + varIdx),
                                        accValue)),
                        accValue);
            }
            return null;
        }
        if (opcode == ArkOpcodesCompat.GETMODULENAMESPACE) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.VariableExpression(
                    "module_ns_" + varIdx);
            return new StatementResult(null, expr);
        }

        // --- Dynamic import ---
        if (opcode == ArkOpcodesCompat.DYNAMICIMPORT) {
            ArkTSExpression specifier = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression callExpr =
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("import"),
                            List.of(specifier));
            return new StatementResult(null, callExpr);
        }

        // --- Global record stores (const/var declarations) ---
        if (opcode == ArkOpcodesCompat.STCONSTTOGLOBALRECORD) {
            int stringIdx = (int) operands.get(0).getValue();
            String varName = ctx.resolveString(stringIdx);
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "const", varName, null, accValue),
                        accValue);
            }
            return null;
        }
        if (opcode == ArkOpcodesCompat.STTOGLOBALRECORD) {
            int stringIdx = (int) operands.get(0).getValue();
            String varName = ctx.resolveString(stringIdx);
            if (accValue != null) {
                return new StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, null, accValue),
                        accValue);
            }
            return null;
        }

        // --- Create array/object with buffer ---
        if (opcode == ArkOpcodesCompat.CREATEARRAYWITHBUFFER) {
            int numElements = (int) operands.get(0).getValue();
            ArkTSExpression expr = new ArkTSExpression.ArrayLiteralExpression(
                    createPlaceholderElements(numElements));
            return new StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHBUFFER) {
            ArkTSExpression expr =
                    new ArkTSExpression.ObjectLiteralExpression(
                            Collections.emptyList());
            return new StatementResult(null, expr);
        }

        // --- STARRAYSPREAD (spread into array) ---
        if (opcode == ArkOpcodesCompat.STARRAYSPREAD) {
            // starrayspread dst, src
            int dstReg = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(1).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSExpression.VariableExpression("v" + srcReg);
            ArkTSExpression spread =
                    new ArkTSExpression.SpreadExpression(spreadArg);
            return new StatementResult(
                    new ArkTSStatement.ExpressionStatement(spread),
                    spread);
        }

        // --- Fallback: emit a comment ---
        return new StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* " + insn.getMnemonic() + " */")),
                null);
    }

    /**
     * Attempts to determine the type of an accumulator expression.
     *
     * @param expr the expression
     * @param typeInf the type inference engine
     * @return the inferred type name, or null
     */
    private String getAccType(ArkTSExpression expr, TypeInference typeInf) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) expr;
            switch (lit.getKind()) {
                case NUMBER:
                    return "number";
                case STRING:
                    return "string";
                case BOOLEAN:
                    return "boolean";
                case NULL:
                    return "null";
                case UNDEFINED:
                    return "undefined";
                default:
                    return null;
            }
        }
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return typeInf.getRegisterType(
                    ((ArkTSExpression.VariableExpression) expr).getName());
        }
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            String op = ((ArkTSExpression.BinaryExpression) expr).getOperator();
            if (TypeInference.isComparisonOpFromSymbol(op)) {
                return "boolean";
            }
            if (TypeInference.isBinaryArithmeticOpFromSymbol(op)) {
                return "number";
            }
        }
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            String op = ((ArkTSExpression.UnaryExpression) expr).getOperator();
            if ("!".equals(op)) {
                return "boolean";
            }
            if ("-".equals(op)) {
                return "number";
            }
        }
        if (expr instanceof ArkTSExpression.ArrayLiteralExpression) {
            return "Array<unknown>";
        }
        if (expr instanceof ArkTSExpression.ObjectLiteralExpression) {
            return "Object";
        }
        if (expr instanceof ArkTSExpression.CallExpression) {
            return null;
        }
        if (expr instanceof ArkTSExpression.NewExpression) {
            return null;
        }
        return null;
    }

    // --- Call translation ---

    private ArkTSExpression translateCall(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression callee;
        List<ArkTSExpression> args = new ArrayList<>();

        // Try to resolve the callee method name from the ABC file
        // For callthis* opcodes, the callee is already in the accumulator
        // (loaded via ldobjbyname or similar)
        // For callarg* opcodes, the callee is in the accumulator
        // The first operand is typically a method index or unused

        switch (opcode) {
            case ArkOpcodesCompat.CALLARG0:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
            case ArkOpcodesCompat.CALLARG1:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                break;
            case ArkOpcodesCompat.CALLARGS2:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                break;
            case ArkOpcodesCompat.CALLARGS3:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(3).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS0:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
            case ArkOpcodesCompat.CALLTHIS1:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS2:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHIS3:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(1).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(2).getValue()));
                args.add(new ArkTSExpression.VariableExpression(
                        "v" + operands.get(3).getValue()));
                break;
            case ArkOpcodesCompat.CALLTHISRANGE:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                "v" + (firstReg + a)));
                    }
                }
                break;
            case ArkOpcodesCompat.CALLRANGE:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                if (operands.size() >= 3) {
                    int numRangeArgs = (int) operands.get(0).getValue();
                    int firstReg = (int) operands.get(
                            operands.size() - 1).getValue();
                    for (int a = 0; a < numRangeArgs; a++) {
                        args.add(new ArkTSExpression.VariableExpression(
                                "v" + (firstReg + a)));
                    }
                }
                break;
            default:
                callee = accValue != null
                        ? accValue
                        : new ArkTSExpression.VariableExpression(ACC);
                break;
        }

        return new ArkTSExpression.CallExpression(callee, args);
    }

    // --- Property access translation ---

    private ArkTSExpression translatePropertyLoad(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else {
            obj = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
        }

        if (opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE) {
            int reg = (int) operands.get(operands.size() - 1).getValue();
            ArkTSExpression prop =
                    new ArkTSExpression.VariableExpression("v" + reg);
            return new ArkTSExpression.MemberExpression(obj, prop, true);
        }

        // Name-based access
        String propName = ctx.resolveString((int) operands.get(1).getValue());
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.MemberExpression(obj, prop, false);
    }

    private ArkTSExpression translatePropertyStore(ArkInstruction insn,
            ArkTSExpression accValue, DecompilationContext ctx) {
        int opcode = insn.getOpcode();
        List<ArkOperand> operands = insn.getOperands();

        ArkTSExpression obj;
        if (opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE) {
            obj = new ArkTSExpression.ThisExpression();
        } else {
            int objReg = (int) operands.get(operands.size() - 1).getValue();
            obj = new ArkTSExpression.VariableExpression("v" + objReg);
        }

        ArkTSExpression prop;
        if (opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYVALUE) {
            int keyReg = (int) operands.get(operands.size() - 2).getValue();
            prop = new ArkTSExpression.VariableExpression("v" + keyReg);
            return new ArkTSExpression.AssignExpression(
                    new ArkTSExpression.MemberExpression(obj, prop, true),
                    accValue != null ? accValue
                            : new ArkTSExpression.VariableExpression(ACC));
        }

        // Name-based
        String propName = ctx.resolveString((int) operands.get(1).getValue());
        prop = new ArkTSExpression.VariableExpression(propName);
        return new ArkTSExpression.AssignExpression(
                new ArkTSExpression.MemberExpression(obj, prop, false),
                accValue != null ? accValue
                        : new ArkTSExpression.VariableExpression(ACC));
    }

    // --- Define class with buffer processing ---

    /**
     * Processes a defineclasswithbuffer instruction.
     *
     * <p>This instruction creates a class at runtime. The operands encode:
     * <ul>
     *   <li>Operand 0: method index for the constructor</li>
     *   <li>Operand 1: literal array index for the class buffer
     *       (contains field names, etc.)</li>
     *   <li>Operand 2: method index or additional metadata</li>
     *   <li>Last operand: destination register</li>
     * </ul>
     *
     * @param insn the instruction
     * @param ctx the decompilation context
     * @return the statement result with a class-creating expression
     */
    private StatementResult processDefineClassWithBuffer(ArkInstruction insn,
            DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int methodIdx = (int) operands.get(0).getValue();
        int literalIdx = (int) operands.get(1).getValue();
        int lastReg = (int) operands.get(operands.size() - 1).getValue();

        String className = "class_" + methodIdx;
        if (ctx.abcFile != null) {
            // Try to resolve class name from the ABC file
            className = resolveClassNameFromMethod(methodIdx, ctx.abcFile);
        }

        // Build a class expression
        ArkTSExpression classExpr =
                new ArkTSExpression.VariableExpression(className);

        // Store the class in the destination register
        String varName = "v" + lastReg;
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration(
                "let", varName, className, classExpr);

        return new StatementResult(stmt, classExpr);
    }

    /**
     * Resolves a class name from a method index by searching ABC classes.
     *
     * @param methodIdx the method index
     * @param abcFile the ABC file
     * @return the resolved class name
     */
    private String resolveClassNameFromMethod(int methodIdx, AbcFile abcFile) {
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod m : cls.getMethods()) {
                // Match by method identity within the class
                if (isConstructorMethod(m, sanitizeClassName(cls.getName()))) {
                    return sanitizeClassName(cls.getName());
                }
            }
        }
        return "class_" + methodIdx;
    }

    /**
     * Creates placeholder elements for an array literal.
     *
     * @param count the number of elements
     * @return the list of placeholder expressions
     */
    private static List<ArkTSExpression> createPlaceholderElements(int count) {
        List<ArkTSExpression> elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elements.add(new ArkTSExpression.LiteralExpression(
                    "/* element_" + i + " */",
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING));
        }
        return elements;
    }

    // --- Helpers ---

    private boolean isBinaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.ADD2
                || opcode == ArkOpcodesCompat.SUB2
                || opcode == ArkOpcodesCompat.MUL2
                || opcode == ArkOpcodesCompat.DIV2
                || opcode == ArkOpcodesCompat.MOD2
                || opcode == ArkOpcodesCompat.EQ
                || opcode == ArkOpcodesCompat.NOTEQ
                || opcode == ArkOpcodesCompat.LESS
                || opcode == ArkOpcodesCompat.LESSEQ
                || opcode == ArkOpcodesCompat.GREATER
                || opcode == ArkOpcodesCompat.GREATEREQ
                || opcode == ArkOpcodesCompat.SHL2
                || opcode == ArkOpcodesCompat.SHR2
                || opcode == ArkOpcodesCompat.ASHR2
                || opcode == ArkOpcodesCompat.AND2
                || opcode == ArkOpcodesCompat.OR2
                || opcode == ArkOpcodesCompat.XOR2
                || opcode == ArkOpcodesCompat.EXP
                || opcode == ArkOpcodesCompat.STRICTEQ
                || opcode == ArkOpcodesCompat.STRICTNOTEQ
                || opcode == ArkOpcodesCompat.INSTANCEOF
                || opcode == ArkOpcodesCompat.ISIN;
    }

    private String getBinaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.ADD2: return "+";
            case ArkOpcodesCompat.SUB2: return "-";
            case ArkOpcodesCompat.MUL2: return "*";
            case ArkOpcodesCompat.DIV2: return "/";
            case ArkOpcodesCompat.MOD2: return "%";
            case ArkOpcodesCompat.EQ: return "==";
            case ArkOpcodesCompat.NOTEQ: return "!=";
            case ArkOpcodesCompat.LESS: return "<";
            case ArkOpcodesCompat.LESSEQ: return "<=";
            case ArkOpcodesCompat.GREATER: return ">";
            case ArkOpcodesCompat.GREATEREQ: return ">=";
            case ArkOpcodesCompat.SHL2: return "<<";
            case ArkOpcodesCompat.SHR2: return ">>>";
            case ArkOpcodesCompat.ASHR2: return ">>";
            case ArkOpcodesCompat.AND2: return "&";
            case ArkOpcodesCompat.OR2: return "|";
            case ArkOpcodesCompat.XOR2: return "^";
            case ArkOpcodesCompat.EXP: return "**";
            case ArkOpcodesCompat.STRICTEQ: return "===";
            case ArkOpcodesCompat.STRICTNOTEQ: return "!==";
            case ArkOpcodesCompat.INSTANCEOF: return "instanceof";
            case ArkOpcodesCompat.ISIN: return "in";
            default: return "/* op */";
        }
    }

    private boolean isUnaryOp(int opcode) {
        return opcode == ArkOpcodesCompat.NEG
                || opcode == ArkOpcodesCompat.NOT
                || opcode == ArkOpcodesCompat.TYPEOF;
    }

    private String getUnaryOperator(int opcode) {
        switch (opcode) {
            case ArkOpcodesCompat.NEG: return "-";
            case ArkOpcodesCompat.NOT: return "!";
            case ArkOpcodesCompat.TYPEOF: return "typeof";
            default: return "/* unary */";
        }
    }

    private boolean isCallOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.CALLARG0
                || opcode == ArkOpcodesCompat.CALLARG1
                || opcode == ArkOpcodesCompat.CALLARGS2
                || opcode == ArkOpcodesCompat.CALLARGS3
                || opcode == ArkOpcodesCompat.CALLTHIS0
                || opcode == ArkOpcodesCompat.CALLTHIS1
                || opcode == ArkOpcodesCompat.CALLTHIS2
                || opcode == ArkOpcodesCompat.CALLTHIS3
                || opcode == ArkOpcodesCompat.CALLTHISRANGE
                || opcode == ArkOpcodesCompat.CALLRANGE;
    }

    private boolean isPropertyLoadOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.LDOBJBYNAME
                || opcode == ArkOpcodesCompat.LDOBJBYVALUE
                || opcode == ArkOpcodesCompat.LDOBJBYINDEX
                || opcode == ArkOpcodesCompat.LDTHISBYNAME
                || opcode == ArkOpcodesCompat.LDTHISBYVALUE
                || opcode == ArkOpcodesCompat.LDSUPERBYNAME;
    }

    private boolean isPropertyStoreOpcode(int opcode) {
        return opcode == ArkOpcodesCompat.STOBJBYNAME
                || opcode == ArkOpcodesCompat.STOBJBYVALUE
                || opcode == ArkOpcodesCompat.STOBJBYINDEX
                || opcode == ArkOpcodesCompat.STTHISBYNAME
                || opcode == ArkOpcodesCompat.STTHISBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYNAME
                || opcode == ArkOpcodesCompat.STOWNBYVALUE
                || opcode == ArkOpcodesCompat.STOWNBYINDEX
                || opcode == ArkOpcodesCompat.STSUPERBYNAME;
    }

    private AbcProto resolveProto(AbcMethod method, AbcFile abcFile) {
        if (abcFile == null || method.getProtoIdx() < 0) {
            return null;
        }
        List<AbcProto> protos = abcFile.getProtos();
        int idx = method.getProtoIdx();
        if (idx < protos.size()) {
            return protos.get(idx);
        }
        return null;
    }

    /**
     * Extracts debug parameter names for a method from its debug info.
     *
     * @param method the method
     * @param abcFile the parent ABC file
     * @return the list of debug parameter names, or null if unavailable
     */
    private List<String> getDebugParamNames(AbcMethod method, AbcFile abcFile) {
        if (abcFile == null) {
            return null;
        }
        AbcDebugInfo debugInfo = abcFile.getDebugInfoForMethod(method);
        if (debugInfo == null || debugInfo.getParameterNames().isEmpty()) {
            return null;
        }
        return debugInfo.getParameterNames();
    }

    private String buildEmptyMethod(AbcMethod method, AbcProto proto) {
        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto, 0);
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSStatement.FunctionDeclaration func =
                new ArkTSStatement.FunctionDeclaration(
                        method.getName(), params, returnType, body);
        return func.toArkTS(0);
    }

    private String buildMethodSource(AbcMethod method, AbcProto proto,
            AbcCode code, List<ArkTSStatement> bodyStmts,
            AbcFile abcFile) {
        // Filter out trailing return undefined for cleaner output
        List<ArkTSStatement> filteredStmts =
                filterTrivialReturnUndefined(bodyStmts);

        List<ArkTSStatement.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto, code.getNumArgs(),
                        getDebugParamNames(method, abcFile));
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(filteredStmts);
        ArkTSStatement.FunctionDeclaration func =
                new ArkTSStatement.FunctionDeclaration(
                        method.getName(), params, returnType, body);
        return func.toArkTS(0);
    }

    /**
     * Removes a trailing "return;" (return undefined) that is the last
     * statement, as it adds no useful information to the decompiled output.
     *
     * @param stmts the list of statements
     * @return the filtered list (may be the same list if no change needed)
     */
    private static List<ArkTSStatement> filterTrivialReturnUndefined(
            List<ArkTSStatement> stmts) {
        if (stmts.isEmpty()) {
            return stmts;
        }
        ArkTSStatement last = stmts.get(stmts.size() - 1);
        if (last instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) last).getValue();
            if (val == null) {
                // return; (return undefined) at end is implicit
                List<ArkTSStatement> filtered =
                        new ArrayList<>(stmts.size() - 1);
                for (int i = 0; i < stmts.size() - 1; i++) {
                    filtered.add(stmts.get(i));
                }
                return filtered;
            }
        }
        return stmts;
    }

    // --- Context ---

    /**
     * Holds shared state during decompilation of a single method.
     */
    /**
     * Holds shared state during decompilation of a single method.
     * Package-private for testing.
     */
    static class DecompilationContext {
        final AbcMethod method;
        final AbcCode code;
        final AbcProto proto;
        final AbcFile abcFile;
        final ControlFlowGraph cfg;
        final List<ArkInstruction> instructions;
        final int numArgs;
        boolean isAsync;
        ArkTSExpression currentAccValue;

        /**
         * Stack of loop contexts for break/continue detection.
         * Each entry is [loopHeaderOffset, loopEndOffset].
         */
        final List<int[]> loopContextStack;

        DecompilationContext(AbcMethod method, AbcCode code,
                AbcProto proto, AbcFile abcFile,
                ControlFlowGraph cfg,
                List<ArkInstruction> instructions) {
            this.method = method;
            this.code = code;
            this.proto = proto;
            this.abcFile = abcFile;
            this.cfg = cfg;
            this.instructions = instructions;
            this.numArgs = code != null ? (int) code.getNumArgs() : 0;
            this.isAsync = false;
            this.currentAccValue = null;
            this.loopContextStack = new ArrayList<>();
        }

        /**
         * Pushes a loop context onto the stack.
         *
         * @param headerOffset the loop header (condition) offset
         * @param endOffset the offset just past the loop end
         */
        void pushLoopContext(int headerOffset, int endOffset) {
            loopContextStack.add(new int[] {headerOffset, endOffset});
        }

        /**
         * Pops the most recent loop context from the stack.
         */
        void popLoopContext() {
            if (!loopContextStack.isEmpty()) {
                loopContextStack.remove(loopContextStack.size() - 1);
            }
        }

        /**
         * Returns the current innermost loop context, or null.
         *
         * @return [headerOffset, endOffset] or null
         */
        int[] getCurrentLoopContext() {
            if (loopContextStack.isEmpty()) {
                return null;
            }
            return loopContextStack.get(loopContextStack.size() - 1);
        }

        /**
         * Resolves a string table index to a string value.
         * Returns a placeholder if the string cannot be resolved.
         *
         * @param stringIdx the string table index
         * @return the resolved string or a placeholder
         */
        String resolveString(int stringIdx) {
            if (abcFile != null) {
                try {
                    List<Long> lnpIndex = abcFile.getLnpIndex();
                    if (stringIdx >= 0 && stringIdx < lnpIndex.size()) {
                        long strOff = lnpIndex.get(stringIdx);
                        byte[] data = abcFile.getRawData();
                        if (data != null && strOff >= 0
                                && strOff < data.length) {
                            return readMutf8At(data, (int) strOff);
                        }
                    }
                } catch (Exception e) {
                    // Fall through to placeholder
                }
            }
            return "str_" + stringIdx;
        }

        /**
         * Reads a Modified UTF-8 encoded string from raw bytecode data.
         * Handles the null terminator and basic MUTF-8 multi-byte sequences.
         *
         * @param data the raw bytecode data
         * @param offset the starting offset
         * @return the decoded string
         */
        public static String readMutf8At(byte[] data, int offset) {
            int pos = offset;
            StringBuilder sb = new StringBuilder();
            while (pos < data.length && data[pos] != 0) {
                int b = data[pos] & 0xFF;
                if (b < 0x80) {
                    // Single byte (ASCII)
                    sb.append((char) b);
                    pos++;
                } else if ((b & 0xE0) == 0xC0) {
                    // Two-byte sequence
                    if (pos + 1 < data.length) {
                        int b2 = data[pos + 1] & 0xFF;
                        char ch = (char) (((b & 0x1F) << 6)
                                | (b2 & 0x3F));
                        sb.append(ch);
                        pos += 2;
                    } else {
                        break;
                    }
                } else if ((b & 0xF0) == 0xE0) {
                    // Three-byte sequence
                    if (pos + 2 < data.length) {
                        int b2 = data[pos + 1] & 0xFF;
                        int b3 = data[pos + 2] & 0xFF;
                        char ch = (char) (((b & 0x0F) << 12)
                                | ((b2 & 0x3F) << 6) | (b3 & 0x3F));
                        sb.append(ch);
                        pos += 3;
                    } else {
                        break;
                    }
                } else {
                    // Unknown byte, skip
                    pos++;
                }
            }
            return sb.toString();
        }
    }
}
