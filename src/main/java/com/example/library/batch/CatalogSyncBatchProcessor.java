package com.example.library.batch;

import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconciles book copy statuses against active loan records.
 *
 * Two discrepancy types are corrected:
 *   1. AVAILABLE copies that have an ACTIVE loan  -> set to CHECKED_OUT
 *   2. CHECKED_OUT copies that have no ACTIVE loan -> set to AVAILABLE
 *
 * Note: BookCopyRepository.findByStatus accepts a String because the repository
 * was defined that way; we pass CopyStatus.name() to match.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogSyncBatchProcessor {

    private final BatchJobRepository batchJobRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public void process(BatchJob job) {
        log.info("Starting CATALOG_SYNC job id={}", job.getId());

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        batchJobRepository.save(job);

        int fixedToCheckedOut = 0;
        int fixedToAvailable = 0;
        int failed = 0;

        try {
            // --- Pass 1: AVAILABLE copies that should be CHECKED_OUT ---
            List<BookCopy> availableCopies = bookCopyRepository.findByStatus(CopyStatus.AVAILABLE.name());
            log.info("Checking {} AVAILABLE copies for active loans", availableCopies.size());

            for (BookCopy copy : availableCopies) {
                try {
                    boolean hasActiveLoan = hasActiveLoan(copy.getId());
                    if (hasActiveLoan) {
                        log.warn("CATALOG_SYNC: copy id={} barcode={} is AVAILABLE but has an ACTIVE loan; correcting to CHECKED_OUT",
                                copy.getId(), copy.getBarcode());
                        copy.setStatus(CopyStatus.CHECKED_OUT);
                        bookCopyRepository.save(copy);
                        fixedToCheckedOut++;
                    }
                } catch (Exception e) {
                    log.error("Error reconciling AVAILABLE copy id={}: {}", copy.getId(), e.getMessage(), e);
                    failed++;
                }
            }

            // --- Pass 2: CHECKED_OUT copies that should be AVAILABLE ---
            List<BookCopy> checkedOutCopies = bookCopyRepository.findByStatus(CopyStatus.CHECKED_OUT.name());
            log.info("Checking {} CHECKED_OUT copies for missing active loans", checkedOutCopies.size());

            for (BookCopy copy : checkedOutCopies) {
                try {
                    boolean hasActiveLoan = hasActiveLoan(copy.getId());
                    if (!hasActiveLoan) {
                        log.warn("CATALOG_SYNC: copy id={} barcode={} is CHECKED_OUT but has no ACTIVE loan; correcting to AVAILABLE",
                                copy.getId(), copy.getBarcode());
                        copy.setStatus(CopyStatus.AVAILABLE);
                        bookCopyRepository.save(copy);
                        fixedToAvailable++;
                    }
                } catch (Exception e) {
                    log.error("Error reconciling CHECKED_OUT copy id={}: {}", copy.getId(), e.getMessage(), e);
                    failed++;
                }
            }

            int totalProcessed = fixedToCheckedOut + fixedToAvailable;
            int totalInspected = availableCopies.size() + checkedOutCopies.size();

            job.setStatus(JobStatus.COMPLETED);
            log.info("CATALOG_SYNC job id={} completed: inspected={} fixedToCheckedOut={} fixedToAvailable={} failed={}",
                    job.getId(), totalInspected, fixedToCheckedOut, fixedToAvailable, failed);

            // recordsProcessed = discrepancies corrected; recordsFailed = errors
            job.setRecordsProcessed(totalProcessed);
            job.setRecordsFailed(failed);

        } catch (Exception e) {
            log.error("CATALOG_SYNC job id={} encountered a fatal error: {}", job.getId(), e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        job.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(job);
    }

    /**
     * Returns true if there is at least one ACTIVE or OVERDUE loan for the given book copy.
     */
    private boolean hasActiveLoan(Long bookCopyId) {
        List<Loan> loans = loanRepository.findByBookCopy_Id(bookCopyId);
        return loans.stream()
                .anyMatch(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE);
    }
}
