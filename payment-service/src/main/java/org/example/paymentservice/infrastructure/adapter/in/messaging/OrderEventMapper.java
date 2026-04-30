package org.example.paymentservice.infrastructure.adapter.in.messaging;

import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.domain.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {

    @Mapping(target = "amount", expression = "java(mapMoney(event))")
    ProcessPaymentCommand toCommand(OrderCreatedEvent event);

    default Money mapMoney(OrderCreatedEvent event) {
        return Money.of(event.totalAmount(), event.currency());
    }
}