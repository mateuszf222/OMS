package org.example.orderservice.domain.model;

import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCancelledDomainEvent;
import org.example.orderservice.domain.exception.CannotCancelOrderException;
import org.example.orderservice.domain.exception.CannotConfirmCancelledOrderException;
import org.example.orderservice.domain.exception.OrderAlreadyConfirmedException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("Powinien potwierdzic platnosc dla nowo utworzonego zamowienia")
    void shouldConfirmPayment() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        order.confirmPayment();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Nie powinien pozwolic na utworzenie zamowienia bez produktow")
    void shouldThrowExceptionWhenCreatingOrderWithoutItems() {
        assertThatThrownBy(() -> OrderBuilder.anOrder().withoutItems().build())
                .isInstanceOf(OrderMustContainProductsException.class)
                .hasMessageContaining("produkty");
    }

    @Test
    @DisplayName("Nie powinien pozwolic na podwojne potwierdzenie zamowienia")
    void shouldThrowExceptionWhenConfirmingAlreadyConfirmedOrder() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();
        order.confirmPayment();

        assertThatThrownBy(order::confirmPayment)
                .isInstanceOf(OrderAlreadyConfirmedException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    @DisplayName("Nie powinien pozwolic na potwierdzenie platnosci dla anulowanego zamowienia")
    void shouldThrowExceptionWhenConfirmingCancelledOrder() {
        Order order = OrderBuilder.anOrder().build();
        order.cancel("Brak wplaty");

        assertThatThrownBy(order::confirmPayment)
                .isInstanceOf(CannotConfirmCancelledOrderException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    @DisplayName("Powinien odtworzyc zamowienie z zapisanego stanu bez generowania zdarzen domenowych")
    void shouldRestoreOrderFromStateWithoutEmittingEvents() {
        OrderState savedState = OrderStateBuilder.anOrderState()
                .withStatus(OrderStatus.CONFIRMED)
                .build();

        Order order = Order.restore(savedState);

        assertThat(order)
                .isNotNull()
                .satisfies(o -> {
                    assertThat(o.getId()).isEqualTo(savedState.id());
                    assertThat(o.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
                    assertThat(o.pullDomainEvents()).as("Restore from persistence must not emit events").isEmpty();
                });
    }

    @Test
    @DisplayName("Powinien anulowac zamowienie w statusie PENDING i wyemitowac zdarzenie")
    void shouldCancelPendingOrder() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        order.cancel("Brak wplaty w terminie 15 minut");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        List<DomainEvent> events = order.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(OrderCancelledDomainEvent.class);
    }

    @Test
    @DisplayName("Nie powinien pozwolic na anulowanie zamowienia, ktore zostalo juz anulowane")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
        Order order = OrderBuilder.anOrder().build();
        order.cancel("Powod 1");

        assertThatThrownBy(() -> order.cancel("Powod 2"))
                .isInstanceOf(CannotCancelOrderException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    @DisplayName("Powinien pozwolic na anulowanie zamowienia, ktore jest juz oplacone")
    void shouldCancelConfirmedOrder() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();
        order.confirmPayment();

        order.cancel("Rezygnacja klienta");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.pullDomainEvents())
                .singleElement()
                .satisfies(event -> assertThat(event)
                        .hasFieldOrPropertyWithValue("previousStatus", OrderStatus.CONFIRMED));
    }
}
