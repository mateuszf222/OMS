package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByAdminCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByAdminUseCase;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerUseCase;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderDueToPaymentFailureCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderDueToPaymentFailureUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancellationService implements
        CancelOrderByCustomerUseCase,
        CancelOrderByAdminUseCase,
        CancelOrderDueToPaymentFailureUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void cancelOrderByCustomer(CancelOrderByCustomerCommand command) {
        log.debug(
                "Customer {} attempts to cancel order {} with reason {}",
                command.customerId(),
                command.orderId(),
                command.reason().value()
        );

        Order order = findOrder(command.orderId());

        if (!order.getCustomerId().equals(command.customerId())) {
            log.warn("Customer {} attempted to cancel someone else's order {}", command.customerId(), command.orderId());
            throw new OrderNotFoundException(command.orderId());
        }

        order.cancelByCustomer(command.reason());
        orderRepository.save(order);

        log.info("Order {} cancelled by customer {}", command.orderId(), command.customerId());
    }

    @Override
    @Transactional
    public void cancelOrderByAdmin(CancelOrderByAdminCommand command) {
        log.debug(
                "Admin {} cancels order {} with reason {}",
                command.adminId(),
                command.orderId(),
                command.reason().value()
        );

        Order order = findOrder(command.orderId());
        order.cancelByAdmin(command.adminId(), command.reason());
        orderRepository.save(order);

        log.info("Order {} cancelled by admin {}", command.orderId(), command.adminId());
    }

    @Override
    @Transactional
    public void cancelOrderDueToPaymentFailure(CancelOrderDueToPaymentFailureCommand command) {
        log.debug(
                "Cancelling order {} after failed payment {} with reason {}",
                command.orderId(),
                command.paymentId(),
                command.reason().value()
        );

        Order order = findOrder(command.orderId());
        order.cancelDueToPaymentFailure(command.paymentId(), command.reason());
        orderRepository.save(order);

        log.info("Order {} cancelled due to failed payment {}", command.orderId(), command.paymentId());
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
