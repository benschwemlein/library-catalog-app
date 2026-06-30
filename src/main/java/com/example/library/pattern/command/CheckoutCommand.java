package com.example.library.pattern.command;

import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.LibraryBranch;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Command that checks out a book copy to a member.
 *
 * <p>Prototype-scoped so each checkout operation gets its own command instance
 * with isolated field state. Set fields via setters before calling {@code execute()}.</p>
 */
@Component
@Scope("prototype")
@Slf4j
public class CheckoutCommand implements LibraryCommand {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Setter private Long memberId;
    @Setter private Long bookCopyId;
    @Setter private Long branchId;

    /** Populated after a successful execute(); used by undo(). */
    private Long loanId;

    @Override
    public CommandResult execute() {
        try {
            BookCopy copy = bookCopyRepository.findById(bookCopyId)
                    .orElseThrow(() -> new IllegalArgumentException("BookCopy not found: " + bookCopyId));

            if (copy.getStatus() != CopyStatus.AVAILABLE) {
                return CommandResult.failure("BookCopy " + bookCopyId + " is not available (status=" + copy.getStatus() + ")");
            }

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

            LibraryBranch branch = libraryBranchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

            // Determine loan period from the member's tier
            int loanDays = member.getMembershipTier().getDaysLoanPeriod();
            LocalDateTime now = LocalDateTime.now();

            Loan loan = Loan.builder()
                    .bookCopy(copy)
                    .member(member)
                    .branch(branch)
                    .checkoutDate(now)
                    .dueDate(now.plusDays(loanDays))
                    .renewalCount(0)
                    .status(LoanStatus.ACTIVE)
                    .build();

            // Mark copy as checked out
            copy.setStatus(CopyStatus.CHECKED_OUT);
            bookCopyRepository.save(copy);

            Loan saved = loanRepository.save(loan);
            this.loanId = saved.getId();

            log.info("CheckoutCommand executed: loanId={} memberId={} copyId={} dueDate={}",
                    loanId, memberId, bookCopyId, saved.getDueDate());

            return CommandResult.success(
                    "Book checked out successfully. Due date: " + saved.getDueDate().toLocalDate(),
                    loanId
            );
        } catch (Exception e) {
            log.error("CheckoutCommand failed: memberId={} copyId={} error={}", memberId, bookCopyId, e.getMessage(), e);
            return CommandResult.failure("Checkout failed: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (loanId == null) {
            return CommandResult.failure("Cannot undo: no loanId recorded (was execute() called?)");
        }
        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

            BookCopy copy = loan.getBookCopy();
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);

            loan.setStatus(LoanStatus.RETURNED);
            loan.setReturnDate(LocalDateTime.now());
            loanRepository.save(loan);

            log.info("CheckoutCommand undone: loanId={} copyId={} status restored to AVAILABLE", loanId, copy.getId());

            return CommandResult.success("Checkout reversed. Copy marked as available.", loanId);
        } catch (Exception e) {
            log.error("CheckoutCommand undo failed: loanId={} error={}", loanId, e.getMessage(), e);
            return CommandResult.failure("Undo checkout failed: " + e.getMessage(), loanId);
        }
    }

    @Override
    public String getDescription() {
        return String.format("Checkout copyId=%d to memberId=%d at branchId=%d (loanId=%s)",
                bookCopyId, memberId, branchId, loanId);
    }
}
