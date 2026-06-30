package com.example.library.pattern.observer;

/**
 * Published by the overdue-loan scheduler when an active loan has passed its due date.
 * Triggers notification delivery to the member.
 */
public class OverdueNoticeEvent extends LibraryEvent {

    private final Long loanId;
    private final Long memberId;
    private final int daysOverdue;

    public OverdueNoticeEvent(Object source,
                              Long loanId,
                              Long memberId,
                              int daysOverdue) {
        super(source);
        this.loanId = loanId;
        this.memberId = memberId;
        this.daysOverdue = daysOverdue;
    }

    public Long getLoanId()    { return loanId; }
    public Long getMemberId()  { return memberId; }
    public int getDaysOverdue(){ return daysOverdue; }
}
