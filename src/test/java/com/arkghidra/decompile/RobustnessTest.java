package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.AbcTestFixture;
import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcFile;

/**
 * Robustness tests for the ArkTS decompiler.
 *
 * <p>Tests that the decompiler handles edge cases and malformed bytecode
 * gracefully, producing output rather than crashing.</p>
 */
class RobustnessTest {

    private ArkTSDecompiler decompiler;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
    }

    // =====================================================================
    // Truncated / zero-length bytecode
    // =====================================================================

    @Nested
    class TruncatedBytecodeTests {

        @Test
        void testDecompile_emptyInstructions_producesOutput() {
            List<ArkInstruction> empty =
                    Collections.emptyList();
            String result =
                    decompiler.decompileInstructions(empty);
            assertNotNull(result);
        }

        @Test
        void testDecompile_singleNop_producesOutput() {
            byte[] code = new byte[] {0x00}; // NOP
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
        }

        @Test
        void testDecompile_singleReturnUndefined_producesOutput() {
            byte[] code = new byte[] {0x65}; // RETURNUNDEFINED
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertTrue(result.contains("return"));
        }

        @Test
        void testDecompile_truncatedLdai_disassemblyThrows() {
            // LDAI is 5 bytes (opcode + 32-bit imm), but only 2 bytes
            // Disassembler throws — this is expected behavior
            byte[] code = new byte[] {0x62, 0x00};
            ArkDisassembler disasm = new ArkDisassembler();
            try {
                List<ArkInstruction> insns =
                        disasm.disassemble(code, 0, code.length);
                // If it doesn't throw, still verify decompiler handles it
                String result =
                        decompiler.decompileInstructions(insns);
                assertNotNull(result);
            } catch (Exception e) {
                // Disassembler correctly rejects truncated instruction
                assertTrue(e.getMessage().contains("ldai")
                        || e.getMessage().contains("Not enough"));
            }
        }

        @Test
        void testDecompile_truncatedAfterOpcode_disassemblyThrows() {
            // STA expects a register operand but we only have the opcode
            byte[] code = new byte[] {0x61};
            ArkDisassembler disasm = new ArkDisassembler();
            try {
                List<ArkInstruction> insns =
                        disasm.disassemble(code, 0, code.length);
                String result =
                        decompiler.decompileInstructions(insns);
                assertNotNull(result);
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("sta")
                        || e.getMessage().contains("Not enough"));
            }
        }
    }

    // =====================================================================
    // Invalid jump targets
    // =====================================================================

    @Nested
    class InvalidJumpTests {

        @Test
        void testDecompile_jumpBeyondCode_producesOutput() {
            // JMP +100 (way beyond the code)
            byte[] code = new byte[] {0x4D, 0x64, 0x64};
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
        }

        @Test
        void testDecompile_negativeJumpBeyondStart_producesOutput() {
            // JMP -100 (way before the code start)
            byte[] code = new byte[] {
                (byte) 0x4D, (byte) 0x9C, // jmp -100
                0x64 // return
            };
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
        }
    }

    // =====================================================================
    // decompileFile robustness
    // =====================================================================

    @Nested
    class DecompileFileRobustness {

        @Test
        void testDecompileFile_comprehensiveAbc_producesNonEmptyOutput() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testDecompileFile_realisticAbc_producesNonEmptyOutput() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildRealisticAbc();
            AbcFile abc = AbcFile.parse(data);
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testDecompileFile_largeAbc_producesNonEmptyOutput() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildLargeAbc();
            AbcFile abc = AbcFile.parse(data);
            String result = decompiler.decompileFile(abc);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void testDecompileFile_nullAbc_returnsEmpty() {
            String result = decompiler.decompileFile(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // =====================================================================
    // decompileMethod robustness
    // =====================================================================

    @Nested
    class DecompileMethodRobustness {

        @Test
        void testDecompileMethod_nullMethod_returnsComment() {
            String result = decompiler.decompileMethod(
                    null, null, null);
            assertNotNull(result);
            assertTrue(result.contains("unknown method"));
        }

        @Test
        void testDecompileMethod_multipleCallsSameDecompiler() {
            AbcTestFixture fixture = new AbcTestFixture();
            byte[] data = fixture.buildComprehensiveAbc();
            AbcFile abc = AbcFile.parse(data);

            for (int i = 0; i < 3; i++) {
                String result = decompiler.decompileFile(abc);
                assertNotNull(result);
                assertFalse(result.isEmpty());
            }
        }
    }

    // =====================================================================
    // Repeated opcode sequences
    // =====================================================================

    @Nested
    class RepeatedOpcodeTests {

        @Test
        void testDecompile_manyNops_producesOutput() {
            byte[] code = new byte[100]; // 100 NOPs
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
        }

        @Test
        void testDecompile_manyReturns_producesOutput() {
            byte[] code = new byte[50];
            for (int i = 0; i < code.length; i++) {
                code[i] = 0x65; // RETURNUNDEFINED
            }
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
        }

        @Test
        void testDecompile_alternatingLdaSta_producesOutput() {
            // lda v0, sta v1, lda v0, sta v2, ...
            byte[] code = new byte[200];
            for (int i = 0; i < code.length; i += 4) {
                code[i] = 0x60; // LDA
                code[i + 1] = 0x00; // v0
                if (i + 2 < code.length) {
                    code[i + 2] = 0x61; // STA
                    code[i + 3] = (byte) ((i / 4 + 1) & 0xFF);
                }
            }
            ArkDisassembler disasm = new ArkDisassembler();
            List<ArkInstruction> insns =
                    disasm.disassemble(code, 0, code.length);
            String result =
                    decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
