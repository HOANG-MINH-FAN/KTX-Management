package com.dormitory.service.impl;

import com.dormitory.entity.Violation;
import com.dormitory.entity.enums.ViolationStatus;
import com.dormitory.repository.ViolationRepository;
import com.dormitory.service.NotificationService;
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
    private final NotificationService notificationService;

    public ViolationServiceImpl(
            ViolationRepository violationRepository,
            NotificationService notificationService) {
        this.violationRepository = violationRepository;
        this.notificationService = notificationService;
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
        boolean isNew = violation.getId() == null;

        Violation saved = violationRepository.save(violation);

        // Chỉ gửi thông báo khi tạo vi phạm mới
        if (isNew && saved.getStudent() != null) {
            notificationService.createNotification(
                    saved.getStudent().getId(),
                    "VIOLATION",
                    "Thông báo vi phạm mới",
                    "Bạn có một vi phạm mới trong hệ thống."
                            + (saved.getNote() != null
                            && !saved.getNote().isBlank()
                            ? " Nội dung: " + saved.getNote() : ""),
                    "VIOLATION",
                    saved.getId()
            );
        }

        return saved;
    }

    @Override
    public void deleteById(Long id) {
        violationRepository.deleteById(id);
    }

    @Override
    public void markAsPaid(Long violationId) {
        Violation v = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy vi phạm ID: " + violationId));

        v.setStatus(ViolationStatus.PAID);
        violationRepository.save(v);

        if (v.getStudent() != null) {
            notificationService.createNotification(
                    v.getStudent().getId(),
                    "VIOLATION",
                    "Đã cập nhật trạng thái vi phạm",
                    "Vi phạm của bạn đã được ghi nhận là đã nộp phạt.",
                    "VIOLATION",
                    v.getId()
            );
        }
    }

    @Override
    public void markAsWaived(Long violationId, String note) {
        Violation v = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy vi phạm ID: " + violationId));

        v.setStatus(ViolationStatus.WAIVED);

        if (note != null && !note.isBlank()) {
            v.setNote(note);
        }

        violationRepository.save(v);

        if (v.getStudent() != null) {
            notificationService.createNotification(
                    v.getStudent().getId(),
                    "VIOLATION",
                    "Vi phạm được miễn phạt",
                    "Vi phạm của bạn đã được miễn phạt."
                            + (note != null && !note.isBlank()
                            ? " Ghi chú: " + note : ""),
                    "VIOLATION",
                    v.getId()
            );
        }
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

