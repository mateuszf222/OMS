package org.example.orderservice.domain.model.test;

import org.example.orderservice.domain.model.builder.OrderStateBuilder;

import org.example.orderservice.domain.model.builder.OrderBuilder;

import org.example.orderservice.domain.model.assertion.OrderExceptionAssert;

import org.example.orderservice.domain.model.assertion.OrderAssert;

import org.example.orderservice.domain.model.OrderState;

import org.example.orderservice.domain.model.Order;

import org.example.orderservice.domain.event.OrderCancelledDomainEvent;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.domain.model.OrderStatus.CANCELLED;
import static org.example.orderservice.domain.model.OrderStatus.CONFIRMED;

@DisplayName("Order Aggregate")
class OrderTest {

    private static final String APPLY_SUCCESSFUL_PAYMENT_ACTION = "apply successful payment";
    private static final String CANCEL_ACTION = "cancel order";

    @Test
    @DisplayName("powinien potwierdzić płatność dla nowo utworzonego zamówienia")
    void shouldConfirmOrderAfterSuccessfulPayment() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        order.applySuccessfulPayment();

        OrderAssert.assertThat(order).isConfirmed();
    }

    @Test
    @DisplayName("nie powinien pozwolić na utworzenie zamówienia bez produktów")
    void shouldThrowExceptionWhenCreatingOrderWithoutItems() {
        assertThatExceptionOfType(OrderMustContainProductsException.class)
                .isThrownBy(() -> OrderBuilder.anOrder().withoutItems().build())
                .withMessageContaining("produkty");
    }

    @Nested
    @DisplayName("when applying successful payment")
    class ApplySuccessfulPayment {

        @Test
        @DisplayName("should throw exception when order is already confirmed")
        void shouldThrowWhenAlreadyConfirmed() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();
            order.applySuccessfulPayment();

            assertThatExceptionOfType(InvalidOrderStateTransitionException.class)
                    .isThrownBy(order::applySuccessfulPayment)
                    .satisfies(ex -> OrderExceptionAssert.assertThat(ex)
                            .hasCurrentStatus(CONFIRMED)
                            .hasTargetStatus(CONFIRMED)
                            .hasAction(APPLY_SUCCESSFUL_PAYMENT_ACTION)
                            .hasMessageContaining(APPLY_SUCCESSFUL_PAYMENT_ACTION, "CONFIRMED")
                    );

            OrderAssert.assertThat(order).isConfirmed();
        }

        @Test
        @DisplayName("should throw exception when order is cancelled")
        void shouldThrowWhenOrderIsCancelled() {
            Order order = OrderBuilder.anOrder().build();
            order.cancelWithReason("Brak wpłaty");

            assertThatExceptionOfType(InvalidOrderStateTransitionException.class)
                    .isThrownBy(order::applySuccessfulPayment)
                    .satisfies(ex -> OrderExceptionAssert.assertThat(ex)
                            .hasCurrentStatus(CANCELLED)
                            .hasTargetStatus(CONFIRMED)
                            .hasAction(APPLY_SUCCESSFUL_PAYMENT_ACTION)
                            .hasMessageContaining(APPLY_SUCCESSFUL_PAYMENT_ACTION, "CANCELLED", "CONFIRMED")
                    );

            OrderAssert.assertThat(order).isCancelled();
        }
    }

    @Nested
    @DisplayName("when cancelling order")
    class CancelOrder {

        @Test
        @DisplayName("should cancel pending order and emit event")
        void shouldCancelPendingOrder() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();

            order.cancelWithReason("Brak wpłaty w terminie 15 minut");

            OrderAssert.assertThat(order)
                    .isCancelled()
                    .emittedEvent(OrderCancelledDomainEvent.class);
        }

        @Test
        @DisplayName("should allow cancelling confirmed order")
        void shouldAllowCancellingConfirmedOrder() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();
            order.applySuccessfulPayment();

            order.cancelWithReason("Rezygnacja klienta");

            OrderAssert.assertThat(order)
                    .isCancelled()
                    .emittedCancellationFrom(CONFIRMED);
        }

        @Test
        @DisplayName("should throw exception when order is already cancelled")
        void shouldThrowWhenAlreadyCancelled() {
            Order order = OrderBuilder.anOrder().build();
            order.cancelWithReason("Powód 1");

            assertThatExceptionOfType(InvalidOrderStateTransitionException.class)
                    .isThrownBy(() -> order.cancelWithReason("Powód 2"))
                    .satisfies(ex -> OrderExceptionAssert.assertThat(ex)
                            .hasCurrentStatus(CANCELLED)
                            .hasTargetStatus(CANCELLED)
                            .hasAction(CANCEL_ACTION)
                            .hasMessageContaining(CANCEL_ACTION, "CANCELLED")
                    );

            OrderAssert.assertThat(order).isCancelled();
        }
    }

    @Test
    @DisplayName("powinien odtworzyć zamówienie z zapisanego stanu bez generowania zdarzeń domenowych")
    void shouldRestoreOrderFromStateWithoutEmittingEvents() {
        OrderState savedState = OrderStateBuilder.anOrderState()
                .withStatus(CONFIRMED)
                .build();

        Order order = Order.restore(savedState);

        OrderAssert.assertThat(order)
                .hasId(savedState.id())
                .belongsTo(savedState.customerId())
                .isConfirmed()
                .wasCreatedAt(savedState.createdAt().toInstant())
                .hasNoDomainEvents();
    }
}

