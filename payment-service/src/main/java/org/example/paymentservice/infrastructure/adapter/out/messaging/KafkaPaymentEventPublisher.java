package org.example.paymentservice.infrastructure.adapter.out.messaging;

import org.example.paymentservice.application.port.out.PaymentEventPublisher;
import org.example.paymentservice.domain.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(payment.getOrderId(), payment.getId());
        kafkaTemplate.send("payment-events", payment.getOrderId().toString(), event);
    }

    @Override
    public void publishPaymentFailedEvent(Payment payment) {
        PaymentFailedEvent event = new PaymentFailedEvent(payment.getOrderId(), payment.getId(), "Payment processing failed");
        kafkaTemplate.send("payment-events", payment.getOrderId().toString(), event);
    }
}