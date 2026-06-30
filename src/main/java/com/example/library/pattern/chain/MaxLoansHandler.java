package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MaxLoansHandler extends LoanEligibilityHandler {

    @Autowired
    private LoanRepository loanRepository;

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        int activeCount = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE).size();
        int overdueCount = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE).size();
        int totalLoans = activeCount + overdueCount;

        int limit = getLimitForTier(member.getMembershipTier());

        if (totalLoans >= limit) {
            return ValidationResult.invalid(
                "Maximum loan limit reached for " + member.getMembershipTier().name() + " membership",
                "MaxLoansHandler"
            );
        }
        return passToNext(request, member, copy);
    }

    private int getLimitForTier(MembershipTier tier) {
        switch (tier) {
            case PREMIUM: return 8;
            case STUDENT: return 3;
            case STANDARD:
            default: return 5;
        }
    }
}
