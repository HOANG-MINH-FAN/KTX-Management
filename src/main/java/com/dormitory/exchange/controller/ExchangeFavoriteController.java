package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.exchange.service.ExchangeFavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/exchange/favorites")
public class ExchangeFavoriteController {

    private final ExchangeFavoriteService service;
    private final ExchangeUserHelper userHelper;

    public ExchangeFavoriteController(ExchangeFavoriteService service, ExchangeUserHelper userHelper) {
        this.service = service;
        this.userHelper = userHelper;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        Student student = userHelper.getStudent(auth);
        if (student == null) return "redirect:/";
        model.addAttribute("items", service.getFavoriteItems(student.getId()));
        return "exchange/favorites";
    }

    @PostMapping("/{itemId}/toggle")
    public String toggle(@PathVariable Long itemId, Authentication auth, RedirectAttributes ra,
                         @RequestHeader(value = "Referer", required = false) String referer) {
        Student student = userHelper.getStudent(auth);
        if (student == null) return "redirect:/";
        try {
            boolean saved = service.toggle(student.getId(), itemId);
            ra.addFlashAttribute("success", saved ? "Đã lưu tin." : "Đã bỏ lưu tin.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/exchange/favorites");
    }
}