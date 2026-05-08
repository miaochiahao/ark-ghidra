package com.arkghidra.disasm;

/**
 * Ark bytecode opcode constants and metadata.
 *
 * <p>Defines every opcode in the Ark ISA (version 13.0.0.0) along with its mnemonic
 * and instruction format. Opcodes are grouped by functional category. The 0xFD prefix
 * byte signals a wide instruction whose second byte is the sub-opcode.
 *
 * <p>Special prefix bytes:
 * <ul>
 *   <li>0xFB — callruntime (reserved)</li>
 *   <li>0xFC — deprecated</li>
 *   <li>0xFD — wide prefix</li>
 *   <li>0xFE — throw</li>
 * </ul>
 */
public final class ArkOpcodes {

    // --- Special prefix bytes ---
    public static final int PREFIX_WIDE = 0xFD;
    public static final int PREFIX_THROW = 0xFE;
    public static final int PREFIX_DEPRECATED = 0xFC;
    public static final int PREFIX_CALLRUNTIME = 0xFB;

    // --- Constant loaders / singletons (NONE format) ---
    public static final int LDUNDEFINED = 0x00;
    public static final int LDNULL = 0x01;
    public static final int LDTRUE = 0x02;
    public static final int LDFALSE = 0x03;
    public static final int CREATEEMPTYOBJECT = 0x04;
    public static final int RETURN = 0x64;
    public static final int RETURNUNDEFINED = 0x65;
    public static final int GETPROPITERATOR = 0x66;
    public static final int POPLEXENV = 0x69;
    public static final int LDNAN = 0x6A;
    public static final int LDINFINITY = 0x6B;
    public static final int GETUNMAPPEDARGS = 0x6C;
    public static final int LDGLOBAL = 0x6D;
    public static final int LDNEWTARGET = 0x6E;
    public static final int LDTHIS = 0x6F;
    public static final int LDHOLE = 0x70;
    public static final int LDSYMBOL = 0xAD;
    public static final int ASYNCFUNCTIONENTER = 0xAE;
    public static final int LDFUNCTION = 0xAF;
    public static final int DEBUGGER = 0xB0;
    public static final int NOP = 0xD5;
    public static final int RESUMEGENERATOR = 0xBF;
    public static final int GETRESUMEMODE = 0xC0;
    public static final int ISTRUE = 0x23;
    public static final int ISFALSE = 0x24;
    public static final int DYNAMICIMPORT = 0xBD;

    // --- Array/object creation with immediate ---
    public static final int CREATEEMPTYARRAY = 0x05;
    public static final int GETTEMPLATEOBJECT = 0x76;
    public static final int COPYRESTARGS = 0xCF;
    public static final int SETGENERATORSTATE = 0xD6;

    // --- Object creation with register args (V8) ---
    public static final int CREATEGENERATOROBJ = 0xB1;
    public static final int CREATEASYNCGENERATOROBJ = 0xB7;

    // --- Two registers (V8_V8) ---
    public static final int CREATEITERRESULTOBJ = 0xB2;

    // --- Load immediate (IMM32 / IMM64) ---
    public static final int LDAI = 0x62;
    public static final int FLDAI = 0x63;

    // --- String constant load (IMM16) ---
    public static final int LDA_STR = 0x3E;

    // --- Register moves ---
    public static final int MOV = 0x44;
    public static final int LDA = 0x60;
    public static final int STA = 0x61;

    // --- Binary operations (IMM8_V8) ---
    public static final int ADD2 = 0x0A;
    public static final int SUB2 = 0x0B;
    public static final int MUL2 = 0x0C;
    public static final int DIV2 = 0x0D;
    public static final int MOD2 = 0x0E;
    public static final int EQ = 0x0F;
    public static final int NOTEQ = 0x10;
    public static final int LESS = 0x11;
    public static final int LESSEQ = 0x12;
    public static final int GREATER = 0x13;
    public static final int GREATEREQ = 0x14;
    public static final int SHL2 = 0x15;
    public static final int SHR2 = 0x16;
    public static final int ASHR2 = 0x17;
    public static final int AND2 = 0x18;
    public static final int OR2 = 0x19;
    public static final int XOR2 = 0x1A;
    public static final int EXP = 0x1B;

    // --- Comparison with register (IMM8_V8) ---
    public static final int ISIN = 0x25;
    public static final int INSTANCEOF = 0x26;
    public static final int STRICTNOTEQ = 0x27;
    public static final int STRICTEQ = 0x28;

    // --- Unary operations (IMM8) ---
    public static final int TYPEOF = 0x1C;
    public static final int TONUMBER = 0x1D;
    public static final int TONUMERIC = 0x1E;
    public static final int NEG = 0x1F;
    public static final int NOT = 0x20;
    public static final int INC = 0x21;
    public static final int DEC = 0x22;

    // --- Call instructions ---
    public static final int CALLARG0 = 0x29;
    public static final int CALLARG1 = 0x2A;
    public static final int CALLARGS2 = 0x2B;
    public static final int CALLARGS3 = 0x2C;
    public static final int CALLTHIS0 = 0x2D;
    public static final int CALLTHIS1 = 0x2E;
    public static final int CALLTHIS2 = 0x2F;
    public static final int CALLTHIS3 = 0x30;

    // --- Lexical variable access (IMM4_IMM4) ---
    public static final int LDLEXVAR = 0x3C;
    public static final int STLEXVAR = 0x3D;

    // --- Property access by name (IMM8_IMM16 or IMM8_IMM16_V8) ---
    public static final int TRYLDGLOBALBYNAME = 0x3F;
    public static final int TRYSTGLOBALBYNAME = 0x40;
    public static final int LDOBJBYNAME = 0x42;
    public static final int STOBJBYNAME = 0x43;
    public static final int LDSUPERBYNAME = 0x46;
    public static final int LDTHISBYNAME = 0x49;
    public static final int STTHISBYNAME = 0x4A;
    public static final int STOWNBYNAME = 0x7A;
    public static final int STSUPERBYNAME = 0xD0;

    // --- Property access by value (IMM8_V8 or IMM8_V8_V8) ---
    public static final int LDOBJBYVALUE = 0x37;
    public static final int STOBJBYVALUE = 0x38;
    public static final int LDSUPERBYVALUE = 0x39;
    public static final int LDTHISBYVALUE = 0x4B;
    public static final int STTHISBYVALUE = 0x4C;
    public static final int STOWNBYVALUE = 0x78;
    public static final int STSUPERBYVALUE = 0xC9;

    // --- Property access by index (IMM8_IMM16 or IMM8_V8_IMM16) ---
    public static final int LDOBJBYINDEX = 0x3A;
    public static final int STOBJBYINDEX = 0x3B;
    public static final int STOWNBYINDEX = 0x79;

    // --- Global variable access (IMM8_IMM16) ---
    public static final int LDGLOBALVAR = 0x41;
    public static final int STGLOBALVAR = 0x7F;
    public static final int STCONSTTOGLOBALRECORD = 0x47;
    public static final int STTOGLOBALRECORD = 0x48;

    // --- Module variable access (IMM8) ---
    public static final int GETMODULENAMESPACE = 0x7B;
    public static final int STMODULEVAR = 0x7C;
    public static final int LDLOCALMODULEVAR = 0x7D;
    public static final int LDEXTERNALMODULEVAR = 0x7E;

    // --- Jump instructions (8-bit offset) ---
    public static final int JMP_IMM8 = 0x4D;
    public static final int JEQZ_IMM8 = 0x4F;
    public static final int JNEZ_IMM8 = 0x51;
    public static final int JSTRICTEQZ_IMM8 = 0x52;
    public static final int JNSTRICTEQZ_IMM8 = 0x53;
    public static final int JEQNULL_IMM8 = 0x54;
    public static final int JNENULL_IMM8 = 0x55;
    public static final int JSTRICTEQNULL_IMM8 = 0x56;
    public static final int JNSTRICTEQNULL_IMM8 = 0x57;
    public static final int JEQUNDEFINED_IMM8 = 0x58;
    public static final int JNEUNDEFINED_IMM8 = 0x59;

    // --- Jump instructions (16-bit offset) ---
    public static final int JMP_IMM16 = 0x4E;
    public static final int JEQZ_IMM16 = 0x50;
    public static final int JNEZ_IMM16 = 0x9B;
    public static final int JSTRICTEQZ_IMM16 = 0x9D;
    public static final int JNSTRICTEQZ_IMM16 = 0x9E;
    public static final int JEQNULL_IMM16 = 0x9F;
    public static final int JNENULL_IMM16 = 0xA0;
    public static final int JSTRICTEQNULL_IMM16 = 0xA1;
    public static final int JNSTRICTEQNULL_IMM16 = 0xA2;
    public static final int JEQUNDEFINED_IMM16 = 0xA3;
    public static final int JNEUNDEFINED_IMM16 = 0xA4;
    public static final int JSTRICTEQUNDEFINED_IMM16 = 0xA5;
    public static final int JNSTRICTEQUNDEFINED_IMM16 = 0xA6;

    // --- Jump with register (8-bit offset) ---
    public static final int JEQ_IMM8 = 0x5C;
    public static final int JNE_IMM8 = 0x5D;
    public static final int JSTRICTEQ_IMM8 = 0x5E;
    public static final int JNSTRICTEQ_IMM8 = 0x5F;

    // --- Jump with register (16-bit offset) ---
    public static final int JEQ_IMM16 = 0xA7;
    public static final int JNE_IMM16 = 0xA8;
    public static final int JSTRICTEQ_IMM16 = 0xA9;
    public static final int JNSTRICTEQ_IMM16 = 0xAA;

    // --- Iterator ---
    public static final int GETITERATOR_IMM8 = 0x67;
    public static final int CLOSEITERATOR_IMM8 = 0x68;
    public static final int GETNEXTPROPNAME = 0x36;
    public static final int GETITERATOR_IMM16 = 0xAB;
    public static final int CLOSEITERATOR_IMM16 = 0xAC;
    public static final int GETASYNCITERATOR = 0xD7;

    // --- Function definition (IMM8_IMM16_IMM8) ---
    public static final int DEFINEFUNC = 0x33;
    public static final int DEFINEMETHOD = 0x34;

    // --- Create with buffer (IMM8_IMM16) ---
    public static final int CREATEARRAYWITHBUFFER = 0x06;
    public static final int CREATEOBJECTWITHBUFFER = 0x07;

    // --- Newobj range (IMM8_IMM8_V8) ---
    public static final int NEWOBJRANGE = 0x08;
    public static final int NEWLEXENV = 0x09;
    public static final int SUPERCALLTHISRANGE = 0x32;
    public static final int CALLTHISRANGE = 0x31;
    public static final int CALLRANGE = 0x73;
    public static final int SUPERCALLARROWRANGE = 0xBB;

    // --- Special operations (IMM8_V8 or IMM8_V8_V8) ---
    public static final int SUPERCALLSPREAD = 0xB9;
    public static final int APPLY = 0xBA;
    public static final int NEWOBJAPPLY = 0xB4;

    // --- Create regexp (IMM8_IMM16_IMM8) ---
    public static final int CREATEREGEXPWITHLITERAL = 0x71;

    // --- Async generator ---
    public static final int ASYNCGENERATORRESOLVE = 0xB8;
    public static final int ASYNCGENERATORREJECT = 0x97;

    // --- Misc (V8) ---
    public static final int DELOBJPROP = 0xC2;
    public static final int SUSPENDGENERATOR = 0xC3;
    public static final int ASYNCFUNCTIONAWAITUNCAUGHT = 0xC4;
    public static final int COPYDATAPROPERTIES = 0xC5;
    public static final int STARRAYSPREAD = 0xC6;
    public static final int ASYNCFUNCTIONRESOLVE = 0xCD;
    public static final int ASYNCFUNCTIONREJECT = 0xCE;

    // --- Set object with proto (IMM8_V8) ---
    public static final int SETOBJECTWITHPROTO = 0x77;

    // --- Object with nameset (IMM8_V8_V8 or IMM8_IMM16_V8) ---
    public static final int STOWNBYVALUEWITHNAMESET = 0x99;
    public static final int STOWNBYNAMEWITHNAMESET = 0x8E;

    // --- Create with excluded keys (IMM8_V8_V8) ---
    public static final int CREATEOBJECTWITHEXCLUDEDKEYS = 0xB3;

    // --- Lex env with name (IMM8_IMM16) ---
    public static final int NEWLEXENVWITHNAME = 0xB6;

    // --- Define getter/setter by value (V8_V8_V8_V8) ---
    public static final int DEFINEGETTERSETTERBYVALUE = 0xBC;

    // --- Ldbigint (IMM16) ---
    public static final int LDBIGINT = 0xD3;

    // --- Private property (IMM8_IMM16_IMM16 or IMM8_IMM16_IMM16_V8) ---
    public static final int LDPRIVATEPROPERTY = 0xD8;
    public static final int STPRIVATEPROPERTY = 0xD9;
    public static final int TESTIN = 0xDA;
    public static final int DEFINEFIELDBYNAME = 0xDB;
    public static final int DEFINEPROPERTYBYNAME = 0xDC;

    // --- Class definition (IMM8_IMM16_IMM16_IMM8_V8) ---
    public static final int DEFINECLASSWITHBUFFER = 0x35;

    // --- Wide (0xFD) sub-opcodes ---
    public static final int WIDE_CREATEEMPTYARRAY = 0x80;
    public static final int WIDE_CREATEARRAYWITHBUFFER = 0x81;
    public static final int WIDE_CREATEOBJECTWITHBUFFER = 0x82;
    public static final int WIDE_NEWOBJRANGE = 0x83;
    public static final int WIDE_TYPEOF = 0x84;
    public static final int WIDE_LDOBJBYVALUE = 0x85;
    public static final int WIDE_STOBJBYVALUE = 0x86;
    public static final int WIDE_LDSUPERBYVALUE = 0x87;
    public static final int WIDE_LDOBJBYINDEX = 0x88;
    public static final int WIDE_STOBJBYINDEX = 0x89;
    public static final int WIDE_LDLEXVAR = 0x8A;
    public static final int WIDE_STLEXVAR = 0x8B;
    public static final int WIDE_TRYLDGLOBALBYNAME = 0x8C;
    public static final int WIDE_TRYSTGLOBALBYNAME = 0x8D;
    public static final int WIDE_LDOBJBYNAME = 0x90;
    public static final int WIDE_STOBJBYNAME = 0x91;
    public static final int WIDE_LDSUPERBYNAME = 0x92;
    public static final int WIDE_LDTHISBYNAME = 0x93;
    public static final int WIDE_STTHISBYNAME = 0x94;
    public static final int WIDE_LDTHISBYVALUE = 0x95;
    public static final int WIDE_STTHISBYVALUE = 0x96;
    public static final int WIDE_DEFINEFUNC = 0x74;
    public static final int WIDE_GETTEMPLATEOBJECT = 0xC1;
    public static final int WIDE_SETOBJECTWITHPROTO = 0xC7;
    public static final int WIDE_STOWNBYVALUE = 0xC8;
    public static final int WIDE_STOWNBYINDEX = 0xCB;
    public static final int WIDE_STOWNBYNAME = 0xCC;
    public static final int WIDE_DEFINEMETHOD = 0xBE;
    public static final int WIDE_SUPERCALLTHISRANGE = 0x75;
    public static final int WIDE_MOV = 0x8F;

    private ArkOpcodes() {
        // utility class — no instances
    }

    /**
     * Returns the mnemonic for the given primary opcode, or {@code "unknown_XX"} if
     * the opcode is not recognized.
     *
     * @param opcode the primary opcode byte (0x00-0xFF)
     * @return the mnemonic string
     */
    public static String getMnemonic(int opcode) {
        switch (opcode & 0xFF) {
            case LDUNDEFINED: return "ldundefined";
            case LDNULL: return "ldnull";
            case LDTRUE: return "ldtrue";
            case LDFALSE: return "ldfalse";
            case CREATEEMPTYOBJECT: return "createemptyobject";
            case CREATEEMPTYARRAY: return "createemptyarray";
            case CREATEARRAYWITHBUFFER: return "createarraywithbuffer";
            case CREATEOBJECTWITHBUFFER: return "createobjectwithbuffer";
            case NEWOBJRANGE: return "newobjrange";
            case NEWLEXENV: return "newlexenv";
            case ADD2: return "add2";
            case SUB2: return "sub2";
            case MUL2: return "mul2";
            case DIV2: return "div2";
            case MOD2: return "mod2";
            case EQ: return "eq";
            case NOTEQ: return "noteq";
            case LESS: return "less";
            case LESSEQ: return "lesseq";
            case GREATER: return "greater";
            case GREATEREQ: return "greatereq";
            case SHL2: return "shl2";
            case SHR2: return "shr2";
            case ASHR2: return "ashr2";
            case AND2: return "and2";
            case OR2: return "or2";
            case XOR2: return "xor2";
            case EXP: return "exp";
            case TYPEOF: return "typeof";
            case TONUMBER: return "tonumber";
            case TONUMERIC: return "tonumeric";
            case NEG: return "neg";
            case NOT: return "not";
            case INC: return "inc";
            case DEC: return "dec";
            case ISTRUE: return "istrue";
            case ISFALSE: return "isfalse";
            case ISIN: return "isin";
            case INSTANCEOF: return "instanceof";
            case STRICTNOTEQ: return "strictnoteq";
            case STRICTEQ: return "stricteq";
            case CALLARG0: return "callarg0";
            case CALLARG1: return "callarg1";
            case CALLARGS2: return "callargs2";
            case CALLARGS3: return "callargs3";
            case CALLTHIS0: return "callthis0";
            case CALLTHIS1: return "callthis1";
            case CALLTHIS2: return "callthis2";
            case CALLTHIS3: return "callthis3";
            case CALLTHISRANGE: return "callthisrange";
            case SUPERCALLTHISRANGE: return "supercallthisrange";
            case DEFINEFUNC: return "definefunc";
            case DEFINEMETHOD: return "definemethod";
            case DEFINECLASSWITHBUFFER: return "defineclasswithbuffer";
            case GETNEXTPROPNAME: return "getnextpropname";
            case LDOBJBYVALUE: return "ldobjbyvalue";
            case STOBJBYVALUE: return "stobjbyvalue";
            case LDSUPERBYVALUE: return "ldsuperbyvalue";
            case LDOBJBYINDEX: return "ldobjbyindex";
            case STOBJBYINDEX: return "stobjbyindex";
            case LDLEXVAR: return "ldlexvar";
            case STLEXVAR: return "stlexvar";
            case LDA_STR: return "lda.str";
            case TRYLDGLOBALBYNAME: return "tryldglobalbyname";
            case TRYSTGLOBALBYNAME: return "trystglobalbyname";
            case LDGLOBALVAR: return "ldglobalvar";
            case LDOBJBYNAME: return "ldobjbyname";
            case STOBJBYNAME: return "stobjbyname";
            case MOV: return "mov";
            case LDA: return "lda";
            case STA: return "sta";
            case LDAI: return "ldai";
            case FLDAI: return "fldai";
            case RETURN: return "return";
            case RETURNUNDEFINED: return "returnundefined";
            case GETPROPITERATOR: return "getpropiterator";
            case GETITERATOR_IMM8: return "getiterator";
            case CLOSEITERATOR_IMM8: return "closeiterator";
            case POPLEXENV: return "poplexenv";
            case LDNAN: return "ldnan";
            case LDINFINITY: return "ldinfinity";
            case GETUNMAPPEDARGS: return "getunmappedargs";
            case LDGLOBAL: return "ldglobal";
            case LDNEWTARGET: return "ldnewtarget";
            case LDTHIS: return "ldthis";
            case LDHOLE: return "ldhole";
            case CREATEREGEXPWITHLITERAL: return "createregexpwithliteral";
            case SETOBJECTWITHPROTO: return "setobjectwithproto";
            case GETTEMPLATEOBJECT: return "gettemplateobject";
            case STOWNBYVALUE: return "stownbyvalue";
            case STOWNBYINDEX: return "stownbyindex";
            case STOWNBYNAME: return "stownbyname";
            case GETMODULENAMESPACE: return "getmodulenamespace";
            case STMODULEVAR: return "stmodulevar";
            case LDLOCALMODULEVAR: return "ldlocalmodulevar";
            case LDEXTERNALMODULEVAR: return "ldexternalmodulevar";
            case STGLOBALVAR: return "stglobalvar";
            case JMP_IMM8: return "jmp";
            case JMP_IMM16: return "jmp";
            case JEQZ_IMM8: return "jeqz";
            case JEQZ_IMM16: return "jeqz";
            case JNEZ_IMM8: return "jnez";
            case JNEZ_IMM16: return "jnez";
            case JSTRICTEQZ_IMM8: return "jstricteqz";
            case JSTRICTEQZ_IMM16: return "jstricteqz";
            case JNSTRICTEQZ_IMM8: return "jnstricteqz";
            case JNSTRICTEQZ_IMM16: return "jnstricteqz";
            case JEQNULL_IMM8: return "jeqnull";
            case JEQNULL_IMM16: return "jeqnull";
            case JNENULL_IMM8: return "jnenull";
            case JNENULL_IMM16: return "jnenull";
            case JSTRICTEQNULL_IMM8: return "jstricteqnull";
            case JSTRICTEQNULL_IMM16: return "jstricteqnull";
            case JNSTRICTEQNULL_IMM8: return "jnstricteqnull";
            case JNSTRICTEQNULL_IMM16: return "jnstricteqnull";
            case JEQUNDEFINED_IMM8: return "jequndefined";
            case JEQUNDEFINED_IMM16: return "jequndefined";
            case JNEUNDEFINED_IMM8: return "jneundefined";
            case JNEUNDEFINED_IMM16: return "jneundefined";
            case JSTRICTEQUNDEFINED_IMM16: return "jstrictequndefined";
            case JNSTRICTEQUNDEFINED_IMM16: return "jnstrictequndefined";
            case JEQ_IMM8: return "jeq";
            case JEQ_IMM16: return "jeq";
            case JNE_IMM8: return "jne";
            case JNE_IMM16: return "jne";
            case JSTRICTEQ_IMM8: return "jstricteq";
            case JSTRICTEQ_IMM16: return "jstricteq";
            case JNSTRICTEQ_IMM8: return "jnstricteq";
            case JNSTRICTEQ_IMM16: return "jnstricteq";
            case LDTHISBYNAME: return "ldthisbyname";
            case STTHISBYNAME: return "stthisbyname";
            case STCONSTTOGLOBALRECORD: return "stconsttoglobalrecord";
            case STTOGLOBALRECORD: return "sttoglobalrecord";
            case LDSUPERBYNAME: return "ldsuperbyname";
            case LDTHISBYVALUE: return "ldthisbyvalue";
            case STTHISBYVALUE: return "stthisbyvalue";
            case CALLRANGE: return "callrange";
            case STOWNBYVALUEWITHNAMESET: return "stownbyvaluewithnameset";
            case STOWNBYNAMEWITHNAMESET: return "stownbynamewithnameset";
            case ASYNCGENERATORREJECT: return "asyncgeneratorreject";
            case RESUMEGENERATOR: return "resumegenerator";
            case GETRESUMEMODE: return "getresumemode";
            case DELOBJPROP: return "delobjprop";
            case SUSPENDGENERATOR: return "suspendgenerator";
            case ASYNCFUNCTIONAWAITUNCAUGHT: return "asyncfunctionawaituncaught";
            case COPYDATAPROPERTIES: return "copydataproperties";
            case STARRAYSPREAD: return "starrayspread";
            case ASYNCFUNCTIONRESOLVE: return "asyncfunctionresolve";
            case ASYNCFUNCTIONREJECT: return "asyncfunctionreject";
            case COPYRESTARGS: return "copyrestargs";
            case STSUPERBYNAME: return "stsuperbyname";
            case STSUPERBYVALUE: return "stsuperbyvalue";
            case CREATEOBJECTWITHEXCLUDEDKEYS: return "createobjectwithexcludedkeys";
            case CREATEGENERATOROBJ: return "creategeneratorobj";
            case CREATEITERRESULTOBJ: return "createiterresultobj";
            case NEWOBJAPPLY: return "newobjapply";
            case SUPERCALLSPREAD: return "supercallspread";
            case APPLY: return "apply";
            case SUPERCALLARROWRANGE: return "supercallarrowrange";
            case DYNAMICIMPORT: return "dynamicimport";
            case DEFINEGETTERSETTERBYVALUE: return "definegettersetterbyvalue";
            case CREATEASYNCGENERATOROBJ: return "createasyncgeneratorobj";
            case ASYNCGENERATORRESOLVE: return "asyncgeneratorresolve";
            case NEWLEXENVWITHNAME: return "newlexenvwithname";
            case LDBIGINT: return "ldbigint";
            case NOP: return "nop";
            case SETGENERATORSTATE: return "setgeneratorstate";
            case GETASYNCITERATOR: return "getasynciterator";
            case LDSYMBOL: return "ldsymbol";
            case ASYNCFUNCTIONENTER: return "asyncfunctionenter";
            case LDFUNCTION: return "ldfunction";
            case DEBUGGER: return "debugger";
            case GETITERATOR_IMM16: return "getiterator";
            case CLOSEITERATOR_IMM16: return "closeiterator";
            case LDPRIVATEPROPERTY: return "ldprivateproperty";
            case STPRIVATEPROPERTY: return "stprivateproperty";
            case TESTIN: return "testin";
            case DEFINEFIELDBYNAME: return "definefieldbyname";
            case DEFINEPROPERTYBYNAME: return "definepropertybyname";
            default: return String.format("unknown_%02X", opcode & 0xFF);
        }
    }

    /**
     * Returns the instruction format for the given primary opcode.
     *
     * @param opcode the primary opcode byte
     * @return the instruction format
     */
    public static ArkInstructionFormat getFormat(int opcode) {
        switch (opcode & 0xFF) {
            // NONE format — opcode only
            case LDUNDEFINED:
            case LDNULL:
            case LDTRUE:
            case LDFALSE:
            case CREATEEMPTYOBJECT:
            case RETURN:
            case RETURNUNDEFINED:
            case GETPROPITERATOR:
            case POPLEXENV:
            case LDNAN:
            case LDINFINITY:
            case GETUNMAPPEDARGS:
            case LDGLOBAL:
            case LDNEWTARGET:
            case LDTHIS:
            case LDHOLE:
            case ISTRUE:
            case ISFALSE:
            case LDSYMBOL:
            case ASYNCFUNCTIONENTER:
            case LDFUNCTION:
            case DEBUGGER:
            case NOP:
            case RESUMEGENERATOR:
            case GETRESUMEMODE:
            case DYNAMICIMPORT:
                return ArkInstructionFormat.NONE;

            // V8 format — opcode + register
            case LDA:
            case STA:
            case CREATEGENERATOROBJ:
            case CREATEASYNCGENERATOROBJ:
            case DELOBJPROP:
            case SUSPENDGENERATOR:
            case ASYNCFUNCTIONAWAITUNCAUGHT:
            case COPYDATAPROPERTIES:
            case ASYNCFUNCTIONRESOLVE:
            case ASYNCFUNCTIONREJECT:
            case ASYNCGENERATORREJECT:
            case GETNEXTPROPNAME:
                return ArkInstructionFormat.V8;

            // V8_V8 format — opcode + 2 registers
            case MOV:
            case CREATEITERRESULTOBJ:
            case STARRAYSPREAD:
                return ArkInstructionFormat.V8_V8;

            // V8_V8_V8 format — opcode + 3 registers
            case ASYNCGENERATORRESOLVE:
                return ArkInstructionFormat.V8_V8_V8;

            // V8_V8_V8_V8 format — opcode + 4 registers
            case DEFINEGETTERSETTERBYVALUE:
                return ArkInstructionFormat.V8_V8_V8_V8;

            // IMM8 format — opcode + 8-bit immediate
            case CREATEEMPTYARRAY:
            case TYPEOF:
            case TONUMBER:
            case TONUMERIC:
            case NEG:
            case NOT:
            case INC:
            case DEC:
            case CALLARG0:
            case NEWLEXENV:
            case GETMODULENAMESPACE:
            case STMODULEVAR:
            case LDLOCALMODULEVAR:
            case LDEXTERNALMODULEVAR:
            case GETTEMPLATEOBJECT:
            case COPYRESTARGS:
            case SETGENERATORSTATE:
            case GETASYNCITERATOR:
                return ArkInstructionFormat.IMM8;

            // IMM8_V8 format — opcode + imm8 + register
            case ADD2:
            case SUB2:
            case MUL2:
            case DIV2:
            case MOD2:
            case EQ:
            case NOTEQ:
            case LESS:
            case LESSEQ:
            case GREATER:
            case GREATEREQ:
            case SHL2:
            case SHR2:
            case ASHR2:
            case AND2:
            case OR2:
            case XOR2:
            case EXP:
            case ISIN:
            case INSTANCEOF:
            case STRICTNOTEQ:
            case STRICTEQ:
            case CALLARG1:
            case CALLTHIS0:
            case LDOBJBYVALUE:
            case LDSUPERBYVALUE:
            case LDTHISBYVALUE:
            case GETITERATOR_IMM8:
            case SETOBJECTWITHPROTO:
            case SUPERCALLSPREAD:
            case NEWOBJAPPLY:
            case STOWNBYVALUEWITHNAMESET:
            case CREATEOBJECTWITHEXCLUDEDKEYS:
                return ArkInstructionFormat.IMM8_V8;

            // IMM8_V8_V8 format
            case CALLARGS2:
            case CALLTHIS1:
            case STOBJBYVALUE:
            case STTHISBYVALUE:
            case STOWNBYVALUE:
            case STSUPERBYVALUE:
            case CLOSEITERATOR_IMM8:
            case APPLY:
                return ArkInstructionFormat.IMM8_V8_V8;

            // IMM8_V8_V8_V8 format
            case CALLARGS3:
            case CALLTHIS2:
                return ArkInstructionFormat.IMM8_V8_V8_V8;

            // IMM16 format — opcode + 16-bit immediate
            case LDA_STR:
            case LDBIGINT:
                return ArkInstructionFormat.IMM16;

            // IMM4_IMM4 format — packed 4-bit pair
            case LDLEXVAR:
            case STLEXVAR:
                return ArkInstructionFormat.IMM4_IMM4;

            // IMM8_IMM16 format — opcode + imm8 + imm16
            case CREATEARRAYWITHBUFFER:
            case CREATEOBJECTWITHBUFFER:
            case TRYLDGLOBALBYNAME:
            case TRYSTGLOBALBYNAME:
            case LDGLOBALVAR:
            case STGLOBALVAR:
            case STCONSTTOGLOBALRECORD:
            case STTOGLOBALRECORD:
            case LDOBJBYINDEX:
            case LDOBJBYNAME:
            case LDSUPERBYNAME:
            case LDTHISBYNAME:
            case NEWLEXENVWITHNAME:
                return ArkInstructionFormat.IMM8_IMM16;

            // IMM8_IMM16_V8 format — opcode + imm8 + imm16 + reg
            case STOBJBYNAME:
            case STTHISBYNAME:
            case STOWNBYNAME:
            case STSUPERBYNAME:
            case STOWNBYNAMEWITHNAMESET:
                return ArkInstructionFormat.IMM8_IMM16_V8;

            // IMM8_IMM16_IMM8 format — opcode + imm8 + imm16 + imm8
            case DEFINEFUNC:
            case DEFINEMETHOD:
            case CREATEREGEXPWITHLITERAL:
                return ArkInstructionFormat.IMM8_IMM16_IMM8;

            // IMM8_IMM16_IMM16 format — opcode + imm8 + imm16 + imm16
            case LDPRIVATEPROPERTY:
            case TESTIN:
                return ArkInstructionFormat.IMM8_IMM16_IMM16;

            // IMM8_IMM16_IMM16_V8 format
            case STPRIVATEPROPERTY:
                return ArkInstructionFormat.IMM8_IMM16_IMM16_V8;

            // IMM8_IMM16_V8 (for definefieldbyname, definepropertybyname)
            case DEFINEFIELDBYNAME:
            case DEFINEPROPERTYBYNAME:
                return ArkInstructionFormat.IMM8_IMM16_V8;

            // IMM8_IMM8_V8 format
            case NEWOBJRANGE:
            case SUPERCALLTHISRANGE:
            case CALLTHISRANGE:
            case CALLRANGE:
            case SUPERCALLARROWRANGE:
                return ArkInstructionFormat.IMM8_IMM8_V8;

            // IMM32 format — opcode + 32-bit signed immediate
            case LDAI:
                return ArkInstructionFormat.IMM32;

            // IMM64 format — opcode + 64-bit signed immediate
            case FLDAI:
                return ArkInstructionFormat.IMM64;

            // IMM8_V8_IMM16 format
            case STOBJBYINDEX:
            case STOWNBYINDEX:
                return ArkInstructionFormat.IMM8_V8_IMM16;

            // IMM8_IMM16_IMM16_IMM8_V8
            case DEFINECLASSWITHBUFFER:
                return ArkInstructionFormat.IMM8_IMM16_IMM16_V8;

            // --- Jump instructions (signed offset) ---
            case JMP_IMM8:
            case JEQZ_IMM8:
            case JNEZ_IMM8:
            case JSTRICTEQZ_IMM8:
            case JNSTRICTEQZ_IMM8:
            case JEQNULL_IMM8:
            case JNENULL_IMM8:
            case JSTRICTEQNULL_IMM8:
            case JNSTRICTEQNULL_IMM8:
            case JEQUNDEFINED_IMM8:
            case JNEUNDEFINED_IMM8:
                return ArkInstructionFormat.IMM8;

            case JMP_IMM16:
            case JEQZ_IMM16:
            case JNEZ_IMM16:
            case JSTRICTEQZ_IMM16:
            case JNSTRICTEQZ_IMM16:
            case JEQNULL_IMM16:
            case JNENULL_IMM16:
            case JSTRICTEQNULL_IMM16:
            case JNSTRICTEQNULL_IMM16:
            case JEQUNDEFINED_IMM16:
            case JNEUNDEFINED_IMM16:
            case JSTRICTEQUNDEFINED_IMM16:
            case JNSTRICTEQUNDEFINED_IMM16:
                return ArkInstructionFormat.IMM16;

            // V8_IMM8 format — register + signed 8-bit offset
            case JEQ_IMM8:
            case JNE_IMM8:
            case JSTRICTEQ_IMM8:
            case JNSTRICTEQ_IMM8:
                return ArkInstructionFormat.V8_IMM8;

            // V8_IMM16 format — register + signed 16-bit offset
            case JEQ_IMM16:
            case JNE_IMM16:
            case JSTRICTEQ_IMM16:
            case JNSTRICTEQ_IMM16:
                return ArkInstructionFormat.V8_IMM16;

            // GETITERATOR / CLOSEITERATOR with 16-bit
            case GETITERATOR_IMM16:
                return ArkInstructionFormat.IMM16;
            case CLOSEITERATOR_IMM16:
                return ArkInstructionFormat.IMM16_V8;

            // Wide prefix
            case PREFIX_WIDE:
                return ArkInstructionFormat.NONE;

            default:
                return ArkInstructionFormat.UNKNOWN;
        }
    }

    /**
     * Returns the mnemonic for a wide (0xFD-prefixed) sub-opcode.
     *
     * @param subOpcode the sub-opcode byte following the 0xFD prefix
     * @return the mnemonic string
     */
    public static String getWideMnemonic(int subOpcode) {
        switch (subOpcode & 0xFF) {
            case WIDE_CREATEEMPTYARRAY: return "createemptyarray";
            case WIDE_CREATEARRAYWITHBUFFER: return "createarraywithbuffer";
            case WIDE_CREATEOBJECTWITHBUFFER: return "createobjectwithbuffer";
            case WIDE_NEWOBJRANGE: return "newobjrange";
            case WIDE_TYPEOF: return "typeof";
            case WIDE_LDOBJBYVALUE: return "ldobjbyvalue";
            case WIDE_STOBJBYVALUE: return "stobjbyvalue";
            case WIDE_LDSUPERBYVALUE: return "ldsuperbyvalue";
            case WIDE_LDOBJBYINDEX: return "ldobjbyindex";
            case WIDE_STOBJBYINDEX: return "stobjbyindex";
            case WIDE_LDLEXVAR: return "ldlexvar";
            case WIDE_STLEXVAR: return "stlexvar";
            case WIDE_TRYLDGLOBALBYNAME: return "tryldglobalbyname";
            case WIDE_TRYSTGLOBALBYNAME: return "trystglobalbyname";
            case WIDE_LDOBJBYNAME: return "ldobjbyname";
            case WIDE_STOBJBYNAME: return "stobjbyname";
            case WIDE_LDSUPERBYNAME: return "ldsuperbyname";
            case WIDE_LDTHISBYNAME: return "ldthisbyname";
            case WIDE_STTHISBYNAME: return "stthisbyname";
            case WIDE_LDTHISBYVALUE: return "ldthisbyvalue";
            case WIDE_STTHISBYVALUE: return "stthisbyvalue";
            case WIDE_DEFINEFUNC: return "definefunc";
            case WIDE_GETTEMPLATEOBJECT: return "gettemplateobject";
            case WIDE_SETOBJECTWITHPROTO: return "setobjectwithproto";
            case WIDE_STOWNBYVALUE: return "stownbyvalue";
            case WIDE_STOWNBYINDEX: return "stownbyindex";
            case WIDE_STOWNBYNAME: return "stownbyname";
            case WIDE_DEFINEMETHOD: return "definemethod";
            case WIDE_SUPERCALLTHISRANGE: return "supercallthisrange";
            case WIDE_MOV: return "mov";
            default: return String.format("wide_unknown_%02X", subOpcode & 0xFF);
        }
    }

    /**
     * Returns the instruction format for a wide (0xFD-prefixed) sub-opcode.
     *
     * @param subOpcode the sub-opcode byte following the 0xFD prefix
     * @return the instruction format
     */
    public static ArkInstructionFormat getWideFormat(int subOpcode) {
        switch (subOpcode & 0xFF) {
            case WIDE_CREATEEMPTYARRAY:
            case WIDE_TYPEOF:
            case WIDE_GETTEMPLATEOBJECT:
                return ArkInstructionFormat.WIDE_IMM16;

            case WIDE_CREATEARRAYWITHBUFFER:
            case WIDE_CREATEOBJECTWITHBUFFER:
            case WIDE_LDLEXVAR:
            case WIDE_STLEXVAR:
            case WIDE_TRYLDGLOBALBYNAME:
            case WIDE_TRYSTGLOBALBYNAME:
            case WIDE_LDOBJBYNAME:
            case WIDE_LDSUPERBYNAME:
            case WIDE_LDTHISBYNAME:
            case WIDE_LDOBJBYINDEX:
                return ArkInstructionFormat.WIDE_IMM16_IMM16;

            case WIDE_LDOBJBYVALUE:
            case WIDE_LDSUPERBYVALUE:
            case WIDE_SETOBJECTWITHPROTO:
            case WIDE_LDTHISBYVALUE:
            case WIDE_STTHISBYVALUE:
                return ArkInstructionFormat.WIDE_IMM16_V8;

            case WIDE_STOBJBYVALUE:
            case WIDE_STOWNBYVALUE:
                return ArkInstructionFormat.WIDE_IMM16_V8_V8;

            case WIDE_MOV:
                return ArkInstructionFormat.WIDE_V8_V8;

            case WIDE_NEWOBJRANGE:
            case WIDE_SUPERCALLTHISRANGE:
                return ArkInstructionFormat.WIDE_IMM16_IMM8_V8;

            case WIDE_STOBJBYINDEX:
            case WIDE_STOWNBYINDEX:
                return ArkInstructionFormat.WIDE_IMM16_V8_IMM16;

            case WIDE_STOBJBYNAME:
            case WIDE_STTHISBYNAME:
            case WIDE_STOWNBYNAME:
                return ArkInstructionFormat.WIDE_IMM16_IMM16_V8;

            case WIDE_DEFINEFUNC:
            case WIDE_DEFINEMETHOD:
                return ArkInstructionFormat.WIDE_IMM16_IMM16_IMM8;

            default:
                return ArkInstructionFormat.UNKNOWN;
        }
    }
}
