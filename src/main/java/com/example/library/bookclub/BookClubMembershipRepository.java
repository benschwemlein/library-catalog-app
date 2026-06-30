package com.example.library.bookclub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookClubMembershipRepository extends JpaRepository<BookClubMembership, Long> {
    List<BookClubMembership> findByClubIdAndActiveTrue(Long clubId);
    List<BookClubMembership> findByMemberIdAndActiveTrue(Long memberId);
    Optional<BookClubMembership> findByClubIdAndMemberId(Long clubId, Long memberId);
    long countByClubIdAndActiveTrue(Long clubId);
}
