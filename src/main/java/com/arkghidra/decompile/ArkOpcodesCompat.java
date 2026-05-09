package com.arkghidra.decompile;

import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.disasm.ArkOpcodes;

/**
 * Opcode constants accessible from the decompile package.
 *
 * <p>This class re-exports the opcode constants from {@link ArkOpcodes} so that
 * decompile classes can reference them with a short name. It also adds convenience
 * methods for opcode classification used during decompilation.
 */
final class ArkOpcodesCompat {
    // Re-export for convenient access
    static final int RETURN = ArkOpcodes.RETURN;
    static final int RETURNUNDEFINED = ArkOpcodes.RETURNUNDEFINED;
    static final int LDAI = ArkOpcodes.LDAI;
    static final int FLDAI = ArkOpcodes.FLDAI;
    static final int LDA_STR = ArkOpcodes.LDA_STR;
    static final int LDA = ArkOpcodes.LDA;
    static final int STA = ArkOpcodes.STA;
    static final int MOV = ArkOpcodes.MOV;
    static final int LDUNDEFINED = ArkOpcodes.LDUNDEFINED;
    static final int LDNULL = ArkOpcodes.LDNULL;
    static final int LDTRUE = ArkOpcodes.LDTRUE;
    static final int LDFALSE = ArkOpcodes.LDFALSE;
    static final int LDTHIS = ArkOpcodes.LDTHIS;
    static final int LDGLOBAL = ArkOpcodes.LDGLOBAL;
    static final int LDNAN = ArkOpcodes.LDNAN;
    static final int LDINFINITY = ArkOpcodes.LDINFINITY;
    static final int LDSYMBOL = ArkOpcodes.LDSYMBOL;
    static final int LDHOLE = ArkOpcodes.LDHOLE;
    static final int LDNEWTARGET = ArkOpcodes.LDNEWTARGET;
    static final int LDFUNCTION = ArkOpcodes.LDFUNCTION;

    static final int ADD2 = ArkOpcodes.ADD2;
    static final int SUB2 = ArkOpcodes.SUB2;
    static final int MUL2 = ArkOpcodes.MUL2;
    static final int DIV2 = ArkOpcodes.DIV2;
    static final int MOD2 = ArkOpcodes.MOD2;
    static final int EQ = ArkOpcodes.EQ;
    static final int NOTEQ = ArkOpcodes.NOTEQ;
    static final int LESS = ArkOpcodes.LESS;
    static final int LESSEQ = ArkOpcodes.LESSEQ;
    static final int GREATER = ArkOpcodes.GREATER;
    static final int GREATEREQ = ArkOpcodes.GREATEREQ;
    static final int SHL2 = ArkOpcodes.SHL2;
    static final int SHR2 = ArkOpcodes.SHR2;
    static final int ASHR2 = ArkOpcodes.ASHR2;
    static final int AND2 = ArkOpcodes.AND2;
    static final int OR2 = ArkOpcodes.OR2;
    static final int XOR2 = ArkOpcodes.XOR2;
    static final int EXP = ArkOpcodes.EXP;

    static final int TYPEOF = ArkOpcodes.TYPEOF;
    static final int NEG = ArkOpcodes.NEG;
    static final int NOT = ArkOpcodes.NOT;
    static final int INC = ArkOpcodes.INC;
    static final int DEC = ArkOpcodes.DEC;
    static final int INSTANCEOF = ArkOpcodes.INSTANCEOF;
    static final int ISIN = ArkOpcodes.ISIN;
    static final int ISTRUE = ArkOpcodes.ISTRUE;
    static final int ISFALSE = ArkOpcodes.ISFALSE;
    static final int STRICTEQ = ArkOpcodes.STRICTEQ;
    static final int STRICTNOTEQ = ArkOpcodes.STRICTNOTEQ;

    static final int CALLARG0 = ArkOpcodes.CALLARG0;
    static final int CALLARG1 = ArkOpcodes.CALLARG1;
    static final int CALLARGS2 = ArkOpcodes.CALLARGS2;
    static final int CALLARGS3 = ArkOpcodes.CALLARGS3;
    static final int CALLTHIS0 = ArkOpcodes.CALLTHIS0;
    static final int CALLTHIS1 = ArkOpcodes.CALLTHIS1;
    static final int CALLTHIS2 = ArkOpcodes.CALLTHIS2;
    static final int CALLTHIS3 = ArkOpcodes.CALLTHIS3;
    static final int CALLTHISRANGE = ArkOpcodes.CALLTHISRANGE;
    static final int CALLRANGE = ArkOpcodes.CALLRANGE;
    static final int NEWOBJRANGE = ArkOpcodes.NEWOBJRANGE;

    static final int JMP_IMM8 = ArkOpcodes.JMP_IMM8;
    static final int JMP_IMM16 = ArkOpcodes.JMP_IMM16;
    static final int JEQZ_IMM8 = ArkOpcodes.JEQZ_IMM8;
    static final int JEQZ_IMM16 = ArkOpcodes.JEQZ_IMM16;
    static final int JNEZ_IMM8 = ArkOpcodes.JNEZ_IMM8;
    static final int JNEZ_IMM16 = ArkOpcodes.JNEZ_IMM16;
    static final int JEQ_IMM8 = ArkOpcodes.JEQ_IMM8;
    static final int JEQ_IMM16 = ArkOpcodes.JEQ_IMM16;
    static final int JNE_IMM8 = ArkOpcodes.JNE_IMM8;
    static final int JNE_IMM16 = ArkOpcodes.JNE_IMM16;
    static final int JEQNULL_IMM8 = ArkOpcodes.JEQNULL_IMM8;
    static final int JEQNULL_IMM16 = ArkOpcodes.JEQNULL_IMM16;
    static final int JNENULL_IMM8 = ArkOpcodes.JNENULL_IMM8;
    static final int JNENULL_IMM16 = ArkOpcodes.JNENULL_IMM16;
    static final int JEQUNDEFINED_IMM8 = ArkOpcodes.JEQUNDEFINED_IMM8;
    static final int JEQUNDEFINED_IMM16 = ArkOpcodes.JEQUNDEFINED_IMM16;
    static final int JNEUNDEFINED_IMM8 = ArkOpcodes.JNEUNDEFINED_IMM8;
    static final int JNEUNDEFINED_IMM16 = ArkOpcodes.JNEUNDEFINED_IMM16;
    static final int JSTRICTEQZ_IMM8 = ArkOpcodes.JSTRICTEQZ_IMM8;
    static final int JSTRICTEQZ_IMM16 = ArkOpcodes.JSTRICTEQZ_IMM16;
    static final int JNSTRICTEQZ_IMM8 = ArkOpcodes.JNSTRICTEQZ_IMM8;
    static final int JNSTRICTEQZ_IMM16 = ArkOpcodes.JNSTRICTEQZ_IMM16;

    static final int LDOBJBYNAME = ArkOpcodes.LDOBJBYNAME;
    static final int STOBJBYNAME = ArkOpcodes.STOBJBYNAME;
    static final int LDOBJBYVALUE = ArkOpcodes.LDOBJBYVALUE;
    static final int STOBJBYVALUE = ArkOpcodes.STOBJBYVALUE;
    static final int LDOBJBYINDEX = ArkOpcodes.LDOBJBYINDEX;
    static final int STOBJBYINDEX = ArkOpcodes.STOBJBYINDEX;
    static final int STOWNBYNAME = ArkOpcodes.STOWNBYNAME;
    static final int STOWNBYVALUE = ArkOpcodes.STOWNBYVALUE;
    static final int STOWNBYINDEX = ArkOpcodes.STOWNBYINDEX;
    static final int LDTHISBYNAME = ArkOpcodes.LDTHISBYNAME;
    static final int STTHISBYNAME = ArkOpcodes.STTHISBYNAME;
    static final int LDTHISBYVALUE = ArkOpcodes.LDTHISBYVALUE;
    static final int STTHISBYVALUE = ArkOpcodes.STTHISBYVALUE;
    static final int LDSUPERBYNAME = ArkOpcodes.LDSUPERBYNAME;
    static final int STSUPERBYNAME = ArkOpcodes.STSUPERBYNAME;

    static final int LDLEXVAR = ArkOpcodes.LDLEXVAR;
    static final int STLEXVAR = ArkOpcodes.STLEXVAR;
    static final int TRYLDGLOBALBYNAME = ArkOpcodes.TRYLDGLOBALBYNAME;
    static final int TRYSTGLOBALBYNAME = ArkOpcodes.TRYSTGLOBALBYNAME;
    static final int LDGLOBALVAR = ArkOpcodes.LDGLOBALVAR;
    static final int STGLOBALVAR = ArkOpcodes.STGLOBALVAR;

    static final int CREATEEMPTYOBJECT = ArkOpcodes.CREATEEMPTYOBJECT;
    static final int CREATEEMPTYARRAY = ArkOpcodes.CREATEEMPTYARRAY;
    static final int CREATEARRAYWITHBUFFER = ArkOpcodes.CREATEARRAYWITHBUFFER;
    static final int CREATEOBJECTWITHBUFFER = ArkOpcodes.CREATEOBJECTWITHBUFFER;

    static final int DEFINEFUNC = ArkOpcodes.DEFINEFUNC;
    static final int DEFINECLASSWITHBUFFER = ArkOpcodes.DEFINECLASSWITHBUFFER;
    static final int NEWLEXENV = ArkOpcodes.NEWLEXENV;

    static final int NOP = ArkOpcodes.NOP;

    static final int ASYNCFUNCTIONENTER = ArkOpcodes.ASYNCFUNCTIONENTER;
    static final int ASYNCFUNCTIONAWAITUNCAUGHT =
            ArkOpcodes.ASYNCFUNCTIONAWAITUNCAUGHT;
    static final int ASYNCFUNCTIONRESOLVE = ArkOpcodes.ASYNCFUNCTIONRESOLVE;
    static final int ASYNCFUNCTIONREJECT = ArkOpcodes.ASYNCFUNCTIONREJECT;

    static final int CREATEGENERATOROBJ = ArkOpcodes.CREATEGENERATOROBJ;
    static final int SUSPENDGENERATOR = ArkOpcodes.SUSPENDGENERATOR;
    static final int RESUMEGENERATOR = ArkOpcodes.RESUMEGENERATOR;
    static final int GETRESUMEMODE = ArkOpcodes.GETRESUMEMODE;

    static final int STARRAYSPREAD = ArkOpcodes.STARRAYSPREAD;

    static final int STCONSTTOGLOBALRECORD = ArkOpcodes.STCONSTTOGLOBALRECORD;
    static final int STTOGLOBALRECORD = ArkOpcodes.STTOGLOBALRECORD;
    static final int LDEXTERNALMODULEVAR = ArkOpcodes.LDEXTERNALMODULEVAR;
    static final int LDLOCALMODULEVAR = ArkOpcodes.LDLOCALMODULEVAR;
    static final int STMODULEVAR = ArkOpcodes.STMODULEVAR;
    static final int GETMODULENAMESPACE = ArkOpcodes.GETMODULENAMESPACE;
    static final int DYNAMICIMPORT = ArkOpcodes.DYNAMICIMPORT;
    static final int DEFINEMETHOD = ArkOpcodes.DEFINEMETHOD;
    static final int DEFINEFIELDBYNAME = ArkOpcodes.DEFINEFIELDBYNAME;
    static final int DEFINEPROPERTYBYNAME = ArkOpcodes.DEFINEPROPERTYBYNAME;
    static final int SUPERCALLTHISRANGE = ArkOpcodes.SUPERCALLTHISRANGE;
    static final int SUPERCALLSPREAD = ArkOpcodes.SUPERCALLSPREAD;
    static final int SUPERCALLARROWRANGE = ArkOpcodes.SUPERCALLARROWRANGE;

    static final int LDPRIVATEPROPERTY = ArkOpcodes.LDPRIVATEPROPERTY;
    static final int STPRIVATEPROPERTY = ArkOpcodes.STPRIVATEPROPERTY;
    static final int TESTIN = ArkOpcodes.TESTIN;

    static final int SETGENERATORSTATE = ArkOpcodes.SETGENERATORSTATE;
    static final int CREATEASYNCGENERATOROBJ = ArkOpcodes.CREATEASYNCGENERATOROBJ;
    static final int ASYNCGENERATORRESOLVE = ArkOpcodes.ASYNCGENERATORRESOLVE;
    static final int ASYNCGENERATORREJECT = ArkOpcodes.ASYNCGENERATORREJECT;

    static final int CREATEOBJECTWITHEXCLUDEDKEYS =
            ArkOpcodes.CREATEOBJECTWITHEXCLUDEDKEYS;
    static final int DEFINEGETTERSETTERBYVALUE =
            ArkOpcodes.DEFINEGETTERSETTERBYVALUE;

    static final int DELOBJPROP = ArkOpcodes.DELOBJPROP;
    static final int COPYDATAPROPERTIES = ArkOpcodes.COPYDATAPROPERTIES;

    static final int GETPROPITERATOR = ArkOpcodes.GETPROPITERATOR;
    static final int GETITERATOR_IMM8 = ArkOpcodes.GETITERATOR_IMM8;
    static final int GETITERATOR_IMM16 = ArkOpcodes.GETITERATOR_IMM16;
    static final int CLOSEITERATOR_IMM8 = ArkOpcodes.CLOSEITERATOR_IMM8;
    static final int CLOSEITERATOR_IMM16 = ArkOpcodes.CLOSEITERATOR_IMM16;
    static final int GETNEXTPROPNAME = ArkOpcodes.GETNEXTPROPNAME;

    static final int THROW = ArkOpcodes.PREFIX_THROW;

    static final int DEBUGGER = ArkOpcodes.DEBUGGER;
    static final int POPLEXENV = ArkOpcodes.POPLEXENV;
    static final int TONUMBER = ArkOpcodes.TONUMBER;
    static final int TONUMERIC = ArkOpcodes.TONUMERIC;
    static final int GETUNMAPPEDARGS = ArkOpcodes.GETUNMAPPEDARGS;
    static final int CREATEITERRESULTOBJ = ArkOpcodes.CREATEITERRESULTOBJ;
    static final int NEWLEXENVWITHNAME = ArkOpcodes.NEWLEXENVWITHNAME;
    static final int APPLY = ArkOpcodes.APPLY;
    static final int NEWOBJAPPLY = ArkOpcodes.NEWOBJAPPLY;
    static final int CREATEREGEXPWITHLITERAL =
            ArkOpcodes.CREATEREGEXPWITHLITERAL;
    static final int LDBIGINT = ArkOpcodes.LDBIGINT;
    static final int COPYRESTARGS = ArkOpcodes.COPYRESTARGS;
    static final int GETTEMPLATEOBJECT = ArkOpcodes.GETTEMPLATEOBJECT;
    static final int SETOBJECTWITHPROTO = ArkOpcodes.SETOBJECTWITHPROTO;
    static final int GETASYNCITERATOR = ArkOpcodes.GETASYNCITERATOR;
    static final int LDSUPERBYVALUE = ArkOpcodes.LDSUPERBYVALUE;
    static final int STSUPERBYVALUE = ArkOpcodes.STSUPERBYVALUE;
    static final int STOWNBYVALUEWITHNAMESET =
            ArkOpcodes.STOWNBYVALUEWITHNAMESET;
    static final int STOWNBYNAMEWITHNAMESET =
            ArkOpcodes.STOWNBYNAMEWITHNAMESET;

    // --- Wide (0xFD-prefixed) sub-opcodes ---
    static final int WIDE_CREATEEMPTYARRAY = ArkOpcodes.WIDE_CREATEEMPTYARRAY;
    static final int WIDE_CREATEARRAYWITHBUFFER =
            ArkOpcodes.WIDE_CREATEARRAYWITHBUFFER;
    static final int WIDE_CREATEOBJECTWITHBUFFER =
            ArkOpcodes.WIDE_CREATEOBJECTWITHBUFFER;
    static final int WIDE_NEWOBJRANGE = ArkOpcodes.WIDE_NEWOBJRANGE;
    static final int WIDE_TYPEOF = ArkOpcodes.WIDE_TYPEOF;
    static final int WIDE_LDOBJBYVALUE = ArkOpcodes.WIDE_LDOBJBYVALUE;
    static final int WIDE_STOBJBYVALUE = ArkOpcodes.WIDE_STOBJBYVALUE;
    static final int WIDE_LDSUPERBYVALUE = ArkOpcodes.WIDE_LDSUPERBYVALUE;
    static final int WIDE_LDOBJBYINDEX = ArkOpcodes.WIDE_LDOBJBYINDEX;
    static final int WIDE_STOBJBYINDEX = ArkOpcodes.WIDE_STOBJBYINDEX;
    static final int WIDE_LDLEXVAR = ArkOpcodes.WIDE_LDLEXVAR;
    static final int WIDE_STLEXVAR = ArkOpcodes.WIDE_STLEXVAR;
    static final int WIDE_TRYLDGLOBALBYNAME =
            ArkOpcodes.WIDE_TRYLDGLOBALBYNAME;
    static final int WIDE_TRYSTGLOBALBYNAME =
            ArkOpcodes.WIDE_TRYSTGLOBALBYNAME;
    static final int WIDE_LDOBJBYNAME = ArkOpcodes.WIDE_LDOBJBYNAME;
    static final int WIDE_STOBJBYNAME = ArkOpcodes.WIDE_STOBJBYNAME;
    static final int WIDE_LDSUPERBYNAME = ArkOpcodes.WIDE_LDSUPERBYNAME;
    static final int WIDE_LDTHISBYNAME = ArkOpcodes.WIDE_LDTHISBYNAME;
    static final int WIDE_STTHISBYNAME = ArkOpcodes.WIDE_STTHISBYNAME;
    static final int WIDE_LDTHISBYVALUE = ArkOpcodes.WIDE_LDTHISBYVALUE;
    static final int WIDE_STTHISBYVALUE = ArkOpcodes.WIDE_STTHISBYVALUE;
    static final int WIDE_DEFINEFUNC = ArkOpcodes.WIDE_DEFINEFUNC;
    static final int WIDE_GETTEMPLATEOBJECT =
            ArkOpcodes.WIDE_GETTEMPLATEOBJECT;
    static final int WIDE_SETOBJECTWITHPROTO =
            ArkOpcodes.WIDE_SETOBJECTWITHPROTO;
    static final int WIDE_STOWNBYVALUE = ArkOpcodes.WIDE_STOWNBYVALUE;
    static final int WIDE_STOWNBYINDEX = ArkOpcodes.WIDE_STOWNBYINDEX;
    static final int WIDE_STOWNBYNAME = ArkOpcodes.WIDE_STOWNBYNAME;
    static final int WIDE_DEFINEMETHOD = ArkOpcodes.WIDE_DEFINEMETHOD;
    static final int WIDE_SUPERCALLTHISRANGE =
            ArkOpcodes.WIDE_SUPERCALLTHISRANGE;
    static final int WIDE_MOV = ArkOpcodes.WIDE_MOV;

    private ArkOpcodesCompat() {
    }

    /**
     * Maps a wide (0xFD-prefixed) sub-opcode to its corresponding
     * primary opcode. Returns the sub-opcode unchanged if no mapping
     * exists (i.e., it is not a known wide sub-opcode).
     *
     * @param wideSubOpcode the sub-opcode following the 0xFD prefix
     * @return the equivalent primary opcode
     */
    static int normalizeWideOpcode(int wideSubOpcode) {
        switch (wideSubOpcode) {
            case WIDE_MOV: return MOV;
            case WIDE_DEFINEFUNC: return DEFINEFUNC;
            case WIDE_DEFINEMETHOD: return DEFINEMETHOD;
            case WIDE_NEWOBJRANGE: return NEWOBJRANGE;
            case WIDE_SUPERCALLTHISRANGE: return SUPERCALLTHISRANGE;
            case WIDE_CREATEEMPTYARRAY: return CREATEEMPTYARRAY;
            case WIDE_CREATEARRAYWITHBUFFER: return CREATEARRAYWITHBUFFER;
            case WIDE_CREATEOBJECTWITHBUFFER: return CREATEOBJECTWITHBUFFER;
            case WIDE_TYPEOF: return TYPEOF;
            case WIDE_GETTEMPLATEOBJECT: return GETTEMPLATEOBJECT;
            case WIDE_SETOBJECTWITHPROTO: return SETOBJECTWITHPROTO;
            case WIDE_LDLEXVAR: return LDLEXVAR;
            case WIDE_STLEXVAR: return STLEXVAR;
            case WIDE_TRYLDGLOBALBYNAME: return TRYLDGLOBALBYNAME;
            case WIDE_TRYSTGLOBALBYNAME: return TRYSTGLOBALBYNAME;
            case WIDE_LDOBJBYNAME: return LDOBJBYNAME;
            case WIDE_STOBJBYNAME: return STOBJBYNAME;
            case WIDE_LDSUPERBYNAME: return LDSUPERBYNAME;
            case WIDE_LDTHISBYNAME: return LDTHISBYNAME;
            case WIDE_STTHISBYNAME: return STTHISBYNAME;
            case WIDE_LDOBJBYVALUE: return LDOBJBYVALUE;
            case WIDE_STOBJBYVALUE: return STOBJBYVALUE;
            case WIDE_LDSUPERBYVALUE: return LDSUPERBYVALUE;
            case WIDE_LDTHISBYVALUE: return LDTHISBYVALUE;
            case WIDE_STTHISBYVALUE: return STTHISBYVALUE;
            case WIDE_LDOBJBYINDEX: return LDOBJBYINDEX;
            case WIDE_STOBJBYINDEX: return STOBJBYINDEX;
            case WIDE_STOWNBYVALUE: return STOWNBYVALUE;
            case WIDE_STOWNBYINDEX: return STOWNBYINDEX;
            case WIDE_STOWNBYNAME: return STOWNBYNAME;
            default: return wideSubOpcode;
        }
    }

    /**
     * Returns true if the opcode is a known wide (0xFD-prefixed)
     * sub-opcode.
     *
     * @param opcode the opcode to check
     * @return true if this is a wide sub-opcode
     */
    static boolean isWideSubOpcode(int opcode) {
        switch (opcode) {
            case WIDE_MOV:
            case WIDE_DEFINEFUNC:
            case WIDE_DEFINEMETHOD:
            case WIDE_NEWOBJRANGE:
            case WIDE_SUPERCALLTHISRANGE:
            case WIDE_CREATEEMPTYARRAY:
            case WIDE_CREATEARRAYWITHBUFFER:
            case WIDE_CREATEOBJECTWITHBUFFER:
            case WIDE_TYPEOF:
            case WIDE_GETTEMPLATEOBJECT:
            case WIDE_SETOBJECTWITHPROTO:
            case WIDE_LDLEXVAR:
            case WIDE_STLEXVAR:
            case WIDE_TRYLDGLOBALBYNAME:
            case WIDE_TRYSTGLOBALBYNAME:
            case WIDE_LDOBJBYNAME:
            case WIDE_STOBJBYNAME:
            case WIDE_LDSUPERBYNAME:
            case WIDE_LDTHISBYNAME:
            case WIDE_STTHISBYNAME:
            case WIDE_LDOBJBYVALUE:
            case WIDE_STOBJBYVALUE:
            case WIDE_LDSUPERBYVALUE:
            case WIDE_LDTHISBYVALUE:
            case WIDE_STTHISBYVALUE:
            case WIDE_LDOBJBYINDEX:
            case WIDE_STOBJBYINDEX:
            case WIDE_STOWNBYVALUE:
            case WIDE_STOWNBYINDEX:
            case WIDE_STOWNBYNAME:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the normalized opcode for an instruction. For wide (0xFD-prefixed)
     * instructions, returns the equivalent primary opcode. For normal instructions,
     * returns the opcode unchanged.
     *
     * @param insn the instruction
     * @return the normalized opcode
     */
    static int getNormalizedOpcode(ArkInstruction insn) {
        return insn.isWide()
                ? normalizeWideOpcode(insn.getOpcode())
                : insn.getOpcode();
    }

    /**
     * Returns true if the opcode is an unconditional jump.
     *
     * @param opcode the opcode to check
     * @return true if the opcode is jmp
     */
    static boolean isUnconditionalJump(int opcode) {
        return opcode == JMP_IMM8 || opcode == JMP_IMM16;
    }

    /**
     * Returns true if the opcode is a conditional branch.
     *
     * @param opcode the opcode to check
     * @return true if the opcode is a conditional branch
     */
    static boolean isConditionalBranch(int opcode) {
        return opcode == JEQZ_IMM8 || opcode == JEQZ_IMM16
                || opcode == JNEZ_IMM8 || opcode == JNEZ_IMM16
                || opcode == JEQ_IMM8 || opcode == JEQ_IMM16
                || opcode == JNE_IMM8 || opcode == JNE_IMM16
                || opcode == JEQNULL_IMM8 || opcode == JEQNULL_IMM16
                || opcode == JNENULL_IMM8 || opcode == JNENULL_IMM16
                || opcode == JEQUNDEFINED_IMM8 || opcode == JEQUNDEFINED_IMM16
                || opcode == JNEUNDEFINED_IMM8 || opcode == JNEUNDEFINED_IMM16
                || opcode == JSTRICTEQZ_IMM8 || opcode == JSTRICTEQZ_IMM16
                || opcode == JNSTRICTEQZ_IMM8 || opcode == JNSTRICTEQZ_IMM16;
    }

    /**
     * Returns true if the opcode terminates control flow (jump, return, throw).
     *
     * @param opcode the opcode to check
     * @return true if the opcode is a control flow terminator
     */
    static boolean isTerminator(int opcode) {
        return isUnconditionalJump(opcode) || isConditionalBranch(opcode)
                || opcode == RETURN || opcode == RETURNUNDEFINED
                || opcode == THROW;
    }

    static boolean isGetIterator(int opcode) {
        return opcode == GETITERATOR_IMM8 || opcode == GETITERATOR_IMM16;
    }

    static boolean isGetAsyncIterator(int opcode) {
        return opcode == GETASYNCITERATOR;
    }

    static boolean isCloseIterator(int opcode) {
        return opcode == CLOSEITERATOR_IMM8 || opcode == CLOSEITERATOR_IMM16;
    }
}
