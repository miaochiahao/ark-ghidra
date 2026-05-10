package com.arkghidra.loader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.format.AbcFile;

/**
 * Real-world HAP decompilation tests using actual HarmonyOS applications.
 *
 * <p>Loads real .hap files from ~/Downloads/, extracts embedded .abc files,
 * parses them with AbcFile, and decompiles to ArkTS source code.
 * Output is saved to data/test_hap/ for inspection.</p>
 */
@DisplayName("Real HAP decompilation")
class RealHapDecompilationTest {

    private static final String DOWNLOADS_DIR =
            System.getProperty("user.home") + "/Downloads";
    private static final String OUTPUT_DIR =
            System.getProperty("user.dir") + "/data/test_hap";

    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
        decompiler.setMethodTimeoutMs(10_000);
    }

    @Test
    @DisplayName("decompiles entry-default-unsigned.hap (8.6MB)")
    void testEntryDefaultUnsigned() throws IOException {
        decompileHap("entry-default-unsigned.hap");
    }

    @Test
    @DisplayName("decompiles Melotopia-1.10.2.hap (16MB)")
    void testMelotopia() throws IOException {
        decompileHap("Melotopia-1.10.2.hap");
    }

    @Test
    @DisplayName("decompiles Kazumi HAP (76MB)")
    void testKazumi() throws IOException {
        decompileHap("Kazumi_ohos_2.0.8_unsigned.hap");
    }

    @Test
    @DisplayName("decompiles PiliPlus HAP (78MB)")
    void testPiliPlus() throws IOException {
        decompileHap("PiliPlus_ohos_1.1.5.4-ohos-2_unsigned.hap");
    }

    @Test
    @DisplayName("decompiles entry-default-unsigned-2.hap (156MB)")
    void testEntryDefaultUnsigned2() throws IOException {
        decompileHap("entry-default-unsigned-2.hap");
    }

    private void decompileHap(String fileName) throws IOException {
        File hapFile = new File(DOWNLOADS_DIR, fileName);
        assumeTrue(hapFile.exists(), "HAP file not found: " + fileName);

        System.out.println("\n>>> Decompiling: " + fileName);
        byte[] hapBytes = Files.readAllBytes(hapFile.toPath());
        System.out.println("    HAP size: " + hapBytes.length / 1024 + " KB");

        List<HapLoader.AbcEntry> entries =
                HapLoader.extractAbcEntries(hapBytes);
        assertFalse(entries.isEmpty(),
                "HAP must contain at least one .abc entry: " + fileName);
        System.out.println("    ABC entries: " + entries.size());

        for (HapLoader.AbcEntry entry : entries) {
            System.out.println("\n    Parsing " + entry.path + " ("
                    + entry.bytes.length / 1024 + " KB)...");

            AbcFile abc = AbcFile.parse(entry.bytes);
            assertNotNull(abc, "AbcFile.parse should succeed");
            int classCount = abc.getClasses().size();
            System.out.println("    Classes: " + classCount);
            assertTrue(classCount > 0, "ABC should have at least one class");

            long start = System.currentTimeMillis();
            String result = decompiler.decompileFile(abc);
            long elapsed = System.currentTimeMillis() - start;

            assertNotNull(result, "Decompilation should produce output");
            assertFalse(result.isEmpty(),
                    "Decompilation output should not be empty");

            int lines = result.split("\n").length;
            System.out.println("    Decompilation: " + elapsed + "ms, "
                    + lines + " lines, " + result.length() + " chars");

            // Save output to file for inspection
            String baseName = fileName.replace(".hap", "")
                    .replace("_unsigned", "")
                    .replace("-unsigned", "");
            String abcName = entry.path.replace("/", "_")
                    .replace(".abc", "");
            Path outputPath = Path.of(OUTPUT_DIR,
                    baseName + "_" + abcName + ".ts");
            Files.createDirectories(outputPath.getParent());
            byte[] bytes = result.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8);
            Files.write(outputPath, bytes);
            System.out.println("    Saved to: " + outputPath);

            // Print first 3000 chars as preview
            String preview = result.substring(0,
                    Math.min(3000, result.length()));
            System.out.println("--- Preview ---");
            System.out.println(preview);
            System.out.println("--- End Preview ---\n");
        }
    }
}
