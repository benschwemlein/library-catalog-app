package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class CopyAvailableHandler extends LoanEligibilityHandler {

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            return ValidationResult.invalid(
                "Book copy is not available (status: " + copy.getStatus() + ")",
                "CopyAvailableHandler"
            );
        }
        return passToNext(request, member, copy);
    }
}
