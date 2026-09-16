package com.dormitory.service;

import com.dormitory.entity.Invoice;
import com.dormitory.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho hóa đơn và tính phí (áp dụng OOP Fee).
 */
public interface InvoiceService {

    Optional<Invoice> findById(Long id);

    /** Lấy hóa đơn của hợp đồng. */
    List<Invoice> findByContractId(Long contractId);

    /** Lấy hóa đơn của sinh viên. */
    List<Invoice> findByStudentId(Long studentId);

    /** Hóa đơn chưa thanh toán. */
    List<Invoice> findUnpaidInvoices();

    /**
     * Tạo hóa đơn tháng với OOP Fee calculation.
     *
     * @param contractId       Hợp đồng cần lập hóa đơn
     * @param month            Tháng (1-12)
     * @param year             Năm
     * @param prevElecReading  Chỉ số điện kỳ trước
     * @param currElecReading  Chỉ số điện kỳ này
     * @param prevWaterReading Chỉ số nước kỳ trước
     * @param currWaterReading Chỉ số nước kỳ này
     * @param pricePerKwh      Giá điện/kWh (VND)
     * @param pricePerM3       Giá nước/m³ (VND)
     * @param serviceFeeAmount Phí dịch vụ phát sinh (VND, 0 nếu không có)
     */
    Invoice createInvoice(Long contractId, int month, int year,
                          int prevElecReading, int currElecReading,
                          int prevWaterReading, int currWaterReading,
                          BigDecimal pricePerKwh, BigDecimal pricePerM3,
                          BigDecimal serviceFeeAmount);

    /**
     * Ghi nhận thanh toán hóa đơn. Nếu đủ tiền → status = PAID.
     */
    void recordPayment(Long invoiceId, BigDecimal amountPaid, String paymentMethod, String note);

    /** Tổng nợ chưa thanh toán. */
    BigDecimal getTotalUnpaidAmount();

    long countByStatus(InvoiceStatus status);
}
