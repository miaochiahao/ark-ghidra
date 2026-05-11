package com.arkghidra.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for finding all occurrences of a word in source text.
 *
 * <p>Matches whole-word occurrences only (bounded by non-identifier characters).
 * Has no Ghidra or Swing dependencies and can be tested in isolation.</p>
 */
public class SymbolHighlighter {

    /**
     * Extracts the identifier word at the given character offset within text.
     *
     * <p>Returns an empty string if the character at {@code offset} is not
     * part of an identifier (letter, digit, underscore, or {@code $}).</p>
     *
     * @param text   the full source text
     * @param offset character offset into text (0-based)
     * @return the word at the offset, or empty string if none
     */
    public String extractWordAt(String text, int offset) {
        if (text == null || offset < 0 || offset >= text.length()) {
            return "";
        }
        if (!isIdentifierChar(text.charAt(offset))) {
            return "";
        }
        int start = offset;
        while (start > 0 && isIdentifierChar(text.charAt(start - 1))) {
            start--;
        }
        int end = offset;
        while (end < text.length() - 1
                && isIdentifierChar(text.charAt(end + 1))) {
            end++;
        }
        return text.substring(start, end + 1);
    }

    /**
     * Finds all whole-word occurrences of {@code word} in {@code text}.
     *
     * @param text the source text to search
     * @param word the word to find (must be non-empty)
     * @return list of start offsets (0-based), never null
     */
    public List<Integer> findAllOccurrences(String text, String word) {
        if (text == null || word == null || word.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> positions = new ArrayList<>();
        int fromIndex = 0;
        while (fromIndex <= text.length() - word.length()) {
            int idx = text.indexOf(word, fromIndex);
            if (idx < 0) {
                break;
            }
            boolean startOk = idx == 0
                    || !isIdentifierChar(text.charAt(idx - 1));
            boolean endOk = idx + word.length() >= text.length()
                    || !isIdentifierChar(text.charAt(idx + word.length()));
            if (startOk && endOk) {
                positions.add(idx);
            }
            fromIndex = idx + 1;
        }
        return positions;
    }

    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }
}
