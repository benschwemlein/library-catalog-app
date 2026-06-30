package com.example.library.exception;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String message) {
        super(message);
    }

    public BranchNotFoundException(Long id) {
        super("Branch not found with id: " + id);
    }
}
