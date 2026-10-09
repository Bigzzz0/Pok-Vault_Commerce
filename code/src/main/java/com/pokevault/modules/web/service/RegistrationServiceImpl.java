package com.pokevault.modules.web.service;

import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.modules.catalog.dto.UserProfileResponse;
import com.pokevault.modules.web.dto.RegisterRequest;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserProfileResponse registerCustomer(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("ชื่อผู้ใช้นี้ถูกใช้แล้ว: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("อีเมลนี้ถูกใช้สมัครแล้ว: " + email);
        }

        // สมัครผ่านหน้าเว็บได้เฉพาะ CUSTOMER เท่านั้น ไม่รับ role จาก request
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFacebookName().trim())
                .phoneNumber(request.getPhoneNumber())
                .build();
        user.setUserProfile(profile);

        User saved = userRepository.save(user);
        log.info("Registered new customer [ID: {}]: username={}", saved.getId(), saved.getUsername());

        return UserProfileResponse.fromUser(saved);
    }
}
