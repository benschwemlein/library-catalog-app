package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedSearchRequest {

    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String subject;
    private String publisher;
    private Integer yearFrom;
    private Integer yearTo;
    private String language;
    private boolean availableOnly;
}
