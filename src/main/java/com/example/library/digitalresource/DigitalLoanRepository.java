package com.example.library.digitalresource;

import com.example.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitalLoanRepository extends JpaRepository<DigitalLoan, Long> {

    List<DigitalLoan> findByMemberAndStatus(Member member, DigitalLoanStatus status);

    List<DigitalLoan> findByMemberId(Long memberId);

    List<DigitalLoan> findByResourceAndStatus(DigitalResource resource, DigitalLoanStatus status);

    long countByResourceAndStatus(DigitalResource resource, DigitalLoanStatus status);
}
