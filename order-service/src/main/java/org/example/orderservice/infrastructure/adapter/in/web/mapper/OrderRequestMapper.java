package org.example.orderservice.infrastructure.adapter.in.web.mapper;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder.CancelOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "items", source = "request.items")
    CreateOrderCommand toCommand(UUID customerId, CreateOrderRequest request);

    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "reason", source = "request.reason")
    CancelOrderCommand toCancelCommand(UUID orderId, CancelOrderRequest request);
}