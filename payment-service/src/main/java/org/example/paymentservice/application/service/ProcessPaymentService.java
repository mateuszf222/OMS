package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.example.paymentservice.application.port.out.PaymentEventPublisher;
import org.example.paymentservice.application.port.out.PaymentGatewayPort;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentGatewayPort paymentGatewayPort;

    @Override
    public void processPayment(ProcessPaymentCommand command) {
        Payment payment = Payment.initialize(command.orderId(), command.amount(), command.currency());
        payment.validateLimits();
        paymentRepository.save(payment);

        try {
            String redirectUrl = paymentGatewayPort.initiatePayment(payment, "127.0.0.1");
            log.info("Zainicjowano płatność. Link dla klienta: {}", redirectUrl);
        } catch (Exception e) {
            log.error("Błąd komunikacji z bramką płatności.", e);
            payment.fail();
            Payment savedPayment = paymentRepository.save(payment);
            paymentEventPublisher.publishPaymentFailedEvent(savedPayment);
        }
    }
}