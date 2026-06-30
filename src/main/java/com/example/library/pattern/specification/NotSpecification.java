package com.example.library.pattern.specification;

public class NotSpecification<T> extends AbstractSpecification<T> {

    private final Specification<T> inner;

    public NotSpecification(Specification<T> inner) {
        this.inner = inner;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !inner.isSatisfiedBy(candidate);
    }
}
