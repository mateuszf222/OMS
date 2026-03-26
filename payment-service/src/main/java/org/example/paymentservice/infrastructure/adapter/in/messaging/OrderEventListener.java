package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ObjectMapper objectMapper;

    public OrderEventListener(ProcessPaymentUseCase processPaymentUseCase, ObjectMapper objectMapper) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderCreatedEvent(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    event.orderId(),
                    event.totalAmount(),
                    event.currency()
            );
            processPaymentUseCase.processPayment(command);
        } catch (Exception e) {
            throw new RuntimeException("Error processing OrderCreatedEvent", e);
        }
    }
}