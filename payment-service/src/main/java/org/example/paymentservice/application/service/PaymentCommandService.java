package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.example.paymentservice.application.port.out.PaymentEventPublisher;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService implements ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    @Transactional
    public void processPayment(ProcessPaymentCommand command) {
        Payment payment = Payment.initialize(command.orderId(), command.amount(), command.currency());

        try {
            simulatePaymentProcessing(payment);
            payment.complete();
            Payment savedPayment = paymentRepository.save(payment);
            paymentEventPublisher.publishPaymentCompletedEvent(savedPayment);
        } catch (Exception e) {
            payment.fail();
            Payment savedPayment = paymentRepository.save(payment);
            paymentEventPublisher.publishPaymentFailedEvent(savedPayment);
        }
    }

    private void simulatePaymentProcessing(Payment payment) {
        payment.validateLimits(); // Application service just orchestrates
    }
}