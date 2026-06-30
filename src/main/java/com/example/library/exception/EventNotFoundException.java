package com.example.library.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }

    public EventNotFoundException(Long id) {
        super("Library event not found with id: " + id);
    }
}
