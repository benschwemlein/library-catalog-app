package com.example.library.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(Long id) {
        super("Book review not found with id: " + id);
    }
}
