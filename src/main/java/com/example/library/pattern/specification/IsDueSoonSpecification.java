package com.example.library.pattern.specification;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import java.time.LocalDateTime;

public class IsDueSoonSpecification extends AbstractSpecification<Loan> {

    private final int daysThreshold;

    public IsDueSoonSpecification(int daysThreshold) {
        this.daysThreshold = daysThreshold;
    }

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        return loan.getStatus() == LoanStatus.ACTIVE
            && loan.getDueDate() != null
            && loan.getDueDate().isBefore(LocalDateTime.now().plusDays(daysThreshold));
    }
}
