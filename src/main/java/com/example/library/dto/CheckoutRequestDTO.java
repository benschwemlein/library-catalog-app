package com.example.library.dto;

public class CheckoutRequestDTO {
    private Long memberId;
    private Long bookCopyId;
    private String membershipNumber;
    private String copyBarcode;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getBookCopyId() { return bookCopyId; }
    public void setBookCopyId(Long bookCopyId) { this.bookCopyId = bookCopyId; }
    public String getMembershipNumber() { return membershipNumber; }
    public void setMembershipNumber(String membershipNumber) { this.membershipNumber = membershipNumber; }
    public String getCopyBarcode() { return copyBarcode; }
    public void setCopyBarcode(String copyBarcode) { this.copyBarcode = copyBarcode; }
}
