package com.dormitory.exchange.controller;

import com.dormitory.entity.User;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/exchange")
public class ExchangeItemController {

    private final ExchangeItemService service;
    private final UserRepository userRepository;

    public ExchangeItemController(ExchangeItemService service,
                                  UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
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
                    return "exchange/detail";
                })
                .orElse("redirect:/exchange");
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("item", new ExchangeItem());
        return "exchange/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ExchangeItem item,
                       Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        item.setOwnerId(user.getId());
        item.setStatus("AVAILABLE");

        service.save(item);

        return "redirect:/exchange";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/exchange";
    }
}