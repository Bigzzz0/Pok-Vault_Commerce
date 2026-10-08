package com.pokevault.modules.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO สำหรับรับข้อมูลสมัครสมาชิกลูกค้าใหม่จากหน้า Sign Up
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username may contain only letters, digits and underscore")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

    // ชื่อ Facebook ที่ลูกค้าใช้ทักแชทร้าน — เก็บลง user_profiles.full_name
    @NotBlank(message = "Facebook name is required")
    @Size(max = 100, message = "Facebook name must not exceed 100 characters")
    private String facebookName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
}
