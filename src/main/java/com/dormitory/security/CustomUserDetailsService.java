package com.dormitory.security;

import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 * CustomUserDetailsService
 * ============================================================
 * Cầu nối giữa Spring Security và database.
 *
 * Spring Security gọi loadUserByUsername() khi đăng nhập:
 * 1. Tìm User trong DB theo username
 * 2. Trả về UserDetails (gồm username, password, authorities)
 * 3. Spring Security so sánh password bằng BCryptPasswordEncoder
 * 4. Nếu khớp → đăng nhập thành công, tạo SecurityContext
 *
 * Role → Authority mapping:
 *   Role.ADMIN   → "ROLE_ADMIN"
 *   Role.STUDENT → "ROLE_STUDENT"
 * ============================================================
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong DB (có thể là Admin hoặc Student — nhờ SINGLE_TABLE)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        // Tạo authority từ role (Spring Security yêu cầu tiền tố "ROLE_")
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Trả về UserDetails để Spring Security dùng để xác thực
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),   // BCrypt hash — Spring Security tự decode
                user.isEnabled(),
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                List.of(authority)
        );
    }
}
