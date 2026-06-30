package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.Member;

public abstract class LoanEligibilityHandler {

    protected LoanEligibilityHandler next;

    public LoanEligibilityHandler setNext(LoanEligibilityHandler handler) {
        this.next = handler;
        return handler;
    }

    public abstract ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy);

    protected ValidationResult passToNext(CheckoutRequest request, Member member, BookCopy copy) {
        if (next != null) {
            return next.handle(request, member, copy);
        }
        return ValidationResult.valid();
    }
}
