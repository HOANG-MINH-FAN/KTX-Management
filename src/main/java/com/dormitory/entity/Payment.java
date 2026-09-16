package com.dormitory.entity;

import com.dormitory.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITY: Payment (Lịch sử thanh toán)
 * ============================================================
 * Ghi nhận một lần thanh toán cho một hóa đơn.
 * Một hóa đơn có thể được thanh toán nhiều lần (nếu trả góp).
 * Khi tổng amount_paid >= invoice.total_amount → đánh dấu PAID.
 * ============================================================
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hóa đơn được thanh toán.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * Số tiền thanh toán trong lần này (VND).
     */
    @NotNull
    @DecimalMin("1.0")
    @Column(name = "amount_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;

    /**
     * Ngày thực hiện thanh toán.
     */
    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /**
     * Phương thức thanh toán: CASH | BANK_TRANSFER | MOMO | VNPAY.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    /**
     * Ghi chú bổ sung (VD: số tham chiếu giao dịch).
     */
    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Constructors =====================

    public Payment() {}

    public Payment(Invoice invoice, BigDecimal amountPaid, LocalDate paymentDate, PaymentMethod method) {
        this.invoice       = invoice;
        this.amountPaid    = amountPaid;
        this.paymentDate   = paymentDate;
        this.paymentMethod = method;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", amountPaid=" + amountPaid
                + ", date=" + paymentDate + ", method=" + paymentMethod + "}";
    }
}
