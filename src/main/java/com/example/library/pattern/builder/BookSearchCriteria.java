package com.example.library.pattern.builder;

public class BookSearchCriteria {
    private final String title;
    private final String author;
    private final String isbn;
    private final String genre;
    private final String publisher;
    private final Integer yearFrom;
    private final Integer yearTo;
    private final String language;
    private final boolean availableOnly;
    private final Double minRating;
    private final String subject;
    private final int page;
    private final int size;
    private final String sortBy;

    BookSearchCriteria(BookSearchCriteriaBuilder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.isbn = builder.isbn;
        this.genre = builder.genre;
        this.publisher = builder.publisher;
        this.yearFrom = builder.yearFrom;
        this.yearTo = builder.yearTo;
        this.language = builder.language;
        this.availableOnly = builder.availableOnly;
        this.minRating = builder.minRating;
        this.subject = builder.subject;
        this.page = builder.page;
        this.size = builder.size;
        this.sortBy = builder.sortBy;
    }

    public static BookSearchCriteriaBuilder builder() {
        return new BookSearchCriteriaBuilder();
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getGenre() { return genre; }
    public String getPublisher() { return publisher; }
    public Integer getYearFrom() { return yearFrom; }
    public Integer getYearTo() { return yearTo; }
    public String getLanguage() { return language; }
    public boolean isAvailableOnly() { return availableOnly; }
    public Double getMinRating() { return minRating; }
    public String getSubject() { return subject; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getSortBy() { return sortBy; }
}
