package org.example.paymentservice.application.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.payment.port.in.ApplyGatewayPaymentStatusUseCase;
import org.example.paymentservice.application.payment.port.in.GatewayPaymentStatus;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyGatewayPaymentStatusService implements ApplyGatewayPaymentStatusUseCase {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void applyGatewayPaymentStatus(UUID paymentId, GatewayPaymentStatus status) {
        paymentRepository.findById(paymentId).ifPresentOrElse(
                payment -> applyGatewayDecision(payment, status),
                () -> log.warn("Ignored gateway status. Payment not found: {}", paymentId)
        );
    }

    private void applyGatewayDecision(Payment payment, GatewayPaymentStatus status) {
        if (payment.isSettled()) {
            return;
        }

        switch (status) {
            case SUCCESS -> completePayment(payment);
            case FAILURE -> rejectPayment(payment);
            case PENDING -> log.info("Payment {} is still waiting for the customer.", payment.getId());
            case UNKNOWN -> log.warn("Payment {} received unknown gateway status and requires review.", payment.getId());
        }
    }

    private void completePayment(Payment payment) {
        payment.complete();
        paymentRepository.save(payment);
        log.info("Payment {} completed successfully.", payment.getId());
    }

    private void rejectPayment(Payment payment) {
        payment.fail();
        paymentRepository.save(payment);
        log.info("Payment {} rejected by gateway.", payment.getId());
    }
}
