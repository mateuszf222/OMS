package org.example.orderservice.infrastructure.adapter.in.web;

import org.example.orderservice.application.port.in.CreateOrderCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "items", source = "request.items")
    CreateOrderCommand toCommand(UUID customerId, CreateOrderRequest request);

    @Mapping(target = "price", source = "request.price")
    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

}