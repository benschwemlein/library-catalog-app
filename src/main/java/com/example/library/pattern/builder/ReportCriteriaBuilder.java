package com.example.library.pattern.builder;

import java.time.LocalDate;

public class ReportCriteriaBuilder {
    LocalDate dateFrom;
    LocalDate dateTo;
    Long branchId;
    Long memberId;
    String reportType;
    boolean includeCharts = false;
    String groupBy;
    int maxResults = 50;

    public ReportCriteriaBuilder dateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; return this; }
    public ReportCriteriaBuilder dateTo(LocalDate dateTo) { this.dateTo = dateTo; return this; }
    public ReportCriteriaBuilder branchId(Long branchId) { this.branchId = branchId; return this; }
    public ReportCriteriaBuilder memberId(Long memberId) { this.memberId = memberId; return this; }
    public ReportCriteriaBuilder reportType(String reportType) { this.reportType = reportType; return this; }
    public ReportCriteriaBuilder includeCharts(boolean includeCharts) { this.includeCharts = includeCharts; return this; }
    public ReportCriteriaBuilder groupBy(String groupBy) { this.groupBy = groupBy; return this; }
    public ReportCriteriaBuilder maxResults(int maxResults) { this.maxResults = maxResults; return this; }

    public ReportCriteria build() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be positive");
        }
        if (maxResults > 1000) {
            maxResults = 1000;
        }
        return new ReportCriteria(this);
    }
}
