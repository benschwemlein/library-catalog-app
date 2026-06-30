package com.example.library.pattern.specification;

import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class IsEligibleToBorrowSpecification extends AbstractSpecification<Member> {

    private static final BigDecimal FINE_THRESHOLD = new BigDecimal("10.00");

    @Autowired
    private LoanRepository loanRepository;

    @Override
    public boolean isSatisfiedBy(Member member) {
        if (!member.isActive()) {
            return false;
        }
        if (member.getFineBalance() != null && member.getFineBalance().compareTo(FINE_THRESHOLD) > 0) {
            return false;
        }
        int activeCount = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE).size();
        int overdueCount = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE).size();
        int totalLoans = activeCount + overdueCount;
        int limit = getLimitForTier(member.getMembershipTier());
        return totalLoans < limit;
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
