package org.example.orderservice.infrastructure.adapter.in.web.dto.createorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Lista produktów nie może być null")
        @NotEmpty(message = "Zamówienie musi zawierać co najmniej jeden produkt")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "ID produktu jest wymagane")
            UUID productId,

            @Positive(message = "Ilość musi być większa niż 0")
            int quantity,

            @NotNull(message = "Cena jest wymagana")
            @DecimalMin(value = "0.01", message = "Cena musi być większa niż 0")
            BigDecimal price,

            @NotNull(message = "Waluta jest wymagana")
            String currency
    ) {}
}