package org.example.orderservice;

import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;

import java.util.List;
import java.util.UUID;

import static org.example.orderservice.infrastructure.adapter.out.pricing.InMemoryProductPriceCatalog.STANDARD_PRODUCT_ID;

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
                        STANDARD_PRODUCT_ID,
                        2
                ))
        );
    }
}

