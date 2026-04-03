package org.example.paymentservice.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentEventPublisher;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private void sendEvent(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("Payment Publisher - Wysyłanie eventu na Kafkę [{}]: {}", topic, payload);
            kafkaTemplate.send(topic, key, payload);
        } catch (JsonProcessingException e) {
            log.error("Payment Publisher - Błąd serializacji eventu", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(payment.getOrderId(), payment.getId());
        sendEvent("payment-completed-events", payment.getOrderId().toString(), event);
    }

    @Override
    public void publishPaymentFailedEvent(Payment payment) {
        PaymentFailedEvent event = new PaymentFailedEvent(payment.getOrderId(), payment.getId(), "Payment processing failed");
        sendEvent("payment-failed-events", payment.getOrderId().toString(), event);
    }
}