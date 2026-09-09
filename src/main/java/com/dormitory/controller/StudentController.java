package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.repository.StudentRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;


@Controller
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/students")
    public String students(Model model, HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "no_access";
        }

        model.addAttribute("students", studentRepository.findAll());
        return "students";
    }

    @PostMapping("/students/save")
    public String saveStudent(@ModelAttribute Student student) {
        studentRepository.save(student);
        return "redirect:/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Integer id) {
        studentRepository.deleteById(id);
        return "redirect:/students";
    }
}