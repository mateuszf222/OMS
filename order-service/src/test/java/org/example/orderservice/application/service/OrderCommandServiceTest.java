package org.example.orderservice.application.service;

import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.application.exception.ProductNotAvailableException;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.application.port.out.ProductPriceCatalog;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.example.orderservice.domain.exception.OrderItemsMustUseSameCurrencyException;
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

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.orderservice.application.service.OrderCommandTestData.SECOND_PRODUCT_ID;
import static org.example.orderservice.application.service.OrderCommandTestData.STANDARD_PRODUCT_ID;
import static org.example.orderservice.application.service.OrderCommandTestData.UNKNOWN_PRODUCT_ID;
import static org.example.orderservice.application.service.OrderCommandTestData.createOrderWithMixedCurrencies;
import static org.example.orderservice.application.service.OrderCommandTestData.createOrderWithTwoPlnItems;
import static org.example.orderservice.application.service.OrderCommandTestData.createOrderWithUnavailableProduct;
import static org.example.orderservice.application.service.OrderCommandTestData.customerId;
import static org.example.orderservice.application.service.OrderCommandTestData.money;
import static org.example.orderservice.application.service.OrderCommandTestData.orderId;
import static org.example.orderservice.application.service.OrderCommandTestData.paymentCompletedFor;
import static org.example.orderservice.domain.model.data.OrderTestData.EUR;
import static org.example.orderservice.domain.model.data.OrderTestData.PLN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandService")
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductPriceCatalog productPriceCatalog;

    @InjectMocks
    private OrderCommandService service;

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        void shouldCreatePendingOrderAndPersistIt() {
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            productPriceWillBe(STANDARD_PRODUCT_ID, "25.00", PLN);
            productPriceWillBe(SECOND_PRODUCT_ID, "50.00", PLN);

            var customerId = customerId();
            var command = createOrderWithTwoPlnItems(customerId);

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
            productPriceWillBe(STANDARD_PRODUCT_ID, "10.00", PLN);
            productPriceWillBe(SECOND_PRODUCT_ID, "10.00", EUR);

            var command = createOrderWithMixedCurrencies(customerId());

            assertThatExceptionOfType(OrderItemsMustUseSameCurrencyException.class)
                    .isThrownBy(() -> service.createOrder(command))
                    .withMessageContaining("tej samej walucie");

            verify(orderRepository, never()).save(any());
        }

        @Test
        void shouldRejectUnavailableProductBeforePersistingOrder() {
            when(productPriceCatalog.priceFor(UNKNOWN_PRODUCT_ID))
                    .thenThrow(new ProductNotAvailableException(UNKNOWN_PRODUCT_ID));

            var command = createOrderWithUnavailableProduct(customerId());

            assertThatExceptionOfType(ProductNotAvailableException.class)
                    .isThrownBy(() -> service.createOrder(command));

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

    private void productPriceWillBe(UUID productId, String amount, Currency currency) {
        when(productPriceCatalog.priceFor(productId)).thenReturn(money(amount, currency));
    }
}
