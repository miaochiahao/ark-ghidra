package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for annotation class detection and rendering.
 * Uses package-private access to DeclarationBuilder.
 */
class AnnotationClassTest {

    private final ArkTSDecompiler decompiler = new ArkTSDecompiler();
    private final DeclarationBuilder declBuilder =
            new DeclarationBuilder(decompiler);

    @Nested
    @DisplayName("Annotation class detection")
    class AnnotationClassDetection {

        @Test
        @DisplayName("ACC_ANNOTATION class rendered as class")
        void testAnnotationClassRendered() {
            AbcClass cls = new AbcClass("Lcom/example/MyAnnotation;",
                    0, AbcAccessFlags.ACC_ANNOTATION | AbcAccessFlags.ACC_PUBLIC,
                    Collections.emptyList(), Collections.emptyList(), 0);

            ArkTSStatement decl = declBuilder.buildClassDeclaration(
                    cls, null, new HashSet<>());

            assertTrue(decl instanceof ArkTSDeclarations.ClassDeclaration,
                    "Should be ClassDeclaration: "
                            + decl.getClass().getSimpleName());
            String output = decl.toArkTS(0);
            assertTrue(output.startsWith("class MyAnnotation"),
                    "Should start with 'class MyAnnotation': " + output);
        }

        @Test
        @DisplayName("Non-annotation abstract class stays abstract class")
        void testNonAnnotationClassPreserved() {
            AbcClass cls = new AbcClass("Lcom/example/MyClass;",
                    0, AbcAccessFlags.ACC_ABSTRACT | AbcAccessFlags.ACC_PUBLIC,
                    Collections.emptyList(), Collections.emptyList(), 0);

            ArkTSStatement decl = declBuilder.buildClassDeclaration(
                    cls, null, new HashSet<>());

            String output = decl.toArkTS(0);
            assertTrue(output.contains("abstract class"),
                    "Should be abstract class: " + output);
        }

        @Test
        @DisplayName("Annotation class with fields renders fields")
        void testAnnotationWithFields() {
            AbcField field = new AbcField(0, 0, "value",
                    AbcAccessFlags.ACC_PUBLIC, 0);
            AbcClass cls = new AbcClass("Lcom/example/Log;",
                    0, AbcAccessFlags.ACC_ANNOTATION | AbcAccessFlags.ACC_PUBLIC,
                    List.of(field), Collections.emptyList(), 0);

            ArkTSStatement decl = declBuilder.buildClassDeclaration(
                    cls, null, new HashSet<>());

            String output = decl.toArkTS(0);
            assertTrue(output.contains("value"),
                    "Should contain field 'value': " + output);
        }

        @Test
        @DisplayName("ACC_INTERFACE takes priority over ACC_ANNOTATION")
        void testInterfacePriorityOverAnnotation() {
            AbcClass cls = new AbcClass("Lcom/example/IAnno;",
                    0,
                    AbcAccessFlags.ACC_INTERFACE | AbcAccessFlags.ACC_ANNOTATION
                            | AbcAccessFlags.ACC_ABSTRACT | AbcAccessFlags.ACC_PUBLIC,
                    Collections.emptyList(), Collections.emptyList(), 0);

            ArkTSStatement decl = declBuilder.buildClassDeclaration(
                    cls, null, new HashSet<>());

            assertTrue(decl
                    instanceof ArkTSTypeDeclarations.InterfaceDeclaration,
                    "Interface should take priority: "
                            + decl.getClass().getSimpleName());
        }

        @Test
        @DisplayName("Plain class without ACC_ANNOTATION not affected")
        void testPlainClassNotAffected() {
            AbcClass cls = new AbcClass("Lcom/example/Foo;",
                    0, AbcAccessFlags.ACC_PUBLIC,
                    Collections.emptyList(), Collections.emptyList(), 0);

            ArkTSStatement decl = declBuilder.buildClassDeclaration(
                    cls, null, new HashSet<>());

            assertTrue(decl instanceof ArkTSDeclarations.ClassDeclaration);
            String output = decl.toArkTS(0);
            assertTrue(output.startsWith("class Foo"),
                    "Should start with 'class Foo': " + output);
        }
    }
}
