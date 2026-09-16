package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.service.ContractService;
import com.dormitory.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ContractController — Xem và quản lý hợp đồng.
 * ADMIN: Xem tất cả, kết thúc sớm hợp đồng.
 * STUDENT: Chỉ xem hợp đồng của mình.
 */
@Controller
public class ContractController {

    private final ContractService contractService;
    private final StudentService  studentService;

    public ContractController(ContractService contractService, StudentService studentService) {
        this.contractService = contractService;
        this.studentService  = studentService;
    }

    @GetMapping("/admin/contracts")
    public String adminList(Model model) {
        model.addAttribute("contracts", contractService.findAll());
        return "admin/contracts/list";
    }

    @PostMapping("/admin/contracts/terminate/{id}")
    public String terminate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            contractService.terminateContract(id);
            ra.addFlashAttribute("success", "Đã kết thúc hợp đồng và giải phóng chỗ phòng.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/contracts";
    }

    @GetMapping("/student/contracts")
    public String studentContracts(Authentication auth, Model model) {
        Student student = studentService.findByUsername(auth.getName()).orElse(null);
        if (student == null) return "redirect:/login";
        model.addAttribute("contracts", contractService.findByStudentId(student.getId()));
        return "student/contracts";
    }
}
