package com.example.library.exception;

public class CopyNotAvailableException extends RuntimeException {
    public CopyNotAvailableException(String message) {
        super(message);
    }

    public CopyNotAvailableException(Long copyId) {
        super("Book copy is not available: " + copyId);
    }
}
