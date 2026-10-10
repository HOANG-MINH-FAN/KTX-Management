package com.dormitory.service.impl;

import com.dormitory.entity.*;
import com.dormitory.entity.enums.FeeType;
import com.dormitory.entity.enums.InvoiceStatus;
import com.dormitory.entity.enums.PaymentMethod;
import com.dormitory.fee.*;
import com.dormitory.repository.*;
import com.dormitory.service.InvoiceService;
import com.dormitory.service.NotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            ContractRepository contractRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService) {
        this.invoiceRepository = invoiceRepository;
        this.contractRepository = contractRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
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

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Invoice createInvoice(
            Long contractId,
            int month,
            int year,
            int prevElecReading,
            int currElecReading,
            int prevWaterReading,
            int currWaterReading,
            BigDecimal pricePerKwh,
            BigDecimal pricePerM3,
            BigDecimal serviceFeeAmount) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy hợp đồng ID: " + contractId));

        if (invoiceRepository.existsByContractIdAndPeriodMonthAndPeriodYear(
                contractId, month, year)) {
            throw new IllegalStateException(
                    "Hóa đơn tháng " + month + "/" + year
                            + " cho hợp đồng này đã tồn tại.");
        }

        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setPeriodMonth(month);
        invoice.setPeriodYear(year);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setDueDate(
                LocalDate.of(year, month, 1)
                        .plusMonths(1)
                        .withDayOfMonth(15));

        List<AbstractFee> feeItems = new ArrayList<>();

        feeItems.add(new RoomFee(contract.getMonthlyFee(), 1));

        if (currElecReading > prevElecReading) {
            feeItems.add(new ElectricityFee(
                    pricePerKwh, prevElecReading, currElecReading));
        }

        if (currWaterReading > prevWaterReading) {
            feeItems.add(new WaterFee(
                    pricePerM3, prevWaterReading, currWaterReading));
        }

        if (serviceFeeAmount != null
                && serviceFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
            feeItems.add(new ServiceFee(
                    "Phí dịch vụ tháng " + month + "/" + year,
                    serviceFeeAmount));
        }

        List<InvoiceDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (AbstractFee fee : feeItems) {
            BigDecimal feeAmount = fee.calculateFee();

            InvoiceDetail detail = new InvoiceDetail(
                    invoice,
                    FeeType.valueOf(fee.getFeeTypeName()),
                    fee.getDescription(),
                    fee.getQuantity(),
                    fee.getUnitPrice(),
                    feeAmount);

            details.add(detail);
            totalAmount = totalAmount.add(feeAmount);
        }

        invoice.setDetails(details);
        invoice.setTotalAmount(totalAmount);

        // Lưu hóa đơn
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Thông báo cho sinh viên thuộc hợp đồng
        Student student = contract.getStudent();

        if (student != null) {
            notificationService.createNotification(
                    student.getId(),
                    "INVOICE",
                    "Hóa đơn mới",
                    "Bạn có hóa đơn tháng " + month + "/" + year
                            + ". Tổng tiền: " + totalAmount + " VNĐ.",
                    "INVOICE",
                    savedInvoice.getId());
        }

        return savedInvoice;
    }

    @Override
    public void recordPayment(
            Long invoiceId,
            BigDecimal amountPaid,
            String paymentMethodStr,
            String note) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy hóa đơn ID: " + invoiceId));

        if (invoice.isPaid()) {
            throw new IllegalStateException(
                    "Hóa đơn này đã được thanh toán đầy đủ.");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(paymentMethodStr);
        } catch (IllegalArgumentException e) {
            method = PaymentMethod.CASH;
        }

        Payment payment = new Payment(
                invoice, amountPaid, LocalDate.now(), method);
        payment.setNote(note);
        paymentRepository.save(payment);

        BigDecimal totalPaid =
                paymentRepository.getTotalPaidForInvoice(invoiceId);

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

