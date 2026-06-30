package com.example.library.pattern.chain;

import com.example.library.entity.BookCopy;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BranchAccessHandler extends LoanEligibilityHandler {

    @Override
    public ValidationResult handle(CheckoutRequest request, Member member, BookCopy copy) {
        LibraryBranch branch = copy.getBranch();
        log.debug("Checking branch access for member {} at branch {}", member.getId(), branch != null ? branch.getId() : "unknown");
        if (isRestrictedBranch(branch)) {
            return ValidationResult.invalid("Member does not have access to this branch", "BranchAccessHandler");
        }
        return passToNext(request, member, copy);
    }

    public boolean isRestrictedBranch(LibraryBranch branch) {
        return false;
    }
}
