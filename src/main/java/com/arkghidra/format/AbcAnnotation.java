package com.arkghidra.format;

import java.util.Collections;
import java.util.List;

/**
 * An annotation (decorator) parsed from the .abc file.
 *
 * <p>In ArkTS/HarmonyOS bytecode, annotations encode decorator metadata
 * such as {@code @Component}, {@code @Entry}, {@code @State}, etc.
 * Each annotation has a type name and zero or more named elements
 * (key-value pairs).
 *
 * <p>Annotations can be attached to classes, methods, or fields and are
 * typically found in the tagged-value sections of those structures.
 */
public class AbcAnnotation {
    private final int typeIdx;
    private final String typeName;
    private final List<AbcAnnotationElement> elements;
    private final long targetOff;

    /**
     * Constructs an annotation.
     *
     * @param typeIdx the string table index of the annotation type name
     * @param typeName the resolved annotation type name (e.g., "Component", "State")
     * @param elements the annotation elements (key-value pairs), may be empty
     * @param targetOff the offset in the ABC file where this annotation is defined
     */
    public AbcAnnotation(int typeIdx, String typeName,
            List<AbcAnnotationElement> elements, long targetOff) {
        this.typeIdx = typeIdx;
        this.typeName = typeName;
        this.elements = Collections.unmodifiableList(elements);
        this.targetOff = targetOff;
    }

    public int getTypeIdx() {
        return typeIdx;
    }

    public String getTypeName() {
        return typeName;
    }

    public List<AbcAnnotationElement> getElements() {
        return elements;
    }

    public long getTargetOff() {
        return targetOff;
    }

    /**
     * Returns the simple decorator name without any namespace prefix.
     * For example, "Lohos/Component;" becomes "Component".
     *
     * @return the simple decorator name
     */
    public String getSimpleTypeName() {
        if (typeName == null) {
            return "";
        }
        String name = typeName;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        int semi = name.indexOf(';');
        if (semi >= 0) {
            name = name.substring(0, semi);
        }
        if (name.startsWith("L") && name.length() > 1) {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Formats this annotation as an ArkTS decorator string.
     * For example: {@code @Component} or {@code @Param("value")}
     * or {@code @Param(count: 5, name: "test")}.
     *
     * @return the formatted decorator string
     */
    public String toDecoratorString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(getSimpleTypeName());
        if (!elements.isEmpty()) {
            sb.append("(");
            if (elements.size() == 1 && elements.get(0).getName() == null) {
                sb.append(elements.get(0).getValueAsString());
            } else {
                for (int i = 0; i < elements.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    AbcAnnotationElement elem = elements.get(i);
                    if (elem.getName() != null) {
                        sb.append(elem.getName()).append(": ");
                    }
                    sb.append(elem.getValueAsString());
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
