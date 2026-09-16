package com.dormitory.repository;

import com.dormitory.entity.Invoice;
import com.dormitory.entity.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository cho Invoice (hóa đơn).
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Lấy tất cả hóa đơn của một hợp đồng.
     */
    List<Invoice> findByContractIdOrderByPeriodYearDescPeriodMonthDesc(Long contractId);

    /**
     * Lấy hóa đơn theo sinh viên (qua contract).
     */
    @Query("SELECT i FROM Invoice i JOIN i.contract c WHERE c.student.id = :studentId ORDER BY i.periodYear DESC, i.periodMonth DESC")
    List<Invoice> findByStudentId(Long studentId);

    /**
     * Lấy hóa đơn chưa thanh toán.
     */
    List<Invoice> findByStatusOrderByDueDateAsc(InvoiceStatus status);

    /**
     * Đếm hóa đơn theo trạng thái.
     */
    long countByStatus(InvoiceStatus status);

    /**
     * Tổng nợ chưa thanh toán (UNPAID + OVERDUE).
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status IN ('UNPAID', 'OVERDUE')")
    BigDecimal getTotalUnpaidAmount();

    /**
     * Kiểm tra hóa đơn cho kỳ này đã tồn tại chưa.
     */
    boolean existsByContractIdAndPeriodMonthAndPeriodYear(Long contractId, int month, int year);
}
