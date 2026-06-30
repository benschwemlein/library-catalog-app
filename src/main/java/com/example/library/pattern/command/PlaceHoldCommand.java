package com.example.library.pattern.command;

import com.example.library.entity.Book;
import com.example.library.entity.Hold;
import com.example.library.entity.HoldStatus;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.HoldRepository;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Command that places a hold (reservation) on a book for a member at a specified branch.
 *
 * <p>Prototype-scoped; set {@code bookId}, {@code memberId}, and {@code branchId}
 * before calling {@code execute()}.</p>
 */
@Component
@Scope("prototype")
@Slf4j
public class PlaceHoldCommand implements LibraryCommand {

    /** Default hold expiry: 7 days from when the hold becomes READY for pickup. */
    private static final int HOLD_EXPIRY_DAYS = 7;

    @Autowired
    private HoldRepository holdRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Setter private Long bookId;
    @Setter private Long memberId;
    @Setter private Long branchId;

    /** Populated after a successful execute(); used by undo(). */
    private Long holdId;

    @Override
    public CommandResult execute() {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

            LibraryBranch branch = libraryBranchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

            LocalDateTime now = LocalDateTime.now();

            Hold hold = Hold.builder()
                    .book(book)
                    .member(member)
                    .pickupBranch(branch)
                    .requestDate(now)
                    .expiryDate(now.plusDays(HOLD_EXPIRY_DAYS * 7L)) // overall request window (not pickup expiry)
                    .status(HoldStatus.PENDING)
                    .build();

            Hold saved = holdRepository.save(hold);
            this.holdId = saved.getId();

            log.info("PlaceHoldCommand executed: holdId={} bookId={} memberId={} branchId={}",
                    holdId, bookId, memberId, branchId);

            return CommandResult.success(
                    String.format("Hold placed successfully for book '%s' at branch '%s'",
                            book.getTitle(), branch.getName()),
                    holdId
            );
        } catch (Exception e) {
            log.error("PlaceHoldCommand failed: bookId={} memberId={} branchId={} error={}",
                    bookId, memberId, branchId, e.getMessage(), e);
            return CommandResult.failure("Place hold failed: " + e.getMessage());
        }
    }

    @Override
    public CommandResult undo() {
        if (holdId == null) {
            return CommandResult.failure("Cannot undo: no holdId recorded (was execute() called?)");
        }
        try {
            Hold hold = holdRepository.findById(holdId)
                    .orElseThrow(() -> new IllegalArgumentException("Hold not found: " + holdId));

            hold.setStatus(HoldStatus.CANCELLED);
            holdRepository.save(hold);

            log.info("PlaceHoldCommand undone: holdId={} status set to CANCELLED", holdId);

            return CommandResult.success("Hold cancelled successfully.", holdId);
        } catch (Exception e) {
            log.error("PlaceHoldCommand undo failed: holdId={} error={}", holdId, e.getMessage(), e);
            return CommandResult.failure("Undo hold failed: " + e.getMessage(), holdId);
        }
    }

    @Override
    public String getDescription() {
        return String.format("PlaceHold bookId=%d for memberId=%d at branchId=%d (holdId=%s)",
                bookId, memberId, branchId, holdId);
    }
}
