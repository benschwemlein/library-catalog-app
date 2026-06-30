package com.example.library.pattern.template;

import com.example.library.entity.Fine;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.pattern.builder.ReportCriteria;
import com.example.library.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OverdueReportGenerator extends AbstractReportGenerator<Loan> {

    @Autowired
    private LoanRepository loanRepository;

    @Override
    protected void validateCriteria(ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("ReportCriteria cannot be null");
        }
    }

    @Override
    protected List<Loan> fetchData(ReportCriteria criteria) {
        List<Loan> allOverdue = loanRepository.findByDueDateBeforeAndStatus(LocalDateTime.now(), LoanStatus.OVERDUE);
        return allOverdue.stream()
            .filter(loan -> {
                if (criteria.getDateFrom() != null && loan.getCheckoutDate() != null
                        && loan.getCheckoutDate().toLocalDate().isBefore(criteria.getDateFrom())) {
                    return false;
                }
                if (criteria.getDateTo() != null && loan.getCheckoutDate() != null
                        && loan.getCheckoutDate().toLocalDate().isAfter(criteria.getDateTo())) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    @Override
    protected ReportData processData(List<Loan> data) {
        int totalCount = data.size();

        BigDecimal totalFineEstimate = data.stream()
            .map(loan -> {
                Fine fine = loan.getFine();
                return fine != null && fine.getAmount() != null ? fine.getAmount() : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> countByBranch = data.stream()
            .collect(Collectors.groupingBy(
                loan -> loan.getBranch() != null ? loan.getBranch().getName() : "Unknown",
                Collectors.counting()
            ));

        Map<String, Object> aggregates = new HashMap<>();
        aggregates.put("totalCount", totalCount);
        aggregates.put("estimatedFines", totalFineEstimate);
        aggregates.put("byBranch", countByBranch);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Loan loan : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("loanId", loan.getId());
            row.put("memberNumber", loan.getMember() != null ? loan.getMember().getMembershipNumber() : "N/A");
            row.put("bookTitle", loan.getBookCopy() != null && loan.getBookCopy().getBook() != null
                ? loan.getBookCopy().getBook().getTitle() : "N/A");
            row.put("dueDate", loan.getDueDate());
            row.put("branchName", loan.getBranch() != null ? loan.getBranch().getName() : "N/A");
            Fine fine = loan.getFine();
            row.put("fineEstimate", fine != null && fine.getAmount() != null ? fine.getAmount() : BigDecimal.ZERO);
            rows.add(row);
        }

        return new ReportData(new HashMap<>(), rows, aggregates);
    }

    @Override
    protected Report formatReport(ReportData reportData, ReportCriteria criteria) {
        Map<String, Object> aggregates = reportData.getAggregates();
        int totalCount = (int) aggregates.getOrDefault("totalCount", 0);
        BigDecimal estimatedFines = (BigDecimal) aggregates.getOrDefault("estimatedFines", BigDecimal.ZERO);

        List<Map<String, Object>> summaryData = new ArrayList<>();
        summaryData.add(aggregates);

        ReportSection summarySection = new ReportSection(
            "Summary",
            summaryData,
            "Total overdue loans: " + totalCount + ", Estimated fines: $" + estimatedFines
        );

        ReportSection detailSection = new ReportSection(
            "Overdue Loans Detail",
            reportData.getRows(),
            "Listing " + reportData.getRows().size() + " overdue loans"
        );

        List<ReportSection> sections = new ArrayList<>();
        sections.add(summarySection);
        sections.add(detailSection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportType", "OVERDUE");
        metadata.put("generatedBy", "OverdueReportGenerator");

        return new Report("Overdue Loans Report", LocalDateTime.now(), criteria, sections, metadata);
    }
}
