package com.example.library.pattern.builder;

public class BookSearchCriteriaBuilder {
    String title;
    String author;
    String isbn;
    String genre;
    String publisher;
    Integer yearFrom;
    Integer yearTo;
    String language;
    boolean availableOnly = false;
    Double minRating;
    String subject;
    int page = 0;
    int size = 20;
    String sortBy = "title";

    public BookSearchCriteriaBuilder title(String title) { this.title = title; return this; }
    public BookSearchCriteriaBuilder author(String author) { this.author = author; return this; }
    public BookSearchCriteriaBuilder isbn(String isbn) { this.isbn = isbn; return this; }
    public BookSearchCriteriaBuilder genre(String genre) { this.genre = genre; return this; }
    public BookSearchCriteriaBuilder publisher(String publisher) { this.publisher = publisher; return this; }
    public BookSearchCriteriaBuilder yearFrom(Integer yearFrom) { this.yearFrom = yearFrom; return this; }
    public BookSearchCriteriaBuilder yearTo(Integer yearTo) { this.yearTo = yearTo; return this; }
    public BookSearchCriteriaBuilder language(String language) { this.language = language; return this; }
    public BookSearchCriteriaBuilder availableOnly(boolean availableOnly) { this.availableOnly = availableOnly; return this; }
    public BookSearchCriteriaBuilder minRating(Double minRating) { this.minRating = minRating; return this; }
    public BookSearchCriteriaBuilder subject(String subject) { this.subject = subject; return this; }
    public BookSearchCriteriaBuilder page(int page) { this.page = page; return this; }
    public BookSearchCriteriaBuilder size(int size) { this.size = size; return this; }
    public BookSearchCriteriaBuilder sortBy(String sortBy) { this.sortBy = sortBy; return this; }

    public BookSearchCriteria build() {
        if (yearFrom != null && yearTo != null && yearFrom > yearTo) {
            throw new IllegalArgumentException("yearFrom cannot be greater than yearTo");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            size = 100;
        }
        if (minRating != null && (minRating < 1.0 || minRating > 5.0)) {
            throw new IllegalArgumentException("minRating must be between 1.0 and 5.0");
        }
        return new BookSearchCriteria(this);
    }
}
