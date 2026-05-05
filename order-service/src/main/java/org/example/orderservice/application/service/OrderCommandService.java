package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderUseCase;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentUseCase;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.exception.OrderNotFoundException;
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
        CancelOrderUseCase,
        CompletePaymentUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        List<OrderItem> domainItems = command.items().stream()
                .map(item -> new OrderItem(
                        UUID.randomUUID(),
                        item.productId(),
                        item.quantity(),
                        new Money(item.price(), item.currency())
                ))
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

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.confirmPayment();
        orderRepository.save(order);

        log.info("Payment completed for order: {}", command.orderId());
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        log.debug("Attempting to cancel order: {} with reason: {}", command.orderId(), command.reason());

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        order.cancel(command.reason());
        orderRepository.save(order);

        log.info("Order {} cancelled with reason: {}", command.orderId(), command.reason());
    }
}