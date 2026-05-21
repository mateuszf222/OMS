package org.example.orderservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.orderservice.domain.model.OrderStatus.CANCELLED;
import static org.example.orderservice.domain.model.OrderStatus.CONFIRMED;
import static org.example.orderservice.domain.model.OrderStatus.DELIVERED;
import static org.example.orderservice.domain.model.OrderStatus.PENDING;
import static org.example.orderservice.domain.model.OrderStatus.RETURNED;
import static org.example.orderservice.domain.model.OrderStatus.SHIPPED;

class OrderStateTransitionsTest {

    @Test
    @DisplayName("Powinien pozwalac na potwierdzenie platnosci tylko dla PENDING")
    void shouldAllowConfirmingOnlyPendingOrder() {
        assertThat(PENDING.canTransitionTo(CONFIRMED)).isTrue();
        assertThat(CONFIRMED.canTransitionTo(CONFIRMED)).isFalse();
        assertThat(CANCELLED.canTransitionTo(CONFIRMED)).isFalse();
    }

    @Test
    @DisplayName("Powinien pozwalac na anulowanie tylko dla PENDING i CONFIRMED")
    void shouldAllowCancellingPendingAndConfirmedOrder() {
        assertThat(PENDING.canTransitionTo(CANCELLED)).isTrue();
        assertThat(CONFIRMED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(CANCELLED.canTransitionTo(CANCELLED)).isFalse();
        assertThat(DELIVERED.canTransitionTo(CANCELLED)).isFalse();
    }

    @Test
    @DisplayName("Powinien oznaczac statusy bez przejsc jako finalne")
    void shouldMarkStatusesWithoutAllowedTransitionsAsFinal() {
        assertThat(PENDING.isFinal()).isFalse();
        assertThat(CONFIRMED.isFinal()).isFalse();
        assertThat(SHIPPED.isFinal()).isFalse();
        assertThat(DELIVERED.isFinal()).isTrue();
        assertThat(CANCELLED.isFinal()).isTrue();
        assertThat(RETURNED.isFinal()).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} powinno byc {2}")
    @MethodSource("transitionMatrix")
    void shouldAllowTransition(OrderStatus from, OrderStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to))
                .as("Przejscie %s -> %s", from, to)
                .isEqualTo(expected);
    }

    static Stream<Arguments> transitionMatrix() {
        return Stream.of(
                Arguments.of(PENDING, CONFIRMED, true),
                Arguments.of(PENDING, CANCELLED, true),
                Arguments.of(PENDING, SHIPPED, false),
                Arguments.of(PENDING, DELIVERED, false),
                Arguments.of(PENDING, RETURNED, false),

                Arguments.of(CONFIRMED, SHIPPED, true),
                Arguments.of(CONFIRMED, CANCELLED, true),
                Arguments.of(CONFIRMED, CONFIRMED, false),
                Arguments.of(CONFIRMED, DELIVERED, false),
                Arguments.of(CONFIRMED, RETURNED, false),

                Arguments.of(SHIPPED, DELIVERED, true),
                Arguments.of(SHIPPED, CANCELLED, false),
                Arguments.of(SHIPPED, CONFIRMED, false),
                Arguments.of(SHIPPED, RETURNED, false),

                Arguments.of(CANCELLED, CONFIRMED, false),
                Arguments.of(CANCELLED, CANCELLED, false),
                Arguments.of(CANCELLED, SHIPPED, false),

                Arguments.of(DELIVERED, DELIVERED, false),
                Arguments.of(DELIVERED, RETURNED, false),
                Arguments.of(DELIVERED, CANCELLED, false),

                Arguments.of(RETURNED, PENDING, false),
                Arguments.of(RETURNED, CONFIRMED, false),
                Arguments.of(RETURNED, RETURNED, false)
        );
    }
}
