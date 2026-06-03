package org.example.orderservice.domain.exception;

public sealed interface OrderBusinessRefusal permits InvalidOrderStateTransitionException {
}
