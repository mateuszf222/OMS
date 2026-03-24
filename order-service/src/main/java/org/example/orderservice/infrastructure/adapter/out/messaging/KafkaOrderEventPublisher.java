package org.example.orderservice.infrastructure.adapter.out.messaging;

import org.example.orderservice.application.port.out.OrderEventPublisher;
import org.example.orderservice.domain.model.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency().getCurrencyCode()
        );

        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }
}