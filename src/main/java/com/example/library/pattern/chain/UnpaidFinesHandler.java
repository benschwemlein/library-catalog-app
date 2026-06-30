package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.Member;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class UnpaidFinesHandler extends LoanEligibilityHandler {

    private static final BigDecimal FINE_THRESHOLD = new BigDecimal("10.00");

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        if (member.getFineBalance() != null && member.getFineBalance().compareTo(FINE_THRESHOLD) > 0) {
            return ValidationResult.invalid("Unpaid fines balance exceeds $10.00", "UnpaidFinesHandler");
        }
        return passToNext(request, member, copy);
    }
}
