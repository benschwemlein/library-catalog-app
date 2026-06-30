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
public class BookSearchResultDTO {

    private List<BookSummaryDTO> books;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
