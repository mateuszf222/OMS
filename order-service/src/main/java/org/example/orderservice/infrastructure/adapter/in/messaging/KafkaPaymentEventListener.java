package org.example.orderservice.infrastructure.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.CancelOrderUseCase;
import org.example.orderservice.application.port.in.CompletePaymentUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventListener {

    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentCompletedEvents}", groupId = "order-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Odebrano potwierdzenie płatności dla zamówienia: {}", event.orderId());
        completePaymentUseCase.completePayment(event.orderId());
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentFailedEvents}", groupId = "order-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Odebrano odrzucenie płatności dla zamówienia: {}", event.orderId());
        cancelOrderUseCase.cancelOrder(event.orderId(), event.reason());
    }
}