package com.arkghidra;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Builder utility for constructing realistic ABC binary fixtures for integration tests.
 *
 * <p>Produces a valid byte[] that can be parsed by {@code AbcFile.parse()}.
 * The fixture supports multiple classes with inheritance, methods with various
 * bytecode patterns, fields with different access flags, string constants,
 * and literal arrays.</p>
 */
public class AbcTestFixture {

    private static final int BUFFER_SIZE = 8192;

    private ByteBuffer bb;

    // Layout offsets — computed during build
    private int stringAreaOff;
    private int classAreaOff;
    private int codeAreaOff;
    private int regionHeaderOff;
    private int classIdxOff;
    private int literalArrayIdxOff;
    private int lnpIdxOff;
    private int classRegionIdxOff;
    private int methodRegionIdxOff;
    private int fieldRegionIdxOff;
    private int protoIdxOff;
    private int protoAreaOff;
    private int literalArrayAreaOff;
    private int fileSize;

    // String table tracking
    private int stringCursor;

    // Class descriptors for building
    private int numClasses;
    private int numMethods;
    private int numFields;

    // Recorded string offsets for methods/fields
    private int[] methodNameStringOff;
    private int[] classNameStringOff;
    private int[] fieldNameStringOff;

    // Code offsets for each method
    private int[] methodCodeOff;

    // Code byte sizes for each method
    private int[] methodCodeSize;

    public AbcTestFixture() {
        reset();
    }

    /**
     * Resets the fixture for reuse.
     */
    public final void reset() {
        bb = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        stringCursor = 0;
        numClasses = 0;
        numMethods = 0;
        numFields = 0;
    }

    /**
     * Encodes a value as ULEB128.
     */
    public static byte[] uleb128(long value) {
        byte[] buf = new byte[10];
        int i = 0;
        do {
            byte b = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                b |= 0x80;
            }
            buf[i++] = b;
        } while (value != 0);
        byte[] result = new byte[i];
        System.arraycopy(buf, 0, result, 0, i);
        return result;
    }

    /**
     * Encodes an MUTF-8 string with ULEB128 length header and null terminator.
     */
    public static byte[] mutf8String(String s) {
        byte[] strBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] header = uleb128(((long) s.length() << 1) | 1);
        byte[] result = new byte[header.length + strBytes.length + 1];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(strBytes, 0, result, header.length, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    /**
     * Encodes a 16-bit little-endian value.
     */
    public static byte[] le16(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }

    /**
     * Encodes a 32-bit little-endian value.
     */
    public static byte[] le32(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }

    /**
     * Concatenates multiple byte arrays.
     */
    public static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) {
            total += a.length;
        }
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    /**
     * Builds the bytecode for a simple load-and-return method:
     * ldai &lt;value&gt;, sta v0, lda v0, return
     *
     * @param value the 32-bit immediate to load
     * @return bytecode bytes
     */
    public static byte[] simpleMethodCode(int value) {
        return concat(
            ldai(value),              // ldai <value>
            new byte[] {0x61, 0x00},  // sta v0
            new byte[] {0x60, 0x00},  // lda v0
            new byte[] {0x64}         // return
        );
    }

    /**
     * Builds the bytecode for an arithmetic method: add two values.
     * lda v0, add2 0x0, v1, sta v2, return
     *
     * @return bytecode bytes
     */
    public static byte[] arithmeticMethodCode() {
        return concat(
            new byte[] {0x60, 0x00},        // lda v0
            new byte[] {0x0A, 0x00, 0x01},  // add2 0x0, v1
            new byte[] {0x61, 0x02},        // sta v2
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a subtract method.
     * lda v0, sub2 0x0, v1, sta v2, return
     *
     * @return bytecode bytes
     */
    public static byte[] subtractMethodCode() {
        return concat(
            new byte[] {0x60, 0x00},        // lda v0
            new byte[] {0x0B, 0x00, 0x01},  // sub2 0x0, v1
            new byte[] {0x61, 0x02},        // sta v2
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a multiply method.
     * lda v0, mul2 0x0, v1, sta v2, return
     *
     * @return bytecode bytes
     */
    public static byte[] multiplyMethodCode() {
        return concat(
            new byte[] {0x60, 0x00},        // lda v0
            new byte[] {0x0C, 0x00, 0x01},  // mul2 0x0, v1
            new byte[] {0x61, 0x02},        // sta v2
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a conditional branch (if/else) method:
     * ldai 0, jeqz +8, ldai 1, jmp +4, ldai 2, return
     *
     * @return bytecode bytes
     */
    public static byte[] conditionalBranchMethodCode() {
        return concat(
            new byte[] {0x62}, le32(0),     // ldai 0
            new byte[] {0x4F, 0x08},        // jeqz +8 (skip to ldai 2)
            new byte[] {0x62}, le32(1),     // ldai 1
            new byte[] {0x4D, 0x04},        // jmp +4 (skip to return)
            new byte[] {0x62}, le32(2),     // ldai 2
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a while-loop pattern:
     *   loop_start: ldai 10, jeqz +14 (to return)
     *               lda v0, inc 0x0, sta v0
     *               jmp -11 (to loop_start)
     *   return
     *
     * <p>Byte layout:
     * <pre>
     *  0: ldai 10           (5 bytes)
     *  5: jeqz +14          (2 bytes) -> target = 21
     *  7: lda v0            (2 bytes)
     *  9: inc 0x0           (2 bytes)
     * 11: sta v0            (2 bytes)
     * 13: jmp -11           (2 bytes) -> target = 2 = loop_start at ldai...
     * </pre>
     * Wait, we need to recalculate. Let's be precise:
     *
     * Actually, for simplicity, let's build a clean loop:
     *   offset 0: ldai 10         (5 bytes, total=5)
     *   offset 5: jeqz +14        (2 bytes, total=7) -> jumps to offset 21
     *   offset 7: lda v0          (2 bytes, total=9)
     *   offset 9: inc 0x0         (2 bytes, total=11)
     *   offset 11: sta v0         (2 bytes, total=13)
     *   offset 13: jmp -11        (2 bytes, total=15) -> jumps to offset 2
     *
     * That doesn't make sense. Let's do:
     *   offset 0: lda v0          (2 bytes)
     *   offset 2: jeqz +12        (2 bytes) -> target offset 16
     *   offset 4: lda v0          (2 bytes)
     *   offset 6: inc 0x0         (2 bytes)
     *   offset 8: sta v0          (2 bytes)
     *   offset 10: jmp -10        (2 bytes) -> target offset 0 (wait, -10 from 10=0? No, -10 from next=12->2)
     *
     * Let's be cleaner:
     * </pre>
     *
     * @return bytecode bytes
     */
    public static byte[] loopMethodCode() {
        // offset 0: lda v0          (2 bytes) -> total 2
        // offset 2: jeqz +12        (2 bytes) -> target offset 16
        // offset 4: lda v0          (2 bytes) -> total 6
        // offset 6: inc 0x0         (2 bytes) -> total 8
        // offset 8: sta v0          (2 bytes) -> total 10
        // offset 10: jmp -10        (2 bytes) -> target offset 0 (next_pc=12, 12+(-10)=2... hmm)
        // Actually jmp offset is relative to the jmp instruction start:
        // jmp at offset 10, the signed offset is relative to the offset of jmp instruction.
        // Let's just use jmp -10 which means: pc=10, target = 10 + (-10) = 0.
        // Wait, in Ark bytecode, jump offsets are relative to the current instruction's offset.
        // So jmp at offset 10 with offset -10 means target = 10 + (-10) = 0. But actually
        // jmp is IMM8 format so the operand is the signed offset from the instruction start.
        // Let's just use a clean layout:
        //
        // offset 0: lda v0         (2 bytes)
        // offset 2: jeqz +8        (2 bytes) -> target = 2+8 = 10
        // offset 4: inc 0x0        (2 bytes)
        // offset 6: sta v0         (2 bytes)
        // offset 8: jmp -8         (2 bytes) -> target = 8+(-8) = 0
        // offset 10: return         (1 byte)
        return concat(
            new byte[] {0x60, 0x00},        // lda v0
            new byte[] {0x4F, 0x08},        // jeqz +8 -> target offset 10
            new byte[] {0x21, 0x00},        // inc 0x0
            new byte[] {0x61, 0x00},        // sta v0
            new byte[] {0x4D, (byte) 0xF8}, // jmp -8 -> target offset 0
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a try/catch pattern. The code section will
     * include try block metadata.
     *
     * <p>Instruction layout:
     * <pre>
     * 0: ldai 42          (5 bytes)
     * 5: sta v0            (2 bytes)
     * 7: ldai 0            (5 bytes)
     * 12: return            (1 byte)
     * </pre>
     *
     * <p>The try/catch metadata is appended after the instructions in the code section.
     *
     * @return bytecode bytes (instructions only; try/catch encoded separately in code section)
     */
    public static byte[] tryCatchInstructions() {
        return concat(
            new byte[] {0x62}, le32(42),    // ldai 42
            new byte[] {0x61, 0x00},        // sta v0
            new byte[] {0x62}, le32(0),     // ldai 0
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a constructor method.
     * ldthis, sta v0, return
     *
     * @return bytecode bytes
     */
    public static byte[] constructorMethodCode() {
        return concat(
            new byte[] {0x6F},              // ldthis
            new byte[] {0x61, 0x00},        // sta v0
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a method that calls another function:
     * lda v0, callarg0 0x05, sta v1, return
     *
     * @return bytecode bytes
     */
    public static byte[] callMethodCode() {
        return concat(
            new byte[] {0x60, 0x00},        // lda v0
            new byte[] {0x29, 0x05},        // callarg0 0x05
            new byte[] {0x61, 0x01},        // sta v1
            new byte[] {0x64}               // return
        );
    }

    /**
     * Builds the bytecode for a property access method:
     * lda v0, ldobjbyname 0x01, 0x0001, stobjbyname 0x01, 0x0002, v1, return
     *
     * @return bytecode bytes
     */
    public static byte[] propertyAccessMethodCode() {
        return concat(
            new byte[] {0x60, 0x00},                 // lda v0
            new byte[] {0x42, 0x01}, le16(0x0001),   // ldobjbyname 0x01, 0x0001
            new byte[] {0x43, 0x01}, le16(0x0002), new byte[] {0x01}, // stobjbyname 0x01, 0x0002, v1
            new byte[] {0x64}                        // return
        );
    }

    /**
     * Builds the bytecode for a method using wide (0xFD prefix) instructions.
     * 0xFD 0x90 0x0001 0x0042 (wide ldobjbyname 0x0001, 0x0042), return
     *
     * @return bytecode bytes
     */
    public static byte[] wideInstructionMethodCode() {
        return concat(
            new byte[] {(byte) 0xFD, (byte) 0x90}, le16(0x0001), le16(0x0042), // wide ldobjbyname
            new byte[] {0x64}  // return
        );
    }

    /**
     * Encodes the LDAI instruction (load 32-bit immediate into accumulator).
     *
     * @param value the 32-bit signed value
     * @return bytecode bytes for the instruction
     */
    public static byte[] ldai(int value) {
        return concat(new byte[] {0x62}, le32(value));
    }

    /**
     * Builds a comprehensive ABC binary with:
     * <ul>
     *   <li>2 classes (BaseClass and ChildClass extending BaseClass)</li>
     *   <li>Multiple methods with different bytecode patterns</li>
     *   <li>Fields with various access flags</li>
     *   <li>String constants</li>
     *   <li>One literal array</li>
     * </ul>
     *
     * @return a valid byte[] that AbcFile.parse() can correctly parse
     */
    public byte[] buildComprehensiveAbc() {
        // Layout with generous spacing to avoid overlaps:
        //   [0, 60)                Header (60 bytes)
        //   [60, 200)              Padding / reserved
        //   [200, 600)             String area
        //   [600, 1600)            Class area (2 classes with fields and methods)
        //   [1600, 3600)           Code area (7 methods, 200 bytes each)
        //   [3600, 3700)           Proto area
        //   [3700, 3800)           Literal array area
        //   [3800, 3900)           Region headers
        //   [3900, 4100)           Index arrays (class, method, field, proto indices)
        //   [4100, 8192)           Padding

        stringAreaOff = 200;
        classAreaOff = 600;
        codeAreaOff = 1600;
        protoAreaOff = 3600;
        literalArrayAreaOff = 3700;
        regionHeaderOff = 3800;
        classIdxOff = 3900;
        methodRegionIdxOff = 3920;
        fieldRegionIdxOff = 3950;
        protoIdxOff = 3980;
        classRegionIdxOff = 4000;
        literalArrayIdxOff = 4020;
        lnpIdxOff = 4040;
        fileSize = BUFFER_SIZE;

        numClasses = 2;
        // Class 0 (BaseClass): 3 methods (constructor, simpleReturn, arithmetic)
        // Class 1 (ChildClass): 4 methods (constructor, conditional, loop, callArg)
        numMethods = 7;
        // Class 0: 2 fields (value, name)
        // Class 1: 1 field (tag)
        numFields = 3;

        classNameStringOff = new int[numClasses];
        methodNameStringOff = new int[numMethods];
        fieldNameStringOff = new int[numFields];
        methodCodeOff = new int[numMethods];
        methodCodeSize = new int[numMethods];

        // --- Write string area ---
        writeStrings();

        // --- Write class definitions ---
        writeClasses();

        // --- Write code sections ---
        writeCodeSections();

        // --- Write proto area ---
        writeProtos();

        // --- Write literal array area ---
        writeLiteralArrays();

        // --- Write region headers ---
        writeRegionHeaders();

        // --- Write index arrays ---
        writeIndexArrays();

        // --- Write header ---
        writeHeader();

        bb.position(0);
        byte[] result = new byte[fileSize];
        bb.get(result);
        return result;
    }

    private void writeStrings() {
        bb.position(stringAreaOff);
        stringCursor = stringAreaOff;

        // Class names
        classNameStringOff[0] = stringCursor;
        byte[] cls0Name = mutf8String("Lcom/example/BaseClass;");
        bb.put(cls0Name);
        stringCursor += cls0Name.length;

        classNameStringOff[1] = stringCursor;
        byte[] cls1Name = mutf8String("Lcom/example/ChildClass;");
        bb.put(cls1Name);
        stringCursor += cls1Name.length;

        // Method names (in order: BaseClass constructor, simpleReturn, arithmetic,
        //                     ChildClass constructor, conditional, loop, callArg)
        String[] methodNames = {
            "<init>",
            "simpleReturn",
            "arithmetic",
            "<init>",
            "conditional",
            "loopMethod",
            "callArg"
        };
        for (int i = 0; i < methodNames.length; i++) {
            methodNameStringOff[i] = stringCursor;
            byte[] nameBytes = mutf8String(methodNames[i]);
            bb.put(nameBytes);
            stringCursor += nameBytes.length;
        }

        // Field names
        String[] fieldNames = {"value", "name", "tag"};
        for (int i = 0; i < fieldNames.length; i++) {
            fieldNameStringOff[i] = stringCursor;
            byte[] nameBytes = mutf8String(fieldNames[i]);
            bb.put(nameBytes);
            stringCursor += nameBytes.length;
        }
    }

    private void writeClasses() {
        // Class 0: BaseClass
        int class0Off = classAreaOff;
        bb.position(class0Off);
        bb.put(mutf8String("Lcom/example/BaseClass;"));
        bb.putInt(0);                   // superClassOff = 0 (no superclass)
        bb.put(uleb128(0x0001));        // accessFlags = ACC_PUBLIC
        bb.put(uleb128(2));             // numFields = 2
        bb.put(uleb128(3));             // numMethods = 3
        bb.put(new byte[] {0x00});      // end tag values

        // Field 0: value (ACC_PUBLIC)
        writeField(0, 0, fieldNameStringOff[0], 0x0001);
        // Field 1: name (ACC_PUBLIC)
        writeField(0, 1, fieldNameStringOff[1], 0x0001);

        // Method 0: <init> (constructor)
        methodCodeOff[0] = codeAreaOff;
        byte[] ctorCode = constructorMethodCode();
        methodCodeSize[0] = ctorCode.length;
        writeMethod(0, 0, methodNameStringOff[0], 0x0001, methodCodeOff[0]);

        // Method 1: simpleReturn
        methodCodeOff[1] = codeAreaOff + 100;
        byte[] simpleCode = simpleMethodCode(42);
        methodCodeSize[1] = simpleCode.length;
        writeMethod(0, 0, methodNameStringOff[1], 0x0001, methodCodeOff[1]);

        // Method 2: arithmetic
        methodCodeOff[2] = codeAreaOff + 200;
        byte[] arithCode = arithmeticMethodCode();
        methodCodeSize[2] = arithCode.length;
        writeMethod(0, 0, methodNameStringOff[2], 0x0001, methodCodeOff[2]);

        // Class 1: ChildClass (extends BaseClass)
        int class1Off = classAreaOff + 400;
        bb.position(class1Off);
        bb.put(mutf8String("Lcom/example/ChildClass;"));
        bb.putInt(class0Off);           // superClassOff = BaseClass offset
        bb.put(uleb128(0x0001));        // accessFlags = ACC_PUBLIC
        bb.put(uleb128(1));             // numFields = 1
        bb.put(uleb128(4));             // numMethods = 4
        bb.put(new byte[] {0x00});      // end tag values

        // Field 2: tag (ACC_PRIVATE)
        writeField(1, 0, fieldNameStringOff[2], 0x0002);

        // Method 3: <init> (ChildClass constructor)
        methodCodeOff[3] = codeAreaOff + 300;
        byte[] ctor2Code = constructorMethodCode();
        methodCodeSize[3] = ctor2Code.length;
        writeMethod(1, 0, methodNameStringOff[3], 0x0001, methodCodeOff[3]);

        // Method 4: conditional
        methodCodeOff[4] = codeAreaOff + 400;
        byte[] condCode = conditionalBranchMethodCode();
        methodCodeSize[4] = condCode.length;
        writeMethod(1, 0, methodNameStringOff[4], 0x0001, methodCodeOff[4]);

        // Method 5: loopMethod
        methodCodeOff[5] = codeAreaOff + 500;
        byte[] loopCode = loopMethodCode();
        methodCodeSize[5] = loopCode.length;
        writeMethod(1, 0, methodNameStringOff[5], 0x0001, methodCodeOff[5]);

        // Method 6: callArg
        methodCodeOff[6] = codeAreaOff + 600;
        byte[] callCode = callMethodCode();
        methodCodeSize[6] = callCode.length;
        writeMethod(1, 0, methodNameStringOff[6], 0x0001, methodCodeOff[6]);
    }

    private void writeField(int classIdx, int typeIdx, int nameOff, long accessFlags) {
        bb.putShort((short) classIdx);
        bb.putShort((short) typeIdx);
        bb.putInt(nameOff);
        bb.put(uleb128(accessFlags));
        bb.put(new byte[] {0x00}); // end tag values
    }

    private void writeMethod(int classIdx, int protoIdx, int nameOff,
            long accessFlags, int codeOff) {
        bb.putShort((short) classIdx);
        bb.putShort((short) protoIdx);
        bb.putInt(nameOff);
        bb.put(uleb128(accessFlags));
        // Method tags: tag 0x01 = code offset, tag 0x00 = end
        bb.put(new byte[] {0x01});
        bb.putInt(codeOff);
        bb.put(new byte[] {0x00}); // end tags
    }

    private void writeCodeSections() {
        // Method 0: constructor
        writeCodeBlock(methodCodeOff[0], 4, 1, constructorMethodCode(), 0);

        // Method 1: simpleReturn (ldai 42, sta v0, lda v0, return)
        byte[] simpleCode = simpleMethodCode(42);
        writeCodeBlock(methodCodeOff[1], 4, 1, simpleCode, 0);

        // Method 2: arithmetic
        byte[] arithCode = arithmeticMethodCode();
        writeCodeBlock(methodCodeOff[2], 4, 2, arithCode, 0);

        // Method 3: ChildClass constructor
        writeCodeBlock(methodCodeOff[3], 4, 1, constructorMethodCode(), 0);

        // Method 4: conditional branch
        byte[] condCode = conditionalBranchMethodCode();
        writeCodeBlock(methodCodeOff[4], 4, 1, condCode, 0);

        // Method 5: loop
        byte[] loopCode = loopMethodCode();
        writeCodeBlock(methodCodeOff[5], 4, 1, loopCode, 0);

        // Method 6: callArg
        byte[] callCode = callMethodCode();
        writeCodeBlock(methodCodeOff[6], 4, 2, callCode, 0);
    }

    private void writeCodeBlock(int off, int numVregs, int numArgs,
            byte[] instructions, int triesSize) {
        bb.position(off);
        bb.put(uleb128(numVregs));
        bb.put(uleb128(numArgs));
        bb.put(uleb128(instructions.length));
        bb.put(uleb128(triesSize));
        bb.put(instructions);
    }

    private void writeProtos() {
        // Write one proto: ()I32 (return I32, no params)
        // Shorty encoding: 4-bit type codes packed into 16-bit groups
        // I32 = 0x07 in the lower nibble, then 0x0 in the next nibble to terminate
        // So the 16-bit group = 0x0007 (nibble 0 = 0x07, nibble 1 = 0x00 = terminator)
        bb.position(protoAreaOff);
        bb.putShort((short) 0x0007); // I32 return, then 0 = terminator
    }

    private void writeLiteralArrays() {
        // Write one literal array with a single u8 literal value
        bb.position(literalArrayAreaOff);
        bb.putInt(2);                   // numLiterals = 2 (tag + value pairs)
        bb.put((byte) 0x01);            // tag = u8
        bb.put((byte) 0x2A);            // value = 42
    }

    private void writeRegionHeaders() {
        // One region header covering the entire file
        bb.position(regionHeaderOff);
        int class0Off = classAreaOff;
        int codeEndOff = codeAreaOff + 700;
        bb.putInt(class0Off);           // startOff
        bb.putInt(codeEndOff);          // endOff
        bb.putInt(numClasses);          // classIdxSize
        bb.putInt(classRegionIdxOff);   // classIdxOff
        bb.putInt(numMethods);          // methodIdxSize
        bb.putInt(methodRegionIdxOff);  // methodIdxOff
        bb.putInt(numFields);           // fieldIdxSize
        bb.putInt(fieldRegionIdxOff);   // fieldIdxOff
        bb.putInt(1);                   // protoIdxSize
        bb.putInt(protoIdxOff);         // protoIdxOff
    }

    private void writeIndexArrays() {
        // Class index
        bb.position(classIdxOff);
        bb.putInt(classAreaOff);            // BaseClass
        bb.putInt(classAreaOff + 400);      // ChildClass

        // Class region index
        bb.position(classRegionIdxOff);
        bb.putInt(classAreaOff);
        bb.putInt(classAreaOff + 400);

        // Method region index
        bb.position(methodRegionIdxOff);
        for (int i = 0; i < numMethods; i++) {
            // These point to method definitions within classes
            // Not needed for basic parsing but included for completeness
            bb.putInt(0);
        }

        // Field region index
        bb.position(fieldRegionIdxOff);
        for (int i = 0; i < numFields; i++) {
            bb.putInt(0);
        }

        // Proto index
        bb.position(protoIdxOff);
        bb.putInt(protoAreaOff);

        // Literal array index
        bb.position(literalArrayIdxOff);
        bb.putInt(literalArrayAreaOff);

        // LNP index (empty)
        bb.position(lnpIdxOff);
    }

    private void writeHeader() {
        bb.position(0);
        bb.put(new byte[] {'P', 'A', 'N', 'D', 'A', 0, 0, 0});  // magic
        bb.putInt(0);                                              // checksum
        bb.put(new byte[] {'0', '0', '0', '2'});                  // version
        bb.putInt(fileSize);                                       // fileSize
        bb.putInt(0);                                              // foreignOff
        bb.putInt(0);                                              // foreignSize
        bb.putInt(numClasses);                                     // numClasses
        bb.putInt(classIdxOff);                                    // classIdxOff
        bb.putInt(0);                                              // numLnps
        bb.putInt(lnpIdxOff);                                      // lnpIdxOff
        bb.putInt(1);                                              // numLiteralArrays
        bb.putInt(literalArrayIdxOff);                             // literalArrayIdxOff
        bb.putInt(1);                                              // numIndexRegions
        bb.putInt(regionHeaderOff);                                // indexSectionOff
    }

    /**
     * Returns the expected number of classes in the comprehensive fixture.
     *
     * @return class count
     */
    public int getExpectedClassCount() {
        return 2;
    }

    /**
     * Returns the expected total number of methods across all classes.
     *
     * @return method count
     */
    public int getExpectedMethodCount() {
        return 7;
    }

    /**
     * Returns the expected total number of fields across all classes.
     *
     * @return field count
     */
    public int getExpectedFieldCount() {
        return 3;
    }

    /**
     * Returns the expected class name at the given index.
     *
     * @param index class index (0-based)
     * @return class name
     */
    public String getExpectedClassName(int index) {
        switch (index) {
            case 0:
                return "Lcom/example/BaseClass;";
            case 1:
                return "Lcom/example/ChildClass;";
            default:
                return "";
        }
    }

    /**
     * Returns the expected method name for the given method index.
     *
     * @param index method index (0-based across all classes)
     * @return method name
     */
    public String getExpectedMethodName(int index) {
        String[] names = {
            "<init>", "simpleReturn", "arithmetic",
            "<init>", "conditional", "loopMethod", "callArg"
        };
        return names[index];
    }

    /**
     * Returns the expected field name for the given field index.
     *
     * @param index field index (0-based across all classes)
     * @return field name
     */
    public String getExpectedFieldName(int index) {
        String[] names = {"value", "name", "tag"};
        return names[index];
    }

    /**
     * Returns the expected number of methods for the class at the given index.
     *
     * @param classIndex class index (0-based)
     * @return method count for that class
     */
    public int getExpectedMethodCountForClass(int classIndex) {
        switch (classIndex) {
            case 0:
                return 3;
            case 1:
                return 4;
            default:
                return 0;
        }
    }

    /**
     * Returns the expected number of fields for the class at the given index.
     *
     * @param classIndex class index (0-based)
     * @return field count for that class
     */
    public int getExpectedFieldCountForClass(int classIndex) {
        switch (classIndex) {
            case 0:
                return 2;
            case 1:
                return 1;
            default:
                return 0;
        }
    }
}
