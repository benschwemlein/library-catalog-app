package com.example.library.acquisition;

import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "acquisition_request")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquisitionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member requestedByMember;

    @Column(name = "requested_by_staff", length = 200)
    private String requestedByStaff;

    @Column(nullable = false)
    private String title;

    @Column
    private String author;

    @Column(length = 20)
    private String isbn;

    @Column
    private String publisher;

    @Column
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AcquisitionPriority priority = AcquisitionPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AcquisitionStatus status = AcquisitionStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "reviewed_by_name", length = 200)
    private String reviewedByName;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private LibraryBranch targetBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private PurchaseOrder purchaseOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
