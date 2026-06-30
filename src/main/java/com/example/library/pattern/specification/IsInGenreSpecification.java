package com.example.library.pattern.specification;

import com.example.library.entity.Book;

public class IsInGenreSpecification extends AbstractSpecification<Book> {

    private final String genreName;

    public IsInGenreSpecification(String genreName) {
        this.genreName = genreName;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        if (book.getGenres() == null || book.getGenres().isEmpty()) {
            return false;
        }
        return book.getGenres().stream()
            .anyMatch(g -> g.getName().equalsIgnoreCase(genreName));
    }
}
