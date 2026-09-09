package com.dormitory.controller;

import com.dormitory.entity.Admin;
import com.dormitory.repository.AdminRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private final AdminRepository adminRepository;

    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {

        Admin admin = adminRepository.findByUsername(username);

        if (admin == null) {
            return "redirect:/login?error=User not found";
        }

        if (!admin.getPassword().equals(password)) {
            return "redirect:/login?error=Wrong password";
        }

        session.setAttribute("admin", true);

        return "redirect:/";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username, @RequestParam String password) {

        if (adminRepository.findByUsername(username) != null) {
            return "redirect:/login?error=Username already exists";
        }

        Admin a = new Admin();
        a.setUsername(username);
        a.setPassword(password);
        adminRepository.save(a);

        return "redirect:/login";
    }
}

