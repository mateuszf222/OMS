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
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private final ZonedDateTime createdAt;

    public static Payment initialize(UUID orderId, BigDecimal amount, String currency) {
        if (orderId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentDomainException("Invalid payment initialization parameters.");
        }
        return new Payment(UUID.randomUUID(), orderId, amount, currency, PaymentStatus.PENDING, ZonedDateTime.now());
    }

    public static Payment restore(UUID id, UUID orderId, BigDecimal amount, String currency, PaymentStatus status, ZonedDateTime createdAt) {
        return new Payment(id, orderId, amount, currency, status, createdAt);
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
}