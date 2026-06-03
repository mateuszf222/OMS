package org.example.paymentservice.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.application.payment.port.in.ApplyGatewayPaymentStatusUseCase;
import org.example.paymentservice.application.payment.port.in.GatewayPaymentStatus;
import org.example.paymentservice.infrastructure.config.payu.PayUProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.DigestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PayUWebhookControllerTest {

    private static final String SECOND_KEY = "secret-second-key";

    private ApplyGatewayPaymentStatusUseCase applyGatewayPaymentStatusUseCase;
    private PayUWebhookController controller;

    @BeforeEach
    void setUp() {
        applyGatewayPaymentStatusUseCase = mock(ApplyGatewayPaymentStatusUseCase.class);
        PayUProperties properties = new PayUProperties();
        properties.setSecondKey(SECOND_KEY);
        controller = new PayUWebhookController(applyGatewayPaymentStatusUseCase, properties, new ObjectMapper());
    }

    @Test
    void shouldRejectWebhookWithInvalidSignature() {
        String payload = payuPayload(UUID.randomUUID(), "COMPLETED");

        var response = controller.onPayUWebhook("signature=invalid", payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(applyGatewayPaymentStatusUseCase);
    }

    @Test
    void shouldRejectMalformedWebhookJson() {
        String payload = "{malformed-json";

        var response = controller.onPayUWebhook(signatureFor(payload), payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(applyGatewayPaymentStatusUseCase);
    }

    @Test
    void shouldRejectWebhookWithInvalidPaymentId() {
        String payload = """
                {"order":{"extOrderId":"not-a-uuid","status":"COMPLETED"}}
                """;

        var response = controller.onPayUWebhook(signatureFor(payload), payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(applyGatewayPaymentStatusUseCase);
    }

    @Test
    void shouldApplyCompletedWebhookStatus() {
        UUID paymentId = UUID.randomUUID();
        String payload = payuPayload(paymentId, "COMPLETED");

        var response = controller.onPayUWebhook(signatureFor(payload), payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(applyGatewayPaymentStatusUseCase).applyGatewayPaymentStatus(paymentId, GatewayPaymentStatus.SUCCESS);
    }

    @Test
    void shouldApplyUnknownWebhookStatusWithoutRejectingTransport() {
        UUID paymentId = UUID.randomUUID();
        String payload = payuPayload(paymentId, "UNEXPECTED_STATUS");

        var response = controller.onPayUWebhook(signatureFor(payload), payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(applyGatewayPaymentStatusUseCase).applyGatewayPaymentStatus(paymentId, GatewayPaymentStatus.UNKNOWN);
    }

    private static String payuPayload(UUID paymentId, String status) {
        return """
                {"order":{"extOrderId":"%s","status":"%s"}}
                """.formatted(paymentId, status);
    }

    private static String signatureFor(String payload) {
        return "signature=" + DigestUtils.md5DigestAsHex((payload + SECOND_KEY).getBytes());
    }
}
