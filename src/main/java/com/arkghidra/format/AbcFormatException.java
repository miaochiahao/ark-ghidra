package com.arkghidra.format;

public class AbcFormatException extends RuntimeException {
    public AbcFormatException(String message) {
        super(message);
    }

    public AbcFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
