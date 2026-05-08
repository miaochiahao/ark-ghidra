package com.arkghidra.disasm;

/**
 * Exception thrown when Ark bytecode cannot be decoded.
 */
public class DisassemblyException extends RuntimeException {

    /**
     * Constructs a disassembly exception with a message.
     *
     * @param message the detail message
     */
    public DisassemblyException(String message) {
        super(message);
    }

    /**
     * Constructs a disassembly exception with a message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public DisassemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
