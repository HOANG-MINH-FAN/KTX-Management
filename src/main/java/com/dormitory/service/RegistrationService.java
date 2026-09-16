package com.dormitory.service;

import com.dormitory.entity.Registration;
import com.dormitory.entity.enums.RegistrationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho workflow đăng ký phòng.
 */
public interface RegistrationService {

    List<Registration> findAll();

    Optional<Registration> findById(Long id);

    /** Đăng ký phòng mới (PENDING). */
    Registration submitRegistration(Long studentId, Long roomId, String desiredStartDate, String note);

    /**
     * Duyệt đơn đăng ký → tự động tạo Contract + tăng room.occupied.
     */
    void approveRegistration(Long registrationId, String adminNote);

    /**
     * Từ chối đơn đăng ký.
     */
    void rejectRegistration(Long registrationId, String adminNote);

    /** Lấy đơn theo trạng thái. */
    List<Registration> findByStatus(RegistrationStatus status);

    /** Lấy đơn của sinh viên. */
    List<Registration> findByStudentId(Long studentId);

    /** Sinh viên có đơn PENDING không. */
    boolean hasPendingRegistration(Long studentId);
}
