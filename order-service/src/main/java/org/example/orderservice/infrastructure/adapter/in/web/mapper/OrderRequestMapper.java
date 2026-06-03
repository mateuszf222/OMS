package org.example.orderservice.infrastructure.adapter.in.web.mapper;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder.CancelOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.exception.InvalidCurrencyCodeException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "items", source = "request.items")
    CreateOrderCommand toCreateOrderCommand(UUID customerId, CreateOrderRequest request);

    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "reason", source = "request.reason")
    CancelOrderCommand toCancelOrderCommand(UUID orderId, CancelOrderRequest request);

    default Currency toCurrency(String currencyCode) {
        if (currencyCode == null) {
            throw new InvalidCurrencyCodeException(null);
        }

        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCurrencyCodeException(currencyCode);
        }
    }
}
