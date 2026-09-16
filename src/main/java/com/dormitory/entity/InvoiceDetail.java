package com.dormitory.entity;

import com.dormitory.entity.enums.FeeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * ============================================================
 * ENTITY: InvoiceDetail (Chi tiết khoản phí trong hóa đơn)
 * ============================================================
 * Mỗi InvoiceDetail tương ứng với một khoản phí trong hóa đơn,
 * được tính bởi một lớp con của AbstractFee (OOP Polymorphism):
 *
 *   fee_type = ROOM        → RoomFee.calculateFee()
 *   fee_type = ELECTRICITY → ElectricityFee.calculateFee()
 *   fee_type = WATER       → WaterFee.calculateFee()
 *   fee_type = SERVICE     → ServiceFee.calculateFee()
 *   fee_type = VIOLATION   → ViolationFee.calculateFee()
 *
 * Công thức: amount = quantity × unit_price
 * ============================================================
 */
@Entity
@Table(name = "invoice_details")
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hóa đơn chứa chi tiết này.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * Loại phí, xác định lớp Fee nào đã tính khoản này.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    /**
     * Mô tả chi tiết.
     * VD: "Tiền điện tháng 9: 150 kWh × 3,500đ/kWh"
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Số lượng (kWh, m³, tháng, đơn vị...).
     */
    @DecimalMin("0.0")
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    /**
     * Đơn giá (VND).
     */
    @DecimalMin("0.0")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /**
     * Thành tiền = quantity × unit_price.
     * Được tính và lưu khi tạo hóa đơn.
     */
    @DecimalMin("0.0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // ===================== Constructors =====================

    public InvoiceDetail() {}

    public InvoiceDetail(Invoice invoice, FeeType feeType, String description,
                          BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount) {
        this.invoice     = invoice;
        this.feeType     = feeType;
        this.description = description;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.amount      = amount;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public FeeType getFeeType() { return feeType; }
    public void setFeeType(FeeType feeType) { this.feeType = feeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "InvoiceDetail{feeType=" + feeType + ", description='" + description
                + "', amount=" + amount + "}";
    }
}
