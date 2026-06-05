package org.example.orderservice.infrastructure.adapter.in.web.exception;

import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.application.exception.ProductNotAvailableException;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.exception.OrderMustContainProductsException;
import org.example.orderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundForMissingOrder() {
        UUID orderId = UUID.randomUUID();

        ResponseEntity<ProblemDetail> response = handler.onOrderNotFound(new OrderNotFoundException(orderId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo("Order Not Found");
                    assertThat(problem.getDetail()).contains(orderId.toString());
                });
    }

    @Test
    void shouldReturnUnprocessableEntityForDomainRuleViolation() {
        ResponseEntity<ProblemDetail> response = handler.onOrderDomainException(new OrderMustContainProductsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(problem -> assertThat(problem.getTitle()).isEqualTo("Domain Rule Violation"));
    }

    @Test
    void shouldReturnConflictForBusinessRefusal() {
        var refusal = new InvalidOrderStateTransitionException(
                OrderStatus.CANCELLED,
                OrderStatus.CONFIRMED,
                "apply successful payment"
        );

        ResponseEntity<ProblemDetail> response = handler.onOrderBusinessRefusal(refusal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo("Business Refusal");
                    assertThat(problem.getDetail()).contains("apply successful payment");
                });
    }

    @Test
    void shouldReturnUnprocessableEntityForUnavailableProduct() {
        ResponseEntity<ProblemDetail> response =
                handler.onProductNotAvailable(new ProductNotAvailableException(UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo("Product Not Available");
                    assertThat(problem.getDetail()).contains("not available");
                });
    }
}
