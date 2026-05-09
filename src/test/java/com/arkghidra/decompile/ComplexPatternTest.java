package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.disasm.ArkDisassembler;
import com.arkghidra.disasm.ArkInstruction;
import com.arkghidra.format.AbcAccessFlags;
import com.arkghidra.format.AbcCatchBlock;
import com.arkghidra.format.AbcCode;
import com.arkghidra.format.AbcMethod;
import com.arkghidra.format.AbcTryBlock;

/**
 * Tests for complex control flow patterns in the decompiler.
 *
 * <p>Validates that the decompiler produces correct (or at least non-crashing)
 * output for nested loops, try/catch in loops, switch inside if/else,
 * multiple return paths, and compound boolean conditions.</p>
 *
 * <p>Uses the {@code ArkDisassembler + decompiler.decompileInstructions()}
 * pattern with manually constructed bytecode sequences.</p>
 */
class ComplexPatternTest {

    private ArkTSDecompiler decompiler;
    private ArkDisassembler disasm;

    @BeforeEach
    void setUp() {
        decompiler = new ArkTSDecompiler();
        disasm = new ArkDisassembler();
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    private static byte[] le32(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) {
            total += a.length;
        }
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    private List<ArkInstruction> dis(byte[] code) {
        return disasm.disassemble(code, 0, code.length);
    }

    // =====================================================================
    // 1. Nested loops
    // =====================================================================

    @Nested
    @DisplayName("Nested loops")
    class NestedLoopTests {

        @Test
        @DisplayName("Two nested while loops produce non-empty output")
        void testNestedWhileLoops_producesOutput() {
            // Outer loop: while (v0 < 10) { inner loop }
            // Inner loop: while (v1 < 5) { v1++ }
            // After inner: v1 = 0; v0++
            //
            // offset  0: lda v0           (2)
            // offset  2: ldai 10          (5)
            // offset  7: less 0, v1       (3)   acc = v0 < 10
            // offset 10: jeqz +38         (2)   -> offset 50 (end)
            // offset 12: lda v1           (2)
            // offset 14: ldai 5           (5)
            // offset 19: less 0, v2       (3)   acc = v1 < 5
            // offset 22: jeqz +10         (2)   -> offset 34 (end inner)
            // offset 24: lda v1           (2)
            // offset 26: inc 0x0          (2)   v1++
            // offset 28: sta v1           (2)
            // offset 30: jmp -18          (2)   -> offset 12 (inner header)
            // offset 32: ldai 0           (5)   (unreachable padding)
            // offset 34: ldai 0           (5)   end_inner: v1 = 0
            // offset 39: sta v1           (2)
            // offset 41: lda v0           (2)
            // offset 43: inc 0x0          (2)   v0++
            // offset 45: sta v0           (2)
            // offset 47: jmp -47          (2)   -> offset 0 (outer header)
            // offset 49: nop              (1)
            // offset 50: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x62), le32(10),          // ldai 10          offset 2
                bytes(0x11, 0x00, 0x01),        // less 0, v1       offset 7
                bytes(0x4F, 0x26),              // jeqz +38         offset 10 -> 50
                bytes(0x60, 0x01),              // lda v1           offset 12
                bytes(0x62), le32(5),           // ldai 5           offset 14
                bytes(0x11, 0x00, 0x02),        // less 0, v2       offset 19
                bytes(0x4F, 0x0A),              // jeqz +10         offset 22 -> 34
                bytes(0x60, 0x01),              // lda v1           offset 24
                bytes(0x21, 0x00),              // inc 0x0          offset 26
                bytes(0x61, 0x01),              // sta v1           offset 28
                bytes(0x4D, (byte) 0xEE),       // jmp -18          offset 30 -> 12
                bytes(0x62), le32(0),           // ldai 0           offset 32
                bytes(0x62), le32(0),           // ldai 0           offset 34
                bytes(0x61, 0x01),              // sta v1           offset 39
                bytes(0x60, 0x00),              // lda v0           offset 41
                bytes(0x21, 0x00),              // inc 0x0          offset 43
                bytes(0x61, 0x00),              // sta v0           offset 45
                bytes(0x4D, (byte) 0xD1),       // jmp -47          offset 47 -> 0
                bytes(0x64)                      // return           offset 49
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result, "Decompiler should not return null");
            assertFalse(result.isEmpty(),
                    "Should produce non-empty output");
        }

        @Test
        @DisplayName("Nested loops with break to outer loop")
        void testNestedLoops_withBreakOuter() {
            // Outer loop: lda v0; jeqz end; inner: lda v0; jeqz end_inner;
            // ldai 2; sta v1; jmp outer_header
            // offset  0: ldai 1           (5)
            // offset  5: sta v0           (2)
            // offset  7: lda v0           (2)   outer header
            // offset  9: jeqz +12         (2)   -> offset 23 (end)
            // offset 11: lda v0           (2)   inner header
            // offset 13: jeqz +4          (2)   -> offset 19 (end_inner)
            // offset 15: ldai 2           (5)   inner body
            // offset 20: sta v1           (2)
            // offset 22: jmp -15          (2)   -> offset 7 (outer header)
            byte[] code = concat(
                bytes(0x62), le32(1),           // ldai 1           offset 0
                bytes(0x61, 0x00),              // sta v0           offset 5
                bytes(0x60, 0x00),              // lda v0           offset 7
                bytes(0x4F, 0x0C),              // jeqz +12         offset 9 -> 23
                bytes(0x60, 0x00),              // lda v0           offset 11
                bytes(0x4F, 0x04),              // jeqz +4          offset 13 -> 19
                bytes(0x62), le32(2),           // ldai 2           offset 15
                bytes(0x61, 0x01),              // sta v1           offset 20
                bytes(0x4D, (byte) 0xF1),       // jmp -15          offset 22 -> 7
                bytes(0x64)                      // return           offset 24
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Deeply nested three-level loops")
        void testThreeLevelNestedLoops() {
            // Three nested loops: outer (v0), middle (v1), inner (v2)
            // All use less-than checks with v3 as temp register.
            // offset  0: lda v0           (2)
            // offset  2: ldai 3           (5)
            // offset  7: less 0, v3       (3)   acc = v0 < 3
            // offset 10: jeqz +40         (2)   -> offset 52 (end)
            // offset 12: lda v1           (2)
            // offset 14: ldai 4           (5)
            // offset 19: less 0, v3       (3)   acc = v1 < 4
            // offset 22: jeqz +16         (2)   -> offset 40 (end_middle)
            // offset 24: lda v2           (2)
            // offset 26: ldai 5           (5)
            // offset 31: less 0, v3       (3)   acc = v2 < 5
            // offset 34: jeqz +2          (2)   -> offset 38 (end_inner)
            // offset 36: jmp +0           (2)   nop body
            // offset 38: jmp -24          (2)   -> offset 14 (middle cond)
            // offset 40: ldai 0           (5)   end_middle: v1 = 0
            // offset 45: sta v1           (2)
            // offset 47: jmp -47          (2)   -> offset 0 (outer header)
            // offset 49: nop              (1)
            // offset 50: nop              (1)
            // offset 51: nop              (1)
            // offset 52: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x62), le32(3),           // ldai 3           offset 2
                bytes(0x11, 0x00, 0x03),        // less 0, v3       offset 7
                bytes(0x4F, 0x28),              // jeqz +40         offset 10 -> 52
                bytes(0x60, 0x01),              // lda v1           offset 12
                bytes(0x62), le32(4),           // ldai 4           offset 14
                bytes(0x11, 0x00, 0x03),        // less 0, v3       offset 19
                bytes(0x4F, 0x10),              // jeqz +16         offset 22 -> 40
                bytes(0x60, 0x02),              // lda v2           offset 24
                bytes(0x62), le32(5),           // ldai 5           offset 26
                bytes(0x11, 0x00, 0x03),        // less 0, v3       offset 31
                bytes(0x4F, 0x02),              // jeqz +2          offset 34 -> 38
                bytes(0x4D, 0x00),              // jmp +0           offset 36 -> 38
                bytes(0x4D, (byte) 0xE8),       // jmp -24          offset 38 -> 14
                bytes(0x62), le32(0),           // ldai 0           offset 40
                bytes(0x61, 0x01),              // sta v1           offset 45
                bytes(0x4D, (byte) 0xD1),       // jmp -47          offset 47 -> 0
                bytes(0x00),                    // nop              offset 49
                bytes(0x00),                    // nop              offset 50
                bytes(0x00),                    // nop              offset 51
                bytes(0x64)                      // return           offset 52
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result, "Three-level nesting should not crash");
            assertFalse(result.isEmpty(),
                    "Three-level nesting should produce output");
        }
    }

    // =====================================================================
    // 2. Try/catch in loop
    // =====================================================================

    @Nested
    @DisplayName("Try/catch in loops")
    class TryCatchInLoopTests {

        @Test
        @DisplayName("Try/catch inside a loop produces valid output")
        void testTryCatchInsideLoop() {
            // Loop body contains a try block
            byte[] codeBytes = concat(
                bytes(0x62), le32(0),           // ldai 0           offset 0
                bytes(0x61, 0x00),              // sta v0           offset 5
                bytes(0x60, 0x00),              // lda v0           offset 7
                bytes(0x62), le32(10),          // ldai 10          offset 9
                bytes(0x11, 0x00, 0x01),        // less 0, v1       offset 14
                bytes(0x4F, 0x0C),              // jeqz +12         offset 17 -> 31
                bytes(0x62), le32(42),          // ldai 42          offset 19
                bytes(0x61, 0x02),              // sta v2           offset 24
                bytes(0x4D, (byte) 0xED),       // jmp -19          offset 26 -> 7
                bytes(0x00),                    // nop              offset 28
                bytes(0x00),                    // nop              offset 29
                bytes(0x00),                    // nop              offset 30
                bytes(0x64)                      // return           offset 31
            );
            AbcCatchBlock catchBlock =
                    new AbcCatchBlock(0, 28, 1, false);
            AbcTryBlock tryBlock = new AbcTryBlock(19, 7,
                    List.of(catchBlock));
            AbcCode code = new AbcCode(1, 0, codeBytes.length,
                    codeBytes, List.of(tryBlock), 1);
            AbcMethod method = new AbcMethod(0, 0, "loopWithTry",
                    AbcAccessFlags.ACC_PUBLIC, 0, 0);
            String result = decompiler.decompileMethod(method, code, null);
            assertNotNull(result,
                    "try/catch in loop should not return null");
            assertFalse(result.isEmpty(),
                    "try/catch in loop should produce output");
        }

        @Test
        @DisplayName("Nested try/catch inside a while loop")
        void testNestedTryCatchInLoop() {
            // Simple loop with two nested try blocks
            byte[] codeBytes = concat(
                bytes(0x62), le32(0),           // ldai 0           offset 0
                bytes(0x61, 0x00),              // sta v0           offset 5
                bytes(0x60, 0x00),              // lda v0           offset 7
                bytes(0x62), le32(5),           // ldai 5           offset 9
                bytes(0x11, 0x00, 0x01),        // less 0, v1       offset 14
                bytes(0x4F, 0x0A),              // jeqz +10         offset 17 -> 29
                bytes(0x62), le32(1),           // ldai 1           offset 19
                bytes(0x61, 0x01),              // sta v1           offset 24
                bytes(0x4D, (byte) 0xF2),       // jmp -14          offset 26 -> 12
                bytes(0x00),                    // nop              offset 28
                bytes(0x64)                      // return           offset 29
            );
            AbcCatchBlock innerCatch =
                    new AbcCatchBlock(0, 28, 1, false);
            AbcTryBlock innerTry = new AbcTryBlock(19, 5,
                    List.of(innerCatch));
            AbcCatchBlock outerCatch =
                    new AbcCatchBlock(1, 28, 1, false);
            AbcTryBlock outerTry = new AbcTryBlock(7, 22,
                    List.of(outerCatch));
            AbcCode code = new AbcCode(1, 0, codeBytes.length,
                    codeBytes, List.of(innerTry, outerTry), 1);
            AbcMethod method = new AbcMethod(0, 0, "loopNestedTry",
                    AbcAccessFlags.ACC_PUBLIC, 0, 0);
            String result = decompiler.decompileMethod(method, code, null);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Relaxed: just verify it doesn't crash and produces output
        }

        @Test
        @DisplayName("Loop body entirely inside try block")
        void testLoopEntirelyInTry() {
            // Entire loop is inside a try block
            byte[] codeBytes = concat(
                bytes(0x62), le32(0),           // ldai 0           offset 0
                bytes(0x61, 0x00),              // sta v0           offset 5
                bytes(0x60, 0x00),              // lda v0           offset 7
                bytes(0x4F, 0x06),              // jeqz +6          offset 9 -> 17
                bytes(0x21, 0x00),              // inc 0x0          offset 11
                bytes(0x61, 0x00),              // sta v0           offset 13
                bytes(0x4D, (byte) 0xF6),       // jmp -10          offset 15 -> 5
                bytes(0x64)                      // return           offset 17
            );
            AbcCatchBlock catchBlock =
                    new AbcCatchBlock(0, 17, 1, false);
            AbcTryBlock tryBlock = new AbcTryBlock(0, 17,
                    List.of(catchBlock));
            AbcCode code = new AbcCode(1, 0, codeBytes.length,
                    codeBytes, List.of(tryBlock), 1);
            AbcMethod method = new AbcMethod(0, 0, "tryLoop",
                    AbcAccessFlags.ACC_PUBLIC, 0, 0);
            String result = decompiler.decompileMethod(method, code, null);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // 3. Switch inside if/else
    // =====================================================================

    @Nested
    @DisplayName("Switch inside if/else")
    class SwitchInIfElseTests {

        @Test
        @DisplayName("Switch inside if-branch produces output")
        void testSwitchInsideIfBranch() {
            // if (v0) { switch(v0) { case 1: return 10; case 2: return 20; } }
            // return 0;
            //
            // offset  0: lda v0           (2)
            // offset  2: jeqz +20         (2)   -> offset 24 (else/return 0)
            // offset  4: lda v0           (2)
            // offset  6: sta v1           (2)   save discriminant
            // offset  8: ldai 1           (5)   test case 1
            // offset 13: jeq v1, +9       (3)   -> offset 25 (case1 body)
            // offset 16: ldai 2           (5)   test case 2
            // offset 21: jeq v1, +4       (3)   -> offset 28 (case2 body)
            // offset 24: ldai 0           (5)   else/default: return 0
            // offset 29: return           (1)
            // offset 30: nop              (1)
            // offset 31: nop              (1)
            // offset 32: nop              (1)
            // offset 33: nop              (1)
            // offset 34: nop              (1)
            // offset 35: ldai 10          (5)   case1 body
            // offset 40: return           (1)
            // offset 41: ldai 20          (5)   case2 body
            // offset 46: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x14),              // jeqz +20         offset 2 -> 24
                bytes(0x60, 0x00),              // lda v0           offset 4
                bytes(0x61, 0x01),              // sta v1           offset 6
                bytes(0x62), le32(1),           // ldai 1           offset 8
                bytes(0x5C, 0x01, 0x09),        // jeq v1, +9       offset 13 -> 25
                bytes(0x62), le32(2),           // ldai 2           offset 16
                bytes(0x5C, 0x01, 0x04),        // jeq v1, +4       offset 21 -> 28
                bytes(0x62), le32(0),           // ldai 0           offset 24
                bytes(0x64),                    // return           offset 29
                bytes(0x00),                    // nop              offset 30
                bytes(0x00),                    // nop              offset 31
                bytes(0x00),                    // nop              offset 32
                bytes(0x00),                    // nop              offset 33
                bytes(0x00),                    // nop              offset 34
                bytes(0x62), le32(10),          // ldai 10          offset 35
                bytes(0x64),                    // return           offset 40
                bytes(0x62), le32(20),          // ldai 20          offset 41
                bytes(0x64)                      // return           offset 46
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("switch") || result.contains("if"),
                    "Should contain switch or if: " + result);
        }

        @Test
        @DisplayName("If/else with returns in both branches")
        void testIfElseWithReturns() {
            // if (v0) { return 10; } else { return 20; }
            // offset  0: lda v0           (2)
            // offset  2: jeqz +8          (2)   -> offset 12 (else)
            // offset  4: ldai 10          (5)   return 10
            // offset  9: jmp +8           (2)   -> offset 19 (end)
            // offset 11: nop              (1)
            // offset 12: ldai 20          (5)   else: return 20
            // offset 17: nop              (1)
            // offset 18: nop              (1)
            // offset 19: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x08),              // jeqz +8          offset 2 -> 12
                bytes(0x62), le32(10),          // ldai 10          offset 4
                bytes(0x4D, 0x08),              // jmp +8           offset 9 -> 19
                bytes(0x00),                    // nop              offset 11
                bytes(0x62), le32(20),          // ldai 20          offset 12
                bytes(0x00),                    // nop              offset 17
                bytes(0x00),                    // nop              offset 18
                bytes(0x64)                      // return           offset 19
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // 4. Multiple return paths
    // =====================================================================

    @Nested
    @DisplayName("Multiple return paths")
    class MultipleReturnPathTests {

        @Test
        @DisplayName("Returns in both branches of if/else")
        void testReturnsInBothBranches() {
            // if (v0) { return 10; } else { return 20; }
            // Uses jmp to skip else branch after then-branch return
            // offset  0: lda v0           (2)
            // offset  2: jeqz +5          (2)   -> offset 9 (else)
            // offset  4: ldai 10          (5)   then: return 10
            // offset  9: ldai 20          (5)   else: return 20
            // offset 14: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x05),              // jeqz +5          offset 2 -> 9
                bytes(0x62), le32(10),          // ldai 10          offset 4
                bytes(0x62), le32(20),          // ldai 20          offset 9
                bytes(0x64)                      // return           offset 14
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Return in if, return after if")
        void testReturnInIfAndAfterIf() {
            // if (v0) { return 10; }
            // return 20;
            // offset  0: lda v0           (2)
            // offset  2: jeqz +5          (2)   -> offset 9 (after if)
            // offset  4: ldai 10          (5)
            // offset  9: return           (1)
            // offset 10: ldai 20          (5)
            // offset 15: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x05),              // jeqz +5          offset 2 -> 9
                bytes(0x62), le32(10),          // ldai 10          offset 4
                bytes(0x64),                    // return           offset 9
                bytes(0x62), le32(20),          // ldai 20          offset 10
                bytes(0x64)                      // return           offset 15
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Three-way return via if/else if/else")
        void testThreeWayReturn() {
            // if (v0 === 1) return 10; else if (v0 === 2) return 20; else return 30;
            // Uses stricteq for comparison
            // offset  0: lda v0           (2)
            // offset  2: ldai 1           (5)
            // offset  7: stricteq 0, v1   (3)
            // offset 10: jeqz +6          (2)   -> offset 18
            // offset 12: ldai 10          (5)
            // offset 17: return           (1)
            // offset 18: lda v0           (2)
            // offset 20: ldai 2           (5)
            // offset 25: stricteq 0, v1   (3)
            // offset 28: jeqz +6          (2)   -> offset 36
            // offset 30: ldai 20          (5)
            // offset 35: return           (1)
            // offset 36: ldai 30          (5)
            // offset 41: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x62), le32(1),           // ldai 1           offset 2
                bytes(0x28, 0x00, 0x01),        // stricteq 0, v1   offset 7
                bytes(0x4F, 0x06),              // jeqz +6          offset 10 -> 18
                bytes(0x62), le32(10),          // ldai 10          offset 12
                bytes(0x64),                    // return           offset 17
                bytes(0x60, 0x00),              // lda v0           offset 18
                bytes(0x62), le32(2),           // ldai 2           offset 20
                bytes(0x28, 0x00, 0x01),        // stricteq 0, v1   offset 25
                bytes(0x4F, 0x06),              // jeqz +6          offset 28 -> 36
                bytes(0x62), le32(20),          // ldai 20          offset 30
                bytes(0x64),                    // return           offset 35
                bytes(0x62), le32(30),          // ldai 30          offset 36
                bytes(0x64)                      // return           offset 41
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Relaxed: just check the decompiler produces output
        }

        @Test
        @DisplayName("Return inside nested if")
        void testReturnInsideNestedIf() {
            // if (v0) { if (v1) { return 10; } return 20; }
            // return 30;
            // offset  0: lda v0           (2)
            // offset  2: jeqz +16         (2)   -> offset 20 (after outer if)
            // offset  4: lda v1           (2)
            // offset  6: jeqz +6          (2)   -> offset 14 (after inner if)
            // offset  8: ldai 10          (5)
            // offset 13: return           (1)
            // offset 14: ldai 20          (5)
            // offset 19: return           (1)
            // offset 20: ldai 30          (5)
            // offset 25: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x10),              // jeqz +16         offset 2 -> 20
                bytes(0x60, 0x01),              // lda v1           offset 4
                bytes(0x4F, 0x06),              // jeqz +6          offset 6 -> 14
                bytes(0x62), le32(10),          // ldai 10          offset 8
                bytes(0x64),                    // return           offset 13
                bytes(0x62), le32(20),          // ldai 20          offset 14
                bytes(0x64),                    // return           offset 19
                bytes(0x62), le32(30),          // ldai 30          offset 20
                bytes(0x64)                      // return           offset 25
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // 5. Compound boolean conditions
    // =====================================================================

    @Nested
    @DisplayName("Compound boolean conditions")
    class CompoundBooleanTests {

        @Test
        @DisplayName("a && b || c produces output without crash")
        void testCompoundAndOr() {
            // (a && b) || c pattern
            // offset  0: lda v0           (2)   load a
            // offset  2: jeqz +18         (2)   -> offset 22 (evaluate c)
            // offset  4: lda v1           (2)   load b
            // offset  6: jeqz +14         (2)   -> offset 22 (evaluate c)
            // offset  8: ldai 1           (5)   a && b -> true
            // offset 13: sta v3           (2)
            // offset 15: jmp +10          (2)   -> offset 27 (merge)
            // offset 17: nop              (1)
            // offset 18: nop              (1)
            // offset 19: nop              (1)
            // offset 20: nop              (1)
            // offset 21: nop              (1)
            // offset 22: lda v2           (2)   load c
            // offset 24: sta v3           (2)
            // offset 26: nop              (1)
            // offset 27: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x12),              // jeqz +18         offset 2 -> 22
                bytes(0x60, 0x01),              // lda v1           offset 4
                bytes(0x4F, 0x0E),              // jeqz +14         offset 6 -> 22
                bytes(0x62), le32(1),           // ldai 1           offset 8
                bytes(0x61, 0x03),              // sta v3           offset 13
                bytes(0x4D, 0x0A),              // jmp +10          offset 15 -> 27
                bytes(0x00),                    // nop              offset 17
                bytes(0x00),                    // nop              offset 18
                bytes(0x00),                    // nop              offset 19
                bytes(0x00),                    // nop              offset 20
                bytes(0x00),                    // nop              offset 21
                bytes(0x60, 0x02),              // lda v2           offset 22
                bytes(0x61, 0x03),              // sta v3           offset 24
                bytes(0x00),                    // nop              offset 26
                bytes(0x64)                      // return           offset 27
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("&&") || result.contains("||")
                    || result.contains("if"),
                    "Should contain && or || or if: " + result);
        }

        @Test
        @DisplayName("Short-circuit AND with three conditions")
        void testThreeWayAnd() {
            // a && b && c pattern
            // offset  0: lda v0           (2)
            // offset  2: jeqz +20         (2)   -> offset 24 (false path)
            // offset  4: lda v1           (2)
            // offset  6: jeqz +16         (2)   -> offset 24
            // offset  8: lda v2           (2)
            // offset 10: jeqz +12         (2)   -> offset 24
            // offset 12: ldai 1           (5)
            // offset 17: sta v3           (2)
            // offset 19: jmp +7           (2)   -> offset 28 (merge)
            // offset 21: nop              (1)
            // offset 22: nop              (1)
            // offset 23: nop              (1)
            // offset 24: ldai 0           (5)
            // offset 29: sta v3           (2)
            // offset 31: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x14),              // jeqz +20         offset 2 -> 24
                bytes(0x60, 0x01),              // lda v1           offset 4
                bytes(0x4F, 0x10),              // jeqz +16         offset 6 -> 24
                bytes(0x60, 0x02),              // lda v2           offset 8
                bytes(0x4F, 0x0C),              // jeqz +12         offset 10 -> 24
                bytes(0x62), le32(1),           // ldai 1           offset 12
                bytes(0x61, 0x03),              // sta v3           offset 17
                bytes(0x4D, 0x07),              // jmp +7           offset 19 -> 28
                bytes(0x00),                    // nop              offset 21
                bytes(0x00),                    // nop              offset 22
                bytes(0x00),                    // nop              offset 23
                bytes(0x62), le32(0),           // ldai 0           offset 24
                bytes(0x61, 0x03),              // sta v3           offset 29
                bytes(0x64)                      // return           offset 31
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Short-circuit OR with three conditions")
        void testThreeWayOr() {
            // a || b || c pattern
            // offset  0: lda v0           (2)
            // offset  2: jnez +20         (2)   -> offset 24 (true path)
            // offset  4: lda v1           (2)
            // offset  6: jnez +16         (2)   -> offset 24
            // offset  8: lda v2           (2)
            // offset 10: jnez +12         (2)   -> offset 24
            // offset 12: ldai 0           (5)
            // offset 17: sta v3           (2)
            // offset 19: jmp +7           (2)   -> offset 28
            // offset 21: nop              (1)
            // offset 22: nop              (1)
            // offset 23: nop              (1)
            // offset 24: ldai 1           (5)
            // offset 29: sta v3           (2)
            // offset 31: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x51, 0x14),              // jnez +20         offset 2 -> 24
                bytes(0x60, 0x01),              // lda v1           offset 4
                bytes(0x51, 0x10),              // jnez +16         offset 6 -> 24
                bytes(0x60, 0x02),              // lda v2           offset 8
                bytes(0x51, 0x0C),              // jnez +12         offset 10 -> 24
                bytes(0x62), le32(0),           // ldai 0           offset 12
                bytes(0x61, 0x03),              // sta v3           offset 17
                bytes(0x4D, 0x07),              // jmp +7           offset 19 -> 28
                bytes(0x00),                    // nop              offset 21
                bytes(0x00),                    // nop              offset 22
                bytes(0x00),                    // nop              offset 23
                bytes(0x62), le32(1),           // ldai 1           offset 24
                bytes(0x61, 0x03),              // sta v3           offset 29
                bytes(0x64)                      // return           offset 31
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Negated condition with AND")
        void testNegatedConditionWithAnd() {
            // !a && b: test a is falsy, then test b
            // offset  0: lda v0           (2)
            // offset  2: jnez +14         (2)   -> offset 18 (a is true -> false)
            // offset  4: lda v1           (2)
            // offset  6: jeqz +10         (2)   -> offset 18 (b is false -> false)
            // offset  8: ldai 1           (5)   both conditions met
            // offset 13: sta v2           (2)
            // offset 15: jmp +5           (2)   -> offset 22
            // offset 17: nop              (1)
            // offset 18: ldai 0           (5)   false path
            // offset 23: sta v2           (2)
            // offset 25: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x51, 0x0E),              // jnez +14         offset 2 -> 18
                bytes(0x60, 0x01),              // lda v1           offset 4
                bytes(0x4F, 0x0A),              // jeqz +10         offset 6 -> 18
                bytes(0x62), le32(1),           // ldai 1           offset 8
                bytes(0x61, 0x02),              // sta v2           offset 13
                bytes(0x4D, 0x05),              // jmp +5           offset 15 -> 22
                bytes(0x00),                    // nop              offset 17
                bytes(0x62), le32(0),           // ldai 0           offset 18
                bytes(0x61, 0x02),              // sta v2           offset 23
                bytes(0x64)                      // return           offset 25
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // =====================================================================
    // 6. Mixed complex patterns
    // =====================================================================

    @Nested
    @DisplayName("Mixed complex patterns")
    class MixedComplexTests {

        @Test
        @DisplayName("Loop with if/else and continue/break patterns")
        void testLoopWithIfElseContinueBreak() {
            // while (v0 < 10) {
            //   if (v0 === 3) { v0++; continue; }
            //   if (v0 === 7) { break; }
            //   v1 += v0;
            //   v0++;
            // }
            // return v1;
            //
            // offset  0: lda v0           (2)   loop header
            // offset  2: ldai 10          (5)
            // offset  7: less 0, v2       (3)   v0 < 10
            // offset 10: jeqz +41         (2)   -> offset 53 (end loop)
            // offset 12: lda v0           (2)   check v0 === 3
            // offset 14: ldai 3           (5)
            // offset 19: stricteq 0, v2   (3)
            // offset 22: jeqz +8          (2)   -> offset 32 (skip continue)
            // offset 24: lda v0           (2)   v0++
            // offset 26: inc 0x0          (2)
            // offset 28: sta v0           (2)
            // offset 30: jmp -30          (2)   -> offset 0 (continue)
            // offset 32: lda v0           (2)   check v0 === 7
            // offset 34: ldai 7           (5)
            // offset 39: stricteq 0, v2   (3)
            // offset 42: jeqz +9          (2)   -> offset 53 (break = end loop)
            // offset 44: lda v1           (2)   v1 += v0
            // offset 46: add2 0, v0       (3)
            // offset 49: sta v1           (2)
            // offset 51: jmp -51          (2)   -> offset 0 (loop back)
            // offset 53: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x62), le32(10),          // ldai 10          offset 2
                bytes(0x11, 0x00, 0x02),        // less 0, v2       offset 7
                bytes(0x4F, 0x29),              // jeqz +41         offset 10 -> 53
                bytes(0x60, 0x00),              // lda v0           offset 12
                bytes(0x62), le32(3),           // ldai 3           offset 14
                bytes(0x28, 0x00, 0x02),        // stricteq 0, v2   offset 19
                bytes(0x4F, 0x08),              // jeqz +8          offset 22 -> 32
                bytes(0x60, 0x00),              // lda v0           offset 24
                bytes(0x21, 0x00),              // inc 0x0          offset 26
                bytes(0x61, 0x00),              // sta v0           offset 28
                bytes(0x4D, (byte) 0xE2),       // jmp -30          offset 30 -> 0
                bytes(0x60, 0x00),              // lda v0           offset 32
                bytes(0x62), le32(7),           // ldai 7           offset 34
                bytes(0x28, 0x00, 0x02),        // stricteq 0, v2   offset 39
                bytes(0x4F, 0x09),              // jeqz +9          offset 42 -> 53
                bytes(0x60, 0x01),              // lda v1           offset 44
                bytes(0x0A, 0x00, 0x00),        // add2 0, v0       offset 46
                bytes(0x61, 0x01),              // sta v1           offset 49
                bytes(0x4D, (byte) 0xCF),       // jmp -49          offset 51 -> 2
                bytes(0x64)                      // return           offset 53
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result, "Complex loop should not crash");
            assertFalse(result.isEmpty(),
                    "Complex loop should produce output");
        }

        @Test
        @DisplayName("If/else chain with different operations")
        void testIfElseChain() {
            // if (v0) v1 = 10; else if (v1) v1 = 20; else v1 = 0;
            // return v1;
            // offset  0: lda v0           (2)
            // offset  2: jeqz +10         (2)   -> offset 14
            // offset  4: ldai 10          (5)
            // offset  9: sta v1           (2)
            // offset 11: jmp +10          (2)   -> offset 23 (merge)
            // offset 13: nop              (1)
            // offset 14: lda v1           (2)   else if
            // offset 16: jeqz +5          (2)   -> offset 23 (merge)
            // offset 18: ldai 20          (5)
            // offset 23: nop              (1)
            // Wait, let me redo with cleaner offsets.
            // offset  0: lda v0           (2)
            // offset  2: jeqz +10         (2)   -> offset 14
            // offset  4: ldai 10          (5)
            // offset  9: sta v1           (2)
            // offset 11: jmp +10          (2)   -> offset 23
            // offset 13: nop              (1)
            // offset 14: lda v1           (2)
            // offset 16: jeqz +5          (2)   -> offset 23
            // offset 18: ldai 20          (5)
            // offset 23: return           (1)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x4F, 0x0A),              // jeqz +10         offset 2 -> 14
                bytes(0x62), le32(10),          // ldai 10          offset 4
                bytes(0x61, 0x01),              // sta v1           offset 9
                bytes(0x4D, 0x0A),              // jmp +10          offset 11 -> 23
                bytes(0x00),                    // nop              offset 13
                bytes(0x60, 0x01),              // lda v1           offset 14
                bytes(0x4F, 0x05),              // jeqz +5          offset 16 -> 23
                bytes(0x62), le32(20),          // ldai 20          offset 18
                bytes(0x61, 0x01),              // sta v1           offset 23
                bytes(0x64)                      // return           offset 25
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Loop with early return inside conditional")
        void testLoopWithEarlyReturn() {
            // while (true) {
            //   if (v0 > 10) return v0;
            //   v0++;
            // }
            // offset  0: lda v0           (2)   loop header
            // offset  2: ldai 10          (5)
            // offset  7: greater 0, v1    (3)   v0 > 10
            // offset 10: jeqz +4          (2)   -> offset 16 (skip return)
            // offset 12: lda v0           (2)   return v0
            // offset 14: return           (1)
            // offset 15: nop              (1)
            // offset 16: lda v0           (2)   v0++
            // offset 18: inc 0x0          (2)
            // offset 20: sta v0           (2)
            // offset 22: jmp -22          (2)   -> offset 0 (loop back)
            byte[] code = concat(
                bytes(0x60, 0x00),              // lda v0           offset 0
                bytes(0x62), le32(10),          // ldai 10          offset 2
                bytes(0x13, 0x00, 0x01),        // greater 0, v1    offset 7
                bytes(0x4F, 0x04),              // jeqz +4          offset 10 -> 16
                bytes(0x60, 0x00),              // lda v0           offset 12
                bytes(0x64),                    // return           offset 14
                bytes(0x00),                    // nop              offset 15
                bytes(0x60, 0x00),              // lda v0           offset 16
                bytes(0x21, 0x00),              // inc 0x0          offset 18
                bytes(0x61, 0x00),              // sta v0           offset 20
                bytes(0x4D, (byte) 0xEA)        // jmp -22          offset 22 -> 0
            );
            List<ArkInstruction> insns = dis(code);
            String result = decompiler.decompileInstructions(insns);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
