package org.example.paymentservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.example.paymentservice.domain.exception.PaymentDomainException;

import java.math.BigDecimal;
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

    private static final BigDecimal MAX_PAYMENT_LIMIT_VALUE = new BigDecimal("10000.00");

    public static Payment initialize(UUID orderId, UUID customerId, Money amount) {
        if (orderId == null || customerId == null || amount == null || amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentDomainException("Invalid payment initialization parameters.");
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
            throw new PaymentDomainException("Cannot complete payment in status: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentDomainException("Cannot fail payment in status: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
    }

    public void validateLimits() {
        Money maxLimit = new Money(
                MAX_PAYMENT_LIMIT_VALUE,
                this.amount.currency()
        );

        if (this.amount.isGreaterThan(maxLimit)) {
            throw new PaymentDomainException("Amount exceeds maximum limit.");
        }
    }
}