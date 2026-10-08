package com.dormitory.controller;

import com.dormitory.service.MaintenanceRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/maintenance")
public class MaintenanceAdminController {

    private final MaintenanceRequestService maintenanceService;

    public MaintenanceAdminController(
            MaintenanceRequestService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (status != null && !status.isBlank()
                && !List.of("PENDING", "IN_PROGRESS",
                            "COMPLETED", "REJECTED").contains(status)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Trạng thái lọc không hợp lệ.");
            return "redirect:/admin/maintenance";
        }

        model.addAttribute(
                "requests", maintenanceService.getRequestsByStatus(status));

        model.addAttribute(
                "selectedStatus", status == null ? "" : status);

        return "maintenance/admin-list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String adminNote,
            RedirectAttributes redirectAttributes) {

        try {
            maintenanceService.updateStatus(id, status, adminNote);
            redirectAttributes.addFlashAttribute(
                    "success", "Cập nhật yêu cầu thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Không thể cập nhật: " + e.getMessage());
        }

        return "redirect:/admin/maintenance";
    }
}

