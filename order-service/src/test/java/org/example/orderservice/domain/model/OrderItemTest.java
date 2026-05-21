package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.InvalidOrderItemQuantityException;
import org.example.orderservice.domain.exception.MissingOrderItemDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.orderservice.domain.model.OrderItemTestData.EXPECTED_SUBTOTAL_FOR_THREE_ITEMS;
import static org.example.orderservice.domain.model.OrderItemTestData.itemPricedAt1999WithQuantityThree;
import static org.example.orderservice.domain.model.OrderItemTestData.itemWithQuantity;
import static org.example.orderservice.domain.model.OrderItemTestData.itemWithoutId;
import static org.example.orderservice.domain.model.OrderItemTestData.itemWithoutProductId;
import static org.example.orderservice.domain.model.OrderItemTestData.itemWithoutUnitPrice;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

class OrderItemTest {

    @Test
    void shouldCalculateSubtotalFromUnitPriceAndQuantity() {
        OrderItem item = itemPricedAt1999WithQuantityThree();

        Money subtotal = item.getSubtotal();

        assertThat(subtotal.amount()).isEqualByComparingTo(EXPECTED_SUBTOTAL_FOR_THREE_ITEMS);
        assertThat(subtotal.currency()).isEqualTo(PLN);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> itemWithQuantity(quantity).build())
                .isInstanceOf(InvalidOrderItemQuantityException.class)
                .hasMessageContaining("0");
    }

    @Test
    void shouldRejectMissingItemId() {
        assertThatThrownBy(() -> itemWithoutId().build())
                .isInstanceOf(MissingOrderItemDataException.class);
    }

    @Test
    void shouldRejectMissingProductId() {
        assertThatThrownBy(() -> itemWithoutProductId().build())
                .isInstanceOf(MissingOrderItemDataException.class);
    }

    @Test
    void shouldRejectMissingUnitPrice() {
        assertThatThrownBy(() -> itemWithoutUnitPrice().build())
                .isInstanceOf(MissingOrderItemDataException.class);
    }
}
