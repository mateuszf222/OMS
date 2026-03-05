package application.service;

import application.port.in.CreateOrderCommand;
import application.port.in.CreateOrderUseCase;
import application.port.out.OrderEventPublisher;
import application.port.out.OrderRepository;
import domain.model.Money;
import domain.model.Order;
import domain.model.OrderItem;

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