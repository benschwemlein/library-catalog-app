package com.example.library.batch;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.LoanRepository;
import com.example.library.service.FineService;
import com.example.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes overdue loans: marks them OVERDUE, issues fines, and notifies members.
 *
 * Uses LoanRepository.findOverdueLoans(now) which queries:
 *   SELECT l FROM Loan l WHERE l.dueDate < :now AND l.status = 'ACTIVE'
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueBatchProcessor {

    private static final int CHUNK_SIZE = 100;

    private final BatchJobRepository batchJobRepository;
    private final LoanRepository loanRepository;
    private final FineService fineService;
    private final NotificationService notificationService;

    @Transactional
    public void process(BatchJob job) {
        log.info("Starting OVERDUE_PROCESSING job id={}", job.getId());

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        batchJobRepository.save(job);

        int processed = 0;
        int failed = 0;

        try {
            List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());
            log.info("Found {} overdue loans to process", overdueLoans.size());

            List<List<Loan>> chunks = partition(overdueLoans, CHUNK_SIZE);

            for (List<Loan> chunk : chunks) {
                for (Loan loan : chunk) {
                    try {
                        processOverdueLoan(loan);
                        processed++;
                    } catch (Exception e) {
                        log.error("Failed to process overdue loan id={}: {}", loan.getId(), e.getMessage(), e);
                        failed++;
                    }
                }

                // Persist progress after each chunk
                job.setRecordsProcessed(processed);
                job.setRecordsFailed(failed);
                batchJobRepository.save(job);
            }

            job.setStatus(failed > 0 && processed == 0 ? JobStatus.FAILED : JobStatus.COMPLETED);
            log.info("OVERDUE_PROCESSING job id={} completed: processed={} failed={}", job.getId(), processed, failed);

        } catch (Exception e) {
            log.error("OVERDUE_PROCESSING job id={} encountered a fatal error: {}", job.getId(), e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        job.setRecordsProcessed(processed);
        job.setRecordsFailed(failed);
        job.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(job);
    }

    private void processOverdueLoan(Loan loan) {
        log.debug("Processing overdue loan id={} memberId={} dueDate={}",
                loan.getId(), loan.getMember().getId(), loan.getDueDate());

        // Mark loan overdue
        loan.setStatus(LoanStatus.OVERDUE);
        loanRepository.save(loan);

        // Issue a fine (FineService returns null if amount <= 0, so guard before use)
        try {
            fineService.issueFine(loan);
        } catch (Exception e) {
            log.warn("Could not issue fine for loan id={}: {}", loan.getId(), e.getMessage());
        }

        // Send overdue notification to member
        String message = String.format(
                "Your loan of \"%s\" (due %s) is overdue. Please return it as soon as possible.",
                loan.getBookCopy().getBook().getTitle(),
                loan.getDueDate().toLocalDate());

        notificationService.sendNotification(
                loan.getMember().getId(),
                NotificationType.OVERDUE,
                message,
                NotificationChannel.EMAIL);
    }

    private <T> List<List<T>> partition(List<T> source, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            partitions.add(source.subList(i, Math.min(i + size, source.size())));
        }
        return partitions;
    }
}
