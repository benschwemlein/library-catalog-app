package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.Member;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanEligibilityChain {

    @Autowired
    private MembershipActiveHandler membershipActiveHandler;

    @Autowired
    private MaxLoansHandler maxLoansHandler;

    @Autowired
    private UnpaidFinesHandler unpaidFinesHandler;

    @Autowired
    private CopyAvailableHandler copyAvailableHandler;

    @Autowired
    private BranchAccessHandler branchAccessHandler;

    @Autowired
    private AgeRestrictionHandler ageRestrictionHandler;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @PostConstruct
    public void init() {
        membershipActiveHandler
            .setNext(maxLoansHandler)
            .setNext(unpaidFinesHandler)
            .setNext(copyAvailableHandler)
            .setNext(branchAccessHandler)
            .setNext(ageRestrictionHandler);
        log.info("LoanEligibilityChain initialized with 6 handlers");
    }

    public ValidationResult validate(CheckoutRequest request, Member member, BookCopy copy) {
        log.debug("Starting loan eligibility validation for member {} and copy {}", member.getId(), copy.getId());
        ValidationResult result = membershipActiveHandler.handle(request, member, copy);
        if (result.isValid()) {
            log.debug("Loan eligibility validation passed for member {}", member.getId());
        } else {
            log.info("Loan eligibility validation failed: {} (handler: {})", result.getFailureReason(), result.getFailedHandler());
        }
        return result;
    }
}
