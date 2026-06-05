package org.example.orderservice.infrastructure.adapter.in.messaging;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderDueToPaymentFailureCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;

import java.util.UUID;

final class PaymentEventTestData {

    static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
    static final String TIMEOUT = "TIMEOUT";

    private PaymentEventTestData() {
    }

    static PaymentCompletedEvent paymentCompletedEvent() {
        return new PaymentCompletedEvent(UUID.randomUUID(), UUID.randomUUID());
    }

    static PaymentFailedEvent paymentFailedBecauseLimitExceeded() {
        return paymentFailedEvent(LIMIT_EXCEEDED);
    }

    static PaymentFailedEvent paymentFailedEvent(String reason) {
        return new PaymentFailedEvent(UUID.randomUUID(), UUID.randomUUID(), reason);
    }

    static CompletePaymentCommand completePaymentCommandFor(PaymentCompletedEvent event) {
        return new CompletePaymentCommand(event.orderId(), event.paymentId());
    }

    static CancelOrderDueToPaymentFailureCommand cancelOrderCommandFor(PaymentFailedEvent event) {
        return new CancelOrderDueToPaymentFailureCommand(
                event.orderId(),
                event.paymentId(),
                new PaymentFailureCancellationReason(event.reason())
        );
    }
}

