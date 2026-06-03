package org.example.notificationservice.domain.exception;

public final class InvalidEmailRecipientException extends NotificationDomainException {

    private final String recipient;

    public InvalidEmailRecipientException(String recipient) {
        super("Invalid email recipient address: " + recipient);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}
