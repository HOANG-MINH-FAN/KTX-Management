package com.dormitory.exchange.controller;

import com.dormitory.exchange.service.ExchangeReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/exchange/reports")
public class ExchangeReportAdminController {

    private final ExchangeReportService reportService;

    public ExchangeReportAdminController(ExchangeReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", reportService.getAllViews());
        model.addAttribute("pendingCount", reportService.countPending());
        return "admin/exchange/reports";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            reportService.markResolved(id);
            ra.addFlashAttribute("success", "Đã đánh dấu xử lý.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/exchange/reports";
    }

    @PostMapping("/{id}/hide-item")
    public String hideItem(@PathVariable Long id, RedirectAttributes ra) {
        try {
            reportService.hideItemAndResolve(id);
            ra.addFlashAttribute("success", "Đã ẩn tin vi phạm và đóng báo cáo.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/exchange/reports";
    }
}