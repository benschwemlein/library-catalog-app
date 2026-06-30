package com.example.library.recommendation;

import com.example.library.entity.Member;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationEngine {

    private final HybridRecommendationService hybridRecommendationService;
    private final RecommendationCache recommendationCache;
    private final MemberRepository memberRepository;

    public List<RecommendationDTO> getRecommendations(Long memberId, int limit) {
        log.info("Fetching recommendations for memberId={}, limit={}", memberId, limit);

        Optional<List<RecommendationDTO>> cached = recommendationCache.get(memberId);
        if (cached.isPresent()) {
            log.info("Returning cached recommendations for memberId={}", memberId);
            return cached.get();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        List<RecommendationDTO> recommendations = hybridRecommendationService.getRecommendations(member, limit);
        recommendationCache.put(memberId, recommendations);

        log.info("Generated and cached {} recommendations for memberId={}", recommendations.size(), memberId);
        return recommendations;
    }

    public List<RecommendationDTO> getRecommendationsForCurrentMember(int limit) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching recommendations for current user username={}, limit={}", username, limit);

        Optional<Member> memberOpt = memberRepository.findByUser_Email(username);
        if (memberOpt.isEmpty()) {
            log.info("No member record for user {}, returning empty recommendations", username);
            return List.of();
        }

        return getRecommendations(memberOpt.get().getId(), limit);
    }
}
