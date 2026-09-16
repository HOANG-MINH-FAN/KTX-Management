package com.dormitory.entity;

import com.dormitory.entity.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITY: Registration (Đơn đăng ký phòng)
 * ============================================================
 * Sinh viên gửi đơn đăng ký → Admin duyệt/từ chối.
 *
 * Workflow nghiệp vụ:
 *   1. Sinh viên chọn phòng → tạo Registration (status=PENDING)
 *   2. Admin xem danh sách PENDING
 *   3. Admin APPROVE → ContractService tự động tạo Contract
 *      và tăng room.occupied
 *   4. Admin REJECT → ghi admin_note lý do từ chối
 *
 * Ràng buộc:
 *   - Một sinh viên chỉ có tối đa 1 đơn PENDING tại một thời điểm.
 * ============================================================
 */
@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sinh viên gửi đơn đăng ký.
     */
    @NotNull(message = "Sinh viên không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Phòng muốn đăng ký.
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * Ngày muốn bắt đầu vào ở.
     */
    @NotNull(message = "Ngày vào ở không được để trống")
    @Column(name = "desired_start_date", nullable = false)
    private LocalDate desiredStartDate;

    /**
     * Ghi chú của sinh viên khi gửi đơn.
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Trạng thái xét duyệt: PENDING | APPROVED | REJECTED.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    /**
     * Ghi chú của Admin khi duyệt hoặc từ chối đơn.
     */
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===================== Business Logic =====================

    /** Kiểm tra đơn có đang chờ duyệt không. */
    public boolean isPending()  { return RegistrationStatus.PENDING.equals(this.status); }

    /** Kiểm tra đơn đã được duyệt không. */
    public boolean isApproved() { return RegistrationStatus.APPROVED.equals(this.status); }

    /** Kiểm tra đơn đã bị từ chối không. */
    public boolean isRejected() { return RegistrationStatus.REJECTED.equals(this.status); }

    // ===================== Constructors =====================

    public Registration() {}

    public Registration(Student student, Room room, LocalDate desiredStartDate) {
        this.student          = student;
        this.room             = room;
        this.desiredStartDate = desiredStartDate;
        this.status           = RegistrationStatus.PENDING;
    }

    // ===================== Getters & Setters =====================

    public Long getId() { return id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getDesiredStartDate() { return desiredStartDate; }
    public void setDesiredStartDate(LocalDate desiredStartDate) { this.desiredStartDate = desiredStartDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", student=" + (student != null ? student.getStudentCode() : "null")
                + ", status=" + status + "}";
    }
}
