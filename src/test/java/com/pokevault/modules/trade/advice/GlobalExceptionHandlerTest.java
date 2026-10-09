package com.pokevault.modules.trade.advice;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler: HTTP Status Code & ErrorResponse Mapping Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/orders/1/items/1/trade-status");
        this.request = mockRequest;
    }

    @Test
    @DisplayName("404: NoResourceFoundException เมื่อไม่มีเส้นทาง URL หรือ Static Resource")
    void handleNoResourceFound_Returns404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.PATCH, "api/v1/orders/1/items/1/trade-status");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("No static resource");
    }

    @Test
    @DisplayName("404: ResourceNotFoundException เมื่อหา Order หรือ OrderItem ไม่พบ")
    void handleResourceNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", "id", 999L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Order not found with id");
    }

    @Test
    @DisplayName("400: MethodArgumentTypeMismatchException เมื่อชื่อสถานะใน query parameter ไม่ถูกต้อง")
    void handleTypeMismatch_Returns400() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "INVALID_STATUS", String.class, "status", null, new IllegalArgumentException());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_PARAMETER");
        assertThat(response.getBody().getMessage()).contains("Parameter 'status' has invalid value");
    }

    @Test
    @DisplayName("400: IllegalArgumentException เมื่อส่งสถานะที่ไม่รองรับ เช่น UNASSIGNED หรือ FRIEND_PENDING")
    void handleIllegalArgument_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Unsupported trade status: UNASSIGNED");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("Unsupported trade status: UNASSIGNED");
    }

    @Test
    @DisplayName("400: InsufficientStockException เมื่อสต็อกการ์ดไม่พอ")
    void handleInsufficientStock_Returns400() {
        InsufficientStockException ex = new InsufficientStockException("Insufficient stock for Pikachu");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientStock(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    @DisplayName("409: TradeStateConflictException เมื่อผิดลำดับ / ยังไม่จับคู่ / ออเดอร์ยังไม่พร้อม")
    void handleTradeStateConflict_Returns409() {
        TradeStateConflictException ex = new TradeStateConflictException(
                "Cannot set TRADE_SENT: Order must be in SHIPPING status (current status: PAID)");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStateConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("STATE_CONFLICT");
        assertThat(response.getBody().getMessage()).contains("Order must be in SHIPPING status");
    }

    @Test
    @DisplayName("409: InvalidOrderStateException เมื่อการเปลี่ยนสถานะ State Machine ขัดแย้ง")
    void handleInvalidOrderState_Returns409() {
        InvalidOrderStateException ex = new InvalidOrderStateException("Cannot ship order in current state: PENDING");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStateConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("STATE_CONFLICT");
    }

    @Test
    @DisplayName("403: AccessDeniedException เมื่อผู้ใช้ไม่มีสิทธิ์ (เช่น CUSTOMER)")
    void handleAccessDenied_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("500: Exception ทั่วไปเมื่อเกิดข้อผิดพลาดภายในระบบที่ไม่คาดคิด")
    void handleGeneralException_Returns500() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected database failure");
    }
}
