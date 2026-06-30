package com.example.library.exception;

public class MembershipExpiredException extends RuntimeException {
    public MembershipExpiredException(String message) {
        super(message);
    }

    public MembershipExpiredException(Long memberId) {
        super("Membership has expired for member id: " + memberId);
    }
}
