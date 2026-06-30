package com.example.library.pattern.state;

import com.example.library.entity.LoanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class OverdueLoanState implements LoanState {

    @Autowired
    @Lazy
    private ReturnedLoanState returnedLoanState;

    @Autowired
    @Lazy
    private LostLoanState lostLoanState;

    @Override
    public void checkout(LoanContext context) {
        throw new IllegalStateException("Cannot checkout - loan is overdue");
    }

    @Override
    public void returnBook(LoanContext context) {
        context.getLoan().setReturnDate(LocalDateTime.now());
        context.setState(returnedLoanState);
        log.info("Overdue loan {} returned", context.getLoan().getId());
    }

    @Override
    public void renew(LoanContext context) {
        throw new IllegalStateException("Cannot renew an overdue loan - return the book first");
    }

    @Override
    public void markOverdue(LoanContext context) {
        log.warn("Loan {} is already marked as overdue", context.getLoan().getId());
    }

    @Override
    public void markLost(LoanContext context) {
        context.setState(lostLoanState);
        log.info("Loan {} marked as lost", context.getLoan().getId());
    }

    @Override
    public LoanStatus getStatus() {
        return LoanStatus.OVERDUE;
    }
}
