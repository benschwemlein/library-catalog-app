package com.example.library.pattern.strategy;

import com.example.library.entity.Book;

import java.util.List;

public interface SearchRankingStrategy {

    /**
     * Re-rank a list of books for the given search query.
     *
     * @param results the initial result list (may be mutated or replaced)
     * @param query   the original search term entered by the user
     * @return a new ordered list; must contain the same elements as results
     */
    List<Book> rank(List<Book> results, String query);
}
