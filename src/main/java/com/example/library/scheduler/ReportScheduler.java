package com.example.library.scheduler;

import com.example.library.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

    @Autowired
    private ReportService reportService;

    /**
     * Runs at 6:00 AM every Monday to generate the weekly operational reports.
     */
    @Scheduled(cron = "0 0 6 * * MON")
    public void generateWeeklyReports() {
        log.info("Starting weekly report generation at {}", LocalDateTime.now());

        try {
            var mostBorrowed = reportService.getMostBorrowedBooks(10);
            log.info("Most-borrowed report generated. Top {} books retrieved.", mostBorrowed.size());
        } catch (Exception e) {
            log.error("Failed to generate most-borrowed report: {}", e.getMessage(), e);
        }

        try {
            var overdueReport = reportService.getOverdueReport();
            log.info("Overdue loan report generated. {} overdue loans found.", overdueReport.size());
        } catch (Exception e) {
            log.error("Failed to generate overdue report: {}", e.getMessage(), e);
        }

        log.info("Weekly report generation complete at {}", LocalDateTime.now());
    }
}
