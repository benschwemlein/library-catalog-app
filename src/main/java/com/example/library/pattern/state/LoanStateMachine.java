package com.example.library.pattern.state;

import com.example.library.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoanStateMachine {

    @Autowired
    private ActiveLoanState activeLoanState;

    @Autowired
    private OverdueLoanState overdueLoanState;

    @Autowired
    private ReturnedLoanState returnedLoanState;

    @Autowired
    private LostLoanState lostLoanState;

    public LoanContext createContext(Loan loan) {
        if (loan.getStatus() == null) {
            throw new IllegalArgumentException("Unknown loan status: null");
        }
        switch (loan.getStatus()) {
            case ACTIVE:
                return new LoanContext(loan, activeLoanState);
            case OVERDUE:
                return new LoanContext(loan, overdueLoanState);
            case RETURNED:
                return new LoanContext(loan, returnedLoanState);
            case LOST:
                return new LoanContext(loan, lostLoanState);
            default:
                throw new IllegalArgumentException("Unknown loan status: " + loan.getStatus());
        }
    }
}
