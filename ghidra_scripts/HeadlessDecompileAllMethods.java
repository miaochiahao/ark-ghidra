import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.listing.Program;

import com.arkghidra.decompile.ArkTSDecompiler;
import com.arkghidra.format.AbcClass;
import com.arkghidra.format.AbcFile;
import com.arkghidra.format.AbcMethod;

/**
 * Ghidra headless script: imports HAP, decompiles all ABC methods to ArkTS.
 *
 * <p>Run via {@code analyzeHeadless} with {@code -postScript HeadlessDecompileAllMethods}.
 * Script args (positional): output_dir, method_timeout_ms.</p>
 */
public class HeadlessDecompileAllMethods extends GhidraScript {

    private static final Pattern UNHANDLED_PATTERN =
            Pattern.compile("/\\*\\s*(?:unhandled|decompilation timed out)");

    @Override
    protected void run() throws Exception {
        String outputDir = getArg(0, "build/ghidra_test_output");
        long timeoutMs = Long.parseLong(getArg(1, "30000"));

        String programName = currentProgram.getDomainFile().getName();
        println("=== Headless Decompile: " + programName + " ===");

        List<byte[]> abcBlocks = readAllAbcBytes(currentProgram);
        if (abcBlocks.isEmpty()) {
            println("WARNING: No ABC memory blocks found");
            printMetrics(0, 0, 0, 0, 0, 0, 0, 0);
            return;
        }

        ArkTSDecompiler decompiler = new ArkTSDecompiler();
        decompiler.setMethodTimeoutMs(timeoutMs);

        int totalClasses = 0;
        int totalMethods = 0;
        int decompileSuccess = 0;
        int decompileFail = 0;
        int decompileTimeout = 0;
        int unhandledOpcodes = 0;
        long totalOutputBytes = 0;
        int totalOutputLines = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < abcBlocks.size(); i++) {
            byte[] abcData = abcBlocks.get(i);
            String blockTag = "abc_" + i;

            println("  Parsing ABC block " + i + " (" + abcData.length / 1024 + " KB)...");

            AbcFile abc;
            try {
                abc = AbcFile.parse(abcData);
            } catch (Exception e) {
                println("  ERROR parsing ABC block " + i + ": " + e.getMessage());
                decompileFail++;
                continue;
            }

            int classCount = abc.getClasses().size();
            totalClasses += classCount;
            println("  Classes: " + classCount);

            String result;
            try {
                result = decompiler.decompileFile(abc);
            } catch (Exception e) {
                println("  ERROR decompiling ABC block " + i + ": " + e.getMessage());
                decompileFail++;
                continue;
            }

            if (result == null || result.isEmpty()) {
                println("  WARNING: Empty decompilation output for block " + i);
                decompileFail++;
                continue;
            }

            int methodsInBlock = 0;
            for (AbcClass cls : abc.getClasses()) {
                for (AbcMethod m : cls.getMethods()) {
                    if (m.getCodeOff() != 0) {
                        methodsInBlock++;
                    }
                }
            }
            totalMethods += methodsInBlock;

            int lines = result.split("\n").length;
            totalOutputLines += lines;
            totalOutputBytes += result.length();

            Matcher m = UNHANDLED_PATTERN.matcher(result);
            while (m.find()) {
                unhandledOpcodes++;
            }

            long timedOutCount = countTimedOut(result);
            decompileTimeout += timedOutCount;
            decompileSuccess += methodsInBlock - timedOutCount;

            Path outputPath = Path.of(outputDir,
                    sanitizeFileName(programName) + "__" + blockTag + ".ts");
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, result);
            println("  Output: " + lines + " lines, "
                    + result.length() / 1024 + " KB -> " + outputPath.getFileName());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        println("  Elapsed: " + elapsed + " ms");

        printMetrics(abcBlocks.size(), totalClasses, totalMethods,
                decompileSuccess, decompileFail, decompileTimeout,
                unhandledOpcodes, elapsed);
    }

    private List<byte[]> readAllAbcBytes(Program program) {
        List<byte[]> result = new ArrayList<>();
        Memory memory = program.getMemory();
        for (MemoryBlock block : memory.getBlocks()) {
            if (!block.getName().startsWith("abc")) {
                continue;
            }
            long size = block.getSize();
            if (size <= 0 || size > Integer.MAX_VALUE) {
                continue;
            }
            byte[] bytes = new byte[(int) size];
            try {
                block.getBytes(block.getStart(), bytes);
                result.add(bytes);
            } catch (Exception e) {
                println("  ERROR reading block '" + block.getName() + "': " + e.getMessage());
            }
        }
        return result;
    }

    private int countTimedOut(String output) {
        int count = 0;
        int idx = 0;
        String marker = "decompilation timed out";
        while ((idx = output.indexOf(marker, idx)) != -1) {
            count++;
            idx += marker.length();
        }
        return count;
    }

    private void printMetrics(int abcFiles, int classes, int methods,
            int success, int fail, int timeouts, int unhandled, long elapsed) {
        println("");
        println("===DECOMPILE_METRICS===");
        println("abc_files=" + abcFiles);
        println("classes=" + classes);
        println("total_methods=" + methods);
        println("success=" + success);
        println("fail=" + fail);
        println("timeouts=" + timeouts);
        println("unhandled_opcodes=" + unhandled);
        println("elapsed_ms=" + elapsed);
        println("===END_METRICS===");
    }

    private String getArg(int index, String defaultVal) {
        String[] args = getScriptArgs();
        if (args != null && index < args.length) {
            return args[index];
        }
        return defaultVal;
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
