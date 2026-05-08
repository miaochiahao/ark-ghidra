package com.arkghidra;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the SLEIGH language specification files for the Ark Bytecode
 * processor module. Checks file existence, basic syntax, instruction count,
 * and duplicate opcode detection.
 */
public class SleighValidationTest {

    private static final Path LANG_DIR = Paths.get("data/languages");

    private static final String SINC_FILE = "ArkBytecode.sinc";
    private static final String SLASPEC_FILE = "ArkBytecode.slaspec";
    private static final String LDEFS_FILE = "ArkBytecode.ldefs";
    private static final String CSPEC_FILE = "ArkBytecode.cspec";
    private static final String PSPEC_FILE = "ArkBytecode.pspec";
    private static final String OPINION_FILE = "ArkBytecode.opinion";

    @Test
    public void testSincFile_existsAndNonEmpty() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        assertTrue(Files.exists(sincPath), SINC_FILE + " must exist");
        assertTrue(Files.size(sincPath) > 0, SINC_FILE + " must not be empty");
    }

    @Test
    public void testSlaspecFile_existsAndNonEmpty() throws IOException {
        Path slaspecPath = LANG_DIR.resolve(SLASPEC_FILE);
        assertTrue(Files.exists(slaspecPath), SLASPEC_FILE + " must exist");
        assertTrue(Files.size(slaspecPath) > 0, SLASPEC_FILE + " must not be empty");
    }

    @Test
    public void testLdefsFile_existsAndNonEmpty() throws IOException {
        Path ldefsPath = LANG_DIR.resolve(LDEFS_FILE);
        assertTrue(Files.exists(ldefsPath), LDEFS_FILE + " must exist");
        assertTrue(Files.size(ldefsPath) > 0, LDEFS_FILE + " must not be empty");
    }

    @Test
    public void testCspecFile_existsAndNonEmpty() throws IOException {
        Path cspecPath = LANG_DIR.resolve(CSPEC_FILE);
        assertTrue(Files.exists(cspecPath), CSPEC_FILE + " must exist");
        assertTrue(Files.size(cspecPath) > 0, CSPEC_FILE + " must not be empty");
    }

    @Test
    public void testPspecFile_existsAndNonEmpty() throws IOException {
        Path pspecPath = LANG_DIR.resolve(PSPEC_FILE);
        assertTrue(Files.exists(pspecPath), PSPEC_FILE + " must exist");
        assertTrue(Files.size(pspecPath) > 0, PSPEC_FILE + " must not be empty");
    }

    @Test
    public void testOpinionFile_existsAndNonEmpty() throws IOException {
        Path opinionPath = LANG_DIR.resolve(OPINION_FILE);
        assertTrue(Files.exists(opinionPath), OPINION_FILE + " must exist");
        assertTrue(Files.size(opinionPath) > 0, OPINION_FILE + " must not be empty");
    }

    @Test
    public void testSincFile_hasBalancedBraces() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        String content = Files.readString(sincPath);

        int openBraces = 0;
        int closeBraces = 0;
        for (char c : content.toCharArray()) {
            if (c == '{') {
                openBraces++;
            } else if (c == '}') {
                closeBraces++;
            }
        }
        assertEquals(openBraces, closeBraces,
                "Braces must be balanced in " + SINC_FILE);
    }

    @Test
    public void testSincFile_instructionDefinitionsHaveBraces() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);

        List<String> badLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("define ") || line.startsWith("@")) {
                continue;
            }
            if (line.startsWith(":")) {
                boolean hasOpenBrace = line.contains("{");
                boolean hasCloseBrace = line.contains("}");
                if (!hasOpenBrace || !hasCloseBrace) {
                    badLines.add("Line " + (i + 1) + ": " + line);
                }
            }
        }
        assertTrue(badLines.isEmpty(),
                "Instruction definitions missing braces:\n"
                        + String.join("\n", badLines));
    }

    @Test
    public void testSincFile_instructionCountAtLeast200() throws IOException {
        List<String> instructions = parseInstructions();
        assertTrue(instructions.size() >= 200,
                "Expected at least 200 instruction definitions, found "
                        + instructions.size());
    }

    @Test
    public void testSincFile_noDuplicateOpcodePatterns() throws IOException {
        List<ParsedInstruction> instructions = parseInstructionsDetailed();
        Map<String, List<Integer>> patternToLines = new HashMap<>();

        for (ParsedInstruction instr : instructions) {
            String key = instr.patternKey;
            if (!patternToLines.containsKey(key)) {
                patternToLines.put(key, new ArrayList<>());
            }
            patternToLines.get(key).add(instr.lineNumber);
        }

        List<String> duplicates = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : patternToLines.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.add("Pattern '" + entry.getKey() + "' defined at lines "
                        + entry.getValue());
            }
        }
        assertTrue(duplicates.isEmpty(),
                "Duplicate opcode patterns found:\n" + String.join("\n", duplicates));
    }

    @Test
    public void testSincFile_noTernaryOperators() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);

        List<String> ternaryLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.contains("?") && line.contains(":")
                    && line.startsWith(":")) {
                if (line.contains("? 1 :") || line.contains("? 0 :")) {
                    ternaryLines.add("Line " + (i + 1) + ": " + line);
                }
            }
        }
        assertTrue(ternaryLines.isEmpty(),
                "Ternary operators not allowed in SLEIGH pcode:\n"
                        + String.join("\n", ternaryLines));
    }

    @Test
    public void testSincFile_noInvalidShiftOperators() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);

        List<String> badShifts = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.contains(">>>")) {
                badShifts.add("Line " + (i + 1) + ": " + line);
            }
        }
        assertTrue(badShifts.isEmpty(),
                "Unsigned right shift '>>>' not valid in SLEIGH (use '>>'):\n"
                        + String.join("\n", badShifts));
    }

    @Test
    public void testSincFile_hasRequiredTokenDefinitions() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        String content = Files.readString(sincPath);

        assertTrue(content.contains("define token inst_byte"),
                "Must define inst_byte token for opcode");
        assertTrue(content.contains("define token byte1"),
                "Must define byte1 token for first operand byte");
        assertTrue(content.contains("define token bytes12"),
                "Must define bytes12 token for 16-bit immediate operands");
        assertTrue(content.contains("define token bytes1234"),
                "Must define bytes1234 token for 32-bit immediate operands");
    }

    @Test
    public void testSincFile_hasRequiredRegisterDefinitions() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        String content = Files.readString(sincPath);

        assertTrue(content.contains("define register offset=0x00"),
                "Must define core registers");
        assertTrue(content.contains("define register offset=0x1000"),
                "Must define virtual registers");
        assertTrue(content.contains(" sp ") && content.contains(" fp ")
                && content.contains(" acc "),
                "Must define sp, fp, acc core registers");
    }

    @Test
    public void testSincFile_hasAttachVariablesForAllRegisterPositions()
            throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        String content = Files.readString(sincPath);

        assertTrue(content.contains("attach variables reg1"),
                "Must attach variables for reg1 (byte position 1)");
        assertTrue(content.contains("attach variables reg2"),
                "Must attach variables for reg2 (byte position 2)");
        assertTrue(content.contains("attach variables reg3"),
                "Must attach variables for reg3 (byte position 3)");
        assertTrue(content.contains("attach variables reg4"),
                "Must attach variables for reg4 (byte position 4)");
    }

    @Test
    public void testSincFile_wideInstructionsUseCorrectPrefix() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.contains("wide_prefix=0xFD") && line.startsWith(":")) {
                assertTrue(line.contains("wide_opcode="),
                        "Wide instruction at line " + (i + 1)
                                + " must specify wide_opcode");
            }
        }
    }

    @Test
    public void testSincFile_noReservedWordConflicts() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        String content = Files.readString(sincPath);

        assertFalse(content.contains("define token instruction("),
                "Token name 'instruction' is a SLEIGH reserved word; use 'inst_byte'");
    }

    private List<String> parseInstructions() throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);
        List<String> instructions = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith(":") && trimmed.length() > 1) {
                int spaceIdx = trimmed.indexOf(' ');
                if (spaceIdx > 0) {
                    instructions.add(trimmed.substring(1, spaceIdx));
                } else {
                    int isIdx = trimmed.indexOf(" is ");
                    if (isIdx > 0) {
                        instructions.add(trimmed.substring(1, isIdx).trim());
                    }
                }
            }
        }
        return instructions;
    }

    private List<ParsedInstruction> parseInstructionsDetailed()
            throws IOException {
        Path sincPath = LANG_DIR.resolve(SINC_FILE);
        List<String> lines = Files.readAllLines(sincPath);
        List<ParsedInstruction> instructions = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (trimmed.startsWith(":") && trimmed.contains(" is ")) {
                int isIdx = trimmed.indexOf(" is ");
                String name = trimmed.substring(1, isIdx).trim();
                String rest = trimmed.substring(isIdx + 4).trim();

                int braceIdx = rest.indexOf('{');
                String pattern = braceIdx > 0
                        ? rest.substring(0, braceIdx).trim()
                        : rest;

                instructions.add(new ParsedInstruction(name, pattern, i + 1));
            }
        }
        return instructions;
    }

    private static class ParsedInstruction {
        final String name;
        final String patternKey;
        final int lineNumber;

        ParsedInstruction(String name, String pattern, int lineNumber) {
            this.name = name;
            this.patternKey = pattern.replaceAll("\\s+", " ").trim();
            this.lineNumber = lineNumber;
        }
    }
}
