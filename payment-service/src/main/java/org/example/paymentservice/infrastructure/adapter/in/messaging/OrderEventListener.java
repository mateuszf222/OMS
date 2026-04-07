package org.example.paymentservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.application.port.in.ProcessPaymentUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.orderEvents}", groupId = "payment-service-group")
    public void handleOrderCreatedEvent(String payload) {
        log.info("Payment Listener - Odebrano surowy payload z Kafki: {}", payload);
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    event.orderId(),
                    event.totalAmount(),
                    event.currency()
            );
            processPaymentUseCase.processPayment(command);
            log.info("Payment Listener - Pomyślnie przetworzono płatność dla zamówienia: {}", event.orderId());
        } catch (Exception e) {
            log.error("Payment Listener - KRYTYCZNY BŁĄD podczas przetwarzania: ", e);
            throw new RuntimeException(e);
        }
    }
}