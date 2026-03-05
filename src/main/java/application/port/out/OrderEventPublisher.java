package application.port.out;

import domain.model.Order;

public interface OrderEventPublisher {
    void publishOrderCreatedEvent(Order order);
}