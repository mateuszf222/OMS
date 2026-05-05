package org.example.orderservice.infrastructure.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderUseCase;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentUseCase;
import org.example.orderservice.domain.exception.OrderDomainException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventListener {

    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentCompletedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void handlePaymentCompleted(PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        log.info("Received PaymentCompletedEvent for orderId: {}", event.orderId());

        try {
            completePaymentUseCase.completePayment(
                    new CompletePaymentCommand(event.orderId(), event.paymentId())
            );
            log.info("Order {} confirmed successfully", event.orderId());
            acknowledgment.acknowledge();

        } catch (OrderDomainException e) {
            log.warn("Idempotent skip for order {}: {}", event.orderId(), e.getMessage());
            acknowledgment.acknowledge();

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent modification detected for order {}. Event will be redelivered.", event.orderId());

        } catch (Exception e) {
            log.error("Unexpected error processing PaymentCompletedEvent for orderId: {}", event.orderId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentFailedEvents}", groupId = "#{@kafkaTopicsProperties.groups.orderService}")
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment acknowledgment) {
        log.info("Received PaymentFailedEvent for orderId: {}", event.orderId());

        try {
            cancelOrderUseCase.cancelOrder(new CancelOrderCommand(event.orderId(), event.reason()));
            log.info("Order {} cancelled successfully due to: {}", event.orderId(), event.reason());
            acknowledgment.acknowledge();

        } catch (OrderDomainException e) {
            log.warn("Idempotent skip for order {}: {}", event.orderId(), e.getMessage());
            acknowledgment.acknowledge();

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent modification detected for order {}. Event will be redelivered.", event.orderId());

        } catch (Exception e) {
            log.error("Unexpected error processing PaymentFailedEvent for orderId: {}", event.orderId(), e);
            throw e;
        }
    }
}