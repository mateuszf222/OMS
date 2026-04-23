package org.example.orderservice.domain.exception;

public class InvalidCurrencyException extends OrderDomainException {

    public InvalidCurrencyException(String currencyCode) {
        super(String.format("Nieprawidłowy kod waluty: '%s'. Oczekiwano 3-literowego kodu w standardzie ISO 4217 (np. PLN, EUR).", currencyCode));
    }
}