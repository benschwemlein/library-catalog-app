package com.example.library.recommendation;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private Long bookId;
    private String title;
    private String isbn;
    private List<String> authors;
    private List<String> genres;
    private String reason;
    private double score;
    private int publicationYear;
    private boolean available;
}
