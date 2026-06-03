package org.example.orderservice.infrastructure.adapter.in.web.exception;

public final class InvalidCurrencyCodeException extends RuntimeException {

    private final String currencyCode;

    public InvalidCurrencyCodeException(String currencyCode) {
        super("Invalid currency code: '%s'. Expected ISO 4217 currency code.".formatted(currencyCode));
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
