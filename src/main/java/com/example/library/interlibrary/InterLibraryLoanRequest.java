package com.example.library.interlibrary;

import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ill_request")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterLibraryLoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member requestingMember;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch requestingBranch;

    @Column(nullable = false)
    private String bookTitle;

    private String authorName;

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false)
    private LocalDate requestDate;

    private LocalDate neededByDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ILLStatus status = ILLStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "partner_library_id")
    private PartnerLibrary partnerLibrary;

    @Column
    private String notes;

    private LocalDate estimatedArrival;

    private String reviewedByName;

    @Column
    private String reviewNote;

    @Column
    private String denialReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
