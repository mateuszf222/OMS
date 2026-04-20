package org.example.paymentservice.infrastructure.adapter.out.payu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.infrastructure.config.PayUProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayUClient {

    private final RestClient payURestClient;
    private final PayUProperties properties;

    public String createPayment(String orderId, String paymentId, int amountInCents, String currencyCode, String customerIp) {

        PayUOrderRequest request = new PayUOrderRequest(
                properties.getNotifyUrl(),
                customerIp,
                properties.getPosId(),
                "Zamówienie " + orderId,
                currencyCode,
                String.valueOf(amountInCents),
                paymentId,
                List.of(new PayUOrderRequest.Product("Zamówienie", String.valueOf(amountInCents), "1"))
        );

        PayUOrderResponse response = payURestClient.post()
                .uri("/api/v2_1/orders")
                .headers(headers -> {
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    headers.add("User-Agent", "Mozilla/5.0");
                    headers.add("Accept-Language", "pl-PL");
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PayUOrderResponse.class);

        log.info("Klient HTTP - Zlecono płatność w PayU. Link do opłacenia: {}", response.redirectUri());
        return response.redirectUri();
    }

    record PayUOrderRequest(
            String notifyUrl,
            String customerIp,
            String merchantPosId,
            String description,
            String currencyCode,
            String totalAmount,
            String extOrderId,
            List<Product> products
    ) {
        record Product(String name, String unitPrice, String quantity) {}
    }

    record PayUOrderResponse(String redirectUri, String orderId, Status status) {
        record Status(String statusCode) {}
    }
}