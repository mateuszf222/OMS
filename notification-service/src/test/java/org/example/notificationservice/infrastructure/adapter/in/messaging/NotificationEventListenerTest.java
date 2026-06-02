package org.example.notificationservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationservice.NotificationTestData.NotificationIds;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.kafka.support.Acknowledgment;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.example.notificationservice.NotificationTestData.REJECTED_BY_BANK;
import static org.example.notificationservice.NotificationTestData.malformedJson;
import static org.example.notificationservice.NotificationTestData.notificationIds;
import static org.example.notificationservice.NotificationTestData.orderCreatedEvent;
import static org.example.notificationservice.NotificationTestData.paymentCompletedEvent;
import static org.example.notificationservice.NotificationTestData.paymentFailedEvent;
import static org.example.notificationservice.NotificationTestData.payload;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationEventListenerTest {

    private SendNotificationUseCase notificationUseCase;
    private ObjectMapper objectMapper;
    private RedisMessageDeduplicator messageDeduplicator;
    private Acknowledgment acknowledgment;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        notificationUseCase = mock(SendNotificationUseCase.class);
        objectMapper = new ObjectMapper();
        messageDeduplicator = mock(RedisMessageDeduplicator.class);
        acknowledgment = mock(Acknowledgment.class);
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(true);
        listener = new NotificationEventListener(notificationUseCase, objectMapper, messageDeduplicator);
    }

    @Test
    void shouldSendNotificationAfterOrderCreatedEvent() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, orderCreatedEvent(ids));

        listener.sendNotificationAfterOrderCreated(payload, null, acknowledgment);

        verify(notificationUseCase).sendOrderCreatedNotification(ids.orderId(), ids.customerId());
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSendNotificationAfterPaymentCompletedEvent() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, paymentCompletedEvent(ids));

        listener.sendNotificationAfterPaymentCompleted(payload, null, acknowledgment);

        verify(notificationUseCase).sendPaymentSuccessNotification(ids.orderId(), ids.customerId());
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSendNotificationAfterPaymentFailedEventWithReason() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, paymentFailedEvent(ids, REJECTED_BY_BANK));

        listener.sendNotificationAfterPaymentFailed(payload, null, acknowledgment);

        verify(notificationUseCase).sendPaymentFailedNotification(ids.orderId(), ids.customerId(), REJECTED_BY_BANK);
        verify(messageDeduplicator).rememberMessageAsProcessed(any(MessageDeduplicationKey.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldSkipDuplicateEventBeforeSendingNotification() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, paymentCompletedEvent(ids));
        when(messageDeduplicator.claimMessageForProcessing(any(MessageDeduplicationKey.class))).thenReturn(false);

        listener.sendNotificationAfterPaymentCompleted(payload, null, acknowledgment);

        verifyNoInteractions(notificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @ParameterizedTest
    @MethodSource("malformedPayloadListenerInvocations")
    void shouldIgnoreMalformedPayload(Consumer<NotificationEventListener> listenerInvocation) {
        listenerInvocation.accept(listener);

        verifyNoInteractions(notificationUseCase);
    }

    static Stream<Consumer<NotificationEventListener>> malformedPayloadListenerInvocations() {
        return Stream.of(
                listener -> listener.sendNotificationAfterOrderCreated(malformedJson(), null, mock(Acknowledgment.class)),
                listener -> listener.sendNotificationAfterPaymentCompleted(malformedJson(), null, mock(Acknowledgment.class)),
                listener -> listener.sendNotificationAfterPaymentFailed(malformedJson(), null, mock(Acknowledgment.class))
        );
    }
}
