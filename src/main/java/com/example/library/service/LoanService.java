package com.example.library.service;

import com.example.library.annotation.AuditableOperation;
import com.example.library.dto.CheckoutRequest;
import com.example.library.dto.CheckoutRequestDTO;
import com.example.library.entity.*;
import com.example.library.exception.*;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LoanService {

    private static final BigDecimal UNPAID_FINES_THRESHOLD = new BigDecimal("10.00");
    private static final int MAX_CONCURRENT_LOANS = 5;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Autowired
    private FineService fineService;

    @Autowired
    private NotificationService notificationService;

    @AuditableOperation(action = "CHECKOUT", entityType = "Loan")
    public Loan checkout(CheckoutRequestDTO req) {
        if (req.getMemberId() == null && (req.getMembershipNumber() == null || req.getMembershipNumber().isBlank())) {
            throw new CheckoutValidationException("memberId or membershipNumber is required");
        }
        if (req.getBookCopyId() == null && (req.getCopyBarcode() == null || req.getCopyBarcode().isBlank())) {
            throw new CheckoutValidationException("bookCopyId or copyBarcode is required");
        }

        Member member = req.getMemberId() != null
                ? memberRepository.findById(req.getMemberId())
                        .orElseThrow(() -> new MemberNotFoundException("Member not found: " + req.getMemberId()))
                : memberRepository.findByMembershipNumber(req.getMembershipNumber())
                        .orElseThrow(() -> new MemberNotFoundException("Member not found: " + req.getMembershipNumber()));

        if (!member.isActive()) {
            throw new MembershipExpiredException("Member account is not active.");
        }
        if (member.getExpiryDate() != null && member.getExpiryDate().isBefore(LocalDate.now())) {
            throw new MembershipExpiredException("Membership has expired for member: " + member.getMembershipNumber());
        }

        BigDecimal totalUnpaid = fineService.getTotalUnpaidFines(member.getId());
        if (totalUnpaid.compareTo(UNPAID_FINES_THRESHOLD) > 0) {
            throw new UnpaidFinesException("Member has unpaid fines exceeding threshold: " + totalUnpaid);
        }

        long activeLoans = loanRepository.countByMemberIdAndStatusIn(
                member.getId(), List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
        if (activeLoans >= MAX_CONCURRENT_LOANS) {
            throw new ConcurrentLoanLimitException(
                    "Member has reached the maximum of " + MAX_CONCURRENT_LOANS + " concurrent loans.");
        }

        BookCopy copy = req.getBookCopyId() != null
                ? bookCopyRepository.findById(req.getBookCopyId())
                        .orElseThrow(() -> new CopyNotAvailableException("Book copy not found: " + req.getBookCopyId()))
                : bookCopyRepository.findByBarcode(req.getCopyBarcode())
                        .orElseThrow(() -> new CopyNotAvailableException("Book copy not found: " + req.getCopyBarcode()));

        if (!CopyStatus.AVAILABLE.equals(copy.getStatus())) {
            throw new CopyNotAvailableException("Book copy is not available. Current status: " + copy.getStatus());
        }

        int loanDays = getLoanDaysForTier(member.getMembershipTier());

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBookCopy(copy);
        loan.setCheckoutDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(loanDays));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setRenewalCount(0);

        // Use first available branch if none specified
        if (copy.getBranch() != null) {
            loan.setBranch(copy.getBranch());
        }

        copy.setStatus(CopyStatus.CHECKED_OUT);
        bookCopyRepository.save(copy);

        Loan savedLoan = loanRepository.save(loan);

        notificationService.sendNotification(
                member.getId(),
                NotificationType.DUE_SOON,
                "You have checked out: " + copy.getBook().getTitle() + ". Due date: " + loan.getDueDate(),
                NotificationChannel.EMAIL
        );

        return savedLoan;
    }

    public Loan checkout(CheckoutRequest req) {
        CheckoutRequestDTO dto = new CheckoutRequestDTO();
        dto.setMemberId(req.getMemberId());
        dto.setBookCopyId(req.getCopyId());
        return checkout(dto);
    }

    public Loan returnBook(Long loanId) {
        Loan loan = findById(loanId);

        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RETURNED);

        BookCopy copy = loan.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        if (loan.getDueDate() != null && LocalDateTime.now().isAfter(loan.getDueDate())) {
            fineService.issueFine(loan);
        }

        Loan savedLoan = loanRepository.save(loan);

        notificationService.sendNotification(
                loan.getMember().getId(),
                NotificationType.FINE_ISSUED,
                "Thank you for returning: " + copy.getBook().getTitle(),
                NotificationChannel.EMAIL
        );

        return savedLoan;
    }

    @AuditableOperation(action = "RENEW", entityType = "Loan")
    public Loan renewLoan(Long loanId) {
        return renew(loanId);
    }

    public Loan renew(Long loanId) {
        Loan loan = findById(loanId);

        int maxRenewals = getMaxRenewalsForTier(loan.getMember().getMembershipTier());
        if (loan.getRenewalCount() >= maxRenewals) {
            throw new MaxRenewalsExceededException("Maximum renewals (" + maxRenewals + ") exceeded for loan: " + loanId);
        }

        int loanDays = getLoanDaysForTier(loan.getMember().getMembershipTier());
        loan.setDueDate(loan.getDueDate().plusDays(loanDays));
        loan.setRenewalCount(loan.getRenewalCount() + 1);

        Loan savedLoan = loanRepository.save(loan);

        notificationService.sendNotification(
                loan.getMember().getId(),
                NotificationType.DUE_SOON,
                "Your loan for '" + loan.getBookCopy().getBook().getTitle() + "' has been renewed. New due date: " + loan.getDueDate(),
                NotificationChannel.EMAIL
        );

        return savedLoan;
    }

    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans() {
        return findOverdue();
    }

    @Transactional(readOnly = true)
    public List<Loan> findOverdue() {
        return loanRepository.findOverdueLoans(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoanHistory(Long memberId) {
        return loanRepository.findByMember_Id(memberId);
    }

    @Transactional(readOnly = true)
    public Loan findById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));
    }

    private int getLoanDaysForTier(MembershipTier tier) {
        if (tier == null) return 14;
        switch (tier) {
            case PREMIUM: return 28;
            case STUDENT: return 21;
            case STANDARD: return 21;
            default: return 14;
        }
    }

    private int getMaxRenewalsForTier(MembershipTier tier) {
        if (tier == null) return 2;
        switch (tier) {
            case PREMIUM: return 5;
            case STUDENT: return 3;
            case STANDARD: return 3;
            default: return 2;
        }
    }
}
