package org.example.orderservice.infrastructure.adapter.in.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerCommand;
import org.example.orderservice.application.port.in.cancelorder.CancelOrderByCustomerUseCase;
import org.example.orderservice.application.port.in.createorder.CreateOrderCommand;
import org.example.orderservice.application.port.in.createorder.CreateOrderUseCase;
import org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder.CancelOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder.CancelOrderResponse;
import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderRequest;
import org.example.orderservice.infrastructure.adapter.in.web.dto.createorder.CreateOrderResponse;
import org.example.orderservice.infrastructure.adapter.in.web.mapper.OrderRequestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final CancelOrderByCustomerUseCase cancelOrderByCustomerUseCase;
    private final OrderRequestMapper orderRequestMapper;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = customerIdFrom(jwt);
        CreateOrderCommand command = orderRequestMapper.toCreateOrderCommand(customerId, request);

        UUID orderId = createOrderUseCase.createOrder(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId)
                .toUri();

        CreateOrderResponse response = new CreateOrderResponse(orderId);

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID customerId = customerIdFrom(jwt);
        CancelOrderByCustomerCommand command = orderRequestMapper.toCancelOrderByCustomerCommand(orderId, customerId, request);

        cancelOrderByCustomerUseCase.cancelOrderByCustomer(command);

        CancelOrderResponse response = new CancelOrderResponse(
                orderId,
                "CANCELLED",
                "Zamówienie zostało anulowane."
        );

        return ResponseEntity.ok(response);
    }

    private UUID customerIdFrom(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
