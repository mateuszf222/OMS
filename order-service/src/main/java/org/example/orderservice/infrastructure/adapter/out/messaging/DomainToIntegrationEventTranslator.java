package org.example.orderservice.infrastructure.adapter.out.messaging;

import org.example.orderservice.domain.event.DomainEvent;
import org.example.orderservice.domain.event.OrderCreatedDomainEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DomainToIntegrationEventTranslator {

    public Optional<IntegrationEvent> translate(DomainEvent event) {
        return switch (event) {
            case OrderCreatedDomainEvent e -> Optional.of(new OrderCreatedEvent(
                    e.orderId(),
                    e.customerId(),
                    e.totalAmount().amount(),
                    e.totalAmount().currency().getCurrencyCode()
            ));

            // default -> Optional.empty(); // lub throw new UnsupportedOperationException
        };
    }
}