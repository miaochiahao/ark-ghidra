package com.arkghidra.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Top-level parser for Panda .abc binary files.
 * Reads header, class index, region headers, classes, methods, fields, code, and protos.
 */
public class AbcFile {
    private final AbcHeader header;
    private final List<AbcRegionHeader> regionHeaders;
    private final List<AbcClass> classes;
    private final List<AbcProto> protos;
    private final List<AbcLiteralArray> literalArrays;
    private final List<Long> classIndex;
    private final List<Long> lnpIndex;
    private final List<Long> literalArrayIndex;
    private final byte[] rawData;

    // Tag constants for class_data TaggedValues
    private static final int CLASS_TAG_NOTHING = 0x00;
    private static final int CLASS_TAG_SOURCE_LANG = 0x02;
    private static final int CLASS_TAG_SOURCE_FILE = 0x07;

    // Tag constants for method_data TaggedValues
    private static final int METHOD_TAG_END = 0x00;
    private static final int METHOD_TAG_CODE_OFFSET = 0x01;
    private static final int METHOD_TAG_SOURCE_LANG = 0x02;
    private static final int METHOD_TAG_DEBUG_INFO = 0x03;
    // Tags 0x04-0x09 are uint32 values (various metadata)

    private AbcFile(AbcHeader header, List<AbcRegionHeader> regionHeaders,
            List<AbcClass> classes, List<AbcProto> protos,
            List<AbcLiteralArray> literalArrays,
            List<Long> classIndex, List<Long> lnpIndex,
            List<Long> literalArrayIndex, byte[] rawData) {
        this.header = header;
        this.regionHeaders = Collections.unmodifiableList(regionHeaders);
        this.classes = Collections.unmodifiableList(classes);
        this.protos = Collections.unmodifiableList(protos);
        this.literalArrays = Collections.unmodifiableList(literalArrays);
        this.classIndex = Collections.unmodifiableList(classIndex);
        this.lnpIndex = Collections.unmodifiableList(lnpIndex);
        this.literalArrayIndex = Collections.unmodifiableList(literalArrayIndex);
        this.rawData = rawData;
    }

    public static AbcFile parse(byte[] data) {
        AbcReader r = new AbcReader(data);

        AbcHeader header = parseHeader(r);
        if (!header.hasValidMagic()) {
            throw new AbcFormatException("Invalid ABC magic");
        }

        List<Long> classIndex = parseIndexArray(r, header.getClassIdxOff(), header.getNumClasses());
        List<Long> lnpIndex = parseIndexArray(r, header.getLnpIdxOff(), header.getNumLnps());
        List<Long> literalArrayIndex = parseIndexArray(r, header.getLiteralArrayIdxOff(),
                header.getNumLiteralArrays());

        List<AbcRegionHeader> regionHeaders = parseRegionHeaders(r,
                header.getIndexSectionOff(), header.getNumIndexRegions());

        List<AbcClass> classes = new ArrayList<>();
        for (long classOff : classIndex) {
            r.position((int) classOff);
            classes.add(parseClass(r, (int) classOff, header));
        }

        List<AbcProto> protos = new ArrayList<>();
        for (AbcRegionHeader rh : regionHeaders) {
            parseProtosForRegion(r, rh, protos);
        }

        List<AbcLiteralArray> literalArrays = new ArrayList<>();
        for (long laOff : literalArrayIndex) {
            r.position((int) laOff);
            literalArrays.add(parseLiteralArray(r));
        }

        return new AbcFile(header, regionHeaders, classes, protos, literalArrays,
                classIndex, lnpIndex, literalArrayIndex, data);
    }

    private static AbcHeader parseHeader(AbcReader r) {
        byte[] magic = r.readBytes(8);
        int checksum = r.readS32();
        byte[] version = r.readBytes(4);
        long fileSize = r.readU32();
        long foreignOff = r.readU32();
        long foreignSize = r.readU32();
        long numClasses = r.readU32();
        long classIdxOff = r.readU32();
        long numLnps = r.readU32();
        long lnpIdxOff = r.readU32();
        long numLiteralArrays = r.readU32();
        long literalArrayIdxOff = r.readU32();
        long numIndexRegions = r.readU32();
        long indexSectionOff = r.readU32();
        return new AbcHeader(magic, checksum, version, fileSize, foreignOff, foreignSize,
                numClasses, classIdxOff, numLnps, lnpIdxOff, numLiteralArrays,
                literalArrayIdxOff, numIndexRegions, indexSectionOff);
    }

    private static List<Long> parseIndexArray(AbcReader r, long off, long count) {
        if (count == 0) {
            return Collections.emptyList();
        }
        r.position((int) off);
        List<Long> result = new ArrayList<>((int) count);
        for (int i = 0; i < (int) count; i++) {
            result.add(r.readU32());
        }
        return result;
    }

    private static List<AbcRegionHeader> parseRegionHeaders(AbcReader r, long off, long count) {
        if (count == 0) {
            return Collections.emptyList();
        }
        r.position((int) off);
        List<AbcRegionHeader> result = new ArrayList<>((int) count);
        for (int i = 0; i < (int) count; i++) {
            result.add(parseRegionHeader(r));
        }
        return result;
    }

    private static AbcRegionHeader parseRegionHeader(AbcReader r) {
        return new AbcRegionHeader(
                r.readU32(), r.readU32(),
                r.readU32(), r.readU32(),
                r.readU32(), r.readU32(),
                r.readU32(), r.readU32(),
                r.readU32(), r.readU32());
    }

    private static AbcClass parseClass(AbcReader r, int classOff, AbcHeader header) {
        String name = r.readMutf8();
        long superClassOff = r.readU32();
        long accessFlags = r.readUleb128();
        long numFields = r.readUleb128();
        long numMethods = r.readUleb128();
        long sourceFileOff = parseClassTags(r);

        List<AbcField> fields = new ArrayList<>((int) numFields);
        for (int i = 0; i < (int) numFields; i++) {
            fields.add(parseField(r));
        }

        List<AbcMethod> methods = new ArrayList<>((int) numMethods);
        for (int i = 0; i < (int) numMethods; i++) {
            methods.add(parseMethod(r));
        }

        return new AbcClass(name, superClassOff, accessFlags, fields, methods,
                classOff, sourceFileOff);
    }

    private static AbcField parseField(AbcReader r) {
        int fieldPos = r.position();
        int classIdx = r.readU16();
        int typeIdx = r.readU16();
        String name = readStringAt(r, (int) r.readU32());
        long accessFlags = r.readUleb128();
        parseClassTags(r);
        return new AbcField(classIdx, typeIdx, name, accessFlags, fieldPos);
    }

    private static AbcMethod parseMethod(AbcReader r) {
        int methodPos = r.position();
        int classIdx = r.readU16();
        int protoIdx = r.readU16();
        long nameOff = r.readU32();
        String name = readStringAt(r, (int) nameOff);
        long accessFlags = r.readUleb128();
        long[] methodMeta = parseMethodTags(r);
        long codeOff = methodMeta[0];
        long debugInfoOff = methodMeta[1];
        return new AbcMethod(classIdx, protoIdx, name, accessFlags, codeOff,
                methodPos, debugInfoOff);
    }

    /**
     * Parses method_data TaggedValues, extracting code offset and debug info offset.
     *
     * @return a two-element array: [codeOff, debugInfoOff]
     */
    private static long[] parseMethodTags(AbcReader r) {
        long codeOff = 0;
        long debugInfoOff = 0;
        while (true) {
            int tag = r.readU8() & 0xFF;
            if (tag == METHOD_TAG_END) {
                break;
            }
            switch (tag) {
                case METHOD_TAG_CODE_OFFSET:
                    codeOff = r.readU32();
                    break;
                case METHOD_TAG_SOURCE_LANG:
                    r.readU8();
                    break;
                case METHOD_TAG_DEBUG_INFO:
                    debugInfoOff = r.readU32();
                    break;
                case 0x04: case 0x05: case 0x06:
                case 0x07: case 0x08: case 0x09:
                    r.readU32();
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown method tag: 0x" + Integer.toHexString(tag));
            }
        }
        return new long[] {codeOff, debugInfoOff};
    }

    /**
     * Parses class_data TaggedValues, capturing SOURCE_FILE offset.
     *
     * @return the source file string table offset, or 0 if none
     */
    private static long parseClassTags(AbcReader r) {
        long sourceFileOff = 0;
        while (true) {
            int tag = r.readU8() & 0xFF;
            if (tag == CLASS_TAG_NOTHING) {
                break;
            }
            switch (tag) {
                case CLASS_TAG_SOURCE_FILE:
                    sourceFileOff = r.readU32();
                    break;
                case CLASS_TAG_SOURCE_LANG:
                    r.readU8();
                    break;
                case 0x01: case 0x03: case 0x04:
                case 0x05: case 0x06:
                    r.readU32();
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown tag: 0x" + Integer.toHexString(tag));
            }
        }
        return sourceFileOff;
    }

    public static AbcCode parseCode(AbcReader r, int codeOff) {
        r.position(codeOff);
        long numVregs = r.readUleb128();
        long numArgs = r.readUleb128();
        long codeSize = r.readUleb128();
        long triesSize = r.readUleb128();
        byte[] instructions = r.readBytes((int) codeSize);

        List<AbcTryBlock> tryBlocks = new ArrayList<>((int) triesSize);
        for (int i = 0; i < (int) triesSize; i++) {
            tryBlocks.add(parseTryBlock(r));
        }
        return new AbcCode(numVregs, numArgs, codeSize, instructions, tryBlocks, codeOff);
    }

    private static AbcTryBlock parseTryBlock(AbcReader r) {
        long startPc = r.readUleb128();
        long length = r.readUleb128();
        long numCatches = r.readUleb128();
        List<AbcCatchBlock> catchBlocks = new ArrayList<>((int) numCatches);
        for (int i = 0; i < (int) numCatches; i++) {
            long typeIdx = r.readUleb128();
            long handlerPc = r.readUleb128();
            long handlerCodeSize = r.readUleb128();
            catchBlocks.add(new AbcCatchBlock(typeIdx, handlerPc, handlerCodeSize, typeIdx == 0));
        }
        return new AbcTryBlock(startPc, length, catchBlocks);
    }

    private static void parseProtosForRegion(AbcReader r, AbcRegionHeader rh, List<AbcProto> protos) {
        if (rh.getProtoIdxSize() == 0) {
            return;
        }
        r.position((int) rh.getProtoIdxOff());
        List<Long> protoOffsets = new ArrayList<>((int) rh.getProtoIdxSize());
        for (int i = 0; i < (int) rh.getProtoIdxSize(); i++) {
            protoOffsets.add(r.readU32());
        }
        for (long protoOff : protoOffsets) {
            r.position((int) protoOff);
            r.align(2);
            protos.add(parseProto(r));
        }
    }

    private static AbcProto parseProto(AbcReader r) {
        List<AbcProto.ShortyType> shorty = new ArrayList<>();
        List<Integer> refTypes = new ArrayList<>();
        List<Integer> shortyRaw = new ArrayList<>();
        while (true) {
            int group = r.readU16();
            for (int shift = 0; shift < 16; shift += 4) {
                int nibble = (group >>> shift) & 0xF;
                if (nibble == 0) {
                    return new AbcProto(shorty, refTypes);
                }
                AbcProto.ShortyType type = AbcProto.ShortyType.fromCode(nibble);
                shorty.add(type);
                shortyRaw.add(nibble);
            }
        }
    }

    private static AbcLiteralArray parseLiteralArray(AbcReader r) {
        long numLiterals = r.readU32();
        List<byte[]> literals = new ArrayList<>((int) numLiterals);
        for (int i = 0; i < (int) numLiterals / 2; i++) {
            int tag = r.readU8() & 0xFF;
            byte[] value = readLiteralValue(r, tag);
            literals.add(value);
            if (tag >= 0x0A && tag <= 0x15) {
                break;
            }
        }
        return new AbcLiteralArray(numLiterals, literals);
    }

    private static byte[] readLiteralValue(AbcReader r, int tag) {
        switch (tag) {
            case 0x01:
                return r.readBytes(1);
            case 0x02: case 0x03:
                return r.readBytes(4);
            case 0x04:
                return r.readBytes(8);
            case 0x05: case 0x06: case 0x07: case 0x16: case 0x17: case 0x18:
                return r.readBytes(4);
            case 0x08: case 0x19:
                return r.readBytes(1);
            case 0x09:
                return r.readBytes(2);
            case 0x0A: case 0x0B:
                r.align(1);
                return readArrayLiteral(r, 1);
            case 0x0C: case 0x0D:
                r.align(2);
                return readArrayLiteral(r, 2);
            case 0x0E: case 0x0F: case 0x12: case 0x14:
                r.align(4);
                return readArrayLiteral(r, 4);
            case 0x10: case 0x11: case 0x13:
                r.align(8);
                return readArrayLiteral(r, 8);
            case 0xFF:
                return r.readBytes(1);
            default:
                return r.readBytes(4);
        }
    }

    private static byte[] readArrayLiteral(AbcReader r, int elemSize) {
        long count = r.readU32();
        int size = (int) count * elemSize;
        return r.readBytes(size);
    }

    private static String readStringAt(AbcReader r, int off) {
        int saved = r.position();
        r.position(off);
        String s = r.readMutf8();
        r.position(saved);
        return s;
    }

    public AbcHeader getHeader() {
        return header;
    }
    public List<AbcRegionHeader> getRegionHeaders() {
        return regionHeaders;
    }
    public List<AbcClass> getClasses() {
        return classes;
    }
    public List<AbcProto> getProtos() {
        return protos;
    }
    public List<AbcLiteralArray> getLiteralArrays() {
        return literalArrays;
    }
    public List<Long> getClassIndex() {
        return classIndex;
    }
    public List<Long> getLnpIndex() {
        return lnpIndex;
    }
    public List<Long> getLiteralArrayIndex() {
        return literalArrayIndex;
    }
    public byte[] getRawData() {
        return rawData;
    }

    public boolean isForeignOffset(long off) {
        return off >= header.getForeignOff()
                && off < header.getForeignOff() + header.getForeignSize();
    }

    public AbcRegionHeader findRegionForOffset(long off) {
        for (AbcRegionHeader rh : regionHeaders) {
            if (rh.containsOffset(off)) {
                return rh;
            }
        }
        return null;
    }

    public AbcCode getCodeForMethod(AbcMethod method) {
        if (method.getCodeOff() == 0) {
            return null;
        }
        return parseCode(new AbcReader(rawData), (int) method.getCodeOff());
    }

    /**
     * Resolves a source file name from a string table offset.
     *
     * @param stringOff the string table offset (from AbcClass.getSourceFileOff())
     * @return the source file name, or null if offset is 0 or out of range
     */
    public String getSourceFileName(long stringOff) {
        if (stringOff <= 0 || stringOff >= rawData.length) {
            return null;
        }
        return readStringAt(new AbcReader(rawData), (int) stringOff);
    }

    /**
     * Returns the source file name for a class, or null if not available.
     *
     * @param cls the class
     * @return the source file name, or null
     */
    public String getSourceFileForClass(AbcClass cls) {
        return getSourceFileName(cls.getSourceFileOff());
    }

    /**
     * Parses and returns debug info for a method, if available.
     *
     * @param method the method
     * @return the debug info, or null if none
     */
    public AbcDebugInfo getDebugInfoForMethod(AbcMethod method) {
        if (method.getDebugInfoOff() == 0) {
            return null;
        }
        return parseDebugInfo(new AbcReader(rawData),
                (int) method.getDebugInfoOff());
    }

    /**
     * Parses debug info from the given offset.
     *
     * <p>Debug info structure:
     * <ul>
     *   <li>line_start (ULEB128) - starting line number</li>
     *   <li>num_parameters (ULEB128) - number of parameters with debug names</li>
     *   <li>parameters[] (ULEB128 each) - string table offsets for param names</li>
     *   <li>constant_pool_size (ULEB128)</li>
     *   <li>constant_pool[] (ULEB128 each)</li>
     *   <li>line_number_program_idx[] (ULEB128 each)</li>
     * </ul>
     */
    static AbcDebugInfo parseDebugInfo(AbcReader r, int off) {
        r.position(off);
        long lineStart = r.readUleb128();
        long numParameters = r.readUleb128();

        List<String> parameterNames = new ArrayList<>((int) numParameters);
        for (int i = 0; i < (int) numParameters; i++) {
            long nameOff = r.readUleb128();
            if (nameOff > 0) {
                parameterNames.add(readStringAt(r, (int) nameOff));
            } else {
                parameterNames.add(null);
            }
        }

        long constantPoolSize = r.readUleb128();
        for (int i = 0; i < (int) constantPoolSize; i++) {
            r.readUleb128();
        }

        List<Long> lnpIdx = new ArrayList<>();
        while (r.remaining() > 0) {
            long idx = r.readUleb128();
            lnpIdx.add(idx);
            // Check if next byte is 0 (end marker) or end of data
            if (r.remaining() == 0) {
                break;
            }
            int next = r.readU8() & 0xFF;
            if (next == 0) {
                break;
            }
            // Not end - push back by reading as part of next ULEB128
            // Actually, the LNP index entries are just a sequence of ULEB128s
            // terminated by the end of the debug_info block. We don't have an
            // explicit end marker, so we stop after the first entry for now.
            // In practice, there is typically one LNP index per debug info.
            break;
        }

        return new AbcDebugInfo(lineStart, numParameters, parameterNames,
                constantPoolSize, lnpIdx, off);
    }

    /**
     * Parses a line number program and returns the line number entries.
     *
     * <p>Implements a DWARF v3 line number state machine with the following opcodes:
     * <ul>
     *   <li>0x00 - END_SEQUENCE</li>
     *   <li>0x01 - ADVANCE_PC (ULEB128 offset)</li>
     *   <li>0x02 - ADVANCE_LINE (SLEB128 offset)</li>
     *   <li>0x03 - START_LOCAL (reg ULEB128, nameIdx ULEB128, typeIdx ULEB128)</li>
     *   <li>0x04 - START_LOCAL_EXTENDED (reg, nameIdx, typeIdx, sigIdx)</li>
     *   <li>0x05 - END_LOCAL (reg ULEB128)</li>
     *   <li>0x06 - RESTART_LOCAL (reg ULEB128)</li>
     *   <li>0x07 - SET_PROLOGUE_END</li>
     *   <li>0x08 - SET_EPILOGUE_BEGIN</li>
     *   <li>0x09 - SET_FILE (ULEB128 string idx)</li>
     *   <li>0x0A - SET_SOURCE_CODE (ULEB128 string idx)</li>
     *   <li>0x0B - SET_COLUMN (ULEB128 column)</li>
     *   <li>0x0C-0xFF - special opcodes</li>
     * </ul>
     *
     * @param lnpOff the offset of the line number program data
     * @return the list of line number entries
     */
    public List<AbcLineNumberEntry> parseLineNumberProgram(long lnpOff) {
        if (lnpOff <= 0 || lnpOff >= rawData.length) {
            return Collections.emptyList();
        }
        return parseLineNumberProgramData(new AbcReader(rawData), (int) lnpOff);
    }

    /**
     * Parses the line number program from the LNP index.
     *
     * <p>The LNP index table stores offsets to line number programs.
     * Each entry is a ULEB128 offset. The line number program at that offset
     * is executed as a state machine.
     *
     * @param lnpIndex the index into the LNP table (0-based)
     * @return the line number entries, or empty list if not available
     */
    public List<AbcLineNumberEntry> getLineNumberEntries(int lnpIndex) {
        if (lnpIndex < 0 || lnpIndex >= this.lnpIndex.size()) {
            return Collections.emptyList();
        }
        long lnpOff = this.lnpIndex.get(lnpIndex);
        return parseLineNumberProgram(lnpOff);
    }

    static List<AbcLineNumberEntry> parseLineNumberProgramData(AbcReader r, int off) {
        r.position(off);
        List<AbcLineNumberEntry> entries = new ArrayList<>();
        long address = 0;
        long line = 1;

        while (r.remaining() > 0) {
            int opcode = r.readU8() & 0xFF;

            if (opcode == 0x00) {
                // END_SEQUENCE
                break;
            } else if (opcode == 0x01) {
                // ADVANCE_PC
                long delta = r.readUleb128();
                address += delta;
            } else if (opcode == 0x02) {
                // ADVANCE_LINE
                long delta = r.readSleb128();
                line += delta;
            } else if (opcode == 0x03) {
                // START_LOCAL
                r.readUleb128(); // register
                r.readUleb128(); // name idx
                r.readUleb128(); // type idx
            } else if (opcode == 0x04) {
                // START_LOCAL_EXTENDED
                r.readUleb128(); // register
                r.readUleb128(); // name idx
                r.readUleb128(); // type idx
                r.readUleb128(); // signature idx
            } else if (opcode == 0x05) {
                // END_LOCAL
                r.readUleb128(); // register
            } else if (opcode == 0x06) {
                // RESTART_LOCAL
                r.readUleb128(); // register
            } else if (opcode == 0x07) {
                // SET_PROLOGUE_END - no operands
                break;
            } else if (opcode == 0x08) {
                // SET_EPILOGUE_BEGIN - no operands
                break;
            } else if (opcode == 0x09) {
                // SET_FILE
                r.readUleb128(); // string idx
            } else if (opcode == 0x0A) {
                // SET_SOURCE_CODE
                r.readUleb128(); // string idx
            } else if (opcode == 0x0B) {
                // SET_COLUMN
                r.readUleb128(); // column
            } else if (opcode >= 0x0C) {
                // Special opcode: address += adjusted / 15, line += -4 + (adjusted % 15)
                int adjusted = opcode - 0x0C;
                address += adjusted / 15;
                line += -4 + (adjusted % 15);
                entries.add(new AbcLineNumberEntry(address, line));
            }
        }

        return entries;
    }
}
