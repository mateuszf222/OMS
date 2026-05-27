package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;

import java.util.UUID;

import static org.example.orderservice.application.service.CreateOrderCommandBuilder.aCreateOrderCommand;
import static org.example.orderservice.application.service.CreateOrderCommandBuilder.orderItem;
import static org.example.orderservice.domain.model.data.OrderTestData.EUR;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

final class OrderCommandTestData {

    static final String REJECTED_BY_BANK = "REJECTED_BY_BANK";

    private OrderCommandTestData() {
    }

    static UUID customerId() {
        return UUID.randomUUID();
    }

    static UUID orderId() {
        return UUID.randomUUID();
    }

    static CreateOrderCommand createOrderWithTwoPlnItems(UUID customerId) {
        return aCreateOrderCommand()
                .withCustomerId(customerId)
                .withItems(
                        orderItem("25.00", PLN, 2),
                        orderItem("50.00", PLN, 1)
                )
                .build();
    }

    static CreateOrderCommand createOrderWithMixedCurrencies(UUID customerId) {
        return aCreateOrderCommand()
                .withCustomerId(customerId)
                .withItems(
                        orderItem("10.00", PLN, 1),
                        orderItem("10.00", EUR, 1)
                )
                .build();
    }

    static CompletePaymentCommand paymentCompletedFor(UUID orderId) {
        return new CompletePaymentCommand(orderId, UUID.randomUUID());
    }

    static CancelOrderCommand paymentRejectedByBankFor(UUID orderId) {
        return new CancelOrderCommand(orderId, REJECTED_BY_BANK);
    }
}

