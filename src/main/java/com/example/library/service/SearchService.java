package com.example.library.service;

import com.example.library.dto.AdvancedSearchRequest;
import com.example.library.dto.SearchRequest;
import com.example.library.dto.SearchResultDTO;
import com.example.library.entity.Book;
import com.example.library.entity.SearchLog;
import com.example.library.repository.BookRepository;
import com.example.library.repository.SearchLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Transactional(readOnly = true)
    public SearchResultDTO searchBooks(String query, String genre, Integer yearFrom, Integer yearTo, int page, int size) {
        String q = query != null ? query : "";
        List<Book> results = bookRepository.searchByTitleOrDescription(q);

        if (genre != null && !genre.isBlank()) {
            String genreLower = genre.toLowerCase();
            results = results.stream()
                    .filter(b -> b.getGenres() != null &&
                            b.getGenres().stream().anyMatch(g -> g.getName() != null && g.getName().toLowerCase().contains(genreLower)))
                    .collect(Collectors.toList());
        }

        if (yearFrom != null) {
            results = results.stream()
                    .filter(b -> b.getPublicationYear() >= yearFrom)
                    .collect(Collectors.toList());
        }

        if (yearTo != null) {
            results = results.stream()
                    .filter(b -> b.getPublicationYear() <= yearTo)
                    .collect(Collectors.toList());
        }

        logSearch(q, results.size());

        SearchResultDTO dto = new SearchResultDTO();
        dto.setQuery(q);
        dto.setResults(results);
        dto.setTotalCount(results.size());
        return dto;
    }

    @Transactional(readOnly = true)
    public SearchResultDTO searchBooks(SearchRequest req) {
        SearchResultDTO result = searchBooks(req.getQuery(), req.getGenre(), req.getYearFrom(), req.getYearTo(), req.getPage(), req.getSize());
        if (req.getLanguage() != null && !req.getLanguage().isBlank()) {
            String lang = req.getLanguage();
            List<Book> filtered = result.getResults().stream()
                    .filter(b -> lang.equalsIgnoreCase(b.getLanguage()))
                    .collect(Collectors.toList());
            result.setResults(filtered);
            result.setTotalCount(filtered.size());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SearchResultDTO advancedSearch(AdvancedSearchRequest req) {
        String q = req.getTitle() != null ? req.getTitle() : "";
        List<Book> results = bookRepository.searchByTitleOrDescription(q);

        if (req.getAuthor() != null && !req.getAuthor().isBlank()) {
            String authorNameLower = req.getAuthor().toLowerCase();
            results = results.stream()
                    .filter(b -> b.getAuthors() != null && b.getAuthors().stream()
                            .anyMatch(a -> (a.getFirstName() + " " + a.getLastName()).toLowerCase().contains(authorNameLower)))
                    .collect(Collectors.toList());
        }

        if (req.getIsbn() != null && !req.getIsbn().isBlank()) {
            results = results.stream()
                    .filter(b -> req.getIsbn().equals(b.getIsbn()))
                    .collect(Collectors.toList());
        }

        if (req.getGenre() != null && !req.getGenre().isBlank()) {
            String genreLower = req.getGenre().toLowerCase();
            results = results.stream()
                    .filter(b -> b.getGenres() != null &&
                            b.getGenres().stream().anyMatch(g -> g.getName() != null && g.getName().toLowerCase().contains(genreLower)))
                    .collect(Collectors.toList());
        }

        if (req.getLanguage() != null && !req.getLanguage().isBlank()) {
            results = results.stream()
                    .filter(b -> req.getLanguage().equalsIgnoreCase(b.getLanguage()))
                    .collect(Collectors.toList());
        }

        if (req.getYearFrom() != null) {
            results = results.stream()
                    .filter(b -> b.getPublicationYear() >= req.getYearFrom())
                    .collect(Collectors.toList());
        }

        if (req.getYearTo() != null) {
            results = results.stream()
                    .filter(b -> b.getPublicationYear() <= req.getYearTo())
                    .collect(Collectors.toList());
        }

        logSearch(q, results.size());

        SearchResultDTO dto = new SearchResultDTO();
        dto.setQuery(q);
        dto.setResults(results);
        dto.setTotalCount(results.size());
        return dto;
    }

    public void logSearch(String query, int resultCount) {
        SearchLog log = new SearchLog();
        log.setQuery(query != null ? query : "");
        log.setResultCount(resultCount);
        log.setTimestamp(LocalDateTime.now());
        searchLogRepository.save(log);
    }
}
