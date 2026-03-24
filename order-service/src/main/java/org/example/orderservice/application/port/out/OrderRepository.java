package org.example.orderservice.application.port.out;

import org.example.orderservice.domain.model.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}