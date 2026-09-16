package com.dormitory.repository;

import com.dormitory.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho InvoiceDetail (chi tiết khoản phí hóa đơn).
 */
@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    List<InvoiceDetail> findByInvoiceId(Long invoiceId);
}
