package com.example.library.pattern.strategy;

import com.example.library.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FineCalculationStrategy {

    /**
     * Calculate the fine owed for a loan given the actual return date.
     *
     * @param loan       the loan being returned or assessed
     * @param returnDate the date the item is returned (or today if still outstanding)
     * @return the fine amount; never null, may be BigDecimal.ZERO
     */
    BigDecimal calculateFine(Loan loan, LocalDate returnDate);
}
