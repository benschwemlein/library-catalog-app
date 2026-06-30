package com.example.library.acquisition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/library/acquisitions")
@Slf4j
@RequiredArgsConstructor
public class AcquisitionController {

    private final AcquisitionService acquisitionService;

    // -------------------------------------------------------------------------
    // Inner request body classes
    // -------------------------------------------------------------------------

    static class MemberRequestBody {
        public Long memberId;
        public String title;
        public String author;
        public String isbn;
        public String publisher;
        public String reason;
        public Long branchId;
    }

    static class StaffRequestBody {
        public String staffName;
        public String title;
        public String author;
        public String isbn;
        public String publisher;
        public String reason;
        public AcquisitionPriority priority;
        public Long branchId;
    }

    static class ReviewRequestBody {
        public String staffName;
        public String note;
    }

    static class ApproveRequestBody {
        public BigDecimal estimatedCost;
    }

    static class DenyRequestBody {
        public String staffName;
        public String reason;
    }

    static class CreateOrderBody {
        public List<Long> requestIds;
        public String supplier;
        public LocalDate expectedDelivery;
        public String submittedBy;
    }

    // -------------------------------------------------------------------------
    // Acquisition Request endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/requests/member")
    public ResponseEntity<AcquisitionRequestDTO> submitMemberRequest(
            @RequestBody MemberRequestBody body) {
        log.info("POST /requests/member — memberId={}, title='{}'", body.memberId, body.title);
        AcquisitionRequestDTO dto = acquisitionService.submitMemberRequest(
                body.memberId, body.title, body.author, body.isbn,
                body.publisher, body.reason, body.branchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/requests/staff")
    public ResponseEntity<AcquisitionRequestDTO> submitStaffRequest(
            @RequestBody StaffRequestBody body) {
        log.info("POST /requests/staff — staff='{}', title='{}'", body.staffName, body.title);
        AcquisitionRequestDTO dto = acquisitionService.submitStaffRequest(
                body.staffName, body.title, body.author, body.isbn,
                body.publisher, body.reason, body.priority, body.branchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<AcquisitionRequestDTO>> getRequests(
            @RequestParam(required = false) List<AcquisitionStatus> statuses,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /requests — statuses={}", statuses);
        Page<AcquisitionRequestDTO> page = acquisitionService.getRequests(statuses, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<AcquisitionRequestDTO> getRequest(@PathVariable Long id) {
        log.info("GET /requests/{}", id);
        return ResponseEntity.ok(acquisitionService.getRequest(id));
    }

    @GetMapping("/requests/member/{memberId}")
    public ResponseEntity<List<AcquisitionRequestDTO>> getMemberRequests(
            @PathVariable Long memberId) {
        log.info("GET /requests/member/{}", memberId);
        return ResponseEntity.ok(acquisitionService.getMemberRequests(memberId));
    }

    @PutMapping("/requests/{id}/review")
    public ResponseEntity<AcquisitionRequestDTO> reviewRequest(
            @PathVariable Long id,
            @RequestBody ReviewRequestBody body) {
        log.info("PUT /requests/{}/review — staff='{}'", id, body.staffName);
        return ResponseEntity.ok(acquisitionService.reviewRequest(id, body.staffName, body.note));
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<AcquisitionRequestDTO> approveRequest(
            @PathVariable Long id,
            @RequestBody ApproveRequestBody body) {
        log.info("PUT /requests/{}/approve — estimatedCost={}", id, body.estimatedCost);
        return ResponseEntity.ok(acquisitionService.approveRequest(id, body.estimatedCost));
    }

    @PutMapping("/requests/{id}/deny")
    public ResponseEntity<AcquisitionRequestDTO> denyRequest(
            @PathVariable Long id,
            @RequestBody DenyRequestBody body) {
        log.info("PUT /requests/{}/deny — staff='{}'", id, body.staffName);
        return ResponseEntity.ok(acquisitionService.denyRequest(id, body.staffName, body.reason));
    }

    // -------------------------------------------------------------------------
    // Purchase Order endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/orders")
    public ResponseEntity<Page<PurchaseOrderDTO>> getOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /orders");
        return ResponseEntity.ok(acquisitionService.getOrders(pageable));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<PurchaseOrderDTO> getOrder(@PathVariable Long id) {
        log.info("GET /orders/{}", id);
        return ResponseEntity.ok(acquisitionService.getOrder(id));
    }

    @PostMapping("/orders")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(
            @RequestBody CreateOrderBody body) {
        log.info("POST /orders — supplier='{}', requestIds={}", body.supplier, body.requestIds);
        PurchaseOrderDTO dto = acquisitionService.createPurchaseOrder(
                body.requestIds, body.supplier, body.expectedDelivery, body.submittedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/orders/{id}/submit")
    public ResponseEntity<PurchaseOrderDTO> submitOrder(@PathVariable Long id) {
        log.info("PUT /orders/{}/submit", id);
        return ResponseEntity.ok(acquisitionService.submitOrder(id));
    }

    @PutMapping("/orders/{id}/receive")
    public ResponseEntity<PurchaseOrderDTO> receiveOrder(@PathVariable Long id) {
        log.info("PUT /orders/{}/receive", id);
        return ResponseEntity.ok(acquisitionService.receiveOrder(id));
    }
}
