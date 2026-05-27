package org.example.notificationservice.infrastructure.adapter.out.mail;

import org.example.notificationservice.domain.EmailMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.example.notificationservice.NotificationTestData.paymentCompletedEmail;
import static org.example.notificationservice.NotificationTestData.paymentFailedEmail;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailSenderAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderAdapter adapter;

    @Test
    void shouldMapDomainEmailMessageToSpringMailMessage() {
        EmailMessage email = paymentCompletedEmail();

        adapter.sendEmail(email);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo("sklep@enterprise-oms.com");
        assertThat(sentMessage.getTo()).containsExactly("customer@dummy-domain.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Payment completed");
        assertThat(sentMessage.getText()).isEqualTo("Payment completed successfully.");
    }

    @Test
    void shouldHandleFallbackWithoutThrowingAfterMailRetryFailure() {
        EmailMessage email = paymentFailedEmail();

        assertThatCode(() -> adapter.sendEmailFallback(email, new MailSendException("smtp unavailable")))
                .doesNotThrowAnyException();
    }
}
