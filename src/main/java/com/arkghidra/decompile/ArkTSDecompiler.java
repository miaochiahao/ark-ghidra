package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcDebugInfo;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcLocalVariable;
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

        populateRegisterNames(method, abcFile, ctx);

        // Detect rest parameter from COPYRESTARGS instruction
        int restParamIndex = detectRestParamIndex(instructions,
                (int) code.getNumArgs());
        ctx.restParamIndex = restParamIndex;

        List<ArkTSStatement> bodyStmts;
        try {
            bodyStmts = applyConstOptimization(generateStatements(ctx));
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
                abcFile, restParamIndex, ctx.isAsync);
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
        ControlFlowGraph cfg;
        try {
            cfg = ControlFlowGraph.build(instructions);
        } catch (Exception e) {
            return buildFallbackInstructionListing(instructions,
                    "CFG construction failed: " + e.getMessage());
        }
        AbcCode code = new AbcCode(0, 0, 0, new byte[0],
                Collections.emptyList(), 0);
        AbcMethod method = new AbcMethod(0, 0, "f", 0, 0, 0);
        DecompilationContext ctx = new DecompilationContext(
                method, code, null, null, cfg, instructions);

        List<ArkTSStatement> stmts;
        try {
            stmts = generateStatements(ctx);
            stmts = applyConstOptimization(stmts);
        } catch (Exception e) {
            return buildFallbackInstructionListing(instructions,
                    "statement generation failed: " + e);
        }
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

        String sourceFileName = null;
        if (!abcFile.getClasses().isEmpty()) {
            sourceFileName = abcFile.getSourceFileForClass(
                    abcFile.getClasses().get(0));
        }
        int classCount = abcFile.getClasses().size();

        List<ArkTSStatement> imports = new ArrayList<>();
        List<ArkTSStatement> declarations = new ArrayList<>();
        List<ArkTSStatement> exports = new ArrayList<>();
        Set<String> seenImportPaths = new HashSet<>();

        // Track imports per module path for merging
        Map<String, ModuleImportCollector> importCollectors =
                new LinkedHashMap<>();

        for (int i = 0; i < abcFile.getClasses().size(); i++) {
            AbcModuleRecord record = abcFile.getModuleRecord(i);
            if (record != null) {
                List<String> moduleRequests =
                        abcFile.resolveModuleRequests(record);

                // Collect regular imports, detecting default imports
                for (AbcModuleRecord.RegularImport ri :
                        record.getRegularImports()) {
                    String localName = abcFile.getSourceFileName(
                            ri.getLocalNameOffset());
                    String importName = abcFile.getSourceFileName(
                            ri.getImportNameOffset());
                    String modulePath = getModulePath(
                            moduleRequests, ri.getModuleRequestIdx());
                    if (modulePath == null) {
                        continue;
                    }
                    seenImportPaths.add(modulePath);
                    ModuleImportCollector collector =
                            importCollectors.computeIfAbsent(
                                    modulePath,
                                    k -> new ModuleImportCollector());
                    if ("default".equals(importName)) {
                        collector.setDefaultImport(localName);
                    } else {
                        collector.addNamedImport(
                                importName, localName);
                    }
                }

                // Collect namespace imports
                for (AbcModuleRecord.NamespaceImport ni :
                        record.getNamespaceImports()) {
                    String localName = abcFile.getSourceFileName(
                            ni.getLocalNameOffset());
                    String modulePath = getModulePath(
                            moduleRequests, ni.getModuleRequestIdx());
                    if (localName != null && modulePath != null) {
                        seenImportPaths.add(modulePath);
                        ModuleImportCollector collector =
                                importCollectors.computeIfAbsent(
                                        modulePath,
                                        k -> new ModuleImportCollector());
                        collector.setNamespaceImport(localName);
                    }
                }

                // Local exports
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

                // Indirect exports (re-exports with from clause)
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
                                        namedExports, null, false,
                                        modulePath));
                    }
                }

                // Star exports
                for (AbcModuleRecord.StarExport se :
                        record.getStarExports()) {
                    String modulePath = getModulePath(moduleRequests,
                            se.getModuleRequestIdx());
                    if (modulePath != null) {
                        exports.add(
                                new ArkTSDeclarations.ExportStatement(
                                        Collections.emptyList(), null,
                                        false, modulePath, true));
                    }
                }
            }
        }

        // Build merged import statements from collectors
        for (Map.Entry<String, ModuleImportCollector> entry
                : importCollectors.entrySet()) {
            String modulePath = entry.getKey();
            ModuleImportCollector collector = entry.getValue();
            imports.addAll(collector.toImportStatements(modulePath));
        }

        for (AbcClass abcClass : abcFile.getClasses()) {
            try {
                ArkTSStatement classDecl =
                        declBuilder.buildClassDeclaration(
                                abcClass, abcFile, seenImportPaths);
                if (classDecl != null) {
                    if (declBuilder.isExported(abcClass)) {
                        exports.add(
                                new ArkTSDeclarations.ExportStatement(
                                        Collections.emptyList(),
                                        classDecl, false));
                    } else {
                        declarations.add(classDecl);
                    }
                }
            } catch (Exception e) {
                String clsName = DeclarationBuilder.sanitizeClassName(
                        abcClass.getName());
                declarations.add(
                        new ArkTSStatement.ExpressionStatement(
                                new ArkTSExpression.VariableExpression(
                                        "/* error decompiling class "
                                                + clsName + ": "
                                                + e.getMessage() + " */")));
            }
        }

        // Group declarations by namespace
        declarations = groupByNamespace(declarations);

        List<ArkTSStatement> enumDecls =
                declBuilder.detectEnumsFromLiteralArrays(abcFile);
        declarations.addAll(enumDecls);

        return buildFileOutput(imports, declarations, exports,
                sourceFileName, classCount);
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

        populateRegisterNames(method, abcFile, ctx);

        try {
            List<ArkTSStatement> stmts = generateStatements(ctx);
            return applyConstOptimization(stmts);
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

    // --- Const vs let optimization ---

    private static List<ArkTSStatement> applyConstOptimization(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        Set<String> declared = new HashSet<>();
        Set<String> reassigned = new HashSet<>();
        collectVarUsage(stmts, declared, reassigned);

        Set<String> constEligible = new HashSet<>(declared);
        constEligible.removeAll(reassigned);
        if (constEligible.isEmpty()) {
            return stmts;
        }
        rewriteLetToConst(stmts, constEligible);
        return stmts;
    }

    private static List<ArkTSStatement> extractBodyList(
            ArkTSStatement block) {
        if (block instanceof ArkTSStatement.BlockStatement) {
            return ((ArkTSStatement.BlockStatement) block).getBody();
        }
        return null;
    }

    private static void collectVarUsage(List<ArkTSStatement> stmts,
            Set<String> declared, Set<String> reassigned) {
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.VariableDeclaration) {
                ArkTSStatement.VariableDeclaration varDecl =
                        (ArkTSStatement.VariableDeclaration) stmt;
                if ("let".equals(varDecl.getKind())) {
                    declared.add(varDecl.getName());
                }
            } else if (stmt instanceof ArkTSStatement.ExpressionStatement) {
                ArkTSExpression expr =
                        ((ArkTSStatement.ExpressionStatement) stmt)
                                .getExpression();
                collectReassigned(expr, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.IfStatement) {
                ArkTSControlFlow.IfStatement ifStmt =
                        (ArkTSControlFlow.IfStatement) stmt;
                collectVarUsageFromStmt(
                        ifStmt.getThenBlock(), declared, reassigned);
                collectVarUsageFromStmt(
                        ifStmt.getElseBlock(), declared, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.WhileStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.WhileStatement) stmt).getBody(),
                        declared, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.DoWhileStatement) stmt)
                                .getBody(),
                        declared, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.ForStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.ForStatement) stmt).getBody(),
                        declared, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.ForOfStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.ForOfStatement) stmt).getBody(),
                        declared, reassigned);
            } else if (stmt instanceof ArkTSControlFlow.ForInStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.ForInStatement) stmt).getBody(),
                        declared, reassigned);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForAwaitOfStatement) {
                collectVarUsageFromStmt(
                        ((ArkTSControlFlow.ForAwaitOfStatement) stmt)
                                .getBody(),
                        declared, reassigned);
            } else if (stmt
                    instanceof ArkTSControlFlow.SwitchStatement) {
                ArkTSControlFlow.SwitchStatement sw =
                        (ArkTSControlFlow.SwitchStatement) stmt;
                for (ArkTSControlFlow.SwitchStatement.SwitchCase sc
                        : sw.getCases()) {
                    collectVarUsage(sc.getBody(), declared, reassigned);
                }
                collectVarUsageFromStmt(
                        sw.getDefaultBlock(), declared, reassigned);
            } else if (stmt
                    instanceof ArkTSControlFlow.TryCatchStatement) {
                ArkTSControlFlow.TryCatchStatement tc =
                        (ArkTSControlFlow.TryCatchStatement) stmt;
                collectVarUsageFromStmt(
                        tc.getTryBlock(), declared, reassigned);
                collectVarUsageFromStmt(
                        tc.getCatchBlock(), declared, reassigned);
                collectVarUsageFromStmt(
                        tc.getFinallyBlock(), declared, reassigned);
            } else if (stmt
                    instanceof ArkTSControlFlow
                            .MultiCatchTryCatchStatement) {
                ArkTSControlFlow.MultiCatchTryCatchStatement mc =
                        (ArkTSControlFlow.MultiCatchTryCatchStatement)
                                stmt;
                collectVarUsageFromStmt(
                        mc.getTryBlock(), declared, reassigned);
                for (ArkTSControlFlow.MultiCatchTryCatchStatement
                        .CatchClause cc : mc.getCatchClauses()) {
                    collectVarUsageFromStmt(
                            cc.getBody(), declared, reassigned);
                }
                collectVarUsageFromStmt(
                        mc.getFinallyBlock(), declared, reassigned);
            } else if (stmt instanceof ArkTSStatement.BlockStatement) {
                collectVarUsage(
                        ((ArkTSStatement.BlockStatement) stmt).getBody(),
                        declared, reassigned);
            }
        }
    }

    private static void collectVarUsageFromStmt(ArkTSStatement block,
            Set<String> declared, Set<String> reassigned) {
        if (block == null) {
            return;
        }
        List<ArkTSStatement> body = extractBodyList(block);
        if (body != null) {
            collectVarUsage(body, declared, reassigned);
        } else {
            collectVarUsage(List.of(block), declared, reassigned);
        }
    }

    private static void collectReassigned(ArkTSExpression expr,
            Set<String> reassigned) {
        if (expr instanceof ArkTSExpression.AssignExpression) {
            ArkTSExpression.AssignExpression assign =
                    (ArkTSExpression.AssignExpression) expr;
            ArkTSExpression target = assign.getTarget();
            if (target instanceof ArkTSExpression.VariableExpression) {
                reassigned.add(
                        ((ArkTSExpression.VariableExpression) target)
                                .getName());
            }
        }
    }

    private static void rewriteLetToConst(List<ArkTSStatement> stmts,
            Set<String> constEligible) {
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.VariableDeclaration) {
                ArkTSStatement.VariableDeclaration varDecl =
                        (ArkTSStatement.VariableDeclaration) stmt;
                if ("let".equals(varDecl.getKind())
                        && constEligible.contains(varDecl.getName())) {
                    varDecl.setKind("const");
                }
            } else if (stmt instanceof ArkTSControlFlow.IfStatement) {
                ArkTSControlFlow.IfStatement ifStmt =
                        (ArkTSControlFlow.IfStatement) stmt;
                rewriteLetToConstInStmt(
                        ifStmt.getThenBlock(), constEligible);
                rewriteLetToConstInStmt(
                        ifStmt.getElseBlock(), constEligible);
            } else if (stmt instanceof ArkTSControlFlow.WhileStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.WhileStatement) stmt).getBody(),
                        constEligible);
            } else if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.DoWhileStatement) stmt)
                                .getBody(),
                        constEligible);
            } else if (stmt instanceof ArkTSControlFlow.ForStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.ForStatement) stmt).getBody(),
                        constEligible);
            } else if (stmt instanceof ArkTSControlFlow.ForOfStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.ForOfStatement) stmt).getBody(),
                        constEligible);
            } else if (stmt instanceof ArkTSControlFlow.ForInStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.ForInStatement) stmt).getBody(),
                        constEligible);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForAwaitOfStatement) {
                rewriteLetToConstInStmt(
                        ((ArkTSControlFlow.ForAwaitOfStatement) stmt)
                                .getBody(),
                        constEligible);
            } else if (stmt
                    instanceof ArkTSControlFlow.SwitchStatement) {
                ArkTSControlFlow.SwitchStatement sw =
                        (ArkTSControlFlow.SwitchStatement) stmt;
                for (ArkTSControlFlow.SwitchStatement.SwitchCase sc
                        : sw.getCases()) {
                    rewriteLetToConst(sc.getBody(), constEligible);
                }
                rewriteLetToConstInStmt(
                        sw.getDefaultBlock(), constEligible);
            } else if (stmt
                    instanceof ArkTSControlFlow.TryCatchStatement) {
                ArkTSControlFlow.TryCatchStatement tc =
                        (ArkTSControlFlow.TryCatchStatement) stmt;
                rewriteLetToConstInStmt(
                        tc.getTryBlock(), constEligible);
                rewriteLetToConstInStmt(
                        tc.getCatchBlock(), constEligible);
                rewriteLetToConstInStmt(
                        tc.getFinallyBlock(), constEligible);
            } else if (stmt
                    instanceof ArkTSControlFlow
                            .MultiCatchTryCatchStatement) {
                ArkTSControlFlow.MultiCatchTryCatchStatement mc =
                        (ArkTSControlFlow.MultiCatchTryCatchStatement)
                                stmt;
                rewriteLetToConstInStmt(
                        mc.getTryBlock(), constEligible);
                for (ArkTSControlFlow.MultiCatchTryCatchStatement
                        .CatchClause cc : mc.getCatchClauses()) {
                    rewriteLetToConstInStmt(
                            cc.getBody(), constEligible);
                }
                rewriteLetToConstInStmt(
                        mc.getFinallyBlock(), constEligible);
            } else if (stmt instanceof ArkTSStatement.BlockStatement) {
                rewriteLetToConst(
                        ((ArkTSStatement.BlockStatement) stmt).getBody(),
                        constEligible);
            }
        }
    }

    private static void rewriteLetToConstInStmt(ArkTSStatement block,
            Set<String> constEligible) {
        if (block == null) {
            return;
        }
        List<ArkTSStatement> body = extractBodyList(block);
        if (body != null) {
            rewriteLetToConst(body, constEligible);
        } else {
            rewriteLetToConst(List.of(block), constEligible);
        }
    }

    // --- Fallback listings ---

    private String buildFallbackInstructionListing(
            List<ArkInstruction> instructions, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("/* decompilation fallback: ").append(reason)
                .append(" */\n");
        for (ArkInstruction insn : instructions) {
            sb.append("/* ").append(formatInstructionText(insn))
                    .append(" */\n");
        }
        return sb.toString().trim();
    }

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
            List<ArkTSStatement> exports,
            String sourceFileName, int classCount) {
        StringBuilder sb = new StringBuilder();
        // File header comment
        if (sourceFileName != null && !sourceFileName.isEmpty()) {
            sb.append("// Decompiled from: ")
                    .append(sourceFileName);
            sb.append(" (").append(classCount).append(" class");
            if (classCount != 1) {
                sb.append("es");
            }
            sb.append(")\n\n");
        }
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
            if (!declarations.isEmpty()) {
                sb.append("\n\n");
            } else if (!imports.isEmpty()) {
                sb.append("\n");
            }
            for (int i = 0; i < exports.size(); i++) {
                sb.append(exports.get(i).toArkTS(0));
                if (i < exports.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        return ArkTSStatement.normalizeBlankLines(sb.toString());
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
        return buildMethodSource(method, proto, code, bodyStmts,
                abcFile, -1, false);
    }

    private String buildMethodSource(AbcMethod method, AbcProto proto,
            AbcCode code, List<ArkTSStatement> bodyStmts,
            AbcFile abcFile, int restParamIndex, boolean isAsync) {
        List<ArkTSStatement> filteredStmts =
                filterTrivialReturnUndefined(bodyStmts);

        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code.getNumArgs(),
                        getDebugParamNames(method, abcFile),
                        restParamIndex);
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(filteredStmts);
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        method.getName(), params, returnType, body,
                        isAsync);
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

    /**
     * Populates register-to-name mappings from debug info into the
     * decompilation context. Iterates over each line number program
     * index in the debug info and parses local variables, then
     * registers each variable's name via
     * {@link DecompilationContext#setRegisterName}.
     *
     * @param method the method being decompiled
     * @param abcFile the parent ABC file (may be null)
     * @param ctx the decompilation context (may be null)
     */
    private static void populateRegisterNames(AbcMethod method,
            AbcFile abcFile, DecompilationContext ctx) {
        if (abcFile == null || ctx == null) {
            return;
        }
        AbcDebugInfo debugInfo =
                abcFile.getDebugInfoForMethod(method);
        if (debugInfo == null) {
            return;
        }
        List<Long> lnpIndices = debugInfo.getLineNumProgramIdx();
        for (Long lnpIdx : lnpIndices) {
            if (lnpIdx == null || lnpIdx <= 0) {
                continue;
            }
            List<AbcLocalVariable> locals =
                    abcFile.parseLocalVariables(lnpIdx);
            for (AbcLocalVariable local : locals) {
                if (local.getName() != null
                        && !local.getName().isEmpty()) {
                    ctx.setRegisterName(local.getRegisterNum(),
                            local.getName());
                }
            }
        }
    }

    /**
     * Detects the rest parameter index by scanning instructions for
     * COPYRESTARGS. In Ark bytecode, COPYRESTARGS stores the rest
     * arguments into a register. The rest parameter is the last
     * parameter when this opcode is present.
     *
     * @param instructions the method instructions
     * @param numArgs the number of declared arguments
     * @return the rest parameter index, or -1 if no rest parameter
     */
    static int detectRestParamIndex(
            List<ArkInstruction> instructions, int numArgs) {
        for (ArkInstruction insn : instructions) {
            if (insn.getOpcode() == ArkOpcodesCompat.COPYRESTARGS) {
                // Rest parameter is the last parameter
                return Math.max(0, numArgs - 1);
            }
        }
        return -1;
    }

    /**
     * Detects whether a method is async by scanning its code for
     * the ASYNCFUNCTIONENTER opcode.
     *
     * @param code the method's code section
     * @return true if the method contains an ASYNCFUNCTIONENTER opcode
     */
    boolean detectAsyncMethod(AbcCode code) {
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return false;
        }
        for (byte b : code.getInstructions()) {
            if ((b & 0xFF) == ArkOpcodesCompat.ASYNCFUNCTIONENTER) {
                return true;
            }
        }
        return false;
    }

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

    /**
     * Groups top-level declarations by their namespace. Declarations
     * with a namespace are wrapped in a {@link NamespaceStatement}.
     * Declarations without a namespace remain as-is.
     *
     * @param decls the flat list of declarations
     * @return declarations grouped by namespace
     */
    static List<ArkTSStatement> groupByNamespace(
            List<ArkTSStatement> decls) {
        Map<String, List<ArkTSStatement>> nsGroups =
                new LinkedHashMap<>();
        List<ArkTSStatement> noNamespace = new ArrayList<>();

        for (ArkTSStatement decl : decls) {
            String ns = extractNamespace(decl);
            if (ns != null) {
                nsGroups.computeIfAbsent(ns,
                        k -> new ArrayList<>()).add(decl);
            } else {
                noNamespace.add(decl);
            }
        }

        List<ArkTSStatement> result = new ArrayList<>();
        for (Map.Entry<String, List<ArkTSStatement>> entry
                : nsGroups.entrySet()) {
            result.add(new ArkTSDeclarations.NamespaceStatement(
                    entry.getKey(), entry.getValue()));
        }
        result.addAll(noNamespace);
        return result;
    }

    /**
     * Extracts the namespace from a declaration's class name pattern.
     *
     * <p>For class names derived from Ark bytecode like
     * "Lcom/example/MyClass;", the namespace is "com.example".
     * Returns null if the class name has no package prefix.
     *
     * @param decl the declaration statement
     * @return the namespace string, or null
     */
    static String extractNamespace(ArkTSStatement decl) {
        if (decl instanceof ArkTSDeclarations.ClassDeclaration) {
            String rawName =
                    ((ArkTSDeclarations.ClassDeclaration) decl)
                            .getRawName();
            if (rawName != null) {
                return namespaceFromClassName(rawName);
            }
        }
        String className = getDeclarationName(decl);
        if (className == null) {
            return null;
        }
        return namespaceFromClassName(className);
    }

    /**
     * Gets the simple name from a declaration statement.
     *
     * @param decl the declaration
     * @return the name, or null
     */
    static String getDeclarationName(ArkTSStatement decl) {
        if (decl instanceof ArkTSDeclarations.ClassDeclaration) {
            return ((ArkTSDeclarations.ClassDeclaration) decl).getName();
        }
        if (decl instanceof ArkTSTypeDeclarations.StructDeclaration) {
            return ((ArkTSTypeDeclarations.StructDeclaration) decl)
                    .getName();
        }
        if (decl
                instanceof ArkTSTypeDeclarations.GenericClassDeclaration) {
            return ((ArkTSTypeDeclarations.GenericClassDeclaration) decl)
                    .getName();
        }
        if (decl instanceof ArkTSTypeDeclarations.EnumDeclaration) {
            return ((ArkTSTypeDeclarations.EnumDeclaration) decl)
                    .getName();
        }
        if (decl instanceof ArkTSDeclarations.FunctionDeclaration) {
            return ((ArkTSDeclarations.FunctionDeclaration) decl)
                    .getName();
        }
        if (decl instanceof ArkTSDeclarations.NamespaceStatement) {
            return ((ArkTSDeclarations.NamespaceStatement) decl)
                    .getName();
        }
        return null;
    }

    /**
     * Derives a namespace from a class name that may contain package
     * path separators.
     *
     * <p>Handles names like "com.example.MyClass" or
     * "Lcom/example/MyClass;". Returns null if the name has no
     * package component (i.e. no dot or slash separator).
     *
     * @param className the class name
     * @return the namespace, or null
     */
    static String namespaceFromClassName(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        String name = className;
        if (name.startsWith("L") && name.endsWith(";")) {
            name = name.substring(1, name.length() - 1);
        }
        int lastSlash = name.lastIndexOf('/');
        int lastDot = name.lastIndexOf('.');
        if (lastSlash > 0) {
            String ns = name.substring(0, lastSlash);
            return ns.replace('/', '.');
        }
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return null;
    }

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

    /**
     * Collects import entries for a single module path, supporting
     * merging of default, named, and namespace imports into one
     * import statement.
     */
    public static class ModuleImportCollector {
        private String defaultImportName;
        private String namespaceImportName;
        private final List<String> namedImports = new ArrayList<>();
        private final Set<String> seenNames = new HashSet<>();

        public void setDefaultImport(String localName) {
            this.defaultImportName = localName;
        }

        public void setNamespaceImport(String localName) {
            this.namespaceImportName = localName;
        }

        public void addNamedImport(String importName, String localName) {
            String entry;
            if (localName != null && importName != null
                    && !localName.equals(importName)) {
                entry = importName + " as " + localName;
            } else if (importName != null) {
                entry = importName;
            } else {
                return;
            }
            if (seenNames.add(entry)) {
                namedImports.add(entry);
            }
        }

        /**
         * Builds import statements for this module path.
         *
         * <p>When both namespace and named imports exist for the same module,
         * they need separate statements since ES module syntax does not
         * allow combining them in one import declaration.
         *
         * @param modulePath the module path
         * @return the list of import statements
         */
        public List<ArkTSDeclarations.ImportStatement> toImportStatements(
                String modulePath) {
            List<ArkTSDeclarations.ImportStatement> result =
                    new ArrayList<>();
            if (namespaceImportName != null && !namedImports.isEmpty()) {
                // Namespace + named: separate statements
                result.add(new ArkTSDeclarations.ImportStatement(
                        namedImports, modulePath,
                        defaultImportName != null,
                        defaultImportName, null));
                result.add(new ArkTSDeclarations.ImportStatement(
                        Collections.emptyList(), modulePath,
                        false, null, namespaceImportName));
            } else {
                result.add(new ArkTSDeclarations.ImportStatement(
                        namedImports, modulePath,
                        defaultImportName != null,
                        defaultImportName, namespaceImportName));
            }
            return result;
        }

        /**
         * Convenience method: returns the first (or only) import statement.
         */
        public ArkTSDeclarations.ImportStatement toImportStatement(
                String modulePath) {
            List<ArkTSDeclarations.ImportStatement> stmts =
                    toImportStatements(modulePath);
            return stmts.isEmpty() ? null : stmts.get(0);
        }
    }
}
