package com.example.library.entity;

import java.math.BigDecimal;

public enum MembershipTier {

    STANDARD(21, 1, new BigDecimal("0.25")),
    PREMIUM(28, 3, new BigDecimal("0.10")),
    STUDENT(14, 2, new BigDecimal("0.15"));

    private final int daysLoanPeriod;
    private final int maxRenewals;
    private final BigDecimal dailyFineRate;

    MembershipTier(int daysLoanPeriod, int maxRenewals, BigDecimal dailyFineRate) {
        this.daysLoanPeriod = daysLoanPeriod;
        this.maxRenewals = maxRenewals;
        this.dailyFineRate = dailyFineRate;
    }

    public int getDaysLoanPeriod() {
        return daysLoanPeriod;
    }

    public int getMaxRenewals() {
        return maxRenewals;
    }

    public BigDecimal getDailyFineRate() {
        return dailyFineRate;
    }
}
