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
public class LibraryEventDTO {

    private Long id;
    @NotBlank
    private String title;
    private String description;
    private Long branchId;
    private String branchName;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    private int capacity;
    private int registeredCount;
    private String eventType;
    private int spotsAvailable;
}
