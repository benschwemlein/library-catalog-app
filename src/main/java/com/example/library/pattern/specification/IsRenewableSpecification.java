package com.example.library.pattern.specification;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import org.springframework.stereotype.Component;

@Component
public class IsRenewableSpecification extends AbstractSpecification<Loan> {

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        return loan.getStatus() == LoanStatus.ACTIVE
            && loan.getMember() != null
            && loan.getRenewalCount() < loan.getMember().getMembershipTier().getMaxRenewals();
    }
}
