package com.example.library.pattern.state;

import com.example.library.entity.LoanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class LostLoanState implements LoanState {

    @Autowired
    @Lazy
    private ReturnedLoanState returnedLoanState;

    @Override
    public void checkout(LoanContext context) {
        throw new IllegalStateException("Cannot checkout - loan is marked as lost");
    }

    @Override
    public void returnBook(LoanContext context) {
        context.getLoan().setReturnDate(LocalDateTime.now());
        context.setState(returnedLoanState);
        log.info("Lost loan {} found and returned", context.getLoan().getId());
    }

    @Override
    public void renew(LoanContext context) {
        throw new IllegalStateException("Cannot renew a lost loan");
    }

    @Override
    public void markOverdue(LoanContext context) {
        throw new IllegalStateException("Cannot mark lost loan as overdue");
    }

    @Override
    public void markLost(LoanContext context) {
        log.warn("Loan {} is already marked as lost", context.getLoan().getId());
    }

    @Override
    public LoanStatus getStatus() {
        return LoanStatus.LOST;
    }
}
