package org.example.orderservice.infrastructure.adapter.in.messaging;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderUseCase;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentUseCase;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.LIMIT_EXCEEDED;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.TIMEOUT;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.cancelOrderCommandFor;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.completePaymentCommandFor;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.paymentCompletedEvent;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.paymentFailedBecauseLimitExceeded;
import static org.example.orderservice.infrastructure.adapter.in.messaging.PaymentEventTestData.paymentFailedEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaPaymentEventListenerTest {

    @Mock
    private CompletePaymentUseCase completePaymentUseCase;

    @Mock
    private CancelOrderUseCase cancelOrderUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private RedisMessageDeduplicator messageDeduplicator;

    @InjectMocks
    private KafkaPaymentEventListener listener;

    @Test
    void shouldCompleteOrderAndAcknowledgePaymentCompletedEvent() {
        PaymentCompletedEvent event = paymentCompletedEvent();
        messageClaimWillBeAccepted();

        listener.completeOrderPaymentAfterPaymentCompleted(event, null, acknowledgment);

        ArgumentCaptor<CompletePaymentCommand> commandCaptor = ArgumentCaptor.forClass(CompletePaymentCommand.class);
        verify(completePaymentUseCase).completePayment(commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(completePaymentCommandFor(event));
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeBusinessRefusalAsIdempotentCompletedEvent() {
        PaymentCompletedEvent event = paymentCompletedEvent();
        messageClaimWillBeAccepted();
        doThrow(new InvalidOrderStateTransitionException(
                OrderStatus.CONFIRMED,
                OrderStatus.CONFIRMED,
                "apply successful payment"
        ))
                .when(completePaymentUseCase)
                .completePayment(completePaymentCommandFor(event));

        listener.completeOrderPaymentAfterPaymentCompleted(event, null, acknowledgment);

        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeCompletedEventWhenOptimisticLockingFails() {
        PaymentCompletedEvent event = paymentCompletedEvent();
        messageClaimWillBeAccepted();
        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(completePaymentUseCase)
                .completePayment(completePaymentCommandFor(event));

        listener.completeOrderPaymentAfterPaymentCompleted(event, null, acknowledgment);

        verify(messageDeduplicator).releaseMessageClaim(any(MessageDeduplicationKey.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldCancelOrderAndAcknowledgePaymentFailedEventWithReason() {
        PaymentFailedEvent event = paymentFailedBecauseLimitExceeded();
        messageClaimWillBeAccepted();

        listener.cancelOrderAfterPaymentFailed(event, null, acknowledgment);

        ArgumentCaptor<CancelOrderCommand> commandCaptor = ArgumentCaptor.forClass(CancelOrderCommand.class);
        verify(cancelOrderUseCase).cancelOrder(commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(cancelOrderCommandFor(event));
        assertThat(commandCaptor.getValue().reason()).isEqualTo(LIMIT_EXCEEDED);
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSkipDuplicatePaymentCompletedEventBeforeBusinessUseCase() {
        PaymentCompletedEvent event = paymentCompletedEvent();
        messageClaimWillBeRejected();

        listener.completeOrderPaymentAfterPaymentCompleted(event, null, acknowledgment);

        verifyNoInteractions(completePaymentUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldRethrowUnexpectedFailureForKafkaRedelivery() {
        PaymentFailedEvent event = paymentFailedEvent(TIMEOUT);
        RuntimeException unexpected = new RuntimeException("database unavailable");
        messageClaimWillBeAccepted();
        doThrow(unexpected)
                .when(cancelOrderUseCase)
                .cancelOrder(cancelOrderCommandFor(event));

        assertThatThrownBy(() -> listener.cancelOrderAfterPaymentFailed(event, null, acknowledgment))
                .isSameAs(unexpected);

        verify(messageDeduplicator).releaseMessageClaim(any(MessageDeduplicationKey.class));
        verify(acknowledgment, never()).acknowledge();
    }

    private void messageClaimWillBeAccepted() {
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(true);
    }

    private void messageClaimWillBeRejected() {
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(false);
    }
}

