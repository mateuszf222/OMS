package org.example.paymentservice.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.application.service.PaymentWebhookService;
import org.example.paymentservice.infrastructure.config.PayUProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PayUWebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final PayUProperties payUProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("OpenPayu-Signature") String signatureHeader,
            @RequestBody String rawBody) {

        log.info("Odebrano webhook z PayU!");

        if (!isSignatureValid(signatureHeader, rawBody)) {
            log.warn("Niewłaściwa sygnatura PayU!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            PayUNotification notification = objectMapper.readValue(rawBody, PayUNotification.class);
            UUID paymentId = UUID.fromString(notification.order().extOrderId());
            PayUStatus externalStatus = PayUStatus.fromString(notification.order().status());

            paymentWebhookService.handleWebhook(paymentId, externalStatus);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Błąd podczas przetwarzania webhooka PayU", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isSignatureValid(String signatureHeader, String rawBody) {
        String expectedSignature = "";
        String[] parts = signatureHeader.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("signature=")) {
                expectedSignature = part.trim().substring("signature=".length());
            }
        }

        String concatenated = rawBody + payUProperties.getSecondKey();
        String calculatedSignature = DigestUtils.md5DigestAsHex(concatenated.getBytes());

        return calculatedSignature.equals(expectedSignature);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PayUNotification(Order order) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Order(String extOrderId, String status) {}
    }
}