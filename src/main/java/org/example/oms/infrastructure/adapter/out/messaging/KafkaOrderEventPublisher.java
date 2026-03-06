package org.example.oms.infrastructure.adapter.out.messaging;

import org.example.oms.application.port.out.OrderEventPublisher;
import org.example.oms.domain.model.Order;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    @Override
    public void publishOrderCreatedEvent(Order order) {
    }
}