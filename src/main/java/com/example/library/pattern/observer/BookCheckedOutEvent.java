package com.example.library.pattern.observer;

import java.time.LocalDateTime;

/**
 * Published when a member checks out a book copy.
 */
public class BookCheckedOutEvent extends LibraryEvent {

    private final Long loanId;
    private final Long memberId;
    private final Long bookId;
    private final Long branchId;
    private final LocalDateTime dueDate;

    public BookCheckedOutEvent(Object source,
                               Long loanId,
                               Long memberId,
                               Long bookId,
                               Long branchId,
                               LocalDateTime dueDate) {
        super(source);
        this.loanId = loanId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.branchId = branchId;
        this.dueDate = dueDate;
    }

    public Long getLoanId()   { return loanId; }
    public Long getMemberId() { return memberId; }
    public Long getBookId()   { return bookId; }
    public Long getBranchId() { return branchId; }
    public LocalDateTime getDueDate() { return dueDate; }
}
