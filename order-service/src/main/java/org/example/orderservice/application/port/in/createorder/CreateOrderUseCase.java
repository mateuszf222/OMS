package org.example.orderservice.application.port.in.createorder;

import java.util.UUID;

public interface CreateOrderUseCase {
    UUID createOrder(CreateOrderCommand command);
}