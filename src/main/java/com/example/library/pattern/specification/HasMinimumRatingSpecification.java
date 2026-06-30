package com.example.library.pattern.specification;

import com.example.library.entity.Book;
import com.example.library.entity.BookReview;

public class HasMinimumRatingSpecification extends AbstractSpecification<Book> {

    private final double minRating;

    public HasMinimumRatingSpecification(double minRating) {
        this.minRating = minRating;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        if (book.getReviews() == null || book.getReviews().isEmpty()) {
            return false;
        }
        double average = book.getReviews().stream()
            .mapToInt(BookReview::getRating)
            .average()
            .orElse(0.0);
        return average >= minRating;
    }
}
