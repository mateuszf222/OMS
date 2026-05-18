package org.example.paymentservice.application.payment.port.out;

import org.example.paymentservice.domain.model.payment.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByOrderId(UUID orderId);
}
