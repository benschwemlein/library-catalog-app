package com.example.library.search;

import java.util.Map;

/**
 * A single ranked hit returned by the full-text search engine.
 * Carries the entity reference, a human-readable snippet, the relevance
 * score, and per-field highlighted excerpts with query terms wrapped
 * in ** markers.
 */
public class SearchResult {

    private final String entityType;
    private final Long entityId;
    private final String title;
    private final String snippet;
    private final double score;
    private final Map<String, String> highlights;

    public SearchResult(String entityType,
                        Long entityId,
                        String title,
                        String snippet,
                        double score,
                        Map<String, String> highlights) {
        this.entityType = entityType;
        this.entityId   = entityId;
        this.title      = title;
        this.snippet    = snippet;
        this.score      = score;
        this.highlights = highlights;
    }

    public String getEntityType()              { return entityType; }
    public Long   getEntityId()                { return entityId; }
    public String getTitle()                   { return title; }
    public String getSnippet()                 { return snippet; }
    public double getScore()                   { return score; }
    public Map<String, String> getHighlights() { return highlights; }

    @Override
    public String toString() {
        return "SearchResult{type=" + entityType + ", id=" + entityId
                + ", title='" + title + "', score=" + score + "}";
    }
}
