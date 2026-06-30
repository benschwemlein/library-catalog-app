package com.example.library.service;

import com.example.library.dto.CreateBookRequest;
import com.example.library.dto.SearchResultDTO;
import com.example.library.dto.UpdateBookRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.BookReview;
import com.example.library.entity.CopyStatus;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookSubjectRepository bookSubjectRepository;

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
    }

    @Transactional(readOnly = true)
    public SearchResultDTO search(String query) {
        List<Book> results = bookRepository.searchByTitleOrDescription(query != null ? query : "");
        SearchResultDTO dto = new SearchResultDTO();
        dto.setResults(results);
        dto.setTotalCount(results.size());
        dto.setQuery(query);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        return bookRepository.searchByTitleOrDescription(query);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> findCopies(Long bookId) {
        return findById(bookId).getCopies();
    }

    @Transactional(readOnly = true)
    public List<BookReview> findReviews(Long bookId) {
        return findById(bookId).getReviews();
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getAvailableCopies(Long bookId) {
        Book book = findById(bookId);
        return book.getCopies().stream()
                .filter(copy -> CopyStatus.AVAILABLE.equals(copy.getStatus()))
                .collect(Collectors.toList());
    }

    public Book create(Book book) {
        return bookRepository.save(book);
    }

    public Book update(Long id, Book updated) {
        Book book = findById(id);
        if (updated.getTitle() != null) book.setTitle(updated.getTitle());
        if (updated.getDescription() != null) book.setDescription(updated.getDescription());
        if (updated.getLanguage() != null) book.setLanguage(updated.getLanguage());
        if (updated.getPublicationYear() != 0) book.setPublicationYear(updated.getPublicationYear());
        if (updated.getPageCount() != 0) book.setPageCount(updated.getPageCount());
        if (updated.getPublisher() != null) book.setPublisher(updated.getPublisher());
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = findById(id);
        bookRepository.delete(book);
    }

    public Book addBook(CreateBookRequest req) {
        Book book = new Book();
        book.setTitle(req.getTitle());
        book.setIsbn(req.getIsbn());
        book.setDescription(req.getDescription());
        book.setLanguage(req.getLanguage());
        if (req.getPublicationYear() != null) book.setPublicationYear(req.getPublicationYear());
        if (req.getPageCount() != null) book.setPageCount(req.getPageCount());

        if (req.getPublisherId() != null) {
            publisherRepository.findById(req.getPublisherId())
                    .ifPresent(book::setPublisher);
        }

        if (req.getAuthorIds() != null && !req.getAuthorIds().isEmpty()) {
            book.setAuthors(new java.util.HashSet<>(authorRepository.findAllById(req.getAuthorIds())));
        }

        if (req.getGenreIds() != null && !req.getGenreIds().isEmpty()) {
            book.setGenres(new java.util.HashSet<>(genreRepository.findAllById(req.getGenreIds())));
        }

        if (req.getSubjectIds() != null && !req.getSubjectIds().isEmpty()) {
            book.setSubjects(new java.util.HashSet<>(bookSubjectRepository.findAllById(req.getSubjectIds())));
        }

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, UpdateBookRequest req) {
        Book book = findById(id);

        if (req.getTitle() != null) book.setTitle(req.getTitle());
        if (req.getDescription() != null) book.setDescription(req.getDescription());
        if (req.getLanguage() != null) book.setLanguage(req.getLanguage());
        if (req.getPublicationYear() != null) book.setPublicationYear(req.getPublicationYear());
        if (req.getPageCount() != null) book.setPageCount(req.getPageCount());
        if (req.getPublisherId() != null) {
            publisherRepository.findById(req.getPublisherId())
                    .ifPresent(book::setPublisher);
        }
        if (req.getAuthorIds() != null) {
            book.setAuthors(new java.util.HashSet<>(authorRepository.findAllById(req.getAuthorIds())));
        }
        if (req.getGenreIds() != null) {
            book.setGenres(new java.util.HashSet<>(genreRepository.findAllById(req.getGenreIds())));
        }
        if (req.getSubjectIds() != null) {
            book.setSubjects(new java.util.HashSet<>(bookSubjectRepository.findAllById(req.getSubjectIds())));
        }

        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        delete(id);
    }

    @Transactional(readOnly = true)
    public List<Book> getRecentlyAdded(int limit) {
        return bookRepository.findAll(Pageable.ofSize(limit)).getContent();
    }
}
