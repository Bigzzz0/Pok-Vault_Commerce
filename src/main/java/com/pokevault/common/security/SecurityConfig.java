package com.pokevault.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * คลาสกำหนดค่าความปลอดภัยสำหรับ Spring Security 6 และ Spring Boot 3.4
 * ผู้รับผิดชอบ: สมาชิกคนที่ 1 (Core Foundation & Card Catalog Lead)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. ยกเว้น CSRF สำหรับ REST APIs, H2 Console และ Actuator
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**", "/actuator/**")
            )
            // 2. ปลดล็อก Frame สำหรับ H2 Console (sameOrigin)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            // 3. กฎการอนุญาตเข้าถึง URL
            .authorizeHttpRequests(auth -> auth
                // ไฟล์ Static ทั่วไป
                .requestMatchers("/css/**", "/js/**", "/images/**", "/audio/**", "/favicon.ico").permitAll()
                // เอกสาร API และ Healthcheck (จำเป็นมากสำหรับ Docker healthcheck & CI/CD)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/**").permitAll()
                // หน้าสาธารณะทั่วไป
                .requestMatchers("/", "/cards", "/login", "/h2-console/**").permitAll()
                // อนุญาตให้เรียก REST APIs ได้โดยตรงเพื่อไม่กระทบ Unit Tests และ Frontend AJAX
                .requestMatchers("/api/**").permitAll()
                // หน้าจัดการเฉพาะ Store Admin และ Staff
                .requestMatchers("/inventory/**", "/accounts/**", "/orders/**").hasAnyRole("ADMIN", "STAFF")
                // นอกเหนือจากนั้นต้องยืนยันตัวตน
                .anyRequest().authenticated()
            )
            // 4. ตั้งค่า Form Login เชื่อมกับ /login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // 5. ตั้งค่า Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
