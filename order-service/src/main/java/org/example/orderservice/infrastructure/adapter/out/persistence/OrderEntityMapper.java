package org.example.orderservice.infrastructure.adapter.out.persistence;

import org.example.orderservice.domain.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface OrderEntityMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency.currencyCode")
    OrderJpaEntity toJpaEntity(Order order);

    @Mapping(target = "unitPrice", source = "unitPrice.amount")
    @Mapping(target = "order", ignore = true)
    OrderItemJpaEntity toOrderItemJpaEntity(OrderItem item);

    default String mapStatus(OrderStatus status) {
        return status.name();
    }

    default List<OrderItemJpaEntity> toOrderItemEntities(OrderLines items) {
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
        if (entity == null) return null;

        return Order.restore(new OrderState(
                entity.getId(),
                entity.getCustomerId(),
                OrderStatus.valueOf(entity.getStatus()),
                toOrderLines(entity),
                entity.getCreatedAt(),
                entity.getVersion()
        ));
    }

    default OrderLines toOrderLines(OrderJpaEntity entity) {
        return new OrderLines(
                entity.getItems().stream()
                        .map(i -> new OrderItem(
                                i.getId(),
                                i.getProductId(),
                                i.getQuantity(),
                                toMoney(i.getUnitPrice(), entity.getCurrency())
                        ))
                        .toList()
        );
    }

    default Money toMoney(BigDecimal amount, String currency) {
        return new Money(amount, Currency.getInstance(currency));
    }
}