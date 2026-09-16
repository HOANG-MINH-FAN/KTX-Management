package com.dormitory.entity;

import com.dormitory.entity.enums.ViolationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITY: Violation (Vi phạm nội quy)
 * ============================================================
 * Ghi nhận hành vi vi phạm nội quy của sinh viên.
 *
 * Vi phạm có thể:
 * - Tạo khoản phạt (fine_amount > 0)
 * - Được miễn giảm (status = WAIVED)
 * - Được tích hợp vào hóa đơn qua ViolationFee (OOP)
 *
 * Thống kê vi phạm dùng cho dashboard và báo cáo.
 * ============================================================
 */
@Entity
@Table(name = "violations")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sinh viên vi phạm.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Mô tả hành vi vi phạm.
     * VD: "Gây ồn ào sau 22h", "Mang thức ăn vào phòng..."
     */
    @NotBlank(message = "Mô tả vi phạm không được để trống")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Ngày xảy ra vi phạm.
     */
    @NotNull
    @Column(name = "violation_date", nullable = false)
    private LocalDate violationDate;

    /**
     * Mức phạt tiền (VND).
     * 0 = chỉ cảnh cáo, không phạt tiền.
     */
    @DecimalMin("0.0")
    @Column(name = "fine_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    /**
     * Trạng thái nộp phạt: UNPAID | PAID | WAIVED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ViolationStatus status = ViolationStatus.UNPAID;

    /**
     * Ghi chú bổ sung của quản trị viên.
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Business Logic =====================

    /** Kiểm tra vi phạm có phạt tiền không. */
    public boolean hasFine() {
        return this.fineAmount != null && this.fineAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /** Kiểm tra vi phạm cần nộp phạt. */
    public boolean requiresPayment() {
        return hasFine() && ViolationStatus.UNPAID.equals(this.status);
    }

    // ===================== Constructors =====================

    public Violation() {}

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDate violationDate) { this.violationDate = violationDate; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    public ViolationStatus getStatus() { return status; }
    public void setStatus(ViolationStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Violation{id=" + id + ", student=" + (student != null ? student.getStudentCode() : "null")
                + ", fineAmount=" + fineAmount + ", status=" + status + "}";
    }
}
