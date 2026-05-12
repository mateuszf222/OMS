package org.example.paymentservice.infrastructure.adapter.out.payu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.port.out.PaymentGatewayOptions;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.infrastructure.config.payu.PayUProperties;
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

    public String createPayment(Payment payment, PaymentGatewayOptions options) {

        String amountStr = String.valueOf(payment.getAmount().toCents());
        String paymentIdStr = payment.getId().toString();

        PayUOrderRequest request = new PayUOrderRequest(
                properties.getNotifyUrl(),
                options.customerIp(),
                properties.getPosId(),
                "Zamówienie " + payment.getOrderId(),
                payment.getAmount().currency(),
                amountStr,
                paymentIdStr,
                List.of(new PayUOrderRequest.Product("Zamówienie", amountStr, "1"))
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