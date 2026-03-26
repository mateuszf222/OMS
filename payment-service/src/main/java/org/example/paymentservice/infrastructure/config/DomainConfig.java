package org.example.paymentservice.infrastructure.config;

import org.example.paymentservice.application.port.out.PaymentEventPublisher;
import org.example.paymentservice.application.port.out.PaymentRepository;
import org.example.paymentservice.application.service.PaymentCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DomainConfig {

    @Bean
    @Transactional
    public PaymentCommandService paymentCommandService(
            PaymentRepository paymentRepository,
            PaymentEventPublisher eventPublisher) {

        return new PaymentCommandService(paymentRepository, eventPublisher);
    }
}