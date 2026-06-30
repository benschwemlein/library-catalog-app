package com.example.library.interlibrary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterLibraryLoanRepository extends JpaRepository<InterLibraryLoanRequest, Long> {

    List<InterLibraryLoanRequest> findByRequestingMemberId(Long memberId);

    List<InterLibraryLoanRequest> findByStatus(ILLStatus status);

    List<InterLibraryLoanRequest> findByRequestingBranchId(Long branchId);

    Page<InterLibraryLoanRequest> findByStatusIn(List<ILLStatus> statuses, Pageable pageable);
}
