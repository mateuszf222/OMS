package org.example.orderservice.domain.exception;

public abstract sealed class OrderDomainException extends DomainException permits
        CustomerRequiredForOrderException,
        InvalidMoneyOperationException,
        InvalidOrderItemException,
        MissingCancellationReasonException,
        OrderLineCannotBeNullException,
        OrderItemsMustUseSameCurrencyException,
        OrderMustContainProductsException {

    public OrderDomainException(String message) {
        super(message);
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
