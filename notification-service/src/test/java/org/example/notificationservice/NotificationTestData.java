package org.example.notificationservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationservice.domain.EmailMessage;
import org.example.notificationservice.infrastructure.adapter.in.messaging.OrderCreatedEvent;
import org.example.notificationservice.infrastructure.adapter.in.messaging.PaymentCompletedEvent;
import org.example.notificationservice.infrastructure.adapter.in.messaging.PaymentFailedEvent;

import java.math.BigDecimal;
import java.util.UUID;

import static org.example.notificationservice.domain.EmailMessageBuilder.anEmailMessage;

public final class NotificationTestData {

    public static final String CUSTOMER_EMAIL = "customer@dummy-domain.com";
    public static final String PAYMENT_FAILURE_REASON = "LIMIT_EXCEEDED";
    public static final String REJECTED_BY_BANK = "REJECTED_BY_BANK";

    private NotificationTestData() {
    }

    public static NotificationIds notificationIds() {
        return new NotificationIds(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    public static EmailMessage orderCreatedEmail() {
        return anEmailMessage()
                .withRecipient(CUSTOMER_EMAIL)
                .withSubject("Order created")
                .withText("Your order has been created.")
                .build();
    }

    public static EmailMessage paymentCompletedEmail() {
        return anEmailMessage()
                .withRecipient(CUSTOMER_EMAIL)
                .withSubject("Payment completed")
                .withText("Payment completed successfully.")
                .build();
    }

    public static EmailMessage paymentFailedEmail() {
        return anEmailMessage()
                .withRecipient(CUSTOMER_EMAIL)
                .withSubject("Payment failed")
                .withText("Payment failed.")
                .build();
    }

    public static OrderCreatedEvent orderCreatedEvent(NotificationIds ids) {
        return new OrderCreatedEvent(ids.orderId(), ids.customerId(), new BigDecimal("100.00"), "PLN");
    }

    public static PaymentCompletedEvent paymentCompletedEvent(NotificationIds ids) {
        return new PaymentCompletedEvent(ids.orderId(), ids.paymentId(), ids.customerId());
    }

    public static PaymentFailedEvent paymentFailedEvent(NotificationIds ids, String reason) {
        return new PaymentFailedEvent(ids.orderId(), ids.paymentId(), ids.customerId(), reason);
    }

    public static String payload(ObjectMapper objectMapper, Object event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public static String malformedJson() {
        return "{malformed-json";
    }

    public record NotificationIds(UUID orderId, UUID paymentId, UUID customerId) {
        public String syntheticCustomerEmail() {
            return customerId + "@dummy-domain.com";
        }
    }
}
