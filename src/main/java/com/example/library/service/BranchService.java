package com.example.library.service;

import com.example.library.dto.BranchStatsDTO;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.LoanStatus;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BranchService {

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public List<LibraryBranch> findAll() {
        return libraryBranchRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LibraryBranch findById(Long id) {
        return libraryBranchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with id: " + id));
    }

    public LibraryBranch create(LibraryBranch branch) {
        return libraryBranchRepository.save(branch);
    }

    public LibraryBranch update(Long id, LibraryBranch updated) {
        LibraryBranch branch = findById(id);
        branch.setName(updated.getName());
        branch.setAddress(updated.getAddress());
        branch.setPhone(updated.getPhone());
        branch.setEmail(updated.getEmail());
        branch.setOpeningHours(updated.getOpeningHours());
        return libraryBranchRepository.save(branch);
    }

    public void delete(Long id) {
        LibraryBranch branch = findById(id);
        libraryBranchRepository.delete(branch);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getInventory(Long branchId) {
        LibraryBranch branch = findById(branchId);
        return branch.getCopies();
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getBranchInventory(Long branchId) {
        return getInventory(branchId);
    }

    @Transactional(readOnly = true)
    public BranchStatsDTO getStats(Long branchId) {
        LibraryBranch branch = findById(branchId);

        long totalCopies = branch.getCopies() != null ? branch.getCopies().size() : 0;
        long availableCopies = branch.getCopies() != null
                ? branch.getCopies().stream().filter(c -> CopyStatus.AVAILABLE.equals(c.getStatus())).count()
                : 0;

        var branchLoans = loanRepository.findActiveLoansByBranch(branchId);
        long activeLoans = branchLoans.stream().filter(l -> LoanStatus.ACTIVE.equals(l.getStatus())).count();
        long overdueLoans = branchLoans.stream()
                .filter(l -> LoanStatus.ACTIVE.equals(l.getStatus()) &&
                        l.getDueDate() != null && l.getDueDate().isBefore(LocalDateTime.now()))
                .count();

        BranchStatsDTO dto = new BranchStatsDTO();
        dto.setBranchId(branchId);
        dto.setBranchName(branch.getName());
        dto.setTotalCopies((int) totalCopies);
        dto.setAvailableCopies((int) availableCopies);
        dto.setActiveLoans((int) activeLoans);
        dto.setOverdueLoans((int) overdueLoans);

        return dto;
    }

    @Transactional(readOnly = true)
    public BranchStatsDTO getBranchStats(Long branchId) {
        return getStats(branchId);
    }
}
