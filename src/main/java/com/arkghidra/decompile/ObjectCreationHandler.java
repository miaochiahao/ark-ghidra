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

        String varName = "v" + lastReg;
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
        List<ArkTSExpression> parts = new ArrayList<>();
        flattenAddChain(accValue, parts);
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
        parts.add(expr);
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

    ArkTSStatement tryDetectArrayDestructuring(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars,
            List<ArkTSStatement> stmts) {

        List<String> bindings = new ArrayList<>();
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

            if (opcode == ArkOpcodesCompat.LDA
                    && scanIdx + 2 < instructions.size()) {
                ArkInstruction nextInsn = instructions.get(scanIdx + 1);
                ArkInstruction afterNext = instructions.get(scanIdx + 2);

                if (nextInsn.getOpcode() == ArkOpcodesCompat.LDOBJBYINDEX
                        && afterNext.getOpcode()
                                == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0)
                            .getValue();
                    String currentSource = "v" + srcReg;
                    List<ArkOperand> ldOps = nextInsn.getOperands();
                    int index = (int) ldOps.get(
                            ldOps.size() - 1).getValue();
                    int targetReg = (int) afterNext.getOperands()
                            .get(0).getValue();
                    String targetVar = "v" + targetReg;

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)
                            && index == consecutiveIdx) {
                        bindings.add(targetVar);
                        declaredVars.add(targetVar);
                        consecutiveIdx++;
                        scanIdx += 3;
                        continue;
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
                            bindings, restBinding, source);
            return new ArkTSTypeDeclarations.DestructuringDeclaration(
                    "const", destrExpr);
        }
        return null;
    }

    // --- Object destructuring detection ---

    ArkTSStatement tryDetectObjectDestructuring(
            List<ArkInstruction> instructions, int startIndex,
            DecompilationContext ctx, Set<String> declaredVars,
            List<ArkTSStatement> stmts) {

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

                if (nextInsn.getOpcode()
                        == ArkOpcodesCompat.LDOBJBYNAME
                        && afterNext.getOpcode()
                                == ArkOpcodesCompat.STA) {
                    int srcReg = (int) insn.getOperands().get(0)
                            .getValue();
                    String currentSource = "v" + srcReg;
                    String propName = ctx.resolveString(
                            (int) nextInsn.getOperands().get(1)
                                    .getValue());
                    int targetReg = (int) afterNext.getOperands()
                            .get(0).getValue();
                    String targetVar = "v" + targetReg;

                    if (sourceVar == null) {
                        sourceVar = currentSource;
                    }

                    if (currentSource.equals(sourceVar)) {
                        bindings.add(
                                new ArkTSPropertyExpressions
                                        .ObjectDestructuringExpression
                                        .DestructuringBinding(
                                                propName, null));
                        declaredVars.add(targetVar);
                        propertyCount++;
                        scanIdx += 3;
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
            return new ArkTSTypeDeclarations.DestructuringDeclaration(
                    "const", destrExpr);
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
        if (isNullEqualityCheck(condition)) {
            ArkTSExpression checkedValue =
                    getNullCheckTarget(condition);
            if (checkedValue != null && expressionsMatch(checkedValue,
                    falseValue)) {
                return new ArkTSPropertyExpressions
                        .NullishCoalescingExpression(
                        falseValue, trueValue);
            }
        }
        return null;
    }

    private boolean isNullEqualityCheck(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            return "===".equals(bin.getOperator())
                    || "==".equals(bin.getOperator());
        }
        return false;
    }

    private ArkTSExpression getNullCheckTarget(
            ArkTSExpression condition) {
        if (!(condition instanceof ArkTSExpression.BinaryExpression)) {
            return null;
        }
        ArkTSExpression.BinaryExpression bin =
                (ArkTSExpression.BinaryExpression) condition;
        ArkTSExpression left = bin.getLeft();
        ArkTSExpression right = bin.getRight();
        if (isNullLiteralExpr(right)) {
            return left;
        }
        if (isNullLiteralExpr(left)) {
            return right;
        }
        return null;
    }

    private boolean isNullLiteralExpr(ArkTSExpression expr) {
        if (expr instanceof ArkTSExpression.LiteralExpression) {
            return ((ArkTSExpression.LiteralExpression) expr).getKind()
                    == ArkTSExpression.LiteralExpression.LiteralKind.NULL;
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
}
