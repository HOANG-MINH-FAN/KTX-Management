package com.dormitory.exchange.controller;

import com.dormitory.exchange.service.ExchangeItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/exchange")
public class ExchangeItemController {

    private final ExchangeItemService service;

    public ExchangeItemController(ExchangeItemService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.getAvailable());
        return "exchange/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        return service.getById(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    // Lấy số điện thoại chủ tin từ Student.phone nếu cần hiển thị
                    service.getOwnerStudent(item.getOwnerId()).ifPresent(owner -> {
                        model.addAttribute("ownerPhone", owner.getPhone());
                        model.addAttribute("ownerName", owner.getFullName());
                    });
                    return "exchange/detail";
                })
                .orElse("redirect:/exchange");
    }

    /**
     * Chuyển hướng người dùng từ URL cũ /exchange/new sang controller quản lý tin cá nhân.
     */
    @GetMapping("/new")
    public String redirectToNewForm() {
        return "redirect:/exchange/my-items/new";
    }
}