package com.example.library.controller;

import com.example.library.dto.BookSummaryDTO;
import com.example.library.dto.SearchResultDTO;
import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookSummaryDTO>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        Stream<Book> stream = bookService.findAll().stream();
        if (title != null && !title.isBlank()) {
            String lc = title.toLowerCase();
            stream = stream.filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(lc));
        }
        if (author != null && !author.isBlank()) {
            String lc = author.toLowerCase();
            stream = stream.filter(b -> b.getAuthors() != null && b.getAuthors().stream()
                    .anyMatch(a -> (a.getFirstName() + " " + a.getLastName()).toLowerCase().contains(lc)));
        }
        List<BookSummaryDTO> summaries = stream.map(this::toSummary).collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<BookSummaryDTO>> getNewArrivals() {
        List<BookSummaryDTO> summaries = bookService.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(20)
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findByIsbn(isbn));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResultDTO> searchBooks(@RequestParam String q) {
        return ResponseEntity.ok(bookService.search(q));
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return ResponseEntity.ok(bookService.update(id, book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/copies")
    public ResponseEntity<List<BookCopy>> getBookCopies(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findCopies(id));
    }

    private BookSummaryDTO toSummary(Book book) {
        List<String> authorNames = book.getAuthors() == null ? List.of() :
                book.getAuthors().stream()
                        .map(a -> a.getFirstName() + " " + a.getLastName())
                        .collect(Collectors.toList());

        String publisherName = book.getPublisher() != null ? book.getPublisher().getName() : "";

        int availableCopies = book.getCopies() == null ? 0 :
                (int) book.getCopies().stream()
                        .filter(c -> CopyStatus.AVAILABLE.equals(c.getStatus()))
                        .count();

        return BookSummaryDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authorNames(authorNames)
                .publisherName(publisherName)
                .publicationYear(book.getPublicationYear())
                .coverImageUrl(book.getCoverImageUrl())
                .availableCopies(availableCopies)
                .build();
    }
}
