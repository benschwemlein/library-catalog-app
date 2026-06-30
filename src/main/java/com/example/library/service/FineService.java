package com.example.library.service;

import com.example.library.dto.PayFineRequest;
import com.example.library.dto.WaiveFineRequest;
import com.example.library.entity.*;
import com.example.library.exception.LoanNotFoundException;
import com.example.library.repository.FineRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.util.FineCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FineService {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineCalculator fineCalculator;

    @Transactional(readOnly = true)
    public BigDecimal calculateFine(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        LocalDateTime returnDate = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now();
        return fineCalculator.calculateFine(loan.getDueDate(), returnDate, loan.getMember().getMembershipTier());
    }

    public Fine issueFine(Loan loan) {
        LocalDateTime returnDate = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDateTime.now();
        BigDecimal amount = fineCalculator.calculateFine(loan.getDueDate(), returnDate, loan.getMember().getMembershipTier());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        Fine fine = new Fine();
        fine.setLoan(loan);
        fine.setMember(loan.getMember());
        fine.setAmount(amount);
        fine.setIssuedDate(LocalDateTime.now());
        fine.setReason("Overdue: " + loan.getBookCopy().getBook().getTitle());

        return fineRepository.save(fine);
    }

    public Fine payFine(Long fineId, PayFineRequest req) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with id: " + fineId));

        if (fine.getPaidDate() != null) {
            throw new IllegalStateException("Fine is already paid.");
        }

        fine.setPaidDate(LocalDateTime.now());

        return fineRepository.save(fine);
    }

    public Fine waiveFine(Long fineId, WaiveFineRequest req) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with id: " + fineId));

        if (fine.getPaidDate() != null) {
            throw new IllegalStateException("Fine is already paid and cannot be waived.");
        }

        fine.setWaived(true);
        fine.setPaidDate(LocalDateTime.now());
        fine.setWaivedReason(req.getReason());
        fine.setWaivedBy(req.getWaivedBy());

        return fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public List<Fine> getUnpaidFines(Long memberId) {
        return fineRepository.findByMember_IdAndPaidDateIsNull(memberId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidFines(Long memberId) {
        List<Fine> unpaidFines = getUnpaidFines(memberId);
        return unpaidFines.stream()
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
