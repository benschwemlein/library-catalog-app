package com.example.library.exception;

public class MaxRenewalsExceededException extends RuntimeException {
    public MaxRenewalsExceededException(String message) {
        super(message);
    }

    public MaxRenewalsExceededException(Long loanId, int maxRenewals) {
        super("Maximum renewals (" + maxRenewals + ") exceeded for loan id: " + loanId);
    }
}
