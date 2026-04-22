package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.orderEvents}", groupId = "#{@kafkaTopicsProperties.groups.paymentService}")
    public void handleOrderCreatedEvent(String payload, Acknowledgment acknowledgment) {
        log.info("Received OrderCreatedEvent: {}", payload);

        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

            boolean paymentExists = paymentRepository.findByOrderId(event.orderId()).isPresent();

            if (paymentExists) {
                log.info("Payment for orderId {} already exists. Skipping duplicate event.", event.orderId());
                acknowledgment.acknowledge();
                return;
            }

            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    event.orderId(),
                    event.totalAmount(),
                    event.currency()
            );
            processPaymentUseCase.processPayment(command);

            log.info("Payment processing initiated for orderId: {}", event.orderId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent", e);
            throw new RuntimeException(e);
        }
    }
}