package com.example.library.search;

import com.example.library.entity.SearchLog;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main search facade.  Delegates index lookups to {@link SearchIndexRepository},
 * applies in-memory term scoring, paginates, logs the query, and generates
 * auto-complete suggestions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FullTextSearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final SearchLogRepository searchLogRepository;

    // -------------------------------------------------------------------------
    // Primary search entry-point
    // -------------------------------------------------------------------------

    /**
     * Executes a full-text search against the index, optionally filtering
     * by entity type, and returns a paginated result page with relevance-ranked
     * hits and auto-complete suggestions.
     *
     * @param query      raw query string from the user
     * @param entityType optional filter (BOOK, MEMBER, AUTHOR); pass null or blank to search all
     * @param page       zero-based page number
     * @param pageSize   number of results per page
     * @return paginated, ranked search results
     */
    public SearchResultPage search(String query, String entityType, int page, int pageSize) {
        long start = System.currentTimeMillis();

        if (query == null || query.isBlank()) {
            return new SearchResultPage(Collections.emptyList(), 0, page, pageSize, 0,
                    Collections.emptyList());
        }

        String trimmedQuery = query.trim();

        // Fetch raw candidates from the index
        List<SearchIndex> rawResults = searchIndexRepository.fullTextSearch(trimmedQuery);

        // Optional entity-type filter
        if (entityType != null && !entityType.isBlank()) {
            EntityType type = EntityType.valueOf(entityType.toUpperCase());
            rawResults = rawResults.stream()
                    .filter(r -> r.getEntityType() == type)
                    .collect(Collectors.toList());
        }

        // Score, rank and sort
        List<SearchResult> scored = rawResults.stream()
                .map(r -> scoreResult(r, trimmedQuery))
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .collect(Collectors.toList());

        long total = scored.size();

        // Paginate in memory
        int fromIndex = page * pageSize;
        int toIndex   = Math.min(fromIndex + pageSize, scored.size());
        List<SearchResult> pageResults = (fromIndex < scored.size())
                ? scored.subList(fromIndex, toIndex)
                : Collections.emptyList();

        long queryTimeMs = System.currentTimeMillis() - start;

        // Persist audit log entry (non-fatal)
        logSearch(trimmedQuery, total);

        List<String> suggestions = generateSuggestions(trimmedQuery);

        log.debug("Search '{}' returned {} hits in {}ms (page {}/{})",
                trimmedQuery, total, queryTimeMs, page, (int) Math.ceil((double) total / pageSize));

        return new SearchResultPage(pageResults, total, page, pageSize, queryTimeMs, suggestions);
    }

    // -------------------------------------------------------------------------
    // Auto-complete
    // -------------------------------------------------------------------------

    /**
     * Returns up to 5 title suggestions for the given prefix, used by the
     * front-end search-as-you-type feature.
     *
     * @param prefix the characters typed so far
     * @return list of matching book titles, ordered by boost score
     */
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.length() < 2) return Collections.emptyList();
        Pageable limit = PageRequest.of(0, 5);
        return searchIndexRepository.findTitleSuggestions(prefix, EntityType.BOOK, limit)
                .stream()
                .map(SearchIndex::getTitle)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private scoring helpers
    // -------------------------------------------------------------------------

    /**
     * Computes a relevance score for a single index entry against the query
     * and wraps it in a {@link SearchResult}.
     *
     * Scoring tiers (cumulative):
     *   +10  exact title match
     *   + 5  title starts with query
     *   + 3  title contains query
     *   + 2  tag match
     *   + 1  content match
     *   Base: entity's boost value (static, set at index time)
     */
    private SearchResult scoreResult(SearchIndex index, String query) {
        double score = index.getBoost();
        String lowerQuery = query.toLowerCase();
        String lowerTitle = index.getTitle() != null ? index.getTitle().toLowerCase() : "";

        // Title match
        if (lowerTitle.equals(lowerQuery))          score += 10.0;
        else if (lowerTitle.startsWith(lowerQuery)) score +=  5.0;
        else if (lowerTitle.contains(lowerQuery))   score +=  3.0;

        // Content body match
        if (index.getContent() != null
                && index.getContent().toLowerCase().contains(lowerQuery)) {
            score += 1.0;
        }

        // Tag match
        if (index.getTags() != null
                && index.getTags().toLowerCase().contains(lowerQuery)) {
            score += 2.0;
        }

        // Build highlights
        Map<String, String> highlights = new HashMap<>();
        highlights.put("title", highlightText(index.getTitle(), query));
        if (index.getContent() != null) {
            String snippet = extractSnippet(index.getContent(), query, 150);
            highlights.put("content", highlightText(snippet, query));
        }

        String snippet = extractSnippet(
                index.getContent() != null ? index.getContent() : index.getTitle(),
                query, 200);

        return new SearchResult(
                index.getEntityType().name(),
                index.getEntityId(),
                index.getTitle(),
                snippet,
                score,
                highlights);
    }

    /**
     * Wraps all case-insensitive occurrences of {@code query} in {@code **...**}
     * for downstream rendering.
     */
    private String highlightText(String text, String query) {
        if (text == null || text.isBlank()) return "";
        return text.replaceAll("(?i)(" + Pattern.quote(query) + ")", "**$1**");
    }

    /**
     * Extracts a short window of text centered on the first occurrence of
     * {@code query} within {@code content}.  Adds ellipsis markers when the
     * window is not at the start or end of the string.
     */
    private String extractSnippet(String content, String query, int length) {
        if (content == null || content.isBlank()) return "";
        int idx = content.toLowerCase().indexOf(query.toLowerCase());
        if (idx < 0) {
            // No hit: just return the leading characters
            return content.substring(0, Math.min(length, content.length()));
        }
        int start = Math.max(0, idx - 50);
        int end   = Math.min(content.length(), start + length);
        String prefix = start > 0 ? "..." : "";
        String suffix = end < content.length() ? "..." : "";
        return prefix + content.substring(start, end) + suffix;
    }

    /**
     * Generates up to 5 related title suggestions to display below search
     * results.  Excludes the query itself to avoid self-suggesting.
     */
    private List<String> generateSuggestions(String query) {
        if (query.length() < 3) return Collections.emptyList();
        Pageable limit = PageRequest.of(0, 5);
        return searchIndexRepository.findTitleSuggestions(query, EntityType.BOOK, limit)
                .stream()
                .map(SearchIndex::getTitle)
                .filter(t -> !t.equalsIgnoreCase(query))
                .collect(Collectors.toList());
    }

    /**
     * Persists a {@link SearchLog} record for analytics purposes.
     * Errors are caught and suppressed so that a logging failure never
     * causes the search response to fail.
     */
    public List<String> getAvailableGenres() {
        return bookRepository.findAll().stream()
                .flatMap(b -> b.getGenres().stream())
                .map(g -> g.getName())
                .distinct().sorted().collect(Collectors.toList());
    }

    public List<String> getAvailableLanguages() {
        return bookRepository.findAll().stream()
                .map(b -> b.getLanguage())
                .filter(l -> l != null && !l.isBlank())
                .distinct().sorted().collect(Collectors.toList());
    }

    private void logSearch(String query, long resultCount) {
        try {
            SearchLog entry = new SearchLog();
            entry.setQuery(query);
            entry.setResultCount((int) resultCount);
            entry.setTimestamp(LocalDateTime.now());
            searchLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist search log for query '{}': {}", query, e.getMessage());
        }
    }
}
