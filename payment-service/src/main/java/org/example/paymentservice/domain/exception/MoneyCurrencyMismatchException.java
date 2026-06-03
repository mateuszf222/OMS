package org.example.paymentservice.domain.exception;

public final class MoneyCurrencyMismatchException extends PaymentDomainException {

    public MoneyCurrencyMismatchException(String leftCurrency, String rightCurrency) {
        super("Cannot compare money with different currencies: " + leftCurrency + " vs " + rightCurrency);
    }
}
