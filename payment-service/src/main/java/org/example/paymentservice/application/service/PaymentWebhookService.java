package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.GatewayPaymentStatus;
import org.example.paymentservice.application.port.in.HandlePaymentWebhookUseCase;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.model.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService implements HandlePaymentWebhookUseCase {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void handleWebhook(UUID paymentId, GatewayPaymentStatus status) {
        paymentRepository.findById(paymentId).ifPresentOrElse(
                payment -> processPaymentStatusUpdate(payment, status),
                () -> log.warn("Zignorowano webhook. Nie znaleziono płatności: {}", paymentId)
        );
    }

    private void processPaymentStatusUpdate(Payment payment, GatewayPaymentStatus status) {

        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        switch (status) {
            case SUCCESS -> {
                payment.complete();
                paymentRepository.save(payment);
                log.info("Płatność {} sfinalizowana sukcesem!", payment.getId());
            }
            case FAILURE -> {
                payment.fail();
                paymentRepository.save(payment);
                log.info("Płatność {} odrzucona przez operatora.", payment.getId());
            }
            case PENDING -> {
                log.info("Płatność {} oczekuje na klienta (status bramki: PENDING)", payment.getId());
            }
            case UNKNOWN -> {
                log.warn("Płatność {} otrzymała nieznany status z bramki. Wymaga manualnej weryfikacji.", payment.getId());
            }
        }
    }
}