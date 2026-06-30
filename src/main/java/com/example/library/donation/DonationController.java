package com.example.library.donation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/library/donations")
@Slf4j
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    // ---------------------------------------------------------------------------
    // Inner request classes
    // ---------------------------------------------------------------------------

    @Data
    static class RecordDonationRequest {
        private String donorName;
        private String donorEmail;
        private String donorPhone;
        private String donorAddress;
        private List<DonationItemDTO> items;
        private Long branchId;
    }

    @Data
    static class ReviewRequest {
        private String reviewerName;
        private String notes;
        private DonationStatus status;
    }

    @Data
    static class DispositionRequest {
        private ItemDisposition disposition;
        private String notes;
        private Long bookId;
    }

    // ---------------------------------------------------------------------------
    // Endpoints
    // ---------------------------------------------------------------------------

    /**
     * POST /api/library/donations
     * Record a new donation. Returns 201 Created with the created DonationDTO.
     */
    @PostMapping
    public ResponseEntity<DonationDTO> recordDonation(@RequestBody RecordDonationRequest request) {
        log.info("Recording donation from donor '{}'", request.getDonorName());
        DonationDTO created = donationService.recordDonation(
                request.getDonorName(),
                request.getDonorEmail(),
                request.getDonorPhone(),
                request.getDonorAddress(),
                request.getItems(),
                request.getBranchId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/library/donations/{id}
     * Retrieve a single donation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DonationDTO> getDonation(@PathVariable Long id) {
        DonationDTO dto = donationService.getDonation(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/library/donations
     * List donations, optionally filtered by status. Supports pagination.
     */
    @GetMapping
    public ResponseEntity<Page<DonationDTO>> listDonations(
            @RequestParam(required = false) DonationStatus status,
            @PageableDefault(size = 20, sort = "donationDate") Pageable pageable) {
        Page<DonationDTO> page = donationService.getDonationsByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * PUT /api/library/donations/{id}/review
     * Review a donation and set its decision status.
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<DonationDTO> reviewDonation(
            @PathVariable Long id,
            @RequestBody ReviewRequest request) {
        log.info("Reviewing donation {} with decision {}", id, request.getStatus());
        DonationDTO dto = donationService.reviewDonation(
                id,
                request.getReviewerName(),
                request.getNotes(),
                request.getStatus()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/library/donations/items/{itemId}/disposition
     * Set the final disposition for a single donation item.
     */
    @PutMapping("/items/{itemId}/disposition")
    public ResponseEntity<DonationItemDTO> processItem(
            @PathVariable Long itemId,
            @RequestBody DispositionRequest request) {
        log.info("Processing item {} with disposition {}", itemId, request.getDisposition());
        DonationItemDTO dto = donationService.processItem(
                itemId,
                request.getDisposition(),
                request.getNotes(),
                request.getBookId()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/library/donations/{id}/acknowledge
     * Mark a donation as acknowledged and record the acknowledgement date.
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<DonationDTO> sendAcknowledgement(@PathVariable Long id) {
        log.info("Sending acknowledgement for donation {}", id);
        DonationDTO dto = donationService.sendAcknowledgement(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/library/donations/stats
     * Return aggregate statistics for donations and items.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDonationStats() {
        Map<String, Object> stats = donationService.getDonationStats();
        return ResponseEntity.ok(stats);
    }
}
