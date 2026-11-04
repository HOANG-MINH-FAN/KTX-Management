package com.dormitory.exchange.controller;

import com.dormitory.entity.Student;
import com.dormitory.exchange.service.ExchangeItemService;
import com.dormitory.exchange.service.ExchangeReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/exchange/reports")
public class ExchangeReportController {

    private final ExchangeReportService reportService;
    private final ExchangeItemService itemService;
    private final ExchangeUserHelper userHelper;

    public ExchangeReportController(ExchangeReportService reportService,
                                    ExchangeItemService itemService,
                                    ExchangeUserHelper userHelper) {
        this.reportService = reportService;
        this.itemService = itemService;
        this.userHelper = userHelper;
    }

    @GetMapping("/new")
    public String form(@RequestParam Long itemId, Authentication auth, Model model) {
        if (userHelper.getStudent(auth) == null) return "redirect:/";
        return itemService.getById(itemId).map(item -> {
            model.addAttribute("item", item);
            model.addAttribute("reasons", ExchangeReportService.REASONS);
            return "exchange/reports";
        }).orElse("redirect:/exchange");
    }

    @PostMapping
    public String submit(@RequestParam Long itemId,
                         @RequestParam String reason,
                         @RequestParam(required = false) String description,
                         Authentication auth, RedirectAttributes ra) {
        Student student = userHelper.getStudent(auth);
        if (student == null) return "redirect:/";
        try {
            reportService.createReport(student.getId(), itemId, reason, description);
            ra.addFlashAttribute("success", "Đã gửi báo cáo. Admin sẽ xem xét.");
            return "redirect:/exchange";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/exchange/reports/new?itemId=" + itemId;
        }
    }
}