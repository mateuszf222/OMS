package org.example.orderservice;

import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

final class OrderApiTestData {

    static final String EXPECTED_SINGLE_ITEM_TOTAL = "300.00";
    static final String PLN = "PLN";

    private OrderApiTestData() {
    }

    static UUID customerId() {
        return UUID.randomUUID();
    }

    static String consumerGroupId() {
        return "manual-test-group-" + UUID.randomUUID();
    }

    static CreateOrderRequest createOrderRequestWithSinglePlnItem() {
        return new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderItemRequest(
                        UUID.randomUUID(),
                        2,
                        new BigDecimal("150.00"),
                        PLN
                ))
        );
    }
}

