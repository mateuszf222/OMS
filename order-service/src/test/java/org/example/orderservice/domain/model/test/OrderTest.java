package org.example.orderservice.domain.model.test;

import org.example.orderservice.domain.cancellation.CustomerCancellationReason;
import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;
import org.example.orderservice.domain.event.OrderCancelledByCustomerEvent;
import org.example.orderservice.domain.event.OrderCancelledDueToPaymentFailureEvent;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.OrderState;
import org.example.orderservice.domain.model.assertion.OrderAssert;
import org.example.orderservice.domain.model.assertion.OrderExceptionAssert;
import org.example.orderservice.domain.model.builder.OrderBuilder;
import org.example.orderservice.domain.model.builder.OrderStateBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.domain.model.OrderStatus.CANCELLED;
import static org.example.orderservice.domain.model.OrderStatus.CONFIRMED;

@DisplayName("Order Aggregate")
class OrderTest {

    private static final String APPLY_SUCCESSFUL_PAYMENT_ACTION = "apply successful payment";
    private static final String CANCEL_ACTION = "cancel order";

    @Test
    @DisplayName("should confirm a newly created order after successful payment")
    void shouldConfirmOrderAfterSuccessfulPayment() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        order.applySuccessfulPayment();

        OrderAssert.assertThat(order).isConfirmed();
    }

    @Test
    @DisplayName("should reject creating order without products")
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
            order.cancelByCustomer(new CustomerCancellationReason("No payment"));

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
        @DisplayName("should cancel pending order due to payment failure and emit explicit event")
        void shouldCancelPendingOrderDueToPaymentFailure() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();

            order.cancelDueToPaymentFailure(
                    UUID.randomUUID(),
                    new PaymentFailureCancellationReason("Payment timeout")
            );

            OrderAssert.assertThat(order)
                    .isCancelled()
                    .emittedEvent(OrderCancelledDueToPaymentFailureEvent.class);
        }

        @Test
        @DisplayName("should allow customer cancellation for confirmed order")
        void shouldAllowCustomerCancellationForConfirmedOrder() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();
            order.applySuccessfulPayment();

            order.cancelByCustomer(new CustomerCancellationReason("Customer resigned"));

            OrderAssert.assertThat(order)
                    .isCancelled()
                    .emittedEvent(OrderCancelledByCustomerEvent.class)
                    .emittedCancellationFrom(CONFIRMED);
        }

        @Test
        @DisplayName("should throw exception when order is already cancelled")
        void shouldThrowWhenAlreadyCancelled() {
            Order order = OrderBuilder.anOrder().build();
            order.cancelByCustomer(new CustomerCancellationReason("Reason 1"));

            assertThatExceptionOfType(InvalidOrderStateTransitionException.class)
                    .isThrownBy(() -> order.cancelByCustomer(new CustomerCancellationReason("Reason 2")))
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
    @DisplayName("should restore order from saved state without emitting domain events")
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
