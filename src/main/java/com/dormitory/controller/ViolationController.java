package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.entity.Violation;
import com.dormitory.service.StudentService;
import com.dormitory.service.ViolationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ViolationController — Quản lý vi phạm nội quy (chỉ ADMIN).
 */
@Controller
@RequestMapping("/admin/violations")
public class ViolationController {

    private final ViolationService violationService;
    private final StudentService   studentService;

    public ViolationController(ViolationService violationService, StudentService studentService) {
        this.violationService = violationService;
        this.studentService   = studentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("violations", violationService.findAll());
        model.addAttribute("students",   studentService.findAll());
        return "admin/violations/list";
    }

    @PostMapping("/save")
    public String save(@RequestParam Long   studentId,
                       @RequestParam String description,
                       @RequestParam String violationDate,
                       @RequestParam(defaultValue = "0") BigDecimal fineAmount,
                       @RequestParam(required = false) String note,
                       RedirectAttributes ra) {

        Student student = studentService.findById(studentId).orElse(null);
        if (student == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sinh viên.");
            return "redirect:/admin/violations";
        }

        Violation v = new Violation();
        v.setStudent(student);
        v.setDescription(description);
        v.setViolationDate(LocalDate.parse(violationDate));
        v.setFineAmount(fineAmount);
        v.setNote(note);
        violationService.save(v);

        ra.addFlashAttribute("success", "Ghi nhận vi phạm thành công!");
        return "redirect:/admin/violations";
    }

    @PostMapping("/paid/{id}")
    public String markPaid(@PathVariable Long id, RedirectAttributes ra) {
        violationService.markAsPaid(id);
        ra.addFlashAttribute("success", "Đã đánh dấu đã nộp phạt.");
        return "redirect:/admin/violations";
    }

    @PostMapping("/waive/{id}")
    public String markWaived(@PathVariable Long id,
                             @RequestParam(required = false) String note,
                             RedirectAttributes ra) {
        violationService.markAsWaived(id, note);
        ra.addFlashAttribute("success", "Đã miễn phạt cho vi phạm này.");
        return "redirect:/admin/violations";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        violationService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa vi phạm.");
        return "redirect:/admin/violations";
    }
}
