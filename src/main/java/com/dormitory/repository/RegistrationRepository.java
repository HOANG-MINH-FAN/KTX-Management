package com.dormitory.repository;

import com.dormitory.entity.Registration;
import com.dormitory.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Registration (đơn đăng ký phòng).
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Lấy đơn đăng ký theo sinh viên, sắp xếp mới nhất trước.
     */
    List<Registration> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Lấy tất cả đơn theo trạng thái.
     */
    List<Registration> findByStatusOrderByCreatedAtAsc(RegistrationStatus status);

    /**
     * Kiểm tra sinh viên có đơn PENDING không (tránh gửi nhiều đơn).
     */
    boolean existsByStudentIdAndStatus(Long studentId, RegistrationStatus status);

    /**
     * Đếm số đơn đang chờ duyệt.
     */
    long countByStatus(RegistrationStatus status);

    /**
     * Lấy tất cả đơn sắp xếp mới nhất trước.
     */
    List<Registration> findAllByOrderByCreatedAtDesc();
}
