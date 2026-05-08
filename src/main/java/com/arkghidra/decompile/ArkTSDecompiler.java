package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcDebugInfo;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcModuleRecord;
import com.arkghidra.format.AbcProto;

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
 *
 * <p>Internal logic is delegated to:
 * <ul>
 *   <li>{@link InstructionHandler} -- per-instruction processing</li>
 *   <li>{@link ControlFlowReconstructor} -- CFG pattern detection</li>
 *   <li>{@link DeclarationBuilder} -- class/method/field declarations</li>
 * </ul>
 */
public class ArkTSDecompiler {

    private static final String ACC = "acc";

    private final InstructionHandler instrHandler;
    private final ControlFlowReconstructor cfReconstructor;
    private final DeclarationBuilder declBuilder;

    public ArkTSDecompiler() {
        this.instrHandler = new InstructionHandler(this);
        this.cfReconstructor = new ControlFlowReconstructor(
                this, instrHandler);
        this.declBuilder = new DeclarationBuilder(this);
    }

    // --- Public entry points ---

    /**
     * Decompiles a method to ArkTS source code.
     *
     * @param method the method to decompile
     * @param code the method's code section
     * @param abcFile the parent ABC file (for resolving references)
     * @return the decompiled ArkTS source code
     */
    public String decompileMethod(AbcMethod method, AbcCode code,
            AbcFile abcFile) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return buildEmptyMethod(method, null);
        }

        ArkDisassembler disasm = new ArkDisassembler();
        List<ArkInstruction> instructions;
        try {
            instructions = disasm.disassemble(
                    code.getInstructions(), 0, (int) code.getCodeSize());
        } catch (Exception e) {
            return buildFallbackMethod(method, code, abcFile,
                    "disassembly failed: " + e.getMessage());
        }

        if (instructions.isEmpty()) {
            return buildEmptyMethod(method, null);
        }

        if (instructions.size() == 1) {
            ArkInstruction only = instructions.get(0);
            if (only.getOpcode() == ArkOpcodesCompat.RETURNUNDEFINED) {
                return buildEmptyMethod(method,
                        resolveProto(method, abcFile));
            }
            if (only.getOpcode() == ArkOpcodesCompat.RETURN) {
                AbcProto proto = resolveProto(method, abcFile);
                ArkTSExpression val =
                        new ArkTSExpression.VariableExpression(ACC);
                List<ArkTSStatement> stmts = List.of(
                        new ArkTSStatement.ReturnStatement(val));
                return buildMethodSource(method, proto, code, stmts,
                        abcFile);
            }
        }

        ControlFlowGraph cfg;
        try {
            cfg = ControlFlowGraph.build(instructions,
                    code.getTryBlocks());
        } catch (Exception e) {
            return buildFallbackMethod(method, code, abcFile,
                    "CFG construction failed: " + e.getMessage());
        }

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        List<ArkTSStatement> bodyStmts;
        try {
            bodyStmts = generateStatements(ctx);
        } catch (Exception e) {
            List<ArkTSStatement> fallbackStmts = new ArrayList<>();
            fallbackStmts.add(new ArkTSStatement.ExpressionStatement(
                    new ArkTSExpression.VariableExpression(
                            "/* decompilation failed: "
                                    + e.getMessage() + " */")));
            for (ArkInstruction insn : instructions) {
                fallbackStmts.add(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "/* " + formatInstructionText(insn)
                                                + " */")));
            }
            return buildMethodSource(method, proto, code,
                    fallbackStmts, abcFile);
        }

        return buildMethodSource(method, proto, code, bodyStmts,
                abcFile);
    }

    /**
     * Decompiles just the instructions (without ABC metadata) for testing.
     *
     * @param instructions the decoded instructions
     * @return the decompiled ArkTS source
     */
    public String decompileInstructions(
            List<ArkInstruction> instructions) {
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

    /**
     * Decompiles an entire ABC file into a complete ArkTS source file.
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

        for (int i = 0; i < abcFile.getClasses().size(); i++) {
            AbcModuleRecord record = abcFile.getModuleRecord(i);
            if (record != null) {
                List<String> moduleRequests =
                        abcFile.resolveModuleRequests(record);

                for (AbcModuleRecord.RegularImport ri :
                        record.getRegularImports()) {
                    String localName = abcFile.getSourceFileName(
                            ri.getLocalNameOffset());
                    String importName = abcFile.getSourceFileName(
                            ri.getImportNameOffset());
                    String modulePath = getModulePath(
                            moduleRequests, ri.getModuleRequestIdx());
                    List<String> namedImports = new ArrayList<>();
                    if (localName != null && importName != null
                            && !localName.equals(importName)) {
                        namedImports.add(
                                importName + " as " + localName);
                    } else if (importName != null) {
                        namedImports.add(importName);
                    }
                    if (modulePath != null) {
                        imports.add(
                                new ArkTSDeclarations.ImportStatement(
                                        namedImports, modulePath,
                                        false, null, null));
                        seenImports.add(modulePath);
                    }
                }

                for (AbcModuleRecord.NamespaceImport ni :
                        record.getNamespaceImports()) {
                    String localName = abcFile.getSourceFileName(
                            ni.getLocalNameOffset());
                    String modulePath = getModulePath(
                            moduleRequests, ni.getModuleRequestIdx());
                    if (localName != null && modulePath != null) {
                        imports.add(
                                new ArkTSDeclarations.ImportStatement(
                                        Collections.emptyList(),
                                        modulePath, false, null,
                                        localName));
                        seenImports.add(modulePath);
                    }
                }

                for (AbcModuleRecord.LocalExport le :
                        record.getLocalExports()) {
                    String localName = abcFile.getSourceFileName(
                            le.getLocalNameOffset());
                    String exportName = abcFile.getSourceFileName(
                            le.getExportNameOffset());
                    List<String> namedExports = new ArrayList<>();
                    if (localName != null && exportName != null
                            && !localName.equals(exportName)) {
                        namedExports.add(
                                localName + " as " + exportName);
                    } else if (localName != null) {
                        namedExports.add(localName);
                    }
                    if (!namedExports.isEmpty()) {
                        exports.add(
                                new ArkTSDeclarations.ExportStatement(
                                        namedExports, null, false));
                    }
                }

                for (AbcModuleRecord.IndirectExport ie :
                        record.getIndirectExports()) {
                    String exportName = abcFile.getSourceFileName(
                            ie.getExportNameOffset());
                    String importName = abcFile.getSourceFileName(
                            ie.getImportNameOffset());
                    String modulePath = getModulePath(moduleRequests,
                            ie.getModuleRequestIdx());
                    List<String> namedExports = new ArrayList<>();
                    if (importName != null && exportName != null
                            && !importName.equals(exportName)) {
                        namedExports.add(
                                importName + " as " + exportName);
                    } else if (importName != null) {
                        namedExports.add(importName);
                    }
                    if (modulePath != null
                            && !namedExports.isEmpty()) {
                        exports.add(
                                new ArkTSDeclarations.ExportStatement(
                                        namedExports, null, false));
                    }
                }
            }
        }

        for (AbcClass abcClass : abcFile.getClasses()) {
            ArkTSStatement classDecl =
                    declBuilder.buildClassDeclaration(
                            abcClass, abcFile, seenImports);
            if (classDecl != null) {
                if (declBuilder.isExported(abcClass)) {
                    exports.add(new ArkTSDeclarations.ExportStatement(
                            Collections.emptyList(), classDecl, false));
                } else {
                    declarations.add(classDecl);
                }
            }
        }

        List<ArkTSStatement> enumDecls =
                declBuilder.detectEnumsFromLiteralArrays(abcFile);
        declarations.addAll(enumDecls);

        for (String moduleRef : seenImports) {
            boolean alreadyImported = imports.stream().anyMatch(s ->
                    s instanceof ArkTSDeclarations.ImportStatement
                    && ((ArkTSDeclarations.ImportStatement) s)
                            .getModulePath().equals(moduleRef));
            if (!alreadyImported) {
                imports.add(new ArkTSDeclarations.ImportStatement(
                        Collections.emptyList(), moduleRef, false,
                        null, null));
            }
        }

        return buildFileOutput(imports, declarations, exports);
    }

    // --- Internal: method body decompilation ---

    List<ArkTSStatement> decompileMethodBody(AbcMethod method,
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

        ControlFlowGraph cfg;
        try {
            cfg = ControlFlowGraph.build(instructions,
                    code.getTryBlocks());
        } catch (Exception e) {
            return fallbackLinearListing(instructions,
                    "CFG construction failed: " + e.getMessage());
        }

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        try {
            return generateStatements(ctx);
        } catch (Exception e) {
            ctx.warnings.add("Statement generation failed: "
                    + e.getMessage());
            return fallbackLinearListingWithWarnings(
                    instructions, ctx.warnings);
        }
    }

    private List<ArkTSStatement> generateStatements(
            DecompilationContext ctx) {
        ControlFlowGraph cfg = ctx.cfg;
        List<BasicBlock> blocks = cfg.getBlocks();

        if (blocks.isEmpty()) {
            return Collections.emptyList();
        }

        if (blocks.size() == 1) {
            BasicBlock single = blocks.get(0);
            ArkInstruction lastInsn = single.getLastInstruction();
            if (lastInsn != null
                    && ArkOpcodesCompat.isUnconditionalJump(
                            lastInsn.getOpcode())) {
                int target = ControlFlowGraph
                        .getJumpTargetPublic(lastInsn);
                if (target == single.getStartOffset()) {
                    List<ArkTSStatement> bodyStmts =
                            cfReconstructor
                                    .processBlockInstructionsExcluding(
                                            single, ctx, lastInsn);
                    ArkTSStatement body =
                            new ArkTSStatement.BlockStatement(
                                    bodyStmts);
                    ArkTSExpression trueCond =
                            new ArkTSExpression.LiteralExpression(
                                    "true",
                                    ArkTSExpression.LiteralExpression
                                            .LiteralKind.BOOLEAN);
                    return List.of(new ArkTSControlFlow.WhileStatement(
                            trueCond, body));
                }
            }
            return cfReconstructor.processBlockInstructions(
                    single, ctx);
        }

        Set<BasicBlock> visited = new HashSet<>();
        return cfReconstructor.reconstructControlFlow(
                cfg, blocks, ctx, visited);
    }

    // --- Fallback listings ---

    private List<ArkTSStatement> fallbackLinearListing(
            List<ArkInstruction> instructions, String reason) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        stmts.add(new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.VariableExpression(
                        "/* linear fallback: " + reason + " */")));
        for (ArkInstruction insn : instructions) {
            stmts.add(new ArkTSStatement.ExpressionStatement(
                    new ArkTSExpression.VariableExpression(
                            "/* " + formatInstructionText(insn)
                                    + " */")));
        }
        return stmts;
    }

    private List<ArkTSStatement> fallbackLinearListingWithWarnings(
            List<ArkInstruction> instructions,
            List<String> warnings) {
        List<ArkTSStatement> stmts = new ArrayList<>();
        for (String warning : warnings) {
            stmts.add(new ArkTSStatement.ExpressionStatement(
                    new ArkTSExpression.VariableExpression(
                            "/* warning: " + warning + " */")));
        }
        stmts.add(new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.VariableExpression(
                        "/* linear fallback listing */")));
        for (ArkInstruction insn : instructions) {
            stmts.add(new ArkTSStatement.ExpressionStatement(
                    new ArkTSExpression.VariableExpression(
                            "/* " + formatInstructionText(insn)
                                    + " */")));
        }
        return stmts;
    }

    // --- Output formatting ---

    private String buildFileOutput(List<ArkTSStatement> imports,
            List<ArkTSStatement> declarations,
            List<ArkTSStatement> exports) {
        StringBuilder sb = new StringBuilder();
        for (ArkTSStatement imp : imports) {
            sb.append(imp.toArkTS(0)).append("\n");
        }
        if (!imports.isEmpty()
                && (!declarations.isEmpty() || !exports.isEmpty())) {
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

    private String buildEmptyMethod(AbcMethod method, AbcProto proto) {
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto, 0);
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(Collections.emptyList());
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        method.getName(), params, returnType, body);
        return func.toArkTS(0);
    }

    private String buildFallbackMethod(AbcMethod method, AbcCode code,
            AbcFile abcFile, String reason) {
        AbcProto proto = resolveProto(method, abcFile);
        List<ArkTSStatement> bodyStmts = new ArrayList<>();
        bodyStmts.add(new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.VariableExpression(
                        "/* decompilation failed: " + reason
                                + " */")));
        return buildMethodSource(method, proto, code, bodyStmts,
                abcFile);
    }

    private String buildMethodSource(AbcMethod method, AbcProto proto,
            AbcCode code, List<ArkTSStatement> bodyStmts,
            AbcFile abcFile) {
        List<ArkTSStatement> filteredStmts =
                filterTrivialReturnUndefined(bodyStmts);

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code.getNumArgs(),
                        getDebugParamNames(method, abcFile));
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(filteredStmts);
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        method.getName(), params, returnType, body);
        return func.toArkTS(0);
    }

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

    // --- Resolver helpers ---

    AbcProto resolveProto(AbcMethod method, AbcFile abcFile) {
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

    List<String> getDebugParamNames(AbcMethod method, AbcFile abcFile) {
        if (abcFile == null) {
            return null;
        }
        AbcDebugInfo debugInfo =
                abcFile.getDebugInfoForMethod(method);
        if (debugInfo == null
                || debugInfo.getParameterNames().isEmpty()) {
            return null;
        }
        return debugInfo.getParameterNames();
    }

    String resolveTypeName(int typeIdx, AbcFile abcFile) {
        if (abcFile == null) {
            return null;
        }
        try {
            List<Long> lnpIndex = abcFile.getLnpIndex();
            if (typeIdx >= 0 && typeIdx < lnpIndex.size()) {
                long strOff = lnpIndex.get(typeIdx);
                byte[] data = abcFile.getRawData();
                if (data != null && strOff >= 0
                        && strOff < data.length) {
                    return DecompilationContext.readMutf8At(
                            data, (int) strOff);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    String resolveClassNameFromMethod(int methodIdx,
            AbcFile abcFile) {
        for (AbcClass cls : abcFile.getClasses()) {
            for (AbcMethod m : cls.getMethods()) {
                if (declBuilder.isConstructorMethod(m,
                        DeclarationBuilder.sanitizeClassName(
                                cls.getName()))) {
                    return DeclarationBuilder.sanitizeClassName(
                            cls.getName());
                }
            }
        }
        return "class_" + methodIdx;
    }

    // --- Static utilities ---

    // --- Delegates for test-visible methods ---

    ArkTSExpression tryReconstructTemplateLiteral(
            ArkTSExpression accValue) {
        return instrHandler.tryReconstructTemplateLiteral(accValue);
    }

    ArkTSExpression tryDetectNullishCoalescing(
            ArkTSExpression condition,
            ArkTSExpression trueValue,
            ArkTSExpression falseValue) {
        return instrHandler.tryDetectNullishCoalescing(
                condition, trueValue, falseValue);
    }

    private static String getModulePath(
            List<String> moduleRequests, int idx) {
        if (idx < 0 || idx >= moduleRequests.size()) {
            return null;
        }
        return moduleRequests.get(idx);
    }

    private static String formatInstructionText(ArkInstruction insn) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x")
                .append(Integer.toHexString(insn.getOffset()));
        sb.append(": ").append(insn.getMnemonic());
        for (ArkOperand op : insn.getOperands()) {
            sb.append(" ").append(op.getValue());
        }
        return sb.toString();
    }

    /**
     * Holds shared state during decompilation of a single method.
     * Delegates to {@link DecompilationContext} for backwards
     * compatibility with test references.
     */
    public static class DecompilationContext
            extends com.arkghidra.decompile.DecompilationContext {

        public DecompilationContext(AbcMethod method, AbcCode code,
                AbcProto proto, AbcFile abcFile,
                ControlFlowGraph cfg,
                List<ArkInstruction> instructions) {
            super(method, code, proto, abcFile, cfg, instructions);
        }
    }
}
