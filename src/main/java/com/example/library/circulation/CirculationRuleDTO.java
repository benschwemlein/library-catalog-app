package com.example.library.circulation;

import com.example.library.entity.MembershipTier;

import java.math.BigDecimal;

public record CirculationRuleDTO(
        Long id,
        MembershipTier membershipTier,
        ItemType itemType,
        Long branchId,
        int loanPeriodDays,
        int maxRenewals,
        BigDecimal fineRatePerDay,
        BigDecimal maxFineAmount,
        int maxLoansAllowed,
        int reservationHoldDays,
        int minAgeRequired,
        boolean active,
        String tierDisplayName,
        String itemTypeDisplayName
) {

    /**
     * Converts a {@link CirculationRule} entity into a DTO, adding human-readable
     * display names for the tier and item type.
     */
    public static CirculationRuleDTO from(CirculationRule rule) {
        String tierDisplay = rule.getMembershipTier() != null
                ? formatEnum(rule.getMembershipTier().name())
                : "All Tiers";

        String itemTypeDisplay = rule.getItemType() != null
                ? formatEnum(rule.getItemType().name())
                : "Unknown";

        return new CirculationRuleDTO(
                rule.getId(),
                rule.getMembershipTier(),
                rule.getItemType(),
                rule.getBranchId(),
                rule.getLoanPeriodDays(),
                rule.getMaxRenewals(),
                rule.getFineRatePerDay(),
                rule.getMaxFineAmount(),
                rule.getMaxLoansAllowed(),
                rule.getReservationHoldDays(),
                rule.getMinAgeRequired(),
                rule.isActive(),
                tierDisplay,
                itemTypeDisplay
        );
    }

    /** Converts SCREAMING_SNAKE_CASE enum names to Title Case for display. */
    private static String formatEnum(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
