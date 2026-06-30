package com.example.library.acquisition;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcquisitionRequestRepository extends JpaRepository<AcquisitionRequest, Long> {

    List<AcquisitionRequest> findByStatus(AcquisitionStatus status);

    List<AcquisitionRequest> findByPriority(AcquisitionPriority priority);

    Page<AcquisitionRequest> findByStatusIn(List<AcquisitionStatus> statuses, Pageable pageable);

    List<AcquisitionRequest> findByTargetBranchId(Long branchId);

    List<AcquisitionRequest> findByRequestedByMemberId(Long memberId);
}
