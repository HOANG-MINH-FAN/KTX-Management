
package com.dormitory.controller;

import com.dormitory.entity.Invoice;
import com.dormitory.entity.Student;
import com.dormitory.entity.Contract;
import com.dormitory.service.ContractService;
import com.dormitory.service.InvoiceService;
import com.dormitory.service.StudentService;
import com.dormitory.service.RoomService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * InvoiceController — Quản lý hóa đơn.
 * ADMIN: Tạo hóa đơn, xem tất cả, ghi nhận thanh toán.
 * STUDENT: Xem hóa đơn của mình.
 */
@Controller
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ContractService contractService;
    private final StudentService studentService;
    private final RoomService roomService;

    public InvoiceController(
            InvoiceService invoiceService,
            ContractService contractService,
            StudentService studentService,
            RoomService roomService) {

        this.invoiceService = invoiceService;
        this.contractService = contractService;
        this.studentService = studentService;
        this.roomService = roomService;
    }

    // ===================== ADMIN =====================

    @GetMapping("/admin/invoices")
    public String adminList(Model model) {
        model.addAttribute("invoices",
                invoiceService.findAllInvoices());

        model.addAttribute("contracts",
                contractService.findAll());

        return "admin/invoices/list";
    }

    @GetMapping("/admin/invoices/create")
    public String createForm(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/invoices/create";
    }

    /**
     * Tao hoa don cho tat ca sinh vien trong phong.
     */
    @PostMapping("/admin/invoices/create")
    public String create(
            @RequestParam Long roomId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam int prevElec,
            @RequestParam int currElec,
            @RequestParam int prevWater,
            @RequestParam int currWater,
            @RequestParam(defaultValue = "3500") BigDecimal pricePerKwh,
            @RequestParam(defaultValue = "10000") BigDecimal pricePerM3,
            @RequestParam(defaultValue = "0") BigDecimal serviceFee,
            RedirectAttributes ra) {

        try {
            List<Invoice> invoices =
                    invoiceService.createInvoicesForRoom(
                            roomId, month, year,
                            prevElec, currElec,
                            prevWater, currWater,
                            pricePerKwh, pricePerM3,
                            serviceFee);

            ra.addFlashAttribute(
                    "success",
                    "Da tao " + invoices.size()
                            + " hoa don cho phong thanh cong!");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Loi: " + e.getMessage());
        }

        return "redirect:/admin/invoices";
    }

    @PostMapping("/admin/invoices/pay/{id}")
    public String recordPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amountPaid,
            @RequestParam(defaultValue = "CASH") String paymentMethod,
            @RequestParam(required = false) String note,
            RedirectAttributes ra) {

        try {
            invoiceService.recordPayment(
                    id, amountPaid, paymentMethod, note);

            ra.addFlashAttribute(
                    "success",
                    "Ghi nhan thanh toan thanh cong!");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Loi: " + e.getMessage());
        }

        return "redirect:/admin/invoices";
    }

    // ===================== STUDENT =====================

    @GetMapping("/student/invoices")
    public String studentInvoices(Authentication auth, Model model) {

        Student student = studentService
                .findByUsername(auth.getName())
                .orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        Contract contract = contractService
                .findActiveContractByStudentId(student.getId())
                .orElse(null);

        List<Invoice> myInvoices =
                invoiceService.findByStudentId(student.getId());

        model.addAttribute("invoices", myInvoices);

        if (contract != null) {
            List<Invoice> roomInvoices =
                    invoiceService.findUnpaidInvoicesByRoomId(
                            contract.getRoom().getId());

            model.addAttribute("roomInvoices", roomInvoices);
        } else {
            model.addAttribute("roomInvoices", List.of());
        }

        return "student/invoices";
    }
    @PostMapping("/student/invoices/checkout")
    public String checkout(
            Authentication auth,
            @RequestParam List<Long> invoiceIds,
            Model model) {

        Student student = studentService
                .findByUsername(auth.getName())
                .orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        try {
            List<Invoice> selectedInvoices =
                    invoiceService.validateStudentPayment(
                            student.getId(), invoiceIds);

            BigDecimal total = BigDecimal.ZERO;

            for (Invoice invoice : selectedInvoices) {
                total = total.add(invoice.getTotalAmount());
            }

                        model.addAttribute("selectedInvoices", selectedInvoices);
            model.addAttribute("total", total);

            // Mã thanh toán duy nhất cho mỗi lần thanh toán
            String paymentCode = "KTX-" + student.getId() + "-" + System.currentTimeMillis();
            model.addAttribute("paymentCode", paymentCode);

            // Nội dung nhúng vào QR (mô phỏng, không phải chuẩn ngân hàng)
            model.addAttribute("qrContent",
                    "KTXPAY|" + paymentCode + "|" + total.toPlainString());

            return "student/invoice-checkout";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return studentInvoices(auth, model);
        }
    }
        @PostMapping("/student/invoices/confirm-paid")
    public String confirmPaid(
            Authentication auth,
            @RequestParam("invoiceIds") List<Long> invoiceIds,
            @RequestParam("paymentCode") String paymentCode,
            RedirectAttributes ra) {

        Student student = studentService
                .findByUsername(auth.getName())
                .orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        try {
            invoiceService.confirmStudentPayment(
                    student.getId(), invoiceIds, paymentCode);

            ra.addFlashAttribute("success",
                    "Thanh toán thành công. Mã giao dịch: " + paymentCode);

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/student/invoices";
    }
}
