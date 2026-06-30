package com.example.library.repository;

import com.example.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMembershipNumber(String membershipNumber);

    List<Member> findByMembershipTier(String membershipTier);

    List<Member> findByExpiryDateBefore(LocalDate date);

    List<Member> findByFineBalanceGreaterThan(BigDecimal amount);

    List<Member> findByActiveTrue();

    Optional<Member> findByUser_Email(String email);

    Optional<Member> findByUser_Id(Long userId);
}
