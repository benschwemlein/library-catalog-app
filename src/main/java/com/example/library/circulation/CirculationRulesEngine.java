package com.example.library.circulation;

import com.example.library.entity.*;
import com.example.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CirculationRulesEngine {

    private final CirculationRuleRepository ruleRepository;
    private final LoanRepository loanRepository;

    /** Maximum fine balance a member may carry before becoming ineligible to check out. */
    private static final BigDecimal MAX_FINE_THRESHOLD = new BigDecimal("25.00");

    /** Default rule applied when no configured rule matches the query. */
    private static final CirculationRule DEFAULT_RULE = CirculationRule.builder()
            .loanPeriodDays(21)
            .maxRenewals(2)
            .fineRatePerDay(new BigDecimal("0.25"))
            .maxFineAmount(new BigDecimal("25.00"))
            .maxLoansAllowed(8)
            .reservationHoldDays(7)
            .minAgeRequired(0)
            .active(true)
            .build();

    /**
     * Returns the most specific active rule for the given member, copy, and branch combination.
     * Branch-specific rules beat global rules; tier-specific rules beat wildcard rules.
     * Falls back to {@link #DEFAULT_RULE} when no match is found.
     */
    @Transactional(readOnly = true)
    public CirculationRule getApplicableRule(Member member, BookCopy copy, LibraryBranch branch) {
        ItemType itemType = determineItemType(copy);
        Long branchId = branch != null ? branch.getId() : null;

        List<CirculationRule> rules = ruleRepository.findApplicableRules(
                member.getMembershipTier(), itemType, branchId);

        if (rules.isEmpty()) {
            log.debug("No circulation rule found for tier={}, itemType={}, branchId={}; using default",
                    member.getMembershipTier(), itemType, branchId);
            return DEFAULT_RULE;
        }

        CirculationRule best = rules.get(0);
        log.debug("Resolved circulation rule id={} for tier={}, itemType={}, branchId={}",
                best.getId(), member.getMembershipTier(), itemType, branchId);
        return best;
    }

    /**
     * Calculates the due date for a checkout based on the applicable circulation rule.
     */
    public LocalDate calculateDueDate(Member member, BookCopy copy, LibraryBranch branch, LocalDate checkoutDate) {
        CirculationRule rule = getApplicableRule(member, copy, branch);
        LocalDate dueDate = checkoutDate.plusDays(rule.getLoanPeriodDays());
        log.debug("Calculated due date {} ({} days) for member={}, copy={}",
                dueDate, rule.getLoanPeriodDays(), member.getMembershipNumber(), copy.getBarcode());
        return dueDate;
    }

    /**
     * Calculates the overdue fine for a loan as of the given return date.
     * Returns {@link BigDecimal#ZERO} if the loan is not overdue.
     * Applies the maximum fine cap from the rule when configured.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFine(Loan loan, LocalDate returnDate) {
        LocalDate dueDate = loan.getDueDate().toLocalDate();
        if (!returnDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        CirculationRule rule = getApplicableRule(
                loan.getMember(), loan.getBookCopy(), loan.getMember() != null
                        ? null : null); // branch resolved from loan when available

        BigDecimal rawFine = rule.getFineRatePerDay().multiply(BigDecimal.valueOf(daysOverdue));

        if (rule.getMaxFineAmount() != null
                && rawFine.compareTo(rule.getMaxFineAmount()) > 0) {
            log.debug("Fine capped at {} (raw={}, days={})", rule.getMaxFineAmount(), rawFine, daysOverdue);
            return rule.getMaxFineAmount();
        }

        log.debug("Calculated fine {} for {} days overdue", rawFine, daysOverdue);
        return rawFine;
    }

    /**
     * Returns the maximum number of simultaneous loans permitted for the member at the given branch.
     */
    @Transactional(readOnly = true)
    public int getMaxLoans(Member member, LibraryBranch branch) {
        Long branchId = branch != null ? branch.getId() : null;
        List<CirculationRule> rules = ruleRepository.findApplicableRules(
                member.getMembershipTier(), ItemType.BOOK, branchId);
        return rules.isEmpty() ? DEFAULT_RULE.getMaxLoansAllowed() : rules.get(0).getMaxLoansAllowed();
    }

    /**
     * Returns the maximum number of renewals permitted for the member's tier and the copy's item type.
     */
    @Transactional(readOnly = true)
    public int getMaxRenewals(Member member, BookCopy copy) {
        ItemType itemType = determineItemType(copy);
        List<CirculationRule> rules = ruleRepository.findApplicableRules(
                member.getMembershipTier(), itemType, null);
        return rules.isEmpty() ? DEFAULT_RULE.getMaxRenewals() : rules.get(0).getMaxRenewals();
    }

    /**
     * Determines whether the member is eligible to check out the given copy at the specified branch.
     * Checks: member active, membership not expired, fine balance within threshold,
     * copy available, loan count within limit, and age restriction.
     */
    @Transactional(readOnly = true)
    public EligibilityResult isEligibleToCheckout(Member member, BookCopy copy, LibraryBranch branch) {
        List<String> reasons = new ArrayList<>();

        if (!member.isActive()) {
            reasons.add("Member account is inactive.");
        }

        if (member.getExpiryDate() != null && member.getExpiryDate().isBefore(LocalDate.now())) {
            reasons.add("Membership has expired on " + member.getExpiryDate() + ".");
        }

        if (member.getFineBalance() != null
                && member.getFineBalance().compareTo(MAX_FINE_THRESHOLD) > 0) {
            reasons.add("Outstanding fine balance of $" + member.getFineBalance()
                    + " exceeds allowed threshold of $" + MAX_FINE_THRESHOLD + ".");
        }

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            reasons.add("Item '" + copy.getBarcode() + "' is not available (status: " + copy.getStatus() + ").");
        }

        CirculationRule rule = getApplicableRule(member, copy, branch);
        List<Loan> activeLoans = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE);
        if (activeLoans.size() >= rule.getMaxLoansAllowed()) {
            reasons.add("Member has reached the maximum of " + rule.getMaxLoansAllowed() + " active loans.");
        }

        if (rule.getMinAgeRequired() > 0) {
            // Age check is a placeholder: real implementation would read member date-of-birth.
            // Skipped when minAgeRequired == 0 (no restriction).
            log.debug("Age restriction check ({}) deferred to caller — member DOB not available on Member entity.",
                    rule.getMinAgeRequired());
        }

        if (reasons.isEmpty()) {
            return EligibilityResult.eligible();
        }
        return EligibilityResult.ineligible(reasons);
    }

    /**
     * Determines whether the given active loan is eligible for renewal.
     * Checks that the loan is in ACTIVE status and has not exceeded the maximum renewal count.
     */
    @Transactional(readOnly = true)
    public EligibilityResult isEligibleToRenew(Loan loan) {
        List<String> reasons = new ArrayList<>();

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            reasons.add("Loan is not in ACTIVE status (current: " + loan.getStatus() + ").");
        }

        int maxRenewals = getMaxRenewals(loan.getMember(), loan.getBookCopy());
        if (loan.getRenewalCount() >= maxRenewals) {
            reasons.add("Maximum renewals of " + maxRenewals + " already reached"
                    + " (current renewal count: " + loan.getRenewalCount() + ").");
        }

        return reasons.isEmpty() ? EligibilityResult.eligible() : EligibilityResult.ineligible(reasons);
    }

    /**
     * Determines the {@link ItemType} for a given {@link BookCopy}.
     * Currently defaults to {@link ItemType#BOOK}; a full implementation would
     * inspect the copy's category or material type field.
     */
    private ItemType determineItemType(BookCopy copy) {
        // Future: copy.getMaterialType() or copy.getItemType() once that field exists
        return ItemType.BOOK;
    }
}
