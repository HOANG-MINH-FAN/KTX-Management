package com.dormitory.service.impl;

import com.dormitory.entity.*;
import com.dormitory.entity.enums.FeeType;
import com.dormitory.entity.enums.InvoiceStatus;
import com.dormitory.entity.enums.PaymentMethod;
import com.dormitory.fee.*;
import com.dormitory.repository.*;
import com.dormitory.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Implementation của InvoiceService.
 *
 * TÂM ĐIỂM OOP: createInvoice() sử dụng đa hình (Polymorphism)
 * qua hệ thống phí AbstractFee:
 *
 *   List<AbstractFee> feeItems = new ArrayList<>();
 *   feeItems.add(new RoomFee(...));         // Phí phòng
 *   feeItems.add(new ElectricityFee(...));  // Phí điện
 *   feeItems.add(new WaterFee(...));        // Phí nước
 *   if (hasServiceFee)
 *     feeItems.add(new ServiceFee(...));    // Phí dịch vụ
 *
 *   // Đa hình: gọi calculateFee() cho từng loại mà không cần instanceof
 *   for (AbstractFee fee : feeItems) {
 *       BigDecimal amount = fee.calculateFee(); // override khác nhau
 *       // Tạo InvoiceDetail từ fee
 *   }
 * ============================================================
 */
@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository       invoiceRepository;
    private final ContractRepository      contractRepository;
    private final PaymentRepository       paymentRepository;

    public InvoiceServiceImpl(InvoiceRepository  invoiceRepository,
                              ContractRepository contractRepository,
                              PaymentRepository  paymentRepository) {
        this.invoiceRepository  = invoiceRepository;
        this.contractRepository = contractRepository;
        this.paymentRepository  = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByContractId(Long contractId) {
        return invoiceRepository.findByContractIdOrderByPeriodYearDescPeriodMonthDesc(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByStudentId(Long studentId) {
        return invoiceRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findUnpaidInvoices() {
        return invoiceRepository.findByStatusOrderByDueDateAsc(InvoiceStatus.UNPAID);
    }

    /**
     * ============================================================
     * Tạo hóa đơn tháng — Áp dụng OOP Polymorphism.
     *
     * Quy trình:
     * 1. Tạo Invoice entity (header hóa đơn)
     * 2. Tạo các lớp Fee con (RoomFee, ElectricityFee, WaterFee...)
     * 3. Duyệt qua List<AbstractFee> → gọi calculateFee() đa hình
     * 4. Tạo InvoiceDetail từ kết quả mỗi Fee
     * 5. Tính tổng và lưu Invoice
     * ============================================================
     */
    @Override
    public Invoice createInvoice(Long contractId, int month, int year,
                                 int prevElecReading, int currElecReading,
                                 int prevWaterReading, int currWaterReading,
                                 BigDecimal pricePerKwh, BigDecimal pricePerM3,
                                 BigDecimal serviceFeeAmount) {

        // Bước 1: Kiểm tra hợp đồng
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hợp đồng ID: " + contractId));

        // Kiểm tra trùng hóa đơn
        if (invoiceRepository.existsByContractIdAndPeriodMonthAndPeriodYear(contractId, month, year)) {
            throw new IllegalStateException("Hóa đơn tháng " + month + "/" + year
                    + " cho hợp đồng này đã tồn tại.");
        }

        // Bước 2: Tạo Invoice (header)
        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setPeriodMonth(month);
        invoice.setPeriodYear(year);
        invoice.setStatus(InvoiceStatus.UNPAID);
        // Hạn thanh toán: cuối tháng kế tiếp
        invoice.setDueDate(LocalDate.of(year, month, 1).plusMonths(1).withDayOfMonth(15));

        // Bước 3: Xây dựng danh sách các khoản phí (OOP Polymorphism)
        // ============================================================
        // Đây là điểm áp dụng đa hình: code gọi fee.calculateFee()
        // mà không cần biết fee là loại nào — RoomFee, ElectricityFee, hay WaterFee.
        // ============================================================
        List<AbstractFee> feeItems = new ArrayList<>();

        // Phí phòng (1 tháng)
        feeItems.add(new RoomFee(contract.getMonthlyFee(), 1));

        // Phí điện (nếu có tiêu thụ)
        if (currElecReading > prevElecReading) {
            feeItems.add(new ElectricityFee(pricePerKwh, prevElecReading, currElecReading));
        }

        // Phí nước (nếu có tiêu thụ)
        if (currWaterReading > prevWaterReading) {
            feeItems.add(new WaterFee(pricePerM3, prevWaterReading, currWaterReading));
        }

        // Phí dịch vụ (nếu có)
        if (serviceFeeAmount != null && serviceFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
            feeItems.add(new ServiceFee("Phí dịch vụ tháng " + month + "/" + year, serviceFeeAmount));
        }

        // Bước 4: Tính phí bằng đa hình → tạo InvoiceDetail
        List<InvoiceDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (AbstractFee fee : feeItems) {
            // GỌI ĐA HÌNH: mỗi lớp con override calculateFee() khác nhau
            BigDecimal feeAmount = fee.calculateFee();

            InvoiceDetail detail = new InvoiceDetail(
                invoice,
                FeeType.valueOf(fee.getFeeTypeName()),  // Enum từ String
                fee.getDescription(),
                fee.getQuantity(),
                fee.getUnitPrice(),
                feeAmount
            );
            details.add(detail);
            totalAmount = totalAmount.add(feeAmount);
        }

        // Bước 5: Gán details và total → lưu
        invoice.setDetails(details);
        invoice.setTotalAmount(totalAmount);

        return invoiceRepository.save(invoice);
    }

    /**
     * Ghi nhận thanh toán hóa đơn.
     * Nếu tổng tiền đã trả >= totalAmount → đánh dấu PAID.
     */
    @Override
    public void recordPayment(Long invoiceId, BigDecimal amountPaid,
                              String paymentMethodStr, String note) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + invoiceId));

        if (invoice.isPaid()) {
            throw new IllegalStateException("Hóa đơn này đã được thanh toán đầy đủ.");
        }

        // Tạo bản ghi thanh toán
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(paymentMethodStr);
        } catch (IllegalArgumentException e) {
            method = PaymentMethod.CASH;
        }

        Payment payment = new Payment(invoice, amountPaid, LocalDate.now(), method);
        payment.setNote(note);
        paymentRepository.save(payment);

        // Kiểm tra tổng tiền đã trả
        BigDecimal totalPaid = paymentRepository.getTotalPaidForInvoice(invoiceId);
        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidAmount() {
        return invoiceRepository.getTotalUnpaidAmount();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }
}
