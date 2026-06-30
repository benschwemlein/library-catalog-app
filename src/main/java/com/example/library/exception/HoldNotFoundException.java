package com.example.library.exception;

public class HoldNotFoundException extends RuntimeException {
    public HoldNotFoundException(String message) {
        super(message);
    }

    public HoldNotFoundException(Long id) {
        super("Hold not found with id: " + id);
    }
}
