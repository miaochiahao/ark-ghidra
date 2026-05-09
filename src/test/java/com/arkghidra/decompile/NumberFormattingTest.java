package com.arkghidra.decompile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.arkghidra.decompile.ArkTSExpression.LiteralExpression;

/**
 * Tests for number literal formatting (hex for bitmask patterns).
 */
@DisplayName("Number literal formatting")
class NumberFormattingTest {

    private static String render(long n) {
        return new LiteralExpression(String.valueOf(n),
                LiteralExpression.LiteralKind.NUMBER).toArkTS();
    }

    @Nested
    @DisplayName("Hex formatting for bitmask patterns")
    class HexFormatting {

        @Test
        @DisplayName("255 → 0xFF (8-bit mask)")
        void test255() {
            assertEquals("0xFF", render(255));
        }

        @Test
        @DisplayName("65535 → 0xFFFF (16-bit mask)")
        void test65535() {
            assertEquals("0xFFFF", render(65535));
        }

        @Test
        @DisplayName("256 → 0x100 (power of 2)")
        void test256() {
            assertEquals("0x100", render(256));
        }

        @Test
        @DisplayName("4096 → 0x1000 (power of 2)")
        void test4096() {
            assertEquals("0x1000", render(4096));
        }

        @Test
        @DisplayName("4294967295 → 0xFFFFFFFF (32-bit mask)")
        void testMaxInt() {
            assertEquals("0xFFFFFFFF", render(4294967295L));
        }

        @Test
        @DisplayName("4294967296 → 0x100000000 (2^32)")
        void test4GB() {
            assertEquals("0x100000000", render(4294967296L));
        }
    }

    @Nested
    @DisplayName("Decimal kept for normal numbers")
    class DecimalFormatting {

        @Test
        @DisplayName("0 stays as 0")
        void testZero() {
            assertEquals("0", render(0));
        }

        @Test
        @DisplayName("42 stays as 42")
        void testSmallNumber() {
            assertEquals("42", render(42));
        }

        @Test
        @DisplayName("100 stays as 100")
        void test100() {
            assertEquals("100", render(100));
        }

        @Test
        @DisplayName("300 stays as 300 (not a power of 2 or mask)")
        void test300() {
            assertEquals("300", render(300));
        }

        @Test
        @DisplayName("1000 stays as 1000")
        void test1000() {
            assertEquals("1000", render(1000));
        }

        @Test
        @DisplayName("512 → 0x200 (power of 2)")
        void test512() {
            assertEquals("0x200", render(512));
        }

        @Test
        @DisplayName("1024 → 0x400 (power of 2)")
        void test1024() {
            assertEquals("0x400", render(1024));
        }

        @Test
        @DisplayName("65536 → 0x10000 (power of 2)")
        void test65536() {
            assertEquals("0x10000", render(65536));
        }
    }
}
