package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ExchangeUserHelper {

    private final UserRepository userRepository;

    public ExchangeUserHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Trả về sinh viên đang đăng nhập, hoặc null nếu là admin / chưa đăng nhập. */
    public Student getStudent(Authentication auth) {
        if (auth == null) return null;
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        return (user instanceof Student) ? (Student) user : null;
    }
}