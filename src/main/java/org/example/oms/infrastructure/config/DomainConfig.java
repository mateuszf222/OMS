package org.example.oms.infrastructure.config;

import org.example.oms.application.port.out.OrderEventPublisher;
import org.example.oms.application.port.out.OrderRepository;
import org.example.oms.application.service.OrderCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DomainConfig {

    @Bean
    @Transactional
    public OrderCommandService orderCommandService(
            OrderRepository orderRepository,
            OrderEventPublisher eventPublisher) {

        return new OrderCommandService(orderRepository, eventPublisher);
    }
}