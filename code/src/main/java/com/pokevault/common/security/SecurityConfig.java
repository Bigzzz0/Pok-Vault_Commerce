package com.pokevault.common.security;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokevault.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
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
    private final ObjectMapper objectMapper;

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
                .requestMatchers(HttpMethod.GET, "/api/v1/cards", "/api/v1/cards/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/cards", "/api/v1/cards/**",
                    "/api/v1/accounts", "/api/v1/accounts/**",
                    "/api/v1/trades/**", "/api/v1/admin/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole("CUSTOMER", "ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/*").hasAnyRole("CUSTOMER", "ADMIN", "STAFF")
                .requestMatchers("/api/**").authenticated()
                // หน้าจัดการเฉพาะ Store Admin และ Staff
                .requestMatchers("/inventory/**", "/accounts/**", "/orders/**").hasAnyRole("ADMIN", "STAFF")
                // นอกเหนือจากนั้นต้องยืนยันตัวตน
                .anyRequest().authenticated()
            )
            // API clients receive JSON 401/403; browser page login redirects remain unchanged.
            .exceptionHandling(errors -> errors
                .defaultAuthenticationEntryPointFor(
                    (request, response, exception) -> writeApiError(request, response, 401,
                        "AUTHENTICATION_REQUIRED", "Please sign in to use this API"),
                    new AntPathRequestMatcher("/api/**"))
                .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                    new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")))
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                        writeApiError(request, response, 403, "ACCESS_DENIED", "You do not have permission to use this API");
                    } else {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                })
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

    private void writeApiError(HttpServletRequest request, HttpServletResponse response,
            int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(status).error(code).message(message)
                .path(request.getRequestURI()).build());
    }
}
