package com.example.library.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RecommendationCache {

    private static final long TTL_MINUTES = 30;

    private final ConcurrentHashMap<Long, CachedRecommendations> cache = new ConcurrentHashMap<>();

    public void put(Long memberId, List<RecommendationDTO> results) {
        CachedRecommendations entry = new CachedRecommendations(results, LocalDateTime.now().plusMinutes(TTL_MINUTES));
        cache.put(memberId, entry);
        log.info("Cache put for memberId={}, expiresAt={}", memberId, entry.getExpiresAt());
    }

    public Optional<List<RecommendationDTO>> get(Long memberId) {
        CachedRecommendations entry = cache.get(memberId);
        if (entry == null) {
            log.debug("Cache miss for memberId={}", memberId);
            return Optional.empty();
        }
        if (LocalDateTime.now().isAfter(entry.getExpiresAt())) {
            log.info("Cache entry expired for memberId={}, evicting", memberId);
            cache.remove(memberId);
            return Optional.empty();
        }
        log.debug("Cache hit for memberId={}", memberId);
        return Optional.of(entry.getResults());
    }

    public void evict(Long memberId) {
        CachedRecommendations removed = cache.remove(memberId);
        if (removed != null) {
            log.info("Cache evicted for memberId={}", memberId);
        } else {
            log.debug("Cache evict requested for memberId={} but no entry found", memberId);
        }
    }

    public void evictAll() {
        int size = cache.size();
        cache.clear();
        log.info("Cache evicted all {} entries", size);
    }

    public static class CachedRecommendations {
        private final List<RecommendationDTO> results;
        private final LocalDateTime expiresAt;

        public CachedRecommendations(List<RecommendationDTO> results, LocalDateTime expiresAt) {
            this.results = results;
            this.expiresAt = expiresAt;
        }

        public List<RecommendationDTO> getResults() {
            return results;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }
}
