package com.example.library.batch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {

    private final BatchJobService batchJobService;

    /**
     * Submit a new batch job for asynchronous execution.
     *
     * POST /api/v1/batch/jobs
     */
    @PostMapping("/jobs")
    public ResponseEntity<BatchJobDTO> submitJob(@RequestBody @Valid BatchJobRequest request) {
        log.info("REST: submit batch job type={} triggeredBy={}", request.getJobType(), request.getTriggeredBy());
        BatchJob job = batchJobService.submitJob(
                request.getJobType(),
                request.getParameters(),
                request.getTriggeredBy());
        return ResponseEntity.ok(BatchJobDTO.from(job));
    }

    /**
     * Retrieve the 20 most recently created batch jobs.
     *
     * GET /api/v1/batch/jobs
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<BatchJobDTO>> getRecentJobs() {
        log.debug("REST: get recent batch jobs");
        return ResponseEntity.ok(batchJobService.getRecentJobs());
    }

    /**
     * Get the current status and details of a specific batch job.
     *
     * GET /api/v1/batch/jobs/{id}
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<BatchJobDTO> getJobStatus(@PathVariable Long id) {
        log.debug("REST: get batch job status id={}", id);
        BatchJob job = batchJobService.getJobStatus(id);
        return ResponseEntity.ok(BatchJobDTO.from(job));
    }

    /**
     * Cancel a PENDING or RUNNING batch job.
     *
     * POST /api/v1/batch/jobs/{id}/cancel
     */
    @PostMapping("/jobs/{id}/cancel")
    public ResponseEntity<BatchJobDTO> cancelJob(@PathVariable Long id) {
        log.info("REST: cancel batch job id={}", id);
        BatchJob job = batchJobService.cancelJob(id);
        return ResponseEntity.ok(BatchJobDTO.from(job));
    }

    /**
     * Retry a FAILED or CANCELLED batch job by creating a new job of the same type.
     *
     * POST /api/v1/batch/jobs/{id}/retry
     */
    @PostMapping("/jobs/{id}/retry")
    public ResponseEntity<BatchJobDTO> retryJob(@PathVariable Long id) {
        log.info("REST: retry batch job id={}", id);
        BatchJob job = batchJobService.retryFailedJob(id);
        return ResponseEntity.ok(BatchJobDTO.from(job));
    }
}
