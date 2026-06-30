package com.example.library.repository;

import com.example.library.entity.Hold;
import com.example.library.entity.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoldRepository extends JpaRepository<Hold, Long> {

    List<Hold> findByMember_IdAndStatus(Long memberId, HoldStatus status);

    @Query("SELECT h FROM Hold h JOIN FETCH h.book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH h.member m LEFT JOIN FETCH m.user WHERE m.id = :memberId ORDER BY h.requestDate DESC")
    List<Hold> findByMemberIdWithDetails(@Param("memberId") Long memberId);

    List<Hold> findByBook_IdAndStatus(Long bookId, HoldStatus status);

    List<Hold> findByPickupBranch_IdAndStatus(Long branchId, HoldStatus status);

    @Query("SELECT h FROM Hold h WHERE h.expiryDate < :now AND h.status IN ('PENDING', 'READY')")
    List<Hold> findExpiredHolds(@Param("now") LocalDateTime now);

    long countByBook_IdAndStatus(Long bookId, HoldStatus status);
}
