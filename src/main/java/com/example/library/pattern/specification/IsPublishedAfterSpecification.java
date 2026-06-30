package com.example.library.pattern.specification;

import com.example.library.entity.Book;

public class IsPublishedAfterSpecification extends AbstractSpecification<Book> {

    private final int year;

    public IsPublishedAfterSpecification(int year) {
        this.year = year;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        return book.getPublicationYear() > year;
    }
}
