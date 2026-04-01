package org.example.orderservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.in.CancelOrderUseCase;
import org.example.orderservice.application.port.in.CompletePaymentUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaPaymentEventListener {

    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            UUID orderId = UUID.fromString(jsonNode.get("orderId").asText());

            if (jsonNode.has("reason")) {
                String reason = jsonNode.get("reason").asText();
                cancelOrderUseCase.cancelOrder(orderId, reason);
            } else {
                completePaymentUseCase.completePayment(orderId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}