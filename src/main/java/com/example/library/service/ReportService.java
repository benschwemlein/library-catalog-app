package com.example.library.service;

import com.example.library.dto.BranchStatsDTO;
import com.example.library.dto.MemberActivityReportDTO;
import com.example.library.dto.MostBorrowedDTO;
import com.example.library.dto.OverdueReportDTO;
import com.example.library.entity.*;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    private String getMemberName(Member member) {
        if (member.getUser() != null) {
            return member.getUser().getFirstName() + " " + member.getUser().getLastName();
        }
        return member.getMembershipNumber();
    }

    public List<MostBorrowedDTO> getMostBorrowedBooks(int limit) {
        List<Loan> allLoans = loanRepository.findAll();

        Map<Book, Long> loanCounts = allLoans.stream()
                .filter(l -> l.getBookCopy() != null && l.getBookCopy().getBook() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getBookCopy().getBook(),
                        Collectors.counting()
                ));

        return loanCounts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    MostBorrowedDTO dto = new MostBorrowedDTO();
                    dto.setBookId(e.getKey().getId());
                    dto.setTitle(e.getKey().getTitle());
                    dto.setIsbn(e.getKey().getIsbn());
                    dto.setBorrowCount(e.getValue().intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<OverdueReportDTO> getOverdueReport() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());

        return overdueLoans.stream().map(loan -> {
            OverdueReportDTO dto = new OverdueReportDTO();
            dto.setLoanId(loan.getId());
            dto.setMemberName(getMemberName(loan.getMember()));
            dto.setMembershipNumber(loan.getMember().getMembershipNumber());
            dto.setBookTitle(loan.getBookCopy().getBook().getTitle());
            dto.setDueDate(loan.getDueDate());
            long daysOverdue = (LocalDateTime.now().toLocalDate().toEpochDay()
                    - loan.getDueDate().toLocalDate().toEpochDay());
            dto.setDaysOverdue((int) Math.max(0, daysOverdue));
            return dto;
        }).collect(Collectors.toList());
    }

    public MemberActivityReportDTO getMemberActivityReport(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        List<Loan> loans = loanRepository.findByMember_Id(memberId);

        MemberActivityReportDTO report = new MemberActivityReportDTO();
        report.setMemberId(memberId);
        report.setMemberName(getMemberName(member));
        report.setMembershipNumber(member.getMembershipNumber());
        report.setTotalLoans(loans.size());

        long activeLoans = loans.stream().filter(l -> LoanStatus.ACTIVE.equals(l.getStatus())).count();
        report.setCurrentLoans((int) activeLoans);

        long overdueLoans = loans.stream()
                .filter(l -> LoanStatus.ACTIVE.equals(l.getStatus()) &&
                        l.getDueDate() != null && l.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        report.setOverdueCount((int) overdueLoans);

        report.setHolds(member.getHolds() != null ? member.getHolds().size() : 0);

        return report;
    }

    public BranchStatsDTO getBranchStatistics(Long branchId) {
        LibraryBranch branch = libraryBranchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

        List<Loan> branchLoans = loanRepository.findActiveLoansByBranch(branchId);
        long activeLoans = branchLoans.stream().filter(l -> LoanStatus.ACTIVE.equals(l.getStatus())).count();
        long overdueLoans = branchLoans.stream()
                .filter(l -> LoanStatus.ACTIVE.equals(l.getStatus()) &&
                        l.getDueDate() != null && l.getDueDate().isBefore(LocalDateTime.now()))
                .count();

        long totalCopies = branch.getCopies() != null ? branch.getCopies().size() : 0;
        long availableCopies = branch.getCopies() != null
                ? branch.getCopies().stream().filter(c -> CopyStatus.AVAILABLE.equals(c.getStatus())).count()
                : 0;

        BranchStatsDTO dto = new BranchStatsDTO();
        dto.setBranchId(branchId);
        dto.setBranchName(branch.getName());
        dto.setTotalCopies((int) totalCopies);
        dto.setAvailableCopies((int) availableCopies);
        dto.setActiveLoans((int) activeLoans);
        dto.setOverdueLoans((int) overdueLoans);

        return dto;
    }

    public List<BranchStatsDTO> getAllBranchStatistics() {
        return libraryBranchRepository.findAll().stream()
                .map(b -> getBranchStatistics(b.getId()))
                .collect(Collectors.toList());
    }

    public List<String> getPopularGenres(int limit) {
        List<Loan> allLoans = loanRepository.findAll();

        Map<String, Long> genreCounts = new HashMap<>();
        for (Loan loan : allLoans) {
            if (loan.getBookCopy() != null && loan.getBookCopy().getBook() != null
                    && loan.getBookCopy().getBook().getGenres() != null) {
                for (Genre genre : loan.getBookCopy().getBook().getGenres()) {
                    genreCounts.merge(genre.getName(), 1L, Long::sum);
                }
            }
        }

        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
