package com.example.library.repository;

import com.example.library.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    List<StaffMember> findByBranch_IdAndRole(Long branchId, String role);

    List<StaffMember> findByRole(String role);

    List<StaffMember> findByBranch_Id(Long branchId);

    Optional<StaffMember> findByUser_Id(Long userId);
}
