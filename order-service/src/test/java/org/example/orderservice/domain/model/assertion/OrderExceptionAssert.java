package org.example.orderservice.domain.model.assertion;

import org.example.orderservice.domain.model.OrderStatus;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;

public class OrderExceptionAssert extends AbstractAssert<OrderExceptionAssert, InvalidOrderStateTransitionException> {

    private OrderExceptionAssert(InvalidOrderStateTransitionException actual) {
        super(actual, OrderExceptionAssert.class);
    }

    public static OrderExceptionAssert assertThat(InvalidOrderStateTransitionException actual) {
        return new OrderExceptionAssert(actual);
    }

    public OrderExceptionAssert hasCurrentStatus(OrderStatus status) {
        isNotNull();
        Assertions.assertThat(actual.getCurrentStatus())
                .as("Current status in exception")
                .isEqualTo(status);
        return this;
    }

    public OrderExceptionAssert hasTargetStatus(OrderStatus status) {
        isNotNull();
        Assertions.assertThat(actual.getTargetStatus())
                .as("Target status in exception")
                .isEqualTo(status);
        return this;
    }

    public OrderExceptionAssert hasAction(String action) {
        isNotNull();
        Assertions.assertThat(actual.getAction())
                .as("Action that caused the transition error")
                .isEqualTo(action);
        return this;
    }

    public OrderExceptionAssert hasMessageContaining(String... fragments) {
        isNotNull();
        Assertions.assertThat(actual.getMessage())
                .as("Exception message")
                .contains(fragments);
        return this;
    }
}

