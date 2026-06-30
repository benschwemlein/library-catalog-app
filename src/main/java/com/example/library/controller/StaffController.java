package com.example.library.controller;

import com.example.library.entity.StaffMember;
import com.example.library.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<List<StaffMember>> getAllStaff() {
        return ResponseEntity.ok(staffService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffMember> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StaffMember> createStaff(@RequestBody StaffMember staffMember) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(staffMember));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffMember> updateStaff(@PathVariable Long id, @RequestBody StaffMember staffMember) {
        return ResponseEntity.ok(staffService.update(id, staffMember));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<StaffMember>> getStaffByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(staffService.findByBranch(branchId));
    }
}
