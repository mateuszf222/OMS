package org.example.paymentservice.domain.specification;

import org.example.paymentservice.domain.model.Payment;

import java.math.BigDecimal;

public class MaxAmountSpecification implements Specification<Payment> {

    private static final BigDecimal LIMIT_PLN = new BigDecimal("10000.00");
    private static final BigDecimal LIMIT_EUR = new BigDecimal("2500.00");

    @Override
    public boolean isSatisfiedBy(Payment payment) {
        String currency = payment.getAmount().currency();
        BigDecimal amount = payment.getAmount().amount();

        if ("PLN".equals(currency) && amount.compareTo(LIMIT_PLN) > 0) {
            return false;
        }
        if ("EUR".equals(currency) && amount.compareTo(LIMIT_EUR) > 0) {
            return false;
        }

        return true;
    }

    @Override
    public String getReasonNotSatisfied() {
        return "Kwota płatności przekracza maksymalny dopuszczalny limit dla danej waluty.";
    }
}