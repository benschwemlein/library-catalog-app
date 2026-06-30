package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fine")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false, unique = true)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String reason;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean waived = false;

    @Column(name = "waived_by", length = 200)
    private String waivedBy;

    @Column(name = "waived_reason", length = 500)
    private String waivedReason;

    public String getStatus() {
        if (waived) return "WAIVED";
        if (paidDate != null) return "PAID";
        return "UNPAID";
    }
}
