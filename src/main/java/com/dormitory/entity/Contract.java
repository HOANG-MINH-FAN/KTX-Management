package com.dormitory.entity;

import com.dormitory.entity.enums.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * ENTITY: Contract (Hợp đồng ở)
 * ============================================================
 * Được tạo tự động khi Admin APPROVE một Registration.
 *
 * Hợp đồng ghi nhận:
 * - Sinh viên nào ở phòng nào
 * - Từ ngày nào đến ngày nào
 * - Giá phòng tại thời điểm ký (snapshot, không thay đổi dù phòng đổi giá)
 *
 * Khi Contract được tạo:
 * - room.occupied tăng 1
 * - Room status cập nhật (AVAILABLE/FULL)
 *
 * Khi Contract TERMINATED/EXPIRED:
 * - room.occupied giảm 1
 * ============================================================
 */
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sinh viên ký hợp đồng.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Phòng ở trong hợp đồng.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * Đơn đăng ký nguồn gốc (nếu có).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Registration registration;

    /**
     * Ngày bắt đầu hợp đồng.
     */
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Ngày kết thúc hợp đồng.
     */
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Giá thuê phòng mỗi tháng tại thời điểm ký hợp đồng (VND).
     * Snapshot từ room.roomFeePerMonth — không thay đổi nếu phòng đổi giá.
     */
    @NotNull
    @Column(name = "monthly_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyFee;

    /**
     * Trạng thái hợp đồng: ACTIVE | EXPIRED | TERMINATED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractStatus status = ContractStatus.ACTIVE;

    /**
     * Danh sách hóa đơn phát sinh từ hợp đồng này.
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<Invoice> invoices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===================== Business Logic =====================

    /** Hợp đồng có đang hiệu lực không. */
    public boolean isActive() { return ContractStatus.ACTIVE.equals(this.status); }

    /**
     * Kiểm tra hợp đồng có còn hiệu lực theo ngày không.
     * (Hợp đồng ACTIVE và end_date chưa qua)
     */
    public boolean isCurrentlyValid() {
        return isActive() && !LocalDate.now().isAfter(this.endDate);
    }

    // ===================== Constructors =====================

    public Contract() {}

    public Contract(Student student, Room room, LocalDate startDate, LocalDate endDate, BigDecimal monthlyFee) {
        this.student    = student;
        this.room       = room;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.monthlyFee = monthlyFee;
        this.status     = ContractStatus.ACTIVE;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Registration getRegistration() { return registration; }
    public void setRegistration(Registration registration) { this.registration = registration; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public List<Invoice> getInvoices() { return invoices; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Contract{id=" + id + ", student=" + (student != null ? student.getStudentCode() : "null")
                + ", room=" + (room != null ? room.getRoomNumber() : "null")
                + ", status=" + status + "}";
    }
}
