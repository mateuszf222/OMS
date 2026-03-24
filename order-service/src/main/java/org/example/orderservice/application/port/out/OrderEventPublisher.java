package org.example.orderservice.application.port.out;

import org.example.orderservice.domain.model.Order;

public interface OrderEventPublisher {
    void publishOrderCreatedEvent(Order order);
}