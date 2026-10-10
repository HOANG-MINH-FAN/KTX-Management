package com.dormitory.controller;

import com.dormitory.entity.ContractExtensionRequest;
import com.dormitory.entity.Student;
import com.dormitory.entity.enums.RequestStatus;
import com.dormitory.service.ContractExtensionRequestService;
import com.dormitory.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class ContractExtensionRequestController {

    private final ContractExtensionRequestService requestService;
    private final StudentService studentService;

    public ContractExtensionRequestController(ContractExtensionRequestService requestService,
                                              StudentService studentService) {
        this.requestService = requestService;
        this.studentService = studentService;
    }

    // ================= STUDENT =================

    @GetMapping("/student/contract-extensions")
    public String studentList(Authentication auth, Model model) {
        Student student = studentService.findByUsername(auth.getName()).orElse(null);
        if (student == null) return "redirect:/login";
        model.addAttribute("requests", requestService.findByStudentId(student.getId()));
        return "student/contract_extensions/list";
    }

    @PostMapping("/student/contract-extensions/create")
    public String createRequest(Authentication auth,
                                @RequestParam Long contractId,
                                @RequestParam String newEndDate,
                                @RequestParam(required = false) String reason,
                                RedirectAttributes ra) {
        Student student = studentService.findByUsername(auth.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        try {
            requestService.createRequest(student.getId(), contractId, LocalDate.parse(newEndDate), reason);
            ra.addFlashAttribute("success", "Đã gửi yêu cầu gia hạn hợp đồng. Vui lòng chờ phê duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/student/contract-extensions";
    }

    // ================= ADMIN =================

    @GetMapping("/admin/contract-extensions")
    public String adminList(@RequestParam(required = false) RequestStatus status, Model model) {
        if (status != null) {
            model.addAttribute("requests", requestService.findByStatus(status));
        } else {
            model.addAttribute("requests", requestService.findAll());
        }
        model.addAttribute("statuses", RequestStatus.values());
        model.addAttribute("currentStatus", status);
        return "admin/contract_extensions/list";
    }

    @PostMapping("/admin/contract-extensions/approve/{id}")
    public String approveRequest(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            requestService.approveRequest(id, auth.getName());
            ra.addFlashAttribute("success", "Đã phê duyệt yêu cầu gia hạn hợp đồng.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/contract-extensions";
    }

    @PostMapping("/admin/contract-extensions/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam String rejectReason,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            requestService.rejectRequest(id, rejectReason, auth.getName());
            ra.addFlashAttribute("success", "Đã từ chối yêu cầu gia hạn hợp đồng.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/contract-extensions";
    }
}
