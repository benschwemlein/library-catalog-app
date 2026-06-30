package com.example.library.pattern.state;

import com.example.library.entity.LoanStatus;
import org.springframework.stereotype.Component;

@Component
public class ReturnedLoanState implements LoanState {

    private static final String TERMINAL_MESSAGE = "Loan is already returned - terminal state";

    @Override
    public void checkout(LoanContext context) {
        throw new IllegalStateException(TERMINAL_MESSAGE);
    }

    @Override
    public void returnBook(LoanContext context) {
        throw new IllegalStateException(TERMINAL_MESSAGE);
    }

    @Override
    public void renew(LoanContext context) {
        throw new IllegalStateException(TERMINAL_MESSAGE);
    }

    @Override
    public void markOverdue(LoanContext context) {
        throw new IllegalStateException(TERMINAL_MESSAGE);
    }

    @Override
    public void markLost(LoanContext context) {
        throw new IllegalStateException(TERMINAL_MESSAGE);
    }

    @Override
    public LoanStatus getStatus() {
        return LoanStatus.RETURNED;
    }
}
