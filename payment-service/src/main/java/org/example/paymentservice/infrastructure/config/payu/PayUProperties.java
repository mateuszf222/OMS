package org.example.paymentservice.infrastructure.config.payu;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payu")
public class PayUProperties {
    private String baseUrl;
    private String posId;
    private String clientId;
    private String clientSecret;
    private String secondKey;
    private String notifyUrl;
}