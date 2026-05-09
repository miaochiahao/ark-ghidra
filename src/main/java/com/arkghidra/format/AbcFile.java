package com.arkghidra.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Top-level parser for Panda .abc binary files.
 * Reads header, class index, region headers, classes, methods, fields, code, and protos.
 *
 * <p>String lookups via {@link #readStringAt(AbcReader, int)} are cached to avoid
 * re-reading and re-decoding the same MUTF-8 bytes on repeated access. Code sections
 * are parsed lazily when accessed via {@link #getCodeForMethod(AbcMethod)}.
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

    /**
     * Module record literal array offsets indexed by class index.
     * Each entry maps a class index to the LiteralArray offset that
     * contains the module record for that class.
     */
    private final Map<Integer, Long> moduleRecordOffsets = new HashMap<>();

    /**
     * Cache of parsed module records keyed by LiteralArray offset.
     */
    private final Map<Long, AbcModuleRecord> moduleRecordCache = new HashMap<>();

    /**
     * Cache of string table offset to decoded string. Avoids re-reading MUTF-8
     * bytes and creating a new AbcReader for repeated lookups of the same offset.
     */
    private final Map<Integer, String> stringCache = new HashMap<>();

    /**
     * Cache of code offset to parsed AbcCode. Avoids re-parsing the same code
     * section when multiple components (loader, analyzer, decompiler) request it.
     */
    private final Map<Integer, AbcCode> codeCache = new HashMap<>();

    /**
     * Cache of debug info offset to parsed AbcDebugInfo. Avoids re-parsing the
     * same debug info section when multiple components request it.
     */
    private final Map<Integer, AbcDebugInfo> debugInfoCache = new HashMap<>();

    /**
     * Cache of LNP string index to decoded string. Populated once on first
     * access, then shared across all decompilation contexts for this file.
     * Eliminates per-method string re-decoding overhead.
     */
    private volatile String[] lnpStringCache;

    /**
     * Lazily computed flat method list. Built once on first access, then
     * returned as an unmodifiable list on subsequent calls.
     */
    private volatile List<AbcMethod> flatMethodList;

    /**
     * Lazily computed method lookup array. Indexed by flat method index.
     * Built alongside flatMethodList on first access.
     */
    private volatile AbcMethod[] flatMethodArray;

    private final List<AbcAnnotation> annotations = new ArrayList<>();
    private final Map<Long, AbcAnnotation> annotationCache = new HashMap<>();
    private final Map<Integer, List<AbcAnnotation>> classAnnotations = new HashMap<>();
    private final Map<Long, List<AbcAnnotation>> fieldAnnotations = new HashMap<>();
    private final Map<Long, List<AbcAnnotation>> methodAnnotations = new HashMap<>();
    private final Set<Long> annotationOffsets = new HashSet<>();

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

    // Tag constants for field_data TaggedValues
    private static final int FIELD_TAG_NOTHING = 0x00;
    private static final int FIELD_TAG_INT_VALUE = 0x01;
    private static final int FIELD_TAG_VALUE = 0x02;
    private static final int FIELD_TAG_ANNOTATION = 0x05;

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
        initModuleRecordOffsets();
    }

    public static AbcFile parse(byte[] data) {
        AbcReader r = new AbcReader(data);

        AbcHeader header = parseHeader(r);
        if (!header.hasValidMagic()) {
            throw new AbcFormatException("Invalid ABC magic");
        }

        validateHeaderOffsets(header, r.capacity());

        List<Long> classIndex = parseIndexArray(r, header.getClassIdxOff(), header.getNumClasses());
        List<Long> lnpIndex = parseIndexArray(r, header.getLnpIdxOff(), header.getNumLnps());
        List<Long> literalArrayIndex = parseIndexArray(r, header.getLiteralArrayIdxOff(),
                header.getNumLiteralArrays());

        List<AbcRegionHeader> regionHeaders = parseRegionHeaders(r,
                header.getIndexSectionOff(), header.getNumIndexRegions());

        Set<Long> collectedAnnotationOffsets = new HashSet<>();
        Map<Long, Integer> annotationToClassIdx = new HashMap<>();
        Map<Long, Long> annotationToFieldOff = new HashMap<>();
        Map<Long, Long> annotationToMethodOff = new HashMap<>();
        int classIdxCounter = 0;
        List<AbcClass> classes = new ArrayList<>();
        for (long classOff : classIndex) {
            if (classOff < 0 || classOff >= r.capacity()) {
                throw new AbcFormatException(
                        "Class offset " + classOff + " out of range [0, " + r.capacity() + ")");
            }
            r.position((int) classOff);
            final int currentClassIdx = classIdxCounter;
            classes.add(parseClass(r, (int) classOff, header,
                    collectedAnnotationOffsets,
                    annOff -> annotationToClassIdx.put(annOff, currentClassIdx),
                    annotationToFieldOff, annotationToMethodOff));
            classIdxCounter++;
        }

        List<AbcProto> protos = new ArrayList<>();
        for (AbcRegionHeader rh : regionHeaders) {
            parseProtosForRegion(r, rh, protos);
        }

        List<AbcLiteralArray> literalArrays = new ArrayList<>();
        for (long laOff : literalArrayIndex) {
            if (laOff < 0 || laOff >= r.capacity()) {
                throw new AbcFormatException(
                        "Literal array offset " + laOff + " out of range [0, " + r.capacity() + ")");
            }
            r.position((int) laOff);
            literalArrays.add(parseLiteralArray(r));
        }

        AbcFile abcFile = new AbcFile(header, regionHeaders, classes, protos, literalArrays,
                classIndex, lnpIndex, literalArrayIndex, data);
        abcFile.initAnnotations(collectedAnnotationOffsets,
                annotationToClassIdx, annotationToFieldOff,
                annotationToMethodOff);
        return abcFile;
    }

    /**
     * Second-phase initialization: scans fields for module record indices
     * and caches them for lazy module record parsing.
     * Called internally after construction.
     */
    private void initModuleRecordOffsets() {
        int classIdx = 0;
        for (AbcClass cls : classes) {
            for (AbcField field : cls.getFields()) {
                if (field.isModuleRecordIdx() && field.getIntValue() != 0) {
                    moduleRecordOffsets.put(classIdx, field.getIntValue());
                }
            }
            classIdx++;
        }
    }

    private void initAnnotations(Set<Long> collectedOffsets,
            Map<Long, Integer> annotationToClassIdx,
            Map<Long, Long> annotationToFieldOff,
            Map<Long, Long> annotationToMethodOff) {
        if (collectedOffsets.isEmpty()) {
            return;
        }
        annotationOffsets.addAll(collectedOffsets);
        for (Long annOff : collectedOffsets) {
            AbcAnnotation annotation = parseAnnotationAt(annOff);
            if (annotation != null) {
                annotations.add(annotation);
                annotationCache.put(annOff, annotation);

                Integer cidx = annotationToClassIdx.get(annOff);
                if (cidx != null) {
                    classAnnotations.computeIfAbsent(cidx,
                            k -> new ArrayList<>()).add(annotation);
                }

                Long fOff = annotationToFieldOff.get(annOff);
                if (fOff != null) {
                    fieldAnnotations.computeIfAbsent(fOff,
                            k -> new ArrayList<>()).add(annotation);
                }

                Long mOff = annotationToMethodOff.get(annOff);
                if (mOff != null) {
                    methodAnnotations.computeIfAbsent(mOff,
                            k -> new ArrayList<>()).add(annotation);
                }
            }
        }
    }

    private AbcAnnotation parseAnnotationAt(long off) {
        if (off <= 0 || off >= rawData.length) {
            return null;
        }
        AbcReader r = new AbcReader(rawData);
        r.position((int) off);
        int typeIdx = (int) r.readU32();
        int retention = r.readU8() & 0xFF;
        long numElements = r.readUleb128();

        String typeName = readStringAtCached(r, typeIdx);
        List<AbcAnnotationElement> elements =
                new ArrayList<>((int) numElements);
        for (int i = 0; i < (int) numElements; i++) {
            AbcAnnotationElement elem = parseAnnotationElement(r);
            if (elem != null) {
                elements.add(elem);
            }
        }

        return new AbcAnnotation(typeIdx, typeName, elements, off);
    }

    private AbcAnnotationElement parseAnnotationElement(AbcReader r) {
        int nameIdx = (int) r.readU32();
        int tag = r.readU8() & 0xFF;
        String name = readStringAtCached(r, nameIdx);

        switch (tag) {
            case AbcAnnotationElement.TAG_INT:
                long intVal = r.readSleb128();
                return new AbcAnnotationElement(nameIdx, name, tag, intVal);
            case AbcAnnotationElement.TAG_DOUBLE:
                long doubleBits = r.readU64();
                double doubleVal = Double.longBitsToDouble(doubleBits);
                return new AbcAnnotationElement(nameIdx, name, tag, doubleVal);
            case AbcAnnotationElement.TAG_STRING:
                int strIdx = (int) r.readU32();
                String strVal = readStringAtCached(r, strIdx);
                return new AbcAnnotationElement(nameIdx, name, tag, strVal,
                        false);
            case AbcAnnotationElement.TAG_ID:
                int idIdx = (int) r.readU32();
                String idVal = readStringAtCached(r, idIdx);
                return new AbcAnnotationElement(nameIdx, name, tag, idVal,
                        true);
            default:
                return null;
        }
    }

    /**
     * Validates that header offsets point within the file data bounds.
     *
     * @param header the parsed header
     * @param dataLength the total length of the file data
     * @throws AbcFormatException if any offset is out of range
     */
    private static void validateHeaderOffsets(AbcHeader header, int dataLength) {
        if (header.getNumClasses() > 0) {
            checkOffset("classIdxOff", header.getClassIdxOff(), dataLength);
        }
        if (header.getNumLnps() > 0) {
            checkOffset("lnpIdxOff", header.getLnpIdxOff(), dataLength);
        }
        if (header.getNumLiteralArrays() > 0) {
            checkOffset("literalArrayIdxOff", header.getLiteralArrayIdxOff(), dataLength);
        }
        if (header.getNumIndexRegions() > 0) {
            checkOffset("indexSectionOff", header.getIndexSectionOff(), dataLength);
        }
    }

    private static void checkOffset(String name, long offset, int dataLength) {
        if (offset < 0 || offset >= dataLength) {
            throw new AbcFormatException(
                    "Header offset " + name + "=" + offset + " out of range [0, " + dataLength + ")");
        }
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

    private static AbcClass parseClass(AbcReader r, int classOff,
            AbcHeader header, Set<Long> annOffsets,
            Consumer<Long> classAnnConsumer,
            Map<Long, Long> annotationToFieldOff,
            Map<Long, Long> annotationToMethodOff) {
        String name = r.readMutf8();
        long superClassOff = r.readU32();
        long accessFlags = r.readUleb128();
        long numFields = r.readUleb128();
        long numMethods = r.readUleb128();
        long sourceFileOff = parseClassTags(r, annOffsets,
                classAnnConsumer);

        List<AbcField> fields = new ArrayList<>((int) numFields);
        for (int i = 0; i < (int) numFields; i++) {
            fields.add(parseField(r, annOffsets,
                    annotationToFieldOff));
        }

        List<AbcMethod> methods = new ArrayList<>((int) numMethods);
        for (int i = 0; i < (int) numMethods; i++) {
            methods.add(parseMethod(r, annOffsets,
                    annotationToMethodOff));
        }

        return new AbcClass(name, superClassOff, accessFlags, fields, methods,
                classOff, sourceFileOff);
    }

    private static AbcField parseField(AbcReader r, Set<Long> annOffsets,
            Map<Long, Long> annotationToFieldOff) {
        int fieldPos = r.position();
        int classIdx = r.readU16();
        int typeIdx = r.readU16();
        String name = readStringAt(r, (int) r.readU32());
        long accessFlags = r.readUleb128();
        long intValue = parseFieldTags(r, annOffsets, fieldPos,
                annotationToFieldOff);
        return new AbcField(classIdx, typeIdx, name, accessFlags, fieldPos, intValue);
    }

    private static AbcMethod parseMethod(AbcReader r, Set<Long> annOffsets,
            Map<Long, Long> annotationToMethodOff) {
        int methodPos = r.position();
        int classIdx = r.readU16();
        int protoIdx = r.readU16();
        long nameOff = r.readU32();
        String name = readStringAt(r, (int) nameOff);
        long accessFlags = r.readUleb128();
        long[] methodMeta = parseMethodTags(r, annOffsets, methodPos,
                annotationToMethodOff);
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
    private static long[] parseMethodTags(AbcReader r, Set<Long> annOffsets,
            int methodPos, Map<Long, Long> annotationToMethodOff) {
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
                case 0x04:
                    r.readU32();
                    break;
                case 0x05: case 0x06:
                    long annOff = r.readU32();
                    if (annOff > 0) {
                        annOffsets.add(annOff);
                        annotationToMethodOff.put(annOff, (long) methodPos);
                    }
                    break;
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
    private static long parseClassTags(AbcReader r, Set<Long> annOffsets,
            Consumer<Long> classAnnConsumer) {
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
                case 0x01:
                    r.readU32();
                    break;
                case 0x03: case 0x04:
                    r.readU32();
                    break;
                case 0x05: case 0x06:
                    long annOff = r.readU32();
                    if (annOff > 0) {
                        annOffsets.add(annOff);
                        classAnnConsumer.accept(annOff);
                    }
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown tag: 0x" + Integer.toHexString(tag));
            }
        }
        return sourceFileOff;
    }

    /**
     * Parses field_data TaggedValues, capturing INT_VALUE for module record index.
     *
     * <p>Field tags follow a Tag-Value pattern:
     * <ul>
     *   <li>0x00 (NOTHING) - end of tags</li>
     *   <li>0x01 (INT_VALUE) - sleb128 integer value</li>
     *   <li>0x02 (VALUE) - uint32 value (type-dependent)</li>
     *   <li>0x03-0x06 (ANNOTATIONS) - uint32 annotation offsets</li>
     * </ul>
     *
     * @return the INT_VALUE, or 0 if none
     */
    private static long parseFieldTags(AbcReader r, Set<Long> annOffsets,
            int fieldPos, Map<Long, Long> annotationToFieldOff) {
        long intValue = 0;
        while (true) {
            int tag = r.readU8() & 0xFF;
            if (tag == FIELD_TAG_NOTHING) {
                break;
            }
            switch (tag) {
                case FIELD_TAG_INT_VALUE:
                    intValue = r.readSleb128();
                    break;
                case FIELD_TAG_VALUE:
                    r.readU32();
                    break;
                case 0x03: case 0x04:
                    long annOff3 = r.readU32();
                    if (annOff3 > 0) {
                        annOffsets.add(annOff3);
                        annotationToFieldOff.put(annOff3, (long) fieldPos);
                    }
                    break;
                case FIELD_TAG_ANNOTATION: case 0x06:
                    long annOff5 = r.readU32();
                    if (annOff5 > 0) {
                        annOffsets.add(annOff5);
                        annotationToFieldOff.put(annOff5, (long) fieldPos);
                    }
                    break;
                default:
                    throw new AbcFormatException(
                            "Unknown field tag: 0x" + Integer.toHexString(tag));
            }
        }
        return intValue;
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
        int count = (int) numLiterals / 2;
        List<byte[]> literals = new ArrayList<>(count);
        List<Integer> tags = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int tag = r.readU8() & 0xFF;
            byte[] value = readLiteralValue(r, tag);
            literals.add(value);
            tags.add(tag);
            if (tag >= 0x0A && tag <= 0x15) {
                break;
            }
        }
        return new AbcLiteralArray(numLiterals, literals, tags);
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

    /**
     * Reads a string from the given offset, using the string cache to avoid
     * redundant MUTF-8 decoding for repeated lookups.
     *
     * @param r the reader (position is saved and restored)
     * @param off the string table offset
     * @return the decoded string
     */
    private String readStringAtCached(AbcReader r, int off) {
        String cached = stringCache.get(off);
        if (cached != null) {
            return cached;
        }
        int saved = r.position();
        r.position(off);
        String s = r.readMutf8();
        r.position(saved);
        stringCache.put(off, s);
        return s;
    }

    /**
     * Returns the number of strings currently in the lookup cache.
     * Used for testing cache effectiveness.
     *
     * @return cached string count
     */
    public int getStringCacheSize() {
        return stringCache.size();
    }

    /**
     * Resolves a string by LNP index using a file-level cache.
     * The cache is populated lazily on first access and shared across
     * all decompilation contexts, avoiding per-method re-decoding.
     *
     * @param stringIdx the LNP string index
     * @return the decoded string, or a placeholder if resolution fails
     */
    public String resolveStringByIndex(int stringIdx) {
        if (lnpStringCache == null) {
            synchronized (this) {
                if (lnpStringCache == null) {
                    lnpStringCache = buildLnpStringCache();
                }
            }
        }
        if (stringIdx >= 0 && stringIdx < lnpStringCache.length) {
            String val = lnpStringCache[stringIdx];
            return val != null ? val : "str_" + stringIdx;
        }
        return "str_" + stringIdx;
    }

    private String[] buildLnpStringCache() {
        List<Long> index = lnpIndex;
        String[] cache = new String[index.size()];
        if (rawData == null) {
            return cache;
        }
        for (int i = 0; i < index.size(); i++) {
            long off = index.get(i);
            if (off >= 0 && off < rawData.length) {
                cache[i] = readMutf8At(rawData, (int) off);
            }
        }
        return cache;
    }

    static String readMutf8At(byte[] data, int offset) {
        int pos = offset;
        StringBuilder sb = new StringBuilder();
        while (pos < data.length && data[pos] != 0) {
            int b = data[pos] & 0xFF;
            if (b < 0x80) {
                sb.append((char) b);
                pos++;
            } else if ((b & 0xE0) == 0xC0) {
                if (pos + 1 < data.length) {
                    int b2 = data[pos + 1] & 0xFF;
                    char ch = (char) (((b & 0x1F) << 6)
                            | (b2 & 0x3F));
                    sb.append(ch);
                    pos += 2;
                } else {
                    break;
                }
            } else if ((b & 0xF0) == 0xE0) {
                if (pos + 2 < data.length) {
                    int b2 = data[pos + 1] & 0xFF;
                    int b3 = data[pos + 2] & 0xFF;
                    char ch = (char) (((b & 0x0F) << 12)
                            | ((b2 & 0x3F) << 6)
                            | (b3 & 0x3F));
                    sb.append(ch);
                    pos += 3;
                } else {
                    break;
                }
            } else {
                pos++;
            }
        }
        return sb.toString();
    }

    /**
     * Returns the number of debug info entries currently cached.
     *
     * @return cached debug info count
     */
    public int getDebugInfoCacheSize() {
        return debugInfoCache.size();
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

    /**
     * Returns a flat list of all methods across all classes.
     * Lazily computed and cached on first access.
     *
     * @return all methods in the file
     */
    public List<AbcMethod> getMethods() {
        if (flatMethodList != null) {
            return flatMethodList;
        }
        List<AbcMethod> allMethods = new ArrayList<>();
        for (AbcClass cls : classes) {
            allMethods.addAll(cls.getMethods());
        }
        flatMethodList = Collections.unmodifiableList(allMethods);
        flatMethodArray = allMethods.toArray(new AbcMethod[0]);
        return flatMethodList;
    }

    /**
     * Returns the method at the given flat index across all classes.
     * O(1) lookup via pre-computed array.
     *
     * @param flatIndex the 0-based flat method index
     * @return the method, or null if the index is out of range
     */
    public AbcMethod getMethodByFlatIndex(int flatIndex) {
        if (flatIndex < 0) {
            return null;
        }
        if (flatMethodArray == null) {
            getMethods();
        }
        if (flatIndex >= flatMethodArray.length) {
            return null;
        }
        return flatMethodArray[flatIndex];
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

    public List<AbcAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    public List<AbcAnnotation> getAnnotationsForClass(int classIdx) {
        return classAnnotations.getOrDefault(classIdx,
                Collections.emptyList());
    }

    public List<AbcAnnotation> getAnnotationsForField(long fieldOffset) {
        return fieldAnnotations.getOrDefault(fieldOffset,
                Collections.emptyList());
    }

    public List<AbcAnnotation> getAnnotationsForMethod(long methodOffset) {
        return methodAnnotations.getOrDefault(methodOffset,
                Collections.emptyList());
    }

    public int getAnnotationCount() {
        return annotations.size();
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
        int codeOff = (int) method.getCodeOff();
        AbcCode cached = codeCache.get(codeOff);
        if (cached != null) {
            return cached;
        }
        AbcCode code = parseCode(new AbcReader(rawData), codeOff);
        codeCache.put(codeOff, code);
        return code;
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
        int off = (int) stringOff;
        String cached = stringCache.get(off);
        if (cached != null) {
            return cached;
        }
        AbcReader r = new AbcReader(rawData);
        String result = readStringAtCached(r, off);
        return result;
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
     * Results are cached by debug info offset.
     *
     * @param method the method
     * @return the debug info, or null if none
     */
    public AbcDebugInfo getDebugInfoForMethod(AbcMethod method) {
        if (method.getDebugInfoOff() == 0) {
            return null;
        }
        int off = (int) method.getDebugInfoOff();
        AbcDebugInfo cached = debugInfoCache.get(off);
        if (cached != null) {
            return cached;
        }
        AbcDebugInfo debugInfo = parseDebugInfo(new AbcReader(rawData), off);
        debugInfoCache.put(off, debugInfo);
        return debugInfo;
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

    /**
     * Extracts local variable debug info from a line number program.
     *
     * <p>Parses the LNP state machine, collecting START_LOCAL and
     * START_LOCAL_EXTENDED entries as {@link AbcLocalVariable} objects.
     *
     * @param lnpOff the offset of the line number program data
     * @return the list of local variables (may be empty)
     */
    public List<AbcLocalVariable> parseLocalVariables(long lnpOff) {
        if (lnpOff <= 0 || lnpOff >= rawData.length) {
            return Collections.emptyList();
        }
        AbcReader r = new AbcReader(rawData);
        r.position((int) lnpOff);
        List<AbcLocalVariable> locals = new ArrayList<>();
        long address = 0;

        while (r.remaining() > 0) {
            int opcode = r.readU8() & 0xFF;

            if (opcode == 0x00) {
                break;
            } else if (opcode == 0x01) {
                address += r.readUleb128();
            } else if (opcode == 0x02) {
                r.readSleb128();
            } else if (opcode == 0x03) {
                int reg = (int) r.readUleb128();
                long nameIdx = r.readUleb128();
                long typeIdx = r.readUleb128();
                String name = nameIdx > 0
                        ? readStringAt(r, (int) nameIdx) : null;
                String typeName = typeIdx > 0
                        ? readStringAt(r, (int) typeIdx) : null;
                locals.add(new AbcLocalVariable(address, -1, reg,
                        name, typeName, null));
            } else if (opcode == 0x04) {
                int reg = (int) r.readUleb128();
                long nameIdx = r.readUleb128();
                long typeIdx = r.readUleb128();
                long sigIdx = r.readUleb128();
                String name = nameIdx > 0
                        ? readStringAt(r, (int) nameIdx) : null;
                String typeName = typeIdx > 0
                        ? readStringAt(r, (int) typeIdx) : null;
                String sig = sigIdx > 0
                        ? readStringAt(r, (int) sigIdx) : null;
                locals.add(new AbcLocalVariable(address, -1, reg,
                        name, typeName, sig));
            } else if (opcode == 0x05 || opcode == 0x06) {
                r.readUleb128();
            } else if (opcode == 0x07 || opcode == 0x08) {
                // SET_PROLOGUE_END / SET_EPILOGUE_BEGIN — no operands
                continue;
            } else if (opcode == 0x09 || opcode == 0x0A
                    || opcode == 0x0B) {
                r.readUleb128();
            } else if (opcode >= 0x0C) {
                int adjusted = opcode - 0x0C;
                address += adjusted / 15;
            }
        }

        return locals;
    }

    // --- Module record access ---

    /**
     * Returns the module record for a given class index, or null if none.
     *
     * <p>Module records are stored as special LiteralArrays referenced by a
     * field named "moduleRecordIdx" in the class data.
     *
     * @param classIdx the 0-based class index
     * @return the module record, or null
     */
    public AbcModuleRecord getModuleRecord(int classIdx) {
        Long laOff = moduleRecordOffsets.get(classIdx);
        if (laOff == null || laOff == 0) {
            return null;
        }
        AbcModuleRecord cached = moduleRecordCache.get(laOff);
        if (cached != null) {
            return cached;
        }
        AbcModuleRecord record = parseModuleRecord(laOff);
        moduleRecordCache.put(laOff, record);
        return record;
    }

    /**
     * Returns all module records in the file, keyed by class index.
     *
     * @return the map of class index to module record
     */
    public Map<Integer, AbcModuleRecord> getAllModuleRecords() {
        Map<Integer, AbcModuleRecord> result = new HashMap<>();
        for (Integer classIdx : moduleRecordOffsets.keySet()) {
            AbcModuleRecord record = getModuleRecord(classIdx);
            if (record != null) {
                result.put(classIdx, record);
            }
        }
        return result;
    }

    /**
     * Returns the number of module records found in the file.
     *
     * @return the module record count
     */
    public int getModuleRecordCount() {
        return moduleRecordOffsets.size();
    }

    /**
     * Resolves a module request path string from its offset in the module record.
     *
     * @param stringOff the string table offset
     * @return the module path string, or null if not available
     */
    public String resolveModuleRequest(long stringOff) {
        return getSourceFileName(stringOff);
    }

    /**
     * Resolves all module request paths from a module record.
     *
     * @param record the module record
     * @return the list of module request path strings
     */
    public List<String> resolveModuleRequests(AbcModuleRecord record) {
        List<String> result = new ArrayList<>(record.getModuleRequestOffsets().size());
        for (Long off : record.getModuleRequestOffsets()) {
            String path = resolveModuleRequest(off);
            result.add(path != null ? path : "");
        }
        return result;
    }

    /**
     * Parses a module record from the LiteralArray at the given offset.
     *
     * <p>Module record layout:
     * <ul>
     *   <li>num_module_requests (uint32)</li>
     *   <li>module_requests[] (uint32 each - string table offsets)</li>
     *   <li>regular_import_num (uint32)</li>
     *   <li>regular_imports[] (local_name_off: uint32, import_name_off: uint32,
     *       module_request_idx: uint16)</li>
     *   <li>namespace_import_num (uint32)</li>
     *   <li>namespace_imports[] (local_name_off: uint32,
     *       module_request_idx: uint16)</li>
     *   <li>local_export_num (uint32)</li>
     *   <li>local_exports[] (local_name_off: uint32, export_name_off: uint32)</li>
     *   <li>indirect_export_num (uint32)</li>
     *   <li>indirect_exports[] (export_name_off: uint32, import_name_off: uint32,
     *       module_request_idx: uint16)</li>
     *   <li>star_export_num (uint32)</li>
     *   <li>star_exports[] (module_request_idx: uint16)</li>
     * </ul>
     *
     * @param literalArrayOff the offset of the module record LiteralArray
     * @return the parsed module record
     */
    private AbcModuleRecord parseModuleRecord(long literalArrayOff) {
        AbcReader r = new AbcReader(rawData);
        r.position((int) literalArrayOff);

        // Skip num_literals header (uint32)
        long numLiterals = r.readU32();

        // Read module requests
        long numModuleRequests = r.readU32();
        List<Long> moduleRequestOffsets = new ArrayList<>((int) numModuleRequests);
        for (int i = 0; i < (int) numModuleRequests; i++) {
            moduleRequestOffsets.add(r.readU32());
        }

        // Read regular imports
        long regularImportNum = r.readU32();
        List<AbcModuleRecord.RegularImport> regularImports =
                new ArrayList<>((int) regularImportNum);
        for (int i = 0; i < (int) regularImportNum; i++) {
            long localNameOff = r.readU32();
            long importNameOff = r.readU32();
            int moduleRequestIdx = r.readU16();
            regularImports.add(new AbcModuleRecord.RegularImport(
                    localNameOff, importNameOff, moduleRequestIdx));
        }

        // Read namespace imports
        long namespaceImportNum = r.readU32();
        List<AbcModuleRecord.NamespaceImport> namespaceImports =
                new ArrayList<>((int) namespaceImportNum);
        for (int i = 0; i < (int) namespaceImportNum; i++) {
            long localNameOff = r.readU32();
            int moduleRequestIdx = r.readU16();
            namespaceImports.add(new AbcModuleRecord.NamespaceImport(
                    localNameOff, moduleRequestIdx));
        }

        // Read local exports
        long localExportNum = r.readU32();
        List<AbcModuleRecord.LocalExport> localExports =
                new ArrayList<>((int) localExportNum);
        for (int i = 0; i < (int) localExportNum; i++) {
            long localNameOff = r.readU32();
            long exportNameOff = r.readU32();
            localExports.add(new AbcModuleRecord.LocalExport(
                    localNameOff, exportNameOff));
        }

        // Read indirect exports
        long indirectExportNum = r.readU32();
        List<AbcModuleRecord.IndirectExport> indirectExports =
                new ArrayList<>((int) indirectExportNum);
        for (int i = 0; i < (int) indirectExportNum; i++) {
            long exportNameOff = r.readU32();
            long importNameOff = r.readU32();
            int moduleRequestIdx = r.readU16();
            indirectExports.add(new AbcModuleRecord.IndirectExport(
                    exportNameOff, importNameOff, moduleRequestIdx));
        }

        // Read star exports
        long starExportNum = r.readU32();
        List<AbcModuleRecord.StarExport> starExports =
                new ArrayList<>((int) starExportNum);
        for (int i = 0; i < (int) starExportNum; i++) {
            int moduleRequestIdx = r.readU16();
            starExports.add(new AbcModuleRecord.StarExport(moduleRequestIdx));
        }

        return new AbcModuleRecord(moduleRequestOffsets, regularImports,
                namespaceImports, localExports, indirectExports, starExports,
                literalArrayOff);
    }
}
