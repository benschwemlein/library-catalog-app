package com.example.library.pattern.specification;

import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import org.springframework.stereotype.Component;

@Component
public class IsPremiumMemberSpecification extends AbstractSpecification<Member> {

    @Override
    public boolean isSatisfiedBy(Member member) {
        return member.getMembershipTier() == MembershipTier.PREMIUM;
    }
}
