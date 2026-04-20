package org.example.paymentservice.infrastructure.config;

import org.example.paymentservice.infrastructure.adapter.out.payu.PayUTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class PayUConfig {

    @Bean
    public RestClient payURestClient(PayUProperties properties, PayUTokenProvider tokenProvider) {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenProvider.getAccessToken());
                    return execution.execute(request, body);
                })
                .build();
    }
}