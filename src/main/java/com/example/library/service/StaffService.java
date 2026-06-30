package com.example.library.service;

import com.example.library.entity.LibraryBranch;
import com.example.library.entity.StaffMember;
import com.example.library.entity.StaffRole;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.StaffMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StaffService {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Transactional(readOnly = true)
    public List<StaffMember> findAll() {
        return staffMemberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public StaffMember findById(Long id) {
        return staffMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with id: " + id));
    }

    public StaffMember create(StaffMember staffMember) {
        if (staffMember.getBranch() != null && staffMember.getBranch().getId() != null) {
            libraryBranchRepository.findById(staffMember.getBranch().getId())
                    .ifPresent(staffMember::setBranch);
        }
        return staffMemberRepository.save(staffMember);
    }

    public StaffMember update(Long id, StaffMember updated) {
        StaffMember staff = findById(id);
        staff.setRole(updated.getRole());

        if (updated.getBranch() != null && updated.getBranch().getId() != null) {
            libraryBranchRepository.findById(updated.getBranch().getId())
                    .ifPresent(staff::setBranch);
        }

        return staffMemberRepository.save(staff);
    }

    public void delete(Long id) {
        StaffMember staff = findById(id);
        staffMemberRepository.delete(staff);
    }

    @Transactional(readOnly = true)
    public List<StaffMember> getStaffByBranch(Long branchId) {
        return staffMemberRepository.findByBranch_Id(branchId);
    }

    @Transactional(readOnly = true)
    public List<StaffMember> findByBranch(Long branchId) {
        return getStaffByBranch(branchId);
    }

    @Transactional(readOnly = true)
    public List<StaffMember> getStaffByRole(StaffRole role) {
        return staffMemberRepository.findByRole(role.name());
    }
}
