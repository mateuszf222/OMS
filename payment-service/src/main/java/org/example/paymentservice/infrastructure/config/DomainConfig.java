package org.example.paymentservice.infrastructure.config;

import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.specification.MaxAmountSpecification;
import org.example.paymentservice.domain.specification.Specification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public Specification<Payment> maxAmountSpecification() {
        return new MaxAmountSpecification();
    }
}