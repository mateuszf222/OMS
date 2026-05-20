package org.example.orderservice.domain.exception;

public class InvalidMoneyOperationException extends OrderDomainException {

    public InvalidMoneyOperationException(String message) {
        super(message);
    }
}
