package com.arkghidra.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbcAnnotationTest {

    private static final int FILE_SIZE = 8192;
    private static final int STRING_AREA_OFF = 200;
    private static final int CLASS_OFF = 800;
    private static final int CODE_OFF = 2000;
    private static final int REGION_HEADER_OFF = 3000;
    private static final int CLASS_IDX_OFF = 3100;
    private static final int LITERAL_ARRAY_IDX_OFF = 3108;
    private static final int LNP_IDX_OFF = 3116;
    private static final int CLASS_REGION_IDX_OFF = 3200;
    private static final int ANNOTATION_DATA_OFF = 600;

    private static byte[] uleb128(long value) {
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

    private static byte[] sleb128(long value) {
        byte[] buf = new byte[10];
        int i = 0;
        boolean more = true;
        while (more) {
            byte b = (byte) (value & 0x7F);
            value >>= 7;
            if ((value == 0 && (b & 0x40) == 0)
                    || (value == -1 && (b & 0x40) != 0)) {
                more = false;
            } else {
                b |= 0x80;
            }
            buf[i++] = b;
        }
        byte[] result = new byte[i];
        System.arraycopy(buf, 0, result, 0, i);
        return result;
    }

    private static byte[] mutf8String(String s) {
        byte[] strBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] header = uleb128(((long) s.length() << 1) | 1);
        byte[] result = new byte[header.length + strBytes.length + 1];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(strBytes, 0, result, header.length, strBytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    /**
     * Writes the common ABC header and structure (class index, region header,
     * region index) into the buffer.
     */
    private static void writeAbcSkeleton(ByteBuffer bb) {
        bb.put(new byte[]{'P', 'A', 'N', 'D', 'A', 0, 0, 0});
        bb.putInt(0);
        bb.put(new byte[]{'0', '0', '0', '2'});
        bb.putInt(FILE_SIZE);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(1); // numClasses
        bb.putInt(CLASS_IDX_OFF);
        bb.putInt(0); // numLnps
        bb.putInt(LNP_IDX_OFF);
        bb.putInt(0); // numLiteralArrays
        bb.putInt(LITERAL_ARRAY_IDX_OFF);
        bb.putInt(1); // numIndexRegions
        bb.putInt(REGION_HEADER_OFF);

        bb.position(CLASS_IDX_OFF);
        bb.putInt(CLASS_OFF);

        bb.position(REGION_HEADER_OFF);
        bb.putInt(CLASS_OFF);
        bb.putInt(CODE_OFF + 32);
        bb.putInt(1);
        bb.putInt(CLASS_REGION_IDX_OFF);
        for (int i = 0; i < 6; i++) {
            bb.putInt(0);
        }

        bb.position(CLASS_REGION_IDX_OFF);
        bb.putInt(CLASS_OFF);
    }

    /**
     * Writes a class with no fields, no methods, and the given class tags.
     */
    private static void writeClassDef(ByteBuffer bb, String className,
            byte[] classTags) {
        bb.position(CLASS_OFF);
        bb.put(mutf8String(className));
        bb.putInt(0); // superClassOff
        bb.put(uleb128(0x0001)); // accessFlags
        bb.put(uleb128(0)); // numFields
        bb.put(uleb128(0)); // numMethods
        bb.put(classTags);
    }

    // --- Tests ---

    @Test
    void testAnnotationSimpleTypeName() {
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/Component;",
                Collections.emptyList(), 100L);
        assertEquals("Component", ann.getSimpleTypeName());
    }

    @Test
    void testAnnotationSimpleNameWithoutPrefix() {
        AbcAnnotation ann = new AbcAnnotation(0, "Component",
                Collections.emptyList(), 100L);
        assertEquals("Component", ann.getSimpleTypeName());
    }

    @Test
    void testAnnotationWithNestedNamespace() {
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/ark/ui/Entry;",
                Collections.emptyList(), 100L);
        assertEquals("Entry", ann.getSimpleTypeName());
    }

    @Test
    void testDecoratorStringNoElements() {
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/Component;",
                Collections.emptyList(), 100L);
        assertEquals("@Component", ann.toDecoratorString());
    }

    @Test
    void testDecoratorStringWithIntElement() {
        AbcAnnotationElement elem = new AbcAnnotationElement(0, "count",
                AbcAnnotationElement.TAG_INT, 42L);
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/Prop;",
                List.of(elem), 100L);
        assertEquals("@Prop(count: 42)", ann.toDecoratorString());
    }

    @Test
    void testDecoratorStringWithStringElement() {
        AbcAnnotationElement elem = new AbcAnnotationElement(0, "name",
                AbcAnnotationElement.TAG_STRING, "hello", false);
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/Param;",
                List.of(elem), 100L);
        assertEquals("@Param(name: \"hello\")", ann.toDecoratorString());
    }

    @Test
    void testElementGetValueAsString_int() {
        AbcAnnotationElement elem = new AbcAnnotationElement(0, "val",
                AbcAnnotationElement.TAG_INT, 100L);
        assertEquals("100", elem.getValueAsString());
    }

    @Test
    void testElementGetValueAsString_string() {
        AbcAnnotationElement elem = new AbcAnnotationElement(0, "val",
                AbcAnnotationElement.TAG_STRING, "test", false);
        assertEquals("\"test\"", elem.getValueAsString());
    }

    @Test
    void testElementGetValueAsString_id() {
        AbcAnnotationElement elem = new AbcAnnotationElement(0, "ref",
                AbcAnnotationElement.TAG_ID, "SomeClass", true);
        assertEquals("SomeClass", elem.getValueAsString());
    }

    @Test
    void testAnnotationElementAccessors() {
        AbcAnnotationElement intElem = new AbcAnnotationElement(1, "count",
                AbcAnnotationElement.TAG_INT, 5L);
        assertEquals(1, intElem.getNameIdx());
        assertEquals("count", intElem.getName());
        assertEquals(AbcAnnotationElement.TAG_INT, intElem.getTag());
        assertEquals(5L, intElem.getIntValue());

        AbcAnnotationElement strElem = new AbcAnnotationElement(2, "label",
                AbcAnnotationElement.TAG_STRING, "ok", false);
        assertEquals(2, strElem.getNameIdx());
        assertEquals("label", strElem.getName());
        assertEquals("ok", strElem.getStringValue());
    }

    @Test
    void testParseAnnotationFromClassTag() {
        ByteBuffer bb = ByteBuffer.allocate(FILE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeAbcSkeleton(bb);

        // Write strings: className, annotation typeName
        bb.position(STRING_AREA_OFF);
        byte[] classNameBytes = mutf8String("Lcom/example/MyComp;");
        bb.put(classNameBytes);
        int annTypeNameOff = bb.position();
        bb.put(mutf8String("Lohos/Component;"));

        // Write annotation data at ANNOTATION_DATA_OFF
        ByteBuffer annBuf = ByteBuffer.allocate(32)
                .order(ByteOrder.LITTLE_ENDIAN);
        annBuf.putInt(annTypeNameOff);
        annBuf.put((byte) 1);
        annBuf.put(uleb128(0));
        byte[] annData = new byte[annBuf.position()];
        annBuf.flip();
        annBuf.get(annData);
        bb.position(ANNOTATION_DATA_OFF);
        bb.put(annData);

        // Class with annotation tag
        ByteBuffer tags = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN);
        tags.put((byte) 0x05);
        tags.putInt(ANNOTATION_DATA_OFF);
        tags.put((byte) 0x00);
        byte[] tagBytes = new byte[tags.position()];
        tags.flip();
        tags.get(tagBytes);
        writeClassDef(bb, "Lcom/example/MyComp;", tagBytes);

        bb.position(0);
        byte[] result = new byte[FILE_SIZE];
        bb.get(result);
        AbcFile abcFile = AbcFile.parse(result);

        assertEquals(1, abcFile.getAnnotationCount());
        List<AbcAnnotation> classAnns =
                abcFile.getAnnotationsForClass(0);
        assertEquals(1, classAnns.size());
        assertEquals("Lohos/Component;", classAnns.get(0).getTypeName());
        assertEquals("Component", classAnns.get(0).getSimpleTypeName());
    }

    @Test
    void testParseAnnotationWithIntElement() {
        ByteBuffer bb = ByteBuffer.allocate(FILE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeAbcSkeleton(bb);

        // Write strings: className, typeName, elemName
        bb.position(STRING_AREA_OFF);
        byte[] classNameBytes = mutf8String("Lcom/example/MyComp;");
        bb.put(classNameBytes);
        int annTypeNameOff = bb.position();
        byte[] annTypeNameBytes = mutf8String("Lohos/Prop;");
        bb.put(annTypeNameBytes);
        int elemNameOff = bb.position();
        bb.put(mutf8String("count"));

        // Build element: nameIdx, TAG_INT, sleb128(42)
        ByteBuffer elemBuf = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN);
        elemBuf.putInt(elemNameOff);
        elemBuf.put((byte) AbcAnnotationElement.TAG_INT);
        elemBuf.put(sleb128(42));
        byte[] elemData = new byte[elemBuf.position()];
        elemBuf.flip();
        elemBuf.get(elemData);

        // Annotation: typeIdx, retention=1, num_elements=1, element
        ByteBuffer annBuf = ByteBuffer.allocate(32)
                .order(ByteOrder.LITTLE_ENDIAN);
        annBuf.putInt(annTypeNameOff);
        annBuf.put((byte) 1);
        annBuf.put(uleb128(1));
        annBuf.put(elemData);
        byte[] annData = new byte[annBuf.position()];
        annBuf.flip();
        annBuf.get(annData);
        bb.position(ANNOTATION_DATA_OFF);
        bb.put(annData);

        ByteBuffer tags = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN);
        tags.put((byte) 0x05);
        tags.putInt(ANNOTATION_DATA_OFF);
        tags.put((byte) 0x00);
        byte[] tagBytes = new byte[tags.position()];
        tags.flip();
        tags.get(tagBytes);
        writeClassDef(bb, "Lcom/example/MyComp;", tagBytes);

        bb.position(0);
        byte[] result = new byte[FILE_SIZE];
        bb.get(result);
        AbcFile abcFile = AbcFile.parse(result);

        List<AbcAnnotation> classAnns =
                abcFile.getAnnotationsForClass(0);
        assertEquals(1, classAnns.size());
        AbcAnnotation ann = classAnns.get(0);
        assertEquals("Lohos/Prop;", ann.getTypeName());
        assertEquals(1, ann.getElements().size());
        assertEquals(AbcAnnotationElement.TAG_INT,
                ann.getElements().get(0).getTag());
        assertEquals(42L, ann.getElements().get(0).getIntValue());
    }

    @Test
    void testParseAnnotationWithStringElement() {
        ByteBuffer bb = ByteBuffer.allocate(FILE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeAbcSkeleton(bb);

        // Write strings: className, typeName, elemName, elemStrVal
        bb.position(STRING_AREA_OFF);
        byte[] classNameBytes = mutf8String("Lcom/example/MyComp;");
        bb.put(classNameBytes);
        int annTypeNameOff = bb.position();
        byte[] annTypeNameBytes = mutf8String("Lohos/Param;");
        bb.put(annTypeNameBytes);
        int elemNameOff = bb.position();
        byte[] elemNameBytes = mutf8String("msg");
        bb.put(elemNameBytes);
        int elemStrValOff = bb.position();
        bb.put(mutf8String("hello"));

        // Element: nameIdx, TAG_STRING, stringIdx
        ByteBuffer elemBuf = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN);
        elemBuf.putInt(elemNameOff);
        elemBuf.put((byte) AbcAnnotationElement.TAG_STRING);
        elemBuf.putInt(elemStrValOff);
        byte[] elemData = new byte[elemBuf.position()];
        elemBuf.flip();
        elemBuf.get(elemData);

        // Annotation
        ByteBuffer annBuf = ByteBuffer.allocate(32)
                .order(ByteOrder.LITTLE_ENDIAN);
        annBuf.putInt(annTypeNameOff);
        annBuf.put((byte) 1);
        annBuf.put(uleb128(1));
        annBuf.put(elemData);
        byte[] annData = new byte[annBuf.position()];
        annBuf.flip();
        annBuf.get(annData);
        bb.position(ANNOTATION_DATA_OFF);
        bb.put(annData);

        ByteBuffer tags = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN);
        tags.put((byte) 0x05);
        tags.putInt(ANNOTATION_DATA_OFF);
        tags.put((byte) 0x00);
        byte[] tagBytes = new byte[tags.position()];
        tags.flip();
        tags.get(tagBytes);
        writeClassDef(bb, "Lcom/example/MyComp;", tagBytes);

        bb.position(0);
        byte[] result = new byte[FILE_SIZE];
        bb.get(result);
        AbcFile abcFile = AbcFile.parse(result);

        List<AbcAnnotation> classAnns =
                abcFile.getAnnotationsForClass(0);
        assertEquals(1, classAnns.size());
        AbcAnnotation ann = classAnns.get(0);
        assertEquals("Lohos/Param;", ann.getTypeName());
        assertEquals(1, ann.getElements().size());
        assertEquals(AbcAnnotationElement.TAG_STRING,
                ann.getElements().get(0).getTag());
        assertEquals("hello", ann.getElements().get(0).getStringValue());
    }

    @Test
    void testNoAnnotationsReturnsEmptyList() {
        ByteBuffer bb = ByteBuffer.allocate(FILE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        writeAbcSkeleton(bb);

        bb.position(STRING_AREA_OFF);
        bb.put(mutf8String("Lcom/example/Plain;"));

        writeClassDef(bb, "Lcom/example/Plain;", new byte[]{0x00});

        bb.position(0);
        byte[] result = new byte[FILE_SIZE];
        bb.get(result);

        AbcFile abcFile = AbcFile.parse(result);
        assertEquals(0, abcFile.getAnnotationCount());
        assertTrue(abcFile.getAnnotationsForClass(0).isEmpty());
        assertTrue(abcFile.getAnnotations().isEmpty());
    }

    @Test
    void testDecoratorStringWithMultipleElements() {
        AbcAnnotationElement elem1 = new AbcAnnotationElement(0, "name",
                AbcAnnotationElement.TAG_STRING, "test", false);
        AbcAnnotationElement elem2 = new AbcAnnotationElement(1, "count",
                AbcAnnotationElement.TAG_INT, 5L);
        AbcAnnotation ann = new AbcAnnotation(0, "Lohos/Widget;",
                List.of(elem1, elem2), 100L);
        String dec = ann.toDecoratorString();
        assertEquals("@Widget(name: \"test\", count: 5)", dec);
    }

    @Test
    void testAnnotationAccessors() {
        List<AbcAnnotationElement> elems = Collections.emptyList();
        AbcAnnotation ann = new AbcAnnotation(42, "Lohos/Entry;",
                elems, 500L);
        assertEquals(42, ann.getTypeIdx());
        assertEquals("Lohos/Entry;", ann.getTypeName());
        assertEquals(elems, ann.getElements());
        assertEquals(500L, ann.getTargetOff());
    }
}
