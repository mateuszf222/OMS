package org.example.orderservice.domain.exception;

import java.math.BigDecimal;

public final class NegativeMoneyMultiplierException extends InvalidMoneyOperationException {

    public NegativeMoneyMultiplierException(BigDecimal multiplier) {
        super("Mnożnik nie może być ujemny. Otrzymano: " + multiplier + ".");
    }
}
