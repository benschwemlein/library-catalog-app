package com.example.library.pattern.template;

import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.pattern.builder.ReportCriteria;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BranchStatisticsReportGenerator extends AbstractReportGenerator<BookCopy> {

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Override
    protected void validateCriteria(ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("ReportCriteria cannot be null");
        }
        if (criteria.getBranchId() == null) {
            throw new IllegalArgumentException("branchId is required for BranchStatisticsReport");
        }
    }

    @Override
    protected List<BookCopy> fetchData(ReportCriteria criteria) {
        return bookCopyRepository.findByBranch_Id(criteria.getBranchId());
    }

    @Override
    protected ReportData processData(List<BookCopy> data) {
        long totalCopies = data.size();

        Map<String, Long> statusCounts = data.stream()
            .collect(Collectors.groupingBy(
                copy -> copy.getStatus().name(),
                Collectors.counting()
            ));

        long availableCopies = statusCounts.getOrDefault(CopyStatus.AVAILABLE.name(), 0L);
        long checkedOutCopies = statusCounts.getOrDefault(CopyStatus.CHECKED_OUT.name(), 0L);
        double utilizationRate = totalCopies > 0 ? (double) checkedOutCopies / totalCopies * 100.0 : 0.0;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("status", entry.getKey());
            row.put("count", entry.getValue());
            row.put("percentage", totalCopies > 0 ? (double) entry.getValue() / totalCopies * 100.0 : 0.0);
            rows.add(row);
        }

        Map<String, Object> aggregates = new HashMap<>();
        aggregates.put("totalCopies", totalCopies);
        aggregates.put("availableCopies", availableCopies);
        aggregates.put("checkedOutCopies", checkedOutCopies);
        aggregates.put("utilizationRate", String.format("%.1f%%", utilizationRate));
        aggregates.put("utilizationRateDouble", utilizationRate);

        return new ReportData(new HashMap<>(), rows, aggregates);
    }

    @Override
    protected Report formatReport(ReportData reportData, ReportCriteria criteria) {
        Map<String, Object> aggregates = reportData.getAggregates();
        long totalCopies = (long) aggregates.getOrDefault("totalCopies", 0L);
        double utilizationRate = (double) aggregates.getOrDefault("utilizationRateDouble", 0.0);

        ReportSection statsSection = new ReportSection(
            "Branch Copy Statistics",
            reportData.getRows(),
            String.format("Branch has %d total copies, %.1f%% utilization", totalCopies, utilizationRate)
        );

        List<ReportSection> sections = new ArrayList<>();
        sections.add(statsSection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("branchId", criteria.getBranchId());
        metadata.put("reportType", "BRANCH_STATS");
        metadata.put("generatedBy", "BranchStatisticsReportGenerator");

        return new Report("Branch Statistics Report", LocalDateTime.now(), criteria, sections, metadata);
    }
}
