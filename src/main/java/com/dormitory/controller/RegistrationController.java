package com.dormitory.controller;

import com.dormitory.entity.Registration;
import com.dormitory.entity.Student;
import com.dormitory.entity.enums.RegistrationStatus;
import com.dormitory.service.RegistrationService;
import com.dormitory.service.RoomService;
import com.dormitory.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * RegistrationController — Workflow đăng ký phòng.
 *
 * STUDENT: Gửi đơn đăng ký (/student/register)
 * ADMIN:   Xem và duyệt đơn (/admin/registrations)
 */
@Controller
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RoomService         roomService;
    private final StudentService      studentService;

    public RegistrationController(RegistrationService registrationService,
                                  RoomService         roomService,
                                  StudentService      studentService) {
        this.registrationService = registrationService;
        this.roomService         = roomService;
        this.studentService      = studentService;
    }

    // ===================== ADMIN =====================

    @GetMapping("/admin/registrations")
    public String adminList(Model model) {
        model.addAttribute("registrations", registrationService.findAll());
        model.addAttribute("pendingCount",
                registrationService.findByStatus(RegistrationStatus.PENDING).size());
        return "admin/registrations/list";
    }

    @PostMapping("/admin/registrations/approve/{id}")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String adminNote,
                          RedirectAttributes ra) {
        try {
            registrationService.approveRegistration(id, adminNote);
            ra.addFlashAttribute("success", "Đã duyệt đơn đăng ký và tạo hợp đồng tự động!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi duyệt: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    @PostMapping("/admin/registrations/reject/{id}")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String adminNote,
                         RedirectAttributes ra) {
        try {
            registrationService.rejectRegistration(id, adminNote);
            ra.addFlashAttribute("success", "Đã từ chối đơn đăng ký.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    // ===================== STUDENT =====================

    @GetMapping("/student/register")
    public String studentRegisterForm(Authentication auth, Model model, RedirectAttributes ra) {
        Student student = getStudentFromAuth(auth);
        if (student == null) return "redirect:/login";

        // Kiểm tra có đơn PENDING chưa
        if (registrationService.hasPendingRegistration(student.getId())) {
            ra.addFlashAttribute("warning", "Bạn đã có một đơn đăng ký đang chờ duyệt.");
            return "redirect:/student/dashboard";
        }

        model.addAttribute("availableRooms", roomService.findAvailableRooms());
        return "student/registration";
    }

    @PostMapping("/student/register")
    public String studentSubmitRegistration(@RequestParam Long   roomId,
                                            @RequestParam String desiredStartDate,
                                            @RequestParam(required = false) String note,
                                            Authentication auth,
                                            RedirectAttributes ra) {
        Student student = getStudentFromAuth(auth);
        if (student == null) return "redirect:/login";

        try {
            registrationService.submitRegistration(student.getId(), roomId, desiredStartDate, note);
            ra.addFlashAttribute("success", "Gửi đơn đăng ký thành công! Vui lòng chờ quản trị viên duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    // ===================== Helper =====================

    private Student getStudentFromAuth(Authentication auth) {
        if (auth == null) return null;
        return studentService.findByUsername(auth.getName()).orElse(null);
    }
}
