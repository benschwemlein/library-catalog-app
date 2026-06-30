package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Slf4j
@Component
public class AgeRestrictionHandler extends LoanEligibilityHandler {

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        String title = copy.getBook().getTitle();
        if (title != null && title.toUpperCase().contains("RESTRICTED")) {
            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            if (member.getJoinDate() != null && member.getJoinDate().isAfter(oneYearAgo)) {
                log.info("Restricted material access denied for member {} (joined {})", member.getId(), member.getJoinDate());
                return ValidationResult.invalid(
                    "Access to restricted material requires at least 1 year of membership",
                    "AgeRestrictionHandler"
                );
            }
        }
        return passToNext(request, member, copy);
    }
}
