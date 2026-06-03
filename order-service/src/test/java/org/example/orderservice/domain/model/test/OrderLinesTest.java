package org.example.orderservice.domain.model.test;

import org.example.orderservice.domain.model.assertion.MoneyAssert;

import org.example.orderservice.domain.exception.OrderLineCannotBeNullException;
import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.EXPECTED_TOTAL_FOR_TWO_PLN_LINES;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.emptyLines;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.extraPlnLineForSnapshotMutation;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.mixedCurrencyLines;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.singlePlnLine;
import static org.example.orderservice.domain.model.data.OrderLinesTestData.twoPlnLines;
import static org.example.orderservice.domain.model.data.OrderTestData.orderLinesWith;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;

class OrderLinesTest {

    @Test
    void shouldReturnTotalAmountForAllLinesInSingleCurrency() {
        MoneyAssert.assertThat(twoPlnLines().totalAmount())
                .hasValue(EXPECTED_TOTAL_FOR_TWO_PLN_LINES, PLN);
    }

    @Test
    void shouldRejectEmptyLines() {
        assertThatExceptionOfType(OrderMustContainProductsException.class)
                .isThrownBy(() -> emptyLines().build())
                .withMessageContaining("produkty");
    }

    @Test
    void shouldRejectMixedCurrenciesInsideSingleOrderAggregate() {
        assertThatExceptionOfType(OrderItemsMustUseSameCurrencyException.class)
                .isThrownBy(() -> mixedCurrencyLines())
                .withMessageContaining("tej samej walucie");
    }

    @Test
    void shouldRejectNullLineInsideOrderAggregate() {
        assertThatExceptionOfType(OrderLineCannotBeNullException.class)
                .isThrownBy(() -> orderLinesWith((org.example.orderservice.domain.model.OrderItem) null));
    }

    @Test
    void shouldExposeImmutableSnapshotOfLines() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> singlePlnLine().toList().add(extraPlnLineForSnapshotMutation()));
    }
}

