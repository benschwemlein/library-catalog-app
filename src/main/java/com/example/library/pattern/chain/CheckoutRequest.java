package com.example.library.pattern.chain;

public class CheckoutRequest {
    private final Long memberId;
    private final Long bookCopyId;
    private final Long branchId;

    public CheckoutRequest(Long memberId, Long bookCopyId, Long branchId) {
        this.memberId = memberId;
        this.bookCopyId = bookCopyId;
        this.branchId = branchId;
    }

    public Long getMemberId() { return memberId; }
    public Long getBookCopyId() { return bookCopyId; }
    public Long getBranchId() { return branchId; }
}
