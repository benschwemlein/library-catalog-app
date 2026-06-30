package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.acquisition.*;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcquisitionServiceTest {

    @Mock
    private AcquisitionRequestRepository acquisitionRequestRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LibraryBranchRepository libraryBranchRepository;

    @InjectMocks
    private AcquisitionService acquisitionService;

    // -------------------------------------------------------------------------
    // submitMemberRequest
    // -------------------------------------------------------------------------

    @Test
    void submitRequest_setsStatusPending() {
        Member member = TestDataFactory.createMember();
        LibraryBranch branch = TestDataFactory.createBranch();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(libraryBranchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        AcquisitionRequest savedRequest = AcquisitionRequest.builder()
                .id(1L)
                .requestedByMember(member)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .publisher("Prentice Hall")
                .reason("Team reference")
                .status(AcquisitionStatus.PENDING)
                .priority(AcquisitionPriority.MEDIUM)
                .requestDate(LocalDate.now())
                .targetBranch(branch)
                .build();

        when(acquisitionRequestRepository.save(any(AcquisitionRequest.class))).thenReturn(savedRequest);

        AcquisitionRequestDTO result = acquisitionService.submitMemberRequest(
                member.getId(), "Clean Code", "Robert C. Martin",
                "978-0132350884", "Prentice Hall", "Team reference", branch.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AcquisitionStatus.PENDING);
        verify(acquisitionRequestRepository).save(any(AcquisitionRequest.class));
    }

    // -------------------------------------------------------------------------
    // approveRequest
    // -------------------------------------------------------------------------

    @Test
    void reviewRequest_approve_changesStatus() {
        Member member = TestDataFactory.createMember();
        AcquisitionRequest pendingRequest = AcquisitionRequest.builder()
                .id(10L)
                .requestedByMember(member)
                .title("Effective Java")
                .status(AcquisitionStatus.PENDING)
                .priority(AcquisitionPriority.HIGH)
                .requestDate(LocalDate.now().minusDays(2))
                .build();

        AcquisitionRequest approvedRequest = AcquisitionRequest.builder()
                .id(10L)
                .requestedByMember(member)
                .title("Effective Java")
                .status(AcquisitionStatus.APPROVED)
                .priority(AcquisitionPriority.HIGH)
                .estimatedCost(new BigDecimal("49.99"))
                .requestDate(LocalDate.now().minusDays(2))
                .build();

        when(acquisitionRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(acquisitionRequestRepository.save(any(AcquisitionRequest.class))).thenReturn(approvedRequest);

        AcquisitionRequestDTO result = acquisitionService.approveRequest(10L, new BigDecimal("49.99"));

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AcquisitionStatus.APPROVED);
    }

    // -------------------------------------------------------------------------
    // denyRequest
    // -------------------------------------------------------------------------

    @Test
    void reviewRequest_deny_setsReviewNote() {
        Member member = TestDataFactory.createMember();
        AcquisitionRequest pendingRequest = AcquisitionRequest.builder()
                .id(11L)
                .requestedByMember(member)
                .title("Some Book")
                .status(AcquisitionStatus.PENDING)
                .priority(AcquisitionPriority.LOW)
                .requestDate(LocalDate.now().minusDays(1))
                .build();

        when(acquisitionRequestRepository.findById(11L)).thenReturn(Optional.of(pendingRequest));
        when(acquisitionRequestRepository.save(any(AcquisitionRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AcquisitionRequestDTO result = acquisitionService.denyRequest(
                11L, "Head Librarian", "Budget constraints this quarter");

        assertThat(result.getStatus()).isEqualTo(AcquisitionStatus.DENIED);
        assertThat(result.getReviewNote()).isEqualTo("Budget constraints this quarter");
        assertThat(result.getReviewedByName()).isEqualTo("Head Librarian");
    }

    // -------------------------------------------------------------------------
    // createPurchaseOrder
    // -------------------------------------------------------------------------

    @Test
    void createPurchaseOrder_onlyIncludesApproved() {
        Member member = TestDataFactory.createMember();

        AcquisitionRequest approved1 = AcquisitionRequest.builder()
                .id(1L).title("Book A").status(AcquisitionStatus.APPROVED)
                .estimatedCost(new BigDecimal("20.00")).requestDate(LocalDate.now())
                .requestedByMember(member).build();
        AcquisitionRequest approved2 = AcquisitionRequest.builder()
                .id(2L).title("Book B").status(AcquisitionStatus.APPROVED)
                .estimatedCost(new BigDecimal("30.00")).requestDate(LocalDate.now())
                .requestedByMember(member).build();
        AcquisitionRequest pending = AcquisitionRequest.builder()
                .id(3L).title("Book C").status(AcquisitionStatus.PENDING)
                .estimatedCost(new BigDecimal("15.00")).requestDate(LocalDate.now())
                .requestedByMember(member).build();

        when(acquisitionRequestRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(approved1, approved2, pending));

        PurchaseOrder savedOrder = PurchaseOrder.builder()
                .id(100L)
                .supplier("Books Inc.")
                .orderDate(LocalDate.now())
                .expectedDelivery(LocalDate.now().plusWeeks(2))
                .totalCost(new BigDecimal("50.00"))
                .status(PurchaseOrderStatus.DRAFT)
                .requests(new ArrayList<>())
                .build();

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(savedOrder);
        when(acquisitionRequestRepository.saveAll(any())).thenReturn(List.of(approved1, approved2));

        PurchaseOrderDTO result = acquisitionService.createPurchaseOrder(
                List.of(1L, 2L, 3L), "Books Inc.",
                LocalDate.now().plusWeeks(2), "Librarian Jane");

        assertThat(result).isNotNull();

        ArgumentCaptor<List<AcquisitionRequest>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(acquisitionRequestRepository).saveAll(saveAllCaptor.capture());
        List<AcquisitionRequest> linkedRequests = saveAllCaptor.getValue();
        assertThat(linkedRequests).hasSize(2);
        assertThat(linkedRequests).extracting(AcquisitionRequest::getStatus)
                .containsOnly(AcquisitionStatus.APPROVED);
    }

    @Test
    void createPurchaseOrder_sumsEstimatedCosts() {
        Member member = TestDataFactory.createMember();

        AcquisitionRequest req1 = AcquisitionRequest.builder()
                .id(1L).title("Book A").status(AcquisitionStatus.APPROVED)
                .estimatedCost(new BigDecimal("10.00")).requestDate(LocalDate.now())
                .requestedByMember(member).build();
        AcquisitionRequest req2 = AcquisitionRequest.builder()
                .id(2L).title("Book B").status(AcquisitionStatus.APPROVED)
                .estimatedCost(new BigDecimal("15.00")).requestDate(LocalDate.now())
                .requestedByMember(member).build();

        when(acquisitionRequestRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(req1, req2));

        ArgumentCaptor<PurchaseOrder> orderCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);

        PurchaseOrder savedOrder = PurchaseOrder.builder()
                .id(200L)
                .supplier("Publisher Direct")
                .orderDate(LocalDate.now())
                .expectedDelivery(LocalDate.now().plusMonths(1))
                .totalCost(new BigDecimal("25.00"))
                .status(PurchaseOrderStatus.DRAFT)
                .requests(new ArrayList<>())
                .build();

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(savedOrder);
        when(acquisitionRequestRepository.saveAll(any())).thenReturn(List.of(req1, req2));

        PurchaseOrderDTO result = acquisitionService.createPurchaseOrder(
                List.of(1L, 2L), "Publisher Direct",
                LocalDate.now().plusMonths(1), "Librarian Bob");

        verify(purchaseOrderRepository).save(orderCaptor.capture());
        BigDecimal capturedTotal = orderCaptor.getValue().getTotalCost();
        assertThat(capturedTotal).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    // -------------------------------------------------------------------------
    // receiveOrder
    // -------------------------------------------------------------------------

    @Test
    void receiveOrder_cascadesStatusToRequests() {
        Member member = TestDataFactory.createMember();

        AcquisitionRequest req1 = AcquisitionRequest.builder()
                .id(1L).title("Book A").status(AcquisitionStatus.ORDERED)
                .requestDate(LocalDate.now()).requestedByMember(member).build();
        AcquisitionRequest req2 = AcquisitionRequest.builder()
                .id(2L).title("Book B").status(AcquisitionStatus.ORDERED)
                .requestDate(LocalDate.now()).requestedByMember(member).build();

        List<AcquisitionRequest> linkedRequests = new ArrayList<>(List.of(req1, req2));

        PurchaseOrder order = PurchaseOrder.builder()
                .id(300L)
                .supplier("Wholesale Books")
                .orderDate(LocalDate.now().minusDays(10))
                .status(PurchaseOrderStatus.SUBMITTED)
                .requests(linkedRequests)
                .build();

        when(purchaseOrderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(acquisitionRequestRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        acquisitionService.receiveOrder(300L);

        assertThat(req1.getStatus()).isEqualTo(AcquisitionStatus.RECEIVED);
        assertThat(req2.getStatus()).isEqualTo(AcquisitionStatus.RECEIVED);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        verify(acquisitionRequestRepository).saveAll(linkedRequests);
    }
}
