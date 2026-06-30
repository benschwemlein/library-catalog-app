package com.example.library.digitalresource;

import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "digital_loan")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private DigitalResource resource;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    private int downloadCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigitalLoanStatus status;

    private LocalDateTime returnDate;
}
