package com.dormitory.repository;

import com.dormitory.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository cho Payment (lịch sử thanh toán).
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    /**
     * Tổng tiền đã thanh toán cho một hóa đơn.
     */
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal getTotalPaidForInvoice(Long invoiceId);
}
