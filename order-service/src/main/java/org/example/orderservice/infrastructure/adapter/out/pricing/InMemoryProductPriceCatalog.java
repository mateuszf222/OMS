package org.example.orderservice.infrastructure.adapter.out.pricing;

import org.example.orderservice.application.exception.ProductNotAvailableException;
import org.example.orderservice.application.port.out.ProductPriceCatalog;
import org.example.orderservice.domain.model.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

@Component
public class InMemoryProductPriceCatalog implements ProductPriceCatalog {

    public static final UUID STANDARD_PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SECOND_PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID PREMIUM_PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final Currency PLN = Currency.getInstance("PLN");

    private final Map<UUID, Money> prices = Map.of(
            STANDARD_PRODUCT_ID, new Money(new BigDecimal("150.00"), PLN),
            SECOND_PRODUCT_ID, new Money(new BigDecimal("25.00"), PLN),
            PREMIUM_PRODUCT_ID, new Money(new BigDecimal("50.00"), PLN)
    );

    @Override
    public Money priceFor(UUID productId) {
        Money price = prices.get(productId);

        if (price == null) {
            throw new ProductNotAvailableException(productId);
        }

        return price;
    }
}
