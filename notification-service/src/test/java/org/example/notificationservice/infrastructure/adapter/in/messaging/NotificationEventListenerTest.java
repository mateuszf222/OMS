package org.example.notificationservice.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationservice.NotificationTestData.NotificationIds;
import org.example.notificationservice.application.port.in.SendNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.example.notificationservice.NotificationTestData.REJECTED_BY_BANK;
import static org.example.notificationservice.NotificationTestData.malformedJson;
import static org.example.notificationservice.NotificationTestData.notificationIds;
import static org.example.notificationservice.NotificationTestData.orderCreatedEvent;
import static org.example.notificationservice.NotificationTestData.paymentCompletedEvent;
import static org.example.notificationservice.NotificationTestData.paymentFailedEvent;
import static org.example.notificationservice.NotificationTestData.payload;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationEventListenerTest {

    private SendNotificationUseCase notificationUseCase;
    private ObjectMapper objectMapper;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        notificationUseCase = mock(SendNotificationUseCase.class);
        objectMapper = new ObjectMapper();
        listener = new NotificationEventListener(notificationUseCase, objectMapper);
    }

    @Test
    void shouldDispatchOrderCreatedEventToNotificationUseCase() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, orderCreatedEvent(ids));

        listener.handleOrderEvents(payload);

        verify(notificationUseCase).sendOrderCreatedNotification(ids.orderId(), ids.customerId());
    }

    @Test
    void shouldDispatchPaymentCompletedEventToNotificationUseCase() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, paymentCompletedEvent(ids));

        listener.handlePaymentCompletedEvents(payload);

        verify(notificationUseCase).sendPaymentSuccessNotification(ids.orderId(), ids.customerId());
    }

    @Test
    void shouldDispatchPaymentFailedEventWithReasonToNotificationUseCase() throws Exception {
        NotificationIds ids = notificationIds();
        String payload = payload(objectMapper, paymentFailedEvent(ids, REJECTED_BY_BANK));

        listener.handlePaymentFailedEvents(payload);

        verify(notificationUseCase).sendPaymentFailedNotification(ids.orderId(), ids.customerId(), REJECTED_BY_BANK);
    }

    @ParameterizedTest
    @MethodSource("malformedPayloadHandlers")
    void shouldIgnoreMalformedPayload(Consumer<NotificationEventListener> handler) {
        handler.accept(listener);

        verifyNoInteractions(notificationUseCase);
    }

    static Stream<Consumer<NotificationEventListener>> malformedPayloadHandlers() {
        return Stream.of(
                listener -> listener.handleOrderEvents(malformedJson()),
                listener -> listener.handlePaymentCompletedEvents(malformedJson()),
                listener -> listener.handlePaymentFailedEvents(malformedJson())
        );
    }
}
