package org.example.paymentservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic paymentCompletedEventsTopic() {
        return TopicBuilder.name("payment-completed-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedEventsTopic() {
        return TopicBuilder.name("payment-failed-events").partitions(3).replicas(1).build();
    }
}