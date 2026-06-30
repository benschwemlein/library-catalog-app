package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    @NotBlank(message = "Report name is required")
    private String reportName;

    @NotNull(message = "Generated at timestamp is required")
    private LocalDateTime generatedAt;

    private Object data;
}
