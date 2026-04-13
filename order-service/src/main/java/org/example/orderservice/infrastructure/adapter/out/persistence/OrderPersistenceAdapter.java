package org.example.orderservice.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.model.Money;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.OrderItem;
import org.example.orderservice.domain.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomainModel(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainModel);
    }

    private OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().amount())
                .currency(order.getTotalAmount().currency().getCurrencyCode())
                .createdAt(order.getCreatedAt())
                .version(order.getVersion())
                .build();

        List<OrderItemJpaEntity> items = order.getItems().stream()
                .map(item -> OrderItemJpaEntity.builder()
                        .id(item.getId())
                        .order(entity)
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().amount())
                        .build())
                .toList();

        entity.setItems(items);
        return entity;
    }

    private Order toDomainModel(OrderJpaEntity entity) {
        List<OrderItem> domainItems = entity.getItems().stream()
                .map(itemEntity -> new OrderItem(
                        itemEntity.getId(),
                        itemEntity.getProductId(),
                        itemEntity.getQuantity(),
                        new Money(itemEntity.getUnitPrice(), Currency.getInstance(entity.getCurrency()))
                ))
                .toList();

        return Order.restore(
                entity.getId(),
                entity.getCustomerId(),
                OrderStatus.valueOf(entity.getStatus()),
                domainItems,
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }
}