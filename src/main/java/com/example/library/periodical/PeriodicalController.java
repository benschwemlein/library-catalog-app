package com.example.library.periodical;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/library/periodicals")
@Slf4j
@RequiredArgsConstructor
public class PeriodicalController {

    private final PeriodicalService periodicalService;

    @GetMapping
    public ResponseEntity<Page<PeriodicalDTO>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Search periodicals query={} category={}", query, category);
        Page<PeriodicalDTO> results = periodicalService.searchPeriodicals(query, category, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeriodicalDTO> getPeriodical(@PathVariable Long id) {
        log.debug("Get periodical id={}", id);
        return ResponseEntity.ok(periodicalService.getPeriodical(id));
    }

    @PostMapping
    public ResponseEntity<PeriodicalDTO> createPeriodical(@RequestBody CreatePeriodicalRequest request) {
        log.debug("Create periodical title={}", request.getTitle());
        PeriodicalDTO created = periodicalService.createPeriodical(
                request.getTitle(),
                request.getIssn(),
                request.getPublisher(),
                request.getFrequency(),
                request.getCategory(),
                request.getDescription(),
                request.getBranchId(),
                request.isDigitalAccess(),
                request.getDigitalUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/issues")
    public ResponseEntity<List<PeriodicalIssueDTO>> getIssues(@PathVariable Long id) {
        log.debug("Get issues for periodical id={}", id);
        return ResponseEntity.ok(periodicalService.getIssues(id));
    }

    @GetMapping("/{id}/issues/current")
    public ResponseEntity<List<PeriodicalIssueDTO>> getCurrentIssues(@PathVariable Long id) {
        log.debug("Get current issues for periodical id={}", id);
        return ResponseEntity.ok(periodicalService.getCurrentIssues(id));
    }

    @PostMapping("/{id}/issues")
    public ResponseEntity<PeriodicalIssueDTO> addIssue(
            @PathVariable Long id,
            @RequestBody AddIssueRequest request) {
        log.debug("Add issue to periodical id={} vol={} num={}", id, request.getVolume(), request.getIssueNumber());
        PeriodicalIssueDTO created = periodicalService.addIssue(
                id,
                request.getVolume(),
                request.getIssueNumber(),
                request.getPublicationDate(),
                request.getCondition(),
                request.getLocation()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/issues/{issueId}/status")
    public ResponseEntity<PeriodicalIssueDTO> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody UpdateIssueStatusRequest request) {
        log.debug("Update issue id={} status={}", issueId, request.getStatus());
        return ResponseEntity.ok(periodicalService.updateIssueStatus(issueId, request.getStatus()));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.debug("Get all periodical categories");
        return ResponseEntity.ok(periodicalService.getCategories());
    }

    // -------------------------------------------------------------------------
    // Inner request classes
    // -------------------------------------------------------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePeriodicalRequest {
        private String title;
        private String issn;
        private String publisher;
        private PeriodicalFrequency frequency;
        private String category;
        private String description;
        private Long branchId;
        private boolean digitalAccess;
        private String digitalUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddIssueRequest {
        private int volume;
        private int issueNumber;
        private LocalDate publicationDate;
        private String condition;
        private String location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateIssueStatusRequest {
        private PeriodicalIssueStatus status;
    }
}
