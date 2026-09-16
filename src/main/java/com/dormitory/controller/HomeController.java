package com.dormitory.controller;

import com.dormitory.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController — Trang chủ / Dashboard.
 * Sau đăng nhập, redirect đến trang phù hợp với role.
 */
@Controller
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Trang chủ: redirect ADMIN → /admin/dashboard, STUDENT → /student/dashboard.
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/student/dashboard";
    }

    /**
     * Dashboard quản trị viên.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalRooms",               dashboardService.getTotalRooms());
        model.addAttribute("totalStudentsInDorm",      dashboardService.getTotalStudents());
        model.addAttribute("totalStudentsAll",         dashboardService.getTotalStudentsAll());
        model.addAttribute("emptyPlaces",              dashboardService.getTotalEmptyPlaces());
        model.addAttribute("activeContracts",          dashboardService.getTotalActiveContracts());
        model.addAttribute("pendingRegistrations",     dashboardService.getTotalPendingRegistrations());
        model.addAttribute("unpaidInvoices",           dashboardService.getTotalUnpaidInvoices());
        model.addAttribute("unpaidViolations",         dashboardService.getTotalUnpaidViolations());
        return "admin/dashboard";
    }
}
