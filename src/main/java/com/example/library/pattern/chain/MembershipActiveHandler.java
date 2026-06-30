package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.Member;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class MembershipActiveHandler extends LoanEligibilityHandler {

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        if (!member.isActive() || member.getExpiryDate() == null || !member.getExpiryDate().isAfter(LocalDate.now())) {
            return ValidationResult.invalid("Membership is expired or inactive", "MembershipActiveHandler");
        }
        return passToNext(request, member, copy);
    }
}
