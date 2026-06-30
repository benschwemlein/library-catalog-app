package com.example.library.periodical;

import com.example.library.entity.LibraryBranch;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "periodical_subscription")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicalSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodical_id", nullable = false)
    @ToString.Exclude
    private Periodical periodical;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch branch;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate renewalDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal annualCost;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean autoRenew = true;

    @Column
    private String notes;
}
