package com.example.library.pattern.template;

import com.example.library.entity.Fine;
import com.example.library.pattern.builder.ReportCriteria;
import com.example.library.repository.FineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FineCollectionReportGenerator extends AbstractReportGenerator<Fine> {

    @Autowired
    private FineRepository fineRepository;

    @Override
    protected void validateCriteria(ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("ReportCriteria cannot be null");
        }
    }

    @Override
    protected List<Fine> fetchData(ReportCriteria criteria) {
        return fineRepository.findAll().stream()
            .filter(fine -> {
                if (criteria.getDateFrom() != null && fine.getIssuedDate() != null
                        && fine.getIssuedDate().toLocalDate().isBefore(criteria.getDateFrom())) {
                    return false;
                }
                if (criteria.getDateTo() != null && fine.getIssuedDate() != null
                        && fine.getIssuedDate().toLocalDate().isAfter(criteria.getDateTo())) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    @Override
    protected ReportData processData(List<Fine> data) {
        BigDecimal totalCollected = data.stream()
            .filter(f -> f.getPaidDate() != null && !f.isWaived())
            .map(Fine::getAmount)
            .filter(amt -> amt != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWaived = data.stream()
            .filter(Fine::isWaived)
            .map(Fine::getAmount)
            .filter(amt -> amt != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstanding = data.stream()
            .filter(f -> f.getPaidDate() == null && !f.isWaived())
            .map(Fine::getAmount)
            .filter(amt -> amt != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalFines = data.size();
        BigDecimal totalAmount = data.stream()
            .map(Fine::getAmount)
            .filter(amt -> amt != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = totalFines > 0
            ? totalAmount.divide(BigDecimal.valueOf(totalFines), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Fine fine : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("fineId", fine.getId());
            row.put("memberId", fine.getMember() != null ? fine.getMember().getId() : null);
            row.put("amount", fine.getAmount());
            row.put("reason", fine.getReason());
            row.put("issuedDate", fine.getIssuedDate());
            row.put("paid", fine.getPaidDate() != null);
            row.put("waived", fine.isWaived());
            rows.add(row);
        }

        Map<String, Object> aggregates = new HashMap<>();
        aggregates.put("totalCollected", totalCollected);
        aggregates.put("totalWaived", totalWaived);
        aggregates.put("outstanding", outstanding);
        aggregates.put("totalFines", totalFines);
        aggregates.put("averageAmount", averageAmount);

        return new ReportData(new HashMap<>(), rows, aggregates);
    }

    @Override
    protected Report formatReport(ReportData reportData, ReportCriteria criteria) {
        Map<String, Object> aggregates = reportData.getAggregates();
        BigDecimal totalCollected = (BigDecimal) aggregates.getOrDefault("totalCollected", BigDecimal.ZERO);
        BigDecimal outstanding = (BigDecimal) aggregates.getOrDefault("outstanding", BigDecimal.ZERO);

        ReportSection fineSection = new ReportSection(
            "Fine Collection Summary",
            reportData.getRows(),
            String.format("Total collected: $%s, Outstanding: $%s", totalCollected, outstanding)
        );

        List<ReportSection> sections = new ArrayList<>();
        sections.add(fineSection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportType", "FINE_COLLECTION");
        metadata.put("generatedBy", "FineCollectionReportGenerator");

        return new Report("Fine Collection Report", LocalDateTime.now(), criteria, sections, metadata);
    }
}
