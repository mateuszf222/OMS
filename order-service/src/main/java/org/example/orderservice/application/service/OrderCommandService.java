package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.in.CreateOrderCommand;
import org.example.orderservice.application.port.in.CreateOrderUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.model.Money;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCommandService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        List<OrderItem> domainItems = command.items().stream()
                .map(item -> new OrderItem(
                        UUID.randomUUID(),
                        item.productId(),
                        item.quantity(),
                        new Money(item.price(), item.currency())
                ))
                .toList();

        Order order = Order.create(command.customerId(), domainItems);

        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }
}