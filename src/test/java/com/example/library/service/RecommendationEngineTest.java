package com.example.library.service;

import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.recommendation.HybridRecommendationService;
import com.example.library.recommendation.RecommendationCache;
import com.example.library.recommendation.RecommendationDTO;
import com.example.library.recommendation.RecommendationEngine;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineTest {

    @Mock
    private HybridRecommendationService hybridRecommendationService;

    @Mock
    private RecommendationCache recommendationCache;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecommendationEngine engine;

    private Member member;
    private List<RecommendationDTO> cachedRecommendations;
    private List<RecommendationDTO> computedRecommendations;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .membershipNumber("M001")
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();

        cachedRecommendations = List.of(
                RecommendationDTO.builder().bookId(10L).title("Cached Book 1").build(),
                RecommendationDTO.builder().bookId(20L).title("Cached Book 2").build()
        );

        computedRecommendations = List.of(
                RecommendationDTO.builder().bookId(30L).title("Computed Book 1").build(),
                RecommendationDTO.builder().bookId(40L).title("Computed Book 2").build(),
                RecommendationDTO.builder().bookId(50L).title("Computed Book 3").build()
        );
    }

    @Test
    void getRecommendations_cacheHit_skipComputation() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.of(cachedRecommendations));

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 5);

        assertThat(result).isEqualTo(cachedRecommendations);
        verify(memberRepository, never()).findById(any());
        verify(hybridRecommendationService, never()).getRecommendations(any(), anyInt());
    }

    @Test
    void getRecommendations_cacheMiss_computesAndCaches() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 5)).thenReturn(computedRecommendations);

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 5);

        assertThat(result).isEqualTo(computedRecommendations);
        verify(hybridRecommendationService).getRecommendations(member, 5);
    }

    @Test
    void getRecommendations_memberNotFound_throwsRuntimeException() {
        when(recommendationCache.get(99L)).thenReturn(Optional.empty());
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> engine.getRecommendations(99L, 5));
    }

    @Test
    void getRecommendations_newMemberNoHistory_returnsPopularBooks() {
        List<RecommendationDTO> popularBooks = List.of(
                RecommendationDTO.builder().bookId(100L).title("Popular Book").score(0.9).build()
        );
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 5)).thenReturn(popularBooks);

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 5);

        assertThat(result).isEqualTo(popularBooks);
    }

    @Test
    void getRecommendations_returnsCorrectNumberOfRecommendations() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 3)).thenReturn(computedRecommendations);

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 3);

        assertThat(result).hasSize(3);
    }

    @Test
    void getRecommendations_storeResultInCache() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 5)).thenReturn(computedRecommendations);

        engine.getRecommendations(member.getId(), 5);

        verify(recommendationCache).put(member.getId(), computedRecommendations);
    }

    @Test
    void getRecommendations_cacheHit_doesNotCallMemberRepository() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.of(cachedRecommendations));

        engine.getRecommendations(member.getId(), 10);

        verifyNoInteractions(memberRepository);
        verifyNoInteractions(hybridRecommendationService);
    }

    @Test
    void getRecommendations_cacheHit_returnsFullCachedList() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.of(cachedRecommendations));

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 1); // limit param ignored on cache hit

        assertThat(result).hasSize(2); // returns full cached list
        assertThat(result).containsExactlyElementsOf(cachedRecommendations);
    }

    @Test
    void getRecommendations_memberNotFound_messageContainsMemberId() {
        Long missingId = 999L;
        when(recommendationCache.get(missingId)).thenReturn(Optional.empty());
        when(memberRepository.findById(missingId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> engine.getRecommendations(missingId, 5));

        assertThat(ex.getMessage()).contains("999");
    }

    @Test
    void getRecommendations_emptyResultFromHybrid_cachesEmptyList() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 5)).thenReturn(Collections.emptyList());

        List<RecommendationDTO> result = engine.getRecommendations(member.getId(), 5);

        assertThat(result).isEmpty();
        verify(recommendationCache).put(member.getId(), Collections.emptyList());
    }

    @Test
    void getRecommendations_cacheMiss_memberRetrievedById() {
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, 5)).thenReturn(computedRecommendations);

        engine.getRecommendations(member.getId(), 5);

        verify(memberRepository).findById(member.getId());
    }

    @Test
    void getRecommendations_hybridServiceCalledWithCorrectLimit() {
        int customLimit = 7;
        when(recommendationCache.get(member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(hybridRecommendationService.getRecommendations(member, customLimit)).thenReturn(computedRecommendations);

        engine.getRecommendations(member.getId(), customLimit);

        verify(hybridRecommendationService).getRecommendations(member, customLimit);
    }
}
