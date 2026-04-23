package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.CancelOrderUseCase;
import org.example.orderservice.application.port.in.CompletePaymentUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.exception.OrderNotFoundException;
import org.example.orderservice.domain.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaCommandService implements CompletePaymentUseCase, CancelOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void completePayment(UUID orderId) {
        log.debug("Attempting to complete payment for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.confirmPayment();
        orderRepository.save(order);

        log.info("Payment completed for order: {}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        log.debug("Attempting to cancel order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel(reason);
        orderRepository.save(order);

        log.info("Order {} cancelled with reason: {}", orderId, reason);
    }
}