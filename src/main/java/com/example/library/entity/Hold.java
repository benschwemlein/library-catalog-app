package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hold")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "notified_date")
    private LocalDateTime notifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_branch_id")
    private LibraryBranch pickupBranch;

    @Version
    @Column(name = "version")
    private Long version;
}
