package com.example.library.pattern.builder;

import java.time.LocalDate;

public class ReportCriteria {
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final Long branchId;
    private final Long memberId;
    private final String reportType;
    private final boolean includeCharts;
    private final String groupBy;
    private final int maxResults;

    ReportCriteria(ReportCriteriaBuilder builder) {
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
        this.branchId = builder.branchId;
        this.memberId = builder.memberId;
        this.reportType = builder.reportType;
        this.includeCharts = builder.includeCharts;
        this.groupBy = builder.groupBy;
        this.maxResults = builder.maxResults;
    }

    public static ReportCriteriaBuilder builder() {
        return new ReportCriteriaBuilder();
    }

    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public Long getBranchId() { return branchId; }
    public Long getMemberId() { return memberId; }
    public String getReportType() { return reportType; }
    public boolean isIncludeCharts() { return includeCharts; }
    public String getGroupBy() { return groupBy; }
    public int getMaxResults() { return maxResults; }
}
