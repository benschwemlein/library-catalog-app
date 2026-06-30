package com.example.library.interlibrary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/library/ill")
@Slf4j
@RequiredArgsConstructor
public class InterLibraryLoanController {

    private final InterLibraryLoanService illService;
    private final PartnerLibraryRepository partnerLibraryRepository;

    @GetMapping("/requests")
    public ResponseEntity<Page<InterLibraryLoanDTO>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/library/ill/requests");
        return ResponseEntity.ok(illService.getAllRequests(PageRequest.of(page, size)));
    }

    @PostMapping("/requests")
    public ResponseEntity<?> submitRequest(@RequestBody SubmitILLRequest request) {
        log.info("POST /api/library/ill/requests - bookTitle={}", request.getBookTitle());
        try {
            InterLibraryLoanDTO dto = illService.submitRequest(request);
            return ResponseEntity.status(201).body(dto);
        } catch (RuntimeException e) {
            log.warn("Failed to submit ILL request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<?> getRequest(@PathVariable Long id) {
        log.info("GET /api/library/ill/requests/{}", id);
        try {
            return ResponseEntity.ok(illService.getRequestById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requests/member/{memberId}")
    public ResponseEntity<List<InterLibraryLoanDTO>> getMemberRequests(@PathVariable Long memberId) {
        log.info("GET /api/library/ill/requests/member/{}", memberId);
        return ResponseEntity.ok(illService.getMemberRequests(memberId));
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<Page<InterLibraryLoanDTO>> getRequestsByStatus(
            @PathVariable ILLStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/library/ill/requests/status/{}", status);
        return ResponseEntity.ok(illService.getRequestsByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<InterLibraryLoanDTO>> getPendingRequests() {
        log.info("GET /api/library/ill/requests/pending");
        return ResponseEntity.ok(illService.getPendingRequests());
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("PUT /api/library/ill/requests/{}/approve", id);
        try {
            String staffName = body.get("staffName");
            String note = body.get("note");
            return ResponseEntity.ok(illService.approveRequest(id, staffName, note));
        } catch (RuntimeException e) {
            log.warn("Failed to approve ILL request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/order")
    public ResponseEntity<?> orderFromPartner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("PUT /api/library/ill/requests/{}/order", id);
        try {
            Long partnerLibraryId = Long.valueOf(body.get("partnerLibraryId").toString());
            return ResponseEntity.ok(illService.orderFromPartner(id, partnerLibraryId));
        } catch (RuntimeException e) {
            log.warn("Failed to order ILL request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/received")
    public ResponseEntity<?> markReceived(@PathVariable Long id) {
        log.info("PUT /api/library/ill/requests/{}/received", id);
        try {
            return ResponseEntity.ok(illService.markReceived(id));
        } catch (RuntimeException e) {
            log.warn("Failed to mark ILL request {} received: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/available")
    public ResponseEntity<?> markAvailable(@PathVariable Long id) {
        log.info("PUT /api/library/ill/requests/{}/available", id);
        try {
            return ResponseEntity.ok(illService.markAvailable(id));
        } catch (RuntimeException e) {
            log.warn("Failed to mark ILL request {} available: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/return")
    public ResponseEntity<?> processReturn(@PathVariable Long id) {
        log.info("PUT /api/library/ill/requests/{}/return", id);
        try {
            return ResponseEntity.ok(illService.processReturn(id));
        } catch (RuntimeException e) {
            log.warn("Failed to process return for ILL request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/requests/{id}/deny")
    public ResponseEntity<?> denyRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("PUT /api/library/ill/requests/{}/deny", id);
        try {
            String staffName = body.get("staffName");
            String reason = body.get("reason");
            return ResponseEntity.ok(illService.denyRequest(id, staffName, reason));
        } catch (RuntimeException e) {
            log.warn("Failed to deny ILL request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/partners")
    public ResponseEntity<List<PartnerLibrary>> getActivePartners() {
        log.info("GET /api/library/ill/partners");
        return ResponseEntity.ok(partnerLibraryRepository.findByActiveTrue());
    }
}
