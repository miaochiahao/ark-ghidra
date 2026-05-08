package com.arkghidra.loader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ghidra.util.Msg;

/**
 * Parses module.json / module.json5 content from a HarmonyOS HAP file
 * into a {@link HapMetadata} instance.
 *
 * <p>HarmonyOS HAP files use JSON5 for module metadata, which may
 * contain comments and trailing commas not permitted in standard
 * JSON. This parser preprocesses the input to strip those constructs
 * before parsing with a hand-written extractor that requires no
 * external JSON library.</p>
 */
public final class HapMetadataParser {

    private static final String OWNER =
            HapMetadataParser.class.getSimpleName();

    private HapMetadataParser() {
    }

    /**
     * Parses raw module.json bytes into a HapMetadata object.
     * Returns null if parsing fails.
     *
     * @param jsonBytes the raw bytes of the module.json file
     * @return parsed metadata, or null on failure
     */
    public static HapMetadata parse(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            String content = new String(jsonBytes, StandardCharsets.UTF_8);
            content = preprocessJson5(content);
            return parseModuleJson(content);
        } catch (Exception e) {
            Msg.warn(OWNER, "Failed to parse module.json: "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * Preprocesses JSON5 text by stripping single-line comments,
     * block comments, and trailing commas before closing braces
     * and brackets. String literals are preserved verbatim.
     *
     * @param text the raw JSON5 text
     * @return cleaned JSON text
     */
    static String preprocessJson5(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int i = 0;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);

            if (c == '"') {
                i = copyStringLiteral(text, i, sb);
                continue;
            }

            if (c == '\'') {
                i = copySingleQuotedLiteral(text, i, sb);
                continue;
            }

            if (c == '/' && i + 1 < len) {
                char next = text.charAt(i + 1);
                if (next == '/') {
                    i = skipLineComment(text, i);
                    continue;
                }
                if (next == '*') {
                    i = skipBlockComment(text, i);
                    continue;
                }
            }

            sb.append(c);
            i++;
        }

        String result = sb.toString();
        result = stripTrailingCommas(result);
        return result;
    }

    private static int copyStringLiteral(String text, int start,
            StringBuilder out) {
        out.append('"');
        int i = start + 1;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);
            out.append(c);
            i++;
            if (c == '\\' && i < len) {
                out.append(text.charAt(i));
                i++;
            } else if (c == '"') {
                break;
            }
        }
        return i;
    }

    private static int copySingleQuotedLiteral(String text, int start,
            StringBuilder out) {
        out.append('\'');
        int i = start + 1;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);
            out.append(c);
            i++;
            if (c == '\\' && i < len) {
                out.append(text.charAt(i));
                i++;
            } else if (c == '\'') {
                break;
            }
        }
        return i;
    }

    private static int skipLineComment(String text, int start) {
        int i = start + 2;
        int len = text.length();
        while (i < len && text.charAt(i) != '\n') {
            i++;
        }
        return i;
    }

    private static int skipBlockComment(String text, int start) {
        int i = start + 2;
        int len = text.length();
        while (i + 1 < len) {
            if (text.charAt(i) == '*'
                    && text.charAt(i + 1) == '/') {
                return i + 2;
            }
            i++;
        }
        return len;
    }

    private static String stripTrailingCommas(String json) {
        StringBuilder sb = new StringBuilder(json.length());
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',') {
                int j = i + 1;
                while (j < json.length()
                        && Character.isWhitespace(json.charAt(j))) {
                    j++;
                }
                if (j < json.length()
                        && (json.charAt(j) == '}'
                        || json.charAt(j) == ']')) {
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static HapMetadata parseModuleJson(String json) {
        String moduleObj = extractObject(json, "\"module\"");
        if (moduleObj == null) {
            return parseFlatModule(json);
        }

        return parseModuleFields(moduleObj);
    }

    private static HapMetadata parseFlatModule(String json) {
        String moduleName = extractStringValue(json, "\"name\"");
        String moduleType = extractStringValue(json, "\"type\"");
        String versionName = extractStringValue(json, "\"versionName\"");
        int versionCode = extractIntValue(json, "\"versionCode\"", 0);
        String packageName = extractStringValue(json, "\"packageName\"");
        String vendorName = extractStringValue(json, "\"vendorName\"");

        List<HapMetadata.AbilityInfo> abilities = new ArrayList<>();
        String abilitiesArray = extractArray(json, "\"abilities\"");
        if (abilitiesArray != null) {
            parseAbilities(abilitiesArray, abilities);
        }

        return new HapMetadata(moduleName, moduleType, versionName,
                versionCode, packageName, vendorName, abilities);
    }

    private static HapMetadata parseModuleFields(String moduleObj) {
        String moduleName = extractStringValue(moduleObj, "\"name\"");
        String moduleType = extractStringValue(moduleObj, "\"type\"");
        String versionName = extractStringValue(moduleObj, "\"versionName\"");
        int versionCode = extractIntValue(moduleObj, "\"versionCode\"", 0);
        String packageName = extractStringValue(moduleObj, "\"packageName\"");
        String vendorName = extractStringValue(moduleObj, "\"vendorName\"");

        List<HapMetadata.AbilityInfo> abilities = new ArrayList<>();
        String abilitiesArray = extractArray(moduleObj, "\"abilities\"");
        if (abilitiesArray != null) {
            parseAbilities(abilitiesArray, abilities);
        }

        return new HapMetadata(moduleName, moduleType, versionName,
                versionCode, packageName, vendorName, abilities);
    }

    private static void parseAbilities(String arrayJson,
            List<HapMetadata.AbilityInfo> out) {
        int i = 0;
        int len = arrayJson.length();
        while (i < len) {
            int objStart = arrayJson.indexOf('{', i);
            if (objStart < 0) {
                break;
            }
            int objEnd = findMatchingBrace(arrayJson, objStart);
            if (objEnd < 0) {
                break;
            }
            String obj = arrayJson.substring(objStart, objEnd + 1);
            String name = extractStringValue(obj, "\"name\"");
            String label = extractStringValue(obj, "\"label\"");
            String type = extractStringValue(obj, "\"type\"");
            out.add(new HapMetadata.AbilityInfo(name, label, type));
            i = objEnd + 1;
        }
    }

    static String extractObject(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx < 0) {
            return null;
        }
        int braceStart = json.indexOf('{', colonIdx);
        if (braceStart < 0) {
            return null;
        }
        int braceEnd = findMatchingBrace(json, braceStart);
        if (braceEnd < 0) {
            return null;
        }
        return json.substring(braceStart, braceEnd + 1);
    }

    static String extractArray(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx < 0) {
            return null;
        }
        int bracketStart = json.indexOf('[', colonIdx);
        if (bracketStart < 0) {
            return null;
        }
        int bracketEnd = findMatchingBracket(json, bracketStart);
        if (bracketEnd < 0) {
            return null;
        }
        return json.substring(bracketStart + 1, bracketEnd);
    }

    static String extractStringValue(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx < 0) {
            return null;
        }
        int quoteStart = json.indexOf('"', colonIdx);
        if (quoteStart < 0) {
            return null;
        }
        int quoteEnd = quoteStart + 1;
        int len = json.length();
        while (quoteEnd < len) {
            if (json.charAt(quoteEnd) == '\\') {
                quoteEnd += 2;
            } else if (json.charAt(quoteEnd) == '"') {
                break;
            } else {
                quoteEnd++;
            }
        }
        if (quoteEnd >= len) {
            return null;
        }
        String raw = json.substring(quoteStart + 1, quoteEnd);
        return unescapeString(raw);
    }

    static int extractIntValue(String json, String key, int defaultVal) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return defaultVal;
        }
        int colonIdx = json.indexOf(':', keyIdx + key.length());
        if (colonIdx < 0) {
            return defaultVal;
        }
        int i = colonIdx + 1;
        int len = json.length();
        while (i < len && (json.charAt(i) == ' '
                || json.charAt(i) == '\t'
                || json.charAt(i) == '\n'
                || json.charAt(i) == '\r')) {
            i++;
        }
        int start = i;
        while (i < len && json.charAt(i) >= '0'
                && json.charAt(i) <= '9') {
            i++;
        }
        if (start == i) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(json.substring(start, i));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static int findMatchingBrace(String json, int start) {
        return findMatchingDelim(json, start, '{', '}');
    }

    private static int findMatchingBracket(String json, int start) {
        return findMatchingDelim(json, start, '[', ']');
    }

    private static int findMatchingDelim(String json, int start,
            char open, char close) {
        int depth = 0;
        int len = json.length();
        int i = start;
        while (i < len) {
            char c = json.charAt(i);
            if (c == '"') {
                i++;
                while (i < len) {
                    if (json.charAt(i) == '\\' && i + 1 < len) {
                        i += 2;
                    } else if (json.charAt(i) == '"') {
                        i++;
                        break;
                    } else {
                        i++;
                    }
                }
                continue;
            }
            if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    private static String unescapeString(String s) {
        if (s.indexOf('\\') < 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case '/': sb.append('/'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    default: sb.append(c); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
