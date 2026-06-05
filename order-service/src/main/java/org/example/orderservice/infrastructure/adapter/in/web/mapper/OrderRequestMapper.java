package org.example.orderservice.infrastructure.adapter.in.web.mapper;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.domain.cancellation.CustomerCancellationReason;
import org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder.CancelOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "items", source = "request.items")
    CreateOrderCommand toCreateOrderCommand(UUID customerId, CreateOrderRequest request);

    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "reason", source = "request.reason")
    CancelOrderByCustomerCommand toCancelOrderByCustomerCommand(UUID orderId, UUID customerId, CancelOrderRequest request);

    default CustomerCancellationReason toCustomerCancellationReason(String reason) {
        return new CustomerCancellationReason(reason);
    }
}
