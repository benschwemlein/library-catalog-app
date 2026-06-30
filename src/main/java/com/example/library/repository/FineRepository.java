package com.example.library.repository;

import com.example.library.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByMember_IdAndPaidDateIsNull(Long memberId);

    List<Fine> findByLoan_Id(Long loanId);

    List<Fine> findByMember_Id(Long memberId);

    @Query("SELECT f FROM Fine f LEFT JOIN FETCH f.loan l LEFT JOIN FETCH l.bookCopy bc LEFT JOIN FETCH bc.book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH f.member m LEFT JOIN FETCH m.user WHERE m.id = :memberId ORDER BY f.issuedDate DESC")
    List<Fine> findByMemberIdWithDetails(@Param("memberId") Long memberId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.member.id = :memberId AND f.paidDate IS NULL AND f.waived = false")
    BigDecimal sumUnpaidFinesByMember(@Param("memberId") Long memberId);
}
