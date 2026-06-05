package org.example.orderservice.application.port.out;

import org.example.orderservice.domain.model.Money;

import java.util.UUID;

public interface ProductPriceCatalog {
    Money priceFor(UUID productId);
}
