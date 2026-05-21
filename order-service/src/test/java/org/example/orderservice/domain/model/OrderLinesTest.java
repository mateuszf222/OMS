package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.orderservice.domain.model.OrderLinesTestData.EXPECTED_TOTAL_FOR_TWO_PLN_LINES;
import static org.example.orderservice.domain.model.OrderLinesTestData.emptyLines;
import static org.example.orderservice.domain.model.OrderLinesTestData.extraPlnLineForSnapshotMutation;
import static org.example.orderservice.domain.model.OrderLinesTestData.mixedCurrencyLines;
import static org.example.orderservice.domain.model.OrderLinesTestData.singlePlnLine;
import static org.example.orderservice.domain.model.OrderLinesTestData.twoPlnLines;
import static org.example.orderservice.domain.model.OrderTestData.PLN;

class OrderLinesTest {

    @Test
    void shouldCalculateTotalForAllLinesInSingleCurrency() {
        OrderLines lines = twoPlnLines();

        Money total = lines.calculateTotal();

        assertThat(total.amount()).isEqualByComparingTo(EXPECTED_TOTAL_FOR_TWO_PLN_LINES);
        assertThat(total.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldRejectEmptyLines() {
        assertThatThrownBy(() -> emptyLines().build())
                .isInstanceOf(OrderMustContainProductsException.class)
                .hasMessageContaining("produkty");
    }

    @Test
    void shouldRejectMixedCurrenciesInsideSingleOrderAggregate() {
        assertThatThrownBy(() -> mixedCurrencyLines())
                .isInstanceOf(OrderItemsMustUseSameCurrencyException.class)
                .hasMessageContaining("tej samej walucie");
    }

    @Test
    void shouldExposeImmutableSnapshotOfLines() {
        OrderLines lines = singlePlnLine();

        assertThatThrownBy(() -> lines.toList().add(extraPlnLineForSnapshotMutation()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
