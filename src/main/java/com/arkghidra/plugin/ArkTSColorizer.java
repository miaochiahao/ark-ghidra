package com.arkghidra.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax colorizer for ArkTS source code.
 *
 * <p>Tokenizes a line of ArkTS source and produces a list of
 * {@link StyledSegment} instances, each with a text fragment and an
 * associated {@link TokenType}. Callers can map token types to colors
 * or font styles for display.</p>
 *
 * <p>This class has no Ghidra runtime dependencies and can be tested
 * in isolation.</p>
 */
public class ArkTSColorizer {

    /**
     * Token types representing different ArkTS syntax categories.
     * Each type maps to a distinct display style (color, font weight, etc.).
     */
    public enum TokenType {
        /** Plain text with no special styling. */
        PLAIN,
        /** Keywords: let, const, if, else, return, class, etc. */
        KEYWORD,
        /** Built-in types: number, string, boolean, void, etc. */
        TYPE,
        /** Decorator annotations: @Component, @State, @Entry, etc. */
        DECORATOR,
        /** String literals in single or double quotes. */
        STRING,
        /** Numeric literals: integer, floating-point, hex. */
        NUMBER,
        /** Line comments and block comments. */
        COMMENT,
        /** Access modifiers: public, private, protected, readonly. */
        MODIFIER
    }

    /**
     * A text fragment paired with its syntax token type.
     */
    public static class StyledSegment {
        private final String text;
        private final TokenType tokenType;

        /**
         * Constructs a styled segment.
         *
         * @param text the text fragment
         * @param tokenType the syntax token type
         */
        public StyledSegment(String text, TokenType tokenType) {
            this.text = text;
            this.tokenType = tokenType;
        }

        public String getText() {
            return text;
        }

        public TokenType getTokenType() {
            return tokenType;
        }

        @Override
        public String toString() {
            return tokenType + ":" + text;
        }
    }

    // --- Keyword sets ---

    /** Control flow keywords. */
    public static final Set<String> CONTROL_FLOW_KEYWORDS = Set.of(
            "if", "else", "for", "while", "do", "switch", "case",
            "break", "continue", "return", "throw", "try", "catch",
            "finally", "default"
    );

    /** Declaration keywords. */
    public static final Set<String> DECLARATION_KEYWORDS = Set.of(
            "let", "const", "function", "class", "constructor", "new",
            "extends", "implements", "import", "export", "from",
            "as", "namespace", "enum", "interface", "struct",
            "async", "await", "yield", "typeof", "instanceof",
            "in", "of", "get", "set", "static"
    );

    /** Built-in type names and primitive types. */
    public static final Set<String> TYPE_KEYWORDS = Set.of(
            "number", "string", "boolean", "void", "null", "undefined",
            "true", "false", "any", "unknown", "never", "object",
            "Object", "Array", "Map", "Set", "Promise", "Date",
            "RegExp", "Error", "symbol", "bigint"
    );

    /** Access modifier keywords. */
    public static final Set<String> MODIFIER_KEYWORDS = Set.of(
            "public", "private", "protected", "readonly", "abstract",
            "override", "declare", "is", "super", "this"
    );

    /** All keywords combined (for lookup). */
    public static final Set<String> ALL_KEYWORDS;
    static {
        Set<String> all = new java.util.HashSet<>();
        all.addAll(CONTROL_FLOW_KEYWORDS);
        all.addAll(DECLARATION_KEYWORDS);
        all.addAll(TYPE_KEYWORDS);
        all.addAll(MODIFIER_KEYWORDS);
        ALL_KEYWORDS = Collections.unmodifiableSet(all);
    }

    // --- Regex patterns for tokenization ---

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "//.*"
                    + "|/\\*.*?\\*/"
                    + "|@\\w+"
                    + "|\"(?:[^\"\\\\]|\\\\.)*\""
                    + "|'(?:[^'\\\\]|\\\\.)*'"
                    + "|`(?:[^`\\\\]|\\\\.)*`"
                    + "|\\b(0[xX][0-9a-fA-F_]+"
                    + "|[0-9][0-9_]*(?:\\.[0-9_]+)?"
                    + "(?:[eE][+-]?[0-9_]+)?)\\b"
                    + "|\\b[A-Za-z_$][\\w$]*\\b"
                    + "|."
    );

    /**
     * Colorizes a single line of ArkTS source code into styled segments.
     *
     * <p>The returned list contains consecutive, non-overlapping segments
     * that cover the entire input line.</p>
     *
     * @param line the source line to colorize
     * @return the list of styled segments, never null
     */
    public List<StyledSegment> colorizeLine(String line) {
        if (line == null || line.isEmpty()) {
            return Collections.emptyList();
        }

        List<StyledSegment> segments = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(line);

        while (matcher.find()) {
            String token = matcher.group();
            TokenType type = classifyToken(token);
            segments.add(new StyledSegment(token, type));
        }

        return segments;
    }

    /**
     * Colorizes multiple lines of ArkTS source code.
     *
     * @param source the multi-line source code
     * @return a list of styled segments per line
     */
    public List<List<StyledSegment>> colorizeSource(String source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        String[] lines = source.split("\n", -1);
        List<List<StyledSegment>> result = new ArrayList<>(lines.length);
        boolean inBlockComment = false;

        for (String line : lines) {
            if (inBlockComment) {
                int endIdx = line.indexOf("*/");
                if (endIdx >= 0) {
                    String commentPart = line.substring(0, endIdx + 2);
                    String rest = line.substring(endIdx + 2);
                    List<StyledSegment> segs = new ArrayList<>();
                    segs.add(new StyledSegment(commentPart, TokenType.COMMENT));
                    segs.addAll(colorizeLine(rest));
                    result.add(segs);
                    inBlockComment = false;
                } else {
                    result.add(List.of(
                            new StyledSegment(line, TokenType.COMMENT)));
                }
            } else {
                int startIdx = line.indexOf("/*");
                if (startIdx >= 0) {
                    int endIdx = line.indexOf("*/", startIdx + 2);
                    if (endIdx >= 0) {
                        String before = line.substring(0, startIdx);
                        String comment = line.substring(
                                startIdx, endIdx + 2);
                        String after = line.substring(endIdx + 2);
                        List<StyledSegment> segs = new ArrayList<>();
                        segs.addAll(colorizeLine(before));
                        segs.add(new StyledSegment(
                                comment, TokenType.COMMENT));
                        segs.addAll(colorizeLine(after));
                        result.add(segs);
                    } else {
                        String before = line.substring(0, startIdx);
                        String comment = line.substring(startIdx);
                        List<StyledSegment> segs = new ArrayList<>();
                        segs.addAll(colorizeLine(before));
                        segs.add(new StyledSegment(
                                comment, TokenType.COMMENT));
                        result.add(segs);
                        inBlockComment = true;
                    }
                } else {
                    result.add(colorizeLine(line));
                }
            }
        }

        return result;
    }

    /**
     * Classifies a single token string into a token type.
     *
     * @param token the token text
     * @return the token type
     */
    TokenType classifyToken(String token) {
        // Line comments
        if (token.startsWith("//")) {
            return TokenType.COMMENT;
        }
        // Block comments (inline)
        if (token.startsWith("/*") && token.endsWith("*/")
                && token.length() >= 4) {
            return TokenType.COMMENT;
        }
        // Decorators
        if (token.startsWith("@") && token.length() > 1) {
            return TokenType.DECORATOR;
        }
        // String literals
        if ((token.startsWith("\"") && token.endsWith("\""))
                || (token.startsWith("'") && token.endsWith("'"))
                || (token.startsWith("`") && token.endsWith("`"))) {
            return TokenType.STRING;
        }
        // Numeric literals
        if (isNumericLiteral(token)) {
            return TokenType.NUMBER;
        }
        // Identifiers - check keyword categories
        if (Character.isJavaIdentifierStart(token.charAt(0))) {
            if (CONTROL_FLOW_KEYWORDS.contains(token)
                    || DECLARATION_KEYWORDS.contains(token)) {
                return TokenType.KEYWORD;
            }
            if (TYPE_KEYWORDS.contains(token)) {
                return TokenType.TYPE;
            }
            if (MODIFIER_KEYWORDS.contains(token)) {
                return TokenType.MODIFIER;
            }
        }
        return TokenType.PLAIN;
    }

    private boolean isNumericLiteral(String token) {
        if (token.isEmpty()) {
            return false;
        }
        char first = token.charAt(0);
        if (!Character.isDigit(first)) {
            return false;
        }
        try {
            if (token.startsWith("0x") || token.startsWith("0X")) {
                Long.parseLong(
                        token.substring(2).replace("_", ""), 16);
                return true;
            }
            String cleaned = token.replace("_", "");
            if (cleaned.contains(".") || cleaned.contains("e")
                    || cleaned.contains("E")) {
                Double.parseDouble(cleaned);
                return true;
            }
            Long.parseLong(cleaned);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns all keyword categories for iteration or display.
     *
     * @return the list of all keyword set names
     */
    public static List<String> getKeywordCategoryNames() {
        return List.of(
                "control_flow",
                "declaration",
                "type",
                "modifier"
        );
    }

    /**
     * Checks whether the given word is a keyword in any category.
     *
     * @param word the word to check
     * @return true if the word is a known ArkTS keyword
     */
    public static boolean isKeyword(String word) {
        return ALL_KEYWORDS.contains(word);
    }

    /**
     * Checks whether the given word is a control flow keyword.
     *
     * @param word the word to check
     * @return true if the word is a control flow keyword
     */
    public static boolean isControlFlowKeyword(String word) {
        return CONTROL_FLOW_KEYWORDS.contains(word);
    }

    /**
     * Checks whether the given word is a declaration keyword.
     *
     * @param word the word to check
     * @return true if the word is a declaration keyword
     */
    public static boolean isDeclarationKeyword(String word) {
        return DECLARATION_KEYWORDS.contains(word);
    }

    /**
     * Checks whether the given word is a type keyword.
     *
     * @param word the word to check
     * @return true if the word is a type keyword
     */
    public static boolean isTypeKeyword(String word) {
        return TYPE_KEYWORDS.contains(word);
    }

    /**
     * Checks whether the given word is a modifier keyword.
     *
     * @param word the word to check
     * @return true if the word is a modifier keyword
     */
    public static boolean isModifierKeyword(String word) {
        return MODIFIER_KEYWORDS.contains(word);
    }
}
