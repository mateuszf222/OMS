package domain.model;

import domain.exception.OrderDomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
            throw new OrderDomainException("ID, ProductID oraz UnitPrice nie mogą być null.");
        }
        if (quantity <= 0) {
            throw new OrderDomainException("Ilość produktów musi być większa niż 0.");
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