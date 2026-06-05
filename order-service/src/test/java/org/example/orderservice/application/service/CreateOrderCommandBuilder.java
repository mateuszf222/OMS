package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

class CreateOrderCommandBuilder {

    private UUID customerId = UUID.randomUUID();
    private List<CreateOrderCommand.OrderItemCommand> items = List.of(orderItem(UUID.randomUUID(), 1));

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

    static CreateOrderCommand.OrderItemCommand orderItem(UUID productId, int quantity) {
        return new CreateOrderCommand.OrderItemCommand(
                productId,
                quantity
        );
    }
}

