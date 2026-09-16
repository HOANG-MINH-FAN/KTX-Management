package com.dormitory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================
 * SecurityConfig — Cấu hình Spring Security
 * ============================================================
 * Phân quyền URL:
 *   PUBLIC       → /login, /css/**, /js/**, /images/**
 *   ADMIN only   → /admin/**
 *   STUDENT only → /student/**
 *   ADMIN + STUDENT → /registrations/**, /contracts/**
 *   Còn lại      → phải đăng nhập (bất kỳ role)
 *
 * Form Login:
 *   - URL: POST /login
 *   - Tham số: username, password
 *   - Thành công: redirect đến / (HomeController xử lý redirect tiếp)
 *   - Thất bại: redirect đến /login?error
 *
 * BCrypt:
 *   - Tất cả password lưu DB đều hash BCrypt (cost=10)
 *   - Không bao giờ lưu plain text
 * ============================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Bean BCryptPasswordEncoder — dùng khắp nơi để hash/verify password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Authentication Provider dùng CustomUserDetailsService + BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Security 7.x: constructor nhận UserDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Chuỗi bộ lọc bảo mật — định nghĩa phân quyền URL và form login.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập công khai
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // Chỉ ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Chỉ STUDENT
                .requestMatchers("/student/**").hasRole("STUDENT")

                // Cả hai role (đăng ký, hợp đồng)
                .requestMatchers("/registrations/**", "/contracts/**").authenticated()

                // Các URL còn lại → cần đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")                // Trang login tùy chỉnh
                .loginProcessingUrl("/login")       // URL xử lý form POST
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)       // Sau login thành công
                .failureUrl("/login?error=true")    // Sau login thất bại
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Cho phép iframe trong cùng origin (nếu dùng)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
