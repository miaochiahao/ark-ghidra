package com.arkghidra.format;

/**
 * A single element (key-value pair) within an annotation.
 *
 * <p>Annotation elements encode named parameters passed to a decorator.
 * Each element has a name and a typed value determined by its tag:
 * <ul>
 *   <li>0x03 (INT) - a 32-bit signed integer</li>
 *   <li>0x04 (DOUBLE) - a 64-bit floating-point number</li>
 *   <li>0x05 (STRING) - a string table index</li>
 *   <li>0x06 (ID) - an identifier reference (string table index)</li>
 * </ul>
 */
public class AbcAnnotationElement {
    /** Element value type tag for integer values. */
    public static final int TAG_INT = 0x03;
    /** Element value type tag for double-precision floating-point values. */
    public static final int TAG_DOUBLE = 0x04;
    /** Element value type tag for string values (string table index). */
    public static final int TAG_STRING = 0x05;
    /** Element value type tag for identifier values (string table index). */
    public static final int TAG_ID = 0x06;

    private final int nameIdx;
    private final String name;
    private final int tag;
    private final long intValue;
    private final double doubleValue;
    private final String stringValue;
    private final String idValue;

    /**
     * Constructs an annotation element with an integer value.
     *
     * @param nameIdx the string table index of the element name
     * @param name the resolved element name
     * @param tag the value type tag (must be TAG_INT)
     * @param intValue the integer value
     */
    public AbcAnnotationElement(int nameIdx, String name, int tag, long intValue) {
        this.nameIdx = nameIdx;
        this.name = name;
        this.tag = tag;
        this.intValue = intValue;
        this.doubleValue = 0.0;
        this.stringValue = null;
        this.idValue = null;
    }

    /**
     * Constructs an annotation element with a double value.
     *
     * @param nameIdx the string table index of the element name
     * @param name the resolved element name
     * @param tag the value type tag (must be TAG_DOUBLE)
     * @param doubleValue the double value
     */
    public AbcAnnotationElement(int nameIdx, String name, int tag, double doubleValue) {
        this.nameIdx = nameIdx;
        this.name = name;
        this.tag = tag;
        this.intValue = 0;
        this.doubleValue = doubleValue;
        this.stringValue = null;
        this.idValue = null;
    }

    /**
     * Constructs an annotation element with a string value.
     *
     * @param nameIdx the string table index of the element name
     * @param name the resolved element name
     * @param tag the value type tag (must be TAG_STRING or TAG_ID)
     * @param stringValue the string value
     * @param isId true if this is an ID reference, false if a plain string
     */
    public AbcAnnotationElement(int nameIdx, String name, int tag,
            String stringValue, boolean isId) {
        this.nameIdx = nameIdx;
        this.name = name;
        this.tag = tag;
        this.intValue = 0;
        this.doubleValue = 0.0;
        this.stringValue = isId ? null : stringValue;
        this.idValue = isId ? stringValue : null;
    }

    public int getNameIdx() {
        return nameIdx;
    }

    public String getName() {
        return name;
    }

    public int getTag() {
        return tag;
    }

    public long getIntValue() {
        return intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public String getIdValue() {
        return idValue;
    }

    /**
     * Returns the value as a formatted string suitable for ArkTS source output.
     * Strings are quoted, integers and doubles are plain.
     *
     * @return the formatted value string
     */
    public String getValueAsString() {
        switch (tag) {
            case TAG_INT:
                return String.valueOf(intValue);
            case TAG_DOUBLE:
                return String.valueOf(doubleValue);
            case TAG_STRING:
                return stringValue != null ? "\"" + stringValue + "\"" : "null";
            case TAG_ID:
                return idValue != null ? idValue : "null";
            default:
                return String.valueOf(intValue);
        }
    }
}
