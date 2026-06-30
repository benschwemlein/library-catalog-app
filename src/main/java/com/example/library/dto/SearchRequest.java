package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    private String query;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String publisher;
    private Integer yearFrom;
    private Integer yearTo;
    private String language;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "title";

    @Builder.Default
    private String sortDir = "asc";
}
