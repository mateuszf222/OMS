package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentGatewayPort;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.infrastructure.adapter.out.messaging.PaymentInitiatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayWorker {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-initiated-events", groupId = "payment-gateway-worker")
    public void handlePaymentInitiated(String payload, Acknowledgment acknowledgment) {
        try {
            PaymentInitiatedEvent event = objectMapper.readValue(payload, PaymentInitiatedEvent.class);
            log.info("Processing PaymentInitiatedEvent for paymentId: {}", event.paymentId());

            Payment payment = paymentRepository.findById(event.paymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + event.paymentId()));

            String redirectUrl = paymentGatewayPort.initiatePayment(payment, "127.0.0.1");
            log.info("Payment {} initiated successfully. Redirect URL: {}", event.paymentId(), redirectUrl);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process payment gateway initiation", e);

            try {
                PaymentInitiatedEvent event = objectMapper.readValue(payload, PaymentInitiatedEvent.class);
                paymentRepository.findById(event.paymentId()).ifPresent(payment -> {
                    payment.fail();
                    paymentRepository.save(payment);
                });
                acknowledgment.acknowledge();
            } catch (Exception ex) {
                log.error("Failed to handle payment gateway error", ex);
                throw new RuntimeException(ex);
            }
        }
    }
}
