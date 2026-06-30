package com.example.library.dto;

public class BookCopyTransferDTO {
    private Long targetBranchId;
    private String reason;

    public Long getTargetBranchId() { return targetBranchId; }
    public void setTargetBranchId(Long targetBranchId) { this.targetBranchId = targetBranchId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
