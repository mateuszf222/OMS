package org.example.paymentservice.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.payment.port.in.ApplyGatewayPaymentStatusUseCase;
import org.example.paymentservice.application.payment.port.in.GatewayPaymentStatus;
import org.example.paymentservice.infrastructure.config.payu.PayUProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PayUWebhookController {

    private final ApplyGatewayPaymentStatusUseCase applyGatewayPaymentStatusUseCase;
    private final PayUProperties payUProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<Void> onPayUWebhook(
            @RequestHeader("OpenPayu-Signature") String signatureHeader,
            @RequestBody String rawBody) {

        log.info("Received PayU webhook.");

        if (!isSignatureValid(signatureHeader, rawBody)) {
            log.warn("Invalid PayU signature.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            PayUNotification notification = objectMapper.readValue(rawBody, PayUNotification.class);
            UUID paymentId = UUID.fromString(notification.order().extOrderId());
            PayUStatus payUStatus = PayUStatus.fromString(notification.order().status());
            GatewayPaymentStatus gatewayStatus = gatewayStatusFrom(payUStatus);

            applyGatewayPaymentStatusUseCase.applyGatewayPaymentStatus(paymentId, gatewayStatus);
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            log.error("PayU webhook could not be applied.", exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    private GatewayPaymentStatus gatewayStatusFrom(PayUStatus payUStatus) {
        return switch (payUStatus) {
            case COMPLETED -> GatewayPaymentStatus.SUCCESS;
            case CANCELED, REJECTED -> GatewayPaymentStatus.FAILURE;
            case NEW, PENDING -> GatewayPaymentStatus.PENDING;
            case UNKNOWN -> GatewayPaymentStatus.UNKNOWN;
        };
    }

    private boolean isSignatureValid(String signatureHeader, String rawBody) {
        String expectedSignature = "";
        String[] parts = signatureHeader.split(";");

        for (String part : parts) {
            if (!part.trim().startsWith("signature=")) {
                continue;
            }

            expectedSignature = part.trim().substring("signature=".length());
            break;
        }

        String signaturePayload = rawBody + payUProperties.getSecondKey();
        String actualSignature = DigestUtils.md5DigestAsHex(signaturePayload.getBytes());

        return actualSignature.equals(expectedSignature);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PayUNotification(Order order) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Order(String extOrderId, String status) {
        }
    }
}
