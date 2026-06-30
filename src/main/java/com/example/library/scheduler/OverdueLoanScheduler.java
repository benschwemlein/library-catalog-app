package com.example.library.scheduler;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.LoanRepository;
import com.example.library.service.FineService;
import com.example.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OverdueLoanScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueLoanScheduler.class);

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineService fineService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Runs at 8:00 AM every day to identify overdue loans, issue fines, and notify members.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void processOverdueLoans() {
        log.info("Starting overdue loan processing at {}", LocalDateTime.now());

        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());
        int finesIssued = 0;
        int notificationsSent = 0;

        for (Loan loan : overdueLoans) {
            try {
                // Issue a fine if one has not already been issued for this loan
                com.example.library.entity.Fine fine = fineService.issueFine(loan);
                if (fine != null) {
                    finesIssued++;
                }

                // Send overdue notification to the member
                notificationService.sendNotification(
                        loan.getMember().getId(),
                        NotificationType.OVERDUE,
                        "Your loan for \"" + loan.getBookCopy().getBook().getTitle()
                                + "\" is overdue. Please return it as soon as possible.",
                        NotificationChannel.EMAIL);
                notificationsSent++;
            } catch (Exception e) {
                log.error("Error processing overdue loan id={}: {}", loan.getId(), e.getMessage(), e);
            }
        }

        log.info("Overdue loan processing complete. Loans processed: {}, Fines issued: {}, Notifications sent: {}",
                overdueLoans.size(), finesIssued, notificationsSent);
    }

    /**
     * Runs at 8:30 AM every day to remind members of loans due within 2 days.
     */
    @Scheduled(cron = "0 30 8 * * *")
    public void sendDueSoonReminders() {
        log.info("Sending due-soon reminders at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        // Fetch loans that are active and due before two days from now (but not yet overdue)
        List<Loan> dueSoonLoans = loanRepository.findByDueDateBeforeAndStatus(twoDaysFromNow, LoanStatus.ACTIVE);
        int remindersSent = 0;

        for (Loan loan : dueSoonLoans) {
            // Skip loans that are already returned
            if (loan.getReturnDate() != null) {
                continue;
            }
            try {
                notificationService.sendNotification(
                        loan.getMember().getId(),
                        NotificationType.DUE_SOON,
                        "Reminder: \"" + loan.getBookCopy().getBook().getTitle()
                                + "\" is due on " + loan.getDueDate().toLocalDate() + ".",
                        NotificationChannel.EMAIL);
                remindersSent++;
            } catch (Exception e) {
                log.error("Error sending due-soon reminder for loan id={}: {}", loan.getId(), e.getMessage(), e);
            }
        }

        log.info("Due-soon reminders complete. Reminders sent: {}", remindersSent);
    }
}
