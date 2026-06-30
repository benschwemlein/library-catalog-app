package com.example.library.pattern.builder;

import com.example.library.entity.MembershipTier;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MemberSearchCriteriaBuilder {
    String name;
    String membershipNumber;
    MembershipTier tier;
    Boolean active;
    BigDecimal fineBalanceMin;
    LocalDate joinedAfter;
    LocalDate expiringBefore;
    int page = 0;
    int size = 20;

    public MemberSearchCriteriaBuilder name(String name) { this.name = name; return this; }
    public MemberSearchCriteriaBuilder membershipNumber(String membershipNumber) { this.membershipNumber = membershipNumber; return this; }
    public MemberSearchCriteriaBuilder tier(MembershipTier tier) { this.tier = tier; return this; }
    public MemberSearchCriteriaBuilder active(Boolean active) { this.active = active; return this; }
    public MemberSearchCriteriaBuilder fineBalanceMin(BigDecimal fineBalanceMin) { this.fineBalanceMin = fineBalanceMin; return this; }
    public MemberSearchCriteriaBuilder joinedAfter(LocalDate joinedAfter) { this.joinedAfter = joinedAfter; return this; }
    public MemberSearchCriteriaBuilder expiringBefore(LocalDate expiringBefore) { this.expiringBefore = expiringBefore; return this; }
    public MemberSearchCriteriaBuilder page(int page) { this.page = page; return this; }
    public MemberSearchCriteriaBuilder size(int size) { this.size = size; return this; }

    public MemberSearchCriteria build() {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 200) {
            size = 200;
        }
        if (fineBalanceMin != null && fineBalanceMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("fineBalanceMin cannot be negative");
        }
        return new MemberSearchCriteria(this);
    }
}
