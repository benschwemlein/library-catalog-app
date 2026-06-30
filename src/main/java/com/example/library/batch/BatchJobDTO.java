package com.example.library.batch;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobDTO {

    private Long id;
    private JobType jobType;
    private JobStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int recordsProcessed;
    private int recordsFailed;
    private String errorMessage;
    private String triggeredBy;
    private String parameters;
    private LocalDateTime createdAt;
    private long durationSeconds;

    public static BatchJobDTO from(BatchJob job) {
        return BatchJobDTO.builder()
                .id(job.getId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .recordsProcessed(job.getRecordsProcessed())
                .recordsFailed(job.getRecordsFailed())
                .errorMessage(job.getErrorMessage())
                .triggeredBy(job.getTriggeredBy())
                .parameters(job.getParameters())
                .createdAt(job.getCreatedAt())
                .durationSeconds(job.getDurationSeconds())
                .build();
    }
}
