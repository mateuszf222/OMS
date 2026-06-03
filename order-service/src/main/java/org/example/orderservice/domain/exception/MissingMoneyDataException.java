package org.example.orderservice.domain.exception;

public final class MissingMoneyDataException extends InvalidMoneyOperationException {

    public MissingMoneyDataException(String fieldName) {
        super("Money " + fieldName + " cannot be null.");
    }
}
