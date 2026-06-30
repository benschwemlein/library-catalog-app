package com.example.library.pattern.strategy;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks books by textual relevance to the search query.
 * Scoring: +10 title match, +5 any author name match, +3 description match.
 */
@Component
public class RelevanceRankingStrategy implements SearchRankingStrategy {

    @Override
    public List<Book> rank(List<Book> results, String query) {
        if (results == null || results.isEmpty() || query == null || query.isBlank()) {
            return results != null ? results : List.of();
        }

        String lowerQuery = query.toLowerCase().trim();
        Map<Long, Integer> scores = new HashMap<>();

        for (Book book : results) {
            int score = 0;

            // Title match: +10
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery)) {
                score += 10;
            }

            // Author match: +5 if any author's full name contains the query
            if (book.getAuthors() != null) {
                for (Author author : book.getAuthors()) {
                    String fullName = (author.getFirstName() + " " + author.getLastName()).toLowerCase();
                    if (fullName.contains(lowerQuery)) {
                        score += 5;
                        break; // Count at most once per book
                    }
                }
            }

            // Description match: +3
            if (book.getDescription() != null && book.getDescription().toLowerCase().contains(lowerQuery)) {
                score += 3;
            }

            scores.put(book.getId(), score);
        }

        List<Book> ranked = new ArrayList<>(results);
        ranked.sort(Comparator.comparingInt((Book b) -> scores.getOrDefault(b.getId(), 0)).reversed());
        return ranked;
    }
}
