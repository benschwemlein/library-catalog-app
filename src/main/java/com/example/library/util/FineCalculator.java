package com.example.library.util;

import com.example.library.entity.Loan;
import com.example.library.entity.MembershipTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class FineCalculator {

    private static final BigDecimal STUDENT_MAX_FINE = new BigDecimal("25.00");
    private static final BigDecimal STANDARD_MAX_FINE = new BigDecimal("50.00");

    /**
     * Calculates the fine amount based on how overdue the loan is.
     *
     * @param dueDate    the date the loan was due
     * @param returnDate the actual return date
     * @param tier       the member's membership tier
     * @return the calculated fine amount, capped per tier rules
     */
    public BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnDate, MembershipTier tier) {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = tier.getDailyFineRate();
        BigDecimal fineAmount = dailyRate.multiply(BigDecimal.valueOf(daysOverdue));

        return switch (tier) {
            case STUDENT -> fineAmount.min(STUDENT_MAX_FINE);
            case STANDARD -> fineAmount.min(STANDARD_MAX_FINE);
            case PREMIUM -> fineAmount; // No cap for premium members
        };
    }

    /**
     * Checks whether a loan is currently overdue.
     *
     * @param loan the loan to check
     * @return true if the loan is active and past its due date
     */
    public boolean isOverdue(Loan loan) {
        if (loan == null || loan.getDueDate() == null) {
            return false;
        }
        return loan.getReturnDate() == null && LocalDateTime.now().isAfter(loan.getDueDate());
    }

    /**
     * Returns the number of days a loan is overdue. Returns 0 if not overdue.
     *
     * @param loan the loan to check
     * @return number of days overdue, or 0 if not overdue
     */
    public long getDaysOverdue(Loan loan) {
        if (!isOverdue(loan)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
    }
}
