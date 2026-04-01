package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.in.CancelOrderUseCase;
import org.example.orderservice.application.port.in.CompletePaymentUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.exception.OrderDomainException;
import org.example.orderservice.domain.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSagaCommandService implements CompletePaymentUseCase, CancelOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void completePayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderDomainException("Order not found: " + orderId));
        order.confirmPayment();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderDomainException("Order not found: " + orderId));
        order.cancel(reason);
        orderRepository.save(order);
    }
}