package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.event.OrderCancelledDomainEvent;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
import org.example.orderservice.application.exception.OrderNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.application.service.OrderCommandTestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandService")
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderCommandService service;

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        void shouldCreatePendingOrderAndPersistIt() {
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var customerId = customerId();
            CreateOrderCommand command = createOrderWithTwoPlnItems(customerId);

            var createdOrderId = service.createOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            Order savedOrder = captor.getValue();

            OrderAssert.assertThat(savedOrder)
                    .hasId(createdOrderId)
                    .isPendingOrder()
                    .belongsTo(customerId)
                    .hasTotalValueOf("100.00 PLN")
                    .hasExactly(2).items()
                    .emittedEvent(OrderCreatedDomainEvent.class);
        }

        @Test
        void shouldRejectOrderWithMixedCurrencies() {
            CreateOrderCommand command = createOrderWithMixedCurrencies(customerId());

            assertThatExceptionOfType(OrderItemsMustUseSameCurrencyException.class)
                    .isThrownBy(() -> service.createOrder(command))
                    .withMessageContaining("tej samej walucie");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("completePayment")
    class CompletePayment {

        @Test
        void shouldMarkExistingPendingOrderAsPaid() {
            Order order = OrderBuilder.anOrder().build();
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            service.completePayment(paymentCompletedFor(order.getId()));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            OrderAssert.assertThat(captor.getValue()).isConfirmed();
        }

        @Test
        void shouldThrowWhenOrderDoesNotExist() {
            var nonExistingId = orderId();
            when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> service.completePayment(paymentCompletedFor(nonExistingId)));

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        void shouldCancelOrderWithReasonFromPaymentFailedEvent() {
            Order order = OrderBuilder.anOrder().build();
            order.pullDomainEvents();

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            CancelOrderCommand command = paymentRejectedByBankFor(order.getId());

            service.cancelOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            OrderAssert.assertThat(captor.getValue())
                    .isCancelled()
                    .emittedEvent(OrderCancelledDomainEvent.class)
                    .emittedCancellationBecause(REJECTED_BY_BANK);
        }
    }
}

