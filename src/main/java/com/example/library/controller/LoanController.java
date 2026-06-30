package com.example.library.controller;

import com.example.library.dto.CheckoutRequestDTO;
import com.example.library.entity.Loan;
import com.example.library.repository.LoanRepository;
import com.example.library.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.library.entity.LoanStatus;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanRepository.findAll());
    }

    @PostMapping("/checkout")
    public ResponseEntity<Loan> checkout(@RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.checkout(request));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<Loan> renewLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.renew(id));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.findOverdue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.findById(id));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Loan>> getLoansDueToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Loan> dueToday = loanRepository.findAll().stream()
                .filter(l -> l.getDueDate() != null
                        && l.getDueDate().isAfter(startOfDay)
                        && l.getDueDate().isBefore(endOfDay)
                        && LoanStatus.ACTIVE.equals(l.getStatus()))
                .toList();
        return ResponseEntity.ok(dueToday);
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Loan> getLoanByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(loanRepository.findAll().stream()
                .filter(l -> barcode.equals(l.getBookCopy().getBarcode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active loan for barcode: " + barcode)));
    }
}
