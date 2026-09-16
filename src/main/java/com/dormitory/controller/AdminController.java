package com.dormitory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LoginController — Chỉ xử lý trang login.
 * Toàn bộ xác thực do Spring Security đảm nhận (POST /login).
 *
 * AdminController cũ đã bị thay thế bởi SecurityConfig +
 * CustomUserDetailsService (đúng chuẩn Spring Security).
 */
@Controller
public class AdminController {

    /**
     * Trang đăng nhập (GET /login).
     * Spring Security tự xử lý POST /login (không cần @PostMapping ở đây).
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
