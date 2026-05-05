package org.example.orderservice.infrastructure.adapter.in.web.dto.cancelorder;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(
        @NotBlank(message = "Powód anulowania jest wymagany")
        String reason
) {}