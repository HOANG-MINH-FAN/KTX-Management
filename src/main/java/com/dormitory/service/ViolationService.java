package com.dormitory.service;

import com.dormitory.entity.Violation;
import com.dormitory.entity.enums.ViolationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho vi phạm nội quy.
 */
public interface ViolationService {

    List<Violation> findAll();

    Optional<Violation> findById(Long id);

    List<Violation> findByStudentId(Long studentId);

    Violation save(Violation violation);

    void deleteById(Long id);

    void markAsPaid(Long violationId);

    void markAsWaived(Long violationId, String note);

    long countByStatus(ViolationStatus status);

    BigDecimal getTotalUnpaidFines();
}
