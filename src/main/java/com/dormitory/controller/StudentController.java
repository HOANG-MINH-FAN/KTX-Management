package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.service.StudentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * StudentController — CRUD sinh viên (chỉ ADMIN).
 */
@Controller
@RequestMapping("/admin/students")
public class StudentController {

    private final StudentService  studentService;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentService studentService, PasswordEncoder passwordEncoder) {
        this.studentService  = studentService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "admin/students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        return "admin/students/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Student student = studentService.findById(id).orElse(null);
        if (student == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sinh viên.");
            return "redirect:/admin/students";
        }
        model.addAttribute("student", student);
        return "admin/students/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long   id,
                       @RequestParam String username,
                       @RequestParam(required = false) String password,
                       @RequestParam String studentCode,
                       @RequestParam String fullName,
                       @RequestParam(required = false) String phone,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String faculty,
                       @RequestParam(required = false) String gender,
                       @RequestParam(required = false) String dateOfBirth,
                       @RequestParam(required = false) String hometown,
                       RedirectAttributes ra) {

        // Kiểm tra trùng username khi tạo mới
        if (id == null && studentService.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại.");
            return "redirect:/admin/students/new";
        }

        // Kiểm tra trùng mã SV khi tạo mới
        if (id == null && studentService.existsByStudentCode(studentCode)) {
            ra.addFlashAttribute("error", "Mã sinh viên '" + studentCode + "' đã tồn tại.");
            return "redirect:/admin/students/new";
        }

        Student student = (id != null)
                ? studentService.findById(id).orElse(new Student())
                : new Student();

        student.setUsername(username);
        student.setStudentCode(studentCode);
        student.setFullName(fullName);
        student.setPhone(phone);
        student.setEmail(email);
        student.setFaculty(faculty);
        student.setGender(gender);
        student.setHometown(hometown);
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            student.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }

        // Chỉ hash password khi tạo mới hoặc admin muốn đổi
        if (password != null && !password.isBlank()) {
            student.setPassword(passwordEncoder.encode(password));
        }

        studentService.save(student);
        ra.addFlashAttribute("success", "Lưu thông tin sinh viên thành công!");
        return "redirect:/admin/students";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        studentService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa sinh viên thành công!");
        return "redirect:/admin/students";
    }
}