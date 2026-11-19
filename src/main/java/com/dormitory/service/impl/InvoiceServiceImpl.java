package com.dormitory.service.impl;

import com.dormitory.entity.*;
import com.dormitory.entity.enums.FeeType;
import com.dormitory.entity.enums.InvoiceStatus;
import com.dormitory.entity.enums.PaymentMethod;
import com.dormitory.entity.enums.ContractStatus;
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
import java.util.HashSet;
import java.util.Set;
import java.math.RoundingMode;

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
    public List<Invoice> validateStudentPayment(
            Long studentId, List<Long> invoiceIds) {

        if (invoiceIds == null || invoiceIds.isEmpty()) {
            throw new IllegalArgumentException("Bạn chưa chọn hóa đơn.");
        }

        Contract contract = contractRepository
                .findByStudentIdAndStatus(studentId, ContractStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "Bạn không có hợp đồng đang hoạt động."));

        Long roomId = contract.getRoom().getId();

        List<Invoice> invoices =
                invoiceRepository.findUnpaidInvoicesByRoomId(roomId);

        Set<Long> validIds = new HashSet<>();

        for (Invoice invoice : invoices) {
            validIds.add(invoice.getId());
        }

        Set<Long> selectedIds = new HashSet<>(invoiceIds);

        if (selectedIds.size() != invoiceIds.size()) {
            throw new IllegalArgumentException(
                    "Danh sách hóa đơn có dữ liệu trùng lặp.");
        }

        for (Long id : selectedIds) {
            if (!validIds.contains(id)) {
                throw new IllegalArgumentException(
                        "Có hóa đơn không hợp lệ hoặc đã được thanh toán.");
            }
        }

        List<Invoice> result = new ArrayList<>();

        for (Invoice invoice : invoices) {
            if (selectedIds.contains(invoice.getId())) {
                result.add(invoice);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findUnpaidInvoicesByRoomId(Long roomId) {
        return invoiceRepository.findUnpaidInvoicesByRoomId(roomId);
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
public List<Invoice> createInvoicesForRoom(
        Long roomId,
        int month,
        int year,
        int prevElecReading,
        int currElecReading,
        int prevWaterReading,
        int currWaterReading,
        BigDecimal pricePerKwh,
        BigDecimal pricePerM3,
        BigDecimal serviceFeeAmount) {

    // 1. Kiem tra du lieu dau vao
    if (month < 1 || month > 12 || year < 2000) {
        throw new IllegalArgumentException("Thang hoac nam khong hop le.");
    }

    if (prevElecReading < 0 || currElecReading < prevElecReading
            || prevWaterReading < 0 || currWaterReading < prevWaterReading) {
        throw new IllegalArgumentException("Chi so dien, nuoc khong hop le.");
    }

    if (pricePerKwh == null || pricePerM3 == null
            || serviceFeeAmount == null
            || pricePerKwh.compareTo(BigDecimal.ZERO) < 0
            || pricePerM3.compareTo(BigDecimal.ZERO) < 0
            || serviceFeeAmount.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("So tien khong hop le.");
    }

    // 2. Lay tat ca hop dong ACTIVE cua phong
    List<Contract> contracts =
            contractRepository.findByRoomIdAndStatus(
                    roomId, ContractStatus.ACTIVE);

    if (contracts.isEmpty()) {
        throw new IllegalStateException(
                "Phong nay chua co sinh vien co hop dong ACTIVE.");
    }

    int numberOfStudents = contracts.size();

    // 3. Kiem tra hoa don da ton tai
    for (Contract contract : contracts) {
        if (invoiceRepository.existsByContractIdAndPeriodMonthAndPeriodYear(
                contract.getId(), month, year)) {
            throw new IllegalStateException(
                    "Da ton tai hoa don cua sinh vien "
                    + contract.getStudent().getFullName()
                    + " trong thang " + month + "/" + year);
        }
    }

    // 4. Tinh tong tien dien, nuoc cua ca phong
    AbstractFee electricity = new ElectricityFee(
            pricePerKwh, prevElecReading, currElecReading);

    AbstractFee water = new WaterFee(
            pricePerM3, prevWaterReading, currWaterReading);

    BigDecimal totalElectricity = electricity.calculateFee();
    BigDecimal totalWater = water.calculateFee();

    // 5. Chia tien dien, nuoc, dich vu cho tung sinh vien
    List<Invoice> result = new ArrayList<>();

    for (int i = 0; i < numberOfStudents; i++) {

        Contract contract = contracts.get(i);

        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setPeriodMonth(month);
        invoice.setPeriodYear(year);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setDueDate(
                LocalDate.of(year, month, 1)
                        .plusMonths(1)
                        .withDayOfMonth(15));

        List<InvoiceDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Tien phong theo hop dong rieng
        AbstractFee roomFee = new RoomFee(
                contract.getMonthlyFee(), 1);

        BigDecimal roomAmount = roomFee.calculateFee();

        details.add(new InvoiceDetail(
                invoice,
                FeeType.ROOM,
                roomFee.getDescription(),
                roomFee.getQuantity(),
                roomFee.getUnitPrice(),
                roomAmount,
                1));

        totalAmount = totalAmount.add(roomAmount);

        // Tien dien chia deu
        BigDecimal electricityShare = divideRoomFee(
                totalElectricity, numberOfStudents, i);

        if (electricityShare.compareTo(BigDecimal.ZERO) > 0) {
            details.add(new InvoiceDetail(
                invoice,
                FeeType.ELECTRICITY,
                "Chỉ số công tơ: " + prevElecReading + " → " + currElecReading,
                BigDecimal.valueOf(currElecReading - prevElecReading), // số kWh
                pricePerKwh,                                           // đơn giá
                electricityShare,                                      // phần của sinh viên
                numberOfStudents));

            totalAmount = totalAmount.add(electricityShare);
    }

        // Tien nuoc chia deu
        BigDecimal waterShare = divideRoomFee(
                totalWater, numberOfStudents, i);

        if (waterShare.compareTo(BigDecimal.ZERO) > 0) {
            details.add(new InvoiceDetail(
                invoice,
                FeeType.WATER,
                "Chỉ số đồng hồ: " + prevWaterReading + " → " + currWaterReading,
                BigDecimal.valueOf(currWaterReading - prevWaterReading), // số m³
                pricePerM3,
                waterShare,
                numberOfStudents));

            totalAmount = totalAmount.add(waterShare);
        }

        // Phi dich vu chia deu
        BigDecimal serviceShare = divideRoomFee(
                serviceFeeAmount, numberOfStudents, i);
        if (serviceShare.compareTo(BigDecimal.ZERO) > 0) {
            details.add(new InvoiceDetail(
                    invoice,
                    FeeType.SERVICE,
                    "Tháng " + month + "/" + year,
                    BigDecimal.ONE,
                    serviceFeeAmount,   // tổng cả phòng
                    serviceShare,       // phần của sinh viên
                    numberOfStudents));

            totalAmount = totalAmount.add(serviceShare);
        }

        invoice.setDetails(details);
        invoice.setTotalAmount(totalAmount);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        result.add(savedInvoice);

        // Thong bao cho tung sinh vien
        Student student = contract.getStudent();

        notificationService.createNotification(
                student.getId(),
                "INVOICE",
                "Hoa don moi",
                "Ban co hoa don thang " + month + "/" + year
                        + ". Tong tien: " + totalAmount + " VNĐ.",
                "INVOICE",
                savedInvoice.getId());
    }

    return result;
}

    /**
     * Chia tien den don vi dong.
     * Cac phan du 1 dong duoc phan bo lan luot.
     */
    private BigDecimal divideRoomFee(
            BigDecimal total, int numberOfStudents, int index) {

        long totalCents = total.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();

        long baseCents = totalCents / numberOfStudents;
        long remainder = totalCents % numberOfStudents;

        long studentCents = baseCents;

        if (index < remainder) {
            studentCents++;
        }

        return BigDecimal.valueOf(studentCents, 2);
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
    public void confirmStudentPayment(
            Long studentId, List<Long> invoiceIds, String paymentCode) {

        // Kiểm tra lại quyền: hóa đơn phải thuộc phòng của sinh viên và chưa thanh toán
        List<Invoice> invoices = validateStudentPayment(studentId, invoiceIds);

        for (Invoice invoice : invoices) {
            recordPayment(
                    invoice.getId(),
                    invoice.getTotalAmount(),                 // số tiền lấy từ DB, không nhận từ form
                    PaymentMethod.BANK_TRANSFER.name(),
                    "Thanh toán QR - Mã: " + paymentCode);
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

