package org.example.notificationservice.application.exception;

public final class MissingNotificationDataException extends NotificationApplicationException {

    private final String fieldName;

    public MissingNotificationDataException(String fieldName) {
        super("Notification " + fieldName + " cannot be null or blank.");
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
