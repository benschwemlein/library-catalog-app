package com.example.library.pattern.observer;

/**
 * Published when a new library member account is created.
 */
public class MemberRegisteredEvent extends LibraryEvent {

    private final Long memberId;
    private final String email;
    private final String membershipNumber;

    public MemberRegisteredEvent(Object source,
                                 Long memberId,
                                 String email,
                                 String membershipNumber) {
        super(source);
        this.memberId = memberId;
        this.email = email;
        this.membershipNumber = membershipNumber;
    }

    public Long getMemberId()          { return memberId; }
    public String getEmail()           { return email; }
    public String getMembershipNumber(){ return membershipNumber; }
}
