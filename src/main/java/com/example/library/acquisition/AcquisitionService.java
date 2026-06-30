package com.example.library.acquisition;

import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AcquisitionService {

    private final AcquisitionRequestRepository acquisitionRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MemberRepository memberRepository;
    private final LibraryBranchRepository libraryBranchRepository;

    public AcquisitionRequestDTO submitMemberRequest(Long memberId, String title, String author,
                                                      String isbn, String publisher, String reason,
                                                      Long branchId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        LibraryBranch branch = null;
        if (branchId != null) {
            branch = libraryBranchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        }

        AcquisitionRequest request = AcquisitionRequest.builder()
                .requestedByMember(member)
                .title(title)
                .author(author)
                .isbn(isbn)
                .publisher(publisher)
                .reason(reason)
                .priority(AcquisitionPriority.MEDIUM)
                .status(AcquisitionStatus.PENDING)
                .requestDate(LocalDate.now())
                .targetBranch(branch)
                .build();

        AcquisitionRequest saved = acquisitionRequestRepository.save(request);
        log.info("Member {} submitted acquisition request for '{}'", memberId, title);
        return toDTO(saved);
    }

    public AcquisitionRequestDTO submitStaffRequest(String staffName, String title, String author,
                                                     String isbn, String publisher, String reason,
                                                     AcquisitionPriority priority, Long branchId) {
        LibraryBranch branch = null;
        if (branchId != null) {
            branch = libraryBranchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        }

        AcquisitionRequest request = AcquisitionRequest.builder()
                .requestedByStaff(staffName)
                .title(title)
                .author(author)
                .isbn(isbn)
                .publisher(publisher)
                .reason(reason)
                .priority(priority != null ? priority : AcquisitionPriority.MEDIUM)
                .status(AcquisitionStatus.PENDING)
                .requestDate(LocalDate.now())
                .targetBranch(branch)
                .build();

        AcquisitionRequest saved = acquisitionRequestRepository.save(request);
        log.info("Staff '{}' submitted acquisition request for '{}'", staffName, title);
        return toDTO(saved);
    }

    public AcquisitionRequestDTO reviewRequest(Long requestId, String staffName, String note) {
        AcquisitionRequest request = acquisitionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Acquisition request not found: " + requestId));

        if (request.getStatus() != AcquisitionStatus.PENDING) {
            throw new IllegalStateException("Request must be PENDING to review");
        }

        request.setStatus(AcquisitionStatus.UNDER_REVIEW);
        request.setReviewedByName(staffName);
        request.setReviewNote(note);

        AcquisitionRequest saved = acquisitionRequestRepository.save(request);
        log.info("Request {} set to UNDER_REVIEW by '{}'", requestId, staffName);
        return toDTO(saved);
    }

    public AcquisitionRequestDTO approveRequest(Long requestId, BigDecimal estimatedCost) {
        AcquisitionRequest request = acquisitionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Acquisition request not found: " + requestId));

        if (request.getStatus() != AcquisitionStatus.PENDING
                && request.getStatus() != AcquisitionStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Request must be PENDING or UNDER_REVIEW to approve");
        }

        request.setStatus(AcquisitionStatus.APPROVED);
        request.setEstimatedCost(estimatedCost);

        AcquisitionRequest saved = acquisitionRequestRepository.save(request);
        log.info("Request {} approved with estimated cost {}", requestId, estimatedCost);
        return toDTO(saved);
    }

    public AcquisitionRequestDTO denyRequest(Long requestId, String staffName, String reason) {
        AcquisitionRequest request = acquisitionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Acquisition request not found: " + requestId));

        request.setStatus(AcquisitionStatus.DENIED);
        request.setReviewNote(reason);
        request.setReviewedByName(staffName);

        AcquisitionRequest saved = acquisitionRequestRepository.save(request);
        log.info("Request {} denied by '{}'", requestId, staffName);
        return toDTO(saved);
    }

    public PurchaseOrderDTO createPurchaseOrder(List<Long> requestIds, String supplier,
                                                 LocalDate expectedDelivery, String submittedBy) {
        List<AcquisitionRequest> allRequests = acquisitionRequestRepository.findAllById(requestIds);

        List<AcquisitionRequest> approvedRequests = allRequests.stream()
                .filter(r -> r.getStatus() == AcquisitionStatus.APPROVED)
                .collect(Collectors.toList());

        if (approvedRequests.isEmpty()) {
            throw new IllegalArgumentException("No approved requests found for given IDs");
        }

        BigDecimal totalCost = approvedRequests.stream()
                .map(r -> r.getEstimatedCost() != null ? r.getEstimatedCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .orderDate(LocalDate.now())
                .expectedDelivery(expectedDelivery)
                .submittedByName(submittedBy)
                .totalCost(totalCost)
                .status(PurchaseOrderStatus.DRAFT)
                .build();

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        for (AcquisitionRequest req : approvedRequests) {
            req.setPurchaseOrder(savedOrder);
        }
        acquisitionRequestRepository.saveAll(approvedRequests);

        log.info("Purchase order {} created for {} approved requests, total cost {}",
                savedOrder.getId(), approvedRequests.size(), totalCost);
        return toOrderDTO(savedOrder);
    }

    public PurchaseOrderDTO submitOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + orderId));

        order.setStatus(PurchaseOrderStatus.SUBMITTED);
        order.setOrderDate(LocalDate.now());

        for (AcquisitionRequest req : order.getRequests()) {
            req.setStatus(AcquisitionStatus.ORDERED);
        }
        acquisitionRequestRepository.saveAll(order.getRequests());

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        log.info("Purchase order {} submitted", orderId);
        return toOrderDTO(saved);
    }

    public PurchaseOrderDTO receiveOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + orderId));

        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setActualDelivery(LocalDate.now());

        for (AcquisitionRequest req : order.getRequests()) {
            req.setStatus(AcquisitionStatus.RECEIVED);
        }
        acquisitionRequestRepository.saveAll(order.getRequests());

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        log.info("Purchase order {} received on {}", orderId, order.getActualDelivery());
        return toOrderDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<AcquisitionRequestDTO> getRequests(List<AcquisitionStatus> statuses, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            return acquisitionRequestRepository.findAll(pageable).map(this::toDTO);
        }
        return acquisitionRequestRepository.findByStatusIn(statuses, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<AcquisitionRequestDTO> getMemberRequests(Long memberId) {
        return acquisitionRequestRepository.findByRequestedByMemberId(memberId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcquisitionRequestDTO getRequest(Long id) {
        AcquisitionRequest request = acquisitionRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acquisition request not found: " + id));
        return toDTO(request);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDTO getOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + id));
        return toOrderDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable).map(this::toOrderDTO);
    }

    private AcquisitionRequestDTO toDTO(AcquisitionRequest r) {
        Long memberId = null;
        String memberName = null;
        if (r.getRequestedByMember() != null) {
            Member m = r.getRequestedByMember();
            memberId = m.getId();
            memberName = m.getMembershipNumber();
            if (m.getUser() != null) {
                String firstName = m.getUser().getFirstName();
                String lastName = m.getUser().getLastName();
                if (firstName != null || lastName != null) {
                    memberName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
                }
            }
        }

        Long targetBranchId = null;
        String targetBranchName = null;
        if (r.getTargetBranch() != null) {
            targetBranchId = r.getTargetBranch().getId();
            targetBranchName = r.getTargetBranch().getName();
        }

        Long purchaseOrderId = null;
        if (r.getPurchaseOrder() != null) {
            purchaseOrderId = r.getPurchaseOrder().getId();
        }

        return AcquisitionRequestDTO.builder()
                .id(r.getId())
                .memberId(memberId)
                .memberName(memberName)
                .requestedByStaff(r.getRequestedByStaff())
                .title(r.getTitle())
                .author(r.getAuthor())
                .isbn(r.getIsbn())
                .publisher(r.getPublisher())
                .reason(r.getReason())
                .priority(r.getPriority())
                .status(r.getStatus())
                .requestDate(r.getRequestDate())
                .reviewedByName(r.getReviewedByName())
                .reviewNote(r.getReviewNote())
                .estimatedCost(r.getEstimatedCost())
                .targetBranchId(targetBranchId)
                .targetBranchName(targetBranchName)
                .purchaseOrderId(purchaseOrderId)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private PurchaseOrderDTO toOrderDTO(PurchaseOrder o) {
        List<Long> requestIds = o.getRequests() != null
                ? o.getRequests().stream().map(AcquisitionRequest::getId).toList()
                : List.of();

        return PurchaseOrderDTO.builder()
                .id(o.getId())
                .supplier(o.getSupplier())
                .orderDate(o.getOrderDate())
                .expectedDelivery(o.getExpectedDelivery())
                .actualDelivery(o.getActualDelivery())
                .totalCost(o.getTotalCost())
                .status(o.getStatus())
                .notes(o.getNotes())
                .submittedByName(o.getSubmittedByName())
                .requestCount(requestIds.size())
                .requestIds(requestIds)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
