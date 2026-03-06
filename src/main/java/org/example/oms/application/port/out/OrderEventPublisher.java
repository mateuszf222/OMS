package org.example.oms.application.port.out;

import org.example.oms.domain.model.Order;

public interface OrderEventPublisher {
    void publishOrderCreatedEvent(Order order);
}