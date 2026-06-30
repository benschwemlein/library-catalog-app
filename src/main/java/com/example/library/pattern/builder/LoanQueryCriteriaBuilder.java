package com.example.library.pattern.builder;

import com.example.library.entity.LoanStatus;
import java.time.LocalDate;

public class LoanQueryCriteriaBuilder {
    Long memberId;
    Long branchId;
    LoanStatus status;
    LocalDate dueDateFrom;
    LocalDate dueDateTo;
    boolean overdueOnly = false;
    boolean renewedOnly = false;
    int page = 0;
    int size = 20;

    public LoanQueryCriteriaBuilder memberId(Long memberId) { this.memberId = memberId; return this; }
    public LoanQueryCriteriaBuilder branchId(Long branchId) { this.branchId = branchId; return this; }
    public LoanQueryCriteriaBuilder status(LoanStatus status) { this.status = status; return this; }
    public LoanQueryCriteriaBuilder dueDateFrom(LocalDate dueDateFrom) { this.dueDateFrom = dueDateFrom; return this; }
    public LoanQueryCriteriaBuilder dueDateTo(LocalDate dueDateTo) { this.dueDateTo = dueDateTo; return this; }
    public LoanQueryCriteriaBuilder overdueOnly(boolean overdueOnly) { this.overdueOnly = overdueOnly; return this; }
    public LoanQueryCriteriaBuilder renewedOnly(boolean renewedOnly) { this.renewedOnly = renewedOnly; return this; }
    public LoanQueryCriteriaBuilder page(int page) { this.page = page; return this; }
    public LoanQueryCriteriaBuilder size(int size) { this.size = size; return this; }

    public LoanQueryCriteria build() {
        if (overdueOnly && status != null && status != LoanStatus.OVERDUE) {
            throw new IllegalArgumentException("overdueOnly filter conflicts with non-OVERDUE status filter");
        }
        if (dueDateFrom != null && dueDateTo != null && dueDateFrom.isAfter(dueDateTo)) {
            throw new IllegalArgumentException("dueDateFrom cannot be after dueDateTo");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        return new LoanQueryCriteria(this);
    }
}
