package org.example.orderservice.infrastructure.config;

import org.example.orderservice.application.port.out.OrderEventPublisher;
import org.example.orderservice.application.port.out.OrderRepository;
import org.example.orderservice.application.service.OrderCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DomainConfig {

    @Bean
    @Transactional
    public OrderCommandService orderCommandService(
            OrderRepository orderRepository) {

        return new OrderCommandService(orderRepository);
    }
}