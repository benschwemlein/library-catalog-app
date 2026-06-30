package com.example.library.pattern.observer;

/**
 * Published when a new book (or a new copy of an existing book) is added to the catalog.
 */
public class BookAddedEvent extends LibraryEvent {

    private final Long bookId;
    private final String isbn;
    private final String title;
    private final Long branchId;

    public BookAddedEvent(Object source,
                          Long bookId,
                          String isbn,
                          String title,
                          Long branchId) {
        super(source);
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.branchId = branchId;
    }

    public Long getBookId()  { return bookId; }
    public String getIsbn()  { return isbn; }
    public String getTitle() { return title; }
    public Long getBranchId(){ return branchId; }
}
