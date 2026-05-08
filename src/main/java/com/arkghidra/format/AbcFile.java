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
        skipTagValues(r);

        List<AbcField> fields = new ArrayList<>((int) numFields);
        for (int i = 0; i < (int) numFields; i++) {
            fields.add(parseField(r));
        }

        List<AbcMethod> methods = new ArrayList<>((int) numMethods);
        for (int i = 0; i < (int) numMethods; i++) {
            methods.add(parseMethod(r));
        }

        return new AbcClass(name, superClassOff, accessFlags, fields, methods, classOff);
    }

    private static AbcField parseField(AbcReader r) {
        int fieldPos = r.position();
        int classIdx = r.readU16();
        int typeIdx = r.readU16();
        String name = readStringAt(r, (int) r.readU32());
        long accessFlags = r.readUleb128();
        skipTagValues(r);
        return new AbcField(classIdx, typeIdx, name, accessFlags, fieldPos);
    }

    private static AbcMethod parseMethod(AbcReader r) {
        int methodPos = r.position();
        int classIdx = r.readU16();
        int protoIdx = r.readU16();
        long nameOff = r.readU32();
        String name = readStringAt(r, (int) nameOff);
        long accessFlags = r.readUleb128();
        long codeOff = parseMethodTagsForCode(r);
        return new AbcMethod(classIdx, protoIdx, name, accessFlags, codeOff, methodPos);
    }

    private static long parseMethodTagsForCode(AbcReader r) {
        long codeOff = 0;
        while (true) {
            int tag = r.readU8() & 0xFF;
            if (tag == 0x00) {
                break;
            }
            switch (tag) {
                case 0x01:
                    codeOff = r.readU32();
                    break;
                case 0x02:
                    r.readU8();
                    break;
                case 0x03: case 0x04: case 0x05: case 0x06:
                case 0x07: case 0x08: case 0x09:
                    r.readU32();
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown method tag: 0x" + Integer.toHexString(tag));
            }
        }
        return codeOff;
    }

    private static void skipTagValues(AbcReader r) {
        while (true) {
            int tag = r.readU8() & 0xFF;
            if (tag == 0x00) {
                break;
            }
            switch (tag) {
                case 0x01: case 0x03: case 0x04:
                case 0x05: case 0x06: case 0x07:
                    r.readU32();
                    break;
                case 0x02:
                    r.readU8();
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown tag: 0x" + Integer.toHexString(tag));
            }
        }
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
}
