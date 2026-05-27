package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

class CreateOrderCommandBuilder {

    private UUID customerId = UUID.randomUUID();
    private List<CreateOrderCommand.OrderItemCommand> items = List.of(orderItem("100.00", PLN, 1));

    static CreateOrderCommandBuilder aCreateOrderCommand() {
        return new CreateOrderCommandBuilder();
    }

    CreateOrderCommandBuilder withCustomerId(UUID customerId) {
        this.customerId = customerId;
        return this;
    }

    CreateOrderCommandBuilder withItems(CreateOrderCommand.OrderItemCommand... items) {
        this.items = List.of(items);
        return this;
    }

    CreateOrderCommand build() {
        return new CreateOrderCommand(customerId, items);
    }

    static CreateOrderCommand.OrderItemCommand orderItem(String price, Currency currency, int quantity) {
        return new CreateOrderCommand.OrderItemCommand(
                UUID.randomUUID(),
                quantity,
                new BigDecimal(price),
                currency
        );
    }
}

