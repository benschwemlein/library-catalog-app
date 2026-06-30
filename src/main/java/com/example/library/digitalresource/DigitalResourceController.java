package com.example.library.digitalresource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/library/digital")
@Slf4j
@RequiredArgsConstructor
public class DigitalResourceController {

    private final DigitalResourceService digitalResourceService;

    @GetMapping({"", "/"})
    public ResponseEntity<List<DigitalResourceDTO>> searchResources(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) DigitalResourceType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        log.info("GET /api/library/digital/ - query='{}', type={}, page={}, size={}", query, type, page, size);
        try {
            PageRequest pageable = PageRequest.of(page, size);
            Page<DigitalResourceDTO> results = digitalResourceService.searchResources(query, type, pageable);
            return ResponseEntity.ok(results.getContent());
        } catch (Exception e) {
            log.error("Error searching digital resources", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalResourceDTO> getResource(@PathVariable Long id) {
        log.info("GET /api/library/digital/{}", id);
        try {
            DigitalResourceDTO resource = digitalResourceService.getResource(id);
            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            log.error("Error getting digital resource: id={}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<DigitalLoanDTO> checkout(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long memberId = body.get("memberId");
        log.info("POST /api/library/digital/{}/checkout - memberId={}", id, memberId);
        try {
            DigitalLoanDTO loan = digitalResourceService.checkout(memberId, id);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            log.error("Error checking out digital resource: resourceId={}, memberId={}", id, memberId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/loans/{loanId}/return")
    public ResponseEntity<DigitalLoanDTO> returnResource(
            @PathVariable Long loanId,
            @RequestBody Map<String, Long> body) {
        Long memberId = body.get("memberId");
        Long resourceId = body.get("resourceId");
        log.info("POST /api/library/digital/loans/{}/return - memberId={}, resourceId={}", loanId, memberId, resourceId);
        try {
            DigitalLoanDTO loan = digitalResourceService.returnResource(memberId, resourceId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            log.error("Error returning digital resource: loanId={}, memberId={}, resourceId={}", loanId, memberId, resourceId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/loans/{loanId}/download")
    public ResponseEntity<DigitalLoanDTO> trackDownload(@PathVariable Long loanId) {
        log.info("POST /api/library/digital/loans/{}/download", loanId);
        try {
            DigitalLoanDTO loan = digitalResourceService.trackDownload(loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            log.error("Error tracking download: loanId={}", loanId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/member/{memberId}/loans")
    public ResponseEntity<List<DigitalLoanDTO>> getMemberLoans(@PathVariable Long memberId) {
        log.info("GET /api/library/digital/member/{}/loans", memberId);
        try {
            List<DigitalLoanDTO> loans = digitalResourceService.getMemberLoans(memberId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            log.error("Error getting member loans: memberId={}", memberId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
