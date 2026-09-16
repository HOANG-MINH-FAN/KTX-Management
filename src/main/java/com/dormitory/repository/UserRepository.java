package com.dormitory.repository;

import com.dormitory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho User (base của Admin và Student).
 * Dùng bởi CustomUserDetailsService để load user khi đăng nhập.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo username (cho Spring Security authentication).
     */
    Optional<User> findByUsername(String username);

    /**
     * Kiểm tra username đã tồn tại chưa.
     */
    boolean existsByUsername(String username);
}
