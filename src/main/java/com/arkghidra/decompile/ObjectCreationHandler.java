package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Handles object/array creation, field/property definition, template literal
 * reconstruction, destructuring detection, and nullish coalescing detection
 * during ArkTS decompilation.
 */
class ObjectCreationHandler {

    private final ArkTSDecompiler decompiler;

    ObjectCreationHandler(ArkTSDecompiler decompiler) {
        this.decompiler = decompiler;
    }

    // --- Placeholder element creation ---

    private static List<ArkTSExpression> createPlaceholderElements(
            int count) {
        List<ArkTSExpression> elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elements.add(new ArkTSExpression.LiteralExpression(
                    "/* element_" + i + " */",
                    ArkTSExpression.LiteralExpression
                            .LiteralKind.STRING));
        }
        return elements;
    }

    // --- Define class with buffer processing ---

    InstructionHandler.StatementResult processDefineClassWithBuffer(
            ArkInstruction insn, DecompilationContext ctx) {
        List<ArkOperand> operands = insn.getOperands();
        int methodIdx = (int) operands.get(0).getValue();
        int literalIdx = (int) operands.get(1).getValue();
        int lastReg = (int) operands.get(
                operands.size() - 1).getValue();

        String className = "class_" + methodIdx;
        if (ctx.abcFile != null) {
            className = decompiler.resolveClassNameFromMethod(
                    methodIdx, ctx.abcFile);
        }

        ArkTSExpression classExpr =
                new ArkTSExpression.VariableExpression(className);

        // Track the class expression in the register so that later
        // newobjrange can resolve the class name from the register.
        ctx.setRegisterExpression(lastReg, classExpr);

        String varName = ctx.resolveRegisterName(lastReg);
        ArkTSStatement stmt = new ArkTSStatement.VariableDeclaration(
                "let", varName, className, classExpr);

        return new InstructionHandler.StatementResult(stmt, classExpr);
    }

    // --- Template literal reconstruction ---

    ArkTSExpression tryReconstructTemplateLiteral(
            ArkTSExpression accValue) {
        if (accValue == null) {
            return null;
        }
        if (accValue instanceof ArkTSAccessExpressions
                .TemplateLiteralExpression) {
            return accValue;
        }
        List<ArkTSExpression> parts = new ArrayList<>();
        flattenAddChain(accValue, parts);
        if (parts.size() < 2) {
            return accValue;
        }
        List<String> quasis = new ArrayList<>();
        List<ArkTSExpression> expressions = new ArrayList<>();
        buildTemplateFromParts(parts, quasis, expressions);
        boolean hasString = false;
        for (ArkTSExpression part : parts) {
            if (isStringLiteralExpr(part)) {
                hasString = true;
                break;
            }
        }
        if (hasString && quasis.size() == expressions.size() + 1
                && !quasis.isEmpty()) {
            return new ArkTSAccessExpressions.TemplateLiteralExpression(
                    quasis, expressions);
        }
        return accValue;
    }

    private void flattenAddChain(ArkTSExpression expr,
            List<ArkTSExpression> parts) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            if ("+".equals(bin.getOperator())) {
                flattenAddChain(bin.getLeft(), parts);
                flattenAddChain(bin.getRight(), parts);
                return;
            }
        }
        if (expr instanceof ArkTSAccessExpressions
                .TemplateLiteralExpression) {
            flattenTemplateLiteral(
                    (ArkTSAccessExpressions.TemplateLiteralExpression) expr,
                    parts);
            return;
        }
        parts.add(expr);
    }

    private void flattenTemplateLiteral(
            ArkTSAccessExpressions.TemplateLiteralExpression tmpl,
            List<ArkTSExpression> parts) {
        List<String> quasis = tmpl.getQuasis();
        List<ArkTSExpression> exprs = tmpl.getExpressions();
        for (int i = 0; i < quasis.size(); i++) {
            String quasi = quasis.get(i);
            parts.add(new ArkTSExpression.LiteralExpression(quasi,
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING));
            if (i < exprs.size()) {
                parts.add(exprs.get(i));
            }
        }
    }

    private void buildTemplateFromParts(List<ArkTSExpression> parts,
            List<String> quasis, List<ArkTSExpression> expressions) {
        StringBuilder currentQuasi = new StringBuilder();
        for (ArkTSExpression part : parts) {
            if (isStringLiteralExpr(part)) {
                currentQuasi.append(extractStringValue(part));
            } else {
                quasis.add(currentQuasi.toString());
                currentQuasi = new StringBuilder();
                expressions.add(part);
            }
        }
        quasis.add(currentQuasi.toString());
    }

    private boolean isStringLiteralExpr(ArkTSExpression expr) {
        return expr instanceof ArkTSExpression.LiteralExpression
                && ((ArkTSExpression.LiteralExpression) expr).getKind()
                        == ArkTSExpression.LiteralExpression.LiteralKind.STRING;
    }

    private String extractStringValue(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return ((ArkTSExpression.LiteralExpression) expr).getValue();
        }
        return "";
    }

    // --- Array destructuring detection ---

    /**
     * Result holder for destructuring detection, containing the number
     * of instructions consumed and the resulting statement.
     */
    static class DestructuringResult {
        final int instructionsConsumed;
        final ArkTSStatement statement;

        DestructuringResult(int consumed, ArkTSStatement statement) {
            this.instructionsConsumed = consumed;
            this.statement = statement;
        }
    }

    /**
     * Attempts to detect an array destructuring pattern starting at
     * the given instruction index within a basic block.
     *
     * <p>Pattern: lda srcReg; ldobjbyindex imm, idx; sta dstReg
     * repeated with consecutive indices from the same source register.
     * After all positional bindings, a rest element may follow via
     * createarraywithbuffer/starrayspread or similar patterns.
     *
     * @param instructions the instruction list for the block
     * @param startIndex the index to start scanning from
     * @param ctx the decompilation context
     * @param declaredVars the set of already-declared variables
     * @return a DestructuringResult if pattern found, null otherwise
     */
    DestructuringResult tryDetectArrayDestructuringInBlock(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars) {

        List<ArkTSPropertyExpressions.ArrayDestructuringExpression
                .ArrayBinding> bindings = new ArrayList<>();
        String restBinding = null;
        String sourceVar = null;
        int consecutiveIdx = 0;
        int scanIdx = startIndex;

        while (scanIdx < instructions.size()) {
            ArkInstruction insn = instructions.get(scanIdx);
            int opcode = insn.getOpcode();

            if (opcode == ArkOpcodesCompat.NOP) {
                scanIdx++;
                continue;
            }

            // Main pattern: lda src; ldobjbyindex imm, idx; sta dst
            if (opcode == ArkOpcodesCompat.LDA
                    && scanIdx + 2 < instructions.size()) {
                ArkInstruction nextInsn = instructions.get(scanIdx + 1);
                ArkInstruction afterNext = instructions.get(scanIdx + 2);

                if (ArkOpcodesCompat.getNormalizedOpcode(nextInsn)
                        == ArkOpcodesCompat.LDOBJBYINDEX
                        && afterNext.getOpcode()
                                == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0)
                            .getValue();
                    String currentSource =
                            ctx.resolveRegisterName(srcReg);
                    List<ArkOperand> ldOps = nextInsn.getOperands();
                    int index = (int) ldOps.get(
                            ldOps.size() - 1).getValue();
                    int targetReg = (int) afterNext.getOperands()
                            .get(0).getValue();
                    String targetVar =
                            ctx.resolveRegisterName(targetReg);

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)
                            && index == consecutiveIdx) {
                        // Check for default value pattern after binding
                        ArkTSExpression[] defaultOut =
                                new ArkTSExpression[1];
                        int defaultConsumed = tryDetectDefaultValue(
                                instructions, scanIdx + 3,
                                targetReg, defaultOut, ctx);

                        if (defaultOut[0] != null) {
                            bindings.add(
                                    new ArkTSPropertyExpressions
                                            .ArrayDestructuringExpression
                                            .ArrayBinding(targetVar,
                                                    defaultOut[0]));
                        } else {
                            bindings.add(
                                    new ArkTSPropertyExpressions
                                            .ArrayDestructuringExpression
                                            .ArrayBinding(targetVar));
                        }
                        declaredVars.add(targetVar);
                        consecutiveIdx++;
                        scanIdx += 3 + defaultConsumed;
                        continue;
                    }
                }

                // Rest element pattern: after positional bindings,
                // look for spread/slice pattern into a rest variable.
                if (bindings.size() >= 1 && sourceVar != null
                        && restBinding == null) {
                    int restResult = tryDetectRestElement(
                            instructions, scanIdx, sourceVar,
                            consecutiveIdx);
                    if (restResult >= 0) {
                        String restVar = findRestTarget(
                                instructions, scanIdx, restResult, ctx);
                        if (restVar != null) {
                            restBinding = restVar;
                            declaredVars.add(restVar);
                            scanIdx += restResult;
                            continue;
                        }
                    }
                }
            }
            break;
        }

        if (bindings.size() >= 2 && sourceVar != null) {
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression(sourceVar);
            ArkTSExpression destrExpr =
                    new ArkTSPropertyExpressions
                            .ArrayDestructuringExpression(
                            bindings, restBinding, source, true);
            ArkTSStatement stmt =
                    new ArkTSTypeDeclarations.DestructuringDeclaration(
                            "const", destrExpr);
            return new DestructuringResult(
                    scanIdx - startIndex, stmt);
        }
        return null;
    }

    /**
     * Attempts to detect a rest element pattern after positional
     * array destructuring bindings.
     *
     * <p>Looks for patterns where a spread or slice operation
     * captures the remaining elements after positional bindings.
     *
     * @return number of instructions consumed, or -1 if not detected
     */
    private int tryDetectRestElement(
            List<ArkInstruction> instructions, int scanIdx,
            String sourceVar, int startIdx) {
        if (scanIdx + 2 < instructions.size()) {
            ArkInstruction insn = instructions.get(scanIdx);
            ArkInstruction nextInsn = instructions.get(scanIdx + 1);
            ArkInstruction afterNext = instructions.get(scanIdx + 2);

            // Pattern: lda srcReg; some_spread_op; sta restReg
            if (insn.getOpcode() == ArkOpcodesCompat.LDA
                    && afterNext.getOpcode() == ArkOpcodesCompat.STA) {
                int midOpcode = nextInsn.getOpcode();
                if (midOpcode == ArkOpcodesCompat.STARRAYSPREAD
                        || midOpcode
                                == ArkOpcodesCompat.CREATEARRAYWITHBUFFER
                        || midOpcode
                                == ArkOpcodesCompat.CREATEEMPTYARRAY) {
                    return 3;
                }
            }
        }
        return -1;
    }

    /**
     * Finds the target variable from the last STA in a rest pattern.
     */
    private String findRestTarget(List<ArkInstruction> instructions,
            int startIdx, int count, DecompilationContext ctx) {
        for (int i = startIdx; i < startIdx + count
                && i < instructions.size(); i++) {
            ArkInstruction insn = instructions.get(i);
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                int reg = (int) insn.getOperands().get(0).getValue();
                return ctx.resolveRegisterName(reg);
            }
        }
        return null;
    }

    // --- Default value detection in destructuring ---

    /**
     * Attempts to detect a default value pattern after a destructuring
     * binding's STA instruction.
     *
     * <p>Pattern in Ark bytecode:
     * <pre>
     *   lda targetReg          // load just-destructured value
     *   jstrictequndefined +N  // if NOT undefined, skip default
     *   ldai/lda/fldai value   // load default value
     *   sta targetReg          // store default into same register
     * </pre>
     *
     * <p>Or using explicit undefined comparison:
     * <pre>
     *   lda targetReg; ldundefined; stricteq 0
     *   jeqz +N; ldai value; sta targetReg
     * </pre>
     *
     * @param instructions the instruction list
     * @param scanIdx the index right after the binding's STA
     * @param targetReg the register that received the binding
     * @return number of instructions consumed for the default, or 0
     */
    private int tryDetectDefaultValue(List<ArkInstruction> instructions,
            int scanIdx, int targetReg,
            ArkTSExpression[] defaultOut, DecompilationContext ctx) {

        if (scanIdx + 3 >= instructions.size()) {
            return 0;
        }

        ArkInstruction insn = instructions.get(scanIdx);
        int opcode = insn.getOpcode();

        // Pattern 1: lda targetReg; jstrictequndefined +N;
        //            ldai/lda/fldai/lda.str value; sta targetReg
        if (opcode == ArkOpcodesCompat.LDA) {
            int reg = (int) insn.getOperands().get(0).getValue();
            if (reg != targetReg) {
                return 0;
            }

            ArkInstruction branchInsn = instructions.get(scanIdx + 1);
            int branchOpcode = branchInsn.getOpcode();

            if (ArkOpcodesCompat.isConditionalBranch(branchOpcode)) {
                // Check if branch is checking for undefined/null
                if (isUndefinedCheckBranch(branchOpcode)) {
                    // Skip over the branch, look for default value + STA
                    int valueIdx = scanIdx + 2;
                    ArkTSExpression defaultExpr =
                            extractLoadExpression(instructions, valueIdx,
                                    ctx);
                    if (defaultExpr != null) {
                        // Find the STA to same target reg
                        int staIdx = findStaAfterLoad(
                                instructions, valueIdx, targetReg);
                        if (staIdx > valueIdx) {
                            defaultOut[0] = defaultExpr;
                            return (staIdx + 1) - scanIdx;
                        }
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Checks if a branch opcode is checking for undefined or null.
     * Matches both "jump if equal" and "jump if not equal" variants
     * since the compiler may use either to implement default values.
     */
    private static boolean isUndefinedCheckBranch(int branchOpcode) {
        return branchOpcode == ArkOpcodesCompat.JSTRICTEQUNDEFINED_IMM16
                || branchOpcode == ArkOpcodesCompat.JNSTRICTEQUNDEFINED_IMM16
                || branchOpcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM8
                || branchOpcode == ArkOpcodesCompat.JSTRICTEQNULL_IMM16
                || branchOpcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM8
                || branchOpcode == ArkOpcodesCompat.JNSTRICTEQNULL_IMM16;
    }

    /**
     * Extracts a load expression from an instruction at the given index.
     * Supports ldai, fldai, lda.str, lda, ldtrue, ldfalse, ldnull,
     * ldundefined, ldnan, ldinfinity.
     */
    private static ArkTSExpression extractLoadExpression(
            List<ArkInstruction> instructions, int idx,
            DecompilationContext ctx) {
        if (idx >= instructions.size()) {
            return null;
        }
        ArkInstruction insn = instructions.get(idx);
        int opcode = insn.getOpcode();

        if (opcode == ArkOpcodesCompat.LDAI) {
            return new ArkTSExpression.LiteralExpression(
                    String.valueOf(insn.getOperands().get(0).getValue()),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        }
        if (opcode == ArkOpcodesCompat.FLDAI) {
            double val = Double.longBitsToDouble(
                    insn.getOperands().get(0).getValue());
            return new ArkTSExpression.LiteralExpression(
                    String.valueOf(val),
                    ArkTSExpression.LiteralExpression.LiteralKind.NUMBER);
        }
        if (opcode == ArkOpcodesCompat.LDA_STR) {
            return new ArkTSExpression.LiteralExpression(
                    "str_" + insn.getOperands().get(0).getValue(),
                    ArkTSExpression.LiteralExpression.LiteralKind.STRING);
        }
        if (opcode == ArkOpcodesCompat.LDTRUE) {
            return new ArkTSExpression.LiteralExpression("true",
                    ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN);
        }
        if (opcode == ArkOpcodesCompat.LDFALSE) {
            return new ArkTSExpression.LiteralExpression("false",
                    ArkTSExpression.LiteralExpression.LiteralKind.BOOLEAN);
        }
        if (opcode == ArkOpcodesCompat.LDNULL) {
            return new ArkTSExpression.LiteralExpression("null",
                    ArkTSExpression.LiteralExpression.LiteralKind.NULL);
        }
        if (opcode == ArkOpcodesCompat.LDUNDEFINED) {
            return new ArkTSExpression.LiteralExpression("undefined",
                    ArkTSExpression.LiteralExpression.LiteralKind.UNDEFINED);
        }
        if (opcode == ArkOpcodesCompat.LDA) {
            int reg = (int) insn.getOperands().get(0).getValue();
            return new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(reg));
        }
        return null;
    }

    /**
     * Finds a STA instruction targeting the specified register after a
     * load instruction at valueIdx.
     */
    private static int findStaAfterLoad(List<ArkInstruction> instructions,
            int valueIdx, int targetReg) {
        for (int i = valueIdx + 1; i < instructions.size()
                && i <= valueIdx + 2; i++) {
            ArkInstruction insn = instructions.get(i);
            if (insn.getOpcode() == ArkOpcodesCompat.STA) {
                int reg = (int) insn.getOperands().get(0).getValue();
                if (reg == targetReg) {
                    return i;
                }
            }
        }
        return -1;
    }

    // --- Object destructuring detection ---

    /**
     * Attempts to detect an object destructuring pattern starting at
     * the given instruction index.
     *
     * <p>Pattern: lda srcReg; ldobjbyname imm, stringIdx; sta dstReg
     * repeated from the same source register. Supports renamed
     * properties (when property name != target variable) and default
     * values (conditional assignment after the binding).
     *
     * @param instructions the instruction list for the block
     * @param startIndex the index to start scanning from
     * @param ctx the decompilation context
     * @param declaredVars the set of already-declared variables
     * @return a DestructuringResult if pattern found, null otherwise
     */
    DestructuringResult tryDetectObjectDestructuringInBlock(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars) {

        List<ArkTSPropertyExpressions.ObjectDestructuringExpression
                .DestructuringBinding> bindings = new ArrayList<>();
        String sourceVar = null;
        int scanIdx = startIndex;
        int propertyCount = 0;

        while (scanIdx < instructions.size()) {
            ArkInstruction insn = instructions.get(scanIdx);
            int opcode = insn.getOpcode();

            if (opcode == ArkOpcodesCompat.NOP) {
                scanIdx++;
                continue;
            }

            if (opcode == ArkOpcodesCompat.LDA
                    && scanIdx + 2 < instructions.size()) {
                ArkInstruction nextInsn = instructions.get(scanIdx + 1);
                ArkInstruction afterNext = instructions.get(scanIdx + 2);

                if (ArkOpcodesCompat.getNormalizedOpcode(nextInsn)
                        == ArkOpcodesCompat.LDOBJBYNAME
                        && afterNext.getOpcode()
                                == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0)
                            .getValue();
                    String currentSource =
                            ctx.resolveRegisterName(srcReg);
                    String propName = ctx.resolveString(
                            (int) nextInsn.getOperands().get(1)
                                    .getValue());
                    int targetReg = (int) afterNext.getOperands()
                            .get(0).getValue();
                    String targetVar =
                            ctx.resolveRegisterName(targetReg);

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)) {
                        // Determine if this is a rename
                        // (prop: alias) vs shorthand (prop).
                        // If the target register is not a simple vN
                        // that matches propName, use alias syntax.
                        String alias = null;
                        // The resolved property name from string table
                        // becomes the property. The target register
                        // name is the alias if different.
                        if (!targetVar.equals(propName)) {
                            alias = targetVar;
                        }

                        // Check for default value pattern after binding
                        ArkTSExpression[] defaultOut =
                                new ArkTSExpression[1];
                        int defaultConsumed = tryDetectDefaultValue(
                                instructions, scanIdx + 3,
                                targetReg, defaultOut, ctx);

                        bindings.add(
                                new ArkTSPropertyExpressions
                                        .ObjectDestructuringExpression
                                        .DestructuringBinding(
                                                propName, alias,
                                                defaultOut[0]));
                        declaredVars.add(targetVar);
                        propertyCount++;
                        scanIdx += 3 + defaultConsumed;
                        continue;
                    }
                }
            }
            break;
        }

        if (propertyCount >= 2 && sourceVar != null) {
            ArkTSExpression source =
                    new ArkTSExpression.VariableExpression(sourceVar);
            ArkTSExpression destrExpr =
                    new ArkTSPropertyExpressions
                            .ObjectDestructuringExpression(
                            bindings, source);
            ArkTSStatement stmt =
                    new ArkTSTypeDeclarations.DestructuringDeclaration(
                            "const", destrExpr);
            return new DestructuringResult(
                    scanIdx - startIndex, stmt);
        }
        return null;
    }

    // --- Nullish coalescing detection ---

    ArkTSExpression tryDetectNullishCoalescing(
            ArkTSExpression condition,
            ArkTSExpression trueValue,
            ArkTSExpression falseValue) {
        if (condition == null || trueValue == null
                || falseValue == null) {
            return null;
        }
        // Pattern 1: x === null ? y : x  (equality check)
        //   condition = x === null, trueValue = y, falseValue = x
        if (isNullOrUndefinedEqualityCheck(condition)) {
            ArkTSExpression checkedValue =
                    getNullOrUndefinedCheckTarget(condition);
            if (checkedValue != null && expressionsMatch(checkedValue,
                    falseValue)) {
                return new ArkTSPropertyExpressions
                        .NullishCoalescingExpression(
                        falseValue, trueValue);
            }
        }
        // Pattern 2: x !== null ? x : y  (inequality check)
        //   condition = x !== null, trueValue = x, falseValue = y
        if (isNullOrUndefinedInequalityCheck(condition)) {
            ArkTSExpression checkedValue =
                    getNullOrUndefinedCheckTarget(condition);
            if (checkedValue != null && expressionsMatch(checkedValue,
                    trueValue)) {
                return new ArkTSPropertyExpressions
                        .NullishCoalescingExpression(
                        trueValue, falseValue);
            }
        }
        return null;
    }

    private boolean isNullOrUndefinedEqualityCheck(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            String op = bin.getOperator();
            if (!("===".equals(op) || "==".equals(op))) {
                return false;
            }
            return isNullOrUndefinedLiteral(bin.getLeft())
                    || isNullOrUndefinedLiteral(bin.getRight());
        }
        return false;
    }

    private boolean isNullOrUndefinedInequalityCheck(
            ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            String op = bin.getOperator();
            if (!("!==".equals(op) || "!=".equals(op))) {
                return false;
            }
            return isNullOrUndefinedLiteral(bin.getLeft())
                    || isNullOrUndefinedLiteral(bin.getRight());
        }
        return false;
    }

    private ArkTSExpression getNullOrUndefinedCheckTarget(
            ArkTSExpression condition) {
        if (!(condition instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) condition;
        ArkTSExpression left = bin.getLeft();
        ArkTSExpression right = bin.getRight();
        if (isNullOrUndefinedLiteral(right)) {
            return left;
        }
        if (isNullOrUndefinedLiteral(left)) {
            return right;
        }
        return null;
    }

    private boolean isNullOrUndefinedLiteral(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            ArkTSExpression.LiteralExpression.LiteralKind kind =
                    ((ArkTSExpression.LiteralExpression) expr).getKind();
            return kind == ArkTSExpression.LiteralExpression
                    .LiteralKind.NULL
                    || kind == ArkTSExpression.LiteralExpression
                            .LiteralKind.UNDEFINED;
        }
        return false;
    }

    private boolean expressionsMatch(ArkTSExpression a,
            ArkTSExpression b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.toArkTS().equals(b.toArkTS());
    }

    // --- Prototype-related patterns ---

    /**
     * Builds an Object.setPrototypeOf() call expression.
     *
     * <p>Used when {@code setobjectwithproto} sets the prototype of
     * an object to a value in the accumulator.
     *
     * @param objExpr the object whose prototype is being set
     * @param protoExpr the prototype expression
     * @return the call expression for Object.setPrototypeOf(obj, proto)
     */
    static ArkTSExpression buildSetPrototypeOfCall(
            ArkTSExpression objExpr, ArkTSExpression protoExpr) {
        ArkTSExpression setPrototypeOf =
                new ArkTSExpression.MemberExpression(
                        new ArkTSExpression.VariableExpression("Object"),
                        new ArkTSExpression.VariableExpression(
                                "setPrototypeOf"),
                        false);
        return new ArkTSExpression.CallExpression(
                setPrototypeOf, List.of(objExpr, protoExpr));
    }

    /**
     * Builds a __proto__ assignment expression.
     *
     * <p>Alternative form for setting prototype:
     * {@code obj.__proto__ = proto}.
     *
     * @param objExpr the object whose prototype is being set
     * @param protoExpr the prototype expression
     * @return the assignment expression
     */
    static ArkTSExpression buildProtoAssignment(
            ArkTSExpression objExpr, ArkTSExpression protoExpr) {
        ArkTSExpression protoMember =
                new ArkTSExpression.MemberExpression(
                        objExpr,
                        new ArkTSExpression.VariableExpression("__proto__"),
                        false);
        return new ArkTSExpression.AssignExpression(protoMember, protoExpr);
    }

    /**
     * Creates an object literal with a __proto__ property.
     *
     * <p>When an object is created with CREATEOBJECTWITHBUFFER and
     * immediately followed by SETOBJECTWITHPROTO, the resulting
     * pattern is an object literal with a __proto__ property.
     *
     * @param properties the object properties
     * @param protoExpr the prototype expression (may be null)
     * @return the object literal expression
     */
    static ArkTSExpression createObjectWithPrototype(
            List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty>
                    properties,
            ArkTSExpression protoExpr) {
        List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty>
                allProps = new ArrayList<>();
        if (protoExpr != null) {
            allProps.add(
                    new ArkTSAccessExpressions.ObjectLiteralExpression
                            .ObjectProperty("__proto__", protoExpr));
        }
        allProps.addAll(properties);
        return new ArkTSAccessExpressions.ObjectLiteralExpression(allProps);
    }
}
