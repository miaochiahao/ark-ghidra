package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.List;

/**
 * Expression tree visitor utilities for variable analysis and replacement.
 * Handles counting variable usage, replacing variable references,
 * and inlining single-use variables in statement lists.
 */
class ExpressionVisitor {

    private ExpressionVisitor() {
    }

    // --- Single-use variable inlining ---

    /**
     * Inlines single-use variables at the top level.
     *
     * <p>Runs multiple passes to enable cascading inlining. Handles
     * return, throw, and simple expression statement targets.
     *
     * @param stmts the statement list (may be modified)
     * @return the optimized statement list
     */
    static List<ArkTSStatement> inlineSingleUseVariables(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.size() < 2) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts);
        boolean changed = true;
        int passes = 0;
        while (changed && passes < 3) {
            changed = false;
            passes++;
            for (int i = result.size() - 2; i >= 0; i--) {
                if (!(result.get(i)
                        instanceof ArkTSStatement.VariableDeclaration)) {
                    continue;
                }
                ArkTSStatement.VariableDeclaration varDecl =
                        (ArkTSStatement.VariableDeclaration) result.get(i);
                ArkTSExpression init = varDecl.getInitializer();
                if (init == null) {
                    continue;
                }
                String varName = varDecl.getName();
                if (countVariableUsage(init, varName) > 0) {
                    continue;
                }
                if (isUsedInEarlierStatements(result, i, varName)) {
                    continue;
                }
                ArkTSStatement next = result.get(i + 1);
                ArkTSStatement inlined =
                        tryInlineInto(varName, init, next);
                if (inlined != null) {
                    result.set(i + 1, inlined);
                    result.remove(i);
                    changed = true;
                }
            }
        }
        return result;
    }

    /**
     * Eliminates redundant simple variable-to-variable copy assignments.
     *
     * <p>Patterns like {@code v0 = v4; v1 = v5; v2 = v6;} are common
     * parameter register copies in Ark bytecode. This method replaces all
     * subsequent uses of the destination variable with the source variable
     * and removes the copy statement.
     *
     * <p>Only handles simple {@code dst = src} where both are plain
     * variable references (no property access, no complex expressions).
     *
     * @param stmts the statement list
     * @return the optimized statement list
     */
    static List<ArkTSStatement> eliminateRedundantCopies(
            List<ArkTSStatement> stmts) {
        if (stmts == null || stmts.size() < 2) {
            return stmts;
        }
        List<ArkTSStatement> result = new ArrayList<>(stmts);
        boolean changed = true;
        int passes = 0;
        while (changed && passes < 5) {
            changed = false;
            passes++;
            for (int i = 0; i < result.size(); i++) {
                String[] copyPair = extractSimpleCopy(result.get(i));
                if (copyPair == null) {
                    continue;
                }
                String dst = copyPair[0];
                String src = copyPair[1];
                // Don't eliminate copies where dst is re-assigned later
                if (isReassignedLater(result, i, dst)) {
                    continue;
                }
                // Replace all uses of dst with src in subsequent statements
                for (int j = i + 1; j < result.size(); j++) {
                    ArkTSStatement replaced =
                            replaceVariableInStmt(result.get(j), dst, src);
                    if (replaced != null) {
                        result.set(j, replaced);
                    }
                }
                result.remove(i);
                changed = true;
                break;
            }
        }
        return result;
    }

    /**
     * Extracts the dst/src names from a simple copy assignment
     * ({@code dst = src}). Returns null if not a simple copy.
     * Only matches local variable copies (v0, v1, etc. pattern).
     */
    private static String[] extractSimpleCopy(ArkTSStatement stmt) {
        if (!(stmt instanceof ArkTSStatement.ExpressionStatement)) {
            return null;
        }
        ArkTSExpression expr =
                ((ArkTSStatement.ExpressionStatement) stmt).getExpression();
        if (!(expr instanceof ArkTSExpression.AssignExpression)) {
            return null;
        }
        ArkTSExpression.AssignExpression assign =
                (ArkTSExpression.AssignExpression) expr;
        if (!(assign.getTarget()
                instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        if (!(assign.getValue()
                instanceof ArkTSExpression.VariableExpression)) {
            return null;
        }
        String dst = ((ArkTSExpression.VariableExpression) assign.getTarget())
                .getName();
        String src = ((ArkTSExpression.VariableExpression) assign.getValue())
                .getName();
        if (dst.equals(src)) {
            return null;
        }
        // Only eliminate copies between local variables (v0, v1, etc.)
        if (!isLocalVariable(dst) || !isLocalVariable(src)) {
            return null;
        }
        return new String[]{dst, src};
    }

    private static boolean isLocalVariable(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (!name.startsWith("v")) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return name.length() > 1;
    }

    /**
     * Checks if a variable is reassigned in any statement after index i.
     */
    private static boolean isReassignedLater(List<ArkTSStatement> stmts,
            int afterIndex, String varName) {
        for (int i = afterIndex + 1; i < stmts.size(); i++) {
            if (isAssignmentTarget(stmts.get(i), varName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if varName appears as the target of an assignment.
     */
    private static boolean isAssignmentTarget(ArkTSStatement stmt,
            String varName) {
        if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            ArkTSExpression expr =
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression();
            if (expr instanceof ArkTSExpression.AssignExpression) {
                ArkTSExpression target =
                        ((ArkTSExpression.AssignExpression) expr).getTarget();
                return countVariableUsage(target, varName) > 0;
            }
        }
        if (stmt instanceof ArkTSStatement.VariableDeclaration) {
            ArkTSStatement.VariableDeclaration decl =
                    (ArkTSStatement.VariableDeclaration) stmt;
            return decl.getName().equals(varName);
        }
        return false;
    }

    /**
     * Replaces a variable reference in a statement (non-assignment LHS).
     * Returns the modified statement or null if unchanged.
     */
    private static ArkTSStatement replaceVariableInStmt(ArkTSStatement stmt,
            String varName, String replacement) {
        ArkTSExpression replExpr =
                new ArkTSExpression.VariableExpression(replacement);
        if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            ArkTSExpression expr =
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression();
            if (countVariableUsage(expr, varName) == 0) {
                return null;
            }
            ArkTSExpression replaced =
                    replaceVariable(expr, varName, replExpr);
            if (replaced != null) {
                return new ArkTSStatement.ExpressionStatement(replaced);
            }
        }
        if (stmt instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) stmt).getValue();
            if (val != null && countVariableUsage(val, varName) > 0) {
                ArkTSExpression replaced =
                        replaceVariable(val, varName, replExpr);
                if (replaced != null) {
                    return new ArkTSStatement.ReturnStatement(replaced);
                }
            }
        }
        if (stmt instanceof ArkTSStatement.ThrowStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ThrowStatement) stmt).getValue();
            if (val != null && countVariableUsage(val, varName) > 0) {
                ArkTSExpression replaced =
                        replaceVariable(val, varName, replExpr);
                if (replaced != null) {
                    return new ArkTSStatement.ThrowStatement(replaced);
                }
            }
        }
        return null;
    }

    /**
     * Tries to inline a variable's initializer into the next statement.
     * Handles return, throw, and expression statements.
     *
     * @return the replacement statement, or null if inlining not possible
     */
    private static ArkTSStatement tryInlineInto(String varName,
            ArkTSExpression init, ArkTSStatement target) {
        if (target instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) target).getValue();
            if (isSingleVarRef(val, varName)) {
                return new ArkTSStatement.ReturnStatement(init);
            }
        }
        if (target instanceof ArkTSStatement.ThrowStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ThrowStatement) target).getValue();
            if (isSingleVarRef(val, varName)) {
                return new ArkTSStatement.ThrowStatement(init);
            }
        }
        if (target instanceof ArkTSStatement.ExpressionStatement) {
            ArkTSExpression expr =
                    ((ArkTSStatement.ExpressionStatement) target)
                            .getExpression();
            if (countVariableUsage(expr, varName) == 1) {
                ArkTSExpression replaced =
                        replaceVariable(expr, varName, init);
                if (replaced != null) {
                    return new ArkTSStatement.ExpressionStatement(replaced);
                }
            }
        }
        return null;
    }

    private static boolean isSingleVarRef(ArkTSExpression expr,
            String varName) {
        return expr instanceof ArkTSExpression.VariableExpression
                && varName.equals(
                        ((ArkTSExpression.VariableExpression) expr).getName());
    }

    private static boolean isUsedInEarlierStatements(
            List<ArkTSStatement> stmts, int beforeIndex, String varName) {
        for (int j = 0; j < beforeIndex; j++) {
            if (stmtReferencesVariable(stmts.get(j), varName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces all occurrences of a variable with an expression.
     * Returns null if replacement is not safe for the expression type.
     * Package-private for testing.
     */
    public static ArkTSExpression replaceVariable(ArkTSExpression expr,
            String varName, ArkTSExpression replacement) {
        return replaceVariableImpl(expr, varName, replacement);
    }

    private static ArkTSExpression replaceVariableImpl(ArkTSExpression expr,
            String varName, ArkTSExpression replacement) {
        if (expr instanceof ArkTSExpression.VariableExpression) {
            if (varName.equals(
                    ((ArkTSExpression.VariableExpression) expr).getName())) {
                return replacement;
            }
            return expr;
        }
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            ArkTSExpression left =
                    replaceVariableImpl(bin.getLeft(), varName, replacement);
            ArkTSExpression right =
                    replaceVariableImpl(bin.getRight(), varName, replacement);
            if (left == null || right == null) {
                return null;
            }
            return new ArkTSExpression.BinaryExpression(
                    left, bin.getOperator(), right);
        }
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            ArkTSExpression.UnaryExpression un =
                    (ArkTSExpression.UnaryExpression) expr;
            ArkTSExpression operand =
                    replaceVariableImpl(un.getOperand(), varName, replacement);
            if (operand == null) {
                return null;
            }
            return new ArkTSExpression.UnaryExpression(
                    un.getOperator(), operand, un.isPrefix());
        }
        if (expr instanceof ArkTSExpression.CallExpression) {
            ArkTSExpression.CallExpression call =
                    (ArkTSExpression.CallExpression) expr;
            ArkTSExpression callee =
                    replaceVariableImpl(call.getCallee(), varName, replacement);
            if (callee == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression arg : call.getArguments()) {
                ArkTSExpression replaced =
                        replaceVariableImpl(arg, varName, replacement);
                if (replaced == null) {
                    return null;
                }
                newArgs.add(replaced);
            }
            return new ArkTSExpression.CallExpression(callee, newArgs);
        }
        if (expr instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression mem =
                    (ArkTSExpression.MemberExpression) expr;
            ArkTSExpression obj =
                    replaceVariableImpl(mem.getObject(), varName, replacement);
            if (obj == null) {
                return null;
            }
            return new ArkTSExpression.MemberExpression(
                    obj, mem.getProperty(), mem.isComputed());
        }
        if (expr instanceof ArkTSExpression.AssignExpression) {
            ArkTSExpression.AssignExpression assign =
                    (ArkTSExpression.AssignExpression) expr;
            // Don't replace in the assignment target (left-hand side)
            if (isSingleVarRef(assign.getTarget(), varName)) {
                return null;
            }
            ArkTSExpression target = replaceVariableImpl(
                    assign.getTarget(), varName, replacement);
            ArkTSExpression value =
                    replaceVariableImpl(assign.getValue(), varName, replacement);
            if (target == null || value == null) {
                return null;
            }
            return new ArkTSExpression.AssignExpression(target, value);
        }
        // NewExpression: replace in callee and arguments
        if (expr instanceof ArkTSExpression.NewExpression) {
            ArkTSExpression.NewExpression ne =
                    (ArkTSExpression.NewExpression) expr;
            ArkTSExpression callee =
                    replaceVariableImpl(ne.getCallee(), varName, replacement);
            if (callee == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression arg : ne.getArguments()) {
                ArkTSExpression replaced =
                        replaceVariableImpl(arg, varName, replacement);
                if (replaced == null) {
                    return null;
                }
                newArgs.add(replaced);
            }
            return new ArkTSExpression.NewExpression(callee, newArgs);
        }
        // ConditionalExpression (ternary): replace in all branches
        if (expr instanceof ArkTSAccessExpressions.ConditionalExpression) {
            ArkTSAccessExpressions.ConditionalExpression cond =
                    (ArkTSAccessExpressions.ConditionalExpression) expr;
            ArkTSExpression test =
                    replaceVariableImpl(cond.getTest(), varName, replacement);
            ArkTSExpression cons =
                    replaceVariableImpl(cond.getConsequent(), varName,
                            replacement);
            ArkTSExpression alt =
                    replaceVariableImpl(cond.getAlternate(), varName,
                            replacement);
            if (test == null || cons == null || alt == null) {
                return null;
            }
            return new ArkTSAccessExpressions.ConditionalExpression(
                    test, cons, alt);
        }
        // SwitchExpression: replace in discriminant, cases, and default
        if (expr instanceof ArkTSAccessExpressions.SwitchExpression) {
            ArkTSAccessExpressions.SwitchExpression se =
                    (ArkTSAccessExpressions.SwitchExpression) expr;
            ArkTSExpression disc =
                    replaceVariableImpl(se.getDiscriminant(), varName,
                            replacement);
            if (disc == null) {
                return null;
            }
            List<ArkTSAccessExpressions.SwitchExpression.SwitchExprCase>
                    newCases = new ArrayList<>();
            for (ArkTSAccessExpressions.SwitchExpression.SwitchExprCase c
                    : se.getCases()) {
                List<ArkTSExpression> newTests = new ArrayList<>();
                for (ArkTSExpression t : c.getTests()) {
                    ArkTSExpression nt =
                            replaceVariableImpl(t, varName, replacement);
                    if (nt == null) {
                        return null;
                    }
                    newTests.add(nt);
                }
                ArkTSExpression nv =
                        replaceVariableImpl(c.getValue(), varName,
                                replacement);
                if (nv == null) {
                    return null;
                }
                newCases.add(
                        new ArkTSAccessExpressions.SwitchExpression
                                .SwitchExprCase(newTests, nv));
            }
            ArkTSExpression newDefault = se.getDefaultValue() != null
                    ? replaceVariableImpl(se.getDefaultValue(), varName,
                            replacement)
                    : null;
            if (se.getDefaultValue() != null && newDefault == null) {
                return null;
            }
            return new ArkTSAccessExpressions.SwitchExpression(
                    disc, newCases, newDefault);
        }
        // CompoundAssignExpression: replace in target and value
        if (expr instanceof ArkTSExpression.CompoundAssignExpression) {
            ArkTSExpression.CompoundAssignExpression ca =
                    (ArkTSExpression.CompoundAssignExpression) expr;
            if (isSingleVarRef(ca.getTarget(), varName)) {
                return null;
            }
            ArkTSExpression t =
                    replaceVariableImpl(ca.getTarget(), varName, replacement);
            ArkTSExpression v =
                    replaceVariableImpl(ca.getValue(), varName, replacement);
            if (t == null || v == null) {
                return null;
            }
            return new ArkTSExpression.CompoundAssignExpression(
                    t, ca.getOperator(), v);
        }
        // LogicalAssignExpression: replace in target and value
        if (expr instanceof ArkTSExpression.LogicalAssignExpression) {
            ArkTSExpression.LogicalAssignExpression la =
                    (ArkTSExpression.LogicalAssignExpression) expr;
            if (isSingleVarRef(la.getTarget(), varName)) {
                return null;
            }
            ArkTSExpression t =
                    replaceVariableImpl(la.getTarget(), varName, replacement);
            ArkTSExpression v =
                    replaceVariableImpl(la.getValue(), varName, replacement);
            if (t == null || v == null) {
                return null;
            }
            return new ArkTSExpression.LogicalAssignExpression(
                    t, la.getOperator(), v);
        }
        // IncrementExpression: replace in target
        if (expr instanceof ArkTSExpression.IncrementExpression) {
            ArkTSExpression.IncrementExpression inc =
                    (ArkTSExpression.IncrementExpression) expr;
            if (isSingleVarRef(inc.getTarget(), varName)) {
                return null;
            }
            ArkTSExpression t =
                    replaceVariableImpl(inc.getTarget(), varName, replacement);
            if (t == null) {
                return null;
            }
            return new ArkTSExpression.IncrementExpression(
                    t, inc.isPrefix(), inc.isIncrement());
        }
        // Await: replace in argument
        if (expr instanceof ArkTSAccessExpressions.AwaitExpression) {
            ArkTSExpression arg = replaceVariableImpl(
                    ((ArkTSAccessExpressions.AwaitExpression) expr)
                            .getArgument(), varName, replacement);
            if (arg == null) {
                return null;
            }
            return new ArkTSAccessExpressions.AwaitExpression(arg);
        }
        // Yield: replace in argument
        if (expr instanceof ArkTSAccessExpressions.YieldExpression) {
            ArkTSAccessExpressions.YieldExpression yield =
                    (ArkTSAccessExpressions.YieldExpression) expr;
            ArkTSExpression yArg = yield.getArgument();
            if (yArg == null) {
                return expr;
            }
            ArkTSExpression replaced =
                    replaceVariableImpl(yArg, varName, replacement);
            if (replaced == null) {
                return null;
            }
            return new ArkTSAccessExpressions.YieldExpression(
                    replaced, yield.isDelegate());
        }
        // Spread: replace in argument
        if (expr instanceof ArkTSAccessExpressions.SpreadExpression) {
            ArkTSExpression arg = replaceVariableImpl(
                    ((ArkTSAccessExpressions.SpreadExpression) expr)
                            .getArgument(), varName, replacement);
            if (arg == null) {
                return null;
            }
            return new ArkTSAccessExpressions.SpreadExpression(arg);
        }
        // SpreadCall: replace in callee and args
        if (expr instanceof ArkTSAccessExpressions.SpreadCallExpression) {
            ArkTSAccessExpressions.SpreadCallExpression sc =
                    (ArkTSAccessExpressions.SpreadCallExpression) expr;
            ArkTSExpression callee =
                    replaceVariableImpl(sc.getCallee(), varName, replacement);
            if (callee == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a : sc.getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.SpreadCallExpression(
                    callee, newArgs);
        }
        // SpreadArray: replace in elements
        if (expr instanceof ArkTSAccessExpressions.SpreadArrayExpression) {
            List<ArkTSExpression> newElems = new ArrayList<>();
            for (ArkTSExpression e :
                    ((ArkTSAccessExpressions.SpreadArrayExpression) expr)
                            .getElements()) {
                ArkTSExpression r =
                        replaceVariableImpl(e, varName, replacement);
                if (r == null) {
                    return null;
                }
                newElems.add(r);
            }
            return new ArkTSAccessExpressions.SpreadArrayExpression(
                    newElems);
        }
        // SpreadObject: replace in properties
        if (expr instanceof ArkTSAccessExpressions.SpreadObjectExpression) {
            List<ArkTSExpression> newProps = new ArrayList<>();
            for (ArkTSExpression p :
                    ((ArkTSAccessExpressions.SpreadObjectExpression) expr)
                            .getProperties()) {
                ArkTSExpression r =
                        replaceVariableImpl(p, varName, replacement);
                if (r == null) {
                    return null;
                }
                newProps.add(r);
            }
            return new ArkTSAccessExpressions.SpreadObjectExpression(
                    newProps);
        }
        // TemplateLiteral: replace in expressions
        if (expr instanceof ArkTSAccessExpressions.TemplateLiteralExpression) {
            ArkTSAccessExpressions.TemplateLiteralExpression tl =
                    (ArkTSAccessExpressions.TemplateLiteralExpression) expr;
            List<ArkTSExpression> newExprs = new ArrayList<>();
            for (ArkTSExpression e : tl.getExpressions()) {
                ArkTSExpression r =
                        replaceVariableImpl(e, varName, replacement);
                if (r == null) {
                    return null;
                }
                newExprs.add(r);
            }
            return new ArkTSAccessExpressions.TemplateLiteralExpression(
                    tl.getQuasis(), newExprs);
        }
        // ArrayLiteral: replace in elements
        if (expr instanceof ArkTSAccessExpressions.ArrayLiteralExpression) {
            List<ArkTSExpression> newElems = new ArrayList<>();
            for (ArkTSExpression e :
                    ((ArkTSAccessExpressions.ArrayLiteralExpression) expr)
                            .getElements()) {
                ArkTSExpression r =
                        replaceVariableImpl(e, varName, replacement);
                if (r == null) {
                    return null;
                }
                newElems.add(r);
            }
            return new ArkTSAccessExpressions.ArrayLiteralExpression(
                    newElems);
        }
        // ObjectLiteral: replace in property values
        if (expr instanceof ArkTSAccessExpressions.ObjectLiteralExpression) {
            ArkTSAccessExpressions.ObjectLiteralExpression ol =
                    (ArkTSAccessExpressions.ObjectLiteralExpression) expr;
            List<ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty>
                    newProps = new ArrayList<>();
            for (ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty
                    p : ol.getProperties()) {
                ArkTSExpression val =
                        replaceVariableImpl(p.getValue(), varName, replacement);
                if (val == null) {
                    return null;
                }
                if (p.isComputed()) {
                    ArkTSExpression key =
                            replaceVariableImpl(p.getComputedKey(), varName,
                                    replacement);
                    if (key == null) {
                        return null;
                    }
                    newProps.add(
                            new ArkTSAccessExpressions.ObjectLiteralExpression
                                    .ObjectProperty(key, val, true));
                } else {
                    newProps.add(
                            new ArkTSAccessExpressions.ObjectLiteralExpression
                                    .ObjectProperty(p.getKey(), val));
                }
            }
            return new ArkTSAccessExpressions.ObjectLiteralExpression(
                    newProps);
        }
        // As (type cast): replace in expression
        if (expr instanceof ArkTSAccessExpressions.AsExpression) {
            ArkTSExpression inner = replaceVariableImpl(
                    ((ArkTSAccessExpressions.AsExpression) expr)
                            .getExpression(), varName, replacement);
            if (inner == null) {
                return null;
            }
            return new ArkTSAccessExpressions.AsExpression(inner,
                    ((ArkTSAccessExpressions.AsExpression) expr)
                            .getTypeName());
        }
        // NonNull: replace in expression
        if (expr instanceof ArkTSAccessExpressions.NonNullExpression) {
            ArkTSExpression inner = replaceVariableImpl(
                    ((ArkTSAccessExpressions.NonNullExpression) expr)
                            .getExpression(), varName, replacement);
            if (inner == null) {
                return null;
            }
            return new ArkTSAccessExpressions.NonNullExpression(inner);
        }
        // OptionalChain: replace in object and property
        if (expr instanceof ArkTSAccessExpressions.OptionalChainExpression) {
            ArkTSAccessExpressions.OptionalChainExpression oc =
                    (ArkTSAccessExpressions.OptionalChainExpression) expr;
            ArkTSExpression obj =
                    replaceVariableImpl(oc.getObject(), varName, replacement);
            if (obj == null) {
                return null;
            }
            return new ArkTSAccessExpressions.OptionalChainExpression(
                    obj, oc.getProperty(), oc.isComputed());
        }
        // OptionalChainCall: replace in object, property, and args
        if (expr instanceof ArkTSAccessExpressions
                .OptionalChainCallExpression) {
            ArkTSAccessExpressions.OptionalChainCallExpression occ =
                    (ArkTSAccessExpressions.OptionalChainCallExpression) expr;
            ArkTSExpression obj =
                    replaceVariableImpl(occ.getObject(), varName, replacement);
            if (obj == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a : occ.getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.OptionalChainCallExpression(
                    obj, occ.getProperty(), occ.isComputed(), newArgs);
        }
        // BuiltInNew: replace in arguments
        if (expr instanceof ArkTSAccessExpressions.BuiltInNewExpression) {
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a :
                    ((ArkTSAccessExpressions.BuiltInNewExpression) expr)
                            .getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.BuiltInNewExpression(
                    ((ArkTSAccessExpressions.BuiltInNewExpression) expr)
                            .getClassName(), newArgs);
        }
        // --- ArkTSPropertyExpressions types ---

        // PrivateMemberExpression: replace in object
        if (expr instanceof ArkTSPropertyExpressions.PrivateMemberExpression) {
            ArkTSExpression obj = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.PrivateMemberExpression) expr)
                            .getObject(), varName, replacement);
            if (obj == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.PrivateMemberExpression(
                    obj,
                    ((ArkTSPropertyExpressions.PrivateMemberExpression) expr)
                            .getPropertyName());
        }
        // InExpression: replace in property and object
        if (expr instanceof ArkTSPropertyExpressions.InExpression) {
            ArkTSPropertyExpressions.InExpression ie =
                    (ArkTSPropertyExpressions.InExpression) expr;
            ArkTSExpression prop =
                    replaceVariableImpl(ie.getProperty(), varName, replacement);
            ArkTSExpression obj =
                    replaceVariableImpl(ie.getObject(), varName, replacement);
            if (prop == null || obj == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.InExpression(prop, obj);
        }
        // InstanceofExpression: replace in expression and targetType
        if (expr instanceof ArkTSPropertyExpressions.InstanceofExpression) {
            ArkTSPropertyExpressions.InstanceofExpression ie =
                    (ArkTSPropertyExpressions.InstanceofExpression) expr;
            ArkTSExpression e =
                    replaceVariableImpl(ie.getExpression(), varName, replacement);
            ArkTSExpression t =
                    replaceVariableImpl(ie.getTargetType(), varName, replacement);
            if (e == null || t == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.InstanceofExpression(e, t);
        }
        // DeleteExpression: replace in target
        if (expr instanceof ArkTSPropertyExpressions.DeleteExpression) {
            ArkTSExpression t = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.DeleteExpression) expr)
                            .getTarget(), varName, replacement);
            if (t == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.DeleteExpression(t);
        }
        // CopyDataPropertiesExpression: replace in target and source
        if (expr instanceof ArkTSPropertyExpressions
                .CopyDataPropertiesExpression) {
            ArkTSPropertyExpressions.CopyDataPropertiesExpression cd =
                    (ArkTSPropertyExpressions.CopyDataPropertiesExpression)
                            expr;
            ArkTSExpression t =
                    replaceVariableImpl(cd.getTarget(), varName, replacement);
            ArkTSExpression s =
                    replaceVariableImpl(cd.getSource(), varName, replacement);
            if (t == null || s == null) {
                return null;
            }
            return new ArkTSPropertyExpressions
                    .CopyDataPropertiesExpression(t, s);
        }
        // GeneratorStateExpression: replace in value
        if (expr instanceof ArkTSPropertyExpressions
                .GeneratorStateExpression) {
            ArkTSExpression v = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.GeneratorStateExpression) expr)
                            .getValue(), varName, replacement);
            if (v == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.GeneratorStateExpression(v);
        }
        // NullishCoalescingExpression: replace in left and right
        if (expr instanceof ArkTSPropertyExpressions
                .NullishCoalescingExpression) {
            ArkTSPropertyExpressions.NullishCoalescingExpression nc =
                    (ArkTSPropertyExpressions.NullishCoalescingExpression)
                            expr;
            ArkTSExpression l =
                    replaceVariableImpl(nc.getLeft(), varName, replacement);
            ArkTSExpression r =
                    replaceVariableImpl(nc.getRight(), varName, replacement);
            if (l == null || r == null) {
                return null;
            }
            return new ArkTSPropertyExpressions
                    .NullishCoalescingExpression(l, r);
        }
        // DefinePropertyExpression: replace in object, property, value
        if (expr instanceof ArkTSPropertyExpressions
                .DefinePropertyExpression) {
            ArkTSPropertyExpressions.DefinePropertyExpression dp =
                    (ArkTSPropertyExpressions.DefinePropertyExpression) expr;
            ArkTSExpression o =
                    replaceVariableImpl(dp.getObject(), varName, replacement);
            ArkTSExpression p =
                    replaceVariableImpl(dp.getProperty(), varName, replacement);
            ArkTSExpression v =
                    replaceVariableImpl(dp.getValue(), varName, replacement);
            if (o == null || p == null || v == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.DefinePropertyExpression(
                    o, p, v);
        }
        // TypePredicateExpression: replace in expression
        if (expr instanceof ArkTSPropertyExpressions
                .TypePredicateExpression) {
            ArkTSExpression e = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.TypePredicateExpression) expr)
                            .getExpression(), varName, replacement);
            if (e == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.TypePredicateExpression(
                    e,
                    ((ArkTSPropertyExpressions.TypePredicateExpression) expr)
                            .getTypeName());
        }
        // ConstAssertionExpression: replace in expression
        if (expr instanceof ArkTSPropertyExpressions
                .ConstAssertionExpression) {
            ArkTSExpression e = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.ConstAssertionExpression) expr)
                            .getExpression(), varName, replacement);
            if (e == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.ConstAssertionExpression(e);
        }
        // SatisfiesExpression: replace in expression
        if (expr instanceof ArkTSPropertyExpressions.SatisfiesExpression) {
            ArkTSExpression e = replaceVariableImpl(
                    ((ArkTSPropertyExpressions.SatisfiesExpression) expr)
                            .getExpression(), varName, replacement);
            if (e == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.SatisfiesExpression(
                    e,
                    ((ArkTSPropertyExpressions.SatisfiesExpression) expr)
                            .getTypeName());
        }
        // StaticFieldExpression: replace in target and value
        if (expr instanceof ArkTSPropertyExpressions.StaticFieldExpression) {
            ArkTSPropertyExpressions.StaticFieldExpression sf =
                    (ArkTSPropertyExpressions.StaticFieldExpression) expr;
            ArkTSExpression t =
                    replaceVariableImpl(sf.getTarget(), varName, replacement);
            ArkTSExpression v =
                    replaceVariableImpl(sf.getValue(), varName, replacement);
            if (t == null || v == null) {
                return null;
            }
            return new ArkTSPropertyExpressions.StaticFieldExpression(t, v);
        }
        // ArrayDestructuringExpression: replace in source and defaults
        if (expr instanceof ArkTSPropertyExpressions
                .ArrayDestructuringExpression) {
            ArkTSPropertyExpressions.ArrayDestructuringExpression ad =
                    (ArkTSPropertyExpressions.ArrayDestructuringExpression)
                            expr;
            ArkTSExpression src = ad.getSource() != null
                    ? replaceVariableImpl(ad.getSource(), varName, replacement)
                    : null;
            if (ad.getSource() != null && src == null) {
                return null;
            }
            List<ArkTSPropertyExpressions.ArrayDestructuringExpression
                    .ArrayBinding> newBindings = new ArrayList<>();
            for (ArkTSPropertyExpressions.ArrayDestructuringExpression
                    .ArrayBinding b : ad.getBindings()) {
                if (b.getDefaultValue() != null) {
                    ArkTSExpression dv = replaceVariableImpl(
                            b.getDefaultValue(), varName, replacement);
                    if (dv == null) {
                        return null;
                    }
                    newBindings.add(
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding(b.getName(), dv));
                } else {
                    newBindings.add(
                            new ArkTSPropertyExpressions
                                    .ArrayDestructuringExpression
                                    .ArrayBinding(b.getName()));
                }
            }
            return new ArkTSPropertyExpressions.ArrayDestructuringExpression(
                    newBindings, ad.getRestBinding(), src,
                    ad.getRestBinding() != null);
        }
        // ObjectDestructuringExpression: replace in source and defaults
        if (expr instanceof ArkTSPropertyExpressions
                .ObjectDestructuringExpression) {
            ArkTSPropertyExpressions.ObjectDestructuringExpression od =
                    (ArkTSPropertyExpressions.ObjectDestructuringExpression)
                            expr;
            ArkTSExpression src = od.getSource() != null
                    ? replaceVariableImpl(od.getSource(), varName, replacement)
                    : null;
            if (od.getSource() != null && src == null) {
                return null;
            }
            List<ArkTSPropertyExpressions.ObjectDestructuringExpression
                    .DestructuringBinding> newBindings = new ArrayList<>();
            for (ArkTSPropertyExpressions.ObjectDestructuringExpression
                    .DestructuringBinding b : od.getBindings()) {
                if (b.getDefaultValue() != null) {
                    ArkTSExpression dv = replaceVariableImpl(
                            b.getDefaultValue(), varName, replacement);
                    if (dv == null) {
                        return null;
                    }
                    newBindings.add(
                            new ArkTSPropertyExpressions
                                    .ObjectDestructuringExpression
                                    .DestructuringBinding(
                                    b.getProperty(), b.getAlias(), dv));
                } else {
                    newBindings.add(
                            new ArkTSPropertyExpressions
                                    .ObjectDestructuringExpression
                                    .DestructuringBinding(
                                    b.getProperty(), b.getAlias()));
                }
            }
            return new ArkTSPropertyExpressions
                    .ObjectDestructuringExpression(newBindings, src);
        }
        // Function boundary expressions — don't traverse into closures
        if (expr instanceof ArkTSAccessExpressions.ArrowFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .AnonymousFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .GeneratorFunctionExpression
                || expr instanceof ArkTSAccessExpressions.ClosureExpression) {
            return expr;
        }
        // SpreadNewExpression: replace in callee and arguments
        if (expr instanceof ArkTSAccessExpressions.SpreadNewExpression) {
            ArkTSAccessExpressions.SpreadNewExpression sn =
                    (ArkTSAccessExpressions.SpreadNewExpression) expr;
            ArkTSExpression callee =
                    replaceVariableImpl(sn.getCallee(), varName, replacement);
            if (callee == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a : sn.getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.SpreadNewExpression(
                    callee, newArgs);
        }
        // TaggedTemplateExpression: replace in expressions
        if (expr instanceof ArkTSAccessExpressions
                .TaggedTemplateExpression) {
            ArkTSAccessExpressions.TaggedTemplateExpression tte =
                    (ArkTSAccessExpressions.TaggedTemplateExpression) expr;
            List<ArkTSExpression> newExprs = new ArrayList<>();
            for (ArkTSExpression e : tte.getExpressions()) {
                ArkTSExpression r =
                        replaceVariableImpl(e, varName, replacement);
                if (r == null) {
                    return null;
                }
                newExprs.add(r);
            }
            return new ArkTSAccessExpressions.TaggedTemplateExpression(
                    tte.getTag(), tte.getQuasis(), newExprs);
        }
        // RuntimeCallExpression: replace in arguments
        if (expr instanceof ArkTSAccessExpressions.RuntimeCallExpression) {
            ArkTSAccessExpressions.RuntimeCallExpression rc =
                    (ArkTSAccessExpressions.RuntimeCallExpression) expr;
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a : rc.getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.RuntimeCallExpression(
                    rc.getRuntimeName(), newArgs);
        }
        // IifeExpression: replace in function expression and arguments
        if (expr instanceof ArkTSAccessExpressions.IifeExpression) {
            ArkTSAccessExpressions.IifeExpression iife =
                    (ArkTSAccessExpressions.IifeExpression) expr;
            ArkTSExpression func =
                    replaceVariableImpl(iife.getFunctionExpression(),
                            varName, replacement);
            if (func == null) {
                return null;
            }
            List<ArkTSExpression> newArgs = new ArrayList<>();
            for (ArkTSExpression a : iife.getArguments()) {
                ArkTSExpression r =
                        replaceVariableImpl(a, varName, replacement);
                if (r == null) {
                    return null;
                }
                newArgs.add(r);
            }
            return new ArkTSAccessExpressions.IifeExpression(func, newArgs);
        }
        // DynamicImportExpression: replace in specifier
        if (expr instanceof ArkTSAccessExpressions.DynamicImportExpression) {
            ArkTSExpression spec = replaceVariableImpl(
                    ((ArkTSAccessExpressions.DynamicImportExpression) expr)
                            .getSpecifier(), varName, replacement);
            if (spec == null) {
                return null;
            }
            return new ArkTSAccessExpressions.DynamicImportExpression(spec);
        }
        // Leaf expressions that never contain variables — return as-is
        if (expr instanceof ArkTSExpression.LiteralExpression
                || expr instanceof ArkTSExpression.ThisExpression
                || expr instanceof ArkTSAccessExpressions
                        .RegExpLiteralExpression
                || expr instanceof ArkTSAccessExpressions
                        .TypeReferenceExpression
                || expr instanceof ArkTSAccessExpressions
                        .RestParameterExpression
                || expr instanceof ArkTSPropertyExpressions.SuperExpression
                || expr instanceof ArkTSPropertyExpressions
                        .TemplateObjectExpression
                || expr instanceof ArkTSPropertyExpressions
                        .PrivateFieldDeclarationExpression) {
            return expr;
        }
        // For unknown expression types, don't attempt replacement
        return null;
    }

    /**
     * Checks whether a statement contains a reference to the given variable.
     * Only checks top-level expression statements and variable declarations.
     *
     * @param stmt the statement to check
     * @param varName the variable name to look for
     * @return true if the variable is referenced
     */
    private static boolean stmtReferencesVariable(ArkTSStatement stmt,
            String varName) {
        if (stmt instanceof ArkTSStatement.ExpressionStatement) {
            return countVariableUsage(
                    ((ArkTSStatement.ExpressionStatement) stmt)
                            .getExpression(), varName) > 0;
        }
        if (stmt instanceof ArkTSStatement.VariableDeclaration) {
            ArkTSStatement.VariableDeclaration vd =
                    (ArkTSStatement.VariableDeclaration) stmt;
            if (varName.equals(vd.getName())) {
                return true;
            }
            if (vd.getInitializer() != null
                    && countVariableUsage(vd.getInitializer(), varName) > 0) {
                return true;
            }
        }
        if (stmt instanceof ArkTSStatement.ReturnStatement) {
            ArkTSExpression val =
                    ((ArkTSStatement.ReturnStatement) stmt).getValue();
            if (val != null && countVariableUsage(val, varName) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively counts how many times a variable name appears as a
     * VariableExpression in an expression tree. Returns
     * {@link Integer#MAX_VALUE} for function expressions to prevent
     * inlining across function boundaries.
     *
     * @param expr the expression to search
     * @param varName the variable name to count
     * @return the number of occurrences, or MAX_VALUE for closures
     */
    static int countVariableUsage(ArkTSExpression expr, String varName) {
        if (expr == null) {
            return 0;
        }
        // Variable reference
        if (expr instanceof ArkTSExpression.VariableExpression) {
            return varName.equals(
                    ((ArkTSExpression.VariableExpression) expr).getName())
                    ? 1 : 0;
        }
        // Function boundaries — don't cross
        if (expr instanceof ArkTSAccessExpressions.ArrowFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .AnonymousFunctionExpression
                || expr instanceof ArkTSAccessExpressions
                        .GeneratorFunctionExpression
                || expr instanceof ArkTSAccessExpressions.ClosureExpression) {
            return Integer.MAX_VALUE;
        }
        // Literals — no variables
        if (expr instanceof ArkTSExpression.LiteralExpression
                || expr instanceof ArkTSExpression.ThisExpression
                || expr instanceof ArkTSAccessExpressions
                        .RegExpLiteralExpression
                || expr instanceof ArkTSAccessExpressions
                        .TypeReferenceExpression
                || expr instanceof ArkTSPropertyExpressions
                        .SuperExpression
                || expr instanceof ArkTSPropertyExpressions
                        .PrivateFieldDeclarationExpression) {
            return 0;
        }
        // Binary
        if (expr instanceof ArkTSExpression.BinaryExpression) {
            ArkTSExpression.BinaryExpression bin =
                    (ArkTSExpression.BinaryExpression) expr;
            return countVariableUsage(bin.getLeft(), varName)
                    + countVariableUsage(bin.getRight(), varName);
        }
        // Unary
        if (expr instanceof ArkTSExpression.UnaryExpression) {
            return countVariableUsage(
                    ((ArkTSExpression.UnaryExpression) expr).getOperand(),
                    varName);
        }
        // Call
        if (expr instanceof ArkTSExpression.CallExpression) {
            ArkTSExpression.CallExpression call =
                    (ArkTSExpression.CallExpression) expr;
            int count = countVariableUsage(call.getCallee(), varName);
            for (ArkTSExpression arg : call.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Member
        if (expr instanceof ArkTSExpression.MemberExpression) {
            ArkTSExpression.MemberExpression mem =
                    (ArkTSExpression.MemberExpression) expr;
            int count = countVariableUsage(mem.getObject(), varName);
            if (mem.isComputed()) {
                count += countVariableUsage(mem.getProperty(), varName);
            }
            return count;
        }
        // Assign
        if (expr instanceof ArkTSExpression.AssignExpression) {
            ArkTSExpression.AssignExpression assign =
                    (ArkTSExpression.AssignExpression) expr;
            return countVariableUsage(assign.getTarget(), varName)
                    + countVariableUsage(assign.getValue(), varName);
        }
        // Compound assign
        if (expr instanceof ArkTSExpression.CompoundAssignExpression) {
            ArkTSExpression.CompoundAssignExpression ca =
                    (ArkTSExpression.CompoundAssignExpression) expr;
            return countVariableUsage(ca.getTarget(), varName)
                    + countVariableUsage(ca.getValue(), varName);
        }
        // Logical assign
        if (expr instanceof ArkTSExpression.LogicalAssignExpression) {
            ArkTSExpression.LogicalAssignExpression la =
                    (ArkTSExpression.LogicalAssignExpression) expr;
            return countVariableUsage(la.getTarget(), varName)
                    + countVariableUsage(la.getValue(), varName);
        }
        // Increment/decrement
        if (expr instanceof ArkTSExpression.IncrementExpression) {
            return countVariableUsage(
                    ((ArkTSExpression.IncrementExpression) expr).getTarget(),
                    varName);
        }
        // New
        if (expr instanceof ArkTSExpression.NewExpression) {
            ArkTSExpression.NewExpression ne =
                    (ArkTSExpression.NewExpression) expr;
            int count = countVariableUsage(ne.getCallee(), varName);
            for (ArkTSExpression arg : ne.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Conditional (ternary)
        if (expr instanceof ArkTSAccessExpressions.ConditionalExpression) {
            ArkTSAccessExpressions.ConditionalExpression cond =
                    (ArkTSAccessExpressions.ConditionalExpression) expr;
            return countVariableUsage(cond.getTest(), varName)
                    + countVariableUsage(cond.getConsequent(), varName)
                    + countVariableUsage(cond.getAlternate(), varName);
        }
        // Switch expression
        if (expr instanceof ArkTSAccessExpressions.SwitchExpression) {
            ArkTSAccessExpressions.SwitchExpression se =
                    (ArkTSAccessExpressions.SwitchExpression) expr;
            int count = countVariableUsage(se.getDiscriminant(), varName);
            for (ArkTSAccessExpressions.SwitchExpression.SwitchExprCase c
                    : se.getCases()) {
                for (ArkTSExpression t : c.getTests()) {
                    count += countVariableUsage(t, varName);
                }
                count += countVariableUsage(c.getValue(), varName);
            }
            if (se.getDefaultValue() != null) {
                count += countVariableUsage(se.getDefaultValue(), varName);
            }
            return count;
        }
        // Array literal
        if (expr instanceof ArkTSAccessExpressions.ArrayLiteralExpression) {
            int count = 0;
            for (ArkTSExpression elem :
                    ((ArkTSAccessExpressions.ArrayLiteralExpression) expr)
                            .getElements()) {
                count += countVariableUsage(elem, varName);
            }
            return count;
        }
        // Object literal
        if (expr instanceof ArkTSAccessExpressions.ObjectLiteralExpression) {
            int count = 0;
            for (ArkTSAccessExpressions.ObjectLiteralExpression.ObjectProperty
                    prop :
                    ((ArkTSAccessExpressions.ObjectLiteralExpression) expr)
                            .getProperties()) {
                count += countVariableUsage(prop.getValue(), varName);
                if (prop.isComputed()) {
                    count += countVariableUsage(
                            prop.getComputedKey(), varName);
                }
            }
            return count;
        }
        // Template literal
        if (expr instanceof ArkTSAccessExpressions
                .TemplateLiteralExpression) {
            int count = 0;
            for (ArkTSExpression interp :
                    ((ArkTSAccessExpressions.TemplateLiteralExpression) expr)
                            .getExpressions()) {
                count += countVariableUsage(interp, varName);
            }
            return count;
        }
        // Tagged template literal
        if (expr instanceof ArkTSAccessExpressions
                .TaggedTemplateExpression) {
            ArkTSAccessExpressions.TaggedTemplateExpression tte =
                    (ArkTSAccessExpressions.TaggedTemplateExpression) expr;
            int count = 0;
            for (ArkTSExpression interp : tte.getExpressions()) {
                count += countVariableUsage(interp, varName);
            }
            return count;
        }
        // Spread
        if (expr instanceof ArkTSAccessExpressions.SpreadExpression) {
            return countVariableUsage(
                    ((ArkTSAccessExpressions.SpreadExpression) expr)
                            .getArgument(), varName);
        }
        // Spread call
        if (expr instanceof ArkTSAccessExpressions.SpreadCallExpression) {
            ArkTSAccessExpressions.SpreadCallExpression sc =
                    (ArkTSAccessExpressions.SpreadCallExpression) expr;
            int count = countVariableUsage(sc.getCallee(), varName);
            for (ArkTSExpression arg : sc.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Spread new
        if (expr instanceof ArkTSAccessExpressions.SpreadNewExpression) {
            ArkTSAccessExpressions.SpreadNewExpression sn =
                    (ArkTSAccessExpressions.SpreadNewExpression) expr;
            int count = countVariableUsage(sn.getCallee(), varName);
            for (ArkTSExpression arg : sn.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Spread array
        if (expr instanceof ArkTSAccessExpressions.SpreadArrayExpression) {
            int count = 0;
            for (ArkTSExpression elem :
                    ((ArkTSAccessExpressions.SpreadArrayExpression) expr)
                            .getElements()) {
                count += countVariableUsage(elem, varName);
            }
            return count;
        }
        // Spread object
        if (expr instanceof ArkTSAccessExpressions.SpreadObjectExpression) {
            int count = 0;
            for (ArkTSExpression prop :
                    ((ArkTSAccessExpressions.SpreadObjectExpression) expr)
                            .getProperties()) {
                count += countVariableUsage(prop, varName);
            }
            return count;
        }
        // Optional chain
        if (expr instanceof ArkTSAccessExpressions.OptionalChainExpression) {
            ArkTSAccessExpressions.OptionalChainExpression oc =
                    (ArkTSAccessExpressions.OptionalChainExpression) expr;
            int count = countVariableUsage(oc.getObject(), varName);
            if (oc.isComputed()) {
                count += countVariableUsage(oc.getProperty(), varName);
            }
            return count;
        }
        // Optional chain call
        if (expr instanceof ArkTSAccessExpressions
                .OptionalChainCallExpression) {
            ArkTSAccessExpressions.OptionalChainCallExpression occ =
                    (ArkTSAccessExpressions.OptionalChainCallExpression) expr;
            int count = countVariableUsage(occ.getObject(), varName);
            if (occ.isComputed()) {
                count += countVariableUsage(occ.getProperty(), varName);
            }
            for (ArkTSExpression arg : occ.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Await
        if (expr instanceof ArkTSAccessExpressions.AwaitExpression) {
            return countVariableUsage(
                    ((ArkTSAccessExpressions.AwaitExpression) expr)
                            .getArgument(), varName);
        }
        // Yield
        if (expr instanceof ArkTSAccessExpressions.YieldExpression) {
            ArkTSExpression arg =
                    ((ArkTSAccessExpressions.YieldExpression) expr)
                            .getArgument();
            return arg != null ? countVariableUsage(arg, varName) : 0;
        }
        // As (type cast)
        if (expr instanceof ArkTSAccessExpressions.AsExpression) {
            return countVariableUsage(
                    ((ArkTSAccessExpressions.AsExpression) expr)
                            .getExpression(), varName);
        }
        // Non-null assertion
        if (expr instanceof ArkTSAccessExpressions.NonNullExpression) {
            return countVariableUsage(
                    ((ArkTSAccessExpressions.NonNullExpression) expr)
                            .getExpression(), varName);
        }
        // IIFE
        if (expr instanceof ArkTSAccessExpressions.IifeExpression) {
            ArkTSAccessExpressions.IifeExpression iife =
                    (ArkTSAccessExpressions.IifeExpression) expr;
            int count = countVariableUsage(
                    iife.getFunctionExpression(), varName);
            for (ArkTSExpression arg : iife.getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Dynamic import
        if (expr instanceof ArkTSAccessExpressions.DynamicImportExpression) {
            return countVariableUsage(
                    ((ArkTSAccessExpressions.DynamicImportExpression) expr)
                            .getSpecifier(), varName);
        }
        // Built-in new
        if (expr instanceof ArkTSAccessExpressions.BuiltInNewExpression) {
            int count = 0;
            for (ArkTSExpression arg :
                    ((ArkTSAccessExpressions.BuiltInNewExpression) expr)
                            .getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Runtime call
        if (expr instanceof ArkTSAccessExpressions.RuntimeCallExpression) {
            int count = 0;
            for (ArkTSExpression arg :
                    ((ArkTSAccessExpressions.RuntimeCallExpression) expr)
                            .getArguments()) {
                count += countVariableUsage(arg, varName);
            }
            return count;
        }
        // Rest parameter
        if (expr instanceof ArkTSAccessExpressions.RestParameterExpression) {
            return 0;
        }
        // Private member access
        if (expr instanceof ArkTSPropertyExpressions
                .PrivateMemberExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.PrivateMemberExpression) expr)
                            .getObject(), varName);
        }
        // In expression
        if (expr instanceof ArkTSPropertyExpressions.InExpression) {
            ArkTSPropertyExpressions.InExpression ie =
                    (ArkTSPropertyExpressions.InExpression) expr;
            return countVariableUsage(ie.getProperty(), varName)
                    + countVariableUsage(ie.getObject(), varName);
        }
        // Instanceof
        if (expr instanceof ArkTSPropertyExpressions.InstanceofExpression) {
            ArkTSPropertyExpressions.InstanceofExpression ie =
                    (ArkTSPropertyExpressions.InstanceofExpression) expr;
            return countVariableUsage(ie.getExpression(), varName)
                    + countVariableUsage(ie.getTargetType(), varName);
        }
        // Delete
        if (expr instanceof ArkTSPropertyExpressions.DeleteExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.DeleteExpression) expr)
                            .getTarget(), varName);
        }
        // Copy data properties
        if (expr instanceof ArkTSPropertyExpressions
                .CopyDataPropertiesExpression) {
            ArkTSPropertyExpressions.CopyDataPropertiesExpression cd =
                    (ArkTSPropertyExpressions.CopyDataPropertiesExpression)
                            expr;
            return countVariableUsage(cd.getTarget(), varName)
                    + countVariableUsage(cd.getSource(), varName);
        }
        // Generator state
        if (expr instanceof ArkTSPropertyExpressions
                .GeneratorStateExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.GeneratorStateExpression) expr)
                            .getValue(), varName);
        }
        // Array destructuring
        if (expr instanceof ArkTSPropertyExpressions
                .ArrayDestructuringExpression) {
            ArkTSPropertyExpressions.ArrayDestructuringExpression ad =
                    (ArkTSPropertyExpressions.ArrayDestructuringExpression)
                            expr;
            int count = 0;
            for (ArkTSPropertyExpressions.ArrayDestructuringExpression
                    .ArrayBinding binding : ad.getBindings()) {
                if (binding.getDefaultValue() != null) {
                    count += countVariableUsage(
                            binding.getDefaultValue(), varName);
                }
            }
            if (ad.getSource() != null) {
                count += countVariableUsage(ad.getSource(), varName);
            }
            return count;
        }
        // Object destructuring
        if (expr instanceof ArkTSPropertyExpressions
                .ObjectDestructuringExpression) {
            ArkTSPropertyExpressions.ObjectDestructuringExpression od =
                    (ArkTSPropertyExpressions.ObjectDestructuringExpression)
                            expr;
            int count = 0;
            for (ArkTSPropertyExpressions.ObjectDestructuringExpression
                    .DestructuringBinding binding : od.getBindings()) {
                if (binding.getDefaultValue() != null) {
                    count += countVariableUsage(
                            binding.getDefaultValue(), varName);
                }
            }
            if (od.getSource() != null) {
                count += countVariableUsage(od.getSource(), varName);
            }
            return count;
        }
        // Nullish coalescing
        if (expr instanceof ArkTSPropertyExpressions
                .NullishCoalescingExpression) {
            ArkTSPropertyExpressions.NullishCoalescingExpression nc =
                    (ArkTSPropertyExpressions.NullishCoalescingExpression)
                            expr;
            return countVariableUsage(nc.getLeft(), varName)
                    + countVariableUsage(nc.getRight(), varName);
        }
        // Define property
        if (expr instanceof ArkTSPropertyExpressions
                .DefinePropertyExpression) {
            ArkTSPropertyExpressions.DefinePropertyExpression dp =
                    (ArkTSPropertyExpressions.DefinePropertyExpression) expr;
            return countVariableUsage(dp.getObject(), varName)
                    + countVariableUsage(dp.getProperty(), varName)
                    + countVariableUsage(dp.getValue(), varName);
        }
        // Template object
        if (expr instanceof ArkTSPropertyExpressions
                .TemplateObjectExpression) {
            return 0;
        }
        // Type predicate
        if (expr instanceof ArkTSPropertyExpressions
                .TypePredicateExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.TypePredicateExpression) expr)
                            .getExpression(), varName);
        }
        // Const assertion
        if (expr instanceof ArkTSPropertyExpressions
                .ConstAssertionExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.ConstAssertionExpression) expr)
                            .getExpression(), varName);
        }
        // Satisfies
        if (expr instanceof ArkTSPropertyExpressions.SatisfiesExpression) {
            return countVariableUsage(
                    ((ArkTSPropertyExpressions.SatisfiesExpression) expr)
                            .getExpression(), varName);
        }
        // Static field
        if (expr instanceof ArkTSPropertyExpressions.StaticFieldExpression) {
            ArkTSPropertyExpressions.StaticFieldExpression sf =
                    (ArkTSPropertyExpressions.StaticFieldExpression) expr;
            return countVariableUsage(sf.getTarget(), varName)
                    + countVariableUsage(sf.getValue(), varName);
        }
        // Unknown expression type — conservatively return MAX_VALUE
        return Integer.MAX_VALUE;
    }

}
