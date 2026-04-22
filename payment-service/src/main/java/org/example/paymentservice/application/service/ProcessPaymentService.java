package org.example.paymentservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void processPayment(ProcessPaymentCommand command) {
        Payment payment = Payment.initialize(command.orderId(), command.amount(), command.currency());
        payment.validateLimits();
        paymentRepository.save(payment);

        log.info("Payment initiated for orderId: {}. Awaiting external gateway processing.", command.orderId());
    }
}