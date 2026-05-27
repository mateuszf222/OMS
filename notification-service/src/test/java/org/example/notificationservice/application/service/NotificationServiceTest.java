package org.example.notificationservice.application.service;

import org.example.notificationservice.domain.EmailMessage;
import org.example.notificationservice.NotificationTestData.NotificationIds;
import org.example.notificationservice.domain.EmailMessageAssert;
import org.example.notificationservice.infrastructure.adapter.out.mail.EmailSenderAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.example.notificationservice.NotificationTestData.PAYMENT_FAILURE_REASON;
import static org.example.notificationservice.NotificationTestData.notificationIds;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailSenderAdapter emailSenderAdapter;

    @InjectMocks
    private NotificationService service;

    @Test
    void shouldSendOrderCreatedNotificationToSyntheticCustomerEmail() {
        NotificationIds ids = notificationIds();

        service.sendOrderCreatedNotification(ids.orderId(), ids.customerId());

        EmailMessage message = capturedMessage();
        EmailMessageAssert.assertThat(message)
                .isAddressedTo(ids.syntheticCustomerEmail())
                .hasSubjectContaining(ids.orderId().toString())
                .hasTextContaining(ids.orderId().toString());
    }

    @Test
    void shouldSendPaymentSuccessNotification() {
        NotificationIds ids = notificationIds();

        service.sendPaymentSuccessNotification(ids.orderId(), ids.customerId());

        EmailMessage message = capturedMessage();
        EmailMessageAssert.assertThat(message)
                .isAddressedTo(ids.syntheticCustomerEmail())
                .hasSubjectContaining(ids.orderId().toString())
                .hasTextContaining(ids.orderId().toString())
                .hasTextContaining("sukcesem");
    }

    @Test
    void shouldSendPaymentFailedNotificationWithExplicitFailureReason() {
        NotificationIds ids = notificationIds();

        service.sendPaymentFailedNotification(ids.orderId(), ids.customerId(), PAYMENT_FAILURE_REASON);

        EmailMessage message = capturedMessage();
        EmailMessageAssert.assertThat(message)
                .isAddressedTo(ids.syntheticCustomerEmail())
                .hasSubjectContaining(ids.orderId().toString())
                .hasTextContaining(ids.orderId().toString())
                .hasTextContaining(PAYMENT_FAILURE_REASON);
    }

    private EmailMessage capturedMessage() {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSenderAdapter).sendEmail(captor.capture());
        return captor.getValue();
    }
}
