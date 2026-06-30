package com.example.library.pattern.strategy;

import com.example.library.entity.Loan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Fine strategy for STANDARD members: $0.25 per day overdue, capped at $25.00.
 */
@Component
@Slf4j
public class StandardFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("0.25");
    private static final BigDecimal MAX_FINE = new BigDecimal("25.00");

    @Override
    public BigDecimal calculateFine(Loan loan, LocalDate returnDate) {
        LocalDate dueDate = loan.getDueDate().toLocalDate();
        LocalDate effectiveReturn = (returnDate != null) ? returnDate : LocalDate.now();

        if (!effectiveReturn.isAfter(dueDate)) {
            log.debug("Loan {} returned on time (due={}, returned={}); no fine",
                    loan.getId(), dueDate, effectiveReturn);
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, effectiveReturn);
        BigDecimal fine = DAILY_RATE.multiply(BigDecimal.valueOf(daysOverdue));

        if (fine.compareTo(MAX_FINE) > 0) {
            log.debug("Loan {} fine capped at {} (raw={}, days={})",
                    loan.getId(), MAX_FINE, fine, daysOverdue);
            fine = MAX_FINE;
        }

        log.debug("Loan {} STANDARD fine: ${} ({} days overdue at ${}/ day)",
                loan.getId(), fine, daysOverdue, DAILY_RATE);
        return fine;
    }
}
