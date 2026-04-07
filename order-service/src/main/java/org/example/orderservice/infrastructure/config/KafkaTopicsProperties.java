package org.example.orderservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka.topics")
public class KafkaTopicsProperties {
    private String orderEvents;
    private String paymentCompletedEvents;
    private String paymentFailedEvents;
    private String orderService;
}