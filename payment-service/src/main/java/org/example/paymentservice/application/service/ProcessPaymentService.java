package org.example.paymentservice.application.service;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.specification.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final Specification<Payment> maxAmountSpecification;

    @Override
    @Transactional
    public void processPayment(ProcessPaymentCommand command) {
        Payment payment = Payment.initialize(command.orderId(), command.customerId(), command.amount());

        payment.checkSpecification(maxAmountSpecification);

        paymentRepository.save(payment);

        log.info("Payment initiated for orderId: {}. Awaiting external gateway processing.", command.orderId());
    }
}