package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentUseCase;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.application.port.out.ProductPriceCatalog;
import org.example.orderservice.domain.model.Money;
import org.example.orderservice.domain.model.Order;
import org.example.orderservice.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandService implements
        CreateOrderUseCase,
        CompletePaymentUseCase {

    private final OrderRepository orderRepository;
    private final ProductPriceCatalog productPriceCatalog;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        List<OrderItem> domainItems = command.items().stream()
                .map(this::pricedOrderItem)
                .toList();

        Order order = Order.create(command.customerId(), domainItems);

        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    @Override
    @Transactional
    public void completePayment(CompletePaymentCommand command) {
        log.debug("Attempting to complete payment for order: {} using paymentId: {}",
                command.orderId(), command.paymentId());

        Order order = findOrder(command.orderId());

        order.applySuccessfulPayment();
        orderRepository.save(order);

        log.info("Order {} confirmed after successful payment.", command.orderId());
    }

    private OrderItem pricedOrderItem(CreateOrderCommand.OrderItemCommand item) {
        Money trustedUnitPrice = productPriceCatalog.priceFor(item.productId());

        return new OrderItem(
                UUID.randomUUID(),
                item.productId(),
                item.quantity(),
                trustedUnitPrice
        );
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
