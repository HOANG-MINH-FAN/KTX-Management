package com.dormitory.entity;

import com.dormitory.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * ENTITY: Invoice (Hóa đơn tháng)
 * ============================================================
 * Mỗi hóa đơn ứng với một tháng của một hợp đồng.
 *
 * Hóa đơn gồm nhiều InvoiceDetail (chi tiết từng khoản phí).
 * Tổng tiền = tổng tất cả InvoiceDetail.amount.
 *
 * Áp dụng OOP:
 * InvoiceService dùng các lớp Fee (RoomFee, ElectricityFee,
 * WaterFee, ServiceFee, ViolationFee) để tính amount cho mỗi detail.
 *
 * Unique constraint: mỗi hợp đồng chỉ có 1 hóa đơn/tháng.
 * ============================================================
 */
@Entity
@Table(
    name = "invoices",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_invoice_contract_period",
            columnNames = {"contract_id", "period_month", "period_year"}
        )
    }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hợp đồng phát sinh hóa đơn này.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    /**
     * Tháng trong kỳ hóa đơn (1-12).
     */
    @Min(1) @Max(12)
    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    /**
     * Năm trong kỳ hóa đơn (VD: 2024).
     */
    @Min(2000)
    @Column(name = "period_year", nullable = false)
    private int periodYear;

    /**
     * Tổng số tiền phải thanh toán (VND).
     * Bằng tổng tất cả InvoiceDetail.amount.
     */
    @DecimalMin("0.0")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Trạng thái thanh toán: UNPAID | PAID | OVERDUE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    /**
     * Hạn thanh toán hóa đơn.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Danh sách chi tiết khoản phí trong hóa đơn.
     * cascade ALL + orphanRemoval: xóa hóa đơn → xóa chi tiết.
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> details = new ArrayList<>();

    /**
     * Lịch sử thanh toán cho hóa đơn này.
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Business Logic =====================

    /** Hóa đơn đã được thanh toán chưa. */
    public boolean isPaid() { return InvoiceStatus.PAID.equals(this.status); }

    /** Hóa đơn có bị quá hạn không. */
    public boolean isOverdue() { return InvoiceStatus.OVERDUE.equals(this.status); }

    /**
     * Tính lại tổng tiền từ danh sách chi tiết.
     * Gọi sau khi thêm/xóa InvoiceDetail.
     */
    public void recalculateTotal() {
        this.totalAmount = this.details.stream()
                .map(InvoiceDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lấy kỳ hóa đơn dạng "Tháng M/YYYY". */
    public String getPeriodDisplay() {
        return "Tháng " + periodMonth + "/" + periodYear;
    }

    // ===================== Constructors =====================

    public Invoice() {}

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public int getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(int periodMonth) { this.periodMonth = periodMonth; }

    public int getPeriodYear() { return periodYear; }
    public void setPeriodYear(int periodYear) { this.periodYear = periodYear; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    public List<Payment> getPayments() { return payments; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Invoice{id=" + id + ", period=" + periodMonth + "/" + periodYear
                + ", total=" + totalAmount + ", status=" + status + "}";
    }
}
