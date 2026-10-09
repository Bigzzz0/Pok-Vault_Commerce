package com.pokevault.modules.web.service;

import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * สร้างบัญชี Guest กลางของร้าน (ถ้ายังไม่มี) ไว้ให้ staff ใช้จองแทนลูกค้าที่สั่งผ่าน Facebook
 * โดยไม่ได้สมัครสมาชิกบนเว็บ — ออเดอร์ต้องผูกกับ user เสมอ จึงใช้บัญชีนี้ร่วมกัน
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestCustomerInitializer implements ApplicationRunner {

    public static final String GUEST_USERNAME = "guest_fb";
    private static final String GUEST_EMAIL = "guest_fb@pokevault.local";
    private static final String GUEST_DISPLAY_NAME = "Facebook Guest";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(GUEST_USERNAME)) {
            return;
        }
        // รหัสผ่านสุ่มและไม่ถูกเก็บไว้ที่ไหน: บัญชีนี้มีไว้ผูกออเดอร์เท่านั้น ล็อกอินไม่ได้
        User guest = User.builder()
                .username(GUEST_USERNAME)
                .email(GUEST_EMAIL)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(UserRole.CUSTOMER)
                .build();
        guest.setUserProfile(UserProfile.builder().user(guest).fullName(GUEST_DISPLAY_NAME).build());

        User saved = userRepository.save(guest);
        log.info("Created shared guest customer [ID: {}]: username={}", saved.getId(), saved.getUsername());
    }
}
