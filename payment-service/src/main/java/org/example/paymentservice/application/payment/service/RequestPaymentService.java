package org.example.paymentservice.application.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.payment.port.in.RequestPaymentCommand;
import org.example.paymentservice.application.payment.port.in.RequestPaymentUseCase;
import org.example.paymentservice.application.payment.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.payment.Payment;
import org.example.paymentservice.domain.specification.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestPaymentService implements RequestPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final Specification<Payment> maxAmountSpecification;

    @Override
    @Transactional
    public void requestPayment(RequestPaymentCommand command) {
        Payment payment = Payment.initialize(command.orderId(), command.customerId(), command.amount());

        payment.ensureAllowedBy(maxAmountSpecification);

        paymentRepository.save(payment);

        log.info("Payment requested for orderId: {}. Awaiting external gateway decision.", command.orderId());
    }
}
