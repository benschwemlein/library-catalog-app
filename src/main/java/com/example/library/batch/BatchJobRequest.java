package com.example.library.batch;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class BatchJobRequest {

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private Map<String, String> parameters;

    private String triggeredBy;
}
