package org.example.paymentservice.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
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
    private Groups groups;

    @Getter
    @Setter
    public static class Groups {
        @NotBlank
        private String paymentService;
    }
}