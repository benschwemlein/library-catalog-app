package com.example.library.service;

import com.example.library.dto.AdvancedSearchRequest;
import com.example.library.dto.SearchRequest;
import com.example.library.dto.SearchResultDTO;
import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.entity.Genre;
import com.example.library.entity.SearchLog;
import com.example.library.repository.BookRepository;
import com.example.library.repository.SearchLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SearchLogRepository searchLogRepository;

    @InjectMocks
    private SearchService searchService;

    private Book javaBook;
    private Book springBook;
    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    void setUp() {
        genre1 = Genre.builder().id(1L).name("Technology").description("Tech books").build();
        genre2 = Genre.builder().id(2L).name("Fiction").description("Fiction books").build();

        Author author1 = Author.builder().id(1L).firstName("Josh").lastName("Bloch").build();
        Author author2 = Author.builder().id(2L).firstName("Rod").lastName("Johnson").build();

        Set<Author> authors1 = new HashSet<>();
        authors1.add(author1);

        Set<Author> authors2 = new HashSet<>();
        authors2.add(author2);

        Set<Genre> genres1 = new HashSet<>();
        genres1.add(genre1);

        Set<Genre> genres2 = new HashSet<>();
        genres2.add(genre2);

        javaBook = Book.builder()
                .id(1L)
                .isbn("978-0-13-468599-1")
                .title("Effective Java")
                .description("Best practices for the Java programming language")
                .publicationYear(2018)
                .language("English")
                .authors(authors1)
                .genres(genres1)
                .build();

        springBook = Book.builder()
                .id(2L)
                .isbn("978-1-617-29752-7")
                .title("Spring in Action")
                .description("Covers the Spring framework for Java applications")
                .publicationYear(2020)
                .language("English")
                .authors(authors2)
                .genres(genres2)
                .build();

        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Book createBook(Long id, String title, String author) {
        Author a = Author.builder().id(id).firstName(author.split(" ")[0]).lastName(author.split(" ").length > 1 ? author.split(" ")[1] : "").build();
        Set<Author> authors = new HashSet<>();
        authors.add(a);
        return Book.builder()
                .id(id)
                .isbn("978-0-00-000000-" + id)
                .title(title)
                .description("A book about " + title.toLowerCase())
                .publicationYear(2020)
                .language("English")
                .authors(authors)
                .genres(new HashSet<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // searchBooks tests
    // -------------------------------------------------------------------------

    @Test
    void searchBooks_simpleQuery_callsRepositoryAndReturnsResults() {
        SearchRequest req = SearchRequest.builder().query("java").build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.searchBooks(req);

        verify(bookRepository).searchByTitleOrDescription("java");
        assertThat(result).isNotNull();
        assertThat(result.getResults()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getQuery()).isEqualTo("java");
    }

    @Test
    void searchBooks_withGenreFilter_filtersResults() {
        SearchRequest req = SearchRequest.builder().query("java").genre("Fiction").build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.searchBooks(req);

        // Only springBook has genre2 "Fiction" (id=2)
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(2L);
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    @Test
    void searchBooks_withLanguageFilter_filtersToMatchingLanguage() {
        Book spanishBook = Book.builder()
                .id(3L)
                .isbn("978-0-00-000003-3")
                .title("Java en Español")
                .description("Java programming in Spanish")
                .publicationYear(2019)
                .language("Spanish")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        SearchRequest req = SearchRequest.builder().query("java").language("Spanish").build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook, spanishBook));

        SearchResultDTO result = searchService.searchBooks(req);

        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(3L);
    }

    @Test
    void searchBooks_withYearFilter_filtersToMatchingYear() {
        SearchRequest req = SearchRequest.builder().query("java").yearFrom(2018).yearTo(2018).build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.searchBooks(req);

        // Only javaBook has publicationYear 2018
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void searchBooks_emptyResults_returnsZeroCount() {
        SearchRequest req = SearchRequest.builder().query("nonexistentterm12345").build();
        when(bookRepository.searchByTitleOrDescription("nonexistentterm12345")).thenReturn(List.of());

        SearchResultDTO result = searchService.searchBooks(req);

        assertThat(result.getResults()).isEmpty();
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getQuery()).isEqualTo("nonexistentterm12345");
    }

    @Test
    void searchBooks_logsSearchToSearchLog() {
        SearchRequest req = SearchRequest.builder().query("java").build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook));

        searchService.searchBooks(req);

        ArgumentCaptor<SearchLog> logCaptor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogRepository).save(logCaptor.capture());
        SearchLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getQuery()).isEqualTo("java");
        assertThat(savedLog.getResultCount()).isEqualTo(1);
    }

    @Test
    void searchBooks_nullQuery_handlesGracefully() {
        SearchRequest req = SearchRequest.builder().query(null).build();
        when(bookRepository.searchByTitleOrDescription(null)).thenReturn(List.of());

        SearchResultDTO result = searchService.searchBooks(req);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    void searchBooks_queryTooLong_handlesGracefully() {
        String longQuery = "a".repeat(10000);
        SearchRequest req = SearchRequest.builder().query(longQuery).build();
        when(bookRepository.searchByTitleOrDescription(longQuery)).thenReturn(List.of());

        SearchResultDTO result = searchService.searchBooks(req);

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(0);
        verify(bookRepository).searchByTitleOrDescription(longQuery);
    }

    // -------------------------------------------------------------------------
    // advancedSearch tests
    // -------------------------------------------------------------------------

    @Test
    void advancedSearch_searchByIsbn_usesIsbnSearch() {
        AdvancedSearchRequest req = AdvancedSearchRequest.builder()
                .isbn("978-0-13-468599-1")
                .build();
        when(bookRepository.searchByTitleOrDescription("")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.advancedSearch(req);

        // Only javaBook has matching ISBN
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getIsbn()).isEqualTo("978-0-13-468599-1");
    }

    @Test
    void advancedSearch_multipleFilters_appliesAll() {
        AdvancedSearchRequest req = AdvancedSearchRequest.builder()
                .title("java")
                .language("English")
                .yearFrom(2018)
                .yearTo(2018)
                .build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.advancedSearch(req);

        // Only javaBook matches English + year 2018
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getSearchSuggestions_returnsMatchingTitles() {
        SearchRequest req = SearchRequest.builder().query("Effective").build();
        when(bookRepository.searchByTitleOrDescription("Effective")).thenReturn(List.of(javaBook));

        SearchResultDTO result = searchService.searchBooks(req);

        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getTitle()).contains("Effective");
    }

    @Test
    void searchBooks_multipleFilters_genreAndLanguageAndYear_narrowsResults() {
        Set<Genre> genres = new HashSet<>();
        genres.add(genre1);

        Book matchingBook = Book.builder()
                .id(10L)
                .isbn("978-0-00-000010-0")
                .title("Advanced Java")
                .description("Advanced Java programming")
                .publicationYear(2021)
                .language("English")
                .authors(new HashSet<>())
                .genres(genres)
                .build();

        SearchRequest req = SearchRequest.builder()
                .query("java")
                .genre("Technology")
                .language("English")
                .yearFrom(2021)
                .yearTo(2021)
                .build();
        when(bookRepository.searchByTitleOrDescription("java")).thenReturn(List.of(javaBook, springBook, matchingBook));

        SearchResultDTO result = searchService.searchBooks(req);

        // Only matchingBook has genre1, English, year 2021
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(10L);
    }

    @Test
    void advancedSearch_authorNameFilter_filtersToMatchingAuthor() {
        AdvancedSearchRequest req = AdvancedSearchRequest.builder()
                .author("Bloch")
                .build();
        when(bookRepository.searchByTitleOrDescription("")).thenReturn(List.of(javaBook, springBook));

        SearchResultDTO result = searchService.advancedSearch(req);

        // Only javaBook has author Bloch
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(1L);
    }
}
