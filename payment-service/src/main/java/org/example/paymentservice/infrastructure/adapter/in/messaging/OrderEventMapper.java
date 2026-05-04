package org.example.paymentservice.infrastructure.adapter.in.messaging;

import org.example.paymentservice.application.port.in.ProcessPaymentCommand;
import org.example.paymentservice.domain.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {

    @Mapping(target = "amount", source = ".", qualifiedByName = "toMoney")
    ProcessPaymentCommand toCommand(OrderCreatedEvent event);

    @Named("toMoney")
    default Money toMoney(OrderCreatedEvent event) {
        return Money.of(event.totalAmount(), event.currency());
    }
}