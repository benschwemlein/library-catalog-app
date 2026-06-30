package com.example.library.exception;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(String message) {
        super(message);
    }

    public AuthorNotFoundException(Long id) {
        super("Author not found with id: " + id);
    }
}
