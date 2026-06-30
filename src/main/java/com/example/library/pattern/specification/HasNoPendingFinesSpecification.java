package com.example.library.pattern.specification;

import com.example.library.entity.Member;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class HasNoPendingFinesSpecification extends AbstractSpecification<Member> {

    @Override
    public boolean isSatisfiedBy(Member member) {
        return member.getFineBalance() != null
            && member.getFineBalance().compareTo(BigDecimal.ZERO) == 0;
    }
}
