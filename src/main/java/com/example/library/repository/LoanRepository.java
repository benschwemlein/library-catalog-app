package com.example.library.repository;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMember_IdAndStatus(Long memberId, LoanStatus status);

    List<Loan> findByDueDateBeforeAndStatus(LocalDateTime date, LoanStatus status);

    List<Loan> findByMember_Id(Long memberId);

    List<Loan> findByBookCopy_Id(Long bookCopyId);

    @Query("SELECT l FROM Loan l WHERE l.branch.id = :branchId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansByBranch(@Param("branchId") Long branchId);

    @Query("SELECT l FROM Loan l WHERE l.dueDate < :now AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("now") LocalDateTime now);

    List<Loan> findByMember_IdAndBookCopy_Book_Id(Long memberId, Long bookId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.member.id = :memberId AND l.status IN :statuses")
    long countByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") java.util.List<LoanStatus> statuses);
}
