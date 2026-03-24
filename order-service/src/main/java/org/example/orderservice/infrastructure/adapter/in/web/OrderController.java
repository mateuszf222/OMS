package org.example.orderservice.infrastructure.adapter.in.web;

import org.example.orderservice.application.port.in.CreateOrderCommand;
import org.example.orderservice.application.port.in.CreateOrderUseCase;
import org.springframework.http.ResponseEntity;
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
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = mapToCommand(request);
        UUID orderId = createOrderUseCase.createOrder(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    private CreateOrderCommand mapToCommand(CreateOrderRequest request) {
        var commandItems = request.items().stream()
                .map(item -> new CreateOrderCommand.OrderItemCommand(
                        item.productId(),
                        item.quantity(),
                        item.price(),
                        Currency.getInstance(item.currency())
                ))
                .toList();

        return new CreateOrderCommand(request.customerId(), commandItems);
    }
}