package com.example.library.pattern.strategy;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blends four sub-ranking strategies into a single composite score.
 *
 * <p>Each sub-strategy produces an ordered list; each book's position within that list
 * is converted to a normalized [0, 1] score (1 = best). The four normalized scores are
 * then combined with configured weights.</p>
 *
 * <p>Default weights:
 * <ul>
 *   <li>Relevance: 0.4</li>
 *   <li>Popularity: 0.3</li>
 *   <li>Recency: 0.2</li>
 *   <li>Availability: 0.1</li>
 * </ul>
 */
@Component
public class CompositeRankingStrategy implements SearchRankingStrategy {

    private final RelevanceRankingStrategy relevanceStrategy;
    private final PopularityRankingStrategy popularityStrategy;
    private final RecencyRankingStrategy recencyStrategy;
    private final AvailabilityRankingStrategy availabilityStrategy;

    private double relevanceWeight = 0.4;
    private double popularityWeight = 0.3;
    private double recencyWeight = 0.2;
    private double availabilityWeight = 0.1;

    public CompositeRankingStrategy(RelevanceRankingStrategy relevanceStrategy,
                                    PopularityRankingStrategy popularityStrategy,
                                    RecencyRankingStrategy recencyStrategy,
                                    AvailabilityRankingStrategy availabilityStrategy) {
        this.relevanceStrategy = relevanceStrategy;
        this.popularityStrategy = popularityStrategy;
        this.recencyStrategy = recencyStrategy;
        this.availabilityStrategy = availabilityStrategy;
    }

    @Override
    public List<Book> rank(List<Book> results, String query) {
        if (results == null || results.isEmpty()) {
            return results != null ? results : List.of();
        }
        if (results.size() == 1) {
            return new ArrayList<>(results);
        }

        // Get ranked lists from each sub-strategy
        List<Book> byRelevance   = relevanceStrategy.rank(results, query);
        List<Book> byPopularity  = popularityStrategy.rank(results, query);
        List<Book> byRecency     = recencyStrategy.rank(results, query);
        List<Book> byAvailability = availabilityStrategy.rank(results, query);

        int n = results.size();
        Map<Long, Double> compositeScores = new HashMap<>();

        for (Book book : results) {
            double rScore = normalizedScore(book.getId(), byRelevance,   n);
            double pScore = normalizedScore(book.getId(), byPopularity,  n);
            double cScore = normalizedScore(book.getId(), byRecency,     n);
            double aScore = normalizedScore(book.getId(), byAvailability, n);

            double composite = (rScore * relevanceWeight)
                             + (pScore * popularityWeight)
                             + (cScore * recencyWeight)
                             + (aScore * availabilityWeight);

            compositeScores.put(book.getId(), composite);
        }

        List<Book> ranked = new ArrayList<>(results);
        ranked.sort(Comparator.comparingDouble((Book b) ->
                compositeScores.getOrDefault(b.getId(), 0.0)).reversed());
        return ranked;
    }

    /**
     * Convert a book's position in a ranked list to a [0, 1] score.
     * Position 0 (best) maps to 1.0; last position maps to 1/n.
     */
    private double normalizedScore(Long bookId, List<Book> rankedList, int total) {
        for (int i = 0; i < rankedList.size(); i++) {
            if (rankedList.get(i).getId().equals(bookId)) {
                // (total - i) / total gives 1.0 at index 0, approaches 0 at the end
                return (double) (total - i) / total;
            }
        }
        return 0.0;
    }

    // --- Weight setters for runtime tuning ---

    public void setRelevanceWeight(double relevanceWeight) {
        this.relevanceWeight = relevanceWeight;
    }

    public void setPopularityWeight(double popularityWeight) {
        this.popularityWeight = popularityWeight;
    }

    public void setRecencyWeight(double recencyWeight) {
        this.recencyWeight = recencyWeight;
    }

    public void setAvailabilityWeight(double availabilityWeight) {
        this.availabilityWeight = availabilityWeight;
    }

    public double getRelevanceWeight()   { return relevanceWeight; }
    public double getPopularityWeight()  { return popularityWeight; }
    public double getRecencyWeight()     { return recencyWeight; }
    public double getAvailabilityWeight(){ return availabilityWeight; }
}
