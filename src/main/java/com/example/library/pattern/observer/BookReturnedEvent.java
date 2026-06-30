package com.example.library.pattern.observer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published when a member returns a borrowed book.
 */
public class BookReturnedEvent extends LibraryEvent {

    private final Long loanId;
    private final Long memberId;
    private final Long bookId;
    private final LocalDateTime returnDate;
    private final boolean wasOverdue;
    private final boolean fineIssued;
    private final BigDecimal fineAmount;

    public BookReturnedEvent(Object source,
                             Long loanId,
                             Long memberId,
                             Long bookId,
                             LocalDateTime returnDate,
                             boolean wasOverdue,
                             boolean fineIssued,
                             BigDecimal fineAmount) {
        super(source);
        this.loanId = loanId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.returnDate = returnDate;
        this.wasOverdue = wasOverdue;
        this.fineIssued = fineIssued;
        this.fineAmount = fineAmount != null ? fineAmount : BigDecimal.ZERO;
    }

    public Long getLoanId()            { return loanId; }
    public Long getMemberId()          { return memberId; }
    public Long getBookId()            { return bookId; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public boolean isWasOverdue()      { return wasOverdue; }
    public boolean isFineIssued()      { return fineIssued; }
    public BigDecimal getFineAmount()  { return fineAmount; }
}
