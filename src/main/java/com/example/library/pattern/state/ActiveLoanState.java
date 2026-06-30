package com.example.library.pattern.state;

import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.exception.MaxRenewalsExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ActiveLoanState implements LoanState {

    @Autowired
    @Lazy
    private ReturnedLoanState returnedLoanState;

    @Autowired
    @Lazy
    private OverdueLoanState overdueLoanState;

    @Override
    public void checkout(LoanContext context) {
        throw new IllegalStateException("Loan is already active - cannot checkout again");
    }

    @Override
    public void returnBook(LoanContext context) {
        context.getLoan().setReturnDate(LocalDateTime.now());
        context.setState(returnedLoanState);
        log.info("Loan {} returned successfully", context.getLoan().getId());
    }

    @Override
    public void renew(LoanContext context) {
        Member member = context.getLoan().getMember();
        int maxRenewals = member.getMembershipTier().getMaxRenewals();
        int currentRenewals = context.getLoan().getRenewalCount();
        if (currentRenewals >= maxRenewals) {
            throw new MaxRenewalsExceededException(
                "Maximum renewals (" + maxRenewals + ") exceeded for loan id: " + context.getLoan().getId()
            );
        }
        context.getLoan().setRenewalCount(currentRenewals + 1);
        context.getLoan().setDueDate(context.getLoan().getDueDate().plusDays(14));
        log.info("Loan {} renewed (renewal {}/{}), new due date: {}",
            context.getLoan().getId(),
            context.getLoan().getRenewalCount(),
            maxRenewals,
            context.getLoan().getDueDate());
    }

    @Override
    public void markOverdue(LoanContext context) {
        context.setState(overdueLoanState);
        log.info("Loan {} marked as overdue", context.getLoan().getId());
    }

    @Override
    public void markLost(LoanContext context) {
        throw new IllegalStateException("Loan must be overdue before marking as lost");
    }

    @Override
    public LoanStatus getStatus() {
        return LoanStatus.ACTIVE;
    }
}
