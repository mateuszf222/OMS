package org.example.notificationservice.domain;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class EmailMessageAssert extends AbstractAssert<EmailMessageAssert, EmailMessage> {

    private EmailMessageAssert(EmailMessage actual) {
        super(actual, EmailMessageAssert.class);
    }

    public static EmailMessageAssert assertThat(EmailMessage actual) {
        return new EmailMessageAssert(actual);
    }

    public EmailMessageAssert isAddressedTo(String expected) {
        isNotNull();

        Assertions.assertThat(actual.to())
                .as("email recipient")
                .isEqualTo(expected);
        return this;
    }

    public EmailMessageAssert hasSubject(String expected) {
        isNotNull();

        Assertions.assertThat(actual.subject())
                .as("email subject")
                .isEqualTo(expected);
        return this;
    }

    public EmailMessageAssert hasSubjectContaining(String expected) {
        isNotNull();

        Assertions.assertThat(actual.subject())
                .as("email subject")
                .contains(expected);
        return this;
    }

    public EmailMessageAssert hasText(String expected) {
        isNotNull();

        Assertions.assertThat(actual.text())
                .as("email text")
                .isEqualTo(expected);
        return this;
    }

    public EmailMessageAssert hasTextContaining(String expected) {
        isNotNull();

        Assertions.assertThat(actual.text())
                .as("email text")
                .contains(expected);
        return this;
    }
}
