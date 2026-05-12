package org.example.paymentservice.domain.specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    String getReasonNotSatisfied();
}