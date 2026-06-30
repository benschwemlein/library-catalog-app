package com.example.library.pattern.template;

import com.example.library.pattern.builder.ReportCriteria;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public abstract class AbstractReportGenerator<T> {

    public final Report generate(ReportCriteria criteria) {
        validateCriteria(criteria);
        List<T> data = fetchData(criteria);
        ReportData reportData = processData(data);
        Report report = formatReport(reportData, criteria);
        logReportGeneration(criteria, report);
        return report;
    }

    protected abstract void validateCriteria(ReportCriteria criteria);

    protected abstract List<T> fetchData(ReportCriteria criteria);

    protected abstract ReportData processData(List<T> data);

    protected abstract Report formatReport(ReportData reportData, ReportCriteria criteria);

    protected void logReportGeneration(ReportCriteria criteria, Report report) {
        log.info("Generated report '{}' at {} with {} sections",
            report.getTitle(),
            report.getGeneratedAt(),
            report.getSections() != null ? report.getSections().size() : 0);
    }
}
