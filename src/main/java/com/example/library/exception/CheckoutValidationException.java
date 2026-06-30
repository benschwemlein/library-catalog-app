package com.example.library.exception;

public class CheckoutValidationException extends RuntimeException {
    public CheckoutValidationException(String message) {
        super(message);
    }
}
