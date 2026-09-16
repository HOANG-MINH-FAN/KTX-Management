package com.dormitory.entity;

import com.dormitory.entity.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ============================================================
 * LỚP CƠ SỞ TRỪU TƯỢNG: User
 * ============================================================
 * Áp dụng các nguyên lý OOP:
 *
 * 1. TRỪU TƯỢNG HÓA (Abstraction):
 *    - Lớp abstract, không thể khởi tạo trực tiếp.
 *    - Định nghĩa hợp đồng chung: mọi người dùng đều có
 *      username, password, role và trạng thái enabled.
 *
 * 2. ĐÓNG GÓI (Encapsulation):
 *    - Tất cả trường private, chỉ truy cập qua getter/setter.
 *
 * 3. KẾ THỪA (Inheritance) — SINGLE_TABLE Strategy:
 *    - Toàn bộ hierarchy lưu trong một bảng `users`.
 *    - Cột `role` làm discriminator để JPA biết tạo Admin hay Student.
 *    - Admin  → @DiscriminatorValue("ADMIN")
 *    - Student → @DiscriminatorValue("STUDENT")
 * ============================================================
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên đăng nhập - unique trong toàn hệ thống.
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Mật khẩu đã mã hóa bằng BCrypt.
     * Không bao giờ lưu plain text.
     */
    @Column(name = "password_hash", nullable = false)
    private String password;

    /**
     * Vai trò - đọc từ discriminator column, không ghi đè.
     * insertable=false, updatable=false vì cột này do JPA tự quản lý.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", insertable = false, updatable = false)
    private Role role;

    /**
     * Tài khoản có hoạt động không.
     * false = bị khóa, Spring Security sẽ từ chối đăng nhập.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===================== Constructors =====================

    protected User() {}

    protected User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled  = true;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Kiểm tra nhanh xem user có phải Admin không.
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * Kiểm tra nhanh xem user có phải Student không.
     */
    public boolean isStudent() {
        return Role.STUDENT.equals(this.role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
