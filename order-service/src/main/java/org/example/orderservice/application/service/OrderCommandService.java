package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.CreateOrderCommand;
import org.example.orderservice.application.port.in.CreateOrderUseCase;
import org.example.orderservice.application.port.out.OrderEventPublisher;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.model.Money;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.OrderItem;

import java.util.List;
import java.util.UUID;

public class OrderCommandService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderCommandService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
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
        eventPublisher.publishOrderCreatedEvent(savedOrder);

        return savedOrder.getId();
    }
}