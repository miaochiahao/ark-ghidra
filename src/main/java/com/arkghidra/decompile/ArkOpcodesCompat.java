package com.arkghidra.decompile;

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

    private ArkOpcodesCompat() {
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
                || opcode == RETURN || opcode == RETURNUNDEFINED;
    }
}
