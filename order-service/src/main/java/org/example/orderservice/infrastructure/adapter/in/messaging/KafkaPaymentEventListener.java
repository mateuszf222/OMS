package org.example.orderservice.infrastructure.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.CancelOrderUseCase;
import org.example.orderservice.application.port.in.CompletePaymentUseCase;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.domain.model.OrderStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventListener {

    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentCompletedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void handlePaymentCompleted(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        log.info("Received PaymentCompletedEvent for orderId: {}", event.orderId());

        try {
            orderRepository.findById(event.orderId()).ifPresentOrElse(
                    order -> {
                        if (order.getStatus() == OrderStatus.PENDING) {
                            completePaymentUseCase.completePayment(event.orderId());
                            log.info("Order {} confirmed successfully", event.orderId());
                        } else {
                            log.info("Order {} already in status {}. Skipping duplicate event.",
                                    event.orderId(), order.getStatus());
                        }
                    },
                    () -> log.warn("Order {} not found. Ignoring PaymentCompletedEvent.", event.orderId())
            );

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process PaymentCompletedEvent for orderId: {}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentFailedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment acknowledgment) {
        log.info("Received PaymentFailedEvent for orderId: {}", event.orderId());

        try {
            orderRepository.findById(event.orderId()).ifPresentOrElse(
                    order -> {
                        if (order.getStatus() == OrderStatus.PENDING) {
                            cancelOrderUseCase.cancelOrder(event.orderId(), event.reason());
                            log.info("Order {} cancelled successfully due to: {}", event.orderId(), event.reason());
                        } else {
                            log.info("Order {} already in status {}. Skipping duplicate event.",
                                    event.orderId(), order.getStatus());
                        }
                    },
                    () -> log.warn("Order {} not found. Ignoring PaymentFailedEvent.", event.orderId())
            );

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process PaymentFailedEvent for orderId: {}", event.orderId(), e);
            throw e;
        }
    }
}