package com.example.library.readingchallenge;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int targetBooks;
    private String targetGenreNames;
    private String badge;
    private boolean active;
    private int enrolledCount;
    private int completedCount;
    private LocalDateTime createdAt;
}
