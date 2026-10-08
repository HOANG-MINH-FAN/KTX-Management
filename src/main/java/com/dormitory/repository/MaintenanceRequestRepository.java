package com.dormitory.repository;

import com.dormitory.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRequestRepository
        extends JpaRepository<MaintenanceRequest, Long> {

    // Lấy yêu cầu sửa chữa của một sinh viên
    List<MaintenanceRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    // Lấy tất cả yêu cầu sửa chữa, mới nhất trước
    List<MaintenanceRequest> findAllByOrderByCreatedAtDesc();

    // Lấy yêu cầu theo trạng thái
    List<MaintenanceRequest> findByStatusOrderByCreatedAtDesc(String status);
}

