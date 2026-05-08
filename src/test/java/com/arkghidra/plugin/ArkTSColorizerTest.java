package com.arkghidra.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arkghidra.plugin.ArkTSColorizer.StyledSegment;
import com.arkghidra.plugin.ArkTSColorizer.TokenType;

/**
 * Tests for the ArkTS syntax colorizer.
 *
 * <p>Verifies that keywords, types, decorators, strings, comments,
 * and numeric literals are correctly classified into their respective
 * token types.</p>
 */
class ArkTSColorizerTest {

    private ArkTSColorizer colorizer;

    @BeforeEach
    void setUp() {
        colorizer = new ArkTSColorizer();
    }

    // --- Keyword detection ---

    @Test
    @DisplayName("let keyword is classified as KEYWORD")
    void testLetKeyword() {
        List<StyledSegment> segs = colorizer.colorizeLine("let x = 42;");
        assertSegmentType(segs, "let", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("const keyword is classified as KEYWORD")
    void testConstKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("const PI = 3.14;");
        assertSegmentType(segs, "const", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("class keyword is classified as KEYWORD")
    void testClassKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("class MyClass {");
        assertSegmentType(segs, "class", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("async keyword is classified as KEYWORD")
    void testAsyncKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("async function fetch() {");
        assertSegmentType(segs, "async", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("return keyword is classified as KEYWORD")
    void testReturnKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("return 42;");
        assertSegmentType(segs, "return", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("import keyword is classified as KEYWORD")
    void testImportKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("import { Foo } from 'bar';");
        assertSegmentType(segs, "import", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("if and else keywords are classified as KEYWORD")
    void testIfElseKeywords() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("if (x) { } else { }");
        assertSegmentType(segs, "if", TokenType.KEYWORD);
        assertSegmentType(segs, "else", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("for keyword is classified as KEYWORD")
    void testForKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("for (let i = 0; i < 10; i++) {");
        assertSegmentType(segs, "for", TokenType.KEYWORD);
    }

    @Test
    @DisplayName("function keyword is classified as KEYWORD")
    void testFunctionKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("function add(a: number) {");
        assertSegmentType(segs, "function", TokenType.KEYWORD);
    }

    // --- Type detection ---

    @Test
    @DisplayName("number type is classified as TYPE")
    void testNumberType() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let x: number = 1;");
        assertSegmentType(segs, "number", TokenType.TYPE);
    }

    @Test
    @DisplayName("string type is classified as TYPE")
    void testStringType() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let name: string = 'hi';");
        assertSegmentType(segs, "string", TokenType.TYPE);
    }

    @Test
    @DisplayName("boolean type is classified as TYPE")
    void testBooleanType() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let flag: boolean = true;");
        assertSegmentType(segs, "boolean", TokenType.TYPE);
    }

    @Test
    @DisplayName("void type is classified as TYPE")
    void testVoidType() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("function f(): void {");
        assertSegmentType(segs, "void", TokenType.TYPE);
    }

    @Test
    @DisplayName("true and false literals are classified as TYPE")
    void testBooleanLiterals() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let a = true; let b = false;");
        assertSegmentType(segs, "true", TokenType.TYPE);
        assertSegmentType(segs, "false", TokenType.TYPE);
    }

    // --- Decorator detection ---

    @Test
    @DisplayName("@Component decorator is classified as DECORATOR")
    void testComponentDecorator() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("@Component");
        assertSegmentType(segs, "@Component", TokenType.DECORATOR);
    }

    @Test
    @DisplayName("@State decorator is classified as DECORATOR")
    void testStateDecorator() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("@State");
        assertSegmentType(segs, "@State", TokenType.DECORATOR);
    }

    @Test
    @DisplayName("@Entry decorator is classified as DECORATOR")
    void testEntryDecorator() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("@Entry");
        assertSegmentType(segs, "@Entry", TokenType.DECORATOR);
    }

    @Test
    @DisplayName("@Prop decorator is classified as DECORATOR")
    void testPropDecorator() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("@Prop message: string;");
        assertSegmentType(segs, "@Prop", TokenType.DECORATOR);
    }

    // --- String literal detection ---

    @Test
    @DisplayName("Single-quoted string is classified as STRING")
    void testSingleQuotedString() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let s = 'hello';");
        assertSegmentType(segs, "'hello'", TokenType.STRING);
    }

    @Test
    @DisplayName("Double-quoted string is classified as STRING")
    void testDoubleQuotedString() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let s = \"world\";");
        assertSegmentType(segs, "\"world\"", TokenType.STRING);
    }

    @Test
    @DisplayName("Template literal is classified as STRING")
    void testTemplateLiteral() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let s = `template`;");
        assertSegmentType(segs, "`template`", TokenType.STRING);
    }

    // --- Comment detection ---

    @Test
    @DisplayName("Line comment is classified as COMMENT")
    void testLineComment() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("// this is a comment");
        assertSegmentType(segs, "// this is a comment",
                TokenType.COMMENT);
    }

    @Test
    @DisplayName("Inline block comment is classified as COMMENT")
    void testInlineBlockComment() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let x = /* inline */ 5;");
        assertSegmentType(segs, "/* inline */", TokenType.COMMENT);
    }

    @Test
    @DisplayName("Multi-line block comment across two lines")
    void testMultiLineBlockComment() {
        String source = "let x = /* start\n   end */ 5;";
        List<List<StyledSegment>> lines =
                colorizer.colorizeSource(source);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).stream().anyMatch(s ->
                s.getTokenType() == TokenType.COMMENT
                        && s.getText().contains("/*")));
        assertTrue(lines.get(1).stream().anyMatch(s ->
                s.getTokenType() == TokenType.COMMENT
                        && s.getText().contains("*/")));
    }

    // --- Number detection ---

    @Test
    @DisplayName("Integer literal is classified as NUMBER")
    void testIntegerLiteral() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let x = 42;");
        assertSegmentType(segs, "42", TokenType.NUMBER);
    }

    @Test
    @DisplayName("Hex literal is classified as NUMBER")
    void testHexLiteral() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let x = 0xFF;");
        assertSegmentType(segs, "0xFF", TokenType.NUMBER);
    }

    @Test
    @DisplayName("Float literal is classified as NUMBER")
    void testFloatLiteral() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let x = 3.14;");
        assertSegmentType(segs, "3.14", TokenType.NUMBER);
    }

    // --- Modifier detection ---

    @Test
    @DisplayName("public modifier is classified as MODIFIER")
    void testPublicModifier() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("public method() {");
        assertSegmentType(segs, "public", TokenType.MODIFIER);
    }

    @Test
    @DisplayName("private modifier is classified as MODIFIER")
    void testPrivateModifier() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("private field: number;");
        assertSegmentType(segs, "private", TokenType.MODIFIER);
    }

    @Test
    @DisplayName("this keyword is classified as MODIFIER")
    void testThisKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("let v0 = this;");
        assertSegmentType(segs, "this", TokenType.MODIFIER);
    }

    @Test
    @DisplayName("super keyword is classified as MODIFIER")
    void testSuperKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("super();");
        assertSegmentType(segs, "super", TokenType.MODIFIER);
    }

    // --- Coverage and edge cases ---

    @Test
    @DisplayName("Empty input returns empty list")
    void testEmptyInput() {
        assertTrue(colorizer.colorizeLine("").isEmpty());
        assertTrue(colorizer.colorizeLine(null).isEmpty());
    }

    @Test
    @DisplayName("Plain text with no keywords returns PLAIN segments")
    void testPlainText() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("x + y");
        boolean allPlain = segs.stream().allMatch(
                s -> s.getTokenType() == TokenType.PLAIN
                        || s.getTokenType() == TokenType.MODIFIER);
        assertTrue(allPlain,
                "Expected all segments to be PLAIN or MODIFIER, got: "
                        + segs);
    }

    @Test
    @DisplayName("All control flow keywords are recognized")
    void testAllControlFlowKeywords() {
        for (String kw : ArkTSColorizer.CONTROL_FLOW_KEYWORDS) {
            assertTrue(ArkTSColorizer.isControlFlowKeyword(kw),
                    "Expected '" + kw + "' to be a control flow keyword");
            assertTrue(ArkTSColorizer.isKeyword(kw),
                    "Expected '" + kw + "' to be a keyword");
        }
    }

    @Test
    @DisplayName("All declaration keywords are recognized")
    void testAllDeclarationKeywords() {
        for (String kw : ArkTSColorizer.DECLARATION_KEYWORDS) {
            assertTrue(ArkTSColorizer.isDeclarationKeyword(kw),
                    "Expected '" + kw
                            + "' to be a declaration keyword");
            assertTrue(ArkTSColorizer.isKeyword(kw),
                    "Expected '" + kw + "' to be a keyword");
        }
    }

    @Test
    @DisplayName("All type keywords are recognized")
    void testAllTypeKeywords() {
        for (String kw : ArkTSColorizer.TYPE_KEYWORDS) {
            assertTrue(ArkTSColorizer.isTypeKeyword(kw),
                    "Expected '" + kw + "' to be a type keyword");
            assertTrue(ArkTSColorizer.isKeyword(kw),
                    "Expected '" + kw + "' to be a keyword");
        }
    }

    @Test
    @DisplayName("All modifier keywords are recognized")
    void testAllModifierKeywords() {
        for (String kw : ArkTSColorizer.MODIFIER_KEYWORDS) {
            assertTrue(ArkTSColorizer.isModifierKeyword(kw),
                    "Expected '" + kw + "' to be a modifier keyword");
            assertTrue(ArkTSColorizer.isKeyword(kw),
                    "Expected '" + kw + "' to be a keyword");
        }
    }

    @Test
    @DisplayName("Non-keyword is not flagged as keyword")
    void testNonKeyword() {
        assertFalse(ArkTSColorizer.isKeyword("myVariable"));
        assertFalse(ArkTSColorizer.isKeyword("MyClass"));
        assertFalse(ArkTSColorizer.isKeyword("foo123"));
    }

    @Test
    @DisplayName("Keyword category names are returned correctly")
    void testKeywordCategoryNames() {
        List<String> names = ArkTSColorizer.getKeywordCategoryNames();
        assertEquals(4, names.size());
        assertTrue(names.contains("control_flow"));
        assertTrue(names.contains("declaration"));
        assertTrue(names.contains("type"));
        assertTrue(names.contains("modifier"));
    }

    @Test
    @DisplayName("Segments reconstruct original line exactly")
    void testSegmentCoverage() {
        String line = "let x: number = 42; // comment";
        List<StyledSegment> segs = colorizer.colorizeLine(line);
        StringBuilder reconstructed = new StringBuilder();
        for (StyledSegment seg : segs) {
            reconstructed.append(seg.getText());
        }
        assertEquals(line, reconstructed.toString());
    }

    @Test
    @DisplayName("colorizeSource returns empty list for null/empty input")
    void testColorizeSourceEmpty() {
        assertTrue(colorizer.colorizeSource(null).isEmpty());
        assertTrue(colorizer.colorizeSource("").isEmpty());
    }

    @Test
    @DisplayName("colorizeSource handles multi-line code correctly")
    void testColorizeSourceMultiLine() {
        String source = "let x = 1;\nlet y = 2;";
        List<List<StyledSegment>> lines =
                colorizer.colorizeSource(source);
        assertEquals(2, lines.size());
        assertFalse(lines.get(0).isEmpty());
        assertFalse(lines.get(1).isEmpty());
    }

    @Test
    @DisplayName("StyledSegment toString includes type and text")
    void testStyledSegmentToString() {
        StyledSegment seg =
                new StyledSegment("let", TokenType.KEYWORD);
        String str = seg.toString();
        assertTrue(str.contains("KEYWORD"));
        assertTrue(str.contains("let"));
    }

    @Test
    @DisplayName("Static keyword is classified as KEYWORD (declaration)")
    void testStaticKeyword() {
        List<StyledSegment> segs =
                colorizer.colorizeLine("static method() {");
        assertSegmentType(segs, "static", TokenType.KEYWORD);
    }

    // --- Helper ---

    private void assertSegmentType(List<StyledSegment> segments,
            String expectedText, TokenType expectedType) {
        boolean found = false;
        for (StyledSegment seg : segments) {
            if (seg.getText().equals(expectedText)) {
                assertEquals(expectedType, seg.getTokenType(),
                        "Token '" + expectedText
                                + "' should be " + expectedType);
                found = true;
                break;
            }
        }
        assertTrue(found,
                "Expected to find segment with text '" + expectedText
                        + "' in " + segments);
    }
}
