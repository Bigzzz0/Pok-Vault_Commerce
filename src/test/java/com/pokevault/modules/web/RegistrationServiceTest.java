package com.pokevault.modules.web;

import com.pokevault.domain.entity.User;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.modules.catalog.dto.UserProfileResponse;
import com.pokevault.modules.web.dto.RegisterRequest;
import com.pokevault.modules.web.service.RegistrationServiceImpl;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ RegistrationServiceImpl (สมัครสมาชิกลูกค้าใหม่)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationServiceImpl(userRepository, passwordEncoder);
    }

    private RegisterRequest request() {
        return RegisterRequest.builder()
                .username("new_trainer")
                .email("New.Trainer@Kanto.com")
                .password("secret1234")
                .facebookName("New Trainer FB")
                .phoneNumber("080-000-0000")
                .build();
    }

    @Test
    @DisplayName("registerCustomer: ข้อมูลถูกต้อง ควรบันทึกเป็น CUSTOMER พร้อมรหัสผ่าน BCrypt และโปรไฟล์ REGULAR")
    void registerCustomer_WhenValid_ShouldSaveCustomerWithHashedPassword() {
        when(userRepository.existsByUsername("new_trainer")).thenReturn(false);
        when(userRepository.existsByEmail("new.trainer@kanto.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = registrationService.registerCustomer(request());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(saved.getEmail()).isEqualTo("new.trainer@kanto.com");
        assertThat(saved.getPassword()).isNotEqualTo("secret1234");
        assertThat(passwordEncoder.matches("secret1234", saved.getPassword())).isTrue();
        assertThat(saved.getUserProfile().getUser()).isSameAs(saved);
        assertThat(saved.getUserProfile().getMembershipTier()).isEqualTo(MembershipTier.REGULAR);

        assertThat(response.getUsername()).isEqualTo("new_trainer");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getFullName()).isEqualTo("New Trainer FB");
    }

    @Test
    @DisplayName("registerCustomer: username ซ้ำ ควรโยน IllegalArgumentException และไม่บันทึก")
    void registerCustomer_WhenUsernameTaken_ShouldThrow() {
        when(userRepository.existsByUsername("new_trainer")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerCustomer(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerCustomer: email ซ้ำ ควรโยน IllegalArgumentException และไม่บันทึก")
    void registerCustomer_WhenEmailRegistered_ShouldThrow() {
        when(userRepository.existsByUsername("new_trainer")).thenReturn(false);
        when(userRepository.existsByEmail("new.trainer@kanto.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerCustomer(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }
}
