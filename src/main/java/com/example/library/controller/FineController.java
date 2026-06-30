package com.example.library.controller;

import com.example.library.dto.PayFineRequest;
import com.example.library.dto.WaiveFineRequest;
import jakarta.validation.Valid;
import com.example.library.entity.Fine;
import com.example.library.repository.FineRepository;
import com.example.library.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fines")
public class FineController {

    @Autowired
    private FineService fineService;

    @Autowired
    private FineRepository fineRepository;

    @GetMapping
    public ResponseEntity<List<Fine>> getAllFines() {
        return ResponseEntity.ok(fineRepository.findAll());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Fine>> getFinesByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getUnpaidFines(memberId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Fine> payFine(@PathVariable Long id, @RequestBody(required = false) PayFineRequest req) {
        return ResponseEntity.ok(fineService.payFine(id, req != null ? req : new PayFineRequest()));
    }

    @PostMapping("/{id}/waive")
    public ResponseEntity<Fine> waiveFine(@PathVariable Long id, @Valid @RequestBody WaiveFineRequest req) {
        return ResponseEntity.ok(fineService.waiveFine(id, req));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<Fine>> getUnpaidFines(@RequestParam(required = false) Long memberId) {
        return ResponseEntity.ok(memberId != null ? fineService.getUnpaidFines(memberId) : List.of());
    }
}
