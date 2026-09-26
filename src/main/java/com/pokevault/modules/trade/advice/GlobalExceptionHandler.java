package com.pokevault.modules.trade.advice;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. ดักจับเมื่อหา Resource ไม่พบ -> 404 NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI(),
                null);
    }

    // 2. ดักจับเมื่อสต็อกการ์ดไม่พอ -> 400 BAD_REQUEST
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", ex.getMessage(), request.getRequestURI(),
                null);
    }

    // 3. ดักจับเมื่อเปลี่ยน State ออเดอร์ผิดกฎ -> 400 BAD_REQUEST
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderState(InvalidOrderStateException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATE", ex.getMessage(), request.getRequestURI(),
                null);
    }

    // 4. ดักจับ Bean Validation (@Valid) -> 400 BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

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

    // 5. ดักจับ Error อื่นๆ ที่ไม่คาดคิด -> 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage(),
                request.getRequestURI(), null);
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
