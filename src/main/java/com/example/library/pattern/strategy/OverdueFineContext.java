package com.example.library.pattern.strategy;

import com.example.library.entity.Loan;
import com.example.library.entity.MembershipTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Context class that selects and delegates to the appropriate
 * {@link FineCalculationStrategy} based on the member's {@link MembershipTier}.
 */
@Component
@Slf4j
public class OverdueFineContext {

    private final Map<MembershipTier, FineCalculationStrategy> strategies;

    /**
     * Spring injects this map via MapFactoryBean or a @Bean method.
     * The keys are MembershipTier enum values and values are the corresponding strategy beans.
     *
     * @param standardFineStrategy  strategy for STANDARD members
     * @param premiumFineStrategy   strategy for PREMIUM members
     * @param studentFineStrategy   strategy for STUDENT members
     */
    public OverdueFineContext(StandardFineStrategy standardFineStrategy,
                              PremiumFineStrategy premiumFineStrategy,
                              StudentFineStrategy studentFineStrategy) {
        this.strategies = new java.util.EnumMap<>(MembershipTier.class);
        this.strategies.put(MembershipTier.STANDARD, standardFineStrategy);
        this.strategies.put(MembershipTier.PREMIUM, premiumFineStrategy);
        this.strategies.put(MembershipTier.STUDENT, studentFineStrategy);
    }

    /**
     * Calculate a fine for the given loan by selecting the strategy that matches
     * the member's membership tier.
     *
     * @param loan       the loan to assess
     * @param returnDate the actual or effective return date
     * @return the calculated fine amount
     * @throws IllegalStateException if no strategy is registered for the member's tier
     */
    public BigDecimal calculateFine(Loan loan, LocalDate returnDate) {
        MembershipTier tier = loan.getMember().getMembershipTier();
        FineCalculationStrategy strategy = strategies.get(tier);

        if (strategy == null) {
            log.error("No fine calculation strategy registered for tier={}", tier);
            throw new IllegalStateException("No fine strategy registered for membership tier: " + tier);
        }

        log.debug("Calculating fine for loan={} using strategy={} (tier={})",
                loan.getId(), strategy.getClass().getSimpleName(), tier);

        BigDecimal fine = strategy.calculateFine(loan, returnDate);
        log.info("Fine calculated for loan={} member={} tier={}: ${}",
                loan.getId(), loan.getMember().getId(), tier, fine);
        return fine;
    }

    /**
     * Override the strategy for a given membership tier at runtime.
     * Useful for testing or special promotions.
     *
     * @param tier     the tier whose strategy to replace
     * @param strategy the new strategy to use
     */
    public void setStrategy(MembershipTier tier, FineCalculationStrategy strategy) {
        log.info("Overriding fine strategy for tier={} with {}", tier, strategy.getClass().getSimpleName());
        strategies.put(tier, strategy);
    }

    /**
     * Return a read-only view of the current strategy map.
     */
    public Map<MembershipTier, FineCalculationStrategy> getStrategies() {
        return java.util.Collections.unmodifiableMap(strategies);
    }
}
