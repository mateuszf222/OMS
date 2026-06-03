package org.example.notificationservice.domain;

import org.example.notificationservice.domain.exception.InvalidEmailRecipientException;
import org.example.notificationservice.domain.exception.MissingEmailBodyException;
import org.example.notificationservice.domain.exception.MissingEmailSubjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.notificationservice.NotificationTestData.CUSTOMER_EMAIL;
import static org.example.notificationservice.NotificationTestData.orderCreatedEmail;
import static org.example.notificationservice.domain.EmailMessageBuilder.anEmailMessage;

class EmailMessageTest {

    @Test
    void shouldCreateValidEmailMessage() {
        EmailMessage message = orderCreatedEmail();

        EmailMessageAssert.assertThat(message)
                .isAddressedTo(CUSTOMER_EMAIL)
                .hasSubject("Order created")
                .hasTextContaining("created");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"invalid", ""})
    void shouldRejectInvalidRecipientAddress(String recipient) {
        assertThatExceptionOfType(InvalidEmailRecipientException.class)
                .isThrownBy(() -> anEmailMessage()
                        .withRecipient(recipient)
                        .build())
                .withMessageContaining("recipient");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankSubject(String subject) {
        assertThatExceptionOfType(MissingEmailSubjectException.class)
                .isThrownBy(() -> anEmailMessage()
                        .withSubject(subject)
                        .build())
                .withMessageContaining("subject");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankBody(String text) {
        assertThatExceptionOfType(MissingEmailBodyException.class)
                .isThrownBy(() -> anEmailMessage()
                        .withText(text)
                        .build())
                .withMessageContaining("body");
    }
}
