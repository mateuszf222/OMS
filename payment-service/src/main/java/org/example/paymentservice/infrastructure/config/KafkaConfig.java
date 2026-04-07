package org.example.paymentservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaTopicsProperties topics;

    @Bean
    public NewTopic paymentCompletedEventsTopic() {
        return TopicBuilder.name(topics.getPaymentCompletedEvents()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedEventsTopic() {
        return TopicBuilder.name(topics.getPaymentFailedEvents()).partitions(3).replicas(1).build();
    }
}