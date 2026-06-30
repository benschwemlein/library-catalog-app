package com.example.library.batch;

import com.example.library.entity.Hold;
import com.example.library.entity.HoldStatus;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.HoldRepository;
import com.example.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Processes expired holds: marks READY holds past their expiry date as EXPIRED
 * and notifies the member.
 *
 * Uses HoldRepository.findExpiredHolds(now) which queries:
 *   SELECT h FROM Hold h WHERE h.expiryDate < :now AND h.status IN ('PENDING', 'READY')
 * We further filter to only READY holds here, since PENDING holds that expire
 * without ever becoming READY should also be expired.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryBatchProcessor {

    private final BatchJobRepository batchJobRepository;
    private final HoldRepository holdRepository;
    private final NotificationService notificationService;

    @Transactional
    public void process(BatchJob job) {
        log.info("Starting HOLD_EXPIRY job id={}", job.getId());

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        batchJobRepository.save(job);

        int processed = 0;
        int failed = 0;

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Hold> expiredHolds = holdRepository.findExpiredHolds(now);
            log.info("Found {} expired holds to process", expiredHolds.size());

            for (Hold hold : expiredHolds) {
                try {
                    processExpiredHold(hold);
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to process expired hold id={}: {}", hold.getId(), e.getMessage(), e);
                    failed++;
                }

                // Save progress every 50 records
                if ((processed + failed) % 50 == 0) {
                    job.setRecordsProcessed(processed);
                    job.setRecordsFailed(failed);
                    batchJobRepository.save(job);
                }
            }

            job.setStatus(failed > 0 && processed == 0 ? JobStatus.FAILED : JobStatus.COMPLETED);
            log.info("HOLD_EXPIRY job id={} completed: processed={} failed={}", job.getId(), processed, failed);

        } catch (Exception e) {
            log.error("HOLD_EXPIRY job id={} encountered a fatal error: {}", job.getId(), e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        job.setRecordsProcessed(processed);
        job.setRecordsFailed(failed);
        job.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(job);
    }

    private void processExpiredHold(Hold hold) {
        log.debug("Expiring hold id={} memberId={} status={} expiryDate={}",
                hold.getId(), hold.getMember().getId(), hold.getStatus(), hold.getExpiryDate());

        HoldStatus previousStatus = hold.getStatus();
        hold.setStatus(HoldStatus.EXPIRED);
        holdRepository.save(hold);

        // Only notify members whose hold was READY (item was available but not picked up)
        // PENDING expiries are silent cancellations
        if (previousStatus == HoldStatus.READY) {
            String message = String.format(
                    "Your hold on \"%s\" has expired. The item was available for pickup until %s. "
                            + "Please place a new hold if you are still interested.",
                    hold.getBook().getTitle(),
                    hold.getExpiryDate().toLocalDate());

            notificationService.sendNotification(
                    hold.getMember().getId(),
                    NotificationType.HOLD_READY,  // closest available type; a HOLD_EXPIRED type would be ideal
                    message,
                    NotificationChannel.EMAIL);
        }
    }
}
