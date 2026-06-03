package org.example.orderservice.domain.exception;

public final class MissingOrderItemDataException extends InvalidOrderItemException {

    public MissingOrderItemDataException() {
        super("ID, ProductID oraz UnitPrice nie mogą być null.");
    }
}
