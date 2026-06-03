package org.example.orderservice.domain.exception;

public abstract sealed class InvalidMoneyOperationException extends OrderDomainException permits
        MissingMoneyDataException,
        MoneyCurrencyMismatchException,
        NegativeMoneyMultiplierException {

    public InvalidMoneyOperationException(String message) {
        super(message);
    }
}
