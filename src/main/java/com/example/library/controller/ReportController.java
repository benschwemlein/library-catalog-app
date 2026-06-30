package com.example.library.controller;

import com.example.library.dto.BranchStatsDTO;
import com.example.library.dto.MemberActivityReportDTO;
import com.example.library.dto.MostBorrowedDTO;
import com.example.library.dto.OverdueReportDTO;
import com.example.library.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mostBorrowedCount", reportService.getMostBorrowedBooks(10).size());
        stats.put("overdueCount", reportService.getOverdueReport().size());
        stats.put("branchCount", reportService.getAllBranchStatistics().size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/most-borrowed")
    public ResponseEntity<List<MostBorrowedDTO>> getMostBorrowed(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getMostBorrowedBooks(limit));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<OverdueReportDTO>> getOverdueReport() {
        return ResponseEntity.ok(reportService.getOverdueReport());
    }

    @GetMapping("/member-activity/{memberId}")
    public ResponseEntity<MemberActivityReportDTO> getMemberActivityReport(@PathVariable Long memberId) {
        return ResponseEntity.ok(reportService.getMemberActivityReport(memberId));
    }

    @GetMapping("/member-activity")
    public ResponseEntity<MemberActivityReportDTO> getMemberActivityReportByParam(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String period) {
        if (memberId == null) return ResponseEntity.ok(new MemberActivityReportDTO());
        return ResponseEntity.ok(reportService.getMemberActivityReport(memberId));
    }

    @GetMapping("/branch-stats/{branchId}")
    public ResponseEntity<BranchStatsDTO> getBranchStats(@PathVariable Long branchId) {
        return ResponseEntity.ok(reportService.getBranchStatistics(branchId));
    }

    @GetMapping("/branch-stats")
    public ResponseEntity<List<BranchStatsDTO>> getAllBranchStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(reportService.getAllBranchStatistics());
    }
}
