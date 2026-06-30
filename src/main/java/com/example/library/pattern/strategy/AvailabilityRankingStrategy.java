package com.example.library.pattern.strategy;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranks books so that those with at least one AVAILABLE copy appear first.
 * Within each group, original order is preserved (stable sort).
 */
@Component
public class AvailabilityRankingStrategy implements SearchRankingStrategy {

    @Override
    public List<Book> rank(List<Book> results, String query) {
        if (results == null || results.isEmpty()) {
            return results != null ? results : List.of();
        }

        List<Book> ranked = new ArrayList<>(results);
        // Sort: books with an available copy get a score of 0 (sorts first), others get 1
        ranked.sort(Comparator.comparingInt(book -> hasAvailableCopy(book) ? 0 : 1));
        return ranked;
    }

    private boolean hasAvailableCopy(Book book) {
        if (book.getCopies() == null || book.getCopies().isEmpty()) {
            return false;
        }
        for (BookCopy copy : book.getCopies()) {
            if (CopyStatus.AVAILABLE.equals(copy.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
