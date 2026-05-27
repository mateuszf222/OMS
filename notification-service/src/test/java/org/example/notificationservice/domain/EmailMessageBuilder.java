package org.example.notificationservice.domain;

public class EmailMessageBuilder {

    private String to = "customer@dummy-domain.com";
    private String subject = "Order created";
    private String text = "Your order has been created.";

    public static EmailMessageBuilder anEmailMessage() {
        return new EmailMessageBuilder();
    }

    public EmailMessageBuilder withRecipient(String to) {
        this.to = to;
        return this;
    }

    public EmailMessageBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailMessageBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public EmailMessage build() {
        return new EmailMessage(to, subject, text);
    }
}
