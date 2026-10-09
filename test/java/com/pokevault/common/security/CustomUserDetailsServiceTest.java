package com.pokevault.common.security;

import com.pokevault.domain.entity.User;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ CustomUserDetailsService และ BCryptPasswordEncoder
 * ผู้รับผิดชอบ: สมาชิกคนที่ 1 (Core Foundation & Card Catalog Lead)
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername: เมื่อพบผู้ใช้ ควรแปลงเป็น UserDetails พร้อม GrantedAuthority ROLE_ ถูกต้อง")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$7Z8V8aQ01LzC3X6B0F9LPe.rQ2.TjZ0wPzU5R7A4e8I3Q8P4l3DGe")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo(adminUser.getPassword());
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: เมื่อไม่พบผู้ใช้ ควรโยน UsernameNotFoundException")
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    @Test
    @DisplayName("BCryptPasswordEncoder: สามารถเข้ารหัสและตรวจสอบรหัสผ่าน password123 ได้ถูกต้อง")
    void passwordEncoder_MatchesRawPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encoded = "$2a$10$Sc5AiVMRiIt6i129ZqA3WOHDwxGpqrxdXpAAgRCItPVVnFGOVVhEa";

        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }
}
