package org.example.orderservice.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.in.CreateOrderCommand;
import org.example.orderservice.application.port.in.CreateOrderUseCase;
import org.example.orderservice.domain.exception.InvalidCurrencyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<Void> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = UUID.fromString(jwt.getSubject());
        CreateOrderCommand command = mapToCommand(customerId, request);
        UUID orderId = createOrderUseCase.createOrder(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    private CreateOrderCommand mapToCommand(UUID customerId, CreateOrderRequest request) {
        var commandItems = request.items().stream()
                .map(item -> {
                    Currency currency;
                    try {
                        currency = Currency.getInstance(item.currency());
                    } catch (IllegalArgumentException e) {
                        throw new InvalidCurrencyException(item.currency());
                    }

                    return new CreateOrderCommand.OrderItemCommand(
                            item.productId(),
                            item.quantity(),
                            item.price(),
                            currency
                    );
                })
                .toList();

        return new CreateOrderCommand(customerId, commandItems);
    }
}