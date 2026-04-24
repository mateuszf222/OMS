package org.example.paymentservice.infrastructure.adapter.in.messaging;

import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {

    @Mapping(target = "amount", source = "totalAmount")
    ProcessPaymentCommand toCommand(OrderCreatedEvent event);
}