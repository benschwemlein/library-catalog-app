package com.example.library.recommendation;

import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/recommendations")
@Slf4j
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final RecommendationCache recommendationCache;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<RecommendationDTO>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/library/recommendations?limit={}", limit);
        List<RecommendationDTO> recommendations = recommendationEngine.getRecommendationsForCurrentMember(limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/me")
    public ResponseEntity<List<RecommendationDTO>> getRecommendationsForCurrentMember(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/library/recommendations/me?limit={}", limit);
        List<RecommendationDTO> recommendations = recommendationEngine.getRecommendationsForCurrentMember(limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RecommendationDTO>> getRecommendationsForMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/library/recommendations/member/{}?limit={}", memberId, limit);
        List<RecommendationDTO> recommendations = recommendationEngine.getRecommendations(memberId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @DeleteMapping("/cache/member/{memberId}")
    public ResponseEntity<Void> evictMemberCache(@PathVariable Long memberId) {
        log.info("DELETE /api/library/recommendations/cache/member/{}", memberId);
        recommendationCache.evict(memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Void> evictAllCache() {
        log.info("DELETE /api/library/recommendations/cache");
        recommendationCache.evictAll();
        return ResponseEntity.noContent().build();
    }
}
