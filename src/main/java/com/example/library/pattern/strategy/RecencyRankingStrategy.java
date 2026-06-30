package com.example.library.pattern.strategy;

import com.example.library.entity.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranks books by publication year, newest first.
 * Useful for surfacing recently published material.
 */
@Component
public class RecencyRankingStrategy implements SearchRankingStrategy {

    @Override
    public List<Book> rank(List<Book> results, String query) {
        if (results == null || results.isEmpty()) {
            return results != null ? results : List.of();
        }

        List<Book> ranked = new ArrayList<>(results);
        ranked.sort(Comparator.comparingInt(Book::getPublicationYear).reversed());
        return ranked;
    }
}
