package com.dormitory.controller;

import com.dormitory.entity.Student;
import com.dormitory.service.ContractService;
import com.dormitory.service.InvoiceService;
import com.dormitory.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * InvoiceController — Quản lý hóa đơn.
 * ADMIN: Tạo hóa đơn, xem tất cả, ghi nhận thanh toán.
 * STUDENT: Xem hóa đơn của mình.
 */
@Controller
public class InvoiceController {

    private final InvoiceService  invoiceService;
    private final ContractService contractService;
    private final StudentService  studentService;

    public InvoiceController(InvoiceService  invoiceService,
                             ContractService contractService,
                             StudentService  studentService) {
        this.invoiceService  = invoiceService;
        this.contractService = contractService;
        this.studentService  = studentService;
    }

    // ===================== ADMIN =====================

    @GetMapping("/admin/invoices")
    public String adminList(Model model) {
        model.addAttribute("invoices",   invoiceService.findUnpaidInvoices());
        model.addAttribute("contracts",  contractService.findAll());
        return "admin/invoices/list";
    }

    @GetMapping("/admin/invoices/create")
    public String createForm(Model model) {
        model.addAttribute("contracts", contractService.findAll());
        return "admin/invoices/create";
    }

    /**
     * Tạo hóa đơn tháng — gọi InvoiceService.createInvoice()
     * sử dụng OOP Fee calculation.
     */
    @PostMapping("/admin/invoices/create")
    public String create(@RequestParam Long contractId,
                         @RequestParam int  month,
                         @RequestParam int  year,
                         @RequestParam int  prevElec,
                         @RequestParam int  currElec,
                         @RequestParam int  prevWater,
                         @RequestParam int  currWater,
                         @RequestParam(defaultValue = "3500") BigDecimal pricePerKwh,
                         @RequestParam(defaultValue = "10000") BigDecimal pricePerM3,
                         @RequestParam(defaultValue = "0") BigDecimal serviceFee,
                         RedirectAttributes ra) {
        try {
            invoiceService.createInvoice(contractId, month, year,
                    prevElec, currElec, prevWater, currWater,
                    pricePerKwh, pricePerM3, serviceFee);
            ra.addFlashAttribute("success", "Tạo hóa đơn tháng " + month + "/" + year + " thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/invoices";
    }

    @PostMapping("/admin/invoices/pay/{id}")
    public String recordPayment(@PathVariable Long id,
                                @RequestParam BigDecimal amountPaid,
                                @RequestParam(defaultValue = "CASH") String paymentMethod,
                                @RequestParam(required = false) String note,
                                RedirectAttributes ra) {
        try {
            invoiceService.recordPayment(id, amountPaid, paymentMethod, note);
            ra.addFlashAttribute("success", "Ghi nhận thanh toán thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/invoices";
    }

    // ===================== STUDENT =====================

    @GetMapping("/student/invoices")
    public String studentInvoices(Authentication auth, Model model) {
        Student student = studentService.findByUsername(auth.getName()).orElse(null);
        if (student == null) return "redirect:/login";
        model.addAttribute("invoices", invoiceService.findByStudentId(student.getId()));
        return "student/invoices";
    }
}
