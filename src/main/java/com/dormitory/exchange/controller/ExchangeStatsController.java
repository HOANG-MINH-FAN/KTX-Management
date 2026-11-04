package com.dormitory.exchange.controller;

import com.dormitory.exchange.service.ExchangeReportService;
import com.dormitory.exchange.service.ExchangeStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/exchange/stats")
public class ExchangeStatsController {

    private final ExchangeStatsService statsService;
    private final ExchangeReportService reportService;

    public ExchangeStatsController(ExchangeStatsService statsService, ExchangeReportService reportService) {
        this.statsService = statsService;
        this.reportService = reportService;
    }

    @GetMapping
    public String stats(Model model) {
        model.addAttribute("total", statsService.totalItems());
        model.addAttribute("active", statsService.activeItems());
        model.addAttribute("completed", statsService.completedTransactions());
        model.addAttribute("reused", statsService.reusedItems());
        model.addAttribute("pendingReports", reportService.countPending());
        return "admin/exchange/stats";
    }
}