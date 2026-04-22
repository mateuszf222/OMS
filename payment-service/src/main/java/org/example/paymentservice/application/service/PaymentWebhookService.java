package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.HandlePaymentWebhookUseCase;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.model.PaymentStatus;
import org.example.paymentservice.infrastructure.adapter.in.web.PayUStatus;
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
    public void handleWebhook(UUID paymentId, PayUStatus externalStatus) {
        paymentRepository.findById(paymentId).ifPresentOrElse(
                payment -> processPaymentStatusUpdate(payment, externalStatus),
                () -> log.warn("Zignorowano webhook. Nie znaleziono płatności: {}", paymentId)
        );
    }

    private void processPaymentStatusUpdate(Payment payment, PayUStatus externalStatus) {

        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        switch (externalStatus) {
            case COMPLETED -> {
                payment.complete();
                paymentRepository.save(payment);
                log.info("Płatność {} sfinalizowana sukcesem!", payment.getId());
            }
            case CANCELED, REJECTED -> {
                payment.fail();
                paymentRepository.save(payment);
                log.info("Płatność {} odrzucona przez operatora.", payment.getId());
            }
            case NEW, PENDING -> {
                log.info("Płatność {} oczekuje na klienta (status PayU: {})", payment.getId(), externalStatus);
            }
            case UNKNOWN -> {
                log.warn("Płatność {} otrzymała nieznany status z PayU. Wymaga manualnej weryfikacji.", payment.getId());
            }
        }
    }
}