package com.arkghidra.decompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOperand;

/**
 * Handles miscellaneous load, store, module, iterator, super, and other
 * instruction categories during ArkTS decompilation.
 *
 * <p>Contains the long tail of instruction handling that does not fit into
 * the primary dispatch categories (simple loads, binary/unary ops, calls,
 * property access). This includes module variables, global records, iterator
 * opcodes, super calls, type conversion, regex, template objects, and
 * various Ark-specific operations.
 */
class LoadStoreHandler {

    private static final String ACC = "acc";

    private LoadStoreHandler() {
        // utility class — all methods are static
    }

    /**
     * Handles the remaining instruction categories not covered by the
     * primary dispatch in InstructionHandler.
     */
    static InstructionHandler.StatementResult handleRemainingOpcodes(
            int opcode, ArkInstruction insn,
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx, Set<String> declaredVars,
            TypeInference typeInf) {

        // --- Super call this range ---
        if (opcode == ArkOpcodesCompat.SUPERCALLTHISRANGE
                || opcode == ArkOpcodesCompat.SUPERCALLARROWRANGE) {
            int numArgs = (int) operands.get(0).getValue();
            int firstReg = (int) operands.get(
                    operands.size() - 1).getValue();
            List<ArkTSExpression> args = new ArrayList<>();
            for (int a = 0; a < numArgs; a++) {
                args.add(new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(firstReg + a)));
            }
            ArkTSStatement superCall =
                    new ArkTSControlFlow.SuperCallStatement(args);
            return new InstructionHandler.StatementResult(superCall,
                    new ArkTSExpression.CallExpression(
                            new ArkTSPropertyExpressions.SuperExpression(),
                            args));
        }

        // --- Super call spread ---
        if (opcode == ArkOpcodesCompat.SUPERCALLSPREAD) {
            int spreadReg = (int) operands.get(0).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.SpreadExpression(
                            new ArkTSExpression.VariableExpression(
                                    ctx.resolveRegisterName(spreadReg)));
            List<ArkTSExpression> args = new ArrayList<>();
            args.add(spreadArg);
            ArkTSStatement superCall =
                    new ArkTSControlFlow.SuperCallStatement(args);
            return new InstructionHandler.StatementResult(superCall,
                    new ArkTSAccessExpressions.SpreadCallExpression(
                            new ArkTSPropertyExpressions.SuperExpression(),
                            args));
        }

        // --- Global name resolution (tryldglobalbyname / ldglobalvar) ---
        if (opcode == ArkOpcodesCompat.TRYLDGLOBALBYNAME
                || opcode == ArkOpcodesCompat.LDGLOBALVAR) {
            // Format is IMM8_IMM16. The stringIdx is the IMM16 field
            // (operand[1]). Operand[0] is an internal tag/prefix byte.
            int stringIdx = operands.size() >= 2
                    ? (int) operands.get(1).getValue()
                    : (int) operands.get(0).getValue();
            String globalName = ctx.resolveString(stringIdx);
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression(globalName));
        }

        // --- Global name store (trystglobalbyname / stglobalvar) ---
        if (opcode == ArkOpcodesCompat.TRYSTGLOBALBYNAME
                || opcode == ArkOpcodesCompat.STGLOBALVAR) {
            int stringIdx = operands.size() >= 2
                    ? (int) operands.get(1).getValue()
                    : (int) operands.get(0).getValue();
            String globalName = ctx.resolveString(stringIdx);
            ArkTSExpression value = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new InstructionHandler.StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.AssignExpression(
                                    new ArkTSExpression.VariableExpression(
                                            globalName),
                                    value)),
                    value);
        }

        // --- Module variable access ---
        if (opcode == ArkOpcodesCompat.LDEXTERNALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            String resolvedName = resolveExternalModuleVar(
                    varIdx, ctx);
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            resolvedName);
            return new InstructionHandler.StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.LDLOCALMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "local_mod_" + varIdx);
            return new InstructionHandler.StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.STMODULEVAR) {
            int varIdx = (int) operands.get(0).getValue();
            if (accValue != null) {
                return new InstructionHandler.StatementResult(
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
            ArkTSExpression expr =
                    new ArkTSExpression.VariableExpression(
                            "module_ns_" + varIdx);
            return new InstructionHandler.StatementResult(null, expr);
        }

        // --- Dynamic import ---
        if (opcode == ArkOpcodesCompat.DYNAMICIMPORT) {
            ArkTSExpression specifier = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression callExpr =
                    new ArkTSAccessExpressions.DynamicImportExpression(
                            specifier);
            return new InstructionHandler.StatementResult(null, callExpr);
        }

        // --- Global record stores (const/var declarations) ---
        if (opcode == ArkOpcodesCompat.STCONSTTOGLOBALRECORD) {
            int stringIdx = (int) operands.get(0).getValue();
            String varName = ctx.resolveString(stringIdx);
            if (accValue != null) {
                return new InstructionHandler.StatementResult(
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
                return new InstructionHandler.StatementResult(
                        new ArkTSStatement.VariableDeclaration(
                                "let", varName, null, accValue),
                        accValue);
            }
            return null;
        }

        // --- Create array/object with buffer ---
        if (opcode == ArkOpcodesCompat.CREATEARRAYWITHBUFFER) {
            int numElements = (int) operands.get(0).getValue();
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.ArrayLiteralExpression(
                            createPlaceholderElements(numElements));
            return new InstructionHandler.StatementResult(null, expr);
        }
        if (opcode == ArkOpcodesCompat.CREATEOBJECTWITHBUFFER) {
            ArkTSExpression expr =
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            Collections.emptyList());
            return new InstructionHandler.StatementResult(null, expr);
        }

        // --- STARRAYSPREAD (spread into array) ---
        if (opcode == ArkOpcodesCompat.STARRAYSPREAD) {
            int dstReg = (int) operands.get(0).getValue();
            int srcReg = (int) operands.get(1).getValue();
            ArkTSExpression spreadArg =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(srcReg));
            ArkTSExpression spread =
                    new ArkTSAccessExpressions.SpreadExpression(spreadArg);
            return new InstructionHandler.StatementResult(
                    new ArkTSStatement.ExpressionStatement(spread),
                    spread);
        }

        // --- Iterator opcodes ---
        if (ArkOpcodesCompat.isGetIterator(opcode)) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("iterator"));
        }
        if (ArkOpcodesCompat.isCloseIterator(opcode)) {
            return null;
        }
        if (opcode == ArkOpcodesCompat.GETNEXTPROPNAME) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("nextProp"));
        }
        if (opcode == ArkOpcodesCompat.GETPROPITERATOR) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("propIterator"));
        }

        // --- Accumulator-only loads ---
        if (opcode == ArkOpcodesCompat.LDSYMBOL) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("Symbol"));
        }
        if (opcode == ArkOpcodesCompat.LDFUNCTION) {
            int funcIdx = (int) operands.get(0).getValue();
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression(
                            "func_" + funcIdx));
        }
        if (opcode == ArkOpcodesCompat.LDNEWTARGET) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("new.target"));
        }
        if (opcode == ArkOpcodesCompat.LDHOLE) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("undefined"));
        }
        if (opcode == ArkOpcodesCompat.LDBIGINT) {
            int bigintIdx = (int) operands.get(0).getValue();
            String bigintStr = ctx.resolveString(bigintIdx);
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.LiteralExpression(bigintStr + "n",
                            ArkTSExpression.LiteralExpression
                                    .LiteralKind.NUMBER));
        }

        // --- Type conversion ---
        if (opcode == ArkOpcodesCompat.TONUMBER) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Number"),
                            List.of(val)));
        }
        if (opcode == ArkOpcodesCompat.TONUMERIC) {
            ArkTSExpression val = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.VariableExpression("Number"),
                            List.of(val)));
        }

        // --- Debugger ---
        if (opcode == ArkOpcodesCompat.DEBUGGER) {
            return new InstructionHandler.StatementResult(
                    new ArkTSStatement.ExpressionStatement(
                            new ArkTSExpression.VariableExpression(
                                    "debugger")),
                    accValue);
        }

        // --- Lexical environment ---
        if (opcode == ArkOpcodesCompat.POPLEXENV) {
            return InstructionHandler.StatementResult.NO_OP;
        }
        if (opcode == ArkOpcodesCompat.NEWLEXENV) {
            return InstructionHandler.StatementResult.NO_OP;
        }
        if (opcode == ArkOpcodesCompat.NEWLEXENVWITHNAME) {
            return InstructionHandler.StatementResult.NO_OP;
        }

        // --- Argument handling ---
        if (opcode == ArkOpcodesCompat.GETUNMAPPEDARGS) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression("arguments"));
        }
        if (opcode == ArkOpcodesCompat.COPYRESTARGS) {
            int restIdx = (int) operands.get(0).getValue();
            String restName = "rest_" + restIdx;
            if (ctx != null && ctx.restParamIndex >= 0) {
                restName = "param_" + ctx.restParamIndex;
            }
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.VariableExpression(restName));
        }

        // --- Iterator result ---
        if (opcode == ArkOpcodesCompat.CREATEITERRESULTOBJ) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSAccessExpressions.ObjectLiteralExpression(
                            Collections.emptyList()));
        }

        // --- Apply/call ---
        if (opcode == ArkOpcodesCompat.APPLY) {
            return handleApply(operands, accValue, ctx);
        }
        if (opcode == ArkOpcodesCompat.NEWOBJAPPLY) {
            return handleNewObjApply(operands, accValue, ctx);
        }

        // --- RegExp ---
        if (opcode == ArkOpcodesCompat.CREATEREGEXPWITHLITERAL) {
            int stringIdx = (int) operands.get(1).getValue();
            String pattern = ctx.resolveString(stringIdx);
            String flags = "";
            if (operands.size() >= 3) {
                flags = decodeRegexFlags(
                        (int) operands.get(2).getValue());
            }
            return new InstructionHandler.StatementResult(null,
                    new ArkTSAccessExpressions.RegExpLiteralExpression(
                            pattern, flags));
        }

        // --- Template ---
        if (opcode == ArkOpcodesCompat.GETTEMPLATEOBJECT) {
            int templateIdx = (int) operands.get(0).getValue();
            return new InstructionHandler.StatementResult(null,
                    new ArkTSPropertyExpressions.TemplateObjectExpression(
                            templateIdx));
        }

        // --- Object prototype ---
        if (opcode == ArkOpcodesCompat.SETOBJECTWITHPROTO) {
            int objReg = (int) operands.get(
                    operands.size() - 1).getValue();
            ArkTSExpression obj =
                    new ArkTSExpression.VariableExpression(
                            ctx.resolveRegisterName(objReg));
            ArkTSExpression proto = accValue != null
                    ? accValue
                    : new ArkTSExpression.VariableExpression(ACC);
            ArkTSExpression objectDotSetPrototypeOf =
                    new ArkTSExpression.MemberExpression(
                            new ArkTSExpression.VariableExpression("Object"),
                            new ArkTSExpression.VariableExpression(
                                    "setPrototypeOf"),
                            false);
            ArkTSExpression callExpr =
                    new ArkTSExpression.CallExpression(
                            objectDotSetPrototypeOf,
                            List.of(obj, proto));
            return new InstructionHandler.StatementResult(
                    new ArkTSStatement.ExpressionStatement(callExpr), null);
        }

        // --- Async iterator ---
        if (opcode == ArkOpcodesCompat.GETASYNCITERATOR) {
            return new InstructionHandler.StatementResult(null,
                    new ArkTSExpression.CallExpression(
                            new ArkTSExpression.MemberExpression(
                                    accValue != null ? accValue
                                            : new ArkTSExpression
                                                    .VariableExpression(ACC),
                                    new ArkTSExpression.VariableExpression(
                                            "Symbol.asyncIterator"),
                                    false),
                            Collections.emptyList()));
        }

        // --- Super by value ---
        if (opcode == ArkOpcodesCompat.LDSUPERBYVALUE) {
            return handleLdSuperByValue(operands, ctx);
        }
        if (opcode == ArkOpcodesCompat.STSUPERBYVALUE) {
            return handleStSuperByValue(operands, accValue, ctx);
        }

        // --- Own property stores with name set ---
        if (opcode == ArkOpcodesCompat.STOWNBYVALUEWITHNAMESET) {
            return handleStOwnByValueWithNameSet(operands, accValue,
                    ctx);
        }
        if (opcode == ArkOpcodesCompat.STOWNBYNAMEWITHNAMESET) {
            return handleStOwnByNameWithNameSet(operands, accValue, ctx);
        }

        return null;
    }

    private static InstructionHandler.StatementResult handleApply(
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        ArkTSExpression func = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        int numArgs = (int) operands.get(0).getValue();
        int firstReg = (int) operands.get(1).getValue();
        List<ArkTSExpression> applyArgs = new ArrayList<>();
        for (int a = 0; a < numArgs; a++) {
            applyArgs.add(new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(firstReg + a)));
        }
        // When apply has a single array arg, emit fn(...args)
        if (applyArgs.size() == 1) {
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.SpreadExpression(applyArgs.get(0));
            return new InstructionHandler.StatementResult(null,
                    new ArkTSAccessExpressions.SpreadCallExpression(func,
                            List.of(spreadArg)));
        }
        // Multiple args: fn(...[v0, v1, ...])
        ArkTSExpression spreadArr =
                new ArkTSAccessExpressions.ArrayLiteralExpression(applyArgs);
        return new InstructionHandler.StatementResult(null,
                new ArkTSAccessExpressions.SpreadCallExpression(func,
                        List.of(new ArkTSAccessExpressions
                                .SpreadExpression(spreadArr))));
    }

    private static InstructionHandler.StatementResult handleNewObjApply(
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        ArkTSExpression ctor = accValue != null
                ? accValue
                : new ArkTSExpression.VariableExpression(ACC);
        int numArgs = (int) operands.get(0).getValue();
        int firstReg = (int) operands.get(1).getValue();
        List<ArkTSExpression> applyArgs = new ArrayList<>();
        for (int a = 0; a < numArgs; a++) {
            applyArgs.add(new ArkTSExpression.VariableExpression(
                    ctx.resolveRegisterName(firstReg + a)));
        }
        // When apply has a single arg, emit new Ctor(...args)
        if (applyArgs.size() == 1) {
            ArkTSExpression spreadArg =
                    new ArkTSAccessExpressions.SpreadExpression(applyArgs.get(0));
            return new InstructionHandler.StatementResult(null,
                    new ArkTSAccessExpressions.SpreadNewExpression(ctor,
                            List.of(spreadArg)));
        }
        // Multiple args: new Ctor(...[v0, v1, ...])
        ArkTSExpression spreadArr =
                new ArkTSAccessExpressions.ArrayLiteralExpression(applyArgs);
        return new InstructionHandler.StatementResult(null,
                new ArkTSAccessExpressions.SpreadNewExpression(ctor,
                        List.of(new ArkTSAccessExpressions
                                .SpreadExpression(spreadArr))));
    }

    private static InstructionHandler.StatementResult handleLdSuperByValue(
            List<ArkOperand> operands, DecompilationContext ctx) {
        int keyReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(keyReg));
        return new InstructionHandler.StatementResult(null,
                new ArkTSExpression.MemberExpression(
                        new ArkTSPropertyExpressions.SuperExpression(),
                        prop, true));
    }

    private static InstructionHandler.StatementResult handleStSuperByValue(
            List<ArkOperand> operands, ArkTSExpression accValue,
            DecompilationContext ctx) {
        int keyReg = (int) operands.get(
                operands.size() - 2).getValue();
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(keyReg));
        return new InstructionHandler.StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.MemberExpression(
                                        new ArkTSPropertyExpressions
                                                .SuperExpression(),
                                        prop, true),
                                accValue != null ? accValue
                                        : new ArkTSExpression
                                                .VariableExpression(ACC))),
                accValue);
    }

    private static InstructionHandler.StatementResult
            handleStOwnByValueWithNameSet(
                    List<ArkOperand> operands, ArkTSExpression accValue,
                    DecompilationContext ctx) {
        int keyReg = (int) operands.get(
                operands.size() - 2).getValue();
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(objReg));
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(keyReg));
        return new InstructionHandler.StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.MemberExpression(
                                        obj, prop, true),
                                accValue != null ? accValue
                                        : new ArkTSExpression
                                                .VariableExpression(ACC))),
                accValue);
    }

    private static InstructionHandler.StatementResult
            handleStOwnByNameWithNameSet(
                    List<ArkOperand> operands, ArkTSExpression accValue,
                    DecompilationContext ctx) {
        int objReg = (int) operands.get(
                operands.size() - 1).getValue();
        ArkTSExpression obj =
                new ArkTSExpression.VariableExpression(
                        ctx.resolveRegisterName(objReg));
        String pn = ctx.resolveString(
                (int) operands.get(1).getValue());
        ArkTSExpression prop =
                new ArkTSExpression.VariableExpression(pn);
        return new InstructionHandler.StatementResult(
                new ArkTSStatement.ExpressionStatement(
                        new ArkTSExpression.AssignExpression(
                                new ArkTSExpression.MemberExpression(
                                        obj, prop, false),
                                accValue != null ? accValue
                                        : new ArkTSExpression
                                                .VariableExpression(ACC))),
                accValue);
    }

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

    /**
     * Resolves an external module variable index to its import name.
     *
     * <p>In Ark bytecode, external module variables are indexed in the order
     * they appear in the module record's regular imports list. The variable
     * index corresponds to the Nth regular import's local name.
     *
     * @param varIdx the variable index from ldexternalmodulevar
     * @param ctx the decompilation context (may be null)
     * @return the resolved import name or a placeholder
     */
    private static String resolveExternalModuleVar(
            int varIdx, DecompilationContext ctx) {
        if (ctx != null && ctx.abcFile != null) {
            try {
                for (int ci = 0;
                        ci < ctx.abcFile.getClasses().size(); ci++) {
                    com.arkghidra.format.AbcModuleRecord record =
                            ctx.abcFile.getModuleRecord(ci);
                    if (record == null) {
                        continue;
                    }
                    List<com.arkghidra.format
                            .AbcModuleRecord.RegularImport> regImports =
                            record.getRegularImports();
                    if (varIdx >= 0 && varIdx < regImports.size()) {
                        String localName = ctx.abcFile.getSourceFileName(
                                regImports.get(varIdx)
                                        .getLocalNameOffset());
                        if (localName != null) {
                            return localName;
                        }
                    }
                    // Namespace imports follow regular imports
                    int nsBase = regImports.size();
                    List<com.arkghidra.format
                            .AbcModuleRecord.NamespaceImport> nsImports =
                            record.getNamespaceImports();
                    int nsIdx = varIdx - nsBase;
                    if (nsIdx >= 0 && nsIdx < nsImports.size()) {
                        String localName = ctx.abcFile.getSourceFileName(
                                nsImports.get(nsIdx)
                                        .getLocalNameOffset());
                        if (localName != null) {
                            return localName;
                        }
                    }
                }
            } catch (Exception e) {
                // Fall through to placeholder
            }
        }
        return "ext_mod_" + varIdx;
    }

    /**
     * Decodes a regex flags bitmask into a flags string.
     *
     * <p>The Ark bytecode regex flags bitmask uses:
     * bit 0 = 'g' (global), bit 1 = 'i' (ignoreCase),
     * bit 2 = 'm' (multiline), bit 3 = 's' (dotAll),
     * bit 4 = 'u' (unicode), bit 5 = 'y' (sticky).
     *
     * @param flagsBitmask the flags bitmask value
     * @return the decoded flags string
     */
    static String decodeRegexFlags(int flagsBitmask) {
        StringBuilder sb = new StringBuilder();
        if ((flagsBitmask & 0x01) != 0) {
            sb.append('g');
        }
        if ((flagsBitmask & 0x02) != 0) {
            sb.append('i');
        }
        if ((flagsBitmask & 0x04) != 0) {
            sb.append('m');
        }
        if ((flagsBitmask & 0x08) != 0) {
            sb.append('s');
        }
        if ((flagsBitmask & 0x10) != 0) {
            sb.append('u');
        }
        if ((flagsBitmask & 0x20) != 0) {
            sb.append('y');
        }
        return sb.toString();
    }
}
