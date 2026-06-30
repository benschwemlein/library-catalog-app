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
public class BookSummaryDTO {

    private Long id;
    private String isbn;
    private String title;
    private List<String> authorNames;
    private String publisherName;
    private Integer publicationYear;
    private String coverImageUrl;
    private int availableCopies;
}
