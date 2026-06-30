package com.example.library.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final FullTextSearchService searchService;
    private final SearchIndexService indexService;

    /**
     * General search across all entity types.
     * GET /api/v1/search?q=java&type=BOOK&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<SearchResultPage> search(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Search request: q='{}', type={}, page={}, size={}", q, type, page, size);

        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        size = Math.min(size, 100);  // Cap page size
        SearchResultPage results = searchService.search(q.trim(), type, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * Search books specifically.
     * GET /api/v1/search/books?q=tolkien&page=0&size=20
     */
    @GetMapping("/books")
    public ResponseEntity<SearchResultPage> searchBooks(
            @RequestParam(name = "query", required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        SearchResultPage results = searchService.search(q, "BOOK", page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * Get title suggestions for autocomplete.
     * GET /api/v1/search/suggest?q=har
     */
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam(name = "query", required = false, defaultValue = "") String q) {
        if (q == null || q.length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<String> suggestions = searchService.getSuggestions(q.trim());
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get index statistics.
     * GET /api/v1/search/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = indexService.getIndexStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Trigger a full reindex (admin only).
     * POST /api/v1/search/reindex
     */
    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerReindex() {
        log.info("Manual reindex triggered");
        indexService.reindexAll();
        return ResponseEntity.ok(Map.of(
            "message", "Reindex triggered successfully",
            "status", "RUNNING"
        ));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(searchService.getAvailableGenres());
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getLanguages() {
        return ResponseEntity.ok(searchService.getAvailableLanguages());
    }

    @PostMapping("/advanced")
    public ResponseEntity<SearchResultPage> advancedSearch(@RequestBody Map<String, Object> request) {
        String q = request.getOrDefault("query", "").toString();
        String type = request.containsKey("type") ? request.get("type").toString() : null;
        int page = request.containsKey("page") ? Integer.parseInt(request.get("page").toString()) : 0;
        int size = Math.min(request.containsKey("size") ? Integer.parseInt(request.get("size").toString()) : 20, 100);
        return ResponseEntity.ok(searchService.search(q, type, page, size));
    }

    /**
     * Remove a specific entity from the search index.
     * DELETE /api/v1/search/index/{entityType}/{entityId}
     */
    @DeleteMapping("/index/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFromIndex(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase());
        indexService.removeFromIndex(type, entityId);
        return ResponseEntity.noContent().build();
    }
}
