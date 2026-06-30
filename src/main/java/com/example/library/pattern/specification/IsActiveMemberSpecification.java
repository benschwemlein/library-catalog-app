package com.example.library.pattern.specification;

import com.example.library.entity.Member;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class IsActiveMemberSpecification extends AbstractSpecification<Member> {

    @Override
    public boolean isSatisfiedBy(Member member) {
        return member.isActive()
            && member.getExpiryDate() != null
            && member.getExpiryDate().isAfter(LocalDate.now());
    }
}
