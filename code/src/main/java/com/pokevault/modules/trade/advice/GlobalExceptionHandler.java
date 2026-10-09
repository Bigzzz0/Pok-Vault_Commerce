package com.pokevault.modules.trade.advice;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. ดักจับเมื่อไม่พบ URL, ออเดอร์ หรือรายการสินค้า -> 404 NOT_FOUND
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("Resource/URL not found (404): {} at path {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), null);
    }

    // 2. ดักจับข้อขัดแย้งเชิงสถานะ (ผิดลำดับ, ยังไม่จับคู่, ออเดอร์ยังไม่พร้อม) -> 409 CONFLICT
    @ExceptionHandler({TradeStateConflictException.class, InvalidOrderStateException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleStateConflict(RuntimeException ex, HttpServletRequest request) {
        log.warn("State conflict error (409): {} at path {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, "STATE_CONFLICT", ex.getMessage(), request.getRequestURI(), null);
    }

    // 3. ดักจับเมื่อชื่อสถานะไม่ถูกต้อง หรือ Parameter ผิดพลาด -> 400 BAD_REQUEST
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Parameter '%s' has invalid value: '%s'", ex.getName(), ex.getValue());
        log.warn("Method argument type mismatch (400): {} at path {}", message, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request.getRequestURI(), null);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        log.warn("Missing request parameter (400): {} at path {}", message, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request.getRequestURI(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument error (400): {} at path {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request.getRequestURI(), null);
    }

    // 4. ดักจับเมื่อสต็อกการ์ดไม่พอ -> 400 BAD_REQUEST
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        log.warn("Insufficient stock error (400): {} at path {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", ex.getMessage(), request.getRequestURI(), null);
    }

    // 5. ดักจับ Bean Validation (@Valid) -> 400 BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation error (400) at path {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message("Validation error in request payload")
                .path(request.getRequestURI())
                .details(fieldErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 6. ดักจับเมื่อไม่มีสิทธิ์เข้าถึง (CUSTOMER หรือไม่ได้รับอนุญาต) -> 403 FORBIDDEN
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied (403): {} at path {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied: " + ex.getMessage(), request.getRequestURI(), null);
    }

    // 7. ดักจับ Error อื่นๆ ที่ไม่คาดคิด (Internal Server Error) -> 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Internal server error (500) at path: {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage(), request.getRequestURI(), null);
    }

    // Helper method สร้าง ErrorResponse object ผ่าน Builder
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode, String message,
            String path, Map<String, String> details) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(path)
                .details(details)
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}
