package com.example.library.circulation;

import com.example.library.exception.BranchNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CirculationRuleService {

    private final CirculationRuleRepository repository;
    private final CirculationRulesEngine engine;

    public CirculationRule createRule(CirculationRuleRequest request) {
        validateRule(request);
        CirculationRule rule = applyRequest(new CirculationRule(), request);
        rule.setActive(true);
        CirculationRule saved = repository.save(rule);
        log.info("Created circulation rule id={} for tier={}, itemType={}, branchId={}",
                saved.getId(), saved.getMembershipTier(), saved.getItemType(), saved.getBranchId());
        return saved;
    }

    public CirculationRule updateRule(Long id, CirculationRuleRequest request) {
        CirculationRule existing = getRule(id);
        validateRule(request);
        CirculationRule updated = applyRequest(existing, request);
        CirculationRule saved = repository.save(updated);
        log.info("Updated circulation rule id={}", saved.getId());
        return saved;
    }

    /**
     * Soft-deletes a rule by marking it inactive rather than physically removing it.
     * This preserves historical accuracy for loans that were governed by the rule.
     */
    public void deleteRule(Long id) {
        CirculationRule rule = getRule(id);
        rule.setActive(false);
        repository.save(rule);
        log.info("Soft-deleted circulation rule id={}", id);
    }

    @Transactional(readOnly = true)
    public CirculationRule getRule(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Circulation rule not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CirculationRule> getAllActiveRules() {
        return repository.findByActiveTrue();
    }

    /**
     * Finds the most applicable active rule for a given tier, item type, and optional branch.
     * Branch-specific and tier-specific rules take priority over generic ones.
     * Returns the engine's built-in default rule when nothing matches.
     */
    @Transactional(readOnly = true)
    public CirculationRule findApplicableRule(com.example.library.entity.MembershipTier tier,
                                              ItemType itemType, Long branchId) {
        List<CirculationRule> rules = repository.findApplicableRules(tier, itemType, branchId);
        if (rules.isEmpty()) {
            // Return a transient default — not persisted, so id will be null.
            return CirculationRule.builder()
                    .membershipTier(tier)
                    .itemType(itemType)
                    .branchId(branchId)
                    .loanPeriodDays(21)
                    .maxRenewals(2)
                    .fineRatePerDay(new java.math.BigDecimal("0.25"))
                    .maxFineAmount(new java.math.BigDecimal("25.00"))
                    .maxLoansAllowed(8)
                    .reservationHoldDays(7)
                    .minAgeRequired(0)
                    .active(true)
                    .build();
        }
        return rules.get(0);
    }

    /**
     * Creates a new rule that is a copy of the source rule but scoped to the specified branch.
     * Useful for quickly establishing branch-specific overrides of an existing global rule.
     */
    public CirculationRule cloneRuleForBranch(Long ruleId, Long targetBranchId) {
        CirculationRule source = getRule(ruleId);
        CirculationRule clone = CirculationRule.builder()
                .membershipTier(source.getMembershipTier())
                .itemType(source.getItemType())
                .branchId(targetBranchId)
                .loanPeriodDays(source.getLoanPeriodDays())
                .maxRenewals(source.getMaxRenewals())
                .fineRatePerDay(source.getFineRatePerDay())
                .maxFineAmount(source.getMaxFineAmount())
                .maxLoansAllowed(source.getMaxLoansAllowed())
                .reservationHoldDays(source.getReservationHoldDays())
                .minAgeRequired(source.getMinAgeRequired())
                .active(true)
                .build();
        CirculationRule saved = repository.save(clone);
        log.info("Cloned rule id={} to new rule id={} for branchId={}", ruleId, saved.getId(), targetBranchId);
        return saved;
    }

    private void validateRule(CirculationRuleRequest request) {
        if (request.getItemType() == null) {
            throw new IllegalArgumentException("Item type is required.");
        }
        if (request.getLoanPeriodDays() < 1 || request.getLoanPeriodDays() > 365) {
            throw new IllegalArgumentException("Loan period days must be between 1 and 365.");
        }
        if (request.getMaxRenewals() < 0 || request.getMaxRenewals() > 10) {
            throw new IllegalArgumentException("Max renewals must be between 0 and 10.");
        }
        if (request.getFineRatePerDay() == null || request.getFineRatePerDay().signum() < 0) {
            throw new IllegalArgumentException("Fine rate per day must be zero or positive.");
        }
        if (request.getMaxFineAmount() != null && request.getMaxFineAmount().signum() < 0) {
            throw new IllegalArgumentException("Max fine amount must be zero or positive.");
        }
        if (request.getMaxLoansAllowed() < 1 || request.getMaxLoansAllowed() > 50) {
            throw new IllegalArgumentException("Max loans allowed must be between 1 and 50.");
        }
        if (request.getReservationHoldDays() < 1 || request.getReservationHoldDays() > 30) {
            throw new IllegalArgumentException("Reservation hold days must be between 1 and 30.");
        }
        if (request.getMinAgeRequired() < 0 || request.getMinAgeRequired() > 18) {
            throw new IllegalArgumentException("Min age required must be between 0 and 18.");
        }
    }

    private CirculationRule applyRequest(CirculationRule rule, CirculationRuleRequest request) {
        rule.setMembershipTier(request.getMembershipTier());
        rule.setItemType(request.getItemType());
        rule.setBranchId(request.getBranchId());
        rule.setLoanPeriodDays(request.getLoanPeriodDays());
        rule.setMaxRenewals(request.getMaxRenewals());
        rule.setFineRatePerDay(request.getFineRatePerDay());
        rule.setMaxFineAmount(request.getMaxFineAmount());
        rule.setMaxLoansAllowed(request.getMaxLoansAllowed());
        rule.setReservationHoldDays(request.getReservationHoldDays());
        rule.setMinAgeRequired(request.getMinAgeRequired());
        return rule;
    }
}
