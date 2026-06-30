package com.example.library.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {

    List<BatchJob> findByStatus(JobStatus status);

    List<BatchJob> findByJobTypeAndStatus(JobType jobType, JobStatus status);

    List<BatchJob> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT b FROM BatchJob b WHERE b.jobType = :jobType AND b.status = :status AND b.startedAt > :since")
    List<BatchJob> findByJobTypeAndStatusAndStartedAtAfter(
            @Param("jobType") JobType jobType,
            @Param("status") JobStatus status,
            @Param("since") LocalDateTime since);

    @Query("SELECT b FROM BatchJob b WHERE b.status = 'RUNNING' AND b.startedAt < :stuckThreshold")
    List<BatchJob> findStuckJobs(@Param("stuckThreshold") LocalDateTime stuckThreshold);

    List<BatchJob> findByJobType(JobType jobType);
}
