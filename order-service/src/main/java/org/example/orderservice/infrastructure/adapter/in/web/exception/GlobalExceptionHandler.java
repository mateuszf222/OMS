package org.example.orderservice.infrastructure.adapter.in.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.application.exception.OrderNotFoundException;
import org.example.orderservice.domain.exception.InvalidOrderStateTransitionException;
import org.example.orderservice.domain.exception.OrderDomainException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> onValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                details
        );
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Validation error: {}", details);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> onMalformedRequestBody(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains values with invalid types."
        );
        problem.setTitle("Malformed Request Body");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> onPathOrQueryParameterTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid value '%s' for parameter '%s'.".formatted(ex.getValue(), ex.getName())
        );
        problem.setTitle("Invalid Request Parameter");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Invalid request parameter {}={}", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ProblemDetail> onOrderNotFound(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Order Not Found");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Order not found: {}", ex.getOrderId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidCurrencyCodeException.class)
    public ResponseEntity<ProblemDetail> onInvalidCurrency(InvalidCurrencyCodeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Invalid Currency Code");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Invalid currency requested: {}", ex.getCurrencyCode());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ProblemDetail> onOrderDomainException(OrderDomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Domain Rule Violation");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Domain exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> onOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Please refresh and try again."
        );
        problem.setTitle("Concurrent Modification Conflict");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Optimistic locking failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidOrderStateTransitionException.class)
    public ResponseEntity<ProblemDetail> onOrderBusinessRefusal(InvalidOrderStateTransitionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Business Refusal");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Business refusal: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> onUnexpectedException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support if the problem persists."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());

        log.error("Unhandled exception occurred", ex);
        return ResponseEntity.internalServerError().body(problem);
    }
}
