package com.example.library.dto;

import com.example.library.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {

    private List<BookSummaryDTO> books;
    private List<Book> results;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private long totalCount;
    private String query;
    private Map<String, String> appliedFilters;
}
