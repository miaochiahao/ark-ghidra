package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcFile;

/**
 * Tests for per-method decompilation timeout support.
 *
 * <p>Ensures that complex methods can be aborted after a configurable
 * timeout, returning a comment instead of blocking the full file
 * decompilation.</p>
 */
class MethodTimeoutTest {

    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
    }

    // =====================================================================
    // Default configuration
    // =====================================================================

    @Nested
    class DefaultConfiguration {

        @Test
        void testDefaultTimeoutIs5000ms() {
            assertEquals(5000L, decompiler.getMethodTimeoutMs());
        }

        @Test
        void testSetterChangesTimeout() {
            decompiler.setMethodTimeoutMs(1000L);
            assertEquals(1000L, decompiler.getMethodTimeoutMs());
        }

        @Test
        void testDisableTimeoutWithZero() {
            decompiler.setMethodTimeoutMs(0L);
            assertEquals(0L, decompiler.getMethodTimeoutMs());
        }
    }

    // =====================================================================
    // Timeout triggers comment output
    // =====================================================================

    @Nested
    class TimeoutTriggersComment {

        @Test
        void testDecompileMethod_withZeroTimeout_stillProducesOutput() {
            // Setting timeout to 0 disables it, so normal decompilation
            // should proceed
            decompiler.setMethodTimeoutMs(0L);

            List<ArkInstruction> insns =
                    ArkInstructionTestHelper.makeReturnUndefined();
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.contains("timed out"));
        }

        @Test
        void testDecompileMethod_withTimeoutDisabled_producesNormalOutput() {
            decompiler.setMethodTimeoutMs(0L);

            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.contains("timed out"));
        }

        @Test
        void testDecompileInstructions_withGenerousTimeout_succeeds() {
            // 30 seconds should be more than enough for any test fixture
            decompiler.setMethodTimeoutMs(30000L);

            List<ArkInstruction> insns =
                    ArkInstructionTestHelper.makeSimpleReturn();
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.contains("timed out"));
        }
    }

    // =====================================================================
    // Full file decompilation with timeout
    // =====================================================================

    @Nested
    class FullFileDecompilation {

        @Test
        void testDecompileFile_withTimeout_completesSuccessfully() {
            decompiler.setMethodTimeoutMs(30000L);

            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testDecompileFile_withZeroTimeout_completesSuccessfully() {
            decompiler.setMethodTimeoutMs(0L);

            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // Timeout produces comment body (extreme timeout)
    // =====================================================================

    @Nested
    class ExtremeTimeout {

        @Test
        void testDecompileInstructions_with1msTimeout_eitherSucceedsOrTimesOut() {
            // 1ms is extremely aggressive -- the decompiler may or may not
            // complete. Either outcome is acceptable.
            decompiler.setMethodTimeoutMs(1L);

            List<ArkInstruction> insns =
                    ArkInstructionTestHelper.makeSimpleReturn();
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            // Must contain either normal output or a timeout comment
            assertTrue(
                    result.contains("return")
                            || result.contains("timed out")
                            || result.contains("fallback"),
                    "Expected either decompiled output or timeout comment, "
                            + "got: " + result);
        }
    }

    // =====================================================================
    // Helper for building test instructions
    // =====================================================================

    private static class ArkInstructionTestHelper {

        private static final ArkDisassembler DISASM = new ArkDisassembler();

        static List<ArkInstruction> makeReturnUndefined() {
            byte[] code = new byte[] {
                (byte) 0x65 // RETURNUNDEFINED
            };
            return DISASM.disassemble(code, 0, code.length);
        }

        static List<ArkInstruction> makeSimpleReturn() {
            byte[] code = new byte[] {
                (byte) 0x60, 0x00, // LDA v0
                (byte) 0x64       // RETURN
            };
            return DISASM.disassemble(code, 0, code.length);
        }
    }
}
