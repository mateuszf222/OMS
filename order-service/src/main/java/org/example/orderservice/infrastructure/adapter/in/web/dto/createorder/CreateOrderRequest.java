package org.example.orderservice.infrastructure.adapter.in.web.dto.createorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Lista produktów jest wymagana")
        @NotEmpty(message = "Zamówienie musi zawierać co najmniej jeden produkt")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "ID produktu jest wymagane")
            UUID productId,

            @Positive(message = "Ilość musi być większa niż 0")
            int quantity
    ) {}
}
