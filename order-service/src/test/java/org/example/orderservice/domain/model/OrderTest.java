package org.example.orderservice.domain.model;

import org.example.orderservice.domain.exception.OrderDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final Currency PLN = Currency.getInstance("PLN");
    private final Currency EUR = Currency.getInstance("EUR");
    private final UUID customerId = UUID.randomUUID();

    @Test
    void shouldCreateOrderAndCalculateTotalAmount() {
        OrderItem item1 = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 2, new Money(new BigDecimal("50.00"), PLN));
        OrderItem item2 = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 1, new Money(new BigDecimal("150.00"), PLN));

        Order order = Order.create(customerId, List.of(item1, item2));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("250.00"); // 2*50 + 1*150
        assertThat(order.getTotalAmount().currency()).isEqualTo(PLN);
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderWithMixedCurrencies() {
        OrderItem item1 = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 1, new Money(new BigDecimal("50.00"), PLN));
        OrderItem item2 = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 1, new Money(new BigDecimal("50.00"), EUR));

        assertThatThrownBy(() -> Order.create(customerId, List.of(item1, item2)))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("Produkty w zamówieniu muszą być w tej samej walucie");
    }

    @Test
    void shouldConfirmPayment() {
        OrderItem item = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 1, new Money(new BigDecimal("50.00"), PLN));
        Order order = Order.create(customerId, List.of(item));

        order.confirmPayment();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldCancelOrder() {
        OrderItem item = new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 1, new Money(new BigDecimal("50.00"), PLN));
        Order order = Order.create(customerId, List.of(item));

        order.cancel("Brak środków");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}