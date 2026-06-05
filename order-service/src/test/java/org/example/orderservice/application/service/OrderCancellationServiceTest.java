package org.example.orderservice.application.service;

import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.event.OrderCancelledByAdminEvent;
import org.example.orderservice.domain.event.OrderCancelledByCustomerEvent;
import org.example.orderservice.domain.event.OrderCancelledDueToPaymentFailureEvent;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.assertion.OrderAssert;
import org.example.orderservice.domain.model.builder.OrderBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.application.service.OrderCommandTestData.ADMIN_CANCELLATION_REASON;
import static org.example.orderservice.application.service.OrderCommandTestData.CANCELLED_BY_CUSTOMER;
import static org.example.orderservice.application.service.OrderCommandTestData.REJECTED_BY_BANK;
import static org.example.orderservice.application.service.OrderCommandTestData.adminCancelledOrderFor;
import static org.example.orderservice.application.service.OrderCommandTestData.customerCancelledOrderFor;
import static org.example.orderservice.application.service.OrderCommandTestData.customerIdOtherThan;
import static org.example.orderservice.application.service.OrderCommandTestData.paymentRejectedByBankFor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancellationService")
class OrderCancellationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderCancellationService service;

    @Nested
    @DisplayName("cancelOrderByCustomer")
    class CancelOrderByCustomer {

        @Test
        void shouldCancelCustomerOrderOwnedByAuthenticatedCustomer() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            service.cancelOrderByCustomer(customerCancelledOrderFor(order.getId(), order.getCustomerId()));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            OrderAssert.assertThat(captor.getValue())
                    .isCancelled()
                    .emittedEvent(OrderCancelledByCustomerEvent.class)
                    .emittedCancellationBecause(CANCELLED_BY_CUSTOMER);
        }

        @Test
        void shouldHideOrderWhenAuthenticatedCustomerDoesNotOwnIt() {
            Order order = OrderBuilder.anOrder().build();
            UUID otherCustomerId = customerIdOtherThan(order.getCustomerId());

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> service.cancelOrderByCustomer(
                            customerCancelledOrderFor(order.getId(), otherCustomerId)
                    ))
                    .satisfies(exception -> assertThat(exception.getOrderId()).isEqualTo(order.getId()));

            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    void shouldCancelOrderByAdminWithoutCustomerOwnershipCheck() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        service.cancelOrderByAdmin(adminCancelledOrderFor(order.getId()));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        OrderAssert.assertThat(captor.getValue())
                .isCancelled()
                .emittedEvent(OrderCancelledByAdminEvent.class)
                .emittedCancellationBecause(ADMIN_CANCELLATION_REASON);
    }

    @Test
    void shouldCancelOrderWithReasonFromPaymentFailedEvent() {
        Order order = OrderBuilder.anOrder().build();
        order.pullDomainEvents();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        service.cancelOrderDueToPaymentFailure(paymentRejectedByBankFor(order.getId()));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        OrderAssert.assertThat(captor.getValue())
                .isCancelled()
                .emittedEvent(OrderCancelledDueToPaymentFailureEvent.class)
                .emittedCancellationBecause(REJECTED_BY_BANK);
    }
}
