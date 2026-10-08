package com.pokevault.modules.web.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.modules.catalog.dto.UserProfileResponse;
import com.pokevault.modules.web.dto.RegisterRequest;
import com.pokevault.modules.web.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Controller สำหรับสมัครสมาชิกลูกค้าใหม่ (Sign Up) จากหน้า login
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Endpoints for customer self-registration")
public class AuthApiController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    @Operation(summary = "Register new customer", description = "Create a new CUSTOMER user with a BCrypt-hashed password and a REGULAR profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = registrationService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer account created successfully", response));
    }

    // username / email ซ้ำ ตอบ 409 แทน 500 ของ handler กลาง
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }
}
