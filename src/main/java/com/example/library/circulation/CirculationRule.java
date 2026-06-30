package com.example.library.circulation;

import com.example.library.entity.MembershipTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "circulation_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CirculationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", length = 20)
    private MembershipTier membershipTier; // null means applies to all tiers

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Column(name = "branch_id")
    private Long branchId; // null means applies to all branches

    @Column(name = "loan_period_days", nullable = false)
    private int loanPeriodDays;

    @Column(name = "max_renewals", nullable = false)
    private int maxRenewals;

    @Column(name = "fine_rate_per_day", nullable = false, precision = 5, scale = 2)
    private BigDecimal fineRatePerDay;

    @Column(name = "max_fine_amount", precision = 10, scale = 2)
    private BigDecimal maxFineAmount; // null means no cap

    @Column(name = "max_loans_allowed", nullable = false)
    private int maxLoansAllowed;

    @Column(name = "reservation_hold_days", nullable = false)
    private int reservationHoldDays;

    @Column(name = "min_age_required")
    private int minAgeRequired;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
