
package com.dormitory.controller;

import com.dormitory.entity.Contract;
import com.dormitory.entity.Student;
import com.dormitory.service.ContractService;
import com.dormitory.service.MaintenanceRequestService;
import com.dormitory.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceService;
    private final StudentService studentService;
    private final ContractService contractService;

    public MaintenanceRequestController(
            MaintenanceRequestService maintenanceService,
            StudentService studentService,
            ContractService contractService) {
        this.maintenanceService = maintenanceService;
        this.studentService = studentService;
        this.contractService = contractService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        Student student = getStudent(auth);
        if (student == null) return "redirect:/login";

        model.addAttribute("requests",
                maintenanceService.getRequestsByStudent(student.getId()));

        return "maintenance/list";
    }

    @GetMapping("/new")
    public String showForm(Authentication auth, Model model,
                           RedirectAttributes redirectAttributes) {
        Student student = getStudent(auth);
        if (student == null) return "redirect:/login";

        Contract contract = getValidContract(student.getId());

        if (contract == null || contract.getRoom() == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Bạn cần có hợp đồng còn hiệu lực để báo sửa chữa.");
            return "redirect:/maintenance";
        }

        model.addAttribute("room", contract.getRoom());
        return "maintenance/form";
    }

    @PostMapping("/new")
    public String create(Authentication auth,
                         @RequestParam String title,
                         @RequestParam String description,
                         RedirectAttributes redirectAttributes) {
        Student student = getStudent(auth);
        if (student == null) return "redirect:/login";

        try {
            Contract contract = getValidContract(student.getId());

            if (contract == null || contract.getRoom() == null) {
                throw new RuntimeException(
                        "Bạn không có hợp đồng còn hiệu lực.");
            }

            Long roomId = contract.getRoom().getId();

            maintenanceService.createRequest(
                    student.getId(), roomId, title, description);

            redirectAttributes.addFlashAttribute(
                    "success", "Gửi yêu cầu sửa chữa thành công!");

            return "redirect:/maintenance";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Không thể gửi yêu cầu: " + e.getMessage());
            return "redirect:/maintenance/new";
        }
    }

    private Student getStudent(Authentication auth) {
        if (auth == null) return null;

        return studentService.findByUsername(auth.getName())
                .orElse(null);
    }

    private Contract getValidContract(Long studentId) {
        return contractService.findActiveContractByStudentId(studentId)
                .filter(Contract::isCurrentlyValid)
                .orElse(null);
    }
}