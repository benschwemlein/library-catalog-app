package com.example.library.exception;

public class ConcurrentLoanLimitException extends RuntimeException {
    public ConcurrentLoanLimitException(String message) {
        super(message);
    }
}
