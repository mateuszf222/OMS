package org.example.orderservice.domain.cancellation;

import org.example.orderservice.domain.exception.MissingCancellationReasonException;

public sealed interface CancellationReason permits
        AdminCancellationReason,
        CustomerCancellationReason,
        PaymentFailureCancellationReason {

    String value();

    static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new MissingCancellationReasonException();
        }

        return value;
    }
}
