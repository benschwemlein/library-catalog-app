package com.example.library.recommendation;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class MemberBorrowingProfile {
    private Long memberId;
    private Map<String, Integer> genreFrequency;
    private Map<Long, Integer> authorFrequency;
    private Set<Long> borrowedBookIds;
    private LocalDateTime builtAt;
}
