package org.example.orderservice.application.service;

import org.example.orderservice.application.port.in.cancelorder.CancelOrderByAdminCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderDueToPaymentFailureCommand;
import org.example.orderservice.application.port.in.completepayment.CompletePaymentCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.domain.cancellation.AdminCancellationReason;
import org.example.orderservice.domain.cancellation.CustomerCancellationReason;
import org.example.orderservice.domain.cancellation.PaymentFailureCancellationReason;
import org.example.orderservice.domain.model.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.example.orderservice.application.service.CreateOrderCommandBuilder.aCreateOrderCommand;
import static org.example.orderservice.application.service.CreateOrderCommandBuilder.orderItem;

final class OrderCommandTestData {

    static final String REJECTED_BY_BANK = "REJECTED_BY_BANK";
    static final String CANCELLED_BY_CUSTOMER = "CANCELLED_BY_CUSTOMER";
    static final String ADMIN_CANCELLATION_REASON = "ADMIN_CANCELLATION";
    static final UUID STANDARD_PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID SECOND_PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID UNKNOWN_PRODUCT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private OrderCommandTestData() {
    }

    static UUID customerId() {
        return UUID.randomUUID();
    }

    static UUID orderId() {
        return UUID.randomUUID();
    }

    static UUID customerIdOtherThan(UUID customerId) {
        UUID knownOtherCustomerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        if (knownOtherCustomerId.equals(customerId)) {
            return UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        }

        return knownOtherCustomerId;
    }

    static CreateOrderCommand createOrderWithTwoPlnItems(UUID customerId) {
        return aCreateOrderCommand()
                .withCustomerId(customerId)
                .withItems(
                        orderItem(STANDARD_PRODUCT_ID, 2),
                        orderItem(SECOND_PRODUCT_ID, 1)
                )
                .build();
    }

    static CreateOrderCommand createOrderWithMixedCurrencies(UUID customerId) {
        return aCreateOrderCommand()
                .withCustomerId(customerId)
                .withItems(
                        orderItem(STANDARD_PRODUCT_ID, 1),
                        orderItem(SECOND_PRODUCT_ID, 1)
                )
                .build();
    }

    static CreateOrderCommand createOrderWithUnavailableProduct(UUID customerId) {
        return aCreateOrderCommand()
                .withCustomerId(customerId)
                .withItems(orderItem(UNKNOWN_PRODUCT_ID, 1))
                .build();
    }

    static CompletePaymentCommand paymentCompletedFor(UUID orderId) {
        return new CompletePaymentCommand(orderId, UUID.randomUUID());
    }

    static CancelOrderByCustomerCommand customerCancelledOrderFor(UUID orderId, UUID customerId) {
        return new CancelOrderByCustomerCommand(
                orderId,
                customerId,
                new CustomerCancellationReason(CANCELLED_BY_CUSTOMER)
        );
    }

    static CancelOrderByAdminCommand adminCancelledOrderFor(UUID orderId) {
        return new CancelOrderByAdminCommand(
                orderId,
                UUID.randomUUID(),
                new AdminCancellationReason(ADMIN_CANCELLATION_REASON)
        );
    }

    static CancelOrderDueToPaymentFailureCommand paymentRejectedByBankFor(UUID orderId) {
        return new CancelOrderDueToPaymentFailureCommand(
                orderId,
                UUID.randomUUID(),
                new PaymentFailureCancellationReason(REJECTED_BY_BANK)
        );
    }

    static Money money(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}

