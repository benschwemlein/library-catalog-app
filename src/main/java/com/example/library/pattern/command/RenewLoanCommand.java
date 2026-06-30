package com.example.library.pattern.command;

import com.example.library.entity.Loan;
import com.example.library.repository.LoanRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Command that renews an active loan by extending the due date by 14 days
 * and incrementing the renewal counter.
 *
 * <p>Prototype-scoped; set {@code loanId} before calling {@code execute()}.</p>
 */
@Component
@Scope("prototype")
@Slf4j
public class RenewLoanCommand implements LibraryCommand {

    private static final int RENEWAL_DAYS = 14;

    @Autowired
    private LoanRepository loanRepository;

    @Setter private Long loanId;

    /** Stored before mutation so undo() can restore the original due date. */
    private LocalDateTime previousDueDate;

    @Override
    public CommandResult execute() {
        if (loanId == null) {
            return CommandResult.failure("Cannot execute: loanId is required");
        }
        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

            int maxRenewals = loan.getMember().getMembershipTier().getMaxRenewals();
            if (loan.getRenewalCount() >= maxRenewals) {
                return CommandResult.failure(
                        String.format("Loan %d cannot be renewed: maximum renewals (%d) reached", loanId, maxRenewals),
                        loanId
                );
            }

            this.previousDueDate = loan.getDueDate();

            loan.setDueDate(loan.getDueDate().plusDays(RENEWAL_DAYS));
            loan.setRenewalCount(loan.getRenewalCount() + 1);
            loanRepository.save(loan);

            log.info("RenewLoanCommand executed: loanId={} newDueDate={} renewalCount={}",
                    loanId, loan.getDueDate(), loan.getRenewalCount());

            return CommandResult.success(
                    String.format("Loan renewed. New due date: %s (renewal %d of %d)",
                            loan.getDueDate().toLocalDate(), loan.getRenewalCount(), maxRenewals),
                    loanId
            );
        } catch (Exception e) {
            log.error("RenewLoanCommand failed: loanId={} error={}", loanId, e.getMessage(), e);
            return CommandResult.failure("Renewal failed: " + e.getMessage(), loanId);
        }
    }

    @Override
    public CommandResult undo() {
        if (loanId == null || previousDueDate == null) {
            return CommandResult.failure("Cannot undo renewal: no prior state recorded");
        }
        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

            loan.setDueDate(previousDueDate);
            loan.setRenewalCount(Math.max(0, loan.getRenewalCount() - 1));
            loanRepository.save(loan);

            log.info("RenewLoanCommand undone: loanId={} dueDateRestored={} renewalCount={}",
                    loanId, previousDueDate, loan.getRenewalCount());

            return CommandResult.success(
                    "Renewal reversed. Due date restored to: " + previousDueDate.toLocalDate(),
                    loanId
            );
        } catch (Exception e) {
            log.error("RenewLoanCommand undo failed: loanId={} error={}", loanId, e.getMessage(), e);
            return CommandResult.failure("Undo renewal failed: " + e.getMessage(), loanId);
        }
    }

    @Override
    public String getDescription() {
        return String.format("Renew loanId=%d (previousDueDate=%s)", loanId,
                previousDueDate != null ? previousDueDate.toLocalDate() : "not yet executed");
    }
}
