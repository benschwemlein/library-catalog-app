package com.example.library.pattern.strategy;

import com.example.library.entity.Loan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Fine strategy for reference materials: $1.00 per day overdue, NO cap.
 * Reference items are high-value, high-demand resources and incur steep fines.
 */
@Component
@Slf4j
public class ReferenceFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAILY_RATE = new BigDecimal("1.00");

    @Override
    public BigDecimal calculateFine(Loan loan, LocalDate returnDate) {
        LocalDate dueDate = loan.getDueDate().toLocalDate();
        LocalDate effectiveReturn = (returnDate != null) ? returnDate : LocalDate.now();

        if (!effectiveReturn.isAfter(dueDate)) {
            log.debug("Reference loan {} returned on time (due={}, returned={}); no fine",
                    loan.getId(), dueDate, effectiveReturn);
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, effectiveReturn);
        BigDecimal fine = DAILY_RATE.multiply(BigDecimal.valueOf(daysOverdue));

        log.warn("Reference loan {} incurs fine of ${} ({} days overdue at ${}/day — no cap)",
                loan.getId(), fine, daysOverdue, DAILY_RATE);
        return fine;
    }
}
