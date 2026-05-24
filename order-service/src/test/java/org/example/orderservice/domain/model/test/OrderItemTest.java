package org.example.orderservice.domain.model.test;

import org.example.orderservice.domain.model.assertion.MoneyAssert;

import org.example.orderservice.domain.exception.InvalidOrderItemQuantityException;
import org.example.orderservice.domain.exception.MissingOrderItemDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.domain.model.data.OrderItemTestData.EXPECTED_SUBTOTAL_FOR_THREE_ITEMS;
import static org.example.orderservice.domain.model.data.OrderItemTestData.itemPricedAt1999WithQuantityThree;
import static org.example.orderservice.domain.model.data.OrderItemTestData.itemWithQuantity;
import static org.example.orderservice.domain.model.data.OrderItemTestData.itemWithoutId;
import static org.example.orderservice.domain.model.data.OrderItemTestData.itemWithoutProductId;
import static org.example.orderservice.domain.model.data.OrderItemTestData.itemWithoutUnitPrice;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

class OrderItemTest {

    @Test
    void shouldCalculateSubtotalFromUnitPriceAndQuantity() {
        MoneyAssert.assertThat(itemPricedAt1999WithQuantityThree().getSubtotal())
                .hasValue(EXPECTED_SUBTOTAL_FOR_THREE_ITEMS, PLN);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatExceptionOfType(InvalidOrderItemQuantityException.class)
                .isThrownBy(() -> itemWithQuantity(quantity).build())
                .withMessageContaining("0");
    }

    @Test
    void shouldRejectMissingItemId() {
        assertThatExceptionOfType(MissingOrderItemDataException.class)
                .isThrownBy(() -> itemWithoutId().build());
    }

    @Test
    void shouldRejectMissingProductId() {
        assertThatExceptionOfType(MissingOrderItemDataException.class)
                .isThrownBy(() -> itemWithoutProductId().build());
    }

    @Test
    void shouldRejectMissingUnitPrice() {
        assertThatExceptionOfType(MissingOrderItemDataException.class)
                .isThrownBy(() -> itemWithoutUnitPrice().build());
    }
}

