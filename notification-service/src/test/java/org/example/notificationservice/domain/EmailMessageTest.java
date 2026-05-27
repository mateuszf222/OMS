package org.example.notificationservice.domain;

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
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> anEmailMessage()
                        .withRecipient(recipient)
                        .build())
                .withMessageContaining("adresu e-mail");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankSubject(String subject) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> anEmailMessage()
                        .withSubject(subject)
                        .build())
                .withMessageContaining("Temat");
    }
}
