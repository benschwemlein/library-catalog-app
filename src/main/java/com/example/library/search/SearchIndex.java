package com.example.library.search;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Denormalized search index entry.  Each row tracks one indexed entity
 * (BOOK, MEMBER, or AUTHOR) and stores all the text fields that should
 * participate in full-text queries so they can be searched without
 * joining across multiple tables.
 */
@Entity
@Table(name = "search_index",
        indexes = {
                @Index(name = "idx_search_entity", columnList = "entity_type,entity_id"),
                @Index(name = "idx_search_last_indexed", columnList = "last_indexed")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Primary display title — used for suggestion queries and title-match boosting.
     */
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * Concatenated, space-separated body of all searchable text fields for the
     * entity (title, description, author names, genre names, ISBN, etc.).
     */
    @Column
    private String content;

    /**
     * Comma-separated genre / subject tags that can be matched independently
     * for an extra relevance boost.
     */
    @Column(length = 1000)
    private String tags;

    @Column(name = "last_indexed")
    private LocalDateTime lastIndexed;

    /**
     * Static boost factor applied before term-scoring.  Promoted entries
     * (e.g. featured books) can be set higher than the default 1.0.
     */
    @Column(nullable = false)
    @Builder.Default
    private float boost = 1.0f;

    /**
     * Optional canonical URL for deep-linking from search result UI.
     */
    @Column(name = "entity_url", length = 500)
    private String entityUrl;
}
