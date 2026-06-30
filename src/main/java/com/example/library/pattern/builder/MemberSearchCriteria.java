package com.example.library.pattern.builder;

import com.example.library.entity.MembershipTier;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MemberSearchCriteria {
    private final String name;
    private final String membershipNumber;
    private final MembershipTier tier;
    private final Boolean active;
    private final BigDecimal fineBalanceMin;
    private final LocalDate joinedAfter;
    private final LocalDate expiringBefore;
    private final int page;
    private final int size;

    MemberSearchCriteria(MemberSearchCriteriaBuilder builder) {
        this.name = builder.name;
        this.membershipNumber = builder.membershipNumber;
        this.tier = builder.tier;
        this.active = builder.active;
        this.fineBalanceMin = builder.fineBalanceMin;
        this.joinedAfter = builder.joinedAfter;
        this.expiringBefore = builder.expiringBefore;
        this.page = builder.page;
        this.size = builder.size;
    }

    public static MemberSearchCriteriaBuilder builder() {
        return new MemberSearchCriteriaBuilder();
    }

    public String getName() { return name; }
    public String getMembershipNumber() { return membershipNumber; }
    public MembershipTier getTier() { return tier; }
    public Boolean getActive() { return active; }
    public BigDecimal getFineBalanceMin() { return fineBalanceMin; }
    public LocalDate getJoinedAfter() { return joinedAfter; }
    public LocalDate getExpiringBefore() { return expiringBefore; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
