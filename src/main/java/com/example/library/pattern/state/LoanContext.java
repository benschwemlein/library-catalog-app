package com.example.library.pattern.state;

import com.example.library.entity.Loan;

public class LoanContext {

    private final Loan loan;
    private LoanState currentState;

    public LoanContext(Loan loan, LoanState initialState) {
        this.loan = loan;
        this.currentState = initialState;
        loan.setStatus(initialState.getStatus());
    }

    public void setState(LoanState state) {
        this.currentState = state;
        loan.setStatus(state.getStatus());
    }

    public LoanState getState() { return currentState; }
    public Loan getLoan() { return loan; }

    public void checkout() { currentState.checkout(this); }
    public void returnBook() { currentState.returnBook(this); }
    public void renew() { currentState.renew(this); }
    public void markOverdue() { currentState.markOverdue(this); }
    public void markLost() { currentState.markLost(this); }
}
