
package com.dormitory.controller;

import com.dormitory.entity.Notification;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import com.dormitory.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Lấy người dùng đang đăng nhập từ database
    private User getCurrentUser(Authentication authentication) {
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng"));
    }

    // Hiển thị danh sách thông báo
    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Long userId = user.getId();

        model.addAttribute(
                "notifications",
                notificationService.getNotifications(userId));

        model.addAttribute(
                "unreadCount",
                notificationService.countUnread(userId));

        return "notifications/list";
    }

    // Đánh dấu thông báo đã đọc
    @PostMapping("/{id}/read")
    public String markRead(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        notificationService.markAsRead(id, user.getId());

        return "redirect:/notifications";
    }
}