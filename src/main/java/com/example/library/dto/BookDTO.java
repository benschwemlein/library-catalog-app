package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;
    private String isbn;
    private String title;
    private String subtitle;
    private String description;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String coverImageUrl;
    private List<String> authorNames;
    private String publisherName;
    private List<String> genreNames;
    private List<String> subjectNames;
    private int availableCopies;
    private int totalCopies;
    private Double averageRating;
}
