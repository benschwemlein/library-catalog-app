package com.example.library.circulation;

import com.example.library.entity.MembershipTier;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CirculationRuleRequest {

    /** Optional — null means the rule applies to all membership tiers. */
    private MembershipTier membershipTier;

    @NotNull(message = "Item type is required.")
    private ItemType itemType;

    /** Optional — null means the rule applies to all branches. */
    private Long branchId;

    @Min(value = 1, message = "Loan period must be at least 1 day.")
    @Max(value = 365, message = "Loan period cannot exceed 365 days.")
    private int loanPeriodDays;

    @Min(value = 0, message = "Maximum renewals cannot be negative.")
    @Max(value = 10, message = "Maximum renewals cannot exceed 10.")
    private int maxRenewals;

    @NotNull(message = "Fine rate per day is required.")
    @DecimalMin(value = "0.00", message = "Fine rate per day cannot be negative.")
    private BigDecimal fineRatePerDay;

    /** Optional — null means no cap on accumulated fines. */
    @DecimalMin(value = "0.00", message = "Maximum fine amount cannot be negative.")
    private BigDecimal maxFineAmount;

    @Min(value = 1, message = "Maximum loans allowed must be at least 1.")
    @Max(value = 50, message = "Maximum loans allowed cannot exceed 50.")
    private int maxLoansAllowed;

    @Min(value = 1, message = "Reservation hold days must be at least 1.")
    @Max(value = 30, message = "Reservation hold days cannot exceed 30.")
    private int reservationHoldDays;

    @Min(value = 0, message = "Minimum age required cannot be negative.")
    @Max(value = 18, message = "Minimum age required cannot exceed 18.")
    private int minAgeRequired;
}
