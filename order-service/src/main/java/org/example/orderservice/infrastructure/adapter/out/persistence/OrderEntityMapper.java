package org.example.orderservice.infrastructure.adapter.out.persistence;

import org.example.orderservice.domain.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Currency;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface OrderEntityMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency.currencyCode")
    OrderJpaEntity toJpaEntity(Order order);

    @Mapping(target = "unitPrice", source = "unitPrice.amount")
    @Mapping(target = "order", ignore = true)
    OrderItemJpaEntity toOrderItemJpaEntity(OrderItem item);

    default List<OrderItemJpaEntity> mapOrderLines(OrderLines items) {
        if (items == null) return null;
        return items.toList().stream()
                .map(this::toOrderItemJpaEntity)
                .toList();
    }

    @AfterMapping
    default void linkOrderItems(@MappingTarget OrderJpaEntity entity) {
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> item.setOrder(entity));
        }
    }

    default Order toDomainModel(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

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