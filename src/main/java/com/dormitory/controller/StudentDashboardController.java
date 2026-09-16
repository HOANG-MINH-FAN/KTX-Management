package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.enums.ContractStatus;
import com.dormitory.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * StudentDashboardController — Dashboard cho sinh viên đăng nhập.
 */
@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentService      studentService;
    private final ContractService     contractService;
    private final InvoiceService      invoiceService;
    private final RegistrationService registrationService;
    private final ViolationService    violationService;

    public StudentDashboardController(StudentService      studentService,
                                      ContractService     contractService,
                                      InvoiceService      invoiceService,
                                      RegistrationService registrationService,
                                      ViolationService    violationService) {
        this.studentService      = studentService;
        this.contractService     = contractService;
        this.invoiceService      = invoiceService;
        this.registrationService = registrationService;
        this.violationService    = violationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Student student = studentService.findByUsername(auth.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        model.addAttribute("student", student);

        // Hợp đồng ACTIVE (nếu có)
        contractService.findActiveContractByStudentId(student.getId())
                .ifPresent(c -> model.addAttribute("activeContract", c));

        // Hóa đơn gần đây
        model.addAttribute("recentInvoices",
                invoiceService.findByStudentId(student.getId()).stream().limit(5).toList());

        // Vi phạm
        model.addAttribute("violations",
                violationService.findByStudentId(student.getId()));

        // Đơn đăng ký
        model.addAttribute("registrations",
                registrationService.findByStudentId(student.getId()));

        return "student/dashboard";
    }
}
