package com.example.library.controller;

import com.example.library.dto.BranchStatsDTO;
import com.example.library.entity.BookCopy;
import com.example.library.entity.LibraryBranch;
import com.example.library.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @GetMapping
    public ResponseEntity<List<LibraryBranch>> getAllBranches() {
        return ResponseEntity.ok(branchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryBranch> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LibraryBranch> createBranch(@RequestBody LibraryBranch branch) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(branch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryBranch> updateBranch(@PathVariable Long id, @RequestBody LibraryBranch branch) {
        return ResponseEntity.ok(branchService.update(id, branch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<BranchStatsDTO>> getAllBranchStats() {
        List<BranchStatsDTO> stats = branchService.findAll().stream()
                .map(b -> branchService.getStats(b.getId()))
                .toList();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<List<BookCopy>> getBranchInventory(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getInventory(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<BranchStatsDTO> getBranchStats(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getStats(id));
    }
}
