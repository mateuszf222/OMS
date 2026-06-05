package org.example.orderservice.application.exception;

import java.util.UUID;

public final class ProductNotAvailableException extends OrderApplicationException {

    private final UUID productId;

    public ProductNotAvailableException(UUID productId) {
        super("Product is not available for ordering.");
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}
