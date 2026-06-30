package com.example.library.search;

import java.util.Collections;
import java.util.List;

/**
 * Paginated container for full-text search hits.
 * Carries the current page of {@link SearchResult}s together with
 * aggregate metadata (total hits, timing) and auto-complete suggestions
 * derived from the query.
 */
public class SearchResultPage {

    private final List<SearchResult> results;
    private final long totalHits;
    private final int page;
    private final int pageSize;
    private final long queryTimeMs;
    private final List<String> suggestions;

    public SearchResultPage(List<SearchResult> results,
                            long totalHits,
                            int page,
                            int pageSize,
                            long queryTimeMs,
                            List<String> suggestions) {
        this.results     = results != null ? results : Collections.emptyList();
        this.totalHits   = totalHits;
        this.page        = page;
        this.pageSize    = pageSize;
        this.queryTimeMs = queryTimeMs;
        this.suggestions = suggestions != null ? suggestions : Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<SearchResult> getResults()   { return results; }
    public long   getTotalHits()             { return totalHits; }
    public int    getPage()                  { return page; }
    public int    getPageSize()              { return pageSize; }
    public long   getQueryTimeMs()           { return queryTimeMs; }
    public List<String> getSuggestions()     { return suggestions; }

    /** Convenience: total number of pages given current pageSize. */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalHits / pageSize);
    }

    /** Whether there are more pages after this one. */
    public boolean hasNextPage() {
        return (long) (page + 1) * pageSize < totalHits;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private List<SearchResult> results;
        private long totalHits;
        private int page;
        private int pageSize;
        private long queryTimeMs;
        private List<String> suggestions;

        public Builder results(List<SearchResult> results)     { this.results = results; return this; }
        public Builder totalHits(long totalHits)               { this.totalHits = totalHits; return this; }
        public Builder page(int page)                          { this.page = page; return this; }
        public Builder pageSize(int pageSize)                  { this.pageSize = pageSize; return this; }
        public Builder queryTimeMs(long queryTimeMs)           { this.queryTimeMs = queryTimeMs; return this; }
        public Builder suggestions(List<String> suggestions)   { this.suggestions = suggestions; return this; }

        public SearchResultPage build() {
            return new SearchResultPage(results, totalHits, page, pageSize, queryTimeMs, suggestions);
        }
    }

    @Override
    public String toString() {
        return "SearchResultPage{totalHits=" + totalHits + ", page=" + page
                + ", pageSize=" + pageSize + ", queryTimeMs=" + queryTimeMs + "ms}";
    }
}
