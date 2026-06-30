package com.example.library.search;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistence layer for {@link SearchIndex}.
 *
 * All full-text queries use LIKE with LOWER() for H2 compatibility while
 * remaining portable to MySQL / PostgreSQL for production deployments.
 */
@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {

    /**
     * Broad full-text search across all indexed text fields.
     * Results are ordered by the static boost score descending so that
     * promoted entries surface first before in-memory term scoring is applied.
     */
    @Query("SELECT s FROM SearchIndex s WHERE "
            + "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(s.title)   LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(s.tags)    LIKE LOWER(CONCAT('%', :query, '%')) "
            + "ORDER BY s.boost DESC")
    List<SearchIndex> fullTextSearch(@Param("query") String query);

    /**
     * Look up the existing index entry for a specific entity so that
     * re-indexing can update in place rather than inserting a duplicate.
     */
    Optional<SearchIndex> findByEntityTypeAndEntityId(EntityType type, Long entityId);

    /**
     * Returns all index entries whose {@code lastIndexed} timestamp is older
     * than the given threshold, enabling scheduled stale-entry cleanup.
     */
    @Query("SELECT s FROM SearchIndex s WHERE s.lastIndexed < :threshold")
    List<SearchIndex> findStaleIndexEntries(@Param("threshold") LocalDateTime threshold);

    /**
     * Returns all entries of the given entity type, used for per-type
     * statistics reporting.
     */
    List<SearchIndex> findByEntityType(EntityType type);

    /**
     * Prefix-match on title within a specific entity type, used to power
     * the auto-complete/suggestions endpoint.  Results are ordered by boost
     * descending so the highest-quality matches appear at the top.
     */
    @Query("SELECT s FROM SearchIndex s WHERE "
            + "LOWER(s.title) LIKE LOWER(CONCAT(:prefix, '%')) "
            + "AND s.entityType = :type "
            + "ORDER BY s.boost DESC")
    List<SearchIndex> findTitleSuggestions(@Param("prefix") String prefix,
                                           @Param("type")   EntityType type,
                                           Pageable pageable);
}
