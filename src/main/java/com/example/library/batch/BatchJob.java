package com.example.library.batch;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "records_processed")
    @Builder.Default
    private int recordsProcessed = 0;

    @Column(name = "records_failed")
    @Builder.Default
    private int recordsFailed = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "triggered_by", length = 200)
    private String triggeredBy;

    @Column(name = "parameters")
    private String parameters;  // JSON string

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return 0;
        return Duration.between(startedAt, completedAt).getSeconds();
    }
}
