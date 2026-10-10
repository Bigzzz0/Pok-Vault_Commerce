package com.pokevault.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 4 Custom Exceptions Unit Tests")
class CustomExceptionTest {

    @Test
    @DisplayName("TradeStateConflictException should correctly store message and cause")
    void testTradeStateConflictException() {
        TradeStateConflictException exWithMessage = new TradeStateConflictException("Order must be in SHIPPING status");
        assertThat(exWithMessage.getMessage()).isEqualTo("Order must be in SHIPPING status");
        assertThat(exWithMessage.getCause()).isNull();

        Throwable cause = new IllegalStateException("Underlying conflict");
        TradeStateConflictException exWithCause = new TradeStateConflictException("State conflict occurred", cause);
        assertThat(exWithCause.getMessage()).isEqualTo("State conflict occurred");
        assertThat(exWithCause.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("InvalidOrderStateException should correctly store message and cause")
    void testInvalidOrderStateException() {
        InvalidOrderStateException exWithMessage = new InvalidOrderStateException("Cannot cancel while SHIPPING");
        assertThat(exWithMessage.getMessage()).isEqualTo("Cannot cancel while SHIPPING");
        assertThat(exWithMessage.getCause()).isNull();

        Throwable cause = new RuntimeException("Illegal state");
        InvalidOrderStateException exWithCause = new InvalidOrderStateException("Invalid transition", cause);
        assertThat(exWithCause.getMessage()).isEqualTo("Invalid transition");
        assertThat(exWithCause.getCause()).isSameAs(cause);
    }
}
