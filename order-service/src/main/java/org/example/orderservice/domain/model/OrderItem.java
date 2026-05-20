package org.example.orderservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.example.orderservice.domain.exception.InvalidOrderItemQuantityException;
import org.example.orderservice.domain.exception.MissingOrderItemDataException;

import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public class OrderItem {
    private final UUID id;
    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(UUID id, UUID productId, int quantity, Money unitPrice) {
        if (id == null || productId == null || unitPrice == null) {
            throw new MissingOrderItemDataException();
        }
        if (quantity <= 0) {
            throw new InvalidOrderItemQuantityException(quantity);
        }
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }
}
