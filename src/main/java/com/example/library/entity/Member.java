package com.example.library.entity;

import com.example.library.entity.MembershipTier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private com.example.catalog.model.User user;

    @Column(name = "membership_number", nullable = false, unique = true, length = 50)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", nullable = false, length = 20)
    private MembershipTier membershipTier;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "fine_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fineBalance = BigDecimal.ZERO;

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Hold> holds = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
