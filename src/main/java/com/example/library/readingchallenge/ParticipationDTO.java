package com.example.library.readingchallenge;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDTO {

    private Long participationId;
    private Long challengeId;
    private String challengeName;
    private Long memberId;
    private String memberName;
    private LocalDate enrollDate;
    private int completedBooks;
    private int targetBooks;
    private boolean badgeEarned;
    private LocalDate completedDate;
    private double progressPercentage;
}
