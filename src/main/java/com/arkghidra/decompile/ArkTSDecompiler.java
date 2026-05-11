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

    private long methodTimeoutMs = 5000L;

    private final InstructionHandler instrHandler;
    private final ControlFlowReconstructor cfReconstructor;
    private final DeclarationBuilder declBuilder;
    private final ArkDisassembler disasm = new ArkDisassembler();

    public ArkTSDecompiler() {
        this.instrHandler = new InstructionHandler(this);
        this.cfReconstructor = new ControlFlowReconstructor(
                this, instrHandler);
        this.declBuilder = new DeclarationBuilder(this);
    }

    /**
     * Sets the per-method decompilation timeout in milliseconds.
     * When a method takes longer than this, it returns a timeout
     * comment instead of decompiled output. Default is 5000ms.
     * Set to 0 to disable timeout.
     *
     * @param ms timeout in milliseconds, or 0 to disable
     */
    public void setMethodTimeoutMs(long ms) {
        this.methodTimeoutMs = ms;
    }

    /**
     * Returns the current per-method timeout in milliseconds.
     *
     * @return the timeout in milliseconds, or 0 if disabled
     */
    public long getMethodTimeoutMs() {
        return methodTimeoutMs;
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
        if (method == null) {
            return "/* unknown method */";
        }
        if (code == null || code.getInstructions() == null
                || code.getCodeSize() == 0) {
            return buildEmptyMethod(method, null);
        }

        long startTimeNs = System.nanoTime();

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

        if (isTimedOut(startTimeNs)) {
            return buildTimeoutMethod(method, code, abcFile,
                    startTimeNs);
        }

        ControlFlowGraph cfg;
        try {
            cfg = ControlFlowGraph.build(instructions,
                    code.getTryBlocks());
        } catch (Exception e) {
            return buildFallbackMethod(method, code, abcFile,
                    "CFG construction failed: " + e.getMessage());
        }

        if (isTimedOut(startTimeNs)) {
            return buildTimeoutMethod(method, code, abcFile,
                    startTimeNs);
        }

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        populateRegisterNames(method, abcFile, ctx);
        populateLineNumbers(method, abcFile, ctx);

        // Detect rest parameter from COPYRESTARGS instruction
        int restParamIndex = detectRestParamIndex(instructions,
                (int) code.getNumArgs());
        ctx.restParamIndex = restParamIndex;

        List<ArkTSStatement> bodyStmts;
        try {
            bodyStmts = generateStatements(ctx);

            if (isTimedOut(startTimeNs)) {
                return buildTimeoutMethod(method, code, abcFile,
                        startTimeNs);
            }

            bodyStmts = applyConstOptimization(bodyStmts);
            bodyStmts = ExpressionVisitor.inlineSingleUseVariables(
                    bodyStmts);
            bodyStmts = mergeNestedIfConditions(bodyStmts);
            bodyStmts = detectSwitchExpressions(bodyStmts);
            bodyStmts = simplifyReturnIfTernary(bodyStmts);
            bodyStmts = convertIfElseChainToSwitch(bodyStmts);
            bodyStmts = removeUnreachableCode(bodyStmts);
            bodyStmts = removeAlwaysFalseConditions(bodyStmts);
            bodyStmts = simplifyNegatedComparisons(bodyStmts);
            bodyStmts = removeUnusedVariables(bodyStmts);
            bodyStmts = eliminateDeadPropertyLoads(bodyStmts);
            bodyStmts = simplifyIncrementDecrement(bodyStmts);
            bodyStmts = ExpressionVisitor.eliminateRedundantCopies(
                    bodyStmts);
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
            stmts = ExpressionVisitor.inlineSingleUseVariables(stmts);
            stmts = mergeNestedIfConditions(stmts);
            stmts = detectSwitchExpressions(stmts);
            stmts = simplifyReturnIfTernary(stmts);
            stmts = convertIfElseChainToSwitch(stmts);
            stmts = removeUnreachableCode(stmts);
            stmts = removeAlwaysFalseConditions(stmts);
            stmts = simplifyNegatedComparisons(stmts);
            stmts = removeUnusedVariables(stmts);
            stmts = eliminateDeadPropertyLoads(stmts);
            stmts = simplifyIncrementDecrement(stmts);
            stmts = ExpressionVisitor.eliminateRedundantCopies(stmts);
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

        List<ArkTSStatement> imports = new ArrayList<>(8);
        List<ArkTSStatement> declarations = new ArrayList<>(classCount);
        List<ArkTSStatement> exports = new ArrayList<>(8);
        Set<String> seenImportPaths = new HashSet<>(8);

        // Track imports per module path for merging
        Map<String, ModuleImportCollector> importCollectors =
                new LinkedHashMap<>(8);

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

        long startTimeNs = System.nanoTime();

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

        if (isTimedOut(startTimeNs)) {
            return buildTimeoutBody(startTimeNs);
        }

        AbcProto proto = resolveProto(method, abcFile);
        DecompilationContext ctx = new DecompilationContext(
                method, code, proto, abcFile, cfg, instructions);

        populateRegisterNames(method, abcFile, ctx);

        try {
            List<ArkTSStatement> stmts = generateStatements(ctx);

            if (isTimedOut(startTimeNs)) {
                return buildTimeoutBody(startTimeNs);
            }

            stmts = applyConstOptimization(stmts);
            stmts = ExpressionVisitor.inlineSingleUseVariables(stmts);
            stmts = mergeNestedIfConditions(stmts);
            stmts = detectSwitchExpressions(stmts);
            stmts = simplifyReturnIfTernary(stmts);
            stmts = convertIfElseChainToSwitch(stmts);
            stmts = removeUnreachableCode(stmts);
            stmts = removeAlwaysFalseConditions(stmts);
            stmts = simplifyNegatedComparisons(stmts);
            stmts = removeUnusedVariables(stmts);
            stmts = eliminateDeadPropertyLoads(stmts);
            stmts = simplifyIncrementDecrement(stmts);
            return ExpressionVisitor.eliminateRedundantCopies(stmts);
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

    // --- Switch expression detection ---

    /**
     * Detects switch-in-assignment patterns and converts them to
     * switch expressions.
     *
     * <p>Pattern: variable declaration without initializer followed
     * by a switch statement where every case assigns the same variable.
     * Transforms to:
     * <pre>
     * let result = switch (x) {
     *   case 1: "one"
     *   case 2: "two"
     *   default: "other"
     * }
     * </pre>
     *
     * @param stmts the statement list to transform
     * @return the transformed list
     */
    static List<ArkTSStatement> detectSwitchExpressions(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.size() < 2) {
            return stmts;
        }
        for (int i = 0; i < stmts.size() - 1; i++) {
            ArkTSStatement cur = stmts.get(i);
            ArkTSStatement next = stmts.get(i + 1);

            String varName = extractUninitVarDecl(cur);
            if (varName == null) {
                continue;
            }
            if (!(next instanceof ArkTSControlFlow.SwitchStatement)) {
                continue;
            }

            ArkTSControlFlow.SwitchStatement switchStmt =
                    (ArkTSControlFlow.SwitchStatement) next;
            ArkTSAccessExpressions.SwitchExpression switchExpr =
                    tryConvertToSwitchExpression(switchStmt, varName);
            if (switchExpr != null) {
                // Replace: var declaration gets switch expression as
                // initializer, switch statement is removed
                ArkTSStatement newDecl =
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, null, switchExpr);
                List<ArkTSStatement> result =
                        new ArrayList<>(stmts);
                result.set(i, newDecl);
                result.remove(i + 1);
                return result;
            }
        }
        return stmts;
    }

    // --- Return-if ternary conversion ---

    /**
     * Converts if/else blocks where both branches return (or throw) into
     * a single return/throw with a ternary expression.
     *
     * <p>Transforms:
     * <pre>
     * if (cond) { return a; } else { return b; }
     * </pre>
     * Into:
     * <pre>
     * return cond ? a : b;
     * </pre>
     *
     * <p>Also handles: if-return followed by trailing return (no else).
     */
    static List<ArkTSStatement> simplifyReturnIfTernary(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (int i = 0; i < stmts.size(); i++) {
            ArkTSStatement stmt = stmts.get(i);
            // Check if-else with both branches returning
            if (stmt instanceof ArkTSControlFlow.IfStatement) {
                ArkTSControlFlow.IfStatement ifStmt =
                        (ArkTSControlFlow.IfStatement) stmt;
                ArkTSStatement simplified =
                        tryConvertReturnIf(ifStmt);
                if (simplified != null) {
                    result.add(simplified);
                    continue;
                }
                // Check trailing return pattern:
                // if (cond) { return a; } followed by return b;
                if (ifStmt.getElseBlock() == null && i + 1 < stmts.size()) {
                    ArkTSStatement next = stmts.get(i + 1);
                    ArkTSStatement trailing =
                            tryConvertTrailingReturn(ifStmt, next);
                    if (trailing != null) {
                        result.add(trailing);
                        i++; // skip next statement
                        continue;
                    }
                }
            }
            result.add(stmt);
        }
        return result;
    }

    /**
     * Tries to convert if-return followed by a trailing return/throw
     * into a single return/throw with ternary.
     * Pattern: if (cond) { return a; } return b; → return cond ? a : b;
     */
    private static ArkTSStatement tryConvertTrailingReturn(
            ArkTSControlFlow.IfStatement ifStmt,
            ArkTSStatement next) {
        ArkTSStatement thenBlock = ifStmt.getThenBlock();
        ArkTSStatement.ReturnStatement thenReturn =
                extractSingleReturn(thenBlock);
        ArkTSStatement.ThrowStatement thenThrow =
                thenReturn == null ? extractSingleThrow(thenBlock) : null;
        if (thenReturn == null && thenThrow == null) {
            return null;
        }
        if (thenReturn != null && next
                instanceof ArkTSStatement.ReturnStatement) {
            ArkTSStatement.ReturnStatement trailingReturn =
                    (ArkTSStatement.ReturnStatement) next;
            return new ArkTSStatement.ReturnStatement(
                    new ArkTSAccessExpressions.ConditionalExpression(
                            ifStmt.getCondition(),
                            thenReturn.getValue(),
                            trailingReturn.getValue()));
        }
        if (thenThrow != null && next
                instanceof ArkTSStatement.ThrowStatement) {
            ArkTSStatement.ThrowStatement trailingThrow =
                    (ArkTSStatement.ThrowStatement) next;
            return new ArkTSStatement.ThrowStatement(
                    new ArkTSAccessExpressions.ConditionalExpression(
                            ifStmt.getCondition(),
                            thenThrow.getValue(),
                            trailingThrow.getValue()));
        }
        return null;
    }

    /**
     * Tries to convert an if/else where both branches return into
     * a single return with ternary. Returns null if not applicable.
     */
    private static ArkTSStatement tryConvertReturnIf(
            ArkTSControlFlow.IfStatement ifStmt) {
        ArkTSStatement thenBlock = ifStmt.getThenBlock();
        ArkTSStatement elseBlock = ifStmt.getElseBlock();

        // Extract single return/throw from then-block
        ArkTSStatement.ReturnStatement thenReturn =
                extractSingleReturn(thenBlock);
        ArkTSStatement.ThrowStatement thenThrow =
                thenReturn == null ? extractSingleThrow(thenBlock) : null;
        if (thenReturn == null && thenThrow == null) {
            return null;
        }

        if (elseBlock != null) {
            // if/else pattern
            if (thenReturn != null) {
                ArkTSStatement.ReturnStatement elseReturn =
                        extractSingleReturn(elseBlock);
                if (elseReturn != null) {
                    return new ArkTSStatement.ReturnStatement(
                            new ArkTSAccessExpressions.ConditionalExpression(
                                    ifStmt.getCondition(),
                                    thenReturn.getValue(),
                                    elseReturn.getValue()));
                }
            }
            if (thenThrow != null) {
                ArkTSStatement.ThrowStatement elseThrow =
                        extractSingleThrow(elseBlock);
                if (elseThrow != null) {
                    return new ArkTSStatement.ThrowStatement(
                            new ArkTSAccessExpressions.ConditionalExpression(
                                    ifStmt.getCondition(),
                                    thenThrow.getValue(),
                                    elseThrow.getValue()));
                }
            }
        }
        return null;
    }

    /**
     * Tries to convert if-return followed by a trailing return/throw
     * into a single return with ternary. Returns null if not applicable.
     */

    private static ArkTSStatement.ReturnStatement extractSingleReturn(
            ArkTSStatement block) {
        List<ArkTSStatement> body = extractBodyList(block);
        if (body == null) {
            return block instanceof ArkTSStatement.ReturnStatement
                    ? (ArkTSStatement.ReturnStatement) block
                    : null;
        }
        if (body.size() != 1) {
            return null;
        }
        return body.get(0) instanceof ArkTSStatement.ReturnStatement
                ? (ArkTSStatement.ReturnStatement) body.get(0)
                : null;
    }

    private static ArkTSStatement.ThrowStatement extractSingleThrow(
            ArkTSStatement block) {
        List<ArkTSStatement> body = extractBodyList(block);
        if (body == null) {
            return block instanceof ArkTSStatement.ThrowStatement
                    ? (ArkTSStatement.ThrowStatement) block
                    : null;
        }
        if (body.size() != 1) {
            return null;
        }
        return body.get(0) instanceof ArkTSStatement.ThrowStatement
                ? (ArkTSStatement.ThrowStatement) body.get(0)
                : null;
    }

    // --- If-else chain to switch conversion ---

    /**
     * Converts consecutive if/else-if chains that compare the same variable
     * against different constants into switch statements.
     *
     * <p>Transforms:
     * <pre>
     * if (x === 1) { ... } else if (x === 2) { ... } else { ... }
     * </pre>
     * Into:
     * <pre>
     * switch (x) { case 1: ... case 2: ... default: ... }
     * </pre>
     */
    static List<ArkTSStatement> convertIfElseChainToSwitch(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            result.add(tryConvertIfElseChain(stmt));
        }
        return result;
    }

    private static ArkTSStatement tryConvertIfElseChain(
            ArkTSStatement stmt) {
        if (!(stmt instanceof ArkTSControlFlow.IfStatement)) {
            return stmt;
        }
        ArkTSControlFlow.IfStatement ifStmt =
                (ArkTSControlFlow.IfStatement) stmt;

        // Collect chain: list of (condition, thenBlock) + optional default
        List<ChainEntry> chain = new ArrayList<>();
        ArkTSStatement defaultBlock = collectIfElseChain(ifStmt, chain);

        // Need at least 3 branches to convert
        if (chain.size() < 3) {
            return stmt;
        }

        // Check if all conditions compare same variable with ===
        String discriminantVar = null;
        List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                new ArrayList<>();
        for (ChainEntry entry : chain) {
            String[] match = extractStrictEqComparison(entry.condition);
            if (match == null) {
                return stmt; // not a strict equality comparison
            }
            String varName = match[0];
            String constVal = match[1];
            if (discriminantVar == null) {
                discriminantVar = varName;
            } else if (!discriminantVar.equals(varName)) {
                return stmt; // different variables
            }
            ArkTSExpression testExpr =
                    new ArkTSExpression.LiteralExpression(constVal,
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER);
            List<ArkTSStatement> body = extractBodyListOrSingleton(
                    entry.thenBlock);
            cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                    testExpr, body));
        }

        if (discriminantVar == null) {
            return stmt;
        }

        ArkTSExpression discriminant =
                new ArkTSExpression.VariableExpression(discriminantVar);
        return new ArkTSControlFlow.SwitchStatement(
                discriminant, cases, defaultBlock);
    }

    private static ArkTSStatement collectIfElseChain(
            ArkTSControlFlow.IfStatement ifStmt,
            List<ChainEntry> chain) {
        chain.add(new ChainEntry(ifStmt.getCondition(),
                ifStmt.getThenBlock()));
        if (ifStmt.getElseBlock() == null) {
            return null;
        }
        if (ifStmt.getElseBlock()
                instanceof ArkTSControlFlow.IfStatement) {
            return collectIfElseChain(
                    (ArkTSControlFlow.IfStatement)
                            ifStmt.getElseBlock(),
                    chain);
        }
        return ifStmt.getElseBlock();
    }

    /**
     * Extracts variable name and constant value from a strict equality
     * comparison (x === const or const === x). Returns null if not
     * a strict equality with a constant on one side.
     */
    private static String[] extractStrictEqComparison(
            ArkTSExpression condition) {
        if (!(condition instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) condition;
        if (!"===".equals(bin.getOperator())) {
            return null;
        }
        // Check left is variable, right is constant
        String leftVar = tryGetVariableName(bin.getLeft());
        String rightConst = tryGetConstantValue(bin.getRight());
        if (leftVar != null && rightConst != null) {
            return new String[]{leftVar, rightConst};
        }
        // Check right is variable, left is constant
        String rightVar = tryGetVariableName(bin.getRight());
        String leftConst = tryGetConstantValue(bin.getLeft());
        if (rightVar != null && leftConst != null) {
            return new String[]{rightVar, leftConst};
        }
        return null;
    }

    private static String tryGetVariableName(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return ((ArkTSExpression.VariableExpression) expr).getName();
        }
        return null;
    }

    private static String tryGetConstantValue(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return ((ArkTSExpression.LiteralExpression) expr).getValue();
        }
        return null;
    }

    private static List<ArkTSStatement> extractBodyListOrSingleton(
            ArkTSStatement block) {
        List<ArkTSStatement> body = extractBodyList(block);
        if (body != null) {
            return body;
        }
        return Collections.singletonList(block);
    }

    private static class ChainEntry {
        final ArkTSExpression condition;
        final ArkTSStatement thenBlock;

        ChainEntry(ArkTSExpression condition, ArkTSStatement thenBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
        }
    }

    // --- Unused variable removal ---

    /**
     * Removes variable declarations that are never referenced after
     * their declaration point.
     *
     * <p>Preserves declarations with side-effecting initializers
     * (function calls, object creation, etc.).
     */
    static List<ArkTSStatement> removeUnusedVariables(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        // Collect all variable declaration names
        Set<String> declaredNames = new HashSet<>();
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.VariableDeclaration) {
                declaredNames.add(
                        ((ArkTSStatement.VariableDeclaration) stmt)
                                .getName());
            }
        }
        if (declaredNames.isEmpty()) {
            return stmts;
        }
        // Check which variables are used via ExpressionVisitor
        Set<String> usedVars = new HashSet<>();
        for (String varName : declaredNames) {
            for (ArkTSStatement stmt : stmts) {
                if (countVarUsageInStmt(stmt, varName) > 0) {
                    usedVars.add(varName);
                    break;
                }
            }
        }

        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.VariableDeclaration) {
                ArkTSStatement.VariableDeclaration decl =
                        (ArkTSStatement.VariableDeclaration) stmt;
                if (!usedVars.contains(decl.getName())
                        && !hasSideEffectingInitializer(decl)) {
                    continue;
                }
            }
            result.add(stmt);
        }
        return result.isEmpty() && !stmts.isEmpty()
                ? stmts : result;
    }

    private static int countVarReadUsageInStmt(ArkTSStatement stmt,
            String varName) {
        if (stmt instanceof ArkTSStatement.VariableDeclaration) {
            ArkTSStatement.VariableDeclaration decl =
                    (ArkTSStatement.VariableDeclaration) stmt;
            if (decl.getName().equals(varName)) {
                return 0;
            }
            return decl.getInitializer() != null
                    ? ExpressionVisitor.countVariableReadUsage(
                            decl.getInitializer(), varName)
                    : 0;
        } else if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            return ExpressionVisitor.countVariableReadUsage(
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression(),
                    varName);
        } else if (stmt instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) stmt).getValue();
            return val != null
                    ? ExpressionVisitor.countVariableReadUsage(
                            val, varName)
                    : 0;
        } else if (stmt instanceof ArkTSStatement.ThrowStatement) {
            return ExpressionVisitor.countVariableReadUsage(
                    ((ArkTSStatement.ThrowStatement) stmt).getValue(),
                    varName);
        }
        // For control flow (if/while/switch/try etc.), delegate to
        // countVarUsageInStmt which handles nested blocks correctly.
        return countVarUsageInStmt(stmt, varName);
    }

    private static int countVarUsageInStmt(ArkTSStatement stmt,
            String varName) {
        if (stmt instanceof ArkTSStatement.VariableDeclaration) {
            ArkTSStatement.VariableDeclaration decl =
                    (ArkTSStatement.VariableDeclaration) stmt;
            if (decl.getName().equals(varName)) {
                return 0; // declaration itself is not a usage
            }
            return decl.getInitializer() != null
                    ? ExpressionVisitor.countVariableUsage(
                            decl.getInitializer(), varName)
                    : 0;
        } else if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            return ExpressionVisitor.countVariableUsage(
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression(),
                    varName);
        } else if (stmt instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) stmt).getValue();
            return val != null
                    ? ExpressionVisitor.countVariableUsage(val, varName)
                    : 0;
        } else if (stmt instanceof ArkTSStatement.ThrowStatement) {
            return ExpressionVisitor.countVariableUsage(
                    ((ArkTSStatement.ThrowStatement) stmt).getValue(),
                    varName);
        } else if (stmt instanceof ArkTSControlFlow.IfStatement) {
            ArkTSControlFlow.IfStatement ifStmt =
                    (ArkTSControlFlow.IfStatement) stmt;
            int count = ExpressionVisitor.countVariableUsage(
                    ifStmt.getCondition(), varName);
            count += countVarUsageInBlock(
                    ifStmt.getThenBlock(), varName);
            count += countVarUsageInBlock(
                    ifStmt.getElseBlock(), varName);
            return count;
        } else if (stmt instanceof ArkTSControlFlow.WhileStatement) {
            ArkTSControlFlow.WhileStatement ws =
                    (ArkTSControlFlow.WhileStatement) stmt;
            int count = ExpressionVisitor.countVariableUsage(
                    ws.getCondition(), varName);
            count += countVarUsageInBlock(ws.getBody(), varName);
            return count;
        } else if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
            ArkTSControlFlow.DoWhileStatement dw =
                    (ArkTSControlFlow.DoWhileStatement) stmt;
            int count = ExpressionVisitor.countVariableUsage(
                    dw.getCondition(), varName);
            count += countVarUsageInBlock(dw.getBody(), varName);
            return count;
        } else if (stmt instanceof ArkTSControlFlow.ForStatement) {
            return countVarUsageInBlock(
                    ((ArkTSControlFlow.ForStatement) stmt).getBody(),
                    varName);
        } else if (stmt instanceof ArkTSControlFlow.ForOfStatement) {
            return countVarUsageInBlock(
                    ((ArkTSControlFlow.ForOfStatement) stmt).getBody(),
                    varName);
        } else if (stmt instanceof ArkTSControlFlow.ForInStatement) {
            return countVarUsageInBlock(
                    ((ArkTSControlFlow.ForInStatement) stmt).getBody(),
                    varName);
        } else if (stmt
                instanceof ArkTSControlFlow.ForAwaitOfStatement) {
            return countVarUsageInBlock(
                    ((ArkTSControlFlow.ForAwaitOfStatement) stmt)
                            .getBody(),
                    varName);
        } else if (stmt instanceof ArkTSControlFlow.SwitchStatement) {
            ArkTSControlFlow.SwitchStatement sw =
                    (ArkTSControlFlow.SwitchStatement) stmt;
            int count = ExpressionVisitor.countVariableUsage(
                    sw.getDiscriminant(), varName);
            for (ArkTSControlFlow.SwitchStatement.SwitchCase sc
                    : sw.getCases()) {
                for (ArkTSStatement s : sc.getBody()) {
                    count += countVarUsageInStmt(s, varName);
                }
            }
            count += countVarUsageInBlock(
                    sw.getDefaultBlock(), varName);
            return count;
        } else if (stmt instanceof ArkTSStatement.BlockStatement) {
            int count = 0;
            for (ArkTSStatement s
                    : ((ArkTSStatement.BlockStatement) stmt).getBody()) {
                count += countVarUsageInStmt(s, varName);
            }
            return count;
        } else if (stmt
                instanceof ArkTSControlFlow.TryCatchStatement) {
            ArkTSControlFlow.TryCatchStatement tc =
                    (ArkTSControlFlow.TryCatchStatement) stmt;
            int count = countVarUsageInBlock(
                    tc.getTryBlock(), varName);
            count += countVarUsageInBlock(
                    tc.getCatchBlock(), varName);
            count += countVarUsageInBlock(
                    tc.getFinallyBlock(), varName);
            return count;
        } else if (stmt
                instanceof ArkTSControlFlow
                        .MultiCatchTryCatchStatement) {
            ArkTSControlFlow.MultiCatchTryCatchStatement mc =
                    (ArkTSControlFlow.MultiCatchTryCatchStatement) stmt;
            int count = countVarUsageInBlock(
                    mc.getTryBlock(), varName);
            for (ArkTSControlFlow.MultiCatchTryCatchStatement
                    .CatchClause cc : mc.getCatchClauses()) {
                count += countVarUsageInBlock(
                        cc.getBody(), varName);
            }
            count += countVarUsageInBlock(
                    mc.getFinallyBlock(), varName);
            return count;
        } else if (stmt
                instanceof ArkTSStatement.LabeledStatement) {
            return countVarUsageInStmt(
                    ((ArkTSStatement.LabeledStatement) stmt)
                            .getStatement(),
                    varName);
        }
        return 0;
    }

    private static int countVarUsageInBlock(ArkTSStatement block,
            String varName) {
        if (block == null) {
            return 0;
        }
        List<ArkTSStatement> body = extractBodyList(block);
        if (body != null) {
            int count = 0;
            for (ArkTSStatement s : body) {
                count += countVarUsageInStmt(s, varName);
            }
            return count;
        }
        return countVarUsageInStmt(block, varName);
    }

    private static boolean hasSideEffectingInitializer(
            ArkTSStatement.VariableDeclaration decl) {
        ArkTSExpression init = decl.getInitializer();
        if (init == null) {
            return false;
        }
        return init instanceof ArkTSExpression.CallExpression
                || init instanceof ArkTSExpression.NewExpression
                || init instanceof ArkTSExpression.AssignExpression;
    }

    // --- Increment/decrement simplification ---

    /**
     * Removes statements after throw/return in block-level statement lists.
     *
     * <p>Ark bytecode often has unreachable code after unconditional control
     * flow (throw, return). This pass truncates statement lists at the first
     * throw/return in each block, and recursively processes nested blocks
     * (if/else, for, while, do-while, try/catch, switch).
     */
    static List<ArkTSStatement> removeUnreachableCode(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        if (stmts.size() == 1) {
            ArkTSStatement cleaned = removeUnreachableInStmt(stmts.get(0));
            if (cleaned == stmts.get(0)) {
                return stmts;
            }
            return List.of(cleaned);
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            result.add(removeUnreachableInStmt(stmt));
            if (isUnconditionalExit(stmt)) {
                break;
            }
        }
        return result;
    }

    private static boolean isUnconditionalExit(ArkTSStatement stmt) {
        return stmt instanceof ArkTSStatement.ThrowStatement
                || stmt instanceof ArkTSStatement.ReturnStatement;
    }

    private static ArkTSStatement removeUnreachableInStmt(
            ArkTSStatement stmt) {
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            List<ArkTSStatement> body =
                    ((ArkTSStatement.BlockStatement) stmt).getBody();
            List<ArkTSStatement> cleaned = removeUnreachableCode(body);
            if (cleaned == body) {
                return stmt;
            }
            return new ArkTSStatement.BlockStatement(cleaned);
        }
        if (stmt instanceof ArkTSControlFlow.IfStatement) {
            ArkTSControlFlow.IfStatement ifStmt =
                    (ArkTSControlFlow.IfStatement) stmt;
            ArkTSStatement thenBlock =
                    removeUnreachableInStmt(ifStmt.getThenBlock());
            ArkTSStatement elseBlock = ifStmt.getElseBlock() != null
                    ? removeUnreachableInStmt(ifStmt.getElseBlock())
                    : null;
            return new ArkTSControlFlow.IfStatement(
                    ifStmt.getCondition(), thenBlock, elseBlock);
        }
        if (stmt instanceof ArkTSControlFlow.ForStatement) {
            ArkTSControlFlow.ForStatement forStmt =
                    (ArkTSControlFlow.ForStatement) stmt;
            return new ArkTSControlFlow.ForStatement(
                    forStmt.getInit(), forStmt.getConditionExpr(),
                    forStmt.getUpdate(),
                    removeUnreachableInStmt(forStmt.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.WhileStatement) {
            ArkTSControlFlow.WhileStatement ws =
                    (ArkTSControlFlow.WhileStatement) stmt;
            return new ArkTSControlFlow.WhileStatement(
                    ws.getCondition(),
                    removeUnreachableInStmt(ws.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
            ArkTSControlFlow.DoWhileStatement dw =
                    (ArkTSControlFlow.DoWhileStatement) stmt;
            return new ArkTSControlFlow.DoWhileStatement(
                    removeUnreachableInStmt(dw.getBody()),
                    dw.getCondition());
        }
        if (stmt instanceof ArkTSControlFlow.TryCatchStatement) {
            ArkTSControlFlow.TryCatchStatement tc =
                    (ArkTSControlFlow.TryCatchStatement) stmt;
            return new ArkTSControlFlow.TryCatchStatement(
                    removeUnreachableInStmt(tc.getTryBlock()),
                    tc.getCatchParam(), tc.getCatchParamType(),
                    tc.getCatchBlock() != null
                            ? removeUnreachableInStmt(tc.getCatchBlock())
                            : null,
                    tc.getFinallyBlock() != null
                            ? removeUnreachableInStmt(tc.getFinallyBlock())
                            : null);
        }
        if (stmt instanceof ArkTSControlFlow.SwitchStatement) {
            ArkTSControlFlow.SwitchStatement sw =
                    (ArkTSControlFlow.SwitchStatement) stmt;
            List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                    new ArrayList<>();
            for (ArkTSControlFlow.SwitchStatement.SwitchCase sc :
                    sw.getCases()) {
                List<ArkTSStatement> cleanedBody =
                        removeUnreachableCode(sc.getBody());
                cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        sc.getTests(), cleanedBody));
            }
            return new ArkTSControlFlow.SwitchStatement(
                    sw.getDiscriminant(), cases,
                    sw.getDefaultBlock() != null
                            ? removeUnreachableInStmt(sw.getDefaultBlock())
                            : null);
        }
        return stmt;
    }

    /**
     * Removes return statements with values from void methods.
     * Ark bytecode always emits RETURN with the accumulator, even in void
     * methods. This strips the value from terminal return statements when
     * the method return type is void.
     */
    private static List<ArkTSStatement> filterVoidMethodReturns(
            List<ArkTSStatement> stmts, boolean isVoidMethod) {
        if (stmts.isEmpty()) {
            return stmts;
        }
        ArkTSStatement last = stmts.get(stmts.size() - 1);
        if (last instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) last).getValue();
            if (val == null) {
                // Plain return; — remove entirely
                return stmts.subList(0, stmts.size() - 1);
            }
            // In void methods, strip return undefined;
            if (isVoidMethod && isUndefinedLiteral(val)) {
                return stmts.subList(0, stmts.size() - 1);
            }
        }
        return stmts;
    }

    private static boolean isUndefinedLiteral(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        return ((ArkTSExpression.LiteralExpression) expr).getKind()
                == ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED;
    }

    /**
     * Removes if-statements with always-false literal conditions
     * (undefined, false, null, 0, NaN, ""). Keeps the else-block if
     * present. Recurses into nested blocks.
     */
    static List<ArkTSStatement> removeAlwaysFalseConditions(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            ArkTSStatement cleaned = simplifyFalseConditionStmt(stmt);
            if (cleaned != null) {
                result.add(cleaned);
            }
        }
        return result;
    }

    private static ArkTSStatement simplifyFalseConditionStmt(
            ArkTSStatement stmt) {
        if (stmt instanceof ArkTSControlFlow.IfStatement) {
            ArkTSControlFlow.IfStatement ifStmt =
                    (ArkTSControlFlow.IfStatement) stmt;
            if (isAlwaysFalsy(ifStmt.getCondition())) {
                if (ifStmt.getElseBlock() != null) {
                    return simplifyFalseConditionStmt(
                            ifStmt.getElseBlock());
                }
                return null;
            }
            return new ArkTSControlFlow.IfStatement(
                    ifStmt.getCondition(),
                    simplifyFalseConditionInBlock(
                            ifStmt.getThenBlock()),
                    ifStmt.getElseBlock() != null
                            ? simplifyFalseConditionInBlock(
                                    ifStmt.getElseBlock())
                            : null);
        }
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            List<ArkTSStatement> body =
                    ((ArkTSStatement.BlockStatement) stmt).getBody();
            List<ArkTSStatement> cleaned =
                    removeAlwaysFalseConditions(body);
            return new ArkTSStatement.BlockStatement(cleaned);
        }
        if (stmt instanceof ArkTSControlFlow.ForStatement) {
            ArkTSControlFlow.ForStatement fs =
                    (ArkTSControlFlow.ForStatement) stmt;
            return new ArkTSControlFlow.ForStatement(
                    fs.getInit(), fs.getConditionExpr(),
                    fs.getUpdate(),
                    simplifyFalseConditionInBlock(fs.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.WhileStatement) {
            ArkTSControlFlow.WhileStatement ws =
                    (ArkTSControlFlow.WhileStatement) stmt;
            return new ArkTSControlFlow.WhileStatement(
                    ws.getCondition(),
                    simplifyFalseConditionInBlock(ws.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
            ArkTSControlFlow.DoWhileStatement dw =
                    (ArkTSControlFlow.DoWhileStatement) stmt;
            return new ArkTSControlFlow.DoWhileStatement(
                    simplifyFalseConditionInBlock(dw.getBody()),
                    dw.getCondition());
        }
        if (stmt instanceof ArkTSControlFlow.TryCatchStatement) {
            ArkTSControlFlow.TryCatchStatement tc =
                    (ArkTSControlFlow.TryCatchStatement) stmt;
            return new ArkTSControlFlow.TryCatchStatement(
                    simplifyFalseConditionInBlock(tc.getTryBlock()),
                    tc.getCatchParam(), tc.getCatchParamType(),
                    tc.getCatchBlock() != null
                            ? simplifyFalseConditionInBlock(
                                    tc.getCatchBlock())
                            : null,
                    tc.getFinallyBlock() != null
                            ? simplifyFalseConditionInBlock(
                                    tc.getFinallyBlock())
                            : null);
        }
        if (stmt instanceof ArkTSControlFlow.SwitchStatement) {
            ArkTSControlFlow.SwitchStatement sw =
                    (ArkTSControlFlow.SwitchStatement) stmt;
            List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                    new ArrayList<>();
            for (ArkTSControlFlow.SwitchStatement.SwitchCase sc :
                    sw.getCases()) {
                cases.add(
                        new ArkTSControlFlow.SwitchStatement.SwitchCase(
                                sc.getTests(),
                                removeAlwaysFalseConditions(
                                        sc.getBody())));
            }
            return new ArkTSControlFlow.SwitchStatement(
                    sw.getDiscriminant(), cases,
                    sw.getDefaultBlock() != null
                            ? simplifyFalseConditionInBlock(
                                    sw.getDefaultBlock())
                            : null);
        }
        return stmt;
    }

    private static ArkTSStatement simplifyFalseConditionInBlock(
            ArkTSStatement block) {
        if (block == null) {
            return null;
        }
        ArkTSStatement cleaned = simplifyFalseConditionStmt(block);
        if (cleaned == null) {
            return new ArkTSStatement.BlockStatement(
                    Collections.emptyList());
        }
        return cleaned;
    }

    private static boolean isAlwaysFalsy(ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.LiteralExpression)) {
            return false;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) expr;
        switch (lit.getKind()) {
            case UNDEFINED:
            case NULL:
            case NAN:
                return true;
            case BOOLEAN:
                return "false".equals(lit.getValue());
            case NUMBER:
                return "0".equals(lit.getValue());
            case STRING:
                return lit.getValue().isEmpty();
            default:
                return false;
        }
    }

    static List<ArkTSStatement> eliminateDeadPropertyLoads(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.size() < 2) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < result.size() - 1; i++) {
                if (!(result.get(i)
                        instanceof ArkTSStatement.VariableDeclaration)) {
                    continue;
                }
                ArkTSStatement.VariableDeclaration decl =
                        (ArkTSStatement.VariableDeclaration) result.get(i);
                ArkTSExpression init = decl.getInitializer();
                if (!(init instanceof ArkTSExpression.MemberExpression)
                        && !(init
                                instanceof ArkTSExpression
                                        .VariableExpression)) {
                    continue;
                }
                String varName = decl.getName();
                if (isOnlyUsedAsAssignTarget(result, i, varName)) {
                    result.remove(i);
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    private static boolean isOnlyUsedAsAssignTarget(
            List<ArkTSStatement> stmts, int declIdx, String varName) {
        boolean hasAssignment = false;
        for (int i = declIdx + 1; i < stmts.size(); i++) {
            ArkTSStatement stmt = stmts.get(i);
            if (stmt instanceof ArkTSStatement.ExpressionStatement) {
                ArkTSExpression expr =
                        ((ArkTSStatement.ExpressionStatement) stmt)
                                .getExpression();
                if (expr instanceof ArkTSExpression.AssignExpression) {
                    ArkTSExpression.AssignExpression assign =
                            (ArkTSExpression.AssignExpression) expr;
                    if (isVariableTarget(assign.getTarget(), varName)) {
                        hasAssignment = true;
                        continue;
                    }
                }
                if (ExpressionVisitor.countVariableReadUsage(expr, varName)
                        > 0) {
                    return false;
                }
            } else if (stmt instanceof ArkTSStatement.ReturnStatement
                    || stmt instanceof ArkTSStatement.ThrowStatement) {
                if (countVarReadUsageInStmt(stmt, varName) > 0) {
                    return false;
                }
            } else if (countVarUsageInStmt(stmt, varName) > 0) {
                return false;
            }
        }
        return hasAssignment;
    }

    private static boolean isVariableTarget(ArkTSExpression target,
            String varName) {
        if (target instanceof ArkTSExpression.VariableExpression) {
            return ((ArkTSExpression.VariableExpression) target)
                    .getName().equals(varName);
        }
        return false;
    }

    /**
     * Simplifies negated comparisons: {@code !(a === b)} becomes
     * {@code a !== b}, {@code !(a < b)} becomes {@code a >= b}, etc.
     */
    static List<ArkTSStatement> simplifyNegatedComparisons(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            result.add(simplifyNegatedInStmt(stmt));
        }
        return result;
    }

    private static final Map<String, String> NEGATED_COMPARISON_OPS =
            Map.ofEntries(
                    Map.entry("===", "!=="),
                    Map.entry("!==", "==="),
                    Map.entry("==", "!="),
                    Map.entry("!=", "=="),
                    Map.entry("<", ">="),
                    Map.entry("<=", ">"),
                    Map.entry(">", "<="),
                    Map.entry(">=", "<"));

    private static ArkTSExpression simplifyNegatedExpr(
            ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            ArkTSExpression.UnaryExpression unary =
                    (ArkTSExpression.UnaryExpression) expr;
            if ("!".equals(unary.getOperator())
                    && unary.getOperand()
                            instanceof ArkTSExpression.BinaryExpression) {
                ArkTSExpression.BinaryExpression bin =
                        (ArkTSExpression.BinaryExpression) unary.getOperand();
                String negatedOp =
                        NEGATED_COMPARISON_OPS.get(bin.getOperator());
                if (negatedOp != null) {
                    return new ArkTSExpression.BinaryExpression(
                            bin.getLeft(), negatedOp, bin.getRight());
                }
            }
        }
        return expr;
    }

    private static ArkTSStatement simplifyNegatedInStmt(
            ArkTSStatement stmt) {
        if (stmt instanceof ArkTSControlFlow.IfStatement) {
            ArkTSControlFlow.IfStatement ifStmt =
                    (ArkTSControlFlow.IfStatement) stmt;
            return new ArkTSControlFlow.IfStatement(
                    simplifyNegatedExpr(ifStmt.getCondition()),
                    simplifyNegatedInStmt(ifStmt.getThenBlock()),
                    ifStmt.getElseBlock() != null
                            ? simplifyNegatedInStmt(
                                    ifStmt.getElseBlock())
                            : null);
        }
        if (stmt instanceof ArkTSControlFlow.WhileStatement) {
            ArkTSControlFlow.WhileStatement whileStmt =
                    (ArkTSControlFlow.WhileStatement) stmt;
            return new ArkTSControlFlow.WhileStatement(
                    simplifyNegatedExpr(whileStmt.getCondition()),
                    simplifyNegatedInStmt(whileStmt.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
            ArkTSControlFlow.DoWhileStatement doWhileStmt =
                    (ArkTSControlFlow.DoWhileStatement) stmt;
            return new ArkTSControlFlow.DoWhileStatement(
                    simplifyNegatedInStmt(doWhileStmt.getBody()),
                    simplifyNegatedExpr(doWhileStmt.getCondition()));
        }
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            ArkTSStatement.BlockStatement block =
                    (ArkTSStatement.BlockStatement) stmt;
            return new ArkTSStatement.BlockStatement(
                    simplifyNegatedInStmts(block.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.ForOfStatement) {
            ArkTSControlFlow.ForOfStatement forOf =
                    (ArkTSControlFlow.ForOfStatement) stmt;
            return new ArkTSControlFlow.ForOfStatement(
                    forOf.getVariableKind(), forOf.getVariableName(),
                    forOf.getIterable(),
                    simplifyNegatedInStmt(forOf.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.SwitchStatement) {
            ArkTSControlFlow.SwitchStatement switchStmt =
                    (ArkTSControlFlow.SwitchStatement) stmt;
            List<ArkTSControlFlow.SwitchStatement.SwitchCase> cases =
                    new ArrayList<>();
            for (ArkTSControlFlow.SwitchStatement.SwitchCase sc :
                    switchStmt.getCases()) {
                cases.add(new ArkTSControlFlow.SwitchStatement.SwitchCase(
                        sc.getTest(),
                        simplifyNegatedInStmts(sc.getBody())));
            }
            return new ArkTSControlFlow.SwitchStatement(
                    switchStmt.getDiscriminant(), cases,
                    switchStmt.getDefaultBlock());
        }
        return stmt;
    }

    private static List<ArkTSStatement> simplifyNegatedInStmts(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement s : stmts) {
            result.add(simplifyNegatedInStmt(s));
        }
        return result;
    }

    /**
     * Converts {@code x += 1} to {@code x++} and {@code x -= 1} to
     * {@code x--} when used as ExpressionStatement.
     */
    static List<ArkTSStatement> simplifyIncrementDecrement(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            result.add(simplifyIncrementInStmt(stmt));
        }
        return result;
    }

    private static ArkTSStatement simplifyIncrementInStmt(
            ArkTSStatement stmt) {
        if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            ArkTSExpression expr =
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression();
            ArkTSExpression simplified = tryIncrementDecrement(expr);
            if (simplified != null) {
                return new ArkTSStatement.ExpressionStatement(simplified);
            }
        }
        if (stmt instanceof ArkTSControlFlow.IfStatement) {
            ArkTSControlFlow.IfStatement ifStmt =
                    (ArkTSControlFlow.IfStatement) stmt;
            return new ArkTSControlFlow.IfStatement(
                    ifStmt.getCondition(),
                    simplifyIncrementInBlock(ifStmt.getThenBlock()),
                    ifStmt.getElseBlock() != null
                            ? simplifyIncrementInBlock(
                                    ifStmt.getElseBlock())
                            : null);
        }
        if (stmt instanceof ArkTSControlFlow.WhileStatement) {
            ArkTSControlFlow.WhileStatement ws =
                    (ArkTSControlFlow.WhileStatement) stmt;
            return new ArkTSControlFlow.WhileStatement(
                    ws.getCondition(),
                    simplifyIncrementInBlock(ws.getBody()));
        }
        if (stmt instanceof ArkTSControlFlow.DoWhileStatement) {
            ArkTSControlFlow.DoWhileStatement dw =
                    (ArkTSControlFlow.DoWhileStatement) stmt;
            return new ArkTSControlFlow.DoWhileStatement(
                    dw.getBody(),
                    dw.getCondition());
        }
        if (stmt instanceof ArkTSControlFlow.ForStatement) {
            // Cannot rebuild ForStatement (no public getters for init/
            // condition/update) — just return as-is, increment patterns
            // in for-update are already correct
            return stmt;
        }
        if (stmt instanceof ArkTSControlFlow.ForOfStatement) {
            ArkTSControlFlow.ForOfStatement fo =
                    (ArkTSControlFlow.ForOfStatement) stmt;
            return new ArkTSControlFlow.ForOfStatement(
                    fo.getVariableKind(), fo.getVariableName(),
                    fo.getIterable(),
                    simplifyIncrementInBlock(fo.getBody()));
        }
        if (stmt instanceof ArkTSStatement.BlockStatement) {
            List<ArkTSStatement> body =
                    ((ArkTSStatement.BlockStatement) stmt).getBody();
            List<ArkTSStatement> newBody =
                    new ArrayList<>(body.size());
            for (ArkTSStatement s : body) {
                newBody.add(simplifyIncrementInStmt(s));
            }
            return new ArkTSStatement.BlockStatement(newBody);
        }
        return stmt;
    }

    private static ArkTSStatement simplifyIncrementInBlock(
            ArkTSStatement block) {
        if (block == null) {
            return null;
        }
        if (block instanceof ArkTSStatement.BlockStatement) {
            return simplifyIncrementInStmt(block);
        }
        return simplifyIncrementInStmt(block);
    }

    private static ArkTSExpression tryIncrementDecrement(
            ArkTSExpression expr) {
        if (!(expr instanceof ArkTSExpression.CompoundAssignExpression)) {
            return null;
        }
        ArkTSExpression.CompoundAssignExpression compound =
                (ArkTSExpression.CompoundAssignExpression) expr;
        if (!"+=".equals(compound.getOperator())
                && !"-=".equals(compound.getOperator())) {
            return null;
        }
        ArkTSExpression value = compound.getValue();
        if (!(value instanceof ArkTSExpression.LiteralExpression)) {
            return null;
        }
        ArkTSExpression.LiteralExpression lit =
                (ArkTSExpression.LiteralExpression) value;
        if (lit.getKind()
                != ArkTSExpression.LiteralExpression.LiteralKind.NUMBER) {
            return null;
        }
        boolean isIncrement = "+=".equals(compound.getOperator());
        if ("1".equals(lit.getValue())) {
            return new ArkTSExpression.IncrementExpression(
                    compound.getTarget(), false, isIncrement);
        }
        return null;
    }

    private static String extractUninitVarDecl(ArkTSStatement stmt) {
        if (!(stmt instanceof ArkTSStatement.VariableDeclaration)) {
            return null;
        }
        ArkTSStatement.VariableDeclaration decl =
                (ArkTSStatement.VariableDeclaration) stmt;
        if (decl.getInitializer() != null) {
            return null;
        }
        return decl.getName();
    }

    private static ArkTSAccessExpressions.SwitchExpression
            tryConvertToSwitchExpression(
                    ArkTSControlFlow.SwitchStatement switchStmt,
                    String targetVar) {
        List<ArkTSAccessExpressions.SwitchExpression.SwitchExprCase>
                exprCases = new ArrayList<>();
        ArkTSExpression defaultExpr = null;

        for (ArkTSControlFlow.SwitchStatement.SwitchCase caze
                : switchStmt.getCases()) {
            ArkTSExpression caseValue =
                    extractSingleAssignment(caze.getBody(), targetVar);
            if (caseValue == null) {
                return null;
            }
            exprCases.add(
                    new ArkTSAccessExpressions.SwitchExpression
                            .SwitchExprCase(caze.getTests(), caseValue));
        }

        if (switchStmt.getDefaultBlock() != null) {
            List<ArkTSStatement> defaultBody =
                    extractBodyList(switchStmt.getDefaultBlock());
            defaultExpr = extractSingleAssignment(defaultBody, targetVar);
            if (defaultExpr == null) {
                return null;
            }
        }

        return new ArkTSAccessExpressions.SwitchExpression(
                switchStmt.getDiscriminant(), exprCases, defaultExpr);
    }

    /**
     * Extracts the assigned value when the body is a single assignment
     * to targetVar followed by optional break.
     */
    private static ArkTSExpression extractSingleAssignment(
            List<ArkTSStatement> body, String targetVar) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        // Filter out break statements
        List<ArkTSStatement> nonBreak = new ArrayList<>();
        for (ArkTSStatement s : body) {
            if (!(s instanceof ArkTSStatement.BreakStatement)) {
                nonBreak.add(s);
            }
        }
        if (nonBreak.size() != 1) {
            return null;
        }
        ArkTSStatement stmt = nonBreak.get(0);
        if (!(stmt instanceof ArkTSStatement.ExpressionStatement)) {
            return null;
        }
        ArkTSExpression expr =
                ((ArkTSStatement.ExpressionStatement) stmt)
                        .getExpression();
        if (!(expr instanceof ArkTSExpression.AssignExpression)) {
            return null;
        }
        ArkTSExpression.AssignExpression assign =
                (ArkTSExpression.AssignExpression) expr;
        if (!(assign.getTarget()
                instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        String name = ((ArkTSExpression.VariableExpression) assign
                .getTarget()).getName();
        if (!name.equals(targetVar)) {
            return null;
        }
        return assign.getValue();
    }

    // --- Nested if-condition merging ---

    /**
     * Merges nested if-only statements into a single if with &amp;&amp;
     * conditions.
     *
     * <p>Transforms:
     * <pre>
     * if (a) {
     *   if (b) {
     *     stmt
     *   }
     * }
     * </pre>
     * Into:
     * <pre>
     * if (a && b) {
     *   stmt
     * }
     * </pre>
     *
     * @param stmts the statement list to transform
     * @return the transformed list
     */
    static List<ArkTSStatement> mergeNestedIfConditions(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts.size());
        for (ArkTSStatement stmt : stmts) {
            result.add(mergeIfChain(stmt));
        }
        return result;
    }

    private static ArkTSStatement mergeIfChain(ArkTSStatement stmt) {
        if (!(stmt instanceof ArkTSControlFlow.IfStatement)) {
            return stmt;
        }
        ArkTSControlFlow.IfStatement ifStmt =
                (ArkTSControlFlow.IfStatement) stmt;

        // Only merge when there's no else block
        if (ifStmt.getElseBlock() != null) {
            return stmt;
        }

        // Check if then-block contains a single if-only statement
        ArkTSStatement thenBlock = ifStmt.getThenBlock();
        ArkTSControlFlow.IfStatement innerIf =
                extractSingleIfOnly(thenBlock);
        if (innerIf == null) {
            return stmt;
        }

        // Recursively merge deeper nesting in the inner if
        ArkTSStatement innerMerged = mergeIfChain(innerIf);
        ArkTSExpression innerCond =
                innerMerged instanceof ArkTSControlFlow.IfStatement
                        ? ((ArkTSControlFlow.IfStatement) innerMerged)
                                .getCondition()
                        : innerIf.getCondition();
        ArkTSStatement innerBody =
                innerMerged instanceof ArkTSControlFlow.IfStatement
                        ? ((ArkTSControlFlow.IfStatement) innerMerged)
                                .getThenBlock()
                        : innerIf.getThenBlock();

        // Build merged condition: outer && inner
        ArkTSExpression mergedCondition =
                new ArkTSExpression.BinaryExpression(
                        ifStmt.getCondition(), "&&", innerCond);

        return new ArkTSControlFlow.IfStatement(
                mergedCondition, innerBody, null);
    }

    /**
     * Extracts a single if-only statement from a block, or null if
     * the block contains anything other than exactly one if statement
     * with no else block.
     */
    private static ArkTSControlFlow.IfStatement extractSingleIfOnly(
            ArkTSStatement block) {
        List<ArkTSStatement> bodyList = extractBodyList(block);
        if (bodyList == null || bodyList.size() != 1) {
            return null;
        }
        ArkTSStatement inner = bodyList.get(0);
        if (!(inner instanceof ArkTSControlFlow.IfStatement)) {
            return null;
        }
        ArkTSControlFlow.IfStatement innerIf =
                (ArkTSControlFlow.IfStatement) inner;
        if (innerIf.getElseBlock() != null) {
            return null;
        }
        return innerIf;
    }

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

    // --- Timeout support ---

    private boolean isTimedOut(long startTimeNs) {
        if (methodTimeoutMs <= 0) {
            return false;
        }
        long elapsedMs =
                (System.nanoTime() - startTimeNs) / 1_000_000L;
        return elapsedMs > methodTimeoutMs;
    }

    private String buildTimeoutMethod(AbcMethod method, AbcCode code,
            AbcFile abcFile, long startTimeNs) {
        long elapsedMs =
                (System.nanoTime() - startTimeNs) / 1_000_000L;
        AbcProto proto = resolveProto(method, abcFile);
        List<ArkTSStatement> bodyStmts = List.of(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.VariableExpression(
                                "/* decompilation timed out after "
                                        + elapsedMs + "ms */")));
        return buildMethodSource(method, proto, code, bodyStmts,
                abcFile);
    }

    private List<ArkTSStatement> buildTimeoutBody(long startTimeNs) {
        long elapsedMs =
                (System.nanoTime() - startTimeNs) / 1_000_000L;
        return List.of(new ArkTSStatement.ExpressionStatement(
                new ArkTSExpression.VariableExpression(
                        "/* decompilation timed out after "
                                + elapsedMs + "ms */")));
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
                        DeclarationBuilder.sanitizeMethodName(
                                method.getName()),
                        params, returnType, body);
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

    // --- Return type inference ---

    private static final String UNKNOWN_TYPE = "__unknown__";

    /**
     * Infers the return type of a method by analyzing return statements in the
     * body. Walks the statement tree recursively, collecting the types of
     * values returned by each {@code ReturnStatement}.
     *
     * <p>Inference rules:
     * <ul>
     *   <li>All returns are void (or no returns) -> "void"</li>
     *   <li>All returns are the same literal type (number/string/boolean)
     *       -> that type</li>
     *   <li>Mixed types or non-literal returns -> null (don't annotate)</li>
     * </ul>
     *
     * @param stmts the method body statements
     * @return the inferred return type, or null if uncertain
     */
    static String inferReturnType(List<ArkTSStatement> stmts) {
        Set<String> types = new HashSet<>();
        collectReturnTypes(stmts, types);
        if (types.isEmpty()) {
            return "void";
        }
        if (types.contains(UNKNOWN_TYPE)) {
            return null;
        }
        if (types.size() == 1) {
            return types.iterator().next();
        }
        return null;
    }

    private static void collectReturnTypes(List<ArkTSStatement> stmts,
            Set<String> types) {
        if (stmts == null) {
            return;
        }
        for (ArkTSStatement stmt : stmts) {
            if (stmt instanceof ArkTSStatement.ReturnStatement) {
                ArkTSExpression val =
                        ((ArkTSStatement.ReturnStatement) stmt).getValue();
                String rt = classifyReturnValue(val);
                if (rt != null) {
                    types.add(rt);
                }
            } else if (stmt instanceof ArkTSControlFlow.IfStatement) {
                ArkTSControlFlow.IfStatement ifStmt =
                        (ArkTSControlFlow.IfStatement) stmt;
                collectReturnTypesFromStmt(
                        ifStmt.getThenBlock(), types);
                collectReturnTypesFromStmt(
                        ifStmt.getElseBlock(), types);
            } else if (stmt
                    instanceof ArkTSControlFlow.WhileStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.WhileStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.DoWhileStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.DoWhileStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.ForStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForOfStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.ForOfStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForInStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.ForInStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.ForAwaitOfStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSControlFlow.ForAwaitOfStatement) stmt)
                                .getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSControlFlow.SwitchStatement) {
                ArkTSControlFlow.SwitchStatement sw =
                        (ArkTSControlFlow.SwitchStatement) stmt;
                for (ArkTSControlFlow.SwitchStatement.SwitchCase sc
                        : sw.getCases()) {
                    collectReturnTypes(sc.getBody(), types);
                }
                collectReturnTypesFromStmt(
                        sw.getDefaultBlock(), types);
            } else if (stmt
                    instanceof ArkTSControlFlow.TryCatchStatement) {
                ArkTSControlFlow.TryCatchStatement tc =
                        (ArkTSControlFlow.TryCatchStatement) stmt;
                collectReturnTypesFromStmt(
                        tc.getTryBlock(), types);
                collectReturnTypesFromStmt(
                        tc.getCatchBlock(), types);
                collectReturnTypesFromStmt(
                        tc.getFinallyBlock(), types);
            } else if (stmt instanceof ArkTSControlFlow
                    .MultiCatchTryCatchStatement) {
                ArkTSControlFlow.MultiCatchTryCatchStatement mc =
                        (ArkTSControlFlow.MultiCatchTryCatchStatement) stmt;
                collectReturnTypesFromStmt(
                        mc.getTryBlock(), types);
                for (ArkTSControlFlow.MultiCatchTryCatchStatement
                        .CatchClause cc : mc.getCatchClauses()) {
                    collectReturnTypesFromStmt(cc.getBody(), types);
                }
                collectReturnTypesFromStmt(
                        mc.getFinallyBlock(), types);
            } else if (stmt
                    instanceof ArkTSStatement.BlockStatement) {
                collectReturnTypes(
                        ((ArkTSStatement.BlockStatement) stmt).getBody(),
                        types);
            } else if (stmt
                    instanceof ArkTSStatement.LabeledStatement) {
                collectReturnTypesFromStmt(
                        ((ArkTSStatement.LabeledStatement) stmt)
                                .getStatement(),
                        types);
            }
        }
    }

    private static void collectReturnTypesFromStmt(ArkTSStatement block,
            Set<String> types) {
        if (block == null) {
            return;
        }
        if (block instanceof ArkTSStatement.BlockStatement) {
            collectReturnTypes(
                    ((ArkTSStatement.BlockStatement) block).getBody(),
                    types);
        } else {
            collectReturnTypes(List.of(block), types);
        }
    }

    /**
     * Classifies a return value expression into a type string.
     * Returns {@link #UNKNOWN_TYPE} for expressions whose type
     * cannot be reliably determined (variables, complex expressions).
     */
    private static String classifyReturnValue(ArkTSExpression val) {
        if (val == null) {
            return "void";
        }
        if (val instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression lit =
                    (ArkTSExpression.LiteralExpression) val;
            switch (lit.getKind()) {
                case NUMBER:
                case NAN:
                case INFINITY:
                    return "number";
                case STRING:
                    return "string";
                case BOOLEAN:
                    return "boolean";
                case NULL:
                    return "null";
                case UNDEFINED:
                    return "void";
                default:
                    return UNKNOWN_TYPE;
            }
        }
        return UNKNOWN_TYPE;
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
        List<ArkTSDeclarations.FunctionDeclaration.FunctionParam> params =
                MethodSignatureBuilder.buildParams(proto,
                        code.getNumArgs(),
                        getDebugParamNames(method, abcFile),
                        restParamIndex);
        String returnType = MethodSignatureBuilder.getReturnType(proto);
        boolean isVoidMethod = "void".equals(returnType);
        // When proto-based type is void (no proto or void shorty),
        // try to infer a more specific return type from the body
        if (isVoidMethod) {
            String inferred =
                    inferReturnType(bodyStmts);
            if (inferred != null && !"void".equals(inferred)) {
                returnType = inferred;
                isVoidMethod = false;
            }
        }
        List<ArkTSStatement> filteredStmts =
                filterVoidMethodReturns(bodyStmts, isVoidMethod);
        ArkTSStatement body =
                new ArkTSStatement.BlockStatement(filteredStmts);
        ArkTSDeclarations.FunctionDeclaration func =
                new ArkTSDeclarations.FunctionDeclaration(
                        DeclarationBuilder.sanitizeMethodName(
                                method.getName()),
                        params, returnType, body, isAsync);
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
     * Populates the source line number map in the decompilation context
     * from debug info. Maps bytecode offsets to source line numbers.
     */
    private static void populateLineNumbers(AbcMethod method,
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
        for (int i = 0; i < lnpIndices.size(); i++) {
            Long lnpIdx = lnpIndices.get(i);
            if (lnpIdx == null || lnpIdx <= 0) {
                continue;
            }
            List<com.arkghidra.format.AbcLineNumberEntry> entries =
                    abcFile.getLineNumberEntries(i);
            for (com.arkghidra.format.AbcLineNumberEntry entry : entries) {
                ctx.setLineNumber((int) entry.getPc(), entry.getLine());
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
        // Use O(1) method lookup + classIdx to find the owning class
        AbcMethod method = abcFile.getMethodByFlatIndex(methodIdx);
        if (method != null) {
            int classIdx = method.getClassIdx();
            List<AbcClass> classes = abcFile.getClasses();
            if (classIdx >= 0 && classIdx < classes.size()) {
                AbcClass cls = classes.get(classIdx);
                return DeclarationBuilder.sanitizeClassName(cls.getName());
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
