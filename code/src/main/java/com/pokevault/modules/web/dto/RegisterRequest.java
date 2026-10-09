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

    @NotBlank(message = "กรุณากรอกชื่อผู้ใช้")
    @Size(min = 3, max = 50, message = "ชื่อผู้ใช้ต้องยาว 3-50 ตัวอักษร")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "ชื่อผู้ใช้ใช้ได้เฉพาะตัวอักษรอังกฤษ ตัวเลข และ _")
    private String username;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Size(max = 100, message = "อีเมลต้องยาวไม่เกิน 100 ตัวอักษร")
    private String email;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 8, max = 72, message = "รหัสผ่านต้องยาว 8-72 ตัวอักษร")
    private String password;

    // ชื่อ Facebook ที่ลูกค้าใช้ทักแชทร้าน — เก็บลง user_profiles.full_name
    @NotBlank(message = "กรุณากรอกชื่อ Facebook")
    @Size(max = 100, message = "ชื่อ Facebook ต้องยาวไม่เกิน 100 ตัวอักษร")
    private String facebookName;

    @Size(max = 20, message = "เบอร์โทรต้องยาวไม่เกิน 20 ตัวอักษร")
    private String phoneNumber;
}
