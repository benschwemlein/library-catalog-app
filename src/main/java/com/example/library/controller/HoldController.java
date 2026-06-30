package com.example.library.controller;

import com.example.library.dto.PlaceHoldRequest;
import com.example.library.entity.Hold;
import jakarta.validation.Valid;
import com.example.library.repository.HoldRepository;
import com.example.library.service.HoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holds")
public class HoldController {

    @Autowired
    private HoldService holdService;

    @Autowired
    private HoldRepository holdRepository;

    @GetMapping
    public ResponseEntity<List<Hold>> getAllHolds() {
        return ResponseEntity.ok(holdRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Hold> placeHold(@Valid @RequestBody PlaceHoldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holdService.place(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelHold(@PathVariable Long id) {
        holdService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hold> getHoldById(@PathVariable Long id) {
        return ResponseEntity.ok(holdService.findById(id));
    }

    @PostMapping("/{id}/fulfill")
    public ResponseEntity<Hold> fulfillHold(@PathVariable Long id) {
        return ResponseEntity.ok(holdService.fulfill(id));
    }
}
