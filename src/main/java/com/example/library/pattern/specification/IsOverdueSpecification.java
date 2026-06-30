package com.example.library.pattern.specification;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class IsOverdueSpecification extends AbstractSpecification<Loan> {

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        return loan.getStatus() == LoanStatus.OVERDUE
            || (loan.getStatus() == LoanStatus.ACTIVE
                && loan.getDueDate() != null
                && loan.getDueDate().isBefore(LocalDateTime.now()));
    }
}
