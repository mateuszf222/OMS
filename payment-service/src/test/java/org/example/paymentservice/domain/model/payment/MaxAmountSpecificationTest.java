package org.example.paymentservice.domain.model.payment;

import org.example.paymentservice.domain.exception.PaymentAmountLimitExceededException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.example.paymentservice.domain.model.PaymentTestData.paymentWithAmount;

class MaxAmountSpecificationTest {

    private final MaxAmountSpecification specification = new MaxAmountSpecification();

    @ParameterizedTest(name = "{1} {0} should satisfy limit: {2}")
    @CsvSource({
            "9999.99, PLN, true",
            "10000.00, PLN, true",
            "10000.01, PLN, false",
            "2499.99, EUR, true",
            "2500.00, EUR, true",
            "2500.01, EUR, false"
    })
    void shouldValidateBoundaryAmountsForSupportedCurrencies(String amount, String currency, boolean expected) {
        Payment payment = paymentWithAmount(amount, currency);

        boolean satisfied = specification.isSatisfiedBy(payment);

        assertThat(satisfied).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{1} {0} should be rejected")
    @CsvSource({
            "10000.01, PLN",
            "2500.01, EUR"
    })
    void shouldRejectPaymentsAboveCurrencyLimit(String amount, String currency) {
        Payment payment = paymentWithAmount(amount, currency);

        assertThatExceptionOfType(PaymentAmountLimitExceededException.class)
                .isThrownBy(() -> payment.ensureAllowedBy(specification))
                .withMessageContaining("maksymalny dopuszczalny limit");
    }
}
