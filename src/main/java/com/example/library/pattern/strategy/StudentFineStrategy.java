package com.example.library.pattern.strategy;

import com.example.library.entity.Loan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Fine strategy for STUDENT members: $0.15 per day overdue, capped at $15.00,
 * with a 1-day grace period (no fine if returned within 1 day of due date).
 */
@Component
@Slf4j
public class StudentFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("0.15");
    private static final BigDecimal MAX_FINE = new BigDecimal("15.00");
    private static final int GRACE_PERIOD_DAYS = 1;

    @Override
    public BigDecimal calculateFine(Loan loan, LocalDate returnDate) {
        LocalDate dueDate = loan.getDueDate().toLocalDate();
        LocalDate graceCutoff = dueDate.plusDays(GRACE_PERIOD_DAYS);
        LocalDate effectiveReturn = (returnDate != null) ? returnDate : LocalDate.now();

        if (!effectiveReturn.isAfter(graceCutoff)) {
            log.debug("Loan {} within grace period (due={}, grace={}, returned={}); no fine",
                    loan.getId(), dueDate, graceCutoff, effectiveReturn);
            return BigDecimal.ZERO;
        }

        // Count overdue days from the original due date, not the grace cutoff
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, effectiveReturn);
        BigDecimal fine = DAILY_RATE.multiply(BigDecimal.valueOf(daysOverdue));

        if (fine.compareTo(MAX_FINE) > 0) {
            log.debug("Loan {} STUDENT fine capped at {} (raw={}, days={})",
                    loan.getId(), MAX_FINE, fine, daysOverdue);
            fine = MAX_FINE;
        }

        log.debug("Loan {} STUDENT fine: ${} ({} days overdue at ${}/day, 1-day grace applied)",
                loan.getId(), fine, daysOverdue, DAILY_RATE);
        return fine;
    }
}
