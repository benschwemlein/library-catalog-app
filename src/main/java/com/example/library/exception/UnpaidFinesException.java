package com.example.library.exception;

import java.math.BigDecimal;

public class UnpaidFinesException extends RuntimeException {
    public UnpaidFinesException(String message) {
        super(message);
    }

    public UnpaidFinesException(Long memberId, BigDecimal totalFines) {
        super("Member id " + memberId + " has unpaid fines of $" + totalFines + ". Please pay before checking out.");
    }
}
