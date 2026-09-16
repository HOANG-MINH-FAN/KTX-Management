package com.dormitory.service.impl;

import com.dormitory.entity.Violation;
import com.dormitory.entity.enums.ViolationStatus;
import com.dormitory.repository.ViolationRepository;
import com.dormitory.service.ViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ViolationServiceImpl implements ViolationService {

    private final ViolationRepository violationRepository;

    public ViolationServiceImpl(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Violation> findAll() {
        return violationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Violation> findById(Long id) {
        return violationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Violation> findByStudentId(Long studentId) {
        return violationRepository.findByStudentIdOrderByViolationDateDesc(studentId);
    }

    @Override
    public Violation save(Violation violation) {
        return violationRepository.save(violation);
    }

    @Override
    public void deleteById(Long id) {
        violationRepository.deleteById(id);
    }

    @Override
    public void markAsPaid(Long violationId) {
        Violation v = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm ID: " + violationId));
        v.setStatus(ViolationStatus.PAID);
        violationRepository.save(v);
    }

    @Override
    public void markAsWaived(Long violationId, String note) {
        Violation v = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vi phạm ID: " + violationId));
        v.setStatus(ViolationStatus.WAIVED);
        if (note != null && !note.isBlank()) {
            v.setNote(note);
        }
        violationRepository.save(v);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(ViolationStatus status) {
        return violationRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidFines() {
        return violationRepository.getTotalUnpaidFines();
    }
}
