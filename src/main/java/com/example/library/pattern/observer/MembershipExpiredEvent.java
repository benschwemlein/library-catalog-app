package com.example.library.pattern.observer;

import java.time.LocalDate;

/**
 * Published when a member's library membership passes its expiry date.
 */
public class MembershipExpiredEvent extends LibraryEvent {

    private final Long memberId;
    private final LocalDate expiryDate;

    public MembershipExpiredEvent(Object source, Long memberId, LocalDate expiryDate) {
        super(source);
        this.memberId = memberId;
        this.expiryDate = expiryDate;
    }

    public Long getMemberId()       { return memberId; }
    public LocalDate getExpiryDate(){ return expiryDate; }
}
