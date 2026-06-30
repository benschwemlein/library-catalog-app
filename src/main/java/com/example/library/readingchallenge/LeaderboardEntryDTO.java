package com.example.library.readingchallenge;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {

    private int rank;
    private Long memberId;
    private String memberName;
    private int completedBooks;
    private boolean badgeEarned;
    private LocalDate completedDate;
}
