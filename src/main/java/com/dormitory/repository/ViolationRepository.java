package com.dormitory.repository;

import com.dormitory.entity.Violation;
import com.dormitory.entity.enums.ViolationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository cho Violation (vi phạm nội quy).
 */
@Repository
public interface ViolationRepository extends JpaRepository<Violation, Long> {

    /**
     * Lấy vi phạm theo sinh viên, mới nhất trước.
     */
    List<Violation> findByStudentIdOrderByViolationDateDesc(Long studentId);

    /**
     * Lấy vi phạm chưa nộp phạt.
     */
    List<Violation> findByStudentIdAndStatus(Long studentId, ViolationStatus status);

    /**
     * Đếm vi phạm chưa nộp phạt theo sinh viên.
     */
    long countByStudentIdAndStatus(Long studentId, ViolationStatus status);

    /**
     * Đếm tổng vi phạm theo trạng thái (cho dashboard).
     */
    long countByStatus(ViolationStatus status);

    /**
     * Tổng tiền phạt chưa nộp.
     */
    @Query("SELECT COALESCE(SUM(v.fineAmount), 0) FROM Violation v WHERE v.status = 'UNPAID'")
    BigDecimal getTotalUnpaidFines();

    /**
     * Lấy tất cả vi phạm, mới nhất trước.
     */
    List<Violation> findAllByOrderByCreatedAtDesc();
}
