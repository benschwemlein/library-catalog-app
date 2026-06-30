package com.example.library.pattern.factory;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.Fine;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory for building report data structures.
 *
 * <p>Each method returns a {@code Map<String, Object>} containing the report payload.
 * Callers may serialize this to JSON, render it in a template, or convert it to a DTO.</p>
 */
@Component
@Slf4j
public class ReportFactory {

    /**
     * Build an overdue-loans report summarising all currently overdue loans.
     *
     * @param loans list of loans to analyze (all statuses; non-overdue ones are filtered out)
     * @return report map containing: title, generatedAt, totalCount, totalFineEstimate, entries
     */
    public Map<String, Object> createOverdueReport(List<Loan> loans) {
        List<Loan> overdueLoans = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.OVERDUE ||
                        (l.getStatus() == LoanStatus.ACTIVE
                                && l.getDueDate() != null
                                && l.getDueDate().toLocalDate().isBefore(LocalDate.now())))
                .sorted(Comparator.comparing(Loan::getDueDate))
                .collect(Collectors.toList());

        BigDecimal totalFineEstimate = overdueLoans.stream()
                .map(this::estimateFine)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> entries = new ArrayList<>();
        for (Loan loan : overdueLoans) {
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate().toLocalDate(), LocalDate.now());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("loanId", loan.getId());
            entry.put("memberId", loan.getMember() != null ? loan.getMember().getId() : null);
            entry.put("membershipNumber", loan.getMember() != null ? loan.getMember().getMembershipNumber() : null);
            entry.put("bookTitle", loan.getBookCopy() != null && loan.getBookCopy().getBook() != null
                    ? loan.getBookCopy().getBook().getTitle() : "Unknown");
            entry.put("dueDate", loan.getDueDate() != null ? loan.getDueDate().toLocalDate().toString() : null);
            entry.put("daysOverdue", daysOverdue);
            entry.put("estimatedFine", estimateFine(loan));
            entries.add(entry);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "Overdue Loans Report");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("totalCount", overdueLoans.size());
        report.put("totalFineEstimate", totalFineEstimate);
        report.put("entries", entries);

        log.info("Generated overdue report: {} overdue loans, estimated fines=${}", overdueLoans.size(), totalFineEstimate);
        return report;
    }

    /**
     * Build a popular-books report ranking books by borrow count.
     *
     * @param bookCounts map of Book to borrow-count (e.g., from a query)
     * @return report map containing: title, generatedAt, totalBooksAnalyzed, topBooks
     */
    public Map<String, Object> createPopularBooksReport(Map<Book, Long> bookCounts) {
        List<Map<String, Object>> topBooks = bookCounts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .map(entry -> {
                    Book book = entry.getKey();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("bookId", book.getId());
                    item.put("isbn", book.getIsbn());
                    item.put("title", book.getTitle());
                    item.put("publicationYear", book.getPublicationYear());
                    item.put("borrowCount", entry.getValue());
                    item.put("copiesHeld", book.getCopies() != null ? book.getCopies().size() : 0);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "Popular Books Report");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("totalBooksAnalyzed", bookCounts.size());
        report.put("topBooks", topBooks);

        log.info("Generated popular books report: {} books analyzed", bookCounts.size());
        return report;
    }

    /**
     * Build a member-activity report summarising a member's borrowing history.
     *
     * @param member the member to report on
     * @param loans  all loans associated with the member (any status)
     * @return report map containing: title, generatedAt, member info, loanCount, overdueCount,
     *         activeCount, totalFinesPaid, currentFineBalance, loanHistory
     */
    public Map<String, Object> createMemberActivityReport(Member member, List<Loan> loans) {
        long loanCount    = loans.size();
        long overdueCount = loans.stream().filter(l -> l.getStatus() == LoanStatus.OVERDUE).count();
        long activeCount  = loans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();

        BigDecimal totalFinesPaid = loans.stream()
                .filter(l -> l.getFine() != null && l.getFine().getPaidDate() != null && !l.getFine().isWaived())
                .map(l -> l.getFine().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> loanHistory = loans.stream()
                .sorted(Comparator.comparing(Loan::getCheckoutDate, Comparator.reverseOrder()))
                .map(loan -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("loanId", loan.getId());
                    item.put("bookTitle", loan.getBookCopy() != null && loan.getBookCopy().getBook() != null
                            ? loan.getBookCopy().getBook().getTitle() : "Unknown");
                    item.put("checkoutDate", loan.getCheckoutDate() != null ? loan.getCheckoutDate().toLocalDate().toString() : null);
                    item.put("dueDate", loan.getDueDate() != null ? loan.getDueDate().toLocalDate().toString() : null);
                    item.put("returnDate", loan.getReturnDate() != null ? loan.getReturnDate().toLocalDate().toString() : null);
                    item.put("status", loan.getStatus() != null ? loan.getStatus().name() : null);
                    item.put("fineAmount", loan.getFine() != null ? loan.getFine().getAmount() : BigDecimal.ZERO);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> memberInfo = new LinkedHashMap<>();
        memberInfo.put("memberId", member.getId());
        memberInfo.put("membershipNumber", member.getMembershipNumber());
        memberInfo.put("membershipTier", member.getMembershipTier() != null ? member.getMembershipTier().name() : null);
        memberInfo.put("joinDate", member.getJoinDate() != null ? member.getJoinDate().toString() : null);
        memberInfo.put("expiryDate", member.getExpiryDate() != null ? member.getExpiryDate().toString() : null);
        memberInfo.put("active", member.isActive());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "Member Activity Report");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("member", memberInfo);
        report.put("loanCount", loanCount);
        report.put("activeCount", activeCount);
        report.put("overdueCount", overdueCount);
        report.put("totalFinesPaid", totalFinesPaid);
        report.put("currentFineBalance", member.getFineBalance());
        report.put("loanHistory", loanHistory);

        log.info("Generated member activity report: memberId={} loans={} overdue={}",
                member.getId(), loanCount, overdueCount);
        return report;
    }

    /**
     * Build a branch-statistics report summarising the state of a library branch.
     *
     * @param branch      the library branch to report on
     * @param copies      all book copies held at the branch
     * @param activeLoans all currently active (not returned) loans from this branch
     * @return report map containing: title, generatedAt, branchInfo, totalCopies,
     *         checkedOutCount, availableCount, damagedCount, lostCount, activeLoans
     */
    public Map<String, Object> createBranchStatsReport(LibraryBranch branch,
                                                        List<BookCopy> copies,
                                                        List<Loan> activeLoans) {
        long totalCopies    = copies.size();
        long checkedOut     = copies.stream().filter(c -> c.getStatus() == CopyStatus.CHECKED_OUT).count();
        long available      = copies.stream().filter(c -> c.getStatus() == CopyStatus.AVAILABLE).count();
        long onHold         = copies.stream().filter(c -> c.getStatus() == CopyStatus.ON_HOLD).count();
        long lost           = copies.stream().filter(c -> c.getStatus() == CopyStatus.LOST).count();
        long damaged        = copies.stream().filter(c -> c.getStatus() == CopyStatus.WITHDRAWN).count();

        Map<String, Object> branchInfo = new LinkedHashMap<>();
        branchInfo.put("branchId", branch.getId());
        branchInfo.put("name", branch.getName());
        branchInfo.put("address", branch.getAddress());
        branchInfo.put("city", branch.getCity());
        branchInfo.put("phone", branch.getPhone());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("title", "Branch Statistics Report");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("branch", branchInfo);
        report.put("totalCopies", totalCopies);
        report.put("checkedOutCount", checkedOut);
        report.put("availableCount", available);
        report.put("onHoldCount", onHold);
        report.put("lostCount", lost);
        report.put("damagedCount", damaged);
        report.put("activeLoansCount", activeLoans.size());

        log.info("Generated branch stats report: branchId={} name='{}' totalCopies={} available={} checkedOut={}",
                branch.getId(), branch.getName(), totalCopies, available, checkedOut);
        return report;
    }

    // --- Helpers ---

    /**
     * Estimate a fine for an overdue loan using the member's daily rate.
     * Uses a simple daily-rate * days calculation; does not apply caps.
     */
    private BigDecimal estimateFine(Loan loan) {
        if (loan.getMember() == null || loan.getMember().getMembershipTier() == null || loan.getDueDate() == null) {
            return BigDecimal.ZERO;
        }
        LocalDate dueDate = loan.getDueDate().toLocalDate();
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = loan.getMember().getMembershipTier().getDailyFineRate();
        return rate.multiply(BigDecimal.valueOf(daysOverdue));
    }
}
