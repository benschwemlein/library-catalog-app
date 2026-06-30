package com.example.library.pattern.builder;

import com.example.library.entity.LoanStatus;
import java.time.LocalDate;

public class LoanQueryCriteria {
    private final Long memberId;
    private final Long branchId;
    private final LoanStatus status;
    private final LocalDate dueDateFrom;
    private final LocalDate dueDateTo;
    private final boolean overdueOnly;
    private final boolean renewedOnly;
    private final int page;
    private final int size;

    LoanQueryCriteria(LoanQueryCriteriaBuilder builder) {
        this.memberId = builder.memberId;
        this.branchId = builder.branchId;
        this.status = builder.status;
        this.dueDateFrom = builder.dueDateFrom;
        this.dueDateTo = builder.dueDateTo;
        this.overdueOnly = builder.overdueOnly;
        this.renewedOnly = builder.renewedOnly;
        this.page = builder.page;
        this.size = builder.size;
    }

    public static LoanQueryCriteriaBuilder builder() {
        return new LoanQueryCriteriaBuilder();
    }

    public Long getMemberId() { return memberId; }
    public Long getBranchId() { return branchId; }
    public LoanStatus getStatus() { return status; }
    public LocalDate getDueDateFrom() { return dueDateFrom; }
    public LocalDate getDueDateTo() { return dueDateTo; }
    public boolean isOverdueOnly() { return overdueOnly; }
    public boolean isRenewedOnly() { return renewedOnly; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
