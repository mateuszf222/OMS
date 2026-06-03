package org.example.paymentservice.domain.model.payment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.example.paymentservice.domain.exception.InvalidPaymentStateTransitionException;
import org.example.paymentservice.domain.exception.MissingPaymentDataException;
import org.example.paymentservice.domain.exception.PaymentAmountLimitExceededException;
import org.example.paymentservice.domain.model.Money;
import org.example.paymentservice.domain.specification.Specification;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {
    private final UUID id;
    private final UUID orderId;
    private final Money amount;
    private PaymentStatus status;
    private final ZonedDateTime createdAt;
    private final UUID customerId;

    public static Payment initialize(UUID orderId, UUID customerId, Money amount) {
        if (orderId == null) {
            throw new MissingPaymentDataException("orderId");
        }
        if (customerId == null) {
            throw new MissingPaymentDataException("customerId");
        }
        if (amount == null) {
            throw new MissingPaymentDataException("amount");
        }
        return new Payment(
                UUID.randomUUID(),
                orderId,
                amount,
                PaymentStatus.PENDING,
                ZonedDateTime.now(),
                customerId
        );
    }

    public static Payment restore(PaymentState state) {
        return new Payment(
                state.id(),
                state.orderId(),
                state.amount(),
                state.status(),
                state.createdAt(),
                state.customerId()
        );
    }


    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateTransitionException("complete", this.status, PaymentStatus.COMPLETED);
        }
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateTransitionException("fail", this.status, PaymentStatus.FAILED);
        }
        this.status = PaymentStatus.FAILED;
    }

    public boolean isSettled() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED;
    }

    public boolean isAwaitingGatewayDecision() {
        return status == PaymentStatus.PENDING;
    }

    public void ensureAllowedBy(Specification<Payment> specification) {
        if (!specification.isSatisfiedBy(this)) {
            throw new PaymentAmountLimitExceededException(specification.getReasonNotSatisfied());
        }
    }
}
