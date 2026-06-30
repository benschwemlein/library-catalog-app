package com.example.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Typed configuration binding for the application.library.* namespace.
 * Replaces hardcoded constants in LoanService, FineService, and schedulers.
 *
 * Example (application.yml):
 *   application:
 *     library:
 *       max-loans-per-member: 8
 *       default-loan-period-days: 21
 *       unpaid-fines-threshold: "10.00"
 */
@Configuration
@ConfigurationProperties(prefix = "application.library")
public class LibraryProperties {

    private int maxLoansPerMember = 8;
    private int defaultLoanPeriodDays = 21;
    private int overdueFineGracePeriodDays = 1;
    private int maxRenewals = 3;
    private int holdExpiryDays = 7;
    private int recommendationCacheTtlMinutes = 30;
    private BigDecimal unpaidFinesThreshold = new BigDecimal("10.00");
    private int searchIndexStaleDays = 7;

    public int getMaxLoansPerMember() { return maxLoansPerMember; }
    public void setMaxLoansPerMember(int maxLoansPerMember) { this.maxLoansPerMember = maxLoansPerMember; }

    public int getDefaultLoanPeriodDays() { return defaultLoanPeriodDays; }
    public void setDefaultLoanPeriodDays(int defaultLoanPeriodDays) { this.defaultLoanPeriodDays = defaultLoanPeriodDays; }

    public int getOverdueFineGracePeriodDays() { return overdueFineGracePeriodDays; }
    public void setOverdueFineGracePeriodDays(int overdueFineGracePeriodDays) { this.overdueFineGracePeriodDays = overdueFineGracePeriodDays; }

    public int getMaxRenewals() { return maxRenewals; }
    public void setMaxRenewals(int maxRenewals) { this.maxRenewals = maxRenewals; }

    public int getHoldExpiryDays() { return holdExpiryDays; }
    public void setHoldExpiryDays(int holdExpiryDays) { this.holdExpiryDays = holdExpiryDays; }

    public int getRecommendationCacheTtlMinutes() { return recommendationCacheTtlMinutes; }
    public void setRecommendationCacheTtlMinutes(int recommendationCacheTtlMinutes) { this.recommendationCacheTtlMinutes = recommendationCacheTtlMinutes; }

    public BigDecimal getUnpaidFinesThreshold() { return unpaidFinesThreshold; }
    public void setUnpaidFinesThreshold(BigDecimal unpaidFinesThreshold) { this.unpaidFinesThreshold = unpaidFinesThreshold; }

    public int getSearchIndexStaleDays() { return searchIndexStaleDays; }
    public void setSearchIndexStaleDays(int searchIndexStaleDays) { this.searchIndexStaleDays = searchIndexStaleDays; }
}
