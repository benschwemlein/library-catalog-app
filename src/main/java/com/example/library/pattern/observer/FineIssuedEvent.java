package com.example.library.pattern.observer;

import java.math.BigDecimal;

/**
 * Published when a fine is assessed against a member's account.
 */
public class FineIssuedEvent extends LibraryEvent {

    private final Long fineId;
    private final Long memberId;
    private final Long loanId;
    private final BigDecimal amount;
    private final String reason;

    public FineIssuedEvent(Object source,
                           Long fineId,
                           Long memberId,
                           Long loanId,
                           BigDecimal amount,
                           String reason) {
        super(source);
        this.fineId = fineId;
        this.memberId = memberId;
        this.loanId = loanId;
        this.amount = amount;
        this.reason = reason;
    }

    public Long getFineId()    { return fineId; }
    public Long getMemberId()  { return memberId; }
    public Long getLoanId()    { return loanId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason()  { return reason; }
}
