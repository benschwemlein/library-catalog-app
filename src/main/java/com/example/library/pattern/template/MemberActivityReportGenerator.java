package com.example.library.pattern.template;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.pattern.builder.ReportCriteria;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
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
public class MemberActivityReportGenerator extends AbstractReportGenerator<Loan> {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    protected void validateCriteria(ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("ReportCriteria cannot be null");
        }
    }

    @Override
    protected List<Loan> fetchData(ReportCriteria criteria) {
        List<Loan> loans;
        if (criteria.getMemberId() != null) {
            loans = loanRepository.findByMember_Id(criteria.getMemberId());
        } else {
            loans = loanRepository.findAll();
        }
        return loans.stream()
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
        Map<Long, List<Loan>> byMember = data.stream()
            .filter(loan -> loan.getMember() != null)
            .collect(Collectors.groupingBy(loan -> loan.getMember().getId()));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, List<Loan>> entry : byMember.entrySet()) {
            Long memberId = entry.getKey();
            List<Loan> memberLoans = entry.getValue();

            long totalCheckouts = memberLoans.size();
            long renewedLoans = memberLoans.stream().filter(l -> l.getRenewalCount() > 0).count();
            long overdueLoans = memberLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE)
                .count();

            String memberNumber = memberLoans.get(0).getMember().getMembershipNumber();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("memberId", memberId);
            row.put("memberNumber", memberNumber);
            row.put("totalCheckouts", totalCheckouts);
            row.put("renewedLoans", renewedLoans);
            row.put("overdueLoans", overdueLoans);
            rows.add(row);
        }

        Map<String, Object> aggregates = new HashMap<>();
        aggregates.put("totalLoansAnalyzed", data.size());
        aggregates.put("uniqueMembers", byMember.size());

        return new ReportData(new HashMap<>(), rows, aggregates);
    }

    @Override
    protected Report formatReport(ReportData reportData, ReportCriteria criteria) {
        Map<String, Object> aggregates = reportData.getAggregates();

        ReportSection activitySection = new ReportSection(
            "Member Activity",
            reportData.getRows(),
            "Activity for " + reportData.getRows().size() + " members"
        );

        List<ReportSection> sections = new ArrayList<>();
        sections.add(activitySection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportType", "MEMBER_ACTIVITY");
        metadata.put("totalLoansAnalyzed", aggregates.getOrDefault("totalLoansAnalyzed", 0));
        metadata.put("generatedBy", "MemberActivityReportGenerator");

        return new Report("Member Activity Report", LocalDateTime.now(), criteria, sections, metadata);
    }
}
