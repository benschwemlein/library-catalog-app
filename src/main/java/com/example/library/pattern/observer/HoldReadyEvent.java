package com.example.library.pattern.observer;

import java.time.LocalDateTime;

/**
 * Published when a held book becomes available for pickup at the member's chosen branch.
 */
public class HoldReadyEvent extends LibraryEvent {

    private final Long holdId;
    private final Long memberId;
    private final Long bookId;
    private final Long pickupBranchId;
    private final LocalDateTime expiryDate;

    public HoldReadyEvent(Object source,
                          Long holdId,
                          Long memberId,
                          Long bookId,
                          Long pickupBranchId,
                          LocalDateTime expiryDate) {
        super(source);
        this.holdId = holdId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.pickupBranchId = pickupBranchId;
        this.expiryDate = expiryDate;
    }

    public Long getHoldId()          { return holdId; }
    public Long getMemberId()        { return memberId; }
    public Long getBookId()          { return bookId; }
    public Long getPickupBranchId()  { return pickupBranchId; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
}
