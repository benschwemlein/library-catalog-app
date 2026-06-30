package com.example.library.pattern.strategy;

import com.example.library.entity.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranks books by the number of copies held by the library system.
 * More copies is treated as a proxy for higher demand and popularity.
 */
@Component
public class PopularityRankingStrategy implements SearchRankingStrategy {

    @Override
    public List<Book> rank(List<Book> results, String query) {
        if (results == null || results.isEmpty()) {
            return results != null ? results : List.of();
        }

        List<Book> ranked = new ArrayList<>(results);
        ranked.sort(Comparator.comparingInt((Book b) ->
                b.getCopies() != null ? b.getCopies().size() : 0
        ).reversed());
        return ranked;
    }
}
