package com.example.library.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final OverdueBatchProcessor overdueBatchProcessor;
    private final HoldExpiryBatchProcessor holdExpiryBatchProcessor;
    private final MemberCleanupBatchProcessor memberCleanupBatchProcessor;
    private final CatalogSyncBatchProcessor catalogSyncBatchProcessor;
    private final ObjectMapper objectMapper;

    public BatchJob submitJob(JobType type, Map<String, String> params, String triggeredBy) {
        log.info("Submitting batch job type={} triggeredBy={}", type, triggeredBy);

        String paramsJson = null;
        if (params != null && !params.isEmpty()) {
            try {
                paramsJson = objectMapper.writeValueAsString(params);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize job parameters: {}", e.getMessage());
                paramsJson = params.toString();
            }
        }

        BatchJob job = BatchJob.builder()
                .jobType(type)
                .status(JobStatus.PENDING)
                .triggeredBy(triggeredBy)
                .parameters(paramsJson)
                .build();

        BatchJob saved = batchJobRepository.save(job);
        log.info("Created batch job id={} type={}", saved.getId(), type);

        runJobAsync(saved);

        return saved;
    }

    @Async("libraryTaskExecutor")
    public void runJobAsync(BatchJob job) {
        log.info("Executing batch job id={} type={}", job.getId(), job.getJobType());
        switch (job.getJobType()) {
            case OVERDUE_PROCESSING -> overdueBatchProcessor.process(job);
            case HOLD_EXPIRY -> holdExpiryBatchProcessor.process(job);
            case MEMBER_CLEANUP -> memberCleanupBatchProcessor.process(job);
            case CATALOG_SYNC -> catalogSyncBatchProcessor.process(job);
            default -> {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("No processor registered for job type: " + job.getJobType());
                job.setCompletedAt(LocalDateTime.now());
                batchJobRepository.save(job);
            }
        }
    }

    @Transactional
    public BatchJob cancelJob(Long id) {
        BatchJob job = batchJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch job not found: " + id));

        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel a " + job.getStatus() + " job");
        }

        job.setStatus(JobStatus.CANCELLED);
        job.setCompletedAt(LocalDateTime.now());
        log.info("Cancelled batch job id={}", id);
        return batchJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public BatchJob getJobStatus(Long id) {
        return batchJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch job not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<BatchJobDTO> getRecentJobs() {
        return batchJobRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(BatchJobDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public BatchJob retryFailedJob(Long id) {
        BatchJob original = batchJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch job not found: " + id));

        if (original.getStatus() != JobStatus.FAILED && original.getStatus() != JobStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Can only retry FAILED or CANCELLED jobs. Current status: " + original.getStatus());
        }

        BatchJob retryJob = BatchJob.builder()
                .jobType(original.getJobType())
                .status(JobStatus.PENDING)
                .triggeredBy("RETRY of job #" + original.getId())
                .parameters(original.getParameters())
                .build();

        BatchJob saved = batchJobRepository.save(retryJob);
        log.info("Created retry job id={} for original job id={}", saved.getId(), id);
        runJobAsync(saved);
        return saved;
    }
}
