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

    // --- Vendor-specific named call opcodes (HarmonyOS API 12+) ---
    static final int CALLTHIS0WITHNAME = ArkOpcodes.CALLTHIS0WITHNAME;
    static final int CALLTHIS1WITHNAME = ArkOpcodes.CALLTHIS1WITHNAME;
    static final int CALLTHIS2WITHNAME = ArkOpcodes.CALLTHIS2WITHNAME;
    static final int CALLTHIS3WITHNAME = ArkOpcodes.CALLTHIS3WITHNAME;
    static final int CALLTHISRANGEWITHNAME = ArkOpcodes.CALLTHISRANGEWITHNAME;

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

    static final int JSTRICTEQNULL_IMM8 = ArkOpcodes.JSTRICTEQNULL_IMM8;
    static final int JSTRICTEQNULL_IMM16 = ArkOpcodes.JSTRICTEQNULL_IMM16;
    static final int JNSTRICTEQNULL_IMM8 = ArkOpcodes.JNSTRICTEQNULL_IMM8;
    static final int JNSTRICTEQNULL_IMM16 = ArkOpcodes.JNSTRICTEQNULL_IMM16;

    static final int JSTRICTEQUNDEFINED_IMM16 =
            ArkOpcodes.JSTRICTEQUNDEFINED_IMM16;
    static final int JNSTRICTEQUNDEFINED_IMM16 =
            ArkOpcodes.JNSTRICTEQUNDEFINED_IMM16;

    static final int JSTRICTEQ_IMM8 = ArkOpcodes.JSTRICTEQ_IMM8;
    static final int JSTRICTEQ_IMM16 = ArkOpcodes.JSTRICTEQ_IMM16;
    static final int JNSTRICTEQ_IMM8 = ArkOpcodes.JNSTRICTEQ_IMM8;
    static final int JNSTRICTEQ_IMM16 = ArkOpcodes.JNSTRICTEQ_IMM16;

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

    // --- CallRuntime (0xFB-prefixed) sub-opcodes ---
    static final int CRT_NOTIFYCONCURRENTRESULT =
            ArkOpcodes.CRT_NOTIFYCONCURRENTRESULT;
    static final int CRT_DEFINEFIELDBYVALUE =
            ArkOpcodes.CRT_DEFINEFIELDBYVALUE;
    static final int CRT_DEFINEFIELDBYINDEX =
            ArkOpcodes.CRT_DEFINEFIELDBYINDEX;
    static final int CRT_TOPROPERTYKEY =
            ArkOpcodes.CRT_TOPROPERTYKEY;
    static final int CRT_CREATEPRIVATEPROPERTY =
            ArkOpcodes.CRT_CREATEPRIVATEPROPERTY;
    static final int CRT_DEFINEPRIVATEPROPERTY =
            ArkOpcodes.CRT_DEFINEPRIVATEPROPERTY;
    static final int CRT_CALLINIT =
            ArkOpcodes.CRT_CALLINIT;
    static final int CRT_DEFINESENDABLECLASS =
            ArkOpcodes.CRT_DEFINESENDABLECLASS;
    static final int CRT_LDSENDABLECLASS =
            ArkOpcodes.CRT_LDSENDABLECLASS;
    static final int CRT_LDSENDABLEEXTERNALMODULEVAR =
            ArkOpcodes.CRT_LDSENDABLEEXTERNALMODULEVAR;
    static final int CRT_NEWSENDABLEENV =
            ArkOpcodes.CRT_NEWSENDABLEENV;
    static final int CRT_STSENDABLEVAR =
            ArkOpcodes.CRT_STSENDABLEVAR;
    static final int CRT_STSENDABLEVARPTR =
            ArkOpcodes.CRT_STSENDABLEVARPTR;
    static final int CRT_LDSENDABLEVAR =
            ArkOpcodes.CRT_LDSENDABLEVAR;
    static final int CRT_LDSENDABLEVARPTR =
            ArkOpcodes.CRT_LDSENDABLEVARPTR;
    static final int CRT_ISTRUE =
            ArkOpcodes.CRT_ISTRUE;
    static final int CRT_ISFALSE =
            ArkOpcodes.CRT_ISFALSE;
    static final int CRT_LDLAZYMODULEVAR =
            ArkOpcodes.CRT_LDLAZYMODULEVAR;
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

    // --- 16-bit variant primary opcodes ---
    static final int MOV_8 = ArkOpcodes.MOV_8;
    static final int MOV_16 = ArkOpcodes.MOV_16;
    static final int JMP_IMM32 = ArkOpcodes.JMP_IMM32;
    static final int JEQZ_IMM32 = ArkOpcodes.JEQZ_IMM32;
    static final int JNEZ_IMM32 = ArkOpcodes.JNEZ_IMM32;
    static final int STOBJBYNAME_16 = ArkOpcodes.STOBJBYNAME_16;
    static final int LDOBJBYNAME_16 = ArkOpcodes.LDOBJBYNAME_16;
    static final int CREATEARRAYWITHBUFFER_16 = ArkOpcodes.CREATEARRAYWITHBUFFER_16;
    static final int STOWNBYNAME_16 = ArkOpcodes.STOWNBYNAME_16;
    static final int LDLEXVAR_8 = ArkOpcodes.LDLEXVAR_8;
    static final int STLEXVAR_8 = ArkOpcodes.STLEXVAR_8;
    static final int TRYLDGLOBALBYNAME_16 = ArkOpcodes.TRYLDGLOBALBYNAME_16;
    static final int TRYSTGLOBALBYNAME_16 = ArkOpcodes.TRYSTGLOBALBYNAME_16;
    static final int LDOBJBYVALUE_16 = ArkOpcodes.LDOBJBYVALUE_16;
    static final int STOBJBYVALUE_16 = ArkOpcodes.STOBJBYVALUE_16;
    static final int LDSUPERBYVALUE_16 = ArkOpcodes.LDSUPERBYVALUE_16;
    static final int LDOBJBYINDEX_16 = ArkOpcodes.LDOBJBYINDEX_16;
    static final int STOBJBYINDEX_16 = ArkOpcodes.STOBJBYINDEX_16;
    static final int LDSUPERBYNAME_16 = ArkOpcodes.LDSUPERBYNAME_16;
    static final int LDTHISBYNAME_16 = ArkOpcodes.LDTHISBYNAME_16;
    static final int STTHISBYNAME_16 = ArkOpcodes.STTHISBYNAME_16;
    static final int LDTHISBYVALUE_16 = ArkOpcodes.LDTHISBYVALUE_16;
    static final int STTHISBYVALUE_16 = ArkOpcodes.STTHISBYVALUE_16;
    static final int CREATEEMPTYARRAY_16 = ArkOpcodes.CREATEEMPTYARRAY_16;
    static final int CREATEOBJECTWITHBUFFER_16 = ArkOpcodes.CREATEOBJECTWITHBUFFER_16;
    static final int NEWOBJRANGE_16 = ArkOpcodes.NEWOBJRANGE_16;
    static final int TYPEOF_16 = ArkOpcodes.TYPEOF_16;
    static final int NEWOBJAPPLY_16 = ArkOpcodes.NEWOBJAPPLY_16;
    static final int DEFINEMETHOD_16 = ArkOpcodes.DEFINEMETHOD_16;
    static final int DEFINEFUNC_16 = ArkOpcodes.DEFINEFUNC_16;
    static final int GETTEMPLATEOBJECT_16 = ArkOpcodes.GETTEMPLATEOBJECT_16;
    static final int SETOBJECTWITHPROTO_16 = ArkOpcodes.SETOBJECTWITHPROTO_16;
    static final int STOWNBYVALUE_16 = ArkOpcodes.STOWNBYVALUE_16;
    static final int STOWNBYINDEX_16 = ArkOpcodes.STOWNBYINDEX_16;
    static final int STSUPERBYNAME_16 = ArkOpcodes.STSUPERBYNAME_16;
    static final int STSUPERBYVALUE_16 = ArkOpcodes.STSUPERBYVALUE_16;
    static final int STOWNBYVALUEWITHNAMESET_16 = ArkOpcodes.STOWNBYVALUEWITHNAMESET_16;
    static final int STOWNBYNAMEWITHNAMESET_16 = ArkOpcodes.STOWNBYNAMEWITHNAMESET_16;
    static final int CREATEREGEXPWITHLITERAL_16 = ArkOpcodes.CREATEREGEXPWITHLITERAL_16;

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
        if (insn.isWide()) {
            return normalizeWideOpcode(insn.getOpcode());
        }
        return normalizeVariantOpcode(insn.getOpcode());
    }

    /**
     * Maps 16-bit variant primary opcodes to their 8-bit equivalents.
     * These opcodes appear as primary opcodes (no 0xFD prefix) but use wider
     * IC slot fields. The decompiler treats them identically to their 8-bit
     * counterparts since operand indices are the same.
     */
    static int normalizeVariantOpcode(int opcode) {
        switch (opcode) {
            case MOV_8:
            case MOV_16:
                return MOV;
            case CREATEEMPTYARRAY_16:
                return CREATEEMPTYARRAY;
            case CREATEARRAYWITHBUFFER_16:
                return CREATEARRAYWITHBUFFER;
            case CREATEOBJECTWITHBUFFER_16:
                return CREATEOBJECTWITHBUFFER;
            case NEWOBJRANGE_16:
                return NEWOBJRANGE;
            case TYPEOF_16:
                return TYPEOF;
            case LDOBJBYVALUE_16:
                return LDOBJBYVALUE;
            case STOBJBYVALUE_16:
                return STOBJBYVALUE;
            case LDSUPERBYVALUE_16:
                return LDSUPERBYVALUE;
            case LDOBJBYINDEX_16:
                return LDOBJBYINDEX;
            case STOBJBYINDEX_16:
                return STOBJBYINDEX;
            case LDLEXVAR_8:
                return LDLEXVAR;
            case STLEXVAR_8:
                return STLEXVAR;
            case TRYLDGLOBALBYNAME_16:
                return TRYLDGLOBALBYNAME;
            case TRYSTGLOBALBYNAME_16:
                return TRYSTGLOBALBYNAME;
            case LDOBJBYNAME_16:
                return LDOBJBYNAME;
            case STOBJBYNAME_16:
                return STOBJBYNAME;
            case LDSUPERBYNAME_16:
                return LDSUPERBYNAME;
            case LDTHISBYNAME_16:
                return LDTHISBYNAME;
            case STTHISBYNAME_16:
                return STTHISBYNAME;
            case LDTHISBYVALUE_16:
                return LDTHISBYVALUE;
            case STTHISBYVALUE_16:
                return STTHISBYVALUE;
            case JMP_IMM32:
                return JMP_IMM8;
            case JEQZ_IMM32:
                return JEQZ_IMM8;
            case JNEZ_IMM32:
                return JNEZ_IMM8;
            case NEWOBJAPPLY_16:
                return NEWOBJAPPLY;
            case DEFINEMETHOD_16:
                return DEFINEMETHOD;
            case DEFINEFUNC_16:
                return DEFINEFUNC;
            case GETTEMPLATEOBJECT_16:
                return GETTEMPLATEOBJECT;
            case SETOBJECTWITHPROTO_16:
                return SETOBJECTWITHPROTO;
            case STOWNBYVALUE_16:
                return STOWNBYVALUE;
            case STOWNBYINDEX_16:
                return STOWNBYINDEX;
            case STOWNBYNAME_16:
                return STOWNBYNAME;
            case STSUPERBYNAME_16:
                return STSUPERBYNAME;
            case STSUPERBYVALUE_16:
                return STSUPERBYVALUE;
            case STOWNBYVALUEWITHNAMESET_16:
                return STOWNBYVALUEWITHNAMESET;
            case STOWNBYNAMEWITHNAMESET_16:
                return STOWNBYNAMEWITHNAMESET;
            case CREATEREGEXPWITHLITERAL_16:
                return CREATEREGEXPWITHLITERAL;
            // Vendor-specific named call opcodes map to their base equivalents
            case CALLTHIS0WITHNAME:
                return CALLTHIS0;
            case CALLTHIS1WITHNAME:
                return CALLTHIS1;
            case CALLTHIS2WITHNAME:
                return CALLTHIS2;
            case CALLTHIS3WITHNAME:
                return CALLTHIS3;
            case CALLTHISRANGEWITHNAME:
                return CALLTHISRANGE;
            default:
                return opcode;
        }
    }

    /**
     * Returns true if the opcode is an unconditional jump.
     *
     * @param opcode the opcode to check
     * @return true if the opcode is jmp
     */
    static boolean isUnconditionalJump(int opcode) {
        return opcode == JMP_IMM8 || opcode == JMP_IMM16 || opcode == JMP_IMM32;
    }

    /**
     * Returns true if the opcode is a conditional branch.
     *
     * @param opcode the opcode to check
     * @return true if the opcode is a conditional branch
     */
    static boolean isConditionalBranch(int opcode) {
        return opcode == JEQZ_IMM8 || opcode == JEQZ_IMM16 || opcode == JEQZ_IMM32
                || opcode == JNEZ_IMM8 || opcode == JNEZ_IMM16 || opcode == JNEZ_IMM32
                || opcode == JEQ_IMM8 || opcode == JEQ_IMM16
                || opcode == JNE_IMM8 || opcode == JNE_IMM16
                || opcode == JEQNULL_IMM8 || opcode == JEQNULL_IMM16
                || opcode == JNENULL_IMM8 || opcode == JNENULL_IMM16
                || opcode == JEQUNDEFINED_IMM8 || opcode == JEQUNDEFINED_IMM16
                || opcode == JNEUNDEFINED_IMM8 || opcode == JNEUNDEFINED_IMM16
                || opcode == JSTRICTEQZ_IMM8 || opcode == JSTRICTEQZ_IMM16
                || opcode == JNSTRICTEQZ_IMM8 || opcode == JNSTRICTEQZ_IMM16
                || opcode == JSTRICTEQNULL_IMM8
                || opcode == JSTRICTEQNULL_IMM16
                || opcode == JNSTRICTEQNULL_IMM8
                || opcode == JNSTRICTEQNULL_IMM16
                || opcode == JSTRICTEQUNDEFINED_IMM16
                || opcode == JNSTRICTEQUNDEFINED_IMM16
                || opcode == JSTRICTEQ_IMM8 || opcode == JSTRICTEQ_IMM16
                || opcode == JNSTRICTEQ_IMM8
                || opcode == JNSTRICTEQ_IMM16;
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

    /**
     * Returns true if the opcode is a known callruntime (0xFB-prefixed)
     * sub-opcode.
     *
     * @param opcode the opcode to check
     * @return true if this is a callruntime sub-opcode
     */
    static boolean isCallRuntimeSubOpcode(int opcode) {
        switch (opcode) {
            case CRT_NOTIFYCONCURRENTRESULT:
            case CRT_DEFINEFIELDBYVALUE:
            case CRT_DEFINEFIELDBYINDEX:
            case CRT_TOPROPERTYKEY:
            case CRT_CREATEPRIVATEPROPERTY:
            case CRT_DEFINEPRIVATEPROPERTY:
            case CRT_CALLINIT:
            case CRT_DEFINESENDABLECLASS:
            case CRT_LDSENDABLECLASS:
            case CRT_LDSENDABLEEXTERNALMODULEVAR:
            case CRT_NEWSENDABLEENV:
            case CRT_STSENDABLEVAR:
            case CRT_STSENDABLEVARPTR:
            case CRT_LDSENDABLEVAR:
            case CRT_LDSENDABLEVARPTR:
            case CRT_ISTRUE:
            case CRT_ISFALSE:
            case CRT_LDLAZYMODULEVAR:
                return true;
            default:
                return false;
        }
    }
}
